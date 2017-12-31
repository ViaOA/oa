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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.object.*;
import com.viaoa.util.OAPropertyPath;

/**
 * Used to combine objects from a property path of a root Hub into a single Hub. As any changes are made
 * to any objects included in the property path, the Hub will automatically be updated. Property path
 * can include either type of reference: One or Many.
 * <p>
 * Examples:
 * 
 * <pre>
 * new HubMerger(hubSalesmen, hubOrders, &quot;customers.orders&quot;);
 * 
 * new HubMerger(hubItem, hubForm, &quot;formItem.formSection.formRow.form&quot;);
 * 
 * new HubMerger(hubForm, hubItem, &quot;formRows.formSections.formItems.item&quot;);
 * </pre>
 * 
 * @created 2004/08/20, rewritten 20080804, added recursive links 20120527
 * @see OAPropertyPath for more information about property paths
 */
public class HubMerger<F extends OAObject, T extends OAObject> {
    private static Logger LOG = Logger.getLogger(HubMerger.class.getName());
    public boolean DEBUG;

    /* Programming notes: Node: defines the straight path of nodes. Each node has a child node. Data:
     * used to create a tree of nodes for objects in the hubs. If the property is a type=One then the
     * actual Node will have a temp Hub that is used to store the unique values. Each data has an array
     * of children Data. A new Data is created for each object in the parent Data, until the child.node
     * is null. 20120522 add support for recursive objects in the path */

    private Node nodeRoot; // first node from propertyPath
    private Data dataRoot; // node used for the root Hub

    String propertyPath; // property path
    Hub hubCombined; // Hub that stores the results
    Hub hubRoot; // main hub used as the first hub.
    boolean bShareEndHub; // if true, then hubCombined can be shared with the currently found "single"
                          // Hub
    boolean bShareActiveObject; // if bShareEndHub, then this will set the sharedHub as sharing the AO
    boolean bUseAll; // if false, then only use AO in the rootHub, otherwise all objects will be used.
    boolean bIgnoreIsUsedFlag; // flag to have isUsed() return false;
    private boolean bEnabled = true;
    private boolean bIsRecusive;
    private boolean bIncludeRootHub;
    private boolean bUseBackgroundThread;
    private boolean bLoadingCombined;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public int TotalHubListeners; // for testing only

    public static AtomicInteger aiHubListenerCount = new AtomicInteger(); // number of HubListeners used by all HubMerger

    private boolean bServerSideOnly;

    // used to run onNewList in another thread that can be cancelled
    private final Object lockNewList = new Object();
    private volatile boolean bRunningNewListThread; 
    private volatile boolean bNewListCancel;
    private volatile HubEvent hubEventBackgroundThread;
    
