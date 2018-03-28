
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import lib.BitBuffer;


public class Sandbox {

    public static void main(String[] args) throws IOException {
        
        BitBuffer bb = new BitBuffer();
        bb.append(1543223423, 32);
        System.out.println(bb);
        bb.write(new FileOutputStream("123"));
        bb = BitBuffer.read(new FileInputStream("123"));
        System.out.println(bb);
        
        
    }
    
}
