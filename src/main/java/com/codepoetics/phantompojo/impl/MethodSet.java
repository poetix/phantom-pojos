package com.codepoetics.phantompojo.impl;

import com.codepoetics.navn.Name;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MethodSet {

    public static MethodSet forClasses(Class<?> phantomClass, Class<?> builderClass) {
        return new MethodSet(gettersByName(phantomClass), buildersByName(builderClass));
    }

    private static Map<String, Method> gettersByName(Class<?> phantomClass) {
        return getBuilderAndGetterMethods(phantomClass)
                .collect(Collectors.toMap(
                        MethodSet::getFieldName,
                        Function.identity()));
    }

    private static Stream<Method> getBuilderAndGetterMethods(Class<?> klass) {
        return Stream.of(klass.getDeclaredMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()) && !m.isDefault());
    }

    private static Map<String, List<Method>> buildersByName(Class<?> builderClass) {
        return getBuilderAndGetterMethods(builderClass)
                .collect(Collectors.groupingBy(MethodSet::getFieldName));
    }

    private static String getFieldName(Method method) {
        return Name.of(method.getName()).withoutFirst().toCamelCase();
    }

    private final Map<String, Method> getterMethodsByName;
    private final Map<String, List<Method>> builderMethodsByName;

    private MethodSet(Map<String, Method> getterMethodsByName, Map<String, List<Method>> builderMethodsByName) {
        this.getterMethodsByName = getterMethodsByName;
        this.builderMethodsByName = builderMethodsByName;
    }

    public void verifyMatchingFieldNames() {
        Set<String> getterFieldNames = getterMethodsByName.keySet();
        Set<String> builderFieldNames = builderMethodsByName.keySet();

        if (!getterFieldNames.equals(builderFieldNames)) {
            throw new IllegalArgumentException(
                    String.format("Mismatch between pojo fields %s and builder fields %s",
                            getterFieldNames, builderFieldNames));
        }
    }

    public PojoProperties getPojoProperties() {
        return PojoProperties.forGetters(getterMethodsByName);
    }

    public MethodIndexLookup getMethodIndexLookup(Map<String, Integer> nameIndices) {
        return MethodIndexLookup.create(getReadIndices(nameIndices), getWriteIndices(nameIndices));
    }

    private Map<Method, Integer> getWriteIndices(Map<String, Integer> nameIndices) {
        return builderMethodsByName.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(m -> new AbstractMap.SimpleEntry<>(e.getKey(), m)))
                .collect(Collectors.toMap(Map.Entry::getValue, e -> nameIndices.get(e.getKey())));
    }

    private Map<Method, Integer> getReadIndices(Map<String, Integer> nameIndices) {
        return getterMethodsByName.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> nameIndices.get(e.getKey())));
    }
}
