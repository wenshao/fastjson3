# JSONPatch 类参考

`JSONPatch` 实现 JSON Patch (RFC 6902)，用于对 JSON 文档进行批量修改操作。

## 类声明

```java
public class JSONPatch {
    public static String apply(String target, String patch);
}
```

## 核心方法

### apply — 应用补丁

```java
// 字符串 API
String result = JSONPatch.apply(target, patch);
```

## 支持的操作

| 操作 | 描述 | 示例 |
|------|------|------|
| `add` | 添加值 | `{"op":"add","path":"/foo","value":"bar"}` |
| `remove` | 删除值 | `{"op":"remove","path":"/foo"}` |
| `replace` | 替换值 | `{"op":"replace","path":"/foo","value":"baz"}` |
| `move` | 移动值 | `{"op":"move","from":"/foo","path":"/bar"}` |
| `copy` | 复制值 | `{"op":"copy","from":"/foo","path":"/bar"}` |
| `test` | 测试值 | `{"op":"test","path":"/foo","value":"bar"}` |

## 示例

```java
String target = """
    {"foo":"bar","baz":"qux"}
    """;
String patch = """
    [
        {"op":"add","path":"/hello","value":"world"},
        {"op":"remove","path":"/foo"},
        {"op":"replace","path":"/baz","value":"boo"}
    ]
    """;
String result = JSONPatch.apply(target, patch);
// {"baz":"boo","hello":"world"}
```

## 相关文档

- [JSON 类参考](JSON.md)
- [JSONPointer 类参考](JSONPointer.md)
