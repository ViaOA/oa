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
package com.viaoa.jfc.propertypath;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.propertypath.model.oa.*;
import com.viaoa.jfc.tree.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/**
 * Used to create a Tree for selecting ObjectDef, PropertyDef, CalcPropertyDef, etc.
 * 
 * <pre>
 * tree = new OAPropertyPathTree(hubObjectDef, bShowAll, bShowCalc, bShowProps, bShowMany, bShowOne) {
 *     public void propertyPathCreated(OAPropertyPath pp) {
 *         OAObject oa = (OAObject) hub.getAO();
 *         if (oa != null) oa.setProperty(property, pp);
 *         // if in a OATreeComboBox: comboBox.hidePopup();
 *     }
 * };
 * tree.setColumns(40);
 * </pre>
 * 
 * @see #propertyPathCreated to caputre selected tree node
 */
public class OAPropertyPathTree extends OATree {
    private Hub<ObjectDef> hubObjectDef;
    private boolean bShowAll; // show all objectDefs, not just activeObject
    private boolean bShowCalc = true;
    private boolean bShowProps = true;
    private boolean bAllowMany, bAllowOne;
    private boolean bRecursive;
    private Hub hubFindObjectDef;
    private Hub<ObjectDef> hubAdditionalObjectDefs; // additonal list of ObjectDefs that can be selected (does not allow expanding)
    private boolean bLookupProperty; // used for PropertyPath.lookupProperty, if this PP does not "start with" the objectDef

    public OAPropertyPathTree(Hub hubObjectDef, boolean bShowAll, boolean bShowCalc, boolean bShowProps, boolean bAllowMany,
            boolean bAllowOne) {
        this(hubObjectDef, bShowAll, bShowCalc, bShowProps, bAllowMany, bAllowOne, true);
    }

    /**
     * If true, then this propertyPathTree is used to select lookup ObjectDefs as the root nodes.
     */
    public void setUseAsLookups(boolean b) {
        bLookupProperty = b;
    }

    /**
     * Recursive tree to select an OAPropertyDef from.
     * 
     * @param hubObjectDef
     *            is root hub where the active object is the "starting" point for finding a property.
     * @param bShowAll
     *            used for tree rootNode.showAll(). If tree then all OAObjectDefs will be shown. If
     *            false, then only the active object is shown.
     * @param bShowCalc
     *            flag to know if calculated properties should be shown.
     * @param bShowProps if false, then only OAObjectDef objects will be shown.
     * @param bAllowMany
     *            if true the Links of type MANY are listed
     * @param bAllowOne
     *            if true the Links of type ONE are listed
     * @param bRecursive
     *            if true, then allows to drill into link properties
     */
    public OAPropertyPathTree(Hub hubObjectDef, boolean bShowAll, boolean bShowCalc, final boolean bShowProps, final boolean bAllowMany,
            final boolean bAllowOne, boolean bRecursive) {
        super(18, 32);
        this.hubObjectDef = hubObjectDef;
        this.bShowAll = bShowAll;
        this.bShowCalc = bShowCalc;
        this.bShowProps = bShowProps;
        this.bAllowMany = bAllowMany;
        this.bAllowOne = bAllowOne;
        String cpp;

        setAllowDnD(false);

        OATreeNode nodeObject = new OATreeNode(ObjectDef.P_DisplayName, hubObjectDef) {
            @Override
            public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {
                comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof OATreeNodeData) {
                    value = ((OATreeNodeData) value).getObject();
                }
                OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
                return comp;
            }
        };

        URL url = OAPropertyPathTree.class.getResource("image/objectIcon.gif");
        ImageIcon ii = new ImageIcon(url);
        nodeObject.setIcon(ii);

        nodeObject.setShowAll(bShowAll);

