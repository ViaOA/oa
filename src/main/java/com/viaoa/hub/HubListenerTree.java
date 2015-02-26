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
package com.viaoa.hub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.annotation.OAMany;
import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAPropertyPath;


/**
 *  Used by Hub to manage listeners.
 *  Hub listeners are added to an array, and a tree is created for the dependent propertyPaths (if any are used, ex: calc props).
 *  If one of the dependent propertyPath is changed, then a afterPropertyChange is sent for the listener propery.
 * 
 */
public class HubListenerTree {
    private static Logger LOG = Logger.getLogger(HubListenerTree.class.getName());
    
    private HubListener[] listeners; 
    private HubListenerTreeNode root;

    
    // this is used for large amounts of listeners for a single HubListenerTree, 
    //   so that the HubListener[] does not always grow by one on each add.
    private static HubListenerTree hugeListener;
    private static ArrayList<HubListener> alHuge;
    
    private class HubListenerTreeNode {
        Hub hub;
        String property;
        HubMerger hubMerger;
        HubListenerTreeNode[] children;
        HubListenerTreeNode parent;
        HashMap<HubListener, HubListener[]> hmListener;  // list of HubListeners created for a HubListener
        private OALinkInfo liReverse;
        
        
        // when an object is removed from a hub, the parent property reference could already be null.
        //    this will use the masterObject in the hub.
        //   note: if an object is deleted, it is done on the server and the removed object's parent reference will be null during the remove.
        Object lastRemoveObject;  // object from last hub.remove event
        Object lastRemoveMasterObject;  // master object from last hub.remove event
        
        /*
         *  This allows getting all of the root objects that need to be notified when a change is made.
        */
        Object[] getRootValues(Object obj) {
            if (obj == null) return new Object[0];
            Object[] objs = getRootValues(new Object[] {obj});

            // now make sure that all of the values are in the root.hub
            int cnt = 0;
            for (int i=0; objs != null && i<objs.length; i++) {
                if (!root.hub.contains(objs[i])) objs[i] = null;
                else cnt++;
            }
            if (cnt == 0) return null;
            
            if (cnt == objs.length) return objs;
            
            Object[] newObjs = new Object[cnt];
            int j = 0;
            for (int i=0; i<objs.length; i++) {
                if (objs[i] != null) {
                    newObjs[j] = objs[i];
                }
            }
            return newObjs;
        }
        
        
        private Object[] getRootValues(Object[] objs) {
            if (parent == null) return objs; // reached the root
            if (objs == null) return null;  

            if (liReverse == null) {
                Class c = parent.hub.getObjectClass();
                OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(c, property);
                liReverse = OAObjectInfoDelegate.getReverseLinkInfo(li);
            }
            
            ArrayList<Object> alNewObjects = new ArrayList<Object>();
            
            Method m = null;
            for (Object obj : objs) {
                OAObject oaObj = (OAObject) obj;
                
                String propName = null;
                if (liReverse != null) {
                    propName = liReverse.getName();
                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
                    m = OAObjectInfoDelegate.getMethod(oi, "get"+propName, 0);
                }

                if (oaObj == lastRemoveObject && lastRemoveMasterObject != null) {
                    // from a remove
                    if (alNewObjects.indexOf(lastRemoveMasterObject) < 0) {
                        alNewObjects.add(lastRemoveMasterObject);
                    }
                    lastRemoveObject = null;
                }
                else if (m == null) {
                    // method might not exist (or is private - from a reference that is not made accessible)
                    // need to go up to parent to find all objects that have a reference to "obj"
                    
                    for (Object objx : parent.hub) {
                        Object objz = OAObjectReflectDelegate.getProperty((OAObject) objx, this.property);
                        if (objz == obj || lastRemoveObject == obj) {
                            // found a parent object that has a reference to child
                            if (alNewObjects.indexOf(objx) < 0) {
                                alNewObjects.add(objx);
                            }
                        }
                        else if (objz instanceof Hub) {
                            if (((Hub)objz).contains(obj)) {
                                // found a parent object that has a reference to child
                                if (alNewObjects.indexOf(objx) < 0) {
                                    alNewObjects.add(objx);
                                }
                            }
                        }
                    }
                }
                else {
                    Object value = null;
                    try {
                        value = m.invoke(oaObj, null);
                    }
                    catch (Exception e) {
                        LOG.log(Level.FINE, "error calling "+oaObj.getClass().getName()+".getProperty(\""+propName+"\")", e);
                    }
                    
                    if (value instanceof Hub) {
                        for (Object objx : ((Hub) value)) {
                            if (alNewObjects.indexOf(objx) < 0) {
                                alNewObjects.add(objx);
                            }
                        }
                    }
                    else {
                        if (value != null) {
                            if (alNewObjects.indexOf(value) < 0) {
                                alNewObjects.add(value);
                            }
                        }
                    }
                }                
            }
            objs = alNewObjects.toArray();
            objs = parent.getRootValues(objs);
            
            return objs;
        }
    }
    
