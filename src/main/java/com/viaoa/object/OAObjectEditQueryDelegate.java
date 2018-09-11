package com.viaoa.object;

import java.lang.reflect.Method;

import javax.swing.JLabel;

import com.viaoa.annotation.OAEditQuery;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.HubListener;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * used to call OAObject.onEdit[Property](Object value, OAEditMessage em) 
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
     * Steps:
     *    call owner objects enabled or visible
     *    call class @editQuery
     *    call class editQuery(eq)
     *    call method @editQuery
     *    call oaObject.editQueryName(eq)
     *    call all hubListeners
     *
     *  used by:
     *      OAJfcController to see if an UI component should be enabled
     *      OAObjetEventDelegate.fireBeforePropertyChange 
     * qqqqq <MORE> qqq
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
                Type type = editQuery.getType();
                
                OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
                callOwnerObjects(editQueryX, oaObj, propertyName);
                // set this query to default value from owner property's enabled value
                editQuery.setAllowed(editQueryX.getAllowed());
            }
        }

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

        // 1: check @OAEditQuery for class
        if (editQuery.getType().checkEnabled) {
            String sx = oi.getEnabledProperty();
            boolean bx;
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getEnabledValue();
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                if (bx != OAConv.toBoolean(valx)) editQuery.setAllowed(false);
            }
        }            
        else if (editQuery.getType() == Type.AllowVisible) {
            String sx = oi.getVisibleProperty();
            boolean bx;
            if (OAString.isNotEmpty(sx)) {
                bx = oi.getVisibleValue();
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                if (bx != OAConv.toBoolean(valx)) editQuery.setAllowed(false);
            }
        }            

        // 2: call editQuery method for class
        callEditQuery(oaObj, null, editQuery);
        
            
        // 3: check @EditQuery for method
        if (editQuery.getType().checkEnabled) {
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
                        Method m = OAObjectInfoDelegate.getMethod(oi, propertyName);
                        if (m != null) {
                            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
                            if (eq != null) {
                                sx = eq.enableProperty();
                                bx = eq.enableValue();
                            }
                            
                        }
                    }
                }
                if (OAString.isNotEmpty(sx)) {
                    Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                    if (bx != OAConv.toBoolean(valx)) editQuery.setAllowed(false);
                }
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
                        Method m = OAObjectInfoDelegate.getMethod(oi, propertyName);
                        if (m != null) {
                            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
                            if (eq != null) {
                                sx = eq.visibleProperty();
                                bx = eq.visibleValue();
                            }
                        }
                    }
                }
                if (OAString.isNotEmpty(sx)) {
                    Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                    if (bx != OAConv.toBoolean(valx)) editQuery.setAllowed(false);
                }
            }
        }
        
        // 4: call editQuery method for method
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
        if (editQuery.getType().checkEnabled) {
            String pp = li.getEnabledProperty();
            boolean b = li.getEnabledValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getEnabledProperty();
                b = oi.getEnabledValue();
            }
            if (OAString.isNotEmpty(pp)) {
                if (editQuery.getAllowed()) {  // only allow enabled property to turn allow=false (not =true)
                    Object valx = OAObjectReflectDelegate.getProperty(objOwner, pp);
                    boolean bx  = OAConv.toBoolean(valx); 
                    if (bx != b) editQuery.setAllowed(false);
                }
            }
        }
        else if (editQuery.getType() == Type.AllowVisible) {
            String pp = li.getVisibleProperty();
            boolean b = li.getVisibleValue();
            if (OAString.isEmpty(pp)) {
                pp = oi.getVisibleProperty();
                b = oi.getVisibleValue();
            }
            if (OAString.isNotEmpty(pp)) {
                if (editQuery.getAllowed()) { // only allow enabled property to turn allow=false (not =true)
                    Object valx = OAObjectReflectDelegate.getProperty(objOwner, pp);
                    boolean bx = OAConv.toBoolean(valx); 
                    if (bx != b) editQuery.setAllowed(false);
                }
            }
        }
        
        callEditQuery(oaObj, null, editQuery);
        if (OAString.isNotEmpty(propertyName)) {
            callEditQuery(oaObj, propertyName, editQuery);
        }
    }
    
    
    
    // called directly if hub.masterObject=null
    protected static void processEditQueryForHubListeners(OAObjectEditQuery editQuery, final Hub hub, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        HubListener[] hl = HubEventDelegate.getAllListeners(hub);
        if (hl == null) return;
        int x = hl.length;
        if (x == 0) return;

        HubEvent hubEvent = null;
        boolean b=true;
        try {
            for (int i=0; i<x; i++) {
                switch (editQuery.getType()) {
                case AllowEnabled:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName);
                    b = (hl[i].getAllowEnabled(hubEvent));
                    break;
                case AllowVisible:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName);
                    b = (hl[i].getAllowVisible(hubEvent));
                    break;

                case VerifyPropertyChange:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, oaObj, propertyName, oldValue, newValue);
                    b = (hl[i].isValidPropertyChange(hubEvent));
                    break;

                case AllowAdd:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = (hl[i].getAllowAdd(hubEvent));
                    break;
                case VerifyAdd:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = (hl[i].isValidAdd(hubEvent));
                    break;
                case AllowRemove:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = (hl[i].getAllowRemove(hubEvent));
                    break;
                case VerifyRemove:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = (hl[i].isValidRemove(hubEvent));
                    break;
                case AllowRemoveAll:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = (hl[i].getAllowRemoveAll(hubEvent));
                    break;
                case VerifyRemoveAll:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = (hl[i].isValidRemoveAll(hubEvent));
                    break;
                case AllowDelete:
                    if (hubEvent == null) hubEvent = new HubEvent(hub);
                    b = (hl[i].getAllowDelete(hubEvent));
                    break;
                case VerifyDelete:
                    if (hubEvent == null) hubEvent = new HubEvent(hub, newValue);
                    b = (hl[i].isValidDelete(hubEvent));
                    break;
                }
                
                if (hubEvent == null) break;
                if (!b) {
                    editQuery.setAllowed(false);
                    String s = hubEvent.getResponse();
                    if (OAString.isNotEmpty(s)) editQuery.setResponse(s);
                    break;
                }
            }
        }
        catch (Exception e) {
            b = false;
            editQuery.setThrowable(e);
            editQuery.setAllowed(false);
        }
        
        if (!b) {
            editQuery.setAllowed(false);
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
     * Used by OAObjectModel objects to allow reference models to be updated after they are created.
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
