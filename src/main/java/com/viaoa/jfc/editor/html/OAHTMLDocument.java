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
package com.viaoa.jfc.editor.html;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.Tag;

/**
 * Document class used for OAHTMLTextPane. This should not be created directly,
 * since it is created by OAHTMLEditorKit, which is installed automatically by
 * OAHTMLTextPane. <br>
 * Added functionality to support:
 * <ul>
 * <li>document reading, converting to correct attributes, using CSS.Attribute.X
 * over HTML.Attribute.X where possible.
 * <li>printing flags to know if doc is being printed.
 * <li>Image cache
 * <li>Setting correct font sizes
 * </ul>
 * <p>
 * Font sizes: all fonts are read in and set to the pixel size based on screen
 * dpi. For printing, which uses point for font-size, the size is converted by
 * using a graphics.scale.
 * 
 * @author vvia
 */
public class OAHTMLDocument extends HTMLDocument {

    static final String IMAGE_CACHE_PROPERTY = "imageCache"; // copied from
                                                             // ImageView, to
                                                             // centralize image
                                                             // handler

    /**
     * Created by OAHtmlEditorKit, which is automatically installed for
     * OAHTMLTextPane.
     */
    public OAHTMLDocument(StyleSheet ss) {
        super(ss);
        setAsynchronousLoadPriority(0); // dont load Async 2006/06/30
        // setAsynchronousLoadPriority(4);
        // setTokenThreshold(100);

        try {
            File file = new File(".");
            setBase(file.toURI().toURL());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        putProperty(IMAGE_CACHE_PROPERTY, new Hashtable(17, .75f)); // see
                                                                    // clearImageCache()
    }

    // Parser/Reader
    public HTMLEditorKit.ParserCallback getReader(int pos) {
        Object desc = getProperty(Document.StreamDescriptionProperty);
        if (desc instanceof URL) {
            setBase((URL) desc);
        }

        /**
         * This will fix problems with Attributes. The goal is to have all font
         * sizes correct, and to use CSS.Attribute.X instead of HTML.Attribute.X
         * where ever possible.
         */
        HTMLDocument.HTMLReader reader = new HTMLDocument.HTMLReader(pos, 0, 0, null) {
            final String[] fontSizeValues = { "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large" };
            // From StyleSheet, font pt sizes for the 1-7 <font size=x>
            // conversion
            final int sizeMapDefault[] = { 8, 10, 12, 14, 18, 24, 36 };
            Stack<Boolean> stackNoBorder = new Stack<Boolean>();

            /**
             * This will clean up the attributeset to fix the following
             * problems: 1: html <font size=x>, where x = 1 to 7, will convert
             * to pixel size 1-7. This is changed to convert to a
             * syle=font-size: Xpt and the HTML.Attribue.SIZE is removed from
             * attributeset. 2: Default will create CSS.Attribute.X attributes
             * for all values. It will also create a
             * StyleConstant.NAME=HTML.Tag.X for the type of HTML tag. Then
             * there is another attribute HTML.Tag.X=SimpleAttributeSet (sas)
             * where sas has a list of HTML.Attribute.X values. Some of these
             * values have a corresponding CSS.Attribute.X already created. The
             * problem is that the view components will use the HTML.Attribute
             * and ignore the CSS.Attribute. This has been changed so that the
             * HTML.Attribute is removed, which then allows the CSS.Attribute to
             * be used. When the document is saved as html text, the
             * CSS.Attributes will be mapped back to the HTML attribute
             * name:value, or as a style.
             */
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                boolean b = false;
                if (t == HTML.Tag.TABLE) {
                    Object obj = a.getAttribute(HTML.Attribute.BORDER);
                    if ("0".equals(obj)) {
                        b = true;
                        stackNoBorder.push(true);
                    }
                    else {
                        stackNoBorder.push(false);
                    }
                }
                else if (t == HTML.Tag.TD) {
                    b = stackNoBorder.peek();
                }
                if (b) {
                    Object obj = a.getAttribute(HTML.Attribute.STYLE);
                    String style = "";
                    if (obj instanceof String) {
                        style = ((String) obj).trim();
                        style += " ";
                    }
                    if (style.indexOf("-style:") < 0 && style.indexOf("border-") < 0) {
                        style += "border-style: none";
                        a.addAttribute(HTML.Attribute.STYLE, style);
                    }
                }
                
                
                if (t == HTML.Tag.FONT) {
                    // vv hack: change font "size=x", which is from 1-7, to the
                    // actual point font size to use. Otherwise it is converted
                    // to 1-7 pixel size
                    Object obj = a.getAttribute(HTML.Attribute.SIZE);
                    if (obj instanceof String) {
                        try {
                            int x = Integer.valueOf((String) obj).intValue();
                            if (x > 0 && x < 8) {
                                a.removeAttribute(HTML.Attribute.SIZE);
                                x = sizeMapDefault[x - 1];
                                String s = (String) a.getAttribute(HTML.Attribute.STYLE);
                                if (s == null) s = "";
                                else s += ";";
                                s += "font-size: " + x + "pt";
                                a.addAttribute(HTML.Attribute.STYLE, s);
                            }
                        }
                        catch (Exception e) {
                        }
                    }
                }

                // have HTMLDocument.HTMLReader run
                try {
                    super.handleStartTag(t, a, pos);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }

                // vv hack: remove style attr from Tag, if they are already in
                // the char attributeset
                // otherwise, there will be duplicate attributes: one in content
                // and the other with the HTML.Attribute.XX
                MutableAttributeSet mas = (MutableAttributeSet) charAttr.getAttribute(t);
                if (mas != null) {
                    // remove any attr from mas that exist in charAttr
                    Enumeration en = mas.getAttributeNames();
                    ArrayList al = new ArrayList();
                    while (en.hasMoreElements()) {
                        Object obj = en.nextElement();
                        if (charAttr.getAttribute(obj) != null) {
                            al.add(obj);
                        }
                    }
                    for (Object objx : al) {
                        mas.removeAttribute(objx);
                    }
                    
                    b = (t == HTML.Tag.FONT || t == HTML.Tag.U || t == HTML.Tag.B || t == HTML.Tag.I || t == HTML.Tag.STRIKE || t == HTML.Tag.SUP || t == HTML.Tag.SUB);

                    if (b) {
                        // HTMDocument.HTML.Reader.CharacterAction will convert
                        // these HTML.Attributes to
                        // CSS.Attributes and add to charAttr
                        // need to remove the HTML.Attributes from mas, so that
                        // the charAttr will be used instead.
                        // The view components (HTML specific) will use the
                        // HTML.Attributes, so they need to be removed
                        // so that the charAttr will be used.
                        if (mas != null) {
                            mas.removeAttribute(HTML.Attribute.COLOR);
                            mas.removeAttribute(HTML.Attribute.SIZE);
                            mas.removeAttribute(HTML.Attribute.FACE);
                            mas.removeAttribute(HTML.Attribute.ALIGN);
                            mas.removeAttribute(HTML.Attribute.BGCOLOR);
                        }
                    }
                    if (b || t == HTML.Tag.SPAN) {
                        if (mas.getAttributeCount() == 0) {
                            charAttr.removeAttribute(t);
                        }
                    }
                    mas.removeAttribute(HTML.Attribute.STYLE); // converted to
                                                               // CSS.Attributes
                }
            }
            @Override
            public void handleEndTag(Tag t, int pos) {
                if (t == HTML.Tag.TABLE) {
                    stackNoBorder.pop();
                }
                super.handleEndTag(t, pos);
            }

            /**
             * This is called when reading is done and will call
             * convertHTMLtoCSS, to have any HTML.Attribute.X removed if it has
             * been converted to CSS.Attribute.X
             */
            @Override
            public void flush() throws BadLocationException {
                super.flush();
                convertHTMLtoCSS();
            }

        };
        return reader;
    }

