package com.codepoetics.phantompojo.impl;

import com.codepoetics.phantompojo.*;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PhantomBuilderClassPair<P extends PhantomPojo<B>, B extends Supplier<P>> {

    private static final ConcurrentMap<Class<?>, PhantomBuilderClassPair<?, ?>> cache = new ConcurrentHashMap<>();

    public static <P extends PhantomPojo<B>, B extends Supplier<P>> PhantomBuilderClassPair<P, B> forPhantomClass(Class<? extends P> phantomClass) {
        return (PhantomBuilderClassPair) cache.computeIfAbsent(phantomClass, cls -> forPhantomClassUncached((Class) cls));
    }

    private static <P extends PhantomPojo<B>, B extends Supplier<P>> PhantomBuilderClassPair<P, B> forPhantomClassUncached(Class<? extends P> phantomClass) {
        Class<? extends B> builderClass = getBuilderClass(phantomClass);
        MethodSet methodSet = MethodSet.forClasses(phantomClass, builderClass);

        return new PhantomBuilderClassPair<>(phantomClass, builderClass, PropertySchema.forPhantomClass(phantomClass, methodSet));
    }

    private static <B extends Supplier<T>, T extends PhantomPojo<B>> Class<? extends B> getBuilderClass(Class<? extends T> targetClass) {
        return Stream.of(targetClass.getGenericInterfaces())
                .filter(t -> PhantomPojo.class.equals(ReflectionUtils.rawTypeOf(t)))
                .map(ReflectionUtils::<B>getFirstTypeArgument)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot infer builder class from class " + targetClass));
    }

    private final Class<? extends P> phantomClass;
    private final Class<? extends B> builderClass;
    private final PropertySchema schema;

    private PhantomBuilderClassPair(Class<? extends P> phantomClass, Class<? extends B> builderClass, PropertySchema schema) {
        this.phantomClass = phantomClass;
        this.builderClass = builderClass;
        this.schema = schema;
    }

    public PropertySchema getPropertySchema() {
        return schema;
    }

    public P createPhantom(PropertyStore store) {
        return (P) Proxy.newProxyInstance(phantomClass.getClassLoader(),
                new Class<?>[]{phantomClass},
                new PhantomPojoProxy<>(this, store));
    }

    public B createBuilder(PropertyStore store) {
        return (B) Proxy.newProxyInstance(builderClass.getClassLoader(),
                new Class<?>[]{builderClass, Supplier.class},
                new PhantomBuilderProxy(this, store));
    }

}
