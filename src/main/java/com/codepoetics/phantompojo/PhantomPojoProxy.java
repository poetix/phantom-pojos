package com.codepoetics.phantompojo;

import com.codepoetics.navn.Name;
import org.pcollections.PMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class PhantomPojoProxy<T extends PhantomPojo<B>, B extends Supplier<T>> implements InvocationHandler, PhantomPojo<B> {

    static <T extends PhantomPojo<B>, B extends Supplier<T>> T proxying(
            Class<? extends T> klass,
            Class<? extends B> builderClass,
            PMap<String, Object> propertyValues) {
        return (T) Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[]{klass},
                new PhantomPojoProxy<>(klass, builderClass, propertyValues));
    }

    private final Class<? extends T> klass;
    private final Class<? extends B> builderClass;
    private final PMap<String, Object> propertyValues;

    private PhantomPojoProxy(Class<? extends T> klass, Class<? extends B> builderClass, PMap<String, Object> propertyValues) {
        this.klass = klass;
        this.builderClass = builderClass;
        this.propertyValues = propertyValues;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isTerminal(method)) {
            return method.invoke(this, args);
        }

        return promote(method.getGenericReturnType(),
                propertyValues.get(Name.of(method.getName()).withoutFirst().toCamelCase()));
    }

    private Object promote(Type targetType, Object actual) {
        Class<?> targetClass = ReflectionUtils.rawTypeOf(targetType);

        if (Map.class.isAssignableFrom(actual.getClass())
            && PhantomPojo.class.isAssignableFrom(targetClass)) {
                return PhantomPojo.wrapping((Map<String, Object>) actual)
                        .with((Class<? extends PhantomPojo<?>>) targetClass);
        }

        if (List.class.isAssignableFrom(actual.getClass())) {
            Type listType = ReflectionUtils.getFirstTypeArgument(targetType);
            return ((List<?>) actual).stream()
                    .map(item -> promote(listType, item))
                    .collect(Collectors.toList());
        }

        return actual;
    }

    private boolean isTerminal(Method method) {
        return method.getDeclaringClass().equals(Object.class)
                || method.getDeclaringClass().equals(PhantomPojo.class);
    }

    @Override
    public int hashCode() {
        return Objects.hash(klass, propertyValues);
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
        return Objects.equals(klass, other.klass) &&
                Objects.equals(propertyValues, other.propertyValues);
    }

    @Override
    public String toString() {
        return klass.getSimpleName() + " " + propertyValues.toString();
    }

    @Override
    public B update() {
        return PhantomBuilder.building(klass, builderClass, propertyValues);
    }

    @Override
    public PMap<String, Object> getProperties() {
        return propertyValues;
    }
}
