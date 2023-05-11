package org.texhnolyzze.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
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

    public boolean contains(E e) {
        return parentOf.get(e) != null;
    }

    public int numOfSets() {
        return n;
    }

    public void makeSet(E e) {
        if (e == null) {
            return;
        }
        parentOf.computeIfAbsent(
            e,
            unused -> {
                sizeOf.put(e, 1);
                n++;
                return e;
            }
        );
    }

    public void union(E e1, E e2) {
        E representative1 = find(e1);
        if (representative1 != null) {
            E representative2 = find(e2);
            if (representative2 != null && representative1 != representative2) {
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

    public E find(E e) {
        E temp1 = e;
        E temp2 = parentOf.get(temp1);
        if (temp2 == null) {
            return null;
        }
        while (temp1 != temp2) {
            temp1 = temp2;
            temp2 = parentOf.get(temp2);
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
            temp2 = parentOf.get(temp2);
        }
        return representative;
    }

    public boolean connected(E e1, E e2) {
        E representative1 = find(e1);
        if (representative1 != null) {
            E representative2 = find(e2);
            if (representative2 != null) {
                return representative1 == representative2;
            }
        }
        return false;
    }

    public void remove(E e) {
        E parent = parentOf.get(e);
        if (parent == null) {
            return;
        }
        if (sizeOf.get(e) == 1) {
            removeLeaf(e, parent);
        } else if (parent == e) {
            removeRepresentative(e);
        } else {
            removeInnerNode(e, parent);
        }
    }

    private void removeLeaf(E e, E parent) {
        decreaseSize(parent);
        sizeOf.remove(e);
        parentOf.remove(e);
    }

    private void removeRepresentative(E e) {
        parentOf.remove(e);
        List<E> children = getChildrenOf(e);
        E newRepresentative = children.remove(ThreadLocalRandom.current().nextInt(children.size()));
        parentOf.put(newRepresentative, newRepresentative);
        sizeOf.put(newRepresentative, sizeOf.remove(e) - 1);
        for (E child : children) {
            sizeOf.put(child, 1);
            parentOf.put(child, newRepresentative);
        }
    }

    private void removeInnerNode(E e, E parent) {
        decreaseSize(parent);
        List<E> children = getChildrenOf(e);
        sizeOf.remove(e);
        parentOf.remove(e);
        for (E child : children) {
            sizeOf.put(child, 1);
            parentOf.put(child, parent);
        }
    }

    private List<E> getChildrenOf(E e) {
        List<E> children = new ArrayList<>();
        for (Map.Entry<E, E> entry : parentOf.entrySet()) {
            E parent = entry.getValue();
            if (parent == e) {
                E child = entry.getKey();
                children.add(child);
            }
        }
        return children;
    }

    private void decreaseSize(E parent) {
        E temp = parent;
        do {
            sizeOf.put(temp, sizeOf.get(temp) - 1);
            temp = parentOf.get(temp);
        } while (temp != parent);
    }

}
