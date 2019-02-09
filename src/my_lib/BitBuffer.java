package my_lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static my_lib.MathUtils.*;

/**
 *
 * @author Texhnolyze
 */
public class BitBuffer  {
    
    public static final int R = 32;
    private static final int SHIFT = 5;
    
    private int[] buffer;
    
    private int bitIdx;
    private int intIdx;
    
    public BitBuffer() {this(4);}
    public BitBuffer(int initCap) {buffer = new int[max(1, initCap)];}
    
//  returns the number of significant bits
    public int numBits() {
        return bitIdx;
    }
    
// returns the number of completely filled integers
    public int numInts() {
        return intIdx;
    }
    
    public BitBuffer append(int bit) {
        if ((bitIdx + 1) >> SHIFT == buffer.length)
            resize(2 * buffer.length);
        buffer[intIdx] = buffer[intIdx] | (bit << (R - 1) - (bitIdx % R));
        intIdx = (++bitIdx) >> SHIFT;
        return this;
    }
    
//  Adds n bits to the end of the buffer in order from the 
//  least significant bit to the highest
//  for example, if you add n=14, which is "1110" and your buffer now empty, the buffer will become "0111"
    public BitBuffer append(int bits, int n) {
        if (n <= 0 || n > R) 
            throw new IllegalArgumentException("n must be > 0 and <= " + R);
        if ((bitIdx + n) >> SHIFT == buffer.length) 
            resize(2 * buffer.length);
        int bitsRec = bitIdx % R;
        int shift = min(n, R - bitsRec);
        int r = Integer.reverse(BitUtils.getBitsLow(bits, 0, shift - 1)) >>> bitsRec;
        buffer[intIdx] = buffer[intIdx] | r;
        bitIdx += n;
        intIdx = bitIdx >> SHIFT;
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
        int n = tobit - frombit + 1;
        for (int iidx = frombit >> SHIFT, bitidx = (R - 1) - frombit % R;; iidx++) {
            int record = min(n, bitidx + 1);
            append(Integer.reverse(src.buffer[iidx]) >>> ((R - 1) - bitidx), record);
            n -= record;
            if (n == 0)
                break;
            bitidx = R - 1;
        }
        return this;
        
    }
    
//  sets the number of significant bits to the sbits
    public BitBuffer setNumSignificantBits(int sbits) {
        if (sbits < 0 || sbits > bitIdx)
            throw new IllegalArgumentException("sbits must be in the range from 0 to the numBits().");
        if (sbits == bitIdx)
            return this;
        bitIdx = sbits;
        intIdx = bitIdx >> SHIFT;
        if (intIdx + 1 < buffer.length / 4)
            resize(max(1, 2 * intIdx));
        return this;
    }
    
    public BitBuffer copy() {
        BitBuffer copy = new BitBuffer(bitIdx % R == 0 ? intIdx : intIdx + 1);
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
        if (bitIdx % R != 0) 
            raw[intIdx] = buffer[intIdx];
        return new Pair<>(raw, bitIdx % R);
    }
    
    private void resize(int cap) {
        int[] b = new int[cap];
        System.arraycopy(buffer, 0, b, 0, intIdx);
        if (bitIdx % R != 0) b[intIdx] = buffer[intIdx];
        buffer = b;
    }
    
    public int bitAt(int idx) {
        if (idx < 0 || idx >= bitIdx) 
            throw new IndexOutOfBoundsException(idx + "");
        return bitAt0(idx);
    }
    
    private int bitAt0(int idx) {
        int iidx = idx >> SHIFT;
        return BitUtils.valueAt((R - 1) - (idx % R), buffer[iidx]);
    }
    
    public BitBuffer setBitAt(int idx, int val) {
        if (idx >= bitIdx) 
            throw new IndexOutOfBoundsException(idx + "");
        int iidx = idx >> SHIFT;
        buffer[iidx] = BitUtils.setBit((R - 1) - (idx % R), val, buffer[iidx]);
        return this;
    }
	
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(bitIdx);
        for (int iidx = 0; iidx < intIdx; iidx++) 
            dos.writeInt(buffer[iidx]);
        if (bitIdx % R != 0) 
            dos.writeInt(buffer[intIdx]);
        dos.flush();
    }
    
    public static BitBuffer read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int bidx = dis.readInt();
        BitBuffer bb = new BitBuffer(bidx % R == 0 ? bidx >> SHIFT : (bidx >> SHIFT) + 1);
        bb.bitIdx = bidx;
        bb.intIdx = bidx >> SHIFT;
        for (int iidx = 0; iidx < bb.intIdx; iidx++) 
            bb.buffer[iidx] = dis.readInt();
        if (bidx % R != 0) 
            bb.buffer[bb.intIdx] = dis.readInt();
        return bb;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < intIdx; i++) 
            sb.append(BitUtils.toFullBinaryString(buffer[i]));
        for (int i = R - 1; i > (R - 1) - (bitIdx % R); i--) 
            sb.append(BitUtils.valueAt(i, buffer[intIdx]));
        sb.append(']');
        return sb.toString();
    }
    
}
