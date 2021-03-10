package org.texhnolyzze.common;

import java.util.*;

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
                return alphabet.equals(other.alphabet);
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
        if (from < 0 || to < from || to > 65535) 
            throw new IllegalArgumentException("[from, to] not in the UTF-16 range; from: " + from + "; to: " + to);
        Pair<Integer, Integer> key = Pair.of(from, to);
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
    
    public static Alphabet fromChars(Iterable<Character> chars) {
        SortedSet<Character> set = new TreeSet<>();
        chars.forEach(c -> set.add(c));
        return fromSortedSet(set);
    }
    
    public static Alphabet fromLines(Iterable<String> lines) {
        SortedSet<Character> set = new TreeSet<>();
        lines.forEach((line) -> {
            line.chars().forEach((c) -> {
                set.add((char) c);
            });
        });
        return fromSortedSet(set);
    }
    
    public static Alphabet fromString(String s) {
        SortedSet<Character> set = new TreeSet<>();
        s.chars().forEach(c -> set.add((char) c));
        return fromSortedSet(set);
    }
    
    public static Alphabet fromSortedSet(SortedSet<Character> set) {
        int index = 0;
        char[] alphabet = new char[set.size()];
        for (char c : set)
            alphabet[index++] = c;
        CharArray arr = new CharArray(alphabet);
        arr.hashCode = Arrays.hashCode(arr.chars);
        Alphabet cached = ALL.get(arr);
        if (cached != null)
            return cached;
        Alphabet a = new Alphabet();
        a.alphabet = arr;
        a.indexOf = new HashMap<>(alphabet.length);
        for (int i = 0; i < alphabet.length; i++) 
            a.indexOf.put(alphabet[i], i);
        ALL.put(a.alphabet, a);
        return a;
    }
    
    private static class CharArray {
        
        char[] chars;
        int hashCode;
        
        CharArray(char[] chars) {
            this.chars = chars;
        }

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(chars, ((CharArray) obj).chars);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
        
    }
    
}
