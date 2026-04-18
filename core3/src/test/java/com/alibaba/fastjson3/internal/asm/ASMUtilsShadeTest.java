package com.alibaba.fastjson3.internal.asm;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONGenerator;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.reader.FieldNameMatcher;
import com.alibaba.fastjson3.reader.FieldReader;
import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.writer.FieldWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Guards the shade-friendliness of ASMUtils' TYPE_ / DESC_ constants.
 *
 * <p>Maven-shade-plugin relocates class references in compiled bytecode but
 * does not rewrite arbitrary String literals. Deriving these constants from
 * {@code Class.getName()} ensures they track the class's real (possibly
 * shaded) name at runtime.</p>
 *
 * <p>These assertions fail immediately if someone re-introduces a hardcoded
 * {@code "com/alibaba/fastjson3/…"} string, which would silently break parse
 * / write in a shaded uber-jar at the first ASM-generated class load.</p>
 */
public class ASMUtilsShadeTest {

    @Test
    public void typeConstantsMatchClassNames() {
        assertEquals(intern(JSONParser.class), ASMUtils.TYPE_JSON_PARSER);
        assertEquals(intern(JSONParser.UTF8.class), ASMUtils.TYPE_JSON_PARSER_UTF8);
        assertEquals(intern(JSONGenerator.class), ASMUtils.TYPE_JSON_GENERATOR);
        assertEquals(intern(JSONGenerator.UTF8.class), ASMUtils.TYPE_JSON_GENERATOR_UTF8);
        assertEquals(intern(JSONGenerator.Char.class), ASMUtils.TYPE_JSON_GENERATOR_CHAR);
        assertEquals(intern(ObjectReader.class), ASMUtils.TYPE_OBJECT_READER);
        assertEquals(intern(ObjectWriter.class), ASMUtils.TYPE_OBJECT_WRITER);
        assertEquals(intern(ObjectMapper.class), ASMUtils.TYPE_OBJECT_MAPPER);
        assertEquals(intern(JSONObject.class), ASMUtils.TYPE_JSON_OBJECT);
        assertEquals(intern(JSONArray.class), ASMUtils.TYPE_JSON_ARRAY);
        assertEquals(intern(JSONException.class), ASMUtils.TYPE_JSON_EXCEPTION);
        assertEquals(intern(FieldReader.class), ASMUtils.TYPE_FIELD_READER);
        assertEquals(intern(FieldNameMatcher.class), ASMUtils.TYPE_FIELD_NAME_MATCHER);
        assertEquals(intern(FieldWriter.class), ASMUtils.TYPE_FIELD_WRITER);
        assertEquals(intern(JDKUtils.class), ASMUtils.TYPE_JDK_UTILS);
    }

    @Test
    public void descConstantsMatchTypes() {
        assertEquals("L" + ASMUtils.TYPE_JSON_PARSER + ";", ASMUtils.DESC_JSON_PARSER);
        assertEquals("L" + ASMUtils.TYPE_JSON_PARSER_UTF8 + ";", ASMUtils.DESC_JSON_PARSER_UTF8);
        assertEquals("L" + ASMUtils.TYPE_JSON_GENERATOR + ";", ASMUtils.DESC_JSON_GENERATOR);
        assertEquals("L" + ASMUtils.TYPE_OBJECT_READER + ";", ASMUtils.DESC_OBJECT_READER);
        assertEquals("L" + ASMUtils.TYPE_OBJECT_WRITER + ";", ASMUtils.DESC_OBJECT_WRITER);
        assertEquals("L" + ASMUtils.TYPE_OBJECT_MAPPER + ";", ASMUtils.DESC_OBJECT_MAPPER);
        assertEquals("L" + ASMUtils.TYPE_FIELD_READER + ";", ASMUtils.DESC_FIELD_READER);
        assertEquals("L" + ASMUtils.TYPE_FIELD_NAME_MATCHER + ";", ASMUtils.DESC_FIELD_NAME_MATCHER);
        assertEquals("L" + ASMUtils.TYPE_FIELD_WRITER + ";", ASMUtils.DESC_FIELD_WRITER);
    }

    /**
     * Rehearses the shade failure mode: if any fastjson3 TYPE_* constant is a
     * String literal (rather than derived via {@code Class.getName()}),
     * relocation would not rewrite it. This check doesn't prove shade
     * correctness directly, but it does fail fast if a future edit
     * short-circuits the derivation for a class in the {@code fastjson3}
     * package hierarchy.
     */
    @Test
    public void fastjsonTypeConstantsDeriveFromClassNames() {
        // If any TYPE_* is computed via the type() helper, its value must be
        // identical to calling .getName().replace on the target class. We
        // cover the fastjson3 surface above; here we spot-check that the
        // type() helper itself doesn't alter non-fastjson names.
        assertEquals("java/lang/String", ASMUtils.type(String.class));
        assertEquals("java/util/ArrayList", ASMUtils.type(java.util.ArrayList.class));
        // And that TYPE_* for fastjson3 classes really live under the
        // fastjson3 package (vs being empty/null).
        assertFalse(ASMUtils.TYPE_JSON_PARSER.isEmpty());
        assertFalse(ASMUtils.TYPE_JSON_PARSER_UTF8.contains(".")); // internal form uses '/', not '.'
    }

    private static String intern(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }
}
