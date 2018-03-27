
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import lib.Alphabet;
import lib.HuffmanCoding;
import lib.HuffmanCoding.CodingTree;


public class Sandbox {

    public static void main(String[] args) throws IOException {
        
        CodingTree tree = CodingTree.buildTree("ABRACADABRA");
        tree.write(new FileOutputStream("123"));
        tree = CodingTree.readTree(new FileInputStream("123"));
        Alphabet a = tree.alphabet();
        System.out.println(a.contains('A'));
        HuffmanCoding.encode(tree, "ABRACADABRA", new FileOutputStream("123"));
        System.out.println(HuffmanCoding.decode(tree, new FileInputStream("123")));
        
    }
    
}
