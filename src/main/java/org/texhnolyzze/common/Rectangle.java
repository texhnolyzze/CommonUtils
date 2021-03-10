package org.texhnolyzze.common;

import com.google.common.base.MoreObjects;

public class Rectangle<C extends Comparable<? super C>> extends AbstractRectangle<C, Rectangle<C>> {

    private Rectangle(Segment<C> horizontal, Segment<C> vertical) {
        super(horizontal, vertical);
    }

    @Override
    protected Rectangle<C> create(Segment<C> horizontal, Segment<C> vertical) {
        return new Rectangle<>(horizontal, vertical);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Rectangle.class.getSimpleName())
            .add("horizontal", horizontal())
            .add("vertical", vertical())
            .toString();
    }

    public static <C extends Comparable<? super C>> Rectangle<C> of(
        C horizontalFrom,
        C horizontalTo,
        C verticalFrom,
        C verticalTo
    ) {
        return of(Segment.of(horizontalFrom, horizontalTo), Segment.of(verticalFrom, verticalTo));
    }

    public static <C extends Comparable<? super C>> Rectangle<C> of(Segment<C> horizontal, Segment<C> vertical) {
        return new Rectangle<>(horizontal, vertical);
    }

}