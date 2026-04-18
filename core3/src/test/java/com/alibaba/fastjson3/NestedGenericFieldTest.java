package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises nested-generic and wildcard field types on ordinary POJOs,
 * Records, and classes that inherit parameterized superclasses.
 *
 * <p>Before the generic-resolution overhaul these fields parsed but their
 * element/value types were discarded — a {@code List<List<Bean>>} field
 * came back as {@code ArrayList<ArrayList<LinkedHashMap>>} and a
 * {@code Map<String, Bean>} field as {@code LinkedHashMap<String, LinkedHashMap>}.
 */
public class NestedGenericFieldTest {

    public static class Bean {
        public String name;
        public int value;

        public Bean() {}

        public Bean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    // ==================== POJO with nested collection fields ====================

    public static class Matrix {
        public List<List<Integer>> grid;
        public List<List<Bean>> beans;
    }

    @Test
    public void listOfListOfInt() {
        Matrix m = JSON.parseObject("{\"grid\":[[1,2],[3,4]]}", Matrix.class);
        assertEquals(List.of(1, 2), m.grid.get(0));
        assertEquals(List.of(3, 4), m.grid.get(1));
    }

    @Test
    public void listOfListOfBean() {
        Matrix m = JSON.parseObject(
                "{\"beans\":[[{\"name\":\"a\",\"value\":1}],[{\"name\":\"b\",\"value\":2}]]}",
                Matrix.class);
        assertInstanceOf(Bean.class, m.beans.get(0).get(0),
                "inner list elements must be Bean, not LinkedHashMap");
        assertEquals("a", m.beans.get(0).get(0).name);
        assertEquals(2, m.beans.get(1).get(0).value);
    }

    public static class MapOfListHolder {
        public Map<String, List<Bean>> teams;
    }

    @Test
    public void mapOfListOfBeanField() {
        MapOfListHolder h = JSON.parseObject(
                "{\"teams\":{\"red\":[{\"name\":\"a\",\"value\":1}],\"blue\":[]}}",
                MapOfListHolder.class);
        assertInstanceOf(Bean.class, h.teams.get("red").get(0),
                "Map<String, List<Bean>> field must resolve Bean, not LinkedHashMap");
        assertEquals("a", h.teams.get("red").get(0).name);
        assertTrue(h.teams.get("blue").isEmpty());
    }

    public static class NestedMapHolder {
        public Map<String, Map<String, Bean>> groups;
    }

    @Test
    public void nestedMapField() {
        NestedMapHolder h = JSON.parseObject(
                "{\"groups\":{\"g1\":{\"k1\":{\"name\":\"a\",\"value\":1}}}}",
                NestedMapHolder.class);
        assertInstanceOf(Bean.class, h.groups.get("g1").get("k1"));
        assertEquals("a", h.groups.get("g1").get("k1").name);
    }

    public static class ListOfMapHolder {
        public List<Map<String, Bean>> entries;
    }

    @Test
    public void listOfMapField() {
        ListOfMapHolder h = JSON.parseObject(
                "{\"entries\":[{\"a\":{\"name\":\"one\",\"value\":1}},{\"b\":{\"name\":\"two\",\"value\":2}}]}",
                ListOfMapHolder.class);
        assertInstanceOf(Bean.class, h.entries.get(0).get("a"));
        assertEquals("one", h.entries.get(0).get("a").name);
        assertEquals(2, h.entries.get(1).get("b").value);
    }

    // ==================== Map<String, Bean> at field level (previously broken) ====================

    public static class IndexedBeanHolder {
        public Map<String, Bean> byKey;
    }

    @Test
    public void mapOfBeanField() {
        IndexedBeanHolder h = JSON.parseObject(
                "{\"byKey\":{\"alice\":{\"name\":\"Alice\",\"value\":1}}}",
                IndexedBeanHolder.class);
        assertInstanceOf(Bean.class, h.byKey.get("alice"),
                "Map<String, Bean> field values must resolve to Bean");
        assertEquals("Alice", h.byKey.get("alice").name);
    }

    public static class IntKeyHolder {
        public Map<Integer, Bean> byId;
    }

    @Test
    public void mapIntegerKeyField() {
        IntKeyHolder h = JSON.parseObject(
                "{\"byId\":{\"1\":{\"name\":\"a\",\"value\":1},\"2\":{\"name\":\"b\",\"value\":2}}}",
                IntKeyHolder.class);
        assertEquals("a", h.byId.get(1).name);
        assertEquals("b", h.byId.get(2).name);
    }

