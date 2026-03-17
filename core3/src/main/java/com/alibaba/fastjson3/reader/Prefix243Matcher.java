package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.util.JDKUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Fast field name matcher based on fastjson2's genRead243 algorithm.
 *
 * <p>This matcher uses a two-phase approach:
 * <ol>
 *   <li><b>Phase 1</b>: Match first 4 bytes (including quote) encoded as int.
 *       This uses a lookup switch/tableswitch for O(1) dispatch.</li>
 *   <li><b>Phase 2</b>: Length-specific exact matching within the prefix group.</li>
 * </ol>
 *
 * <p>The algorithm name "243" comes from the field name length constraints:
 * fieldNameLengthMin >= 2 and fieldNameLengthMax <= 43.</p>
 *
 * <p>Example for field name "age":
 * <pre>
 *   JSON: "age":123
 *   Bytes: 0x22 0x61 0x67 0x65 0x3a ...
 *         "    a    g    e    :
 *
 *   Phase 1: Read 4 bytes as int: 0x22616765
 *   Phase 2: Length=3 ("age"), verify remaining bytes
 * </pre>
 */
public final class Prefix243Matcher {

    /**
     * Read an int (4 bytes) from a byte array at the given offset in BIG-ENDIAN order.
     * This is critical for consistent prefix matching across platforms.
     */
    private static int getInt(byte[] buf, int offset) {
        if (JDKUtils.UNSAFE_AVAILABLE) {
            int v = JDKUtils.getIntDirect(buf, offset);
            // Convert little-endian to big-endian if needed
            // Unsafe.getInt reads in native byte order (little-endian on x86)
            // We need big-endian for consistent prefix matching
            return Integer.reverseBytes(v);
        }
        // Fallback: manual assembly (big-endian)
        return ((buf[offset] & 0xFF) << 24)
             | ((buf[offset + 1] & 0xFF) << 16)
             | ((buf[offset + 2] & 0xFF) << 8)
             | (buf[offset + 3] & 0xFF);
    }

    /**
     * A group of field readers that share the same 4-byte prefix.
     */
    public static final class PrefixGroup {
        public final int prefix;  // 4-byte prefix as int
        public final FieldReader[] readers;

        public PrefixGroup(int prefix, FieldReader[] readers) {
            this.prefix = prefix;
            this.readers = readers;
        }
    }

    /**
     * Length-specific match info for a field reader.
     */
    public static final class FieldMatchInfo {
        public final FieldReader reader;
        public final int nameLength;
        public final byte[] nameBytes;

        // For length > 4: additional 4-byte chunks
        public final int[] suffixChunks;  // suffix encoded as int[] (4 bytes each)

        public FieldMatchInfo(FieldReader reader, int nameLength, byte[] nameBytes, int[] suffixChunks) {
            this.reader = reader;
            this.nameLength = nameLength;
            this.nameBytes = nameBytes;
            this.suffixChunks = suffixChunks;
        }
    }

    // Prefix (4 bytes) → group of fields with that prefix
    private final PrefixGroup[] groups;
    private final int[] prefixes;  // sorted prefix array for binary search

    private Prefix243Matcher(PrefixGroup[] groups, int[] prefixes) {
        this.groups = groups;
        this.prefixes = prefixes;
    }

    /**
     * Build a Prefix243Matcher from field readers.
     *
     * @param fieldReaders array of field readers
     * @return the matcher, or null if field name constraints don't allow 243 optimization
     */
    public static Prefix243Matcher build(FieldReader[] fieldReaders) {
        if (fieldReaders.length == 0) {
            return null;
        }

        // Check constraints: min length >= 2, max length <= 43
        int minLen = Integer.MAX_VALUE;
        int maxLen = Integer.MIN_VALUE;
        for (FieldReader fr : fieldReaders) {
            int len = fr.fieldName.length();
            minLen = Math.min(minLen, len);
            maxLen = Math.max(maxLen, len);
        }

        // Include alternate names in length check
        for (FieldReader fr : fieldReaders) {
            for (String alt : fr.alternateNames) {
                int len = alt.length();
                minLen = Math.min(minLen, len);
                maxLen = Math.max(maxLen, len);
            }
        }

        // genRead243 requires: 2 <= length <= 43
        if (minLen < 2 || maxLen > 43) {
            return null;
        }

        // Collect all field names (including alternates)
        int totalNames = 0;
        for (FieldReader fr : fieldReaders) {
            totalNames += 1 + fr.alternateNames.length;
        }

        // Build prefix map: int (4-byte prefix) → List<FieldMatchInfo>
        TreeMap<Integer, List<FieldMatchInfo>> prefixMap = new TreeMap<>();

        for (FieldReader fr : fieldReaders) {
            addField(prefixMap, fr, fr.fieldName);
            for (String alt : fr.alternateNames) {
                addField(prefixMap, fr, alt);
            }
        }

        // Convert to PrefixGroup array
        int size = prefixMap.size();
        PrefixGroup[] groups = new PrefixGroup[size];
        int[] prefixes = new int[size];
        int idx = 0;
        for (var entry : prefixMap.entrySet()) {
            int prefix = entry.getKey();
            List<FieldMatchInfo> infos = entry.getValue();

            FieldReader[] readers = new FieldReader[infos.size()];
            for (int i = 0; i < infos.size(); i++) {
                readers[i] = infos.get(i).reader;
            }

            groups[idx] = new PrefixGroup(prefix, readers);
            prefixes[idx] = prefix;
            idx++;
        }

        return new Prefix243Matcher(groups, prefixes);
    }

