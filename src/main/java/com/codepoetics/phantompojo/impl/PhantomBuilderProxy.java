package com.codepoetics.phantompojo.impl;

import com.codepoetics.phantompojo.PhantomPojo;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PhantomBuilderProxy<P extends PhantomPojo<B>, B extends Supplier<P>> implements DispatchingInvocationHandler, Supplier<P> {

    private final PhantomBuilderClassPair<P, B> classPair;
    private final PropertyStore store;

    PhantomBuilderProxy(PhantomBuilderClassPair<P, B> classPair, PropertyStore store) {
        this.classPair = classPair;
        this.store = store;
    }

    @Override
    public Object invokeMissing(Object proxy, Method method, Object[] args) {
        Type targetType = store.getTargetType(method);
        store.write(method, reify(args[0], targetType));
        return proxy;
    }

    private Object reify(Object arg, Type targetType) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof Collection) {
            return reifyStream(targetType, ((Collection<Object>) arg).stream());
        }

        if (arg.getClass().isArray()) {
            return reifyStream(targetType, Stream.of(ReflectionUtils.toObjectArray(arg)));
        }

        return arg instanceof Supplier
                ? ((Supplier<?>) arg).get()
                : arg;
    }

    private Object reifyStream(Type targetType, Stream<Object> argStream) {
        Class<?> rawType = ReflectionUtils.rawTypeOf(targetType);
        Type itemType = ReflectionUtils.getFirstTypeArgument(targetType);

        Stream<Object> reifiedStream = argStream.map(a -> reify(a, itemType));

        if (rawType.equals(List.class)) {
            return reifiedStream.collect(Collectors.toList());
        }

        if (rawType.equals(Set.class)) {
            return reifiedStream.collect(Collectors.toSet());
        }

        throw new IllegalArgumentException("Unable to convert collection to " + rawType.getSimpleName());
    }

    @Override
    public P get() {
        return classPair.createPhantom(store.copy());
    }
}
