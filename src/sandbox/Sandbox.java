package sandbox;

import lib.BitBuffer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Texhnolyze
 */
public class Sandbox {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        
        BitBuffer bb = new BitBuffer(1);
       
        bb.append(Long.MAX_VALUE, 64);
        System.out.println(bb.toString());

        bb.write(new FileOutputStream("file"));
        bb = BitBuffer.read(new FileInputStream("file"));
        

        System.out.println(bb.toString());
        

//
//        bb.append(Long.MAX_VALUE, 64);
//
//        bb.append(Long.MAX_VALUE, 64);
//        bb.append(Long.MAX_VALUE, 64);
//        
//        bb.append(1);
//        System.out.println(bb.toString());
//        
//        System.out.println(Arrays.toString(bb.raw()));
        
    }
    
    
    
}
