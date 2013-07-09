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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.object.*;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAReflect;

/**
    Used to combine objects from a property path of a root Hub into a single Hub.  
    As any changes are made to any objects included in the property path, the Hub 
    will automatically be updated.  Property path can include either type of 
    reference: One or Many.  
    <p>
    Examples:
    <pre>
    new HubMerger(hubSalesmen, hubOrders, "customers.orders");

    new HubMerger(hubItem, hubForm, "formItem.formSection.formRow.form");
    
    new HubMerger(hubForm, hubItem, "formRows.formSections.formItems.item");
    </pre>
    
    @created 2004/08/20, rewritten 20080804, added recursive links 20120527
*/
public class HubMerger {
    private static Logger LOG = Logger.getLogger(HubMerger.class.getName());

    /* Programming notes:
       Node: defines the straight path of nodes.  Each node has a child node.
       Data: used to create a tree of nodes for objects in the hubs.  If the property is a type=One
       then the actual Node will have a temp Hub that is used to store the unique values.
       Each data has an array of children Data.  A new Data is created for each object
       in the parent Data, until the child.node is null.
       20120522 add support for recursive objects in the path
    */
    
    private Node nodeRoot;  // first node from propertyPath
    private Data dataRoot;  // node used for the root Hub
    
    String path;      // property path 
    Hub hubCombined;  // Hub that stores the results 
    Hub hubRoot;      // main hub used as the first hub.
    boolean bShareEndHub;     // if true, then hubCombined can be shared with the currently found "single" Hub
    boolean bShareActiveObject;  // if bShareEndHub, then this will set the sharedHub as sharing the AO
    boolean bUseAll;   // if false, then only use AO in the rootHub, otherwise all objects will be used. 
    boolean bIgnoreIsUsedFlag; // flag to have isUsed() return false;
    private boolean bEnabled = true;
    private boolean bIsRecusive;
    
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public int TotalHubListeners; // for testing only   

    public static int HubListenerCount;  // number of HubListeners used by all HubMerger


    private boolean bServerSideOnly;
    
    /**
        Used to create an object that will automatically update a Hub with all of the objects from a
        property path of a Hub.
        @param hubRoot root Hub.  The active object of this Hub will be used to get all objects in the propertyPath.
        @param hubCombinedObjects will have all of the objects from the active object of the hubRoot, using propertyPath.
        If hubCombinedObjects.getObjectClass() is null, then it will be assigned the correct class.
        @param propertyPath dot seperated property path from the class of rootHub to the class of combinedHub.
        @param bShareActiveObject if true then the Active Object from found hub will be shared.
        @param bUseAll if true, then each object in hubRoot will be used.  If false, then only the Active Object is used.
    */
    public HubMerger(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bShareActiveObject, boolean bUseAll) {
        this(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, null, bUseAll);
    }
    public HubMerger(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bShareActiveObject, String selectOrder, boolean bUseAll) {
        if (hubRoot == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        if (hubCombinedObjects == null) {
            throw new IllegalArgumentException("Combined hub can not be null");
        }
        init(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, selectOrder, bUseAll);
    }
    public HubMerger(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bUseAll) {
        this(hubRoot, hubCombinedObjects, propertyPath, false, bUseAll);
    }

    public HubMerger(OAObject obj, Hub hubCombinedObjects, String propertyPath) {
        Hub h = new Hub(obj.getClass());
        h.add(obj);
        init(h, hubCombinedObjects, propertyPath, bUseAll, propertyPath, true);
    }
    
    /**
     * This needs to be set to true if it is only created on the server, but
     * client applications will be using the same Hub that is filtered.
     * This is so that changes on the hub will be published to the clients, even if 
     * initiated on an OAClientThread. 
     */
    public void setServerSideOnly(boolean b) {
        bServerSideOnly = b;
    }

    private void init(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bShareActiveObject, String selectOrder, boolean bUseAll) {
        HubData hd = null;
        try {
            // 20120624 hubCombined could  be a detail hub.
            OAThreadLocalDelegate.setSuppressCSMessages(true);
            if (hubCombinedObjects != null && !hubCombinedObjects.data.bInFetch) {
                hd = hubCombinedObjects.data;
                hd.bInFetch = true;
            }
            _init(hubRoot, hubCombinedObjects, propertyPath, bShareActiveObject, selectOrder, bUseAll);
        }
        finally {
            if (hd != null) hd.bInFetch = false;
            OAThreadLocalDelegate.setSuppressCSMessages(false);
        }
    }
    
    private void _init(Hub hubRoot, Hub hubCombinedObjects, String propertyPath, boolean bShareActiveObject, String selectOrder, boolean bUseAll) {
        this.hubRoot = hubRoot;
        this.hubCombined = hubCombinedObjects;
        this.path = propertyPath;
        this.bShareActiveObject = bShareActiveObject;
        this.bUseAll = bUseAll;
        createNodes();  // this will create nodeRoot
        
        this.dataRoot = new Data(nodeRoot, null, hubRoot);
        nodeRoot.data = dataRoot;
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
            if (bServerSideOnly) { // 20120505
                OARemoteThreadDelegate.sendMessages(); // so that events will go out, even if OAClientThread
            }
            if (!bShareEndHub) hubCombined.clear();
            dataRoot.onNewList(null);
            dataRoot.afterChangeActiveObject(null);
        }
    }
    public boolean getEnabled() {
        return this.bEnabled;
    }

