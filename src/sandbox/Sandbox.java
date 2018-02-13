package sandbox;

import java.io.IOException;
import java.sql.SQLException;
import lib.BitBuffer;

/**
 *
 * @author Texhnolyze
 */
public class Sandbox {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        
        BitBuffer bb = new BitBuffer();
        bb.append(1);
        bb.append(1);
        bb.append(1);
        bb.append(1);
        bb.append(0);
        bb.append(0);
        bb.append(0);
        bb.append(0);
        bb.append(Long.MIN_VALUE, 64);
        System.out.println(bb);
        
    }
    
    
}
