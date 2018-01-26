package lib;

/**
 *
 * @author Texhnolyze
 */
public class Stack<E> {
    
    private Node<E> head;
    private int size;

    public Stack() {}

    public void clear() {
        size = 0;
        head = null;
    }
    
    public int getSize() {
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
        size--;
        E e = head.elem;
        head = head.next;
        return e;
    }

    public E peek() {
        return head.elem;
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
