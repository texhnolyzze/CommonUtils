package org.texhnolyzze.common;

import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MonotonicStackTest {

    @Test
    void nextSmaller() {
        test(
            MonotonicStack::nextSmaller,
            (array, nextSmaller) -> {
                for (int i = 0; i < nextSmaller.length; i++) {
                    if (nextSmaller[i] == -1) {
                        for (int j = i + 1; j < nextSmaller.length; j++) {
                            assertThat(array[j]).isGreaterThan(array[i]);
                        }
                    } else {
                        assertThat(array[nextSmaller[i]]).isLessThan(array[i]);
                        for (int j = i + 1; j < nextSmaller[i]; j++) {
                            assertThat(array[j]).isGreaterThan(array[i]);
                        }
                    }
                }
            }
        );
    }

    @Test
    void nextGreater() {
        test(
            MonotonicStack::nextGreater,
            (array, nextGreater) -> {
                for (int i = 0; i < nextGreater.length; i++) {
                    if (nextGreater[i] == -1) {
                        for (int j = i + 1; j < nextGreater.length; j++) {
                            assertThat(array[j]).isLessThan(array[i]);
                        }
                    } else {
                        assertThat(array[nextGreater[i]]).isGreaterThan(array[i]);
                        for (int j = i + 1; j < nextGreater[i]; j++) {
                            assertThat(array[j]).isLessThan(array[i]);
                        }
                    }
                }
            }
        );
    }

    @Test
    void prevSmaller() {
        test(
            MonotonicStack::prevSmaller,
            (array, prevSmaller) -> {
                for (int i = 0; i < prevSmaller.length; i++) {
                    if (prevSmaller[i] == -1) {
                        for (int j = 0; j < i; j++) {
                            assertThat(array[j]).isGreaterThan(array[i]);
                        }
                    } else {
                        assertThat(array[prevSmaller[i]]).isLessThan(array[i]);
                        for (int j = prevSmaller[i] + 1; j < i; j++) {
                            assertThat(array[j]).isGreaterThan(array[i]);
                        }
                    }
                }
            }
        );
    }

    @Test
    void prevGreater() {
        test(
            MonotonicStack::prevGreater,
            (array, prevGreater) -> {
                for (int i = 0; i < prevGreater.length; i++) {
                    if (prevGreater[i] == -1) {
                        for (int j = 0; j < i; j++) {
                            assertThat(array[j]).isLessThan(array[i]);
                        }
                    } else {
                        assertThat(array[prevGreater[i]]).isGreaterThan(array[i]);
                        for (int j = prevGreater[i] + 1; j < i; j++) {
                            assertThat(array[j]).isLessThan(array[i]);
                        }
                    }
                }
            }
        );
    }

    private static void test(
        final TriFunction<Integer[], Comparator<Integer>, IntConsumer, int[]> methodToTest,
        final BiConsumer<Integer[], int[]> test
    ) {
        for (int n = 0; n < 100; n++) {
            final Integer[] array = generateUniqueArray();
            final int[] result = methodToTest.apply(array, Comparator.naturalOrder(), null);
            test.accept(array, result);
        }
    }

    private static Integer[] generateUniqueArray() {
        final Integer[] arr = new Integer[250];
        final Set<Integer> used = new HashSet<>();
        for (int i = 0; i < arr.length; i++) {
            int next;
            while (true) {
                next = ThreadLocalRandom.current().nextInt(0, 1000);
                if (!used.contains(next)) {
                    break;
                }
            }
            used.add(next);
            arr[i] = next;
        }
        return arr;
    }

}