    /**
     * Called once reading is complete, to convert any HTML.Attributes to
     * CSS.Attributes
     */
    protected void convertHTMLtoCSS() {
        try {
            writeLock();
            Element[] roots = this.getRootElements();
            for (Element ele : roots) {
                convertHTMLtoCSS(null, ele);
            }
            
        }
        finally {
            writeUnlock();
        }
    }

    private void convertHTMLtoCSS(Element parent, Element child) {
        MutableAttributeSet childAttribSet = (MutableAttributeSet) child.getAttributes();
        
        Object nameAttrib = childAttribSet.getAttribute(StyleConstants.NameAttribute);
        boolean bRemovedHack = false;
        
        if (nameAttrib != null && (nameAttrib.equals(HTML.Tag.TD) || nameAttrib.equals(HTML.Tag.TH)) ) {
            // the translateHTMLToCSS method will add styles (not needed) to TD,TH from Table values
            //   if the TD/TH does not have a CSS/Style defined for the border, then it will use the table value
            childAttribSet.removeAttribute(StyleConstants.NameAttribute);
            bRemovedHack = true;
        }
        
        BlockElement blockElement = new BlockElement(parent, childAttribSet);
        MutableAttributeSet newAttribSet = (MutableAttributeSet) getStyleSheet().translateHTMLToCSS(blockElement);

        if (bRemovedHack) childAttribSet.addAttribute(StyleConstants.NameAttribute, nameAttrib);
        
        if (newAttribSet.getAttributeCount() > 0) {
            childAttribSet.addAttributes(newAttribSet);
            removeUnneededHTMLAttributes(parent, childAttribSet);
        }
        // recurse children
        int x = child.getElementCount();
        for (int i = 0; i < x; i++) {
            Element ele = child.getElement(i);
            convertHTMLtoCSS(child, ele);
        }
    }

