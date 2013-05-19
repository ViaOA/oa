package com.dispatcher.editor;


import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.plaf.basic.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.datatransfer.*;


public class Editor extends JTextPane {

    public Editor() {
        OACaret caret = new OACaret(2.0f);
        caret.setBlinkRate(500);
        setCaret(caret);
    }

    public void setText(String t) {
        super.setText(t);
    }

	public void paste()	{
	    /** create a new transferable to filter using the the original contents */
    	Clipboard clipboard = getToolkit().getSystemClipboard();
    	Transferable content = clipboard.getContents(this);
        MyTransferable newContent = new MyTransferable(content);
        // newContent.bUsePlain = true; // qqqqqqqqqqqqq dont try to use html in VP qqqqqqq
        clipboard.setContents(newContent, null);
        for (;;) {
	        try {
    	        super.paste();
    	        break;
    	    }
    	    catch (Exception e) {
    	        System.out.println("Paste Error: "+e);
    	    }
            if (newContent.bUsePlain) break;
    	    newContent.bUsePlain = true;
    	}
    }

    /** used to cleanup clipboard data */
//	static class MyTransferable implements Transferable {
	class MyTransferable implements Transferable {
	    DataFlavor htmlDataFlavor, textDataFlavor;
	    String code, plain;
	    boolean bUsePlain;

        public MyTransferable(Transferable content) {
    	    // see: http://forum.java.sun.com/thread.jsp?forum=57&thread=258757
	        DataFlavor[] flavors = content.getTransferDataFlavors();
	        for (int i = 0; i < flavors.length; i++) {
        	    DataFlavor flavor = flavors[i];
	            String mimeType = flavor.getMimeType();

                /*qqqqqqqqqqqqqqqqqqqqqqqqq
                try {
                    System.out.println(i+") "+mimeType+"  "+content.getTransferData(flavor));//qqqqqqqqqqq
                }
                catch (Exception e) {
                    System.out.println("Editor.getTransferDataFlavors() Error: "+e);
                }
                */

	            if (mimeType.indexOf("String") >= 0) {
    	            if (mimeType.indexOf("text/plain") >= 0) {
                        textDataFlavor = flavor;
	                    try {
    	                    plain = (String) content.getTransferData(flavor);
                            plain = plain.replace((char)8216, '\'');
                            plain = plain.replace((char)8217, '\'');
                            plain = plain.replace((char)8220, '"');
                            plain = plain.replace((char)8221, '"');
                            plain = plain.replace((char)8211, '-');
    	                }
    	                catch (Exception e) {
    	                    System.out.println("Editor.getTransferDataFlavors() Error: "+e);
    	                }
    	            }
    	            else if (mimeType.indexOf("text/html") >= 0) {
	                    htmlDataFlavor = flavor;
	                    try {
    	                    code = (String) content.getTransferData(flavor);

                            // Parse out Microsoft Word "garbage"
                            HTMLParser hp = new HTMLParser();
                            if (hp.isMicrosoft(code)) {
                                code = hp.removeBody(code);
                                code = hp.convert(code);
                                code = "<html><body>"+code+"</body></html>";
                            }
    	                }
    	                catch (Exception e) {
    	                    System.out.println("Editor.getTransferDataFlavors() Error: "+e);
    	                }
	                }
	            }
	        }
	    }

	    public DataFlavor[] getTransferDataFlavors() {
            int i = 0;
            if (textDataFlavor != null) i++;
            if (!bUsePlain && htmlDataFlavor != null) i++;

            DataFlavor[] dfs = new DataFlavor[i];
            i = 0;
            if (textDataFlavor != null) dfs[i++] = textDataFlavor;
            if (!bUsePlain && htmlDataFlavor != null) dfs[i] = htmlDataFlavor;
	        return dfs;
	    }

	    public boolean isDataFlavorSupported(DataFlavor flavor) {
            getTransferDataFlavors();
            return (flavor == this.textDataFlavor || (!bUsePlain && flavor == this.htmlDataFlavor));
	    }

	    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException	{
            if (!bUsePlain && flavor == this.htmlDataFlavor) {
                return code;
            }
            else if (flavor == this.textDataFlavor) {
                return plain;
            }
            return null;
        }
    }

}


