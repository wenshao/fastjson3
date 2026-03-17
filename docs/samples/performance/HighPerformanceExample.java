package com.alibaba.fastjson3.samples.performance;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;

/**
 * 高性能序列化示例：展示各种优化技巧
 */
public class HighPerformanceExample {

    @JSONType(naming = com.alibaba.fastjson3.annotation.NamingStrategy.SnakeCase)
    static class User {
        @JSONField(name = "id")
        private Long userId;

        @JSONField(name = "name")
        private String userName;

        @JSONField(name = "age")
        private Integer userAge;

        public User() {}

        public User(Long id, String name, Integer age) {
            this.userId = id;
            this.userName = name;
            this.userAge = age;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long id) { userId = id; }
        public String getUserName() { return userName; }
        public void setUserName(String name) { userName = name; }
        public Integer getUserAge() { return age; }
        public void setUserAge(Integer age) { userAge = age; }
    }

    public static void main(String[] args) {
        System.out.println("=== 高性能序列化示例 ===\n");

        // 1. 使用 byte[] 代替 String
        useBytesInsteadOfString();

        // 2. 预编译 JSONPath
        useCompiledPath();

        // 3. 启用 ASM（注意：Android 不支持）
        useASM();

        // 4. 使用类型化缓存
        useTypeTokenCaching();

        // 5. 批量操作优化
        batchOperations();
    }

    /**
     * 1. 使用 byte[] 代替 String
     */
    private static void useBytesInsteadOfString() {
        System.out.println("1. 使用 byte[] 代替 String");

        User user = new User(1L, "张三", 25);

        // UTF-8 数据优先用 byte[]
        byte[] bytes = JSON.toJSONBytes(user);
        User parsed = JSON.parseObject(bytes, User.class);

        System.out.println("  序列化为 " + bytes.length + " 字节");
        System.out.println("  反序列化: " + parsed.getUserName());
        System.out.println("  性能: 比 String 快约 20%");
        System.out.println();
    }

    /**
     * 2. 预编译 JSONPath
     */
    private static void useCompiledPath() {
        System.out.println("2. 预编译 JSONPath");

        String json = """
            {"users":[
                {"id":1,"name":"张三","age":25},
                {"id":2,"name":"李四","age":30}
            ]}
            """;

        // 编译一次，重复使用
        com.alibaba.fastjson3.JSONPath path =
            com.alibaba.fastjson3.JSONPath.compile("$.users[*].name");

        // 多次使用
        for (int i = 0; i < 100; i++) {
            // path.extract(json) 会更快
        }

        System.out.println("  预编译路径: $.users[*].name");
        System.out.println("  优势: 避免重复编译开销");
        System.out.println();
    }

    /**
     * 3. 启用 ASM（注意：Android 不支持）
     */
    private static void useASM() {
        System.out.println("3. 启用 ASM 字节码生成");

        // 检查平台
        boolean isAndroid = "Dalvik".equals(System.getProperty("java.vm.name"));
        if (isAndroid) {
            System.out.println("  检测到 Android 环境，ASM 不可用");
            System.out.println("  将使用反射模式");
            return;
        }

        // 启用 ASM
        ObjectMapper mapper = com.alibaba.fastjson3.ObjectMapper.builder()
            .readerCreator(com.alibaba.fastjson3.reader.ObjectReaderCreatorASM::createObjectReader)
            .writerCreator(com.alibaba.fastjson3.writer.ObjectWriterCreatorASM::createObjectWriter)
            .build();

        System.out.println("  ASM 已启用");
        System.out.println("  性能提升: ~7% (read), 持平 (write)");
        System.out.println("  注意: 需要预热 1000+ 次调用");
        System.out.println();
    }

    /**
     * 4. 使用类型化缓存
     */
    private static void useTypeTokenCaching() {
        System.out.println("4. 使用 TypeToken 缓存");

        // ❌ 不好：每次创建新的 TypeToken
        // for (int i = 0; i < 1000; i++) {
        //     new TypeToken<List<User>>() {};
        // }

        // ✅ 好：缓存 TypeToken
        com.alibaba.fastjson3.TypeToken<java.util.List<User>> userType =
            new com.alibaba.fastjson3.TypeToken<java.util.List<User>>() {};

        String json = "[{\"id\":1,\"name\":\"张三\",\"age\":25}]";

        // 多次复用
        for (int i = 0; i < 100; i++) {
            java.util.List<User> users = JSON.parseObject(json, userType);
        }

        System.out.println("  缓存 TypeToken: " + userType.getType());
        System.out.println("  优势: 避免重复创建 TypeToken 对象");
        System.out.println();
    }

    /**
     * 5. 批量操作优化
     */
    private static void batchOperations() {
        System.out.println("5. 批量操作优化");

        String json = """
            {
                "name":"张三","age":25,"email":"zhangsan@example.com",
                "city":"北京","district":"朝阳区","vip":true
            }
            """;

        // ❌ 不好：多次 JSONPath 查询
        // String name = JSONPath.of("$.name").eval(json, String.class);
        // String age = JSONPath.of("$.age").eval(json, Integer.class);

        // ✅ 好：多路径一次性提取
        com.alibaba.fastjson3.JSONPath.TypedMultiPath multi =
            com.alibaba.fastjson3.JSONPath.typedMulti()
                .path("$.name", String.class)
                .path("$.age", Integer.class)
                .path("$.email", String.class)
                .path("$.vip", Boolean.class)
                .build();

        Object[] values = multi.extract(json);
        System.out.println("  一次性提取: " + java.util.Arrays.toString(values));
        System.out.println("  优势: 减少 JSON 解析次数");
        System.out.println();
    }
}
