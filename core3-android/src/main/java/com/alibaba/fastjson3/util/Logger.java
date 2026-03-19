package com.alibaba.fastjson3.util;

/**
 * Logger stub for Android (minimal implementation).
 */
public final class Logger {
    /** Private constructor to prevent instantiation. */
    private Logger() {
    }

    /**
     * Get a logger for the specified category.
     *
     * @param name the category name
     * @return the CategoryLogger
     */
    public static CategoryLogger category(final String name) {
        return new CategoryLogger(name);
    }

    /**
     * Log a warning message.
     *
     * @param message the message to log
     */
    public static void warn(final String message) {
    }

    /**
     * Log a debug message.
     *
     * @param message the message to log
     */
    public static void debug(final String message) {
    }

    /**
     * Category-specific logger.
     */
    public static final class CategoryLogger {
        /** Category name. */
        private final String categoryName;

        /**
         * Create a logger for the specified category.
         *
         * @param name the category name
         */
        CategoryLogger(final String name) {
            this.categoryName = name;
        }

        /**
         * Log a debug message.
         *
         * @param message the message to log
         */
        public void debug(final String message) {
        }

        /**
         * Log an info message.
         *
         * @param message the message to log
         */
        public void info(final String message) {
        }

        /**
         * Log a warning message.
         *
         * @param message the message to log
         */
        public void warn(final String message) {
        }

        /**
         * Log an error message.
         *
         * @param message the message to log
         */
        public void error(final String message) {
        }

        /**
         * Log an error message with exception.
         *
         * @param message the message to log
         * @param e the exception
         */
        public void error(final String message, final Throwable e) {
        }
    }
}
