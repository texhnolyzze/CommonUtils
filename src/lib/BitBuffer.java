package lib;

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
        
        if (bitIdx == 0) throw new IllegalStateException("There is no data in buffer to write.");
        
        out.write(bitIdx >> 24); 
        out.write(bitIdx >> 16);
        out.write(bitIdx >> 8);
        out.write(bitIdx);
        
        byte[] b = new byte[bitIdx % 64 == 0 ? 8 * longIdx : 8 * longIdx + (((bitIdx - 1) % 64) / 8) + 1];
        
        for (int lidx = 0; lidx < longIdx; lidx++) {
            long l = buffer[lidx];
            int offset = lidx << 3;
            int from = 56, to = 63;
            for (int bidx = 0; bidx < 8; bidx++) {
                b[offset + bidx] = (byte) BitUtils.getBitsLow(l, from, to);
                from -= 8;
                to -= 8;
            }
        }

        if (bitIdx % 64 != 0) {
            long l = buffer[longIdx];
            int bidx = 8 * longIdx;
            int to = (bitIdx % 64) - 1;
            for (int i = to, j = 7; i >= 0; i--) {
                b[bidx] = (byte) BitUtils.setBit(j, BitUtils.valueAt(i, l), b[bidx]);
                j--;
                if (j == -1) {
                    j = 7;
                    bidx++;
                }
            }
        }
        
        out.write(b);

    }
    
    public static BitBuffer read(InputStream in) throws IOException {
        
        int bidx = in.read();
        bidx = (bidx << 8) | in.read();
        bidx = (bidx << 8) | in.read();
        bidx = (bidx << 8) | in.read();
        
        BitBuffer bb = new BitBuffer(bidx % 64 == 0 ? bidx >> 6 : (bidx >> 6) + 1);
        bb.bitIdx = bidx;
        bb.longIdx = bb.buffer.length - 1;
        
        for (int i = 0; i < bb.longIdx; i++) {
            long l = in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            l = (l << 8) | in.read();
            bb.buffer[i] = l;
        }
        
        if (bidx % 64 != 0) {
            int b = 0;
            long l = 0;
            while ((b = in.read()) != -1) l = (l << 8) | b;
            bb.buffer[bb.longIdx] = l;
        }
        
        return bb;
        
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        for (int i = 0; i < longIdx; i++) 
            sb.append(BitUtils.toFullBinaryString(buffer[i]));
        
        long l = buffer[longIdx];
        for (int i = (bitIdx % 64) - 1; i >= 0; i--) sb.append(BitUtils.valueAt(i, l));
        sb.append(']');
        return sb.toString();
    }
    
    // stolen from: https://gist.github.com/leodutra/63ca94fe86dcffee1bab
    private static int min(int a, int b) {
        return a - ((a - b) & ((b - a) >> 31));
    }
    
}
