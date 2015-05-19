package com.codepoetics.phantompojo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static Class<?> rawTypeOf(Type type) {
        return (Class<?>) (type instanceof ParameterizedType
                ? ((ParameterizedType) type).getRawType()
                : type);
    }

    public static <B> Class<? extends B> getFirstTypeArgument(Type type) {
        return (Class<? extends B>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }
}
