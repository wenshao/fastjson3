package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Android compatibility tests for basic JSON parsing and serialization.
 * These tests run on JVM but simulate Android environment by setting vm.name property.
 */
public class BasicParseTest {
    private String originalVmName;

    @BeforeEach
    public void setUp() {
        // Simulate Android environment
        originalVmName = System.getProperty("java.vm.name");
        System.setProperty("java.vm.name", "Dalvik");
    }

    @AfterEach
    public void tearDown() {
        // Restore original vm.name
        if (originalVmName != null) {
            System.setProperty("java.vm.name", originalVmName);
        } else {
            System.clearProperty("java.vm.name");
        }
    }

    @Test
    public void testParseObject() {
        String json = "{\"name\":\"test\",\"value\":123}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("test", obj.getString("name"));
        assertEquals(123, obj.getIntValue("value"));
    }

    @Test
    public void testParseArray() {
        String json = "[1,2,3,4,5]";
        JSONArray array = JSON.parseArray(json);
        assertEquals(5, array.size());
        assertEquals(1, array.getIntValue(0));
        assertEquals(5, array.getIntValue(4));
    }

    @Test
    public void testParseBean() {
        String json = "{\"id\":1,\"name\":\"fastjson3\"}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals(1, bean.id);
        assertEquals("fastjson3", bean.name);
    }

    @Test
    public void testToJSONString() {
        Bean bean = new Bean();
        bean.id = 42;
        bean.name = "test";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"id\":42"));
        assertTrue(json.contains("\"name\":\"test\""));
    }

    @Test
    public void testParseNestedObject() {
        String json = "{\"user\":{\"id\":1,\"name\":\"Alice\"},\"status\":\"active\"}";
        JSONObject obj = JSON.parseObject(json);
        JSONObject user = obj.getJSONObject("user");
        assertNotNull(user);
        assertEquals(1, user.getIntValue("id"));
        assertEquals("Alice", user.getString("name"));
        assertEquals("active", obj.getString("status"));
    }

    @Test
    public void testParseArrayWithObjects() {
        String json = "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]";
        List<Bean> beans = JSON.parseArray(json, Bean.class);
        assertEquals(2, beans.size());
        assertEquals(1, beans.get(0).id);
        assertEquals("A", beans.get(0).name);
        assertEquals(2, beans.get(1).id);
        assertEquals("B", beans.get(1).name);
    }

    @Test
    public void testJSONPath() {
        String json = "{\"store\":{\"book\":[{\"category\":\"reference\",\"author\":\"Nigel Rees\"}]}}";
        JSONObject obj = JSON.parseObject(json);
        String author = JSONPath.eval(obj, "$.store.book[0].author", String.class);
        assertEquals("Nigel Rees", author);
    }

    @Test
    public void testNullHandling() {
        String json = "{\"value\":null}";
        JSONObject obj = JSON.parseObject(json);
        assertTrue(obj.containsKey("value"));
        assertNull(obj.get("value"));
    }

    @Test
    public void testEscapeCharacters() {
        String json = "{\"message\":\"Hello\\nWorld\\t!\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("Hello\nWorld\t!", obj.getString("message"));
    }

    @Test
    public void testUnicode() {
        String json = "{\"text\":\"\\u4e2d\\u6587\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("中文", obj.getString("text"));
    }

    public static class Bean {
        public int id;
        public String name;

        public Bean() {
        }

        public Bean(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bean bean = (Bean) o;
            return id == bean.id && name.equals(bean.name);
        }

        @Override
        public int hashCode() {
            return 31 * id + (name != null ? name.hashCode() : 0);
        }
    }
}
