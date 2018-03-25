package lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Texhnolyze
 */
public enum SetBinaryOperation {
    
    UNION {

        @Override 
        int size(Set a, Set b) {
            int size = a.size() + b.size();
            for (Object obj : a) {
                if (b.contains(obj))
                    size--;
            }
            return size;
        }

        @Override 
        boolean contains(Set a, Set b, Object obj) {
            return a.contains(obj) || b.contains(obj);
        }

        @Override
        Object[] toArray(Set a, Set b) {
            int i = 0;
            Object[] arr = new Object[size(a, b)];
            for (Object obj : a) {arr[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) arr[i++] = obj;}
            return arr;
        }

        @Override
        Object next(Set a, Set b, Iterator ait, Iterator bit) {
            if (ait.hasNext())
                return ait.next();
            while (bit.hasNext()) {
                Object obj = bit.next();
                if (!a.contains(obj))
                    return obj;
            }
            return null;
        }

    }, INTERSECTION {

        @Override
        int size(Set a, Set b) {
            int size = 0;
            for (Object obj : a) {
                if (b.contains(obj))
                    size++;
            }
            return size;
        }

        @Override
        boolean contains(Set a, Set b, Object obj) {
            return a.contains(obj) && b.contains(obj);
        }

        @Override
        Object[] toArray(Set a, Set b) {
            int i = 0;
            Object[] arr = new Object[size(a, b)];
            for (Object obj : a) {if (b.contains(obj)) arr[i++] = obj;}
            return arr;
        }

        @Override
        Object next(Set a, Set b, Iterator ait, Iterator bit) {
            while (ait.hasNext()) {
                Object obj = ait.next();
                if (b.contains(obj))
                    return obj;
            }
            return null;
        }

    }, DIFFERENCE {

        @Override
        int size(Set a, Set b) {
            int size = a.size();
            for (Object obj : a) {
                if (b.contains(obj))
                    size--;
            }
            return size;
        }

        @Override
        boolean contains(Set a, Set b, Object obj) {
            return a.contains(obj) && !b.contains(obj);
        }

        @Override
        Object[] toArray(Set a, Set b) {
            int i = 0;
            Object[] arr = new Object[size(a, b)];
            for (Object obj : a) {if (!b.contains(obj)) arr[i++] = obj;}
            return arr;
        }

        @Override
        Object next(Set a, Set b, Iterator ait, Iterator bit) {
            while (ait.hasNext()) {
                Object obj = ait.next();
                if (!b.contains(obj))
                    return obj;
            }
            return null;
        }

    }, SYMMETRIC_DIFFERENCE {

        @Override
        int size(Set a, Set b) {
            int ab = DIFFERENCE.size(a, b);
            int ba = DIFFERENCE.size(b, a);
            return ab + ba;
        }

        @Override
        boolean contains(Set a, Set b, Object obj) {
            return DIFFERENCE.contains(a, b, obj) || DIFFERENCE.contains(b, a, obj);
        }

        @Override
        Object[] toArray(Set a, Set b) {
            int i = 0;
            Object[] arr = new Object[size(a, b)];
            for (Object obj : a) {if (!b.contains(obj)) arr[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) arr[i++] = obj;}
            return arr;
        }

        @Override
        Object next(Set a, Set b, Iterator ait, Iterator bit) {
            while (ait.hasNext()) {
                Object obj = ait.next();
                if (!b.contains(obj))
                    return obj;
            }
            while (bit.hasNext()) {
                Object obj = bit.next();
                if (!a.contains(obj))
                    return obj;
            }
            return null;
        }

    };

    abstract int size(Set a, Set b);
    abstract boolean contains(Set a, Set b, Object obj);
    abstract Object[] toArray(Set a, Set b);
    abstract Object next(Set a, Set b, Iterator ait, Iterator bit);

    Iterator iterator(Set a, Set b) {
        return new Iterator() {
            
            Iterator ait = a.iterator();
            Iterator bit = b.iterator();
            Object next = SetBinaryOperation.this.next(a, b, ait, bit);

            
            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Object next() {
                Object obj = next;
                next = SetBinaryOperation.this.next(a, b, ait, bit);
                return obj;
            }
            
        };
    }
    
    public <E> Set<E> constructNewSet(Set<E> a, Set<E> b) {
        E[] arr = (E[]) toArray(a, b);
        return new HashSet<>(Arrays.asList(arr));
    }
    
    public <E> Set<E> delegate(Set<E> a, Set<E> b) {
        return new SetDelegator<>(a, b, this);
    }
        
    private static class SetDelegator<T> implements Set<T> {
        
        private SetBinaryOperation op;
        private Set<T> a, b;
        
        private SetDelegator(Set<T> a, Set<T> b, SetBinaryOperation op) {
            this.a = a;
            this.b = b;
            this.op = op;
        }
        
        @Override
        public int size() {
            return op.size(a, b);
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            return op.contains(a, b, o);
        }
        
        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object obj : c) {
                if (!contains(obj))
                    return false;
            }
            return true;
        }

        @Override
        public Iterator<T> iterator() {
            return op.iterator(a, b);
        }

        @Override
        public Object[] toArray() {
            return op.toArray(a, b);
        }
        
        @Override public void clear() {throw new UnsupportedOperationException("Not supported.");}
        @Override public boolean add(T e) {throw new UnsupportedOperationException("Not supported.");}
        @Override public boolean addAll(Collection<? extends T> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public boolean remove(Object o) {throw new UnsupportedOperationException("Not supported.");}
        @Override public boolean removeAll(Collection<?> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public <T> T[] toArray(T[] a) {throw new UnsupportedOperationException("Not supported.");}        
   
    }
    
}
