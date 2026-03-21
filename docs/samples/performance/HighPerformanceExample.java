package com.alibaba.fastjson3.samples.performance;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeToken;
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
        public Integer getUserAge() { return userAge; }
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
            com.alibaba.fastjson3.JSONPath.of("$.users[*].name");

        // 多次使用
        for (int i = 0; i < 100; i++) {
            // path.extract(json) 会更快
        }

        System.out.println("  预编译路径: $.users[*].name");
        System.out.println("  优势: 避免重复编译开销");
        System.out.println();
    }

    /**
     * 3. 默认配置已是最优
     */
    private static void useASM() {
        System.out.println("3. 默认反射路径（JIT 最优）");

        // 默认配置使用反射路径，JIT 深度内联后比 ASM 快 10-13%
        ObjectMapper mapper = com.alibaba.fastjson3.ObjectMapper.shared();

        System.out.println("  默认反射路径: 已启用");
        System.out.println("  原因: JIT 对紧凑循环深度内联，比 ASM 展开代码快 10-13%");
        System.out.println("  建议: 无需手动配置，预热 1000+ 次调用让 JIT 充分优化");
        System.out.println();
    }

    /**
     * 4. 使用类型化缓存
     */
    private static void useTypeTokenCaching() {
        System.out.println("4. 使用 parseArray 处理 List 类型");

        String json = "[{\"id\":1,\"name\":\"张三\",\"age\":25}]";

        // ✅ 推荐：直接使用 parseArray
        for (int i = 0; i < 100; i++) {
            java.util.List<User> users = JSON.parseArray(json, User.class);
        }

        System.out.println("  parseArray 简洁高效");
        System.out.println("  优势: 无需 TypeToken，直接获取强类型 List");
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

        // ❌ 不好：多次 JSONPath 查询（每次都解析 JSON）
        // String name = JSONPath.of("$.name").extract(json, String.class);
        // String age = JSONPath.of("$.age").extract(json, Integer.class);

        // ✅ 好：使用已解析对象进行多次查询
        com.alibaba.fastjson3.JSONObject obj = JSON.parseObject(json);
        String name = com.alibaba.fastjson3.JSONPath.of("$.name").eval(obj, String.class);
        Integer age = com.alibaba.fastjson3.JSONPath.of("$.age").eval(obj, Integer.class);
        String email = com.alibaba.fastjson3.JSONPath.of("$.email").eval(obj, String.class);
        Boolean vip = com.alibaba.fastjson3.JSONPath.of("$.vip").eval(obj, Boolean.class);

        System.out.println("  一次性提取: name=" + name + ", age=" + age +
                          ", email=" + email + ", vip=" + vip);
        System.out.println("  优势: 减少 JSON 解析次数");
        System.out.println();
    }
}
