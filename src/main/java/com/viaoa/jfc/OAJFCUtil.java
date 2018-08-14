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
import java.util.*;
import javax.swing.*;

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

    
    public static void setBorderError(JLabel label) {
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

    
    
	public static void main(String[] args) {
		showLookAndFeels();
//		showLookAndFeelDefaults();
//		showSystemInfo();
	}
	
}

