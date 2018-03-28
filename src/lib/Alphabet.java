package lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
    public int hashCode() {
        int hash = 7;
        if (indexOf == null) {
            hash = 29 * hash + Integer.hashCode(from);
            hash = 29 * hash + Integer.hashCode(alphabet.length);
        } else 
            hash = 29 * hash + indexOf.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Alphabet other = (Alphabet) obj;
        if (this.indexOf == null) {
            if (other.indexOf != null)
                return false;
            else
                return this.from == other.from && this.alphabet.length == other.alphabet.length;
        } else {
            if (other.indexOf == null)
                return false;
            else
                return this.indexOf.equals(other.indexOf);
        }
    }

    public static Alphabet fromUTF16Range(int from, int to) {
        Alphabet a = new Alphabet();
        a.from = from;
        a.alphabet = new char[from - to + 1];
        for (int i = from; i <= to; i++) 
            a.alphabet[i - from] = (char) i;
        return a;
    }

    public static Alphabet fromCharArray(char[] alphabet) {
        SortedSet<Character> set = new TreeSet<>();
        for (char c : alphabet) 
            set.add(c);
        return fromSortedSet(set);
    }
    
    public static Alphabet fromChars(Collection<Character> chars) {
        SortedSet<Character> set = new TreeSet<>();
        set.addAll(chars);
        return fromSortedSet(set);
    }
    
    public static Alphabet fromStrings(Collection<String> strings) {
        SortedSet<Character> set = new TreeSet<>();
        for (String s : strings) 
            for (int i = 0; i < s.length(); i++)
                set.add(s.charAt(i));
        return fromSortedSet(set);
    }
    
    public static Alphabet fromSortedSet(SortedSet<Character> set) {
        int index = 0;
        char[] alphabet = new char[set.size()];
        for (char c : set)
            alphabet[index++] = c;
        Alphabet a = new Alphabet();
        a.alphabet = alphabet;
        a.indexOf = new HashMap<>();
        for (int i = 0; i < alphabet.length; i++) 
            a.indexOf.put(alphabet[i], i);
        return a;
    }
    
}
