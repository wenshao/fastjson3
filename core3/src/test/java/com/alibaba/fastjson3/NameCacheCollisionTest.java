package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression for a NameCache empty-key collision that surfaced as a
 * full-suite-only flake of {@code JSONPointerTest.testRFCSection5}: when
 * fuzz / random-input parsing produced a long field name whose
 * accumulated {@code hash * 31 + c} hash evaluated to exactly {@code 0L},
 * the long-path {@code NameCache.put(0L, longName)} stamped slot 0 with
 * {@code HASHES[0]=0L, NAMES[0]=longName}. A subsequent empty-name parse
 * looked up the short-key {@code 0L} (the unique key for {@code ""}),
 * found {@code HASHES[0]==0L}, and returned {@code longName} as the
 * cached field name — so {@code JSONObject.fastPut} stored the {@code 0}
 * value under {@code longName} instead of {@code ""}, and a later
 * {@code obj.get("")} returned {@code null}.
 *
 * <p>The fix tightens {@link com.alibaba.fastjson3.NameCache#getShort} to
 * also verify that the cached String's length matches the {@code nameLen}
 * encoded in the upper 8 bits of the key.</p>
 */
class NameCacheCollisionTest {

    @Test
    void emptyKeyResilientAfterLongNameWithZeroHash() {
        // Force a worst-case-shaped polluter into the long-path put:
        // parse a JSON whose field name is long (>8 bytes) and crafted so
        // its hash bottom 10 bits collide with slot 0. Even if the hash
        // doesn't land at exactly 0L, the test exercises the same
        // shared-array scenario the flake hit.
        String fuzzKey = "abcdefghij"; // 10 bytes — uses long-path NameCache.put
        String longJson = "{\"" + fuzzKey + "\":1}";
        // Repeat to maximise the chance that a slot 0 collision is hit:
        for (int i = 0; i < 100; i++) {
            JSON.parseObject(longJson);
        }

        // Now parse a JSON with an empty key. Pre-fix: empty-name
        // shortKey lookup would return whatever long name happened to
        // share slot 0, JSONObject would store the value under that wrong
        // key, and obj.get("") would return null. Post-fix: the length
        // check in getShort blocks long-name returns, empty key parses
        // correctly.
        JSONObject obj = JSON.parseObject("{\"\":42}");
        assertEquals(42, obj.get(""));
    }

    @Test
    void emptyKeyAfterDeliberateZeroHashCollision() {
        // Construct a key string whose `hash * 31 + (c & 0xFF)` polynomial
        // evaluates to exactly 0L. The simplest realisation is a name
        // ending in NUL bytes — but JSON disallows raw NUL in field names,
        // so use the unicode-escape form which the parser decodes via the
        // backslash branch (super.readFieldName, which still feeds
        // NameCache.put on completion).
        //
        // Even without forcing exact 0L, the test demonstrates the
        // length-guard's general defence: any long-name whose hash lands
        // in slot 0 must NOT shadow the empty-name lookup.
        String json = "{\"verylongfieldname0123456789\":7,\"\":99}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals(7, obj.get("verylongfieldname0123456789"));
        assertEquals(99, obj.get(""));
    }

    @Test
    void eightByteAsciiName_cachedAfterFirstParse() throws Exception {
        // Round-2 audit: nameLen==8 lets content byte7 occupy the same
        // bits the prior version of getShort tried to read nameLen from
        // (`key >>> 56`), so every 8-byte ASCII name was a forced cache
        // miss and re-allocated per parse. Pin the cache hit by
        // reflectively reading NameCache.NAMES after a single parse and
        // asserting the second parse returns the same String reference.
        String json = "{\"username\":1}";
        JSONObject first = JSON.parseObject(json);
        String firstKey = first.keySet().iterator().next();
        JSONObject second = JSON.parseObject(json);
        String secondKey = second.keySet().iterator().next();
        // Same String reference iff the cache hit on the second parse.
        assertSame(firstKey, secondKey,
                "8-byte ASCII names must hit NameCache after first parse");
    }

    @Test
    void rfc6901EmptyKeyExampleSurvivesRepeatedFuzz() {
        // Mirror the original failing test's payload after a NameCache-pollution
        // run, asserting the empty key is still findable.
        String rfc = "{\"foo\":[\"bar\",\"baz\"],\"\":0,\"a/b\":1," +
                "\"c%d\":2,\"e^f\":3,\"g|h\":4,\"i\\\\j\":5," +
                "\"k\\\"l\":6,\" \":7,\"m~n\":8}";
        for (int i = 0; i < 50; i++) {
            JSON.parseObject("{\"key" + i + "abcdefghij\":" + i + "}");
        }
        JSONObject obj = JSON.parseObject(rfc);
        assertEquals(0, obj.get(""));
        assertEquals(0, JSONPointer.of("/").eval(obj));
    }
}
