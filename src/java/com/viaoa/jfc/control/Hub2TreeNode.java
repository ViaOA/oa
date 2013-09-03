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
package com.viaoa.jfc.control;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.hub.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.tree.*;

/** 
    Used by OATreeNodeData for each node in the tree that uses a Hub.
    @see OATreeNode
    @see OATreeTitleNode
*/
public class Hub2TreeNode extends HubListenerAdapter {
    OATreeNode node;
    OATreeNodeData parent;
    Hub hub;
    HubFilter hubFilter;  // this is so that HubFilter.close() will be called when this.close() is called.  

    public Hub2TreeNode(Hub hub, OATreeNode node, OATreeNodeData parent, HubFilter hf) {
        String pp = node.getPropertyPath(); 
    	if (pp != null) {
    	    hub.addHubListener(this, pp);
    	}
    	else hub.addHubListener(this);
        
        this.hub = hub;
        this.node = node;
        this.parent = parent;
        this.hubFilter = hf;
    }

    public void close() {
    	if (hub != null) {
        	if (node.getPropertyPath() != null && node.getPropertyPath().indexOf(".") < 0) {
                hub.removeHubListener(this);
        		//20101218 was: hub.removeHubListener(this, node.getPropertyPath());
        	}
        	else hub.removeHubListener(this);
    	}
    	if (hubFilter != null) hubFilter.close();
    }
    
    
    public @Override void afterPropertyChange(HubEvent e) {
        if (e.getObject() instanceof Hub) return;
        String s = e.getPropertyName();
        if (s == null) return;
        // if (s.equalsIgnoreCase("changed") || s.equalsIgnoreCase("new")) return;

        if (node == null) {
            return;
        }
        if (node.getPropertyPath() == null) {
            return;
        }
        
        if (node.getPropertyPath().toUpperCase().indexOf(s.toUpperCase()) < 0) {
        	if (node.getIconColorProperty() == null || node.getIconColorProperty().toUpperCase().indexOf(s.toUpperCase()) < 0) {
            	if (node.getImageProperty() == null || node.getImageProperty().toUpperCase().indexOf(s.toUpperCase()) < 0) {
            	    if (node.getDependentProperties() == null) return;
            	    boolean b = false;
            	    for (String sx : node.getDependentProperties()) {
            	        if (sx != null && sx.toUpperCase().indexOf(s.toUpperCase()) >= 0) {
            	            b = true;
            	            break;
            	        }
            	    }
            		if (!b) return;
            	}
        	}
        }
        
        int pos = parent.getChildIndex(e.getObject(), node);
        if (pos >= 0) {
            ((OATreeModel)node.getTree().getModel()).changedNode(parent, pos);  
            node.getTree().repaint(); 
        }            
    }

    public @Override void afterAdd(final HubEvent e) {
        if (node.getShowAll() && !HubSelectDelegate.isFetching(hub)) {
        	try {
        		if (parent.getChild(node, e.getObject()) == null) {
        			OATreeNodeData tnd = new OATreeNodeData(node, e.getObject(),parent);
        			int pos = getStartPos()+e.getPos();
        			parent.insertChild(pos, tnd);
        			((OATreeModel)node.getTree().getModel()).insertNode(parent, pos, e.getObject());        			
        		}
            }
            catch (Throwable ex) {
                //System.out.println("Hub2TreeNode.afterAdd "+ex+", will continue");
                //ex.printStackTrace();
                ((OATreeModel)node.getTree().getModel()).fireTreeStructureChanged(parent);
            }
        }
    }

    
    public @Override void afterInsert(HubEvent e) {
        afterAdd(e);
    }


    public @Override void afterRemove(HubEvent e) {
		if (node.getShowAll()) {
			OATreeNodeData tnd = parent.getChild(node, e.getObject());
			if (tnd != null) {
        		int pos = parent.getChildIndex(tnd);
        		parent.removeChild(tnd);
    			((OATreeModel)node.getTree().getModel()).removeNode(parent, pos, e.getObject());
    		}
        }
    }

    // from start of parent, not top of tree
    protected int getStartPos() {
    	int nodePos = 0;
    	for (int i=0; i < parent.node.getChildrenTreeNodes().length; i++) {
            OATreeNode tn = parent.node.getChildrenTreeNodes()[i];
            if (tn == node) {
            	nodePos = i;
            	break;
            }
        }
    	nodePos++;
    	
    	int i = 0;
    	parent.loadChildren();
    	for (; i < parent.listChildren.size(); i++) {
    		OATreeNodeData dx = (OATreeNodeData) parent.listChildren.get(i);
    		if (dx.node == node) return i;
        	for (int i2=nodePos; i2 < parent.node.getChildrenTreeNodes().length; i2++) {
                if (parent.node.getChildrenTreeNodes()[i2] == dx.node) return i; 
            }
    	}
    	return i;
    }
    
    
    public @Override void afterMove(HubEvent e) {
    	if (node.getShowAll()) {
            int pos1 = e.getFromPos();
            int pos2 = e.getToPos();
            
            int startPos = getStartPos();
			parent.moveChild(startPos+pos1, startPos+pos2);
            
			((OATreeModel)node.getTree().getModel()).movedNode(parent, startPos+pos1, startPos+pos2); // invokedLater
        }
    }

