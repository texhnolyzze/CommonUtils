package org.texhnolyzze.common;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class AbstractRectangle<C extends Comparable<? super C>, S extends AbstractRectangle<C, S>> {

    private final Segment<C> horizontal;
    private final Segment<C> vertical;

    protected AbstractRectangle(Segment<C> horizontal, Segment<C> vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    protected abstract S create(Segment<C> horizontal, Segment<C> vertical);

    public Segment<C> horizontal() {
        return horizontal;
    }

    public Segment<C> vertical() {
        return vertical;
    }

    public boolean intersects(S other) {
        return intersects(other.horizontal().from(), other.horizontal().to(), other.vertical().from(), other.vertical().to());
    }

    public boolean intersects(C horizontalFrom, C horizontalTo, C verticalFrom, C verticalTo) {
        return this.horizontal.intersects(horizontalFrom, horizontalTo) && this.vertical.intersects(verticalFrom, verticalTo);
    }

    public boolean contains(S other) {
        return this.horizontal.contains(other.horizontal()) && this.vertical.contains(other.vertical());
    }

    public boolean degenerate() {
        return this.horizontal.degenerate() || this.vertical().degenerate();
    }

    /**
     * Разбивает прямоугольник на меньшие прямоугольники.
     */
    public <D> List<List<S>> split(
        D horizontalDelta,
        D verticalDelta,
        BiFunction<? super C, ? super D, ? extends C> adjuster
    ) {
        List<Segment<C>> horizontalSplit = horizontal.split(horizontalDelta, adjuster);
        List<Segment<C>> verticalSplit = vertical.split(verticalDelta, adjuster);
        List<List<S>> result = new ArrayList<>(verticalSplit.size());
        for (Segment<C> ver : verticalSplit) {
            List<S> line = new ArrayList<>(horizontalSplit.size());
            for (Segment<C> hor : horizontalSplit) {
                line.add(create(hor, ver));
            }
            result.add(line);
        }
        return result;
    }

    public List<S> diff(Iterable<? extends S> others) {
        return diff(others.iterator());
    }

    /**
     * Метод однопроходный.
     * Возвращает все участки прямоугольника, не пересекающиеся с никаким другим в переданном {@code iterable}.
     * Как следствие -- если переданные прямоугольники полностью покрывают этот -- возвращает пустой список.
     * Если ни один из прямоугольников не пересекается с данным -- возвращает данный прямоугольник.
     */
    @SuppressWarnings("unchecked")
    public List<S> diff(Iterator<? extends S> others) {
        List<S> result = new ArrayList<>();
        List<S> temp = new ArrayList<>();
        result.add((S) this);
        while (others.hasNext()) {
            S other = others.next();
            ListIterator<S> iter = result.listIterator();
            while (iter.hasNext()) {
                S next = iter.next();
                iter.remove();
                List<S> diff = next.diff(other);
                for (S rect : diff) {
                    if (!rect.degenerate()) {
                        temp.add(rect);
                    }
                }
            }
            result.addAll(temp);
            temp.clear();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<S> diff(S other) {
        if (!other.intersects((S) this))
            return (List<S>) singletonList(this);
        if (other.contains((S) this))
            return emptyList();
        List<Segment<C>> horDiff = this.horizontal.diff(other.horizontal());
        List<Segment<C>> verDiff = this.vertical.diff(other.vertical());
        if (horDiff.isEmpty()) {
            if (verDiff.size() == 1) {
                return singletonList(create(this.horizontal(), verDiff.get(0)));
            } else {
                Preconditions.checkState(verDiff.size() == 2);
                return List.of(
                    create(this.horizontal(), verDiff.get(0)),
                    create(this.horizontal(), verDiff.get(1))
                );
            }
        } else {
            if (verDiff.isEmpty()) {
                if (horDiff.size() == 1)
                    return singletonList(create(horDiff.get(0), this.vertical()));
                else {
                    Preconditions.checkState(horDiff.size() == 2);
                    return List.of(
                        create(horDiff.get(0), this.vertical()),
                        create(horDiff.get(1), this.vertical())
                    );
                }
            } else {
                if (horDiff.size() == 1) {
                    if (verDiff.size() == 1) {
                        if (horDiff.get(0).from().equals(this.horizontal().from())) {
                            return List.of(
                                create(horDiff.get(0), this.vertical()),
                                create(Segment.of(horDiff.get(0).to(), this.horizontal().to()), verDiff.get(0))
                            );
                        } else {
                            return List.of(
                                create(horDiff.get(0), this.vertical()),
                                create(Segment.of(this.horizontal().from(), horDiff.get(0).from()), verDiff.get(0))
                            );
                        }
                    } else {
                        Preconditions.checkState(verDiff.size() == 2);
                        if (horDiff.get(0).from().equals(this.horizontal().from())) {
                            return List.of(
                                create(horDiff.get(0), this.vertical()),
                                create(Segment.of(horDiff.get(0).to(), this.horizontal().to()), verDiff.get(0)),
                                create(Segment.of(horDiff.get(0).to(), this.horizontal().to()), verDiff.get(1))
                            );
                        } else {
                            return List.of(
                                create(horDiff.get(0), this.vertical()),
                                create(Segment.of(this.horizontal().from(), horDiff.get(0).from()), verDiff.get(0)),
                                create(Segment.of(this.horizontal().from(), horDiff.get(0).from()), verDiff.get(1))
                            );
                        }
                    }
                } else {
                    Preconditions.checkState(horDiff.size() == 2);
                    if (verDiff.size() == 1) {
                        return List.of(
                            create(horDiff.get(0), this.vertical()),
                            create(horDiff.get(1), this.vertical()),
                            create(Segment.of(horDiff.get(0).to(), horDiff.get(1).from()), verDiff.get(0))
                        );
                    } else {
                        Preconditions.checkState(verDiff.size() == 2);
                        return List.of(
                            create(horDiff.get(0), this.vertical()),
                            create(horDiff.get(1), this.vertical()),
                            create(Segment.of(horDiff.get(0).to(), horDiff.get(1).from()), verDiff.get(0)),
                            create(Segment.of(horDiff.get(0).to(), horDiff.get(1).from()), verDiff.get(1))
                        );
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRectangle)) return false;
        AbstractRectangle<?, ?> that = (AbstractRectangle<?, ?>) o;
        return horizontal.equals(that.horizontal) && vertical.equals(that.vertical);
    }

    @Override
    public int hashCode() {
        int result = horizontal.hashCode();
        result = 31 * result + vertical.hashCode();
        return result;
    }

}
