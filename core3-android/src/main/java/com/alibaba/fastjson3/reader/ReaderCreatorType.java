package com.alibaba.fastjson3.reader;

/**
 * Strategy for creating ObjectReader instances (Android stub).
 */
public enum ReaderCreatorType {
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
