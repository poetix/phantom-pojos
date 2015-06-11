package com.codepoetics.phantompojo;

import com.codepoetics.phantompojo.impl.ReflectionUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PojoPromoter {

    private PojoPromoter() {
    }

    static Object promote(Type type, Object object) {
        Class<?> rawType = ReflectionUtils.rawTypeOf(type);
        if (object instanceof Map && PhantomPojo.class.isAssignableFrom(rawType)) {
            return PhantomPojo.wrapping((Map<String, Object>) object).with((Class) rawType);
        }

        if (rawType.isPrimitive()) {
            return object;
        }

        if (object.getClass().isArray()) {
            return promoteStream(Stream.of(ReflectionUtils.toObjectArray(object)), rawType, ReflectionUtils.getFirstTypeArgument(type));
        }

        if (object instanceof Collection) {
            return promoteStream(((Collection<Object>) object).stream(), rawType, ReflectionUtils.getFirstTypeArgument(type));
        }

        if (!rawType.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException(String.format("Cannot convert %s into %s", object, type));
        }

        return object;
    }

    static Object promoteStream(Stream<Object> stream, Class<?> collectionType, Type itemType) {
        Stream<Object> promotedStream = stream.map(item -> promote(itemType, item));
        if (collectionType.equals(List.class)) {
            return promotedStream.collect(Collectors.toList());
        }
        if (collectionType.equals(Set.class)) {
            return promotedStream.collect(Collectors.toSet());
        }

        throw new IllegalArgumentException("Unable to convert collection to type " + collectionType.getSimpleName());
    }
}
