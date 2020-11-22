package lib;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import java.util.function.Consumer;

/**
 *
 * @author Texhnolyze
 */
public final class MathUtils {
    
    private MathUtils() {}
    
    public static int to2DArrayHash(int columnIndex, int lineIndex, int columnsNum) {
        return columnIndex + lineIndex * columnsNum;
    }

    public static int to2DArrayColumnIndex(int hash, int columnsNum) {
        return hash % columnsNum;
    }

    public static int to2DArrayLineIndex(int hash, int columnsNum) {
        return hash / columnsNum;
    }
	
    public static int pow(int a, int b) {
        int res = 1, exp = b;
        while (exp != 0) {
            if ((exp & 1) != 0)
                res *= a;
            exp >>= 1;
            if (exp == 0)
                break;
            a *= a;
        }
        return res;
    }
    
    public static long pow(long a, long b) {
        long res = 1, exp = b;
        while (exp != 0) {
            if ((exp & 1) != 0)
                res *= a;
            exp >>= 1;
            if (exp == 0)
                break;
            a *= a;
        }
        return res;
    }
    
//  stolen from: https://gist.github.com/leodutra/63ca94fe86dcffee1bab
    public static int min(int a, int b) {
        return a - ((a - b) & ((b - a) >> 31));
    }
    
    public static int max(int a, int b) {
        return -min(-a, -b);
    }
    
    public static int abs(int a) {
        return (a ^ (a >> 31)) + ((a >> 31) & 1);
    }
    
    public static long min(long a, long b) {
        return a - ((a - b) & ((b - a) >> 63));
    }
    
    public static long max(long a, long b) {
        return -min(-a, -b);
    }
    
    public static long abs(long a) {
        return (a ^ (a >> 63)) + ((a >> 63) & 1);
    }
    
    public static double log(double a, double base) {
        return Math.log(a) / Math.log(base);
    }
    
    public static void goThroughAllDecimalCodes(int numDigits, Consumer<int[]> handler) {
        final int[] code = new int[numDigits];
        final int[] pow = new int[numDigits];
        for (int i = 0; i < numDigits; i++) 
            pow[i] = pow(10, i);
        for (int i = 0; i < 10 * pow[numDigits - 1]; i++) {
            for (int j = 0; j < numDigits; j++) {
                code[numDigits - j - 1] = (i / pow[j]) % 10;
            }
            handler.accept(code);
        }
    }
    
    public static void getDecimalCode(int code, int numDigits, int[] dest) {
        getDecimalCode(code, numDigits, dest, 0);
    }
    
    public static void getDecimalCode(int code, int numDigits, int[] dest, int recordFromIndex) {
        if (recordFromIndex + numDigits > dest.length)
            throw new IllegalArgumentException();
        int pow = 1;
        for (int i = 0; i < numDigits; i++) {
            dest[recordFromIndex + numDigits - i - 1] = (code / pow) % 10;
            pow *= 10;
        }
    }
    
    public static class WGS84 {
    
        public static final double EARTH_RADIUS_METERS = 6371000;

//      args in degrees, 0.5% error for Earth
        public static double getDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
            assertWGS84(lat1, lon1);
            assertWGS84(lat2, lon2);
            double teta1 = toRadians(lat1), teta2 = toRadians(lat2);
            double phi1 = toRadians(lon1), phi2 = toRadians(lon2);
            return EARTH_RADIUS_METERS * acos(sin(teta1) * sin(teta2) + cos(teta1) * cos(teta2) * cos(phi1 - phi2));
        }

        public interface TetraDoubleConsumer {
            void consume(double d1, double d2, double d3, double d4);
        }

        public static void packSphereRectangleWithSphereRectangles(double lat1, double lon1, double lat2, double lon2, double d_lat, double d_lon, TetraDoubleConsumer consumer) {
            assertWGS84(lat1, lon1);
            assertWGS84(lat2, lon2);
            assertPositive(d_lat, "d_lat");
            assertPositive(d_lon, "d_lon");
            assertSphericalRectanle(lat1, lon1, lat2, lon2);
            double lat = lat1;
            for (int i = 0; i < (int) Math.ceil((lat1 - lat2) / d_lat); i++) {
                double lon = lon1;
                for (int j = 0; j < (int) Math.ceil((lon2 - lon1) / d_lon); j++) {
                    consumer.consume(lat, lon, Math.max(lat2, lat - d_lat), Math.min(lon2, lon + d_lon));
                    lon += d_lon;
                }
                lat -= d_lat;
            }
        }

