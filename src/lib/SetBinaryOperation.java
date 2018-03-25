package lib;

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

        @Override
        boolean add(Set a, Set b, Object obj) {
            return a.add(obj) || b.add(obj);
        }

        @Override
        void clear(Set a, Set b) {
            a.clear();
            b.clear();
        }

        @Override
        boolean remove(Set a, Set b, Object obj) {
            return a.remove(obj) | b.remove(obj);
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

        @Override
        boolean add(Set a, Set b, Object obj) {
            return a.add(obj) | b.add(obj);
        }

        @Override
        void clear(Set a, Set b) {
            a.removeAll(b);
        }

        @Override
        boolean remove(Set a, Set b, Object obj) {
            return a.remove(obj) && b.remove(obj);
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

        @Override
        boolean add(Set a, Set b, Object obj) {
            return a.add(obj) | b.remove(obj);
        }

        @Override
        void clear(Set a, Set b) {
            a.retainAll(b);
        }

        @Override
        boolean remove(Set a, Set b, Object obj) {
            return a.remove(obj);
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

        @Override
        boolean add(Set a, Set b, Object obj) {
            boolean res = a.remove(obj) & b.remove(obj);
            a.add(obj);
            return res;
        }

        @Override
        void clear(Set a, Set b) {
            a.retainAll(b);
            b.retainAll(a);
        }

        @Override
        boolean remove(Set a, Set b, Object obj) {
            return a.add(obj) & b.add(obj);
        }

    };

    abstract int size(Set a, Set b);
    abstract boolean contains(Set a, Set b, Object obj);
    abstract boolean add(Set a, Set b, Object obj);
    abstract boolean remove(Set a, Set b, Object obj);
    abstract void clear(Set a, Set b);
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
        Set<E> res = new HashSet<>(size(a, b));
        Iterator<E> it = iterator(a, b);
        while (it.hasNext()) 
            res.add(it.next());
        return res;
    }
    
    public <E> Set<E> delegate(Set<E> a, Set<E> b) {
        return new SetDelegator<>(a, b, this);
    }
        
    private static class SetDelegator<E> implements Set<E> {
        
        private SetBinaryOperation op;
        private Set<E> a, b;
        
        private SetDelegator(Set<E> a, Set<E> b, SetBinaryOperation op) {
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
        public Iterator<E> iterator() {
            return op.iterator(a, b);
        }

        @Override
        public Object[] toArray() {
            return op.toArray(a, b);
        }

        @Override 
        public boolean add(E e) {
            return op.add(a, b, e);
        }
        
        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean b = false;
            for (E e : c)
                b = add(e);
            return b;
        }
        
        @Override 
        public void clear() {
            op.clear(a, b);
        }
        
        @Override 
        public boolean remove(Object obj) {
            return op.remove(a, b, obj);
        }
        
        @Override 
        public boolean removeAll(Collection<?> c) {
            boolean b = false;
            for (Object obj : c)
                b = remove(obj);
            return b;
        }
        
        @Override public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public <T> T[] toArray(T[] a) {throw new UnsupportedOperationException("Not supported.");}        
   
    }
    
}
