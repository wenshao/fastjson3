package com.alibaba.fastjson3.reader;

/**
 * Strategy for creating ObjectReader instances.
 * Controls whether to use ASM bytecode generation or reflection.
 */
public enum ReaderCreatorType {
    /**
     * Automatically choose the best strategy based on the type.
     * - Uses ASM for simple POJOs (public, non-abstract, no schema)
     * - Falls back to reflection for complex types (records, sealed classes, etc.)
     */
    AUTO,

    /**
     * Force use of ASM bytecode generation.
     * Will recursively generate ASM for all supported nested types.
     * Falls back to reflection for unsupported types.
     */
    ASM,

    /**
     * Force use of reflection-based ObjectReader creation.
     * No bytecode generation.
     */
    REFLECT
}
