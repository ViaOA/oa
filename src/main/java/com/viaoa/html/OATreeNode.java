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

import java.util.Vector;
import java.lang.reflect.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.util.*;
import java.util.*;

/** TreeNode for OATree.  */
public class OATreeNode extends OAHtmlComponent implements Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    String propertyPath;  // property path after the path to a hub   ex: emps.dept.name => dept.name
    boolean titleFlag;
    Method[] methodsToHub; // methods to find Hub
    Method[] methodsToObject; // methods to find Object (when no hubs are in path)
    Method[] methodsToProperty;  // methods to get property value from object => name
    boolean showAll = true;  // if false then only the activeObject is shown, if true then all objects in hub are used
    OATreeNode[] treeNodeChildren = new OATreeNode[0];
    Hub hub;
    Hub updateHub;
    String font;
    String colorBackground;
    OATree tree; // set when added to tree or node
    Hub hubFilter;
    protected Vector vecColumns;

    public OATreeNode(String path) {
        this.propertyPath = path;
    }
    /** @param hub is hub to use for data and/or hub to update. */
    public OATreeNode(String path, Hub hub) {
        this(path, hub, hub);
    }

    /** @param property from hub */
    public void addColumn(String property, String heading, int columns) {
        OATableColumn tc = new OATableColumn(property, heading, columns);
        if (vecColumns == null) vecColumns = new Vector(5,3);
        vecColumns.add(tc);
    }

    protected void setTree(OATree tree) {
        this.tree = tree;
        for (int i=0; i < treeNodeChildren.length; i++) {
            OATreeNode node = treeNodeChildren[i];
            node.setTree(tree);
        }
    }
    
    /** @param hub is the list of objects that are valid for this tree node.  
        If an object is not found in this Hub, then it will not be displayed in the tree.
    */
    public void setFilter(Hub hub) {
        hubFilter = hub;
    }
    public Hub getFilter() {
        return hubFilter;
    }

    /** constructor for creating a root node.
        @param hub is hub to use for data.
        @param hubUpdate the hub to keep in sync (the shared hub)
    */
    public OATreeNode(String path, Hub hub, Hub hubUpdate) {
        this.hub = hub;
        this.updateHub = hubUpdate;
        this.propertyPath = path;
    }


    boolean bExpandingAll,bCollaspingAll;  // used by OATree
    public void expandAll() {
        if (tree != null) {
            bExpandingAll = true;
            tree.getHtml();
            bExpandingAll = false;
        }
    }
    public void collapseAll() {
        if (tree != null) {
            bCollaspingAll = true;
            tree.getHtml();
            bCollaspingAll = false;
        }
    }


    /** @param font String within the &lt;font&gt; tag.  Ex: size="1"  */
    public void setFont(String font) {
        this.font = font;
    }
    public String getFont() {
        return this.font;
    }
    public void setBackground(String background) {
        this.colorBackground = background;
    }
    public String getBackground() {
        return this.colorBackground;
    }

    protected void findMethods(Class clazz) {
        if (titleFlag) return;
        if (methodsToHub != null || methodsToProperty != null) return;
        
        int pos,prev;
        String path = propertyPath;
        if (path == null) path = "";
        methodsToHub = null;
        Vector vec = new Vector();
        for (pos=prev=0; pos >= 0; prev=pos+1) {
            pos = path.indexOf('.',prev);
            String name;
            if (pos >= 0) name = "get"+path.substring(prev,pos);
            else {
                name = path.substring(prev);
                if (name.length() == 0) name = "toString";
                else name = "get" + name;
            }
            
            // find method
            Method method = ClassModifier.getMethod(clazz, name);
            if (method == null) throw new RuntimeException("OATreeNode.findMethods() cant find method for \""+name+"\" in PropertyPath \""+propertyPath+"\"");
            vec.addElement(method);

            if ( Hub.class.equals(method.getReturnType())) {
                if (methodsToHub != null) {
                    throw new RuntimeException("OATreeNode.findMethods() propertyPath "+propertyPath+"\" has more then one hub");
                }
                methodsToHub = new Method[vec.size()];
                vec.copyInto(methodsToHub);
                vec.removeAllElements();
                
                // 20090703 was: clazz = Hub.getOAObjectInfo(clazz).getPropertyClass(name.substring(3));
                //new
                ArrayList<OAPropertyInfo> al = OAObjectInfoDelegate.getOAObjectInfo(clazz).getPropertyInfos();
                for (OAPropertyInfo pi : al) {
                    if (pi.getName().equals(name.substring(3))) {
                        clazz = pi.getClassType();
                        break;
                    }
                }
            }
            else clazz = method.getReturnType();
        }
        if (methodsToHub == null) {
            methodsToProperty = new Method[1];
            methodsToProperty[0] = (Method) vec.elementAt(vec.size() - 1);
            vec.removeElementAt(vec.size() - 1);
            methodsToObject = new Method[vec.size()];
            vec.copyInto(methodsToObject);
        }
        else {
            methodsToProperty = new Method[vec.size()];
            vec.copyInto(methodsToProperty);
        }
    }


    public void add(OATreeNode node) {
        int x = treeNodeChildren.length;
        OATreeNode[] temp = new OATreeNode[x+1];
        System.arraycopy(treeNodeChildren, 0, temp, 0, x);
        treeNodeChildren = temp;
        treeNodeChildren[x] = node;
    }

    /** if false, then only the active object will be displayed in tree. */
    public boolean getShowAll() {
        return showAll;
    }
    public void setShowAll(boolean b) {
        showAll = b;
    }
    
    protected void getHtml(StringBuffer sb, OATree tree) {
        getHtml(sb, tree, null, null,null,false);
    }

    
    protected void getHtml(StringBuffer sb, OATree tree, Object object, OATreeData parent, Hub hubToUse, boolean bStartTable) {
        OATreeData data = parent;
        
        // see if any Children need displayed
        OATreeFormNode formNode = null;
        boolean bHasChildren = titleFlag;  // title nodes always need to be expanded
        for (int i=0; !bHasChildren && i < treeNodeChildren.length; i++) {
            OATreeNode node = treeNodeChildren[i];
            if (node instanceof OATreeFormNode) {
                formNode = (OATreeFormNode) node;
                continue;
            }
            if (object != null) node.findMethods(object.getClass());
            if (node.titleFlag) {
                bHasChildren = true;
                continue;
            }
            else if (node.methodsToHub != null) {
                Hub h;
                if (object == null) h = node.hub;
                else h = (Hub) ClassModifier.getPropertyValue(object, node.methodsToHub);
                if (h != null) {
                    h.loadAllData();
                    int x = h.getSize();
                    int j=0;
                    if (!showAll) {
                        j = hub.getPos();
                        if (j < 0) j = 0;
                        x = j+1;
                    }
                    for (; j<x; j++) {
                        OAObject obj = (OAObject) h.elementAt(j);
                        if (obj != null && (node.hubFilter == null || node.hubFilter.getObject(obj) != null) ) {
                            if (tree.bShowNew || !obj.isNew()) {
                                bHasChildren = true;
                            }
                        }
                    }
                }
            }
            else {
                OAObject obj = (OAObject) ClassModifier.getPropertyValue(object, node.methodsToObject);
                if (obj != null && (node.hubFilter == null || node.hubFilter.getObject(obj) != null) ) {
                    if (tree.bShowNew || !obj.isNew()) {
                        bHasChildren = true;
                    }
                }
            }
        }
        

        boolean bIsSelected = tree.isSelected(this, parent, object);
        boolean bIsExpanded = tree.isExpanded(this, parent, object);
        boolean bIsFormExpanded = tree.isFormExpanded(this, parent, object);
        
        
        if (bStartTable) {
            // start a new table   
            sb.append("<tr><td width=1></td>");
            if (!bHasChildren) sb.append("<td width=1></td>");
            sb.append("<td valign=\"top\" colspan=99><table cellpadding=0 cellspacing=0 border=0>");
        }
        
        if (titleFlag) {
            data = new OATreeData(parent, this, null, bIsExpanded, false);
            int pos = tree.addData(data);
            createHtmlRow(sb, tree, propertyPath, pos, (treeNodeChildren.length==0),bIsSelected,bIsExpanded,bHasChildren, true, formNode, false);
        }
        else if (object != null) {
            findMethods(object.getClass());
            String value;
            if (vecColumns != null) {
                value = "";
                int xx = vecColumns.size();
                for (int ii=0; ii<xx; ii++) {
                    OATableColumn tc = (OATableColumn) vecColumns.elementAt(ii);
                    String s = null;
                    try {        
                        s = tc.getValue(object);
                    }
                    catch (Exception e) {
                        handleException(e,"tree column exception");
                        sb.append("OATreeNode.getHtml() for column Exception Occured");
                        return;
                    }
                    if (tc.getPassword()) s = "******";

                    if (s.length() > tc.columns) s = s.substring(0,tc.columns);        
                    if (s.length() == 0) s = "&nbsp;";
                    if (ii != 0) {
                        if (ii == (xx-1)) value += "<td valign=\"top\">";
                        else value += "<td valign=\"top\">";
                    }
                    
                    value += "<nobr>"+s+"</nobr>";
                    if (ii != xx-1) value += "</td>";
                }
            }
            else {
                Object obj = object;
                // 06/24/01 methodsToObject has already been called before this method ...
                // if (methodsToObject != null) obj = ClassModifier.getPropertyValue(object, methodsToObject);
                value = OAConverter.toString(ClassModifier.getPropertyValue(obj, methodsToProperty),getFormat(methodsToProperty[methodsToProperty.length-1]));
            }
            
            boolean b = tree.isSelected(this, parent, object);
            boolean b2 = tree.isExpanded(this, parent, object);
            boolean b3 = tree.isFormExpanded(this, parent, object);
            data = new OATreeData(parent, this, object, b2, b3);
            data.hub = hubToUse;
            int pos = tree.addData(data);
            createHtmlRow(sb, tree, value, pos, (treeNodeChildren.length==0),bIsSelected,bIsExpanded,bHasChildren,false,formNode,bIsFormExpanded);
        }
        else if (hub != null) {
            hub.loadAllData();
            int x = hub.getSize();
            int j=0;
            if (!showAll) {
                j = hub.getPos();
                if (j < 0) j = 0;
                x = j+1;
            }
            createColumnHeading(sb);
            for (; j<x; j++) {
                OAObject obj = (OAObject) hub.elementAt(j);
                if (obj != null && (hubFilter == null || hubFilter.getObject(obj) != null) ) {
                    if (tree.bShowNew || !obj.isNew()) {
                        getHtml(sb,tree,obj,parent,hub,false);
                    }
                }
            }
            return;
        }

        if (!bIsExpanded && !bIsFormExpanded) return;
        
        if (titleFlag && parent != null) object = parent.object;  // titleNodes dont have objects, go to its parent
        
        // Children
        boolean bEndTable = false;
        for (int i=0; (titleFlag || object != null) && i < treeNodeChildren.length; i++) {
            OATreeNode node = treeNodeChildren[i];
            if (node instanceof OATreeFormNode) {
                if (!bIsFormExpanded) continue;
                formNode = (OATreeFormNode) node;


sb.append("<tr><td width=1></td>");
sb.append("<td width=1></td>");
sb.append("<td valign=\"top\" colspan=99><table cellpadding=0 cellspacing=0 border=5>");

sb.append("<tr><td>");

sb.append("Hello ..... "+node.propertyPath);

sb.append("</td></tr></table>");

sb.append("</td></tr>");

continue;//qqqqqqqqqqqqqqqqqqvvvvvvvvvvv                
            }
            if (!bIsExpanded) continue;
            if (object != null) node.findMethods(object.getClass());
            
            if (node.titleFlag) {
                node.getHtml(sb,tree,null,data, hubToUse,true);
                bEndTable = true;
            }
            else if (node.methodsToHub != null || node.hub != null) {
                Hub h;
                if (object == null) h = node.hub;
                else h = (Hub) ClassModifier.getPropertyValue(object, node.methodsToHub);
                h.loadAllData();
                int x = h.getSize();
                int j=0;
                if (!showAll) {
                    j = hub.getPos();
                    if (j < 0) j = 0;
                    x = j+1;
                }
                node.createColumnHeading(sb);
                boolean b = (node.vecColumns == null);
                for (; j<x; j++) {
                    OAObject obj = (OAObject) h.elementAt(j);
                    if (obj != null && (node.hubFilter == null || node.hubFilter.getObject(obj) != null) ) {
                        if (tree.bShowNew || !obj.isNew()) {
                            node.getHtml(sb,tree,obj,data,h,b);
                            b = false;
                        }
                    }
                    bEndTable = true;
                }
            }
            else {
                OAObject obj = (OAObject) ClassModifier.getPropertyValue(object, node.methodsToObject);
                if (obj != null && (node.hubFilter == null || node.hubFilter.getObject(obj) != null) ) {
                    if (tree.bShowNew || !obj.isNew()) {
                        node.getHtml(sb,tree,obj, data, null,true);
                        bEndTable = true;
                    }
                }
            }
            if (bEndTable) sb.append("</table></td></tr>");
        }
    }

    protected void createColumnHeading(StringBuffer sb) {
        if (vecColumns == null) return;

        // start a new table   
        sb.append("<td valign=\"top\" colspan=99><table cellpadding=0 cellspacing=0 border=0>");

        String s, s2;
        sb.append(System.getProperty("line.separator"));
        sb.append("<TR><TD width=1></TD><TD width=1></TD>");

        int x = vecColumns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) vecColumns.elementAt(i);
            s = tc.heading;
            if (s.length() > tc.columns) s = s.substring(0,tc.columns);        
            int xx = s.length();
            for ( ; xx < tc.columns; xx++) s += "&nbsp;";
            sb.append("<td valign=\"top\"");
            sb.append("><nobr>"+s+"</nobr></td>");
            
        }
        sb.append("<td width=\"100%\">&nbsp</td>");
        sb.append("</tr>");
    }
    
    
    
    protected void createHtmlRow(StringBuffer sb, OATree tree, String value, int pos, boolean bLeaf, boolean bSelected, boolean bExpanded, boolean bHasChildren, boolean bTitle, OATreeFormNode formNode, boolean bFormExpanded) {
        String s, s2;
        sb.append(System.getProperty("line.separator"));

        sb.append("<TR><TD valign=\"top\" width=1>");
        

        if (!bLeaf) {
            if (!bHasChildren) {
            }
            else {
                if (bExpanded) {
                    s = "oacommand_"+tree.name.length()+"_"+tree.name+"c"+pos;
                    s2 = tree.getCollapseImageName();
                }
                else {
                    s = "oacommand_"+tree.name.length()+"_"+tree.name+"e"+pos;
                    s2 = tree.getExpandImageName();
                }
                sb.append("<input type=\"image\" name=\""+s+"\" src=\""+s2+"\" border=\"0\">");
            }
            s = "oacommand_"+tree.name.length()+"_"+tree.name+"s"+pos;
            s2 = tree.getNodeClosedImageName();
            if (bSelected) s2 = tree.getNodeOpenedImageName();
        }
        else {
            s = "oacommand_"+tree.name.length()+"_"+tree.name+"s"+pos;
            s2 = tree.getLeafImageName();
        }
        if (formNode != null) {
            if (bFormExpanded) {
                s = "oacommand_"+tree.name.length()+"_"+tree.name+"C"+pos;
                s2 = tree.getExpandFormImageName();
            }
            else {
                s = "oacommand_"+tree.name.length()+"_"+tree.name+"E"+pos;
                s2 = tree.getCollapseFormImageName();
            }
        }
        sb.append("</TD><TD valign=\"top\" width=1><input type=\"image\" name=\""+s+"\" src=\""+s2+"\" border=\"0\">&nbsp;");
        sb.append("</TD><TD valign=\"top\"");

        if (vecColumns != null) {
            if (bSelected) s = tree.selectColor;
            else s = colorBackground;
            if (s != null) sb.append(" bgcolor=\""+s+"\"");
        }
        else sb.append(" width=\"100%\"");

        sb.append(">");
        
        if (value == null) value = "";
        
        for (int i=value.length(); i<8; i++) {
            value += "&nbsp;";
        }

        s = (font == null) ? tree.font : font;
        if (s != null) value = "<FONT "+s+">" + value + "</FONT>";

        if (bSelected) s = tree.selectColor;
        else s = colorBackground;
        if (s != null) {
            if (vecColumns == null) {
                s = "<SPAN STYLE=\"{ background-color: "+s+"}\">";
                s += value;
                s += "</SPAN>";
                value = s;
            }
            else {
                s = " bgcolor=\""+s+"\" ";
                value = Util.convert(value, "<td", "<td"+s+"");
            }
        }
        
        s = "oacommand_"+tree.name.length()+"_"+tree.name+"s"+pos+"=1";
        
        if (!bTitle) s = "<A href=\"javascript:document.forms[0].action = 'oaform.jsp?"+s+"=1'; setOA(); document.forms[0].submit();\">";
        else s = "";
        
        if (tree.bWrap || vecColumns != null) s += value;
        else s += "<nobr>"+value+"</nobr>";
        if (!bTitle) s += "</a>";
        
        sb.append(s);

        sb.append("</TD>");
        sb.append("</TR>");
    }
    
}

