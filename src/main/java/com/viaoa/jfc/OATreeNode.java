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
package com.viaoa.jfc;

import java.util.Vector;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

import com.viaoa.hub.*;
import com.viaoa.jfc.image.*;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.jfc.control.*;
import com.viaoa.jfc.tree.*;
import com.viaoa.jfc.table.*;

/** TreeNode for OATree.
    Node in tree, with leading hub or a property path to data from parent node.
    A node can be added to itself, to make it recursive. 
    <pre>
    public Component getTreeCellRendererComponent(Component comp,JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {
        // NOTE: row is the row in tree, not the row within the node
        if (row < 0) return; // only checking
        JLabel lbl = (JLabel)comp;
        lbl.setOpaque(true);

            
        OATreeNodeData d = (OATreeNodeData) value;
        Yard yard = (Yard) d.getObject();
        -- or -- see note above
        Yard yard = (Yard) hubYard.elementAt(row);
            
        Color color = null;
        if (yard != null) color = yard.getColor();
        if (color == null) color = Color.white;
        lbl.setBackground(color);
        return lbl;
    }
</pre>
*/
public class OATreeNode implements Cloneable {

    public int DEBUG;
    /*
      20110802 - added innerclass "Def" so that the data can be reused for recursive nodes, ex: when a node is added to itself
     */
    String fullPath; // full path of node
    Hub hub; // could be set by OATree
    boolean titleFlag;
    public Method[] methodsToHub; // methods to find Hub
    public boolean bRecursive;
    
    class Def {  // so that nodes can be reused/recursive
        boolean showAll = true;  // if false then only the activeObject is shown, if true then all objects in hub are used
        String propertyPath;  // property path after the path to a hub   ex: emps.dept.name => dept.name
        boolean bAllowDrag = true;  // allow drag and drop
        boolean bAllowDrop = true;  // allow drag and drop
        // only one of the next two can be set
        Method methodToObject; // method to get to object object.  ex: dept.name => dept
        public Method[] methodsToProperty;  // methods to get property value from object => name
        Hub updateHub; // hub to be notfied by OATree when selection is changed
        Hub hubSelected; // hub that has selected/checked tree node items
        // flag to create a hubMerger (if 2 or more hubs in path) - need to also garbage collect it when not used
        //    will have to put object in separate Hub to create merger from
        boolean bRequiresHubMerger; 
        OATreeNode[] treeNodeChildren = new OATreeNode[0];
        OATree tree; // set by OATree
        OATableComponent editor;
        Component editorComponent;
        Font font;
        String fontProperty;
        Color colorBackground;
        String backgroundColorProperty;
        Color colorForeground;
        String foregroundColorProperty;
        JPopupMenu popupMenu;
        Icon icon;
        String imagePath;
        String imageProperty;
        String iconColorProperty;
        String toolTipText;
        String toolTipTextProperty;
        Method[] methodsToFont;
        Method[] methodsToImage;
        Method[] methodsToBackgroundColor;
        Method[] methodsToForegroundColor;
        Method[] methodsToToolTipText;
        Method[] methodsToIconColor;
        String[] dependentProperties;
        String suffix;
        Object updateObject, updateValue;
        Method updateMethod;
        boolean bUseIcon=true;
        int width;  // number of chars
        // Image sizing
        protected int maxImageHeight, maxImageWidth;
    }
    
    Def def = new Def();
    
    private OATreeNode() {
        // used internally by add(..) when adding itself (recursive)
    }
    
    public OATreeNode(String path) {
        this(path,null,null,null);
    }

    /** @param hub is either the Hub or UpdateHub, depending if this is a top level node.*/
    public OATreeNode(String path, Hub hub) {
        this(path,hub,null,null);
    }
    public OATreeNode(String path, Hub hub, Hub updateHub) {
        this(path,hub,updateHub,null);
    }
    public OATreeNode(String path, OATableComponent editor) {
        this(path, editor.getHub(), null, editor);
    }
    public OATreeNode(OATableComponent editor) {
        this(editor.getPropertyPath(), editor.getHub(), null, editor);
    }

    
// replaced with HubRoot    
    // 20120302
    /**
     * Need to be able to have the root Hub for recursive hub not change. 
     *
    public void useRecursiveRootHub() {
        if (hub == null) return;
        Hub hubRoot = hub.getRootHub();
        if (hubRoot == null) return;

        Class clazz = hub.getObjectClass();
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        if (li == null) return;
        
        Hub hubMaster = hubRoot.getMasterHub();
        if (hubMaster == null) return;
        
        this.hub = new Hub(this.hub.getObjectClass());
        
        // create a copy of the original hub
        //   if you use a sharedHub, then setting the AO to an object in a child hub will set the shared hub to that new child hub
        
        final String prop = HubDetailDelegate.getPropertyFromMasterToDetail(hubRoot);
        OAObject obj = (OAObject) hubMaster.getAO();
        if (obj != null) {
            Hub h = (Hub) obj.getProperty(prop);
            hubCopy = new HubCopy(h, this.hub, false);
        }
        
        hubMaster.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                OATreeNode.this.hub.clear();
                if (hubCopy != null) hubCopy.close();
                hubCopy = null;

                Hub hubRoot = OATreeNode.this.hub.getRootHub();
                if (hubRoot == null) return;
                
                Hub hubMaster = hubRoot.getMasterHub();
                if (hubMaster != null) return;
                
                OAObject obj = (OAObject) hubMaster.getAO();
                if (obj == null) return;
                
                Hub h = (Hub) obj.getProperty(prop);
                hubCopy = new HubCopy(h, OATreeNode.this.hub, false);
            }
        });
    }
    private HubCopy hubCopy ;
    */
    // used by Hub2TreeNode when checking propertyChanges
    public void setDependentProperties(String ... props) {
        def.dependentProperties = props;
    }
    public String[] getDependentProperties() {
        return def.dependentProperties;
    }
    
    
    private static AtomicInteger aiXcnt = new AtomicInteger(); 
    
