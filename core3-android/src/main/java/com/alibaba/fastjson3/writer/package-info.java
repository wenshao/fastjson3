/**
 * ObjectWriter provider implementations for Android.
 *
 * <p>On Android, ASM bytecode generation is not available, so all
 * writers use reflection. The provider architecture allows consistent
 * API across platforms.</p>
 */
package com.alibaba.fastjson3.writer;
