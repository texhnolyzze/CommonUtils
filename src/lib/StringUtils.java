package lib;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import static lib.MathUtils.*;

/**
 *
 * @author Texhnolyze
 */
public final class StringUtils {
    
    private StringUtils() {}
    
    private static final int PRIME = 113;
    
    private static int hash(String s, int l, int r) {
	int hash = 0;
	for (int i = l; i <= r; i++) 
            hash = PRIME * hash + s.charAt(i);
	return hash;
    }
    
    private static boolean check(String s, String pattern, int l, int r) {
	for (int i = l, j = 0; i <= r; i++, j++) {
            if (s.charAt(i) != pattern.charAt(j))
                return false;
	}
	return true;
    }
    
    public static int indexOfRabinCarp(String s, String pattern) {
        return indexOfRabinCarp(s, pattern, 0, s.length() - 1);
    }
    
    public static int indexOfRabinCarp(String s, String pattern, int from) {
        return indexOfRabinCarp(s, pattern, from, s.length() - 1);
    }
    
    public static int indexOfRabinCarp(String s, String pattern, int from, int to) {
        int n = to - from + 1, m = pattern.length();
        if (n == 0 || n < m)
            return -1;
        if (m == 0)
            return from;
	int p_pow = pow(PRIME, m - 1);
	int patt_hash = hash(pattern, 0, m - 1);
	int s_hash = hash(s, from, from + m - 1);
	if (s_hash == patt_hash) {
            if (check(s, pattern, from, from + m - 1))
                return from;
	}
	for (int i = from + 1; i <= from + n - m; i++) {
            s_hash = (s_hash - s.charAt(i - 1) * p_pow) * PRIME + s.charAt(i + m - 1);
            if (s_hash == patt_hash) {
                if (check(s, pattern, i, i + m - 1))
                    return i;
            }
	}
	return -1;
    }
    
//  Cache for effective substring search by Knuth-Morris-Pratt(KMP) algorithm
    public static int[] prefixFunction(String s) {
        int[] p = new int[s.length()];
        p[0] = 0;
        outer: for (int i = 1; i < s.length(); i++) {
            int j = p[i - 1];
            while (j > 0) {
                if (s.charAt(j) == s.charAt(i)) {
                    p[i] = j + 1;
                    continue outer;
                }
                j = p[j - 1];
            }
            p[i] = s.charAt(0) == s.charAt(i) ? 1 : 0;
        }
        return p;
    }
    
    public static class StateMachine {
    
        private final int m;
        private final int[] fsm;
        private final Alphabet alphabet;
        
        private StateMachine(int[] fsm, Alphabet alphabet) {
            this.fsm = fsm;
            this.alphabet = alphabet;
            this.m = fsm.length / alphabet.size();
        }
        
        private int state;
        
        public int getState() {
            return state;
        }
        
        public boolean nextChar(char c) {
            if (state == m)
                throw new IllegalStateException("Machine in final state, call reset method first.");
            int i = alphabet.indexOf(c);
            if (i == -1) {
                state = 0;
                return false;
            }
            state = fsm[to2DArrayHash(state, i, m)];
            return state == m;
        }
        
        public void reset() {
            state = 0;
        }
        
        public Alphabet getAlphabet() {
            return alphabet;
        }
        
        public String getPattern() {
            StringBuilder sb = new StringBuilder();
            int m = fsm.length / alphabet.size();
            for (int i = 0; i < m; i++) {
                int j = 0;
                for (char c : alphabet) {
                    if (fsm[to2DArrayHash(i, j, m)] == i + 1) {
                        sb.append(c);
                        break;
                    }
                    j++;
                }
            }
            return sb.toString();
        }
        
//      Cache for effective substring search by FSM algorithm
        public static final StateMachine build(String pattern) {
            int m = pattern.length();
            Alphabet alphabet = Alphabet.fromString(pattern);
            int[] fsm = new int[m * alphabet.size()];
            int[] p = prefixFunction(pattern);
            fsm[to2DArrayHash(0, alphabet.indexOf(pattern.charAt(0)), m)] = 1;
            for (int i = 1; i < m; i++) {
                for (char c : alphabet) {
                    int c_idx = alphabet.indexOf(c);
                    if (c == pattern.charAt(i)) 
                        fsm[to2DArrayHash(i, c_idx, m)] = i + 1;
                    else 
                        fsm[to2DArrayHash(i, c_idx, m)] = fsm[to2DArrayHash(p[i - 1], c_idx, m)];
                }
            }
            return new StateMachine(fsm, alphabet);
        }
        
    }
    
    public static int indexOfFSM(String s, String pattern, StateMachine fsm) {
        return indexOfFSM(s, pattern, 0, s.length() - 1, fsm);
    }
    
    public static int indexOfFSM(String s, String pattern, int from, StateMachine fsm) {
        return indexOfFSM(s, pattern, from, s.length() - 1, fsm);
    }
    
