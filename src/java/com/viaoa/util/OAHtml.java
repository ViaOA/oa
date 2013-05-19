package com.viaoa.util;

import java.io.*;
import java.text.*;

import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.viaoa.html.OAHtmlComponent;

public class OAHtml {
    private String htmlText;
    private boolean bInit;
    private HTMLEditorKit kit;
    private HTMLDocument doc;
    private Reader reader;

    public OAHtml() {
        
    }
    
    public OAHtml(String htmlText) {
        setText(htmlText);
    }
    
    /**
     * Set html text
     */
    public void setText(String htmlText) {
        this.htmlText = htmlText;
        bInit = false;
    }

    /**
     * @return length of text, without markup tags.  Returns -1 if error.
     */
    public int getLength() {
        if (!init()) return -1;
        return doc.getLength();
    }
    /**
     * @return length of text, without markup tags.  Returns -1 if error.
     */
    public int length() {
        return getLength();
    }
    
    
    /**
     * 
     * @return html text
     */
    public String getText() {
        return htmlText;
    }
    
    private boolean init() {
        if (bInit || htmlText == null) return bInit;
        
        reader = new StringReader(htmlText);
        if (kit == null) kit = new HTMLEditorKit();

        doc = (HTMLDocument) kit.createDefaultDocument();
        
        try {
            kit.read(reader, doc, 0);
            bInit = true;
        }
        catch (Exception e) {
        }
        
        return bInit;
    }

    /**
     * Insert a string at a given position.  Use substring to get html document with the inserted text.
     * @param text text to insert, should not include tags.
     * @param pos
     */
    public void insert(String text, int pos) {
        if (!init()) return;
        try {
            doc.insertString(pos, text, null);
        }
        catch (Exception e) {
        }
    }
    
    
    /**
     * Get substring of html document text, which will then include html tags/attributes.
     * example: if the substring(1,3) for html doc '<html><body>abced<body><html>' will return '<html><body>bc<body><html>'
     */
    public String substring(int beginPos, int endPos) {
        if (!init()) return null;
        
        StringWriter w = new StringWriter(); 
        try {
            w = new StringWriter(); 
            kit.write(w, doc, beginPos+1, (endPos-beginPos)+1);
        }
        catch (Exception e) {
        }
        return w.toString();
    }

    
    
    public static void main(String[] args) throws Exception {
        String s = "<html><body><p><i>A</i><div class='joe'>12345<b><i>6789<br>ABCD</b> XYZ</div></body></html>";
        OAHtml h = new OAHtml(s);
        s = h.substring(0, 5);
        System.out.println("==> " + s);
    }
    
    
}
