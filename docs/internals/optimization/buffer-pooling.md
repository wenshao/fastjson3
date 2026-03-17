# 缓冲区池化

**性能提升**：~3% | **难度**：⭐⭐ | **适用场景**：IO 操作

## 问题描述

频繁分配缓冲区导致 GC 压力：

```java
// ❌ 每次分配新缓冲区
byte[] buffer = new byte[8192];
// 使用后丢弃，等待 GC 回收
```

**问题**：
- 大对象分配开销大
- GC 扫描和回收耗时
- 内存碎片增加

## 解决方案：线程本地池

```java
// ✅ 复用缓冲区
static final ThreadLocal<byte[]> BUFFER_POOL =
    ThreadLocal.withInitial(() -> new byte[8192]);

byte[] buffer = BUFFER_POOL.get();
// 使用后不归还，下次线程复用
```

## 工作原理

### ThreadLocal 复用

```java
public class BufferPool {
    private static final int BUFFER_SIZE = 8192;

    private static final ThreadLocal<byte[]> POOL =
        ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);

    public static byte[] get() {
        return POOL.get();  // 每个线程独立实例
    }

    // 不需要归还！线程结束后自动回收
}
```

### 为什么不归还？

```java
// ❌ 不需要归还
byte[] buffer = POOL.get();
try {
    // 使用 buffer
} finally {
    POOL.set(buffer);  // 不需要！
}

// ✅ 直接使用
byte[] buffer = POOL.get();
// 使用 buffer
// ThreadLocal 会保持引用，下次直接复用
```

## 完整实现

```java
public class ThreadLocalBuffer {
    private static final int DEFAULT_SIZE = 8192;

    private static final ThreadLocal<byte[]> BUFFER =
        ThreadLocal.withInitial(() -> new byte[DEFAULT_SIZE]);

    public static byte[] get() {
        return BUFFER.get();
    }

    public static byte[] get(int minSize) {
        byte[] buf = BUFFER.get();
        if (buf.length < minSize) {
            // 需要更大的缓冲区，分配新的
            buf = new byte[nextPowerOfTwo(minSize)];
            BUFFER.set(buf);
        }
        return buf;
    }

    private static int nextPowerOfTwo(int n) {
        int highest = Integer.highestOneBit(n);
        return (highest == n) ? n : highest << 1;
    }
}
```

## 使用示例

### 序列化场景

```java
public class JSONSerializer {
    public byte[] serialize(Object obj) {
        byte[] buf = ThreadLocalBuffer.get(4096);  // 预估大小
        int count = 0;

        // 写入到 buf
        count = writeObject(obj, buf, 0);

        // 如果不够，扩容
        if (count > buf.length) {
            buf = new byte[count];
            writeObject(obj, buf, 0);
        }

        // 复制结果
        byte[] result = Arrays.copyOf(buf, count);
        return result;
    }
}
```

### 反序列化场景

```java
public class JSONDeserializer {
    public <T> T deserialize(byte[] json, Class<T> type) {
        byte[] buf = ThreadLocalBuffer.get();
        // 使用 buf 作为工作缓冲区
        // ...
        return result;
    }
}
```

## 性能分析

### 内存分配对比

```java
// 每次分配：8192 字节
// 每秒调用：10000 次
// 每秒分配：~80 MB
// GC 压力：大

// ThreadLocal 复用：
// 每线程分配：8192 字节（仅一次）
// 每秒调用：10000 次
// 每秒分配：0
// GC 压力：小
```

### 适用场景

| 场景 | 适用性 | 说明 |
|------|--------|------|
| 单线程高频使用 | ✅ 最佳 | 无竞争，复用率高 |
| 多线程中频使用 | ✅ 适用 | 每线程独立，无锁 |
| 多线程低频使用 | ⚠️ 谨慎 | 内存占用 vs GC 压力权衡 |
| 超大缓冲区（>1MB） | ❌ 不适用 | 线程数×内存 可能过大 |

## 进阶优化

### 分级池

```java
public class GradedBufferPool {
    private static final ThreadLocal<byte[]> SMALL =
        ThreadLocal.withInitial(() -> new byte[1024]);
    private static final ThreadLocal<byte[]> MEDIUM =
        ThreadLocal.withInitial(() -> new byte[8192]);
    private static final ThreadLocal<byte[]> LARGE =
        ThreadLocal.withInitial(() -> new byte[65536]);

    public static byte[] get(int size) {
        if (size <= 1024) return SMALL.get();
        if (size <= 8192) return MEDIUM.get();
        return LARGE.get();
    }
}
```

### 对象池

```java
public class ObjectPool<T> {
    private final ThreadLocal<Stack<T>> pool;
    private final Supplier<T> factory;

    public ObjectPool(Supplier<T> factory) {
        this.factory = factory;
        this.pool = ThreadLocal.withInitial(Stack::new);
    }

    public T get() {
        Stack<T> stack = pool.get();
        if (stack.isEmpty()) {
            return factory.get();
        }
        return stack.pop();
    }

    public void release(T obj) {
        Stack<T> stack = pool.get();
        // 限制池大小，避免内存泄漏
        if (stack.size() < 16) {
            stack.push(obj);
        }
    }
}
```

## 注意事项

### 内存泄漏

```java
// ❌ 潜在泄漏：大缓冲区长期持有
private static final ThreadLocal<byte[]> BIG_BUFFER =
    ThreadLocal.withInitial(() -> new byte[1024 * 1024]);

// ✅ 定期清理或使用弱引用
private static final ThreadLocal<byte[]> BUFFER =
    ThreadLocal.withInitial(() -> new byte[8192]);

public static void cleanup() {
    BUFFER.remove();  // 显式清理
}
```

### 线程池场景

```java
// 线程池中线程会复用，缓冲区不会释放
ExecutorService executor = Executors.newFixedThreadPool(10);

// ✅ 使用后清理
executor.submit(() -> {
    byte[] buf = ThreadLocalBuffer.get();
    try {
        // 使用 buf
    } finally {
        // 清理缓冲区（可选，防止敏感数据残留）
        Arrays.fill(buf, (byte) 0);
    }
});
```

## 你可以学到什么

1. **减少分配** - 复用大对象
2. **线程安全** - ThreadLocal 避免竞争
3. **权衡** - 内存 vs GC 压力
4. **适用场景** - 高频、小对象、单线程

## 参考资料

- [ThreadLocal 详解](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html)
- [对象池化最佳实践](https://netty.io/wiki/object-performance.html)

## 相关优化

- [Unsafe 直接操作](unsafe-memory.md) - 常结合缓冲区使用
- [融合容量检查](fused-capacity-check.md) - 减少扩容次数

[← 返回索引](README.md)
