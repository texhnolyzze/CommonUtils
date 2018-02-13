package lib;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Texhnolyze
 */
public class UFS<E> {
    
    private final Map<E, E> parentOf;
    private final Map<E, Integer> sizeOf;

    private int n;

    public UFS() {
        parentOf = new HashMap<>();
        sizeOf = new HashMap<>();
    }
    
    public void clear() {
        parentOf.clear();
        sizeOf.clear();
        n = 0;
    }

    public int getNumOfSets() {
        return n;
    }

    public void makeSet(E e) {
        if (!parentOf.containsKey(e)) {
            parentOf.put(e, e);
            sizeOf.put(e, 1);
            n++;
        }
    }

    public void union(E e1, E e2) {
        E representative1 = find(e1);
        if (representative1 != null) {
            E representative2 = find(e2);
            if (representative2 != null) {
                if (representative1 != representative2) {
                    int size1 = sizeOf.get(representative1);
                    int size2 = sizeOf.get(representative2);
                    if (size1 < size2) {
                        parentOf.put(representative1, representative2);
                        sizeOf.put(representative2, size1 + size2);
                    } else {
                        parentOf.put(representative2, representative1);
                        sizeOf.put(representative1, size1 + size2);
                    }
                    n--;
                }
            }
        }
    }

    public E find(E e) {
        if (!parentOf.containsKey(e)) return null;

        E temp1 = e;
        E temp2 = parentOf.get(temp1);

        for (; temp1 != temp2; temp1 = temp2, temp2 = parentOf.get(temp1));

        E representative = temp1;

        temp1 = e;
        temp2 = parentOf.get(temp1);

        for (int n = 0; temp2 != representative; temp1 = temp2, temp2 = parentOf.get(temp1)) {
            n += sizeOf.get(temp1);
            parentOf.put(temp1, representative);
            sizeOf.put(temp2, sizeOf.get(temp2) - n);
        }

        return representative;
    }

    public boolean connected(E e1, E e2) {
        E r1 = find(e1);
        if (r1 != null) {
            E r2 = find(e2);
            if (r2 != null) return r1 == r2;
        }
        return false;
    }
    
}
