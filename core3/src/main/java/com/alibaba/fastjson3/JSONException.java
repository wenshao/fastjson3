package com.alibaba.fastjson3;

/**
 * Unchecked exception for all JSON processing errors.
 */
public class JSONException extends RuntimeException {
    private final long offset;

    public JSONException(String message) {
        super(message);
        this.offset = -1;
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
        this.offset = -1;
    }

    public JSONException(String message, long offset) {
        super(message);
        this.offset = offset;
    }

    public JSONException(String message, long offset, Throwable cause) {
        super(message, cause);
        this.offset = offset;
    }

    /**
     * The byte/char offset in the input where the error occurred, or -1 if unknown.
     */
    public long getOffset() {
        return offset;
    }
}
