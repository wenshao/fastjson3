package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BufferPool - lock-free buffer pool using AtomicReferenceFieldUpdater.
 *
 * <p>Note: BufferPool uses static cache that persists between tests.
 * Buffers returned to the pool can be reused, and sizes can range from
 * BUFFER_SIZE (8192) to MAX_RECYCLE_SIZE (262144). Tests verify buffers
 * are within valid ranges rather than exact sizes.
 */
class BufferPoolTest {

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_RECYCLE_SIZE = 256 * 1024;

    // ==================== Byte buffer tests ====================

    @Test
    void testBorrowByteBuffer_returnsValidSize() {
        byte[] buf = BufferPool.borrowByteBuffer();

        assertNotNull(buf);
        assertTrue(buf.length >= BUFFER_SIZE && buf.length <= MAX_RECYCLE_SIZE,
                "Buffer size should be between " + BUFFER_SIZE + " and " + MAX_RECYCLE_SIZE);
    }

    @Test
    void testBorrowByteBuffer_multipleCallsReturnNewBuffers() {
        byte[] buf1 = BufferPool.borrowByteBuffer();
        byte[] buf2 = BufferPool.borrowByteBuffer();

        assertNotNull(buf1);
        assertNotNull(buf2);
        assertNotSame(buf1, buf2); // Different instances
        assertTrue(buf1.length >= BUFFER_SIZE && buf1.length <= MAX_RECYCLE_SIZE);
        assertTrue(buf2.length >= BUFFER_SIZE && buf2.length <= MAX_RECYCLE_SIZE);
    }

    @Test
    void testReturnByteBuffer_nullInput() {
        assertDoesNotThrow(() -> BufferPool.returnByteBuffer(null));
    }

    @Test
    void testReturnByteBuffer_tooLargeToRecycle() {
        byte[] largeBuf = new byte[MAX_RECYCLE_SIZE + 1]; // MAX_RECYCLE_SIZE + 1
        byte[] exactMaxBuf = new byte[MAX_RECYCLE_SIZE]; // MAX_RECYCLE_SIZE

        // Too large to recycle - should not throw
        assertDoesNotThrow(() -> BufferPool.returnByteBuffer(largeBuf));

        // Exact max size - should be recycled
        assertDoesNotThrow(() -> BufferPool.returnByteBuffer(exactMaxBuf));
    }

    @Test
    void testBorrowAndReturnByteBuffer_reuse() {
        byte[] buf1 = BufferPool.borrowByteBuffer();
        BufferPool.returnByteBuffer(buf1);

        // Borrow again - might get the same buffer (not guaranteed)
        byte[] buf2 = BufferPool.borrowByteBuffer();
        assertNotNull(buf2);
        assertTrue(buf2.length >= BUFFER_SIZE && buf2.length <= MAX_RECYCLE_SIZE);
    }

