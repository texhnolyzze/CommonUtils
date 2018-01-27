package sandbox;

import java.io.IOException;
import java.util.Collections;
import lib.ProxyExtractor;
import lib.ProxyExtractor.ExtractedProxy;

/**
 *
 * @author Texhnolyze
 */
public class Sandbox {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        Matcher m = PART1.matcher("41.62.58");
//        System.out.println(m.matches());
        ProxyExtractor pe = new ProxyExtractor(Collections.EMPTY_MAP);
        int extracted = pe.extract();
        for (ExtractedProxy ep : pe.getExtractedProxies()) {
            System.out.println(ep.getProps());
        }
        
        
    }
    
    
    
}