    public static int indexOfFSM(String s, String pattern, int from, int to, StateMachine fsm) {
        int n = to - from + 1, m = pattern.length();
        if (n == 0 || n < m)
            return -1;
        if (m == 0)
            return from;
        if (fsm == null) 
            fsm = StateMachine.build(pattern);
        int state = 0;
        for (int i = from; i <= to; i++) {
            int index = fsm.alphabet.indexOf(s.charAt(i));
            if (index == -1)
                state = 0;
            else {
                state = fsm.fsm[to2DArrayHash(state, index, m)];
                if (state == m)
                    return i - m + 1;
            }
        }
        return -1;
    }
    
    public static int indexOfKMP(String s, String pattern, int[] prefixFunction) {
        return indexOfKMP(s, pattern, prefixFunction, 0, s.length() - 1);        
    }
    
    public static int indexOfKMP(String s, String pattern, int[] prefixFunction, int from) {
        return indexOfKMP(s, pattern, prefixFunction, from, s.length() - 1);
    }
    
    public static int indexOfKMP(String s, String pattern, int[] prefixFunction, int from, int to) {
        int n = to - from + 1, m = pattern.length();
        if (n == 0 || n < m)
            return -1;
        if (m == 0)
            return from;
        if (prefixFunction == null)
            prefixFunction = prefixFunction(pattern);
        int[] p = prefixFunction;
        int i = from, j = 0;
        while (i <= to && j < m) {
            if (s.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            } else {
                if (j != 0)
                    j = p[j - 1];
                else 
                    i++;
            }
        }
        return j == m ? i - m : -1;
    }
    
    public static String normalize(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ')
            i++;
        int j = s.length() - 1;
        while (j >= 0 && s.charAt(j) == ' ')
            j--;
        if (i > j)
            return "";
        while (true) {
            sb.append(s.charAt(i++));
            if (i > j)
                return sb.toString();
            if (s.charAt(i) == ' ') {
                i++;
                while (s.charAt(i) == ' ')
                    i++;
                sb.append(' ');
            }
        }
    }
    
    private static final String[] RU_TO_EN_TRANSLIT_TBL = {
        "A", "B", "V", "G", "D", "E", "Zh", "Z", "I", "J", "K", "L", "M", "N", "O", "P", "R", 
        "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Shch", "", "Y", "", "E", "Yu", "Ya",
        "a", "b", "v", "g", "d", "e", "zh", "z", "i", "j", "k", "l", "m", "n", "o", "p", "r", 
        "s", "t", "u", "f", "h", "ts", "ch", "sh", "shch", "", "y", "", "e", "yu", "ya"
    };
    
    public static String translitRU_EN(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '¨':
                    sb.append('Å');
                    break;
                case '¸':
                    sb.append('å');
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        str = sb.toString();
        sb.setLength(0);
        for (int i = 0; i < str.length(); i++) {
            int idx = Alphabet.RUS.indexOf(str.charAt(i));
            if (idx != -1) 
                sb.append(RU_TO_EN_TRANSLIT_TBL[idx]);
            else 
                sb.append(str.charAt(i));
        }
        return sb.toString();
    }
    
    public static enum CharOperation {
        NOOP, // match, no operation 
        REMOVAL, 
        INSERTION
    }
    
