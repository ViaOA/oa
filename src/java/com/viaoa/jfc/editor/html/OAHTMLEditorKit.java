/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc.editor.html;


import java.io.*;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * EditorKit for OAHTMLTextPane
 * <ul>
 * <li>Document - OAHTMLDocument
 * <li>Writing - OAHTMLWriter
 * <li>Custom Views - ViewFactory
 * <li>Custom Actions
 * </ul>
 * @author vvia
 */
public abstract class OAHTMLEditorKit extends HTMLEditorKit {
    private static final long serialVersionUID = 1L;

    public static final String insertOrderedListAction = "insert-ordered-list-action";
    public static final String insertUnorderedListAction = "insert-unordered-list-action";
    public static final String insertBRAction = "insert-br-action";
    public static final String insertParagraphAction = "insert-paragraph-action";
    public static final String insertDivAction = "insert-div-action";
    public static final String insertImageDivAction = "insert-image-div-action";

    private static StyleSheet cssHtml = null;
    // 20100326 was static, now has separate for each document, since each doc could have unique image dir/source
    private OAHTMLViewFactory defaultFactory; // Shared factory for creating HTML Views

    /**
     * Creates and returns an OAHTMLDocument
     */
    public Document createDefaultDocument() {
        // same as HTMLEditorKit, except this will create an "EditorDocument" instead of an "HTMLDocument"
        OAHTMLDocument doc = new OAHTMLDocument(createDefaultStyleSheet());
        doc.setParser(getParser());  // needs to be set so that HTML can be inserted
        return doc;
    }