        OATreeNode nodeProp, nodeCalc;
        nodeProp = nodeCalc = null;
        if (bShowProps) {
            cpp = OAString.cpp(ObjectDef.P_PropertyDefs, PropertyDef.P_DisplayName);
            nodeProp = new PropertyTreeNode(cpp) {
                @Override
                public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {
                    comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
                    if (value instanceof OATreeNodeData) {
                        value = ((OATreeNodeData) value).getObject();
                    }
                    OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
                    return comp;
                }
            };

            url = OAPropertyPathTree.class.getResource("image/propertyIcon.gif");
            ii = new ImageIcon(url);
            nodeProp.setIcon(ii);
            nodeObject.add(nodeProp);
        }

        if (bShowCalc) {
            nodeCalc = new CalcTreeNode();
            nodeObject.add(nodeCalc);
        }
        else if (bShowProps) {
            // show hub calcs
            nodeCalc = new CalcTreeNode(true);
            nodeObject.add(nodeCalc);
        }

        if (bAllowMany || bAllowOne || bShowProps) {
            OATreeNode nodeLink = null;
            cpp = OAString.cpp(ObjectDef.P_LinkPropertyDefs, LinkPropertyDef.P_Name);
            nodeLink = new OATreeNode(cpp) {
                @Override
                public HubFilter getHubFilter(Hub hub) {
                    Hub h = new Hub(LinkPropertyDef.class);
                    HubFilter hf = new HubFilter(hub, h) {
                        public boolean isUsed(Object object) {
                            LinkPropertyDef lp = (LinkPropertyDef) object;
                            if (bAllowMany) {
                                if (lp.getType() == lp.TYPE_Many) return true;
                            }
                            if (bAllowOne) {
                                if (lp.getType() == lp.TYPE_One) return true;
                            }
                            if (bShowProps && (lp.getType() == lp.TYPE_Many)) {
                                ObjectDef od = lp.getToObjectDef();
                                for (CalcPropertyDef cp : od.getCalcPropertyDefs()) {
                                    if (cp.getIsForHub()) return true;
                                }
                            }
                            return false;
                        }
                    };
                    String cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_CalcPropertyDefs,
                            CalcPropertyDef.P_IsForHub);
                    hf.addDependentProperty(cpp);
                    return hf;
                }

                @Override
                public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {
                    comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
                    if (value instanceof OATreeNodeData) {
                        value = ((OATreeNodeData) value).getObject();
                    }
                    OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
                    return comp;
                }
            };
            url = OAPropertyPathTree.class.getResource("image/linkIcon.gif");
            ii = new ImageIcon(url);
            nodeLink.setIcon(ii);
            nodeObject.add(nodeLink);

