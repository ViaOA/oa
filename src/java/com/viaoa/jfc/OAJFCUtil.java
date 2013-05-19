package com.viaoa.jfc;


import java.awt.*;
import java.util.*;
import javax.swing.*;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;


public class OAJFCUtil {

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
	
    
	public static void main(String[] args) {
		showLookAndFeels();
//		showLookAndFeelDefaults();
//		showSystemInfo();
	}
	
}

