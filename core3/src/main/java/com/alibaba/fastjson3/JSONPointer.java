package com.alibaba.fastjson3;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Pointer (RFC 6901) implementation.
 * <p>
 * A JSON Pointer is a string syntax for identifying a specific value within a JSON document.
 * Example: {@code /foo/bar/0} refers to the first element of the "bar" array inside "foo".
 *
 * <pre>{@code
 * JSONPointer pointer = JSONPointer.of("/foo/bar/0");
 * Object value = pointer.eval(jsonObject);
 * pointer.set(jsonObject, "newValue");
 * }</pre>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>
 */
public final class JSONPointer {
    private static final JSONPointer ROOT = new JSONPointer("", new String[0]);

    private final String pointer;
    private final String[] tokens;

    private JSONPointer(String pointer, String[] tokens) {
        this.pointer = pointer;
        this.tokens = tokens;
    }

    /**
     * Compile a JSON Pointer string.
     *
     * @param pointer the pointer string (empty string for root, or starting with '/')
     * @return compiled JSONPointer
     * @throws JSONException if the pointer syntax is invalid
     */
    public static JSONPointer of(String pointer) {
        if (pointer == null) {
            throw new JSONException("JSON Pointer must not be null");
        }
        if (pointer.isEmpty()) {
            return ROOT;
        }
        if (pointer.charAt(0) != '/') {
            throw new JSONException("JSON Pointer must start with '/' or be empty: " + pointer);
        }
        String[] tokens = parseTokens(pointer);
        return new JSONPointer(pointer, tokens);
    }