    // ==================== Set<E> field support ====================

    public static class TagHolder {
        public Set<String> tags;
        public Set<Bean> beans;
    }

    @Test
    public void setOfStringField() {
        TagHolder h = JSON.parseObject("{\"tags\":[\"a\",\"b\",\"a\"]}", TagHolder.class);
        assertEquals(Set.of("a", "b"), h.tags);
    }

    @Test
    public void setOfBeanField() {
        TagHolder h = JSON.parseObject(
                "{\"beans\":[{\"name\":\"a\",\"value\":1}]}",
                TagHolder.class);
        assertEquals(1, h.beans.size());
        assertInstanceOf(Bean.class, h.beans.iterator().next());
    }

    // ==================== Inheritance: Child extends Parent<Bean> ====================

    public static class Parent<T> {
        public T value;
        public List<T> values;
        public Map<String, T> byName;
    }

    public static class Child extends Parent<Bean> {
        public int extra;
    }

    @Test
    public void inheritedTypeVariable() {
        Child c = JSON.parseObject(
                "{\"value\":{\"name\":\"v\",\"value\":9},\"extra\":42,"
                        + "\"values\":[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}],"
                        + "\"byName\":{\"k\":{\"name\":\"c\",\"value\":3}}}",
                Child.class);
        assertEquals(42, c.extra);
        assertInstanceOf(Bean.class, c.value,
                "T value in Parent<T> must resolve to Bean when read as Child extends Parent<Bean>");
        assertEquals("v", c.value.name);
        assertEquals(9, c.value.value);
        assertInstanceOf(Bean.class, c.values.get(0),
                "List<T> in Parent<T> must resolve T to Bean through inheritance");
        assertEquals("a", c.values.get(0).name);
        assertInstanceOf(Bean.class, c.byName.get("k"),
                "Map<String, T> in Parent<T> must resolve T to Bean through inheritance");
    }

    // Two-level inheritance: TypeVariable chain Parent<T> -> Middle<T> -> Leaf
    public static class Middle<T> extends Parent<T> {
    }

    public static class Leaf extends Middle<Bean> {
    }

    @Test
    public void twoLevelInheritedTypeVariable() {
        Leaf l = JSON.parseObject(
                "{\"value\":{\"name\":\"v\",\"value\":1},\"values\":[{\"name\":\"a\",\"value\":2}]}",
                Leaf.class);
        assertInstanceOf(Bean.class, l.value);
        assertEquals("v", l.value.name);
        assertInstanceOf(Bean.class, l.values.get(0));
    }

    // ==================== Wildcards ====================

    public static class WildcardHolder {
        public List<? extends Bean> beans;
        public List<?> anything;
    }

    @Test
    public void upperBoundedWildcardField() {
        WildcardHolder h = JSON.parseObject(
                "{\"beans\":[{\"name\":\"a\",\"value\":1}]}",
                WildcardHolder.class);
        // ? extends Bean normalizes to Bean as the element type.
        assertInstanceOf(Bean.class, h.beans.get(0));
        assertEquals("a", ((Bean) h.beans.get(0)).name);
    }

    @Test
    public void unboundedWildcardField() {
        WildcardHolder h = JSON.parseObject(
                "{\"anything\":[1,\"x\",true]}",
                WildcardHolder.class);
        assertEquals(3, h.anything.size());
    }

    // ==================== Records ====================

    public record BeanBox(String id, List<List<Bean>> grid) {}

    @Test
    public void recordWithNestedList() {
        BeanBox box = JSON.parseObject(
                "{\"id\":\"b1\",\"grid\":[[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}],[]]}",
                BeanBox.class);
        assertEquals("b1", box.id());
        assertInstanceOf(Bean.class, box.grid().get(0).get(0),
                "Record component List<List<Bean>> must resolve Bean");
        assertEquals("a", box.grid().get(0).get(0).name);
        assertTrue(box.grid().get(1).isEmpty());
    }

    public record MapBox(String id, Map<String, List<Bean>> groups) {}

