package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ObjectReaderCreatorASM}'s direct-instantiation decision
 * ({@code canDirectlyInstantiate} — emits {@code new ClassName} when safe,
 * falls back to Unsafe allocation otherwise).
 *
 * <p>The test class and its bean must be fully public so the generated ASM
 * reader can resolve {@code new PublicTopBean} from the
 * {@code com.alibaba.fastjson3.reader.gen} package. Tests for the
 * Unsafe-allocation fallback path exercise canDirectlyInstantiate's logic via
 * reflection — the fallback runtime path is pre-existing and covered by the
 * broader test suite.
 */
public class ObjectReaderCreatorASMAccessTest {
    /** Public top-level bean — directly instantiable via {@code new}. */
    public static final class PublicTopBean {
        public String name;
        public int value;

        public PublicTopBean() {
        }
    }

    /** Public static inner of this public test class — still directly instantiable. */
    public static final class NoNoArgCtorBean {
        public String name;
        public int value;

        public NoNoArgCtorBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    /** Sanity: public bean with public no-arg ctor round-trips via direct new allocation. */
    @Test
    void asmReaderHandlesPublicBean() {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();
        String json = "{\"name\":\"hello\",\"value\":42}";
        PublicTopBean bean = mapper.readValue(json, PublicTopBean.class);
        assertNotNull(bean);
        assertEquals("hello", bean.name);
        assertEquals(42, bean.value);
    }

    /**
     * canDirectlyInstantiate must walk the enclosing class chain: a public
     * inner of a package-private outer is NOT accessible from other packages
     * (specifically, the generated-class package), so it must not be
     * directly instantiated. Verified via reflection on the package-private
     * {@code canDirectlyInstantiate} helper.
     */
    @Test
    void canDirectlyInstantiateRejectsNonPublicEnclosingChain() throws Exception {
        java.lang.reflect.Method m = ObjectReaderCreatorASM.class
                .getDeclaredMethod("canDirectlyInstantiate", Class.class);
        m.setAccessible(true);

        // Public top-level bean — both accepted.
        assertTrue((boolean) m.invoke(null, PublicTopBean.class));

        // Bean with only a non-no-arg constructor — rejected.
        assertFalse((boolean) m.invoke(null, NoNoArgCtorBean.class));

        // Non-public top-level class (this test file's package-private class).
        assertFalse((boolean) m.invoke(null, PackagePrivateBean.class));

        // Public inner of a non-public enclosing class — rejected (enclosing chain walk).
        assertFalse((boolean) m.invoke(null, NonPublicEnclosing.PublicInner.class));
    }
}

/** Top-level package-private bean — used by the reflection-level test above. */
class PackagePrivateBean {
    public String name;

    public PackagePrivateBean() {
    }
}

/** Package-private enclosing — its public inner must be rejected by the walk. */
class NonPublicEnclosing {
    public static class PublicInner {
        public String value;

        public PublicInner() {
        }
    }
}
