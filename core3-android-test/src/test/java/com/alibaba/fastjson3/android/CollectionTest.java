package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for collection types - List, Map, Set, etc.
 */
public class CollectionTest {

    @Test
    public void testList() {
        String json = "[\"apple\",\"banana\",\"cherry\"]";
        List<String> list = JSON.parseArray(json, String.class);
        assertEquals(3, list.size());
        assertEquals("apple", list.get(0));
        assertEquals("banana", list.get(1));
        assertEquals("cherry", list.get(2));
    }

    @Test
    public void testListOfIntegers() {
        String json = "[1,2,3,4,5]";
        List<Integer> list = JSON.parseArray(json, Integer.class);
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test
    public void testEmptyList() {
        String json = "[]";
        List<String> list = JSON.parseArray(json, String.class);
        assertEquals(0, list.size());
    }

    @Test
    public void testListInBean() {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        ListBean bean = JSON.parseObject(json, ListBean.class);
        assertEquals(3, bean.items.size());
        assertEquals("a", bean.items.get(0));
    }

    @Test
    public void testMap() {
        String json = "{\"name\":\"Alice\",\"age\":30,\"city\":\"Beijing\"}";
        Map<String, Object> map = JSON.parseObject(json);
        assertEquals(3, map.size());
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals("Beijing", map.get("city"));
    }

    @Test
    public void testMapInBean() {
        String json = "{\"attributes\":{\"key1\":\"value1\",\"key2\":\"value2\"}}";
        MapBean bean = JSON.parseObject(json, MapBean.class);
        assertEquals(2, bean.attributes.size());
        assertEquals("value1", bean.attributes.get("key1"));
    }

    @Test
    public void testNestedList() {
        String json = "[[1,2],[3,4],[5,6]]";
        JSONArray nested = JSON.parseArray(json);
        assertEquals(3, nested.size());
        assertEquals(2, ((JSONArray) nested.get(0)).size());
    }

    @Test
    public void testNestedStructure() {
        String json = "{\"users\":[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]}";
        JSONObject obj = JSON.parseObject(json);
        JSONArray users = obj.getJSONArray("users");
        assertEquals(2, users.size());
        JSONObject userObj = users.getJSONObject(0);
        assertEquals("A", userObj.getString("name"));
        assertEquals(1, userObj.getIntValue("age"));
    }

    @Test
    public void testJSONArray() {
        JSONArray array = JSON.parseArray("[1,2,3]");
        assertEquals(3, array.size());
        assertEquals(1, array.getIntValue(0));
        assertEquals(2, array.getIntValue(1));
        assertEquals(3, array.getIntValue(2));
    }

    @Test
    public void testJSONObject() {
        JSONObject obj = JSON.parseObject("{\"a\":1,\"b\":2}");
        assertEquals(2, obj.size());
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    @Test
    public void testMixedArray() {
        String json = "[1,\"text\",true,null]";
        JSONArray array = JSON.parseArray(json);
        assertEquals(4, array.size());
        assertEquals(1, array.get(0));
        assertEquals("text", array.get(1));
        assertEquals(true, array.get(2));
        assertEquals(null, array.get(3));
    }

    @Test
    public void testNullCollection() {
        String json = "{\"items\":null}";
        ListBean bean = JSON.parseObject(json, ListBean.class);
        assertEquals(null, bean.items);
    }

    static class ListBean {
        public List<String> items;
    }

    static class MapBean {
        public Map<String, String> attributes;
    }

    static class NestedBean {
        public List<User> users;
    }

    static class User {
        public String name;
        public int age;
    }
}
