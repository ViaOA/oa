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
package com.viaoa.jfc.tree;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

import com.viaoa.jfc.*;
import com.viaoa.object.OAThreadLocalDelegate;


public class OATreeModel implements TreeModel {

    OATree tree;
    OATreeNodeData root;
    boolean bMessage;  // flag to know if an event is currently being called
    TreeModelListener[] listeners = new TreeModelListener[0];
    private final Object lockListeners = new Object();

    public OATreeModel(OATree tree) {
        this.tree = tree;
        root = new OATreeNodeData(tree.getRoot(), null, null);
    }

    public void addTreeModelListener(TreeModelListener l) {
    	if (l == null) return;
        synchronized (lockListeners) {
            for (int i=0; i < listeners.length; i++) {
                if (listeners[i] == l) return;
            }
            TreeModelListener[] l2 = new TreeModelListener[listeners.length+1];
            if (listeners.length > 0) System.arraycopy(listeners, 0, l2, 0, listeners.length);
            l2[listeners.length] = l;
            listeners = l2;
        }
    }
    public void removeTreeModelListener(TreeModelListener l) {
        if (l == null || listeners.length == 0) return;
        synchronized (lockListeners) {
            TreeModelListener[] l2 = new TreeModelListener[listeners.length-1];
            for (int i=0, j=0; i < listeners.length; i++) {
                if (listeners[i] != l) {
                    if (j == l2.length) return; // not found
                    l2[j++] = listeners[i];
                }
            }
            listeners = l2;
        }
    }


    public void fireTreeStructureChanged() {
        if (tree.isReady(true)) {
            fireTreeStructureChanged(root);
        }
    }

    protected Object[] getFullPath(OATreeNodeData node) {
        Vector v = new Vector(5,5);
        for (;node != null;) {
            v.addElement(node);
            node = node.parent;
        }
        int x = v.size();
        Object[] obj = new Object[x];
        for (int i=0; x > 0 ;i++) {
            obj[i] = v.elementAt(--x);
        }
        return obj;
    }



    class Invoker {
        static final int CHANGETREE = 0;
        static final int INSERT = 1;
        static final int REMOVE = 2;
        static final int CHANGENODE = 3;
        static final int MOVED = 4;
        int type;
        OATreeNodeData node;
        int pos, pos2;
        Object obj;
        public Invoker(int type, OATreeNodeData node, int pos, int pos2, Object obj) {
            this.type = type;
            this.node = node;
            this.pos = pos;
            this.pos2 = pos2;
            this.obj = obj;
        }
    }


    // Need to stack (FIFO) events to be processed in order. Otherwise, some events happen in the middle of processing

    boolean bInvoking;
    Vector vecInvoke = new Vector(5,5);
    Object invokeLock = new Object();

    protected void invoke(int type, OATreeNodeData node, int pos, Object object) {
        this.invoke(type, node, pos, 0, object);
    }
    protected void invoke(int type, OATreeNodeData node, int pos, int pos2) {
        this.invoke(type, node, pos, pos2, null);
    }
    