    @Test
    public void recordWithMapOfList() {
        MapBox box = JSON.parseObject(
                "{\"id\":\"m1\",\"groups\":{\"team\":[{\"name\":\"a\",\"value\":1}]}}",
                MapBox.class);
        assertInstanceOf(Bean.class, box.groups().get("team").get(0));
    }

    // ==================== Null + empty ====================

    @Test
    public void nullNestedListField() {
        Matrix m = JSON.parseObject("{\"grid\":null}", Matrix.class);
        assertNull(m.grid);
    }

    @Test
    public void emptyNestedListField() {
        Matrix m = JSON.parseObject("{\"grid\":[]}", Matrix.class);
        assertTrue(m.grid.isEmpty());
    }

    @Test
    public void emptyInnerListField() {
        Matrix m = JSON.parseObject("{\"grid\":[[]]}", Matrix.class);
        assertEquals(1, m.grid.size());
        assertTrue(m.grid.get(0).isEmpty());
    }

    // ==================== Concrete collection types ====================

    public static class ConcreteTypes {
        public ArrayList<Bean> list;
        public HashMap<String, Bean> map;
        public LinkedHashMap<String, Bean> linkedMap;
        public LinkedHashSet<Bean> set;
    }

    @Test
    public void concreteCollectionTypes() {
        ConcreteTypes c = JSON.parseObject(
                "{\"list\":[{\"name\":\"a\",\"value\":1}],"
                        + "\"map\":{\"k\":{\"name\":\"a\",\"value\":1}},"
                        + "\"linkedMap\":{\"k\":{\"name\":\"b\",\"value\":2}},"
                        + "\"set\":[{\"name\":\"c\",\"value\":3}]}",
                ConcreteTypes.class);
        assertInstanceOf(Bean.class, c.list.get(0));
        assertInstanceOf(Bean.class, c.map.get("k"));
        assertInstanceOf(Bean.class, c.linkedMap.get("k"));
        assertInstanceOf(Bean.class, c.set.iterator().next());
    }

    // ==================== TypeReference<Parent<Bean>> / ParameterizedType top-level ====================

    public static class Box<T> {
        public String id;
        public T payload;
        public List<T> history;
    }

    @Test
    public void typeReferenceToParameterizedPojo() {
        Box<Bean> box = JSON.parseObject(
                "{\"id\":\"b1\",\"payload\":{\"name\":\"p\",\"value\":9},"
                        + "\"history\":[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]}",
                new TypeReference<Box<Bean>>() {});
        assertEquals("b1", box.id);
        assertInstanceOf(Bean.class, box.payload,
                "TypeReference<Box<Bean>> must resolve T at top level");
        assertEquals("p", box.payload.name);
        assertInstanceOf(Bean.class, box.history.get(0));
        assertEquals("a", box.history.get(0).name);
    }

    @Test
    public void listOfParameterizedPojo() {
        List<Box<Bean>> boxes = JSON.parseObject(
                "[{\"id\":\"b1\",\"payload\":{\"name\":\"a\",\"value\":1}},"
                        + "{\"id\":\"b2\",\"payload\":{\"name\":\"b\",\"value\":2}}]",
                new TypeReference<List<Box<Bean>>>() {});
        assertEquals(2, boxes.size());
        assertInstanceOf(Bean.class, boxes.get(0).payload,
                "each element must dispatch to the parameterized Box<Bean> reader");
        assertEquals("a", boxes.get(0).payload.name);
    }

    public record RecordBox<T>(String id, T payload) {}

    @Test
    public void typeReferenceToParameterizedRecord() {
        RecordBox<Bean> box = JSON.parseObject(
                "{\"id\":\"r1\",\"payload\":{\"name\":\"p\",\"value\":1}}",
                new TypeReference<RecordBox<Bean>>() {});
        assertEquals("r1", box.id());
        assertInstanceOf(Bean.class, box.payload());
    }

    // ==================== Setter-based generic fields ====================

    public static class SetterBean {
        private Map<String, Bean> data;

        public Map<String, Bean> getData() {
            return data;
        }

        public void setData(Map<String, Bean> data) {
            this.data = data;
        }
    }

    @Test
    public void genericSetterField() {
        SetterBean s = JSON.parseObject(
                "{\"data\":{\"k\":{\"name\":\"a\",\"value\":1}}}",
                SetterBean.class);
        assertInstanceOf(Bean.class, s.data.get("k"),
                "Setter parameter type Map<String, Bean> must resolve Bean");
    }
}
