package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FieldNameMatcher - high-performance field name matching.
 */
class FieldNameMatcherTest {

    @Test
    void testBuild_empty() {
        FieldReader[] readers = new FieldReader[0];
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        assertNotNull(matcher);
        assertEquals(0, matcher.mask);
    }

    @Test
    void testBuild_singleField() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        assertNotNull(matcher);
    }

    @Test
    void testBuild_multipleFields() {
        FieldReader[] readers = {
                createFieldReader("name", 0),
                createFieldReader("age", 1),
                createFieldReader("email", 2)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        assertNotNull(matcher);
    }

    @Test
    void testMatchFlat_singleField() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        long hash = matcher.hash("name");
        FieldReader result = matcher.matchFlat(hash);

        assertSame(nameReader, result);
    }

    @Test
    void testMatchFlat_multipleFields() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader ageReader = createFieldReader("age", 1);
        FieldReader emailReader = createFieldReader("email", 2);

        FieldReader[] readers = {nameReader, ageReader, emailReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        assertSame(nameReader, matcher.matchFlat(matcher.hash("name")));
        assertSame(ageReader, matcher.matchFlat(matcher.hash("age")));
        assertSame(emailReader, matcher.matchFlat(matcher.hash("email")));
    }

    @Test
    void testMatchFlat_notFound() {
        FieldReader[] readers = {createFieldReader("name", 0), createFieldReader("age", 1)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldReader result = matcher.matchFlat(matcher.hash("nonexistent"));

        assertNull(result);
    }

    @Test
    void testMatch_singleField() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        long hash = matcher.hash("name");
        FieldReader result = matcher.match(hash);

        assertSame(nameReader, result);
    }

    @Test
    void testMatch_multipleFields() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader ageReader = createFieldReader("age", 1);
        FieldReader emailReader = createFieldReader("email", 2);

        FieldReader[] readers = {nameReader, ageReader, emailReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        assertSame(nameReader, matcher.match(matcher.hash("name")));
        assertSame(ageReader, matcher.match(matcher.hash("age")));
        assertSame(emailReader, matcher.match(matcher.hash("email")));
    }

    @Test
    void testMatch_notFound() {
        FieldReader[] readers = {createFieldReader("name", 0), createFieldReader("age", 1)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldReader result = matcher.match(matcher.hash("nonexistent"));

        assertNull(result);
    }

    @Test
    void testMatchStrict_withName() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldReader result = matcher.matchStrict(matcher.hash("name"), "name");

        assertSame(nameReader, result);
    }

    @Test
    void testMatchStrict_wrongName() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // Different name with same potential hash collision
        FieldReader result = matcher.matchStrict(matcher.hash("name"), "other");

        assertNull(result);
    }

    @Test
    void testMatchStrict_notFound() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldReader result = matcher.matchStrict(matcher.hash("nonexistent"), "nonexistent");

        assertNull(result);
    }

    // ==================== Hash function tests ====================

    @Test
    void testHash_consistency() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        long hash1 = matcher.hash("name");
        long hash2 = matcher.hash("name");

        assertEquals(hash1, hash2);
    }

    @Test
    void testHash_differentInputs() {
        FieldReader[] readers = {createFieldReader("name", 0), createFieldReader("age", 1)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        long hashName = matcher.hash("name");
        long hashAge = matcher.hash("age");

        assertNotEquals(hashName, hashAge);
    }

    @Test
    void testHash_emptyString() {
        FieldReader[] readers = {createFieldReader("", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        long hash = matcher.hash("");

        // Should not throw
        assertEquals(0, hash);
    }

    // ==================== Strategy tests ====================

    @Test
    void testStrategy_plhv() {
        FieldReader[] readers = {
                createFieldReader("a", 0),
                createFieldReader("b", 1),
                createFieldReader("c", 2)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // These should use PLHV (addition) strategy since there are no collisions
        assertTrue(matcher.strategy == FieldNameMatcher.STRATEGY_PLHV ||
                   matcher.strategy == FieldNameMatcher.STRATEGY_BIHV ||
                   matcher.strategy == FieldNameMatcher.STRATEGY_PRHV);
    }

    @Test
    void testHashStep_plhv() {
        FieldReader[] readers = {createFieldReader("abc", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        if (matcher.strategy == FieldNameMatcher.STRATEGY_PLHV) {
            long h = 0;
            h = matcher.hashStep(h, 'a');
            h = matcher.hashStep(h, 'b');
            h = matcher.hashStep(h, 'c');

            assertEquals('a' + 'b' + 'c', h);
        }
    }

    @Test
    void testHashStep_bihv() {
        // Create matcher that should use BIHV strategy
        FieldReader[] readers = {
                createFieldReader("field1", 0),
                createFieldReader("field2", 1),
                createFieldReader("field3", 2),
                createFieldReader("field4", 3)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // Test that hashStep is deterministic
        long h1 = matcher.hashStep(100, 'a');
        long h2 = matcher.hashStep(100, 'a');

        assertEquals(h1, h2);
    }

    @Test
    void testHashStep_prhv() {
        // Create matcher that might use PRHV strategy
        FieldReader[] readers = {
                createFieldReader("a", 0),
                createFieldReader("aa", 1),
                createFieldReader("aaa", 2),
                createFieldReader("aaaa", 3),
                createFieldReader("aaaaa", 4)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // Test that hashStep is deterministic
        long h1 = matcher.hashStep(100, 'a');
        long h2 = matcher.hashStep(100, 'a');

        assertEquals(h1, h2);
    }

    // ==================== Byte matching tests ====================

    @Test
    void testGetCandidates_empty() {
        FieldReader[] readers = new FieldReader[0];
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldNameMatcher.ByteCandidate[] candidates = matcher.getCandidates('a');

        assertNull(candidates);
    }

    @Test
    void testGetCandidates_singleField() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldNameMatcher.ByteCandidate[] candidates = matcher.getCandidates('n');

        assertNotNull(candidates);
        assertEquals(1, candidates.length);
        assertSame(readers[0], candidates[0].reader);
    }

    @Test
    void testGetCandidates_multipleFieldsSameFirstByte() {
        FieldReader[] readers = {
                createFieldReader("name", 0),
                createFieldReader("number", 1),
                createFieldReader("name2", 2)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldNameMatcher.ByteCandidate[] candidates = matcher.getCandidates('n');

        assertNotNull(candidates);
        assertTrue(candidates.length >= 3);
    }

    @Test
    void testGetCandidates_differentFirstByte() {
        FieldReader[] readers = {
                createFieldReader("name", 0),
                createFieldReader("age", 1)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        FieldNameMatcher.ByteCandidate[] nameCandidates = matcher.getCandidates('n');
        FieldNameMatcher.ByteCandidate[] ageCandidates = matcher.getCandidates('a');

        assertNotNull(nameCandidates);
        assertNotNull(ageCandidates);
    }

    @Test
    void testMatchBytes_exactMatch() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        byte[] input = "name".getBytes(StandardCharsets.UTF_8);
        FieldReader result = matcher.matchBytes(input, 0, input.length);

        assertSame(nameReader, result);
    }

    @Test
    void testMatchBytes_withOffset() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        byte[] input = "XXXnameYYY".getBytes(StandardCharsets.UTF_8);
        FieldReader result = matcher.matchBytes(input, 3, 4);

        assertSame(nameReader, result);
    }

    @Test
    void testMatchBytes_notFound() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        byte[] input = "other".getBytes(StandardCharsets.UTF_8);
        FieldReader result = matcher.matchBytes(input, 0, input.length);

        assertNull(result);
    }

    @Test
    void testMatchBytes_emptyInput() {
        FieldReader[] readers = {createFieldReader("name", 0)};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        byte[] input = new byte[0];
        FieldReader result = matcher.matchBytes(input, 0, 0);

        assertNull(result);
    }

    @Test
    void testMatchBytes_wrongLength() {
        FieldReader nameReader = createFieldReader("name", 0);
        FieldReader[] readers = {nameReader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        byte[] input = "nam".getBytes(StandardCharsets.UTF_8);
        FieldReader result = matcher.matchBytes(input, 0, input.length);

        assertNull(result);
    }

    // ==================== Alternate names tests ====================

    @Test
    void testMatchWithAlternateNames() {
        FieldReader reader = createFieldReader("userName", 0, "user_name");
        FieldReader[] readers = {reader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // Both names should map to the same reader
        assertSame(reader, matcher.matchFlat(matcher.hash("userName")));
        assertSame(reader, matcher.matchFlat(matcher.hash("user_name")));
    }

    @Test
    void testMatchBytesWithAlternateNames() {
        FieldReader reader = createFieldReader("userName", 0, "user_name");
        FieldReader[] readers = {reader};
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // Test byte matching with alternate name
        byte[] input = "user_name".getBytes(StandardCharsets.UTF_8);
        FieldReader result = matcher.matchBytes(input, 0, input.length);

        assertSame(reader, result);
    }

    // ==================== Collision handling tests ====================

    @Test
    void testHashCollision_manyFields() {
        // Create many fields to test collision handling
        FieldReader[] readers = new FieldReader[50];
        for (int i = 0; i < 50; i++) {
            readers[i] = createFieldReader("field" + i, i);
        }

        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // All fields should still be accessible
        for (int i = 0; i < 50; i++) {
            FieldReader result = matcher.matchFlat(matcher.hash("field" + i));
            assertSame(readers[i], result, "Failed to match field" + i);
        }
    }

    @Test
    void testSimilarFieldNames() {
        FieldReader[] readers = {
                createFieldReader("field", 0),
                createFieldReader("field1", 1),
                createFieldReader("field2", 2),
                createFieldReader("fieldA", 3),
                createFieldReader("fieldB", 4)
        };
        FieldNameMatcher matcher = FieldNameMatcher.build(readers);

        // All similar names should be distinguishable
        for (FieldReader reader : readers) {
            FieldReader result = matcher.matchFlat(matcher.hash(reader.fieldName));
            assertSame(reader, result);
        }
    }

    // ==================== ByteCandidate class tests ====================

    @Test
    void testByteCandidate_properties() {
        FieldReader reader = createFieldReader("test", 0);
        byte[] nameBytes = "test".getBytes(StandardCharsets.UTF_8);

        FieldNameMatcher.ByteCandidate candidate =
                new FieldNameMatcher.ByteCandidate(nameBytes, nameBytes.length, reader);

        assertSame(nameBytes, candidate.nameBytes);
        assertEquals(nameBytes.length, candidate.nameLen);
        assertSame(reader, candidate.reader);
    }

    // ==================== Entry class tests ====================

    @Test
    void testEntry_properties() {
        FieldReader reader = createFieldReader("test", 0);
        long hash = 12345L;

        FieldNameMatcher.Entry entry =
                new FieldNameMatcher.Entry(hash, "test", reader, null);

        assertEquals(hash, entry.hash);
        assertEquals("test", entry.name);
        assertSame(reader, entry.fieldReader);
        assertNull(entry.next);
    }

    @Test
    void testEntry_chain() {
        FieldReader reader1 = createFieldReader("test1", 0);
        FieldReader reader2 = createFieldReader("test2", 1);

        FieldNameMatcher.Entry entry1 =
                new FieldNameMatcher.Entry(1L, "test1", reader1, null);
        FieldNameMatcher.Entry entry2 =
                new FieldNameMatcher.Entry(2L, "test2", reader2, entry1);

        assertSame(entry1, entry2.next);
        assertSame(reader1, entry2.next.fieldReader);
    }

    // ==================== Helper methods ====================

    private FieldReader createFieldReader(String name, int ordinal) {
        return new FieldReader(
                name,
                new String[0],
                String.class,
                String.class,
                ordinal,
                null,
                false,
                null,
                null
        );
    }

    private FieldReader createFieldReader(String name, int ordinal, String... alternateNames) {
        return new FieldReader(
                name,
                alternateNames,
                String.class,
                String.class,
                ordinal,
                null,
                false,
                null,
                null
        );
    }
}
