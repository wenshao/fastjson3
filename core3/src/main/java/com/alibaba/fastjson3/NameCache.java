package com.alibaba.fastjson3;

/**
 * Static field name cache for tree parsing. Reuses String instances across parses
 * for identical field names, eliminating repeated String allocation.
 *
 * <p>Inspired by fastjson2's JSONFactory.NAME_CACHE. Uses a fixed-size open-addressing
 * hash table indexed by field name hash. Entries are replaced on collision (no chaining).</p>
 *
 * <p>Thread-safe: entries are immutable records; racy writes are benign
 * (worst case: a cache miss creates a new String, which is correct).</p>
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

    /**
     * Look up a field name by its hash. Returns the cached String if the hash matches,
     * or null if not found.
     */
    static String get(long hash) {
        Entry e = ENTRIES[(int) (hash & MASK)];
        if (e != null && e.hash == hash) {
            return e.name;
        }
        return null;
    }

    /**
     * Store a field name in the cache. Overwrites any existing entry at the same slot.
     */
    static void put(long hash, String name) {
        ENTRIES[(int) (hash & MASK)] = new Entry(name, hash);
    }
}