    private void removeUnneededHTMLAttributes(Element parentElement, MutableAttributeSet attribSet) {
        // remove any HTML.Attributes that were converted to CSS.Attributes
        
        ArrayList al = null;
        for (int i=0; i<5;i++) {  // fixes concurrency issue
            try {
                Enumeration enumx = attribSet.getAttributeNames();
                al = new ArrayList();
                while (enumx.hasMoreElements()) {
                    Object key = enumx.nextElement();
                    al.add(key);
                }
            }
            catch (Exception e) {
                continue;
            }
            break;
        }
        
        for (Object key : al) {
            if (key instanceof HTML.Tag) {
                // the value is a SimpletAttributeSet, need to loop through them
                // to remove HTML.Attributes that have been converted to
                // CSS.Attrbutes
                MutableAttributeSet as = (MutableAttributeSet) attribSet.getAttribute(key);

                removeUnneededHTMLAttributes(parentElement, as);
            }
            else if (key instanceof HTML.Attribute) {
                if (key == HTML.Attribute.VALIGN) {
                    // hack: HTML.Attribute.VALIGN is needed, since
                    // text.html.TabelView does not use use css.vertical-align,
                    // see javax.swing.text.html.CSS.java for list of support
                    // tags
                    continue;
                }
                if (key == HTML.Attribute.CELLPADDING || key == HTML.Attribute.CELLSPACING || key == HTML.Attribute.BORDER) {
                    // hack: needed by tables 20120602
                    continue;
                }
                Object val = attribSet.getAttribute(key);
                MutableAttributeSet mas = new SimpleAttributeSet();
                mas.addAttribute(key, val);
                // need to find out if key/value was converted from
                // HTML.Attribute to CSS.Attribute
                // this is done by using the same code that the View components
                // use.
                BlockElement blockElement = new BlockElement(parentElement, mas);
                MutableAttributeSet a = (MutableAttributeSet) getStyleSheet().translateHTMLToCSS(blockElement);
                if (a.getAttributeCount() > 0) { // it does get converted and
                                                 // exists as a CSS.Attribute.X
                                                 // in the element attributSet
                    attribSet.removeAttribute(key);
                }
            }
        }
    }

    /**
     * Document property "imageCache" is defined in ImageView to cache images.
     * When image is not found, then it calls
     * Toolkit.getDefaultToolkit().getImage() which caches images. If the file
     * image has changed, then the new image is never retrieved.
     */
    public void clearImageCache() {
        Object obj = getProperty(IMAGE_CACHE_PROPERTY);
        if (obj instanceof Hashtable) ((Hashtable) obj).clear();
    }

    public void clearImageCache(URL key) {
        Object obj = getProperty(IMAGE_CACHE_PROPERTY);
        if (obj instanceof Hashtable) ((Hashtable) obj).remove(key);
    }

    /**
     * This is called to insert new elements into document. When data is pasted
     * into document, then it will be converted and will then call this method
     * to create the elements.
     */
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        // this is helpful during debuging, when html code is inserted into the
        // document
        super.insert(offset, data);
    }

    // similar to HTMLDocument.setParagraphAttributes, except this only changes
    // an element
    public void setAttributes(Element e, AttributeSet attributeSet) {
        try {
            writeLock();
            int offset = e.getStartOffset();
            int length = Math.max(0, e.getEndOffset() - offset);

            DefaultDocumentEvent changes = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            AttributeSet asCopy = attributeSet.copyAttributes();

            changes.addEdit(new AttributeUndoableEdit(e, asCopy, true));

            MutableAttributeSet attr = (MutableAttributeSet) e.getAttributes();

            attr.removeAttributes(attr);
            attr.addAttributes(attributeSet);

            changes.end();
            fireChangedUpdate(changes);
            fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        }
        finally {
            writeUnlock();
        }
    }

    // Hack used by insertAfterEnd
    private boolean bGetLengthHack;
    @Override
    public int getLength() {
        int x = super.getLength();
        if (bGetLengthHack) {
            x++;
            bGetLengthHack = false;
        }
        return x;
    }
    // Hack used by insertAfterEnd
    @Override
    public String getText(int offset, int length) throws BadLocationException {
        String s = super.getText(offset, length);
        if (bGetLengthHack) {
            if (length == 1 && s != null && s.length() > 0 && s.charAt(0) == '\n') s = "X";
            bGetLengthHack = false;
        }
        return s;
    }
    
    // Hack to fix when trying to add to end of doc
    @Override
    public void insertAfterEnd(Element elem, String htmlText) throws BadLocationException, IOException {
        if (elem != null) {
            int offset = elem.getEndOffset();
            if (offset > getLength()) {
                bGetLengthHack = true;
            }
            else if (elem.isLeaf() && getText(offset - 1, 1).charAt(0) == '\n') {
                bGetLengthHack = true;
            }
        }        
        super.insertAfterEnd(elem, htmlText);
        bGetLengthHack = false;
    }
    
    
    
    // ========== TEST

    public static void main(String[] args) throws Exception {
        URL key = new URL("file://c:/temp/testPicture.jpg");

        File file = new File(key.getPath());

        // Object obj = key.getContent();
        Image img = (Image) Toolkit.getDefaultToolkit().createImage(file.getPath());
        int w = img.getWidth(new ImageObserver() {
            int cnt = 0;

            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return (++cnt) < 5;
            }
        });
        try {
            Thread.currentThread().sleep(55000);
        }
        catch (Exception e) {
        }
    }

    
}
