package com.dispatcher.editor;


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
import java.net.*;

public class EditorDocument extends HTMLDocument {
    protected double fontScale = 0;
    protected boolean bPrinting; // if true then font sizes do not need to be adjusted


	public EditorDocument(StyleSheet styles) {
		this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
	}

    public EditorDocument() {
        this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
    }

    public EditorDocument(Content c, StyleSheet styles) {
        super(c, styles);
    }

    public Font getRealFont(AttributeSet attr) {
        Font font = super.getFont(attr);
        return font;
    }

    public Font getFont(AttributeSet attr) {
        Font font = super.getFont(attr);
        // Java screen font sizes are based on pixels, printer is based on Point (1/72)
        // this will adjust to make it wysiwyg
        if (!bPrinting) {
            if (fontScale == 0) {
                fontScale = ((double)Toolkit.getDefaultToolkit().getScreenResolution()) / 72.0;
            }
            font = font.deriveFont((float) (font.getSize() * fontScale)); // Note: deriveFont(int) is to set style
        }
        return font;
    }


    public void setTitle(String title) {
        try {
            String titleHTML = "<title></title>";
            Element defaultRoot = getDefaultRootElement();
            
            Element head = getElementByTag(HTML.Tag.HEAD);
            if (head != null) {
                Element pImpl = getElementByTag(head, HTML.Tag.IMPLIED);
                if (pImpl != null) {
                    Element tElem = getElementByTag(pImpl, HTML.Tag.TITLE);
                    if(tElem == null) {
                        insertBeforeEnd(pImpl, titleHTML);
                    }
                }
            }
            else {
                Element body = getElementByTag(HTML.Tag.BODY);
                insertBeforeStart(body, "<head>" + titleHTML + "</head>");
            }
            putProperty(Document.TitleProperty, title);
        }
        catch(Exception e) {
            System.out.println("EditorDocument.setDocumentTitle() error: "+e);
        }
    }

    public String getTitle() {
        Object title = getProperty(Document.TitleProperty);
        if (title != null) return title.toString();
        return null;
    }

	public Element getElementByTag(HTML.Tag tag) {
		Element root = getDefaultRootElement();
		return getElementByTag(root, tag);
	}

	public Element getElementByTag(Element parent, HTML.Tag tag) {
		if (parent == null || tag == null) return null;

		for (int k=0; k<parent.getElementCount(); k++) {
			Element child = parent.getElement(k);
			if (child.getAttributes().getAttribute(
					StyleConstants.NameAttribute).equals(tag))
				return child;
			Element e = getElementByTag(child, tag);
			if (e != null)
				return e;
		}
		return null;
	}

	public void addAttributes(Element e, AttributeSet attributes) {
		if (e == null || attributes == null) return;
		try {
			writeLock();
			MutableAttributeSet mattr = (MutableAttributeSet) e.getAttributes();
			mattr.addAttributes(attributes);
			fireChangedUpdate(new DefaultDocumentEvent(0, getLength(),DocumentEvent.EventType.CHANGE));
		}
		finally {
			writeUnlock();
		}
	}

    // Parser/Reader
    public HTMLEditorKit.ParserCallback getReader(int pos) {
        Object desc = getProperty(Document.StreamDescriptionProperty);
        if (desc instanceof URL) setBase((URL)desc);

        putProperty("IgnoreCharSetDirective", Boolean.TRUE);
        DocumentReader reader = new DocumentReader(pos);
        return reader;
    }


    public HTMLEditorKit.ParserCallback getReader(int pos, int popDepth, int pushDepth, HTML.Tag insertTag) {
        DocumentReader reader = new DocumentReader(pos);//qqqqqqqqqttttttt
        return reader;
        // return super.getReader(pos, popDepth, pushDepth, insertTag);
    }


    /**
    * extend HTMLDocument.HTMLReader to include SPAN tags
    */
    public class DocumentReader extends HTMLDocument.HTMLReader {
        CharacterAction charAction = new CharacterAction();
        AttributeSet styleAttributes;
        boolean bInSpan = false;

        public DocumentReader(int offset) {
            super(offset, 0, 0, null);
        }

        public void flush() throws BadLocationException {
            super.flush();
        }

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            // System.out.print("<"+t+">");
            if (t == HTML.Tag.SPAN) {
                bInSpan = true;
                charAction.start(t, a);
            }
            else super.handleStartTag(t, a, pos);
        }

        public void handleEndTag(HTML.Tag t, int pos) {
            // System.out.println("</"+t+">");
            if (t == HTML.Tag.SPAN) {
                bInSpan = false;
                charAction.end(t);
            }
            else super.handleEndTag(t, pos);
        }    

        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t == HTML.Tag.SPAN) {
                if (bInSpan) handleEndTag(t, pos);
                else handleStartTag(t, a, pos);
            }
            else super.handleSimpleTag(t, a, pos);
        }

        public void handleError(String errorMsg, int pos) {
            // System.out.println("ERROR @"+pos+": \""+errorMsg+"\"");
            super.handleError(errorMsg, pos);
        }
    
        public void handleText(char[] data, int pos) {
            // System.out.print("TEXT:\""+(new String(data))+"\"");
            super.handleText(data, pos);
        }
        
    }
}

