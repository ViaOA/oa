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
package com.viaoa.jfc.tree;

import java.awt.Cursor;
import java.awt.Window;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import com.viaoa.util.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.control.*;


/**
 * Used by OATree Model and Hub2TreeNode to maintain the data and listeners for the OATreeNodes.
 */
public class OATreeNodeData {

	public OATreeNode node;
    public Object object;
    public OATreeNodeData parent;
    public ArrayList<OATreeNodeData> listChildren;
    Hub2TreeNode[] hub2TreeNodes;  // used to listen to all of the Hubs for the children that this is the parent of.
    
    public OATreeNodeData(OATreeNode node, Object obj, OATreeNodeData parent) {
        this.node = node;
        this.object = obj;
        this.parent = parent;
    }
    
    public OATreeNodeData getParent() {
        return parent;
    }

    protected void close(OATreeNode node) {
    	if (this.node == node) close();
    	else {
        	if (listChildren != null) {
        		for (OATreeNodeData tnd : listChildren) {
    				tnd.close(node);
    			}
        	}
    	}
    }    
    protected void close() {
        if (node.getUpdateHub() != null) {
            // 20101229 not needed - this causes problems during a sort (newListEvent)
        	// if (node.getUpdateHub().getAO() == object) node.getUpdateHub().setAO(null); 
        }
    	closeChildren();
    }
    public void closeChildren() {
		for (int i=0; hub2TreeNodes != null && i < hub2TreeNodes.length; i++) {
			if (hub2TreeNodes[i] != null) {
				hub2TreeNodes[i].close();
				hub2TreeNodes[i] = null;
			}
		}

    	if (listChildren != null) {
    	    
    		for (int i=0;i<listChildren.size(); i++) {
    		    try {
    		        OATreeNodeData tnd = listChildren.get(i);
    		        tnd.close();
    		    }
    		    catch (Throwable t) {
    		        break;
    		    }
			}
    		listChildren = null;
    	}
    }

    
    public void loadChildren() {
        if (listChildren != null) return;
		synchronized (this) {
	        if (listChildren != null) return;
			listChildren = new ArrayList<OATreeNodeData>(5);
		}

        Object object = this.object;
        for (int i=0; i < node.getChildrenTreeNodes().length; i++) {
            OATreeNode tn = node.getChildrenTreeNodes()[i];

            if (i == 0) {
            	hub2TreeNodes = new Hub2TreeNode[node.getChildrenTreeNodes().length];
            }
            
            if (tn.getTitleFlag()) {
            	OATreeNodeData tnd = new OATreeNodeData(tn, this.object, this);  // note: make sure that object is "passed down"
        		addChild(tnd);
                continue;
            }

            if (tn.getHub() != null) {
                if (!tn.getShowAll()) {
                	Object obj = tn.getHub().getActiveObject();
                	//was:   if (obj == null) continue;
                    if (obj == null) {
                        // 20111130 need to have a listener for when AO changes again
                        hub2TreeNodes[i] = new Hub2TreeNode(tn.getHub(), tn, this, null);
                        continue;
                    }
                    OATreeNodeData tnd = new OATreeNodeData(tn,obj,this);
            		addChild(tnd);
            		hub2TreeNodes[i] = new Hub2TreeNode(tn.getHub(), tn, this, null);
                }
                else {
            		Hub h = tn.getHub();
            		HubFilter hf = tn.getHubFilter(tn.getHub());
                    if (hf != null) h = hf.getHub();
            		for (int i2=0; ;i2++) {
            			Object obj = h.elementAt(i2);
            			if (obj == null) break;
            			OATreeNodeData tnd = new OATreeNodeData(tn, obj, this);
                		addChild(tnd);
            		}
            		hub2TreeNodes[i] = new Hub2TreeNode(h, tn, this, hf);
                }
                continue;
            }

            if (object == null) continue;
            
/* 20110802 removed            
            //qqqqqqqq
            // Temp Hack: 20101209 changed so that it will always get methods, since a treeNode could be reused (ex: recursive) and the methods could be wrong
            //   this will be fixed later, when the method path is in the TreeNodeChild        
            if (tn.methodsToHub != null) {
                tn.methodsToHub = null;
                tn.methodsToProperty = null;
            }
*/            
            
            if (tn.getMethodsToHub() == null && (tn.bRecursive || tn.getMethodsToProperty() == null)) {
                tn.findMethods(object.getClass(), true);
            }
            
            if (tn.getMethodsToHub() != null) {
                Hub h = getHubToChild(tn);
                
                HubFilter hf = tn.getHubFilter(h);
                if (hf != null) h = hf.getHub();
                if (h == null) {
                    continue;
                }
        		for (int i2=0; ;i2++) {
        			Object obj = h.elementAt(i2);
        			if (obj == null) break;
        			OATreeNodeData tnd = new OATreeNodeData(tn, obj, this);
            		addChild(tnd);
        		}
        		hub2TreeNodes[i] = new Hub2TreeNode(h, tn, this, hf);
            }
            else {
                if (tn.getMethodsToProperty() == null) tn.findMethods(object.getClass(), false);
                if (tn.getMethodToObject() != null) {
                    Object obj = OAReflect.getPropertyValue(object, tn.getMethodToObject());
                    OATreeNodeData tnd = new OATreeNodeData(tn, obj, this);
                	addChild(tnd);
//20111008 note: there is not hub2TreeNode listener, since this is a linkOne and does not have a hub to listen to                	
                }
                else {
                	OATreeNodeData tnd = new OATreeNodeData(tn,object,this);
            		addChild(tnd);
                }
            }
        }
    }

