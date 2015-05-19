package com.codepoetics.phantompojo.impl;

import com.codepoetics.phantompojo.PhantomPojo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

final class PhantomPojoProxy<P extends PhantomPojo<B>, B extends Supplier<P>> implements DispatchingInvocationHandler, PhantomPojo<B> {

    private final PhantomBuilderClassPair<P, B> classPair;
    private final PropertyStore store;

    PhantomPojoProxy(PhantomBuilderClassPair<P, B> classPair, PropertyStore store) {
        this.classPair = classPair;
        this.store = store;
    }

    @Override
    public Object invokeMissing(Object proxy, Method method, Object[] args) throws Throwable {
        return store.read(method);
    }

    @Override
    public int hashCode() {
        return store.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!Proxy.isProxyClass(o.getClass())) {
            return false;
        }
        InvocationHandler ih = Proxy.getInvocationHandler(o);
        if (!(ih instanceof PhantomPojoProxy)) {
            return false;
        }
        PhantomPojoProxy<?, ?> other = (PhantomPojoProxy<?, ?>) ih;
        return Objects.equals(store, other.store);
    }

    @Override
    public String toString() {
        return store.toString();
    }

    @Override
    public B update() {
        return classPair.createBuilder(store.copy());
    }

    @Override
    public Map<String, Object> properties() {
        return store.toMap();
    }
}