    protected void invoke(int type, OATreeNodeData node, int pos, int pos2, Object object) {
        synchronized (invokeLock) {
            vecInvoke.addElement(new Invoker(type, node, pos, pos2, object));
            if (bInvoking) return;
            bInvoking = true;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            try {
                doInvoke();
            }
            catch (Throwable e) {
                fireTreeStructureChanged();
            }
        }
        else {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        doInvoke();
                    }
                    catch (Throwable e) {
                        fireTreeStructureChanged();
                    }
                }
            };
            if (OAThreadLocalDelegate.isLoading()) {
                 SwingUtilities.invokeLater(r);
            }
            else {
                try {
                    SwingUtilities.invokeAndWait(r);
                } 
                catch (Exception e) {}
            }
        }
    }

    protected void doInvoke() {
        for (;;) {
            Invoker invoker;
            synchronized (invokeLock) {
                if (vecInvoke.size() == 0) {
                    bInvoking = false;
                    break;
                }
                invoker = (Invoker) vecInvoke.elementAt(0);
                vecInvoke.removeElementAt(0);
            }

            Object[] obj = getFullPath(invoker.node);

            try {
                switch (invoker.type) {
                    case Invoker.CHANGETREE:
                        TreeModelEvent e = new TreeModelEvent(tree, obj);
                        for (int i=0; i < listeners.length; i++) {
                            listeners[i].treeStructureChanged(e);
                        }
                        break;
                    case Invoker.INSERT:
                        Object child = invoker.node.getChild(invoker.pos);
                        if (child == null) break;
                        OATreeNodeData data = (OATreeNodeData) child;
                        e = new TreeModelEvent(tree,obj, new int[]{invoker.pos}, new Object[] {child} );
                        for (int i=0; i < listeners.length; i++) {
                            listeners[i].treeNodesInserted(e);
                        }
                        break;
                    case Invoker.REMOVE:
                        e = new TreeModelEvent(tree,obj, new int[]{invoker.pos}, new Object[] {invoker.node} );
                        for (int i=0; i < listeners.length; i++) {
                            listeners[i].treeNodesRemoved(e);
                        }
                        break;
                    case Invoker.CHANGENODE:
                        child = invoker.node.getChild(invoker.pos);
                        if (child == null) break;
                        e = new TreeModelEvent(tree,obj, new int[]{invoker.pos}, new Object[] {child} );
                        for (int i=0; i < listeners.length; i++) {
                            listeners[i].treeNodesChanged(e);
                        }
                        break;

                    case Invoker.MOVED:
                        // 20101004 changed from sending structure changed, to remove+insert
                        OATreeNodeData ndChild = invoker.node.getChild(invoker.pos);                        
                        
                    	// was:
                        // this will "tell" the parent that it's children need restructured
                        // e = new TreeModelEvent(tree, obj);
                        
                        e = new TreeModelEvent(tree, obj, new int[]{invoker.pos}, new OATreeNodeData[]{ndChild});
                        for (int i=0; i < listeners.length; i++) {
                            // was: listeners[i].treeStructureChanged(e);
                            listeners[i].treeNodesRemoved(e);
                        }

                        e = new TreeModelEvent(tree, obj, new int[]{invoker.pos2}, new OATreeNodeData[]{ndChild});
                        for (int i=0; i < listeners.length; i++) {
                            listeners[i].treeNodesInserted(e);
                        }
                        break;
                }
            }
            catch (Exception ex) {
                System.out.println("OATree.doInvoke #"+invoker.type+"  "+ex);
                ex.printStackTrace();
                fireTreeStructureChanged();
            }
        }
    }

    public void fireTreeStructureChanged(OATreeNodeData node) {
    	if (node != null) node.close();
        if (!tree.isReady(true)) return;
        invoke(Invoker.CHANGETREE, node, 0, null);
    }

    public void fireTreeStructureChanged(OATreeNode node) {
        if (!tree.isReady(true)) return;
        root.close(node);
        invoke(Invoker.CHANGETREE, root, 0, null);
    }
    
    
    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent,int index) {
        OATreeNodeData data = ((OATreeNodeData) parent).getChild(index);
        return data;
    }

    public int getChildCount(Object parent) {
        int x = ((OATreeNodeData) parent).getChildCount();
        return x;
    }

    public int getIndexOfChild(Object parent,Object child) {
        int x = ((OATreeNodeData)parent).getChildIndex((OATreeNodeData)child);
        return x;
    }

    public boolean isLeaf(Object node) {
        if (node == null) return false;
        OATreeNodeData tnd = (OATreeNodeData) node;
        return tnd.isLeaf();
    }
    public void valueForPathChanged(TreePath path,Object newValue) {
    }

    public void insertNode(OATreeNodeData parent, int pos, Object obj) {
        if (bMessage) return;
        bMessage = true;
        try {
            if (!tree.isReady(true)) return;
            invoke(Invoker.INSERT, parent, pos, obj);
        }
        finally {
            bMessage = false;
        }
    }


    public void removeNode(OATreeNodeData parent, int pos, Object obj) {
        if (!tree.isReady(true)) return;
        if (bMessage) return;
        bMessage = true;
        try {
            invoke(Invoker.REMOVE, parent, pos, obj);
        }
        finally {
            bMessage = false;
        }
    }

    public void movedNode(OATreeNodeData parent, int pos, int pos2) {
        if (!tree.isReady(true)) return;
        if (bMessage) return;
        bMessage = true;
        try {
            invoke(Invoker.MOVED, parent, pos, pos2);
        }
        finally {
            bMessage = false;
        }
    }

    public void changedNode(OATreeNodeData parent, int pos) {
        if (!tree.isReady(true)) return;
        if (bMessage) return;
        bMessage = true;
        try {
            invoke(Invoker.CHANGENODE, parent, pos, null);
        }
        finally {
            bMessage = false;
        }
    }
	
	
}
