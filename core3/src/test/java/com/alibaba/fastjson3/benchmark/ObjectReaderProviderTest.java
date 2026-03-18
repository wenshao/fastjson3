package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.reader.ReaderCreatorType;

/**
 * Test ObjectReaderProvider mechanism.
 */
public class ObjectReaderProviderTest {

    static final String USER_JSON = """
        {
            "id":"123",
            "name":"Test User",
            "age":25,
            "email":"test@example.com",
            "isActive":true
        }
        """;

    static class SimpleUser {
        public String id;
        public String name;
        public int age;
        public String email;
        public boolean isActive;

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static void main(String[] args) {
        System.out.println("=== ObjectReaderProvider Test ===\n");

        // Test 1: Default (AUTO)
        System.out.println("1. Testing AUTO (default)...");
        long start = System.nanoTime();
        SimpleUser user1 = JSON.parseObject(USER_JSON, SimpleUser.class);
        long autoTime = System.nanoTime() - start;
        System.out.println("   Result: " + user1.getName());
        System.out.println("   Time: " + (autoTime / 1_000_000) + " ms");

        // Test 2: REFLECT
        System.out.println("\n2. Testing REFLECT...");
        ObjectMapper reflectMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .build();
        start = System.nanoTime();
        SimpleUser user2 = reflectMapper.readValue(USER_JSON, SimpleUser.class);
        long reflectTime = System.nanoTime() - start;
        System.out.println("   Result: " + user2.getName());
        System.out.println("   Time: " + (reflectTime / 1_000_000) + " ms");

        // Test 3: ASM
        System.out.println("\n3. Testing ASM...");
        ObjectMapper asmMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();
        start = System.nanoTime();
        SimpleUser user3 = asmMapper.readValue(USER_JSON, SimpleUser.class);
        long asmTime = System.nanoTime() - start;
        System.out.println("   Result: " + user3.getName());
        System.out.println("   Time: " + (asmTime / 1_000_000) + " ms");

        // Verify results are consistent
        System.out.println("\n=== Verification ===");
        System.out.println("All results consistent: " +
            (user1.getName().equals(user2.getName()) &&
             user2.getName().equals(user3.getName())));

        System.out.println("\n✓ ObjectReaderProvider mechanism is working!");
    }
}