            if (bRecursive) {
                OATreeNode node1, node2, node3;
                node1 = node2 = node3 = null;
                if (bShowProps) {
                    cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_PropertyDefs, PropertyDef.P_Name);
                    node1 = new PropertyTreeNode(cpp) {
                        @Override
                        public HubFilter getHubFilter(final OATreeNodeData parentTnd, Hub hub) {
                            Hub h = new Hub(PropertyDef.class);
                            HubFilter hf = new HubFilter(hub, h) {
                                public boolean isUsed(Object object) {
                                    if (bAllowOne && !bAllowMany) {
                                        LinkPropertyDef lp = (LinkPropertyDef) parentTnd.getObject();
                                        if (lp.getType() == lp.TYPE_Many) {
                                            // only showing hubCalcProps from link=many
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            };
                            return hf;
                        }
                    };
                    url = OAPropertyPathTree.class.getResource("image/propertyIcon.gif");
                    ii = new ImageIcon(url);
                    node1.setIcon(ii);
                    nodeLink.add(node1);
                }

                if (bShowCalc) {
                    cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_CalcPropertyDefs,
                            CalcPropertyDef.P_Name);
                    node2 = new CalcTreeNode(cpp, false);
                    nodeLink.add(node2);
                }
                else if (bShowProps) {
                    // show hub calcProps only for many links
                    cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_CalcPropertyDefs,
                            CalcPropertyDef.P_Name);
                    node2 = new CalcTreeNode(cpp, true);
                    nodeLink.add(node2);
                }

                cpp = OAString
                        .cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_LinkPropertyDefs, LinkPropertyDef.P_Name);
                node3 = new OATreeNode(cpp) {
                    @Override
                    public HubFilter getHubFilter(final OATreeNodeData parentTnd, Hub hub) {
                        Hub h = new Hub(LinkPropertyDef.class);
                        HubFilter hf = new HubFilter(hub, h) {
                            public boolean isUsed(Object object) {
                                LinkPropertyDef lp = (LinkPropertyDef) object;
                                if (bAllowMany) {
                                    if (lp.getType() == lp.TYPE_Many) return true;
                                }
                                if (bAllowOne) {
                                    if (!bAllowMany) { // only show calcs
                                        LinkPropertyDef lpx = (LinkPropertyDef) parentTnd.getObject();
                                        if (lpx.getType() == lpx.TYPE_Many) {
                                            return false;
                                        }
                                    }
                                    if (lp.getType() == lp.TYPE_One) return true;
                                }
                                if (bShowProps && (lp.getType() == lp.TYPE_Many)) {
                                    ObjectDef od = lp.getToObjectDef();
                                    for (CalcPropertyDef cp : od.getCalcPropertyDefs()) {
                                        if (cp.getIsForHub()) return true;
                                    }
                                }
                                return false;
                            }
                        };
                        String cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_CalcPropertyDefs,
                                CalcPropertyDef.P_IsForHub);
                        hf.addDependentProperty(cpp);
                        return hf;
                    }