    private static void addField(TreeMap<Integer, List<FieldMatchInfo>> prefixMap,
                                  FieldReader reader, String fieldName) {
        byte[] nameBytes = fieldName.getBytes(StandardCharsets.UTF_8);

        // Build 4-byte prefix: " + first 3 chars (or " + 2 chars + " for 2-char names)
        byte[] prefixBytes = new byte[4];
        prefixBytes[0] = '"';  // JSON field name starts with quote

        if (nameBytes.length == 2) {
            // 2-char name: "ab" → prefixBytes = ['"', 'a', 'b', '"']
            System.arraycopy(nameBytes, 0, prefixBytes, 1, 2);
            prefixBytes[3] = '"';
        } else {
            // 3+ char name: "abc" → prefixBytes = ['"', 'a', 'b', 'c']
            System.arraycopy(nameBytes, 0, prefixBytes, 1, Math.min(3, nameBytes.length));
        }

        int prefix = getInt(prefixBytes, 0);

        // Build suffix chunks for names longer than 4 chars
        int[] suffixChunks = null;
        if (nameBytes.length > 4) {
            int numChunks = (nameBytes.length + 3) / 4;
            suffixChunks = new int[numChunks];
            for (int i = 0; i < numChunks; i++) {
                int offset = i * 4;
                int remaining = nameBytes.length - offset;
                if (remaining >= 4) {
                    suffixChunks[i] = getInt(nameBytes, 0 + offset);
                } else {
                    // Partial last chunk: pad with '"' and ':'
                    byte[] pad = new byte[4];
                    System.arraycopy(nameBytes, offset, pad, 0, remaining);
                    pad[remaining] = '"';
                    if (remaining + 1 < 4) {
                        pad[remaining + 1] = ':';
                    }
                    suffixChunks[i] = getInt(pad, 0);
                }
            }
        }

        FieldMatchInfo info = new FieldMatchInfo(reader, nameBytes.length, nameBytes, suffixChunks);
        prefixMap.computeIfAbsent(prefix, k -> new ArrayList<>()).add(info);
    }

    /**
     * Find the field reader matching the field name at the current position.
     * The input should be positioned after the opening quote.
     *
     * @param bytes input JSON bytes
     * @param offset current offset (after opening quote)
     * @param end input end
     * @return matching FieldReader, or null if no match
     */
    public FieldReader match(byte[] bytes, int offset, int end) {
        if (offset + 4 > end) {
            return null;
        }

        // Read 4-byte prefix
        // Position: offset is after opening quote, so we need to go back 1 to include it
        int prefix = getInt(bytes, 0 + offset - 1);

        // Binary search for prefix group
        int idx = binarySearchPrefix(prefixes, prefix);
        if (idx < 0) {
            return null;
        }

        // Check all readers in this prefix group
        PrefixGroup group = groups[idx];
        for (FieldReader reader : group.readers) {
            if (matchExact(bytes, offset, end, reader.fieldName)) {
                return reader;
            }
            // Check alternate names
            for (String alt : reader.alternateNames) {
                if (matchExact(bytes, offset, end, alt)) {
                    return reader;
                }
            }
        }

        return null;
    }

    /**
     * Fast path: match when the field name has no escape sequences.
     * Compares directly against the expected UTF-8 bytes.
     */
    private static boolean matchExact(byte[] input, int offset, int end, String expectedName) {
        byte[] expected = expectedName.getBytes(StandardCharsets.UTF_8);
        int len = expected.length;

        if (offset + len + 1 > end) {  // +1 for closing quote
            return false;
        }

        // Compare each byte
        for (int i = 0; i < len; i++) {
            if (input[offset + i] != expected[i]) {
                return false;
            }
        }

        // Check closing quote
        if (input[offset + len] != '"') {
            return false;
        }

        return true;
    }

    private static int binarySearchPrefix(int[] array, int key) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = array[mid];

            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid;  // key found
            }
        }
        return -(low + 1);  // key not found
    }

    /**
     * Public method for binary search (used by JSONParser.UTF8).
     */
    public int binarySearchPrefix(int key) {
        return binarySearchPrefix(prefixes, key);
    }

    /**
     * Get the prefix group at index.
     */
    public PrefixGroup getGroup(int index) {
        return groups[index];
    }

    /**
     * Get the number of prefix groups (for debugging/benchmarking).
     */
    public int getGroupCount() {
        return groups.length;
    }

    /**
     * Check if this matcher can be used (has groups).
     */
    public boolean isValid() {
        return groups.length > 0;
    }
}
