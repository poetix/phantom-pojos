package com.codepoetics.phantompojo;

import com.codepoetics.phantompojo.impl.PhantomBuilderClassPair;
import com.codepoetics.phantompojo.impl.PropertySchema;
import com.codepoetics.phantompojo.impl.PropertyStore;
import com.codepoetics.phantompojo.impl.ReflectionUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface PhantomPojo<B extends Supplier<? extends PhantomPojo<?>>> {

    interface PropertiesCapture {
        <P extends PhantomPojo<B>, B extends Supplier<P>> P with(Class<? extends P> klass);
    }

    static PropertiesCapture wrapping(Map<String, Object> properties) {
        return new PropertiesCapture() {
            @Override
            public <P extends PhantomPojo<B>, B extends Supplier<P>> P with(Class<? extends P> klass) {
                PhantomBuilderClassPair<P, B> classPair = PhantomBuilderClassPair.forPhantomClass(klass);
                PropertySchema schema = classPair.getPropertySchema();
                PropertyStore store = schema.createStoreFromMap(properties, PhantomPojo::promote);

                return classPair.createPhantom(store);
            }
        };
    }

    static Object promote(Type type, Object object) {
        Class<?> rawType = ReflectionUtils.rawTypeOf(type);
        if (object instanceof Map && PhantomPojo.class.isAssignableFrom(rawType)) {
            return PhantomPojo.wrapping((Map<String, Object>) object).with((Class) rawType);
        }

        if (rawType.isPrimitive()) {
            return object;
        }

        if (!rawType.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException(String.format("Cannot convert %s into %s", object, type));
        }

        if (object instanceof List) {
            return ((List<Object>) object).stream().map(item -> promote(ReflectionUtils.getFirstTypeArgument(type), item)).collect(Collectors.toList());
        }

        return object;
    }

    B update();
    Map<String, Object> properties();
}
