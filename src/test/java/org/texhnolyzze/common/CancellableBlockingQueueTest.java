package org.texhnolyzze.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

class CancellableBlockingQueueTest {

    @Test
    void test() throws InterruptedException {
        int n = 10000;
        int expectedSum = IntStream.range(0, n).sum();
        AtomicInteger actualSum = new AtomicInteger();
        final int[] nextProduced = {0};
        Lock nextProducedLock = new ReentrantLock();
        CancellableBlockingQueue<Integer> queue = new CancellableBlockingQueue<>(100, 10, 10);
        ExecutorService readers = Executors.newFixedThreadPool(10);
        ExecutorService writers = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int fi = i;
            readers.submit(new Callable<Void>() {
                int j = fi;
                @Override
                public Void call() throws InterruptedException {
                    Integer consumed;
                    try {
                        cycle: while ((consumed = queue.poll()) != null) {
                            actualSum.addAndGet(consumed);
                            switch (j) {
                                case 0:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.1)
                                        break cycle;
                                    break;
                                case 1:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.09)
                                        break cycle;
                                    break;
                                case 2:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.08)
                                        break cycle;
                                    break;
                                case 3:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.07)
                                        break cycle;
                                    break;
                                case 4:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.06)
                                        break cycle;
                                    break;
                                case 5:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.05)
                                        break cycle;
                                    break;
                                case 6:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.04)
                                        break cycle;
                                    break;
                                case 7:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.03)
                                        break cycle;
                                    break;
                                case 8:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.02)
                                        break cycle;
                                    break;
                            }
                        }
                    } finally {
                        queue.readerDone();
                    }
                    return null;
                }
            });
        }
        for (int i = 0; i < 10; i++) {
            final int fi = i;
            writers.submit(new Callable<Void>() {
                int j = fi;
                @Override
                public Void call() throws Exception {
                    try {
                        cycle: while (true) {
                            switch (j) {
                                case 0:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.1)
                                        break cycle;
                                    break;
                                case 1:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.09)
                                        break cycle;
                                    break;
                                case 2:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.08)
                                        break cycle;
                                    break;
                                case 3:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.07)
                                        break cycle;
                                    break;
                                case 4:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.06)
                                        break cycle;
                                    break;
                                case 5:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.05)
                                        break cycle;
                                    break;
                                case 6:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.04)
                                        break cycle;
                                    break;
                                case 7:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.03)
                                        break cycle;
                                    break;
                                case 8:
                                    if (ThreadLocalRandom.current().nextDouble() < 0.02)
                                        break cycle;
                                    break;
                            }
                            nextProducedLock.lock();
                            try {
                                if (nextProduced[0] == n)
                                    break;
                                queue.add(nextProduced[0]++);
                            } finally {
                                nextProducedLock.unlock();
                            }
                        }
                    } finally {
                        queue.writerDone();
                    }
                    return null;
                }
            });
        }
        try {
            readers.awaitTermination(1, TimeUnit.SECONDS);
        } finally {
            readers.shutdownNow();
            writers.shutdownNow();
        }
        Assertions.assertThat(actualSum.get()).isEqualTo(expectedSum);

    }

}