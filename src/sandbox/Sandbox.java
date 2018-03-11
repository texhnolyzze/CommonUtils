package sandbox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import lib.MinBinaryHeap;

/**
 *
 * @author Texhnolyze
 */
public class Sandbox {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        
        Integer[] arr = {432,23423,4234234,234,2,13,3423,412,32,523,41243,2345,34,345,34,534,45234};
        MinBinaryHeap<Integer> heap = MinBinaryHeap.fromArray(arr, (Integer o1, Integer o2) -> {
            int i = o1;
            int j = o2;
            return i < j ? -1 : i == j ? 0 : 1;
        });
        while (!heap.isEmpty()) {
            System.out.println(heap.popMin());
        }
    }
    
    
}