    private final int id;
    private static final AtomicInteger aiId = new AtomicInteger();
    
    
    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, String propertyPath) {
        this(hubRoot, hubCombinedObjects, propertyPath, false, null, true, false, false);
    }
    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, String propertyPath, boolean bUseAll) {
        this(hubRoot, hubCombinedObjects, propertyPath, false, null, bUseAll, false, false);
    }

    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, String propertyPath, boolean bShareActiveObject, boolean bUseAll) {
        this(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, null, bUseAll, false, false);
    }

    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, String propertyPath, boolean bShareActiveObject, String selectOrder,
            boolean bUseAll) {
        this(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, selectOrder, bUseAll, false, false);
    }

    
    public void setUseBackgroundThread(boolean b) {
        bUseBackgroundThread = b;
    }
    public boolean getUseBackgroundThread() {
        return bUseBackgroundThread;
    }
    
    
    
    /**
     * Main constructor that includes all of the config params Used to create an new hubMerger that will
     * automatically update a Hub with all of the objects from a property path from a root Hub.
     * 
     * @param hubRoot
     *            root Hub. The active object of this Hub will be used to get all objects in the
     *            propertyPath.
     * @param hubCombinedObjects
     *            will have all of the objects from the active object of the hubRoot, using
     *            propertyPath. If hubCombinedObjects.getObjectClass() is null, then it will be assigned
     *            the correct class.
     * @param propertyPath
     *            dot seperated property path from the class of rootHub to the class of combinedHub.
     * @param bShareActiveObject
     *            if true then the Active Object from found hub will be shared.
     * @param bUseAll
     *            if true, then each object in hubRoot will be used. If false, then only the Active
     *            Object is used.
     * @param bIncludeRootHub
     *            if the objects in the rootHub should also be included.
     */
    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, 
        String propertyPath, boolean bShareActiveObject, String selectOrder,
        boolean bUseAll, boolean bIncludeRootHub, boolean bUseBackgroundThread) 
    {
        id = aiId.getAndIncrement();
        if (hubRoot == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        LOG.fine("hubRoot="+hubRoot.getObjectClass().getSimpleName()+", propertyPath="+propertyPath);        

        if (hubCombinedObjects == null) {
            // 20150720 allow combinedHub to be null
            //throw new IllegalArgumentException("Combined hub can not be null");
        }
        setUseBackgroundThread(bUseBackgroundThread);
        init(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, selectOrder, bUseAll, bIncludeRootHub);
    }
    
    public HubMerger(Hub<F> hubRoot, Hub<T> hubCombinedObjects, String propertyPath, boolean bShareActiveObject, boolean bUseAll,
            boolean bIncludeRootHub) {
        this(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, null, bUseAll, bIncludeRootHub, false);
    }

    private boolean bCreatedFromOneObject;
    public HubMerger(F obj, Hub<T> hubCombinedObjects, String propertyPath) {
        id = aiId.getAndIncrement();
        bCreatedFromOneObject = true;
        Hub h = new Hub(obj.getClass());
        h.add(obj);
        h.setPos(0);
        init(h, hubCombinedObjects, propertyPath, false, null, true, false);
    }

    /**
     * Number of objects that are used in this merger.
     */
    public int getObjectCount() {
        if (dataRoot == null) return 0;
        return dataRoot.getObjectCount();
    }
    
    /**
     * This needs to be set to true if it is only created on the server, but client applications will be
     * using the same Hub that is filtered. This is so that changes on the hub will be published to the
     * clients, even if initiated on an OAClientThread.
     */
    public void setServerSideOnly(boolean b) {
        bServerSideOnly = b;
    }

    private void init(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bShareActiveObject, String selectOrder,
            boolean bUseAll, boolean bIncludeRootHub) {

        this.hubRoot = hubRoot;
        this.hubCombined = hubCombinedObjects;
        this.propertyPath = propertyPath;
        this.bShareActiveObject = bShareActiveObject;
        this.bUseAll = bUseAll;
        this.bIncludeRootHub = bIncludeRootHub;
        
        long ts = System.currentTimeMillis();

        Hub holdDetailHub = OAThreadLocalDelegate.getGetDetailHub();
        String holdDetailPP = OAThreadLocalDelegate.getGetDetailPropertyPath();
        try {
            // 20120624 hubCombined could be a detail hub.
            OAThreadLocalDelegate.setSuppressCSMessages(true);
            setGetDetailHub();
            //was: OAThreadLocalDelegate.setGetDetailHub(this.hubRoot, this.propertyPath);
            _init();
        }
        finally {
            OAThreadLocalDelegate.setSuppressCSMessages(false);
            OAThreadLocalDelegate.resetGetDetailHub(holdDetailHub, holdDetailPP);
        }
        ts = System.currentTimeMillis() - ts;

        
        String s = ("HM."+id+") new HubMerger hub="+hubRoot+", propertyPath="+propertyPath+", useAll="+bUseAll+", useBackgroundThread="+getUseBackgroundThread());
        s += ", combinedHub="+hubCombined;
        s += ", time="+ts+"ms";
        
        if (!getUseBackgroundThread()) {
            if (bUseAll) {
                int x = hubRoot.size();
                if (x > 100) {
                    if (x > 350 || propertyPath.indexOf(".") > 0) {
                        s += ", ALERT (large root hub)";
                    }
                }
            }
            if (hubCombined != null && hubCombined.getSize() > 250) {
                s += ", ALERT (large result hub)";
            }
            if (ts > 1000) {
                s += ", ALERT (took over 1second)";
            }
        }

        if ((hubCombined != null && hubCombined.size() > 2000) || ts > 2500) {
            // Exception e = new Exception("HubMerger performance concern");
            OAPerformance.LOG.log(Level.FINE, s);
        }
        OAPerformance.LOG.finer(s);
        LOG.fine(s);
    }

    private void _init() {
        createNodes(); // this will create nodeRoot
        this.dataRoot = new Data(nodeRoot, null, hubRoot);
        nodeRoot.data = dataRoot;
    }


    protected void setGetDetailHub() {
        OAThreadLocalDelegate.setGetDetailHub(this.hubRoot, this.propertyPath);
    }
    
    public Hub getRootHub() {
        return this.hubRoot;
    }

    public Hub getCombinedHub() {
        return this.hubCombined;
    }

    public void setEnabled(boolean b) {
        if (this.bEnabled == b) return;
        this.bEnabled = b;
        if (bEnabled) {
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                if (!bShareEndHub && hubCombined != null) hubCombined.clear();
                dataRoot.onNewList(null);
                dataRoot.afterChangeActiveObject(null);
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false); 
            }
        }
    }

    public boolean getEnabled() {
        return this.bEnabled;
    }

    public String getPath() {
        return this.propertyPath;
    }

    private String description;

    public void setDescription(String desc) {
        description = desc;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Note: if multiple threads are making changes that affect the node data, then errors could show
     * up.
     */
    public void verify() {
        // qqqqq todo: needs to verify recursive data
        if (true) return;
        if (!bEnabled) return;
        // XOG.finest("verifing nodes");
        // Nodes
        for (Node node = nodeRoot; node != null; node = node.child) {
            if (node.clazz == null) LOG.warning("node.clazz == null");
            if (node.liFromParentToChild == null) {
                if (node != nodeRoot) LOG.warning("liFromParentToChild == null for Node:" + node.property);
            }
            else if (node.liFromParentToChild.getType() == OALinkInfo.ONE) {
                if (node.data == null) {
                    if (bUseAll) {
                        // this might not be a problem, since the properties could be null
                        // LOG.warning("Node: "+node.property+" is used for type=One but data is null");
                    }
                }
                else if (node.data.parentObject != null) {
                    LOG.warning("Node: " + node.property + " is used for type=One and parentObject != null");
                }
            }
            else { // Many
                if (node.data != null) {
                    LOG.warning("Node: " + node.property + " is type=Many data != null");
                }
            }
        }

        // verify hubCombinued objects are used
        if (!bShareEndHub && hubCombined != null) {
            for (int i = 0;; i++) {
                Object obj = hubCombined.getAt(i);
                if (obj == null) break;
                if (!isUsed(obj)) {
                    LOG.warning("Object in hubCombined is not used");
                }
            }
        }

        // XOG.finest("verifying data");
        dataRoot.verify();
        for (Node node = nodeRoot; node != null; node = node.child) {
            if (node.data != null) node.data.verify();
        }
        // XOG.finest("verify complete");
    }

    private boolean isUsed(Object objFind) {
        if (bIgnoreIsUsedFlag) return false;
        if (!bEnabled) {
            return false;
        }
        boolean b = isUsed(objFind, null);
        return b;
    }

    private boolean isUsed(Object objFind, Node nodeFind) {
        if (bIgnoreIsUsedFlag) return false;
        if (!bEnabled) return false;
        // go back to dataRoot, or closest type=One
        Data dataFnd = dataRoot;
        for (Node n = nodeRoot; n != nodeFind; n = n.child) {
            if (n.liFromParentToChild != null && n.liFromParentToChild.getType() == OALinkInfo.ONE && n.data != null) {
                dataFnd = n.data;
            }
        }
        if (dataFnd == null) return false;
        boolean b = dataFnd._isUsed(objFind, nodeFind);
        return b;
    }

    // These are all called when the event happens on the "real" Hub that the combinedHub is "fed" from.

    /**
     * This can be overwritten to get the remove event from the parent, instead of getting the remove
     * event from the combinedHub.<br>
     * Since remove will also set the masterObject property to null, this can be used before it is set
     * to null.
     * 
     * @param e
     */
    protected void beforeRemoveRealHub(HubEvent<T> e) {
    }

    protected void afterRemoveRealHub(HubEvent<T> e) {
    }

    protected void beforeRemoveAllRealHub(HubEvent<T> e) {
    }
    protected void afterRemoveAllRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the move event from the parent, instead of getting the move event
     * from the combinedHub.
     */
    protected void afterMoveRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the insert event from the parent, instead of getting the insert
     * event from the combinedHub.
     */
    protected void beforeInsertRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the insert event from the parent, instead of getting the insert
     * event from the combinedHub.
     */
    protected void afterInsertRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the add event from the parent, instead of getting the add event
     * from the combinedHub.
     */
    protected void beforeAddRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the add event from the parent, instead of getting the add event
     * from the combinedHub.
     */
    protected void afterAddRealHub(HubEvent<T> e) {
    }

    /**
     * This can be overwritten to get the add event from the parent, instead of getting the add event
     * from the combinedHub.
     */
    protected void onNewListRealHub(HubEvent<T> e) {
    }
    
    /* //qqqqqq not sure if this is used // check to see if this, and Data.getChildrenCount() can be
     * removed public int getChildrenCount() { if (!bEnabled) return 0; int cnt = 0;
     * 
     * Node node = nodeRoot; for ( ; node != null; node = node.child) { if (node.data != null) cnt +=
     * node.data.getChildrenCount(); } // this needs to consider what to do if recursive is included cnt
     * += dataRoot.getChildrenCount(); return cnt; } */
    protected void createNodes() {
        bShareEndHub = !bUseAll;
        Class clazz = hubRoot.getObjectClass();

        // 20120809 using new OAPropertyPath
        OAPropertyPath oaPropPath = new OAPropertyPath(propertyPath);
        try {
            oaPropPath.setup(clazz);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cant find property for PropertyPath=\"" + propertyPath + "\" starting with Class "
                    + hubRoot.getObjectClass().getName(), e);
        }
        if (oaPropPath.hasPrivateLink()) {
            throw new RuntimeException("property path has private link, pp="+oaPropPath.getPropertyPath());
        }
        String[] pps = oaPropPath.getProperties();
        Method[] methods = oaPropPath.getMethods();
        Class[] classes = oaPropPath.getClasses();
        Constructor[] filterConstructors = oaPropPath.getFilterConstructors();

        Object[][] filterParamValues = oaPropPath.getFilterParamValues();
        int pos = 0;
        if (filterParamValues != null) {
            for (Object[] objs : filterParamValues) {
                if (objs != null) {
                    int i = 0;
                    for (Object obj : objs) {
                        if ("?".equals(obj)) {
                            //qqqqq if any param is "?" then need to have as HubMerger input value(s)
                            // if (filterInputValues == null || pos > filterInputValues.length - 1) throw new RuntimeExcepiton(...
                            // objs[i] = filterInputValues[pos++]; // this will need to be replacement value

                            throw new RuntimeException("propertyPath has filter with input param '?', which is not yet supported");
                        }
                    }
                }
            }
        }

        nodeRoot = new Node();
        nodeRoot.clazz = clazz;
        Node node = nodeRoot;
        boolean bLastWasMany = false;
        OALinkInfo lastLinkInfo = null; // 20131009

        for (int i = 0;; i++) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo recursiveLinkInfo = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.MANY);
            Node recursiveNode = null;

            // 20131009 check to see if link is recursive
            if (bLastWasMany && recursiveLinkInfo != null && lastLinkInfo != null && lastLinkInfo.getRecursive()) {
                // was: if (bLastWasMany && recursiveLinkInfo != null) {
                bIsRecusive = true;
                recursiveNode = new Node();
                recursiveNode.property = recursiveLinkInfo.getName();
                recursiveNode.liFromParentToChild = recursiveLinkInfo;
                recursiveNode.clazz = recursiveLinkInfo.getToClass();
                recursiveNode.recursiveChild = recursiveNode;
                node.recursiveChild = recursiveNode;
                bShareEndHub = false;
            }

            if (i == pps.length) {
                break;
            }
            String prop = pps[i];

            OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, prop);
            if (linkInfo == null) {
                throw new IllegalArgumentException("Cant find " + prop + " for PropertyPath \"" + propertyPath + "\" starting with Class "
                        + hubRoot.getObjectClass().getName());
            }
            bLastWasMany = linkInfo.getType() == linkInfo.MANY;
            lastLinkInfo = linkInfo;

            if (bShareEndHub) {
                if (linkInfo.getType() == OALinkInfo.MANY) {
                    if (i + 1 <= pps.length) bShareEndHub = false; // only the last one can be many
                }
                else {
                    if (i + 1 == pps.length) bShareEndHub = false; // only the last one can be many, but
                                                                   // not one
                }
            }

            Node node2 = new Node();
            node2.property = prop;
            node2.liFromParentToChild = linkInfo;

            clazz = classes[i];

            node2.clazz = clazz;
            node2.filterConstructor = filterConstructors[i];
            if (filterParamValues != null) {
                node2.filterParams = filterParamValues[i];
            }
            node.child = node2;
            node = node2;

            if (recursiveNode != null) {
                recursiveNode.child = node2;
            }
        }
        // verify that last property is same class as hubCombined
        if (hubCombined != null && hubCombined.getObjectClass() == null) HubDelegate.setObjectClass(hubCombined, clazz);
        if (hubCombined != null && !hubCombined.getObjectClass().equals(clazz)) {
            if (!clazz.equals(Hub.class)) {
                // if (!OAObject.class.equals(clazz)) { // 20120809 could be using generic type reference
                // (ex: OALeftJoin.A)
                throw new IllegalArgumentException("Classes do not match.  Property path \"" + propertyPath + "\" is for objects of Class "
                        + clazz.getName() + " and hubCombined is for objects of Class " + hubCombined.getObjectClass());
                // }
            }
        }
        if (bIncludeRootHub && hubCombined != null) {
            if (!hubRoot.getObjectClass().equals(clazz)) {
                throw new IllegalArgumentException("IncludeRootHub=true, and HubRoot class does not match.  Property path \"" + propertyPath
                        + "\" is for objects of Class " + clazz.getName() + " and hubCombined is for objects of Class "
                        + hubCombined.getObjectClass());
            }
        }
    }

    public void close() {
        // LOG.finer("closing");
        if (nodeRoot == null) return;
        bIgnoreIsUsedFlag = true;
        dataRoot.close();
        Node node = nodeRoot;
        while (node != null) {
            if (dataRoot != node.data) node.close();
            node = node.child;
        }
        bIgnoreIsUsedFlag = false;
        nodeRoot = null;
        dataRoot = null;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    class Node {
        Class clazz;
        String property;
        OALinkInfo liFromParentToChild;
        Constructor filterConstructor;
        Object[] filterParams;
        Node child;
        Node recursiveChild;
        Data data; // first node for root and used for Hub for link.type = One
        OACascade cascade;
        
        void close() {
            if (data != null) data.close();
            data = null;
        }

        public @Override
        String toString() {
            String s = liFromParentToChild == null ? "root" : liFromParentToChild.getType() == OALinkInfo.MANY ? "Many" : "One";
            s = "class: " + clazz + ", property: " + property + ", type:" + s;
            return s;
        }
    }

    final class Data extends HubListenerAdapter {
        Node node;
        OAObject parentObject; // parent object of hub
        Hub hub;
        Hub hubFilterMaster; // if using filter, then this is the master/orig that is then filtered into "hub"
        HubFilter hubFilter;
        volatile ArrayList<Data> alChildren;
        volatile boolean bHubListener;

        Data(Node node, OAObject parentObject, Hub hubNew) {
            if (hubNew == null) {
                throw new RuntimeException("hub can not be null");
            }
            if (!node.clazz.equals(hubNew.getObjectClass())) {
                if (!node.clazz.equals(Hub.class)) {
                    // 20130709
                    if (!OAObject.class.isAssignableFrom(node.clazz)) {
                        throw new RuntimeException("Hub class does not equal Node class");
                    }
                    /* was if (!OAObject.class.equals(node.clazz)) { // 20120809 could be using generic type
                     * reference (ex: OALeftJoin.A) throw new
                     * RuntimeException("Hub class does not equal Node class"); } */
                }
            }
            this.node = node;
            this.parentObject = parentObject;
            this.hub = hubNew;

            if (node.filterConstructor != null) {
                this.hubFilterMaster = hubNew;
                this.hub = new Hub(hubNew.getObjectClass());
                try {
                    int x = this.node.filterParams == null ? 0 : this.node.filterParams.length;
                    Object[] objs = new Object[2 + x];
                    objs[0] = this.hubFilterMaster;
                    objs[1] = this.hub;
                    if (x > 0) {
                        System.arraycopy(this.node.filterParams, 0, objs, 2, x);
                    }
                    hubFilter = ((CustomHubFilter) node.filterConstructor.newInstance(objs)).getHubFilter();
                }
                catch (Exception e) {
                    throw new RuntimeException("exception while creating Filter", e);
                }
            }
            
            // 20160806
            if (node == null || node.child == null || node.child.liFromParentToChild == null || node.child.liFromParentToChild.getDependentProperties() == null || node.child.liFromParentToChild.getDependentProperties().length == 0) {
                this.hub.addHubListener(this);
            }
            else {            
                this.hub.addHubListener(this,
                    node.child.liFromParentToChild.getName(),
                    node.child.liFromParentToChild.getDependentProperties(),
                    true);
            }
                    
            bHubListener = true;
            aiHubListenerCount.incrementAndGet();
            TotalHubListeners++;
            createChildren();
        }

        
        public int getObjectCount() {
            if (hub == null) return 0;
            int cnt = hub.getSize();
            if (alChildren == null) return cnt;
            try {
                lock.readLock().lock();
                cnt = alChildren.size();
                for (Data child : alChildren) {
                    cnt += child.getObjectCount();
                }
            }
            finally {
                lock.readLock().unlock();
            }
            return cnt;
        }
        public int getChildrenCount() {
            if (!bEnabled) return 0;
            int cnt;
            try {
                lock.readLock().lock();
                if (alChildren == null) return 0;
                cnt = alChildren.size();
                for (Data child : alChildren) {
                    if (child.parentObject != null) {
                        cnt += child.getChildrenCount();
                    }
                }
            }
            finally {
                lock.readLock().unlock();
            }
            return cnt;
        }

        void verify() {
            // todo: test when data is recursive
            if (true) return;
            if (!bEnabled) return;
            // All
            if (hub == null) {
                LOG.warning("hub == null, all data should have a hub.");
                return;
            }
            if (!hub.getObjectClass().equals(node.clazz)) LOG.warning("hub.objectClass != node.clazz");

            // node.data
            if (node.data != null) {
                if (node.liFromParentToChild != null && node.liFromParentToChild.getType() != OALinkInfo.ONE) {
                    LOG.warning("node.data != null for type!=one");
                }
            }

            // node.clazz
            if (hub != null && !node.clazz.equals(hub.getObjectClass())) LOG.warning("node.clazz != hub.objectClass");

            // first node
            if (node == nodeRoot) {
                if (parentObject != null) LOG.warning("should not have parentObject for nodeRoot");
                if (hub != hubRoot) LOG.warning("dataRoot.hub != hubRoot");
                try {
                    lock.readLock().lock();
                    if (alChildren == null) {
                        LOG.warning("dataRoot.alChildren == null");
                    }

                    if (bUseAll) {
                        int x1 = alChildren.size();
                        int x2 = hub.getSize();
                        if (x1 != x2) {
                            if (Math.abs(x1 - x2) > 1) LOG.warning("alChildren.size=" + x1 + " != hub.getSize=" + x2);
                        }
                    }
                    else {
                        int x = (hubRoot.getAO() == null) ? 0 : 1;
                        if (node.recursiveChild != null) x *= 2;
                        if (alChildren.size() != x) {
                            LOG.warning("bUseAll=false, alChildren.size != " + x);
                        }
                    }
                }
                finally {
                    lock.readLock().unlock();
                }
            }

            // last node
            if (node.child == null && node.recursiveChild == null) {
                if (alChildren != null) LOG.warning("node.child=null, alChildren != null");
            }

            // not first or last
            if (alChildren == null) {
                if (node.child != null || node.recursiveChild != null) {
                    LOG.warning("alChildren == null");
                }
            }

            if (node.data == this) {
                if (parentObject != null) LOG.warning("parentObject != null");
            }

            // ONE
            if (node.liFromParentToChild != null && node.liFromParentToChild.getType() == OALinkInfo.ONE) {
                if (parentObject != null) LOG.warning("parentObject != null");
                if (node.data != this) LOG.warning("node.data != this");
                for (int i = 0; hub != null; i++) {
                    Object obj = hub.getAt(i);
                    if (obj == null) break;
                    if (!isUsed(obj, node)) {
                        LOG.warning("Object in type.One is not used");
                    }
                }
            }

            // MANY
            if (node.liFromParentToChild == null || node.liFromParentToChild.getType() == OALinkInfo.MANY) {
                if (node.liFromParentToChild == null) {
                    if (node.data == null) LOG.warning("node.data == null for nodeRoot");
                }
                else {
                    if (node.data != null) LOG.warning("node.data != null for type=Many");
                }
                if (node.child == null && bShareEndHub && hubCombined != null && this.hub != hubCombined.getSharedHub()) {
                    LOG.warning("node.hub != hubCombined.sharedHub");
                }
                if (node.child != null) {
                    try {
                        lock.readLock().lock();

                        if (this == dataRoot && !bUseAll) {
                            if (alChildren.size() > 1) {
                                LOG.warning("alChildren.size > 1");
                            }
                        }
                        if (bUseAll) {
                            if (alChildren == null) LOG.warning("alChildren = null");
                            else if (hub == null) LOG.warning("Hub = null");
                            else {
                                int x1 = alChildren.size();
                                int x2 = hub.getSize();
                                if (node.recursiveChild != null) x2 *= 2;
                                if (x1 != x2) {
                                    if (Math.abs(x1 - x2) > 1) LOG.warning("alChildren.size=" + x1 + " != hub.getSize=" + x2);
                                }
                            }
                        }
                        else {
                            for (Data child : alChildren) {
                                if (child.parentObject != null && !hub.contains(child.parentObject)) {
                                    LOG.warning("alChildren object not in hub");
                                }
                            }
                        }
                    }
                    finally {
                        lock.readLock().unlock();
                    }

                    for (int i = 0; hub != null; i++) {
                        Object obj = hub.getAt(i);
                        if (obj == null) break;
                        if (node.child == null && hubCombined != null && !hubCombined.contains(obj)) {
                            LOG.warning("object not in hubCombined");
                        }
                    }
                }
                else {
                    if (node.recursiveChild != null) {
                        if (alChildren == null) LOG.warning("alChildren = null");
                        else if (hub == null) LOG.warning("Hub = null");
                        else {
                            int x1 = alChildren.size();
                            int x2 = hub.getSize();
                            if (x1 != x2) {
                                if (Math.abs(x1 - x2) > 1) LOG.warning("recursive alChildren.size=" + x1 + " != hub.getSize=" + x2);
                            }
                        }
                    }

                    if (!bShareEndHub) {
                        for (int i = 0; hub != null; i++) {
                            Object obj = hub.getAt(i);
                            if (obj == null) break;
                            if (hubCombined != null && !hubCombined.contains(obj)) LOG.warning("object not in hubCombined");
                        }
                    }
                }
            }

            try {
                lock.readLock().lock();
                if (alChildren != null) {
                    for (Data child : alChildren) {
                        if (child.node.data == null) child.verify();
                    }
                }
            }
            finally {
                lock.readLock().unlock();
            }
        }

        void createChildren() {
            if (!bEnabled) return;
            if (bNewListCancel) return;
            // XOG.finer("createChildren");

            if (node.child != null || node.recursiveChild != null) {
                try {
                    int x = Math.max(hub.getSize(), 3);
                    if (!bUseAll && this == dataRoot) x = 1;
                    lock.writeLock().lock();
                    alChildren = new ArrayList<Data>(x);
                }
                finally {
                    lock.writeLock().unlock();
                }
            }

            if (node.child == null) {
                if (bShareEndHub) {
                    if (hubCombined != null) hubCombined.setSharedHub(hub, bShareActiveObject);
                }
                else {
                    if (OASync.isClient(HubMerger.this.getRootHub().getObjectClass())) {
                        // preload, so that any getDetail will be more efficient
                        for (int i=0; ;i++) {
                            OAObject obj = (OAObject) hub.elementAt(i);
                            if (obj == null) break;
                            if (bNewListCancel) return;
                            if (node.child != null && node.child.liFromParentToChild != null) {
                                node.child.liFromParentToChild.getValue(obj);
                            }
                            else if (node.liFromParentToChild != null) {
                                node.liFromParentToChild.getValue(obj);
                            }
                        }
                    }
                    for (int i=0; ;i++) {
                        OAObject obj = (OAObject) hub.elementAt(i);
                        if (obj == null) break;
                        if (bNewListCancel) return;
                        createChild(obj);
                    }
                }
            }
            else {
                if (bUseAll || this.node != nodeRoot && nodeRoot != null) {
                    OAThreadLocal tl;
                    Hub hubx = null;
                    //if (!bCreatedFromOneObject) hubx = OAThreadLocalDelegate.setGetDetailHub(hub);
                    try {
                        if (OASync.isClient(HubMerger.this.getRootHub().getObjectClass())) {
                            // preload, so that any getDetail will be more efficient
                            for (int i=0; ;i++) {
                                OAObject obj = (OAObject) hub.elementAt(i);
                                if (obj == null) break;
                                if (bNewListCancel) return;
                                if (node.child != null && node.child.liFromParentToChild != null) {
                                    node.child.liFromParentToChild.getValue(obj);
                                }
                                else if (node.liFromParentToChild != null) {
                                    node.liFromParentToChild.getValue(obj);
                                }
                            }
                        }
                        for (int i = 0;; i++) {
                            OAObject obj = (OAObject) hub.elementAt(i);
                            if (obj == null) break;
                            if (bNewListCancel) return;
                            createChild(obj);
                        }
                    }
                    finally {
                    //    if (!bCreatedFromOneObject) OAThreadLocalDelegate.resetGetDetailHub(hubx);
                    }
                }
                else {
                    OAObject obj = (OAObject) hub.getAO();
                    if (obj != null) createChild(obj);
                    else {
                        createChildUsingMaster();
                    }
                }
            }
        }

        // 20110809 see if the the masterHub/Object can be used. This is for cases where hub.size=0, but
        // you want
        // to have the merger get objects based on master. ex: OrderContacts propPath
        // "order.customer.contacts" for a
        // hub to link and autocreate the orderContact objects
        
        void createChildUsingMaster() {
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                _createChildUsingMaster();
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
            }
        }
        
        
        void _createChildUsingMaster() {
            if (!bEnabled) return;
            // XOG.finer("createChild");
            if (node.child == null) {
                return;
            }

            String s = HubDetailDelegate.getPropertyFromDetailToMaster(hub);
            if (s == null || !s.equalsIgnoreCase(node.child.property)) return;

            if (node.child.liFromParentToChild.getType() == OALinkInfo.ONE) { // store in Node.data.hub
                if (node.child.data == null) {
                    Hub h;
                    if (node.child.child == null) h = hubCombined;
                    else {
                        h = new Hub(node.child.clazz);
                    }

                    Data data = new Data(node.child, null, h);
                    node.child.data = data;
                }
                OAObject ref = (OAObject) hub.getMasterObject();
                if (ref == null) return;

                if (!node.child.data.hub.contains(ref)) {
                    node.child.data.hub.add(ref); // this will send afterAdd(), which will create children
                }

                try {
                    lock.writeLock().lock();
                    if (alChildren != null) { // could have been closed in another thread
                        this.alChildren.add(node.child.data);
                    }
                }
                finally {
                    lock.writeLock().unlock();
                }
            }
            else {
                Hub h = (Hub) hub.getMasterHub();
                if (h == null) return;
                Data d = new Data(node.child, null, h);
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) { // could have been closed in another thread
                        alChildren.add(d);
                    }
                }
                finally {
                    lock.writeLock().unlock();
                }
            }
        }

        void createChild(OAObject parent) {
            _createChild(parent);
            _createRecursiveChild(parent);
        }

        
        void _createChild(OAObject parent) {
            if (bNewListCancel) return;
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                _createChild2(parent);
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
            }
        }
        
        void _createChild2(OAObject parent) {
            if (!bEnabled) return;
            // XOG.finer("createChild");

            // 20131209
            if (node == nodeRoot && bIncludeRootHub) {
                if (hubCombined != null && !hubCombined.contains(parent)) {
                    try {
                        if (bLoadingCombined) OAThreadLocalDelegate.setLoading(true);
                        hubCombined.add(parent);
                    }
                    finally {
                        if (bLoadingCombined) OAThreadLocalDelegate.setLoading(false);
                    }
                }
            }

            if (node.child == null) {
                if (!bShareEndHub && hubCombined != null && !hubCombined.contains(parent)) {
                    try {
                        if (bLoadingCombined) OAThreadLocalDelegate.setLoading(true);
                        hubCombined.add(parent);
                    }
                    finally {
                        if (bLoadingCombined) OAThreadLocalDelegate.setLoading(false);
                    }
                }
            }
            else if (node.child.liFromParentToChild.getType() == OALinkInfo.ONE) { // store in Node.data.hub
                if (node.child.data == null) {
                    Hub h;
                    if (node.child.child == null) h = hubCombined;
                    else {
                        h = new Hub(node.child.clazz);
                    }
                    Data data = new Data(node.child, null, h);
                    node.child.data = data;
                }
                OAObject ref = (OAObject) node.child.liFromParentToChild.getValue(parent);

                if (ref != null) {
                    if (!node.child.data.hub.contains(ref)) {
                        node.child.data.hub.add(ref); // this will send afterAdd(), which will create children
                    }
                }
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) { // could have been closed in another thread
                        this.alChildren.add(node.child.data); // even if obj==null, so that verify will
                                                              // work - it looks for alChildren.size=1
                    }
                }
                finally {
                    lock.writeLock().unlock();
                }
            }
            else {
                Hub h = (Hub) node.child.liFromParentToChild.getValue(parent);
                Data d = new Data(node.child, parent, h);
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) { // could have been closed in another thread
                        alChildren.add(d);
                    }
                }
                finally {
                    lock.writeLock().unlock();
                }
            }
        }

        void _createRecursiveChild(OAObject parent) {
            if (!bEnabled) return;
            if (node.recursiveChild == null) return;
            if (bNewListCancel) return;

            boolean bHadCascade;
            if (node.recursiveChild.cascade == null) {
                node.recursiveChild.cascade = new OACascade();
                bHadCascade = false;
            }
            else bHadCascade = true;
            
            if (node.recursiveChild.cascade.wasCascaded(parent, true)) return;
            
            Hub h = (Hub) node.recursiveChild.liFromParentToChild.getValue(parent);
            Data d = new Data(node.recursiveChild, parent, h);
            try {
                lock.writeLock().lock();
                if (alChildren != null) { // could have been closed in another thread
                    alChildren.add(d);
                }
            }
            finally {
                lock.writeLock().unlock();
                if (!bHadCascade) node.recursiveChild.cascade = null;
            }
        }

        private boolean _isUsed(Object objFind, Node nodeFind) {
            if (bIgnoreIsUsedFlag) return false;
            if (!bEnabled) return false;
            if (node.child == null) {
                boolean b = (nodeFind == null && hub != null && hub.contains(objFind));
                if (b || node.recursiveChild == null) return b;
            }

            if (this.node.child != null && this.node.child == nodeFind) {
                if (this.node == nodeRoot && !bUseAll) {
                    OAObject obj = (OAObject) this.hub.getAO();
                    if (obj != null) {
                        OAObject ref = (OAObject) node.child.liFromParentToChild.getValue(obj);
                        if (ref == objFind) return true;
                    }
                }
                else {
                    for (int i = 0;; i++) {
                        OAObject obj = (OAObject) this.hub.elementAt(i);
                        if (obj == null) break;
                        OAObject ref = (OAObject) node.child.liFromParentToChild.getValue(obj);
                        if (ref == objFind) return true;
                    }
                }
            }
            else {
                if (alChildren != null) {
                    for (int i=0; ;i++) {
                        Data data;
                        try {
                            lock.readLock().lock();
                            if (i >= alChildren.size()) break;
                            data = alChildren.get(i);
                        }
                        finally {
                            lock.readLock().unlock();
                        }
                        if (data._isUsed(objFind, nodeFind)) return true;
                    }
                }
            }
            return false;
        }

        public @Override
        String toString() {
            String s = "";
            if (hub != null) s = ", hub:" + hub.getObjectClass().getName() + ", cnt:" + hub.getSize();
            return node.property + ", parent:" + parentObject + s;
        }

        void remove(Object obj) {
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                _remove(obj);
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
            }
        }
        
        void _remove(Object obj) {
            if (!bEnabled) return;

            if (alChildren == null || node.child == null) {
                if (isUsed(obj)) {
                    // needs to remove from alChildren, ex: when using recursive properties
                    // was: return;
                }
                else if (!bShareEndHub) {
                    if (this.hub == hubCombined) {
                        if (hubCombined == null || !hubCombined.contains(obj)) {
                            return; // might have already been removed
                        }
                    }
                    if (OAThreadLocalDelegate.isHubMergerChanging()) { // 20120102
                        // 20120612 dont send event, unless there is a recursive prop, which needs to
                        // have recursives nodes updated
                        HubAddRemoveDelegate.remove(hubCombined, obj, false, bIsRecusive, false, false, false, false);
                        // was: HubAddRemoveDelegate.remove(hubCombined, obj, false, false, false,
                        // false, false);
                    }
                    else {
                        if (hubCombined != null) hubCombined.remove(obj);
                    }
                }
                if (alChildren == null) {
                    return;
                }
            }

            // 20131209
            if (node == nodeRoot && bIncludeRootHub) {
                if (!isUsed(obj)) {
                    if (OAThreadLocalDelegate.isHubMergerChanging()) {
                        HubAddRemoveDelegate.remove(hubCombined, obj, false, bIsRecusive, false, false, false, false);
                    }
                    else {
                        if (hubCombined != null) hubCombined.remove(obj);
                    }
                }
            }

            for (int alPos = 0;; alPos++) {
                Data child;
                try {
                    lock.readLock().lock();
                    if (alChildren == null || alPos >= alChildren.size()) break;
                    child = alChildren.get(alPos);
                }
                finally {
                    lock.readLock().unlock();
                }
                if (obj == child.parentObject) { // will always be a type=Many
                    try {
                        lock.writeLock().lock();
                        if (alChildren == null || alPos >= alChildren.size()) break;
                        this.alChildren.remove(alPos);
                        alPos--;
                    }
                    finally {
                        lock.writeLock().unlock();
                    }
                    child.close();
                    if (this.node.recursiveChild == null) break;
                }
                if (child.parentObject == null) { // will always be a type=One
                    Object ref = node.child.liFromParentToChild.getValue(obj);
                    try {
                        lock.writeLock().lock();
                        if (alChildren == null || alPos >= alChildren.size()) break;
                        this.alChildren.remove(alPos);
                        alPos--;
                    }
                    finally {
                        lock.writeLock().unlock();
                    }
                    if (ref != null) {
                        if (!isUsed(ref, child.node)) {
                            if (OAThreadLocalDelegate.isHubMergerChanging()) { // 20120102
                                HubAddRemoveDelegate.remove(child.hub, ref, false, false, false, false, false, false);
                            }
                            else {
                                child.hub.remove(ref);
                            }
                        }
                    }
                    if (this.node.recursiveChild == null) break;
                }
            }
        }

        void close() {
            // XOG.finer("close");
            if (hub != null && bHubListener) {
                hub.removeHubListener(this);
                aiHubListenerCount.decrementAndGet();
                bHubListener = false;
                TotalHubListeners--;
            }
            if (hubFilter != null) {
                hubFilter.close();
                hubFilter = null;
            }
            boolean bLockSet = true;
            try {
                lock.readLock().lock();
                if (alChildren == null || node.child == null) {
                    if (bShareEndHub) {
                        // 20110809 might need to unset hubCombied.sharedHub
                        if (hubCombined != null) hubCombined.setSharedHub(null);
                        return;
                    }
                    if (hub != null) {
                        Object[] objs = hub.toArray();
                        lock.readLock().unlock();
                        bLockSet = false;
                        for (int i = 0; i < objs.length; i++) {
                            remove(objs[i]);
                        }
                    }
                    if (alChildren == null) {
                        return;
                    }
                }
            }
            finally {
                if (bLockSet) lock.readLock().unlock();
            }

            if (node.child != null && node.child.liFromParentToChild.getType() == OALinkInfo.ONE) {
                // dont call close on Node.data. This will instead use remove()
                Object[] objs = hub.toArray();
                for (int i = 0; i < objs.length; i++) {
                    remove(objs[i]);
                }
            }
            else {
                for (;;) {
                    Data child;
                    try {
                        lock.writeLock().lock();
                        if (alChildren == null || alChildren.size() == 0) break;
                        child = alChildren.get(0);
                        alChildren.remove(0);
                    }
                    finally {
                        lock.writeLock().unlock();
                    }
                    child.close();
                }
            }
            try {
                lock.writeLock().lock();
                alChildren = null;
            }
            finally {
                lock.writeLock().unlock();
            }
        }

        // ============ HubListener for Hub used for child
        public @Override void beforeRemoveAll(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                _beforeRemoveAll(e);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        private void _beforeRemoveAll(HubEvent e) {
            //20150622
            if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                HubMerger.this.beforeRemoveAllRealHub(e);
            }
            /*was
            Hub h = e.getHub();
            if (h.getObjectClass().equals(hubCombined.getObjectClass())) {
                HubMerger.this.beforeRemoveAllRealHub(e);
            }
            */

            if (!bEnabled) return;
            
            // 20140611 hub.clear/removeAll no longer removes each obj
            /*
            if (this != dataRoot) return;
            if (!bUseAll) return;
            if (hub.isLoading()) return;
            */
            

            boolean hold = bIgnoreIsUsedFlag;
            bIgnoreIsUsedFlag = true;
            for (int i = 0;; i++) {
                Object obj = hub.getAt(i);
                if (obj == null) break;
                remove(obj);
            }
            if (!hold) bIgnoreIsUsedFlag = false;
        }

        @Override
        public void afterRemoveAll(HubEvent e) {
            //20150622
            if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                HubMerger.this.afterRemoveAllRealHub(e);
            }
            /*
            Hub h = e.getHub();
            if (h.getObjectClass().equals(hubCombined.getObjectClass())) {
                HubMerger.this.afterRemoveAllRealHub(e);
            }
            */
        }
        

        @Override
        public void onNewList(HubEvent hubEvent) {
            if ((hub != hubRoot) || OASync.isServer()) {
                _onNewList(hubEvent);
            }
            else {
                synchronized (lockNewList) {
                    bNewListCancel = true;
                    hubEventBackgroundThread = hubEvent;

                    if (bRunningNewListThread) return;
                    bRunningNewListThread = true;
                }
                
                getExecutorService().submit(new Runnable() {
                    @Override
                    public void run() {
                        HubEvent hubEventCurrent;
                        for (;;) {
                            synchronized (lockNewList) {
                                hubEventCurrent = hubEventBackgroundThread;
                                bNewListCancel = false;
                            }
                            try {
                                _onNewList(hubEventCurrent);
                            }
                            catch (Exception e) {
                                LOG.log(Level.WARNING, "exception running onNewList in HubMerger", e);
                            }
                            
                            synchronized (lockNewList) {
                                if (!bNewListCancel) {
                                    bRunningNewListThread = false;
                                    lockNewList.notifyAll();
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        }

        /**
         * If rootHub, then wait for background thread to finish loading
         */
        @Override
        public void afterNewList(HubEvent hubEvent) {
            if ((hub != hubRoot) || OASync.isServer()) {
                return;
            }

            if (getUseBackgroundThread() || SwingUtilities.isEventDispatchThread()) {
                return; // let run in the background
            }
            
            synchronized (lockNewList) {
                for (int i=0; i<10; i++) {
                    if (!bRunningNewListThread) break;
                    if (i > 5) {
                        LOG.warning("HubMerger lockNewList timeout waiting for HubMerger thread to finish");
                        break;
                    }
                    try {
                        lockNewList.wait(100);
                    }
                    catch (Exception e) {
                    }
                }
            }
        }
        
        public void _onNewList(HubEvent e) {
            long ts = System.currentTimeMillis();
            try {
                if (hub == hubRoot) {
                    OAThreadLocalDelegate.setHubMergerIsChanging(true);
                    if (!bServerSideOnly) bLoadingCombined = true; //20160908
                }
                _onNewList2(e);
            }
            finally {
                if (hub == hubRoot) {
                    OAThreadLocalDelegate.setHubMergerIsChanging(false);
                    if (bLoadingCombined) {
                        bLoadingCombined = false;
                        HubEventDelegate.fireOnNewListEvent(hubCombined, false);
                    }
                }
            }
            //20150630
            if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                onNewListRealHub(e);
            }
            
            if (hub == hubRoot) {
                ts = System.currentTimeMillis() - ts;
                String s = ("HM."+id+") onNewList hub="+hubRoot+", propertyPath="+propertyPath+", useAll="+bUseAll+", useBackgroundThread="+getUseBackgroundThread());
                s += ", combinedHub="+hubCombined;
                s += ", time="+ts+"ms";
                
                if (!getUseBackgroundThread()) {
                    if (bUseAll) {
                        int x = hubRoot.size();
                        if (x > 50) {
                            if (x > 150 || propertyPath.indexOf(".") > 0) {
                                s += ", ALERT";
                            }
                        }
                    }
                    if (hubCombined.getSize() > 250) {
                        s += ", ALERT";
                    }
                    if (ts > 1000) {
                        s += ", ALERT";
                    }
                }
                OAPerformance.LOG.finer(s);
                LOG.fine(s);
            }
        }

        public void _onNewList2(HubEvent e) {
            // 20161103 if root, then all can be removed. Must do this for rootHubs that are recursive
            if (hub == hubRoot) {
                hubCombined.clear();
            }
            try {
                _onNewList();
            }
            finally {
            }
            if (hubCombined != null && this.hub != hubCombined) {
                if (hubCombined.getSharedHub() != this.hub) {
                    if (!bShareEndHub) { // 20110521
                        HubEventDelegate.fireOnNewListEvent(hubCombined, true);
                    }
                }
            }
        }

        private void _onNewList() {
            Hub holdDetailHub = OAThreadLocalDelegate.getGetDetailHub();
            String holdDetailPP = OAThreadLocalDelegate.getGetDetailPropertyPath();
            try {
                setGetDetailHub();
                //was: OAThreadLocalDelegate.setGetDetailHub(HubMerger.this.hubRoot, HubMerger.this.propertyPath);
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                else OAThreadLocalDelegate.setSuppressCSMessages(true);
                _onNewList2();
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
                else OAThreadLocalDelegate.setSuppressCSMessages(false);
                OAThreadLocalDelegate.resetGetDetailHub(holdDetailHub, holdDetailPP);
            }
        }
        
        private void _onNewList2() {
            if (!bEnabled) return;
            
            if (this != dataRoot) return;
            
            if (!bUseAll) {
                // 20110809 need to continue if there is a masterObject/Hub and AO=null
                // in case masterObject was previously null (making hub invalid)
                if (this.hub.getMasterHub() == null) {
                    return;
                }
                // was: return;
            }

            bIgnoreIsUsedFlag = true;
            if (node.child != null && node.child.liFromParentToChild.getType() == OALinkInfo.ONE) {
                // dont call close on Node.data. This will instead call remove()
                for (; node.child.data != null && node.child.data.hub != null;) {
                    Object obj = node.child.data.hub.getAt(0);
                    if (obj == null) break;

                    if (OAThreadLocalDelegate.isHubMergerChanging()) { // 20120102
                        HubAddRemoveDelegate.remove(node.child.data.hub, obj, false, false, false, false, false, false);
                    }
                    else {
                        node.child.data.hub.remove(obj);
                    }
                }
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) {
                        alChildren.clear();
                    }
                }
                finally {
                    lock.writeLock().unlock();
                }
            }
            else {
                for (;;) {
                    Data child;
                    try {
                        lock.writeLock().lock();
                        if (alChildren == null || alChildren.size() == 0) break;
                        child = alChildren.get(0);
                        alChildren.remove(0);
                    }
                    finally {
                        lock.writeLock().unlock();
                    }
                    child.close();
                }
            }
            bIgnoreIsUsedFlag = false;
            createChildren();
        }

        @Override
        public void beforeRemove(HubEvent e) {
            Object obj = e.getObject();
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                if (obj != null) {
                    //20150622
                    if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                        HubMerger.this.beforeRemoveRealHub(e);
                    }
                    /*was
                    Class c = obj.getClass();
                    if (c.equals(hubCombined.getObjectClass()) || OAObject.class.equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeRemoveRealHub(e);
                    }
                    */
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        @Override
        public void afterRemove(HubEvent e) {
            Object obj = e.getObject();

            if (obj != null) {
                //20150622
                if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                    HubMerger.this.afterRemoveRealHub(e);
                }
                /*was
                Class c = obj.getClass();
                if (c.equals(hubCombined.getObjectClass())) {
                    HubMerger.this.afterRemoveRealHub(e);
                }
                */
            }
            if (!bEnabled) return;
            if (this == dataRoot && !bUseAll) {
                if (!bIncludeRootHub) { // 20131209
                    return;
                }
            }
            try {
                // 20120903 removed/commented this, and need to have hub event sent out for remove
                // if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                remove(obj);
            }
            finally {
                // if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        @Override
        public void beforeAdd(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    //20150622
                    if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                        HubMerger.this.beforeAddRealHub(e);
                    }
                    /*was
                    Class c = obj.getClass();
                    if (c.equals(hubCombined.getObjectClass()) || OAObject.class.equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeAddRealHub(e);
                    }
                    */
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        public @Override
        void afterAdd(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    //20150622
                    if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                        HubMerger.this.afterAddRealHub(e);
                    }
                    /*was
                    Class c = obj.getClass();
                    if (c.equals(hubCombined.getObjectClass()) || OAObject.class.equals(hubCombined.getObjectClass())) {
                        HubMerger.this.afterAddRealHub(e);
                    }
                    */
                }
                afterAdd2(e);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        private void afterAdd2(HubEvent e) {
            if (!bEnabled) return;
            if (this == dataRoot && !bUseAll) return;
// 20150713 took this out, since hubFilter initialize sets isLoading=true            
//            if (hub.isLoading()) return;
            
            // 20140312 verify that object is still in Hub
            if (e.getHub().contains(e.getObject())) {
                createChild((OAObject) e.getObject());
            }
        }

        @Override
        public void beforeInsert(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    //20150622
                    if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                        HubMerger.this.beforeInsertRealHub(e);
                    }
                    /*was
                    Class c = obj.getClass();
                    if (c.equals(hubCombined.getObjectClass()) || OAObject.class.equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeInsertRealHub(e);
                    }
                    */
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        public @Override
        void afterInsert(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    //20150622
                    if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                        HubMerger.this.afterInsertRealHub(e);
                    }
                    /*was
                    Class c = obj.getClass();
                    if (c.equals(hubCombined.getObjectClass()) || OAObject.class.equals(hubCombined.getObjectClass())) {
                        HubMerger.this.afterInsertRealHub(e);
                    }
                    */
                }
                afterAdd2(e);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }

        @Override
        public void afterMove(HubEvent e) {
            //20150622
            if ((node == nodeRoot && bIncludeRootHub) || (node.child == null)) {
                HubMerger.this.afterMoveRealHub(e);
            }
            /*
            Hub h = e.getHub();
            if (h != null && h.getObjectClass().equals(hubCombined.getObjectClass())) {
                HubMerger.this.afterMoveRealHub(e);
            }
            */
        }

        public @Override
        void afterPropertyChange(HubEvent e) {
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                _afterPropertyChange(e);
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
            }
        }
        void _afterPropertyChange(HubEvent e) {
            if (!bEnabled) return;
            if (node.child == null) return; // last nodes
            String prop = e.getPropertyName();
            if (prop == null) return;

            // 20160806
            //was: if (node.child.liFromParentToChild.getType() != OALinkInfo.ONE) return;
            if (!node.child.liFromParentToChild.getName().equalsIgnoreCase(prop)) return;

            // 20110324 data might not have been created,
            if (node.child.data == null) return;
            
            // 20160806 could be a calculated many link 
            if (node.child.liFromParentToChild.getType() == OALinkInfo.MANY) {
                if (!node.child.liFromParentToChild.getCalculated()) return;
                Object objx = e.getObject();
                if (!(objx instanceof OAObject)) return;
                // calling the method will cause the hub to be updated
                node.child.liFromParentToChild.getValue((OAObject) objx);
                return;
            }

            if (this == dataRoot && !bUseAll) {
                if (e.getObject() != hubRoot.getAO()) return;
            }

            Object ref = e.getOldValue();
            if (ref != null) {
                if (!isUsed(ref, node.child)) {
                    node.child.data.hub.remove(ref);
                }
            }

            ref = e.getNewValue();
            if (ref != null) {
                if (!node.child.data.hub.contains(ref)) {
                    node.child.data.hub.add(ref);
                }
            }
        }

        public @Override
        void afterChangeActiveObject(HubEvent evt) {
            if (!bEnabled) return;
            if (this.node != nodeRoot || bUseAll) return;

            // 20110809 if the AO is the same, then this can be skipped
            if (evt != null && alChildren != null && alChildren.size() > 0) {
                Data d = alChildren.get(0);
                if (d.parentObject == evt.getObject()) return;
            }

            if (hubCombined != null) {
                long ts = System.currentTimeMillis();
                try {
                    OAThreadLocalDelegate.setHubMergerIsChanging(true);
                    if (!bShareEndHub) {
                        hubCombined.clear();
                    }
                    if (!bServerSideOnly) bLoadingCombined = true; //20160908
                    _afterChangeActiveObject();
                
                }
                finally {
                    OAThreadLocalDelegate.setHubMergerIsChanging(false);
                    if (bLoadingCombined) {
                        bLoadingCombined = false;
                        HubEventDelegate.fireOnNewListEvent(hubCombined, false);
                    }
                }
                
                
                ts = System.currentTimeMillis() - ts;
                String s = ("HM."+id+") onChangeAO hub="+hubRoot+", propertyPath="+propertyPath+", useAll="+bUseAll+", useBackgroundThread="+getUseBackgroundThread());
                s += ", combinedHub="+hubCombined;
                s += ", time="+ts+"ms";
                
                if (!getUseBackgroundThread()) {
                    if (bUseAll) {
                        int x = hubRoot.size();
                        if (x > 50) {
                            if (x > 150 || propertyPath.indexOf(".") > 0) {
                                s += ", ALERT";
                            }
                        }
                    }
                    if (hubCombined.getSize() > 250) {
                        s += ", ALERT";
                    }
                    if (ts > 1000) {
                        s += ", ALERT";
                    }
                }
                OAPerformance.LOG.finer(s);
                LOG.fine(s);
            }
            // 20110419 param was true, but this should only send to other hubs that share this one
            HubEventDelegate.fireOnNewListEvent(hubCombined, false);
        }

        private void _afterChangeActiveObject() {
            try {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(true);
                _afterChangeActiveObject2();
            }
            finally {
                if (bServerSideOnly) OARemoteThreadDelegate.sendMessages(false);
            }
        }
        private void _afterChangeActiveObject2() {
            try {
                lock.writeLock().lock();
                if (alChildren != null && alChildren.size() > 0) {
                    if (node.child.liFromParentToChild.getType() == OALinkInfo.ONE) {
                        alChildren.remove(0);
                        Object obj = node.child.data.hub.getAt(0);
                        if (obj != null) {
                            node.child.data.hub.remove(obj);
                        }
                    }
                    else {
                        // 20120523 added loop
                        for (Data child : alChildren) {
                            child.close();
                        }
                        alChildren.clear();

                        /* qqqqqqq was: Data child; child = alChildren.get(0); alChildren.remove(0);
                         * child.close(); */
                    }
                }
            }
            finally {
                lock.writeLock().unlock();
            }
            Object obj = hub.getAO();
            if (obj != null) createChild((OAObject) obj);
        }

        @Override
        public void afterLoad(HubEvent e) {
            if (!bEnabled) return;

            if (this == dataRoot && !bUseAll) {
                if (e.getObject() != hubRoot.getAO()) return;
            }
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                OAObject obj = (OAObject) e.getObject();
                remove(obj);
                createChild(obj);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
    }

    public boolean getUseAll() {
        return bUseAll;
    }
    
    private static ExecutorService executorService;
    private static final AtomicInteger aiThreadCnt = new AtomicInteger();
    protected static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "HubMerger."+aiThreadCnt.getAndIncrement());
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                }
            });
        }
        return executorService;
    }
    
}
