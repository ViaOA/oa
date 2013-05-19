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

public class EditorKit extends HTMLEditorKit {
    private static ViewFactory defaultFactory; // Shared factory for creating HTML Views


    public Document createDocument() {
        // same as HTMLEditorKit, except this will create an "EditorDocument" instead of an "HTMLDocument"
        
        // copy Styles from EditorKit to Document
        StyleSheet current = getStyleSheet();
        StyleSheet ss = new StyleSheet();
        ss.addStyleSheet(current);

        ss.addRule("p {margin-top: 0}");


        ss.addRule("body { font-family: Arial; font-size: 12pt; }"); // sans-serif

        /** load an external style sheet
                try {
                    Reader rin = new InputStreamReader(HtmlPane.class.getResourceAsStream("vetplan.css"));
                    ss.loadRules(rin, null);
                }
                catch (Exception e) {
                    System.out.println("Error loading vetplan.css: "+e);
                }
        *****/


	    EditorDocument doc = new EditorDocument(ss);
	    doc.setParser(getParser());
	    doc.setAsynchronousLoadPriority(4);
	    doc.setTokenThreshold(100);
	    return doc;
    }

    public @Override ViewFactory getViewFactory() {
        if (defaultFactory == null) {
            defaultFactory = new Factory();
        }
        return defaultFactory;
    }


// ********* View Factory ******************************************************************************
    public class Factory extends HTMLEditorKit.HTMLFactory implements ViewFactory {
        public View create(Element elem) {
            View view = null;
            Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                HTML.Tag kind = (HTML.Tag) o;
                if (kind == HTML.Tag.TABLE) {
                   view = super.create(elem);
                }
                else if (kind == HTML.Tag.COMMENT) {
                    view = new InvisibleView(elem);
                }
                else if (kind instanceof HTML.UnknownTag) {
                    view = new InvisibleView(elem);
                }
                else if (kind == HTML.Tag.BR) {
                    view = new InlineView(elem) {
                        public int getBreakWeight(int axis, float pos, float len) {
	                        if (axis == X_AXIS) return ForcedBreakWeight;
	                        else return super.getBreakWeight(axis, pos, len);
                        }
                        public float getPreferredSpan(int axis) {
                            float fx = super.getPreferredSpan(axis);
	                        if (axis == View.X_AXIS) return fx;
	                        Element element = getElement();
	                        int p1 = element.getStartOffset();
	                        int p2 = element.getEndOffset();
	                        if (p2 - p1 == 1) return fx;
                            return 2f;
                        }
                    };
                }
                else if (kind == HTML.Tag.SPAN) {
                    view = new InlineView(elem) {
                        public float getPreferredSpan(int axis) {
                            float fx = super.getPreferredSpan(axis);
	                        if (axis == View.X_AXIS) return fx;
                            return 1f;
                        }
                    };
                }
                else if (kind == HTML.Tag.CONTENT) {
                    int p0 = elem.getStartOffset();
                    int p1 = elem.getEndOffset();
                    String vs = null;
                    try {
                        vs = elem.getDocument().getText(p0,(p1-p0));
                    }
                    catch (Exception e) {
                    }
                     
                    if (vs != null && vs.length() == 1 && vs.charAt(0) == '\n') {
                        view = new InlineView(elem) {
                            public float getPreferredSpan(int axis) {
                                float fx = super.getPreferredSpan(axis);
	                            if (axis == View.X_AXIS) return fx;

                                View pview = getParent();
                                if (pview != null) {
                                    int x = pview.getViewCount();
                                    if (x == 1) return fx;

                                    for (int i=0; i<x; i++) {
                                        View v = pview.getView(i);
                                        if (v == this) {
                                            if (i == 0) return fx;
                                            return pview.getView(i-1).getPreferredSpan(axis);
                                        }
                                    }
                                }
                                return fx; 
                            }
                            public void paint_TEST_(Graphics g, Shape allocation) {
                                super.paint(g, allocation);
                                g.setColor(Color.red);
                                ((Graphics2D)g).fill(allocation);
                            }
                        };
                    }
                    else {
                        view = new InlineView(elem) {
                            // shrinks line spacing, handles underlining
                            GlyphPainter painter;
                            float origHeight, height, newHeight, descent, descentDx;
                                        
                            public float getPreferredSpan(int axis) {
	                            if (axis == View.X_AXIS) return super.getPreferredSpan(axis);

	                            if (painter == null) painter = getGlyphPainter();
                                origHeight = painter.getHeight(this);
                                if (origHeight != height) {
                                    height = origHeight;
                                    descent = painter.getDescent(this);
                                    // descentDx = descent * .05f;
                                    descentDx = 0;
                                    float ascent = painter.getAscent(this);
                                    // float ascentDx = ascent * .09f;
                                    float leading = height - (descent + ascent);  
                                    // newHeight = height - (leading  + ascentDx + descentDx);
                                    newHeight = height - leading;
	                            }
                                return newHeight;
                            }
                                        
                            boolean bPainting;  // dont have super.paint() underline
                            public boolean isUnderline() {
                                if (bPainting) return false;
                                return super.isUnderline();
                            }
                            public void paint(Graphics g, Shape a) {
                                Rectangle rec = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
                                int holdY = rec.y;
                                rec.y = (int) (rec.y - (height-newHeight) + descentDx);
                                            
                                bPainting = true;
                                super.paint(g, a);
                                bPainting = false;

                                if (isUnderline()) {
                                    //int y = (int) (rec.y + rec.height - newDescent + 1);
                                    int y = (int) (rec.y + (height - descent) + 1);
                                    g.drawLine(rec.x, y, rec.x+rec.width, y);
                                }
                                rec.y = holdY;
                            }
                        };
                    }
                }
                else {
                    view = super.create(elem);
                }
            }
            else {
                view = new LabelView(elem);
            }
            return view;
        }
    }








