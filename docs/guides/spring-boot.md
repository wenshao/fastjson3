# Spring Boot 集成指南

fastjson3 可以轻松集成到 Spring Boot 应用中。

## 基础集成

### 添加依赖

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 配置 ObjectMapper

```java
@Configuration
public class Fastjson3Config {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableRead(ReadFeature.AllowComments)
            .enableWrite(WriteFeature.PrettyFormat)
            .build();
    }
}
```

## HTTP 消息转换

### 自定义 HttpMessageConverter

fastjson3 目前没有内置的 Spring HttpMessageConverter，需要手动实现：

```java
public class Fastjson3HttpMessageConverter
        extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper mapper;

    public Fastjson3HttpMessageConverter(ObjectMapper mapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.mapper = mapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz,
            HttpInputMessage inputMessage) throws IOException {
        byte[] bytes = inputMessage.getBody().readAllBytes();
        return mapper.readValue(bytes, clazz);
    }

    @Override
    protected void writeInternal(Object obj,
            HttpOutputMessage outputMessage) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(obj);
        outputMessage.getBody().write(bytes);
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new Fastjson3HttpMessageConverter(objectMapper));
    }
}
```

### Controller 使用

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 自动将 JSON 转为 User 对象
    @PostMapping
    public User create(@RequestBody User user) {
        // 处理用户创建
        return userService.save(user);
    }

    // 自动将 User 对象转为 JSON
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // 返回集合
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }
}
```

## 请求体验证

### 使用 JSON Schema

```java
@RestController
public class UserController {

    private static final JSONSchema USER_SCHEMA = JSONSchema.parseSchema("""
        {
            "type": "object",
            "properties": {
                "name": {"type": "string", "minLength": 1, "maxLength": 50},
                "email": {"type": "string", "format": "email"}
            },
            "required": ["name", "email"]
        }
        """);

    @PostMapping
    public ResponseEntity<?> create(@RequestBody String jsonBody) {
        // 先验证
        ValidateResult result = USER_SCHEMA.validate(jsonBody);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest()
                .body(result.getMessage());
        }

        // 验证通过，处理请求
        User user = JSON.parseObject(jsonBody, User.class);
        return ResponseEntity.ok(userService.save(user));
    }
}
```

### 自定义验证注解

```java
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonSchemaValidator.class)
public @interface ValidJsonSchema {
    String message() default "Invalid JSON";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String schema();
}

public class JsonSchemaValidator implements
        ConstraintValidator<ValidJsonSchema, String> {

    private JSONSchema schema;

    @Override
    public void initialize(ValidJsonSchema annotation) {
        this.schema = JSONSchema.parseSchema(annotation.schema());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return schema.validate(value).isSuccess();
    }
}
```

## 异常处理

### 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JSONException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(JSONException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("JSON_PARSE_ERROR", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_JSON", "请求体格式错误"));
    }

    record ErrorResponse(String code, String message) {}
}
```

## Redis 序列化

### 配置 RedisTemplate

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用 fastjson3 序列化（参见下方自定义 RedisSerializer）
        Fastjson3RedisSerializer<Object> serializer =
            new Fastjson3RedisSerializer<>(Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
```

### 自定义 RedisSerializer

```java
public class Fastjson3RedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper mapper = ObjectMapper.shared();
    private final Class<T> type;

    public Fastjson3RedisSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) return null;
        try {
            return mapper.writeValueAsBytes(t);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) return null;
        try {
            return mapper.readValue(bytes, type);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize", e);
        }
    }
}
```

## 日志输出

### JSON 格式日志

```java
@Slf4j
@Component
public class JsonLogger {

    private final ObjectMapper mapper = ObjectMapper.builder()
        .enableWrite(WriteFeature.PrettyFormat)
        .build();

    public void logRequest(Object request) {
        try {
            String json = mapper.writeValueAsString(request);
            log.info("Request: {}", json);
        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
    }

    public void logResponse(Object response) {
        try {
            String json = mapper.writeValueAsString(response);
            log.info("Response: {}", json);
        } catch (Exception e) {
            log.error("Failed to log response", e);
        }
    }
}
```

## 配置文件读取

### 读取 application.json

```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private String name;
    private DatabaseConfig database;

    // getters & setters

    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        // getters & setters
    }
}

// 或直接读取 JSON 配置
@Component
public class JsonConfigLoader {

    @Value("classpath:config.json")
    private Resource configResource;

    public <T> T loadConfig(Class<T> type) {
        try (InputStream is = configResource.getInputStream()) {
            return ObjectMapper.shared().readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }
}
```

## 最佳实践

### 1. 单例 ObjectMapper

```java
@Configuration
public class Config {
    @Bean
    public ObjectMapper objectMapper() {
        // 配置一次，全局使用
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .build();
    }
}
```

### 2. 分环境配置

```java
@Profile("prod")
@Configuration
public class ProdConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .readerCreator(ObjectReaderCreatorASM::createObjectReader)
            .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
            .enableWrite(WriteFeature.OptimizedForAscii)
            .build();
    }
}

@Profile("dev")
@Configuration
public class DevConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableWrite(WriteFeature.PrettyFormat)
            .enableRead(ReadFeature.AllowComments)
            .build();
    }
}
```

### 3. 安全配置

```java
@Configuration
public class SecurityConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            // 生产环境禁用 AutoType
            .disableRead(ReadFeature.SupportAutoType)
            // 严格模式
            .enableRead(ReadFeature.ErrorOnUnknownProperties)
            .build();
    }
}
```

## 完整示例

```java
// User.java
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    private Long id;
    private String name;
    private String email;
    // getters & setters
}

// UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user) {
        return ResponseEntity.ok(userService.save(user));
    }
}

// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {

    private static final TypeToken<List<User>> USERS_TYPE =
        new TypeToken<List<User>>() {};

    private final ObjectMapper mapper;
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
```

## 相关文档

- 📖 [POJO 序列化 →](pojo.md)
- 📖 [性能调优 →](performance.md)
- 🔧 [自定义序列化 →](../advanced/custom-serializer.md)
