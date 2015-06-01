package com.codepoetics.phantompojo.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class PojoProperties {

    public static PojoProperties forGetters(Map<String, Method> getterMethodsByName) {
        String[] names = getterMethodsByName.keySet().stream().toArray(String[]::new);
        Type[] types = getFieldTypes(getterMethodsByName, names);

        return new PojoProperties(names, types);
    }

    private static Type[] getFieldTypes(Map<String, Method> getterMethods, String[] names) {
        return Stream.of(names).map(getterMethods::get).map(Method::getGenericReturnType).toArray(Type[]::new);
    }

    private final String[] names;
    private final Type[] types;

    private PojoProperties(String[] names, Type[] types) {
        this.names = names;
        this.types = types;
    }

    public String formatValues(Object[] values) {
        return IntStream.range(0, names.length)
                        .filter(i -> values[i] != null)
                        .mapToObj(i -> String.format("%s=%s", names[i], values[i]))
                        .collect(Collectors.joining(",", "{", "}"));
    }

    public MethodIndexLookup createMethodIndexLookup(MethodSet methodSet) {
        return methodSet.getMethodIndexLookup(getNameIndices());
    }

    private Map<String, Integer> getNameIndices() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], i);
        }
        return map;
    }

    public Map<String, Object> createMap(Object[] values) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return map;
    }

    public Object[] createEmptyValues() {
        return new Object[names.length];
    }

    public Object[] createValues(Map<String, Object> map, BiFunction<Type, Object, Object> typeConverter) {
        Object[] values = createEmptyValues();
        for (int i = 0; i < values.length; i++) {
            Object value = map.get(names[i]);
            if (value != null) {
                values[i] = typeConverter.apply(types[i], value);
            }
        }
        return values;
    }

}
