package sandbox;

import javafx.scene.canvas.GraphicsContext;
import sandbox.Sandbox.Graphics.DefaultGraphics;

public class Sandbox {

    public static void main(String[] args) {
        Sandbox s = new Sandbox();
        s.drawLine(0, 0, 3, 5);
        GraphicsContext g;
        java.awt.Graphics gd;
    }
    
    private static final int X_LESS_0 = 8,
                             Y_LESS_0 = 2,
                             X_GREATER_W_MINUS_1 = 4,
                             Y_GREATER_H_MINUS_1 = 1;

    private Graphics g = new DefaultGraphics();
    
    private int w = 640, h = 480;
    private int w_minus_1 = w - 1, h_minus_1 = h - 1;

    private int code_1, code_2;
    private int temp_x, temp_y;
    private float temp_z;

    private int line_x1, line_y1, line_x2, line_y2; // store clipped line coordinates here
    
    public void drawLine(int x1, int y1, int x2, int y2) {
        code_1 = code(x1, y1);
        code_2 = code(x2, y2);
        clip(x1, y1, x2, y2);
        if (line_x1 != -1)
            bresenham(line_x1, line_y1, line_x2, line_y2);
    }
    
    private void clip(int x1, int y1, int x2, int y2) {
        for (;;) {
            if ((code_1 | code_2) == 0) {
                line_x1 = x1;
                line_x2 = x2;
                line_y1 = y1;
                line_y2 = y2;
                break;
            } else {
                if ((code_1 & code_2) == 0) {
                    if (code_1 != 0) {
                        transfer(x1, y1, x2, y2, code_1);
                        x1 = temp_x;
                        y1 = temp_y;
                        code_1 = code(x1, y1);
                    } else { // code_2 != 0
                        transfer(x2, y2, x1, y1, code_2);
                        x2 = temp_x;
                        y2 = temp_y;
                        code_2 = code(x2, y2);
                    }
                } else {
                    line_x1 = -1; // line is outside the screen
                    break;
                }
            }
        }
    }
    
    private void transfer(int x, int y, int rx, int ry, int code) {
        switch (code) {
            case X_LESS_0:
                x_less_0(x, y, rx, ry);
                break;
            case X_GREATER_W_MINUS_1:
                x_greater_w_minus_1(x, y, rx, ry);
                break;
            case Y_LESS_0:
                y_less_0(x, y, rx, ry);
                break;
            case Y_GREATER_H_MINUS_1:
                y_greater_h_minus_one(x, y, rx, ry);
                break;
            case (X_LESS_0 | Y_LESS_0):
                x_less_0(x, y, rx, ry);
                break;
            case (X_GREATER_W_MINUS_1 | Y_GREATER_H_MINUS_1):
                x_greater_w_minus_1(x, y, rx, ry);
                break;
            case (X_LESS_0 | Y_GREATER_H_MINUS_1):
                y_greater_h_minus_one(x, y, rx, ry);
                break;
            case (X_GREATER_W_MINUS_1 | Y_LESS_0):
                y_less_0(x, y, rx, ry);
                break;
            default: // can't be
                break;
        }
    }
	
    private void x_less_0(float x, float y, float rx, float ry) {
        temp_x = 0;
        temp_y = (int) Math.round(ry + (y - ry) * -(rx / (x - rx)));
    }

    private void y_less_0(float x, float y, float rx, float ry) {
        temp_x = (int) Math.round(rx + (x - rx) * -(ry / (y - ry)));
        temp_y = 0;
    }

    private void x_greater_w_minus_1(float x, float y, float rx, float ry) {
        temp_x = w_minus_1;
        temp_y = (int) Math.round(ry + (y - ry) * ((w_minus_1 - rx) / (x - rx)));
    }

    private void y_greater_h_minus_one(float x, float y, float rx, float ry) {
        temp_x = (int) Math.round(rx + (x - rx) * ((h_minus_1 - ry) / (y - ry)));
        temp_y = h_minus_1;
    }

    private int code(int x, int y) {
        int code = 0;
        if (x < 0) 
            code = X_LESS_0;
        else if (x >= w)
            code = X_GREATER_W_MINUS_1;
        if (y < 0)
            code |= Y_LESS_0;
        else if (y >= h)
            code |= Y_GREATER_H_MINUS_1;
        return code;
    }
    
    private void bresenham(int x1, int y1, int x2, int y2) {
        if (x1 == x2) { // vertical line
            if (y1 == y2)
                g.plot(x1, y1);
            else {
                if (y1 < y2) ver_line(x1, y1, y2);
                else         ver_line(x1, y2, y1);
            }
        } else if (y1 == y2) { // horizontal line
            if (x1 < x2) hor_line(x1, x2, y1);
            else         hor_line(x2, x1, y1);
        } else {
            if (x1 > x2) {
                temp_x = x1;
                temp_y = y1;
                x1 = x2;
                y1 = y2;
                x2 = temp_x;
                y2 = temp_y;
            }
            int dx = x2 - x1, dy;
            if (y2 > y1) {
                dy = y2 - y1;    
                if (dx > dy)      x_line(x1, x2, y1, dx, dy, 1);
                else if (dx < dy) y_line(y1, y2, x1, dx, dy, 1);
                else              line_45(x1, y1, x2, 1);
                
            } else {
                dy = y1 - y2;
                if (dx > dy)      x_line(x1, x2, y1, dx, dy, -1);
                else if (dx < dy) y_line(y1, y2, x1, dx, dy, -1);
                else              line_45(x1, y1, x2, -1);
            }
        }
    }
    
    private void ver_line(int x, int y1, int y2) {
        for (int y = y1;;) {
            g.plot(x, y++);
            if (y > y2)
                break;
        }
    }
    
    private void hor_line(int x1, int x2, int y) {
        for (int x = x1;;) {
            g.plot(x++, y);
            if (x > x2)
                break;
        }
    }
    
    private void line_45(int x1, int y1, int x2, int dy) {
        for (int x = x1, y = y1;;) {
            g.plot(x++, y);
            y += dy;
            if (x > x2)
                break;
        }
    }
    
    private void x_line(int x1, int x2, int y1, int dx, int dy, int inc) {
        for (int x = x1, y = y1, err = 0;;) {
            g.plot(x++, y);
            if (x > x2)
                break;
            err += dy;
            if (err << 1 >= dx) {
                y += inc;
                err -= dx;
            }
        }
    }
    
    private void y_line(int y1, int y2, int x1, int dx, int dy, int inc) {
        for (int y = y1, x = x1, err = 0;;) {
            g.plot(x, y++);
            if (y > y2)
                break;
            err += dx;
            if (err << 1 >= dy) {
                x += inc;
                err -= dy;
            }
        }
    }
    
    public static interface Graphics {
        
        void plot(int x, int y);
        void setColor(int rgb);
        
        default void setColor(int r, int g, int b) {
            setColor(((r << 16) | (g << 8)) | b);
        }
        
        class DefaultGraphics implements Graphics {

            @Override
            public void plot(int x, int y) {
                System.out.println(x + " " + y);
            }

            @Override
            public void setColor(int rgb) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        }
        
    }
    
}
