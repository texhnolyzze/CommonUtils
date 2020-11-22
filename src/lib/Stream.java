package lib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static lib.Promise.promise;

public class Stream<T> {

    private static final Stream<?> EMPTY_STREAM = cons(null, self -> () -> self);

    private final T car;
    private final Promise<Stream<T>> cdr;

    private Stream(T car, Promise<Stream<T>> cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    private Stream(T car, Function<Stream<T>, UncheckedCallable<Stream<T>>> cdr) {
        this.car = car;
        this.cdr = promise(cdr.apply(this));
    }

    public Optional<T> findAny() {
        if (isEmpty())
            return Optional.empty();
        return Optional.of(car());
    }

    boolean isEmpty() {
        return this == EMPTY_STREAM;
    }

    T car() {
        return car;
    }

    Stream<T> cdr() {
        return cdr.get();
    }

    public T ref(long n) {
        Stream<T> res = this;
        while (!res.isEmpty()) {
            if (n == 0)
                return res.car();
            res = res.cdr();
            n--;
        }
        return null;
    }

    public Stream<T> combine(Stream<T> other, BiFunction<? super T, ? super T, ? extends T> combiner) {
        if (isEmpty() || other.isEmpty())
            return empty();
        T res = combiner.apply(car(), other.car());
        return cons(res, () -> cdr().combine(other.cdr(), combiner));
    }

    public Stream<T> filter(Predicate<? super T> predicate) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            if (predicate.test(s.car())) {
                Stream<T> res = s;
                return cons(res.car(), () -> res.cdr().filter(predicate));
            }
            s = s.cdr();
        }
        return empty();
    }

    public <X> Stream<X> flatMap(Function<? super T, Stream<? extends X>> mapper) {
        Stream<T> target = this;
        while (!target.isEmpty()) {
            Stream<? extends X> flat = mapper.apply(target.car());
            if (!flat.isEmpty()) {
                final Stream<T> ftarget = target;
                return drain(flat, () -> ftarget.cdr().flatMap(mapper));
            }
            target = target.cdr();
        }
        return empty();
    }

    private static <X> Stream<X> drain(Stream<? extends X> s, UncheckedCallable<Stream<X>> next) {
        if (s.isEmpty())
            return next.call();
        return cons(s.car(), () -> drain(s.cdr(), next));
    }

    public Stream<T> skip(long n) {
        Stream<T> res = this;
        while (!res.isEmpty() && n != 0) {
            res = res.cdr();
            n--;
        }
        return res;
    }

    public void forEach(Consumer<? super T> consumer) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            consumer.accept(s.car());
            s = s.cdr();
        }
    }

    public <V> Stream<V> map(Function<? super T, ? extends V> mapper) {
        if (isEmpty())
            return empty();
        return cons(mapper.apply(car()), () -> cdr().map(mapper));
    }

    public Stream<T> limit(long n) {
        if (n <= 0)
            return empty();
        return cons(car(), () -> cdr().limit(n - 1));
    }

    public boolean allMatch(Predicate<? super T> predicate) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            boolean test = predicate.test(s.car());
            if (!test)
                return false;
            s = s.cdr();
        }
        return true;
    }

    public boolean anyMatch(Predicate<? super T> predicate) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            boolean test = predicate.test(s.car());
            if (test)
                return true;
            s = s.cdr();
        }
        return false;
    }

    public boolean noneMatch(Predicate<? super T> predicate) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            boolean test = predicate.test(s.car());
            if (test)
                return false;
            s = s.cdr();
        }
        return true;
    }

    public Stream<T> peek(Consumer<? super T> consumer) {
        Stream<T> s = this;
        while (!s.isEmpty()) {
            consumer.accept(s.car());
            s = s.cdr();
        }
        return this;
    }

    public long count() {
        long count = 0;
        Stream<T> s = this;
        while (!s.isEmpty()) {
            count++;
            s = s.cdr();
        }
        return count;
    }

    public void display() {
        forEach(System.out::println);
    }

    public Stream<Pair<Integer, T>> indexed() {
        return indexed(this, 0);
    }

    public Stream<Pair<Integer, T>> indexed(Stream<T> src, int from) {
        if (src.isEmpty())
            return empty();
        return cons(Pair.of(from, src.car()), () -> indexed(src.cdr(), from + 1));
    }

    static <X> Stream<X> cons(X car, UncheckedCallable<Stream<X>> cdr) {
        return new Stream<>(car, promise(cdr));
    }

    static <X> Stream<X> cons(X car, Function<Stream<X>, UncheckedCallable<Stream<X>>> cdr) {
        return new Stream<>(car, cdr);
    }

    @SuppressWarnings("unchecked")
    static <X> Stream<X> empty() {
        return (Stream<X>) EMPTY_STREAM;
    }

    static <X> Stream<X> fromCollection(Collection<X> list) {
        if (list.isEmpty())
            return empty();
        Iterator<X> iterator = list.iterator();
        return fromIterator(iterator);
    }

    static <X> Stream<X> fromIterator(Iterator<X> iterator) {
        return fromIterator(iterator, 0);
    }

    private static <X> Stream<X> fromIterator(Iterator<X> iterator, int index) {
        if (iterator.hasNext())
            return cons(iterator.next(), () -> fromIterator(iterator, index + 1));
        return empty();
    }

}
