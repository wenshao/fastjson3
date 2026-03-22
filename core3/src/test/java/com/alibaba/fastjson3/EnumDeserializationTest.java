package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enum deserialization into POJO fields.
 * Covers the bug: "no suitable constructor found" for nested enum types.
 * See: https://github.com/wenshao/fastjson3/issues/49
 */
class EnumDeserializationTest {

    // ==================== Test models (eishay-style) ====================

    public static class Image {
        public enum Size {
            SMALL, LARGE
        }

        private int height;
        private int width;
        private Size size;
        private String title;
        private String uri;

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public Size getSize() { return size; }
        public void setSize(Size size) { this.size = size; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
    }

    public static class Media {
        public enum Player {
            JAVA, FLASH
        }

        private String uri;
        private Player player;
        private int duration;

        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public Player getPlayer() { return player; }
        public void setPlayer(Player player) { this.player = player; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
    }

    public static class MediaContent {
        private List<Image> images;
        private Media media;

        public List<Image> getImages() { return images; }
        public void setImages(List<Image> images) { this.images = images; }
        public Media getMedia() { return media; }
        public void setMedia(Media media) { this.media = media; }
    }

    // ==================== Basic nested enum deserialization ====================

    @Test
    void deserialize_nestedEnum_fromString() {
        String json = "{\"size\":\"LARGE\"}";
        Image image = JSON.parseObject(json, Image.class);
        assertEquals(Image.Size.LARGE, image.getSize());
    }

    @Test
    void deserialize_nestedEnum_allValues() {
        for (Image.Size size : Image.Size.values()) {
            String json = "{\"size\":\"" + size.name() + "\"}";
            Image image = JSON.parseObject(json, Image.class);
            assertEquals(size, image.getSize());
        }
    }

    @Test
    void deserialize_nestedEnum_withOtherFields() {
        String json = """
                {"height":768,"size":"LARGE","title":"Javaone Keynote","uri":"http://javaone.com/keynote_large.jpg","width":1024}""";
        Image image = JSON.parseObject(json, Image.class);
        assertEquals(768, image.getHeight());
        assertEquals(1024, image.getWidth());
        assertEquals(Image.Size.LARGE, image.getSize());
        assertEquals("Javaone Keynote", image.getTitle());
        assertEquals("http://javaone.com/keynote_large.jpg", image.getUri());
    }

    @Test
    void deserialize_nullEnum() {
        String json = "{\"size\":null,\"height\":100}";
        Image image = JSON.parseObject(json, Image.class);
        assertNull(image.getSize());
        assertEquals(100, image.getHeight());
    }

    // ==================== Media with Player enum ====================

    @Test
    void deserialize_mediaWithPlayer() {
        String json = "{\"uri\":\"http://example.com/video\",\"player\":\"JAVA\",\"duration\":120}";
        Media media = JSON.parseObject(json, Media.class);
        assertEquals(Media.Player.JAVA, media.getPlayer());
        assertEquals("http://example.com/video", media.getUri());
        assertEquals(120, media.getDuration());
    }

    // ==================== Full MediaContent (eishay-style) ====================

    @Test
    void deserialize_mediaContent_eishayStyle() {
        String json = """
                {"images":[{"height":768,"size":"LARGE","title":"Javaone Keynote","uri":"http://javaone.com/keynote_large.jpg","width":1024},\
                {"height":240,"size":"SMALL","title":"Javaone Keynote","uri":"http://javaone.com/keynote_small.jpg","width":320}],\
                "media":{"uri":"http://javaone.com/keynote.mpg","player":"JAVA","duration":18000000}}""";
        MediaContent mc = JSON.parseObject(json, MediaContent.class);

        assertNotNull(mc.getImages());
        assertEquals(2, mc.getImages().size());
        assertEquals(Image.Size.LARGE, mc.getImages().get(0).getSize());
        assertEquals(Image.Size.SMALL, mc.getImages().get(1).getSize());

        assertNotNull(mc.getMedia());
        assertEquals(Media.Player.JAVA, mc.getMedia().getPlayer());
    }

    // ==================== Round-trip ====================

