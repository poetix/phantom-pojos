package com.codepoetics.phantompojo.impl;

import com.codepoetics.phantompojo.PhantomPojo;

import java.lang.reflect.Method;
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
        store.write(method, reify(args[0]));
        return proxy;
    }

    private Object reify(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg.getClass().isArray()) {
            return Stream.of((Object[]) arg).map(this::reify).collect(Collectors.toList());
        }

        return arg instanceof Supplier
                ? ((Supplier<?>) arg).get()
                : arg;
    }

    @Override
    public P get() {
        return classPair.createPhantom(store.copy());
    }
}
