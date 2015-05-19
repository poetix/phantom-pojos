package com.codepoetics.phantompojo;

import com.codepoetics.navn.Name;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.lang.reflect.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PhantomBuilder<T extends PhantomPojo<B>, B extends Supplier<T>> implements DispatchingInvocationHandler, Supplier<T> {
    public static <B extends Supplier<T>, T extends PhantomPojo<B>> B building(Class<? extends T> targetClass) {
        return building(targetClass, getBuilderClass(targetClass), HashTreePMap.empty());
    }

    public static <B extends Supplier<T>, T extends PhantomPojo<B>> B building(Class<? extends T> targetClass, PMap<String, Object> propertyValues) {
        return building(targetClass, getBuilderClass(targetClass), propertyValues);
    }

    private static <B extends Supplier<T>, T extends PhantomPojo<B>> Class<? extends B> getBuilderClass(Class<? extends T> targetClass) {
        return Stream.of(targetClass.getGenericInterfaces())
                .filter(t -> PhantomPojo.class.equals(ReflectionUtils.rawTypeOf(t)))
                .map(ReflectionUtils::<B>getFirstTypeArgument)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot infer builder class from class " + targetClass));
    }

    static <T, B extends Supplier<T>> B building(Class<? extends T> targetClass, Class<? extends B> builderClass, PMap<String, Object> propertyValues) {
        return (B) Proxy.newProxyInstance(builderClass.getClassLoader(),
                new Class<?>[]{builderClass, Supplier.class},
                new PhantomBuilder(targetClass, builderClass, propertyValues));
    }

    private final Class<? extends T> targetClass;
    private Class<? extends B> builderClass;
    private final PMap<String, Object> propertyValues;

    private PhantomBuilder(Class<? extends T> targetClass, Class<? extends B> builderClass, PMap<String, Object> propertyValues) {
        this.targetClass = targetClass;
        this.builderClass = builderClass;
        this.propertyValues = propertyValues;
    }

    @Override
    public Object invokeMissing(Object proxy, Method method, Object[] args) throws Throwable {
        String propertyName = getFieldName(method);
        if (args[0].getClass().isArray()) {
            return saveProperty(propertyName, Stream.of((Object[]) args[0]).map(this::reify).collect(Collectors.toList()));
        } else {
            return saveProperty(propertyName, reify(args[0]));
        }
    }

    private B saveProperty(String propertyName, Object value) {
        return building(targetClass, builderClass, propertyValues.plus(propertyName, value));
    }

    private String getFieldName(Method method) {
        return Name.of(method.getName()).withoutFirst().toCamelCase();
    }

    private Object reify(Object arg) {
        return arg instanceof Supplier
                ? ((Supplier<?>) arg).get()
                : arg;
    }

    @Override
    public T get() {
        return PhantomPojoProxy.proxying(targetClass, builderClass, propertyValues);
    }
}