    public static String longestCommonSubstring(String s1, String s2) {
        if (s1.length() < s2.length()) {
            String temp = s1;
            s1 = s2;
            s2 = temp;
        }
        int max_len = 0; // max len of curr longest substring
        int max_idx = 0; // char index of curr longest substring end in s2
        int n = s1.length(), m = s2.length();
        int[] lcs_prev = new int[m + 1];
        int[] lcs_curr = new int[m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) 
                    lcs_curr[j] = lcs_prev[j - 1] + 1;
                else
                    lcs_curr[j] = 0;
                if (lcs_curr[j] > max_len) {
                    max_idx = j - 1;
                    max_len = lcs_curr[j];
                }
            }
            int[] temp = lcs_prev;
            lcs_prev = lcs_curr;
            lcs_curr = temp;
        }
        if (max_len == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = max_idx - max_len + 1; i <= max_idx; i++) 
            sb.append(s2.charAt(i));
        return sb.toString();
    }
    
    public static String longestCommonSubsequence(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        int col_num = n + 1;
        int[] LCS = new int[(col_num) * (m + 1)];
        for (int i = n; i >= 0; i--) {
            for (int j = m; j >= 0; j--) {
                if (i >= n || j >= m)
                    LCS[to2DArrayHash(i, j, col_num)] = 0;
                else if (s1.charAt(i) == s2.charAt(j))
                    LCS[to2DArrayHash(i, j, col_num)] = 1 + LCS[to2DArrayHash(i + 1, j + 1, col_num)];
                else
                    LCS[to2DArrayHash(i, j, col_num)] = max(LCS[to2DArrayHash(i + 1, j, col_num)], LCS[to2DArrayHash(i, j + 1, col_num)]);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = 0; i < n && j < m;) {
            if (s1.charAt(i) == s2.charAt(j)) {
                sb.append(s1.charAt(i));
                i++;
                j++;
            } else if (LCS[to2DArrayHash(i + 1, j, col_num)] >= LCS[to2DArrayHash(i, j + 1, col_num)])
                i++;
            else
                j++;
        }
        return sb.toString();
    }
    
    public static void diff(String s1, String s2, BiConsumer<Character, CharOperation> c) {
        diff(s1, s2, longestCommonSubsequence(s1, s2), c);
    }
    
    public static void diff(String s1, String s2, String lcs, BiConsumer<Character, CharOperation> c) {
        if (lcs.length() == 0)
            return;
        int s1_idx = 0, s2_idx = 0, lcs_idx = 0;
        do {
            char c1 = s1.charAt(s1_idx);
            char c2 = s2.charAt(s2_idx);
            char c_lcs = lcs.charAt(lcs_idx);
            if (c1 == c2) { 
                s1_idx++;
                s2_idx++;
                lcs_idx++;
                c.accept(c1, CharOperation.NOOP);
            } else {
                if (c1 == c_lcs) {
                    c.accept(c2, CharOperation.INSERTION);
                    s2_idx++;
                } else {
                    c.accept(c1, CharOperation.REMOVAL);
                    s1_idx++;
                }
            }
        } while (lcs_idx < lcs.length());
        while (s1_idx < s1.length()) {
            c.accept(s1.charAt(s1_idx), CharOperation.REMOVAL);
            s1_idx++;
        }
        while (s2_idx < s2.length()) {
            c.accept(s2.charAt(s2_idx), CharOperation.INSERTION);
            s2_idx++;
        }
    }
    
    public static double getHammingDistance(String s1, String s2, BiFunction<Character, Character, Double> exch) {
        if (s1.length() != s2.length())
            throw new IllegalArgumentException("s1.length() must be equal to s2.length()");
        double d = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i))
                d += exch.apply(s1.charAt(i), s2.charAt(i));
        }
        return d;
    }
	
    public static double getLevensteinDistance(String s1, String s2, BiFunction<Character, CharOperation, Double> removeInsertion, BiFunction<Character, Character, Double> exchange) {
        if (s1.length() < s2.length()) {
            String temp = s2;
            s2 = s1;
            s1 = temp;
        }
        int n = s1.length(), m = s2.length();
        double[] ld_prev = new double[m + 1];
        double[] ld_curr = new double[m + 1];
        for (int i = 1; i <= m; i++)
            ld_prev[i] = removeInsertion.apply(s2.charAt(i - 1), CharOperation.INSERTION);
        for (int i = 1; i <= n; i++) {
            ld_curr[0] = ld_prev[0] + removeInsertion.apply(s1.charAt(i - 1), CharOperation.REMOVAL);
            for (int j = 1; j <= m; j++) {
                ld_curr[j] = Math.min(
                    ld_prev[j] + removeInsertion.apply(s1.charAt(i - 1), CharOperation.REMOVAL),
                    Math.min(
                        ld_curr[j - 1] + removeInsertion.apply(s2.charAt(j - 1), CharOperation.INSERTION),
                        ld_prev[j - 1] + exchange.apply(s1.charAt(i - 1), s2.charAt(j - 1))
                    )
                );
            }
            double[] temp = ld_prev;
            ld_prev = ld_curr;
            ld_curr = temp;
        }
        return ld_prev[m];
    }
    
    public static double getRemoveInsertDistance(String s1, String s2, BiFunction<Character, CharOperation, Double> removeInsert) {
        if (s1.length() < s2.length()) {
            String temp = s1;
            s1 = s2;
            s2 = temp;
        }
        int n = s1.length(), m = s2.length();
        double[] d_prev = new double[m + 1];
        double[] d_curr = new double[m + 1];
        for (int i = 1; i <= m; i++) 
            d_prev[i] = removeInsert.apply(s2.charAt(i - 1), CharOperation.INSERTION);
        for (int i = 1; i <= n; i++) {
            d_curr[0] = d_prev[0] + removeInsert.apply(s1.charAt(i - 1), CharOperation.REMOVAL);
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1))
                    d_curr[j] = d_prev[j - 1];
                else {
                    d_curr[j] = Math.min(
                        d_prev[j] + removeInsert.apply(s1.charAt(i - 1), CharOperation.REMOVAL),
                        d_curr[j - 1] + removeInsert.apply(s2.charAt(j - 1), CharOperation.INSERTION)
                    );
                }
            }
            double[] temp = d_prev;
            d_prev = d_curr;
            d_curr = temp;
        }
        return d_prev[m];
    }
    
}
