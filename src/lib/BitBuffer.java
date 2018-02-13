package lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javafx.util.Pair;

/**
 *
 * @author Texhnolyze
 */
public class BitBuffer  {
    
    private long[] buffer; // the maximum capacity of the buffer is 256 megabytes
    
    private int bitIdx;
    private int longIdx;
    
    public BitBuffer() {this(32);}
    public BitBuffer(int initCap) {buffer = new long[initCap];}
    
    //returns the number of bits
    public int numBits() {
        return bitIdx;
    }
    
    // returns the number of completely filled longs
    public int numLongs() {
        return longIdx;
    }
    
    
    public void append(int bit) {
        if ((bitIdx + 1) >> 6 == buffer.length)
            resize(2 * buffer.length);
        buffer[longIdx] = (buffer[longIdx] << 1) | bit;
        longIdx = (++bitIdx) >> 6;
    }
    
//  Adds bits to the end of the buffer in order from the 
//  least significant bit to the highest
    public void append(long bits, int count) {
        if ((bitIdx + count) >> 6 == buffer.length) 
            resize(2 * buffer.length);
        int shift = min(count, 64 - (bitIdx % 64));
        long r = Long.reverse(BitUtils.getBitsLow(bits, 0, shift - 1)) >>> (64 - shift);
        buffer[longIdx] = (buffer[longIdx] << shift) | r;
        bitIdx += count;
        longIdx = bitIdx >> 6;
        if (count != shift) {
            long l = Long.reverse(BitUtils.getBitsLow(bits, shift, count - 1)) >>> (64 - (count - shift));
            buffer[longIdx] = buffer[longIdx] | l;
        }
    }
    
//  returns a pair in which the key is an array of longs whose bits follow 
//  in the order of their recording from highest bit to the least, and the value is the 
//  number of recorded bits in the last element of the array
    public Pair<long[], Integer> raw() {
        long[] raw = new long[bitIdx % 64 == 0 ? longIdx : longIdx + 1];
        System.arraycopy(buffer, 0, raw, 0, longIdx);
        if (bitIdx % 64 != 0) raw[longIdx] = buffer[longIdx] << (64 - (bitIdx % 64));
        return new Pair<>(raw, bitIdx % 64);
    }
    
    private void resize(int cap) {
        long[] b = new long[cap];
        System.arraycopy(buffer, 0, b, 0, longIdx);
        if (bitIdx % 64 != 0) b[longIdx] = buffer[longIdx];
        buffer = b;
    }
    
    public long getBitAt(int idx) {
        if (idx >= bitIdx) throw new IndexOutOfBoundsException(idx + "");
        int lIdx = idx >> 6;
        if (lIdx == longIdx) 
            return BitUtils.valueAt((bitIdx % 64) - (idx % 64) - 1, buffer[lIdx]);
        else 
            return BitUtils.valueAt(63 - (idx % 64), buffer[lIdx]);
    }
    
    public void setBitAt(int idx, int val) {
        if (idx >= bitIdx) throw new IndexOutOfBoundsException(idx + "");
        else {
            int lIdx = idx >> 6;
            if (lIdx == longIdx) 
                buffer[lIdx] = BitUtils.setBit((bitIdx % 64) - (idx % 64) - 1, val, buffer[lIdx]);
            else 
                buffer[lIdx] = BitUtils.setBit(63 - (idx % 64), val, buffer[lIdx]);
        }
    }
	
    public void write(OutputStream out) throws IOException {
        
        BufferedOutputStream bout = new BufferedOutputStream(out);
        
        if (bitIdx == 0) throw new IllegalStateException("There is no data in buffer to write.");
        
        bout.write(bitIdx >> 24); 
        bout.write(bitIdx >> 16);
        bout.write(bitIdx >> 8);
        bout.write(bitIdx);

        for (int lidx = 0; lidx < longIdx; lidx++) {
            long l = buffer[lidx];
            int from = 56, to = 63;
            for (int bidx = 0; bidx < 8; bidx++) {
                bout.write((int) BitUtils.getBitsLow(l, from, to));
                from -= 8;
                to -= 8;
            }
        }

        if (bitIdx % 64 != 0) {
            long l = buffer[longIdx];
            int b = 0;
            int j = 7;
            for (int i = (bitIdx % 64) - 1; i >= 0; i--) {
                int bit = (int) BitUtils.valueAt(i, l);
                b = BitUtils.setBit(j, bit, b);
                j--;
                if (j == -1) {
                    bout.write(b);
                    b = 0;
                    j = 7;
                }
            }
            if (j != 7) bout.write(b);
        }
        
        bout.flush();
        
    }
    
    public static BitBuffer read(InputStream in) throws IOException {
        
        BufferedInputStream bin = new BufferedInputStream(in);
        
        int bidx = bin.read();
        bidx = (bidx << 8) | bin.read();
        bidx = (bidx << 8) | bin.read();
        bidx = (bidx << 8) | bin.read();
        
        BitBuffer bb = new BitBuffer(bidx % 64 == 0 ? bidx >> 6 : (bidx >> 6) + 1);
        bb.bitIdx = bidx;
        bb.longIdx = bidx >> 6;
        
        for (int i = 0; i < bb.longIdx; i++) {
            long l = bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            l = (l << 8) | bin.read();
            bb.buffer[i] = l;
        }
        if (bidx % 64 != 0) {
            int recorded = (bidx - 1) % 8 + 1;
            int b;
            long l = 0;
            while ((b = bin.read()) != -1) l = (l << 8) | b;
            bb.buffer[bb.longIdx] = l >> 8 - recorded;
        }
        
        return bb;
        
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        for (int i = 0; i < longIdx; i++) 
            sb.append(BitUtils.toFullBinaryString(buffer[i]));
        
        for (int i = (bitIdx % 64) - 1; i >= 0; i--) 
            sb.append(BitUtils.valueAt(i, buffer[longIdx]));
        
        sb.append(']');
        return sb.toString();
    }
    
    // stolen from: https://gist.github.com/leodutra/63ca94fe86dcffee1bab
    private static int min(int a, int b) {
        return a - ((a - b) & ((b - a) >> 31));
    }
    
}
