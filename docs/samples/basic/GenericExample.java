package com.example.fastjson3.basic;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;
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

        // 1. List<User>
        System.out.println("1. List<User>");
        String userListJson = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";

        // 使用 TypeToken
        TypeToken<List<User>> userListType = new TypeToken<List<User>>() {};
        List<User> users = JSON.parseObject(userListJson, userListType);

        for (User u : users) {
            System.out.println("  " + u);
        }

        // 2. Map<String, User>
        System.out.println("\n2. Map<String, User>");
        String userMapJson = """
            {"user1":{"name":"张三","age":25},"user2":{"name":"李四","age":30}}
            """;

        TypeToken<Map<String, User>> userMapType = new TypeToken<Map<String, User>>() {};
        Map<String, User> userMap = JSON.parseObject(userMapJson, userMapType);

        userMap.forEach((key, value) -> {
            System.out.println("  " + key + " -> " + value);
        });

        // 3. Map<String, List<User>>
        System.out.println("\n3. Map<String, List<User>>");
        String groupJson = """
            {"groupA":[{"name":"张三","age":25}],"groupB":[{"name":"李四","age":30}]}
            """;

        TypeToken<Map<String, List<User>>> groupType = new TypeToken<Map<String, List<User>>>() {};
        Map<String, List<User>> groups = JSON.parseObject(groupJson, groupType);

        groups.forEach((key, value) -> {
            System.out.println("  " + key + ":");
            for (User u : value) {
                System.out.println("    - " + u);
            }
        });

        // 4. 嵌套泛型 Response<List<User>>
        System.out.println("\n4. 自定义泛型结构");
        String responseJson = """
            {"code":200,"message":"success","data":[{"name":"张三","age":25}]}
            """;

        // 使用静态导入简化
        TypeToken<Response<User>> responseType = new TypeToken<Response<User>>() {};
        Response<User> response = JSON.parseObject(responseJson, responseType);

        System.out.println("  Code: " + response.code);
        System.out.println("  Message: " + response.message);
        System.out.println("  Data: " + response.data);

        // 5. 直接使用 parseArray
        System.out.println("\n5. 使用 parseArray");
        User[] userArray = JSON.parseArray(userListJson, User[].class);
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
    }
}
