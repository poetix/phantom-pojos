package com.codepoetics.phantompojo;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.Map;
import java.util.function.Supplier;

public interface PhantomPojo<B extends Supplier<? extends PhantomPojo<?>>> {

    interface PropertiesCapture {
        <P extends PhantomPojo> P with(Class<? extends P> klass);
    }

    static PropertiesCapture wrapping(Map<String, Object> properties) {
        return properties instanceof PMap
            ? wrapping((PMap<String, Object>) properties)
            : wrapping(HashTreePMap.from(properties));
    }

    static PropertiesCapture wrapping(PMap<String, Object> properties) {
        return new PropertiesCapture() {
            @Override
            public <P extends PhantomPojo> P with(Class<? extends P> klass) {
                return (P) PhantomBuilder.building(klass, properties).get();
            }
        };
    }

    B update();
    PMap<String, Object> getProperties();
}
