package lib;

@FunctionalInterface
public interface UncheckedCallable<V> {
    V call();
}
