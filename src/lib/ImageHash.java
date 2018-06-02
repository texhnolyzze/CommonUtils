package lib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Texhnolyze
 */
public class ImageHash {
    
    private BitBuffer hash;
    
    private BufferedImage mask;
    private final BufferedImage img;
    
    public ImageHash(BufferedImage img) {
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
    
    public int hammingDistance(ImageHash other) {
        return BitUtils.hammingDistance(hash(), other.hash());
    }
    
    private static final int SCALE_SIZE = 8;
    
    private BufferedImage scale() {
        BufferedImage scaled = new BufferedImage(SCALE_SIZE, SCALE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(img, 0, 0, SCALE_SIZE, SCALE_SIZE, null);
        g.dispose();
        return scaled;
    }
    
    private void toGrayscale(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int b = rgb & 0xff;
                int g = (rgb & 0xff00) >> 8;
                int r = (rgb & 0xff0000) >> 16;
                int grayByte = toGrayByteValue(r, g, b);
                int color = (((grayByte << 8) | grayByte) << 8) | grayByte;
                img.setRGB(x, y, color);
            }
        }
    }
    
    private int calculateAVGColor(BufferedImage img) {
        int avg = 0;
        for (int x = 0; x < SCALE_SIZE; x++) {
            for (int y = 0; y < SCALE_SIZE; y++) {
                avg += (img.getRGB(x, y) & 0xff);
            }
        }
        return (avg / (SCALE_SIZE * SCALE_SIZE));
    }
    
    private BitBuffer buildBitsChain(BufferedImage img, int avg) {
        BitBuffer bb = new BitBuffer((SCALE_SIZE * SCALE_SIZE) / BitBuffer.R);
        for (int x = 0; x < SCALE_SIZE; x++) {
            for (int y = 0; y < SCALE_SIZE; y++) {
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
        mask = scale();
        toGrayscale(mask);
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
    
    public static double getSimilarityPercentage(ImageHash h1, ImageHash h2) {
        double d = (double) h1.hammingDistance(h2) / (SCALE_SIZE * SCALE_SIZE);
        return 1.0 - d;
    }
    
}
