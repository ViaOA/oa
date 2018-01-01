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
package com.viaoa.hub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.viaoa.annotation.OAMany;
import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 *  Used by Hub to manage listeners.
 *  
 *  Hub listeners are added to an array, and a tree is created for the dependent propertyPaths (if any are used, ex: calc props).
 *  If one of the dependent propertyPath is changed, then a afterPropertyChange is sent for the listener propery.
 *  
 */
public class HubListenerTree {
    private static Logger LOG = Logger.getLogger(HubListenerTree.class.getName());
    
    private volatile HubListener[] listeners; 
    private final HubListenerTreeNode root = new HubListenerTreeNode();

    private class HubListenerTreeNode {
        Hub hub;
        String property;
        HubMerger hubMerger;
        HubListenerTreeNode[] children;
        HubListenerTreeNode parent;
        HashMap<HubListener, HubListener[]> hmListener;  // list of HubListeners created for a HubListener
        private OALinkInfo liReverse;
        private ArrayList<String> alCalcPropertyNames;
        
        public ArrayList<String> getCalcPropertyNames() {
            if (alCalcPropertyNames == null) alCalcPropertyNames = new ArrayList<String>(3);
            return alCalcPropertyNames;
        }
        
        // when an object is removed from a hub, the parent property reference could already be null.
        //    this will use the masterObject in the hub.
        //   note: if an object is deleted, it is done on the server and the removed object's parent reference will be null during the remove.
        Object lastRemoveObject;  // object from last hub.remove event
        Object lastRemoveMasterObject;  // master object from last hub.remove event
        
        /*
         *  This allows getting all of the root objects that need to be notified when a change is made to an object "down" the tree from it.
        */
        Object[] getRootValues(Object obj) {
            // 20171212 reworked to include option to use a finder
            long ts = System.currentTimeMillis();//qqqq
            
            String spp = null;
            HubListenerTreeNode tn = this;
            for ( ;tn != null; ) {
                if (tn.property != null) {
                    if (spp == null) spp = tn.property;
                    else spp = tn.property + "." + spp;
                }
                tn = tn.parent;
            }

            boolean bUseOrig = true;
            if (spp != null) {
                OAPropertyPath pp = new OAPropertyPath(HubListenerTree.this.root.hub.getObjectClass(), spp);
                OALinkInfo[] lis = pp.getLinkInfos();
                if (lis != null && lis.length>0) {
                    bUseOrig = false;
                    for (OALinkInfo li : lis) {
                        if (li.getReverseLinkInfo().getType() == OALinkInfo.TYPE_ONE) {
                            bUseOrig = true;
                            break;
                        }
                    }
                }
            }
            
            Object[] objs;
            if (bUseOrig) objs = getRootValues_ORIG(obj, (spp != null));
            else objs = null;
            
            if (objs == null && spp != null) {
                OAFinder finder = new OAFinder();
                finder.addEqualFilter(spp, obj);
                ArrayList al = finder.find(HubListenerTree.this.root.hub);
                objs = new Object[al.size()];
                al.toArray(objs);
            }

            long ts2 = System.currentTimeMillis();//qqqqqqqqq
            if ((ts2 - ts) > 1000) {  //qqqqqqqq should not happen, can be removed
                OAPerformance.LOG.warning("fyi: getRootValues took "+(ts2-ts)+"ms, rootHub="+HubListenerTree.this.root.hub+", propPath="+spp);
            }
            return objs;
        }
        