// ************ Actions *******************************************************************

    // fast way to get common actions
    protected Action actionBold, actionItalic, actionUnderline;
    
    public Action getBoldAction() {
        if (actionBold == null) actionBold = getAction("font-bold");
        return actionBold;
    }
    public Action getItalicAction() {
        if (actionItalic == null) actionItalic = getAction("font-italic");
        return actionItalic;
    }
    public Action getUnderlineAction() {
        if (actionUnderline == null) actionUnderline = getAction("font-underline");
        return actionUnderline;
    }
    
    
    
    // defined in DefaultEditorKit: Overwritten to fix bugs or add additonal functionality
    //   public static final String insertBreakAction = "insert-break"; // overwritten from: StyledEditorKit.StyledInsertBreakAction
    //   public static final String deletePrevCharAction = "delete-previous";
    //   public static final String beginLineAction = "caret-begin-line";

    private final Action[] defaultActions = {
	    new InsertBreakAction(insertBreakAction), 
	    new DeletePrevCharAction(deletePrevCharAction),
	    new DeleteNextCharAction(deleteNextCharAction),
	    new BeginLineAction(beginLineAction, false),
        new BeginLineAction(selectionBeginLineAction, true),
    	new AlignmentAction("justified-justify", StyleConstants.ALIGN_JUSTIFIED)
    };

    public Action[] getActions() {
    	return TextAction.augmentList(super.getActions(), this.defaultActions);
    }

    /** copied from DefaultEditorKit 
        Overwritten/replaced to fix bug where deleting at end of line does not "bring up" next line
    */
    static class DeleteNextCharAction extends TextAction {
        DeleteNextCharAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            JTextPane target = (JTextPane) getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } 
                    else if (dot < doc.getLength()) {
                        int delChars = 1;
                        
                        if (dot < doc.getLength() - 1) {
                            String dotChars = doc.getText(dot, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            
                            if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
                                c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                        
                            if (c0 == '\n') { // vav

//qqqqqqqqqqqqq make sure "dot+1" is legal
Element ele = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot);
AttributeSet as = ele.getAttributes();
SimpleAttributeSet sas = new SimpleAttributeSet(as);
((StyledDocument)target.getDocument()).setParagraphAttributes(dot+1, 1, sas, true);

/**                                
                                // need to make both paragraphs attributes match, so that they will be merged into one
                                Element ele = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot+1);
                                AttributeSet as = ele.getAttributes();
                                target.setParagraphAttributes(as, true);
*/
                            }
                        }
                        
                        doc.remove(dot, delChars);
                        beep = false;
                    }
                } 
                catch (BadLocationException bl) {
                }
            }
            if (beep) {
        		UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }

    /** copied from DefaultEditorKit 
        Overwritten/replaced to fix bug where BOL on first line goes to position[0], when text really
        begins at position[1].  The element at position[0] is for a "default" character in the <head>
    */
    class BeginLineAction extends TextAction {
        boolean bSelect;
        BeginLineAction(String nm, boolean bSelect) {
            super(nm);
            this.bSelect = bSelect;
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent editor = getTextComponent(e);
            if (editor != null) {
                try {
                    int offs = editor.getCaretPosition();
                    
                    //orig: int begOffs = Utilities.getRowStart(editor, offs);
                    // has a bug: if rec.width = 0, then it should not be counted
                    // ex: <head> has an element at position [0] that does not have a width, but "y" is same
	                Rectangle r = editor.modelToView(offs);
	                if (r == null) return;
	                int lastOffs = offs;
	                int y = r.y;
	                while ((r != null) && (y == r.y)) {
	                    if (r.width > 0 || r.height > 0) offs = lastOffs;
	                    lastOffs -= 1;
	                    r = (lastOffs >= 0) ? editor.modelToView(lastOffs) : null;
	                }
                                    
                    if (bSelect) editor.moveCaretPosition(offs);
                    else editor.setCaretPosition(offs);
                } 
                catch (BadLocationException bl) {
		            UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }

        private boolean select;
    }


    /*
     * Inserts a line break.  <br>, <p>, or <li>
     * this is copied from DefaultEditorKit, and StyledEditorKit.StyledInsertBreakAction
     */
    static class InsertBreakAction extends StyledTextAction {
        public InsertBreakAction(String name) {
            super(name);
        }

        private SimpleAttributeSet tempSet;
        public void actionPerformed(ActionEvent e) {
            Editor target = (Editor) getTextComponent(e);
            if (target == null || !target.isEditable() || !target.isEnabled()) {
		        UIManager.getLookAndFeel().provideErrorFeedback(target);
		        return;
		    }
            try {
                int pos = target.getCaretPosition();

                if (tempSet == null) tempSet = new SimpleAttributeSet();

                tempSet.addAttributes(target.getInputAttributes());

                if ((e.getModifiers() & e.SHIFT_MASK) == 0 && (e.getModifiers() & e.CTRL_MASK) == 0) {
                    // qqqqqqq TODO: if in a <OL> or <UL>, then insert a <LI>
                    // 2004/07/07 was:
                    //target.editorKit.insertHTML(target.editorDocument, pos, "<br>", 0, 0, HTML.Tag.BR);
                    Element ele = ((DefaultStyledDocument)target.getDocument()).getCharacterElement(pos);
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.BR);
                    ((DefaultStyledDocument)target.getDocument()).insertString(pos, "\n", sas);
                    ((DefaultStyledDocument)target.getDocument()).setCharacterAttributes(pos, 1, sas, false);
                }
                else {
                    Element ele = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(pos);
                    
                    target.replaceSelection("\n");
                    int pos2 = target.getCaretPosition();
                    Element ele2 = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(pos2);
                    // need to make sure that the new paragraph has "<p>"
                    AttributeSet attrs = ele2.getAttributes();
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
                    ((DefaultStyledDocument)target.getDocument()).setParagraphAttributes(pos2, 1, sas, false);
                }
                MutableAttributeSet ia = target.getInputAttributes();
                ia.removeAttributes(ia);
                ia.addAttributes(tempSet);
                tempSet.removeAttributes(tempSet);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            target.setCaretPosition(target.getCaretPosition());
        }
    
    }


    /*
     * Deletes the character of content that precedes the caret.
     * this is copied from DefaultEditorKit
     */
    static class DeletePrevCharAction extends TextAction {
        DeletePrevCharAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            JTextPane target = (JTextPane) getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } 
                    else if (dot > 0) {
                        int delChars = 1;
                        
                        boolean bRemoveEol = false;  //vav
                        if (dot > 1) {
                            String dotChars = doc.getText(dot - 2, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            
                            if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
                                c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                            if (c1 == '\n') bRemoveEol = true; // vav
                        }

                        if (bRemoveEol) { // vav
                            // need to see if this is a <p> and above is <p-implied>
                            Element ele = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot);
                            AttributeSet as = ele.getAttributes();
                            if (as.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.P) {
                                Element ele2 = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot-2);
                                AttributeSet as2 = ele2.getAttributes();
                                if (as2.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMPLIED) {
                                    // need to make current <p-implied> so that it will match the previouse paragraph
                                    //   and be merged with the previous paragraph
                                    SimpleAttributeSet sas = new SimpleAttributeSet(as);
                                    sas.removeAttribute(StyleConstants.NameAttribute);
                                    sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMPLIED);
                                    target.setParagraphAttributes(sas, true);
                                }
                            }
                        }
                        
                        doc.remove(dot - delChars, delChars);
                        beep = false;
                    }
                    else {
                        // vav
                        // added so that a <p> will be removed
                        // need to make sure that the new paragraph has "<p>"
                        SimpleAttributeSet sas = new SimpleAttributeSet(target.getParagraphAttributes());
                        sas.removeAttribute(StyleConstants.NameAttribute);
                        sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMPLIED);

                        target.setParagraphAttributes(sas, true);
                    }
                } 
                catch (BadLocationException bl) {
                }
            }
            if (beep) {
		        UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }


    public Action getAction(String name) {
        if (name == null) return null;
        Action[] actions = this.getActions();
        for (int i=0; i<actions.length; i++) {
            String s = (String) actions[i].getValue(Action.NAME);
            if (s != null && s.equalsIgnoreCase(name)) return actions[i];
        }
        return null;
    }

}

