package com.alibaba.fastjson3.util;

/**
 * Android-specific implementation of JDKUtils.
 *
 * <p>This class replaces the JVM version when building the Android JAR.
 * Key differences from the JVM version:</p>
 * <ul>
 *   <li>FAST_STRING_CREATION = false (Android String structure differs)</li>
 *   <li>VECTOR_SUPPORT = false (no jdk.incubator.vector on Android)</li>
 *   <li>ANDROID = true (for runtime platform detection)</li>
 * </ul>
 */
public final class JDKUtils {
    public static final boolean UNSAFE_AVAILABLE = unsafeAvailable();
    public static final boolean NATIVE_IMAGE = false;
    public static final int JDK_VERSION = Runtime.version().feature();
    public static final boolean ANDROID = true;

    private static final sun.misc.Unsafe UNSAFE;
    private static final long BYTE_ARRAY_OFFSET;
    private static final long CHAR_ARRAY_OFFSET;
    private static final long STRING_VALUE_OFFSET;
    private static final long STRING_CODER_OFFSET;
    private static final boolean COMPACT_STRINGS;

    // Android: Vector API not available
    public static final boolean FAST_STRING_CREATION = false;
    public static final boolean VECTOR_SUPPORT = false;
    public static final int VECTOR_BYTE_SIZE = 0;

    static {
        sun.misc.Unsafe u = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (sun.misc.Unsafe) f.get(null);
        } catch (Throwable ignored) {
            // Unsafe not available
        }
        UNSAFE = u;

