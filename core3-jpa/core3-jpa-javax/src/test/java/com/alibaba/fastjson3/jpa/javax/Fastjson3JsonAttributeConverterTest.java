package com.alibaba.fastjson3.jpa.javax;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3JsonAttributeConverterTest {
    public static class Profile {
        public String name;
        public int age;

        public Profile() {
        }

        public Profile(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class ProfileConverter extends Fastjson3JsonAttributeConverter<Profile> {
        public ProfileConverter() {
            super(Profile.class);
        }
    }

    public static class TagsConverter extends Fastjson3JsonAttributeConverter<List<String>> {
        public TagsConverter() {
            super(new TypeReference<List<String>>() {
            });
        }
    }

    public static class MetaConverter extends Fastjson3JsonAttributeConverter<Map<String, Integer>> {
        public MetaConverter() {
            super(new TypeReference<Map<String, Integer>>() {
            });
        }
    }

    @Test
    void simpleTypeRoundTrip() {
        ProfileConverter c = new ProfileConverter();
        String json = c.convertToDatabaseColumn(new Profile("alice", 30));
        Profile back = c.convertToEntityAttribute(json);
        assertEquals("alice", back.name);
        assertEquals(30, back.age);
    }

    @Test
    void parameterizedTypeRoundTrip() {
        TagsConverter c = new TagsConverter();
        List<String> tags = Arrays.asList("java", "json");
        String json = c.convertToDatabaseColumn(tags);
        List<String> back = c.convertToEntityAttribute(json);
        assertEquals(tags, back);
    }

    @Test
    void emptyCollectionRoundTrip() {
        TagsConverter c = new TagsConverter();
        String json = c.convertToDatabaseColumn(Collections.emptyList());
        assertEquals("[]", json);
        List<String> back = c.convertToEntityAttribute(json);
        assertNotNull(back);
        assertTrue(back.isEmpty());
    }

    @Test
    void nullAttributeProducesNullColumn() {
        assertNull(new ProfileConverter().convertToDatabaseColumn(null));
    }

    @Test
    void nullColumnProducesNullAttribute() {
        assertNull(new ProfileConverter().convertToEntityAttribute(null));
    }

    @Test
    void emptyColumnProducesNullAttribute() {
        assertNull(new ProfileConverter().convertToEntityAttribute(""));
    }

    @Test
    void customMapperRoundTrip() {
        ObjectMapper m = ObjectMapper.builder().build();
        Fastjson3JsonAttributeConverter<Profile> c =
                new Fastjson3JsonAttributeConverter<Profile>(Profile.class, m) {
                };
        Profile back = c.convertToEntityAttribute(c.convertToDatabaseColumn(new Profile("z", 5)));
        assertEquals("z", back.name);
    }

    @Test
    void mapTypeRoundTrip() {
        MetaConverter c = new MetaConverter();
        Map<String, Integer> meta = Map.of("a", 1, "b", 2);
        String json = c.convertToDatabaseColumn(meta);
        Map<String, Integer> back = c.convertToEntityAttribute(json);
        assertEquals(meta, back);
    }

    @Test
    void nullClassTargetTypeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonAttributeConverter<Profile>((Class<Profile>) null) {
                });
    }

    @Test
    void nullTypeReferenceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonAttributeConverter<Profile>((TypeReference<Profile>) null) {
                });
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonAttributeConverter<Profile>(Profile.class, null) {
                });
    }
}
