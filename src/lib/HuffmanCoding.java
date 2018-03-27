package lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import lib.HuffmanCoding.CodingTree.Node;

/**
 *
 * @author Texhnolyze
 */
public final class HuffmanCoding {
    
    private HuffmanCoding() {}
    
    public static CodingTree encode(String message, OutputStream out) throws IOException {
        CodingTree tree = CodingTree.buildTree(message);
        encode(tree, message, out);
        return tree;
    }
    
    public static void encode(CodingTree tree, String message, OutputStream out) throws IOException {
        BitBuffer raw = new BitBuffer();
        Alphabet alphabet = tree.alphabet();
        BitBuffer[] table = tree.codingTable();
        for (int i = 0; i < message.length(); i++) 
            raw.append(table[alphabet.indexOf(message.charAt(i))]);
        raw.write(out);
    }
    
    public static String decode(CodingTree tree, InputStream in) throws IOException {
        BitBuffer bb = BitBuffer.read(in);
        StringBuilder sb = new StringBuilder();
        Node curr = tree.root;
        for (int i = 0; i < bb.numBits(); i++) {
            int bit = bb.bitAt(i);
            curr = curr.childs[bit];
            if (curr.isLeaf()) {
                sb.append(curr.c);
                curr = tree.root;
            }
        }
        return sb.toString();
    }
    
    public static class CodingTree {
        
        private Node root;
        private Alphabet alphabet; //transient
        private BitBuffer[] codingTable; //transient
        
        static class Node {
            char c;
            int freq; //transient
            Node[] childs;
            boolean isLeaf() {return childs == null;}
        }
        
        public Alphabet alphabet() {
            if (alphabet == null) {
                SortedSet<Character> chars = new TreeSet<>();
                collectChars(root, chars);
                alphabet = Alphabet.fromSortedSet(chars);
            }
            return alphabet;
        }
        
        private void collectChars(Node n, SortedSet<Character> dest) {
            if (n.isLeaf()) 
                dest.add(n.c);
            else {
                collectChars(n.childs[0], dest);
                collectChars(n.childs[1], dest);
            }
        }
        
        BitBuffer[] codingTable() {
            if (codingTable == null) {
                codingTable = new BitBuffer[alphabet.size()];
                buildTable(codingTable, new BitBuffer(1), root);
            }
            return codingTable;
        }
        
        private void buildTable(BitBuffer[] table, BitBuffer temp, Node n) {
            if (n.isLeaf()) {
                int index = alphabet.indexOf(n.c);
                table[index] = temp.copy();
            } else {
                buildTable(table, temp.append(0), n.childs[0]);
                temp.setNumSignificantBits(temp.numBits() - 1);
                buildTable(table, temp.append(1), n.childs[1]);
                temp.setNumSignificantBits(temp.numBits() - 1);
            }
        }
        
        public static CodingTree buildTree(String message) {
            Alphabet alphabet = Alphabet.fromCharArray(message.toCharArray());
            Node[] nodes = new Node[alphabet.size()];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = new Node();
                nodes[i].c = alphabet.charBy(i);
            }
            for (int i = 0; i < message.length(); i++) {
                int index = alphabet.indexOf(message.charAt(i));
                nodes[index].freq++;
            }
            PriorityQueue<Node> pq = new PriorityQueue<>((Node n1, Node n2) -> {
                return Integer.compare(n1.freq, n2.freq);
            });
            pq.addAll(Arrays.asList(nodes));
            while (pq.size() > 1) {
                Node n1 = pq.poll();
                Node n2 = pq.poll();
                Node parent = new Node();
                parent.childs = new Node[2];
                parent.childs[0] = n1;
                parent.childs[1] = n2;
                parent.freq = n1.freq + n2.freq;
                pq.add(parent);
            }
            CodingTree tree = new CodingTree();
            tree.root = pq.poll();
            tree.alphabet = alphabet;
            return tree;
        }
        
        public void write(OutputStream out) throws IOException {
            BitBuffer bb = new BitBuffer();
            writeNode(root, bb);
            bb.write(out);
        }
        
        private void writeNode(Node n, BitBuffer bb) {
            if (n.isLeaf()) {
                bb.append(1);
                bb.append(Integer.reverse(n.c) >>> 16, 16);
            } else {
                bb.append(0);
                writeNode(n.childs[0], bb);
                writeNode(n.childs[1], bb);
            }
        }
    
        public static CodingTree readTree(InputStream in) throws IOException {
            BitBuffer bb = BitBuffer.read(in);
            Node n = readNode(bb, 0).x();
            CodingTree tree = new CodingTree();
            tree.root = n;
            return tree;
        }
        
        private static Pair<Node, Integer> readNode(BitBuffer bb, int index) {
            if (bb.bitAt(index) == 1) {
                int charCode = bb.bitAt(index + 1);
                charCode = (charCode << 1) | bb.bitAt(index + 2);
                charCode = (charCode << 1) | bb.bitAt(index + 3);
                charCode = (charCode << 1) | bb.bitAt(index + 4);
                charCode = (charCode << 1) | bb.bitAt(index + 5);
                charCode = (charCode << 1) | bb.bitAt(index + 6);
                charCode = (charCode << 1) | bb.bitAt(index + 7);
                charCode = (charCode << 1) | bb.bitAt(index + 8);
                charCode = (charCode << 1) | bb.bitAt(index + 9);
                charCode = (charCode << 1) | bb.bitAt(index + 10);
                charCode = (charCode << 1) | bb.bitAt(index + 11);
                charCode = (charCode << 1) | bb.bitAt(index + 12);
                charCode = (charCode << 1) | bb.bitAt(index + 13);
                charCode = (charCode << 1) | bb.bitAt(index + 14);
                charCode = (charCode << 1) | bb.bitAt(index + 15);
                charCode = (charCode << 1) | bb.bitAt(index + 16);
                Node n = new Node();
                n.c = (char) charCode;
                return new Pair<>(n, index + 16);
            } else {
                Node parent = new Node();
                parent.childs = new Node[2];
                Pair<Node, Integer> left = readNode(bb, index + 1);
                parent.childs[0] = left.x();
                Pair<Node, Integer> right = readNode(bb, left.y() + 1);
                parent.childs[1] = right.x();
                return new Pair<>(parent, right.y());
            }
        }
        
    }
    
}
