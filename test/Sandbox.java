
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lib.SetBinaryOperation;


public class Sandbox {

    public static void main(String[] args) {
        Set<Integer> a = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        Set<Integer> b = new HashSet<>(Arrays.asList(3, 4, 5, 6));
        Set<Integer> c = SetBinaryOperation.SYMMETRIC_DIFFERENCE.delegate(a, b);
        for (int i : c)
            System.out.println(i);
    }
    
}
