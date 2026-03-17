package com.example.fastjson3.basic;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONValidator;
import com.alibaba.fastjson3.ObjectMapper;

/**
 * 基础示例：演示 fastjson3 的基本用法
 */
public class BasicExample {

    static class User {
        private String name;
        private int age;
        private boolean vip;

        public User() {}

        public User(String name, int age, boolean vip) {
            this.name = name;
            this.age = age;
            this.vip = vip;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public boolean isVip() { return vip; }
        public void setVip(boolean vip) { this.vip = vip; }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", vip=" + vip + "}";
        }
    }

    public static void main(String[] args) {
        // 1. 序列化对象
        System.out.println("=== 序列化 ===");
        User user = new User("张三", 25, true);
        String json = JSON.toJSONString(user);
        System.out.println("序列化结果: " + json);
        // 输出: {"age":25,"name":"张三","vip":true}

        // 美化输出
        String prettyJson = JSON.toJSONString(user, com.alibaba.fastjson3.WriteFeature.PrettyFormat);
        System.out.println("美化输出:\n" + prettyJson);

        // 2. 反序列化为对象
        System.out.println("\n=== 反序列化 ===");
        User parsedUser = JSON.parseObject(json, User.class);
        System.out.println("反序列化结果: " + parsedUser);

        // 3. 处理 JSON 数组
        System.out.println("\n=== JSON 数组 ===");
        String jsonArray = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";
        JSONArray arr = JSON.parseArray(jsonArray);
        System.out.println("数组长度: " + arr.size());
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            System.out.println("  [" + i + "] " + obj.getString("name") + ", " + obj.getIntValue("age"));
        }

        // 4. 解析数组为对象列表
        System.out.println("\n=== 数组转列表 ===");
        User[] users = JSON.parseArray(jsonArray, User[].class);
        for (User u : users) {
            System.out.println("  " + u);
        }

        // 5. 使用 ObjectMapper
        System.out.println("\n=== ObjectMapper ===");
        ObjectMapper mapper = ObjectMapper.shared();
        String mapperJson = mapper.writeValueAsString(user);
        System.out.println("ObjectMapper 序列化: " + mapperJson);
        User mappedUser = mapper.readValue(json, User.class);
        System.out.println("ObjectMapper 反序列化: " + mappedUser);

        // 6. 验证 JSON
        System.out.println("\n=== JSON 验证 ===");
        boolean isValid = JSON.isValid(json);
        System.out.println("JSON 有效: " + isValid);

        boolean isValidObject = JSON.isValidObject(json);
        System.out.println("是 JSON 对象: " + isValidObject);

        // 使用 JSONValidator
        try (JSONValidator validator = JSONValidator.from(json)) {
            if (validator.validate()) {
                System.out.println("JSONValidator 类型: " + validator.getType());
            }
        }
    }
}
