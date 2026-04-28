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
     * Compute a content-key for short Latin1/ASCII names (≤ 8 bytes).
     * The lower 56 bits hold the raw byte content read as a little-endian long,
     * masked to the byte length; the upper 8 bits hold {@code nameLen} to
     * disambiguate names whose prefixes overlap (e.g. "ab" vs "ab\0\0").
     *
     * <p>Because the key IS the content, a cache hit by key equality implies
     * content equality — no {@code contentEquals} loop needed.
     */
    static long shortKey(byte[] bytes, int off, int nameLen) {
        long w = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(bytes, off);
        long mask = (nameLen == 8) ? -1L : (1L << (nameLen << 3)) - 1;
        return (w & mask) | ((long) nameLen << 56);
    }

    /**
     * Look up by short-content-key. Returns the cached String if the slot's
     * stored key matches exactly AND the cached String's length matches the
     * caller's {@code nameLen}. The length check is required because
     * {@link #put} is also used by the long-name path
     * ({@code put(hash, name)}), which can compute a hash that happens to
     * equal a short-key value (most importantly: a long fuzz-generated
     * name whose accumulated {@code hash * 31 + c} polynomial evaluates to
     * {@code 0L} collides with the empty-name short-key, also {@code 0L}).
     * Without the length check, the long name would be returned for
     * subsequent empty-name lookups, and the JSONObject would receive a
     * wrong-key entry.
     *
     * <p>The caller passes {@code nameLen} explicitly rather than letting
     * us extract it from the key's upper 8 bits — that encoding only works
     * for {@code nameLen ∈ [0, 7]}; for {@code nameLen == 8} the content
     * byte at offset 7 occupies the same bits and any 8-byte ASCII name
     * yields a wrong nameLen reading. The parser always knows {@code
     * nameLen} at the call site, so passing it through is both correct and
     * cheaper than the bit extract.
     */
    static String getShort(long key, int nameLen) {
        int idx = (int) (key & MASK);
        if (HASHES[idx] == key) {
            String cached = NAMES[idx];
            if (cached != null && cached.length() == nameLen) {
                return cached;
            }
        }
        return null;
    }

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
     *
     * <p>Fast path: on JDK 9+ with COMPACT_STRINGS, a Latin1 String's internal
     * value[] is a byte[] with the same bytes we're comparing against. Read
     * it via Unsafe and do direct byte-to-byte compare, using long-word
     * batching for bodies ≥ 8 bytes. Much faster than {@code s.charAt(i)}
     * which unpacks each byte to a char.
     *
     * <p>Profile (x64 Tree parse): StringLatin1.charAt was 5.66% of CPU,
     * contentEquals itself 9.88%. Together roughly 15% of parse time spent
     * in this one check.
     */
    private static boolean contentEquals(String s, byte[] bytes, int off, int len) {
        if (s.length() != len) {
            return false;
        }
        if (com.alibaba.fastjson3.util.JDKUtils.UNSAFE_AVAILABLE
                && com.alibaba.fastjson3.util.JDKUtils.getStringCoder(s) == 0) {
            byte[] sv = (byte[]) com.alibaba.fastjson3.util.JDKUtils.getStringValue(s);
            if (sv != null) {
                // SWAR long-compare for ≥ 8 byte bodies.
                int i = 0;
                int swarLimit = len - 7;
                while (i < swarLimit) {
                    long a = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(sv, i);
                    long b = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(bytes, off + i);
                    if (a != b) return false;
                    i += 8;
                }
                while (i < len) {
                    if (sv[i] != bytes[off + i]) return false;
                    i++;
                }
                return true;
            }
        }
        // Fallback: char-based compare for UTF16 Strings or pre-JDK 9.
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != (bytes[off + i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
