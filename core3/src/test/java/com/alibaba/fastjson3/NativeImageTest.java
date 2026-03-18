package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.JDKUtils;

/**
 * Simple test program for GraalVM Native Image verification.
 */
public class NativeImageTest {

    public static class User {
        public String name;
        public int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== FastJSON3 Native Image Test ===");
        System.out.println("NATIVE_IMAGE: " + JDKUtils.NATIVE_IMAGE);
        System.out.println("UNSAFE_AVAILABLE: " + JDKUtils.UNSAFE_AVAILABLE);
        System.out.println("FAST_STRING_CREATION: " + JDKUtils.FAST_STRING_CREATION);

        // Test 1: Simple JSON parsing
        String json = "{\"name\":\"Alice\",\"age\":30}";
        User user = JSON.parseObject(json, User.class);
        System.out.println("Parsed: " + user.name + ", " + user.age);

        // Test 2: JSON generation
        String result = JSON.toJSONString(user);
        System.out.println("Generated: " + result);

        // Test 3: List parsing
        String jsonArray = "[{\"name\":\"Bob\",\"age\":25},{\"name\":\"Carol\",\"age\":35}]";
        java.util.List<User> users = JSON.parseArray(jsonArray, User.class);
        System.out.println("List size: " + users.size());

        // Test 4: Latin1 string optimization
        String latin1 = "Hello";
        byte[] bytes = latin1.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        String created = JDKUtils.createAsciiString(bytes, 0, bytes.length);
        System.out.println("Latin1 string: " + created);

        System.out.println("=== All tests passed! ===");
    }
}
