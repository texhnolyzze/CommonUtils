package org.texhnolyzze.common;

import java.util.*;
import java.util.function.Predicate;

import static org.texhnolyzze.common.MathUtils.overlapping;
import static org.texhnolyzze.common.MathUtils.overlappingStart;

public class LongRectangle {

    public final long x;
    public final long y;
    public final long w;
    public final long h;

    public LongRectangle(final long x, final long y, final long w, final long h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean overlaps(final LongRectangle other) {
        final long overlappingw = overlapping(x, x + w, other.x, other.x + other.w);
        if (overlappingw <= 0) {
            return false;
        }
        final long overlappingh = overlapping(y, y + h, other.y, other.y + other.h);
        return overlappingh > 0;
    }

    public LongRectangle overlap(final LongRectangle other) {
        final long overlappingw = overlapping(x, x + w, other.x, other.x + other.w);
        if (overlappingw <= 0) {
            return null;
        }
        final long overlappingh = overlapping(y, y + h, other.y, other.y + other.h);
        if (overlappingh <= 0) {
            return null;
        }
        final long overlappingx = overlappingStart(x, x + w, other.x, other.x + other.w);
        final long overlappingy = overlappingStart(y, y + h, other.y, other.y + other.h);
        return new LongRectangle(
            overlappingx,
            overlappingy,
            overlappingw,
            overlappingh
        );
    }

    /**
     * Вычитает из данного прямоугольника другой прямоугольник
     * Если прямоугольники не пересекаются -- возвращает null
     * Если данный прямоугольник полностью лежит в другом -- возвращает пустой список
     * Если данный прямоугольник пересекается с другим -- возвращает все прямоугольники, входящие в состав данного,
     * которые не пересекаются с другим
     */
    public List<LongRectangle> subtract(final LongRectangle other) {
        if (other.contains(this)) {
            return Collections.emptyList();
        }
        if (!overlaps(other)) {
            return null;
        }
        if (this.contains(other)) {
            final List<LongRectangle> result = new ArrayList<>();
            result.add(
                new LongRectangle(
                    x,
                    y,
                    other.x - x,
                    h
                )
            );
            result.add(
                new LongRectangle(
                    other.x,
                    other.y + other.h,
                    other.w,
                    (y + h) - (other.y + other.h)
                )
            );
            result.add(
                new LongRectangle(
                    other.x,
                    y,
                    other.w,
                    other.y - y
                )
            );
            result.add(
                new LongRectangle(
                    other.x + other.w,
                    y,
                    (x + w) - (other.x + other.w),
                    h
                )
            );
            result.removeIf(Predicate.not(LongRectangle::valid));
            return result;
        } else {
            return subtract(overlap(other));
        }
    }

    /**
     * Вычитает из данного прямоугольника все, пересекающиеся с ним прямоугольника из переданной коллекции
     * В результирующем списке находятся те меньшие прямоугольники данного,
     * которые не пересекаются ни с одним другим из переданной коллекции
     */
    public List<LongRectangle> subtract(final Collection<? extends LongRectangle> others) {
        final List<LongRectangle> result = new ArrayList<>();
        result.add(this);
        for (final LongRectangle other : others) {
            List<LongRectangle> toAdd = Collections.emptyList();
            for (Iterator<LongRectangle> iter = result.iterator(); iter.hasNext(); ) {
                final LongRectangle subRectangle = iter.next();
                final List<LongRectangle> subtract = subRectangle.subtract(other);
                if (subtract == null) {
                    continue;
                }
                iter.remove();
                if (toAdd == Collections.<LongRectangle>emptyList()) {
                    toAdd = new ArrayList<>();
                }
                toAdd.addAll(subtract);
            }
            result.addAll(toAdd);
        }
        return result;
    }

    public boolean valid() {
        return w > 0 && h > 0;
    }

    public boolean contains(final LongRectangle other) {
        return x <= other.x &&
                   other.x + other.w <= x + w &&
                   y <= other.y &&
                   other.y + other.h <= y + h;
    }

    public long area() {
        return w * h;
    }

    /**
     * Возвращает минимальный ограничивающий прямоугольник данного и другого
     */
    public LongRectangle boundingRectangle(final LongRectangle other) {
        final long x1 = x;
        final long y1 = y;
        final long x2 = x + w;
        final long y2 = y + h;
        final long x3 = other.x;
        final long y3 = other.y;
        final long x4 = other.x + other.w;
        final long y4 = other.y + other.h;
        return new LongRectangle(
            Math.min(x1, x3),
            Math.min(y1, y3),
            Math.max(x2, x4) - Math.min(x1, x3),
            Math.max(y2, y4) - Math.min(y1, y3)
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LongRectangle rectangle = (LongRectangle) o;
        return x == rectangle.x && y == rectangle.y && w == rectangle.w && h == rectangle.h;
    }

    @Override
    public int hashCode() {
        int result = (int) (x ^ (x >>> 32));
        result = 31 * result + (int) (y ^ (y >>> 32));
        result = 31 * result + (int) (w ^ (w >>> 32));
        result = 31 * result + (int) (h ^ (h >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                   "x=" + x +
                   ", y=" + y +
                   ", w=" + w +
                   ", h=" + h +
                   '}';
    }

    public static LongRectangle of(final int x, final int y, final int w, final int h) {
        return new LongRectangle(x, y, w, h);
    }

}
