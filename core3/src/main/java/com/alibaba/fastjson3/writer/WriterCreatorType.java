package com.alibaba.fastjson3.writer;

/**
 * Strategy for creating ObjectWriter instances.
 * Controls whether to use ASM bytecode generation or reflection.
 */
public enum WriterCreatorType {
    /**
     * Automatically choose the best strategy based on the type.
     * - Uses ASM for simple POJOs (public, non-abstract, no schema configuration)
     * - Records and sealed classes are treated as simple POJOs (ASM-compatible)
     * - Falls back to reflection for types with complex configuration
     */
    AUTO,

    /**
     * Force use of ASM bytecode generation.
     * Will recursively generate ASM for all supported nested types.
     * Falls back to reflection for unsupported types.
     */
    ASM,

    /**
     * Force use of reflection-based ObjectWriter creation.
     * No bytecode generation.
     */
    REFLECT
}
