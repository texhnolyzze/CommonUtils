package org.texhnolyzze.common;

import java.util.Objects;
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

    private long prevSeqValue;
    private long nextSeqValue;

    public SequenceAllocator(LongSupplier nextVal, String sequenceName, int increment) {
        this.nextVal = Objects.requireNonNull(nextVal);
        this.sequenceName = sequenceName;
        this.increment = increment;
        this.prevSeqValue = 0L;
        this.nextSeqValue = 0L;
    }

    public long obtain() {
        long res;
        synchronized (this) {
            if (prevSeqValue == nextSeqValue) {
                long next = nextVal.getAsLong();
                prevSeqValue = next + 1;
                nextSeqValue = next + increment;
                res = next;
            } else {
                res = prevSeqValue++;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return toStringHelper(SequenceAllocator.class.getSimpleName())
                .add("nextVal", nextVal)
                .add("sequenceName", sequenceName)
                .add("increment", increment)
                .add("prevSeqValue", prevSeqValue)
                .add("nextSeqValue", nextSeqValue)
                .toString();
    }

}
