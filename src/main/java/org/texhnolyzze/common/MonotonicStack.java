package org.texhnolyzze.common;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.IntConsumer;

public final class MonotonicStack {

    private MonotonicStack() {
        throw new UnsupportedOperationException();
    }

    public static <E> int[] nextSmaller(
        final E[] elems,
        final Comparator<? super E> comparator,
        final IntConsumer onPop
    ) {
        final Deque<Integer> stack = new LinkedList<>();
        final int[] nextSmaller = new int[elems.length];
        for (int i = 0; i < elems.length; i++) {
            final E current = elems[i];
            nextSmaller[i] = -1;
            while (!stack.isEmpty() && comparator.compare(elems[stack.peek()], current) > 0) {
                final int greater = stack.pop();
                nextSmaller[greater] = i;
                if (onPop != null) {
                    onPop.accept(greater);
                }
            }
            stack.push(i);
        }
        return nextSmaller;
    }

    public static <E> int[] nextGreater(
        final E[] elems,
        final Comparator<? super E> comparator,
        final IntConsumer onPop
    ) {
        final Deque<Integer> stack = new LinkedList<>();
        final int[] nextGreater = new int[elems.length];
        for (int i = 0; i < elems.length; i++) {
            final E current = elems[i];
            nextGreater[i] = -1;
            while (!stack.isEmpty() && comparator.compare(elems[stack.peek()], current) < 0) {
                final int smaller = stack.pop();
                nextGreater[smaller] = i;
                if (onPop != null) {
                    onPop.accept(smaller);
                }
            }
            stack.push(i);
        }
        return nextGreater;
    }

    public static <E> int[] prevSmaller(
        final E[] elems,
        final Comparator<? super E> comparator,
        final IntConsumer onPop
    ) {
        final Deque<Integer> stack = new LinkedList<>();
        final int[] prevSmaller = new int[elems.length];
        for (int i = 0; i < elems.length; i++) {
            final E current = elems[i];
            while (!stack.isEmpty() && comparator.compare(elems[stack.peek()], current) > 0) {
                final int greater = stack.pop();
                if (onPop != null) {
                    onPop.accept(greater);
                }
            }
            if (!stack.isEmpty()) {
                prevSmaller[i] = stack.peek();
            } else {
                prevSmaller[i] = -1;
            }
            stack.push(i);
        }
        return prevSmaller;
    }

    public static <E> int[] prevGreater(
        final E[] elems,
        final Comparator<? super E> comparator,
        final IntConsumer onPop
    ) {
        final Deque<Integer> stack = new LinkedList<>();
        final int[] prevGreater = new int[elems.length];
        for (int i = 0; i < elems.length; i++) {
            final E current = elems[i];
            while (!stack.isEmpty() && comparator.compare(elems[stack.peek()], current) < 0) {
                final int smaller = stack.pop();
                if (onPop != null) {
                    onPop.accept(smaller);
                }
            }
            if (!stack.isEmpty()) {
                prevGreater[i] = stack.peek();
            } else {
                prevGreater[i] = -1;
            }
            stack.push(i);
        }
        return prevGreater;
    }


}
