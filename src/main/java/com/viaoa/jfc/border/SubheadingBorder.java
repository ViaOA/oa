/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jfc.border;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.border.*;
import javax.swing.*;


/**
 * Border that uses a label for heading.
 * @author vvia
 */
public class SubheadingBorder extends AbstractBorder {
	private JLabel lbl;
	private Color c1, c2;
	private Dimension dim;
	

	public SubheadingBorder(String text) {
		this(text, JLabel.LEFT);
	}

	public SubheadingBorder(String text, int align) {
        
    	lbl = new JLabel() {
            public void paintComponent(Graphics gr) {
                Graphics2D g = (Graphics2D) gr;
                Dimension d = this.getSize();
                Paint p = g.getPaint();
				
                // GradientPaint gp = new GradientPaint(0,0, c1, d.width, d.height, c2, true);
                GradientPaint gp = new GradientPaint(0,-(int)(d.height*.20), c2, 0, (int)(d.height*.60), c1, true);

                g.setPaint(gp);
                g.fill(new Rectangle(d));
                g.setPaint(p);
                super.paintComponent(g);
            }
    	};
		lbl.setText(text);
    	updateUI();
        lbl.setBackground(c2);
        lbl.setOpaque(true);
        
		lbl.setHorizontalAlignment(align);
		lbl.setVerticalAlignment(JLabel.CENTER);
		lbl.setBorder(new AbstractBorder() {
			Insets insets = new Insets(0, 15, 0, 0);
			@Override
			public Insets getBorderInsets(Component c) {
				return insets;
			}
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,int width, int height) {
				g.setColor(c1);
				g.drawLine(x, y+height-1, x+width-1, y+height-1);
			}
		});
		lbl.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 0), lbl.getBorder())); // hack to get indent to work in first border
	}
	
	public void updateUI() {
        c1 = (Color) UIManager.getDefaults().get("InternalFrame.inactiveTitleBackground");
        c2 = c1.brighter();
        setText(lbl.getText());
	}
	
	public void setText(String txt) {
		lbl.setText(txt);
		dim = lbl.getPreferredSize();
		dim.height += (int) ((double)dim.height * .22);
		lbl.setSize(dim);
	}
	
	public String getText() {
		return lbl.getText();
	}
	
	@Override
	public Insets getBorderInsets(Component c) {
		Insets insets = new Insets(dim.height+1,0,0,0);
		return insets;
	}
	
	@Override
	public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
		Graphics2D g = (Graphics2D) gr;
		
		g.setColor(Color.white);
		g.fillRoundRect(x, y, width-1, height-1, 8, 8);

		lbl.setSize(width, lbl.getHeight());
		gr.translate(x, y);
		lbl.paint(g);
		gr.translate(-x, -y);
	}
	
	
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
        	System.out.println("Error: "+e);
        }
    	
        JDialog dlg = new JDialog((Window)null, "Microsoft Outlook");
        JPanel pan = new JPanel();
        SubheadingBorder b = new SubheadingBorder("All Mail Folders");
        Border bx = new CompoundBorder(new EmptyBorder(10,10,10,10), b);
        pan.setBorder(bx);
        pan.add(new JLabel("Hey"));
        //pan.setBackground(Color.white);
        dlg.add(pan);
        dlg.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dlg.setBounds((int)(dim.width*.40),25,(dim.width/2),(dim.height/2));
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	System.out.println("Looks Great!");
            	System.exit(0);
            }
        });
    }
    
}