        if (UNSAFE_AVAILABLE) {
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            CHAR_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(char[].class);

            long valOffset = -1;
            long coderOffset = -1;
            boolean compact = false;
            try {
                // Android String internal structure may differ, handle conservatively
                Class<?> stringClass = String.class;
                try {
                    java.lang.reflect.Field valueField = stringClass.getDeclaredField("value");
                    valOffset = getSafeObjectFieldOffset(valueField);
                } catch (NoSuchFieldException ignored) {
                    // Android may not have value field, or field name differs
                }
                compact = valOffset >= 0;
            } catch (Throwable ignored) {
                // Cannot access String internals
            }
            STRING_VALUE_OFFSET = valOffset;
            STRING_CODER_OFFSET = coderOffset;
            COMPACT_STRINGS = compact;
        } else {
            BYTE_ARRAY_OFFSET = -1;
            CHAR_ARRAY_OFFSET = -1;
            STRING_VALUE_OFFSET = -1;
            STRING_CODER_OFFSET = -1;
            COMPACT_STRINGS = false;
        }
    }

    private JDKUtils() {
    }

    // ==================== Array bulk operations ====================

    public static long getLong(byte[] buf, int offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getLong(buf, BYTE_ARRAY_OFFSET + offset);
        }
        return manualAssemblyLong(buf, offset);
    }

    public static void putLong(byte[] buf, int offset, long value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putLong(buf, BYTE_ARRAY_OFFSET + offset, value);
            return;
        }
        buf[offset] = (byte) (value >> 56);
        buf[offset + 1] = (byte) (value >> 48);
        buf[offset + 2] = (byte) (value >> 40);
        buf[offset + 3] = (byte) (value >> 32);
        buf[offset + 4] = (byte) (value >> 24);
        buf[offset + 5] = (byte) (value >> 16);
        buf[offset + 6] = (byte) (value >> 8);
        buf[offset + 7] = (byte) value;
    }

    public static long getCharLong(char[] buf, int charOffset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getLong(buf, CHAR_ARRAY_OFFSET + ((long) charOffset << 1));
        }
        return ((long) buf[charOffset] << 48)
                | ((long) buf[charOffset + 1] << 32)
                | ((long) buf[charOffset + 2] << 16)
                | ((long) buf[charOffset + 3]);
    }

    // ==================== Direct memory ops (fallback to non-direct) ====================

    public static void putLongDirect(byte[] buf, int offset, long value) {
        putLong(buf, offset, value);
    }

    public static long getLongDirect(byte[] buf, int offset) {
        return getLong(buf, offset);
    }

    public static int getIntDirect(byte[] buf, int offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getInt(buf, BYTE_ARRAY_OFFSET + offset);
        }
        return ((buf[offset] & 0xFF) << 24)
                | ((buf[offset + 1] & 0xFF) << 16)
                | ((buf[offset + 2] & 0xFF) << 8)
                | (buf[offset + 3] & 0xFF);
    }

    public static void putIntDirect(byte[] buf, int offset, int value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putInt(buf, BYTE_ARRAY_OFFSET + offset, value);
        } else {
            buf[offset] = (byte) (value >> 24);
            buf[offset + 1] = (byte) (value >> 16);
            buf[offset + 2] = (byte) (value >> 8);
            buf[offset + 3] = (byte) value;
        }
    }

    public static void putShortDirect(byte[] buf, int offset, short value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putShort(buf, BYTE_ARRAY_OFFSET + offset, value);
        } else {
            buf[offset] = (byte) (value >> 8);
            buf[offset + 1] = (byte) value;
        }
    }

    // ==================== Fast String creation (Android fallback) ====================

    public static Object getStringValue(String s) {
        if (UNSAFE_AVAILABLE && STRING_VALUE_OFFSET >= 0) {
            try {
                return UNSAFE.getObject(s, STRING_VALUE_OFFSET);
            } catch (Throwable ignored) {
                // Android: may not be able to access internal fields
            }
        }
        return null;
    }

    public static int getStringCoder(String s) {
        if (UNSAFE_AVAILABLE && COMPACT_STRINGS && STRING_CODER_OFFSET >= 0) {
            try {
                return UNSAFE.getByte(s, STRING_CODER_OFFSET);
            } catch (Throwable ignored) {
                // Android: may not be able to access internal fields
            }
        }
        return -1;
    }

    public static String createAsciiString(byte[] bytes, int offset, int length) {
        // Android: use standard constructor
        return new String(bytes, offset, length, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    public static String createLatin1String(byte[] src, int off, int len) {
        // Android: use standard constructor
        byte[] value = java.util.Arrays.copyOfRange(src, off, off + len);
        return new String(value, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    // ==================== Direct field access (with Android fallback) ====================

    public static long objectFieldOffset(java.lang.reflect.Field field) {
        if (UNSAFE_AVAILABLE) {
            try {
                return UNSAFE.objectFieldOffset(field);
            } catch (UnsupportedOperationException e) {
                // Android: certain fields may not allow Unsafe access
                return -1;
            }
        }
        return -1;
    }

    public static Object getObject(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getObject(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static int getInt(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getInt(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static long getLongField(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getLong(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static double getDouble(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getDouble(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static float getFloat(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getFloat(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static boolean getBoolean(Object obj, long offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getBoolean(obj, offset);
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putObject(Object obj, long offset, Object value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putObject(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putInt(Object obj, long offset, int value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putInt(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putLongField(Object obj, long offset, long value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putLong(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putDouble(Object obj, long offset, double value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putDouble(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putFloat(Object obj, long offset, float value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putFloat(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putBoolean(Object obj, long offset, boolean value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putBoolean(obj, offset, value);
            return;
        }
        throw new UnsupportedOperationException("Unsafe not available");
    }

    public static void putStringValue(String s, byte[] value) {
        if (STRING_VALUE_OFFSET >= 0) {
            try {
                UNSAFE.putObject(s, STRING_VALUE_OFFSET, value);
            } catch (Throwable ignored) {
                // Android: may not be able to access internal fields
            }
        }
    }

    // ==================== Bulk byte array comparison ====================

    public static boolean arrayEquals(byte[] a, int aOff, byte[] b, int bOff, int len) {
        if (UNSAFE_AVAILABLE) {
            while (len >= 8) {
                if (UNSAFE.getLong(a, BYTE_ARRAY_OFFSET + aOff)
                        != UNSAFE.getLong(b, BYTE_ARRAY_OFFSET + bOff)) {
                    return false;
                }
                aOff += 8;
                bOff += 8;
                len -= 8;
            }
        }
        while (len > 0) {
            if (a[aOff++] != b[bOff++]) {
                return false;
            }
            len--;
        }
        return true;
    }

    // ==================== Record support (JDK 16+, direct API) ====================

    public static boolean isRecord(Class<?> type) {
        return type.isRecord();
    }

    public static String[] getRecordComponentNames(Class<?> recordType) {
        java.lang.reflect.RecordComponent[] components = recordType.getRecordComponents();
        String[] names = new String[components.length];
        for (int i = 0; i < components.length; i++) {
            names[i] = components[i].getName();
        }
        return names;
    }

    public static Class<?>[] getRecordComponentTypes(Class<?> recordType) {
        java.lang.reflect.RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
        }
        return types;
    }

    public static java.lang.reflect.Type[] getRecordComponentGenericTypes(Class<?> recordType) {
        java.lang.reflect.RecordComponent[] components = recordType.getRecordComponents();
        java.lang.reflect.Type[] types = new java.lang.reflect.Type[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getGenericType();
        }
        return types;
    }

    public static java.lang.reflect.Method[] getRecordComponentAccessors(Class<?> recordType) {
        java.lang.reflect.RecordComponent[] components = recordType.getRecordComponents();
        java.lang.reflect.Method[] accessors = new java.lang.reflect.Method[components.length];
        for (int i = 0; i < components.length; i++) {
            accessors[i] = components[i].getAccessor();
        }
        return accessors;
    }

    // ==================== Private helper methods ====================

    private static boolean unsafeAvailable() {
        try {
            Class.forName("sun.misc.Unsafe");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static long getSafeObjectFieldOffset(java.lang.reflect.Field field) {
        try {
            return UNSAFE.objectFieldOffset(field);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static long manualAssemblyLong(byte[] buf, int offset) {
        return ((long) buf[offset] << 56)
                | ((long) (buf[offset + 1] & 0xFF) << 48)
                | ((long) (buf[offset + 2] & 0xFF) << 40)
                | ((long) (buf[offset + 3] & 0xFF) << 32)
                | ((long) (buf[offset + 4] & 0xFF) << 24)
                | ((long) (buf[offset + 5] & 0xFF) << 16)
                | ((long) (buf[offset + 6] & 0xFF) << 8)
                | ((long) (buf[offset + 7] & 0xFF));
    }
}
