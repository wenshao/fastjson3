package com.alibaba.fastjson3.util;

/**
 * Android-compatible stub for DynamicClassLoader.
 * On Android, we don't support dynamic class generation (ASM),
 * so this class provides a minimal implementation that delegates
 * to the system class loader.
 *
 * ANDROID_COMPATIBLE - do not strip from Android builds
 */
// ANDROID_COMPATIBLE - marker for build scripts to preserve this class
public final class DynamicClassLoader extends ClassLoader {
    private static final DynamicClassLoader SHARED_INSTANCE = new DynamicClassLoader();

    private DynamicClassLoader() {
        super(DynamicClassLoader.class.getClassLoader());
    }

    public static DynamicClassLoader getSharedInstance() {
        return SHARED_INSTANCE;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
