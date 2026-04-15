# 迁移检查清单

使用此清单确保从其他 JSON 库完整迁移到 fastjson3。

## 📦 依赖替换

- [ ] 移除旧库依赖
- [ ] 添加 fastjson3 依赖
- [ ] 更新版本号
- [ ] 清理并重新构建项目

## 📝 代码更新

- [ ] 更新 import 语句
  - [ ] `com.alibaba.fastjson2.*` → `com.alibaba.fastjson3.*`
  - [ ] `com.fasterxml.jackson.*` → `com.alibaba.fastjson3.*`
  - [ ] `com.google.gson.*` → `com.alibaba.fastjson3.*`
  - [ ] `org.json.*` → `com.alibaba.fastjson3.*`

- [ ] 更新注解（如果需要）
  - [ ] `@SerializedName` → `@JSONField(name)`
  - [ ] `@Expose` → `@JSONField(serialize)`
  - [ ] 保留 Jackson 注解（fastjson3 原生支持）

- [ ] 更新 API 调用
  - [ ] `new ObjectMapper()` → `ObjectMapper.shared()` 或 `ObjectMapper.builder().build()`
  - [ ] `writeValue()` → `writeValueAsString()`
  - [ ] `gson.fromJson()` → `mapper.readValue()`
  - [ ] `new JSONObject(json)` → `JSON.parseObject(json)`
  - [ ] `length()` → `size()`
  - [ ] `has()` → `containsKey()`

## ⚙️ 配置更新

- [ ] 更新 ObjectMapper 配置
  - [ ] 命名策略
  - [ ] 日期格式
  - [ ] 序列化特性
  - [ ] 反序列化特性

- [ ] 更新 Spring Boot 配置（如适用）
  - [ ] 添加 `@Configuration` 类
  - [ ] 配置 ObjectMapper Bean
  - [ ] 移除 Jackson 自动配置

## 🧪 测试

### 单元测试

- [ ] 所有单元测试通过
- [ ] 测试序列化功能
- [ ] 测试反序列化功能
- [ ] 测试边界情况（null 值、空对象、大数组等）

### 集成测试

- [ ] API 接口测试通过
- [ ] 数据库序列化测试
- [ ] 缓存序列化测试
- [ ] 消息队列序列化测试

### 性能测试

- [ ] 序列化性能基准测试
- [ ] 反序列化性能基准测试
- [ ] 内存使用测试
- [ ] 并发测试

### 特定场景测试

- [ ] 日期/时间处理
- [ ] 泛型类型处理
- [ ] 循环引用处理
- [ ] 枚举类型处理
- [ ] 自定义序列化器

## 🚀 部署

### 预发布

- [ ] 本地环境验证
- [ ] 开发环境验证
- [ ] 测试环境验证
- [ ] 预发布环境验证

### 生产发布

- [ ] 灰度发布（建议）
- [ ] 监控错误日志
- [ ] 监控性能指标
- [ ] 监控 JVM 指标
- [ ] 准备回滚方案

## 🔍 验收标准

### 功能验收

- [ ] 所有现有功能正常工作
- [ ] 错误处理符合预期
- [ ] 日志输出正常

### 性能验收

- [ ] 序列化性能不低于原库
- [ ] 反序列化性能不低于原库
- [ ] 内存使用无明显增加

### 兼容性验收

- [ ] 现有 JSON 数据可正常解析
- [ ] 输出 JSON 格式符合预期
- [ ] 第三方系统兼容

## 📋 回滚计划

如果迁移出现问题：

- [ ] 准备回滚脚本
- [ ] 保留原库依赖配置（注释状态）
- [ ] 保存原代码分支
- [ ] 记录回滚步骤

## 🎯 迁移后优化

迁移完成后，可以考虑以下优化：

- [ ] 保持默认配置（JVM 上 AUTO provider 自动走 ASM 路径，Path B 后全面超过 fj2 2.0.61）
- [ ] 配置 TypeToken 缓存
- [ ] 使用 byte[] 替代 String 处理 UTF-8 数据
- [ ] 预编译 JSONPath 表达式
- [ ] 配置对象池（如适用）

## 📞 获取帮助

如遇到问题：

1. 查看 [常见问题](faq.md)
2. 查看 [API 文档](../api/)
3. 提交 Issue 到 GitHub

---

迁移愉快！🎉