    /**
     * Evaluate this pointer against a JSON document.
     * <p>
     * Returns {@code null} when the pointer path is not found in the document
     * (e.g., missing key in an object, index out of bounds in an array,
     * or traversal through a non-container value).
     *
     * @param root the root JSON document (JSONObject, JSONArray, or value)
     * @return the value at the pointer location, or {@code null} if the path is not found
     */
    public Object eval(Object root) {
        Object current = root;
        for (String token : tokens) {
            if (current instanceof JSONObject obj) {
                if (!obj.containsKey(token)) {
                    return null;
                }
                current = obj.get(token);
            } else if (current instanceof JSONArray arr) {
                int index = parseIndex(token, arr.size());
                if (index < 0 || index >= arr.size()) {
                    return null;
                }
                current = arr.get(index);
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Evaluate this pointer and return a typed result.
     */
    @SuppressWarnings("unchecked")
    public <T> T eval(Object root, Class<T> type) {
        Object value = eval(root);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (type == String.class) {
            return (T) value.toString();
        }
        if (type == Integer.class || type == int.class) {
            if (!(value instanceof Number)) {
                throw new JSONException("Cannot convert " + value.getClass().getName() + " to " + type.getName());
            }
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (type == Long.class || type == long.class) {
            if (!(value instanceof Number)) {
                throw new JSONException("Cannot convert " + value.getClass().getName() + " to " + type.getName());
            }
            return (T) Long.valueOf(((Number) value).longValue());
        }
        if (type == Double.class || type == double.class) {
            if (!(value instanceof Number)) {
                throw new JSONException("Cannot convert " + value.getClass().getName() + " to " + type.getName());
            }
            return (T) Double.valueOf(((Number) value).doubleValue());
        }
        if (type == Boolean.class || type == boolean.class) {
            if (value instanceof String s) {
                return (T) Boolean.valueOf(s);
            }
            if (value instanceof Number n) {
                return (T) Boolean.valueOf(n.intValue() != 0);
            }
            throw new JSONException("Cannot convert " + value.getClass().getName() + " to " + type.getName());
        }
        throw new JSONException("Cannot convert " + value.getClass().getName() + " to " + type.getName());
    }

    /**
     * Set the value at this pointer location.
     *
     * @param root  the root JSON document
     * @param value the value to set
     * @throws JSONException if the path cannot be resolved
     */
    public void set(Object root, Object value) {
        if (tokens.length == 0) {
            throw new JSONException("Cannot set root document via JSON Pointer");
        }
        Object parent = root;
        for (int i = 0; i < tokens.length - 1; i++) {
            parent = resolveParent(parent, tokens[i]);
        }
        String lastToken = tokens[tokens.length - 1];
        setOnParent(parent, lastToken, value);
    }

    /**
     * Remove the value at this pointer location.
     *
     * @param root the root JSON document
     * @throws JSONException if the path cannot be resolved
     */
    public void remove(Object root) {
        if (tokens.length == 0) {
            throw new JSONException("Cannot remove root document via JSON Pointer");
        }
        Object parent = root;
        for (int i = 0; i < tokens.length - 1; i++) {
            parent = resolveParent(parent, tokens[i]);
        }
        String lastToken = tokens[tokens.length - 1];
        removeFromParent(parent, lastToken);
    }

    /**
     * Check if the value at this pointer location exists.
     */
    public boolean exists(Object root) {
        Object current = root;
        for (String token : tokens) {
            if (current instanceof JSONObject obj) {
                if (!obj.containsKey(token)) {
                    return false;
                }
                current = obj.get(token);
            } else if (current instanceof JSONArray arr) {
                int index;
                try {
                    index = parseIndex(token, arr.size());
                } catch (JSONException e) {
                    return false;
                }
                if (index < 0 || index >= arr.size()) {
                    return false;
                }
                current = arr.get(index);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the raw pointer string.
     */
    public String getPointer() {
        return pointer;
    }

    /**
     * Get the parsed tokens.
     */
    public String[] getTokens() {
        return tokens.clone();
    }

    @Override
    public String toString() {
        return pointer;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof JSONPointer p && pointer.equals(p.pointer));
    }

    @Override
    public int hashCode() {
        return pointer.hashCode();
    }

    // ==================== Internal ====================

    private Object resolveParent(Object current, String token) {
        if (current instanceof JSONObject obj) {
            Object child = obj.get(token);
            if (child == null) {
                throw new JSONException("JSON Pointer path not found: " + token);
            }
            return child;
        } else if (current instanceof JSONArray arr) {
            int index = parseIndex(token, arr.size());
            if (index < 0 || index >= arr.size()) {
                throw new JSONException("Array index out of bounds: " + token);
            }
            return arr.get(index);
        }
        throw new JSONException("Cannot traverse non-container at: " + token);
    }

    private static void setOnParent(Object parent, String token, Object value) {
        if (parent instanceof JSONObject obj) {
            obj.put(token, value);
        } else if (parent instanceof JSONArray arr) {
            if ("-".equals(token)) {
                arr.add(value);
            } else {
                int index = parseIndex(token, arr.size() + 1);
                if (index > arr.size()) {
                    throw new JSONException("Array index out of bounds: " + token + " (size: " + arr.size() + ")");
                }
                if (index == arr.size()) {
                    arr.add(value);
                } else {
                    arr.add(index, value);
                }
            }
        } else {
            throw new JSONException("Cannot set on non-container");
        }
    }

    private static void removeFromParent(Object parent, String token) {
        if (parent instanceof JSONObject obj) {
            if (!obj.containsKey(token)) {
                throw new JSONException("Cannot remove non-existent key: " + token);
            }
            obj.remove(token);
        } else if (parent instanceof JSONArray arr) {
            int index = parseIndex(token, arr.size());
            if (index < 0 || index >= arr.size()) {
                throw new JSONException("Array index out of bounds: " + token);
            }
            arr.remove(index);
        } else {
            throw new JSONException("Cannot remove from non-container");
        }
    }

    static int parseIndex(String token, int size) {
        if ("-".equals(token)) {
            return size; // past-the-end for add operations
        }
        if (token.isEmpty()) {
            throw new JSONException("Invalid array index: empty string");
        }
        // RFC 6901: leading zeros not allowed (except "0")
        if (token.length() > 1 && token.charAt(0) == '0') {
            throw new JSONException("Invalid array index (leading zero): " + token);
        }
        try {
            int index = Integer.parseInt(token);
            if (index < 0) {
                throw new JSONException("Invalid array index (negative): " + token);
            }
            return index;
        } catch (NumberFormatException e) {
            throw new JSONException("Invalid array index: " + token);
        }
    }

    static String[] parseTokens(String pointer) {
        // Skip leading '/'
        List<String> tokens = new ArrayList<>();
        int i = 1;
        while (i <= pointer.length()) {
            int slash = pointer.indexOf('/', i);
            if (slash < 0) {
                slash = pointer.length();
            }
            tokens.add(unescape(pointer.substring(i, slash)));
            i = slash + 1;
        }
        return tokens.toArray(new String[0]);
    }

    /**
     * Unescape RFC 6901 encoded token: ~1 → /, ~0 → ~
     */
    static String unescape(String token) {
        if (token.indexOf('~') < 0) {
            return token;
        }
        StringBuilder sb = new StringBuilder(token.length());
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '~') {
                if (i + 1 >= token.length()) {
                    throw new JSONException("Invalid escape sequence: trailing '~' at end of token");
                }
                char next = token.charAt(i + 1);
                if (next == '1') {
                    sb.append('/');
                    i++;
                } else if (next == '0') {
                    sb.append('~');
                    i++;
                } else {
                    throw new JSONException("Invalid escape sequence: ~" + next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape a token for use in a JSON Pointer: / → ~1, ~ → ~0
     */
    public static String escape(String token) {
        if (token.indexOf('~') < 0 && token.indexOf('/') < 0) {
            return token;
        }
        return token.replace("~", "~0").replace("/", "~1");
    }
}
