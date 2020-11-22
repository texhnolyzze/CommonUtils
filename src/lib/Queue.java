package lib;

import java.util.Iterator;

/**
 *
 * @author Texhnolyze
 */
public class Queue<E> implements Iterable<E> {
    
    private int size;
    private Node<E> head = new Node<>(null, null);
    private Node<E> tail = head;
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public E peek() {
        assertsizepositive();
        return head.elem;
    }
    
    public E poll() {
        assertsizepositive();
        size--;
        E e = head.elem;
        head = head.next;
        return e;
    }
    
    public void add(E e) {
        size++;
        tail.elem = e;
        tail.next = new Node<>(null, null);
        tail = tail.next;
    }
    
    private void assertsizepositive() {
        if (size == 0)
            throw new IllegalStateException("Queue is empty.");
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Node<E> curr = head;
            @Override public boolean hasNext() {return curr != tail;}
            @Override
            public E next() {
                E e = curr.elem;
                curr = curr.next;
                return e;
            }
        };
    }
    
    private static class Node<E> {
        
        E elem;
        Node<E> next;
        
        Node(E elem, Node<E> next) {
            this.elem = elem; 
            this.next = next;
        }
    
    }
    
}
