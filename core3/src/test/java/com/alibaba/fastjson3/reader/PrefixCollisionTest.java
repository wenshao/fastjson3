package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM.FieldJumpInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for prefix collision handling in DirectField optimization.
 * "name" and "namely" have the same 4-byte prefix: 0x226e616d
 */
public class PrefixCollisionTest {

    /**
     * Top-level class for testing prefix collision.
     * "name" and "namely" share the same 4-byte prefix.
     */
    public static class PrefixCollisionBean {
        public String name;
        public String namely;
        public int id;
    }

    @Test
    public void testPrefixCollisionDetection() {
        // Verify that name and namely have the same prefix
        int namePrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("name");
        int namelyPrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("namely");

        assertEquals(namePrefix, namelyPrefix,
                "name and namely should have the same 4-byte prefix");

        // Build field jump info
        FieldReader[] fieldReaders = new FieldReader[3];
        fieldReaders[0] = new FieldReader("name", null, String.class, String.class, 0, null, false, null, null);
        fieldReaders[1] = new FieldReader("namely", null, String.class, String.class, 1, null, false, null, null);
        fieldReaders[2] = new FieldReader("id", null, int.class, int.class, 2, null, false, null, null);

        FieldJumpInfo info = ObjectReaderCreatorASM.buildFieldJumpInfo(fieldReaders, 0, 0);

        // DirectField should be disabled due to prefix collision
        assertFalse(info.enabled,
                "DirectField should be disabled when prefix collision is detected");
        assertEquals(0, info.prefixes.length,
                "No prefix groups should be created when collision exists");
    }

    @Test
    public void testPrefixCollisionBeanParsing() {
        String json = "{\"name\":\"Alice\",\"namely\":\"test\",\"id\":123}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        PrefixCollisionBean bean = JSON.parseObject(bytes, PrefixCollisionBean.class);

        // Even with prefix collision, fields should be parsed correctly
        // because DirectField is disabled and reflection is used
        assertEquals(123, bean.id, "id field should be parsed correctly");
        assertEquals("Alice", bean.name, "name field should be parsed correctly");
        assertEquals("test", bean.namely, "namely field should be parsed correctly");
    }

    @Test
    public void testStringInputAlsoWorks() {
        // String input always uses reflection path
        String json = "{\"name\":\"Bob\",\"namely\":\"example\",\"id\":456}";
        PrefixCollisionBean bean = JSON.parseObject(json, PrefixCollisionBean.class);

        assertEquals(456, bean.id);
        assertEquals("Bob", bean.name);
        assertEquals("example", bean.namely);
    }
}
