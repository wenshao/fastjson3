package com.alibaba.fastjson3.samples.basic;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

import java.util.List;

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

        // 使用 parseArray + Class 直接获取 List<User>
        List<User> users = JSON.parseArray(userListJson, User.class);

        for (User u : users) {
            System.out.println("  " + u);
        }

        // 2. Map<String, User> - 解析为 JSONObject 再按需转换
        System.out.println("\n2. Map<String, User>");
        String userMapJson = """
            {"user1":{"name":"张三","age":25},"user2":{"name":"李四","age":30}}
            """;

        JSONObject userMapObj = JSON.parseObject(userMapJson);
        for (String key : userMapObj.keySet()) {
            // 从 JSONObject 值转换为 User
            User u = userMapObj.getJSONObject(key).toJavaObject(User.class);
            System.out.println("  " + key + " -> " + u);
        }

        // 3. 直接使用 parseArray 获取 List
        System.out.println("\n3. 使用 parseArray");
        List<User> userList = JSON.parseArray(userListJson, User.class);
        for (User u : userList) {
            System.out.println("  " + u);
        }

    }
}