    // called by OATree when updateUI is called.
    protected void updateUICalled() {
        _updateUICalled(aiXcnt.incrementAndGet());
    }    
    private void _updateUICalled(int x) {
        if (this.xcnt >= x) return;
        this.xcnt = x;
        if (checkIcon != null) checkIcon.updateUICalled();
        for (int i=0; i<def.treeNodeChildren.length; i++) {
            def.treeNodeChildren[i]._updateUICalled(x);
        }
        // 20091226
        if (def.editor instanceof JComponent) {
            ((JComponent)def.editor).updateUI();
        }
        if (def.editorComponent instanceof JComponent) {
            ((JComponent)def.editorComponent).updateUI();
        }
    }    
    
    public Method[] getMethodsToHub() {
        return methodsToHub;
    }
    public Method[] getMethodsToProperty() {
        return def.methodsToProperty;
    }
    public Method getMethodToObject() {
        return def.methodToObject;
    }
    public OATableComponent getTableEditor() {
        return def.editor;
    }
    public Component getEditorComponent() {
        return def.editorComponent;
    }
    public String getPropertyPath() {
        return def.propertyPath;
    }
    public OATree getTree() {
        return def.tree;
    }

    /**
     * Hub that retains list of nodes that have been checked.
     * @param hub
     */
    public void setSelectedHub(Hub hub) {
        this.def.hubSelected = hub;
        OATreeNode tn = this;
    }
    public Hub getSelectedHub() {
        return this.def.hubSelected;
    }
    
    protected boolean needsCheckBox() {
        return _needsCheckBox(aiXcnt.incrementAndGet());
        
    }
    private boolean _needsCheckBox(int x) {
        if (def.hubSelected != null) return true;
        if (xcnt >= x) return false;
        xcnt = x;
        for (OATreeNode child : def.treeNodeChildren) {
            if (child._needsCheckBox(x)) return true;
        }       
        return false;
    }

    
    // 2008/04/24 checkbox has been clicked, called by OATree.processMouseEvent
    protected void checkBoxClicked(OATreeNodeData tnd) {
        if (tnd == null) return;
        if (this.def.hubSelected == null) {
            parentCheckBoxClicked(tnd);
        }
        else {
            if (this.def.hubSelected.contains(tnd.object)) this.def.hubSelected.remove(tnd.object);
            else this.def.hubSelected.add(tnd.object);
        }
        def.tree.repaint();
    }

    protected void parentCheckBoxClicked(OATreeNodeData tnd) {
        boolean b = _isAnyChecked(tnd);
        _setChildrenCheck(tnd, !b);
    }
    
    private void _setChildrenCheck(OATreeNodeData tnd, boolean bChecked) {
        tnd.loadChildren();
        for (OATreeNodeData child : tnd.listChildren) {
            if (child.node.def.hubSelected != null) {
                if (bChecked) {
                    if (!child.node.def.hubSelected.contains(child.object)) child.node.def.hubSelected.add(child.object); 
                }
                else if (child.node.def.hubSelected.contains(child.object)) child.node.def.hubSelected.remove(child.object);
            }
            _setChildrenCheck(child, bChecked); 
        }
    }
    
    private boolean _isAnyChecked(OATreeNodeData tnd) {
        if (tnd.listChildren == null) return false;
        for (OATreeNodeData child : tnd.listChildren) {
            if (child.node.def.hubSelected != null) {
                if (child.node.def.hubSelected.contains(child.object)) return true;
            }
            else if (!child.node.needsCheckBox()) continue;
            if (_isAnyChecked(child)) return true;
        }
        return false;
    }
    
    
    private static int CHECK_NONE = 0;
    private static int CHECK_EMPTY = 1;
    private static int CHECK_HALF = 2;
    private static int CHECK_FULL = 3;
    
    protected int getCheckType(OATreeNodeData tnd) {
        if (!needsCheckBox()) return CHECK_NONE; 
        if (tnd.node.def.hubSelected != null) {
            if (tnd.node.def.hubSelected.contains(tnd.object)) return CHECK_FULL;
            else return CHECK_EMPTY;
        }
        
        int x = _getParentCheckType(tnd, CHECK_NONE);
        if (x == CHECK_NONE) x = CHECK_EMPTY;
        return x;
    }
    
