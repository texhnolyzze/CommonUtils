package org.texhnolyzze.common;

public final class ComparableUtil {

    private ComparableUtil() {
        throw new UnsupportedOperationException();
    }

    public static <C extends Comparable<? super C>> C min(C left, C right) {
        return left.compareTo(right) < 0 ? left : right;
    }

    public static <C extends Comparable<? super C>> C max(C left, C right) {
        return left.compareTo(right) < 0 ? right : left;
    }

}
