package org.texhnolyzze.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceAllocatorTest {

    @Test
    void testObtain() throws ExecutionException, InterruptedException {
        int increment = 43;
        AtomicLong sequence = new AtomicLong(0);
        SequenceAllocator allocator = new SequenceAllocator(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return sequence.getAndAdd(increment);
        }, "test_seq", increment);
        ExecutorService pool = Executors.newFixedThreadPool(100);
        Set<Long> all = new HashSet<>();
        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            all.add((long) i);
            Future<Long> next = pool.submit(allocator::obtain);
            futures.add(next);
        }
        for (Future<Long> future : futures) {
            all.remove(future.get());
        }
        assertThat(all).isEmpty();
    }

}