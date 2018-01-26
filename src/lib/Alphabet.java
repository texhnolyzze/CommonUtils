package lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Texhnolyze
 */
public class Alphabet {
    
    private char[] alpha;
    private Map<Character, Integer> indexOf;

    private int from;

    private Alphabet() {}

    public int size() {
        return alpha.length;
    }

    public char charBy(int index) {
        return alpha[index];
    }

    public int indexOf(char c) {
        if (indexOf != null) return indexOf.getOrDefault(c, -1);
        else return c - from < 0 ? -1 : c - from;
    }

    public boolean contains(char c) {
        return indexOf(c) != -1;
    }

    public boolean isValid(String str) {
        for (int i = 0; i < str.length(); i++) 
            if (!contains(str.charAt(i))) 
                return false;
        return true;
    }

    public static Alphabet fromUTF16Range(int from, int to) {

        Alphabet a = new Alphabet();
        a.from = from;

        a.alpha = new char[from - to + 1];

        for (int i = from; i <= to; i++) a.alpha[i - from] = (char) i;

        return a;

    }

    public static Alphabet fromChars(char[] alpha) {

        Alphabet a = new Alphabet();
        a.indexOf = new HashMap<>();
        Set<Character> set = new HashSet<>();
        for (char c : alpha) set.add(c);

        int idx = 0;
        a.alpha = new char[set.size()];
        for (char c : set) {
            a.alpha[idx] = c;
            a.indexOf.put(c, idx++);
        }

        return a;

    }
    
}
