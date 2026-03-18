package com.alibaba.fastjson3.util;

import java.util.function.Supplier;

/**
 * Simple internal logger for diagnostic messages.
 * Uses System.err by default, can be configured via {@link #setOutput(Appendable)}.
 */
public final class Logger {
    private static volatile Appendable output = System.err;
    private static volatile boolean debugEnabled = Boolean.getBoolean("fastjson.debug");

    private Logger() {
    }

    /**
     * Set the output destination for log messages.
     *
     * @param out the output destination (e.g., System.err, a file writer, etc.)
     */
    public static void setOutput(Appendable out) {
        output = out != null ? out : System.err;
    }

    /**
     * Enable or disable debug logging.
     * Can also be set via JVM property: -Dfastjson.debug=true
     *
     * @param enabled true to enable debug logging
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Check if debug logging is enabled.
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Log a debug message. Only outputs if debug is enabled.
     */
    public static void debug(String message) {
        if (debugEnabled) {
            log("[DEBUG] " + message);
        }
    }

    /**
     * Log a debug message with lazy evaluation. Only evaluates the supplier if debug is enabled.
     */
    public static void debug(Supplier<String> message) {
        if (debugEnabled) {
            log("[DEBUG] " + message.get());
        }
    }

    /**
     * Log a warning message.
     */
    public static void warn(String message) {
        log("[WARN] " + message);
    }

    /**
     * Log a warning message with exception.
     */
    public static void warn(String message, Throwable e) {
        log("[WARN] " + message + ": " + e.getMessage());
        if (debugEnabled) {
            logStackTrace(e);
        }
    }

    /**
     * Log an error message.
     */
    public static void error(String message) {
        log("[ERROR] " + message);
    }

    /**
     * Log an error message with exception.
     */
    public static void error(String message, Throwable e) {
        log("[ERROR] " + message + ": " + e.getMessage());
        if (debugEnabled) {
            logStackTrace(e);
        }
    }

    private static void logStackTrace(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        for (String line : sw.toString().split("\n")) {
            log("  " + line);
        }
    }

    private static void log(String message) {
        Appendable out = output;  // Read volatile once
        try {
            synchronized (out) {
                out.append(message).append('\n');
            }
        } catch (Exception e) {
            // Fallback to System.err if output fails
            System.err.println(message);
        }
    }

    /**
     * Create a category-specific logger.
     */
    public static CategoryLogger category(String category) {
        return new CategoryLogger(category);
    }

    /**
     * Category-specific logger that prefixes all messages with the category name.
     */
    public static final class CategoryLogger {
        private final String category;

        CategoryLogger(String category) {
            this.category = category;
        }

        public void debug(String message) {
            Logger.debug("[" + category + "] " + message);
        }

        public void debug(Supplier<String> message) {
            Logger.debug("[" + category + "] " + message.get());
        }

        public void warn(String message) {
            Logger.warn("[" + category + "] " + message);
        }

        public void warn(String message, Throwable e) {
            Logger.warn("[" + category + "] " + message, e);
        }

        public void error(String message) {
            Logger.error("[" + category + "] " + message);
        }

        public void error(String message, Throwable e) {
            Logger.error("[" + category + "] " + message, e);
        }
    }
}