    @Test
    void roundTrip_imageWithEnum() {
        Image image = new Image();
        image.setHeight(768);
        image.setWidth(1024);
        image.setSize(Image.Size.LARGE);
        image.setTitle("Test");
        image.setUri("http://example.com/test.jpg");

        String json = JSON.toJSONString(image);
        Image parsed = JSON.parseObject(json, Image.class);

        assertEquals(image.getHeight(), parsed.getHeight());
        assertEquals(image.getWidth(), parsed.getWidth());
        assertEquals(image.getSize(), parsed.getSize());
        assertEquals(image.getTitle(), parsed.getTitle());
        assertEquals(image.getUri(), parsed.getUri());
    }

    // ==================== Top-level enum (non-nested) ====================

    public enum Color {
        RED, GREEN, BLUE
    }

    public static class Palette {
        private Color primary;
        private Color secondary;

        public Color getPrimary() { return primary; }
        public void setPrimary(Color primary) { this.primary = primary; }
        public Color getSecondary() { return secondary; }
        public void setSecondary(Color secondary) { this.secondary = secondary; }
    }

    @Test
    void deserialize_topLevelEnum_inPojo() {
        String json = "{\"primary\":\"RED\",\"secondary\":\"BLUE\"}";
        Palette palette = JSON.parseObject(json, Palette.class);
        assertEquals(Color.RED, palette.getPrimary());
        assertEquals(Color.BLUE, palette.getSecondary());
    }

    // ==================== UTF-8 byte path ====================

    @Test
    void deserialize_enum_fromUTF8Bytes() {
        byte[] json = "{\"size\":\"SMALL\",\"height\":100}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Image image = JSON.parseObject(json, Image.class);
        assertEquals(Image.Size.SMALL, image.getSize());
        assertEquals(100, image.getHeight());
    }

    // ==================== Ordinal-based enum deserialization ====================

    @Test
    void deserialize_enum_fromOrdinal() {
        // Image.Size: SMALL=0, LARGE=1
        String json = "{\"size\":1,\"height\":100}";
        Image image = JSON.parseObject(json, Image.class);
        assertEquals(Image.Size.LARGE, image.getSize());
        assertEquals(100, image.getHeight());
    }

    @Test
    void deserialize_enum_fromOrdinalZero() {
        String json = "{\"size\":0}";
        Image image = JSON.parseObject(json, Image.class);
        assertEquals(Image.Size.SMALL, image.getSize());
    }

    @Test
    void deserialize_enum_fromOrdinalOutOfRange_throwsException() {
        String json = "{\"size\":99}";
        assertThrows(JSONException.class, () -> JSON.parseObject(json, Image.class));
    }

    @Test
    void deserialize_enum_fromNegativeOrdinal_throwsException() {
        String json = "{\"size\":-1}";
        assertThrows(JSONException.class, () -> JSON.parseObject(json, Image.class));
    }

    // ==================== Invalid enum name ====================

    @Test
    void deserialize_invalidEnumName_throwsException() {
        String json = "{\"size\":\"INVALID\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(json, Image.class));
    }

    // ==================== Enum with abstract methods ====================

    public enum Operation {
        PLUS {
            @Override
            public int apply(int a, int b) { return a + b; }
        },
        MINUS {
            @Override
            public int apply(int a, int b) { return a - b; }
        };

        public abstract int apply(int a, int b);
    }

    public static class Calculator {
        private Operation op;

        public Operation getOp() { return op; }
        public void setOp(Operation op) { this.op = op; }
    }

    @Test
    void deserialize_enumWithAbstractMethods() {
        String json = "{\"op\":\"PLUS\"}";
        Calculator calc = JSON.parseObject(json, Calculator.class);
        assertEquals(Operation.PLUS, calc.getOp());
        assertEquals(3, calc.getOp().apply(1, 2));
    }

    // ==================== Multiple enum fields ====================

    @Test
    void deserialize_multipleEnumFields() {
        Palette palette = JSON.parseObject("{\"primary\":\"RED\",\"secondary\":\"BLUE\"}", Palette.class);
        assertEquals(Color.RED, palette.getPrimary());
        assertEquals(Color.BLUE, palette.getSecondary());

        // Round trip
        String json = JSON.toJSONString(palette);
        Palette parsed = JSON.parseObject(json, Palette.class);
        assertEquals(palette.getPrimary(), parsed.getPrimary());
        assertEquals(palette.getSecondary(), parsed.getSecondary());
    }
}
