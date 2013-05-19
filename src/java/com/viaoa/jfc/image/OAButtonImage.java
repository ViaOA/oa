package com.viaoa.jfc.image;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a 3D looking button Image
 * @author vvia
 *
 */
public class OAButtonImage implements java.io.Serializable {
    JPanel panel;
    float incDither = 0.0125f;  // amount to brighten color, from center to outer edges
    
    void drawButton(Graphics graphics, int width, int height, Color centerColor, Font font, Color fontColor, String text) {
        Graphics2D g = (Graphics2D) graphics;


        g.setColor(centerColor);
        float[] fs = new float[3];
        Color.RGBtoHSB(centerColor.getRed(), centerColor.getGreen(), centerColor.getBlue(), fs);

        float H = fs[0];
        float S = fs[1]; 
        float B = fs[2];

        int i = height/2;
        int j = i;


        int code;
        for ( ; i>2; i--, j++) {
            code = Color.HSBtoRGB(H,S,B);
            // code = code & 0x00FFFFFF; // clean up to byte
            
            g.setColor(new Color(code) );
            g.drawLine(0, i, width, i);
            if (j != i) g.drawLine(0, j, width, j);
            
            B += incDither;
            
            if (B > 1.0f) {
                B = 1.0f;
                S -= incDither;
                if (S < 0.0f) S = 0.0f;
            }
        }


        // Borders
        S =  fs[1] - (fs[1] / 2);
        B =  fs[2] + ((1.0f - fs[2]) / 2);

        float incS =  S / 4;
        float incB = (1.0f - B) / 4;
        
        for ( ; i>=0; i--) {
            code = Color.HSBtoRGB(H,S,B);
            // code = code & 0x00FFFFFF;
            g.setColor(new Color(code) );
            g.drawLine(0, i, width, i);

            B += incB;
            S -= incS;
        }


        S = fs[1];
        B = fs[2] - ((fs[2]) / 4);

        incB = B / 4;
        
        for (i=0; i<3; i++,j++) {
            code = Color.HSBtoRGB(H,S,B);
            // code = code & 0x00FFFFFF;
            g.setColor(new Color(code) );
            g.drawLine(0, j, width, j);

            B -= incB;
        }

        if (font != null) g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        
        
        int x = (width/2) - (w/2);
        int y = (height/2) + (h/2);
        y -= fm.getDescent();
        
        g.setColor(fontColor);
        g.drawString(text, x, y);

    }
    
/***    
    void setup() {
        panel = new JPanel() {
            public void paint(Graphics g) {

                Font font = this.getFont();
                font = new Font(font.getFamily(), Font.BOLD, 16);

                FontMetrics fm = this.getFontMetrics(font);
                String text = "Items & Catalogs";
                int w = fm.stringWidth(text) + 5;

                drawButton(g, w, 32, new Color(163,0,0), font, new Color(255,255,255), text);
                // drawButton(g, w, 32, new Color(42,44,51), font, new Color(255,204,51), text);
            }
        };    
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(panel);
    }



    public static void main(String[] argv) {
        ImageGenerator ig = new ImageGenerator();
        ig.setup();
        ig.setSize(new Dimension(500, 500));
        ig.setLocation(new Point(300, 20));
        ig.setVisible(true);
        ig.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });


/! COMMENT START
        String[] names = new String[] {"clientProgram","itemsCatalogs","orders","home","" };
        String[] texts = new String[] {"Clients/Programs","Items/Catalogs","Orders","Home","" };
        int h = 32;        
        int fontSize = 16;
        int gap = 12;
COMMENT END!/
        String[] names = new String[] { "catalog","item","manufactor","vendor","repGroup","itemOptionType" };
        String[] texts = new String[] { "catalogs","items","manufactors","vendors","rep groups"," item option types" };
        
        int h = 16;
        int fontSize = 10;
        int gap = 5;

        Font font = ig.getFont();
        font = new Font(font.getFamily(), Font.BOLD, fontSize);

        for (int i=0; i<names.length; i++) {
            String name = names[i];
            if (name.length() == 0) continue;
            String text = texts[i];
        

            FontMetrics fm = ig.getFontMetrics(font);
            int w = fm.stringWidth(text) + (gap*2);

            OAImageCreator ic = new OAImageCreator();

            try {
                ig.drawButton(ic.getGraphics(new Dimension(w,h)), w, h, new Color(163,0,0), font, new Color(255,255,255), text);
                ic.createGif("website\\new\\images\\"+name+".gif");

                ig.drawButton(ic.getGraphics(new Dimension(w,h)), w, h, new Color(44,42,51), font, new Color(255,204,51), text);
                ic.createGif("website\\new\\images\\"+name+"Over.gif");
            }
            catch (Exception e) {
                System.out.println("Error: "+e);
            }
        }
    }
*/
}

