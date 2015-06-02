package com.codepoetics.phantompojo;

import com.codepoetics.phantompojo.impl.PhantomBuilderClassPair;
import com.codepoetics.phantompojo.impl.PropertySchema;
import com.codepoetics.phantompojo.impl.PropertyStore;

import java.util.Map;
import java.util.function.Supplier;

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
                PropertyStore store = schema.createStoreFromMap(properties, PojoPromoter::promote);

                return classPair.createPhantom(store);
            }
        };
    }

    B update();
    Map<String, Object> properties();
}
