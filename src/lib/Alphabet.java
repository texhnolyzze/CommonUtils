package lib;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Texhnolyze
 */
public class Alphabet implements Comparator<String> {
    
    private Map<Character, Integer> indexOf;

    private char[] alphabet;
    private int from;
    
    private final boolean UTF16Ordering;

    private Alphabet(boolean UTF16Ordering) {this.UTF16Ordering = UTF16Ordering;}

    public int size() {
        return alphabet.length;
    }

    public char charBy(int index) {
        return alphabet[index];
    }

    public int indexOf(char c) {
        if (indexOf != null) 
            return indexOf.getOrDefault(c, -1);
        else 
            return c - from < 0 ? -1 : c > from + alphabet.length - 1 ? -1 : c - from;
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
    
    @Override
    public int compare(String s1, String s2) {
        if (UTF16Ordering) 
            return s1.compareTo(s2);
        else {
            int len1 = s1.length();
            int len2 = s2.length();
            int lim = Math.min(len1, len2);
            for (int i = 0; i < lim; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) 
                    return indexOf.get(c1) - indexOf.get(c2);
            }
            return len1 - len2;
        }
    }

    public static Alphabet fromUTF16Range(int from, int to) {
        Alphabet a = new Alphabet(true);
        a.from = from;
        a.alphabet = new char[from - to + 1];
        for (int i = from; i <= to; i++) 
            a.alphabet[i - from] = (char) i;
        return a;
    }

    public static Alphabet fromChars(char[] alphabet) {
        boolean UTF16Ordering = true;
        for (int i = 1; i < alphabet.length; i++) {
            if (alphabet[i] < alphabet[i - 1]) {
                UTF16Ordering = false;
                break;
            }
        }
        Set<Character> set = new HashSet<>();
        for (char c : alphabet) {
            if (!set.contains(c)) 
                set.add(c);
            else
                throw new IllegalArgumentException("Char " + c + " is not unic.");
        }
        Alphabet a = new Alphabet(UTF16Ordering);
        a.alphabet = alphabet;
        a.indexOf = new HashMap<>();
        for (int i = 0; i < alphabet.length; i++) 
            a.indexOf.put(alphabet[i], i);
        return a;
    }
    
    public static Alphabet fromStrings(Collection<String> strings) {
        SortedSet<Character> set = new TreeSet<>();
        for (String s : strings) 
            for (int i = 0; i < s.length(); i++)
                set.add(s.charAt(i));
        Alphabet a = new Alphabet(true);
        a.alphabet = new char[set.size()];
        a.indexOf = new HashMap<>();
        int idx = 0;
        for (char c : set) {
            a.alphabet[idx] = c;
            a.indexOf.put(c, idx);
            idx++;
        }
        return a;
    }
    
}
