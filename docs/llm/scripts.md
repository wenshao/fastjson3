# 迁移脚本模板

本文档提供迁移脚本的模板，可用于自动化迁移或辅助 LLM 生成迁移代码。

---

## 批量替换脚本

### Jackson → fastjson3

```bash
#!/bin/bash
# Jackson 2.x/3.x → fastjson3 批量替换脚本

echo "开始迁移 Jackson 到 fastjson3..."

# 1. 更新 Maven 依赖
echo "更新 Maven 依赖..."
sed -i 's/<dependency>/\n<dependency>/g' pom.xml
sed -i 's/<groupId>com.fasterxml.jackson<\/groupId>/<groupId>com.alibaba.fastjson3<\/groupId>/g' pom.xml
sed -i 's/<artifactId>jackson-databind<\/artifactId>/<artifactId>fastjson3<\/artifactId>/g' pom.xml

# 2. 替换导入
echo "替换导入..."
find src -name "*.java" -type f -exec sed -i 's/import com\.fasterxml\.jackson\./import com.alibaba.fastjson3./g' {} +
find src -name "*.java" -type f -exec sed -i 's/import com\.fasterxml\.jackson\.annotation\./import com.alibaba.fastjson3.annotation./g' {} +
find src -name "*.java" -type f -exec sed -i 's/import com\.fasterxml\.jackson\.core\./import com.alibaba.fastjson3./g' {} +

# 3. 替换类名（如需要）
# find src -name "*.java" -type f -exec sed -i 's/ObjectMapper/ObjectMapper/g' {} +

# 4. 删除未使用的导入
echo "清理未使用的导入..."
# 需要使用 IDE 或工具手动清理

echo "Jackson → fastjson3 迁移完成！"
echo "请检查并验证："
echo "1. 编译项目"
echo "2. 运行测试"
echo "3. 检查 ObjectMapper 创建方式"
```

### fastjson 1.x → fastjson3

```bash
#!/bin/bash
# fastjson 1.x → fastjson3 批量替换脚本

echo "开始迁移 fastjson 1.x 到 fastjson3..."

# 1. 更新 Maven 依赖
echo "更新 Maven 依赖..."
sed -i 's/<groupId>com.alibaba<\/groupId>/<groupId>com.alibaba.fastjson3<\/groupId>/g' pom.xml
sed -i 's/<artifactId>fastjson<\/artifactId>/<artifactId>fastjson3<\/artifactId>/g' pom.xml

# 2. 替换导入
echo "替换导入..."
find src -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson\./com.alibaba.fastjson3./g' {} +
find src -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson\.annotation\./com.alibaba.fastjson3.annotation./g' {} +
find src -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson\.serializer\./com.alibaba.fastjson3./g' {} +

# 3. 替换 Feature 枚举
echo "替换 Feature 枚举..."
find src -name "*.java" -type f -exec sed -i 's/SerializerFeature/WriteFeature/g' {} +
find src -name "*.java" -type f -exec sed -i 's/Feature\.ErrorOnUnknownProperties/ReadFeature.ErrorOnUnknownProperties/g' {} +
find src -name "*.java" -type f -exec sed -i 's/Feature\.AllowComments/ReadFeature.AllowComments/g' {} +

# 4. 替换方法名
find src -name "*.java" -type f -exec sed -i 's/\.getInteger(/\.getIntValue(/g' {} +
find src -name "*.java" -type f -exec sed -i 's/\.getLong(/\.getLongValue(/g' {} +
find src -name "*.java" -type f -exec sed -i 's/\.getBoolean(/\.getBooleanValue(/g' {} +
find src -name "*.java" -type f -exec sed -i 's/\.getDouble(/\.getDoubleValue(/g' {} +
find src -name "*.java" -type f -exec sed -i 's/\.getFloat(/\.getFloatValue(/g' {} +
find src -name "*.java" -type f -exec sed -i 's/\.getByte(/\.getByteValue(/g' {} +

echo "fastjson 1.x → fastjson3 迁移完成！"
echo "请检查并验证："
echo "1. 编译项目"
echo "2. 运行测试"
echo "3. 检查日期类型是否需要改为 Java Time API"
```

### Gson → fastjson3

```bash
#!/bin/bash
# Gson → fastjson3 批量替换脚本

echo "开始迁移 Gson 到 fastjson3..."

# 1. 更新 Maven 依赖
echo "更新 Maven 依赖..."
sed -i 's/<groupId>com.google.code.gson<\/groupId>/<groupId>com.alibaba.fastjson3<\/groupId>/g' pom.xml
sed -i 's/<artifactId>gson<\/artifactId>/<artifactId>fastjson3<\/artifactId>/g' pom.xml

# 2. 替换导入
echo "替换导入..."
find src -name "*.java" -type f -exec sed -i 's/import com\.google\.gson\./import com.alibaba.fastjson3./g' {} +
find src -name "*.java" -type f -exec sed -i 's/import com\.google\.gson\.reflect\./import com.alibaba.fastjson3./g' {} +
find src -name "*.java" -type f -exec sed -i 's/import com\.google\.gson\.annotations\./import com.alibaba.fastjson3.annotation./g' {} +

# 3. 替换类名
echo "替换类名..."
find src -name "*.java" -type f -exec sed -i 's/Gson/ObjectMapper/g' {} +
find src -name "*.java" -type f -exec sed -i 's/JsonElement/JSON/g' {} +
find src -name "*.java" -type f -exec sed -i 's/JsonObject/JSONObject/g' {} +
find src -name "*.java" -type f -exec sed -i 's/JsonArray/JSONArray/g' {} +
find src -name "*.java" -type f -exec sed -i 's/JsonParser/JSONReader/g' {} +

# 4. 替换注解
echo "替换注解..."
find src -name "*.java" -type f -exec sed -i 's/@SerializedName/@JSONField(name/g' {} +
find src -name "*.java" -type f -exec sed -i 's/@Expose/@JSONField/g' {} +

echo "Gson → fastjson3 迁移完成！"
echo "请检查并验证："
echo "1. 编译项目"
echo "2. 运行测试"
echo "3. 手动检查 GsonBuilder 用法"
```