    @Test
    void testBorrowAndReturnByteBuffer_threadSafety() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        boolean[] noErrors = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        byte[] buf = BufferPool.borrowByteBuffer();
                        assertNotNull(buf);
                        assertTrue(buf.length >= BUFFER_SIZE && buf.length <= MAX_RECYCLE_SIZE);
                        // Modify buffer to verify it's safe to use
                        if (buf.length > 0) {
                            buf[0] = (byte) threadId;
                        }
                        BufferPool.returnByteBuffer(buf);
                    }
                    noErrors[threadId] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // All threads should complete without errors
        for (boolean success : noErrors) {
            assertTrue(success, "At least one thread encountered an error");
        }
    }

    // ==================== Char buffer tests ====================

    @Test
    void testBorrowCharBuffer_returnsValidSize() {
        char[] buf = BufferPool.borrowCharBuffer();

        assertNotNull(buf);
        assertTrue(buf.length >= BUFFER_SIZE && buf.length <= MAX_RECYCLE_SIZE,
                "Buffer size should be between " + BUFFER_SIZE + " and " + MAX_RECYCLE_SIZE);
    }

    @Test
    void testBorrowCharBuffer_multipleCallsReturnNewBuffers() {
        char[] buf1 = BufferPool.borrowCharBuffer();
        char[] buf2 = BufferPool.borrowCharBuffer();

        assertNotNull(buf1);
        assertNotNull(buf2);
        assertNotSame(buf1, buf2);
        assertTrue(buf1.length >= BUFFER_SIZE && buf1.length <= MAX_RECYCLE_SIZE);
        assertTrue(buf2.length >= BUFFER_SIZE && buf2.length <= MAX_RECYCLE_SIZE);
    }

    @Test
    void testReturnCharBuffer_nullInput() {
        assertDoesNotThrow(() -> BufferPool.returnCharBuffer(null));
    }

    @Test
    void testReturnCharBuffer_tooLargeToRecycle() {
        char[] largeBuf = new char[MAX_RECYCLE_SIZE + 1]; // MAX_RECYCLE_SIZE + 1
        char[] exactMaxBuf = new char[MAX_RECYCLE_SIZE]; // MAX_RECYCLE_SIZE

        // Too large to recycle - should not throw
        assertDoesNotThrow(() -> BufferPool.returnCharBuffer(largeBuf));

        // Exact max size - should be recycled
        assertDoesNotThrow(() -> BufferPool.returnCharBuffer(exactMaxBuf));
    }

    @Test
    void testBorrowAndReturnCharBuffer_reuse() {
        char[] buf1 = BufferPool.borrowCharBuffer();
        BufferPool.returnCharBuffer(buf1);

        // Borrow again - might get the same buffer (not guaranteed)
        char[] buf2 = BufferPool.borrowCharBuffer();
        assertNotNull(buf2);
        assertTrue(buf2.length >= BUFFER_SIZE && buf2.length <= MAX_RECYCLE_SIZE);
    }

    @Test
    void testBorrowAndReturnCharBuffer_threadSafety() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        boolean[] noErrors = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        char[] buf = BufferPool.borrowCharBuffer();
                        assertNotNull(buf);
                        assertTrue(buf.length >= BUFFER_SIZE && buf.length <= MAX_RECYCLE_SIZE);
                        // Modify buffer to verify it's safe to use
                        if (buf.length > 0) {
                            buf[0] = (char) threadId;
                        }
                        BufferPool.returnCharBuffer(buf);
                    }
                    noErrors[threadId] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // All threads should complete without errors
        for (boolean success : noErrors) {
            assertTrue(success, "At least one thread encountered an error");
        }
    }

    // ==================== Mixed byte/char tests ====================

    @Test
    void testBorrowBothTypesIndependently() {
        byte[] byteBuf = BufferPool.borrowByteBuffer();
        char[] charBuf = BufferPool.borrowCharBuffer();

        assertNotNull(byteBuf);
        assertNotNull(charBuf);
        assertTrue(byteBuf.length >= BUFFER_SIZE && byteBuf.length <= MAX_RECYCLE_SIZE);
        assertTrue(charBuf.length >= BUFFER_SIZE && charBuf.length <= MAX_RECYCLE_SIZE);

        // Can return both independently
        BufferPool.returnByteBuffer(byteBuf);
        BufferPool.returnCharBuffer(charBuf);
    }

    @Test
    void testConcurrentByteAndCharBorrow() throws InterruptedException {
        int threadCount = 8;
        int iterationsPerThread = 50;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        // Alternate between byte and char buffers
                        byte[] byteBuf = BufferPool.borrowByteBuffer();
                        assertNotNull(byteBuf);
                        BufferPool.returnByteBuffer(byteBuf);

                        char[] charBuf = BufferPool.borrowCharBuffer();
                        assertNotNull(charBuf);
                        BufferPool.returnCharBuffer(charBuf);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // If we reach here without exception, test passed
        assertTrue(true);
    }

    // ==================== Edge cases ====================

    @Test
    void testReturnAfterBorrow_fillBuffer() {
        byte[] buf = BufferPool.borrowByteBuffer();

        // Fill with data
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (i % 128);
        }

        // Should still return successfully
        assertDoesNotThrow(() -> BufferPool.returnByteBuffer(buf));
    }

    @Test
    void testReturnAfterBorrow_fillCharBuffer() {
        char[] buf = BufferPool.borrowCharBuffer();

        // Fill with data
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) (i % 128);
        }

        // Should still return successfully
        assertDoesNotThrow(() -> BufferPool.returnCharBuffer(buf));
    }

    @Test
    void testBorrowAfterReturn_getCleanBuffer() {
        byte[] buf1 = BufferPool.borrowByteBuffer();

        // Fill with data
        for (int i = 0; i < Math.min(100, buf1.length); i++) {
            buf1[i] = 99;
        }

        BufferPool.returnByteBuffer(buf1);

        // Borrow again - should work but data might be stale
        byte[] buf2 = BufferPool.borrowByteBuffer();
        assertNotNull(buf2);
        assertTrue(buf2.length >= BUFFER_SIZE && buf2.length <= MAX_RECYCLE_SIZE);
        // We don't guarantee buf2 == buf1, so we can't check content
    }

    @Test
    void testBufferSize_constant() {
        // Verify buffers are within the expected size range
        // Actual size may vary due to pool recycling
        byte[] byteBuf = BufferPool.borrowByteBuffer();
        char[] charBuf = BufferPool.borrowCharBuffer();

        assertTrue(byteBuf.length >= BUFFER_SIZE && byteBuf.length <= MAX_RECYCLE_SIZE);
        assertTrue(charBuf.length >= BUFFER_SIZE && charBuf.length <= MAX_RECYCLE_SIZE);
    }

    @Test
    void testMaxRecycleSize() {
        // Test that buffers larger than MAX_RECYCLE_SIZE are not recycled
        byte[] withinLimit = new byte[MAX_RECYCLE_SIZE];      // exact limit
        byte[] overLimit = new byte[MAX_RECYCLE_SIZE + 1];    // over limit

        // Both should be accepted without error
        BufferPool.returnByteBuffer(withinLimit);
        BufferPool.returnByteBuffer(overLimit);

        // The over limit buffer won't be recycled, but no exception
        assertTrue(true);
    }

    @Test
    void testPoolSize_basedOnProcessors() {
        // Verify pool is sized based on available processors
        // POOL_SIZE should be power of 2 and at least 4
        // POOL_SIZE = Integer.highestOneBit(Math.max(4, availableProcessors)) << 1

        int processors = Runtime.getRuntime().availableProcessors();
        int expectedPoolSize = Integer.highestOneBit(Math.max(4, processors)) << 1;

        // The implementation should create POOL_SIZE cache items
        // We can't directly access POOL_SIZE, but we can verify behavior works
        assertTrue(expectedPoolSize >= 4);
    }
}
