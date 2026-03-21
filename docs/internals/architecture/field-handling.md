# 字段处理架构

FieldReader 和 FieldWriter 的设计。

## FieldReader (反序列化)

### 结构

```
FieldReader
├── typeTag (int)           // 类型标签
├── fieldName (String)      // 字段名
├── fieldNameHeader (byte[]) // 预编码 "fieldName": 字节数组
├── offset (long)           // Unsafe 偏移量
└── readXxx() 方法
```

### Type Tag 分派

```java
public final void readField(JSONParser parser, Object bean) {
    switch (typeTag) {  // 编译为跳转表
        case TypeTags.STRING -> readString(parser, bean);
        case TypeTags.INT -> readInt(parser, bean);
        case TypeTags.LONG -> readLong(parser, bean);
        default -> throw new IllegalStateException();
    }
}
```

### 为什么用 Type Tag？

#### 避免 Megamorphic Callsite

```java
// ❌ 多态：超过 3 种类型退化为间接跳转
interface FieldReader {
    void read(JSONParser parser, Object bean);
}
// StringFieldReader, IntFieldReader, LongFieldReader, BooleanFieldReader...
// JIT: >3 种类型 → 无法内联

// ✅ Type Tag：单态调用点
public final void readField(JSONParser parser, Object bean) {
    switch (typeTag) {
        case TYPE_STRING -> readString(...);  // 可内联
        case TYPE_INT -> readInt(...);        // 可内联
    }
}
```

## FieldWriter (序列化)

### 结构

```
FieldWriter
├── typeTag (int)
├── nameByteLongs (long[])  // 预编码字段名
├── nameBytes (byte[])      // 预编码字段名
└── writeXxx() 方法
```

### 预编码字段名

```java
// 初始化时编码
static final long[] NAME_USER_NAME = {
    0x22757365724e616dL,  // "userNam
    0x65223a0000000000L,  // e":\0\0\0\0\0\0
};

// 写入时
public void writeField(JSONGenerator gen, Object bean) {
    gen.writeNameLongs(NAME_USER_NAME, 11);
    gen.writeString(getString(bean, offset));
}
```

## 性能优化

### 1. Unsafe 直接访问

```java
// ❌ 反射
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);
String value = (String) field.get(bean);

// ✅ Unsafe
long offset = UNSAFE.objectFieldOffset(field);
String value = UNSAFE.getString(bean, offset);
```

### 2. 字段名预编码

```java
// ❌ 每次编码
gen.writeFieldName("userName");

// ✅ 预编码
static final long[] NAME_LONGS = encodeFieldName("userName");
gen.writeNameLongs(NAME_LONGS, NAME_LEN);
```

### 3. 类型特化方法

```java
// 为每种类型生成专门方法
void write_userName(JSONGenerator gen, Object bean) {
    gen.writeString(((User) bean).name);  // 无类型检查
}

void write_userAge(JSONGenerator gen, Object bean) {
    gen.writeInt(((User) bean).age);  // 无类型检查
}
```

## 相关文档

- [Type Tag 分派](../optimization/type-tag-dispatch.md)
- [字段预编码](../optimization/field-pre-encoding.md)
- [字段名匹配](field-matcher.md)

[← 返回索引](README.md)