---

## Maven 依赖替换模板

### Jackson → fastjson3

```xml
<!-- 原依赖 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.x.x</version>
</dependency>

<!-- 新依赖 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### fastjson 1.x → fastjson3

```xml
<!-- 原依赖 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.x</version>
</dependency>

<!-- 新依赖 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Gson → fastjson3

```xml
<!-- 原依赖 -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.x.x</version>
</dependency>

<!-- 新依赖 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Gradle 依赖替换模板

### Jackson → fastjson3

```groovy
// 原依赖
implementation 'com.fasterxml.jackson.core:jackson-databind:2.x.x'

// 新依赖
implementation 'com.alibaba.fastjson3:fastjson3:3.0.0'
```

### fastjson 1.x → fastjson3

```groovy
// 原依赖
implementation 'com.alibaba:fastjson:1.2.x'

// 新依赖
implementation 'com.alibaba.fastjson3:fastjson3:3.0.0'
```

### Gson → fastjson3

```groovy
// 原依赖
implementation 'com.google.code.gson:gson:2.x.x'

// 新依赖
implementation 'com.alibaba.fastjson3:fastjson3:3.0.0'
```

---

## 代码转换模板

### ObjectMapper 创建

```java
// ===== 转换前 (Jackson 2.x) =====
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

// ===== 转换后 (fastjson3) =====
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

### JSON 解析

```java
// ===== 转换前 (Jackson) =====
User user = mapper.readValue(json, User.class);
List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});

// ===== 转换后 (fastjson3) =====
User user = mapper.readValue(json, User.class);
List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
```

### JSON 序列化

```java
// ===== 转换前 (Jackson) =====
String json = mapper.writeValueAsString(user);

// ===== 转换后 (fastjson3) =====
String json = mapper.writeValueAsString(user);
```

### 泛型处理

```java
// ===== 转换前 (Jackson/Gson/fastjson1) =====
List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});

// ===== 转换后 (fastjson3) =====
List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
```

---

## Spring Boot 配置模板

### Jackson → fastjson3

```java
@Configuration
public class Fastjson3Config {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 日期格式请使用 @JSONField(format = "yyyy-MM-dd HH:mm:ss") 注解
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)
            .build();
    }
}
```

### fastjson2 → fastjson3

```java
@Configuration
public class Fastjson3Config {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // fastjson2 → fastjson3 主要替换包名
        // API 基本相同
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .build();
    }
}
```

---

## 验证脚本

```bash
#!/bin/bash
# 迁移验证脚本

echo "验证 fastjson3 迁移..."

# 1. 编译检查
echo "1. 编译检查..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误"
    exit 1
fi

# 2. 运行测试
echo "2. 运行测试..."
mvn test
if [ $? -ne 0 ]; then
    echo "测试失败，请检查错误"
    exit 1
fi

# 3. 检查残留引用
echo "3. 检查残留引用..."
echo "Jackson 残留:"
grep -r "com.fasterxml.jackson" src/
echo "fastjson1 残留:"
grep -r "com.alibaba.fastjson[^3]" src/
echo "Gson 残留:"
grep -r "com.google.gson" src/

# 4. 统计迁移量
echo "4. 迁移统计..."
echo "fastjson3 引用数:"
grep -r "com.alibaba.fastjson3" src/ | wc -l

echo "验证完成！"
```

---

## IDE 宏脚本

### IntelliJ IDEA 结构化搜索替换

```
搜索: $objectManager$\.configure\((\w+)\.(\w+),\s*(true|false)\)
替换: ObjectMapper.builder().$2$3($1.$2).build()
作用范围: 项目范围
文件类型: Java
```

### VS Code 正则替换

```
查找: new ObjectMapper\(\)
替换: ObjectMapper.shared().build()
作用范围: 当前文件
```

---

## LLM 辅助迁移提示词

### 通用迁移提示词

```
请将以下 {来源库} 代码迁移到 fastjson3：

{代码片段}

迁移要求：
1. 更新导入为 com.alibaba.fastjson3.*
2. 使用 ObjectMapper.builder() 创建配置
3. 保留原注解（fastjson3 原生支持 {来源库} 注解）
4. 使用 TypeToken 处理泛型：TypeToken.listOf(Class)
5. 确保代码符合 Java 21+ 规范

参考资料：
- API 映射表：docs/llm/api-mapping.md
- 代码模式库：docs/patterns/README.md
```

### 批量迁移提示词

```
请帮我批量迁移 {来源库} 项目到 fastjson3：

1. 生成批量替换脚本
2. 更新 pom.xml 依赖
3. 替换导入语句
4. 替换 Feature 枚举
5. 更新 ObjectMapper 创建方式

使用以下参考资料：
- docs/llm/scripts.md 中的脚本模板
- docs/llm/api-mapping.md 中的 API 映射表
```

---

## 相关文档

- [代码模式库](../patterns/README.md)
- [API 映射表](api-mapping.md)
