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
package com.viaoa.jfc.editor.html.control;


import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.editor.html.OAHTMLDocument;
import com.viaoa.jfc.editor.html.OAHTMLEditorKit;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.editor.html.oa.DocElement;
import com.viaoa.jfc.editor.html.oa.Insert;
import com.viaoa.jfc.editor.html.view.InsertDialog;

/**
 * Controller for InsertDialog, which allows inserting html & attributes.
 * @author vvia
 *
 */
public class InsertController {
    private OAHTMLTextPane textPane;

    private Hub<DocElement> hubRootDocElement;
    private Hub<DocElement> hubDocElement;  // recursive detail hub
    private Hub<Insert> hubInsert;
    private InsertDialog dlgInsert;

    private Element element;
    private AttributeSet attributeSet;
    private int position;
    
    public InsertController(OAHTMLTextPane textPane) {
        this.textPane = textPane;
    }

    public Hub<Insert> getInsertHub() {
        if (hubInsert == null) {
            hubInsert = new Hub<Insert>(Insert.class);
            hubInsert.add(new Insert());
            hubInsert.setPos(0);
        }
        return hubInsert;
    }
    public Insert getInsert() {
        Insert ins = getInsertHub().getAt(0);
        return ins;
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
                    position = element.getStartOffset();
                }
            });
        }
        return hubDocElement;
    }
    
    
    public InsertDialog getInsertDialog() {
        if (dlgInsert == null) {
            dlgInsert = new InsertDialog(SwingUtilities.getWindowAncestor(textPane), getInsertHub(), getRootDocElements(), getDocElements()) {
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
        return dlgInsert;
    }

    
    protected void initSelectedElement() {
        getRootDocElements().clear();
        hmDocElement.clear();
        hmElement.clear();

        OAHTMLDocument doc = (OAHTMLDocument) textPane.getDocument();
        loadElements(doc.getRootElements()[0], getRootDocElements());

        hubRootDocElement.setPos(0);
        
        position = textPane.getCaretPosition();
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
        getInsertDialog().getTreeComboBox().getTree().setSelectedNode(des);
        
        
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

    public void apply() {
        try {
            _apply();
        }
        catch (Exception e) {
            // TODO: handle exception
        }
    }    

    public void _apply() throws Exception {
        Insert insert = getInsert();

        OAHTMLDocument doc = (OAHTMLDocument) textPane.getDocument();
        OAHTMLEditorKit kit = (OAHTMLEditorKit) textPane.getEditorKit();
        
        //note: OAHTMLDocument has "hacks" for insertAfterEnd, when it is at the end of document
        
        if (insert.getType() == Insert.TYPE_BR) {
            switch (insert.getLocation()) {
            case Insert.LOCATION_Inside:
                doc.insertString(position, "  ", new SimpleAttributeSet(attributeSet)); // adds "fake" chars so that insert is not at begin/end of line
                kit.insertHTML(doc, position+1, "<br>", 0, 0, HTML.Tag.BR);
                doc.remove(position, 1);  
                doc.remove(position+1, 1); 
                break;
            case Insert.LOCATION_Before:
                doc.insertBeforeStart(element, "<br>");
                break;
            case Insert.LOCATION_After:
                doc.insertAfterEnd(element, "<br>");
                break;
            }
            return;
        }
        
        if (insert.getType() == Insert.TYPE_P) {
            switch (insert.getLocation()) {
            case Insert.LOCATION_Inside:
                // see OAHTMLEditorKit.InsertBreakAction inner class
                textPane.replaceSelection("\n");
                SimpleAttributeSet sas = new SimpleAttributeSet(attributeSet);
                Element elemx = doc.getParagraphElement(position+1);
                sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
                doc.setParagraphAttributes(position+1, 1, sas, true);
                break;
            case Insert.LOCATION_Before:
                doc.insertBeforeStart(element, "<p></p>");
                break;
            case Insert.LOCATION_After:
                doc.insertAfterEnd(element, "<p></p>");
                break;
            }
            return;
        }
        
        if (insert.getType() == Insert.TYPE_DIV) {
            switch (insert.getLocation()) {
            case Insert.LOCATION_Inside:
                doc.insertString(position, "  ", new SimpleAttributeSet(attributeSet)); // adds "fake" chars so that insert is not at begin/end of line
                kit.insertHTML(doc, position+1, "<div></div>", 0, 0, HTML.Tag.DIV);
                doc.remove(position, 1);  
                doc.remove(position+1, 1); 
                break;
            case Insert.LOCATION_Before:
                doc.insertBeforeStart(element, "<div></div>");
                break;
            case Insert.LOCATION_After:
                doc.insertAfterEnd(element, "<div></div>");
                break;
            }
            return;
        }
    }

    

}
