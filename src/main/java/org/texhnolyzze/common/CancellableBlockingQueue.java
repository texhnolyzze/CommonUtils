package org.texhnolyzze.common;

import com.google.common.base.Preconditions;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Блокирующая очередь с возможностью указания того, что данных больше нет.<br>
 * Идиома для reader-ов выглядит следующим образом:<br>
 * <pre>
 *     CancellableBlockingQueue queue = ...
 *     Object next;
 *     try {
 *         while ((next = queue.poll()) != null) {
 *              processNext(next);
 *         }
 *     } finally {
 *         queue.readerDone();
 *     }
 * </pre>
 * Для writer-ов:<br>
 * <pre>
 *      CancellableBlockingQueue queue = ...
 *      Object next;
 *      try {
 *          while (hasMoreElements) {
 *               queue.add(elem);
 *          }
 *      } finally {
 *          queue.writerDone();
 *      }
 * </pre>
 * @param <E> Тип элементов
 */
public class CancellableBlockingQueue<E> {

    private static final Object BARRIER = new Object();

    private final BlockingQueue<E> buff;

    private final AtomicInteger activeReaders;
    private final AtomicInteger activeWriters;

    public CancellableBlockingQueue(int maxSize) {
        this(maxSize, 1, 1);
    }

    public CancellableBlockingQueue(int maxSize, int numReaders, int numWriters) {
        Preconditions.checkArgument(numReaders > 0 && numWriters > 0);
        Preconditions.checkArgument(maxSize >= numWriters, "maxSize must be >= numWriters to avoid deadlocks");
        this.buff = new LinkedBlockingQueue<>(maxSize);
        this.activeReaders = new AtomicInteger(numReaders);
        this.activeWriters = new AtomicInteger(numWriters);
    }

    public void add(E elem) throws InterruptedException {
        Preconditions.checkNotNull(elem, "Nulls not permitted");
        Preconditions.checkState(activeWriters.get() != 0, "All writers done");
        Preconditions.checkState(activeReaders.get() != 0, "All readers done");
        buff.put(elem);
    }

    @SuppressWarnings("unchecked")
    public E poll() throws InterruptedException {
        E next = buff.take();
        if (next == BARRIER) {
            Preconditions.checkState(buff.isEmpty());
            buff.add((E) BARRIER);
            return null;
        }
        return next;
    }

    @SuppressWarnings("unchecked")
    public void writerDone() throws InterruptedException {
        int activeRemained = activeWriters.decrementAndGet();
        Preconditions.checkState(activeRemained >= 0, "Active writers count is negative");
        if (activeRemained == 0)
            buff.put((E) BARRIER);
    }

    public void readerDone() {
        int activeRemained = activeReaders.decrementAndGet();
        Preconditions.checkState(activeRemained >= 0, "Active readers count is negative");
        if (activeRemained == 0)
            buff.clear();
    }

    /**
     * Возвращает очередь, в которую нельзя писать, но можно один раз прочитать
     */
    @SafeVarargs
    public static <E> CancellableBlockingQueue<E> doneQueue(E...values) {
        CancellableBlockingQueue<E> iter = new CancellableBlockingQueue<>(values.length + 1);
        try {
            for (E value : values) {
                iter.add(value);
            }
            iter.writerDone();
        } catch (InterruptedException e) { // Не будет
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        return iter;
    }

}
