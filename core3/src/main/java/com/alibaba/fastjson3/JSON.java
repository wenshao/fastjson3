package com.alibaba.fastjson3;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Main entry point for JSON processing. Provides simple static methods for the most common operations.
 * All methods delegate to {@link ObjectMapper#shared()}.
 *
 * <h3>Quick start:</h3>
 * <pre>
 * // Parse
 * JSONObject obj = JSON.parseObject(jsonStr);
 * User user = JSON.parseObject(jsonStr, User.class);
 * JSONArray arr = JSON.parseArray(jsonStr);
 *
 * // Serialize
 * String json = JSON.toJSONString(obj);
 * byte[] bytes = JSON.toJSONBytes(obj);
 *
 * // Validate
 * boolean valid = JSON.isValid(jsonStr);
 * </pre>
 *
 * <p>For advanced configuration, use {@link ObjectMapper}:</p>
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .enableRead(ReadFeature.AllowComments)
 *     .enableWrite(WriteFeature.PrettyFormat)
 *     .build();
 * </pre>
 */
public final class JSON {
    /**
     * Version of fastjson3.
     */
    public static final String VERSION = "3.0.0-SNAPSHOT";

    private JSON() {
    }

    // ==================== Parse ====================

    /**
     * Parse JSON string to auto-detected type.
     */
    public static Object parse(String json) {
        return ObjectMapper.shared().readValue(json);
    }

    /**
     * Parse JSON string to auto-detected type with features.
     */
    public static Object parse(String json, ReadFeature... features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, features)) {
            return parser.readAny();
        }
    }

    /**
     * Parse JSON string to JSONObject.
     */
    public static JSONObject parseObject(String json) {
        return ObjectMapper.shared().readObject(json);
    }

    /**
     * Parse JSON string to typed Java object.
     */
    public static <T> T parseObject(String json, Class<T> type) {
        return ObjectMapper.shared().readValue(json, type);
    }

    /**
     * Parse JSON string to typed Java object with features.
     */
    public static <T> T parseObject(String json, Class<T> type, ReadFeature... features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, features)) {
            return parser.read(type);
        }
    }

    /**
     * Parse JSON string to generic type.
     */
    public static <T> T parseObject(String json, Type type) {
        return ObjectMapper.shared().readValue(json, type);
    }

    /**
     * Parse JSON string to generic type using TypeReference.
     */
    public static <T> T parseObject(String json, TypeReference<T> typeRef) {
        return ObjectMapper.shared().readValue(json, typeRef);
    }

    /**
     * Parse JSON bytes (UTF-8) to JSONObject.
     */
    public static JSONObject parseObject(byte[] jsonBytes) {
        return ObjectMapper.shared().readObject(jsonBytes);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed Java object.
     */
    public static <T> T parseObject(byte[] jsonBytes, Class<T> type) {
        return ObjectMapper.shared().readValue(jsonBytes, type);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed Java object with features.
     */
    public static <T> T parseObject(byte[] jsonBytes, Class<T> type, ReadFeature... features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
            return parser.read(type);
        }
    }

    /**
     * Parse JSON string to JSONArray.
     */
    public static JSONArray parseArray(String json) {
        return ObjectMapper.shared().readArray(json);
    }

    /**
     * Parse JSON bytes (UTF-8) to JSONArray.
     */
    public static JSONArray parseArray(byte[] jsonBytes) {
        return ObjectMapper.shared().readArray(jsonBytes);
    }

    /**
     * Parse JSON string to typed list.
     */
    public static <T> List<T> parseArray(String json, Class<T> type) {
        return ObjectMapper.shared().readList(json, type);
    }

    // ==================== Serialize ====================

    /**
     * Serialize object to JSON string.
     */
    public static String toJSONString(Object obj) {
        return ObjectMapper.shared().writeValueAsString(obj);
    }

    /**
     * Serialize object to JSON string with features.
     */
    public static String toJSONString(Object obj, WriteFeature... features) {
        if (obj == null) {
            return "null";
        }
        try (JSONGenerator generator = JSONGenerator.of(features)) {
            generator.writeAny(obj);
            String json = generator.toString();
            long featureMask = WriteFeature.of(features);
            if ((featureMask & WriteFeature.PrettyFormat.mask) != 0) {
                return ObjectMapper.prettyFormat(json);
            }
            return json;
        }
    }

    /**
     * Serialize object to UTF-8 byte array.
     */
    public static byte[] toJSONBytes(Object obj) {
        if (obj == null) {
            return NULL_BYTES;
        }
        ObjectMapper mapper = ObjectMapper.shared();
        try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
            @SuppressWarnings("unchecked")
            ObjectWriter<Object> writer =
                    (ObjectWriter<Object>) mapper.getObjectWriter(obj.getClass());
            if (writer != null) {
                writer.write(generator, obj, null, null, 0);
            } else {
                generator.writeAny(obj);
            }
            return generator.toByteArray();
        }
    }

    /**
     * Serialize object to UTF-8 byte array with features.
     */
    public static byte[] toJSONBytes(Object obj, WriteFeature... features) {
        if (obj == null) {
            return NULL_BYTES;
        }
        try (JSONGenerator generator = JSONGenerator.ofUTF8(features)) {
            generator.writeAny(obj);
            return generator.toByteArray();
        }
    }

    // ==================== Validate ====================

    /**
     * Check if a string is valid JSON.
     */
    public static boolean isValid(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Check if a byte array is valid JSON (UTF-8).
     */
    public static boolean isValid(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return false;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid JSON object.
     */
    public static boolean isValidObject(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        if (firstNonWhitespace(json) != '{') {
            return false;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Check if a byte array is a valid JSON object (UTF-8).
     */
    public static boolean isValidObject(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return false;
        }
        if (firstNonWhitespace(jsonBytes) != '{') {
            return false;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid JSON array.
     */
    public static boolean isValidArray(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        if (firstNonWhitespace(json) != '[') {
            return false;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Check if a byte array is a valid JSON array (UTF-8).
     */
    public static boolean isValidArray(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return false;
        }
        if (firstNonWhitespace(jsonBytes) != '[') {
            return false;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static int firstNonWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return c;
            }
        }
        return -1;
    }

    private static int firstNonWhitespace(byte[] bytes) {
        for (byte b : bytes) {
            if (b != ' ' && b != '\t' && b != '\n' && b != '\r') {
                return b & 0xFF;
            }
        }
        return -1;
    }

    // ==================== Convenience ====================

    /**
     * Create an empty JSONObject.
     */
    public static JSONObject object() {
        return new JSONObject();
    }

    /**
     * Create a JSONObject with one key-value pair.
     */
    public static JSONObject object(String key, Object value) {
        JSONObject obj = new JSONObject(4);
        obj.put(key, value);
        return obj;
    }

    /**
     * Create an empty JSONArray.
     */
    public static JSONArray array() {
        return new JSONArray();
    }

    /**
     * Create a JSONArray with initial elements.
     */
    public static JSONArray array(Object... items) {
        JSONArray arr = new JSONArray(items.length);
        for (Object item : items) {
            arr.add(item);
        }
        return arr;
    }

    private static final byte[] NULL_BYTES = {'n', 'u', 'l', 'l'};

    // ==================== JSONPath ====================

    /**
     * Evaluate a JSONPath expression on a JSON string.
     *
     * <pre>
     * String title = JSON.eval(json, "$.store.book[0].title", String.class);
     * </pre>
     */
    public static <T> T eval(String json, String path, Class<T> type) {
        return JSONPath.eval(json, path, type);
    }
}
