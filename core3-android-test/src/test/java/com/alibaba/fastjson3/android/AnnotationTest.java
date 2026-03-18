package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.annotation.JSONField;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for annotation support - commonly used in Android apps.
 */
public class AnnotationTest {

    @Test
    public void testJSONFieldName() {
        Bean bean = new Bean();
        bean.userName = "Alice";
        String json = JSON.toJSONString(bean);
        // Default uses field name
        assertTrue(json.contains("\"userName\""));
    }

    @Test
    public void testJSONFieldWithAlternateName() {
        String json = "{\"user_name\":\"Bob\"}";
        AlternateNameBean bean = JSON.parseObject(json, AlternateNameBean.class);
        assertEquals("Bob", bean.userName);
    }

    @Test
    public void testJSONFieldSerializeFalse() {
        SecretBean bean = new SecretBean();
        bean.name = "Alice";
        bean.secret = "password123";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\""));
        assertFalse(json.contains("secret"));
    }

    @Test
    public void testJSONFieldDeserializeFalse() {
        String json = "{\"name\":\"Bob\",\"computed\":\"value123\"}";
        ComputedBean bean = JSON.parseObject(json, ComputedBean.class);
        assertEquals("Bob", bean.name);
        assertNull(bean.computed);
    }

    @Test
    public void testJSONFieldFormat() {
        FormattedBean bean = new FormattedBean();
        bean.price = 1234.56;
        String json = JSON.toJSONString(bean);
        assertNotNull(json);
    }

    @Test
    public void testMultipleAnnotations() {
        MultiAnnotationBean bean = new MultiAnnotationBean();
        bean.id = 1;
        bean.name = "test";
        bean.internalData = "secret";

        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"id\""));
        assertTrue(json.contains("\"name\""));
        assertFalse(json.contains("internalData"));
    }

    static class Bean {
        public String userName;
    }

    static class AlternateNameBean {
        @JSONField(name = "user_name")
        public String userName;
    }

    static class SecretBean {
        public String name;

        @JSONField(serialize = false)
        public String secret;
    }

    static class ComputedBean {
        public String name;

        @JSONField(deserialize = false)
        public String computed;
    }

    static class FormattedBean {
        @JSONField(format = "#0.00")
        public Double price;
    }

    static class MultiAnnotationBean {
        public int id;
        public String name;

        @JSONField(serialize = false, deserialize = false)
        public String internalData;
    }
}
