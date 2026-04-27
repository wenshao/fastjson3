package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressions for typed Bean / record fields declared as
 * {@code JSONObject[]} / {@code JSONArray[]} / {@code Object[]}. Prior to
 * this fix, the reader-creation short-circuit (introduced in #139, #141)
 * left {@code objReaders[i]} null for these fields, so the runtime
 * fallback called {@code parser.readAny()} which yields a
 * {@code JSONArray} (a {@code List}). The existing
 * {@link com.alibaba.fastjson3.reader.FieldReader#convertValue} had no
 * {@code List → array} bridge, so {@code FieldReader.setFieldValue}'s
 * {@code Unsafe.putObject} stamped the {@code JSONArray} reference into
 * the array-typed field, tearing the field's static type and crashing
 * the JVM in some configurations (exit 134) — or silently leaving a
 * length-0 array of nulls.
 *
 * <p>The fix adds a {@code List → array} branch in
 * {@code convertValue} so the reader fallback materialises a real array
 * with element-by-element copy; primitive component types are coerced
 * through the existing numeric narrowing rules.
 */
class JsonNodeArrayFieldTest {

    public static class WithJSONObjectArray { public JSONObject[] arr; }
    public static class WithJSONArrayArray { public JSONArray[] arr; }
    public static class WithObjectArray { public Object[] arr; }
    public static class WithIntArray { public int[] arr; }
    public static class WithStringArray { public String[] arr; }

    @Test
    void jsonObjectArray_populated() {
        WithJSONObjectArray b = ObjectMapper.shared().readValue(
                "{\"arr\":[{\"a\":1},{\"b\":2}]}", WithJSONObjectArray.class);
        assertNotNull(b.arr);
        assertEquals(2, b.arr.length);
        assertEquals(1, b.arr[0].get("a"));
        assertEquals(2, b.arr[1].get("b"));
    }

    @Test
    void jsonArrayArray_populated() {
        WithJSONArrayArray b = ObjectMapper.shared().readValue(
                "{\"arr\":[[1,2],[3,4]]}", WithJSONArrayArray.class);
        assertNotNull(b.arr);
        assertEquals(2, b.arr.length);
        assertEquals(2, b.arr[0].size());
        assertEquals(1, b.arr[0].get(0));
        assertEquals(4, b.arr[1].get(1));
    }

    @Test
    void objectArray_heterogeneous() {
        WithObjectArray b = ObjectMapper.shared().readValue(
                "{\"arr\":[{\"a\":1},[1,2],\"s\",42,true,null]}", WithObjectArray.class);
        assertNotNull(b.arr);
        assertEquals(6, b.arr.length);
        assertInstanceOf(JSONObject.class, b.arr[0]);
        assertInstanceOf(JSONArray.class, b.arr[1]);
        assertEquals("s", b.arr[2]);
        assertEquals(42, ((Number) b.arr[3]).intValue());
        assertEquals(Boolean.TRUE, b.arr[4]);
        assertNull(b.arr[5]);
    }

    @Test
    void jsonObjectArray_emptyArray() {
        WithJSONObjectArray b = ObjectMapper.shared().readValue("{\"arr\":[]}", WithJSONObjectArray.class);
        assertNotNull(b.arr);
        assertEquals(0, b.arr.length);
    }

    @Test
    void jsonObjectArray_nullField() {
        WithJSONObjectArray b = ObjectMapper.shared().readValue("{\"arr\":null}", WithJSONObjectArray.class);
        assertNull(b.arr);
    }

    @Test
    void jsonObjectArray_supplierAppliedToElements() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        WithJSONObjectArray b = mapper.readValue(
                "{\"arr\":[{\"a\":1}]}", WithJSONObjectArray.class);
        assertEquals(1, b.arr.length);
        java.lang.reflect.Field inner = JSONObject.class.getDeclaredField("innerMap");
        inner.setAccessible(true);
        assertInstanceOf(ConcurrentHashMap.class, inner.get(b.arr[0]));
    }

    @Test
    void primitiveIntArray_stillWorksUnchanged() {
        // Pre-existing path: int[] has its own dedicated TAG_INT_ARRAY (or
        // similar) reader. List → primitive array coercion through
        // convertValue is the new fallback path; this case ensures the
        // primary fast path is intact.
        WithIntArray b = ObjectMapper.shared().readValue("{\"arr\":[1,2,3]}", WithIntArray.class);
        assertNotNull(b.arr);
        assertArrayEquals(new int[]{1, 2, 3}, b.arr);
    }

    @Test
    void stringArray_stillWorksUnchanged() {
        // Pre-existing path: String[] has TAG_STRING_ARRAY. Pin the regression.
        WithStringArray b = ObjectMapper.shared().readValue(
                "{\"arr\":[\"a\",\"b\",\"c\"]}", WithStringArray.class);
        assertNotNull(b.arr);
        assertArrayEquals(new String[]{"a", "b", "c"}, b.arr);
    }

    public record RecordWithJsonNodeArrays(JSONObject[] objs, JSONArray[] arrs) {}

    @Test
    void recordWithJsonNodeArrays_populated() {
        RecordWithJsonNodeArrays r = ObjectMapper.shared().readValue(
                "{\"objs\":[{\"a\":1}],\"arrs\":[[1,2]]}", RecordWithJsonNodeArrays.class);
        assertNotNull(r.objs());
        assertEquals(1, r.objs().length);
        assertEquals(1, r.objs()[0].get("a"));
        assertNotNull(r.arrs());
        assertEquals(1, r.arrs().length);
        assertEquals(2, r.arrs()[0].size());
    }

    public static class WithIntegerObjArray { public Integer[] arr; }

    @Test
    void integerArray_numericList_coerced() {
        // List<Number> elements (e.g. Integer from readAny) coerce to Integer[].
        WithIntegerObjArray b = ObjectMapper.shared().readValue("{\"arr\":[1,2,3]}", WithIntegerObjArray.class);
        assertNotNull(b.arr);
        assertEquals(3, b.arr.length);
        assertEquals(Integer.valueOf(1), b.arr[0]);
        assertEquals(Integer.valueOf(3), b.arr[2]);
    }

    public static class WithBooleanArr { public Boolean[] arr; }
    public static class WithBooleanPrimArr { public boolean[] arr; }
    public static class WithCharArr { public Character[] arr; }
    public static class WithCharPrimArr { public char[] arr; }

    @Test
    void booleanWrapperArray_coerced() {
        WithBooleanArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[true,false,null]}", WithBooleanArr.class);
        assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.FALSE, null}, b.arr);
    }

    @Test
    void booleanPrimitiveArray_coerced() {
        WithBooleanPrimArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[true,false,true]}", WithBooleanPrimArr.class);
        assertArrayEquals(new boolean[]{true, false, true}, b.arr);
    }

    @Test
    void characterArray_singleCharStrings_coerced() {
        WithCharArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[\"a\",\"b\",\"c\"]}", WithCharArr.class);
        assertArrayEquals(new Character[]{'a', 'b', 'c'}, b.arr);
    }

    @Test
    void charPrimitiveArray_singleCharStrings_coerced() {
        WithCharPrimArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[\"a\",\"b\"]}", WithCharPrimArr.class);
        assertArrayEquals(new char[]{'a', 'b'}, b.arr);
    }

    public enum Color { RED, GREEN, BLUE }
    public static class WithEnumArr { public Color[] arr; }

    @Test
    void enumArray_stringNames_coerced() {
        WithEnumArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[\"RED\",\"BLUE\"]}", WithEnumArr.class);
        assertArrayEquals(new Color[]{Color.RED, Color.BLUE}, b.arr);
    }

    @Test
    void enumArray_unknownConstant_leftNull() {
        WithEnumArr b = ObjectMapper.shared().readValue(
                "{\"arr\":[\"RED\",\"NOT_A_COLOR\"]}", WithEnumArr.class);
        assertEquals(2, b.arr.length);
        assertEquals(Color.RED, b.arr[0]);
        assertNull(b.arr[1]);
    }

    public static class WithNestedJsonObjectArr { public JSONObject[][] matrix; }
    public static class WithNestedIntArr { public int[][] matrix; }

    @Test
    void nestedJsonObjectArray_recursivelyMaterialised() {
        WithNestedJsonObjectArr b = ObjectMapper.shared().readValue(
                "{\"matrix\":[[{\"a\":1}],[{\"b\":2},{\"c\":3}]]}", WithNestedJsonObjectArr.class);
        assertNotNull(b.matrix);
        assertEquals(2, b.matrix.length);
        assertEquals(1, b.matrix[0].length);
        assertEquals(1, b.matrix[0][0].get("a"));
        assertEquals(2, b.matrix[1].length);
        assertEquals(3, b.matrix[1][1].get("c"));
    }

    @Test
    void nestedIntArray_recursivelyMaterialised() {
        WithNestedIntArr b = ObjectMapper.shared().readValue(
                "{\"matrix\":[[1,2],[3,4,5]]}", WithNestedIntArr.class);
        assertNotNull(b.matrix);
        assertEquals(2, b.matrix.length);
        assertArrayEquals(new int[]{1, 2}, b.matrix[0]);
        assertArrayEquals(new int[]{3, 4, 5}, b.matrix[1]);
    }
}
