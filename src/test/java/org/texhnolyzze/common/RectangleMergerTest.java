package org.texhnolyzze.common;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class RectangleMergerTest {

    @Test
    void testMerge() {
        List<Rectangle<Integer>> rectangles = new LinkedList<>(
            List.of(
                Rectangle.of(1, 4, 1, 4),
                Rectangle.of(1, 5, 1, 5)
            )
        );
        RectangleMerger.merge(rectangles, consumer(rectangles));
        assertThat(rectangles).containsExactly(Rectangle.of(1, 5, 1, 5));
        rectangles = new LinkedList<>(
            List.of(
                Rectangle.of(1, 3, 1, 3),
                Rectangle.of(3, 6, 3, 6)
            )
        );
        RectangleMerger.merge(rectangles, consumer(rectangles));
        assertThat(rectangles).containsExactly(Rectangle.of(1, 6, 1, 6));
    }

    private BiConsumer<List<Rectangle<Integer>>, Rectangle<Integer>> consumer(List<Rectangle<Integer>> rectangles) {
        return (rects, result) -> {
            rectangles.removeAll(rects);
            rectangles.add(result);
        };
    }

}