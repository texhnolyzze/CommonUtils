package lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

/**
 *
 * @author Texhnolyze
 */
public class PrefixTree<E> {

    private Node<E> root = new Node<>(null);

    private E prevVal;

    private int size;
    
    public void clear() {
        size = 0;
        root.childs.clear();
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public boolean containsVal(E e) {
        return containsVal(e, root);
    }

    private boolean containsVal(E e, Node<E> n) {
        if (n.elem.equals(e)) return true;
        else {
            for (Node<E> child : n.childs.values()) {
                boolean b = containsVal(e, child);
                if (b) return true;
            }
        }
        return false;
    }
	
    public E get(String key) {
        return get(key, 0, root);
    }
    
    private E get(String key, int i, Node<E> n) {
        if (n == null) return null;
        else { 
            if (i < key.length()) 
                return get(key, i + 1, n.childs.get(key.charAt(i)));
            else 
                return n.elem;
        }
    }
    
    public E put(String key, E e) {
        prevVal = null;
        put(key, e, 0, root);
        size++;
        return prevVal;
    }
    
    private Node<E> put(String key, E e, int i, Node<E> n) {
        
        if (n == null) n = new Node<>();
        
        if (i < key.length()) {
            Node<E> child = n.childs.get(key.charAt(i));
            n.childs.put(key.charAt(i), put(key, e, i + 1, child));
        } else {
            prevVal = n.elem;
            n.elem = e;
        }
        return n;
        
    }
    
    public E remove(String key) {
        prevVal = null;
        remove(key, 0, root);
        size--;
        return prevVal;
    }
    
    private Node<E> remove(String key, int i, Node<E> n) {
        
        if (n == null) return null;
        
        if (i < key.length()) {
            
            Node<E> child = n.childs.get(key.charAt(i));
            child = remove(key, i + 1, child);
            if (child == null) 
                n.childs.remove(key.charAt(i));
            
        } else {
            prevVal = n.elem;
            n.elem = null;
        }
        
        return (n.elem != null || !n.childs.isEmpty()) ? n : null;
        
    }
        
    public Collection<Pair<String, E>> keysWithPrefix(String prefix) {
        Collection c = new ArrayList<>();
        downToPrefix(c, prefix, 0, root);
        return c;
    }
    
    public Collection<Pair<String, E>> pairs() {
        return keysWithPrefix("");
    }
    
    private void downToPrefix(Collection<Pair<String, E>> c, String prefix, int i, Node<E> n) {
        if (n == null) return;
        if (i < prefix.length()) {
            downToPrefix(c, prefix, i + 1, n.childs.get(prefix.charAt(i)));
        } else {
            collectKeys(c, new StringBuilder(prefix), n);
        }
    }
    
    private void collectKeys(Collection<Pair<String, E>> c, StringBuilder sb, Node<E> n) {
        if (n.elem != null) c.add(new Pair<>(sb.toString(), n.elem));
        for (Map.Entry<Character, Node<E>> child : n.childs.entrySet()) {
            collectKeys(c, sb.append(child.getKey().charValue()), child.getValue());
        }
        sb.setLength(sb.length() - 1);
    }
    
    private static class Node<E> {
        
        E elem;
        Map<Character, Node<E>> childs = new HashMap<>(2);
        
        Node() {}
        Node(E elem) {this.elem = elem;}
        
    }
    
}
