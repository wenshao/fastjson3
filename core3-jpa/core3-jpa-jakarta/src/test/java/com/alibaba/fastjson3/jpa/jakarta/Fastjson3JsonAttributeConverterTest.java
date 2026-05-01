package com.alibaba.fastjson3.jpa.jakarta;

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

    public static class CustomMapperConverter extends Fastjson3JsonAttributeConverter<Profile> {
        public CustomMapperConverter(ObjectMapper mapper) {
            super(Profile.class, mapper);
        }
    }

    public static class CustomMapperGenericConverter extends Fastjson3JsonAttributeConverter<List<String>> {
        public CustomMapperGenericConverter(ObjectMapper mapper) {
            super(new TypeReference<List<String>>() {
            }, mapper);
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
    void mapTypeRoundTrip() {
        MetaConverter c = new MetaConverter();
        Map<String, Integer> meta = Map.of("a", 1, "b", 2);
        String json = c.convertToDatabaseColumn(meta);
        Map<String, Integer> back = c.convertToEntityAttribute(json);
        assertEquals(meta, back);
    }

    @Test
    void emptyCollectionRoundTrip() {
        // "[]" must round-trip to an empty (non-null) list
        TagsConverter c = new TagsConverter();
        String json = c.convertToDatabaseColumn(Collections.emptyList());
        assertEquals("[]", json);
        List<String> back = c.convertToEntityAttribute(json);
        assertNotNull(back);
        assertTrue(back.isEmpty());
    }

    @Test
    void nullAttributeProducesNullColumn() {
        ProfileConverter c = new ProfileConverter();
        assertNull(c.convertToDatabaseColumn(null));
    }

    @Test
    void nullColumnProducesNullAttribute() {
        ProfileConverter c = new ProfileConverter();
        assertNull(c.convertToEntityAttribute(null));
    }

    @Test
    void emptyColumnProducesNullAttribute() {
        // Many DBs canonicalize empty TEXT to "" rather than NULL.
        ProfileConverter c = new ProfileConverter();
        assertNull(c.convertToEntityAttribute(""));
    }

    @Test
    void customMapperConstructor() {
        ObjectMapper m = ObjectMapper.builder().build();
        CustomMapperConverter c = new CustomMapperConverter(m);
        Profile p = new Profile("bob", 40);
        Profile back = c.convertToEntityAttribute(c.convertToDatabaseColumn(p));
        assertEquals("bob", back.name);
    }

    @Test
    void customMapperGenericConstructor() {
        ObjectMapper m = ObjectMapper.builder().build();
        CustomMapperGenericConverter c = new CustomMapperGenericConverter(m);
        List<String> back = c.convertToEntityAttribute(c.convertToDatabaseColumn(Arrays.asList("a", "b")));
        assertEquals(Arrays.asList("a", "b"), back);
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
