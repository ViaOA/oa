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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class OAJfcUtil {

	/*
	 https://liquidlnf.dev.java.net/	
	 UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
	 com.birosoft.liquid.LiquidLookAndFeel.setLiquidDecorations(true);
	*/
	
	public static void showLookAndFeels() {
		UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
		for (int i=0; lfs != null && i < lfs.length; i++) {
			System.out.println(i + ") " + lfs[i].getName());
		}
	}
	public static void showLookAndFeelDefaults() {
		UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
		Enumeration keys  = uid.keys();
		while (keys.hasMoreElements()) { 
		    String key   = (String) keys.nextElement(); 
		    Object value = uid.get(key); 
		    System.out.println (key + " = " + value); 
		  } 
	}

	public static void showSystemInfo() {
		Properties props = System.getProperties();
		Enumeration en = props.keys();
		for (int i=0; en.hasMoreElements(); i++) {
			Object key = en.nextElement();
			if (key instanceof String) System.out.println(i+") " + key + " = " + props.getProperty((String)key));;
		}
	}

    public static Window getWindow(Component comp) {
        if (comp == null) return null;
        Window window = SwingUtilities.getWindowAncestor(comp);
        if (window != null) return window;
        for(Container p = comp.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JPopupMenu) {
                JPopupMenu pm = (JPopupMenu) p;
                Component compx = ((JPopupMenu) p).getInvoker();
                if (compx instanceof Container) {
                    p = (Container) compx;
                }
            }
            if (p instanceof Window) {
                window = (Window) p;
                break;
            }
        }
        return window;
    }

    
    
    //** ===========================
    //      Static Helper methods
    
    public static void useRedBorder(JComponent lbl) {
        if (lbl == null) return;
        lbl.setBorder(OATable.BORDER_Red);
    }
    public static void useYellowBorder(JComponent lbl) {
        if (lbl == null) return;
        lbl.setBorder(OATable.BORDER_Yellow);
    }
    public static void useColorBorder(JComponent lbl, Color color) {
        if (lbl == null) return;
        if (color == null) lbl.setBorder(null);
        lbl.setBorder(new CompoundBorder(new LineBorder(Color.white, 1), new LineBorder(color)));
    }
    public static void useNoBorder(JComponent lbl) {
        if (lbl == null) return;
        lbl.setBorder(null);
    }

    public static void useDitto(JLabel lbl) {
        if (lbl == null) return;
        useGrayText(lbl);
        lbl.setText("\"\"");
        alignCenter(lbl);
    }
    public static void alignCenter(JLabel lbl) {
        if (lbl == null) return;
        lbl.setHorizontalAlignment(JLabel.CENTER);
    }
    public static void alignLeft(JLabel lbl) {
        if (lbl == null) return;
        lbl.setHorizontalAlignment(JLabel.LEFT);
    }
    public static void alignRight(JLabel lbl) {
        if (lbl == null) return;
        lbl.setHorizontalAlignment(JLabel.RIGHT);
    }
    
    public static void useTableSelectedBorder(JComponent lbl) {
        if (lbl == null) return;
        lbl.setBorder(new CompoundBorder(new LineBorder(Color.white, 1), UIManager.getBorder("Table.focusCellHighlightBorder")));         
    }
    public static void useGrayText(JLabel lbl) {
        if (lbl == null) return;
        lbl.setForeground(Color.GRAY);
    }

    public static void useColorIcon(JLabel lbl, Color color1) {
        useColorIcon(lbl, color1, null);
    }
    public static void useColorIcon(JLabel lbl, Color color1, Color color2) {
        if (lbl == null) return;
        if (color1 == null) color1 = Color.WHITE;

        LabelColorIcon myColorIcon = new LabelColorIcon(color1, color2);
        lbl.setIcon(myColorIcon);        
    }
    
    static class LabelColorIcon implements Icon {
        Color color, color2;        
        public LabelColorIcon(Color c) {
            this.color = c;
        }
        public LabelColorIcon(Color c, Color c2) {
            this.color = c;
            this.color2 = c2;
        }
        public int getIconHeight() {
            return 17;
        }
        public int getIconWidth() {
            return 12;
        }

        public void paintIcon(Component comp,Graphics g,int x,int y) {
            Color c = color==null?Color.white:color;
            g.setColor(c);
            g.fillRect(x+1,y+3,12,12);
            if (color2 != null) {
                Polygon p = new Polygon();
                p.addPoint(13, 3);
                p.addPoint(1, 15);
                p.addPoint(13, 15);
                g.setColor(color2);
                g.fillPolygon(p);
            }
        }
    }
    
    public static void setBorderError(JComponent label) {
        if (label == null) return;
        label.setBorder(OATable.BORDER_Red);
    }
    public static void setJustifyLeft(JLabel label) {
        if (label == null) return;
        label.setHorizontalAlignment(JLabel.LEFT);
    }
    public static void setJustifyRight(JLabel label) {
        if (label == null) return;
        label.setHorizontalAlignment(JLabel.RIGHT);
    }
    public static void setJustifyCenter(JLabel label) {
        if (label == null) return;
        label.setHorizontalAlignment(JLabel.RIGHT);
    }

    private static ConcurrentHashMap<JComponent, Integer> hmLabelBlink = new ConcurrentHashMap<JComponent, Integer>();
    public void blink(final JComponent component, final int numberOfTimes) {
        if (component == null) return;
        blink(component, component.getBackground(), component.getForeground(), numberOfTimes);
    }
    public void blink(final JComponent component, final Color fColor, final Color bColor, final int numberOfTimes) {
        if (component == null) return;
        
        Object obj = hmLabelBlink.get(component);
        if (obj == null) hmLabelBlink.put(component, 1);
        else {
            hmLabelBlink.put(component, ((Integer)obj).intValue()+1);
        }
        final int cntThisCount = hmLabelBlink.get(component);

        final Color fc = fColor != null ? fColor : component.getBackground();
        final Color bc = bColor != null ? bColor : component.getForeground();
        
        final Color fcOrig = component.getForeground();
        final Color bcOrig = component.getBackground();
        
        
        final Timer timer = new Timer(150, null);
        ActionListener al = new ActionListener() {
            int cnt;
            public void actionPerformed(ActionEvent e) {
                Object objx = hmLabelBlink.get(component);
                int x;
                if (objx == null) {
                    x = 0;
                }
                else x = ((Integer) objx).intValue();
                
                if (x > 0 && x != cntThisCount) {
                    timer.stop();
                    return;
                }
                
                boolean b = (cnt++ % 2 == 0);
                
                Color c;

                c = (b ? fc : fcOrig);
                component.setForeground(c);

                c = (b ? bc : bcOrig);
                component.setBackground(c);

                if (!b && ((cnt / 2) >= numberOfTimes) ) {
                    hmLabelBlink.remove(component);
                    timer.stop();
                }
            }
        };                 
        timer.addActionListener(al);
        timer.setRepeats(true);
        timer.setInitialDelay(250);
        timer.start();
    }

    public static void pack(Window window) {
        if (window == null) return;
        try {
            OAResizePanel.setPacking(window);
            window.pack();
        }
        finally {
            OAResizePanel.setPacking(null);
        }
    }
    
}

