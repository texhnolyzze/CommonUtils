package org.texhnolyzze.common;

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
        void toArray0(Set a, Set b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {dest[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) dest[i++] = obj;}
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
			if (a.contains(obj) || b.contains(obj)) 
				return false;
			a.add(obj);
			return true;
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
        void toArray0(Set a, Set b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (b.contains(obj)) dest[i++] = obj;}
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
        void toArray0(Set a, Set b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (!b.contains(obj)) dest[i++] = obj;}
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
            return a.remove(obj) && !b.contains(obj);
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
        void toArray0(Set a, Set b, Object[] dest) {
            int i = 0;
            for (Object obj : a) {if (!b.contains(obj)) dest[i++] = obj;}
            for (Object obj : b) {if (!a.contains(obj)) dest[i++] = obj;}
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
        void clear(Set a, Set b) {
            a.retainAll(b);
            b.retainAll(a);
        }

        @Override
        boolean remove(Set a, Set b, Object obj) {
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

    abstract int size(Set a, Set b);
    abstract boolean contains(Set a, Set b, Object obj);
    abstract boolean add(Set a, Set b, Object obj);
    abstract boolean remove(Set a, Set b, Object obj);
    abstract void clear(Set a, Set b);
    abstract void toArray0(Set a, Set b, Object[] dest);
    abstract Object next(Set a, Set b, Iterator ait, Iterator bit);
    
    Object[] toArray(Set a, Set b, int size) {
        Object[] arr = new Object[size];
        toArray0(a, b, arr);
        return arr;
    }
    
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

    public String toString(Set a, Set b) {
        StringBuilder sb = new StringBuilder();
        Iterator it = iterator(a, b);
        sb.append("[");
        while (it.hasNext()) {
            sb.append(it.next()).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public <E> Set<E> constructNewSet(Set<? extends E> a, Set<? extends E> b) {
        Set<E> res = new HashSet<>();
        Iterator<E> it = iterator(a, b);
        while (it.hasNext()) 
            res.add(it.next());
        return res;
    }
    
    public <E> Result<E> delegate(Set<? extends E> a, Set<? extends E> b) {
        return new Result(a, b, this);
    }
        
    public static class Result<E> implements Set<E> {
        
        private final Set<E> a;
        private final Set<E> b;
        private final SetBinaryOperation op;
        
        private int size;
        
        private Result(Set<E> a, Set<E> b, SetBinaryOperation op) {
            this.a = a;
            this.b = b;
            this.op = op;
            size = op.size(a, b);
        }
        
        public Set<E> a() {
            return a;
        }
        
        public Set<E> b() {
            return b;
        }
        
        public SetBinaryOperation operation() {
            return op;
        }
        
        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
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
            return op.toArray(a, b, size);
        }

        @Override 
        public boolean add(E e) {
            boolean added = op.add(a, b, e);
            if (added) 
                size++;
            return added;
        }
        
        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean modified = false;
            for (E e : c) 
                modified = add(e) || modified;
            return modified;
        }
        
        @Override 
        public void clear() {
            size = 0;
            op.clear(a, b);
        }
        
        @Override 
        public boolean remove(Object obj) {
            boolean removed = op.remove(a, b, obj);
            if (removed)
                size--;
            return removed;
        }
        
        @Override 
        public boolean removeAll(Collection<?> c) {
            boolean modified = false;
            for (Object obj : c)
                modified = remove(obj) || modified;
            return modified;
        }
        
        @Override
        public String toString() {
            return op.toString(a, b);
        }
        
        @Override public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException("Not supported.");}
        @Override public <T> T[] toArray(T[] a) {throw new UnsupportedOperationException("Not supported.");}
        
    }
    
}
