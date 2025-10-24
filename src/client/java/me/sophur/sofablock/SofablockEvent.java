package me.sophur.sofablock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SofablockEvent<T> extends HashSet<Consumer<T>> {
    public void invoke(T t) {
        forEach(c -> c.accept(t));
    }
}
