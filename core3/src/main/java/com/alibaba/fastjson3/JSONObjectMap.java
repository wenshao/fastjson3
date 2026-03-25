package com.alibaba.fastjson3;

import java.util.*;

/**
 * Flat ordered map optimized for JSON objects. Uses parallel arrays instead of
 * separate Entry nodes, avoiding per-entry allocation overhead.
 *
 * <p>For small maps (≤16 entries), uses linear scan for lookup — this is faster
 * than hash-based lookup due to cache locality and no hashCode computation.
 * For larger maps, builds a hash index on demand.</p>
 *
 * <p>Maintains insertion order (like LinkedHashMap) via array ordering.</p>
 *
 * <p>This is the default backing map for JSONObject in parse mode.</p>
 */
final class JSONObjectMap extends AbstractMap<String, Object> {
    private static final int INITIAL_CAPACITY = 12; // covers most JSON objects (eishay Media has 10 fields)
    private static final int HASH_THRESHOLD = 16;

    String[] keys;
    Object[] values;
    int size;

    // Hash index — built lazily when size > HASH_THRESHOLD
    private int[] hashIndex;
    private int hashMask;

    JSONObjectMap() {
        keys = new String[INITIAL_CAPACITY];
        values = new Object[INITIAL_CAPACITY];
    }