    private Object[] getObjectPath(int amt, Object object) {
        // amt = number of object from the top of the tree to get. -1 to get all
        int i,x;
        OATreeNodeData tnd = parent;

        for (x=0 ;tnd != null; x++ ) tnd = tnd.parent;
        
        if (amt < 0 || amt > x) amt = x;
        //else if (object != null) amt++; // 20130831

        if (object != null) amt++;
        
        Object[] objs = new Object[amt];
        tnd = parent;
        for (i=0; i<x; i++ ) {
            if (x-i <= amt) objs[x-i-1] = tnd;
            tnd = tnd.parent;
        }

        if (object != null) {
            x = parent.getChildCount();
            for (i=0; i<x; i++) {
                tnd = parent.getChild(i);
                if (tnd.object == object) {
                    objs[objs.length-1] = tnd;
                }
            }
        }        
        return objs;
    }
    
    protected void invokeExpandRow(final int row) {
        if (SwingUtilities.isEventDispatchThread()) node.getTree().expandRow(row);
        else {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        node.getTree().expandRow(row);
                    }
                });
            }
            catch (Exception e) {}
        }
    }
    protected void invokeSelectRow(final int row) {
        if (SwingUtilities.isEventDispatchThread()) node.getTree().setSelectionRow(row);
        else {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        node.getTree().setSelectionRow(row);
                    }
                });
            }
            catch (Exception e) {}
        }
    }



    /** if the "node" has an updateHub, then OATreeNode will keep the activeObject in sync
        and this will update the tree.
    */
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (node.getTree().isSelectingNode()) return;
        if (e.getPos() < 0) return;
        HubEvent hcae = e;
        if (!node.getShowAll()) {
            ((OATreeModel)node.getTree().getModel()).fireTreeStructureChanged(parent);
            return;
        }

// 20091124 was:        
//        if (node.getUpdateHub() != null && (HubShareDelegate.isUsingSameSharedHub(node.getUpdateHub(), hub)) ) {
        
        // root node has updateHub set to null, so we need to use source hub if rootNode.
        Hub hubUpdate = node.getUpdateHub();
        if (hubUpdate == null) {
            OATreeNode root = node.getTree().getRoot();
            OATreeNode[] tns = root.getChildrenTreeNodes();
            if (OAArray.contains(tns, node)) hubUpdate = hub;
        }
        
        if (hubUpdate != null && (HubShareDelegate.isUsingSameSharedHub(hubUpdate, hub)) ) {
            if (node.getTree().isSelectingNode()) return; // OATree.valueChanged() in process
            node.getTree().setSettingNode(this, true); // this tells OATree not to respond to valueChange

            // step 1: make sure parent is expanded
            Object[] objs = getObjectPath(-1,null);
            TreePath tp = new TreePath(objs);
            int pos = node.getTree().getRowForPath(tp);
            if (pos < 0) {
                int x = objs.length;
                for (int i=1; i<x; i++) { // dont expand root
                    tp = new TreePath(getObjectPath(i,null));
                    pos = node.getTree().getRowForPath(tp);
                    invokeExpandRow(pos);
                }
            }
            else {
            	invokeExpandRow(pos);  // this will force the load of the node to select
            }

            // step 2: get row 
// 20110102 need to get AO from hubUpdate, since it could have setNullOnRemove=true            
//was:            objs = getObjectPath(-1,hcae.getObject());

            // 20101228: long story: on hub.remove, the listened to hub gets this event before updateHub (which has the removed object still as AO)
            Object objx = hubUpdate.getAO();
            if (objx != null && !hubUpdate.contains(objx)) objx = hcae.getObject();   
            
            objs = getObjectPath(-1, objx);
            
            try {
                tp = new TreePath(objs);
                pos = node.getTree().getRowForPath(tp);
            }
            catch (Exception exc) {
                node.getTree().setSettingNode(this, false);
                return; //hack 20091216 dragNdrop from tree to OABldr diagramLayeredPane causes a nullPtr excpt
            }
            invokeSelectRow(pos);

            Container cont = node.getTree().getParent();
            if (cont instanceof JViewport) {
                Rectangle rec = node.getTree().getRowBounds(pos);
                if (rec != null) {
                    Point pt = ((JViewport) cont).getViewPosition();
                    rec.x -= pt.x;
                    rec.y -= pt.y;
                    ((JViewport) cont).scrollRectToVisible(rec);
                }
            }
            node.getTree().setSettingNode(this, false); // this tells OATree not to respond to valueChange
        }
    }

    public @Override void afterSort(HubEvent e) {
        onNewList(e);
    }
    public @Override void onNewList(HubEvent e) {
        parent.closeChildren();
        ((OATreeModel)node.getTree().getModel()).fireTreeStructureChanged(parent);
    }

}

