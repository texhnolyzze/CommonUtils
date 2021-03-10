package org.texhnolyzze.common;

import com.google.common.base.Preconditions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class RectangleTest {

    @Test
    void testSplit() {
        Rectangle<Integer> rectangle = Rectangle.of(0, 5, 0, 5);
        List<List<Rectangle<Integer>>> split = rectangle.split(1, 1, Integer::sum);
        assertThat(split.stream().distinct()).flatExtracting(Function.identity()).hasSize(5 * 5);
        rectangle = Rectangle.of(0, 2, 0, 5);
        split = rectangle.split(1, 1, Integer::sum);
        assertThat(split.stream().distinct()).flatExtracting(Function.identity()).hasSize(10);
        split = rectangle.split(0, 1, Integer::sum);
        assertThat(split.stream().distinct()).flatExtracting(Function.identity()).hasSize(5);
    }

    @Test
    void testDiff() {
        Rectangle<Integer> rectangle = Rectangle.of(1, 4, 1, 4);
        assertThat(
            rectangle.diff(
                List.of(
                    Rectangle.of(2, 3, 2, 3)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(1, 2, 1, 4),
            Rectangle.of(2, 3, 1, 2),
            Rectangle.of(2, 3, 3, 4),
            Rectangle.of(3, 4, 1, 4)
        );
        rectangle = Rectangle.of(1, 3, 1, 3);
        assertThat(
            rectangle.diff(
                singletonList(
                    Rectangle.of(2, 4, 2, 4)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(1, 2, 1, 3),
            Rectangle.of(2, 3, 1, 2)
        );
        rectangle = Rectangle.of(1, 3, 1, 4);
        assertThat(
            rectangle.diff(
                singletonList(
                    Rectangle.of(0, 2, 2, 3)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(1, 2, 1, 2),
            Rectangle.of(1, 2, 3, 4),
            Rectangle.of(2, 3, 1, 4)
        );
        rectangle = Rectangle.of(1, 3, 1, 2);
        assertThat(
            rectangle.diff(
                List.of(
                    Rectangle.of(1, 2, 1, 2),
                    Rectangle.of(2, 3, 1, 2)
                )
            )
        ).isEmpty();
        assertThat(
            rectangle.diff(emptyList())
        ).containsExactly(
            rectangle
        );
        rectangle = Rectangle.of(1, 2, 1, 4);
        assertThat(
            rectangle.diff(
                singletonList(
                    Rectangle.of(0, 3, 2, 3)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(1, 2, 1, 2),
            Rectangle.of(1, 2, 3, 4)
        );
        rectangle = Rectangle.of(0, 418, 0, 643);
        assertThat(
            rectangle.diff(
                List.of(
                    Rectangle.of(56, 95, 294, 436),
                    Rectangle.of(68, 148, 188, 325)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(0, 56, 0, 643),
            Rectangle.of(56, 68, 0, 294),
            Rectangle.of(56, 95, 436, 643),
            Rectangle.of(68, 95, 0, 188),
            Rectangle.of(95, 148, 0, 188),
            Rectangle.of(95, 148, 325, 643),
            Rectangle.of(148, 418, 0, 643)
        );
        rectangle = Rectangle.of(0, 4, 0, 4);
        assertThat(
            rectangle.diff(
                List.of(
                    Rectangle.of(1, 2, 1, 2),
                    Rectangle.of(2, 3, 2, 3)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(0, 1, 0, 4),
            Rectangle.of(1, 2, 0, 1),
            Rectangle.of(1, 2, 2, 4),
            Rectangle.of(2, 3, 0, 2),
            Rectangle.of(2, 3, 3, 4),
            Rectangle.of(3, 4, 0, 4)
        );
        rectangle = Rectangle.of(0, 62, 0, 211);
        assertThat(
            rectangle.diff(
                List.of(
                    Rectangle.of(12, 17, 203, 219),
                    Rectangle.of(22, 22, 201, 260)
                )
            )
        ).containsExactlyInAnyOrder(
            Rectangle.of(0, 12, 0, 211),
            Rectangle.of(12, 17, 0, 203),
            Rectangle.of(17, 22, 0, 211),
            Rectangle.of(22, 62, 0, 211)
        );
    }

    @Test
    void testDiffRandom() {
        int numIters = 1000;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for (int i = 0; i < numIters; i++) {
            int bound = 100000;
            int horLeft = 0;
            int horRight = ThreadLocalRandom.current().nextInt(bound / 100, bound);
            int horLen = horRight - horLeft;
            int verBottom = 0;
            int verTop = ThreadLocalRandom.current().nextInt(bound / 100, bound);
            int verLen = verTop - verBottom;
            Rectangle<Integer> rectangle = Rectangle.of(horLeft, horRight, verBottom, verTop);
            int numOthers = ThreadLocalRandom.current().nextInt(0, 100);
            List<Rectangle<Integer>> others = new ArrayList<>(numOthers);
            for (int j = 0; j < numOthers; j++) {
                int otherHorLeft = randomBound(horLeft, horRight, horLen, numOthers);
                int otherHorRight = otherHorLeft + ThreadLocalRandom.current().nextInt(1, horLen / numOthers);
                int otherVerBottom = randomBound(verBottom, verTop, verLen, numOthers);
                int otherVerTop = otherVerBottom + ThreadLocalRandom.current().nextInt(1, verLen / numOthers);
                others.add(Rectangle.of(otherHorLeft, otherHorRight, otherVerBottom, otherVerTop));
            }
            List<Rectangle<Integer>> diff1 = rectangle.diff(others);
            others.sort(Comparator.comparing(rect -> rect.horizontal().from()));
            List<Rectangle<Integer>> diff2 = null;
            try {
                diff2 = executor.submit(() -> diffV2(rectangle, others)).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Assertions.fail("No exception expected", e);
                diffV2(rectangle, others);
            }
            assertThat(area(diff1)).isEqualTo(area(diff2));
        }
    }

    @Test
    void testDiff2() {
        int numIterations = 100000;
        for (int i = 0; i < numIterations; i++) {
            int bound = 10;
            int left = ThreadLocalRandom.current().nextInt(1, bound);
            int right = left + ThreadLocalRandom.current().nextInt(bound);
            int bot = ThreadLocalRandom.current().nextInt(1, bound);
            int top = bot + ThreadLocalRandom.current().nextInt(bound);
            int otherLeft = ThreadLocalRandom.current().nextInt(-5, bound);
            int otherRight = otherLeft + ThreadLocalRandom.current().nextInt(bound + 5);
            int otherBot = ThreadLocalRandom.current().nextInt(-5, bound);
            int otherTop = otherBot + ThreadLocalRandom.current().nextInt(bound + 5);
            Rectangle<Integer> rect = Rectangle.of(left, right, bot, top);
            Rectangle<Integer> otherRect = Rectangle.of(otherLeft, otherRight, otherBot, otherTop);
            List<Rectangle<Integer>> diff1 = rect.diff(singletonList(otherRect));
            List<Rectangle<Integer>> diff2 = rect.diff(otherRect);
            assertThat(area(diff1)).isEqualTo(area(diff2));
        }
    }

    private int randomBound(int targetFrom, int targetTo, int targetLen, int numOthers) {
        int otherHorLeft;
        if (ThreadLocalRandom.current().nextDouble() < 0.05) {
            otherHorLeft = ThreadLocalRandom.current().nextInt(-10, targetFrom + 10);
        } else if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            otherHorLeft = ThreadLocalRandom.current().nextInt(targetTo - 10, targetTo + 10);
        } else {
            otherHorLeft = targetFrom + ThreadLocalRandom.current().nextInt(targetLen / numOthers);
        }
        return otherHorLeft;
    }

    private List<Rectangle<Integer>> diffV2(Rectangle<Integer> target, List<Rectangle<Integer>> others) {
        Preconditions.checkState(!target.degenerate());
        List<Rectangle<Integer>> res = new ArrayList<>();
        BufferingIterator<Rectangle<Integer>> iter = Iterators.buffer(others.iterator());
        final DiffEvalCtx<Integer> ctx = new DiffEvalCtx<>();
        ctx.addPoint(target.horizontal().from(), target, false);
        ctx.addPoint(target.horizontal().to(), target, true);
        while (!ctx.points.isEmpty()) {
            Point<Integer> point = ctx.nextPoint();
            ctx.workset.removeIf(rect -> !rect.horizontal().contains(point.pnt));
            while (iter.hasNext()) {
                Rectangle<Integer> next = iter.next();
                if (!iter.prevReturnedBuffered()) {
                    if (next.degenerate() || !next.intersects(target))
                        continue;
                    if (next.contains(target))
                        return emptyList();
                    if (target.horizontal().contains(next.horizontal().from()) && next.horizontal().from().compareTo(point.pnt) != 0) {
                        ctx.addPoint(next.horizontal().from(), next, false);
                    }
                    if (target.horizontal().contains(next.horizontal().to())) {
                        if (next.horizontal().to().compareTo(point.pnt) == 0)
                            point.addRight(next);
                        else {
                            ctx.addPoint(next.horizontal().to(), next, true);
                        }
                    }
                }
                if (next.horizontal().contains(point.pnt)) {
                    ctx.workset.add(next);
                } else {
                    iter.buffer();
                    break;
                }
            }
            if (ctx.prevVerticalDiff != null) {
                Segment<Integer> hor = Segment.of(ctx.prevPoint.pnt, point.pnt);
                for (Segment<Integer> segment : ctx.prevVerticalDiff) {
                    res.add(
                        Rectangle.of(
                            hor,
                            segment
                        )
                    );
                }
            }
            List<Segment<Integer>> verticalDiff = target.vertical().diff(
                Iterators.map(
                    Iterators.filter(
                        ctx.workset.iterator(),
                        point::isNotRight
                    ),
                    Rectangle::vertical
                )
            );
            ctx.prevPoint = point;
            if (verticalDiff.isEmpty()) {
                ctx.prevVerticalDiff = null;
            } else {
                ctx.prevVerticalDiff = verticalDiff;
            }
        }
        return res;
    }

    private long area(List<Rectangle<Integer>> rectangles) {
        return rectangles.stream().mapToLong(this::area).sum();
    }

    private long area(Rectangle<Integer> rectangle) {
        int dx = rectangle.horizontal().to() - rectangle.horizontal().from();
        int dy = rectangle.vertical().to() - rectangle.vertical().from();
        return (long) dx * dy;
    }

    private static class DiffEvalCtx<C extends Comparable<? super C>> {

        Point<C> prevPoint;
        List<Segment<C>> prevVerticalDiff;
        final TreeMap<C, Point<C>> points = new TreeMap<>(Comparable::compareTo);
        final SortedSet<Rectangle<C>> workset = new TreeSet<>((rect1, rect2) -> {
            int cmp = rect1.vertical().from().compareTo(rect2.vertical().from());
            if (cmp == 0)
                return Integer.compare(rect1.hashCode(), rect2.hashCode());
            return cmp;
        });

        void addPoint(C pnt, Rectangle<C> rect, boolean isRight) {
            Point<C> p = points.computeIfAbsent(pnt, Point::new);
            if (isRight)
                p.addRight(rect);
        }

        Point<C> nextPoint() {
            return points.pollFirstEntry().getValue();
        }

    }

    private static class Point<C extends Comparable<? super C>> {

        final C pnt;
        Map<Rectangle<C>, Object> right;

        private Point(C pnt) {
            this.pnt = pnt;
        }

        void addRight(Rectangle<C> rect) {
            if (right == null)
                right = new IdentityHashMap<>(1);
            right.put(rect, rect);
        }

        boolean isNotRight(Rectangle<C> rect) {
            return right == null || !right.containsKey(rect);
        }

    }

}