    private int _getParentCheckType(OATreeNodeData tnd, int result) {
        if (tnd.listChildren == null) {
            if (!needsCheckBox()) return CHECK_NONE;
            tnd.loadChildren();
        }
        for (OATreeNodeData child : tnd.listChildren) {
            if (child.node.def.hubSelected != null) {
                if (child.node.def.hubSelected.contains(child.object)) {
                    if (result == CHECK_NONE) result = CHECK_FULL;
                    else if (result != CHECK_FULL) result = CHECK_HALF;
                }
                else {
                    if (result == CHECK_NONE) result = CHECK_EMPTY;
                    else if (result != CHECK_EMPTY) result = CHECK_HALF;
                }
            }
            else if (!child.node.needsCheckBox()) continue;
            result = _getParentCheckType(child, result);
        }
        return result;
    } 
    
    
    
    
    /**
     * This can be overwritten to provide a HubFilter for the Hub that will be used for each instanceof of this OATreeNode.
     * <code><pre>
     * ex:
      
            @Override
            public HubFilter getHubFilter(OATreeNode tn) {
                Hub h = new Hub(Pet.class);
                HubFilter hf = new HubFilter(tn.getHub(), h) {
                    @Override
                    public boolean isUsed(Object object) {
                        Pet p = (Pet) object;
                        return (p != null && p.getInactiveDate() == null);
                    }
                };
                return hf;
            }
     
      
     * </pre></code>
     * @param hubData the Hub that is used for the node.
     * @return HubFilter 
     */
    public HubFilter getHubFilter(OATreeNodeData parentTnd, Hub hub) {
        return getHubFilter(hub);
    }
    public HubFilter getHubFilter(Hub hubMaster) {
        return null;
    }
    
    
    public void setSuffix(String s) {
        def.suffix = s;
    }
    public String getSuffix() {
        return def.suffix;
    }
    
    public int getMaxImageHeight() {
        return def.maxImageHeight;
    }
    public void setMaxImageHeight(int maxImageHeight) {
        this.def.maxImageHeight = maxImageHeight;
    }

    
    public int getMaxImageWidth() {
        return def.maxImageWidth;
    }
    public void setMaxImageWidth(int maxImageWidth) {
        this.def.maxImageWidth = maxImageWidth;
    }
    
    
    /** 2007/01/05
     * number of characters to use.
     */
    public void setWidth(int chars) {
        this.def.width = chars;
    }
    public int getWidth() {
        return this.def.width;
    }
    
    public Hub getHub() {
        return hub;
    }
    public Hub getUpdateHub() {
        return def.updateHub;
    }
    
    /** @param hub is either Hub to get data from or updateHub. */
    protected OATreeNode(String path, Hub hub, Hub updateHub, OATableComponent editor) {
        this.fullPath = path;
        this.def.propertyPath = path;
        this.hub = hub;
// 20101227 commented out next line so that tree node would be in sync when AO is changed by another component        
//      if (hub == updateHub) updateHub = null; 
        
        setUpdateHub(updateHub);
        if (editor != null) setEditor(editor);
    }

    /** @param updateHub is the Hub to call setShared on. */
    public void setUpdateHub(Hub updateHub) {
        if (updateHubListener != null && this.def.updateHub != null) this.def.updateHub.removeHubListener(updateHubListener);
        this.def.updateHub = updateHub;
        setupUpdateHub();
    }

    /**
       Object to update whenever node is selected.
    */
    public void setUpdateObject(Object object, String property, Object newValue) {
        if (object != null && property != null) {
            def.updateMethod = OAReflect.getMethod(object.getClass(), property);
        }
        else def.updateMethod = null;
    }


    
    /** if there is an updateHub, then the sharedHub that it is using needs to have its
        activeObject in sync with the updateHub.  Hub2TreeNode will then listen for changes
        and update the tree.
    */
    private HubListener updateHubListener;

