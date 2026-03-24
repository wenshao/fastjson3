package com.alibaba.fastjson3;

/**
 * Static field name cache. Reuses String instances across parses for identical
 * field names, eliminating repeated String allocation and byte[] copy.
 *
 * <p>Uses a simple hash-indexed array. Hash is computed as {@code hash * 31 + c}
 * during field name scanning — the same hash works across all parser types
 * (UTF8, LATIN1, Str, CharArray) since it operates on character values.</p>
 *
 * <p>Thread-safe: entries are immutable; racy writes are benign (worst case:
 * cache miss → create new String, which is correct).</p>
 *
 * <p>Inspired by fastjson2's JSONFactory.NAME_CACHE.</p>
 */
final class NameCache {
    private static final int SIZE = 1024; // power of 2
    private static final int MASK = SIZE - 1;
    private static final long[] HASHES = new long[SIZE];
    private static final int[] LENGTHS = new int[SIZE];
    private static final String[] NAMES = new String[SIZE];

    /**
     * Look up a field name by hash and length.
     * Returns cached String if both match, null otherwise.
     * No String.length() call needed — length stored in cache.
     */
    static String get(long hash, int nameLen) {
        int idx = (int) (hash & MASK);
        if (HASHES[idx] == hash && LENGTHS[idx] == nameLen) {
            return NAMES[idx];
        }
        return null;
    }

    static void put(long hash, int nameLen, String name) {
        int idx = (int) (hash & MASK);
        HASHES[idx] = hash;
        LENGTHS[idx] = nameLen;
        NAMES[idx] = name;
    }
}