    protected Hub getHubToChild(final OATreeNode tnChild) {
        Hub h = null;
        if (tnChild.getMethodsToHub() != null) {
            h = (Hub) OAReflect.getPropertyValue(object, tnChild.getMethodsToHub());
        }
        return h;
    }
    
    
    public OATreeNodeData getChild(OATreeNode node, Object obj) {
    	if (listChildren == null) return null;
    	for (int i=0; i < listChildren.size(); i++) {
    		OATreeNodeData data = (OATreeNodeData) listChildren.get(i);
    		if (data.node == node && data.object == obj) return data;
    	}
    	return null;
    }
    
    public void insertChild(int pos, OATreeNodeData data) {
		listChildren.add(pos, data);
    }

    public void addChild(OATreeNodeData data) {
		listChildren.add(data);
    }
    
    public void removeChild(OATreeNodeData data) {
    	if (listChildren != null) {
    		listChildren.remove(data);
    		data.close();
    	}
    }

    // 20091214
    public void WAS_moveChild(int pos1, int pos2) {
    	OATreeNodeData tnd = listChildren.get(pos1);
    	listChildren.add(pos2, tnd);
    	if (pos2 < pos1) pos1++;
		listChildren.remove(pos1);
    }

    public void moveChild(int pos1, int pos2) {
        OATreeNodeData tnd = listChildren.get(pos1);
        listChildren.remove(pos1);
        listChildren.add(pos2, tnd);
    }
    
    public int getChildIndex(OATreeNodeData tnd) {
        if (listChildren == null) return 0;
        return listChildren.indexOf(tnd);
    }
    
    public int getChildIndex(Object obj, OATreeNode node) {
        if (listChildren == null) loadChildren();
        for (int i=0;i<listChildren.size(); i++) {
            try {
                OATreeNodeData tnd = listChildren.get(i);
                if (tnd.node == node && tnd.object == obj) return i;
            }
            catch (Throwable t) {
                break;
            }
        }
        
        
        return -1;
    }
    
    public boolean areChildrenLoaded() {
        return listChildren != null;
    }
    
    // returns the Hub that this tnd.object belongs to
    public Hub getHub() {
        return node.getHubForNodeData(this);
    }
    public Object getObject() {
        return object;
    }
    public OATreeNode getNode() {
        return node;
    }
    public boolean getAreChildrenLoaded() {
        return isLeaf() || listChildren != null;
    }
    public int getChildCount() {
        if (listChildren == null) loadChildren();
        return listChildren.size();
    }

    public OATreeNodeData getChild(int index) {
        if (listChildren == null) loadChildren();
    	if (index < 0 || index >= listChildren.size()) return null;
    	return listChildren.get(index);
    }
            
    public boolean isLeaf() {
        return (node.getChildrenTreeNodes().length == 0);
    }

    public String toString() {
        return (node.toString(this));
    }
}