    // 20110224 redid, see old version below
    protected void setupUpdateHub() {
        if (def.updateHub == null) return;
        updateHubListener = new HubListenerAdapter() {
            public @Override void afterChangeActiveObject(HubEvent e) {
                if (def.tree == null) {
                    return; // error
                }
                if (def.tree.bIsSelectingNode) {
                    return;
                }                    
                    
                if (hub != null && !HubShareDelegate.isUsingSameSharedHub(def.updateHub, hub)) {
                    return;
                }
                
                Object obj = e.getObject();
                Hub h;
                if (OATreeNode.this.hub != null) {
                    h = OATreeNode.this.hub;
                }
                else {
                    // this will "hit" the same hub that the OATreeNodeData is listening to - since it is listening to the "real" Hub.
                    h = HubShareDelegate.getMainSharedHub(def.updateHub);
                }
                if (obj != h.getAO()) {
                    HubAODelegate.setActiveObject(h, obj);
                }
            }
        };
        def.updateHub.addHubListener(updateHubListener);
    }
    
    
    //was: before 20110224
    protected void OLD_setupUpdateHub() {
        if (def.updateHub == null) return;
        updateHubListener = new HubListenerAdapter() {
            boolean bWasOnAnotherHub;  // used for recursive, to know when returning to another of the nodes
            public @Override void afterChangeActiveObject(HubEvent e) {
                if (def.tree == null) {
                    return; // error
                }
                if (def.tree.bIsSelectingNode) {
                    // OATree.valueChanged() in process
                    if (e.getHub().getAO() != null) return;
                }

                // 20110106
                boolean bHoldWasOnAnotherHub = bWasOnAnotherHub;
                if (hub != null && !HubShareDelegate.isUsingSameSharedHub(def.updateHub, hub)) {
                    bWasOnAnotherHub = true;
                    return;
                }
                else {
                    if (e.getObject() != null) bWasOnAnotherHub = false;
                }
                
                if (hub != null && HubShareDelegate.isUsingSameSharedAO(def.updateHub, hub)) {
                    if (!bHoldWasOnAnotherHub) return;
                }
                
                Object obj = e.getObject();
                // 20101228 set the AO on the correct treeNode hub object
                Hub h;
                if (OATreeNode.this.hub != null) {
                    h = OATreeNode.this.hub;
                }
                else {
                    // this next statement will "hit" the same hub that the OATreeNodeData is listening to - since it is listening to the "real" Hub.
                    h = HubShareDelegate.getMainSharedHub(def.updateHub);
                }
                if (obj != h.getAO()) {
                    HubAODelegate.setActiveObject(h, obj);
                }
                else {
                    if (bHoldWasOnAnotherHub) {
                        // 20110106 need to notify Hub2TreeNode to reset selected treeNode 
                        //    since this is a recursive node that is selected again, and
                        //      the AO has not been changed since it is the node that was last selected.
                        //  otherwise, the node will not be selected - since Hub2TreeNode needs to get a changeAO event
                        HubAODelegate.setActiveObject(h, obj, false, false, true);
                        bHoldWasOnAnotherHub = false;
                    }
                }
                
            }
        };
        def.updateHub.addHubListener(updateHubListener);
    }

    
    
    public void setFont(Font font) {
        this.def.font = font;
    }
    public Font getFont() {
        return this.def.font;
    }
    public void setBackground(Color background) {
        this.def.colorBackground = background;
    }
    public Color getBackground() {
        return this.def.colorBackground;
    }
    public void setForeground(Color background) {
        this.def.colorForeground = background;
    }
    public Color getForeground() {
        return this.def.colorForeground;
    }

    /**
     * Flag to use the folder icon. Default is true
     * @param b
     */
    public void setUseIcon(boolean b) {
        this.def.bUseIcon = b;
    }
    public boolean getUseIcon() {
        return def.bUseIcon;
    }
    
    
    public void setEditor(OATableComponent component) {
        def.editor = component;
        //if (editor != null) editor.removeComponentListener();
        if (def.editor == null || !(def.editor instanceof Component)) return;
        def.editorComponent = (Component)def.editor;

        def.editorComponent.addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent e) {
            }
            public void focusLost(FocusEvent e) {
                ((OATreeCellEditor)def.tree.getCellEditor()).fireEditingStopped();
                /* this will force it to be moved when it is used again
                   otherwise, the componentMoved() event might not be called
                */
                def.editorComponent.setLocation(0,0); 
            }
        });


        def.editorComponent.addComponentListener( new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                // if (lblRenderer == null) return;
                Dimension d1 = def.tree.getSize();
                Container c = def.tree.getParent();
                Point pt;
                if (c instanceof JViewport) {
                    d1 = c.getSize();
                    pt = ((JViewport) c).getViewPosition();
                }
                else pt = new Point(0,0);
                
                /* this will position to the right of the icon, not using since it cause to much flicker
                int iconWidth = 0;
                Icon icon = lblRenderer.getIcon();
                if (icon != null) iconWidth = icon.getIconWidth();
                lblRenderer = null;
                Rectangle rec = tf.getBounds();
                tf.setBounds(rec.x + iconWidth, rec.y, d1.width - tf.getLocation().x - iconWidth,tf.getSize().height);
                !! add this next line as a class property
                JLabel lblRenderer; 
                !! add this next line to getTreeCellEditorComponent()
                lblRenderer = (JLabel) tree.getCellRenderer().getTreeCellRendererComponent(tree,value,isSelected,expanded,leaf,row,true);
                */
                def.editorComponent.setSize( d1.width - def.editorComponent.getLocation().x + pt.x , def.editorComponent.getSize().height);
            }
        });
    }


    public void setIcon(Icon icon) {
        this.def.icon = icon;
    }
    public Icon getIcon() {
        return def.icon;
    }

    public void setImageProperty(String s) {
        def.imageProperty = s;
        def.methodsToImage = null;
    }
    public String getImageProperty() {
        return def.imageProperty;
    }

