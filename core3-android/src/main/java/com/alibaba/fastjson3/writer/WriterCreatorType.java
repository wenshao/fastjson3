package com.alibaba.fastjson3.writer;

/**
 * Strategy for creating ObjectWriter instances (Android stub).
 */
public enum WriterCreatorType {
    /**
     * Automatically choose the best strategy.
     */
    AUTO,

    /**
     * Force bytecode generation (not available on Android).
     */
    ASM,

    /**
     * Force reflection only.
     */
    REFLECT
}
