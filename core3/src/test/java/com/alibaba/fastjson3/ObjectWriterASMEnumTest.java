package com.alibaba.fastjson3;

import com.alibaba.fastjson3.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the ASM-writer TYPE_ENUM case. The generator
 * precomputes a {@code "fieldName":"ENUM_VALUE",} byte[] per ordinal and
 * emits bytecode that writes the blob in one System.arraycopy; null enum
 * fields must be omitted (matching the TYPE_STRING / TYPE_LIST_POJO null
 * semantics in the same generator).
 */
public class ObjectWriterASMEnumTest {
    public enum Color { RED, GREEN, BLUE }

    public static class Bean {
        public int id;
        public Color color;
        public String name;
    }

    @Test
    public void enumValueIsEmitted() {
        Bean bean = new Bean();
        bean.id = 7;
        bean.color = Color.GREEN;
        bean.name = "x";

        ObjectWriter<Bean> writer = ObjectWriterCreatorASM.createObjectWriter(Bean.class);
        try (JSONGenerator gen = JSONGenerator.of()) {
            writer.write(gen, bean, null, null, 0);
            String json = gen.toString();
            assertTrue(json.contains("\"color\":\"GREEN\""), "should emit color: " + json);
        }
    }

    @Test
    public void nullEnumFieldIsOmitted() {
        Bean bean = new Bean();
        bean.id = 7;
        bean.color = null;
        bean.name = "x";

        ObjectWriter<Bean> writer = ObjectWriterCreatorASM.createObjectWriter(Bean.class);
        try (JSONGenerator gen = JSONGenerator.of()) {
            writer.write(gen, bean, null, null, 0);
            String json = gen.toString();
            assertFalse(json.contains("\"color\""), "null color must be omitted: " + json);
        }
    }

    @Test
    public void asmEnumMatchesReflectionOutput() {
        Bean bean = new Bean();
        bean.id = 42;
        bean.color = Color.BLUE;
        bean.name = "hello";

        String reflectJson = JSON.toJSONString(bean);

        ObjectWriter<Bean> writer = ObjectWriterCreatorASM.createObjectWriter(Bean.class);
        try (JSONGenerator gen = JSONGenerator.of()) {
            writer.write(gen, bean, null, null, 0);
            String asmJson = gen.toString();
            assertEquals(reflectJson, asmJson, "ASM vs reflect output must match");
        }
    }

    @Test
    public void eachOrdinalRendersCorrectly() {
        ObjectWriter<Bean> writer = ObjectWriterCreatorASM.createObjectWriter(Bean.class);
        for (Color c : Color.values()) {
            Bean bean = new Bean();
            bean.color = c;
            try (JSONGenerator gen = JSONGenerator.of()) {
                writer.write(gen, bean, null, null, 0);
                String json = gen.toString();
                assertTrue(json.contains("\"color\":\"" + c.name() + "\""),
                        "ordinal " + c.ordinal() + " render: " + json);
            }
        }
    }
}
