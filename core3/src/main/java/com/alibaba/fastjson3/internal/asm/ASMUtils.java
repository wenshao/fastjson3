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

/**
 * ASM utility constants and helpers for fastjson3 bytecode generation.
 *
 * <p>All class-name / descriptor constants below are derived from {@code Class}
 * literals via {@code .getName().replace('.', '/')} rather than hardcoded
 * strings. This is load-bearing for {@code maven-shade-plugin} users: shade
 * rewrites class references in compiled bytecode but does NOT rewrite the
 * content of arbitrary String literals. A hardcoded {@code "com/alibaba/fastjson3/…"}
 * string would survive relocation unchanged, the generator would emit bytecode
 * referencing a class that no longer exists in the shaded jar, and the first
 * parse/write would fail with {@link NoClassDefFoundError}. Routing through a
 * class literal makes the emitted class name match the shaded class name at
 * runtime.</p>
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ASMUtils {
    // JDK internal class names — outside shade's relocation scope; safe to hardcode.
    public static final String TYPE_OBJECT = "java/lang/Object";
    public static final String TYPE_STRING = "java/lang/String";
    public static final String TYPE_INTEGER = "java/lang/Integer";
    public static final String TYPE_LONG_OBJ = "java/lang/Long";
    public static final String TYPE_DOUBLE_OBJ = "java/lang/Double";
    public static final String TYPE_FLOAT_OBJ = "java/lang/Float";
    public static final String TYPE_BOOLEAN_OBJ = "java/lang/Boolean";
    public static final String TYPE_NUMBER = "java/lang/Number";
    public static final String TYPE_BIG_DECIMAL = "java/math/BigDecimal";
    public static final String TYPE_LIST = "java/util/List";
    public static final String TYPE_ARRAYLIST = "java/util/ArrayList";
    public static final String TYPE_MAP = "java/util/Map";
    public static final String TYPE_COLLECTION = "java/util/Collection";

    // core3 internal class names — derive from Class literals so shade rewrites them.
    public static final String TYPE_JSON_PARSER = type(JSONParser.class);
    public static final String TYPE_JSON_PARSER_UTF8 = type(JSONParser.UTF8.class);
    public static final String TYPE_JSON_GENERATOR = type(JSONGenerator.class);
    public static final String TYPE_JSON_GENERATOR_UTF8 = type(JSONGenerator.UTF8.class);
    public static final String TYPE_JSON_GENERATOR_CHAR = type(JSONGenerator.Char.class);
    public static final String TYPE_OBJECT_READER = type(ObjectReader.class);
    public static final String TYPE_OBJECT_WRITER = type(ObjectWriter.class);
    public static final String TYPE_OBJECT_MAPPER = type(ObjectMapper.class);
    public static final String TYPE_JSON_OBJECT = type(JSONObject.class);
    public static final String TYPE_JSON_ARRAY = type(JSONArray.class);
    public static final String TYPE_JSON_EXCEPTION = type(JSONException.class);
    public static final String TYPE_FIELD_READER = type(FieldReader.class);
    public static final String TYPE_FIELD_NAME_MATCHER = type(FieldNameMatcher.class);
    public static final String TYPE_FIELD_WRITER = type(FieldWriter.class);
    public static final String TYPE_JDK_UTILS = type(JDKUtils.class);

    // Descriptors — derived from the TYPE_* constants above so they inherit
    // the shade-rewriting behaviour.
    public static final String DESC_OBJECT = "Ljava/lang/Object;";
    public static final String DESC_STRING = "Ljava/lang/String;";
    public static final String DESC_JSON_PARSER = "L" + TYPE_JSON_PARSER + ";";
    public static final String DESC_JSON_PARSER_UTF8 = "L" + TYPE_JSON_PARSER_UTF8 + ";";
    public static final String DESC_JSON_GENERATOR = "L" + TYPE_JSON_GENERATOR + ";";
    public static final String DESC_OBJECT_READER = "L" + TYPE_OBJECT_READER + ";";
    public static final String DESC_OBJECT_WRITER = "L" + TYPE_OBJECT_WRITER + ";";
    public static final String DESC_OBJECT_MAPPER = "L" + TYPE_OBJECT_MAPPER + ";";
    public static final String DESC_FIELD_READER = "L" + TYPE_FIELD_READER + ";";
    public static final String DESC_FIELD_NAME_MATCHER = "L" + TYPE_FIELD_NAME_MATCHER + ";";
    public static final String DESC_FIELD_WRITER = "L" + TYPE_FIELD_WRITER + ";";

    // Method descriptors for ObjectWriter.write
    public static final String METHOD_DESC_WRITE =
            "(" + DESC_JSON_GENERATOR + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";

    // Method descriptors for ObjectReader.readObject
    public static final String METHOD_DESC_READ_OBJECT =
            "(" + DESC_JSON_PARSER + "Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;";

    private ASMUtils() {
    }

    /**
     * Convert a Class to its ASM internal name (e.g., "com/alibaba/fastjson3/JSON").
     */
    public static String type(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Convert a Class to its ASM descriptor (e.g., "Lcom/alibaba/fastjson3/JSON;").
     */
    public static String desc(Class<?> clazz) {
        if (clazz == void.class) {
            return "V";
        }
        if (clazz == boolean.class) {
            return "Z";
        }
        if (clazz == byte.class) {
            return "B";
        }
        if (clazz == char.class) {
            return "C";
        }
        if (clazz == short.class) {
            return "S";
        }
        if (clazz == int.class) {
            return "I";
        }
        if (clazz == long.class) {
            return "J";
        }
        if (clazz == float.class) {
            return "F";
        }
        if (clazz == double.class) {
            return "D";
        }
        if (clazz.isArray()) {
            return "[" + desc(clazz.getComponentType());
        }
        return "L" + type(clazz) + ";";
    }
}
