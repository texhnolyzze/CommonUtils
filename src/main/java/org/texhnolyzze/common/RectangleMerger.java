package org.texhnolyzze.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RectangleMerger {

    private RectangleMerger() {
        throw new UnsupportedOperationException();
    }

    /**
     * Сливает пересекающиеся прямоугольники в один большой прямоугольник, содержащий все остальные прямоугольники (ограничивающий прямоугольник).
     * {@code consumer} принимает список прямоугольников и их ограничивающий прямоугольник.
     * Он должен модифицировать исходный {@code rectangles} (удалить из него все прямоугольники из списка и добавить ограничивающий).
     * Для лучшей производительности {@code rectangles} должен быть отсортирован по левой стороне прямоугольника.
     */
    public static <C extends Comparable<? super C>, R extends AbstractRectangle<C, R>> void merge(
        Iterable<? extends R> rectangles,
        BiConsumer<List<R>, R> consumer
    ) {
        C horMin;
        C horMax;
        C verMin;
        C verMax;
        List<R> temp = new ArrayList<>();
        while (true) {
            horMin = null;
            horMax = null;
            verMin = null;
            verMax = null;
            for (R rectangle : rectangles) {
                if (horMin == null) {
                    horMin = rectangle.horizontal().from();
                    horMax = rectangle.horizontal().to();
                    verMin = rectangle.vertical().from();
                    verMax = rectangle.vertical().to();
                    temp.add(rectangle);
                } else {
                    if (rectangle.intersects(horMin, horMax, verMin, verMax)) {
                        temp.add(rectangle);
                        horMin = ComparableUtils.min(horMin, rectangle.horizontal().from());
                        horMax = ComparableUtils.max(horMax, rectangle.horizontal().to());
                        verMin = ComparableUtils.min(verMin, rectangle.vertical().from());
                        verMax = ComparableUtils.max(verMax, rectangle.vertical().to());
                    }
                }
            }
            if (temp.size() > 1) {
                consumer.accept(temp, temp.get(0).create(Segment.of(horMin, horMax), Segment.of(verMin, verMax)));
                temp.clear();
            } else
                return;
        }
    }

}
