package lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Texhnolyze
 */
public class BitBuffer  {
    
    private int[] buffer;
    
    private int bitIdx;
    private int intIdx;
    
    public BitBuffer() {this(4);}
    public BitBuffer(int initCap) {buffer = new int[initCap];}
    
    //returns the number of significant bits
    public int numBits() {
        return bitIdx;
    }
    
    // returns the number of completely filled integers
    public int numInts() {
        return intIdx;
    }
    
    
    public BitBuffer append(int bit) {
        if ((bitIdx + 1) >> 5 == buffer.length)
            resize(2 * buffer.length);
        buffer[intIdx] = buffer[intIdx] | (bit << 31 - (bitIdx % 32));
        intIdx = (++bitIdx) >> 5;
        return this;
    }
    
//  Adds n bits to the end of the buffer in order from the 
//  least significant bit to the highest
    public BitBuffer append(int bits, int n) {
        if (n <= 0 || n > 32) 
            throw new IllegalArgumentException("n must be > 0 and <= 32");
        if ((bitIdx + n) >> 5 == buffer.length) 
            resize(2 * buffer.length);
        int bitsRec = bitIdx % 32;
        int shift = min(n, 32 - bitsRec);
        int r = Integer.reverse(BitUtils.getBitsLow(bits, 0, shift - 1)) >>> bitsRec;
        buffer[intIdx] = buffer[intIdx] | r;
        bitIdx += n;
        intIdx = bitIdx >> 5;
        if (shift != n) {
            int l = Integer.reverse(BitUtils.getBitsLow(bits, shift, n - 1));
            buffer[intIdx] = buffer[intIdx] | l;
        }
        return this;
    }
    
    public BitBuffer append(BitBuffer src) {
        return append(src, 0, src.bitIdx - 1);
    }
    
    public BitBuffer append(BitBuffer src, int frombit, int tobit) {
        if (frombit < 0 || frombit > tobit || tobit >= src.bitIdx)
            throw new IllegalArgumentException("0 <= frombit <= tobit < src.numBits()");
        for (int i = frombit; i <= tobit; i++)
            append(src.bitAt0(i)); 
        return this;
    }
    
//  sets the number of significant bits to the sbits
    public BitBuffer setNumSignificantBits(int sbits) {
        if (sbits < 0 || sbits > bitIdx)
            throw new IllegalArgumentException("sbits must be in the range from 0 to the numBits().");
        if (sbits == bitIdx)
            return this;
        bitIdx = sbits;
        intIdx = bitIdx >> 5;
        if (intIdx + 1 < buffer.length / 4)
            resize(max(1, 2 * intIdx));
        return this;
    }
    
    public BitBuffer copy() {
        BitBuffer copy = new BitBuffer(bitIdx % 32 == 0 ? intIdx : intIdx + 1);
        System.arraycopy(buffer, 0, copy.buffer, 0, copy.buffer.length);
        copy.bitIdx = bitIdx;
        copy.intIdx = intIdx;
        return copy;
    }
    
//  returns a pair in which the key is an array of integers whose bits follow 
//  in the order of their recording from highest bit to the least, and the value is the 
//  number of recorded bits in the last element of the array
    public Pair<int[], Integer> raw() {
        int[] raw = new int[intIdx + 1];
        System.arraycopy(buffer, 0, raw, 0, intIdx);
        if (bitIdx % 32 != 0) 
            raw[intIdx] = buffer[intIdx];
        return new Pair<>(raw, bitIdx % 32);
    }
    
    private void resize(int cap) {
        int[] b = new int[cap];
        System.arraycopy(buffer, 0, b, 0, intIdx);
        if (bitIdx % 32 != 0) b[intIdx] = buffer[intIdx];
        buffer = b;
    }
    
    public int bitAt(int idx) {
        if (idx < 0 || idx >= bitIdx) 
            throw new IndexOutOfBoundsException(idx + "");
        return bitAt0(idx);
    }
    
    private int bitAt0(int idx) {
        int iidx = idx >> 5;
        return BitUtils.valueAt(31 - (idx % 32), buffer[iidx]);
    }
    
    public BitBuffer setBitAt(int idx, int val) {
        if (idx >= bitIdx) 
            throw new IndexOutOfBoundsException(idx + "");
        int iidx = idx >> 5;
        buffer[iidx] = BitUtils.setBit(31 - (idx % 32), val, buffer[iidx]);
        return this;
    }
	
    public void write(OutputStream out) throws IOException {
        BufferedOutputStream bout = new BufferedOutputStream(out);
        writeInt(bout, bitIdx);
        for (int iidx = 0; iidx < intIdx; iidx++) 
            writeInt(bout, buffer[iidx]);
        if (bitIdx % 32 != 0) 
            writeInt(bout, buffer[intIdx]);
        bout.flush();
    }
    
    public static BitBuffer read(InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        int bidx = readInt(bin);
        BitBuffer bb = new BitBuffer(bidx % 32 == 0 ? bidx >> 5 : (bidx >> 5) + 1);
        bb.bitIdx = bidx;
        bb.intIdx = bidx >> 5;
        for (int iidx = 0; iidx < bb.intIdx; iidx++) 
            bb.buffer[iidx] = readInt(bin);
        if (bidx % 32 != 0) 
            bb.buffer[bb.intIdx] = readInt(bin);
        return bb;
    }
    
    private static int readInt(InputStream in) throws IOException {
        int i = in.read();
        i = (i << 8) | in.read();
        i = (i << 8) | in.read();
        i = (i << 8) | in.read();
        return i;
    }

    private static void writeInt(OutputStream out, int i) throws IOException {
        out.write(i >> 24);
        out.write(i >> 16);
        out.write(i >> 8);
        out.write(i);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < intIdx; i++) 
            sb.append(BitUtils.toFullBinaryString(buffer[i]));
        for (int i = 31; i > 31 - (bitIdx % 32); i--) 
            sb.append(BitUtils.valueAt(i, buffer[intIdx]));
        sb.append(']');
        return sb.toString();
    }
    
//  stolen from: https://gist.github.com/leodutra/63ca94fe86dcffee1bab
    private static int min(int a, int b) {
        return a - ((a - b) & ((b - a) >> 31));
    }
    
    private static int max(int a, int b) {
        return -min(-a, -b);
    }
    
}
