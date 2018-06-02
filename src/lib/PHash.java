package lib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Texhnolyze
 */
public class PHash {
    
    private BitBuffer hash;
    
    private BufferedImage mask;
    private final BufferedImage img;
    
    public PHash(BufferedImage img) {
        this.img = img;
    }

    public BufferedImage img() {
        return img;
    }
    
    public BufferedImage mask() {
        if (hash == null)
            hash = calculateHash();
        return mask;
    }
    
    public BitBuffer hash() {
        if (hash == null)
            hash = calculateHash();
        return hash;
    }
    
    public int hammingDistance(PHash other) {
        return BitUtils.hammingDistance(hash(), other.hash());
    }
    
    private static final int SCALE_SIZE = 32;
    private static final int HALF_HALF_SCALE_SIZE = SCALE_SIZE / 4;
    
    private BufferedImage scale() {
        BufferedImage scaled = new BufferedImage(SCALE_SIZE, SCALE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(img, 0, 0, SCALE_SIZE, SCALE_SIZE, null);
        g.dispose();
        return scaled;
    }
    
    private void toGrayscale(BufferedImage img) {
        for (int x = 0; x < SCALE_SIZE; x++) {
            for (int y = 0; y < SCALE_SIZE; y++) {
                int rgb = img.getRGB(x, y);
                int b = rgb & 0xff;
                int g = (rgb & 0xff00) >> 8;
                int r = (rgb & 0xff0000) >> 16;
                int grayByte = toGrayByteValue(r, g, b);
                img.setRGB(x, y, grayByte);
            }
        }
    }
    
    private BufferedImage dct(BufferedImage src) {
        BufferedImage dct = new BufferedImage(SCALE_SIZE, SCALE_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < SCALE_SIZE; x++) {
            for (int y = 0; y < SCALE_SIZE; y++) {
                dct.setRGB(x, y, dct0(src, x, y));
            }
        }
        return dct;
    }
    
    private int dct0(BufferedImage img, int x, int y) {
        double cx = x == 0 ? Math.sqrt(1.0 / SCALE_SIZE) : Math.sqrt(2.0 / SCALE_SIZE);
        double cy = y == 0 ? Math.sqrt(1.0 / SCALE_SIZE) : Math.sqrt(2.0 / SCALE_SIZE);
        double d = 0.0;
        for (int k = 0; k < SCALE_SIZE; k++) {
            double s = 0.0;
            for (int l = 0; l < SCALE_SIZE; l++) {
                int c = img.getRGB(k, l) & 0xff;
                double ck = Math.cos(((2.0 * k + 1.0) * x * Math.PI) / (2.0 * SCALE_SIZE));
                double cl = Math.cos(((2.0 * l + 1.0) * y * Math.PI) / (2.0 * SCALE_SIZE));
                s += (c * ck * cl);
            }
            d += s;
        }
        return (int) (cx * cy * d);
    }
    
    private int calculateAVGColor(BufferedImage img) {
        int avg = 0;
        for (int x = 0; x < HALF_HALF_SCALE_SIZE; x++) {
            for (int y = 0; y < HALF_HALF_SCALE_SIZE; y++) {
                avg += (img.getRGB(x, y) & 0xff);
            }
        }
        avg -= (img.getRGB(0, 0) & 0xff);
        return (avg / (HALF_HALF_SCALE_SIZE * HALF_HALF_SCALE_SIZE));
    }
    
    private BitBuffer buildBitsChain(BufferedImage img, int avg) {
        BitBuffer bb = new BitBuffer((HALF_HALF_SCALE_SIZE * HALF_HALF_SCALE_SIZE) / BitBuffer.R);
        for (int x = 0; x < HALF_HALF_SCALE_SIZE; x++) {
            for (int y = 0; y < HALF_HALF_SCALE_SIZE; y++) {
                if ((img.getRGB(x, y) & 0xff) > avg) {
                    bb.append(1);
                    img.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    bb.append(0);
                    img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return bb;
    }
    
    private BitBuffer calculateHash() {
        BufferedImage scaled = scale();
        toGrayscale(scaled);
        BufferedImage dct = dct(scaled);
        mask = dct.getSubimage(0, 0, HALF_HALF_SCALE_SIZE, HALF_HALF_SCALE_SIZE);
        int avg = calculateAVGColor(mask);
        return buildBitsChain(mask, avg);
    }

    @Override
    public String toString() {
        return hash().toString();
    }
    
    private static final double GRAY_VECTOR_LEN = Math.sqrt(1 + 1 + 1);
    private static final double UNIT_GRAY_VECTOR_COMPONENT = 1.0 / GRAY_VECTOR_LEN;
    
    private static int toGrayByteValue(int r, int g, int b) {
        double rgbLenSqr = r * r + g * g + b * b;
        int x = -r;
        int y = -g;
        int z = -b;
        int i = y - z;
        int j = z - x;
        int k = x - y; // (i, j, k) is the cross product: (r, g, b) x (1, 1, 1)
        double crossLen = Math.sqrt(i * i + j * j + k * k);
        double distFromGrayLine = crossLen / GRAY_VECTOR_LEN;
        double len = Math.sqrt(rgbLenSqr - distFromGrayLine * distFromGrayLine);
        return (int) (UNIT_GRAY_VECTOR_COMPONENT * len);
    }
    
    public static double getSimilarityPercentage(PHash p1, PHash p2) {
        double d = (double) p1.hammingDistance(p2) / (SCALE_SIZE * SCALE_SIZE);
        return 1.0 - d;
    }
    
}
