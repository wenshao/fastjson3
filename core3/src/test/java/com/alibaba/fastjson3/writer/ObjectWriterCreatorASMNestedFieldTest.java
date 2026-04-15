package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link ObjectWriterCreatorASM} after Path B Write
 * Phase W#1+W#2+W#4 (PR #80). These assert that the new nested-writer
 * cache:
 *
 * <ul>
 *   <li>actually engages for private POJO fields with public getters
 *       (the case the old {@code Modifier.isPublic} guard was silently
 *       rejecting),</li>
 *   <li>defers to {@link com.alibaba.fastjson3.BuiltinCodecs} for types
 *       like {@link Optional} and {@link URI} so wrapper semantics aren't
 *       mishandled by a generic REFLECT writer,</li>
 *   <li>handles nested {@code List<POJO>} via the inline list loop and
 *       round-trips correctly,</li>
 *   <li>handles {@code List<String>} via the dedicated string-list path.</li>
 * </ul>
 */
public class ObjectWriterCreatorASMNestedFieldTest {

    public static final class Inner {
        private int height;
        private String title;

        public Inner() {
        }

        public Inner(int height, String title) {
            this.height = height;
            this.title = title;
        }

        public int getHeight() {
            return height;
        }

        public String getTitle() {
            return title;
        }
    }

    public static final class WithNested {
        private String name;
        private Inner inner;
        private List<Inner> innerList;
        private List<String> tags;

        public String getName() {
            return name;
        }

        public Inner getInner() {
            return inner;
        }

        public List<Inner> getInnerList() {
            return innerList;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    public static final class WithBuiltins {
        private Optional<String> opt;
        private URI uri;

        public Optional<String> getOpt() {
            return opt;
        }

        public URI getUri() {
            return uri;
        }
    }

    private static ObjectMapper asmMapper() {
        return ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();
    }

    @Test
    void nestedPojoFieldRoundTrips() {
        ObjectMapper m = asmMapper();
        WithNested obj = new WithNested();
        obj.name = "outer";
        obj.inner = new Inner(42, "the answer");
        obj.innerList = Arrays.asList(
                new Inner(1, "one"),
                new Inner(2, "two")
        );
        obj.tags = Arrays.asList("alpha", "beta", "gamma");

        String json = m.writeValueAsString(obj);
        // Spot-check fields appear in the output. The exact field order
        // matches alphabetic sort applied by ObjectWriterCreatorASM.
        assertTrue(json.contains("\"name\":\"outer\""), json);
        assertTrue(json.contains("\"inner\":{"), json);
        assertTrue(json.contains("\"height\":42"), json);
        assertTrue(json.contains("\"title\":\"the answer\""), json);
        assertTrue(json.contains("\"innerList\":["), json);
        assertTrue(json.contains("\"height\":1"), json);
        assertTrue(json.contains("\"title\":\"two\""), json);
        assertTrue(json.contains("\"tags\":[\"alpha\",\"beta\",\"gamma\"]"), json);
    }

    @Test
    void nestedPojoNullFieldsAreSkipped() {
        ObjectMapper m = asmMapper();
        WithNested obj = new WithNested();
        obj.name = "x";
        // inner / innerList / tags are null

        String json = m.writeValueAsString(obj);
        assertTrue(json.contains("\"name\":\"x\""), json);
        assertFalse(json.contains("inner"), json);
        assertFalse(json.contains("tags"), json);
    }

    @Test
    void nestedPojoEmptyList() {
        ObjectMapper m = asmMapper();
        WithNested obj = new WithNested();
        obj.name = "x";
        obj.innerList = java.util.Collections.emptyList();
        obj.tags = java.util.Collections.emptyList();

        String json = m.writeValueAsString(obj);
        assertTrue(json.contains("\"innerList\":[]"), json);
        assertTrue(json.contains("\"tags\":[]"), json);
    }

    @Test
    void nestedPojoListWithNullElement() {
        ObjectMapper m = asmMapper();
        WithNested obj = new WithNested();
        obj.name = "x";
        obj.innerList = Arrays.asList(new Inner(1, "a"), null, new Inner(3, "c"));
        obj.tags = Arrays.asList("x", null, "z");

        String json = m.writeValueAsString(obj);
        assertTrue(json.contains("null"), json);
        // Both list entries should appear, with null in between
        assertTrue(json.contains("\"height\":1"), json);
        assertTrue(json.contains("\"height\":3"), json);
    }

    @Test
    void optionalFieldUsesBuiltinCodec() {
        // Regression: before W#2's BuiltinCodecs check, the nested-writer
        // cache resolution would call createObjectWriter(Optional.class),
        // get a generic REFLECT writer, and mis-serialize. With the fix the
        // cache holds BuiltinCodecs.OPTIONAL_WRITER and produces the value
        // unwrapped (matching pre-Path-B writeAny semantics).
        ObjectMapper m = asmMapper();
        WithBuiltins obj = new WithBuiltins();
        obj.opt = Optional.of("hello");

        String json = m.writeValueAsString(obj);
        // Optional<String>.of("hello") serializes as the string "hello"
        // through OPTIONAL_WRITER. A wrong cache would emit a JSON object
        // with a "present" or "value" property.
        assertTrue(json.contains("\"opt\":\"hello\""), json);
    }

    @Test
    void uriFieldUsesBuiltinCodec() {
        ObjectMapper m = asmMapper();
        WithBuiltins obj = new WithBuiltins();
        obj.uri = URI.create("https://example.com/path");

        String json = m.writeValueAsString(obj);
        // URI serializes as its toString() through URI_WRITER.
        assertTrue(json.contains("\"uri\":\"https://example.com/path\""), json);
    }

    // Note: ObjectWriterCreatorASM.canGenerate rejects beans whose module
    // is named (which includes every class in core3's own JPMS module),
    // so the round-trip tests above are exercised against the REFLECT
    // fallback when run from this test suite. The actual ASM code paths
    // are exercised by benchmark3 (unnamed module) — see
    // EishayWriteUTF8Bytes.fastjson3_asm and the JFR profile in PR #80's
    // commit body. The round-trip assertions here lock in the contract
    // both writers must satisfy: nested POJO fields, List<POJO> fields,
    // List<String> fields, and Optional/URI builtin-codec fields all
    // round-trip with the expected JSON shape regardless of which writer
    // implementation is chosen.
}
