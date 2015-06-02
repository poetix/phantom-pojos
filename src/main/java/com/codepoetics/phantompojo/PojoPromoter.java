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

        if (object instanceof Collection) {
            return promoteCollection(type, (Collection<Object>) object, rawType);
        }

        if (!rawType.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException(String.format("Cannot convert %s into %s", object, type));
        }

        return object;
    }

    static Object promoteCollection(Type itemType, Collection<Object> object, Class<?> rawType) {
        Stream<Object> promotedStream = object.stream()
                .map(item -> promote(ReflectionUtils.getFirstTypeArgument(itemType), item));
        if (rawType.equals(List.class)) {
            return promotedStream.collect(Collectors.toList());
        }
        if (rawType.equals(Set.class)) {
            return promotedStream.collect(Collectors.toSet());
        }

        throw new IllegalArgumentException("Unable to convert collection to type " + rawType.getSimpleName());
    }
}
