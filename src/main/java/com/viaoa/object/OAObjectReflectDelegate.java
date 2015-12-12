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
package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteSessionInterface;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.OASelect;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.hub.HubDelegate;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubLinkDelegate;
import com.viaoa.hub.HubMerger;
import com.viaoa.hub.HubSelectDelegate;
import com.viaoa.hub.HubShareDelegate;
import com.viaoa.hub.HubSortDelegate;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OANullObject;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

public class OAObjectReflectDelegate {

    private static Logger LOG = Logger.getLogger(OAObjectReflectDelegate.class.getName());

    /**
     * Create a new instance of an object. If OAClient.client exists, this will create the object on the
     * server, where the server datasource can initialize object.
     */
    public static Object createNewObject(Class clazz) {
        Object obj = _createNewObject(clazz);
        return obj;
    }

    private static Object _createNewObject(Class clazz) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        Object obj = null;

        if (!oi.getLocalOnly()) {
            RemoteSessionInterface rc = OASyncDelegate.getRemoteSession(clazz);
            if (rc != null) {
                obj = rc.createNewObject(clazz);
                return obj;
            }
        }

        try {
            Constructor constructor = clazz.getConstructor(new Class[] {});
            obj = constructor.newInstance(new Object[] {});
        }
        catch (InvocationTargetException te) {
            throw new RuntimeException("OAObject.createNewObject() cant get constructor() for class " + clazz.getName() + " "
                    + te.getCause());
        }
        catch (Exception e) {
            throw new RuntimeException("OAObject.createNewObject() cant get constructor() for class " + clazz.getName() + " " + e);
        }
        return obj;
    }

    
    public static Object getProperty(Hub hub, String propName) {
        return getProperty(hub, null, propName);
    }
    
    /**
     * @see OAObject#getProperty(String)
     */
    public static Object getProperty(OAObject oaObj, String propName) {
        return getProperty(null, oaObj, propName);
    }
    public static Object getProperty(Hub hubLast, OAObject oaObj, String propName) {
        if (propName == null || propName.trim().length() == 0) return null;
        if (hubLast == null && oaObj == null) return null;

        if (propName.indexOf('.') < 0) {
            return _getProperty(hubLast, oaObj, propName);
        }
        StringTokenizer st = new StringTokenizer(propName, ".", false);

        for (;;) {
            String tok = st.nextToken();
            Object value = _getProperty(hubLast, oaObj, tok);
            if (value == null || !st.hasMoreTokens()) return value;
            if (!(value instanceof OAObject)) {
                if (!(value instanceof Hub)) break;
                hubLast = (Hub) value;
                value = hubLast.getAO();
            }
            else {
                hubLast = null;
            }
            oaObj = (OAObject) value;
        }
        return null;
    }

    
    private static Object _getProperty(Hub hubLast, OAObject oaObj, String propName) {
        OAObjectInfo oi;
        if (hubLast != null) {
            oi = OAObjectInfoDelegate.getOAObjectInfo(hubLast.getObjectClass());
        }
        else oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

        Method m;
        if (oi.isHubCalcInfo(propName)) {
            if (hubLast == null) return null;
            m = OAObjectInfoDelegate.getMethod(oi, "get" + propName, 1);
            try {
                return m.invoke(oaObj, hubLast);
            }
            catch (InvocationTargetException e) {
                LOG.log(Level.WARNING, "error calling " + oaObj.getClass().getName() + ".getProperty(\"" + propName + "\")",
                        e.getTargetException());
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error calling " + oaObj.getClass().getName() + ".getProperty(\"" + propName + "\")", e);
            }
            return null;
        }
        else {
            if (oaObj == null) return null;
            m = OAObjectInfoDelegate.getMethod(oi, "get" + propName, 0);
            if (m == null) {
                m = OAObjectInfoDelegate.getMethod(oi, "is" + propName, 0);
                if (m == null) return null;
            }
            if (m != null && (m.getModifiers() & Modifier.PRIVATE) == 0) {            
                if (getPrimitiveNull(oaObj, propName)) return null;
                try {
                    return m.invoke(oaObj, null);
                }
                catch (InvocationTargetException e) {
                    String s;
                    if (oaObj != null) s = oaObj.getClass().getName();
                    else s = "object is null, ?";
                    LOG.log(Level.WARNING, "error calling " + s + ".getProperty(\"" + propName + "\")",
                            e.getTargetException());
                }
                catch (Exception e) {
                    String s;
                    if (oaObj != null) s = oaObj.getClass().getName();
                    else s = "object is null, ?";
                    LOG.log(Level.WARNING, "error calling " + s + ".getProperty(\"" + propName + "\")", e);
                }
                return null;
            }
        }
        
        // check to see if it is in the oaObj.properties
        Object objx = OAObjectPropertyDelegate.getProperty(oaObj, propName, false, true);
        return objx;
    }

    /**
     * @see OAObject#setProperty(String, Object, String) This can also be used to add Objects (or
     *      ObjectKeys) to a Hub. When the Hub is then retrieved, the value will be converted to
     *      OAObject subclasses.
     */
    public static void setProperty(OAObject oaObj, String propName, Object value, String fmt) {
        
        if (oaObj == null || propName == null || propName.length() == 0) {
            LOG.log(Level.WARNING, "property is invalid, =" + propName, new Exception());
            return;
        }

        // 20120822 add support for propertyPath
        if (propName.indexOf('.') >= 0) {
            int pos = propName.lastIndexOf('.');
            String s = propName.substring(0, pos);
            propName = propName.substring(pos + 1);

            Object objx = getProperty(oaObj, s);
            if (objx instanceof OAObject) {
                setProperty((OAObject) objx, propName, value, fmt);
            }
            return;
        }

        boolean bIsLoading = OAThreadLocalDelegate.isLoadingObject();

        String propNameU = propName.toUpperCase();
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        Method m = OAObjectInfoDelegate.getMethod(oi, "SET" + propNameU, 1);

        Class clazz = null;
        if (m != null) {
            // a "real" property
            Class[] cs = m.getParameterTypes();
            if (cs.length == 1) clazz = cs[0];
        }

        Object previousValue = null;

        if (clazz == null) {
            // See if this is for a Hub.  OAXMLReader uses setProperty to set MANY references using Object Id value
            m = OAObjectInfoDelegate.getMethod(oi, "GET" + propNameU, 0);
            if (m != null) {
                clazz = m.getReturnType();
                if (clazz != null && clazz.equals(Hub.class)) {
                    setHubProperty(oaObj, propName, propNameU, value, oi, fmt);
                    return;
                }
            }
            if (!bIsLoading) previousValue = oaObj.getProperty(propName);

            OAObjectEventDelegate.fireBeforePropertyChange(oaObj, propName, previousValue, value, oi.getLocalOnly(), true);
            OAObjectPropertyDelegate.setProperty(oaObj, propName, value);
            OAObjectEventDelegate.firePropertyChange(oaObj, propName, previousValue, value, oi.getLocalOnly(), true);
            return;
        }

        if (value instanceof OANullObject) value = null;
        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propNameU);

        if (li != null) {
            if (bIsLoading) {
                if (value == null) {
                    // 20110315 allow null to be set
                    OAObjectPropertyDelegate.setProperty(oaObj, propName, value);
                    //was: OAObjectPropertyDelegate.removeProperty(oaObj, propName, true);
                }
                else {
                    if (!(value instanceof OAObject) && !(value instanceof OAObjectKey)) {
                        value = OAObjectKeyDelegate.convertToObjectKey(oi, value);
                    }
                    OAObjectPropertyDelegate.setProperty(oaObj, propName, value);
                }
                return;
            }
            previousValue = OAObjectPropertyDelegate.getProperty(oaObj, propName, false, true); // get previous value
        }

        boolean bPrimitiveNull = false; // a primitive type that needs to be set to null value
        if (li == null) {
            if (value == null && clazz.isPrimitive()) {
                bPrimitiveNull = true;
            }
            else {
                if (value != null || !clazz.equals(String.class)) { // conversion will convert a null to a String "" (blank)
                    value = OAConverter.convert(clazz, value, fmt); // convert to right type of class value
                }
            }
        }
        else if (value == null) { // must be a reference property, being set to null value.
            if (previousValue == null) return; // no change
        }
        else if ((value instanceof OAObject)) { // reference property, that is an OAObject class type value
            if (previousValue == value) return;
            if (previousValue instanceof OAObjectKey) {
                OAObjectKey k = OAObjectKeyDelegate.getKey((OAObject) value);
                if (k.equals(previousValue)) {
                    OAObjectPropertyDelegate.setProperty(oaObj, propName, value);
                    return; // no change, was storing key, now storing oaObject
                }
            }
        }
        else { //  (value NOT instanceof OAObject) either OAObjectKey or value of key 
            if (!(value instanceof OAObjectKey)) {
                value = OAObjectKeyDelegate.convertToObjectKey(oi, value);
            }
            if (value.equals(previousValue)) return; // no change
            if (previousValue instanceof OAObject) {
                OAObjectKey k = OAObjectKeyDelegate.getKey((OAObject) previousValue);
                if (k.equals(value)) return; // no change
            }

            // have to get the real object
            Object findValue = getObject(li.toClass, value);
            if (findValue == null) {
                throw new RuntimeException("Cant find object for Id: " + value + ", class=" + li.toClass);
            }
            value = findValue;
        }

        boolean bCallSetMethod = true;
        try {
            if (bPrimitiveNull) {
                if (!bIsLoading) {
                    previousValue = getProperty(oaObj, propName);
                    if (previousValue == null) return; // no change 
                }
                value = OAReflect.getPrimitiveClassWrapperObject(clazz);
                if (value == null) bCallSetMethod = false; // cant call the setMethod, since it is a primitive type that cant be represented with a value
                else if (value.equals(previousValue)) bCallSetMethod = false; // no change, dont need to set the default value.
            }
            if (bCallSetMethod) {
                m.invoke(oaObj, new Object[] { value });
            }
        }
        catch (Exception e) {
            String s = "property=" + propName + ", obj=" + oaObj + ", value=" + value;
            LOG.log(Level.WARNING, s, e);
            // e.printStackTrace();
            throw new RuntimeException("Exception in setProperty(), " + s, e);
        }
        finally {
            if (bPrimitiveNull) {
                // 20131101 calling firePropetyChange will call setPrimitiveNull
                // setPrimitiveNull(oaObj, propNameU);
                OAObjectEventDelegate.firePropertyChange(oaObj, propName, previousValue, null, oi.getLocalOnly(), true); // setting to null
            }
        }
    }

    /**
     * used for "quick" storing/loading objects
     */
    public static void storeLinkValue(OAObject oaObj, String propertyName, Object value) {
        if (!(value instanceof OAObject) && !(value instanceof OAObjectKey)) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            value = OAObjectKeyDelegate.convertToObjectKey(oi, value);
        }
        OAObjectPropertyDelegate.setProperty(oaObj, propertyName, value);
    }

    /* Used to flag primitive property as having a null value. */
    public static boolean getPrimitiveNull(OAObject oaObj, String propertyName) {
        if (oaObj == null || propertyName == null) return false;
        if (oaObj.nulls == null || oaObj.nulls.length == 0) return false;
        synchronized (oaObj) {
            if (oaObj.nulls == null || oaObj.nulls.length == 0) return false;
            return OAObjectInfoDelegate.isPrimitiveNull(oaObj, propertyName);
        }
    }

    // note: a primitive null can only be set by calling OAObjectReflectDelegate.setProperty(...)
    protected static void setPrimitiveNull(OAObject oaObj, String propertyName) {
        if (propertyName == null) return;
        synchronized (oaObj) {
            OAObjectInfoDelegate.setPrimitiveNull(oaObj, propertyName, true);
        }
    }

    // note: a primitive null can only be removed by OAObjectEventDelegate.firePropertyChagnge(...)
    protected static void removePrimitiveNull(OAObject oaObj, String propertyName) {
        if (oaObj.nulls == null || oaObj.nulls.length == 0) return;
        if (propertyName == null) return;
        synchronized (oaObj) {
            OAObjectInfoDelegate.setPrimitiveNull(oaObj, propertyName, false);
        }
    }

    // called by setProperty() when property is a Hub.
    private static void setHubProperty(OAObject oaObj, String propName, String propNameU, Object value, OAObjectInfo oi, String fmt) {
        // this is for a Hub.  OAXMLReader uses setProperty to set MANY references using Object Id value for objects
        if (value == null) return;

        Hub hub;
        Object objOrig = OAObjectPropertyDelegate.getProperty(oaObj, propName, false, true);

        if (value instanceof Hub) {
            OAObjectPropertyDelegate.setPropertyCAS(oaObj, propName, value, objOrig);
            return;
        }

        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propNameU);
        if (li == null) return;

        if (objOrig != null) {
            if (!(objOrig instanceof Hub)) throw new RuntimeException("stored object for " + propName + " is not a hub");
            hub = (Hub) objOrig;
        }
        else {
            hub = new Hub(OAObjectKey.class);
            HubDetailDelegate.setMasterObject(hub, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(li));
            OAObjectPropertyDelegate.setProperty(oaObj, propName, hub);
        }

        Class c = hub.getObjectClass();
        boolean bKeyOnly = (c.equals(OAObjectKey.class));

        if (!(value instanceof OAObject)) {
            if (!(value instanceof OAObjectKey)) { // convert to OAObjectKey
                if (value instanceof Hub) throw new RuntimeException("cant not set the Hub for " + propName);
                value = OAObjectKeyDelegate.convertToObjectKey(li.toClass, value);
            }
        }

        if (bKeyOnly) {
            if (value instanceof OAObject) value = OAObjectKeyDelegate.getKey((OAObject) value);
        }
        else {
            if (value instanceof OAObjectKey) {
                value = OAObjectReflectDelegate.getObject(c, value);
            }
        }
        if (value != null && hub.getObject(value) == null) hub.add(value);
    }

    /**
     * DataSource independent method to retrieve an object. Find the OAObject given a key value. This
     * will look in the Cache, Server (if running as workstation) and the DataSource (if not running as
     * workstation).
     * 
     * @param clazz
     *            class of reference of to find.
     * @param key
     *            can be the value of the key or an OAObjectKey
     */
    public static OAObject getObject(Class clazz, Object key) {
        if (clazz == null || key == null) return null;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        return getObject(clazz, key, oi);
    }

    public static OAObject getObject(Class clazz, Object key, OAObjectInfo oi) {
        if (clazz == null || key == null) return null;

        if (!(key instanceof OAObjectKey)) {
            key = OAObjectKeyDelegate.convertToObjectKey(clazz, key);
        }

        OAObject oaObj = OAObjectCacheDelegate.get(clazz, (OAObjectKey) key);
        if (oaObj == null) {
            if (OASync.isClient(clazz) && (oi == null || !oi.getLocalOnly())) {
                oaObj = (OAObject) OAObjectCSDelegate.getServerObject(clazz, (OAObjectKey) key);
            }
            else {
                oaObj = (OAObject) OAObjectDSDelegate.getObject(clazz, (OAObjectKey) key);
            }
        }
        return oaObj;
    }

    /**
     * DataSource independent method to retrieve a reference property that is a Hub Collection.
     * 
     * @param linkPropertyName name of property to retrieve. (case insensitive)
     * @param sortOrder
     * @param bSequence
     *            if true, then create a hub sequencer to manager the order of the objects in the hub.
     */
    public static Hub getReferenceHub(OAObject oaObj, String linkPropertyName, 
            String sortOrder, boolean bSequence, Hub hubMatch) {
        /*
         lock obj.props[]
           get Hub from oaObj.props[]
           if exists, but is null, then create an empty Hub
           could be weakref, then get value
           if not exists, then set to null
           if hub.objClass is objectKey, then need to create new hub can load using keys
           if client, then get on server, else get from DS
           store hub in props: if hub is cached, then use weakref
         unlock obj.props[]
        */
        if (linkPropertyName == null) return null;

        Hub hub = null;
        OAObjectInfo oi = null;
        OALinkInfo linkInfo = null;

        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, false, true);
        if (obj instanceof Hub) {
            // 20141215 could be server side, that deserialized the object+references without setting up.
            hub = (Hub) obj;
            
            // return if not server
            if (!OAObjectCSDelegate.isServer(oaObj)) return hub;

            // 20150130 the same thread that is loading it could be accessing it again. (ex: matching and hubmerger during getReferenceHub(..))
            if (OAObjectPropertyDelegate.isPropertyLocked(oaObj, linkPropertyName)) return hub;

            oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, linkPropertyName);
            
            // check to see if there needs to be an autoMatch set up
            if (HubDelegate.getAutoMatch(hub) == null) {
                if (linkInfo != null) {
                    String matchProperty = linkInfo.getMatchProperty();
                    if (matchProperty != null && matchProperty.length() > 0) {
                        if (hubMatch == null) {
                            String matchHubPropPath = linkInfo.getMatchHub();
                            if (matchHubPropPath != null && matchHubPropPath.length() > 0) {
                                OAObjectInfo oix = OAObjectInfoDelegate.getOAObjectInfo(linkInfo.getToClass());
                                OALinkInfo linkInfox = OAObjectInfoDelegate.getLinkInfo(oix, matchProperty);
                                if (linkInfox != null) {
                                    hubMatch = new Hub(linkInfox.getToClass());
                                    HubMerger hm = new HubMerger(oaObj, hubMatch, matchHubPropPath);
                                    hm.setServerSideOnly(true);
                                }
                            }
                        }
                        if (hubMatch != null) {
                            hub.setAutoMatch(matchProperty, hubMatch, true); // serverSide only
                        }
                    }
                }
            }
            
            // check to see if there needs to be a seq
            if (sortOrder != null && sortOrder.length() > 0) {
                if (bSequence) {
                    if (HubDelegate.getAutoSequence(hub) == null) {
                        hub.setAutoSequence(sortOrder); // server will keep autoSequence property updated - clients dont need autoSeq (server side managed)
                    }
                }
                else if (HubSortDelegate.getSortListener(hub) == null){
                    // keep the hub sorted on server only
                    HubSortDelegate.sort(hub, sortOrder, true, null, true);// dont sort, or send out sort msg (since no other client has this hub yet)
                }
            }
        }
        else {
            oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, linkPropertyName);
            
            try {
                OAObjectPropertyDelegate.setPropertyLock(oaObj, linkPropertyName);
    
                obj = OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, false, true);
                if (obj instanceof Hub) {
                    return (Hub) obj;
                }
                
                hub = _getReferenceHub(oaObj, linkPropertyName, sortOrder, bSequence, hubMatch, oi, linkInfo);
                if (hub != null && HubDelegate.getMasterObject(hub) == null) {
                    OAObjectHubDelegate.setMasterObject(hub, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(linkInfo));
                }            
            }
            finally {
                OAObjectPropertyDelegate.releasePropertyLock(oaObj, linkPropertyName);
            }
        }
        
        // 20131129 triggers
        if (hub != null && OAObjectCSDelegate.isServer(oaObj) && linkInfo != null) {
            Class[] cs = linkInfo.getTriggerClasses();
            if (cs != null) {
                for (Class c : cs) {
                    try {
                        Constructor con = c.getConstructor(Hub.class);
                        con.newInstance(hub);
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "error while creating trigger", e);
                    }
                }
            }
        }            
        return hub;
    }
    private static Hub _getReferenceHub(final OAObject oaObj, final String linkPropertyName, String sortOrder, 
            boolean bSequence, Hub hubMatch, final OAObjectInfo oi, final OALinkInfo linkInfo ) {

        Object propertyValue = OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, true, true);
        boolean bThisIsServer = OAObjectCSDelegate.isServer(oaObj);
        // dont get calcs from server, calcs are maintained locally, events are not sent
        boolean bIsCalc = (linkInfo != null && linkInfo.bCalculated);
        boolean bIsServerSideCalc = (linkInfo != null && linkInfo.bServerSideCalc);
      
        // 20141109 need to check for sortProp, sortAsc, 
        if (sortOrder == null  && linkInfo != null) {
            sortOrder = linkInfo.getSortProperty();
        }
        // 20141109 need to check for seqProp, seqAsc, 
        if (!bSequence && linkInfo != null) {
            String s = linkInfo.getSeqProperty();
            if (!OAString.isEmpty(s)) {
                sortOrder = s;
                bSequence = true;
            }
        }

        Hub hub = null;
        if (propertyValue == null) { 
            // since it is in props with a null, then it was placed that way to mean it has 0 objects
            //   by OAObjectSerializeDelegate._writeObject
            if (linkInfo == null) {
                hub = new Hub();
                OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, hub);
                return hub;
            }
            // create an empty hub
            hub = new Hub(linkInfo.toClass, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(linkInfo), false);
        }
        else if (propertyValue == OANotExist.instance) {
            propertyValue = null;
        }

        if (propertyValue instanceof Hub) {
            hub = (Hub) propertyValue;
            Class c = hub.getObjectClass();
            if (!OAObjectKey.class.equals(c)) { 
                if (!bThisIsServer) {
                    boolean bAsc = true;
                    String s = HubSortDelegate.getSortProperty(hub); // use sort order from orig hub
                    if (OAString.isEmpty(s)) s = sortOrder;
                    else bAsc = HubSortDelegate.getSortAsc(hub);
                    if (!bSequence && !OAString.isEmpty(s) && !HubSortDelegate.isSorted(hub)) {
                        // client recvd hub that has sorted property, without sortListener, etc.
                        // note: serialized hubs do not have sortListener created - must be manually done
                        //      this is done here (after checking first), for cases where references are serialized in a CS call.
                        //      - or during below, when it is directly called.
                        HubSortDelegate.sort(hub, s, bAsc, null, true);// dont sort, or send out sort msg
                        hub.resort(); // this will not send out event
                    }
                }
                if (OAObjectInfoDelegate.cacheHub(linkInfo, hub)) {
                    OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, new WeakReference(hub));
                }
                else {
                    OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, hub);
                }
                return hub;
            }
            
            // objects are stored as OAObjectKeys
            // Hub with OAObjectKeys exists, need to convert to "real" objects
            if (linkInfo == null) return null;
            Class linkClass = linkInfo.toClass;
            Hub hubNew = new Hub(linkClass, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(linkInfo), false);
            try {
                OAThreadLocalDelegate.setSuppressCSMessages(true);
                for (int i = 0;; i++) {
                    OAObjectKey key = (OAObjectKey) hub.elementAt(i);
                    if (key == null) break;
                    Object objx = getObject(linkClass, key);
                    if (propertyValue != null) hubNew.add(objx);
                }
                hub = hubNew;
                hub.setChanged(false);
            }
            finally {
                OAThreadLocalDelegate.setSuppressCSMessages(false);
            }
            if (OAObjectInfoDelegate.cacheHub(linkInfo, hub)) {
                OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, new WeakReference(hub));
            }
            else {
                OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, hub);
            }
            return hub;
        }

        OASelect select = null;
        if (hub != null) {
        }
        else if (!bThisIsServer && !oi.getLocalOnly() && (!bIsCalc || bIsServerSideCalc)) {
            // request from server
            hub = OAObjectCSDelegate.getServerReferenceHub(oaObj, linkPropertyName); 
            if (hub == null) {
                // 20140311 master not on the Server, might have been GCd, create empty Hub 
                if (linkInfo == null) return null;
                Class linkClass = linkInfo.toClass;
                hub = new Hub(linkClass, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(linkInfo), false);
                // throw new RuntimeException("getHub from Server failed, this.oaObj="+oaObj+", linkPropertyName="+linkPropertyName);
            }
            
            if (HubDelegate.getMasterObject(hub) == null) {
                if (hub.getSize() == 0 && hub.getObjectClass() == null) {
                    if (linkInfo == null) return null;
                    Class linkClass = linkInfo.toClass;
                    hub = new Hub(linkClass, oaObj, OAObjectInfoDelegate.getReverseLinkInfo(linkInfo), false);
                }
            }            
        }
        else {  // hub is null, create now
            if (linkInfo == null) return null;
            Class linkClass = linkInfo.toClass;
            OALinkInfo liReverse = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
            if (liReverse != null) {
                
                // 20141109
                hub = new Hub(linkClass, oaObj, liReverse, false);
                
                if (!bIsCalc) {
                    select = new OASelect(hub.getObjectClass());
                    if (oaObj != null) {
                        select.setWhereObject(oaObj);
                        select.setPropertyFromWhereObject(linkInfo.getName());
                    }
                }                
                //was: hub = new Hub(linkClass, oaObj, liReverse, true); // liReverse = liDetailToMaster
                /* 2013/01/08 recursive if this object is the owner (or ONE to Many) and the select
                 * hub is recursive of a different class - need to only select root objects. All
                 * children (recursive) hubs will automatically be assigned the same owner as the
                 * root hub when owner is changed/assigned. */
                /*
                 * 20130919 recurse does not have to be owner */
                //was: if (!OAObjectInfoDelegate.isMany2Many(linkInfo) && (bThisIsServer || bIsCalc) && linkInfo.isOwner()) {

                // 20131009 new LinkProperty recursive flag.  If owned+recursive, then select root
                if (bThisIsServer && !bIsCalc) {
                    if (linkInfo.getOwner() && linkInfo.getRecursive()) {
                        OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo(linkInfo.getToClass());
                        OALinkInfo li2 = OAObjectInfoDelegate.getRecursiveLinkInfo(oi2, OALinkInfo.ONE);
                        if (li2 != null) {
                            OALinkInfo li3 = OAObjectInfoDelegate.getReverseLinkInfo(li2);
                            if (li3 != linkInfo) {
                                if (select != null) select.setWhere(li2.getName() + " == null");
                                else hub.setSelectWhere(li2.getName() + " == null");
                            }
                        }
                    }
                }
                /*was
                if (!OAObjectInfoDelegate.isMany2Many(linkInfo) && (bThisIsServer || bIsCalc)) {
                    OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo(linkInfo.getToClass());
                    OALinkInfo li2 = OAObjectInfoDelegate.getRecursiveLinkInfo(oi2, OALinkInfo.ONE);
                    if (li2 != null && li2 != liReverse) { // recursive
                        hub.setSelectWhere(li2.getName() + " == null");
                        // was: hub.setSelectRequiredWhere(li2.getName() + " == null");
                    }
                }
                */
            
            }
            else {
                hub = new Hub(linkClass, oaObj, null, false);
            }
        }

        // 20141204 added check to see if property is now there, in case it was deserialized and then
        //    the property was set by HubSerializeDelegate._readResolve
        if (bThisIsServer || OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, false, false) == null) {
            // set property
            if (OAObjectInfoDelegate.cacheHub(linkInfo, hub)) {
                OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, new WeakReference(hub));
            }
            else {
                OAObjectPropertyDelegate.setProperty(oaObj, linkPropertyName, hub);
            }
        }
        
        if ((bThisIsServer || (bIsCalc && !bIsServerSideCalc)) && sortOrder != null && sortOrder.length() > 0) {
            if (hub.getSelect() != null) {
                hub.setSelectOrder(sortOrder);
            }
            else if (select != null) {
                select.setOrder(sortOrder);
            }
        }

        // needs to loadAllData first, otherwise another thread could get the hub without using the lock
        if (bThisIsServer || (bIsCalc && !bIsServerSideCalc)) {
            if (!OAObjectCSDelegate.loadReferenceHubDataOnServer(hub, select)) { // load all data before passing to client
                //hub.loadAllData();
                HubSelectDelegate.loadAllData(hub, select);
            }

            hub.cancelSelect();
            if (select != null) {
                select.cancel();
                HubDataDelegate.resizeToFit(hub);
            }
            if (bThisIsServer && sortOrder != null && sortOrder.length() > 0) {
                if (bSequence) {
                    hub.setAutoSequence(sortOrder); // server will keep autoSequence property updated - clients dont need autoSeq (server side managed)
                }
                else {
                    // keep the hub sorted on server only
                    HubSortDelegate.sort(hub, sortOrder, true, null, true);// dont sort, or send out sort msg (since no other client has this hub yet)
                }
            }
            // 20110505 autoMatch propertyPath
            String matchProperty = linkInfo.getMatchProperty();
            if (matchProperty != null && matchProperty.length() > 0) {
                if (hubMatch == null) {
                    String matchHubProperty = linkInfo.getMatchHub();
                    if (matchHubProperty != null && matchHubProperty.length() > 0) {
                        OAObjectInfo oix = OAObjectInfoDelegate.getOAObjectInfo(linkInfo.getToClass());
                        OALinkInfo linkInfox = OAObjectInfoDelegate.getLinkInfo(oix, matchProperty);
                        if (linkInfox != null) {
                            hubMatch = new Hub(linkInfox.getToClass());
                            HubMerger hm = new HubMerger(oaObj, hubMatch, matchHubProperty);
                            hm.setServerSideOnly(true);
                        }
                    }
                }
                if (hubMatch != null) {
                    hub.setAutoMatch(matchProperty, hubMatch, true);
                }
            }
        }
        else {
            if (!bSequence) {
                // create sorter for client
                boolean bAsc = true;
                String s = HubSortDelegate.getSortProperty(hub); // use sort order from orig hub
                if (OAString.isEmpty(s)) s = sortOrder;
                else bAsc = HubSortDelegate.getSortAsc(hub);
                if (!OAString.isEmpty(s)) {
                    HubSortDelegate.sort(hub, s, bAsc, null, true);// dont sort, or send out sort msg (since no other client has this hub yet)
                }
            }
        }
        return hub;
    }

    /**
     * This method is used to get the value of a relationship. Calling it will not load objects. To load
     * objects, call getReferenceHub(name) or getReferenceObject(name)
     * 
     * @return one of the following: null, OAObjectKey, OAObject, Hub of OAObjects, Hub of OAObjectKeys
     * @see #isPropertyLoaded
     * @see #getReferenceObject to have the OAObject returned.
     * @see #getReferenceHub to have a Hub of OAObjects returned.
     */
    public static Object getRawReference(OAObject oaObj, String name) {
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, name, false, true);
        return obj;
    }

    // 20120616 check to see if an object has a reference holding it from being GCd.
    public static boolean hasReference(OAObject oaObj) {
        if (oaObj == null) return false;
        OAObjectInfo io = OAObjectInfoDelegate.getObjectInfo(oaObj.getClass());
        List<OALinkInfo> al = io.getLinkInfos();
        for (OALinkInfo li : al) {
            String name = li.getName();
            Object obj = getRawReference(oaObj, name);
            if (obj == null) continue;
            if (obj instanceof Hub) return true;

            if (obj instanceof OAObjectKey) {
                obj = OAObjectCacheDelegate.get(li.getToClass(), (OAObjectKey) obj);
            }

            if (obj instanceof OAObject) {
                name = li.getReverseName();
                obj = getRawReference((OAObject) obj, name);
                if (obj != null) return true;
            }
        }
        return false;
    }
    public static String[] getUnloadedReferences(OAObject obj, boolean bIncludeCalc) {
        return getUnloadedReferences(obj, bIncludeCalc, null);
    }

    public static String[] getUnloadedReferences(OAObject obj, boolean bIncludeCalc, String exceptPropertyName) {
        if (obj == null) return null;
        OAObjectInfo io = OAObjectInfoDelegate.getObjectInfo(obj.getClass());
        ArrayList<String> al = null;
        List<OALinkInfo> alLinkInfo = io.getLinkInfos();
        for (OALinkInfo li : alLinkInfo) {
            if (!bIncludeCalc && li.bCalculated) continue;
            if (li.bPrivateMethod) continue;
            String property = li.getName();
            
            if (exceptPropertyName != null && exceptPropertyName.equalsIgnoreCase(property)) continue;
            
            Object value = OAObjectReflectDelegate.getRawReference((OAObject) obj, property);
            if (value == null) {
                if (!OAObjectPropertyDelegate.isPropertyLoaded((OAObject) obj, property)) {
                    if (al == null) al = new ArrayList<String>();
                    al.add(property);
                }
            }
            else if (value instanceof OAObjectKey) {
                if (OAObjectCacheDelegate.get(li.toClass, value) == null) {
                    if (al == null) al = new ArrayList<String>();
                    al.add(property);
                }
            }
        }
        if (al == null) return null;
        int x = al.size();
        String[] props = new String[x];
        al.toArray(props);
        return props;
    }

    /**
     * Used to load all references to an object.
     */
    public static void loadAllReferences(OAObject obj) {
        loadAllReferences(obj, false);
    }

    public static void loadAllReferences(Hub hub) {
        loadAllReferences(hub, false);
    }

    public static void loadAllReferences(Hub hub, boolean bIncludeCalc) {
        Hub hubx = OAThreadLocalDelegate.setGetDetailHub(hub);
        try {
            for (Object obj : hub) {
                if (obj instanceof OAObject) loadAllReferences((OAObject) obj, bIncludeCalc);
            }
        }
        finally {
            OAThreadLocalDelegate.resetGetDetailHub(hubx);
        }
    }

    public static void loadAllReferences(OAObject obj, boolean bIncludeCalc) {
        loadReferences(obj, bIncludeCalc, 0);
    }
    public static void loadReferences(OAObject obj, boolean bIncludeCalc, int max) {
        OAObjectInfo io = OAObjectInfoDelegate.getObjectInfo(obj.getClass());
        List<OALinkInfo> al = io.getLinkInfos();
        int cnt = 0;
        for (OALinkInfo li : al) {
            if (!bIncludeCalc && li.bCalculated) continue;
            if (li.bPrivateMethod) continue;
            String name = li.getName();
            if (max > 0) {
               Object objx = OAObjectPropertyDelegate.getProperty(obj, name, true, true);
               if (objx == null) continue;
               if (objx != OANotExist.instance) {
                   if (!(objx instanceof OAObjectKey)) {
                       continue; // already loaded
                   }
               }
            }
            getProperty(obj, name);
            cnt++;
            if (max > 0 && cnt >= max) continue;
        }
    }

    public static boolean areAllReferencesLoaded(OAObject obj, boolean bIncludeCalc) {
        if (obj == null) return false;
        OAObjectInfo io = OAObjectInfoDelegate.getObjectInfo(obj.getClass());
        List<OALinkInfo> al = io.getLinkInfos();
        boolean bIsServer = OASyncDelegate.isServer(obj);
        for (OALinkInfo li : al) {
            if (al == null) continue;
            if (!bIncludeCalc && li.bCalculated) continue;
            if (li.bPrivateMethod) continue;
            String name = li.getName();
            
            Object val = OAObjectPropertyDelegate.getProperty(obj, name, true, true);
            if (val == OANotExist.instance) return false;
            if (val instanceof OAObjectKey) return false;
            if (val instanceof Hub && bIsServer) {
                Hub hubx = (Hub) val;
                // see if autoMatch (if used) is set up 
                String matchProperty = li.getMatchProperty();
                if (matchProperty != null && matchProperty.length() > 0) {
                    if (HubDelegate.getAutoMatch(hubx) == null) return false;
                }
            }
        }
        return true;
    }

    public static void loadAllReferences(OAObject obj, boolean bOne, boolean bMany, boolean bIncludeCalc) {
        OAObjectInfo io = OAObjectInfoDelegate.getObjectInfo(obj.getClass());
        List<OALinkInfo> al = io.getLinkInfos();
        for (OALinkInfo li : al) {
            if (!bIncludeCalc && li.bCalculated) continue;
            if (li.bPrivateMethod) continue;
            if (!bOne && li.getType() == OALinkInfo.ONE) continue;
            if (!bMany && li.getType() == OALinkInfo.MANY) continue;
            getProperty(obj, li.getName());
        }
    }

    public static void loadAllReferences(OAObject obj, int maxLevelsToLoad) {
        OACascade c = new OACascade();
        loadAllReferences(obj, 0, maxLevelsToLoad, 0, true, null, c);
    }

    public static void loadAllReferences(Hub hub, int maxLevelsToLoad) {
        OACascade c = new OACascade();
        loadAllReferences(hub, 0, maxLevelsToLoad, 0, true, null, c);
    }

    public static void loadAllReferences(OAObject obj, int maxLevelsToLoad, int additionalOwnedLevelsToLoad) {
        OACascade c = new OACascade();
        loadAllReferences(obj, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, true, null, c);
    }

    public static void loadAllReferences(Hub hub, int maxLevelsToLoad, int additionalOwnedLevelsToLoad) {
        OACascade c = new OACascade();
        loadAllReferences(hub, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, true, null, c);
    }

    public static void loadAllReferences(OAObject obj, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc) {
        OACascade c = new OACascade();
        loadAllReferences(obj, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, null, c);
    }

    public static void loadAllReferences(Hub hub, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc) {
        OACascade c = new OACascade();
        loadAllReferences(hub, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, null, c);
    }

    public static void loadAllReferences(OAObject obj, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc,
            OACallback callback) {
        OACascade c = new OACascade();
        loadAllReferences(obj, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, callback, c);
    }

    public static void loadAllReferences(Hub hub, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc,
            OACallback callback) {
        OACascade c = new OACascade();
        loadAllReferences(hub, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, callback, c);
    }

    public static void loadAllReferences(Hub hub, int levelsLoaded, int maxLevelsToLoad, int additionalOwnedLevelsToLoad,
            boolean bIncludeCalc, OACallback callback, OACascade cascade) {
        Hub hubx = OAThreadLocalDelegate.setGetDetailHub(hub);
        try {
            for (Object obj : hub) {
                loadAllReferences((OAObject) obj, levelsLoaded, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, callback,
                        cascade);
            }
        }
        finally {
            OAThreadLocalDelegate.resetGetDetailHub(hubx);
        }
    }

    public static void loadAllReferences(Hub hub, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc,
            OACascade cascade) {
        for (Object obj : hub) {
            loadAllReferences((OAObject) obj, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, null, cascade);
        }
    }

    public static void loadAllReferences(OAObject obj, int maxLevelsToLoad, int additionalOwnedLevelsToLoad, boolean bIncludeCalc,
            OACascade cascade) {
        loadAllReferences(obj, 0, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, null, cascade);
    }

    // ** MAIN reference loader here **
    /**
     * 
     * @param levelsLoaded
     *            number of levels of references that have been loaded.
     * @param maxLevelsToLoad
     *            max levels of references to recursively load.
     * @param additionalOwnedLevelsToLoad
     *            additional levels of owned references to load
     * @param bIncludeCalc
     *            include calculated links
     * @param callback
     *            will be called before loading references. If the callback.updateObject returns false,
     *            then the current object references will not be loaded
     * @param cascade
     *            used to impl vistor pattern
     */
    public static void loadAllReferences(OAObject obj, int levelsLoaded, int maxLevelsToLoad, int additionalOwnedLevelsToLoad,
            boolean bIncludeCalc, OACallback callback, OACascade cascade) {
        if (obj == null) return;
        if (cascade.wasCascaded(obj, true)) return;
        if (callback != null) {
            if (!callback.updateObject(obj)) return;
        }

        boolean bOwnedOnly = (levelsLoaded >= maxLevelsToLoad);

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        for (OALinkInfo li : oi.getLinkInfos()) {
            if (!bIncludeCalc && li.bCalculated) continue;
            if (li.bPrivateMethod) continue;
            if (bOwnedOnly && !li.bOwner) continue;

            Object objx = obj.getProperty(li.getName()); // load prop
            if (objx == null) continue;

            if (levelsLoaded + 1 >= maxLevelsToLoad) {
                if (levelsLoaded + 1 >= (maxLevelsToLoad + additionalOwnedLevelsToLoad)) {
                    continue;
                }
            }

            if (objx instanceof Hub) {
                Hub hubx = OAThreadLocalDelegate.setGetDetailHub((Hub) objx);
                try {
                    for (Object objz : (Hub) objx) {
                        loadAllReferences((OAObject) objz, levelsLoaded + 1, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc,
                                callback, cascade);
                    }
                }
                finally {
                    OAThreadLocalDelegate.resetGetDetailHub(hubx);
                }
            }
            else if (objx instanceof OAObject) {
                loadAllReferences((OAObject) objx, levelsLoaded + 1, maxLevelsToLoad, additionalOwnedLevelsToLoad, bIncludeCalc, callback,
                        cascade);
            }
        }
    }

    // 20121001
    public static byte[] getReferenceBlob(OAObject oaObj, String propertyName) {
        if (oaObj == null) return null;
        if (propertyName == null) return null;

        try {
            OAObjectPropertyDelegate.setPropertyLock(oaObj, propertyName);

            Object val = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
            if (val instanceof byte[]) return (byte[]) val;
            if (val != OANotExist.instance) return null;
            
            if (!OASyncDelegate.isServer(oaObj)) {
                val = OAObjectCSDelegate.getServerReferenceBlob(oaObj, propertyName);
            }
            else {
                OADataSource ds = OADataSource.getDataSource(oaObj.getClass());
                if (ds != null) val = ds.getPropertyBlobValue(oaObj, propertyName);
            }

            val = OAObjectPropertyDelegate.setPropertyCAS(oaObj, propertyName, val, null, true, false);
            if (val instanceof byte[]) return (byte[]) val;
            
        }
        finally {
            OAObjectPropertyDelegate.releasePropertyLock(oaObj, propertyName);
        }
        return null;
    }

    /**
     * DataSource independent method to retrieve a reference property.
     * <p>
     * If reference object is not already loaded, then OADataSource will be used to retrieve object.
     * @param bForce, if false and there is not 
     */
    public static Object getReferenceObject(OAObject oaObj, String linkPropertyName) {
        Object objOriginal = OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, true, true);
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, linkPropertyName);

        if (objOriginal == null) {  // else !null or notExist
            // it is stored as null value
            if (!li.getAutoCreateNew() && !li.getCalculated()) return null;
        }

        boolean bDidNotExist = (objOriginal == OANotExist.instance);
        if (bDidNotExist) {
            objOriginal = null;
        }
        else if (objOriginal == null) {
            if (!li.getAutoCreateNew() && !li.getCalculated()) {
                return null;
            }            
        }
        else if (!(objOriginal instanceof OAObjectKey)) {
            return objOriginal; // found it
        }


        Object result = null;
        try {
            OAObjectPropertyDelegate.setPropertyLock(oaObj, linkPropertyName);
            result = _getReferenceObject(oaObj, linkPropertyName, oi, li);
            OAObjectPropertyDelegate.setPropertyCAS(oaObj, linkPropertyName, result, objOriginal, bDidNotExist, false);
        }
        finally {
            OAObjectPropertyDelegate.releasePropertyLock(oaObj, linkPropertyName);
            if (result instanceof OAObjectKey) {
                result = getReferenceObject(oaObj, linkPropertyName);
            }
        }
        return result;
    }

    // note: this acquired a lock before calling
    private static Object _getReferenceObject(OAObject oaObj, String linkPropertyName, OAObjectInfo oi, OALinkInfo li) {
        if (linkPropertyName == null) return null;

        boolean bIsServer = OASyncDelegate.isServer(oaObj);
        boolean bIsCalc = li != null && li.bCalculated;

        Object ref = null;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, linkPropertyName, true, true);

        if (!(obj instanceof OAObjectKey)) {
            if (obj != OANotExist.instance) {
                // 20151117
                if (obj != null) {
                    return obj;
                }
                else if (li.getAutoCreateNew()) {
                    if (OAObjectInfoDelegate.isOne2One(li)) return null; // will only be null if it was set to null on purpose. (ex: cascade delete)
                }
                else {
                    if (!li.bCalculated) return null;
                }
            }

            // == null.  check to see if it is One2One, and if a select must be used to get the object.
            if (li == null) return null;
            if (OAObjectInfoDelegate.isOne2One(li) && !oaObj.isNew()) {
                if (!bIsServer && !bIsCalc) {
                    if (oaObj.isDeleted()) { // 20151117
                        return null;
                    }
                    ref = OAObjectCSDelegate.getServerReference(oaObj, linkPropertyName);
                }
                else if (!bIsCalc) {
                    OALinkInfo liReverse = OAObjectInfoDelegate.getReverseLinkInfo(li);
                    if (liReverse != null) {
                        OASelect sel = new OASelect(li.getToClass());
                        sel.setWhereObject(oaObj);
                        sel.setPropertyFromWhereObject(li.name);
                        sel.select();
                        ref = sel.next();
                        sel.close();
                    }
                }
            }
            else {
                // first check to see if it is in the hub for the link
                if (li.getPrivateMethod() || !isReferenceObjectNullOrEmpty(oaObj, linkPropertyName)) {
                    Hub hubx = OAObjectHubDelegate.getHub(oaObj, li);
                    if (hubx != null) {
                        ref = HubDelegate.getMasterObject(hubx);
                    }
                }
                
                if (ref == null && li.getPrivateMethod()) {
                    // 20120907 might not have a method created, and uses a linkTable
                    // 20151208 need to get from ds                        
                    if (!bIsServer && !bIsCalc) {
                        if (oaObj.isDeleted()) { // 20151117
                            return null;
                        }
                        ref = OAObjectCSDelegate.getServerReference(oaObj, linkPropertyName);
                    }
                    else {
                        OALinkInfo liReverse = OAObjectInfoDelegate.getReverseLinkInfo(li);
                        if (liReverse != null) {
                            OASelect sel = new OASelect(li.getToClass());
                            sel.setWhere(liReverse.getName()+" = ?");
                            sel.setParams(new Object[] {oaObj});
                            sel.select();
                            ref = sel.next();
                            sel.close();
                        }
                    }                        
                }
            }
        }
        else {
            OAObjectKey key = (OAObjectKey) obj;

            if (li == null) return null;

            ref = OAObjectCacheDelegate.get(li.toClass, key);

            if (ref == null) {
                if (!bIsServer && !bIsCalc && !oi.getLocalOnly()) {
                    ref = OAObjectCSDelegate.getServerReference(oaObj, linkPropertyName);
                }
                else {
                    ref = (OAObject) OAObjectDSDelegate.getObject(oi, li.toClass, (OAObjectKey) obj);
                }
            }
        }

        if (ref == null && li.getAutoCreateNew() && !bIsCalc) {
            boolean b = OAObjectInfoDelegate.isOne2One(li);
            if (b && oaObj.isDeleted() && !bIsServer) {
                // 20151117 dont autocreate new if this is deleted
            }
            else {
                ref = OAObjectReflectDelegate.createNewObject(li.getToClass());
                setProperty(oaObj, linkPropertyName, ref, null); // need to do this so oaObj.changed=true, etc.
            }
        }
        return ref;
    }


    /**
     * Used to retrieve a reference key without actually loading the object. Datasourcs stores the key
     * value for references, that are then used to retrieve the object when requested using
     * getObject(property). This method is a way to get the key, without loading the object.<br>
     * <br>
     * 
     * @return the OAObjectKey of a ONE reference. Does not call getMethod, but internally stored value.
     * @see isLoaded to see if object has been loaded and exists in memory. isLoaded will return false
     *      if the property has never been set, or loaded, or if the objectKey has been set and the
     *      object for the key is not in memory. This method will always return the objectKey for the
     *      reference.
     * @see getObject, which will loaded the object from memory or datasource.
     */
    public static OAObjectKey getPropertyObjectKey(OAObject oaObj, String property) {
        if (property == null) return null;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, property, false, true);
        if (obj == null) return null;
        if (obj instanceof OAObjectKey) return (OAObjectKey) obj;
        if (obj instanceof OAObject) return OAObjectKeyDelegate.getKey((OAObject) obj);
        return null;
    }

    /**
     * Checks to see if the actual value for a property has been loaded. This will also check to see if
     * a reference ObjectKey was loaded and the "real" object is in memory.
     */
    public static boolean hasReferenceObjectBeenLoaded(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return true;
        if (obj == OANotExist.instance) return false;
        if (obj instanceof OAObject) return true;
        if (obj instanceof Hub) {
            Hub h = (Hub) obj;
            Class c = h.getObjectClass();
            if (c.equals(OAObjectKey.class)) return false;
            return true;
        }
        if (obj instanceof OAObjectKey) {
            // use Key to see if object is in memory
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oaObj.getClass(), propertyName);
            if (li == null) return true;

            Object objFound = OAObjectCacheDelegate.get(li.toClass, (OAObjectKey) obj);
            if (objFound != null) {
                OAObjectPropertyDelegate.setPropertyCAS(oaObj, propertyName, objFound, obj);
                return true;
            }
        }
        return false;
    }

    public static boolean isReferenceObjectNullOrEmpty(OAObject oaObj, String propertyName) {
        if (oaObj == null || propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return true; // the ref is null, dont need to load it
        if (obj == OANotExist.instance) return true;
        return false;
    }

    public static boolean isReferenceObjectLoadedAndNotEmpty(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return false; // the ref is null, dont need to load it
        if (obj == OANotExist.instance) return false;
        if (obj instanceof OAObject) return true;

        if (obj instanceof OAObjectKey) {
            // use Key to see if object is in memory
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oaObj.getClass(), propertyName);
            if (li == null) return true;

            Object objFound = OAObjectCacheDelegate.get(li.toClass, (OAObjectKey) obj);
            if (objFound != null) {
                OAObjectPropertyDelegate.setPropertyCAS(oaObj, propertyName, objFound, obj);
                return true;
            }
        }
        return false;
    }

    public static boolean isReferenceNullOrNotLoaded(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return true; // not loaded
        if (obj == OANotExist.instance) return true; // null

        if (obj instanceof OAObject) return false;

        if (obj instanceof Hub) {
            return false;
        }

        if (obj instanceof OAObjectKey) {
            return !hasReferenceObjectBeenLoaded(oaObj, propertyName);
        }
        return false;
    }

    public static boolean isReferenceNullOrNotLoadedOrEmptyHub(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return true; // not loaded
        if (obj == OANotExist.instance) return true; // ref is null 

        if (obj instanceof OAObject) return false;

        if (obj instanceof Hub) {
            return ((Hub) obj).getSize() == 0; // emptyHub
        }

        if (obj instanceof OAObjectKey) {
            return !hasReferenceObjectBeenLoaded(oaObj, propertyName);
        }
        return false;
    }

    public static boolean isReferenceHubLoaded(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        
        if (obj instanceof OANotExist) return false;
        if (obj == null) return true; // flag that hub could be create, with no objects

        if (obj instanceof Hub) return true;

        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oaObj.getClass(), propertyName);
        if (li == null || li.getType() != li.MANY) return false;
        return true;
    }

    // used to check for a known empty hub (already loaded, with size=0)
    public static boolean isReferenceHubLoadedAndEmpty(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
        if (obj == null) return true;
        if (obj instanceof OANotExist) return false;
        
        if (obj instanceof Hub) {
            if (((Hub) obj).isLoading()) return false;
            return ((Hub) obj).getSize() == 0;
        }

        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oaObj.getClass(), propertyName);
        if (li == null || li.getType() != li.MANY) return false;
        return true;
    }

    public static boolean isReferenceHubLoadedAndNotEmpty(OAObject oaObj, String propertyName) {
        if (propertyName == null) return false;
        Object obj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, false, true);
        if (obj == null) return false;
        if (obj instanceof Hub) {
            return ((Hub) obj).getSize() > 0;
        }
        return false;
    }

    /**
     * Used to preload data, this will recursively load all references in the given Property Paths.
     * 
     * @param oaObj
     *            root object to use.
     * @param propertyPaths
     *            one or more propertyPaths, that can be loaded using a single visit to each property.
     */
    public static void loadProperties(OAObject oaObj, String... propertyPaths) {
        if (propertyPaths == null) return;
        if (propertyPaths.length == 0 || oaObj == null) return;

        LoadPropertyNode rootNode = createPropertyTree(propertyPaths);

        _loadProperties(oaObj, rootNode);
    }

    /**
     * Used to preload data, this will recursively load all references in the given Property Paths.
     * 
     * @param hub
     *            root objects to use.
     * @param propertyPaths
     *            one or more propertyPaths, that can be loaded using a single visit to each property.
     */
    public static void loadProperties(Hub hub, String... propertyPaths) {
        if (propertyPaths == null) return;
        if (propertyPaths.length == 0 || hub == null) return;

        LoadPropertyNode rootNode = createPropertyTree(propertyPaths);

        _loadProperties(hub, rootNode);
    }

    /**
     * Used by loadProperties, to take multiple property paths, and create a tree of unique property
     * paths.
     * 
     * @param propertyPaths
     *            example: "orders.orderItems.item.vendor"
     * @return root node of tree, that has it's children as the starting point for the property paths.
     */
    private static LoadPropertyNode createPropertyTree(String... propertyPaths) {
        int x = 0;
        LoadPropertyNode rootNode = new LoadPropertyNode();
        for (String propertyPath : propertyPaths) {
            LoadPropertyNode node = rootNode; // beginning of property paths
            StringTokenizer st = new StringTokenizer(propertyPath, ".", false);
            for (; st.hasMoreTokens();) {
                String prop = st.nextToken();
                boolean b = false;
                if (node.children != null) {
                    for (LoadPropertyNode pn : node.children) {
                        if (pn.prop.equalsIgnoreCase(prop)) {
                            node = pn;
                            b = true;
                            break;
                        }
                    }
                }
                if (!b) {
                    LoadPropertyNode pn = new LoadPropertyNode();
                    pn.prop = prop;
                    node.children = (LoadPropertyNode[]) OAArray.add(LoadPropertyNode.class, node.children, pn);
                    node = pn;
                }
            }
        }
        return rootNode;
    }

    // recursively load path
    private static void _loadProperties(Object object, LoadPropertyNode node) {
        if (object instanceof OAObject) {
            OAObject oaObj = (OAObject) object;
            if (node.children != null) {
                for (LoadPropertyNode pn : node.children) {
                    Object value = _getProperty(null, oaObj, pn.prop);
                    if (value != null) {
                        _loadProperties(value, pn);
                    }
                }
            }
        }
        else if (object instanceof Hub) {
            Hub h = (Hub) object;
            if (!OAObject.class.isAssignableFrom(h.getObjectClass())) return;

            for (int j = 0;; j++) {
                OAObject obj = (OAObject) h.getAt(j);
                if (obj == null) break;
                _loadProperties(obj, node);
            }
        }
        // else no-op/done
    }

    /**
     * Create a copy of an object, excluding selected properties.
     * 
     * @return new copy of the object
     */
    public static OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
        return createCopy(oaObj, excludeProperties, null);
    }

    public static OAObject createCopy(OAObject oaObj, String[] excludeProperties, OACopyCallback copyCallback) {
        HashMap<Integer, Object> hmNew = new HashMap<Integer, Object>();
        OAObject obj = _createCopy(oaObj, excludeProperties, copyCallback, hmNew);
        return obj;
    }
    
    public static OAObject _createCopy(OAObject oaObj, String[] excludeProperties, OACopyCallback copyCallback, HashMap<Integer, Object> hmNew) {
        if (oaObj == null) return null;
        
        OAObject newObject = (OAObject) hmNew.get(OAObjectDelegate.getGuid(oaObj));
        if (newObject != null) return newObject;
        
        // run on server only - otherwise objects can not be updated, since setLoadingObject is true
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
        if (!oi.getLocalOnly()) {
            if (!OASyncDelegate.isServer(oaObj)) {
                // 20130505 needs to be put in msg queue
                newObject = OAObjectCSDelegate.createCopy(oaObj, excludeProperties);
                return newObject;
            }
        }

        try {
            OAThreadLocalDelegate.setLoadingObject(true);
            OAThreadLocalDelegate.setSuppressCSMessages(true);
            newObject = (OAObject) createNewObject(oaObj.getClass());
            _copyInto(oaObj, newObject, excludeProperties, copyCallback, hmNew);
        }
        finally {
            OAThreadLocalDelegate.setLoadingObject(false);
            OAThreadLocalDelegate.setSuppressCSMessages(false);
        }
        return newObject;
    }

    /**
     * Copies the properties and some of the links from a source object (this) to a new object. For
     * links of type One, all of the links are used, the same ref object from the source object is used.
     * For links of type Many, only the owned links are used, and clones of the objects are created in
     * the Hub of the new object. OACopyCallback can be used to control what is copied.
     * 
     */
    public static void copyInto(OAObject oaObj, OAObject newObject, String[] excludeProperties, OACopyCallback copyCallback) {
        HashMap<Integer, Object> hmNew = new HashMap<Integer, Object>();
        copyInto(oaObj, newObject, excludeProperties, copyCallback, hmNew);
    }    

    public static void copyInto(OAObject oaObj, OAObject newObject, String[] excludeProperties, OACopyCallback copyCallback, HashMap<Integer, Object> hmNew) {
        try {
            OAThreadLocalDelegate.setLoadingObject(true);
            OAThreadLocalDelegate.setSuppressCSMessages(true);
            _copyInto(oaObj, newObject, excludeProperties, copyCallback, hmNew);
        }
        finally {
            OAThreadLocalDelegate.setLoadingObject(false);
            OAThreadLocalDelegate.setSuppressCSMessages(false);
        }
    }

    /*
     * note: OAThreadLocalDelegate.setLoadingObject(true/false) is not set in this method.
     *      it is set by copyInto 
     */
    public static void _copyInto(OAObject oaObj, OAObject newObject, String[] excludeProperties, OACopyCallback copyCallback, HashMap<Integer, Object> hmNew) {
        if (oaObj == null || newObject == null) return;
        hmNew.put(OAObjectDelegate.getGuid(oaObj), newObject);
        if (!(oaObj.getClass().isInstance(newObject))) {
            throw new IllegalArgumentException("OAObject.copyInto() object is not same class");
        }
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
        List al = oi.getPropertyInfos();
        for (int i = 0; i < al.size(); i++) {
            OAPropertyInfo pi = (OAPropertyInfo) al.get(i);
            if (excludeProperties != null) {
                int j = 0;
                for (; j >= 0 && j < excludeProperties.length; j++) {
                    if (excludeProperties[j] == null) continue;
                    if (excludeProperties[j].equalsIgnoreCase(pi.getName())) j = -5;
                }
                if (j < 0) continue;
            }
            if (!pi.getId()) {
                Object value = oaObj.getProperty(pi.getName());
                if (copyCallback != null) {
                    value = copyCallback.getPropertyValue(oaObj, pi.getName(), value);
                }
                newObject.setProperty(pi.getName(), value);
            }
        }

        // make copy of owned many objects
        al = oi.getLinkInfos();
        for (int i = 0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.getType() != li.MANY) continue;
            if (li.getCalculated()) continue;
            if (li.getPrivateMethod()) continue;

            boolean bCopy = (li.isOwner());
            if (bCopy && excludeProperties != null) {
                for (int j = 0; bCopy && j < excludeProperties.length; j++) {
                    if (excludeProperties[j] == null) continue;
                    if (excludeProperties[j].equalsIgnoreCase(li.getName())) bCopy = false;
                }
            }
            if (copyCallback != null) {
                bCopy = copyCallback.shouldCopyOwnedHub(oaObj, li.getName(), bCopy);
            }
            if (!bCopy) continue;
            Hub hub = (Hub) OAObjectReflectDelegate.getProperty(oaObj, li.getName());
            Hub hubNew = (Hub) OAObjectReflectDelegate.getProperty(newObject, li.getName());
            for (int j = 0; hub!=null && hubNew!=null; j++) {
                OAObject obj = (OAObject) hub.elementAt(j);
                if (obj == null) break;

                Object objx = hmNew.get(OAObjectDelegate.getGuid((OAObject)obj));
                
                if (objx == null) {
                    if (copyCallback != null) {
                        objx = copyCallback.createCopy(oaObj, li.getName(), hub, obj);
                        if (obj == objx) {
                            objx = _createCopy((OAObject) obj, (String[])null, copyCallback, hmNew);
                        }
                    }
                    else {
                        objx = obj.createCopy();
                    }
                }
                if (objx != null) {
                    if (obj != objx) {
                        hmNew.put(OAObjectDelegate.getGuid(obj), objx);
                    }
                    hubNew.add(objx);
                }
            }
        }

        // set One links, if it is not an owner, or if it is autocreated
        al = oi.getLinkInfos();
        for (int i = 0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.getType() != li.ONE) continue;
            if (li.getCalculated()) continue;
            if (li.getPrivateMethod()) continue;
            
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.isOwner()) {
                if (!li.getAutoCreateNew()) {
                    continue;
                }
            }

            if (excludeProperties != null) {
                boolean b = true;
                for (int j = 0; j < excludeProperties.length; j++) {
                    if (excludeProperties[j] == null) continue;
                    if (excludeProperties[j].equalsIgnoreCase(li.getName())) {
                        b = false;
                        break;
                    }
                }
                if (!b) continue;
            }
            
            Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
            if (li.getAutoCreateNew() && obj instanceof OAObject) {
                Object objx = newObject.getProperty(li.getName());
                if (objx instanceof OAObject) {
                    _copyInto((OAObject) obj, (OAObject) objx, (String[])null, copyCallback, hmNew);
                }
            }
            else {
                boolean b = false;
                if (obj != null) {
                    Object objx = hmNew.get(OAObjectDelegate.getGuid((OAObject)obj));
                    if (objx != null) {
                        b = true;  // object is already a copy
                        obj = objx;
                    }
                }            
                if (!b && copyCallback != null) {
                    obj = copyCallback.getPropertyValue(oaObj, li.getName(), obj);
                    if (obj instanceof OAObject ) {
                        if (shouldMakeACopy((OAObject)obj, excludeProperties, copyCallback, hmNew, 0, null)) {                        
                            Object objx = _createCopy((OAObject)obj, excludeProperties, copyCallback, hmNew);
                            if (objx != obj && objx != null) {
                                hmNew.put(OAObjectDelegate.getGuid((OAObject)obj), objx);
                                obj = objx;
                            }
                        }
                    }
                }
                newObject.setProperty(li.getName(), obj);
            }
        }
    }
    
    // recursively checks 3 levels for replaced objects
    private static boolean shouldMakeACopy(OAObject oaObj, String[] excludeProperties, OACopyCallback copyCallback, HashMap<Integer, Object> hmNew, int cnt, HashSet<Integer> hmVisitor) {
        if (oaObj == null) return false;
        if (hmVisitor == null) hmVisitor = new HashSet<Integer>(101, .75f);
        else if (hmVisitor.contains(OAObjectDelegate.getGuid(oaObj))) return false;
        hmVisitor.add(OAObjectDelegate.getGuid(oaObj));
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
        List<OALinkInfo> alLinkInfo = oi.getLinkInfos();
        for (OALinkInfo li : alLinkInfo) {
            if (li.getCalculated()) continue;
            if (li.getPrivateMethod()) continue;
            
            if (excludeProperties != null) {
                boolean b = true;
                for (int j = 0; j < excludeProperties.length; j++) {
                    if (excludeProperties[j] == null) continue;
                    if (excludeProperties[j].equalsIgnoreCase(li.getName())) {
                        b = false;
                        break;
                    }
                }
                if (!b) continue;
            }
            
            if (li.getType() == li.MANY) {
                Hub hub = (Hub) OAObjectReflectDelegate.getProperty(oaObj, li.getName());
                for (int j = 0; hub!=null; j++) {
                    OAObject obj = (OAObject) hub.elementAt(j);
                    if (obj == null) break;
                    Object objx = hmNew.get(OAObjectDelegate.getGuid((OAObject)obj));
                    if (objx != null) return true;
                    
                    if (cnt < 3 && obj instanceof OAObject ) {
                        if (shouldMakeACopy((OAObject)obj, excludeProperties, copyCallback, hmNew, cnt+1, hmVisitor)) {
                            return true;
                        }                    
                    }
                }
            }
            else {
                Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
                if (obj != null) {
                    Object objx = hmNew.get(OAObjectDelegate.getGuid((OAObject)obj));
                    if (objx != null) return true;
                    
                    if (cnt < 3 && obj instanceof OAObject ) {
                        if (shouldMakeACopy((OAObject)obj, excludeProperties, copyCallback, hmNew, cnt+1, hmVisitor)) {
                            return true;
                        }                    
                    }
                }
            }
        }        
        return false;
    }
    
    
    public static Class getHubObjectClass(Method method) {
        Class cx = null;
        Type rt = method.getGenericReturnType();
        if (rt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) rt;
            try {
                Type[] types = pt.getActualTypeArguments();
                if (types != null && types.length > 0 && types[0] instanceof Class) {
                    cx = (Class) types[0];
                }
            }
            catch (Throwable t) {
            }
        }
        return cx;
    }

    /**
     * Find the common Hub that two objects are descendants of.
     * 
     * @param currentLevel
     *            current level of parents that have been checked
     * @param maxLevelsToCheck
     *            total number of parents to check
     */
    public static Hub findCommonHierarchyHub(OAObject obj1, OAObject obj2, int maxLevelsToCheck) {
        return findCommonHierarchyHub(obj1, obj2, 0, maxLevelsToCheck);
    }

    protected static Hub findCommonHierarchyHub(OAObject obj1, OAObject obj2, int currentLevel, int maxLevelsToCheck) {
        if (obj1 == null || obj2 == null) return null;
        if (currentLevel >= maxLevelsToCheck) return null;

        Hub[] hubs = OAObjectHubDelegate.getHubReferences(obj1);
        for (int i = 0; hubs != null && i < hubs.length; i++) {
            Hub nextHub = hubs[i];
            if (nextHub == null) continue;
            int x = getHierarchyLevelsToHub(nextHub, obj2, 0, maxLevelsToCheck);
            if (x > 0) return nextHub;

            OAObject objMaster = nextHub.getMasterObject();
            Hub h = findCommonHierarchyHub(objMaster, obj2, currentLevel + 1, maxLevelsToCheck);
            if (h != null) return h;
        }
        return null;
    }

    public static int getHierarchyLevelsToHub(Hub findHub, OAObject fromObj, int maxLevelsToCheck) {
        return getHierarchyLevelsToHub(findHub, fromObj, 0, maxLevelsToCheck);
    }

    protected static int getHierarchyLevelsToHub(Hub findHub, OAObject fromObj, int currentLevel, int maxLevelsToCheck) {
        if (findHub == null || fromObj == null) return -1;
        if (currentLevel >= maxLevelsToCheck) return -1;

        Hub[] hubs = OAObjectHubDelegate.getHubReferences(fromObj);
        for (int i = 0; hubs != null && i < hubs.length; i++) {
            Hub hub = hubs[i];
            if (hub == null) continue;
            if (hub == findHub) return currentLevel;

            OAObject nextObj = hub.getMasterObject();
            int x = getHierarchyLevelsToHub(findHub, nextObj, currentLevel + 1, maxLevelsToCheck);
            if (x > 0) return x;
        }
        return -1;
    }

    /**
     * get the property path from a Parent hub to a child hub. 
     * @param hubParent  parent hub that needs to have the path expanded from.
     * @param hubChild child Hub that has a path
     * @return
     */
    private static String getPropertyPathFromMaster(final Hub hubParent, final Hub hubChild) {
        if (hubParent == null) return null;
        if (hubChild == null) return null;
        String pathFromParent = null;

        boolean b = false;
        if (HubLinkDelegate.getLinkedOnPos(hubChild, true)) {
            //String s = HubLinkDelegate.getLinkToProperty(hubChild, true);
            b = true;
        }
        String fromProp = HubLinkDelegate.getLinkFromProperty(hubChild, true);
        if (fromProp != null) {
            b = true;
            //return fromProp;
        }

        
        // see if there is a link path
        pathFromParent = null;
        Hub h = hubChild;
        for ( ; !b; ) {
            Hub hx = HubLinkDelegate.getLinkToHub(h, true);
            if (hx == null) {
                pathFromParent = null ;
                break;
            }

            if (pathFromParent == null) pathFromParent = HubLinkDelegate.getLinkHubPath(h, true);
            else pathFromParent = HubLinkDelegate.getLinkHubPath(h, true) + "." + pathFromParent;
            
            if (hx == hubParent) {
                return pathFromParent;
            }
            if (HubShareDelegate.isUsingSameSharedAO(hubParent, hx, true)) {
                return pathFromParent;
            }
            if (hubParent.getMasterHub() == null) { // 20131109 could be a hub copy
                if (hx.getObjectClass().equals(hubParent.getObjectClass())) {
                    return pathFromParent;
                }
            }
            h = hx;
        }
        // see if if there is a detail path using masterHub
        h = hubChild;
        for ( ;; ) {
            Hub hx = h.getMasterHub();
            if (hx == null) {
                return null;
            }
            if (pathFromParent == null) pathFromParent = HubDetailDelegate.getPropertyFromMasterToDetail(h);
            else pathFromParent = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + pathFromParent;

            if (hx == hubParent) {
                return pathFromParent;
            }
            if (HubShareDelegate.isUsingSameSharedAO(hubParent, hx, true)) {
                return pathFromParent;
            }
            if (hubParent.getMasterHub() == null) { // 20131109 could be a hub copy
                if (hx.getObjectClass().equals(hubParent.getObjectClass())) {
                    return pathFromParent;
                }
            }
            h = hx;
        }
    }
    public static String getPropertyPathFromMaster(final OAObject objParent, final Hub hubChild) {
        if (objParent == null) return null;
        if (hubChild == null) return null;
        String pathFromParent = null;
        final Class parentClass = objParent.getClass();

        boolean b = false;
        if (HubLinkDelegate.getLinkedOnPos(hubChild, true)) {
            //String s = HubLinkDelegate.getLinkToProperty(hubChild, true);
            b = true;
        }
        String fromProp = HubLinkDelegate.getLinkFromProperty(hubChild, true);
        if (fromProp != null) {
            b = true;
            //return fromProp;
        }
        
        
        // see if there is a link path
        pathFromParent = null;
        Hub h = hubChild;
        for ( ;!b; ) {
            Hub hx = HubLinkDelegate.getLinkToHub(h, true);
            if (hx == null) {
                pathFromParent = null ;
                break;
            }

            if (pathFromParent == null) pathFromParent = HubLinkDelegate.getLinkHubPath(h, true);
            else pathFromParent = HubLinkDelegate.getLinkHubPath(h, true) + "." + pathFromParent;
            
            if (parentClass.equals(hx.getObjectClass())) {
                return pathFromParent;
            }
            h = hx;
        }

        // see if if there is a detail path using masterHub
        h = hubChild;
        for ( ;; ) {
            Hub hx = h.getMasterHub();
            if (hx == null) {
                return null;
            }
            if (pathFromParent == null) pathFromParent = HubDetailDelegate.getPropertyFromMasterToDetail(h);
            else pathFromParent = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + pathFromParent;
            if (parentClass.equals(hx.getObjectClass())) {
                return pathFromParent;
            }
            h = hx;
        }
    }
    
