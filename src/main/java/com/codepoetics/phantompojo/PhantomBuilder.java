package com.codepoetics.phantompojo;

import com.codepoetics.phantompojo.impl.PhantomBuilderClassPair;
import com.codepoetics.phantompojo.impl.PropertySchema;

import java.util.function.Supplier;

public final class PhantomBuilder {

    private PhantomBuilder() {
    }

    public static <P extends PhantomPojo<B>, B extends Supplier<P>> B building(Class<? extends P> targetClass) {
        PhantomBuilderClassPair<P, B> classPair = PhantomBuilderClassPair.forPhantomClass(targetClass);
        PropertySchema schema = classPair.getPropertySchema();

        return classPair.createBuilder(schema.createStore());
    }
}
