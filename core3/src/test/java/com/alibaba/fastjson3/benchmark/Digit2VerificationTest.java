package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

/**
 * Verify digit2 implementation correctness.
 */
public class Digit2VerificationTest {

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Digit2 Verification Test");
        System.out.println("===============================================");

        boolean allPass = true;

        // Test 1: Simple 2-digit numbers
        allPass &= test("12", 12);
        allPass &= test("99", 99);
        allPass &= test("00", 0);

        // Test 2: Multi-digit numbers (where digit2 helps)
        allPass &= test("1234", 1234);
        allPass &= test("5678", 5678);
        allPass &= test("9999", 9999);

        // Test 3: Large numbers
        allPass &= test("12345678", 12345678);
        allPass &= test("87654321", 87654321);
        allPass &= test("99999999", 99999999);

        // Test 4: Negative numbers
        allPass &= test("-1234", -1234);
        allPass &= test("-9999", -9999);
        allPass &= test("-12345678", -12345678);

        // Test 5: Edge cases
        allPass &= test("0", 0);
        allPass &= test("-0", 0);
        allPass &= test("2147483647", Integer.MAX_VALUE);
        allPass &= test("-2147483648", Integer.MIN_VALUE);

        // Test 6: JSON object with many numbers
        String json = "{\"a\":123,\"b\":456,\"c\":789,\"d\":9999}";
        JSONObject obj = JSON.parseObject(json);
        allPass &= (obj.getIntValue("a") == 123);
        allPass &= (obj.getIntValue("b") == 456);
        allPass &= (obj.getIntValue("c") == 789);
        allPass &= (obj.getIntValue("d") == 9999);

        System.out.println("===============================================");
        if (allPass) {
            System.out.println("✓ ALL TESTS PASSED");
        } else {
            System.out.println("✗ SOME TESTS FAILED");
        }
        System.out.println("===============================================");
    }

    static boolean test(String json, int expected) {
        try {
            Integer result = JSON.parseObject("{\"value\":" + json + "}")
                .getInteger("value");
            if (result.equals(expected)) {
                System.out.printf("  ✓ \"%s\" -> %d%n", json, result);
                return true;
            } else {
                System.out.printf("  ✗ \"%s\" expected %d, got %d%n", json, expected, result);
                return false;
            }
        } catch (Exception e) {
            System.out.printf("  ✗ \"%s\" threw %s%n", json, e.getMessage());
            return false;
        }
    }
}
