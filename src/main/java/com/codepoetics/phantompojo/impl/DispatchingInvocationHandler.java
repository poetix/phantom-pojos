package com.codepoetics.phantompojo.impl;

import com.codepoetics.phantompojo.impl.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface DispatchingInvocationHandler extends InvocationHandler {

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault())
        {
            return ReflectionUtils.invokeDefault(proxy, method, args);
        }

        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }

        return invokeMissing(proxy, method, args);
    }

    Object invokeMissing(Object proxy, Method method, Object[] args) throws Throwable;
}
