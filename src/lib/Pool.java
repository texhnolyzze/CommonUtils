package lib;

/**
 *
 * @author Texhnolyze
 */
public class Pool<E> {
    
    private Factory<E> factory;
    private Reseter<E> reseter;
    private final Stack<E> stack = new Stack<>();
    
    private int numObjectsCreated;
    
    public Pool(Factory<E> factory, Reseter<E> reseter) {
        this.factory = factory;
        this.reseter = reseter;
    }
    
    public int size() {return stack.size();}
    public boolean isEmpty() {return stack.isEmpty();}
    public int numObjectsCreated() {return numObjectsCreated;}
    
    public E obtain() {
        E e;
        if (stack.isEmpty()) {
            e = factory._new();
            numObjectsCreated++;
        } else 
            e = stack.pop();
        return e;
    }
    
    public void free(E e) {
        reseter.reset(e);
        stack.push(e);
    }
    
    public Factory<E> getFactory() {return factory;}
    public void setFactory(Factory<E> factory) {this.factory = factory;}
    
    public Reseter<E> getReseter() {return reseter;}
    public void setReseter(Reseter<E> reseter) {this.reseter = reseter;}
    
    public interface Factory<E> {
        E _new();
    }
    
    public interface Reseter<E> {
        void reset(E e);
    }
    
}
