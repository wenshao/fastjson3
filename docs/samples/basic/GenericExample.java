package com.alibaba.fastjson3.samples.basic;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;
import com.alibaba.fastjson3.TypeReference;
import com.alibaba.fastjson3.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 泛型示例：演示处理复杂泛型类型
 */
public class GenericExample {

    static class User {
        private String name;
        private int age;

        public User() {}

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 泛型处理 ===\n");

        // 1. List<User> - 使用 TypeToken.listOf()
        System.out.println("1. List<User>");
        String userListJson = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";

        // 使用 TypeToken.listOf() 工厂方法
        TypeToken<List<User>> userListType = TypeToken.listOf(User.class);
        List<User> users = JSON.parse(userListJson, userListType);

        for (User u : users) {
            System.out.println("  " + u);
        }

        // 2. Map<String, User> - 使用 TypeToken.mapOf()
        System.out.println("\n2. Map<String, User>");
        String userMapJson = """
            {"user1":{"name":"张三","age":25},"user2":{"name":"李四","age":30}}
            """;

        TypeToken<Map<String, User>> userMapType = TypeToken.mapOf(User.class);
        Map<String, User> userMap = JSON.parse(userMapJson, userMapType);

        userMap.forEach((key, value) -> {
            System.out.println("  " + key + " -> " + value);
        });

        // 3. 复杂嵌套泛型 Map<String, List<User>> - 使用 TypeReference
        System.out.println("\n3. Map<String, List<User>>");
        String groupJson = """
            {"groupA":[{"name":"张三","age":25}],"groupB":[{"name":"李四","age":30}]}
            """;

        // 复杂嵌套类型使用 TypeReference
        TypeReference<Map<String, List<User>>> groupTypeRef = new TypeReference<>() {};
        Map<String, List<User>> groups = JSON.parseObject(groupJson, groupTypeRef);

        groups.forEach((key, value) -> {
            System.out.println("  " + key + ":");
            for (User u : value) {
                System.out.println("    - " + u);
            }
        });

        // 4. 嵌套泛型 Response<User> - 使用 TypeReference
        System.out.println("\n4. 自定义泛型结构");
        String responseJson = """
            {"code":200,"message":"success","data":{"name":"张三","age":25}}
            """;

        TypeReference<Response<User>> responseTypeRef = new TypeReference<>() {};
        Response<User> response = JSON.parseObject(responseJson, responseTypeRef);

        System.out.println("  Code: " + response.getCode());
        System.out.println("  Message: " + response.getMessage());
        System.out.println("  Data: " + response.getData());

        // 5. 直接使用 parseArray 获取 List
        System.out.println("\n5. 使用 parseArray");
        List<User> userList = JSON.parseArray(userListJson, User.class);
        for (User u : userList) {
            System.out.println("  " + u);
        }

        // 6. 使用 TypeToken.arrayOf() 获取数组
        System.out.println("\n6. 使用 TypeToken.arrayOf()");
        TypeToken<User[]> arrayType = TypeToken.arrayOf(User.class);
        User[] userArray = JSON.parse(userListJson, arrayType);
        for (User u : userArray) {
            System.out.println("  " + u);
        }
    }

    // 自定义泛型容器类
    static class Response<T> {
        private int code;
        private String message;
        private T data;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        @Override
        public String toString() {
            return "Response{code=" + code + ", message='" + message + "', data=" + data + "}";
        }
    }
}
