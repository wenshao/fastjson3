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
    private static final Entry[] ENTRIES = new Entry[SIZE];

    static final class Entry {
        final String name;
        final long hash;

        Entry(String name, long hash) {
            this.name = name;
            this.hash = hash;
        }
    }

    static String get(long hash) {
        Entry e = ENTRIES[(int) (hash & MASK)];
        if (e != null && e.hash == hash) {
            return e.name;
        }
        return null;
    }

    static void put(long hash, String name) {
        ENTRIES[(int) (hash & MASK)] = new Entry(name, hash);
    }
}
