package com.alibaba.fastjson3.benchmark.jjb;

import java.io.InputStream;

public class QuickCompare {
    public static void main(String[] args) throws Exception {
        // Load test data
        InputStream is = QuickCompare.class.getClassLoader().getResourceAsStream("data/jjb/user.json");
        byte[] userBytes = is.readAllBytes();
        is.close();

        is = QuickCompare.class.getClassLoader().getResourceAsStream("data/jjb/client.json");
        byte[] clientBytes = is.readAllBytes();
        is.close();

        // Parse test objects once
        Users user = com.alibaba.fastjson2.JSON.parseObject(userBytes, Users.class);
        Clients client = com.alibaba.fastjson2.JSON.parseObject(clientBytes, Clients.class);

        System.out.println("=== fastjson3 vs wast - Quick Comparison ===\n");

        // Warmup
        for (int i = 0; i < 10000; i++) {
            com.alibaba.fastjson3.JSON.parseObject(userBytes, Users.class);
            io.github.wycst.wast.json.JSON.parseObject(userBytes, Users.class);
        }

        // Test 1: Users Parse
        int iterations = 100000;
        long start, end;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson3.JSON.parseObject(userBytes, Users.class);
        }
        end = System.nanoTime();
        long fastjson3Parse = (end - start) / 1000; // microseconds

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            io.github.wycst.wast.json.JSON.parseObject(userBytes, Users.class);
        }
        end = System.nanoTime();
        long wastParse = (end - start) / 1000;

        System.out.println("Users Parse (iterations=" + iterations + "):");
        System.out.printf("  fastjson3: %,d us (%.0f ops/s)\n", fastjson3Parse, iterations * 1000000000.0 / (fastjson3Parse * 1000));
        System.out.printf("  wast:      %,d us (%.0f ops/s)\n", wastParse, iterations * 1000000000.0 / (wastParse * 1000));
        System.out.printf("  fastjson3领先: %.1f%%\n\n", (wastParse - fastjson3Parse) * 100.0 / wastParse);

        // Test 2: Clients Parse
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson3.JSON.parseObject(clientBytes, Clients.class);
        }
        end = System.nanoTime();
        long fastjson3Parse2 = (end - start) / 1000;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            io.github.wycst.wast.json.JSON.parseObject(clientBytes, Clients.class);
        }
        end = System.nanoTime();
        long wastParse2 = (end - start) / 1000;

        System.out.println("Clients Parse (iterations=" + iterations + "):");
        System.out.printf("  fastjson3: %,d us (%.0f ops/s)\n", fastjson3Parse2, iterations * 1000000000.0 / (fastjson3Parse2 * 1000));
        System.out.printf("  wast:      %,d us (%.0f ops/s)\n", wastParse2, iterations * 1000000000.0 / (wastParse2 * 1000));
        System.out.printf("  fastjson3领先: %.1f%%\n\n", (wastParse2 - fastjson3Parse2) * 100.0 / wastParse2);

        // Test 3: Users Write
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson3.JSON.toJSONBytes(user);
        }
        end = System.nanoTime();
        long fastjson3Write = (end - start) / 1000;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            io.github.wycst.wast.json.JSON.toJsonBytes(user);
        }
        end = System.nanoTime();
        long wastWrite = (end - start) / 1000;

        System.out.println("Users Write (iterations=" + iterations + "):");
        System.out.printf("  fastjson3: %,d us (%.0f ops/s)\n", fastjson3Write, iterations * 1000000000.0 / (fastjson3Write * 1000));
        System.out.printf("  wast:      %,d us (%.0f ops/s)\n", wastWrite, iterations * 1000000000.0 / (wastWrite * 1000));
        System.out.printf("  fastjson3领先: %.1f%%\n\n", (wastWrite - fastjson3Write) * 100.0 / wastWrite);

        // Test 4: Clients Write
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson3.JSON.toJSONBytes(client);
        }
        end = System.nanoTime();
        long fastjson3Write2 = (end - start) / 1000;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            io.github.wycst.wast.json.JSON.toJsonBytes(client);
        }
        end = System.nanoTime();
        long wastWrite2 = (end - start) / 1000;

        System.out.println("Clients Write (iterations=" + iterations + "):");
        System.out.printf("  fastjson3: %,d us (%.0f ops/s)\n", fastjson3Write2, iterations * 1000000000.0 / (fastjson3Write2 * 1000));
        System.out.printf("  wast:      %,d us (%.0f ops/s)\n", wastWrite2, iterations * 1000000000.0 / (wastWrite2 * 1000));
        System.out.printf("  fastjson3领先: %.1f%%\n\n", (wastWrite2 - fastjson3Write2) * 100.0 / wastWrite2);

        // Summary
        System.out.println("=== Summary ===");
        double avgImprovement = ((wastParse - fastjson3Parse) * 100.0 / wastParse +
                                 (wastParse2 - fastjson3Parse2) * 100.0 / wastParse2 +
                                 (wastWrite - fastjson3Write) * 100.0 / wastWrite +
                                 (wastWrite2 - fastjson3Write2) * 100.0 / wastWrite2) / 4.0;
        System.out.printf("fastjson3平均领先: %.1f%%\n", avgImprovement);
    }
}