    protected StyleSheet createDefaultStyleSheet() {
        // copy Styles from EditorKit to Document
        StyleSheet current = getStyleSheet();
        StyleSheet ss = new StyleSheet();
        ss.addStyleSheet(current);
        // in css: ss.addRule("p {margin-top: 0}");
        // in css: ss.addRule("body { font-family: Arial; font-size: 12pt; }"); // sans-serif
        
        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String fontFamilyName = "Arial";
        if (font == null) {
            String[] s = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            if (s != null && s.length > 0) fontFamilyName = s[0];
        }
        else fontFamilyName = font.getFamily();
        
        String defaultFont = "body { font-family: " + fontFamilyName + "; " +"font-size: 12pt; }";
        ss.addRule(defaultFont);
        return ss;
    }
    
    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        OAHTMLWriter w = new OAHTMLWriter(out, (HTMLDocument) doc, pos, len);
        w.write();    
    }
    
    /**
     *  Uses style sheet css/html.css
     */
    public StyleSheet getStyleSheet() {
        if (cssHtml == null) {
            cssHtml = new StyleSheet();
            try {
                InputStream is = this.getClass().getResourceAsStream("css/html.css");
                Reader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
                cssHtml.loadRules(r, null);
                r.close();
            } 
            catch (Throwable e) {
                System.out.println("OAEditorKit.getStyleSheet() exception: " + e);
            }
        }
        return cssHtml;
    }
    
    /**
     * returns OAHTMLViewFactory
     */
    public @Override ViewFactory getViewFactory() {
        if (defaultFactory == null) {
            defaultFactory = new OAHTMLViewFactory() {
                @Override
                protected Image getImage(String src, URL url) {
                    return OAHTMLEditorKit.this.getImage(src, url);
                }
            };
        }
        return defaultFactory;
    }


    // ************ Actions *******************************************************************

    // fast way to get common actions
    protected Action actionBold, actionItalic, actionUnderline, actionSpace;
    
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
    public Action getSpaceAction() {
        if (actionSpace == null) {
            actionSpace = getAction("insert-space");
        }
        return actionSpace;
    }
    
    
    
    // defined in DefaultEditorKit: Overwritten to fix bugs or add additonal functionality
    //   public static final String insertBreakAction = "insert-break"; // overwritten from: StyledEditorKit.StyledInsertBreakAction
    //   public static final String deletePrevCharAction = "delete-previous";
    //   public static final String beginLineAction = "caret-begin-line";

    private final Action[] defaultActions = {
        new InsertBreakAction(insertBreakAction), 
        new InsertParagraphAction(insertParagraphAction), 
        // new InsertBRAction(insertBRAction), 
        new InsertDivAction(insertDivAction),
        new InsertImageDivAction(insertImageDivAction),
        new DeletePrevCharAction(deletePrevCharAction),
        new DeleteNextCharAction(deleteNextCharAction),
        // new BeginLineAction(beginLineAction, false),
        // new BeginLineAction(selectionBeginLineAction, true),
        new AlignmentAction("justified-justify", StyleConstants.ALIGN_JUSTIFIED),
        //new InsertOrderedListAction(insertOrderedListAction),
        //new InsertUnorderedListAction(insertUnorderedListAction),
        new InsertSpaceAction("insert-space")
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
                        
                            if (c0 == '\n') { // vav this is the change/bug fix
                                Element ele = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot);
                                AttributeSet as = ele.getAttributes();
                                SimpleAttributeSet sas = new SimpleAttributeSet(as);
                                ((StyledDocument)target.getDocument()).setParagraphAttributes(dot+1, 1, sas, true);
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
        begins at position[1] when <head> exists.  The element at position[0] is for a "default" character in the <head>
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

    static class InsertSpaceAction extends StyledTextAction {
        public InsertSpaceAction(String name) {
            super(name);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            OAHTMLTextPane editor = (OAHTMLTextPane) getTextComponent(e);
            OAHTMLEditorKit kit = (OAHTMLEditorKit) editor.getEditorKit();
            OAHTMLDocument doc = (OAHTMLDocument) editor.getDocument();
            boolean beep = true;
            if ((editor != null) && (editor.isEditable())) {
                beep = false;
                try {
                    int pos = editor.getCaretPosition();
                    MutableAttributeSet as = kit.getInputAttributes();  // 20110811 added to use current input AS
                    boolean b = false;
                    if (pos > 0) {
                        String s = doc.getText(pos-1, 1);
                        if (s.equals(" ")) b = true;
                    }
                    if (b) {
                        // Element pElem = doc.getParagraphElement(pos);
                        
                        doc.insertString(pos, "\u00A0", as);
                        // doc.insertString(pos, "&nbsp;", null);  // will enter the real string
                        // kit.insertHTML(doc, pos, "&nbsp;", 0, 0, (HTML.Tag) null);  does not work right, adds <p>
                    }
                    else {
                        doc.insertString(pos, " ", as);
                    }
                    editor.setCaretPosition(pos+1);
                }
                catch (Exception ex) {
                    System.out.println("Exception: "+ex);
                    ex.printStackTrace();
                }
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }
        }
    }

    /*
    static class InsertOrderedListAction extends StyledTextAction {
        public InsertOrderedListAction(String name) {
            super(name);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            OAHTMLTextPane editor = (OAHTMLTextPane) getTextComponent(e);
            OAHTMLEditorKit kit = (OAHTMLEditorKit) editor.getEditorKit();
            OAHTMLDocument doc = (OAHTMLDocument) editor.getDocument();
            
            try {
                int pos = editor.getCaretPosition();
                int newPos = kit.insertHtmlList(doc, pos, "<ol><li></li></ol>", HTML.Tag.OL);
                editor.setCaretPosition(newPos);
            }
            catch (Exception ex) {
                System.out.println("Exception: "+ex);
                ex.printStackTrace();
            }
        }
    }
    static class InsertUnorderedListAction extends StyledTextAction {
        public InsertUnorderedListAction(String name) {
            super(name);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            OAHTMLTextPane editor = (OAHTMLTextPane) getTextComponent(e);
            OAHTMLEditorKit kit = (OAHTMLEditorKit) editor.getEditorKit();
            OAHTMLDocument doc = (OAHTMLDocument) editor.getDocument();
            
            try {
                int pos = editor.getCaretPosition();
                int newPos = kit.insertHtmlList(doc, pos, "<ul><li></li></ul>", HTML.Tag.UL);
                editor.setCaretPosition(newPos);
            }
            catch (Exception ex) {
                System.out.println("Exception: "+ex);
                ex.printStackTrace();
            }
        }
    }
    */
    
    /*
     * Inserts a line break.  <br>, <p>, or <li>
     * this is copied from DefaultEditorKit, and StyledEditorKit.StyledInsertBreakAction
     */
    static class InsertBreakAction extends StyledTextAction {
        private SimpleAttributeSet tempSet;
        private boolean bAlwaysUseBR;  // flag used by InsertBRAction subclass, so that a <BR> will always be used.
        private boolean bAlwaysParagraph;
        public InsertBreakAction(String name) {
            super(name);
        }
        public InsertBreakAction(String name, boolean bAlwaysUseBR, boolean bAlwaysParagraph) {
            super(name);
            this.bAlwaysUseBR = bAlwaysUseBR;
            this.bAlwaysParagraph = bAlwaysParagraph;
        }

        // 20121030 changed to use a <br> by default.  If [ctrl] or [shift] key, then insert <p>
        public void actionPerformed(ActionEvent e) {
            JEditorPane target = getEditor(e);

            if (target == null) {
                // See if we are in a JTextComponent.
                JTextComponent text = getTextComponent(e);

                if (text != null) {
                    if ((!text.isEditable()) || (!text.isEnabled())) {
                        UIManager.getLookAndFeel().provideErrorFeedback(target);
                        return;
                    }
                    text.replaceSelection("\n");
                }
                return;
            }
            
            if ((!target.isEditable()) || (!target.isEnabled())) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
                return;
            }
            StyledEditorKit sek = getStyledEditorKit(target);

            if (tempSet != null) {
                tempSet.removeAttributes(tempSet);
            }
            else {
                tempSet = new SimpleAttributeSet();
            }
            int pos = target.getCaretPosition();
            tempSet.addAttributes(sek.getInputAttributes());

            boolean bInList = false;
            OAHTMLDocument doc = (OAHTMLDocument) target.getDocument();
            OAHTMLEditorKit kit = (OAHTMLEditorKit) target.getEditorKit();

            Element elem = doc.getParagraphElement(pos);
            String parentname = elem.getParentElement().getName();
            if (parentname.toLowerCase().equals("li")) {
                bInList = true;
            }

            boolean bInsertP = ((e.getModifiers() & e.SHIFT_MASK) != 0 || (e.getModifiers() & e.CTRL_MASK) != 0);
            if (bInsertP && bInList) {
                bInList = false;
                bInsertP = false;
            }
            
            if (bAlwaysParagraph) {
                bInList = false;
                bInsertP = true;
            }

            AttributeSet as = elem.getAttributes();
            if (bInList) {
                if (elem.getEndOffset() - elem.getStartOffset() > 1) {
                    // create a new <LI>
                    try {
                        doc.insertAfterEnd(elem.getParentElement(),"<li></li>");
                        pos = elem.getParentElement().getEndOffset() - 1;
                    } 
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } 
                else {
                    // done creating <LI>
                    try {
                        doc.remove(pos, 1);
                    } 
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (bInsertP) {
                target.replaceSelection("\n");
                SimpleAttributeSet sas = new SimpleAttributeSet(as);
                Element elemx = doc.getParagraphElement(pos+1);
                sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
                ((StyledDocument)target.getDocument()).setParagraphAttributes(pos+1, 1, sas, true);
            }
            else {
                target.replaceSelection(null);

                String insertHtml;
                HTML.Tag insertTag;
                insertHtml = "<br>";
                insertTag = HTML.Tag.BR;
                
                try {
                    // vv hack: there is a problem when inserting html 1: at end of line, or 2: begin of document
                    //    the HTML will need to take up a single ' '/space in the doc, but wont be rendered as a space
                    doc.insertString(pos, "  ", new SimpleAttributeSet(as)); // adds "fake" chars so that insert is not at begin/end of line
                    kit.insertHTML(doc, pos+1, insertHtml, 0, 0, insertTag);
                    doc.remove(pos, 1);  
                    doc.remove(pos+1, 1); 
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                target.setCaretPosition(pos+1);
            }
            catch (Exception ex) {
                target.setCaretPosition(pos);
            }
            
            MutableAttributeSet ia = sek.getInputAttributes();

            ia.removeAttributes(ia);
            ia.addAttributes(tempSet);
            tempSet.removeAttributes(tempSet);
        }

    }


    /**
     * This is so a <BR> will always be used by InsertBreakAction.
     */
    static class InsertBRAction extends InsertBreakAction {
        public InsertBRAction(String name) {
            super(name, true, false);
        }
    }
    
    /**
     * This is so a <P> will always be used by InsertBreakAction.
     */
    static class InsertParagraphAction extends InsertBreakAction {
        public InsertParagraphAction(String name) {
            super(name, false, true);
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
                                    // need to make current <p-implied> so that it will match the previous paragraph
                                    //   and be merged with the previous paragraph
                                    SimpleAttributeSet sas = new SimpleAttributeSet(as);
                                    sas.removeAttribute(StyleConstants.NameAttribute);
                                    sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMPLIED);
                                    target.setParagraphAttributes(sas, true);
                                }
                            }
                            else if (as.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.DIV) {
                                // 20121101 check for div
                                Element ele2 = ((DefaultStyledDocument)target.getDocument()).getParagraphElement(dot-2);
                                AttributeSet as2 = ele2.getAttributes();
                                if (as2.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMPLIED) {
                                    // need to make current <p-implied> so that it will match the previous paragraph
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

    
    static class InsertDivAction extends InsertHTMLTextAction {
        private SimpleAttributeSet tempSet;
        public InsertDivAction(String name) {
            super(name, "<div></div>", HTML.Tag.BODY, HTML.Tag.DIV);
        }
    }
    
    /**
     * Inserts a Div with a background image, and the width & height are set to the image size.
     */
    class InsertImageDivAction extends InsertHTMLTextAction {
        private SimpleAttributeSet tempSet;
        public InsertImageDivAction(String name) {
            super(name, "<div></div>", HTML.Tag.BODY, HTML.Tag.DIV);
        }
        public void actionPerformed(ActionEvent e) {
            if (callback == null) return;
            String s = callback.getInsertImageDivHtml(e); // this will allow file dialog to pick image file
            if (s == null) return;
            this.html = s;  // set the html to insert
            super.actionPerformed(e); // this will insert into doc
        }
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


    public Action getAction(String name) {
        if (name == null) return null;
        Action[] actions = this.getActions();
        for (int i=0; i<actions.length; i++) {
            String s = (String) actions[i].getValue(Action.NAME);
            if (s != null && s.equalsIgnoreCase(name)) return actions[i];
        }
        return null;
    }

    
    /**
     * Inserts an HTML ordered or unordered list 
     * @param html
     * @param bStartNewBlock if true, then this is a paragraph level insert (ex: OL, UL), if false then this is a char level.
     */
/*    
    public int insertHtmlList(OAHTMLDocument editorDocument, int pos, String html, HTML.Tag tag) throws Exception {
        Element ele =  editorDocument.getParagraphElement(pos);
        int pos1 = ele.getStartOffset();
        int pos2 = ele.getEndOffset();

// 20121031 changed to allow list to be inserted at cursor position        
       
        // use the end of current paragraph, so that a List does not split the paragraph
//was:        pos = Math.max(pos2 - 1, 0);  // before the <cr>, which will be pushed into a new <P>, which will need to be removed, which is done below
        
        // Notes:  
        //         if created at the end of doc, then it will append a <p>
        //      This will give "room" to be able to cursor before/after the List
        
        // vv hack: there is a problem when inserting html 1: at end of line, or 2: begin of document
        //      the HTML will need to take up a single ' '/space in the doc, but wont be rendered as a space
        
        editorDocument.insertString(pos, "  ", new SimpleAttributeSet(getInputAttributes())); // adds "fake" chars so that insert is not at begin/end of line
        int newPos = pos + 1;
        
        insertHTML(editorDocument, pos+1, html, (tag.isBlock()?1:0), 0, tag);

//qqqqqqq        
        editorDocument.remove(pos, 1);  // undo the fake out char #1
        editorDocument.remove(pos+2, 1); // undo the fake out char #2
        
        ele =  editorDocument.getParagraphElement(pos+2);
        pos1 = ele.getStartOffset();
        pos2 = ele.getEndOffset();
        if (pos2 - pos1 == 1) {
            int len = editorDocument.getLength();
            if (pos2 <= len) {
//was:                editorDocument.remove(pos1, 1); // undo the extra (empty) <p> that was created
            }
        }

        if (pos < 2) {
            // remove the empty <p> that was "push down" 
            ele =  editorDocument.getParagraphElement(pos-1);
            pos1 = ele.getStartOffset();
            pos2 = ele.getEndOffset();
            if (pos2 - pos1 == 1) {
                editorDocument.remove(pos, 1); // undo the extra (empty) <p> that was created
                newPos = pos;
            }
        }
return newPos;        
//was:        return newPos;
    }
*/
    
    
    /**
    * Used to supply images for a myImageView.  If null is returned,
    * then the myImageView will get the image.
    */
    protected abstract Image getImage(String src, URL url);

    /**
     * Callback to get HTML to insert for a Div Image
     */
    private Callback callback;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    public static class Callback {
        public String getInsertImageDivHtml(ActionEvent e) {
            return null;
        }
    }
}



