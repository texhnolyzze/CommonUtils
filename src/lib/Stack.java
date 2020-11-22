package lib;

import java.util.Iterator;

/**
 *
 * @author Texhnolyze
 */
public class Stack<E> implements Iterable<E> {
    
    private Node<E> head;
    private int size;

    public Stack() {
    }

    public void clear() {
        size = 0;
        head = null;
    }
    
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(E e) {
        size++;
        head = new Node(e, head);
    }

    public E pop() {
        assertsizepositive();
        size--;
        E e = head.elem;
        head = head.next;
        return e;
    }

    public E peek() {
        assertsizepositive();
        return head.elem;
    }
    
    private void assertsizepositive() {
        if (size == 0)
            throw new IllegalStateException("Stack is empty.");
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Node<E> curr = head;
            @Override public boolean hasNext() {return curr != null;}
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
