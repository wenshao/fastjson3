package com.alibaba.fastjson3;

/**
 * Static field name cache. Reuses String instances across parses for identical
 * field names, eliminating repeated String allocation and byte[] copy.
 *
 * <p>Uses a simple hash-indexed array. Hash is computed as {@code hash * 31 + c}
 * during field name scanning. Note: UTF8/LATIN1 parsers compute hash from raw
 * bytes, while Str/CharArray parsers compute hash from char values. For ASCII
 * field names (the common case), the hash values are identical.</p>
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
    private static final String[] NAMES = new String[SIZE];

    /**
     * Look up a field name by hash, length, and content.
     * Returns cached String if hash+length match AND content equals, null otherwise.
     * Content verification is necessary because hash+length alone can collide
     * (e.g., "Aa" and "BB" have the same hash*31 value).
     */
    static String get(long hash, int nameLen, byte[] bytes, int nameStart) {
        int idx = (int) (hash & MASK);
        if (HASHES[idx] == hash) {
            String cached = NAMES[idx];
            if (cached != null && cached.length() == nameLen && contentEquals(cached, bytes, nameStart, nameLen)) {
                return cached;
            }
        }
        return null;
    }

    /**
     * Look up a field name by hash and content (char-based, for Str/CharArray parsers).
     */
    static String get(long hash, String name) {
        int idx = (int) (hash & MASK);
        if (HASHES[idx] == hash) {
            String cached = NAMES[idx];
            if (cached != null && cached.equals(name)) {
                return cached;
            }
        }
        return null;
    }

    static void put(long hash, String name) {
        int idx = (int) (hash & MASK);
        HASHES[idx] = hash;
        NAMES[idx] = name;
    }

    /**
     * Compare cached String content against raw bytes (ASCII/Latin1).
     */
    private static boolean contentEquals(String s, byte[] bytes, int off, int len) {
        if (s.length() != len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != (bytes[off + i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
