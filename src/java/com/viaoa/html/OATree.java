/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.html;


import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;


public class OATree extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected OATreeNode root;
    protected String font;
    protected String imageNameCollapse = "treeCollapse.gif";
    protected String imageNameExpand = "treeExpand.gif";
    protected String imageNameCollapseForm = "treeCollapseForm.gif";
    protected String imageNameExpandForm = "treeExpandForm.gif";
    protected String imageNameNodeOpened = "treeOpen.gif";
    protected String imageNameNodeClosed = "treeClose.gif";
    protected String imageNameLeaf = "treeLeaf.gif";
    protected String imageNameSpacer = "treeSpacer.gif";
    protected String selectColor = "yellow";
    protected OATreeData selectData;
    protected OATreeData expandData;
    protected OATreeData collapseData;
    protected OATreeData expandFormData;
    protected OATreeData collapseFormData;
    protected String colorBackground;
    protected Object selectObject;
    protected boolean bShowNew=true;
    Vector vector = new Vector(25,25); // displayed nodes - OATreeData
    Vector vectorOld;
    private boolean bExpandingAll,bCollaspingAll;
    protected boolean bWrap;  // allow text to wrap

    public void expandAll() {
        bExpandingAll = true;
        getHtml();
        bExpandingAll = false;
    }
    public void collapseAll() {
        bCollaspingAll = true;
        getHtml();
        bCollaspingAll = false;
    }
    
    public OATree() {
        root = new OATreeNode(""); 
    }

    /** @returns the selected object. */
    public Object getSelectedObject() {
        if (selectObject != null) return selectObject;
        if (selectData == null) return null;
        else return selectData.object;
    }

    /** set the selected object.  Note: this does not set the activeObject in the Hub or UpdateHub. */
    public void setSelectedObject(Object obj) {
        if (obj == null) selectData = null;
        selectObject = obj;
    }

    /** if false, then objects that are "new" are not displayed. Default: true */
    public void setShowNew(boolean b) {
        bShowNew = b;
    }
    public boolean getShowNew() {
        return bShowNew;
    }
    /** if false then displayed data will not wrap. Default: false */
    public void setAllowWrap(boolean b) {
        bWrap = b;
    }
    public boolean getAllowWrap() {
        return bWrap;
    }

    
    public void add(OATreeNode node) {
        if (node.hub == null && !node.titleFlag) {
            throw new RuntimeException("OATree.add() node is not a TitleNode and does not have a Hub assigned");
        }
        root.add(node);
        node.setTree(this);
    }
    
    protected boolean isExpanded(OATreeNode node, OATreeData parent, Object obj) {
        if (bExpandingAll) {
            // check for recursive nodes
            if (parent != null) {
                OATreeData p = parent;
                for ( ;p != null; p = p.parent) {
                    if (p.node == node) {
                        if (p.object == obj) return false;
                    }
                }
            }
            return true;
        }
        if (bCollaspingAll) return false;

        if (node.bExpandingAll) return true;
        if (node.bCollaspingAll) return false;
        
        if (node.treeNodeChildren.length == 0) return false;

        if (expandData != null) {
            if (obj == expandData.object) {
                if (areEqual(parent, expandData.parent)) return true;
            }
        }
        
        if (collapseData != null) {
            if (obj == collapseData.object) {
                if (areEqual(parent, collapseData.parent)) return false;
            }
        }

        if (vectorOld == null) return false;
        int x = vectorOld.size();
        for (int i=0; i<x; i++) {
            OATreeData data = (OATreeData) vectorOld.elementAt(i);
            if (obj == data.object && data.bExpanded) {
                if (areEqual(parent, data.parent)) return true;
            }
        }                    

        return false;
    }

    protected boolean isFormExpanded(OATreeNode node, OATreeData parent, Object obj) {
        if (bCollaspingAll) return false;
        if (node.treeNodeChildren.length == 0) return false;

        if (expandFormData != null) {
            if (obj == expandFormData.object) {
                if (areEqual(parent, expandFormData.parent)) return true;
            }
        }
        
        if (collapseFormData != null) {
            if (obj == collapseFormData.object) {
                if (areEqual(parent, collapseFormData.parent)) return false;
            }
        }

        if (vectorOld == null) return false;
        int x = vectorOld.size();
        for (int i=0; i<x; i++) {
            OATreeData data = (OATreeData) vectorOld.elementAt(i);
            if (obj == data.object && data.bFormExpanded) {
                if (areEqual(parent, data.parent)) return true;
            }
        }                    

        return false;
    }
    
    private boolean areEqual(OATreeData d1, OATreeData d2) {
        // must match all the way to root
        for (; d1 != null && d2 != null; ) {
            if (d1.object != d2.object) break;
            d1 = d1.parent;
            d2 = d2.parent;
        }
        return (d1 == null && d2 == null);
    }

    protected boolean isSelected(OATreeNode node, OATreeData parent, Object obj) {
        if (selectObject != null) return (obj == selectObject);
        
        if (selectData == null) return false;

        if (obj == selectData.object) {
            return areEqual(parent, selectData.parent);
        }
        return false;
    }
    
    
    public String getSelectColor() {
        return selectColor;
    }
    public void setSelectColor(String color) {
        selectColor = color;
    }
    public String getExpandImageName() {
        return imageNameExpand;
    }
    public void setExpandImageName(String imageName) {
        this.imageNameExpand = imageName;
    }
    public String getCollapseImageName() {
        return imageNameCollapse;
    }
    public void setCollapseImageName(String imageName) {
        this.imageNameCollapse = imageName;
    }

 
    public String getExpandFormImageName() {
        return imageNameExpandForm;
    }
    public void setExpandFormImageName(String imageName) {
        this.imageNameExpandForm = imageName;
    }
    public String getCollapseFormImageName() {
        return imageNameCollapseForm;
    }
    public void setCollapseFormImageName(String imageName) {
        this.imageNameCollapseForm = imageName;
    }


    public String getNodeOpenedImageName() {
        return imageNameNodeOpened;
    }
    public void setNodeOpenedImageName(String imageName) {
        this.imageNameNodeOpened = imageName;
    }

    public String getNodeClosedImageName() {
        return imageNameNodeClosed;
    }
    public void setNodeClosedImageName(String imageName) {
        this.imageNameNodeClosed = imageName;
    }

    public String getLeafImageName() {
        return imageNameLeaf;
    }
    public void setLeafImageName(String imageName) {
        this.imageNameLeaf = imageName;
    }
    public String getSpacerImageName() {
        return imageNameSpacer;
    }
    public void setSpacerImageName(String imageName) {
        this.imageNameSpacer = imageName;
    }


    /** background color for tree */
    public void setBackground(String background) {
        this.colorBackground = background;
    }
    public String getBackground() {
        return this.colorBackground;
    }

    /** @param font String within the &lt;font&gt; tag.  Ex: size="1"  */
    public void setFont(String font) {
        this.font = font;
    }
    public String getFont() {
        return font;
    }
    
    /************************** OAHtmlComponent ************************/
    /** called by OAForm.processRequest() */
    protected void beforeSetValuesInternal() {
        expandData = collapseData = null;
        expandFormData = collapseFormData = null;
    }
    
    protected String processCommand(OASession session, OAForm form, String command) {
        // oacommand_8_TreeNamee1   cmd="e"  vectorPos=1

        int len = 0;
        String s = "";
        try {
            s = com.viaoa.html.Util.field(command,'_',2);
            len = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
        }
        
        s = com.viaoa.html.Util.field(command,'_',3);
        char cmd = s.charAt(len);

        int pos=0;
        try {
            s = s.substring(len+1);
            pos = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
        }
        
        OATreeData data = (OATreeData) vector.elementAt(pos);

        switch (cmd) {
            case 'c':  // collapse node
                collapseData = data;
                break;
            case 'e':  // expand node
                expandData = data;
                break;
            case 'C':  // collapse node Form
                collapseFormData = data;
                break;
            case 'E':  // expand node Form
                expandFormData = data;
                break;
            case 's':  // select node
                selectData = data;
                selectObject = null;
                // update active objects in hubs
                if (data != null) recursiveSetActive(data);
                break;
        }
        return null;
    }

    protected void recursiveSetActive(OATreeData data) {
        if (data.parent != null) recursiveSetActive(data.parent);
        if (data.node.updateHub != null) {
            if (data.node.updateHub != data.hub) {
                data.node.updateHub.setSharedHub(data.hub);
            }
            data.node.updateHub.setActiveObject(data.object);
        }
    }


    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
    }

    public String getRawValue() {
        return null;
    }

    public void setOrigValue(String s){
    }
    public String getOrigValue() {
        return null;
    }

    public boolean isChanged() {
        return false;
    }
    public void reset() {
    }
    
    public void update() {
    }

    /** Used by OATreeNode.  returns position to use as an identifier */
    protected int addData(OATreeData data) {
        if (selectObject != null && data.object == selectObject) {
            selectData = data;
            selectObject = null;
        }
        int x = vector.size();
        vector.addElement(data);
        return x;
    }
    
    public String getHtml() {
        if (name == null) name = "NoName";
        vectorOld = vector;
        vector = new Vector(25,25);

        StringBuffer sb = new StringBuffer(2048);
        String s = colorBackground;
        if (s == null) s = "";
        else s = "bgcolor=\""+s+"\" ";
        sb.append("<TABLE "+s+"border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
        
        try {
            for (int i=0; i < root.treeNodeChildren.length; i++) {
                OATreeNode node = root.treeNodeChildren[i];
                node.getHtml(sb, this);
            }
        }
        catch (Exception e) {
            handleException(e,"getHtml()");
            sb.append("Exception Occured");
        }
        
        
        sb.append(System.getProperty("line.separator"));
        sb.append("</TABLE>");
        sb.append(System.getProperty("line.separator"));
        lastValue = new String(sb);
        return lastValue;
    }

    private String lastValue;
    public boolean needsRefreshed() {
        String s = lastValue;
        String s2 = getHtml();
        return (s == null || !s.equals(s2));
    }


}
