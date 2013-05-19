package com.viaoa.jfc.editor.html.control;

import java.awt.Color;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.editor.html.OAHTMLDocument;
import com.viaoa.jfc.editor.html.OAHTMLEditorKit;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.editor.html.oa.Block;
import com.viaoa.jfc.editor.html.oa.DocElement;
import com.viaoa.jfc.editor.html.view.BlockDialog;

/**
 * Controller for BlockDialog, used to edit a paragraph (block) attributes.
 * @author vvia
 *
 */
public class BlockController {
    private OAHTMLTextPane textPane;

    private Hub<DocElement> hubRootDocElement;
    private Hub<DocElement> hubDocElement;  // recursive detail hub
    private Hub<Block> hubBlock;
    private BlockDialog dlgBlock;

    private Element element;
    private AttributeSet attributeSet;
    
    public BlockController(OAHTMLTextPane textPane) {
        this.textPane = textPane;
    }

    public Hub<Block> getBlockHub() {
        if (hubBlock == null) {
            hubBlock = new Hub<Block>(Block.class);
            hubBlock.add(new Block());
            hubBlock.setPos(0);
        }
        return hubBlock;
    }
    public Block getBlock() {
        Block block = getBlockHub().getAt(0);
        return block;
    }
    
    public Hub<DocElement> getRootDocElements() {
        if (hubRootDocElement == null) {
            hubRootDocElement = new Hub<DocElement>(DocElement.class);
        }
        return hubRootDocElement;
    }
    public Hub<DocElement> getDocElements() {
        if (hubDocElement == null) {
            hubDocElement = new DetailHub(getRootDocElements(), "docElements");
            hubDocElement.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    DocElement de = (DocElement) e.getObject();
                    if (de == null) return;
                    element = hmElement.get(de);
                    attributeSet = element.getAttributes();
                    updateBlock();
                }
            });
        }
        return hubDocElement;
    }
    
    
    public BlockDialog getBlockDialog() {
        if (dlgBlock == null) {
            dlgBlock = new BlockDialog(SwingUtilities.getWindowAncestor(textPane), getBlockHub(), getRootDocElements(), getDocElements()) {
                @Override
                protected void onApply() {
                    apply();
                }

                @Override
                protected void onOk() {
                    apply();
                    super.onOk();
                }

                @Override
                public void setVisible(boolean b) {
                    if (b) initSelectedElement();
                    super.setVisible(b);
                }
            };
        }
        return dlgBlock;
    }

    
    protected void initSelectedElement() {
        getRootDocElements().clear();
        hmDocElement.clear();
        hmElement.clear();

        OAHTMLDocument doc = (OAHTMLDocument) textPane.getDocument();
        loadElements(doc.getRootElements()[0], getRootDocElements());

        hubRootDocElement.setPos(0);
        
        int position = textPane.getCaretPosition();
        element = doc.getParagraphElement(position);
        attributeSet = element.getAttributes();
        
        DocElement de = hmDocElement.get(element);
        getDocElements().setAO(de);
        
        // set tree node
        ArrayList<DocElement> al = new ArrayList<DocElement>();
        Element ele = element;
        for ( ; ele != null; ele = ele.getParentElement()) {
            de = hmDocElement.get(ele);
            al.add(0, de);
        }
        DocElement[] des = new DocElement[al.size()];
        al.toArray(des);
        getBlockDialog().getTreeComboBox().getTree().setSelectedNode(des);
        
        
    }
    

    private HashMap<DocElement, Element> hmElement = new HashMap<DocElement, Element>();
    private HashMap<Element, DocElement> hmDocElement = new HashMap<Element, DocElement>();
    private void loadElements(Element ele, Hub hubDocElement) {
        DocElement de = new DocElement();
        hmElement.put(de, ele);
        hmDocElement.put(ele, de);
        
        String name = ele.getName() + "["+ele.getStartOffset()+","+ele.getEndOffset()+"] "; 
        hubDocElement.add(de);

        int x = ele.getElementCount();
        for (int i=0; i<x; i++) {
            Element e = ele.getElement(i);
            
            if (e.isLeaf()) {
                try {
                    int p1 = e.getStartOffset();
                    int p2 = e.getEndOffset();
                    String s = textPane.getDocument().getText(p1, p2-p1);
                    s = OAString.convert(s, "\n", "\\n");
                    if (i == 0) name += " \"";
                    name += s;
                    if (i+1 == x) name += "\"";
                }
                catch (Exception ex) {
                    System.out.println("Error: "+ex);
                }
            }
            else {
                loadElements(e, de.getDocElements());
            }
        }
        de.setName(name);
    }

    
    private void updateBlock() {
        Block block = getBlock();

        OAHTMLDocument doc = (OAHTMLDocument) textPane.getDocument();
        OAHTMLEditorKit kit = (OAHTMLEditorKit) textPane.getEditorKit();
        /*        
        if (attributeSet.getAttribute(StyleConstants.NameAttribute) != HTML.Tag.DIV) {
            Element elemx = element.getParentElement();
            AttributeSet asx = elemx.getAttributes();
            
            if (asx.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.DIV) {
                element = elemx;
                attributeSet = asx;
            }            
        }
         */
        SimpleAttributeSet sas = new SimpleAttributeSet(attributeSet);

        Object obj;
        obj = sas.getAttribute(CSS.Attribute.WIDTH);
        block.setWidth(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.HEIGHT);
        block.setHeight(obj == null ? 0 : OAConv.toInt(obj.toString()));

        block.setMargin(0);
        obj = sas.getAttribute(CSS.Attribute.MARGIN_TOP);
        block.setMarginTop(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.MARGIN_RIGHT);
        block.setMarginRight(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.MARGIN_BOTTOM);
        block.setMarginBottom(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.MARGIN_LEFT);
        block.setMarginLeft(obj == null ? 0 : OAConv.toInt(obj.toString()));

        block.setPadding(0);
        obj = sas.getAttribute(CSS.Attribute.PADDING_TOP);
        block.setPaddingTop(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.PADDING_RIGHT);
        block.setPaddingRight(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.PADDING_BOTTOM);
        block.setPaddingBottom(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.PADDING_LEFT);
        block.setPaddingLeft(obj == null ? 0 : OAConv.toInt(obj.toString()));

        block.setBorderWidth(0);
        obj = sas.getAttribute(CSS.Attribute.BORDER_TOP_WIDTH);
        block.setBorderTopWidth(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.BORDER_RIGHT_WIDTH);
        block.setBorderRightWidth(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.BORDER_BOTTOM_WIDTH);
        block.setBorderBottomWidth(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.BORDER_LEFT_WIDTH);
        block.setBorderLeftWidth(obj == null ? 0 : OAConv.toInt(obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.BORDER_COLOR);
        block.setBorderColor(obj == null ? null : (Color) OAConv.convert(Color.class, obj.toString()));

        obj = sas.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        block.setBackgroundColor(obj == null ? null : (Color) OAConv.convert(Color.class, obj.toString()));
    }

    public void apply() {
        Block block = getBlock();

        OAHTMLDocument doc = (OAHTMLDocument) textPane.getDocument();
        OAHTMLEditorKit kit = (OAHTMLEditorKit) textPane.getEditorKit();

        /*
        int pos = textPane.getCaretPosition();
        element = doc.getParagraphElement(pos);
        attributeSet = element.getAttributes();
        */
        
        SimpleAttributeSet sas = new SimpleAttributeSet(attributeSet);

        if (attributeSet.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMPLIED) {
            sas.removeAttribute(StyleConstants.NameAttribute);
            sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
        }
        
        
        sas.removeAttribute(CSS.Attribute.WIDTH);
        sas.removeAttribute(CSS.Attribute.HEIGHT);

        sas.removeAttribute(CSS.Attribute.MARGIN_TOP);
        sas.removeAttribute(CSS.Attribute.MARGIN_RIGHT);
        sas.removeAttribute(CSS.Attribute.MARGIN_BOTTOM);
        sas.removeAttribute(CSS.Attribute.MARGIN_LEFT);

        sas.removeAttribute(CSS.Attribute.PADDING_TOP);
        sas.removeAttribute(CSS.Attribute.PADDING_RIGHT);
        sas.removeAttribute(CSS.Attribute.PADDING_BOTTOM);
        sas.removeAttribute(CSS.Attribute.PADDING_LEFT);

        sas.removeAttribute(CSS.Attribute.BORDER_TOP_WIDTH);
        sas.removeAttribute(CSS.Attribute.BORDER_RIGHT_WIDTH);
        sas.removeAttribute(CSS.Attribute.BORDER_BOTTOM_WIDTH);
        sas.removeAttribute(CSS.Attribute.BORDER_LEFT_WIDTH);
        sas.removeAttribute(CSS.Attribute.BORDER_COLOR);
        sas.removeAttribute(CSS.Attribute.BORDER_STYLE);

        sas.removeAttribute(CSS.Attribute.BACKGROUND_COLOR);

        String style = block.getStyle();

        Object obj = sas.getAttribute(CSS.Attribute.BACKGROUND_IMAGE);
        if (obj != null) {
            sas.removeAttribute(CSS.Attribute.BACKGROUND_IMAGE);
            int w = block.getWidth();
            int h = block.getHeight();

            String s = obj.toString();
            int pos = s.indexOf('&');
            if (pos < 0)  pos = s.indexOf(')');
            if (pos > 0) s = s.substring(0, pos);

            if (s.indexOf('?') > 0) s += "&";
            else s += "?";
            s += "w="+w+"&h="+h+"";
            
            s = "url('" + s.substring(4) + "')";
            
            style += "background-image:" + s + ";";
        }
        
        AttributeSet asx = doc.getStyleSheet().getDeclaration(style);
        sas.addAttributes(asx);
        
        

        ((OAHTMLDocument) textPane.getDocument()).setAttributes(element, sas);
        /*was
        if (attributeSet.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.DIV) {
            ((OAHTMLDocument) textPane.getDocument()).setAttributes(element, sas);
        }
        else {
            if (attributeSet.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMPLIED) {
                sas.removeAttribute(StyleConstants.NameAttribute);
                sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
            }
            ((OAHTMLDocument) textPane.getDocument()).setParagraphAttributes(position, 1, sas, true);
        }
        */
    }

    

}
