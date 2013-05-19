package com.dispatcher.editor;


import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class OACaret extends DefaultCaret {

	private float caretWidth = 2.0f;
    Stroke stroke;

    public OACaret(float width) {
        setWidth(width);
    }
    
    public void setWidth(float width) {
        stroke = new BasicStroke(caretWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);        
    }
    
    public void paint(Graphics g) {
        ((Graphics2D)g).setStroke(stroke);
        super.paint(g);
    }    

}