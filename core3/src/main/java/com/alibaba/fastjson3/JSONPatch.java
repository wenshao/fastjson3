package com.alibaba.fastjson3;

/**
 * JSON Patch (RFC 6902) implementation.
 * <p>
 * A JSON Patch is a JSON document that represents a sequence of operations
 * to apply to a target JSON document.
 *
 * <pre>{@code
 * String target = "{\"foo\":\"bar\"}";
 * String patch = "[{\"op\":\"add\",\"path\":\"/baz\",\"value\":\"qux\"}]";
 * String result = JSONPatch.apply(target, patch);
 * // {"foo":"bar","baz":"qux"}
 * }</pre>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6902">RFC 6902</a>
 */
public final class JSONPatch {
    private JSONPatch() {
    }

    /**
     * Apply a JSON Patch document to a target JSON string.
     *
     * @param target the target JSON string
     * @param patch  the JSON Patch document (JSON array of operations)
     * @return the patched JSON string
     * @throws JSONException if any operation fails
     */
    public static String apply(String target, String patch) {
        Object targetObj = JSON.parse(target);
        JSONArray operations = JSON.parseArray(patch);
        Object result = apply(targetObj, operations);
        return JSON.toJSONString(result);
    }

    /**
     * Apply a JSON Patch array to a target object.
     *
     * @param target     the target object (JSONObject, JSONArray, or value)
     * @param operations the patch operations as a JSONArray
     * @return the patched object
     * @throws JSONException if any operation fails
     */
    public static Object apply(Object target, JSONArray operations) {
        Object current = target;
        for (int i = 0; i < operations.size(); i++) {
            Object op = operations.get(i);
            if (!(op instanceof JSONObject operation)) {
                throw new JSONException("JSON Patch operation must be an object at index " + i);
            }
            current = applyOperation(current, operation, i);
        }
        return current;
    }

    private static Object applyOperation(Object target, JSONObject operation, int index) {
        String op = operation.getString("op");
        if (op == null) {
            throw new JSONException("JSON Patch operation missing 'op' at index " + index);
        }
        String path = operation.getString("path");
        if (path == null) {
            throw new JSONException("JSON Patch operation missing 'path' at index " + index);
        }

        return switch (op) {
            case "add" -> applyAdd(target, path, requireValue(operation, index));
            case "remove" -> applyRemove(target, path);
            case "replace" -> applyReplace(target, path, requireValue(operation, index));
            case "move" -> applyMove(target, path, requireFrom(operation, index));
            case "copy" -> applyCopy(target, path, requireFrom(operation, index));
            case "test" -> applyTest(target, path, requireValue(operation, index));
            default -> throw new JSONException("Unknown JSON Patch operation: " + op);
        };
    }

    private static Object requireValue(JSONObject operation, int index) {
        if (!operation.containsKey("value")) {
            throw new JSONException("JSON Patch operation missing 'value' at index " + index);
        }
        return operation.get("value");
    }

    private static String requireFrom(JSONObject operation, int index) {
        String from = operation.getString("from");
        if (from == null) {
            throw new JSONException("JSON Patch operation missing 'from' at index " + index);
        }
        return from;
    }

    // ==================== Operations ====================

    private static Object applyAdd(Object target, String path, Object value) {
        if (path.isEmpty()) {
            // Replace the entire document
            return value;
        }
        JSONPointer pointer = JSONPointer.of(path);
        pointer.set(target, deepCopy(value));
        return target;
    }

    private static Object applyRemove(Object target, String path) {
        if (path.isEmpty()) {
            throw new JSONException("Cannot remove root document");
        }
        JSONPointer pointer = JSONPointer.of(path);
        pointer.remove(target);
        return target;
    }

    private static Object applyReplace(Object target, String path, Object value) {
        if (path.isEmpty()) {
            return deepCopy(value);
        }
        JSONPointer pointer = JSONPointer.of(path);
        if (!pointer.exists(target)) {
            throw new JSONException("JSON Patch replace: path does not exist: " + path);
        }
        // For arrays, replace means set at index (not insert)
        String[] tokens = pointer.getTokens();
        Object parent = target;
        for (int i = 0; i < tokens.length - 1; i++) {
            parent = resolveForReplace(parent, tokens[i]);
        }
        String lastToken = tokens[tokens.length - 1];
        if (parent instanceof JSONObject obj) {
            obj.put(lastToken, deepCopy(value));
        } else if (parent instanceof JSONArray arr) {
            int idx = JSONPointer.parseIndex(lastToken, arr.size());
            arr.set(idx, deepCopy(value));
        }
        return target;
    }

    private static Object applyMove(Object target, String path, String from) {
        JSONPointer fromPointer = JSONPointer.of(from);
        Object value = fromPointer.eval(target);
        if (value == null && !fromPointer.exists(target)) {
            throw new JSONException("JSON Patch move: source path does not exist: " + from);
        }
        fromPointer.remove(target);
        return applyAdd(target, path, value);
    }

    private static Object applyCopy(Object target, String path, String from) {
        JSONPointer fromPointer = JSONPointer.of(from);
        Object value = fromPointer.eval(target);
        if (value == null && !fromPointer.exists(target)) {
            throw new JSONException("JSON Patch copy: source path does not exist: " + from);
        }
        return applyAdd(target, path, deepCopy(value));
    }

    private static Object applyTest(Object target, String path, Object expected) {
        JSONPointer pointer = JSONPointer.of(path);
        Object actual = pointer.eval(target);
        if (!deepEquals(actual, expected)) {
            throw new JSONException("JSON Patch test failed at " + path
                    + ": expected " + JSON.toJSONString(expected)
                    + " but got " + JSON.toJSONString(actual));
        }
        return target;
    }

    // ==================== Helpers ====================

    private static Object resolveForReplace(Object current, String token) {
        if (current instanceof JSONObject obj) {
            return obj.get(token);
        } else if (current instanceof JSONArray arr) {
            int idx = JSONPointer.parseIndex(token, arr.size());
            return arr.get(idx);
        }
        throw new JSONException("Cannot traverse: " + token);
    }

    /**
     * Deep copy a JSON value (JSONObject, JSONArray, or primitive).
     */
    static Object deepCopy(Object value) {
        if (value instanceof JSONObject obj) {
            JSONObject copy = new JSONObject(obj.size());
            for (var entry : obj.entrySet()) {
                copy.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof JSONArray arr) {
            JSONArray copy = new JSONArray(arr.size());
            for (Object item : arr) {
                copy.add(deepCopy(item));
            }
            return copy;
        }
        // Primitives (String, Number, Boolean, null) are immutable
        return value;
    }

    /**
     * Deep equality comparison for JSON values.
     */
    static boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a instanceof JSONObject oa && b instanceof JSONObject ob) {
            if (oa.size() != ob.size()) return false;
            for (var entry : oa.entrySet()) {
                if (!ob.containsKey(entry.getKey())) return false;
                if (!deepEquals(entry.getValue(), ob.get(entry.getKey()))) return false;
            }
            return true;
        }
        if (a instanceof JSONArray aa && b instanceof JSONArray ab) {
            if (aa.size() != ab.size()) return false;
            for (int i = 0; i < aa.size(); i++) {
                if (!deepEquals(aa.get(i), ab.get(i))) return false;
            }
            return true;
        }
        if (a instanceof Number na && b instanceof Number nb) {
            return na.doubleValue() == nb.doubleValue();
        }
        return a.equals(b);
    }
}
