package lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Texhnolyze
 */
public class Alphabet implements Iterable<Character> {
    
    private static final Map<CharArray, Alphabet> ALL = new HashMap<>();
    private static final Map<Pair<Integer, Integer>, Alphabet> ALL_UTF16 = new HashMap<>();
    
    public static final Alphabet RUS = fromUTF16Range(1040, 1103);
    
    private Map<Character, Integer> indexOf;

    private CharArray alphabet;
    private int from;
    
    private Alphabet() {}

    public int size() {
        return alphabet.chars.length;
    }

    public char charBy(int index) {
        return alphabet.chars[index];
    }

    public int indexOf(char c) {
        if (indexOf != null) 
            return indexOf.getOrDefault(c, -1);
        else 
            return c - from < 0 ? -1 : c > from + alphabet.chars.length - 1 ? -1 : c - from;
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
            hash = 29 * hash + Integer.hashCode(alphabet.chars.length);
        } else
            hash = 29 * hash + alphabet.hashCode();
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
                return this.from == other.from && this.alphabet.chars.length == other.alphabet.chars.length;
        } else {
            if (other.indexOf == null)
                return false;
            else
                return Arrays.equals(alphabet.chars, other.alphabet.chars);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(alphabet.chars);
    }

    @Override
    public Iterator<Character> iterator() {
        return new Iterator<Character>() {
            
            int i = 0;
            
            @Override public boolean hasNext() {return i < alphabet.chars.length;}
            @Override public Character next() {return alphabet.chars[i++];}
        
        };
    }

    public static Alphabet fromUTF16Range(int from, int to) {
        Pair<Integer, Integer> key = new Pair<>(from, to);
        Alphabet cached = ALL_UTF16.get(key);
        if (cached != null)
            return cached;
        Alphabet a = new Alphabet();
        a.from = from;
        a.alphabet = new CharArray(new char[to - from + 1]);
        for (int i = from; i <= to; i++) 
            a.alphabet.chars[i - from] = (char) i;
        ALL_UTF16.put(key, a);
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
    
    public static Alphabet fromString(String s) {
        SortedSet<Character> set = new TreeSet<>();
        for (int i = 0; i < s.length(); i++)
            set.add(s.charAt(i));
        return fromSortedSet(set);
    }
    
    public static Alphabet fromSortedSet(SortedSet<Character> set) {
        int index = 0;
        char[] alphabet = new char[set.size()];
        for (char c : set)
            alphabet[index++] = c;
        Alphabet cached = ALL.get(new CharArray(alphabet));
        if (cached != null)
            return cached;
        Alphabet a = new Alphabet();
        a.alphabet = new CharArray(alphabet);
        a.indexOf = new HashMap<>(alphabet.length);
        for (int i = 0; i < alphabet.length; i++) 
            a.indexOf.put(alphabet[i], i);
        ALL.put(a.alphabet, a);
        return a;
    }
    
    private static class CharArray {
        
        final char[] chars;
        int hashCode;
        boolean hashCodeCalc;
        
        CharArray(char[] chars) {this.chars = chars;}

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(chars, ((CharArray) obj).chars);
        }

        @Override
        public int hashCode() {
            if (hashCodeCalc)
                return hashCode;
            hashCode = Arrays.hashCode(chars);
            return hashCode;
        }
        
    }
    
}
