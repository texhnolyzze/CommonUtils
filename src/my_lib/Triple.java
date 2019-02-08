package my_lib;

/**
 *
 * @author Texhnolyze
 */
public class Triple<X, Y, Z> {
    
    private final X x;
    private final Y y;
    private final Z z;

    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public X x() {
        return x;
    }
    
    public Y y() {
        return y;
    }
    
    public Z z() {
        return z;
    }

    @Override
    public String toString() {
        return "(x=" + x + ", y=" + y + ", z=" + z + ")";
    }
    
}
