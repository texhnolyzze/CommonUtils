package org.texhnolyzze.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Iterators {

    private Iterators() {
        throw new UnsupportedOperationException();
    }

    public static <E> Iterator<E> filter(Iterator<? extends E> src, Predicate<? super E> filter) {
        return new Iterator<>() {

            E next;

            @Override
            public boolean hasNext() {
                if (next == null)
                    next0();
                return next != null;
            }

            @Override
            public E next() {
                if (next == null)
                    next0();
                if (next == null)
                    throw new NoSuchElementException();
                E res = next;
                next = null;
                return res;
            }

            private void next0() {
                while (src.hasNext()) {
                    E e = src.next();
                    if (filter.test(e)) {
                        next = e;
                        break;
                    }
                }
            }

        };
    }

    public static <E, R> Iterator<R> map(Iterator<? extends E> src, Function<? super E, ? extends R> mapper) {
        return new Iterator<>() {
            @Override public boolean hasNext() {return src.hasNext();}
            @Override public R next() {return mapper.apply(src.next());}
        };
    }

    public static <E> BufferingIterator<E> buffer(Iterator<E> src) {
        return new BufferingIterator<>(src);
    }

}