        public interface BiDoubleConsumer {
            void consume(double d1, double d2);
        }

//      solution found at: https://math.stackexchange.com/questions/327699/discretize-a-circle-on-a-sphere-with-a-given-center-and-radius
        public static void discretizeCircleOnEarthSurface(double lat, double lon, double radius_meters, int angle_sample_size, BiDoubleConsumer consumer) {
            assertWGS84(lat, lon);
            assertPositive(radius_meters, "radius");
            assertPositive(angle_sample_size, "angle_sample_size");
            vec3 temp1 = new vec3(), temp2 = new vec3(), temp3 = new vec3();
            vec3 xc = p(toRadians(lon), toRadians(lat), new vec3());
            vec3 p1 = p(toRadians(lon), toRadians(lat) + 0.5 * PI, new vec3()).normLocal();
            vec3 p2 = vec3.cross(temp1.set(xc).normLocal(), p1, new vec3());
            double a = radius_meters / EARTH_RADIUS_METERS;
            double lambda = 1.0 - 0.5 * (a * a);
            double d_perp = EARTH_RADIUS_METERS * sqrt(1.0 - lambda * lambda);
            xc.scaleLocal(lambda);
            p1.scaleLocal(d_perp);
            p2.scaleLocal(d_perp);
            double d_alpha = 2 * PI / angle_sample_size;
            for (int k = 0; k < angle_sample_size; k++) {
                double alpha = k * d_alpha;
                double p1_mul = cos(alpha);
                double p2_mul = sin(alpha);
                temp3.set(xc).addLocal(temp1.set(p1).scaleLocal(p1_mul)).addLocal(temp2.set(p2).scaleLocal(p2_mul));
                consumer.consume(toDegrees(asin(temp3.z / EARTH_RADIUS_METERS)), toDegrees(-atan2(temp3.y, temp3.x)));
            }
        }

        private static vec3 p(double teta, double phi, vec3 dest) {
            return dest.set(EARTH_RADIUS_METERS * (cos(phi) * cos(teta)),
                EARTH_RADIUS_METERS * -(cos(phi) * sin(teta)),
                EARTH_RADIUS_METERS * sin(phi)
            );
        }

    //  returns the coords (double array) where (coords[0], coords[1]) -- upper left coordinate and (coords[2], coords[3]) -- lower right coordinate in format (lat, lon).
    //  This coords represent minimum bounding spherical rectangle for circle on the sphere
        public static double[] getCircleBoundingRectangle(double center_lat, double center_lon, double radius_meters) {
            assertWGS84(center_lat, center_lon);
            assertPositive(radius_meters, "radius");
//                                max_lat(0)                min_lon(1)              min_lat(2)               max_lon(3)
            double[] res = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
            discretizeCircleOnEarthSurface(center_lat, center_lon, radius_meters, 360 * 360, (lat, lon) -> {
                res[0] = Math.max(res[0], lat);
                res[1] = Math.min(res[1], lon);
                res[2] = Math.min(res[2], lat);
                res[3] = Math.max(res[3], lon);
            });
            return res;
        }

        private static void assertWGS84(double lat, double lon) {
            if (Math.abs(lat) > 90.0 || Math.abs(lon) > 180.0)
                throw new IllegalArgumentException("Not WGS84 coordinates. LAT: " + lat + ", LON: " + lon);
        }

        private static void assertPositive(double d, String name) {
            if (d <= 0)
                throw new IllegalArgumentException(name + " must be positive. " + name + ": " + d);
        }

        private static void assertSphericalRectanle(double lat1, double lon1, double lat2, double lon2) {
            if (lat2 >= lat1)
                throw new IllegalArgumentException("Latitude1 must be > Latitude2");
            if (lon2 <= lon1)
                throw new IllegalArgumentException("Longitude2 must be > Longitude1");
        }
    }
    
    public final static class vec3 {
        
        private double x, y, z;
        
        public vec3() {}
        public vec3(double x, double y, double z) {
            set(x, y, z);
        }
        
        public vec3 set(vec3 v) {return set(v.x, v.y, v.z);}
        public vec3 set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
        
        public vec3 addLocal(vec3 v) {return addLocal(v.x, v.y, v.z);}
        public vec3 addLocal(double x, double y, double z) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }
        
        public double len() {
            return Math.sqrt(x * x + y * y + z * z);
        }
        
        public vec3 normLocal() {
            double len = len();
            return set(x / len, y / len, z / len);
        }
        
        public vec3 scaleLocal(double d) {
            return set(x * d, y * d, z * d);
        }
        
        public static vec3 cross(vec3 a, vec3 b, vec3 dest) {
            return dest.set(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
            );
        }
        
    }
    
    
}