/*qqqqqqqq to do    
    private Dimension dimMaxImageSize;
    public void setImageMaxSize(Dimension d) {
        this.dimMaxImageSize = d;
    }
*/    
    /**
     * Path to find the image.  Use "/", and include a leading "/" if it is from the beginning
     * of the classpath, or root directory.  Will use the classloader to also search jar files.
     */
    public void setImagePath(String s) {
        if (s != null) {
            s += "/";
            s = OAString.convert(s, "\\", "/");
            s = OAString.convert(s, "//", "/");
        }
        def.imagePath = s;
    }
    public String getImagePath() {
        return def.imagePath;
    }

    public void setBackgroundColorProperty(String s) {
        def.backgroundColorProperty = s;
        def.methodsToBackgroundColor = null;
    }
    public String getBackgroundColorProperty() {
        return def.backgroundColorProperty;
    }

    public void setIconColorProperty(String s) {
        def.iconColorProperty = s;
        def.methodsToIconColor = null;
    }
    public String getIconColorProperty() {
        return def.iconColorProperty;
    }
    
    
    public void setFontProperty(String s) {
        def.fontProperty = s;
        def.methodsToFont = null;
    }
    public String getFontProperty() {
        return def.fontProperty;
    }
    
    public void setForegroundColorProperty(String s) {
        def.foregroundColorProperty = s;
        def.methodsToForegroundColor = null;
    }
    public String getForegroundColorProperty() {
        return def.foregroundColorProperty;
    }
    
    public void setToolTipTextProperty(String s) {
        def.toolTipTextProperty = s;
        def.methodsToToolTipText = null;
    }
    public String getToolTipTextProperty() {
        return def.toolTipTextProperty;
    }

    public void setToolTipText(String s) {
        def.toolTipText = s;
    }
    public String getToolTipText() {
        return def.toolTipText;
    }
    
    private int xcnt;  // used when "visiting" nodes, so that recursive nodes dont cause a loop
    public void setTree(OATree tree) {
        setTree2(tree);
        // only one top node can have "hub" set
        if (tree != null) tree.root.setupTopHub(aiXcnt.incrementAndGet(),false);
    }
    private void setTree2(OATree tree) {
        if (this.def.tree == null) {
            this.def.tree = tree;
            // set tree in children
            for (int i=0; i<def.treeNodeChildren.length; i++) {
                if (def.treeNodeChildren[i].def.tree == null) {
                    def.treeNodeChildren[i].setTree2(tree);
                }
            }
        }
    }

    private void setupTopHub(int cnt, boolean bHubSet) {
        if (this.xcnt != cnt) {
            this.xcnt = cnt;
            if (this instanceof OATreeTitleNode) {
// 20120226                 
                //was: bHubSet = false;
            }
            if (!bHubSet) {
                if (hub != null) bHubSet = true;
            }
            else {
                if (def.updateHub == null) setUpdateHub(hub);
                hub = null;
            }
            // set tree in children
            for (int i=0; i<def.treeNodeChildren.length; i++) {
                if (def.treeNodeChildren[i].xcnt != cnt) {
                    def.treeNodeChildren[i].setupTopHub(cnt, bHubSet);
                }
            }
        }
    }

    /**
     * Used to create a recursive node, with a different propertyPath
     * @param node
     * @param propertyPath new propertyPath to use
     */
    public void add(OATreeNode node, String propertyPath) {
        _add(node, propertyPath);
    }
    public void add(OATreeNode node) {
        _add(node, null);
    }
    
    private void _add(final OATreeNode originalNode, String propertyPath) {
        
        OATreeNode node = originalNode;
        if (node == this) {
            if (this.hub != null || !OAString.isEmpty(propertyPath)) {
                // need to create another node, that uses the link property to find hub
                node = new OATreeNode() {
                    @Override
                    public Icon getIcon(Object obj) {
                        return originalNode.getIcon(obj);
                    }
                };
                node.def = this.def;
                node.bRecursive = true;
                if (!OAString.isEmpty(propertyPath)) node.fullPath = propertyPath;
            }
        }
        else if (!OAString.isEmpty(propertyPath)) {
            OATreeNode origNode = node;
            node = new OATreeNode() {
                @Override
                public Icon getIcon(Object obj) {
                    return originalNode.getIcon(obj);
                }
            };
            node.def = origNode.def;
            node.bRecursive = true;
            node.fullPath = propertyPath;
        }

        int x = def.treeNodeChildren.length;
        OATreeNode[] temp = new OATreeNode[x+1];
        System.arraycopy(def.treeNodeChildren, 0, temp, 0, x);
        def.treeNodeChildren = temp;
        def.treeNodeChildren[x] = node;
        if (def.tree != null) {
            node.setTree(def.tree);
            ((OATreeModel)node.def.tree.getModel()).fireTreeStructureChanged(this);
        }
    }
    
    public OATreeNode[] getChildrenTreeNodes() {
        return def.treeNodeChildren;
    }
    
    public boolean getShowAll() {
        return def.showAll;
    }
    public void setShowAll(boolean b) {
        def.showAll = b;
    }
    
