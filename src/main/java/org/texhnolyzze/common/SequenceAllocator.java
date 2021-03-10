package org.texhnolyzze.common;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Буфер последовательности
 */
public class SequenceAllocator {

    /**
     * Функция, возвращающая следующее свободное значение последовательности
     */
    private final LongSupplier nextVal;

    /**
     * Название последовательности
     */
    private final String sequenceName;

    /**
     * Размер инкремента последовательности
     */
    private final int increment;

    private final ReentrantLock lock;

    private final AtomicLong prevSeqValue;
    private volatile long nextSeqValue;

    public SequenceAllocator(LongSupplier nextVal, String sequenceName, int increment) {
        this.nextVal = Objects.requireNonNull(nextVal);
        this.sequenceName = sequenceName;
        this.lock = new ReentrantLock();
        this.increment = increment;
        this.prevSeqValue = new AtomicLong(0L);
        this.nextSeqValue = 0L;
    }

    public long obtain() {
        if (prevSeqValue.get() == nextSeqValue) {
            lock.lock();
            try {
                if (prevSeqValue.get() == nextSeqValue) {
                    long next = nextVal.getAsLong();
                    prevSeqValue.set(next);
                    nextSeqValue = next + increment;
                }
            } finally {
                lock.unlock();
            }
        }
        return prevSeqValue.getAndIncrement();
    }

    @Override
    public String toString() {
        return toStringHelper(SequenceAllocator.class.getSimpleName())
                .add("nextVal", nextVal)
                .add("sequenceName", sequenceName)
                .add("lock", lock)
                .add("increment", increment)
                .add("prevSeqValue", prevSeqValue)
                .add("nextSeqValue", nextSeqValue)
                .toString();
    }

}
