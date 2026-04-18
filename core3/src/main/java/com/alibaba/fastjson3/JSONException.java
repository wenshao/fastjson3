package com.alibaba.fastjson3;

import java.util.ArrayList;
import java.util.List;

/**
 * Unchecked exception for all JSON processing errors.
 *
 * <p>Supports an optional JSON path breadcrumb chain for errors encountered
 * during structured parsing. Callers higher in the stack can call
 * {@link #prependPath(Object)} on a caught {@code JSONException} to attach the
 * field name, array index, or map key that led to the inner error — the
 * formatted {@link #getMessage()} then shows e.g.
 * {@code "expected '{' at offset 42 (path: users[1].address.city)"}.
 */
public class JSONException extends RuntimeException {
    private final long offset;
    private final String rawMessage;
    /**
     * Segments collected as the exception propagates upward. The innermost
     * segment is at index 0; outer segments are appended via
     * {@link #prependPath(Object)}. Formatted outermost-first in
     * {@link #getMessage()}. Lazily allocated — {@code null} when no path has
     * been attached (the overwhelmingly common case at throw sites).
     */
    private List<Object> path;

    public JSONException(String message) {
        super(message);
        this.rawMessage = message;
        this.offset = -1;
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
        this.rawMessage = message;
        this.offset = -1;
    }

    public JSONException(String message, long offset) {
        super(message);
        this.rawMessage = message;
        this.offset = offset;
    }

    public JSONException(String message, long offset, Throwable cause) {
        super(message, cause);
        this.rawMessage = message;
        this.offset = offset;
    }

    /**
     * The byte/char offset in the input where the error occurred, or -1 if unknown.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Attach a path segment naming the field, array index, or map key that
     * contained the value whose parse failed. Called during stack unwind, so
     * segments arrive innermost-first. Returns {@code this} to allow
     * {@code throw e.prependPath(...)}.
     *
     * @param segment a {@link String} field name / map key, or an {@link Integer}
     *                array index — other types are formatted via {@link String#valueOf}.
     */
    public JSONException prependPath(Object segment) {
        if (path == null) {
            path = new ArrayList<>(4);
        }
        path.add(segment);
        return this;
    }

    /**
     * Wrap any parse-time failure so the call-site path segment can be
     * attached. Pass-through for {@code JSONException} (preserves offset and
     * original stack). Wraps other throwables — e.g., a {@link ClassCastException}
     * from a type-mismatched primitive field — into a {@code JSONException}
     * so callers above get a uniform exception type carrying the path chain.
     */
    public static JSONException wrapWithPath(Throwable cause, Object segment) {
        JSONException je;
        if (cause instanceof JSONException existing) {
            je = existing;
        } else {
            String message = cause.getClass().getSimpleName()
                    + (cause.getMessage() != null ? ": " + cause.getMessage() : "");
            je = new JSONException(message, -1, cause);
        }
        return je.prependPath(segment);
    }

    /**
     * Unformatted message as originally thrown, without the path suffix.
     * Useful for callers that want to compose their own message.
     */
    public String getRawMessage() {
        return rawMessage;
    }

    /**
     * Path segments in innermost-first order (outermost appended last).
     * {@code null} if no path was attached.
     */
    public List<Object> getPath() {
        return path;
    }

    @Override
    public String getMessage() {
        if (path == null || path.isEmpty()) {
            return rawMessage;
        }
        StringBuilder sb = new StringBuilder(rawMessage.length() + 16 + path.size() * 8);
        sb.append(rawMessage);
        sb.append(" (path: ");
        // Format outermost-first. Array / map-integer-key segments get
        // `[N]` with no preceding dot; field-name segments get a leading dot
        // except at the very start.
        for (int i = path.size() - 1; i >= 0; i--) {
            Object seg = path.get(i);
            boolean isIndex = seg instanceof Integer;
            if (!isIndex && i != path.size() - 1) {
                sb.append('.');
            }
            if (isIndex) {
                sb.append('[').append(seg).append(']');
            } else {
                sb.append(seg);
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
