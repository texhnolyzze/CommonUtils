package org.texhnolyzze.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class SegmentTest {

    @Test
    void testIntersects() {
        Segment<Integer> segment = Segment.of(1, 10);
        assertThat(segment.intersects(Segment.of(3, 5))).isTrue();
        assertThat(segment.intersects(Segment.of(1, 10))).isTrue();
        assertThat(segment.intersects(Segment.of(-5, 1))).isTrue();
        assertThat(segment.intersects(Segment.of(0, 11))).isTrue();
        assertThat(segment.intersects(Segment.of(11, 15))).isFalse();
        assertThat(segment.intersects(Segment.of(10, 10))).isTrue();
        assertThat(segment.intersects(Segment.of(-5, 10))).isTrue();
    }

    @Test
    void testDiff() {
        Segment<Integer> segment = Segment.of(1, 10);
        assertThat(segment.diff(List.of(Segment.of(11, 15)))).containsExactly(segment);
        assertThat(segment.diff(List.of(Segment.of(3, 5)))).containsExactly(Segment.of(1, 3), Segment.of(5, 10));
        assertThat(segment.diff(List.of(Segment.of(1, 5)))).containsExactly(Segment.of(5, 10));
        assertThat(segment.diff(List.of(Segment.of(1, 10)))).isEmpty();
        assertThat(segment.diff(List.of(Segment.of(2, 6), Segment.of(3, 5)))).containsExactly(Segment.of(1, 2), Segment.of(6, 10));
        assertThat(segment.diff(List.of(Segment.of(2, 5), Segment.of(3, 6)))).containsExactly(Segment.of(1, 2), Segment.of(6, 10));
        assertThat(segment.diff(List.of(Segment.of(2, 4), Segment.of(6, 8)))).containsExactly(Segment.of(1, 2), Segment.of(4, 6), Segment.of(8, 10));
        assertThat(segment.diff(List.of(Segment.of(0, 1), Segment.of(1, 5), Segment.of(3, 11)))).isEmpty();
        assertThat(segment.diff(List.of(Segment.of(2, 9)))).containsExactly(Segment.of(1, 2), Segment.of(9, 10));
        assertThat(segment.diff(List.of(Segment.of(1, 2), Segment.of(2, 3), Segment.of(6, 7), Segment.of(9, 11)))).containsExactly(Segment.of(3, 6), Segment.of(7, 9));
        assertThat(segment.diff(List.of(Segment.of(0, 3)))).containsExactly(Segment.of(3, 10));
        assertThat(segment.diff(List.of(Segment.of(-1, 5), Segment.of(6, 7), Segment.of(10, 11)))).containsExactly(Segment.of(5, 6), Segment.of(7, 10));
        assertThat(segment.diff(List.of(Segment.of(1, 2), Segment.of(1, 4), Segment.of(1, 3), Segment.of(4, 5), Segment.of(1, 7), Segment.of(7, 9)))).containsExactly(Segment.of(9, 10));
        assertThat(segment.diff(List.of(Segment.of(1, 2), Segment.of(2, 2), Segment.of(2, 3), Segment.of(3, 4), Segment.of(4, 5)))).containsExactly(Segment.of(5, 10));
        assertThat(segment.diff(List.of(Segment.of(-1, 2), Segment.of(3, 4)))).containsExactly(Segment.of(2, 3), Segment.of(4, 10));
        segment = Segment.of(0, 20);
        assertThat(
            segment.diff(
                List.of(
                    Segment.of(1, 3),
                    Segment.of(2, 6),
                    Segment.of(4, 7),
                    Segment.of(8, 11),
                    Segment.of(9, 10),
                    Segment.of(12, 21)
                )
            )
        ).containsExactly(Segment.of(0, 1), Segment.of(7, 8), Segment.of(11, 12));
    }

    @Test
    void testSplit() {
        Segment<Integer> segment = Segment.of(1, 5);
        List<Segment<Integer>> split = segment.split(1, Integer::sum);
        assertThat(split).containsExactly(Segment.of(1, 2), Segment.of(2, 3), Segment.of(3, 4), Segment.of(4, 5));
    }

    @Test
    void testDiffRandom() {
        int numIterations = 1000;
        for (int i = 0; i < numIterations; i++) {
            int bound = 1000;
            int left = ThreadLocalRandom.current().nextInt(1, bound);
            int right = left + ThreadLocalRandom.current().nextInt(bound);
            Segment<Integer> segment = Segment.of(left, right);
            int numOthers = 100;
            for (int j = 0; j < numOthers; j++) {
                List<Integer> leftPoints = new ArrayList<>();
                List<Integer> rightPoints = new ArrayList<>();
                List<Segment<Integer>> others = new ArrayList<>(numOthers);
                for (int k = left - 30; k < right - bound / 4; k++) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.005) {
                        leftPoints.add(k);
                    }
                }
                outer: for (Integer point : leftPoints) {
                    for (int k = point; k < right + bound / 4; k++) {
                        if (ThreadLocalRandom.current().nextDouble() < 0.005) {
                            rightPoints.add(k);
                            continue outer;
                        }
                    }
                    rightPoints.add(point + 1);
                }
                for (int k = 0; k < leftPoints.size(); k++) {
                    others.add(Segment.of(leftPoints.get(k), rightPoints.get(k)));
                }
                List<Segment<Integer>> diff1 = segment.diff(others);
                List<Segment<Integer>> diff2 = naiveDiff(segment, others);
                if (!diff1.equals(diff2)) {
                    Assertions.fail("diff1 != diff2");
                    naiveDiff(segment, others);
                    segment.diff(others);
                }
            }
        }
    }

    @Test
    void testDiff2() {
        int numIterations = 100000;
        for (int i = 0; i < numIterations; i++) {
            int bound = 1000;
            int left = ThreadLocalRandom.current().nextInt(1, bound);
            int right = left + ThreadLocalRandom.current().nextInt(bound);
            int otherLeft = ThreadLocalRandom.current().nextInt(-5, bound);
            int otherRight = otherLeft + ThreadLocalRandom.current().nextInt(bound + 5);
            Segment<Integer> seg = Segment.of(left, right);
            Segment<Integer> otherSeg = Segment.of(otherLeft, otherRight);
            List<Segment<Integer>> diff1 = seg.diff(singletonList(otherSeg));
            List<Segment<Integer>> diff2 = seg.diff(otherSeg);
            assertThat(diff1).isEqualTo(diff2);
        }
    }

    private List<Segment<Integer>> naiveDiff(Segment<Integer> target, List<Segment<Integer>> others) {
        List<Segment<Integer>> result = new ArrayList<>();
        List<Segment<Integer>> temp = new ArrayList<>();
        result.add(target);
        for (Segment<Integer> other : others) {
            ListIterator<Segment<Integer>> iter = result.listIterator();
            while (iter.hasNext()) {
                Segment<Integer> next = iter.next();
                iter.remove();
                if (other.intersects(next)) {
                    List<Segment<Integer>> diff = next.diff(other);
                    temp.addAll(diff);
                } else
                    temp.add(next);
            }
            result.addAll(temp);
            temp.clear();
        }
        return result;
    }

}