/*    
    public OATreeNode getParent() {
        return treeNodeParent;
    }
*/
    public Hub getHubForNodeData(OATreeNodeData tnd) {
        if (titleFlag) return null;
        if (hub != null) return hub;
        // 20101208
        if (tnd.parent == null) return null;
        OATreeNode node = tnd.parent.node; 
        //was:  OATreeNode node = treeNodeParent;
        if (node == null) return null;

        // get Object to get Hub from
        if (tnd.parent == null) return null;
        Object object = tnd.parent.object;

        // 20110802 recursive nodes
        if (methodsToHub == null && bRecursive) {
            Class clazz = object.getClass();
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
            
            // find method
            Method method = OAReflect.getMethod(clazz, li.getName());
            if (method == null) {
                throw new RuntimeException("OATreeNode.getHubForNodeData() cant find recursive method for "+clazz.getName()+"."+li.getName());
            }

            fullPath = li.getName() + "." + def.propertyPath; 
            methodsToHub = new Method[] {method};
        }
        
        if (methodsToHub == null && def.methodsToProperty == null) {
            findMethods(object.getClass(), true);
        }
        Hub h = null;
        if (methodsToHub != null) {
            h = (Hub) OAReflect.getPropertyValue(object, methodsToHub);
        }
        return h;
    }
    
 

    public void findMethods(Class clazz, boolean allowHub) {
        if (titleFlag) return;

        
        // 20110802 recursive nodes
        if (methodsToHub == null && bRecursive) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
            
            // find method
            Method method = OAReflect.getMethod(clazz, "get"+li.getName());
            if (method == null) {
                throw new RuntimeException("OATreeNode.getHubForNodeData() cant find recursive method for "+clazz.getName()+"."+li.getName());
            }

            fullPath = li.getName() + "." + def.propertyPath; 
            methodsToHub = new Method[] {method};
        }
        
        
        
        int pos,prev;
        String path = allowHub ? fullPath : def.propertyPath;
        if (path == null) path = "";

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
            Method method = OAReflect.getMethod(clazz, name);
            if (method == null) throw new RuntimeException("OATreeNode.getMethods() cant find method for \""+name+"\" in PropertyPath \""+fullPath+"\"");
            vec.addElement(method);
            clazz = method.getReturnType();
            if ( Hub.class.isAssignableFrom(clazz)) {
                if (!allowHub) {
                    throw new RuntimeException("OATreeNode.getMethods(): path: "+path+" has more then one hub in it");   
                }
                break;
            }
        }
        if ( Hub.class.isAssignableFrom(clazz)) {
            def.propertyPath = path.substring(pos+1);
            methodsToHub = new Method[vec.size()];
            vec.copyInto(methodsToHub);
        }
        else {
            def.propertyPath = path;
            if (methodsToHub == null && vec.size() > 1 && hub == null) {
                def.methodToObject = (Method) vec.elementAt(0);
                vec.removeElementAt(0);
            }
            def.methodsToProperty = new Method[vec.size()];
            vec.copyInto(def.methodsToProperty);
        }
    }

    
    
    public boolean getTitleFlag() {
        return titleFlag;
    }
    
    public String toString(OATreeNodeData tnd) {
        if (titleFlag) return fullPath;

        String s;
        if (tnd.object == null) {
            s = "";
        }
        else {
            if (def.methodsToProperty == null) findMethods(tnd.object.getClass(), false);
            Object obj = OAReflect.getPropertyValue(tnd.object, def.methodsToProperty);
            Class c = (obj == null) ? null : obj.getClass();
            s = OAConverter.toString(obj);
            if (s == null) s = "";
        }
        if (def.suffix != null) s += def.suffix;
        if (def.width > 0) {
            int x = s.length();
            if (x < def.width) {
                for (int i=x; i<def.width; i++) s += " ";
            }
            else if (x > def.width) {
                s = s.substring(0, (def.width > 3) ? def.width-2 : def.width) + "...";
            }
        }
        int x = s.length();
        if (x  < 3) for (int i=x; i<3; i++) s += " ";
        return s;
    }



    private Vector vecListener;
    public void addListener(OATreeListener l) {
        if (vecListener == null) vecListener = new Vector(2,2);
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    public void removeListener(OATreeListener l) {
        vecListener.removeElement(l);
    }
    OATreeListener[] getListeners() {
        if (vecListener == null) return null;
        OATreeListener[] l = new OATreeListener[vecListener.size()];
        vecListener.copyInto(l);
        return l;
    }


    public void setAllowDrop(boolean b) {
        def.bAllowDrop = b;
    }
    public boolean getAllowDrop() {
        return def.bAllowDrop;
    }
    public void setAllowDrag(boolean b) {
        def.bAllowDrag = b;
    }
    public boolean getAllowDrag() {
        return def.bAllowDrag;
    }
    public void setAllowDnd(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
    }
    public void setAllowDnD(boolean b) {
        setAllowDnd(b);
    }

    /** method to override to allow an object of a different class to be dropped.
        By default, the classes have to be assignable.
    */
    public boolean getAllowDrop(Hub hubDrag, Object objectDrag, Hub hubDrop) {
        if (objectDrag == null) return false;
        if (hubDrop == null) return false;
        if ( hubDrop.getObjectClass().isAssignableFrom(objectDrag.getClass()) ) return true;
        return false;
    }

    /** Method to override and create a different object then the default that is moved.
        @returns object to insert/add or null to cancel */
    public Object getDropObject(Hub hubDrag, Object objectDrag, Point pt, Hub hubDrop) {
        return objectDrag;
    }

    /** method to override to supply "rigth-click" functionality */
    public void popup(Point pt) {
        if (def.popupMenu != null) {
            def.popupMenu.show(this.def.tree, pt.x, pt.y);
        }
    }
    public void setPopupMenu(JPopupMenu popupMenu) {
        this.def.popupMenu = popupMenu;
    }
    public JPopupMenu getPopupMenu() {
        return this.def.popupMenu;
    }

    /** method to override to know when node is selected. Called by OATree when node is selected. */
    public void nodeSelected(OATreeNodeData data) {
        if (def.updateMethod != null) {
            try {
                OAReflect.setPropertyValue(def.updateObject, def.updateMethod, def.updateValue);
            }
            catch (Exception ex) {
                throw new RuntimeException("OATreeNode update method exception invoking method="+ def.updateMethod.getName()+" "+ex);
            }
        }
    }

    /** method to override to know when node is double clicked. */
    public void onDoubleClick(Object obj, MouseEvent e) {
    }
   

    /** 
     * method to override to know when node is selected. Called by OATree when node is selected.
     *  
     */
    public void objectSelected(Object obj) {
    }
    /** This is called in the SwingWorker thread, to allow for background processing like
     * getting data from server.
     */
    public void beforeObjectSelected(Object obj) {
    }

    // 20130728 can be overwritten per object
    protected Icon getIcon(Object obj) {
        return this.def.icon;
    }
    
    
    private ColoredLineUnderIcon myColorIcon;
    protected TNCheckIcon checkIcon;
    private MultiIcon myMultiIcon, myMultiIcon2;
    public Component getTreeCellRendererComponent(Component comp, JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {
        if (!def.bUseIcon) ((JLabel)comp).setIcon(null);

        String imageName = null;
        OATreeNodeData tnd = (OATreeNodeData) value;
        Object obj = tnd.getObject();
        Icon icon = getIcon(obj);

        String text = ((JLabel)comp).getText();
        text = getText(obj, text);
        ((JLabel)comp).setText(text);
        
        if (def.imageProperty != null) {
            if (obj != null) {
                if (def.methodsToImage == null) def.methodsToImage = OAReflect.getMethods(obj.getClass(), def.imageProperty);
                
                Class returnClass = def.methodsToImage[def.methodsToImage.length-1].getReturnType();
                if (Icon.class.isAssignableFrom(returnClass)) {
                    icon = (Icon) OAReflect.getPropertyValue(obj, def.methodsToImage);
                }
                else if (returnClass.equals(byte[].class)) {
                    byte[] bs = (byte[]) OAReflect.getPropertyValue(obj, def.methodsToImage);
                    try {
                        if (bs != null) {
                            Image img = OAImageUtil.convertToBufferedImage(bs);
                            if (img != null) icon = new ImageIcon(img);
                        }
                    }
                    catch (IOException ex) {
                    }
                }
                else if (def.methodsToImage != null) {
                    imageName = OAReflect.getPropertyValueAsString(obj, def.methodsToImage);
                }
                
            }
        }

        imageName = getImage(obj, def.imagePath, imageName);
        if (imageName != null && imageName.length() > 0) {
            icon = getIcon(imageName);
            if (icon == null) {
                URL url = OATreeNode.class.getResource(imageName);
                if (url != null) {
                    icon = new ImageIcon(url);
                }
            }
        }       
        if (icon != null && (def.maxImageWidth > 0 || def.maxImageHeight > 0)) {
            icon = new ScaledImageIcon(icon, def.maxImageWidth, def.maxImageHeight);
        }

        Font font = null;
        if (def.fontProperty != null) {
            if (obj != null) {
                if (def.methodsToFont == null) def.methodsToFont = OAReflect.getMethods(obj.getClass(), def.fontProperty);
                if (def.methodsToFont != null) {
                    font = (Font) com.viaoa.util.OAConv.convert(Font.class, OAReflect.getPropertyValue(obj, def.methodsToFont));
                }
            }
        }
        if (font == null) font = ((JLabel)comp).getFont();
        font = getFont(obj, font);
        if (font != null) ((JLabel)comp).setFont(font);
        
        ((JLabel)comp).setOpaque(false);
        Color color = null;
        if (def.backgroundColorProperty != null && (!hasFocus && !selected)) {
            if (obj != null) {
                if (def.methodsToBackgroundColor == null) def.methodsToBackgroundColor = OAReflect.getMethods(obj.getClass(), def.backgroundColorProperty);
                if (def.methodsToBackgroundColor != null) {
                    color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, def.methodsToBackgroundColor));
                }
            }
        }
        color = getBackgroundColor(obj, color);
        if (color != null) {
            ((JLabel)comp).setOpaque(true);
            ((JLabel)comp).setBackground(color);
        }
        
        color = null;
        if (def.iconColorProperty != null) {
            if (obj != null) {
                if (def.methodsToIconColor == null) def.methodsToIconColor = OAReflect.getMethods(obj.getClass(), def.iconColorProperty);
                if (def.methodsToIconColor != null) {
                    color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, def.methodsToIconColor));
                }
            }
            if (color == null) color = Color.white;
        }
        if (color != null) {
            color = getIconColor(obj, color);
            if (color != null) {
                if (myColorIcon == null) myColorIcon = new ColoredLineUnderIcon();
                myColorIcon.setColor(color);
                ((JLabel)comp).setIcon(myColorIcon);
            }
        }
        else {
            if (myColorIcon != null) myColorIcon.setColor(null);
        }
        
        
        if (myMultiIcon == null) {
            myMultiIcon = new MultiIcon();
            myMultiIcon2 = new MultiIcon();
        }
        
        if (icon != null || color != null) {
            myMultiIcon.setIcon1(myColorIcon);
            myMultiIcon.setIcon2(icon);
            ((JLabel)comp).setIcon(myMultiIcon);
        }
        
        color = null;
        if (def.foregroundColorProperty != null) {
            if (obj != null) {
                if (def.methodsToForegroundColor == null) def.methodsToForegroundColor = OAReflect.getMethods(obj.getClass(), def.foregroundColorProperty);
                if (def.methodsToForegroundColor != null) {
                    color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, def.methodsToForegroundColor));
                }
            }
        }
        color = getForegroundColor(obj, color);
        if (color != null) ((JLabel)comp).setForeground(color);
        
        
        String ttt = getToolTipText();
        if (def.toolTipTextProperty != null) {
            if (obj != null) {
                if (def.methodsToToolTipText == null) def.methodsToToolTipText = OAReflect.getMethods(obj.getClass(), def.toolTipTextProperty);
                if (def.methodsToToolTipText != null) {
                    String s = OAReflect.getPropertyValueAsString(obj, def.methodsToToolTipText);
                    if (s != null && s.length() > 0) ttt = s;
                }
            }
        }
        if (ttt == null) ttt = OAConv.toString(value);
        ttt = getToolTipText(obj, ttt);
        if (ttt != null) ((JLabel)comp).setToolTipText(ttt);
        
        
        // 2008/04/24
        int chkType = getCheckType(tnd);
        if (chkType != CHECK_NONE) {
            if (checkIcon == null) checkIcon = new TNCheckIcon();

            checkIcon.checkBox.setSelected(chkType == CHECK_FULL);
            checkIcon.drawHalfCheck = (chkType == CHECK_HALF);

            boolean bInCheckBox = (this.def.tree.rowLastMouse == row && this.def.tree.xLastMouse >= 0 && this.def.tree.xLastMouse < checkIcon.getIconWidth()); 
            
            checkIcon.checkBox.getModel().setRollover(bInCheckBox && !this.def.tree.downLastMouse);
            boolean b = bInCheckBox && this.def.tree.downLastMouse;
            checkIcon.checkBox.getModel().setPressed(b);
            checkIcon.checkBox.getModel().setArmed(b);  // not real sure what this is
         
            myMultiIcon2.setIcon1(checkIcon); 
            myMultiIcon2.setIcon2(((JLabel)comp).getIcon());
            ((JLabel)comp).setIcon(myMultiIcon2);
        }
        
        
        return comp;
    }    
    
    
    
    
    /** 2006/12/21
     *  Called by renderer to get image name for a node.
     *  By default, this returns imagePath + defaultValue
     *  @param imagePath is path of image, which will end with a path seperator.
     */
    public String getImage(Object object, String imagePath, String defaultValue) {
        if (defaultValue != null && defaultValue.length() > 0 && imagePath != null && imagePath.length() > 0) {
            return imagePath + defaultValue;
        }
        return defaultValue;
    }
    public Font getFont(Object object, Font defaultValue) {
        return defaultValue;
    }
    public String getToolTipText(Object object, String defaultValue) {
        return defaultValue;
    }
    public Color getBackgroundColor(Object object, Color defaultValue) {
        return defaultValue;
    }
    public Color getForegroundColor(Object object, Color defaultValue) {
        return defaultValue;
    }
    public Color getIconColor(Object object, Color defaultValue) {
        return defaultValue;
    }
    public String getText(Object object, String text) {
        return text;
    }
    
    /**
     * Called by renderer to get ImageIcon to display.
     */
    public Icon getIcon(String fname) {
        if (fname == null) return null;
        return new ImageIcon(fname);
    }
}

