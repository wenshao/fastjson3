package com.alibaba.fastjson3;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Represents a JSON object. Maintains insertion order.
 *
 * <p>Extends LinkedHashMap for API compatibility, but all operations delegate to
 * an internal {@link JSONObjectMap} (flat array-backed map) for better performance.
 * The inner map avoids per-entry Node allocation and hashCode computation overhead.</p>
 *
 * <p>Users can configure a custom map implementation via
 * {@link #setMapCreator(Supplier)}. When set, JSONObject falls back to
 * LinkedHashMap behavior with the custom map as backing storage.</p>
 *
 * <pre>
 * JSONObject obj = new JSONObject();
 * obj.put("name", "test");
 * String name = obj.getString("name");
 * </pre>
 */
public class JSONObject extends LinkedHashMap<String, Object> {
    /**
     * Global map creator for customizing JSONObject's backing map.
     * When null (default), uses built-in JSONObjectMap.
     * When set, JSONObject uses LinkedHashMap (super) directly — no inner map.
     *
     * <pre>
     * // Use standard LinkedHashMap
     * JSONObject.setMapCreator(LinkedHashMap::new);
     *
     * // Use custom ordered map
     * JSONObject.setMapCreator(() -> new MyOrderedMap());
     * </pre>
     */
    private static volatile Supplier<Map<String, Object>> mapCreator;

    /**
     * Inner map — null when using LinkedHashMap fallback (mapCreator is set,
     * or constructed via {@link #JSONObject(Map)}).
     */
    private transient JSONObjectMap innerMap;

    public static void setMapCreator(Supplier<Map<String, Object>> creator) {
        mapCreator = creator;
    }

    public static Supplier<Map<String, Object>> getMapCreator() {
        return mapCreator;
    }

    public JSONObject() {
        if (mapCreator == null) {
            innerMap = new JSONObjectMap();
        }
    }

    public JSONObject(int initialCapacity) {
        if (mapCreator == null) {
            innerMap = new JSONObjectMap(initialCapacity);
        } else {
            // LinkedHashMap mode — super already initialized
        }
    }

    public JSONObject(Map<String, Object> map) {
        super(map);
        // No inner map — use LinkedHashMap with pre-populated data
    }

    // ==================== Map method overrides ====================
    // All delegated to innerMap when present, super (LinkedHashMap) otherwise.

    @Override
    public int size() {
        return innerMap != null ? innerMap.size() : super.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap != null ? innerMap.isEmpty() : super.isEmpty();
    }

    @Override
    public Object get(Object key) {
        return innerMap != null ? innerMap.get(key) : super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap != null ? innerMap.containsKey(key) : super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap != null ? innerMap.containsValue(value) : super.containsValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        return innerMap != null ? innerMap.put(key, value) : super.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return innerMap != null ? innerMap.remove(key) : super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        if (innerMap != null) {
            for (Map.Entry<? extends String, ?> e : m.entrySet()) {
                innerMap.put(e.getKey(), e.getValue());
            }
        } else {
            super.putAll(m);
        }
    }

    @Override
    public void clear() {
        if (innerMap != null) {
            innerMap.clear();
        } else {
            super.clear();
        }
    }

    @Override
    public Set<String> keySet() {
        return innerMap != null ? innerMap.keySet() : super.keySet();
    }

    @Override
    public Collection<Object> values() {
        return innerMap != null ? innerMap.values() : super.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return innerMap != null ? innerMap.entrySet() : super.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        if (innerMap != null) {
            innerMap.forEach(action);
        } else {
            super.forEach(action);
        }
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (innerMap != null) {
            Object v = innerMap.get(key);
            return v != null || innerMap.containsKey(key) ? v : defaultValue;
        }
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        if (innerMap != null) {
            Object v = innerMap.get(key);
            if (v == null && !innerMap.containsKey(key)) {
                innerMap.put(key, value);
                return null;
            }
            return v;
        }
        return super.putIfAbsent(key, value);
    }

    /**
     * Fast put for parser use. Uses inner map's put (more efficient than LinkedHashMap).
     * Package-private, only called from JSONParser.readObject().
     */
    void fastPut(String key, Object value) {
        if (innerMap != null) {
            innerMap.put(key, value);
        } else {
            super.put(key, value);
        }
    }

    /**
     * Fluent put — returns this for chaining.
     */
    public JSONObject fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    public String getString(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return (String) val;
        }
        return val.toString();
    }

    public Integer getInteger(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Integer.parseInt(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to Integer: " + val);
    }

    public int getIntValue(String key) {
        Integer val = getInteger(key);
        return val == null ? 0 : val;
    }

    public Long getLong(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Long.parseLong(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to Long: " + val);
    }

    public long getLongValue(String key) {
        Long val = getLong(key);
        return val == null ? 0L : val;
    }

    public Boolean getBoolean(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return "true".equalsIgnoreCase(str);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to Boolean: " + val);
    }

    public boolean getBooleanValue(String key) {
        Boolean val = getBoolean(key);
        return val != null && val;
    }

    public BigDecimal getBigDecimal(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return new BigDecimal(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to BigDecimal: " + val);
    }

    public BigInteger getBigInteger(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }
        if (val instanceof BigDecimal) {
            return ((BigDecimal) val).toBigInteger();
        }
        if (val instanceof Number) {
            return BigInteger.valueOf(((Number) val).longValue());
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return new BigInteger(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to BigInteger: " + val);
    }

    public Double getDouble(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Double) {
            return (Double) val;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Double.parseDouble(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to Double: " + val);
    }

    public double getDoubleValue(String key) {
        Double val = getDouble(key);
        return val == null ? 0D : val;
    }

    public Float getFloat(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Float) {
            return (Float) val;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Float.parseFloat(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to Float: " + val);
    }

    public float getFloatValue(String key) {
        Float val = getFloat(key);
        return val == null ? 0F : val;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJSONObject(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            return (JSONObject) val;
        }
        if (val instanceof Map) {
            JSONObject obj = new JSONObject(((Map<String, Object>) val).size());
            obj.putAll((Map<String, Object>) val);
            return obj;
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to JSONObject: " + val);
    }

    public JSONArray getJSONArray(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONArray) {
            return (JSONArray) val;
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] from "
            + val.getClass().getSimpleName() + " to JSONArray: " + val);
    }

    // ==================== Typed getters ====================

    /**
     * Get value and convert to the specified type.
     */
    public <T> T getObject(String key, Class<T> type) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (type.isInstance(val)) {
            return type.cast(val);
        }
        return ObjectMapper.shared().convertValue(val, type);
    }

    /**
     * Get a List of typed elements.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> elementType) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof List<?> list) {
            if (list.isEmpty()) {
                return (List<T>) list;
            }
            if (elementType.isInstance(list.get(0))) {
                return (List<T>) list;
            }
            // Convert via byte[] round-trip (generic TypeReference not supported by convertValue)
            byte[] json = JSON.toJSONBytes(list);
            return ObjectMapper.shared().readList(json, elementType);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to List: " + val.getClass().getSimpleName());
    }

    // ==================== Date/Time getters ====================

    public Date getDate(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            return (Date) val;
        }
        if (val instanceof Number) {
            return new Date(((Number) val).longValue());
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseDate(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to Date: " + val.getClass().getSimpleName());
    }

    public Instant getInstant(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Instant) {
            return (Instant) val;
        }
        if (val instanceof Number) {
            return Instant.ofEpochMilli(((Number) val).longValue());
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseInstant(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to Instant: " + val.getClass().getSimpleName());
    }

    public LocalDate getLocalDate(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof LocalDate) {
            return (LocalDate) val;
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseLocalDate(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to LocalDate: " + val.getClass().getSimpleName());
    }

    public LocalDateTime getLocalDateTime(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof LocalDateTime) {
            return (LocalDateTime) val;
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseLocalDateTime(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to LocalDateTime: " + val.getClass().getSimpleName());
    }

    public LocalTime getLocalTime(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof LocalTime) {
            return (LocalTime) val;
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseLocalTime(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to LocalTime: " + val.getClass().getSimpleName());
    }

    public java.time.OffsetDateTime getOffsetDateTime(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof java.time.OffsetDateTime) {
            return (java.time.OffsetDateTime) val;
        }
        if (val instanceof String str) {
            return com.alibaba.fastjson3.util.DateUtils.parseOffsetDateTime(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to OffsetDateTime: " + val.getClass().getSimpleName());
    }

    /**
     * Get a byte array value (Base64 decoded if the value is a String).
     */
    public byte[] getBytes(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof byte[]) {
            return (byte[]) val;
        }
        if (val instanceof String str) {
            return java.util.Base64.getDecoder().decode(str);
        }
        throw new JSONException("Cannot cast JSONObject['" + key + "'] to byte[]: " + val.getClass().getSimpleName());
    }

    // ==================== Byte/Short getters ====================

    public Byte getByte(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Byte) {
            return (Byte) val;
        }
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        if (val instanceof String str && !str.isEmpty()) {
            return Byte.parseByte(str);
        }
        return null;
    }

    public byte getByteValue(String key) {
        Byte val = getByte(key);
        return val == null ? 0 : val;
    }

    public Short getShort(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Short) {
            return (Short) val;
        }
        if (val instanceof Number) {
            return ((Number) val).shortValue();
        }
        if (val instanceof String str && !str.isEmpty()) {
            return Short.parseShort(str);
        }
        return null;
    }

    public short getShortValue(String key) {
        Short val = getShort(key);
        return val == null ? 0 : val;
    }

    // ==================== Fluent builders ====================

    /**
     * Create and put a new JSONObject, returning the child for chaining.
     */
    public JSONObject putObject(String key) {
        JSONObject child = new JSONObject();
        put(key, child);
        return child;
    }

    /**
     * Create and put a new JSONArray, returning the child for chaining.
     */
    public JSONArray putArray(String key) {
        JSONArray child = new JSONArray();
        put(key, child);
        return child;
    }

    // ==================== JSONPath access ====================

    /**
     * Evaluate a JSONPath expression on this object.
     */
    public Object getByPath(String jsonPath) {
        return JSONPath.of(jsonPath).eval(this);
    }

    // ==================== Conversion ====================

    /**
     * Convert to typed Java object.
     */
    public <T> T toJavaObject(Class<T> clazz) {
        return ObjectMapper.shared().convertValue(this, clazz);
    }

    /**
     * Convert to typed Java object using TypeReference (for generics).
     */
    @SuppressWarnings("unchecked")
    public <T> T toJavaObject(TypeReference<T> typeRef) {
        return ObjectMapper.shared().convertValue(this, typeRef);
    }

    /**
     * Convert to typed Java object using generic Type.
     */
    @SuppressWarnings("unchecked")
    public <T> T toJavaObject(Type type) {
        if (type instanceof Class<?> clazz) {
            return (T) ObjectMapper.shared().convertValue(this, clazz);
        }
        // For generic types, fall back to byte[] round-trip (more efficient than String)
        byte[] json = JSON.toJSONBytes(this);
        return (T) ObjectMapper.shared().readValue(json, type);
    }

    /**
     * Serialize to JSON string with features.
     */
    public String toJSONString(WriteFeature... features) {
        return JSON.toJSONString(this, features);
    }

    /**
     * Convert to JSON string.
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
