package com.alibaba.fastjson3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a JSON array.
 *
 * <pre>
 * JSONArray arr = new JSONArray();
 * arr.add("item");
 * String s = arr.getString(0);
 * </pre>
 *
 * <p>Custom backing list per-instance via the {@link
 * #JSONArray(Supplier)} constructor; the parser threads a configured
 * {@link ObjectMapper.Builder#listSupplier(Supplier) listSupplier}
 * here when one is set on the parsing mapper.</p>
 */
public class JSONArray extends ArrayList<Object> {
    /**
     * Inner list — null when using ArrayList (super) storage. When
     * non-null, every {@code List} method delegates to it; the inherited
     * ArrayList stays empty so memory cost is just the unused header.
     */
    private transient List<Object> innerList;

    public JSONArray() {
    }

    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONArray(Collection<?> c) {
        super(c);
    }

    /**
     * Create a JSONArray backed by the given {@code Supplier}'s List.
     * Used by the parser when a per-instance list supplier is configured
     * via {@link ObjectMapper.Builder#listSupplier(Supplier)}.
     */
    public JSONArray(Supplier<? extends List<Object>> supplier) {
        innerList = supplier.get();
    }

    @Override
    public int size() {
        return innerList != null ? innerList.size() : super.size();
    }

    @Override
    public boolean isEmpty() {
        return innerList != null ? innerList.isEmpty() : super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return innerList != null ? innerList.contains(o) : super.contains(o);
    }

    @Override
    public java.util.Iterator<Object> iterator() {
        return innerList != null ? innerList.iterator() : super.iterator();
    }

    @Override
    public Object[] toArray() {
        return innerList != null ? innerList.toArray() : super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return innerList != null ? innerList.toArray(a) : super.toArray(a);
    }

    @Override
    public boolean add(Object e) {
        return innerList != null ? innerList.add(e) : super.add(e);
    }

    @Override
    public void add(int index, Object element) {
        if (innerList != null) {
            innerList.add(index, element);
        } else {
            super.add(index, element);
        }
    }

    @Override
    public boolean remove(Object o) {
        return innerList != null ? innerList.remove(o) : super.remove(o);
    }

    @Override
    public Object remove(int index) {
        return innerList != null ? innerList.remove(index) : super.remove(index);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        return innerList != null ? innerList.addAll(c) : super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        return innerList != null ? innerList.addAll(index, c) : super.addAll(index, c);
    }

    @Override
    public void clear() {
        if (innerList != null) {
            innerList.clear();
        } else {
            super.clear();
        }
    }

    @Override
    public Object get(int index) {
        return innerList != null ? innerList.get(index) : super.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return innerList != null ? innerList.set(index, element) : super.set(index, element);
    }

    @Override
    public int indexOf(Object o) {
        return innerList != null ? innerList.indexOf(o) : super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return innerList != null ? innerList.lastIndexOf(o) : super.lastIndexOf(o);
    }

    @Override
    public java.util.ListIterator<Object> listIterator() {
        return innerList != null ? innerList.listIterator() : super.listIterator();
    }

    @Override
    public java.util.ListIterator<Object> listIterator(int index) {
        return innerList != null ? innerList.listIterator(index) : super.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return innerList != null ? innerList.subList(fromIndex, toIndex) : super.subList(fromIndex, toIndex);
    }

    /**
     * Fluent add — returns this for chaining.
     */
    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    public String getString(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return (String) val;
        }
        return val.toString();
    }

    public Integer getInteger(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Integer.parseInt(str);
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to Integer: " + val);
    }

    public int getIntValue(int index) {
        Integer val = getInteger(index);
        return val == null ? 0 : val;
    }

    public Long getLong(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Long.parseLong(str);
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to Long: " + val);
    }

    public long getLongValue(int index) {
        Long val = getLong(index);
        return val == null ? 0L : val;
    }

    public Boolean getBoolean(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            return "true".equalsIgnoreCase((String) val);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to Boolean: " + val);
    }

    public boolean getBooleanValue(int index) {
        Boolean val = getBoolean(index);
        return val != null && val;
    }

    public BigDecimal getBigDecimal(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        if (val instanceof String s) {
            JSONParser.checkBigNumberMagnitude(s);
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                throw new JSONException("parse BigDecimal error at JSONArray[" + index + "]: " + s, e);
            }
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to BigDecimal: " + val);
    }

    public BigInteger getBigInteger(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }
        if (val instanceof Number) {
            return BigInteger.valueOf(((Number) val).longValue());
        }
        if (val instanceof String s) {
            JSONParser.checkBigNumberMagnitude(s);
            try {
                return new BigInteger(s);
            } catch (NumberFormatException e) {
                throw new JSONException("parse BigInteger error at JSONArray[" + index + "]: " + s, e);
            }
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to BigInteger: " + val);
    }

    public Double getDouble(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Double) {
            return (Double) val;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            return Double.parseDouble((String) val);
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to Double: " + val);
    }

    public double getDoubleValue(int index) {
        Double val = getDouble(index);
        return val == null ? 0D : val;
    }

    public Float getFloat(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Float) {
            return (Float) val;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            return Float.parseFloat((String) val);
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to Float: " + val);
    }

    public float getFloatValue(int index) {
        Float val = getFloat(index);
        return val == null ? 0F : val;
    }

    public JSONObject getJSONObject(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            return (JSONObject) val;
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to JSONObject: " + val);
    }

    public JSONArray getJSONArray(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONArray) {
            return (JSONArray) val;
        }
        throw new JSONException("Cannot cast JSONArray[" + index + "] from "
            + val.getClass().getSimpleName() + " to JSONArray: " + val);
    }

    /**
     * Convert to typed list.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> toJavaList(Class<T> clazz) {
        return ObjectMapper.shared().convertValue(this, new TypeReference<List<T>>() {});
    }

    /**
     * Serialize to JSON string with features.
     */
    public String toJSONString(WriteFeature... features) {
        return JSON.toJSONString(this, features);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
