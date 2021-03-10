package org.texhnolyzze.common;

@FunctionalInterface
public interface UncheckedCallable<V> {
    V call();
}