    public String getPath() {
        return this.path;
    }
    
    private String description;
    public void setDescription(String desc) {
        description = desc;
    }
    public String getDescription() {
        return description;
    }
    
    /**
        Note: if multiple threads are making changes that affect the node data, then 
        errors could show up.
     */
    public void verify() {
        // qqqqq todo: needs to verify recursive data
if (true) return;       
        if (!bEnabled) return;
        //XOG.finest("verifing nodes");
        // Nodes
        for (Node node = nodeRoot ; node != null; node = node.child) {
            if (node.clazz == null) LOG.warning("node.clazz == null");
            if (node.liFromParentToChild == null) {
                if (node != nodeRoot) LOG.warning("liFromParentToChild == null for Node:"+node.property);
            }
            else if (node.liFromParentToChild.getType() == OALinkInfo.ONE) {
                if (node.data == null) {
                    if (bUseAll) {
                        // this might not be a problem, since the properties could be null
                        // LOG.warning("Node: "+node.property+" is used for type=One but data is null");
                    }
                }
                else if (node.data.parentObject != null) {
                    LOG.warning("Node: "+node.property+" is used for type=One and parentObject != null");
                }
            }
            else { // Many
                if (node.data != null) {
                    LOG.warning("Node: "+node.property+" is type=Many data != null");
                }
            }
        }

        // verify hubCombinued objects are used
        if (!bShareEndHub) {
            for (int i=0; ; i++) {
                Object obj = hubCombined.getAt(i);
                if (obj == null) break;
                if (!isUsed(obj)) {
                    LOG.warning("Object in hubCombined is not used");
                }
            }
        }
        
        //XOG.finest("verifying data");
        dataRoot.verify();
        for (Node node = nodeRoot; node != null; node = node.child) {
            if (node.data != null) node.data.verify();
        }
        //XOG.finest("verify complete");
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
     * This can be overwritten to get the remove event from the parent,
     * instead of getting the remove event from the combinedHub.<br>
     * Since remove will also set the masterObject property to null,
     * this can be used before it is set to null.
     * @param e
     */
    protected void beforeRemoveRealHub(HubEvent e) {
    }
    protected void afterRemoveRealHub(HubEvent e) {
    }
    protected void beforeRemoveAllRealHub(HubEvent e) {
    }
    
    
    /**
     * This can be overwritten to get the move event from the parent,
     * instead of getting the move event from the combinedHub.
     */
    protected void afterMoveRealHub(HubEvent e) {
    }
    /**
     * This can be overwritten to get the insert event from the parent,
     * instead of getting the insert event from the combinedHub.
     */
    protected void beforeInsertRealHub(HubEvent e) {
    }
    /**
     * This can be overwritten to get the insert event from the parent,
     * instead of getting the insert event from the combinedHub.
     */
    protected void afterInsertRealHub(HubEvent e) {
    }
    /**
     * This can be overwritten to get the add event from the parent,
     * instead of getting the add event from the combinedHub.
     */
    protected void beforeAddRealHub(HubEvent e) {
    }
    /**
     * This can be overwritten to get the add event from the parent,
     * instead of getting the add event from the combinedHub.
     */
    protected void afterAddRealHub(HubEvent e) {
    }
    
/*    
//qqqqqq not sure if this is used    
//  check to see if this, and Data.getChildrenCount() can be removed    
    public int getChildrenCount() {
        if (!bEnabled) return 0;
        int cnt = 0;

        Node node = nodeRoot;
        for ( ; node != null; node = node.child) {
            if (node.data != null) cnt += node.data.getChildrenCount();
        }
// this needs to consider what to do if recursive is included       
        cnt += dataRoot.getChildrenCount();
        return cnt;
    }
*/    
    protected void createNodes() {
        bShareEndHub = !bUseAll;  
        Class clazz = hubRoot.getObjectClass();

        // 20120809 using new OAPropertyPath
        OAPropertyPath oaPropPath = new OAPropertyPath(path);
        try {
            oaPropPath.setup(clazz);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cant find property for PropertyPath=\""+path+"\" starting with Class "+hubRoot.getObjectClass().getName(), e);
        }
        String[] pps = oaPropPath.getProperties();
        Method[] methods = oaPropPath.getMethods();
        Class[] classes = oaPropPath.getClasses();
        
        
        nodeRoot = new Node();
        nodeRoot.clazz = clazz;
        Node node = nodeRoot;
        boolean bLastWasMany = false;

        for (int i=0 ; ; i++) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo recursiveLinkInfo = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.MANY);
            Node recursiveNode = null;
            if (bLastWasMany && recursiveLinkInfo != null) {
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
                throw new IllegalArgumentException("Cant find "+prop+" for PropertyPath \""+path+"\" starting with Class "+hubRoot.getObjectClass().getName());
            }
            bLastWasMany = linkInfo.getType() == linkInfo.MANY;
            
