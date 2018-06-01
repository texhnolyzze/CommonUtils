package lib;

import java.util.Iterator;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Texhnolyze
 */
public enum CollectionBinaryOperation {
    
    UNION {

        @Override 
        int size(Collection a, Collection b) {
            int size = a.size() + b.size();
            for (Object obj : a) {
                if (b.contains(obj))
                    size--;
            }
            return size;
        }

        @Override 
        boolean contains(Collection a, Collection b, Object obj) {
            return a.contains(obj) || b.contains(obj);
        }

        @Override
        void toArray0(Collection a, Collection b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {dest[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) dest[i++] = obj;}
        }

        @Override
        Object next(Collection a, Collection b, Iterator ait, Iterator bit) {
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
        boolean add(Collection a, Collection b, Object obj) {
            if (a.contains(obj)) return false;
            if (b.contains(obj)) return false;
            return a.add(obj);
        }

        @Override
        void clear(Collection a, Collection b) {
            a.clear();
            b.clear();
        }

        @Override
        boolean remove(Collection a, Collection b, Object obj) {
            return a.remove(obj) | b.remove(obj);
        }

    }, INTERSECTION {

        @Override
        int size(Collection a, Collection b) {
            int size = 0;
            for (Object obj : a) {
                if (b.contains(obj))
                    size++;
            }
            return size;
        }

        @Override
        boolean contains(Collection a, Collection b, Object obj) {
            return a.contains(obj) && b.contains(obj);
        }

        @Override
        void toArray0(Collection a, Collection b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (b.contains(obj)) dest[i++] = obj;}
        }

        @Override
        Object next(Collection a, Collection b, Iterator ait, Iterator bit) {
            while (ait.hasNext()) {
                Object obj = ait.next();
                if (b.contains(obj))
                    return obj;
            }
            return null;
        }

        @Override
        boolean add(Collection a, Collection b, Object obj) {
            return a.add(obj) | b.add(obj);
        }

        @Override
        void clear(Collection a, Collection b) {
            a.removeAll(b);
        }

        @Override
        boolean remove(Collection a, Collection b, Object obj) {
            return a.remove(obj) && b.remove(obj);
        }

    }, DIFFERENCE {

        @Override
        int size(Collection a, Collection b) {
            int size = a.size();
            for (Object obj : a) {
                if (b.contains(obj))
                    size--;
            }
            return size;
        }

        @Override
        boolean contains(Collection a, Collection b, Object obj) {
            return a.contains(obj) && !b.contains(obj);
        }

        @Override
        void toArray0(Collection a, Collection b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (!b.contains(obj)) dest[i++] = obj;}
        }

        @Override
        Object next(Collection a, Collection b, Iterator ait, Iterator bit) {
            while (ait.hasNext()) {
                Object obj = ait.next();
                if (!b.contains(obj))
                    return obj;
            }
            return null;
        }

        @Override
        boolean add(Collection a, Collection b, Object obj) {
            return a.add(obj) | b.remove(obj);
        }

        @Override
        void clear(Collection a, Collection b) {
            a.retainAll(b);
        }

        @Override
        boolean remove(Collection a, Collection b, Object obj) {
            return a.remove(obj) && !b.contains(obj);
        }

    }, SYMMETRIC_DIFFERENCE {

        @Override
        int size(Collection a, Collection b) {
            int ab = DIFFERENCE.size(a, b);
            int ba = DIFFERENCE.size(b, a);
            return ab + ba;
        }

        @Override
        boolean contains(Collection a, Collection b, Object obj) {
            return DIFFERENCE.contains(a, b, obj) || DIFFERENCE.contains(b, a, obj);
        }

        @Override
        void toArray0(Collection a, Collection b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (!b.contains(obj)) dest[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) dest[i++] = obj;}
        }

        @Override
        Object next(Collection a, Collection b, Iterator ait, Iterator bit) {
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
        boolean add(Collection a, Collection b, Object obj) {
            if (a.contains(obj)) {
                if (b.contains(obj)) {
                    return b.remove(obj);
                }
            } else {
                if (!b.contains(obj))
                    return a.add(obj);
            }
            return false;
        }

        @Override
        void clear(Collection a, Collection b) {
            a.retainAll(b);
            b.retainAll(a);
        }

        @Override
        boolean remove(Collection a, Collection b, Object obj) {
            if (a.contains(obj)) {
                if (!b.contains(obj)) {
                    return a.remove(obj);
                }
            } else {
                if (b.contains(obj))
                    return b.remove(obj);
            }
            return false;
        }

    };

    abstract int size(Collection a, Collection b);
    abstract boolean contains(Collection a, Collection b, Object obj);
    abstract boolean add(Collection a, Collection b, Object obj);
    abstract boolean remove(Collection a, Collection b, Object obj);
    abstract void clear(Collection a, Collection b);
    abstract void toArray0(Collection a, Collection b, Object[] dest);
    abstract Object next(Collection a, Collection b, Iterator ait, Iterator bit);
    
    Object[] toArray(Collection a, Collection b) {
        Object[] arr = new Object[size(a, b)];
        toArray0(a, b, arr);
        return arr;
    }
    
    Iterator iterator(Collection a, Collection b) {
        return new Iterator() {
            
            Iterator ait = a.iterator();
            Iterator bit = b.iterator();
            Object next = CollectionBinaryOperation.this.next(a, b, ait, bit);

            
            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Object next() {
                Object obj = next;
                next = CollectionBinaryOperation.this.next(a, b, ait, bit);
                return obj;
            }
            
        };
    }

    public String toString(Collection a, Collection b) {
        StringBuilder sb = new StringBuilder();
        Iterator it = iterator(a, b);
        sb.append("[");
        while (it.hasNext()) {
            sb.append(it.next()).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public <E> Collection<E> constructNewCollection(Collection<E> a, Collection<E> b) {
        Collection<E> res = new HashSet<>(size(a, b));
        Iterator<E> it = iterator(a, b);
        while (it.hasNext()) 
            res.add(it.next());
        return res;
    }
    
    public <E> Result<E> delegate(Collection<E> a, Collection<E> b) {
        return new Result<>(a, b, this);
    }
        
    public static class Result<E> implements Collection<E> {
        
        private final Collection<E> a;
        private final Collection<E> b;
        private final CollectionBinaryOperation op;
        
        private Result(Collection<E> a, Collection<E> b, CollectionBinaryOperation op) {
            this.a = a;
            this.b = b;
            this.op = op;
        }
        
        public Collection<E> a() {
            return a;
        }
        
        public Collection<E> b() {
            return b;
        }
        
        public CollectionBinaryOperation operation() {
            return op;
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
            boolean bln = false;
            for (E e : c)
                bln = add(e) || bln;
            return bln;
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
            boolean bln = false;
            for (Object obj : c)
                bln = remove(obj) || bln;
            return bln;
        }

        @Override
        public String toString() {
            return op.toString(a, b);
        }
        
        @Override public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public <T> T[] toArray(T[] a) {throw new UnsupportedOperationException("Not supported.");}
        
    }
    
}
