package lib;

/**
 *
 * @author Texhnolyze
 */
public class BitUtils {
    
    private BitUtils() {}
        
    public static long valueAt(int idx, long n) {
        return (n >> idx) & 1L; 
    }

    public static int valueAt(int idx, int n) {
        return (n >> idx) & 1; 
    }

    public static long setToOneAt(int idx, long n) {
        return n | (1L << idx);
    }

    public static int setToOneAt(int idx, int n) {
        return n | (1 << idx);
    }

    public static long setToZeroAt(int idx, long n) {
        return n & ~(1L << idx);
    }

    public static int setToZeroAt(int idx, int n) {
        return n & ~(1 << idx);
    }
    
    public static long setBit(int idx, long bit, long n) {
        return (n & ~(1L << idx)) | (bit << idx);
    }
    
    public static int setBit(int idx, int bit, int n) {
        return (n & ~(1 << idx)) | (bit << idx);
    }
    
    public static long toggle(int idx, long n) {
        return n ^ ~(1L << idx);
    }
    
    public static int toggle(int idx, int n) {
        return n ^ ~(1 << idx);
    }
    
//  returns bits of a number n in the range (fromIdx, toIdx) 
//  shifted to the low-order bit
    public static long getBitsLow(long n, int fromIdx, int toIdx) {
        return (n << (63 - toIdx)) >>> (63 - (toIdx - fromIdx));
    }
    
    public static int getBitsLow(int n, int fromIdx, int toIdx) {
        return (n << (31 - toIdx)) >>> (31 - (toIdx - fromIdx));
    }
    
//  returns bits of a number n in the range (fromIdx, toIdx) 
//  shifted to the high-order bit
    public static long getBitsHigh(long n, int fromIdx, int toIdx) {
        return (n >>> toIdx) << (63 - (toIdx - fromIdx));
    }
    
    public static int getBitsHigh(int n, int fromIdx, int toIdx) {
        return (n >>> toIdx) << (31 - (toIdx - fromIdx));
    }
    
    public static String toFullBinaryString(long n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 63; i >= 0; i--) sb.append(valueAt(i, n));
        return sb.toString();
    }
    
    public static String toFullBinaryString(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 0; i--) sb.append(valueAt(i, n));
        return sb.toString();
    }
    
}
