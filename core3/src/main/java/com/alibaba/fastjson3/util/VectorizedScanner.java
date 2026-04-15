package com.alibaba.fastjson3.util;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/**
 * SIMD-accelerated byte scanning using Java Vector API.
 *
 * <p>This class is only loaded when Vector API is available at runtime
 * (detected by {@link JDKUtils#VECTOR_SUPPORT}). All callers must guard
 * access with that flag to avoid {@link NoClassDefFoundError}.</p>
 *
 * <p>Inspired by simdjson-java's StringParser approach: load a full vector,
 * compare all lanes in parallel, and use trailing-zero bit tricks to find
 * the first interesting byte position.</p>
 */
public final class VectorizedScanner {
    // Use SPECIES_MAX so each scan iteration processes the widest vector the
    // current CPU supports. AVX-512 → 64 bytes/iter on x86; NEON/SVE → full
    // 128/256/... on aarch64. Measured: on the aarch64 test box,
    // SPECIES_PREFERRED was resolving to a narrower species than SPECIES_MAX,
    // and the extra iterations per call dominated scan cost for 10–40 byte
    // Eishay strings. SPECIES_MAX is never smaller than SPECIES_PREFERRED, so
    // the change is neutral-or-better on every platform.
    static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_MAX;
    static final int VECTOR_SIZE = SPECIES.vectorByteSize();

    private static final ByteVector QUOTE_VEC = ByteVector.broadcast(SPECIES, (byte) '"');
    private static final ByteVector BACKSLASH_VEC = ByteVector.broadcast(SPECIES, (byte) '\\');

    private VectorizedScanner() {
    }

    /**
     * Scan {@code buf[off..limit)} for the first byte that is one of:
     * <ul>
     *   <li>{@code '"'} (quote)</li>
     *   <li>{@code '\\'} (backslash / escape)</li>
     *   <li>any byte with the high bit set (non-ASCII, i.e. {@code >= 0x80})</li>
     * </ul>
     *
     * <p>Processes {@link #VECTOR_SIZE} bytes per iteration using SIMD comparison.
     * Returns the index of the first match, or the position where the caller
     * should continue with a scalar tail loop (when fewer than VECTOR_SIZE bytes remain).</p>
     *
     * @param buf  the byte array to scan
     * @param off  start offset (inclusive)
     * @param limit end offset (exclusive), must be {@code <= buf.length}
     * @return index of first quote/backslash/non-ASCII byte, or the tail-start position
     */
    public static int scanStringSimple(byte[] buf, int off, int limit) {
        int vectorLimit = limit - VECTOR_SIZE;
        while (off <= vectorLimit) {
            ByteVector v = ByteVector.fromArray(SPECIES, buf, off);

            // Parallel comparison: quote OR backslash OR non-ASCII (high bit set)
            VectorMask<Byte> found = v.eq(QUOTE_VEC)
                    .or(v.eq(BACKSLASH_VEC))
                    .or(v.compare(VectorOperators.LT, (byte) 0));

            if (found.anyTrue()) {
                return off + Long.numberOfTrailingZeros(found.toLong());
            }
            off += VECTOR_SIZE;
        }
        return off;
    }
}
