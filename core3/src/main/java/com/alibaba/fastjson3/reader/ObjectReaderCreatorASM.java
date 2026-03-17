package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.internal.asm.ClassWriter;
import com.alibaba.fastjson3.internal.asm.Label;
import com.alibaba.fastjson3.internal.asm.MethodWriter;
import com.alibaba.fastjson3.internal.asm.Opcodes;
import com.alibaba.fastjson3.util.DynamicClassLoader;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_FIELD_NAME_MATCHER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_FIELD_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_OBJECT_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_FIELD_NAME_MATCHER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_FIELD_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JDK_UTILS;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_EXCEPTION;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_PARSER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_PARSER_UTF8;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_OBJECT_READER;

/**
 * Enhanced ObjectReaderCreator with genRead243 algorithm support (WIP).
 *
 * <h2>genRead243 Algorithm</h2>
 *
 * <p>Key idea: Use 4-byte prefix of field name (including opening quote) for O(1) dispatch
 * via lookupswitch, then exact match within the small prefix group.</p>
 *
 * <h3>Algorithm Steps:</h3>
 * <ol>
 *   <li>Pre-compute 4-byte prefix for each field name</li>
 *   <li>Build lookupswitch on prefix values</li>
 *   <li>For each prefix case, check exact field name within group</li>
 *   <li>Read field value and continue</li>
 * </ol>
 *
 * <h3>Example:</h3>
 * <pre>
 * Field: "id"   -> JSON: "id":123
 * Prefix: 0x22696422 ('i' 'd' '"')
 *
 * Field: "name" -> JSON: "name":"test"
 * Prefix: 0x226e616d ('n' 'a' 'm')
 * </pre>
 *
 * <h3>Performance:</h3>
 * <ul>
 *   <li>Traditional hash-based: O(n) worst case, hash computation overhead</li>
 *   <li>genRead243: O(1) prefix dispatch + small group exact match</li>
 *   <li>ASM inlined: No virtual calls, branch prediction friendly</li>
 * </ul>
 *
 * <h3>Implementation Status:</h3>
 * <ul>
 *   <li>✅ Prefix computation logic implemented</li>
 *   <li>✅ Prefix grouping implemented</li>
 *   <li>⏳ ASM bytecode generation (TODO)</li>
 *   <li>⏳ Supporting methods (readInt4, checkFieldName, etc.) (TODO)</li>
 * </ul>
 *
 * @see com.alibaba.fastjson2.reader.ObjectReaderCreatorASM#genRead243
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ObjectReaderCreatorASM {
    private static final AtomicLong SEED = new AtomicLong();
    private static final DynamicClassLoader CLASS_LOADER = DynamicClassLoader.getInstance();

    private ObjectReaderCreatorASM() {
    }

    /**
     * Create an ObjectReader using ASM bytecode generation with genRead243 optimization.
     * Currently falls back to reflection as genRead243 is not yet fully implemented.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        // TODO: Implement full genRead243 algorithm
        // For now, always use reflection-based reader
        return ObjectReaderCreator.createObjectReader(type);
    }

    // ==================== Prefix Group Info ====================

    /**
     * Information about prefix groups for genRead243 algorithm.
     * Groups fields by their 4-byte JSON field name prefix.
     */
    public static class PrefixGroupInfo {
        public final int[] prefixes;
        public final int[] prefixCounts;
        public final List<List<PrefixField>> fields;
        public final boolean enabled;

        public PrefixGroupInfo(int[] prefixes, int[] prefixCounts,
                               List<List<PrefixField>> fields, boolean enabled) {
            this.prefixes = prefixes;
            this.prefixCounts = prefixCounts;
            this.fields = fields;
            this.enabled = enabled;
        }
    }

    /**
     * Information about a single field in a prefix group.
     */
    public static class PrefixField {
        public final int index;
        public final String fieldName;
        public final int nameLength;
        public final int prefix;  // 4-byte prefix value

        public PrefixField(int index, String fieldName, int nameLength, int prefix) {
            this.index = index;
            this.fieldName = fieldName;
            this.nameLength = nameLength;
            this.prefix = prefix;
        }
    }

    /**
     * Build prefix group information for genRead243 algorithm.
     *
     * @param fieldReaders array of field readers to process
     * @param minNameLen minimum field name length (inclusive), or 0 for auto-detect
     * @param maxNameLen maximum field name length (inclusive), or 0 for auto-detect
     * @return PrefixGroupInfo with prefix groups, or null if constraints not met
     */
    public static PrefixGroupInfo buildPrefixGroupInfo(FieldReader[] fieldReaders,
                                                      int minNameLen, int maxNameLen) {
        // Determine actual min/max if not specified
        int actualMinLen = Integer.MAX_VALUE;
        int actualMaxLen = Integer.MIN_VALUE;

        for (FieldReader fr : fieldReaders) {
            int len = fr.fieldName.length();
            actualMinLen = Math.min(actualMinLen, len);
            actualMaxLen = Math.max(actualMaxLen, len);
            for (String alt : fr.alternateNames) {
                int altLen = alt.length();
                actualMinLen = Math.min(actualMinLen, altLen);
                actualMaxLen = Math.max(actualMaxLen, altLen);
            }
        }

        // Apply constraints
        if (minNameLen == 0) minNameLen = actualMinLen;
        if (maxNameLen == 0) maxNameLen = actualMaxLen;

        // genRead243 requires: 2 <= length <= 43
        if (minNameLen < 2 || maxNameLen > 43) {
            return new PrefixGroupInfo(new int[0], new int[0], new ArrayList<>(), false);
        }

        // Build prefix map
        TreeMap<Integer, List<PrefixField>> prefixMap = new TreeMap<>();

        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            addFieldToPrefixMap(prefixMap, fr, i, fr.fieldName);
            for (String alt : fr.alternateNames) {
                addFieldToPrefixMap(prefixMap, fr, i, alt);
            }
        }

        if (prefixMap.isEmpty()) {
            return new PrefixGroupInfo(new int[0], new int[0], new ArrayList<>(), false);
        }

        int[] prefixes = new int[prefixMap.size()];
        int[] prefixCounts = new int[prefixMap.size()];
        List<List<PrefixField>> fields = new ArrayList<>(prefixMap.size());

        int idx = 0;
        for (var entry : prefixMap.entrySet()) {
            prefixes[idx] = entry.getKey();
            prefixCounts[idx] = entry.getValue().size();
            fields.add(entry.getValue());
            idx++;
        }

        return new PrefixGroupInfo(prefixes, prefixCounts, fields, true);
    }

    /**
     * Compute the 4-byte prefix for a field name in JSON format.
     * The prefix includes the opening quote and first 3 characters (or first 2 + closing quote for 2-char names).
     *
     * @param fieldName the field name
     * @return 4-byte prefix as int (big-endian), or 0 if invalid
     */
    public static int computeFieldNamePrefix(String fieldName) {
        int len = fieldName.length();
        if (len < 2 || len > 43) {
            return 0;
        }

        byte[] nameBytes = fieldName.getBytes(StandardCharsets.UTF_8);
        byte[] prefixBytes = new byte[4];
        prefixBytes[0] = '"';

        if (nameBytes.length == 2) {
            System.arraycopy(nameBytes, 0, prefixBytes, 1, 2);
            prefixBytes[3] = '"';
        } else {
            System.arraycopy(nameBytes, 0, prefixBytes, 1, Math.min(3, nameBytes.length));
        }

        // Big-endian encoding
        return ((prefixBytes[0] & 0xFF) << 24)
             | ((prefixBytes[1] & 0xFF) << 16)
             | ((prefixBytes[2] & 0xFF) << 8)
             | (prefixBytes[3] & 0xFF);
    }

    private static void addFieldToPrefixMap(
            TreeMap<Integer, List<PrefixField>> prefixMap,
            FieldReader fr, int index, String fieldName
    ) {
        int prefix = computeFieldNamePrefix(fieldName);
        if (prefix == 0) {
            return;
        }
        prefixMap.computeIfAbsent(prefix, k -> new ArrayList<>())
                .add(new PrefixField(index, fieldName, fieldName.length(), prefix));
    }

    /**
     * Utility method to print prefix group information for debugging.
     */
    public static void printPrefixGroupInfo(PrefixGroupInfo info) {
        System.out.println("=== genRead243 Prefix Group Info ===");
        System.out.println("Enabled: " + info.enabled);
        System.out.println("Prefix groups: " + info.prefixes.length);

        for (int i = 0; i < info.prefixes.length; i++) {
            System.out.printf("  Group %d: prefix=0x%08X, count=%d\n",
                i, info.prefixes[i], info.prefixCounts[i]);
            for (PrefixField pf : info.fields.get(i)) {
                System.out.printf("    - [%d] %s (len=%d)\n",
                    pf.index, pf.fieldName, pf.nameLength);
            }
        }
    }

    /**
     * Example usage and test for genRead243 algorithm.
     */
    public static class GenRead243Example {
        public static class TestBean {
            public String id;
            public String name;
            public String age;
            public String email;
            public String city;
        }

        public static void main(String[] args) {
            FieldReader[] fieldReaders = new FieldReader[] {
                new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
                new FieldReader("name", null, String.class, String.class, 1, null, false, null, null),
                new FieldReader("age", null, String.class, String.class, 2, null, false, null, null),
                new FieldReader("email", null, String.class, String.class, 3, null, false, null, null),
                new FieldReader("city", null, String.class, String.class, 4, null, false, null, null),
            };

            PrefixGroupInfo info = buildPrefixGroupInfo(fieldReaders, 0, 0);
            printPrefixGroupInfo(info);

            // Show individual field prefixes
            System.out.println("\n=== Field Prefix Examples ===");
            for (FieldReader fr : fieldReaders) {
                int prefix = computeFieldNamePrefix(fr.fieldName);
                System.out.printf("%s -> 0x%08X\n", fr.fieldName, prefix);
            }
        }
    }
}
