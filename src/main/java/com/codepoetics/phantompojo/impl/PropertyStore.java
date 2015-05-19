package com.codepoetics.phantompojo.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class PropertyStore {

    public PropertyStore(Object[] values, PropertySchema schema) {
        this.values = values;
        this.schema = schema;
    }

    private final Object[] values;
    private final PropertySchema schema;

    public Object read(Method method) {
        return values[schema.getReadIndex(method)];
    }

    public void write(Method method, Object value) {
        values[schema.getWriteIndex(method)] = value;
    }

    public PropertyStore copy() {
        return new PropertyStore(Arrays.copyOf(values, values.length), schema);
    }

    public Map<String, Object> toMap() {
        return schema.createMap(values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, schema);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PropertyStore)) {
            return false;
        }
        PropertyStore otherStore = (PropertyStore) other;
        return Arrays.deepEquals(values, otherStore.values)
                && Objects.equals(schema, otherStore.schema);
    }

    @Override
    public String toString() {
        return schema.formatValues(values);
    }
}
