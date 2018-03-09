package lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 *
 * @author Texhnolyze
 */
public class KnapsackProblemSolver {

    
    public static Triple<Double, Double, Collection<Item>> solve(Collection<Item> items, boolean sorted, double cap) {
        Collection<Item> itemsPtr = items; 
        Item max = new Item() {
            public double value() {return Double.NEGATIVE_INFINITY;}
            public double weight() {return Double.POSITIVE_INFINITY;}
        };
        
        if (!sorted) {
            if (!(itemsPtr instanceof SortedSet)) {
                if (!(itemsPtr instanceof List)) {
                    itemsPtr = new ArrayList<>(itemsPtr);
                }
                Collections.sort((List<Item>) itemsPtr);
            }
        }
        
        for (Item item : itemsPtr) {
            if (item.value() > max.value() && item.weight() <= cap)
                max = item;
        }
        
        double w = 0, v = 0;
        List<Item> result = new ArrayList<>();
        for (Item i : itemsPtr) {
            if (w + i.weight() <= cap) {
                result.add(i);
                v += i.value();
                w += i.weight();
            } else 
                break;
        }
        
        return v < max.value() ? 
                new Triple<>(max.value(), max.weight(), Collections.singletonList(max)) 
              : new Triple<>(v, w, result);
        
    }
    
    public interface Item extends Comparable<Item> {
        
        double value();
        double weight();

        @Override
        default int compareTo(Item other) {
            
            double f1 = this.value() / this.weight();
            double f2 = other.value() / other.weight();

            if (f1 > f2) return -1;
            else if (f1 < f2) return 1;
            else return 0;
            
        }
        
    }
    
}
