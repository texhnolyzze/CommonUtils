package org.texhnolyzze.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BufferingIterator<E> implements Iterator<E> {

    private E buffer;
    private boolean nextReturnBuffer;
    private boolean prevReturnedBuffered;
    private final Iterator<E> src;

    BufferingIterator(Iterator<E> src) {
        this.src = src;
    }

    @Override
    public boolean hasNext() {
        if (nextReturnBuffer)
            return true;
        return src.hasNext();
    }

    @Override
    public E next() {
        if (nextReturnBuffer) {
            nextReturnBuffer = false;
            prevReturnedBuffered = true;
        } else {
            buffer = src.next();
            prevReturnedBuffered = false;
        }
        return buffer;
    }

    public boolean prevReturnedBuffered() {
        return prevReturnedBuffered;
    }

    public void buffer() {
        if (buffer == null)
            throw new NoSuchElementException();
        nextReturnBuffer = true;
    }

}
