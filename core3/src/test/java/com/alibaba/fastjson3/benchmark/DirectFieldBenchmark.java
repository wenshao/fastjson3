package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSONParser;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark to measure DirectField algorithm effectiveness.
 * Compares traditional hash-based field matching with prefix-based matching.
 */
public class DirectFieldBenchmark {

    static class User {
        public String id;
        public String name;
        public String email;
        public String city;
        public String age;
    }

    // Traditional hash-based matching (simulating FieldNameMatcher behavior)
    static FieldMatchResult matchByHash(String input, int offset, String[] fieldNames) {
        // Simulate hash computation
        long hash = 0;
        for (int i = offset; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') break;
            hash = hash * 31 + c;
        }

        // Linear search for matching hash
        for (int i = 0; i < fieldNames.length; i++) {
            long fieldHash = fieldNames[i].hashCode();
            if (hash == fieldHash) {
                if (offset + fieldNames[i].length() <= input.length()) {
                    boolean match = true;
                    for (int j = 0; j < fieldNames[i].length(); j++) {
                        if (input.charAt(offset + j) != fieldNames[i].charAt(j)) {
                            match = false;
                            break;
                        }
                    }
                    if (match && input.charAt(offset + fieldNames[i].length()) == '"') {
                        return new FieldMatchResult(i, fieldNames[i].length());
                    }
                }
            }
        }
        return null;
    }

    // DirectField-based matching
    static FieldMatchResult matchByDirectField(String input, int offset,
                                     int[] prefixes, String[][] fieldGroups) {
        // Read 4-byte prefix (including opening quote)
        if (offset + 4 > input.length()) {
            return null;
        }

        // Encode prefix as int (big-endian)
        int prefix = ((input.charAt(offset) & 0xFF) << 24)
                  | ((input.charAt(offset + 1) & 0xFF) << 16)
                  | ((input.charAt(offset + 2) & 0xFF) << 8)
                  | (input.charAt(offset + 3) & 0xFF);

        // Binary search for prefix group
        int idx = binarySearchPrefix(prefixes, prefix);
        if (idx < 0) {
            return null;  // Prefix not found
        }

        // Check all fields in this prefix group
        String[] groupFields = fieldGroups[idx];
        for (String fieldName : groupFields) {
            if (offset + fieldName.length() > input.length()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < fieldName.length(); i++) {
                if (input.charAt(offset + i) != fieldName.charAt(i)) {
                    match = false;
                    break;
                }
            }
            if (match && input.charAt(offset + fieldName.length()) == '"') {
                return new FieldMatchResult(idx, fieldName.length());
            }
        }

        return null;
    }

    static int binarySearchPrefix(int[] prefixes, int key) {
        int low = 0, high = prefixes.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = prefixes[mid];
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    static class FieldMatchResult {
        final int groupIndex;
        final int nameLength;

        FieldMatchResult(int groupIndex, int nameLength) {
            this.groupIndex = groupIndex;
            this.nameLength = nameLength;
        }
    }

    @Test
    public void benchmarkFieldMatching() {
        String[] fieldNames = {"id", "name", "email", "city", "age"};
        String json = "{\"id\":\"123\",\"name\":\"test\",\"age\":25}";

        // Build DirectField prefix table
        int[] prefixes = new int[fieldNames.length];
        String[][] fieldGroups = new String[fieldNames.length][];

        for (int i = 0; i < fieldNames.length; i++) {
            String fn = fieldNames[i];
            byte[] nameBytes = fn.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] prefixBytes = new byte[4];
            prefixBytes[0] = '"';
            if (nameBytes.length == 2) {
                System.arraycopy(nameBytes, 0, prefixBytes, 1, 2);
                prefixBytes[3] = '"';
            } else {
                System.arraycopy(nameBytes, 0, prefixBytes, 1, 3);
            }
            prefixes[i] = ((prefixBytes[0] & 0xFF) << 24)
                       | ((prefixBytes[1] & 0xFF) << 16)
                       | ((prefixBytes[2] & 0xFF) << 8)
                       | (prefixBytes[3] & 0xFF);
            fieldGroups[i] = new String[]{fn};
        }

        // Sort prefixes for binary search
        java.util.Arrays.sort(prefixes);
        // Reorder groups to match sorted prefixes
        String[][] sortedGroups = new String[fieldNames.length][];
        for (int i = 0; i < fieldNames.length; i++) {
            String fn = fieldNames[i];
            byte[] nameBytes = fn.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] prefixBytes = new byte[4];
            prefixBytes[0] = '"';
            if (nameBytes.length == 2) {
                System.arraycopy(nameBytes, 0, prefixBytes, 1, 2);
                prefixBytes[3] = '"';
            } else {
                System.arraycopy(nameBytes, 0, prefixBytes, 1, 3);
            }
            int prefix = ((prefixBytes[0] & 0xFF) << 24)
                      | ((prefixBytes[1] & 0xFF) << 16)
                      | ((prefixBytes[2] & 0xFF) << 8)
                      | (prefixBytes[3] & 0xFF);

            int idx = java.util.Arrays.binarySearch(prefixes, prefix);
            sortedGroups[idx] = new String[]{fn};
        }

        System.out.println("=== DirectField Field Matching Benchmark ===");
        System.out.println("Fields: " + java.util.Arrays.toString(fieldNames));
        System.out.println("Prefixes: " + java.util.Arrays.toString(prefixes));

        int iterations = 1_000_000;
        int warmup = 100_000;

        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // Warmup
        for (int i = 0; i < warmup; i++) {
            for (int j = 0; j < fieldNames.length; j++) {
                String field = fieldNames[j];
                int start = json.indexOf("\"" + field + "\":");
                matchByHash(json, start + 1, fieldNames);
                matchByDirectField(json, start + 1, prefixes, sortedGroups);
            }
        }

        // Benchmark hash-based
        long hashStart = System.nanoTime();
        int hashMatches = 0;
        for (int i = 0; i < iterations; i++) {
            String field = fieldNames[i % fieldNames.length];
            int start = json.indexOf("\"" + field + "\":");
            FieldMatchResult result = matchByHash(json, start + 1, fieldNames);
            if (result != null) hashMatches++;
        }
        long hashTime = System.nanoTime() - hashStart;

        // Benchmark DirectField
        long directFieldStart = System.nanoTime();
        int directFieldMatches = 0;
        for (int i = 0; i < iterations; i++) {
            String field = fieldNames[i % fieldNames.length];
            int start = json.indexOf("\"" + field + "\":");
            FieldMatchResult result = matchByDirectField(json, start + 1, prefixes, sortedGroups);
            if (result != null) directFieldMatches++;
        }
        long directFieldTime = System.nanoTime() - directFieldStart;

        System.out.println("\n=== Results (" + iterations + " iterations) ===");
        System.out.printf("Hash-based:          %.3f ns/op (%d matches)\n",
            (double) hashTime / iterations, hashMatches);
        System.out.printf("DirectField:          %.3f ns/op (%d matches)\n",
            (double) directFieldTime / iterations, directFieldMatches);
        System.out.printf("Speedup:             %.2fx\n",
            (double) hashTime / directFieldTime);
    }
}
