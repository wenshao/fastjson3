package com.alibaba.fastjson3;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Main entry point for JSON processing. Provides simple static methods for the most common operations.
 * All methods delegate to {@link ObjectMapper#shared()} by default.
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
 * <h3>Using presets for common configurations:</h3>
 * <pre>
 * // Lenient parsing for config files (allows comments, single quotes)
 * User user = JSON.parse(configJson, User.class, ParseConfig.LENIENT);
 *
 * // Pretty output for logging
 * String json = JSON.write(obj, WriteConfig.PRETTY);
 *
 * // Strict validation for APIs
 * User user = JSON.parse(apiJson, User.class, ParseConfig.STRICT);
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
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return parseObject(jsonBytes, type);
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
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ObjectMapper.shared().readValue(jsonBytes, type);
    }

    /**
     * Parse JSON string to generic type using TypeReference.
     */
    public static <T> T parseObject(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ObjectMapper.shared().readValue(jsonBytes, typeRef.getType());
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
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return parseArray(jsonBytes);
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
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ObjectMapper.shared().readList(jsonBytes, type);
    }

    // ==================== Parse: InputStream ====================

    /**
     * Parse JSON from InputStream (UTF-8) to typed Java object.
     */
    public static <T> T parseObject(java.io.InputStream in, Class<T> type) {
        return ObjectMapper.shared().readValue(in, type);
    }

    /**
     * Parse JSON from InputStream (UTF-8) to JSONObject.
     */
    public static JSONObject parseObject(java.io.InputStream in) {
        try {
            byte[] bytes = in.readAllBytes();
            return parseObject(bytes);
        } catch (java.io.IOException e) {
            throw new JSONException("read InputStream error", e);
        }
    }

    /**
     * Parse JSON from InputStream (UTF-8) to generic type.
     */
    public static <T> T parseObject(java.io.InputStream in, Type type) {
        return ObjectMapper.shared().readValue(in, type);
    }

    /**
     * Parse JSON from InputStream (UTF-8) to typed Java object with features.
     */
    public static <T> T parseObject(java.io.InputStream in, Class<T> type, ReadFeature... features) {
        try {
            byte[] bytes = in.readAllBytes();
            return parseObject(bytes, type, features);
        } catch (java.io.IOException e) {
            throw new JSONException("read InputStream error", e);
        }
    }

    // ==================== Parse: Reader ====================

    /**
     * Parse JSON from Reader to typed Java object.
     */
    public static <T> T parseObject(java.io.Reader reader, Class<T> type) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] cbuf = new char[8192];
            int n;
            while ((n = reader.read(cbuf)) != -1) {
                sb.append(cbuf, 0, n);
            }
            return parseObject(sb.toString(), type);
        } catch (java.io.IOException e) {
            throw new JSONException("read Reader error", e);
        }
    }

    // ==================== Serialize ====================

    /**
     * Serialize object to JSON string.
     */
    @SuppressWarnings("unchecked")
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
            return generator.toString();
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

    // ==================== JSON Merge Patch (RFC 7396) ====================

    /**
     * Apply a JSON Merge Patch (RFC 7396) to a target JSON string.
     *
     * <pre>{@code
     * String target = "{\"a\":1,\"b\":2}";
     * String patch = "{\"b\":null,\"c\":3}";
     * String result = JSON.mergePatch(target, patch);
     * // result: {"a":1,"c":3}
     * }</pre>
     *
     * @param target the target JSON string
     * @param patch  the merge patch JSON string
     * @return the merged result as a JSON string
     * @see <a href="https://tools.ietf.org/html/rfc7396">RFC 7396</a>
     */
    public static String mergePatch(String target, String patch) {
        Object targetObj = target != null ? parse(target) : null;
        Object patchObj = parse(patch);
        Object result = mergePatchValue(targetObj, patchObj);
        return toJSONString(result);
    }

    /**
     * Apply a JSON Merge Patch (RFC 7396) to a target value.
     *
     * <p>Both {@code target} and {@code patch} may be any JSON-compatible type
     * (JSONObject, JSONArray, String, Number, Boolean, or {@code null}).
     * When {@code patch} is a {@link JSONObject} and {@code target} is not,
     * {@code target} is treated as an empty JSONObject. When {@code target}
     * is already a {@link JSONObject}, it is mutated in place during the merge
     * (including nested JSONObjects reached through recursion).
     * When {@code patch} is not a JSONObject, the patch value itself is returned,
     * replacing the target entirely.</p>
     *
     * @param target the target value; may be any JSON-compatible type
     * @param patch  the merge patch value; may be any JSON-compatible type
     * @return the merged result
     * @see <a href="https://tools.ietf.org/html/rfc7396">RFC 7396</a>
     */
    public static Object mergePatch(Object target, Object patch) {
        return mergePatchValue(target, patch);
    }

    private static Object mergePatchValue(Object target, Object patch) {
        if (!(patch instanceof JSONObject patchObj)) {
            return patch;
        }

        JSONObject targetObj;
        if (target instanceof JSONObject t) {
            targetObj = t;
        } else {
            targetObj = new JSONObject();
        }

        for (var entry : patchObj.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                targetObj.remove(key);
            } else {
                Object existing = targetObj.get(key);
                targetObj.put(key, mergePatchValue(existing, value));
            }
        }
        return targetObj;
    }

    // ==================== Validate ====================

    /**
     * Check if a string is valid JSON.
     */
    public static boolean isValid(String json) {
        return isValid(json, null);
    }

    /**
     * Check if a byte array is valid JSON (UTF-8).
     */
    public static boolean isValid(byte[] jsonBytes) {
        return isValid(jsonBytes, null);
    }

    /**
     * Check if a string is a valid JSON object.
     */
    public static boolean isValidObject(String json) {
        return isValid(json, '{');
    }

    /**
     * Check if a byte array is a valid JSON object (UTF-8).
     */
    public static boolean isValidObject(byte[] jsonBytes) {
        return isValid(jsonBytes, '{');
    }

    /**
     * Check if a string is a valid JSON array.
     */
    public static boolean isValidArray(String json) {
        return isValid(json, '[');
    }

    /**
     * Check if a byte array is a valid JSON array (UTF-8).
     */
    public static boolean isValidArray(byte[] jsonBytes) {
        return isValid(jsonBytes, '[');
    }

    private static boolean isValid(String json, Character expectedFirstChar) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        if (expectedFirstChar != null && firstNonWhitespace(json) != expectedFirstChar) {
            return false;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            parser.skipValue();
            return parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean isValid(byte[] jsonBytes, Character expectedFirstChar) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return false;
        }
        if (expectedFirstChar != null && firstNonWhitespace(jsonBytes) != expectedFirstChar) {
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

    // ==================== Unified Parse API ====================

    /**
     * Parse JSON string to specified type.
     * <p>This is the unified parsing method that works for any target type.</p>
     *
     * <pre>
     * // Parse to POJO
     * User user = JSON.parse(json, User.class);
     *
     * // Parse to JSON containers
     * JSONObject obj = JSON.parse(json, JSONObject.class);
     * JSONArray arr = JSON.parse(json, JSONArray.class);
     *
     * // Parse with configuration
     * User user = JSON.parse(configJson, User.class, ParseConfig.LENIENT);
     * </pre>
     *
     * @param json JSON string, null or empty returns null
     * @param type target class type
     * @param <T>  target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     * @see #parseObject(String)
     * @see #parseArray(String)
     * @see #parseList(String, Class)
     */
    public static <T> T parse(String json, Class<T> type) {
        return parse(json, type, ParseConfig.DEFAULT);
    }

    /**
     * Parse JSON string to specified type with configuration.
     *
     * <pre>
     * // Lenient parsing for config files
     * User user = JSON.parse(configJson, User.class, ParseConfig.LENIENT);
     *
     * // Strict parsing for API contracts
     * User user = JSON.parse(apiJson, User.class, ParseConfig.STRICT);
     * </pre>
     *
     * @param json   JSON string, null or empty returns null
     * @param type   target class type
     * @param config parsing configuration preset
     * @param <T>    target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, Class<T> type, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, type, config.mask());
    }

    /**
     * Parse JSON bytes (UTF-8) to specified type.
     *
     * @param jsonBytes JSON bytes in UTF-8, null or empty returns null
     * @param type      target class type
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(byte[] jsonBytes, Class<T> type) {
        return parse(jsonBytes, type, ParseConfig.DEFAULT);
    }

    /**
     * Parse JSON bytes (UTF-8) to specified type with configuration.
     *
     * @param jsonBytes JSON bytes in UTF-8, null or empty returns null
     * @param type      target class type
     * @param config    parsing configuration preset
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(byte[] jsonBytes, Class<T> type, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readValue(jsonBytes, type, config.mask());
    }

    /**
     * Parse JSON string to specified type using TypeToken.
     * <p>Use this for generic types like {@code List<T>}, {@code Map<K,V>}, etc.</p>
     *
     * <pre>
     * // Parse to List&lt;User&gt;
     * TypeToken&lt;List&lt;User&gt;&gt; userList = TypeToken.listOf(User.class);
     * List&lt;User&gt; users = JSON.parse(json, userList);
     *
     * // Parse to Map&lt;String, User&gt;
     * TypeToken&lt;Map&lt;String, User&gt;&gt; userMap = TypeToken.mapOf(User.class);
     * Map&lt;String, User&gt; map = JSON.parse(json, userMap);
     *
     * // Parse to complex nested type
     * TypeToken&lt;Map&lt;String, List&lt;User&gt;&gt;&gt; complex =
     *     TypeToken.of(new TypeReference&lt;Map&lt;String, List&lt;User&gt;&gt;&gt;() {});
     * Map&lt;String, List&lt;User&gt;&gt; data = JSON.parse(json, complex);
     * </pre>
     *
     * @param json      JSON string, null or empty returns null
     * @param typeToken type token capturing generic type information
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     * @see TypeToken
     */
    public static <T> T parse(String json, TypeToken<T> typeToken) {
        return parse(json, typeToken, ParseConfig.DEFAULT);
    }

    /**
     * Parse JSON string to specified type using TypeToken with configuration.
     *
     * @param json      JSON string, null or empty returns null
     * @param typeToken type token capturing generic type information
     * @param config    parsing configuration preset
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, TypeToken<T> typeToken, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, typeToken.type(), config.mask());
    }

    /**
     * Parse JSON bytes (UTF-8) to specified type using TypeToken.
     *
     * @param jsonBytes JSON bytes in UTF-8, null or empty returns null
     * @param typeToken type token capturing generic type information
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(byte[] jsonBytes, TypeToken<T> typeToken) {
        return parse(jsonBytes, typeToken, ParseConfig.DEFAULT);
    }

    /**
     * Parse JSON bytes (UTF-8) to specified type using TypeToken with configuration.
     *
     * @param jsonBytes JSON bytes in UTF-8, null or empty returns null
     * @param typeToken type token capturing generic type information
     * @param config    parsing configuration preset
     * @param <T>       target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(byte[] jsonBytes, TypeToken<T> typeToken, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readValue(jsonBytes, typeToken.type(), config.mask());
    }

    /**
     * Parse JSON string with auto-detected type.
     * <p>Returns JSONObject, JSONArray, String, Number, Boolean, or null based on JSON value.</p>
     *
     * @param json JSON string, null or empty returns null
     * @return parsed object with auto-detected type
     * @throws JSONException if JSON syntax is invalid
     */
    public static Object parseAny(String json) {
        return parseAny(json, ParseConfig.DEFAULT);
    }

    /**
     * Parse JSON string with auto-detected type and configuration.
     *
     * @param json   JSON string, null or empty returns null
     * @param config parsing configuration preset
     * @return parsed object with auto-detected type
     * @throws JSONException if JSON syntax is invalid
     */
    public static Object parseAny(String json, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, ReadFeature.valuesFrom(config.mask()))) {
            return parser.readAny();
        }
    }

    // ==================== Collection Type Parsing ====================

    /**
     * Parse JSON string to {@code List<T>}.
     * <p>Convenience method for parsing JSON arrays to typed lists.</p>
     *
     * <pre>
     * List&lt;User&gt; users = JSON.parseList(json, User.class);
     * </pre>
     *
     * @param json       JSON string representing an array
     * @param elementType list element type
     * @param <E>        element type parameter
     * @return typed list, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> List<E> parseList(String json, Class<E> elementType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readList(json, elementType);
    }

    /**
     * Parse JSON string to {@code List<T>} with configuration.
     *
     * <pre>
     * // Lenient parsing for config files
     * List&lt;User&gt; users = JSON.parseList(configJson, User.class, ParseConfig.LENIENT);
     * </pre>
     *
     * @param json        JSON string representing an array
     * @param elementType list element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed list, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> List<E> parseList(String json, Class<E> elementType, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readList(json, elementType, config.mask());
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code List<T>}.
     *
     * @param jsonBytes  JSON bytes representing an array
     * @param elementType list element type
     * @param <E>        element type parameter
     * @return typed list, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> List<E> parseList(byte[] jsonBytes, Class<E> elementType) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readList(jsonBytes, elementType);
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code List<T>} with configuration.
     *
     * @param jsonBytes  JSON bytes representing an array
     * @param elementType list element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed list, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> List<E> parseList(byte[] jsonBytes, Class<E> elementType, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readList(jsonBytes, elementType, config.mask());
    }

    /**
     * Parse JSON string to {@code Set<T>}.
     *
     * <pre>
     * Set&lt;User&gt; users = JSON.parseSet(json, User.class);
     * </pre>
     *
     * @param json       JSON string representing an array
     * @param elementType set element type
     * @param <E>        element type parameter
     * @return typed set, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> java.util.Set<E> parseSet(String json, Class<E> elementType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readSet(json, elementType);
    }

    /**
     * Parse JSON string to {@code Set<T>} with configuration.
     *
     * <pre>
     * Set&lt;User&gt; users = JSON.parseSet(json, User.class, ParseConfig.LENIENT);
     * </pre>
     *
     * @param json        JSON string representing an array
     * @param elementType set element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed set, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> java.util.Set<E> parseSet(String json, Class<E> elementType, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readSet(json, elementType, config.mask());
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code Set<T>}.
     *
     * @param jsonBytes  JSON bytes representing an array
     * @param elementType set element type
     * @param <E>        element type parameter
     * @return typed set, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> java.util.Set<E> parseSet(byte[] jsonBytes, Class<E> elementType) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readSet(jsonBytes, elementType);
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code Set<T>} with configuration.
     *
     * @param jsonBytes  JSON bytes representing an array
     * @param elementType set element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed set, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <E> java.util.Set<E> parseSet(byte[] jsonBytes, Class<E> elementType, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readSet(jsonBytes, elementType, config.mask());
    }

    /**
     * Parse JSON string to {@code Map<String, V>}.
     * <p>Note: JSON object keys are always strings. The key type parameter is omitted
     * for simplicity since JSON only supports string keys.</p>
     *
     * <pre>
     * Map&lt;String, User&gt; users = JSON.parseMap(json, User.class);
     * </pre>
     *
     * @param json      JSON string representing an object
     * @param valueType map value type
     * @param <V>       value type parameter
     * @return typed map, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <V> java.util.Map<String, V> parseMap(String json, Class<V> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readMap(json, valueType);
    }

    /**
     * Parse JSON string to {@code Map<String, V>} with configuration.
     *
     * <pre>
     * Map&lt;String, User&gt; users = JSON.parseMap(json, User.class, ParseConfig.LENIENT);
     * </pre>
     *
     * @param json      JSON string representing an object
     * @param valueType map value type
     * @param config    parsing configuration preset
     * @param <V>       value type parameter
     * @return typed map, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <V> java.util.Map<String, V> parseMap(String json, Class<V> valueType, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readMap(json, valueType, config.mask());
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code Map<String, V>}.
     *
     * @param jsonBytes JSON bytes representing an object
     * @param valueType map value type
     * @param <V>       value type parameter
     * @return typed map, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <V> java.util.Map<String, V> parseMap(byte[] jsonBytes, Class<V> valueType) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readMap(jsonBytes, valueType);
    }

    /**
     * Parse JSON bytes (UTF-8) to {@code Map<String, V>} with configuration.
     *
     * @param jsonBytes JSON bytes representing an object
     * @param valueType map value type
     * @param config    parsing configuration preset
     * @param <V>       value type parameter
     * @return typed map, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <V> java.util.Map<String, V> parseMap(byte[] jsonBytes, Class<V> valueType, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        return ObjectMapper.shared().readMap(jsonBytes, valueType, config.mask());
    }

    /**
     * Parse JSON string to Java array {@code T[]}.
     *
     * <pre>
     * User[] users = JSON.parseTypedArray(json, User.class);
     * </pre>
     *
     * @param json        JSON string representing an array
     * @param elementType array element type
     * @param <E>         element type parameter
     * @return typed Java array, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] parseTypedArray(String json, Class<E> elementType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        List<E> list = ObjectMapper.shared().readList(json, elementType);
        if (list == null) {
            return null;
        }
        return list.toArray((E[]) java.lang.reflect.Array.newInstance(elementType, 0));
    }

    /**
     * Parse JSON string to Java array {@code T[]} with configuration.
     *
     * <pre>
     * User[] users = JSON.parseTypedArray(json, User.class, ParseConfig.LENIENT);
     * </pre>
     *
     * @param json        JSON string representing an array
     * @param elementType array element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed Java array, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] parseTypedArray(String json, Class<E> elementType, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        List<E> list = ObjectMapper.shared().readList(json, elementType, config.mask());
        if (list == null) {
            return null;
        }
        return list.toArray((E[]) java.lang.reflect.Array.newInstance(elementType, 0));
    }

    /**
     * Parse JSON bytes (UTF-8) to Java array {@code T[]}.
     *
     * @param jsonBytes   JSON bytes representing an array
     * @param elementType array element type
     * @param <E>         element type parameter
     * @return typed Java array, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] parseTypedArray(byte[] jsonBytes, Class<E> elementType) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        List<E> list = ObjectMapper.shared().readList(jsonBytes, elementType);
        if (list == null) {
            return null;
        }
        return list.toArray((E[]) java.lang.reflect.Array.newInstance(elementType, 0));
    }

    /**
     * Parse JSON bytes (UTF-8) to Java array {@code T[]} with configuration.
     *
     * @param jsonBytes   JSON bytes representing an array
     * @param elementType array element type
     * @param config      parsing configuration preset
     * @param <E>         element type parameter
     * @return typed Java array, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] parseTypedArray(byte[] jsonBytes, Class<E> elementType, ParseConfig config) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        List<E> list = ObjectMapper.shared().readList(jsonBytes, elementType, config.mask());
        if (list == null) {
            return null;
        }
        return list.toArray((E[]) java.lang.reflect.Array.newInstance(elementType, 0));
    }

    // ==================== Type/TypeReference Support ====================

    /**
     * Parse JSON string to specified type using {@link Type}.
     * <p>This method is kept for compatibility with the existing API.</p>
     *
     * @param json JSON string, null or empty returns null
     * @param type target type
     * @param <T>  target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, type);
    }

    /**
     * Parse JSON string to specified type using {@link Type} with configuration.
     *
     * @param json   JSON string, null or empty returns null
     * @param type   target type
     * @param config parsing configuration preset
     * @param <T>    target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, Type type, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, type, config.mask());
    }

    /**
     * Parse JSON string to specified type using {@link TypeReference}.
     * <p>This method is kept for compatibility with the existing API.</p>
     *
     * @param json    JSON string, null or empty returns null
     * @param typeRef type reference capturing generic type information
     * @param <T>     target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, typeRef);
    }

    /**
     * Parse JSON string to specified type using {@link TypeReference} with configuration.
     *
     * @param json    JSON string, null or empty returns null
     * @param typeRef type reference capturing generic type information
     * @param config  parsing configuration preset
     * @param <T>     target type parameter
     * @return parsed object, or null if input is null/empty
     * @throws JSONException if JSON syntax is invalid or type conversion fails
     */
    public static <T> T parse(String json, TypeReference<T> typeRef, ParseConfig config) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return ObjectMapper.shared().readValue(json, typeRef.getType(), config.mask());
    }

    // ==================== Unified Write API ====================

    /**
     * Serialize object to JSON string.
     * <p>This is the unified serialization method.</p>
     *
     * <pre>
     * String json = JSON.write(obj);
     * </pre>
     *
     * @param obj object to serialize, null returns "null"
     * @return JSON string
     * @throws JSONException if serialization fails
     */
    public static String write(Object obj) {
        return write(obj, WriteConfig.DEFAULT);
    }

    /**
     * Serialize object to JSON string with configuration.
     *
     * <pre>
     * // Pretty output
     * String json = JSON.write(obj, WriteConfig.PRETTY);
     *
     * // With nulls
     * String json = JSON.write(obj, WriteConfig.WITH_NULLS);
     * </pre>
     *
     * @param obj    object to serialize, null returns "null"
     * @param config write configuration preset
     * @return JSON string
     * @throws JSONException if serialization fails
     */
    public static String write(Object obj, WriteConfig config) {
        if (obj == null) {
            return "null";
        }
        return ObjectMapper.shared().writeValueAsString(obj, config.mask());
    }

    /**
     * Serialize object to UTF-8 byte array.
     *
     * <pre>
     * byte[] json = JSON.writeBytes(obj);
     * </pre>
     *
     * @param obj object to serialize, null returns "null" bytes
     * @return JSON bytes in UTF-8
     * @throws JSONException if serialization fails
     */
    public static byte[] writeBytes(Object obj) {
        return writeBytes(obj, WriteConfig.DEFAULT);
    }

    /**
     * Serialize object to UTF-8 byte array with configuration.
     *
     * @param obj    object to serialize, null returns "null" bytes
     * @param config write configuration preset
     * @return JSON bytes in UTF-8
     * @throws JSONException if serialization fails
     */
    public static byte[] writeBytes(Object obj, WriteConfig config) {
        if (obj == null) {
            return NULL_BYTES;
        }
        return ObjectMapper.shared().writeValueAsBytes(obj, config.mask());
    }

    /**
     * Serialize object to pretty formatted JSON string.
     * <p>Convenience method equivalent to {@code write(obj, WriteConfig.PRETTY)}.</p>
     *
     * <pre>
     * String prettyJson = JSON.writePretty(obj);
     * </pre>
     *
     * @param obj object to serialize, null returns "null"
     * @return pretty formatted JSON string
     * @throws JSONException if serialization fails
     */
    public static String writePretty(Object obj) {
        return write(obj, WriteConfig.PRETTY);
    }

    /**
     * Serialize object to compact JSON string.
     * <p>Convenience method equivalent to {@code write(obj, WriteConfig.DEFAULT)}.
     * Note: default output is already compact; this method exists for symmetry.</p>
     *
     * <pre>
     * String compactJson = JSON.writeCompact(obj);
     * </pre>
     *
     * @param obj object to serialize, null returns "null"
     * @return compact JSON string
     * @throws JSONException if serialization fails
     */
    public static String writeCompact(Object obj) {
        return write(obj, WriteConfig.DEFAULT);
    }
}
