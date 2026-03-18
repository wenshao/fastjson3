package com.alibaba.fastjson3.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method that has a different implementation on Android.
 *
 * <p>Classes or methods annotated with {@code @AndroidNative} will be replaced
 * by the corresponding implementation in the {@code core3-android} module when
 * building the Android JAR.</p>
 *
 * <h2>Usage on Classes</h2>
 * <pre>
 * &#64;AndroidNative("Android uses different String internal structure")
 * public final class JDKUtils {
 *     public static final boolean FAST_STRING_CREATION = true;
 *     public static final boolean VECTOR_SUPPORT = true;
 *     // JVM-specific implementation
 * }
 * </pre>
 *
 * <h2>Usage on Methods</h2>
 * <pre>
 * public final class JDKUtils {
 *
 *     &#64;AndroidNative("Vector API not available on Android")
 *     public static int indexOf(byte[] source, byte[] target) {
 *         // JVM-optimized implementation using Vector API
 *         return VectorizedScanner.indexOf(source, target);
 *     }
 *
 *     // The corresponding Android implementation in core3-android
 *     // will have a fallback version without @AndroidNative
 * }
 * </pre>
 *
 * <h2>Build Process</h2>
 * <ol>
 *   <li>During {@code mvn package -Pandroid}, classes marked {@code @AndroidNative}
 *       are identified and removed from the compilation.</li>
 *   <li>The {@code core3-android} module provides Android-specific implementations
 *       that replace the JVM versions.</li>
 * </ol>
 *
 * <h2>When to Use</h2>
 * <ul>
 *   <li>Classes or methods using JVM-specific APIs (e.g., {@code jdk.incubator.vector})</li>
 *   <li>Code relying on JVM-specific behavior (e.g., {@code String} internal structure)</li>
 *   <li>Performance optimizations that don't apply to Android's Dalvik/ART runtime</li>
 * </ul>
 *
 * @see JVMOnly
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface AndroidNative {
    /**
     * Optional description explaining why an Android-specific implementation is needed.
     *
     * @return the description
     */
    String value() default "";
}