    JSONObjectMap(int capacity) {
        int cap = Math.max(INITIAL_CAPACITY, capacity);
        keys = new String[cap];
        values = new Object[cap];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof String s) {
            return getByString(s);
        }
        return null;
    }

    Object getByString(String key) {
        if (hashIndex != null) {
            return getHashed(key);
        }
        // Reverse scan for small maps
        final String[] k = this.keys;
        for (int i = this.size - 1; i >= 0; i--) {
            if (key.equals(k[i])) {
                return values[i];
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        if (key instanceof String s) {
            return indexOfKey(s) >= 0;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        final Object[] v = this.values;
        final int n = this.size;
        if (value == null) {
            for (int i = 0; i < n; i++) {
                if (v[i] == null) return true;
            }
        } else {
            for (int i = 0; i < n; i++) {
                if (value.equals(v[i])) return true;
            }
        }
        return false;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new JSONException("null key not supported");
        }
        int idx = indexOfKey(key);
        if (idx >= 0) {
            Object old = values[idx];
            values[idx] = value;
            return old;
        }
        // Append
        if (size >= keys.length) {
            grow();
        }
        keys[size] = key;
        values[size] = value;
        size++;
        if (hashIndex != null) {
            addToHash(key, size - 1);
        } else if (size > HASH_THRESHOLD) {
            buildHashIndex();
        }
        return null;
    }

    /**
     * Fast append without duplicate check. Only for internal use where caller
     * manages uniqueness. Note: JSON parsing uses put() instead to handle
     * duplicate keys correctly.
     */
    void putDirect(String key, Object value) {
        if (size >= keys.length) {
            grow();
        }
        keys[size] = key;
        values[size] = value;
        size++;
        // No hash index maintenance for parse mode — maps are small
    }

    @Override
    public Object remove(Object key) {
        if (key instanceof String s) {
            int idx = indexOfKey(s);
            if (idx >= 0) {
                Object old = values[idx];
                int lastIdx = size - 1;
                if (idx < lastIdx) {
                    System.arraycopy(keys, idx + 1, keys, idx, lastIdx - idx);
                    System.arraycopy(values, idx + 1, values, idx, lastIdx - idx);
                }
                keys[lastIdx] = null;
                values[lastIdx] = null;
                size--;
                if (hashIndex != null) {
                    rebuildHashIndex();
                }
                return old;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        Arrays.fill(keys, 0, size, null);
        Arrays.fill(values, 0, size, null);
        size = 0;
        hashIndex = null;
    }

    @Override
    public Set<String> keySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<>() {
                    int pos = 0;
                    @Override public boolean hasNext() { return pos < size; }
                    @Override public String next() { return keys[pos++]; }
                };
            }
            @Override public int size() { return size; }
            @Override public boolean contains(Object o) { return containsKey(o); }
        };
    }

    @Override
    public Collection<Object> values() {
        return new AbstractCollection<>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<>() {
                    int pos = 0;
                    @Override public boolean hasNext() { return pos < size; }
                    @Override public Object next() { return values[pos++]; }
                };
            }
            @Override public int size() { return size; }
        };
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<String, Object>> iterator() {
                return new Iterator<>() {
                    int pos = 0;
                    @Override public boolean hasNext() { return pos < size; }
                    @Override public Entry<String, Object> next() {
                        int i = pos++;
                        return new BackedEntry(i);
                    }
                };
            }
            @Override public int size() { return size; }
        };
    }

    /**
     * Map.Entry backed by the parallel arrays, so setValue() modifies the map.
     */
    private final class BackedEntry implements Entry<String, Object> {
        private final int index;
        BackedEntry(int index) { this.index = index; }
        @Override public String getKey() { return keys[index]; }
        @Override public Object getValue() { return values[index]; }
        @Override public Object setValue(Object value) {
            Object old = values[index];
            values[index] = value;
            return old;
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Entry<?, ?> e)) return false;
            return Objects.equals(getKey(), e.getKey()) && Objects.equals(getValue(), e.getValue());
        }
        @Override public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }
    }

    void replaceAllValues(java.util.function.BiFunction<? super String, ? super Object, ?> function) {
        for (int i = 0; i < size; i++) {
            values[i] = function.apply(keys[i], values[i]);
        }
    }

    @Override
    public void forEach(java.util.function.BiConsumer<? super String, ? super Object> action) {
        final String[] k = this.keys;
        final Object[] v = this.values;
        final int n = this.size;
        for (int i = 0; i < n; i++) {
            action.accept(k[i], v[i]);
        }
    }

    // ---- Internal ----

    private int indexOfKey(String key) {
        if (hashIndex != null) {
            return indexOfKeyHashed(key);
        }
        // Reverse scan for small maps
        final String[] k = this.keys;
        for (int i = this.size - 1; i >= 0; i--) {
            if (key.equals(k[i])) {
                return i;
            }
        }
        return -1;
    }

    private void grow() {
        int newLen = keys.length << 1;
        keys = Arrays.copyOf(keys, newLen);
        values = Arrays.copyOf(values, newLen);
    }

    // ---- Hash index for large maps ----

    private void buildHashIndex() {
        int cap = Integer.highestOneBit(size * 2 - 1) << 1;
        hashMask = cap - 1;
        hashIndex = new int[cap];
        Arrays.fill(hashIndex, -1);
        for (int i = 0; i < size; i++) {
            addToHash(keys[i], i);
        }
    }

    private void rebuildHashIndex() {
        Arrays.fill(hashIndex, -1);
        for (int i = 0; i < size; i++) {
            addToHash(keys[i], i);
        }
    }

    private void addToHash(String key, int idx) {
        int bucket = key.hashCode() & hashMask;
        // Linear probing
        while (hashIndex[bucket] >= 0) {
            bucket = (bucket + 1) & hashMask;
        }
        hashIndex[bucket] = idx;
    }

    private Object getHashed(String key) {
        int bucket = key.hashCode() & hashMask;
        while (true) {
            int idx = hashIndex[bucket];
            if (idx < 0) return null;
            if (key.equals(keys[idx])) return values[idx];
            bucket = (bucket + 1) & hashMask;
        }
    }

    private int indexOfKeyHashed(String key) {
        int bucket = key.hashCode() & hashMask;
        while (true) {
            int idx = hashIndex[bucket];
            if (idx < 0) return -1;
            if (key.equals(keys[idx])) return idx;
            bucket = (bucket + 1) & hashMask;
        }
    }
}
