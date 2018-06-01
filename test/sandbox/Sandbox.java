package sandbox;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import lib.CollectionBinaryOperation;


public class Sandbox {

    public static void main(String[] args) {
        System.out.println(sqrt(4));
    }
    
    static double newton(Function<Double, Double> f, double eps) {
        Function<Double, Double> df = (Double y) -> {
            return (f.apply(y + eps) - f.apply(y)) / eps;
        };
        double x0 = 1.0;
        while (true) {
            x0 = x0 - f.apply(x0) / df.apply(x0);
            if (f.apply(x0) < eps)
                return x0;
        }
    }
    
    static double sqrt(double x) {
        Function<Double, Double> f = (Double y) -> {
            return y * y - x;
        };
        return newton(f, 0.000001);
    }
    
}
