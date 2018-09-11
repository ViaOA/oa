package com.viaoa.object;

import java.lang.reflect.Method;

import javax.swing.JLabel;
import com.viaoa.auth.OAAuthDelegate;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.HubListener;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * Allows OAObject to be able to control permission to object/hub, 
 * and interactions with other compenents.  
 * 
 * Works with OAObject and Hub to determine what is allowed/permitted.
 * Uses OAObject annoations, specific methods (onEditQuery*), and HubListeners.
 * 
 * Used to query objects, and find out if certain functions are enabled/visible/allowed,
 * along with other interactive settings/data.
 *
 * Used by OAObject (beforePropChange), Hub (add/remove/removeAll) to check if method is permitted/enabled.
 * Used by OAJfcController and Jfc to set UI components (enabled, visible, tooltip, rendering, etc)
 * 
 * @see OAObjectEditQuery
 * @see OAEditQuery annotation that lists proppaths and values used for enabled/visible.
 * @see OAAnnotationDelegate to see how class and annotation information is stored in Info objects (class/prop/calc/link/method)
 * @author vvia
 */
public class OAObjectEditQueryDelegate {

    public static boolean getAllowEnabled(OAObject obj, String name) {
        return getAllowEnabledEditQuery(obj, name).getAllowed();
    }
    public static boolean getAllowEnabled(Hub hub) {
        return getAllowEnabledEditQuery(hub).getAllowed();
    }
    public static boolean getAllowVisible(OAObject obj, String name) {
        return getAllowVisibleEditQuery(obj, name).getAllowed();
    }