                    @Override
                    public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected,
                            boolean expanded, boolean leaf, int row, boolean hasFocus) {
                        comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
                        if (value instanceof OATreeNodeData) {
                            value = ((OATreeNodeData) value).getObject();
                        }
                        OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
                        return comp;
                    }
                };
                url = OAPropertyPathTree.class.getResource("image/linkIcon.gif");
                ii = new ImageIcon(url);
                node3.setIcon(ii);
                nodeLink.add(node3);

                // make recursive
                if (bShowProps) {
                    node3.add(node1);
                }
                if (bShowCalc || bShowProps) node3.add(node2);
                node3.add(node3);
            }
            else if (bShowProps) { // not recursive
                // show hub calc properties only for Many links
                nodeLink = null;

                cpp = OAString.cpp(ObjectDef.P_LinkPropertyDefs, LinkPropertyDef.P_Name);
                nodeLink = new OATreeNode(cpp) {
                    @Override
                    public HubFilter getHubFilter(Hub hub) {
                        Hub h = new Hub(LinkPropertyDef.class);
                        HubFilter hf = new HubFilter(hub, h) {
                            public boolean isUsed(Object object) {
                                LinkPropertyDef lp = (LinkPropertyDef) object;
                                if (lp.getType() == lp.TYPE_One) return false;
                                ObjectDef od = lp.getToObjectDef();
                                for (CalcPropertyDef cp : od.getCalcPropertyDefs()) {
                                    if (cp.getIsForHub()) return true;
                                }
                                return false;
                            }
                        };

                        String cpp = OAString.cpp(LinkPropertyDef.P_ToObjectDef, ObjectDef.P_CalcPropertyDefs,
                                CalcPropertyDef.P_IsForHub);
                        hf.addDependentProperty(cpp);
                        cpp = OAString.cpp(LinkPropertyDef.P_Type);
                        hf.addDependentProperty(cpp);
                        return hf;
                    }

                    @Override
                    public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected,
                            boolean expanded, boolean leaf, int row, boolean hasFocus) {
                        comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
                        if (value instanceof OATreeNodeData) {
                            value = ((OATreeNodeData) value).getObject();
                        }
                        OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
                        return comp;
                    }
                };

                url = OAPropertyPathTree.class.getResource("image/linkIcon.gif");
                ii = new ImageIcon(url);
                nodeLink.setIcon(ii);
                nodeObject.add(nodeLink);

                cpp = OAString.cpp(ObjectDef.P_CalcPropertyDefs, CalcPropertyDef.P_Name);
                OATreeNode node2 = new CalcTreeNode(cpp, true);
                nodeLink.add(node2);
            }
        }
        add(nodeObject);
    }

    public void updateTreeCellRendererComponent(Component comp, Object value) {
        if (hubFindObjectDef == null) return;
        if (value instanceof LinkPropertyDef && comp instanceof JLabel) {
            LinkPropertyDef lp = (LinkPropertyDef) value;
            if (lp.getToObjectDef() == hubFindObjectDef.getAO()) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            }
        }
    }

    /**
     * additional list of ObjectDefs that can be selected (does not allow expanding)
     * 
     * @param hubAdditionalObjectDefs
     *            list of objectDefs to include at the root level.
     */
    public void setAdditionalObjectDefs(Hub<ObjectDef> hubAdditionalObjectDefs) {
        if (this.hubAdditionalObjectDefs != null) return; //qqq remove/replace not done
        this.hubAdditionalObjectDefs = hubAdditionalObjectDefs;
        if (hubAdditionalObjectDefs != null) {
            OATreeNode node = new OATreeNode(ObjectDef.P_DisplayName, hubAdditionalObjectDefs);
            URL url = OAPropertyPathTree.class.getResource("image/objectIcon.gif");
            ImageIcon ii = new ImageIcon(url);
            node.setIcon(ii);
            add(node);
        }
    }

    public Hub getAdditionalObjectDefs() {
        return hubAdditionalObjectDefs;
    }

    /**
     * Uses the ActiveObject of hub to be the only ObjectDef that can be selected. This is used to
     * select a path that ends with a specific ObjectDef.
     */
    public void setFindObjectDefHub(Hub hub) {
        hubFindObjectDef = hub;
    }

    @Override
    protected boolean isValidSelection(TreeSelectionEvent e) {
        if (e == null) return false;
        TreePath tp = e.getNewLeadSelectionPath();
        if (tp == null) return false;

        if (hubFindObjectDef == null) return true;

        Object[] objs = tp.getPath();
        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
        Object obj = tnd.getObject();

        ObjectDef findObjectDef = (ObjectDef) hubFindObjectDef.getAO();
        if (findObjectDef == null) return false;
        if (!(obj instanceof LinkPropertyDef)) return false;
        LinkPropertyDef lpd = (LinkPropertyDef) obj;
        if (lpd.getToObjectDef() != findObjectDef) return false;

        return true;
    }

    public void nodeSelected(TreeSelectionEvent e) {
        // super.nodeSelected(e);  // dont call, it will set Active objects
        TreePath tp = e.getNewLeadSelectionPath();
        if (tp != null) {
            Object[] objs = tp.getPath();
            OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
            Object obj = tnd.getObject();

            if (bRecursive && !(obj instanceof PropertyDef) && !(obj instanceof CalcPropertyDef)) {
                if (bShowProps || bShowCalc) return;
                // otherwise, select LinkDef
                if (hubFindObjectDef != null) {
                    ObjectDef findObjectDef = (ObjectDef) hubFindObjectDef.getAO();
                    if (findObjectDef != null) {
                        LinkPropertyDef lpd = (LinkPropertyDef) obj;
                        if (lpd.getToObjectDef() != findObjectDef) return;
                    }
                }
            }

            String propertyPath = "";
            boolean bLookup = bLookupProperty;

            ObjectDef od = null;

            //was: for (int i=((lookupProperty||(objs.length==2))?1:2); i<objs.length; i++) {
            for (int i = 1; i < objs.length; i++) {
                tnd = (OATreeNodeData) objs[i];
                obj = tnd.getObject();
                if (obj instanceof LinkPropertyDef) {
                    if (propertyPath.length() > 0) propertyPath += '.';
                    propertyPath += ((LinkPropertyDef) obj).getName();
                }
                else if (obj instanceof PropertyDef) {
                    if (propertyPath.length() > 0) propertyPath += '.';
                    propertyPath += ((PropertyDef) obj).getName();
                }
                else if (obj instanceof CalcPropertyDef) {
                    if (propertyPath.length() > 0) propertyPath += '.';
                    propertyPath += ((CalcPropertyDef) obj).getName();
                }
                else if (obj instanceof ObjectDef) {
                    if (i == 1) {
                        od = (ObjectDef) obj;
                    }
                    else {
                        if (propertyPath.length() > 0) propertyPath += '.';
                        propertyPath += ((ObjectDef) obj).getName();
                    }
                }
            }
            if (propertyPath.length() == 0) {
                if (od == null) return;
                bLookup = true;
            }

            propertyPathCreated(propertyPath);
        }
    }

    /** Can be overwritten to know when a new OAPropertyPath is created. */
    public void propertyPathCreated(String propertyPath) {

    }

    /**
     * Used to select the tree node for a specific propertyPath
     */
    public void selectPropertyPath(String propertyPath) {

        // convert path into objects used in tree
        ArrayList<Object> al = new ArrayList<Object>();

        ObjectDef od = null;

        OATreeNodeData nodeData = (OATreeNodeData) getModel().getRoot();

        if (!bShowAll) {
            od = (ObjectDef) hubObjectDef.getAO();
            if (od == null) return;
            al.add(od);
            nodeData = nodeData.getChild(0);
        }
        if (nodeData == null) return;

        StringTokenizer st = new StringTokenizer(propertyPath, ".");
        for (int i = 0; st.hasMoreTokens(); i++) {

            String value = st.nextToken();
            if (od == null) {
                od = (ObjectDef) hubObjectDef.find(ObjectDef.P_Name, value);
                if (od == null) break;
                int pos = hubObjectDef.getPos(od);
                if (pos < 0) break;
                for (int j = pos; j < nodeData.getChildCount(); j++) {
                    nodeData = nodeData.getChild(j);
                    if (nodeData == null) break;
                    if (nodeData.object == od) break;
                }
                if (nodeData == null) break;
                al.add(od);
            }
            else {
                // try to find as a property
                Hub h = od.getPropertyDefs();
                PropertyDef prop = (PropertyDef) h.find(PropertyDef.P_Name, value);
                if (prop != null) {
                    OATreeNodeData tnd = null;
                    for (int j = 0; j < nodeData.getChildCount(); j++) {
                        tnd = nodeData.getChild(j);
                        if (tnd.object == prop) break;
                        tnd = null;
                    }
                    if (tnd == null) break;
                    nodeData = tnd;
                    al.add(prop);
                }
                else {
                    // try to find as a calc property
                    h = od.getCalcPropertyDefs();
                    CalcPropertyDef calc = (CalcPropertyDef) h.find(CalcPropertyDef.P_Name, value);
                    if (calc != null) {
                        OATreeNodeData tnd = null;
                        for (int j = 0; nodeData != null && j < nodeData.getChildCount(); j++) {
                            tnd = nodeData.getChild(j);
                            if (tnd.object == calc) break;
                            tnd = null;
                        }
                        if (tnd == null) break;
                        nodeData = tnd;
                        if (nodeData == null) break;
                        al.add(calc);
                    }
                    else {
                        // try to find as a link
                        h = od.getLinkPropertyDefs();
                        LinkPropertyDef lp = (LinkPropertyDef) h.find(LinkPropertyDef.P_Name, value);
                        if (lp != null) {
                            OATreeNodeData tnd = null;
                            for (int j = 0; j < nodeData.getChildCount(); j++) {
                                tnd = nodeData.getChild(j);
                                if (tnd.object == lp) break;
                                tnd = null;
                            }
                            if (tnd == null) break;
                            nodeData = tnd;
                            al.add(lp);
                            od = lp.getToObjectDef();
                        }
                        else { // not found
                            if (i == 0) {
                                if (hubAdditionalObjectDefs != null) {
                                    for (ObjectDef odx : hubAdditionalObjectDefs) {
                                        if (value.equalsIgnoreCase(odx.getName())) {
                                            al.clear();
                                            al.add(odx);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        setSelectedNode(al.toArray());
    }

    class PropertyTreeNode extends OATreeNode {
        MyTreeRenderer propertyTreeRenderer = new MyTreeRenderer();

        public PropertyTreeNode(String pp) {
            super(pp);
        }

        @Override
        public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            if (value instanceof OATreeNodeData) {
                value = ((OATreeNodeData) value).getObject();
            }
            if (!(value instanceof PropertyDef)) return comp;
            PropertyDef pd = (PropertyDef) value;

            propertyTreeRenderer.add(comp, BorderLayout.CENTER);
            propertyTreeRenderer.lbl1.setIcon(((JLabel) comp).getIcon());
            ((JLabel) comp).setIcon(null);

            return propertyTreeRenderer;
        }
    };

    class MyTreeRenderer extends JComponent {
        JLabel lbl1, lbl2;

        public MyTreeRenderer() {
            lbl1 = new JLabel();
            lbl2 = new JLabel();
            setLayout(new BorderLayout(1, 0));
            add(lbl1, BorderLayout.WEST);
            add(lbl2, BorderLayout.EAST);
            setBorder(null);
        }
    };

    class CalcTreeNode extends OATreeNode {
        Icon icon1, icon2;
        boolean bHubCalcsOnly;
        Font font;

        public CalcTreeNode() {
            this(OAString.cpp(ObjectDef.P_CalcPropertyDefs, CalcPropertyDef.P_Name), false);
        }

        public CalcTreeNode(boolean bHubCalcsOnly) {
            this(OAString.cpp(ObjectDef.P_CalcPropertyDefs, CalcPropertyDef.P_Name), bHubCalcsOnly);
        }

        public CalcTreeNode(String pp, boolean bHubCalcsOnly) {
            super(pp);
            this.bHubCalcsOnly = bHubCalcsOnly;
        }

        @Override
        public HubFilter getHubFilter(final OATreeNodeData parentTnd, Hub hub) {
            Hub h = new Hub(CalcPropertyDef.class);
            HubFilter hf = new HubFilter(hub, h) {
                @Override
                public boolean isUsed(Object object) {
                    CalcPropertyDef cp = (CalcPropertyDef) object;

                    Object objx = parentTnd.getObject();
                    if (objx instanceof LinkPropertyDef) {
                        LinkPropertyDef lp = (LinkPropertyDef) objx;
                        if (lp.getType() == LinkPropertyDef.TYPE_Many) {
                            if (bAllowMany && bRecursive) {
                                return true; // show all                                
                            }
                            else {
                                return cp.getIsForHub();
                            }
                        }
                    }
                    return !bHubCalcsOnly && !cp.getIsForHub();
                }
            };
            return hf;
        }

        @Override
        public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
            if (value instanceof OATreeNodeData) {
                value = ((OATreeNodeData) value).getObject();
            }

            OAPropertyPathTree.this.updateTreeCellRendererComponent(comp, value);
            if (value instanceof CalcPropertyDef) {
                JLabel lbl = (JLabel) comp;
                if (icon1 == null) {
                    URL url = OAPropertyPathTree.class.getResource("image/calcPropertyIcon.gif");
                    icon1 = new ImageIcon(url);
                    url = OAPropertyPathTree.class.getResource("image/calcHubIcon.gif");
                    icon2 = new ImageIcon(url);

                    font = comp.getFont();
                    font = font.deriveFont(Font.ITALIC);
                    setFont(font);
                }
                if (((CalcPropertyDef) value).getIsForHub()) {
                    lbl.setIcon(icon2);
                }
                else lbl.setIcon(icon1);
            }
            return comp;
        }
    }
}
