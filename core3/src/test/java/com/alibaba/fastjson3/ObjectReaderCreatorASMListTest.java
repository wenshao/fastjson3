package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link com.alibaba.fastjson3.reader.ObjectReaderCreatorASM}'s
 * inline {@code List<POJO>} loop (Phase B3 of Path B). The generator emits
 * bytecode that calls {@code nextIfArrayStart} / {@code nextIfArrayEnd}
 * directly in the hot path and invokes the cached element reader via
 * {@code ObjectReader.readObjectUTF8} — no helper method indirection,
 * no reflection fallback.
 */
public class ObjectReaderCreatorASMListTest {
    public static final class Image {
        public int height;
        public int width;
        public String title;
        public String uri;
    }

    public static final class Album {
        public String name;
        public List<Image> images;
        public int count;
    }

    public static final class WrapOnly {
        public List<Image> items;
    }

    public static final class Nested {
        public String tag;
        public List<Album> albums;
    }

    private static ObjectMapper asmMapper() {
        return ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();
    }

    @Test
    void emptyList() {
        ObjectMapper m = asmMapper();
        Album a = m.readValue(
                "{\"name\":\"x\",\"images\":[],\"count\":0}",
                Album.class);
        assertNotNull(a.images);
        assertEquals(0, a.images.size());
        assertEquals("x", a.name);
        assertEquals(0, a.count);
    }

    @Test
    void singleElement() {
        ObjectMapper m = asmMapper();
        Album a = m.readValue(
                "{\"name\":\"x\",\"images\":[{\"height\":100,\"width\":200,\"title\":\"t\",\"uri\":\"u\"}],\"count\":1}",
                Album.class);
        assertEquals(1, a.images.size());
        Image img = a.images.get(0);
        assertEquals(100, img.height);
        assertEquals(200, img.width);
        assertEquals("t", img.title);
        assertEquals("u", img.uri);
        assertEquals(1, a.count);
    }

    @Test
    void multipleElements() {
        ObjectMapper m = asmMapper();
        String json = "{\"name\":\"alb\",\"images\":["
                + "{\"height\":1,\"width\":2,\"title\":\"a\",\"uri\":\"u1\"},"
                + "{\"height\":3,\"width\":4,\"title\":\"b\",\"uri\":\"u2\"},"
                + "{\"height\":5,\"width\":6,\"title\":\"c\",\"uri\":\"u3\"}"
                + "],\"count\":3}";
        Album a = m.readValue(json, Album.class);
        assertEquals(3, a.images.size());
        assertEquals(1, a.images.get(0).height);
        assertEquals("b", a.images.get(1).title);
        assertEquals("u3", a.images.get(2).uri);
        assertEquals(3, a.count);
    }

    @Test
    void leadingWhitespace() {
        ObjectMapper m = asmMapper();
        String json = "{\"name\":\"x\",\"images\":   [  {\"height\":1,\"width\":2,\"title\":\"a\",\"uri\":\"u\"}  ]  ,\"count\":1}";
        Album a = m.readValue(json, Album.class);
        assertEquals(1, a.images.size());
        assertEquals("a", a.images.get(0).title);
    }

    @Test
    void nullList() {
        ObjectMapper m = asmMapper();
        Album a = m.readValue(
                "{\"name\":\"x\",\"images\":null,\"count\":0}",
                Album.class);
        assertNull(a.images);
    }

    @Test
    void listIsOnlyField() {
        ObjectMapper m = asmMapper();
        WrapOnly w = m.readValue(
                "{\"items\":[{\"height\":1,\"width\":2,\"title\":\"t\",\"uri\":\"u\"}]}",
                WrapOnly.class);
        assertEquals(1, w.items.size());
    }

    @Test
    void nestedListOfPojoWithList() {
        // Exercises the recursive element-reader resolution: the outer
        // generated reader calls into another generated reader for Album,
        // which itself has a List<Image> inline loop.
        ObjectMapper m = asmMapper();
        String json = "{\"tag\":\"n\",\"albums\":["
                + "{\"name\":\"a1\",\"images\":[{\"height\":1,\"width\":2,\"title\":\"t1\",\"uri\":\"u1\"}],\"count\":1},"
                + "{\"name\":\"a2\",\"images\":[],\"count\":0}"
                + "]}";
        Nested n = m.readValue(json, Nested.class);
        assertEquals("n", n.tag);
        assertEquals(2, n.albums.size());
        assertEquals("a1", n.albums.get(0).name);
        assertEquals(1, n.albums.get(0).images.size());
        assertEquals("t1", n.albums.get(0).images.get(0).title);
        assertEquals(0, n.albums.get(1).images.size());
    }

    @Test
    void emptyListLastField() {
        ObjectMapper m = asmMapper();
        WrapOnly w = m.readValue("{\"items\":[]}", WrapOnly.class);
        assertNotNull(w.items);
        assertEquals(0, w.items.size());
    }

    @Test
    void interleavedFieldOrder() {
        ObjectMapper m = asmMapper();
        // Put list field first (non-trailing position) to test separator handling.
        Album a = m.readValue(
                "{\"images\":[{\"height\":10,\"width\":20,\"title\":\"x\",\"uri\":\"y\"}],\"name\":\"n\",\"count\":5}",
                Album.class);
        assertEquals(1, a.images.size());
        assertEquals(10, a.images.get(0).height);
        assertEquals("n", a.name);
        assertEquals(5, a.count);
    }

    @Test
    void eishayStyleMediaContent() {
        // Mirrors the Eishay MediaContent shape — List<Image> wrapped
        // inside a top-level bean, reader generated via ASM.
        ObjectMapper m = asmMapper();
        String json = "{\"name\":\"Javaone\",\"count\":2,\"images\":["
                + "{\"height\":768,\"width\":1024,\"title\":\"kn_large\",\"uri\":\"http://javaone.com/kn_large.jpg\"},"
                + "{\"height\":240,\"width\":320,\"title\":\"kn_small\",\"uri\":\"http://javaone.com/kn_small.jpg\"}"
                + "]}";
        Album a = m.readValue(json, Album.class);
        assertEquals(2, a.images.size());
        assertEquals("Javaone", a.name);
        assertEquals(768, a.images.get(0).height);
        assertEquals("kn_small", a.images.get(1).title);
    }
}
