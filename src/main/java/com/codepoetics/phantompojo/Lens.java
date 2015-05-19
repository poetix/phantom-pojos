package com.codepoetics.phantompojo;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface Lens<P, T> {
    T get(P pojo);
    P set(P pojo, T newValue);

    default P update(P pojo, UnaryOperator<T> updater) {
        return set(pojo, updater.apply(get(pojo)));
    }

    static <P, T> Lens<P, T> on(Function<P, T> getter, BiFunction<P, T, P> setter) {
        return new Lens<P, T>() {
            @Override
            public T get(P pojo) {
                return getter.apply(pojo);
            }

            @Override
            public P set(P pojo, T newValue) {
                return setter.apply(pojo, newValue);
            }
        };
    }

    default <T2> Lens<P, T2> andThen(Lens<T, T2> next) {
        return on(p -> next.get(get(p)),
                (p, t2) -> set(p, next.set(get(p), t2)));
    }

    static <T, B extends Supplier<P>, P extends PhantomPojo<B>> Lens<P, T> onPhantom(Function<P, T> getter, BiFunction<B, T, B> updater) {
        return on(getter, (p, t) -> updater.apply(p.update(), t).get());
    }
}
