package lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        E temp1 = e;
        E temp2 = parentOf.get(temp1);
        if (temp2 == null) 
            return null;
        while (temp1 != temp2) {
            temp1 = temp2;
            temp2 = parentOf.get(temp1);
        }
        E representative = temp1;
        temp1 = e;
        temp2 = parentOf.get(temp1);
        int n = 0;
        while (temp2 != representative) {
            n += sizeOf.get(temp1);
            parentOf.put(temp1, representative);
            sizeOf.put(temp2, sizeOf.get(temp2) - n);
            temp1 = temp2;
            temp2 = parentOf.get(temp1);
        }
        return representative;
    }

    public boolean connected(E e1, E e2) {
        E representative1 = find(e1);
        if (representative1 != null) {
            E representative2 = find(e2);
            if (representative2 != null) 
                return representative1 == representative2;
        }
        return false;
    }
    
    public void remove(E e) {
        if (sizeOf.get(e) == 1)
            removeLeaf(e);
        else if (parentOf.get(e) == e)
            removeRepresentative(e);
        else 
            removeInnerNode(e);
    }

    private void removeLeaf(E e) {
        sizeOf.remove(e);
        parentOf.remove(e);
    }

    private void removeRepresentative(E e) {
        parentOf.remove(e);
        List<E> childs = getChildsOf(e);
        E newRepresentative = childs.remove((int) (Math.random() * childs.size()));
        parentOf.put(newRepresentative, newRepresentative);
        sizeOf.put(newRepresentative, sizeOf.remove(e) - 1);
        for (E child : childs) 
            parentOf.put(child, newRepresentative);
    }

    private void removeInnerNode(E e) {
        List<E> childs = getChildsOf(e);
        E parent = parentOf.get(e);
        sizeOf.put(parent, sizeOf.get(parent) - 1);
        sizeOf.remove(e);
        parentOf.remove(e);
        for (E child : childs)
            parentOf.put(child, parent);
    }
    
    private List<E> getChildsOf(E e) {
        List<E> childs = new ArrayList<>();
        Iterator<Map.Entry<E, E>> it = parentOf.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<E, E> entry = it.next();
            E parent = entry.getValue();
            if (parent == e) {
                E child = entry.getKey();
                childs.add(child);
            }
        }
        return childs;
    }
    
}
