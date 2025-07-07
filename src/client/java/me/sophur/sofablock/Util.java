package me.sophur.sofablock;

import java.util.function.Supplier;
import java.util.function.Consumer;

public class Util {
    private Util() {
    }

    // I love boilerplate

    public static <T> Holder<T> createHolder(Supplier<T> getter, Consumer<T> setter) {
        return new Holder<>() {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T t) {
                setter.accept(t);
            }
        };
    }

    public static abstract class Holder<T> {
        public abstract T get();

        public abstract void set(T t);
    }
}