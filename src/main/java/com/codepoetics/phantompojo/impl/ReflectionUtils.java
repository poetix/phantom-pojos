package com.codepoetics.phantompojo.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static Class<?> rawTypeOf(Type type) {
        return (Class<?>) (type instanceof ParameterizedType
                ? ((ParameterizedType) type).getRawType()
                : type);
    }

    public static Type getFirstTypeArgument(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private static final ConcurrentMap<Class<?>, MethodHandles.Lookup> lookups = new ConcurrentHashMap<>();

    private static MethodHandles.Lookup forDeclaringClass(Class<?> declaringClass) {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup()
                .in(declaringClass);

        try {
            final Field f = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            final int modifiers = f.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, modifiers & ~Modifier.FINAL);
                f.setAccessible(true);
                f.set(lookup, MethodHandles.Lookup.PRIVATE);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return lookup;
    }

    public static Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        final MethodHandles.Lookup lookup = lookups.computeIfAbsent(declaringClass, ReflectionUtils::forDeclaringClass);

        return lookup
                .unreflectSpecial(method, declaringClass)
                .bindTo(proxy)
                .invokeWithArguments(args);
    }

    public static Object[] toObjectArray(Object arg) {
        if (arg instanceof Object[]) {
            return (Object[]) arg;
        }
        Object[] result = new Object[Array.getLength(arg)];
        for (int i = 0; i < Array.getLength(arg); i++) {
            result[i] = Array.get(arg, i);
        }
        return result;
    }
}