        Object[] getRootValues_ORIG(Object obj, boolean bCanQuit) {
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
                    newObjs[j++] = objs[i];
                }
            }
            return newObjs;
        }
        
        
        private Object[] getRootValues(Object[] objs) {
            if (parent == null) return objs; // reached the root
            if (objs == null) return new Object[0];  

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
                        // 20160805  
                        if (root.hubMerger != null && !root.hubMerger.getUseAll()) {
                            Object objx = root.hubMerger.getRootHub().getAO();
                            if (objx != null && alNewObjects.indexOf(objx) < 0) alNewObjects.add(objx);
                        }
                        else {
                            for (Object objx : ((Hub) value)) {
                                if (alNewObjects.indexOf(objx) < 0) {
                                    alNewObjects.add(objx);
                                }
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
        root.hub = hub;
    }
    
    public HubListener[] getHubListeners() {
        return this.listeners;
    }

    // testing
    // public static HashMap<HubListener, StackTraceElement[]> hmAll = new HashMap<HubListener, StackTraceElement[]>();    
    public static volatile int ListenerCount;
    
    private volatile int lastCount; // number of listeners that are set as Last.
    public void addListener(HubListener hl) {
        if (hl == null) return;

        // testing
        ListenerCount++;    
        //if (ListenerCount%100==0)
        //        System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+", hl="+hl);
        //System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+", AutoSequenceHubListenerCount="+HubAutoSequence.AutoSequenceHubListenerCount+" ==>"+hl);
        //System.out.println("HubListenerTree.addListener, ListenerCount="+ListenerCount+" ==>"+hl+", hm.hl.cnt="+HubMerger.HubMergerHubListenerCount);
        // System.out.println("HubListenerTree.addListener() ListenerCount="+(ListenerCount));        
        // StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        // hmAll.put(hl, stes);        

        synchronized (root) {
            HubListener.InsertLocation loc = hl.getLocation();
            if (listeners == null || listeners.length==0 || loc == HubListener.InsertLocation.LAST || (loc == null && lastCount==0)) {
                if (loc == HubListener.InsertLocation.LAST) lastCount++;
                if (listeners == null || OAArray.indexOf(listeners, hl) < 0) {
                    listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
                }
            }
            else if (loc == HubListener.InsertLocation.FIRST) {
                listeners = (HubListener []) OAArray.removeValue(HubListener.class, listeners, hl);
                listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, 0);
            }
            else {
                // insert before first last
                boolean b = false;
                for (int i=listeners.length-1; i>=0; i--) {
                    if (listeners[i].getLocation() != HubListener.InsertLocation.LAST) {
                        if (OAArray.indexOf(listeners, hl) < 0) {
                            listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, i+1);
                        }
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    if (listeners == null || OAArray.indexOf(listeners, hl) < 0) {
                        listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
                    }
                }
            }
            if (listeners.length % 50 == 0) {
                LOG.fine("HubListenerTree.listeners.size()=" +listeners.length+", hub="+(root==null?"null":root.hub));
            }
        }
    }   
    
    
    /**
     * Used by Hub to store HubListers and dependent calcProperties
     */
    public void addListener(HubListener hl, String property) {
        if (hl == null) return;
        this.addListener(hl, property, false);
    }
    public void addListener(HubListener hl, String property, boolean bActiveObjectOnly) {
        if (hl == null) return;
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
        if (calcProps == null) {
            for (OALinkInfo li : oi.getLinkInfos()) {
                if (li.getName().equalsIgnoreCase(property)) {
                    // System.out.println(">>>> "+property);
                    calcProps = li.getDependentProperties();
                    property = li.getName();
                    break;
                }
            }
        }
        addListenerMain(hl, property, calcProps, bActiveObjectOnly, false);
    }

    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths) {
        if (hl == null) return;
        addListener(hl, property, dependentPropertyPaths, false);
    }    
    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly) {
        addListener(hl, property, dependentPropertyPaths, bActiveObjectOnly, false);
    }

    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly, boolean bAllowBackgroundThread) {
        if (hl == null) return;
        try {
            OAThreadLocalDelegate.setHubListenerTree(true);
            addListener(hl, property, bActiveObjectOnly); // this will check for dependent calcProps
            // now add the additional dependent properties
            if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
                addDependentListeners(property, hl, dependentPropertyPaths, bActiveObjectOnly, bAllowBackgroundThread);
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
    private void addListenerMain(HubListener hl, final String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly, final boolean bAllowBackgroundThread) {
        try {
            OAThreadLocalDelegate.setHubListenerTree(true);
            this.addListener(hl);
            if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
                addDependentListeners(property, hl, dependentPropertyPaths, bActiveObjectOnly, bAllowBackgroundThread);
            }
        }
        finally {
            OAThreadLocalDelegate.setHubListenerTree(false);
            OAThreadLocalDelegate.setIgnoreTreeListenerProperty(null);
        }
    }    
    private void addDependentListeners(final String origPropertyName, final HubListener origHubListener, final String[] dependentPropertyNames, final boolean bActiveObjectOnly, final boolean bAllowBackgroundThread) {
        //LOG.finer("Hub="+root.hub+", property="+origPropertyName);

        // 20120826 check for endless loops
        if (OAThreadLocalDelegate.getHubListenerTreeCount() > 25) {
            // need to bail out, before stackoverflow
            LOG.warning("OAThreadLocalDelegate.getHubListenerTreeCount() > 25, will not continue to add listeners. PropertyName="+origPropertyName);
            return;
        }
        
        String ignore = OAThreadLocalDelegate.getIgnoreTreeListenerProperty();
        for (int i=0; i < dependentPropertyNames.length ; i++) {
            if (dependentPropertyNames[i] == null) continue;
            if (dependentPropertyNames[i].length() == 0) continue;
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

            final String dependPropName = dependentPropertyNames[i];
            final OAPropertyPath oaPropPath = new OAPropertyPath(dependPropName);
            try {
                String error = oaPropPath.setup(hub, hub.getObjectClass(), false);
                if (error != null) {
                    if (oaPropPath.getNeedsDataToVerify()) {
                        // 20150715 propPath is using generics and will have to be retried once data is in it.
                        //    this will now set up a listener to try again
                        final HubListener hl = new HubListenerAdapter() {
                            public void afterAdd(HubEvent e) {
                                update();
                            }
                            public void afterInsert(HubEvent e) {
                                update();
                            }
                            public void onNewList(HubEvent e) {
                                Hub h = e.getHub();
                                if (h != null && h.size() > 0) update();
                            }
                            void update() {
                                try {
                                    removeListener(this);
                                    addDependentListeners(origPropertyName, origHubListener, new String[] {dependPropName}, bActiveObjectOnly, bAllowBackgroundThread);
                                }
                                catch (Exception e) {
                                    return;
                                }
                            }
                        };
                        this.addListener(hl);
                        continue;
                    }
                }
            }
            catch (Exception e) {
                String s = ("cant find dependent prop, hub="+hub+", prop="+origPropertyName+", dependendProp="+dependentPropertyNames[i]);
                LOG.warning(s);
                throw new RuntimeException(s);
            }
            if (oaPropPath.hasPrivateLink()) {
                String s = ("propPath has private method, hub="+hub+", prop="+origPropertyName+", dependendProp="+dependentPropertyNames[i]);
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
                boolean bIsHub = false;
                
                if (OAObject.class.isAssignableFrom(returnClass)) {
                    if (j == pps.length-1) hubClass = null; 
                    else hubClass = classes[j];
                }
                else if (Hub.class.isAssignableFrom(returnClass)) {
                    bIsHub = true;
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

                final boolean bUseAll = (!bActiveObjectOnly || j>0);
                
                String ppx = "";
                for (int k=0; k<=j; k++) {
                    if (ppx.length() > 0) ppx += ".";
                    ppx += pps[k];
                }
                final String ppFromRoot = ppx;
                
                if (hubClass != null) {
                    boolean b = false;
                    for (int k=0; node.children != null && k < node.children.length; k++) { 
                        HubListenerTreeNode child = node.children[k];
                        if (property.equalsIgnoreCase(child.property)) {
                            if (j==0 && (node.hubMerger != null) && (!bActiveObjectOnly != node.hubMerger.getUseAll())) {
                                continue; 
                            }
                            node = child;
                            b = true;
                            break;
                        }
                    }

                    if (b) {
                        if (node.getCalcPropertyNames().indexOf(origPropertyName) < 0) {
                            node.getCalcPropertyNames().add(origPropertyName);
                        }
                        if (!bAllowBackgroundThread) node.hubMerger.setUseBackgroundThread(false);
                    }
                    else {
                        //LOG.finer("creating hubMerger");
                        final HubListenerTreeNode newTreeNode = new HubListenerTreeNode();
                        newTreeNode.parent = node;
                        newTreeNode.property = property;
                        newTreeNode.hub = new Hub(hubClass);
                        newTreeNode.getCalcPropertyNames().add(origPropertyName);

                        String spp = "(" + hubClass.getName() + ")" + property;
                        
                        if (bIsHub) {
                            final HubListenerTreeNode nodeThis = node;
                            OAPerformance.LOG.finer("creating hubMerger for hub="+hub+", propPath="+spp);
                            
                            newTreeNode.hubMerger = new HubMerger(hub, newTreeNode.hub, spp, true, bUseAll) {
                                @Override
                                protected void setGetDetailHub() {
                                    // 20171230
                                    OAThreadLocalDelegate.setGetDetailHub(HubListenerTree.this.root.hub, ppFromRoot);
                                }
                                
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
                                @Override
                                protected void afterRemoveAllRealHub(HubEvent e) {
                                    super.afterRemoveAllRealHub(e);
                                    onEvent(e);
                                }
                                private void onEvent(HubEvent e) {
                                    if (nodeThis == root) {
                                        for (String s : newTreeNode.getCalcPropertyNames()) {
                                            HubEventDelegate.fireCalcPropertyChange(root.hub, e.getHub().getMasterObject(), s);
                                        }
                                    }
                                    else {
                                        if (bUseAll) {
                                            Object[] rootObjects = nodeThis.getRootValues(e.getHub().getMasterObject());
                                            if (rootObjects != null && rootObjects.length > 0) {
                                                for (Object obj : rootObjects) {
                                                    for (String s : newTreeNode.getCalcPropertyNames()) {
                                                        HubEventDelegate.fireCalcPropertyChange(root.hub, obj, s);
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            Object aObj = root.hub.getAO(); 
                                            if (aObj != null) {
                                                for (String s : newTreeNode.getCalcPropertyNames()) {
                                                    HubEventDelegate.fireCalcPropertyChange(root.hub, aObj, s);
                                                }
                                            }
                                        }
                                    }
                                }
                            };
                            newTreeNode.hubMerger.setUseBackgroundThread(bAllowBackgroundThread);
                        }
                        else {
                            if (OAObject.class.isAssignableFrom(returnClass)) {
                                HubListenerAdapter hl = new HubListenerAdapter() {
                                    @Override
                                    public void afterPropertyChange(HubEvent e) {
                                        if (!property.equalsIgnoreCase(e.getPropertyName())) return;
                                        
                                        if (bUseAll) {
                                            Object[] rootObjects = newTreeNode.parent.getRootValues(e.getObject());
                                            if (rootObjects != null && rootObjects.length > 0) {
                                                for (Object obj : rootObjects) {
                                                    for (String s : newTreeNode.getCalcPropertyNames()) {
                                                        HubEventDelegate.fireCalcPropertyChange(root.hub, obj, s);
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            Object aObj = root.hub.getAO(); 
                                            if (aObj != null) {
                                                for (String s : newTreeNode.getCalcPropertyNames()) {
                                                    HubEventDelegate.fireCalcPropertyChange(root.hub, aObj, s);
                                                }
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
 
                            OAPerformance.LOG.finer("creating hubMerger for hub="+hub+", propPath="+spp);
                            newTreeNode.hubMerger = new HubMerger(hub, newTreeNode.hub, spp, true, bUseAll) {
                                @Override
                                protected void setGetDetailHub() {
                                    OAThreadLocalDelegate.setGetDetailHub(HubListenerTree.this.root.hub, ppFromRoot);
                               }
                            };
                            newTreeNode.hubMerger.setUseBackgroundThread(bAllowBackgroundThread);
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
                                    if (bUseAll) {
                                        onEvent(nodeThis.getRootValues(e.getObject()));
                                    }
                                    else {
                                        onEvent(new Object[] {root.hub.getAO()});
                                    }
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
                                    if (bUseAll) {
                                        onEvent(nodeThis.getRootValues(e.getObject()));
                                    }
                                    else {
                                        onEvent(new Object[] {root.hub.getAO()});
                                    }
                                }
                            }
                            @Override // 20140423
                            public void afterRemoveAll(HubEvent e) {
                                if (!OAThreadLocalDelegate.isHubMergerChanging()) {
                                    HubEventDelegate.fireCalcPropertyChange(root.hub, null, origPropertyName);
                                }
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
                                
                                if (bUseAll) {
                                    Object[] rootObjects = nodeThis.getRootValues(e.getObject());
                                    if (rootObjects != null) {
                                        for (Object obj : rootObjects) {
                                            HubEventDelegate.fireCalcPropertyChange(root.hub, obj, origPropertyName);
                                        }
                                    }
                                }
                                else {
                                    Object aObj = root.hub.getAO();
                                    if (aObj != null) {
                                        HubEventDelegate.fireCalcPropertyChange(root.hub, aObj, origPropertyName);
                                    }
                                }
                            }                
                        }
                    }; 
                    hub.addHubListener(hl, property, bActiveObjectOnly);  // note: property could be another calc-property

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
        removeListener(hl);
    }
    public void removeListener(HubListener hl) {
        if (hl == null) return;
        // testing
        // hmAll.remove(hl);        
        //LOG.finer("Hub="+thisHub);
        synchronized (root) {
            HubListener[] hold = listeners; 
            listeners = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, hl);
            if (hold == listeners) {
                return;
            }
            --ListenerCount;
            if (hl.getLocation() == HubListener.InsertLocation.LAST) lastCount--;
//System.out.println("HubListenerTree.removeListener, ListenerCount="+ListenerCount+", hl="+hl);
        }
        //System.out.println("HubListenerTree.removeListener, ListenerCount="+ListenerCount+" ==>"+hl+", hm.hl.cnt="+HubMerger.HubMergerHubListenerCount);

        removeChildrenListeners(this.root, hl);
    }    
    
    private void removeChildrenListeners(final HubListenerTreeNode node, final HubListener origHubListener) {

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
                    if (childNode.hubMerger != null) childNode.hubMerger.close();
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

