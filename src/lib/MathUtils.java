package lib;

import java.util.function.Consumer;

/**
 *
 * @author Texhnolyze
 */
public final class MathUtils {
    
    private MathUtils() {}
    
    public static int to2DArrayHash(int columnIndex, int lineIndex, int columnsNum) {
        return columnIndex + lineIndex * columnsNum;
    }

    public static int to2DArrayColumnIndex(int hash, int columnsNum) {
        return hash % columnsNum;
    }

    public static int to2DArrayLineIndex(int hash, int columnsNum) {
        return hash / columnsNum;
    }
	
    public static int pow(int a, int b) {
        int res = 1, exp = b;
        while (exp != 0) {
            if ((exp & 1) != 0)
                res *= a;
            exp >>= 1;
            if (exp == 0)
                break;
            a *= a;
        }
        return res;
    }
    
    public static long pow(long a, long b) {
        long res = 1, exp = b;
        while (exp != 0) {
            if ((exp & 1) != 0)
                res *= a;
            exp >>= 1;
            if (exp == 0)
                break;
            a *= a;
        }
        return res;
    }
    
//  stolen from: https://gist.github.com/leodutra/63ca94fe86dcffee1bab
    public static int min(int a, int b) {
        return a - ((a - b) & ((b - a) >> 31));
    }
    
    public static int max(int a, int b) {
        return -min(-a, -b);
    }
    
    public static int abs(int a) {
        return (a ^ (a >> 31)) + ((a >> 31) & 1);
    }
    
    public static long min(long a, long b) {
        return a - ((a - b) & ((b - a) >> 63));
    }
    
    public static long max(long a, long b) {
        return -min(-a, -b);
    }
    
    public static long abs(long a) {
        return (a ^ (a >> 63)) + ((a >> 63) & 1);
    }
    
    public static double log(double a, double base) {
        return Math.log(a) / Math.log(base);
    }
    
    public static void goThroughAllDecimalCodes(int numDigits, Consumer<int[]> handler) {
        final int[] code = new int[numDigits];
        final int[] pow = new int[numDigits];
        for (int i = 0; i < numDigits; i++) 
            pow[i] = pow(10, i);
        for (int i = 0; i < 10 * pow[numDigits - 1]; i++) {
            for (int j = 0; j < numDigits; j++) {
                code[numDigits - j - 1] = (i / pow[j]) % 10;
            }
            handler.accept(code);
        }
    }
    
    public static void getDecimalCode(int code, int numDigits, int[] dest) {
        getDecimalCode(code, numDigits, dest, 0);
    }
    
    public static void getDecimalCode(int code, int numDigits, int[] dest, int recordFromIndex) {
        if (recordFromIndex + numDigits > dest.length)
            throw new IllegalArgumentException();
        int pow = 1;
        for (int i = 0; i < numDigits; i++) {
            dest[recordFromIndex + numDigits - i - 1] = (code / pow) % 10;
            pow *= 10;
        }
    }
    
}