//qqqqqqqqqqqqqq test this    
//qqqqqqqq    
    // 20140404
    /**
     * get the "real" object that needs to be displayed, based on a parent/from Hub,
     *    and a from object (ex: row in table), and the hub that it originates from. 
     * @param hubFrom     ex: hubDept
     * @param fromObject  ex: dept
     * @param hubChild    ex: hubEmplyeeType, (enum of strings) and is linked to hubEmp
     * @return  hubEmployeeType.getAt(pos), where pos is dept.manager.employeeType
     */
    public static Object getObjectToDisplay(final Hub hubFrom, Object fromObject, final Hub hubChild) {
        if (hubFrom == null) return null;
        if (hubChild == null) return null;
        if (fromObject == null) return null;

        if (!HubLinkDelegate.getLinkedOnPos(hubChild, true)) return fromObject;

        
        Hub hubPosValue = HubLinkDelegate.getLinkToHub(hubChild, false);
        if (hubPosValue == null) return fromObject;
        
        // see if there is a link path
        String pathFromParent = null;
        Hub h = hubPosValue;
        for ( ; ; ) {
            Hub hx = HubLinkDelegate.getLinkToHub(h, true);
            if (hx == null) {
                pathFromParent = null;
                break;
            }

            if (pathFromParent == null) pathFromParent = HubLinkDelegate.getLinkHubPath(h, true);
            else pathFromParent = HubLinkDelegate.getLinkHubPath(h, true) + "." + pathFromParent;
            
            if (hx == hubFrom) {
                break;
            }
            if (HubShareDelegate.isUsingSameSharedAO(hubFrom, hx, true)) {
                break;
            }
            if (hubFrom.getMasterHub() == null) { // 20131109 could be a hub copy
                if (hx.getObjectClass().equals(hubFrom.getObjectClass())) {
                    break;
                }
            }
            h = hx;
        }
        
        if (pathFromParent != null && fromObject instanceof OAObject) {
            Object objx = getProperty((OAObject)fromObject, pathFromParent);
            if (objx == null) return fromObject;
            fromObject = objx;
        }
        if (!(fromObject instanceof OAObject)) {
            return fromObject;
        }
        
        String fromProp = HubLinkDelegate.getLinkToProperty(hubChild);
        if (fromProp == null) return fromObject;
        
        
        Object objx = getProperty((OAObject) fromObject, fromProp);
        int x = OAConv.toInt(objx);
        return hubChild.getAt(x);
    }
    
    
    /**
     * get the property path from a Parent hub to a child hub, that is all of type=One. 
     * @param hubParent  parent hub that needs to have the path expanded from.
     * @param hubChild child Hub that has a path
     */
    public static String getPropertyPathBetweenHubs(final Hub hubParent, final Hub hubChild) {
        return getPropertyPathBetweenHubs(null, hubParent, hubChild, true);
    }
    private static String getPropertyPathBetweenHubs(final String propPath, final Hub hubParent, final Hub hubChild, boolean bCheckLink) {
        if (hubChild == hubParent) return null;
        
        if (hubChild == null || hubParent == null) return null;
        
        if (HubShareDelegate.isUsingSameSharedHub(hubParent, hubChild)) return null;
        
        Hub hx;
        if (bCheckLink) {
            hx = HubLinkDelegate.getLinkToHub(hubChild, true);
            if (hx != null) {
                String s = HubLinkDelegate.getLinkHubPath(hubChild, true);
                if (propPath != null) s = propPath + "." + s;
                
                if (hx == hubParent) {
                    return s;
                }
                if (HubShareDelegate.isUsingSameSharedAO(hubParent, hx, true)) {
                    return s;
                }
                s = getPropertyPathBetweenHubs(s, hubParent, hx, true);
                if (s != null) return s;
            }
        }
        
        hx = hubChild.getMasterHub();
        if (hx == null) return null;
        
        // links must be type=one from master to detail.
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hubChild);
        if (li == null) return null;
        li = OAObjectInfoDelegate.getReverseLinkInfo(li);
        if (li == null) return null;
        if (li.getType() != OALinkInfo.ONE) return null;

        String pathFromParent = HubDetailDelegate.getPropertyFromMasterToDetail(hubChild);
        if (pathFromParent == null) return null;
        if (propPath != null) pathFromParent = pathFromParent + "." + propPath;
        
        if (hx == hubParent) {
            return pathFromParent;
        }
        if (HubShareDelegate.isUsingSameSharedAO(hubParent, hx, true)) {
            return pathFromParent;
        }
        if (hubChild.getMasterHub() == null) { // could be a hub copy
            if (hx.getObjectClass().equals(hubParent.getObjectClass())) {
                return pathFromParent;
            }
        }
        
        String sx = getPropertyPathBetweenHubs(pathFromParent, hubParent, hx, false);
        if (sx != null) return sx;
        
        return null;
    }
    
}

class LoadPropertyNode {
    String prop;
    LoadPropertyNode[] children;
}
