package org.texhnolyzze.common;

public class Promise<V> {

    private V memo;
    private final UncheckedCallable<V> callable;

    private Promise(UncheckedCallable<V> callable) {
        this.callable = callable;
    }

    public V get() {
        if (memo == null)
            memo = callable.call();
        return memo;
    }

    public static <V> Promise<V> promise(UncheckedCallable<V> callable) {
        return new Promise<>(callable);
    }

}