/** replaced by jfc.image.ColorIcon
class TNColorIcon implements Icon {
    Color color;        
    public int getIconHeight() {
        return 17;
    }
    public int getIconWidth() {
        return 12;
    }

    public void paintIcon(Component c,Graphics g,int x,int y) {
        g.setColor(color==null?Color.white:color);
        g.fillRoundRect(x+1,y+3,11,11,2,2);
    }
}
*/

class TNCheckIcon implements Icon {
    JCheckBox checkBox;
    boolean drawHalfCheck;
    Color colorHalf;
    
    public TNCheckIcon() {
        updateUICalled();
    }
    
    public void updateUICalled() {
        if (checkBox == null) checkBox = new JCheckBox();
        else checkBox.updateUI();
        checkBox.setBorder(null);
        checkBox.setOpaque(false);
        checkBox.setSize(checkBox.getPreferredSize());
        colorHalf = (Color) UIManager.getDefaults().get("CheckBox.foreground");
        if (colorHalf == null) colorHalf = Color.gray;
    }
    
    public int getIconHeight() {
        return checkBox.getPreferredSize().height;
    }
    public int getIconWidth() {
        return checkBox.getPreferredSize().width;
    }
    public void paintIcon(Component c,Graphics g,int x,int y) {
        boolean b = checkBox.isDoubleBuffered();
        checkBox.setDoubleBuffered(false);
        checkBox.paint(g);
        
        if (drawHalfCheck) {
            g.setColor(colorHalf);
            int w = getIconWidth()/2;
            int h = getIconHeight()/2;
            g.fillRect(w-2, h-1, 5, 3);
        }
        checkBox.setDoubleBuffered(b);
    }
}


