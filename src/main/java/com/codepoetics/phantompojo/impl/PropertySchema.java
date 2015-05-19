package com.codepoetics.phantompojo.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

public final class PropertySchema {

    public static PropertySchema forPhantomClass(Class<?> phantomClass, MethodSet methodSet) {
        PojoProperties pojoProperties = methodSet.getPojoProperties();
        MethodIndexLookup methodIndexLookup = pojoProperties.createMethodIndexLookup(methodSet);

        return new PropertySchema(phantomClass.getSimpleName(), pojoProperties, methodIndexLookup);
    }

    private final String phantomName;
    private final PojoProperties pojoProperties;
    private final MethodIndexLookup methodIndexLookup;

    public PropertySchema(String phantomName, PojoProperties pojoProperties, MethodIndexLookup methodIndexLookup) {
        this.phantomName = phantomName;
        this.pojoProperties = pojoProperties;
        this.methodIndexLookup = methodIndexLookup;
    }

    public int getReadIndex(Method method) {
        return methodIndexLookup.getReadIndex(method);
    }

    public int getWriteIndex(Method method) {
        return methodIndexLookup.getWriteIndex(method);
    }

    public String formatValues(Object[] values) {
        return phantomName + " " + pojoProperties.formatValues(values);
    }

    public PropertyStore createStore() {
        return new PropertyStore(pojoProperties.createEmptyValues(), this);
    }

    public PropertyStore createStoreFromMap(Map<String, Object> map, BiFunction<Type, Object, Object> typeConverter) {
        Object[] values = pojoProperties.createValues(map, typeConverter);
        return new PropertyStore(values, this);
    }

    public Map<String, Object> createMap(Object[] values) {
        return pojoProperties.createMap(values);
    }

    @Override
    public int hashCode() {
        return pojoProperties.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PropertySchema)) {
            return false;
        }
        return pojoProperties.equals(((PropertySchema) other).pojoProperties);
    }
}