    public static boolean getVerifyPropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
        return getVerifyPropertyChangeEditQuery(obj, propertyName, oldValue, newValue).getAllowed();
    }
    
    public static boolean getAllowAdd(Hub hub) {
        return getAllowAddEditQuery(hub).getAllowed();
    }
    public static boolean getVerifyAdd(Hub hub, OAObject obj) {
        return getVerifyAddEditQuery(hub, obj).getAllowed();
    }
    
    public static boolean getAllowRemove(Hub hub) {
        return getAllowRemoveEditQuery(hub).getAllowed();
    }
    public static boolean getVerifyRemove(Hub hub, OAObject obj) {
        return getVerifyRemoveEditQuery(hub, obj).getAllowed();
    }
    
    public static boolean getAllowRemoveAll(Hub hub) {
        return getAllowRemoveAllEditQuery(hub).getAllowed();
    }
    public static boolean getVerifyRemoveAll(Hub hub) {
        return getVerifyRemoveAllEditQuery(hub).getAllowed();
    }
    
    public static boolean getAllowDelete(OAObject obj) {
        return getAllowDeleteEditQuery(obj).getAllowed();
    }
    public static boolean getVerifyDelete(OAObject obj) {
        return getVerifyDeleteEditQuery(obj).getAllowed();
    }
    
    
    public static String getFormat(OAObject obj, String propertyName, String defaultFormat) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.GetFormat);
        em.setName(propertyName);
        em.setFormat(defaultFormat);
        callEditQuery(obj, null, em);
        callEditQuery(obj, propertyName, em);
        return em.getFormat();
    }
    public static String getToolTip(OAObject obj, String propertyName, String defaultToolTip) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.GetToolTip);
        em.setName(propertyName);
        em.setToolTip(defaultToolTip);
        callEditQuery(obj, null, em);
        callEditQuery(obj, propertyName, em);
        return em.getToolTip();
    }
    public static void RenderLabel(OAObject obj, String propertyName, JLabel label) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.RenderLabel);
        em.setName(propertyName);
        em.setLabel(label);
        callEditQuery(obj, null, em);
        callEditQuery(obj, propertyName, em);
    }
    
    // @param name used for property, calc properfy, method
    public static OAObjectEditQuery getAllowEnabledEditQuery(final OAObject oaObj, final String name) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowEnabled);
        editQuery.setName(name);
        processEditQuery(editQuery, oaObj, name, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getAllowEnabledEditQuery(Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowEnabled);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyPropertyChangeEditQuery(final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyPropertyChange);
        editQuery.setName(propertyName);
        editQuery.setValue(newValue);
        
        processEditQuery(editQuery, oaObj, propertyName, oldValue, newValue);
        return editQuery;
    }
    
    public static OAObjectEditQuery getAllowVisibleEditQuery(final OAObject oaObj, final String name) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowVisible);
        editQuery.setName(name);
        
        processEditQuery(editQuery, oaObj, name, null, null);
        return editQuery;
    }
    
    
    public static OAObjectEditQuery getAllowAddEditQuery(final Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowAdd);
        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyAddEditQuery(final Hub hub, final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyAdd);
        editQuery.setValue(oaObj);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, oaObj, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }

    public static OAObjectEditQuery getAllowRemoveEditQuery(final Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowRemove);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyRemoveEditQuery(final Hub hub, final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyRemove);
        editQuery.setValue(oaObj);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, oaObj, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
        
    public static OAObjectEditQuery getAllowRemoveAllEditQuery(final Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowRemoveAll);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyRemoveAllEditQuery(final Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyRemoveAll);

        OAObject objMaster = hub.getMasterObject();
        if (objMaster == null) {
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }

    public static OAObjectEditQuery getAllowDeleteEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowDelete);
        editQuery.setValue(oaObj);
        
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyDeleteEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyDelete);
        editQuery.setValue(oaObj);
        
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    
    public static OAObjectEditQuery getConfirmPropertyChangeEditQuery(final OAObject oaObj, String property, Object newValue, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.GetConfirmPropertyChange);
        editQuery.setValue(newValue);
        editQuery.setName(property);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        processEditQuery(editQuery, oaObj, property, null, newValue);
        return editQuery;
    }
    
    
    /** 
     * This will process an Edit Query, calling editQuery methods on OAObject, properties, links, methods (depending on type of edit query)
     * 
     *  used by:
     *      OAJfcController to see if an UI component should be enabled
     *      OAObjetEventDelegate.fireBeforePropertyChange
     *      Hub add/remove/removeAll 
     */
    protected static void processEditQuery(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        if (oaObj == null) return;
        
        // first call owners (recursive)
        if (editQuery.getType().checkOwner) {
            if (editQuery.getType() == Type.AllowVisible || editQuery.getType() == Type.AllowEnabled) {
                // get default value from owner
                callOwnerObjects(editQuery, oaObj, propertyName);
            }
            else {
                // need to check owner using AllowEnabled
                OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
                editQueryX.setAllowed(editQuery.getAllowed());
                callOwnerObjects(editQueryX, oaObj, propertyName);
                // set this query to default value from owner property's enabled value
                editQuery.setAllowed(editQueryX.getAllowed());
            }
        }

        final OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

        // 1: check @OAEditQuery for class
        if (editQuery.getType() == Type.AllowEnabled || editQuery.getType().checkEnabledFirst) {
            String sx = oi.getEnabledProperty();
            boolean bx;
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getEnabledValue();
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                boolean b = (bx == OAConv.toBoolean(valx));
                editQuery.setAllowed(b);
            }
            
            sx = oi.getUserEnabledProperty();
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getUserEnabledValue();
                OAObject user = OAAuthDelegate.getCurrentUser();
                boolean b;
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, sx);
                    b = (bx == OAConv.toBoolean(valx));
                }
                editQuery.setAllowed(b);
            }
        }            
        else if (editQuery.getType() == Type.AllowVisible) {
            String sx = oi.getVisibleProperty();
            boolean bx;
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getVisibleValue();
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                boolean b = (bx == OAConv.toBoolean(valx));
                editQuery.setAllowed(b);
            }
            sx = oi.getUserVisibleProperty();
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getUserVisibleValue();
                OAObject user = OAAuthDelegate.getCurrentUser();
                boolean b;
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, sx);
                    b = (bx == OAConv.toBoolean(valx));
                }
                editQuery.setAllowed(b);
            }
        }            

        // 2: call onEditQuery for class
        if (editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            callEditQuery(oaObj, null, editQueryX);
            editQuery.setAllowed(editQueryX.getAllowed());
        }
        callEditQuery(oaObj, null, editQuery);
        
            
        // 3: check @OAEditQuery for method
        if (editQuery.getType() == Type.AllowEnabled || editQuery.getType().checkEnabledFirst) {
            String sx = null;
            boolean bx = true;
            OAPropertyInfo pi = oi.getPropertyInfo(propertyName);
            if (pi != null) {
                sx = pi.getEnabledProperty();
                bx = pi.getEnabledValue();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    sx = li.getEnabledProperty();
                    bx = li.getEnabledValue();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        sx = ci.getEnabledProperty();
                        bx = ci.getEnabledValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            sx = mi.getEnabledProperty();
                            bx = mi.getEnabledValue();
                        }
                    }
                }
            }
            boolean bAlreadySet = false;
            if (OAString.isNotEmpty(sx)) {
                bAlreadySet = true;
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                boolean b = (bx == OAConv.toBoolean(valx));
                editQuery.setAllowed(b);
            }
            
            sx = null;
            bx = true;
            pi = oi.getPropertyInfo(propertyName);
            if (pi != null) {
                sx = pi.getUserEnabledProperty();
                bx = pi.getUserEnabledValue();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    sx = li.getUserEnabledProperty();
                    bx = li.getUserEnabledValue();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        sx = ci.getUserEnabledProperty();
                        bx = ci.getUserEnabledValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            sx = mi.getUserEnabledProperty();
                            bx = mi.getUserEnabledValue();
                        }
                    }
                }
            }
            if (OAString.isNotEmpty(sx)) {
                OAObject user = OAAuthDelegate.getCurrentUser();
                boolean b;
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, sx);
                    b = (bx == OAConv.toBoolean(valx));
                }
                if (!bAlreadySet || !b) editQuery.setAllowed(b);  
            }
        }
        else if (editQuery.getType() == Type.AllowVisible) {
            String sx = null;
            boolean bx = true;
            OAPropertyInfo pi = oi.getPropertyInfo(propertyName);
            if (pi != null) {
                sx = pi.getVisibleProperty();
                bx = pi.getVisibleValue();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    sx = li.getVisibleProperty();
                    bx = li.getVisibleValue();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        sx = ci.getVisibleProperty();
                        bx = ci.getVisibleValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            sx = mi.getVisibleProperty();
                            bx = mi.getVisibleValue();
                        }
                    }
                }
            }
            boolean bAlreadySet = false;
            if (OAString.isNotEmpty(sx)) {
                bAlreadySet = true;
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                boolean b = (bx == OAConv.toBoolean(valx));
                editQuery.setAllowed(b);
            }

            sx = null;
            bx = true;
            pi = oi.getPropertyInfo(propertyName);
            if (pi != null) {
                sx = pi.getUserVisibleProperty();
                bx = pi.getUserVisibleValue();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    sx = li.getUserVisibleProperty();
                    bx = li.getUserVisibleValue();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        sx = ci.getUserVisibleProperty();
                        bx = ci.getUserVisibleValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            sx = mi.getUserVisibleProperty();
                            bx = mi.getUserVisibleValue();
                        }
                    }
                }
            }
            if (OAString.isNotEmpty(sx)) {
                OAObject user = OAAuthDelegate.getCurrentUser();
                boolean b;
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, sx);
                    b = (bx == OAConv.toBoolean(valx));
                }
                if (!bAlreadySet || !b) editQuery.setAllowed(b);  
            }
        }
        
        // 4: call editQuery method for method
        if (editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            callEditQuery(oaObj, propertyName, editQueryX);
            editQuery.setAllowed(editQueryX.getAllowed());
        }
        callEditQuery(oaObj, propertyName, editQuery);
        
        // 5: call hub listeners
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
        if (hubs != null) {
            // check hub.listeners
            for (Hub h : hubs) {
                processEditQueryForHubListeners(editQuery, h, oaObj, propertyName, oldValue, newValue);
            }
        }
    }

    protected static void callOwnerObjects(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName) {
        callOwnerObjects(editQuery, oaObj, propertyName, null);
    }
    protected static void callOwnerObjects(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final OALinkInfo linkInfo) {
        // recursive, goto top owner first
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        
        OALinkInfo li = oi.getOwnedByOne();
        if (li == null) return;

        OAObject objOwner = (OAObject) li.getValue(oaObj);
        if (objOwner == null) return;
        
        callOwnerObjects(editQuery, objOwner, li.getReverseName(), li);  // recursive

        if (linkInfo == null) return;
        li = linkInfo;
        
        // now call editQuery & editQuery[propertyName]

        // check @OAEditQuery Annotation
        if (editQuery.getType() == Type.AllowEnabled || editQuery.getType().checkEnabledFirst) {
            String pp = li.getEnabledProperty();
            boolean b = li.getEnabledValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getEnabledProperty();
                b = oi.getEnabledValue();
            }
            boolean bAlreadySet = false;
            if (OAString.isNotEmpty(pp)) {
                bAlreadySet = true;
                Object valx = OAObjectReflectDelegate.getProperty(objOwner, pp);
                boolean bx = OAConv.toBoolean(valx); 
                editQuery.setAllowed((bx == b));
            }
            pp = li.getUserEnabledProperty();
            b = li.getUserEnabledValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getUserEnabledProperty();
                b = oi.getUserEnabledValue();
            }
            if (OAString.isNotEmpty(pp)) {
                OAObject user = OAAuthDelegate.getCurrentUser();
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, pp);
                    b = (b == OAConv.toBoolean(valx));
                }
                if (!bAlreadySet || !b) editQuery.setAllowed(b);
            }
        }
        else if (editQuery.getType() == Type.AllowVisible) {
            String pp = li.getVisibleProperty();
            boolean b = li.getVisibleValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getVisibleProperty();
                b = oi.getVisibleValue();
            }
            boolean bAlreadySet = false;
            if (OAString.isNotEmpty(pp)) {
                bAlreadySet = true;
                Object valx = OAObjectReflectDelegate.getProperty(objOwner, pp);
                boolean bx = OAConv.toBoolean(valx); 
                editQuery.setAllowed((bx == b));
            }
            pp = li.getUserVisibleProperty();
            b = li.getUserVisibleValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getUserVisibleProperty();
                b = oi.getUserVisibleValue();
            }
            if (OAString.isNotEmpty(pp)) {
                OAObject user = OAAuthDelegate.getCurrentUser();
                if (user == null) b = false;
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, pp);
                    b = (b == OAConv.toBoolean(valx));
                }
                if (!bAlreadySet || !b) editQuery.setAllowed(b);
            }
        }
        
        if (editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            callEditQuery(oaObj, null, editQueryX);
            editQuery.setAllowed(editQueryX.getAllowed());
        }
        
        callEditQuery(oaObj, null, editQuery);
        if (OAString.isNotEmpty(propertyName)) {
            if (editQuery.getType().checkEnabledFirst) {
                OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
                editQueryX.setAllowed(editQuery.getAllowed());
                callEditQuery(oaObj, propertyName, editQueryX);
                editQuery.setAllowed(editQueryX.getAllowed());
            }
            callEditQuery(oaObj, propertyName, editQuery);
        }
    }
    
    // called directly if hub.masterObject=null
    protected static void processEditQueryForHubListeners(OAObjectEditQuery editQuery, final Hub hub, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        if (editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            _processEditQueryForHubListeners(editQueryX, hub, oaObj, propertyName, oldValue, newValue);
            editQuery.setAllowed(editQueryX.getAllowed());
        }    
        _processEditQueryForHubListeners(editQuery, hub, oaObj, propertyName, oldValue, newValue);        
    }
    protected static void _processEditQueryForHubListeners(OAObjectEditQuery editQuery, final Hub hub, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        HubListener[] hl = HubEventDelegate.getAllListeners(hub);
        if (hl == null) return;
        int x = hl.length;
        if (x == 0) return;
        final boolean bBefore = editQuery.getAllowed();
        
        HubEvent hubEvent = null;
        try {
            for (int i=0; i<x; i++) {
                boolean b = editQuery.getAllowed();

                switch (editQuery.getType()) {
                case AllowEnabled:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName);
                    b = hl[i].getAllowEnabled(hubEvent, b);
                    break;
                case AllowVisible:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName);
                    b = hl[i].getAllowVisible(hubEvent, b);
                    break;

                case VerifyPropertyChange:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName, oldValue, newValue);
                    b = hl[i].isValidPropertyChange(hubEvent, b);
                    break;

                case AllowAdd:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = hl[i].getAllowAdd(hubEvent, b);
                    break;
                case VerifyAdd:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = hl[i].isValidAdd(hubEvent, b);
                    break;
                case AllowRemove:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = hl[i].getAllowRemove(hubEvent, b);
                    break;
                case VerifyRemove:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = hl[i].isValidRemove(hubEvent, b);
                    break;
                case AllowRemoveAll:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = hl[i].getAllowRemoveAll(hubEvent, b);
                    break;
                case VerifyRemoveAll:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = hl[i].isValidRemoveAll(hubEvent, b);
                    break;
                case AllowDelete:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = hl[i].getAllowDelete(hubEvent, b);
                    break;
                case VerifyDelete:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = hl[i].isValidDelete(hubEvent, b);
                    break;
                }
                
                if (hubEvent == null) break;
                editQuery.setAllowed(b);
                String s = hubEvent.getResponse();
                if (OAString.isNotEmpty(s)) editQuery.setResponse(s);
            }
        }
        catch (Exception e) {
            editQuery.setThrowable(e);
            editQuery.setAllowed(false);
        }

        if (bBefore != editQuery.getAllowed()) {
            String s = editQuery.getResponse();
            if (OAString.isEmpty(s)) s = editQuery.getType() + " failed for " + oaObj.getClass().getSimpleName() + "." + propertyName;
            editQuery.setResponse(s);
        }
    }
    
    
    protected static void callEditQuery(final OAObject oaObj, String propertyName, final OAObjectEditQuery em) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        
        if (propertyName == null) propertyName = "";
        
        Method method = oi.getEditQueryMethod(propertyName);
        //was: Method method = OAObjectInfoDelegate.getMethod(oi, "onEditQuery"+propertyName, 1);
        if (method == null) return;
            //Class[] cs = method.getParameterTypes();
            //if (cs[0].equals(OAObjectEditQuery.class)) {
                try {
                    method.invoke(oaObj, new Object[] {em});
                }
                catch (Exception e) {
                    em.setThrowable(e);
                    em.setAllowed(false);
                }
            //}
    }    
   
    
    /**
     * Used by OAObjectModel objects to allow model object to be updated after it is created.
     * @param clazz, ex: from SalesOrderModel, SalesOrder.class
     * @param property  ex:  "SalesOrderItems"
     * @param model ex: SalesOrderItemModel
     */
    public static void onEditQueryModel(Class clazz, String property, OAObjectModel model) {
        if (clazz == null || OAString.isEmpty(property) || model == null) return;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        Method m = OAObjectInfoDelegate.getMethod(oi, "onEditQuery" + property + "Model", 1);
        if (m != null) {
            Class[] cs = m.getParameterTypes();
            if (cs[0].equals(OAObjectModel.class)) {
                try {
                    m.invoke(null, new Object[] {model});
                }
                catch (Exception e) {
                    throw new RuntimeException("Exception calling static method onEditQuery"+property, e);
                }
            }
        }
    }
}
