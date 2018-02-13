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
    
    private Map<Character, Integer> indexOf;

    private char[] alphabet;
    private int from;

    private Alphabet() {}

    public int size() {
        return alphabet.length;
    }

    public char charBy(int index) {
        return alphabet[index];
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
        a.alphabet = new char[from - to + 1];
        for (int i = from; i <= to; i++) a.alphabet[i - from] = (char) i;
        return a;
    }

    public static Alphabet fromChars(char[] alphabet) {
        Alphabet a = new Alphabet();
        a.indexOf = new HashMap<>();
        Set<Character> set = new HashSet<>();
        for (char c : alphabet) {
            if (!set.contains(c)) 
                set.add(c);
            else
                throw new IllegalArgumentException("Char " + c + " is not unic.");
        }
        a.alphabet = alphabet;
        for (int i = 0; i < alphabet.length; i++) 
            a.indexOf.put(alphabet[i], i);
        return a;
    }
    
}
