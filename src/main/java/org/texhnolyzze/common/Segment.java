package org.texhnolyzze.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Segment<C extends Comparable<? super C>> {

    private final C from;
    private final C to;

    private Segment(C from, C to) {
        Preconditions.checkArgument(from.compareTo(to) <= 0);
        this.from = from;
        this.to = to;
    }

    public C from() {
        return from;
    }

    public C to() {
        return to;
    }

    public <D> List<Segment<C>> split(
        D delta,
        BiFunction<? super C, ? super D, ? extends C> adjuster
    ) {
        if (adjuster.apply(from, delta).compareTo(from) == 0)
            return singletonList(this);
        C temp = this.from;
        List<Segment<C>> res = new ArrayList<>();
        while (true) {
            C bound = ComparableUtils.min(adjuster.apply(temp, delta), this.to);
            res.add(Segment.of(temp, bound));
            if (this.to.compareTo(bound) == 0)
                break;
            temp = bound;
        }
        return res;
    }

    public boolean contains(Segment<C> other) {
        return this.from.compareTo(other.from) <= 0 && this.to.compareTo(other.to) >= 0;
    }

    public boolean contains(C point) {
        return from.compareTo(point) <= 0 && point.compareTo(to) <= 0;
    }

    public boolean intersects(Segment<C> other) {
        return intersects(other.from, other.to);
    }

    public boolean intersects(C otherFrom, C otherTo) {
        return ComparableUtils.max(this.from, otherFrom).compareTo(ComparableUtils.min(this.to, otherTo)) <= 0;
    }

    public boolean degenerate() {
        return from.compareTo(to) == 0;
    }

    /**
     * Возвращает пересечение этого и переданного сегментов, или null, если его нет
     */
    public Segment<C> intersection(
        final Segment<C> other
    ) {
        final C left = ComparableUtils.max(this.from, other.from);
        final C right = ComparableUtils.min(this.to, other.to);
        if (left.compareTo(right) <= 0) {
            return Segment.of(left, right);
        } else {
            return null;
        }
    }

    public List<Segment<C>> diff(Segment<C> other) {
        if (!other.intersects(this))
            return singletonList(this);
        if (other.contains(this))
            return emptyList();
        if (this.from.compareTo(other.from) < 0) {
            if (this.to.compareTo(other.to) > 0) {
                return List.of(Segment.of(this.from, other.from), Segment.of(other.to, this.to));
            } else {
                return singletonList(Segment.of(this.from, other.from));
            }
        } else {
            return singletonList(Segment.of(other.to, this.to));
        }
    }

    public List<Segment<C>> diff(Iterable<Segment<C>> others) {
        return diff(others.iterator());
    }

    /**
     * Переданный {@code iterator} должен быть отсортирован по левой границе отрезка.
     * Метод однопроходный.
     * Возвращает все участки данного сегмента, не перекрывающиеся с никаким другим в переданном {@code iterator}.
     * Как следствие если сегменты в переданном {@code iterator} полностью покрывают этот -- возвращает пустой список.
     * Если ни один из сегментов в переданном {@code iterator} не пересекается с данным -- возвращает этот сегмент.
     */
    public List<Segment<C>> diff(Iterator<Segment<C>> iter) {
        List<Segment<C>> res = new ArrayList<>();
        C bound = this.from;
        C temp = this.from;
        boolean othersEmpty = true;
        while (iter.hasNext()) {
            Segment<C> next = iter.next();
            if (bound.compareTo(next.to) > 0 || !next.intersects(this))
                continue;
            if (next.contains(this))
                return emptyList();
            othersEmpty = false;
            if (next.intersects(temp, bound)) {
                bound = ComparableUtils.max(bound, next.to);
            } else {
                res.add(Segment.of(bound, next.from));
                if (!iter.hasNext()) {
                    bound = next.to;
                    break;
                } else {
                    bound = next.to;
                    temp = bound;
                }
            }
        }
        if (othersEmpty)
            return singletonList(this);
        if (bound.compareTo(this.to) < 0) {
            res.add(Segment.of(bound, this.to));
        } else {
            if (bound.compareTo(this.to) < 0 && res.isEmpty())
                return singletonList(this);
        }
        return res;
    }

    /**
     * Проверка, что сегмент полностью без разрывов покрыт другими сегментами
     */
    public boolean covered(
        final List<Segment<C>> segments
    ) {
        if (segments.isEmpty()) {
            return false;
        }
        segments.sort(Comparator.comparing(Segment::from));
        C left = segments.get(0).from;
        C right = segments.get(0).to;
        for (int i = 1; i < segments.size(); i++) {
            final Segment<C> otherSegment = segments.get(i);
            if (!otherSegment.intersects(left, right)) {
                break;
            }
            left = ComparableUtils.min(left, otherSegment.from);
            right = ComparableUtils.max(right, otherSegment.to);
        }
        return left.compareTo(this.from()) <= 0 && right.compareTo(this.to()) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;
        Segment<?> segment = (Segment<?>) o;
        return from().equals(segment.from()) && to().equals(segment.to());
    }

    @Override
    public int hashCode() {
        return Objects.hash(from(), to());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Segment.class.getSimpleName())
            .add("from", from)
            .add("to", to)
            .toString();
    }

    public static <C extends Comparable<? super C>> Segment<C> of(C from, C to) {
        return new Segment<>(from, to);
    }

}