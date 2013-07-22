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
package com.viaoa.jfc.editor.html.view;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.editor.html.*;
import com.viaoa.jfc.editor.html.oa.*;
import com.viaoa.jfc.editor.html.oa.DocAttribute;
import com.viaoa.jfc.editor.html.oa.DocElement;
import com.viaoa.jfc.*;

/**
 * Used for testing and viewing OAHTML* classes.
 * note: see sample code below to use in app.
 * @author vincevia
 */
public class HtmlDebug {
    private OAHTMLTextPane editor;
    private OATree tree;
    private Hub hubElement;
    
    public HtmlDebug(OAHTMLTextPane editor) {
        this.editor = editor;
    }
    
    OAHTMLDocument getOAHTMLDocument() {
        return (OAHTMLDocument) editor.getDocument();
    }
    
    public JPanel getPanel() {
        JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout());
        tree = new OATree(12); 
        
        OATreeTitleNode tnode = new OATreeTitleNode("Tree Elements");
        
        tree.setRoot(tnode);
        
        OATreeNode node = new OATreeNode("name", getElementHub());
        tnode.add(node);
        
        OATreeNode node1 = new OATreeNode("docElements.name");
        node.add(node1);

        OATreeNode node2 = new OATreeNode("docAttributes.name");
        node1.add(node2);
        
        
        node1.add(node1);
        
        pan.add(new JScrollPane(tree));
        
        JButton cmd = new JButton("Refresh");
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        OACommand.setup(cmd);
        
        JPanel panCmd = new JPanel(new FlowLayout());
        panCmd.add(cmd);
        pan.add(panCmd, BorderLayout.SOUTH);
        
        return pan;
    }
    
    public void update() {
        Element ele = getOAHTMLDocument().getDefaultRootElement();
        getElementHub().clear();
        loadElements(ele, getElementHub());
        tree.expandAll();
        
        StyleSheet styles = getOAHTMLDocument().getStyleSheet();
        Enumeration rules = styles.getStyleNames();
        while (rules.hasMoreElements()) {
            String name = (String) rules.nextElement();
            Style rule = styles.getStyle(name);
            System.out.println(rule.toString());
        }
        getOAHTMLDocument().dump(System.out);
    }

    
    void loadElements(Element ele, Hub hubDocElement) {
        DocElement de = new DocElement();
        
        String name = ele.getName() + "["+getClassName(ele)+","+ele.getStartOffset()+","+ele.getEndOffset()+"]"; 
        
        int p1 = ele.getStartOffset();
        int p2 = ele.getEndOffset();
        
        if (ele.isLeaf()) {
            try {
                String s = getOAHTMLDocument().getText(p1, p2-p1);
                s = s.replace('\n', '~');
                name += ": \"" + s + "\"";
            }
            catch (Exception e) {
                System.out.println("Error: "+e);
            }
        }
        
        de.setName(name);
        hubDocElement.add(de);

        AttributeSet ats = ele.getAttributes();
        Enumeration enumx = ats.getAttributeNames();
        for ( ;enumx.hasMoreElements(); ) {
            Object o1 = enumx.nextElement();
            Object o2 = ats.getAttribute(o1);
            DocAttribute da = new DocAttribute();
            da.setName(o1.toString()+" ["+getClassName(o1)+"]" + " = " + o2.toString()+" ["+getClassName(o2)+"]");
            de.getDocAttributes().add(da);
        }
        
        int x = ele.getElementCount();
        for (int i=0; i<x; i++) {
            Element e = ele.getElement(i);
            loadElements(e, de.getDocElements());
        }
    }
    
    String getClassName(Object o) {
        Class c = o.getClass();
        String s = c.getName();
        s = s.replace("javax.swing.text.", "");
        s = s.replace("html.", "");
        return s;
    }
    
    Hub getElementHub() {
        if (hubElement == null) {
            hubElement = new Hub(DocElement.class);
        }
        return hubElement;
    }
    
/*
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    sp.setDividerSize(10);
    sp.setOneTouchExpandable(true);
    
    sp.setLeftComponent(getOutlinePanel());
    sp.setRightComponent(new JScrollPane(editor));
    //sp.setDividerLocation(sp.getMinimumDividerLocation());
*/
}

