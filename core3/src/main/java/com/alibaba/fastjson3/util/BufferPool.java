package com.alibaba.fastjson3.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Lock-free buffer pool using AtomicReferenceFieldUpdater (fastjson2-style).
 *
 * <p>Borrow: CAS getAndSet(null) — ~5ns (vs synchronized ~20ns).
 * Return: lazySet(buf) — ~1ns (weakest memory ordering, safe for same-thread reuse).
 * Thread index: identityHashCode(currentThread) & mask — ~2ns (vs ThreadLocal ~10ns).
 *
 * <p>Total overhead per borrow+return cycle: ~8ns (vs previous ~50ns).</p>
 */
public final class BufferPool {
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_RECYCLE_SIZE = 256 * 1024;

    // Striped pool based on available processors
    private static final int POOL_SIZE = Integer.highestOneBit(
            Math.max(4, Runtime.getRuntime().availableProcessors())) << 1;
    private static final int POOL_MASK = POOL_SIZE - 1;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<CacheItem, byte[]> BYTES_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, byte[].class, "bytes");
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<CacheItem, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, char[].class, "chars");

    private static final CacheItem[] CACHE_ITEMS = new CacheItem[POOL_SIZE];

    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            CACHE_ITEMS[i] = new CacheItem();
        }
    }

    private BufferPool() {
    }

    private static int cacheIndex() {
        return System.identityHashCode(Thread.currentThread()) & POOL_MASK;
    }

    // ==================== Byte buffer ====================

    public static byte[] borrowByteBuffer() {
        CacheItem item = CACHE_ITEMS[cacheIndex()];
        byte[] buf = BYTES_UPDATER.getAndSet(item, null);
        if (buf != null) {
            return buf;
        }
        return new byte[BUFFER_SIZE];
    }

    public static void returnByteBuffer(byte[] buf) {
        if (buf == null || buf.length > MAX_RECYCLE_SIZE) {
            return;
        }
        CacheItem item = CACHE_ITEMS[cacheIndex()];
        BYTES_UPDATER.lazySet(item, buf);
    }

    // ==================== Char buffer ====================

    public static char[] borrowCharBuffer() {
        CacheItem item = CACHE_ITEMS[cacheIndex()];
        char[] buf = CHARS_UPDATER.getAndSet(item, null);
        if (buf != null) {
            return buf;
        }
        return new char[BUFFER_SIZE];
    }

    public static void returnCharBuffer(char[] buf) {
        if (buf == null || buf.length > MAX_RECYCLE_SIZE) {
            return;
        }
        CacheItem item = CACHE_ITEMS[cacheIndex()];
        CHARS_UPDATER.lazySet(item, buf);
    }

    static final class CacheItem {
        volatile byte[] bytes;
        volatile char[] chars;
    }
}
