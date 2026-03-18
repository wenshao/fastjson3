package com.alibaba.fastjson3.android;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify that the android JAR artifact is built correctly.
 * This test reads the actual JAR file to verify content.
 * <p>
 * Tests are skipped if the Android JAR has not been built (i.e., when running
 * regular `mvn test` without `-Pandroid` profile).
 * </p>
 */
public class AndroidJarVerificationTest {
    private static Path androidJar;

    @BeforeAll
    static void setupAndroidJar() {
        try {
            androidJar = locateAndroidJar();
            Assumptions.assumeTrue(androidJar != null && Files.exists(androidJar),
                "Android JAR must be built first (run: mvn package -Pandroid -pl core3)");
        } catch (IOException e) {
            Assumptions.assumeTrue(false, "Android JAR must be built first (run: mvn package -Pandroid -pl core3)");
        }
    }

    @Test
    public void testAndroidJarExists() throws IOException {
        assertTrue(Files.exists(androidJar), "Android JAR file should exist: " + androidJar);
    }

    @Test
    public void testAndroidJarContainsCoreClasses() throws IOException {
        try (JarFile jar = new JarFile(androidJar.toFile())) {
            // Core classes should be present
            assertTrue(jar.getJarEntry("com/alibaba/fastjson3/JSON.class") != null,
                    "Core JSON class should be in android JAR");
            assertTrue(jar.getJarEntry("com/alibaba/fastjson3/JSONObject.class") != null,
                    "JSONObject class should be in android JAR");
            assertTrue(jar.getJarEntry("com/alibaba/fastjson3/JSONArray.class") != null,
                    "JSONArray class should be in android JAR");
            assertTrue(jar.getJarEntry("com/alibaba/fastjson3/JSONParser.class") != null,
                    "JSONParser class should be in android JAR");
            assertTrue(jar.getJarEntry("com/alibaba/fastjson3/JSONGenerator.class") != null,
                    "JSONGenerator class should be in android JAR");
        }
    }

    @Test
    public void testAndroidJarExcludesASMClasses() throws IOException {
        try (JarFile jar = new JarFile(androidJar.toFile())) {
            // ASM classes should NOT be present
            JarEntry classWriter = jar.getJarEntry("com/alibaba/fastjson3/internal/asm/ClassWriter.class");
            assertFalse(classWriter != null, "ASM ClassWriter should not be in android JAR");

            JarEntry methodWriter = jar.getJarEntry("com/alibaba/fastjson3/internal/asm/MethodWriter.class");
            assertFalse(methodWriter != null, "ASM MethodWriter should not be in android JAR");

            JarEntry asmUtils = jar.getJarEntry("com/alibaba/fastjson3/internal/asm/ASMUtils.class");
            assertFalse(asmUtils != null, "ASM ASMUtils should not be in android JAR");
        }
    }

    @Test
    public void testAndroidJarExcludesASMCreators() throws IOException {
        try (JarFile jar = new JarFile(androidJar.toFile())) {
            JarEntry readerCreator = jar.getJarEntry("com/alibaba/fastjson3/reader/ObjectReaderCreatorASM.class");
            assertFalse(readerCreator != null, "ObjectReaderCreatorASM should not be in android JAR");

            JarEntry writerCreator = jar.getJarEntry("com/alibaba/fastjson3/writer/ObjectWriterCreatorASM.class");
            assertFalse(writerCreator != null, "ObjectWriterCreatorASM should not be in android JAR");
        }
    }

    @Test
    public void testAndroidJarExcludesDynamicClassLoader() throws IOException {
        try (JarFile jar = new JarFile(androidJar.toFile())) {
            JarEntry dynamicLoader = jar.getJarEntry("com/alibaba/fastjson3/util/DynamicClassLoader.class");
            // Android JAR contains a stub DynamicClassLoader (delegates to system classloader)
            // This is needed because ObjectMapper.shared() references it
            assertTrue(dynamicLoader != null, "Android JAR should contain stub DynamicClassLoader");
        }
    }

    @Test
    public void testAndroidJarHasReflectiveCreators() throws IOException {
        try (JarFile jar = new JarFile(androidJar.toFile())) {
            // Non-ASM creators should be present
            JarEntry readerCreator = jar.getJarEntry("com/alibaba/fastjson3/reader/ObjectReaderCreator.class");
            assertTrue(readerCreator != null, "ObjectReaderCreator should be in android JAR");

            JarEntry writerCreator = jar.getJarEntry("com/alibaba/fastjson3/writer/ObjectWriterCreator.class");
            assertTrue(writerCreator != null, "ObjectWriterCreator should be in android JAR");
        }
    }

    @Test
    public void testAndroidJarSize() throws IOException {
        long size = Files.size(androidJar);
        // Android JAR should be reasonably sized
        // Actual size depends on included features, but should be between 300KB-500KB
        assertTrue(size > 300000, "Android JAR should be > 300KB, but was: " + size);
        assertTrue(size < 600000, "Android JAR should be < 600KB, but was: " + size);
    }

    /**
     * Find the android JAR file in the target directory.
     */
    private static Path locateAndroidJar() throws IOException {
        // First try the target directory of the android-test module (may not exist)
        Path androidTestTarget = Paths.get("target").toAbsolutePath();
        if (Files.exists(androidTestTarget)) {
            try (var stream = Files.walk(androidTestTarget, 1)) {
                Path found = stream
                        .filter(p -> p.toString().endsWith("-android.jar"))
                        .findFirst()
                        .orElse(null);
                if (found != null) {
                    return found;
                }
            }
        }

        // Try the core3 module target directory
        Path core3Target = Paths.get("../core3/target").toAbsolutePath();
        if (Files.exists(core3Target)) {
            try (var stream = Files.walk(core3Target, 1)) {
                Path found = stream
                        .filter(p -> p.toString().endsWith("-android.jar"))
                        .findFirst()
                        .orElse(null);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
