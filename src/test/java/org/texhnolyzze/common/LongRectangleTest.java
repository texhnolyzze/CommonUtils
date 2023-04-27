package org.texhnolyzze.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LongRectangleTest {

    @Test
    void testSubtract() {
        assertThat(
            LongRectangle.of(0, 0, 5, 5).subtract(
                LongRectangle.of(1, 1, 2, 2)
            )
        ).containsExactlyInAnyOrder(
            LongRectangle.of(0, 0, 1, 5),
            LongRectangle.of(1, 0, 2, 1),
            LongRectangle.of(1, 3, 2, 2),
            LongRectangle.of(3, 0, 2, 5)
        );
        assertThat(LongRectangle.of(0, 0, 5, 5).subtract(LongRectangle.of(5, 5, 5, 5))).isNull();
        assertThat(LongRectangle.of(0, 0, 5, 5).subtract(LongRectangle.of(0, 0, 10, 10))).isEmpty();
        assertThat(LongRectangle.of(0, 0, 5, 5).subtract(LongRectangle.of(-1, 1, 7, 1))).containsExactlyInAnyOrder(
            LongRectangle.of(0, 0, 5, 1),
            LongRectangle.of(0, 2, 5, 3)
        );
    }

    @Test
    void testBoundingRectangle() {
        assertThat(
            LongRectangle.of(0, 0, 5, 5).boundingRectangle(
                LongRectangle.of(0, 0, 1, 1)
            )
        ).isEqualTo(LongRectangle.of(0, 0, 5, 5));
        assertThat(
            LongRectangle.of(0, 0, 5, 5).boundingRectangle(
                LongRectangle.of(1, 1, 5, 5)
            )
        ).isEqualTo(LongRectangle.of(0, 0, 6, 6));
    }

}
