package my_lib;

import java.util.Objects;

/**
 *
 * @author Texhnolyze
 */
public class Pair<X, Y> {
    
    private final X x;
    private final Y y;
    
    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }
    
    public X x() {
        return x;
    }
    
    public Y y() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.x);
        hash = 47 * hash + Objects.hashCode(this.y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.x, other.x)) return false;
        return Objects.equals(this.y, other.y);
    }
    
}
