package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.Inclusion;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pin field-name escape behaviour across every writer surface fuzz-driven
 * audit reached. fuzzRoundTrip covers the same cases probabilistically;
 * this class makes the contract explicit so a future refactor can't silently
 * regress without a failing test.
 */
class FieldNameEscapeTest {

    static class AsmEligibleBean {
        @JSONField(name = "a\"b", inclusion = Inclusion.ALWAYS) public int x;
        @JSONField(name = "c\\d", inclusion = Inclusion.ALWAYS) public int y;
        @JSONField(name = "e\nf", inclusion = Inclusion.ALWAYS) public int z;
    }

    static class Bean {
        @JSONField(name = "a\\b") public int aB;
        @JSONField(name = "c\"d") public int cD;
    }

    @Test
    void mapKeyEscapeBackslash() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a\\b", 1);
        assertEquals("{\"a\\\\b\":1}", JSON.toJSONString(m));
    }

    @Test
    void mapKeyEscapeQuote() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a\"b", 1);
        assertEquals("{\"a\\\"b\":1}", JSON.toJSONString(m));
    }

    @Test
    void mapKeyEscapeNamedControl() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a\nb", 1);
        m.put("a\tb", 2);
        assertEquals("{\"a\\nb\":1,\"a\\tb\":2}", JSON.toJSONString(m));
    }

    @Test
    void mapKeyEscapeUnnamedControl() {
        // Chars 0x01 and 0x1F have no named JSON escape per RFC 8259 sec 7
        // so the writer must emit them as the unicode escape form.
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ab", 1);
        m.put("ab", 2);
        assertEquals("{\"a\\u0001b\":1,\"a\\u001fb\":2}", JSON.toJSONString(m));
    }

    @Test
    void mapKeyExternalJsonRoundTripsViaTypedBean() {
        // Standard JSON `{"a\\b":42}` (correctly escaped) — pre-fix the
        // typed reader silently dropped this field because the writer
        // emitted `{"a\b":42}` and the matcher compared raw bytes.
        String standardJson = "{\"a\\\\b\":42,\"c\\\"d\":7}";
        Bean parsed = JSON.parseObject(standardJson, Bean.class);
        assertEquals(42, parsed.aB);
        assertEquals(7, parsed.cD);
    }

    @Test
    void asmEligibleBeanRoundTripsAndIsValidJson() {
        AsmEligibleBean b = new AsmEligibleBean();
        b.x = 42;
        b.y = 100;
        b.z = 7;
        String s = JSON.toJSONString(b);
        // Wire form must escape every special char — pre-fix the ASM
        // pipeline (separate from FieldWriter.encodeNameChars) emitted
        // raw special chars for ASM-eligible beans, which fails any
        // conformant parser.
        assertEquals("{\"a\\\"b\":42,\"c\\\\d\":100,\"e\\nf\":7}", s);
        AsmEligibleBean rt = JSON.parseObject(s, AsmEligibleBean.class);
        assertEquals(42, rt.x);
        assertEquals(100, rt.y);
        assertEquals(7, rt.z);
    }

    @Test
    void prelookupFieldNameWithBackslash() {
        // Hash-table lookup: the matcher's table is built from the raw
        // annotation name `a\b` (3 chars). Slow path resolves the wire
        // `a\\b` to `a\b` via readFieldNameHashEscape and PLHV hash 287
        // matches.
        String json = "{\"a\\\\b\":99}";
        Bean parsed = JSON.parseObject(json, Bean.class);
        assertEquals(99, parsed.aB);
    }
}