    public HubListenerTree(Hub hub) {
        root = new HubListenerTreeNode();    
        root.hub = hub;
    }
    
    private void removeHugeListener() {
        if (hugeListener == this) {
            synchronized (root) {
                if (hugeListener == this) {
                    listeners = alHuge.toArray(new HubListener[0]);
                    hugeListener = null;
                    alHuge = null;
                    //LOG.fine("removed hugeListener array, size="+listeners.length);
                }
            }
        }
    }
    
    public HubListener[] getHubListeners() {
        removeHugeListener();
        return this.listeners;
    }

    // testing
    // public static HashMap<HubListener, StackTraceElement[]> hmAll = new HashMap<HubListener, StackTraceElement[]>();    
    public static int ListenerCount;
    
    private int lastCount; // number of listeners that are set as Last.
    public void addListener(HubListener hl) {
        // testing
        ListenerCount++;    

//if (ListenerCount%100==0)
//        System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+", hl="+hl);
//System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+", AutoSequenceHubListenerCount="+HubAutoSequence.AutoSequenceHubListenerCount+" ==>"+hl);
//System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+" ==>"+hl+", hm.hl.cnt="+HubMerger.HubMergerHubListenerCount);
        
        if (listeners != null && listeners.length > 70) {
            if (listeners.length % 70 == 0) {
                //LOG.fine("HubListenerTree.listeners.size()=" +listeners.length+", hub="+(root==null?"null":root.hub));
            }
        }
        // System.out.println("HubListenerTree.addListener() ListenerCount="+(ListenerCount));        
        // StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        // hmAll.put(hl, stes);        

        synchronized (root) {
            HubListener.InsertLocation loc = hl.getLocation();

            if (listeners == null || listeners.length==0 || loc == HubListener.InsertLocation.LAST || (loc == null && lastCount==0)) {
                if (listeners != null && hugeListener == null && listeners.length > 70) {
                    hugeListener = this;
                    alHuge = new ArrayList<HubListener>(250);
                    alHuge.addAll(Arrays.asList(listeners));
                    listeners = null;
                    //LOG.fine("Using hugeListener, size="+alHuge.size());
                }

                if (loc == HubListener.InsertLocation.LAST) lastCount++;
                if (hugeListener == this) {
                    alHuge.add(hl);
                }
                else {
                    listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
                }
            }
            else if (loc == HubListener.InsertLocation.FIRST) {
                removeHugeListener();
                listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, 0);
            }
            else {
                // insert before first last
                removeHugeListener();
                boolean b = false;
                
                for (int i=listeners.length-1; i<=0; i--) {
                    if (listeners[i].getLocation() != HubListener.InsertLocation.LAST) {
                        listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, i+1);
                        b = true;
                        break;
                    }
                }
                
                if (!b) {
                    listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
                }
            }
        }
    }   
    
    
    /**
     * Used by Hub to store HubListers and dependent calcProperties
     */
    public void addListener(HubListener hl, String property) {
        this.addListener(hl, property, false);
    }
    public void addListener(HubListener hl, String property, boolean bActiveObjectOnly) {
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(root.hub.getObjectClass());
        String[] calcProps = null;
        for (OACalcInfo ci : oi.getCalcInfos()) {
            if (ci.getName().equalsIgnoreCase(property)) {
                // System.out.println(">>>> "+property);
                calcProps = ci.getProperties();
                property = ci.getName();
                break;
            }
        }       
        addListenerMain(hl, property, calcProps, bActiveObjectOnly);
    }

    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths) {
        addListener(hl, property, dependentPropertyPaths, false);
    }    

    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly) {
        try {
            OAThreadLocalDelegate.setHubListenerTree(true);
            addListener(hl, property); // this will check for dependent calcProps
            // now add the additional dependent properties
            if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
                addDependentListeners(property, hl, dependentPropertyPaths, bActiveObjectOnly);
            }
        }
        finally {
            OAThreadLocalDelegate.setHubListenerTree(false);
            OAThreadLocalDelegate.setIgnoreTreeListenerProperty(null);
        }
    }    
    
    /**
     * @param dependentPropertyPaths
     * @param bActiveObjectOnly if true, then dependent props only listen to the hub's AO
     */
    private void addListenerMain(HubListener hl, final String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly) {
        try {
            OAThreadLocalDelegate.setHubListenerTree(true);
            this.addListener(hl);
            if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
                addDependentListeners(property, hl, dependentPropertyPaths, bActiveObjectOnly);
            }
        }
        finally {
            OAThreadLocalDelegate.setHubListenerTree(false);
            OAThreadLocalDelegate.setIgnoreTreeListenerProperty(null);
        }
    }    
    private void addDependentListeners(final String origPropertyName, HubListener origHubListener, String[] dependentPropertyNames, boolean bActiveObjectOnly) {
        //LOG.finer("Hub="+root.hub+", property="+origPropertyName);

        // 20120826 check for endless loops
        if (OAThreadLocalDelegate.getHubListenerTreeCount() > 50) {
            // need to bail out, before stackoveflow
            LOG.warning("OAThreadLocalDelegate.getHubListenerTreeCount() > 50, will not continue to add listeners");
            return;
        }
        
        String ignore = OAThreadLocalDelegate.getIgnoreTreeListenerProperty();
        for (int i=0; i < dependentPropertyNames.length ; i++) {
            if (dependentPropertyNames[i] == null) continue;
            if (dependentPropertyNames[i].length() == 0) continue;
/* qqqq testing            
if (dependentPropertyNames[i].toUpperCase().indexOf("EMPL") >= 0) {
    System.out.println(" ==> "+dependentPropertyNames[i]+" ............ "+this.root.hub);
}
*/
            //LOG.finer("Hub="+root.hub+", property="+origPropertyName+", dependentProp="+dependentPropertyNames[i]);

            // 20120826 if recursive prop then dont need to listen to more, since a hubMerger is already listening
            if (ignore != null && dependentPropertyNames[i].equalsIgnoreCase(ignore) ) {
                // todo: might want to have a better check.  This will only check to see if a recursive property
                //   has the same dependency.  This might be good enough, since there is also a check (begin of method) for endless loop
                //LOG.fine("ignoring "+dependentPropertyNames[i]+", since it was already being listened to");
                continue;
            }
            if (dependentPropertyNames[i].indexOf('.') > 0) {
                OAThreadLocalDelegate.setIgnoreTreeListenerProperty(dependentPropertyNames[i]);
            }
            
            
            HubListenerTreeNode node = root;
            Hub hub = root.hub;

            // 20120809  
            OAPropertyPath oaPropPath = new OAPropertyPath(dependentPropertyNames[i]);
            try {
                oaPropPath.setup(hub.getObjectClass());
            }
            catch (Exception e) {
                String s = ("cant find dependent prop, hub="+hub+", prop="+origPropertyName+", dependendProp="+dependentPropertyNames[i]);
                LOG.warning(s);
                throw new RuntimeException(s);
            }
            
            String[] pps = oaPropPath.getProperties();
            Method[] methods = oaPropPath.getMethods();
            Class[] classes = oaPropPath.getClasses();
           
            
            for (int j=0; j<pps.length; j++) {
                final String property = pps[j];
                
                Class c = hub.getObjectClass();
                Method m = methods[j];
                Class returnClass = m.getReturnType();
                Class hubClass;
                
                if (OAObject.class.isAssignableFrom(returnClass)) {
                    if (j == pps.length-1) hubClass = null; 
                    else hubClass = classes[j];
                }
                else if (Hub.class.isAssignableFrom(returnClass)) {
                    hubClass = classes[j];
                    if (Hub.class.equals(hubClass)) {
                        OAMany om = m.getAnnotation(OAMany.class);
                        if (om != null) {
                            hubClass = OAAnnotationDelegate.getHubObjectClass(om, m);
                        }
                        else {
                            String s = ("getAnnotation OAMany=null for prop method=get"+property+", hub="+hub+", prop="+origPropertyName+", dependendProp="+dependentPropertyNames[i]);
                            LOG.warning(s);
                            throw new RuntimeException(s);
                        }
                    }
                }
                else {
                    if (j != pps.length-1) {
                        String s = ("expected a reference prop, method=get"+property+", hub="+hub+", prop="+origPropertyName+", dependendProp="+dependentPropertyNames[i]);
                        LOG.warning(s);
                        throw new RuntimeException(s);
                    }
                    hubClass = null;
                }

                if (hubClass != null) {
                    boolean b = false;
                    for (int k=0; node.children != null && k < node.children.length; k++) { 
                        HubListenerTreeNode child = node.children[k];
                        b = property.equalsIgnoreCase(child.property);
                        if (b) {
                            node = child;
                            break;
                        }
                    }
                    if (!b) {
                        //LOG.finer("creating hubMerger");
                        final HubListenerTreeNode newTreeNode = new HubListenerTreeNode();
                        newTreeNode.parent = node;
                        newTreeNode.property = property;
                        newTreeNode.hub = new Hub(hubClass);

                        String spp = "(" + hubClass.getName() + ")" + property;
                        
                        if (j == pps.length-1) {
                            // 20120823 if this is the last hub, then need to listen for each add/remove
                            final HubListenerTreeNode nodeThis = node;
                            newTreeNode.hubMerger = new HubMerger(hub, newTreeNode.hub, spp, true, !bActiveObjectOnly||j>0) {
                                @Override
                                protected void beforeRemoveRealHub(HubEvent e) {
                                    // get the parent reference object from the Hub.masterObject, since the 
                                    //    reference in the object could be null once the remove is done
                                    Hub h = (Hub) e.getSource();
                                    newTreeNode.lastRemoveObject = e.getObject();
                                    newTreeNode.lastRemoveMasterObject = h.getMasterObject();
                                    super.beforeRemoveRealHub(e);
                                }
                                @Override
                                protected void afterAddRealHub(HubEvent e) {
                                    newTreeNode.lastRemoveObject = null; // in case it has not been cleared yet
                                    super.afterAddRealHub(e);
                                    onEvent(e);
                                    
                                }
                                @Override
                                protected void afterRemoveRealHub(HubEvent e) {
                                    super.afterRemoveRealHub(e);
                                    onEvent(e);
                                }
                                private void onEvent(HubEvent e) {
                                    if (nodeThis == root) {
                                        HubEventDelegate.fireCalcPropertyChange(root.hub, e.getHub().getMasterObject(), origPropertyName);
                                    }
                                    else {
                                        Object[] rootObjects = nodeThis.parent.getRootValues(e.getHub().getMasterObject());
                                        if (rootObjects != null && rootObjects.length > 0) {
                                            HubEventDelegate.fireCalcPropertyChange(root.hub, rootObjects[0], origPropertyName);
                                        }
                                    }
                                }
                            };
                        }
                        else {
                            // 20140527 need to listen to property
                            if (OAObject.class.isAssignableFrom(returnClass)) {
                                HubListenerAdapter hl = new HubListenerAdapter() {
                                    @Override
                                    public void afterPropertyChange(HubEvent e) {
                                        if (!property.equalsIgnoreCase(e.getPropertyName())) return;
                                        HubEventDelegate.fireCalcPropertyChange(root.hub, e.getObject(), origPropertyName);
                                    }
                                };
                                hub.addHubListener(hl);                                
                                // 20150126
                                HubListener[] hls;
                                if (node.hmListener == null) {
                                    node.hmListener = new HashMap<HubListener, HubListener[]>(3, .75f);
                                    hls = null;
                                }
                                else {
                                    hls = node.hmListener.get(origHubListener);
                                }
                                
                                hls = (HubListener[]) OAArray.add(HubListener.class, hls, hl);
                                node.hmListener.put(origHubListener, hls);
                            }
                            newTreeNode.hubMerger = new HubMerger(hub, newTreeNode.hub, spp, true, !bActiveObjectOnly||j>0) {
                                @Override
                                protected void beforeRemoveRealHub(HubEvent e) {
                                    // get the parent reference object from the Hub.masterObject, since the 
                                    //    reference in the object could be null once the remove is done
                                    Hub h = (Hub) e.getSource();
                                    newTreeNode.lastRemoveObject = e.getObject();
                                    newTreeNode.lastRemoveMasterObject = h.getMasterObject();
                                    super.beforeRemoveRealHub(e);
                                }
                                @Override
                                protected void afterAddRealHub(HubEvent e) {
                                    newTreeNode.lastRemoveObject = null; // in case it is not cleared
                                    super.afterAddRealHub(e);
                                }
                            };
                        }
                        
                        node.children = (HubListenerTreeNode[]) OAArray.add(HubListenerTreeNode.class, node.children, newTreeNode);
                        node = newTreeNode;
                    }
                    hub = node.hub;

                    boolean bx; // might need to have a listener for last hub in propertyPath
                    
                    if (j == pps.length-1) {
                        bx = true;
                    }
                    else {
                        bx = false;
                        if (j == pps.length-2) {
                            // need to know if the last property is oaObj or Hub.  If not, then create a listener on this node
                            Class cx = hub.getObjectClass();
                            Method mx = methods[j+1];
                            if (mx != null) {
                                cx = mx.getReturnType();
                                if (cx == null || (!OAObject.class.isAssignableFrom(cx) && !Hub.class.isAssignableFrom(cx))) {
                                    bx = true;
                                }
                            }
                        }
                    }
                    
                    if (bx) {
                        HubListener hl;
                        final HubListenerTreeNode nodeThis = node;
                        //LOG.finer("creating dependent prop hubListner for Hub");
                        hl = new HubListenerAdapter() {
                            @Override
                            public void afterAdd(HubEvent e) {
                                nodeThis.lastRemoveObject = null; // in case it was not cleared
                                if (!OAThreadLocalDelegate.isHubMergerChanging()) {
                                    Hub h = HubListenerTree.this.root.hub;
                                    onEvent(nodeThis.getRootValues(e.getObject()));
                                }
                            }
                            @Override
                            public void afterPropertyChange(HubEvent e) {
                                nodeThis.lastRemoveObject = null; // in case it was not cleared
                            }
                            @Override
                            public void afterInsert(HubEvent e) {
                                afterAdd(e);
                            }
                            @Override
                            public void afterRemove(HubEvent e) {
                                Hub h = HubListenerTree.this.root.hub;
                                // get the parent reference object from the Hub.masterObject, since the 
                                //    reference in the object could be null
                                Hub hubx = (Hub) e.getSource();
                                Object objx = hubx.getMasterObject();
                                if (objx != null) {
                                    nodeThis.lastRemoveObject = e.getObject();
                                    nodeThis.lastRemoveMasterObject = objx;
                                }
                                // ignore if masterHub is adding, removing (newList, clear)                                
                                if (!OAThreadLocalDelegate.isHubMergerChanging()) {
                                    onEvent(nodeThis.getRootValues(e.getObject()));
                                }
                            }
                            @Override // 20140423
                            public void afterRemoveAll(HubEvent e) {
                                HubEventDelegate.fireCalcPropertyChange(root.hub, null, origPropertyName);
                            }
                            private void onEvent(Object[] rootObjects) {
                                if (rootObjects == null) return;
                                for (Object obj :rootObjects) {
                                    if (obj != null) {
                                        HubEventDelegate.fireCalcPropertyChange(root.hub, obj, origPropertyName);
                                    }
                                }
                            }
                        };
                        hub.addHubListener(hl);
    
                        HubListener[] hls;
                        if (node.hmListener == null) {
                            node.hmListener = new HashMap<HubListener, HubListener[]>(3, .75f);
                            hls = null;
                        }
                        else {
                            hls = node.hmListener.get(origHubListener);
                        }
                        
                        hls = (HubListener[]) OAArray.add(HubListener.class, hls, hl);
                        node.hmListener.put(origHubListener, hls);
                    }
                }
                if (j != pps.length-1) continue;

                // Add a hub listener to end of propertyPath
                
                if (hubClass == null) {
                    //LOG.finer("creating dependent prop hubListner, dependProp="+property);
                    final String propx = property;
                    final HubListenerTreeNode nodeThis = node;
                    HubListener hl = new HubListenerAdapter() {
                        @Override
                        public void afterPropertyChange(HubEvent e) {
                            String prop = e.getPropertyName();
                            if (prop == null) return;
                            if (prop.equalsIgnoreCase(propx)) {
                                Object[] rootObjects = nodeThis.getRootValues(e.getObject());
                                if (rootObjects != null) {
                                    for (Object obj : rootObjects) {
                                        HubEventDelegate.fireCalcPropertyChange(root.hub, obj, origPropertyName);
                                    }
                                }
                            }                
                        }
                    }; 
                    hub.addHubListener(hl, property);  // note: property could be another calc-property

                    HubListener[] hls;
                    if (node.hmListener == null) {
                        node.hmListener = new HashMap<HubListener, HubListener[]>(3, .75f);
                        hls = null;
                    }
                    else {
                        hls = node.hmListener.get(origHubListener);
                    }
                    
                    hls = (HubListener[]) OAArray.add(HubListener.class, hls, hl);
                    
                    node.hmListener.put(origHubListener, hls);
                }
                break;
            }
        }
    }




    public void removeListener(Hub thisHub, HubListener hl) {
        // testing
        // hmAll.remove(hl);        
        
        //LOG.finer("Hub="+thisHub);
        removeHugeListener();
        synchronized (root) {
            HubListener[] hold = listeners; 
            listeners = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, hl);
            if (hold == listeners) return;
            --ListenerCount;
//qqqqqqqqqqqqqqq            
//System.out.println("HubListenerTree.removeListener, ListenerCount="+ListenerCount+", hl="+hl);
            
        }
        //System.out.println("HubListenerTree.removeListener, ListenerCount="+ListenerCount+" ==>"+hl+", hm.hl.cnt="+HubMerger.HubMergerHubListenerCount);

        removeChildrenListeners(this.root, hl);
    }    
    
    private void removeChildrenListeners(HubListenerTreeNode node, HubListener origHubListener) {

        if (node.hmListener != null) {
            HubListener[] hls = node.hmListener.remove(origHubListener);
            if (hls != null) {
                //LOG.finer("removing dependentProp listener, name="+node.property);
                for (HubListener hl : hls) {
                    node.hub.removeHubListener(hl);
                }
            }
        }            

        for (int k=0; node.children != null && k < node.children.length; k++) { 
            HubListenerTreeNode childNode = node.children[k];

            removeChildrenListeners(childNode, origHubListener);  // recurse through the treeNodes

            // see if childNode can be removed - which will remove HubMerger
            if (childNode.hmListener == null || childNode.hmListener.size() == 0 ) {
                // remove child
                if (!isUsed(childNode)) {
                    //LOG.finer("removing hubMerger for dependProp, name="+childNode.property);
                    node.children = (HubListenerTreeNode[]) OAArray.removeAt(HubListenerTreeNode.class, node.children, k);
                    childNode.hubMerger.close();
                    k--;
                }
            }
        }
    }    
    
    private boolean isUsed(HubListenerTreeNode node) {
        if (node.hmListener != null && node.hmListener.size() > 0 ) return true;
        if (node.children == null) return false;
        
        for (int k=0; k < node.children.length; k++) {
            if (isUsed(node.children[k])) return true;
        }
        return false;
    }
}