            if (bShareEndHub) {
                if (linkInfo.getType() == OALinkInfo.MANY) {
                    if (i+1 <= pps.length) bShareEndHub = false;  // only the last one can be many
                }
                else {
                    if (i+1 == pps.length) bShareEndHub = false;  // only the last one can be many, but not one
                }
            }

            Node node2 = new Node();
            node2.property = prop;
            node2.liFromParentToChild = linkInfo;

            clazz = classes[i];
            
            node2.clazz = clazz;
            
            node.child = node2;
            node = node2;

            if (recursiveNode != null) {
                recursiveNode.child = node2;
            }
        }
        // verify that last property is same class as hubCombined
        if (hubCombined.getObjectClass() == null) HubDelegate.setObjectClass(hubCombined, clazz);
        if (!hubCombined.getObjectClass().equals(clazz)) {
//            if (!OAObject.class.equals(clazz)) { // 20120809 could be using generic type reference (ex: OALeftJoin.A)
                throw new IllegalArgumentException("Classes do not match.  Property path \""+path+"\" is for objects of Class "+clazz.getName() + " and hubCombined is for objects of Class "+hubCombined.getObjectClass());
//            }
        }
    }

    public void close() {
        //LOG.finer("closing");
        if (nodeRoot == null) return;
        bIgnoreIsUsedFlag = true;
        dataRoot.close();
        Node node = nodeRoot;
        while (node != null) {
            node.close();
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
        Node child;
        Node recursiveChild;
        Data data;  // first node for root and used for Hub for link.type = One
        void close() {
            if (data != null) data.close();
            data = null;
        }
        public @Override String toString() {
            String s = liFromParentToChild == null ? "root" : liFromParentToChild.getType()==OALinkInfo.MANY?"Many":"One"; 
            s = "class: "+clazz+", property: "+property+", type:"+s;
            return s;
        }
    }

    class Data extends HubListenerAdapter {
        Node node;
        OAObject parentObject;  // parent object of hub
        Hub hub;
        volatile ArrayList<Data> alChildren;
        boolean bHubListener;
        
        Data(Node node, OAObject parentObject, Hub hub) {
            if (hub == null) {
                throw new RuntimeException("hub can not be null");
            }
            if (!node.clazz.equals(hub.getObjectClass())) {
                // 20130709
                if (!OAObject.class.isAssignableFrom(node.clazz)) { 
                    throw new RuntimeException("Hub class does not equal Node class");
                }
                /*was
                if (!OAObject.class.equals(node.clazz)) { // 20120809 could be using generic type reference (ex: OALeftJoin.A)
                    throw new RuntimeException("Hub class does not equal Node class");
                }
                */
            }
            this.node = node;
            this.parentObject = parentObject;
            this.hub = hub;
            
            hub.addHubListener(this);
            bHubListener = true;            
            HubListenerCount++;         
            TotalHubListeners++;            
            createChildren();
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
                            if (Math.abs(x1-x2) > 1) LOG.warning("alChildren.size="+x1+" != hub.getSize="+x2);
                        }
                    }
                    else {
                        int x = (hubRoot.getAO() == null) ? 0 : 1;
                        if (node.recursiveChild != null) x *= 2; 
                        if (alChildren.size() != x) {
                            LOG.warning("bUseAll=false, alChildren.size != "+x);
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
                for (int i=0; hub!=null; i++) {
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
                if (node.child == null && bShareEndHub && this.hub != hubCombined.getSharedHub()) {
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
                                    if (Math.abs(x1-x2) > 1) LOG.warning("alChildren.size="+x1+" != hub.getSize="+x2);
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
                    
                    
                    
                    for (int i=0; hub!=null; i++) {
                        Object obj = hub.getAt(i);
                        if (obj == null) break;
                        if (node.child == null && !hubCombined.contains(obj)) {
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
                                if (Math.abs(x1-x2) > 1) LOG.warning("recursive alChildren.size="+x1+" != hub.getSize="+x2);
                            }
                        }                       
                    }
                    
                    if (!bShareEndHub) {
                        for (int i=0; hub!=null; i++) {
                            Object obj = hub.getAt(i);
                            if (obj == null) break;
                            if (!hubCombined.contains(obj)) LOG.warning("object not in hubCombined");
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
            //XOG.finer("createChildren");
            
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
                    hubCombined.setSharedHub(hub, bShareActiveObject);
                }
                else {
                    for (int i=0; ;i++) {
                        OAObject obj = (OAObject) hub.elementAt(i);
                        if (obj == null) break;
                        createChild(obj);
                    }
                }
            }
            else {
                if (bUseAll || this.node != nodeRoot && nodeRoot != null) {
                    OAThreadLocal tl;
                    Hub hubx = OAThreadLocalDelegate.setGetDetailHub(hub);
                    try {
                        for (int i=0; ;i++) {
                            OAObject obj = (OAObject) hub.elementAt(i);
                            if (obj == null) break;
                            createChild(obj);
                        }
                    }
                    finally {
                        OAThreadLocalDelegate.resetGetDetailHub(hubx);
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

        // 20110809 see if the the masterHub/Object can be used.  This is for cases where hub.size=0, but you want 
        //    to have the merger get objects based on master.  ex: OrderContacts propPath "order.customer.contacts" for a 
        //      hub to link and autocreate the orderContact objects
        void createChildUsingMaster() {
            if (!bEnabled) return;
            //XOG.finer("createChild");
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
                    node.child.data.hub.add(ref);  // this will send afterAdd(), which will create children
                }
                
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) {  // could have been closed in another thread
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
                    if (alChildren != null) {  // could have been closed in another thread
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
            if (!bEnabled) return;
            //XOG.finer("createChild");
            if (node.child == null) {
                if (!bShareEndHub && !hubCombined.contains(parent)) {
                    OARemoteThreadDelegate.sendMessages(); 
                    hubCombined.add(parent);
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
                        node.child.data.hub.add(ref);  // this will send afterAdd(), which will create children
                    }
                }
                try {
                    lock.writeLock().lock();
                    if (alChildren != null) {  // could have been closed in another thread
                        this.alChildren.add(node.child.data); // even if obj==null, so that verify will work - it looks for alChildren.size=1
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
                    if (alChildren != null) {  // could have been closed in another thread
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

            Hub h = (Hub) node.recursiveChild.liFromParentToChild.getValue(parent);
            Data d = new Data(node.recursiveChild, parent, h);
            try {
                lock.writeLock().lock();
                if (alChildren != null) {  // could have been closed in another thread
                    alChildren.add(d);
                }
            }
            finally {
                lock.writeLock().unlock();
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
                    for (int i=0; ; i++) {
                        OAObject obj = (OAObject) this.hub.elementAt(i);
                        if (obj == null) break;
                        OAObject ref = (OAObject) node.child.liFromParentToChild.getValue(obj);
                        if (ref == objFind) return true;
                    }
                }
            }
            else {
                try {
                    lock.readLock().lock();
                    if (alChildren != null) {
                        for (Data data : alChildren) {
                            if (data._isUsed(objFind, nodeFind)) return true;
                        }
                    }
                }
                finally {
                    lock.readLock().unlock();
                }
            }
            return false;
        }
        
        
        

        public @Override String toString() {
            String s = "";
            if (hub != null) s = ", hub:"+hub.getObjectClass().getName()+", cnt:"+hub.getSize();
            return node.property + ", parent:"+parentObject+s;
        }
        
        void remove(Object obj) {
            if (!bEnabled) return;
            
            if (alChildren == null || node.child == null) {
                if (isUsed(obj)) {
                    // needs to remove from alChildren, ex: when using recursive properties
                    // was: return; 
                }
                else if (!bShareEndHub) {
                    if (this.hub == hubCombined) {
                        if (!hubCombined.contains(obj)) {
                            return;  // might have already been removed
                        }
                    }
                    if (bServerSideOnly) {
                        OARemoteThreadDelegate.sendMessages(); 
                    }
                    if (OAThreadLocalDelegate.isHubMergerChanging()) { // 20120102
                        // 20120612 dont send event, unless there is a recursive prop, which needs to have recursives nodes updated                        
                        HubAddRemoveDelegate.remove(hubCombined, obj, false, bIsRecusive, false, false, false);
                        // was: HubAddRemoveDelegate.remove(hubCombined, obj, false, false, false, false, false);
                    }
                    else {
                        hubCombined.remove(obj);
                    }
                }
                if (alChildren == null) {
                    return;
                }
            }
            

            for (int alPos=0; ; alPos++) {
                Data child;
                try {
                    lock.readLock().lock();
                    if (alChildren == null || alPos >= alChildren.size()) break;
                    child = alChildren.get(alPos);
                }
                finally {
                    lock.readLock().unlock();
                }
                if (obj == child.parentObject) {  // will always be a type=Many
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
                                HubAddRemoveDelegate.remove(child.hub, ref, false, false, false, false, false);
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
            //XOG.finer("close");
            if (hub != null) {
                hub.removeHubListener(this);
                if (bHubListener) HubListenerCount--;
                bHubListener=false;
                TotalHubListeners--;            
            }

            boolean bLockSet = true;
            try {
                lock.readLock().lock();
                if (alChildren == null || node.child == null) {
                    if (bShareEndHub) {
                        // 20110809 might need to unset hubCombied.sharedHub
                        hubCombined.setSharedHub(null);
                        return;
                    }
                    if (hub != null) {
                        Object[] objs = hub.toArray();
                        lock.readLock().unlock();
                        bLockSet = false;
                        for (int i=0; i<objs.length; i++) {
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
                // dont call close on Node.data.  This will instead use remove()
                Object[] objs = hub.toArray();
                for (int i=0; i<objs.length; i++) {
                    remove(objs[i]);
                }
            }
            else {
                for (; ; ) {
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
            Hub h = e.getHub();
            if (h.getObjectClass().equals(hubCombined.getObjectClass())) {
                HubMerger.this.beforeRemoveAllRealHub(e);
            }
            
            if (!bEnabled) return;
            if (this != dataRoot) return;  
            if (!bUseAll) return;
            if (hub.isLoading()) return;
            
            boolean hold = bIgnoreIsUsedFlag;
            bIgnoreIsUsedFlag = true;
            for (int i=0; ;i++) {
                Object obj = hub.getAt(i);
                if (obj == null) break;
                remove(obj);
            }
            if (!hold) bIgnoreIsUsedFlag = false;
        }

        public @Override void onNewList(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
               _onNewList(e);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }        
        public void _onNewList(HubEvent e) {
            HubData hd = null;
            try {
                if (this.hub != hubCombined) {
                    if (!hubCombined.data.bInFetch) {
                        hd = hubCombined.data;
                        hd.bInFetch = true;
                    }
                }
                _onNewList();
            }
            finally {
                if (hd != null) hd.bInFetch = false;
            }
            if (this.hub != hubCombined) {
                if (hubCombined.getSharedHub() != this.hub) {
                    if (!bShareEndHub) { // 20110521
                        HubEventDelegate.fireOnNewListEvent(hubCombined, true);
                    }
                }
            }
        }        
        
        private void _onNewList() {
            if (!bEnabled) return;
            if (this != dataRoot) return;
            if (!bUseAll) {
                // 20110809 need to continue if there is a masterObject/Hub and AO=null
                //     in case masterObject was previously null (making hub invalid)
                if (this.hub.getMasterHub() == null) {
                    return;
                }
                // was: return;
            }

            bIgnoreIsUsedFlag = true;
            if (node.child != null && node.child.liFromParentToChild.getType() == OALinkInfo.ONE) {
                // dont call close on Node.data.  This will instead call remove()
                for (;node.child.data!=null && node.child.data.hub!=null;) {
                    Object obj = node.child.data.hub.getAt(0);
                    if (obj == null) break;
                    if (OAThreadLocalDelegate.isHubMergerChanging()) { // 20120102
                        HubAddRemoveDelegate.remove(node.child.data.hub, obj, false, false, false, false, false);
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
                for (; ; ) {
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
                    if (obj.getClass().equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeRemoveRealHub(e);
                    }
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
        public @Override void afterRemove(HubEvent e) {
            Object obj = e.getObject();
            if (obj != null) {
                if (obj.getClass().equals(hubCombined.getObjectClass())) {
                    HubMerger.this.afterRemoveRealHub(e);
                }
            }
            if (!bEnabled) return;
            if (this == dataRoot && !bUseAll) return;
            if (hub.isLoading()) {
                return;
            }
            try {
                // 20120903 removed/commented this, and need to have hub event sent out for remove
                //if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                remove(obj);
            }
            finally {
                //if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
        @Override
        public void beforeAdd(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    if (obj.getClass().equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeAddRealHub(e);
                    }
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
        public @Override void afterAdd(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    if (obj.getClass().equals(hubCombined.getObjectClass())) {
                        HubMerger.this.afterAddRealHub(e);
                    }
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
            if (hub.isLoading()) return;
            createChild((OAObject) e.getObject());
        }
        
        @Override
        public void beforeInsert(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    if (obj.getClass().equals(hubCombined.getObjectClass())) {
                        HubMerger.this.beforeInsertRealHub(e);
                    }
                }
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
        public @Override void afterInsert(HubEvent e) {
            try {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(true);
                Object obj = e.getObject();
                if (obj != null) {
                    if (obj.getClass().equals(hubCombined.getObjectClass())) {
                        HubMerger.this.afterInsertRealHub(e);
                    }
                }
                afterAdd2(e);
            }
            finally {
                if (hub == hubRoot) OAThreadLocalDelegate.setHubMergerIsChanging(false);
            }
        }
        
        @Override
        public void afterMove(HubEvent e) {
            Hub h = e.getHub();
            if (h != null && h.getObjectClass().equals(hubCombined.getObjectClass())) {
                HubMerger.this.afterMoveRealHub(e);
            }
        }
    
        public @Override void afterPropertyChange(HubEvent e) {
            if (!bEnabled) return;
            if (node.child == null) return;  // last nodes
            String prop = e.getPropertyName();
            if (prop == null) return;

            if (!node.child.liFromParentToChild.getName().equalsIgnoreCase(prop)) return;

            if (node.child.liFromParentToChild.getType() != OALinkInfo.ONE) return;
            
            // 20110324 data might not have been created,
            if (node.child.data == null) return;

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
                if (!node.child.data.hub.contains(ref)) node.child.data.hub.add(ref);
            }
        }
        public @Override void afterChangeActiveObject(HubEvent evt) {
            if (!bEnabled) return;
            if (this.node != nodeRoot || bUseAll) return;
            
            // 20110809 if the AO is the same, then this can be skipped
            if (evt != null && alChildren != null && alChildren.size() > 0) { 
                Data d = alChildren.get(0);
                if (d.parentObject == evt.getObject()) return;
            }
            
            if (!bShareEndHub) hubCombined.clear();
            HubData hd = hubCombined.data;
            boolean b = hd.bInFetch;
            try {
                hd.bInFetch = true;
                _afterChangeActiveObject();
            }
            finally {
                hd.bInFetch = b;
            }
            // 20110419 param was true, but this should only send to other hubs that share this one
            HubEventDelegate.fireOnNewListEvent(hubCombined, false);  
        }
        private void _afterChangeActiveObject() {
            try {
                lock.writeLock().lock();
                if (alChildren != null && alChildren.size() > 0) {
                    if (node.child.liFromParentToChild.getType() == OALinkInfo.ONE) {
                        alChildren.remove(0);
                        Object obj = node.child.data.hub.getAt(0);
                        if (obj != null) node.child.data.hub.remove(obj);
                    }
                    else {
                        // 20120523 added loop
                        for (Data child : alChildren) {
                           child.close();
                        }
                        alChildren.clear();
                        
                        /*qqqqqqq was:
                        Data child;
                        child = alChildren.get(0);
                        alChildren.remove(0);
                        child.close();
                        */
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

}


