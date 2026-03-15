package com.alibaba.fastjson3;

/**
 * Lightweight JSON syntax validator. Does NOT build object trees —
 * uses {@link JSONParser#skipValue()} for fast structural validation.
 *
 * <pre>
 * try (JSONValidator v = JSONValidator.from(json)) {
 *     if (v.validate()) {
 *         System.out.println("Type: " + v.getType());
 *     }
 * }
 * </pre>
 */
public final class JSONValidator implements AutoCloseable {
    public enum Type {
        Object,
        Array,
        Value
    }

    private final JSONParser parser;
    private final Type type;
    private Boolean validateResult;

    private JSONValidator(JSONParser parser, Type type) {
        this.parser = parser;
        this.type = type;
    }

    /**
     * Create a validator for a JSON string.
     */
    public static JSONValidator from(String json) {
        if (json == null || json.isEmpty()) {
            return new JSONValidator(null, null);
        }
        Type type = detectType(json);
        return new JSONValidator(JSONParser.of(json), type);
    }

    /**
     * Create a validator for UTF-8 JSON bytes.
     */
    public static JSONValidator fromUtf8(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return new JSONValidator(null, null);
        }
        Type type = detectType(jsonBytes);
        return new JSONValidator(JSONParser.of(jsonBytes), type);
    }

    /**
     * Validate JSON syntax. Result is cached — safe to call multiple times.
     *
     * @return true if the input is syntactically valid JSON with no trailing content
     */
    public boolean validate() {
        if (validateResult != null) {
            return validateResult;
        }
        if (parser == null) {
            validateResult = false;
            return false;
        }
        try {
            parser.skipValue();
            validateResult = parser.isEnd();
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            validateResult = false;
        }
        return validateResult;
    }

    /**
     * Returns the detected JSON type based on the first non-whitespace character.
     * Calls {@link #validate()} if not already called.
     *
     * @return the type, or null if input was null/empty
     */
    public Type getType() {
        if (validateResult == null) {
            validate();
        }
        return type;
    }

    @Override
    public void close() {
        if (parser != null) {
            parser.close();
        }
    }

    private static Type detectType(String json) {
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                continue;
            }
            return switch (c) {
                case '{' -> Type.Object;
                case '[' -> Type.Array;
                default -> Type.Value;
            };
        }
        return Type.Value;
    }

    private static Type detectType(byte[] bytes) {
        for (byte b : bytes) {
            if (b == ' ' || b == '\t' || b == '\n' || b == '\r') {
                continue;
            }
            return switch (b) {
                case '{' -> Type.Object;
                case '[' -> Type.Array;
                default -> Type.Value;
            };
        }
        return Type.Value;
    }
}
