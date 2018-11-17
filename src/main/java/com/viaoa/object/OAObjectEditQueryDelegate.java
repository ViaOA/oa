package com.viaoa.object;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.swing.JLabel;
import com.viaoa.auth.OAAuthDelegate;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubChangeListener;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.HubListener;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * Allows OA to be able to control permission to object/hub, 
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
 * @see OAObjectEditQuery for list of types that can be used.
 * @see OAEditQuery annotation that lists proppaths and values used for enabled/visible.
 * @see OAAnnotationDelegate to see how class and annotation information is stored in Info objects (class/prop/calc/link/method)
 * @author vvia
 */
public class OAObjectEditQueryDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectEditQueryDelegate.class.getName());
    
    public static boolean getAllowVisible(OAObject obj, String name) {
        return getAllowVisibleEditQuery(obj, name).getAllowed();
    }
    public static boolean getAllowVisible(Hub hub) {
        return getAllowVisibleEditQuery(hub).getAllowed();
    }
    
    public static boolean getAllowEnabled(OAObject obj, String name) {
        return getAllowEnabled(obj, name, false);
    }
    public static boolean getAllowEnabled(OAObject obj, String name, boolean bProcessedCheck) {
        return getAllowEnabledEditQuery(obj, name, bProcessedCheck).getAllowed();
    }
    public static boolean getAllowEnabled(Hub hub) {
        return getAllowEnabledEditQuery(hub).getAllowed();
    }
    public static boolean getAllowCopy(OAObject oaObj) {
        if (oaObj == null) return false;
        return getAllowCopyEditQuery(oaObj).getAllowed();
    }
    public static OAObject getCopy(OAObject oaObj) {
        if (oaObj == null) return null;
        OAObjectEditQuery eq = getCopyEditQuery(oaObj);

        Object objx = eq.getValue();
        if (!(objx instanceof OAObject)) {
            if (!eq.getAllowed()) return null;
            objx = oaObj.createCopy();
        }
        
        getAfterCopyEditQuery(oaObj, (OAObject) objx);
        return (OAObject) objx;
    }
    public static void afterCopy(OAObject oaObj, OAObject oaObjCopy) {
        if (oaObj == null || oaObjCopy == null) return;
        getAfterCopyEditQuery(oaObj, oaObjCopy);
    }
    
    
    public static boolean getVerifyPropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
        return getVerifyPropertyChangeEditQuery(obj, propertyName, oldValue, newValue).getAllowed();
    }
    
    public static boolean getAllowAdd(Hub hub) {
        return getAllowAdd(hub, false);
    }
    public static boolean getAllowAdd(Hub hub, boolean bProcessedCheck) {
        return getAllowAddEditQuery(hub, bProcessedCheck).getAllowed();
    }
    public static boolean getVerifyAdd(Hub hub, OAObject obj) {
        return getVerifyAddEditQuery(hub, obj).getAllowed();
    }
    
    public static boolean getAllowRemove(Hub hub, boolean bProcessedCheck) {
        return getAllowRemoveEditQuery(hub, bProcessedCheck).getAllowed();
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
    
    public static boolean getAllowDelete(OAObject obj, boolean bProcessedCheck) {
        return getAllowDeleteEditQuery(obj, bProcessedCheck).getAllowed();
    }
    public static boolean getVerifyDelete(OAObject obj) {
        return getVerifyDeleteEditQuery(obj).getAllowed();
    }
    
    public static boolean getAllowSave(OAObject obj) {
        return getAllowSaveEditQuery(obj).getAllowed();
    }
    public static boolean getVerifySave(OAObject obj) {
        return getVerifySaveEditQuery(obj).getAllowed();
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
    public static void renderLabel(OAObject obj, String propertyName, JLabel label) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.RenderLabel);
        em.setName(propertyName);
        em.setLabel(label);
        callEditQuery(obj, null, em);
        callEditQuery(obj, propertyName, em);
    }
    public static void updateLabel(OAObject obj, String propertyName, JLabel label) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.UpdateLabel);
        em.setName(propertyName);
        em.setLabel(label);
        callEditQuery(obj, propertyName, em);
    }

    
    public static OAObjectEditQuery getAllowVisibleEditQuery(final OAObject oaObj, final String name) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowVisible);
        editQuery.setName(name);
        
        processEditQuery(editQuery, oaObj, name, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getAllowVisibleEditQuery(Hub hub) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowVisible);

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
    
    
    public static OAObjectEditQuery getAllowEnabledEditQuery(final OAObject oaObj, final String name, final boolean bProcessedCheck) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowEnabled);
        editQuery.setName(name);
        processEditQuery(editQuery, oaObj, name, null, null, bProcessedCheck);
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
    public static OAObjectEditQuery getAllowCopyEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowCopy);
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getCopyEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.GetCopy);
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getAfterCopyEditQuery(final OAObject oaObj, final OAObject oaObjCopy) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AfterCopy);
        editQuery.setValue(oaObjCopy);
        processEditQuery(editQuery, oaObj, null, null, oaObjCopy);
        return editQuery;
    }

    public static OAObjectEditQuery getVerifyPropertyChangeEditQuery(final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyPropertyChange);
        editQuery.setName(propertyName);
        editQuery.setValue(newValue);
        
        processEditQuery(editQuery, oaObj, propertyName, oldValue, newValue);
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyCommandEditQuery(final OAObject oaObj, final String methodName) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyCommand);
        editQuery.setName(methodName);
        
        processEditQuery(editQuery, oaObj, methodName, null, null);
        return editQuery;
    }
    
    protected static void updateEditProcessed(OAObjectEditQuery editQuery) {
        if (editQuery == null) return;
        OAObject user = OAAuthDelegate.getUser();
        if (user == null) {
            editQuery.setResponse("processed=true, and user=null");
            editQuery.setAllowed(false);
        }
        else if (!OAAuthDelegate.canEditProcessed()) {
            String sx = OAAuthDelegate.getAllowEditProcessedPropertyPath();
            editQuery.setResponse("User."+sx+"=false");
            editQuery.setAllowed(false);
        }
    }
    
    public static OAObjectEditQuery getAllowAddEditQuery(final Hub hub, final boolean bProcessedCheck) {
        if (hub == null) return null;
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowAdd);
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li == null || li.getPrivateMethod()) {
            if (bProcessedCheck) {
                if (hub.getOAObjectInfo().getProcessed()) {
                    updateEditProcessed(editQuery);
                }
            }
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            OAObject objMaster = hub.getMasterObject();
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null, bProcessedCheck);
        }
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyAddEditQuery(final Hub hub, final OAObject oaObj) {
        if (hub == null) return null;
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyAdd);
        editQuery.setValue(oaObj);

        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li == null || li.getPrivateMethod()) {
            processEditQueryForHubListeners(editQuery, hub, oaObj, null, null, null);
        }
        else {
            OAObject objMaster = hub.getMasterObject();
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, null, null);
        }
        return editQuery;
    }
    
    public static OAObjectEditQuery getAllowRemoveEditQuery(final Hub hub, final boolean bProcessedCheck) {
        if (hub == null) return null;
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowRemove);

        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li == null || li.getPrivateMethod()) {
            if (bProcessedCheck && hub != null) {
                if (hub.getOAObjectInfo().getProcessed()) {
                    updateEditProcessed(editQuery);
                }
            }
            processEditQueryForHubListeners(editQuery, hub, null, null, null, null);
        }
        else {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            OAObject objMaster = hub.getMasterObject();
            processEditQuery(editQuery, objMaster, propertyName, null, null, bProcessedCheck);
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

    public static OAObjectEditQuery getAllowSaveEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowSave);
        editQuery.setValue(oaObj);
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    public static OAObjectEditQuery getVerifySaveEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifySave);
        editQuery.setValue(oaObj);
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }

    public static OAObjectEditQuery getAllowDeleteEditQuery(final OAObject oaObj, boolean bProcessedCheck) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.AllowDelete);
        editQuery.setValue(oaObj);
        
        processEditQuery(editQuery, oaObj, null, null, null, bProcessedCheck);
        return editQuery;
    }
    public static OAObjectEditQuery getVerifyDeleteEditQuery(final OAObject oaObj) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.VerifyDelete);
        editQuery.setValue(oaObj);
        
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    
    public static OAObjectEditQuery getConfirmPropertyChangeEditQuery(final OAObject oaObj, String property, Object newValue, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForPropertyChange);
        editQuery.setValue(newValue);
        editQuery.setName(property);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        processEditQuery(editQuery, oaObj, property, null, newValue);
        return editQuery;
    }
    public static OAObjectEditQuery getConfirmCommandEditQuery(final OAObject oaObj, String methodName, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForCommand);
        editQuery.setName(methodName);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        processEditQuery(editQuery, oaObj, methodName, null, null);
        return editQuery;
    }

    public static OAObjectEditQuery getConfirmSaveEditQuery(final OAObject oaObj, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForSave);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    
    public static OAObjectEditQuery getConfirmDeleteEditQuery(final OAObject oaObj, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForDelete);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        processEditQuery(editQuery, oaObj, null, null, null);
        return editQuery;
    }
    
    public static OAObjectEditQuery getConfirmRemoveEditQuery(final Hub hub, final OAObject oaObj, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForRemove);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        OAObject objMaster = hub.getMasterObject();
        if (objMaster != null) {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, oaObj, oaObj);
        }
        return editQuery;
    }

    public static OAObjectEditQuery getConfirmAddEditQuery(final Hub hub, final OAObject oaObj, String confirmMessage, String confirmTitle) {
        final OAObjectEditQuery editQuery = new OAObjectEditQuery(Type.SetConfirmForAdd);
        editQuery.setConfirmMessage(confirmMessage);
        editQuery.setConfirmTitle(confirmTitle);
        
        OAObject objMaster = hub.getMasterObject();
        if (objMaster != null) {
            String propertyName = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            editQuery.setName(propertyName);
            processEditQuery(editQuery, objMaster, propertyName, oaObj, oaObj);
        }
        return editQuery;
    }

    
    protected static void processEditQuery(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        processEditQuery(editQuery, oaObj, propertyName, oldValue, newValue, false);
    }
    
    protected static void processEditQuery(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue, final boolean bProcessedCheck) {
        _processEditQuery(editQuery, oaObj, propertyName, oldValue, newValue, bProcessedCheck);
        
        if (DEMO_AllowAllToPass) {
            editQuery.setThrowable(null);
            editQuery.setAllowed(true);
        }
        
    }
    private static boolean DEMO_AllowAllToPass;
    public static void demoAllowAllToPass(boolean b) {
        String msg = "WARNING: OAObjectEditQueryDelegate.demoAllowAllToPass="+b;
        if (b) msg += " - all OAObjectEditQuery will be allowed";
        LOG.warning(msg);
        for (int i=0; i<20; i++) {
            System.out.println(msg);
            if (!b) break;
        }
        OAObjectEditQueryDelegate.DEMO_AllowAllToPass = b;
    }
    
    
    /** 
     * This will process an Edit Query, calling editQuery methods on OAObject, properties, links, methods (depending on type of edit query)
     * 
     *  used by:
     *      OAJfcController to see if an UI component should be enabled
     *      OAObjetEventDelegate.fireBeforePropertyChange
     *      Hub add/remove/removeAll 
     */
    protected static void _processEditQuery(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue, final boolean bProcessedCheck) {
        if (oaObj == null) return;
        
        // first call owners (recursive)
        if (editQuery.getType().checkOwner) {
            recursiveProcess(editQuery, oaObj, propertyName);
        }

        final OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

        if (bProcessedCheck && editQuery.getAllowed() && ((editQuery.getType() == Type.AllowEnabled) || editQuery.getType().checkEnabledFirst) && OAString.isEmpty(propertyName) && oi.getProcessed()) {
            // isProcessed=true, need to check if user has rights to edit
            updateEditProcessed(editQuery);
        }
        
        // call onEditQuery for class, which can override bPassed
        if (editQuery.getType() != Type.AllowEnabled && editQuery.getType() != Type.AllowVisible) {
            callEditQuery(oaObj, null, editQuery);
        }
        
        if (editQuery.getAllowed() && editQuery.getType() == Type.AllowVisible && OAString.isNotEmpty(propertyName)) {
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
            if (OAString.isNotEmpty(sx)) {
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, sx);
                editQuery.setAllowed(bx == OAConv.toBoolean(valx));
                if (!editQuery.getAllowed() && OAString.isEmpty(editQuery.getResponse())) {
                    editQuery.setAllowed(false);
                    String s = "Not visible, "+oaObj.getClass().getSimpleName()+"."+sx+" is not "+bx;
                    editQuery.setResponse(s);
                }
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
            if (editQuery.getAllowed() && OAString.isNotEmpty(sx)) {
                OAObject user = OAAuthDelegate.getUser();
                if (user == null) editQuery.setAllowed(false);
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, sx);
                    editQuery.setAllowed(bx == OAConv.toBoolean(valx));
                }
                if (!editQuery.getAllowed() && OAString.isEmpty(editQuery.getResponse())) {
                    editQuery.setAllowed(false);
                    String s = user == null ? "User" : user.getClass().getSimpleName();
                    s = "Not visible, "+s+"."+sx+" is not "+bx;
                    editQuery.setResponse(s);
                }
            }
        }
        else if (editQuery.getAllowed() && (editQuery.getType() == Type.AllowEnabled || editQuery.getType().checkEnabledFirst) && OAString.isNotEmpty(propertyName)) {
            String enabledName = null;
            boolean enabledValue = true;
            OAPropertyInfo pi = oi.getPropertyInfo(propertyName);
            boolean bIsProcessed = false;
            if (pi != null) {
                enabledName = pi.getEnabledProperty();
                enabledValue = pi.getEnabledValue();
                bIsProcessed = pi.getProcessed();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    enabledName = li.getEnabledProperty();
                    enabledValue = li.getEnabledValue();
                    bIsProcessed = li.getProcessed();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        enabledName = ci.getEnabledProperty();
                        enabledValue = ci.getEnabledValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            enabledName = mi.getEnabledProperty();
                            enabledValue = mi.getEnabledValue();
                        }
                    }
                }
            }

            if (bProcessedCheck && bIsProcessed) {
                updateEditProcessed(editQuery);
            }
            
            if (editQuery.getAllowed() && OAString.isNotEmpty(enabledName)) {
                Object valx = OAObjectReflectDelegate.getProperty(oaObj, enabledName);
                editQuery.setAllowed(enabledValue == OAConv.toBoolean(valx));
                if (!editQuery.getAllowed() && OAString.isEmpty(editQuery.getResponse())) {
                    editQuery.setAllowed(false);
                    String s = "Not enabled, "+oaObj.getClass().getSimpleName()+"."+enabledName+" is not "+enabledValue;
                    editQuery.setResponse(s);
                }
            }
            
            enabledName = null;
            enabledValue = true;
            pi = oi.getPropertyInfo(propertyName);
            if (pi != null) {
                enabledName = pi.getUserEnabledProperty();
                enabledValue = pi.getUserEnabledValue();
            }
            else {
                OALinkInfo li = oi.getLinkInfo(propertyName);
                if (li != null) {
                    enabledName = li.getUserEnabledProperty();
                    enabledValue = li.getUserEnabledValue();
                }
                else {
                    OACalcInfo ci = oi.getCalcInfo(propertyName);
                    if (ci != null) {
                        enabledName = ci.getUserEnabledProperty();
                        enabledValue = ci.getUserEnabledValue();
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(propertyName);
                        if (mi != null) {
                            enabledName = mi.getUserEnabledProperty();
                            enabledValue = mi.getUserEnabledValue();
                        }
                    }
                }
            }
            if (editQuery.getAllowed() && OAString.isNotEmpty(enabledName)) {
                OAObject user = OAAuthDelegate.getUser();
                if (user == null) editQuery.setAllowed(false);
                else {
                    Object valx = OAObjectReflectDelegate.getProperty(user, enabledName);
                    editQuery.setAllowed(enabledValue == OAConv.toBoolean(valx));
                }
                if (!editQuery.getAllowed() && OAString.isEmpty(editQuery.getResponse())) {
                    editQuery.setAllowed(false);
                    String s = user == null ? "User" : user.getClass().getSimpleName();
                    s = "Not enabled, "+s+"."+enabledName+" is not "+enabledValue;
                    editQuery.setResponse(s);
                }
            }
        }

        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
        
        // call enabled editQuery for prop/link method
        // this can override eq.allowed
        if (OAString.isNotEmpty(propertyName) && editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            editQueryX.setName(editQuery.getName());
            callEditQuery(oaObj, propertyName, editQueryX);

            // call hub listeners
            if (hubs != null) {
                for (Hub h : hubs) {
                    if (h == null) continue;
                    processEditQueryForHubListeners(editQueryX, h, oaObj, propertyName, oldValue, newValue);
                }
            }
            editQuery.setAllowed(editQueryX.getAllowed());
            if (OAString.isEmpty(editQuery.getResponse())) editQuery.setResponse(editQueryX.getResponse());
        }
        
        if (OAString.isNotEmpty(propertyName)) {
            // this can override eq.allowed
            callEditQuery(oaObj, propertyName, editQuery);
        }
        
        // call hub listeners
        if (hubs != null) {
            for (Hub h : hubs) {
                if (h == null) continue;
                processEditQueryForHubListeners(editQuery, h, oaObj, propertyName, oldValue, newValue);
            }
        }
    }
    
    protected static void recursiveProcess(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName) {
        _recursiveProcess(editQuery, oaObj, propertyName, null);
    }
    protected static void _recursiveProcess(OAObjectEditQuery editQuery, final OAObject oaObj, final String propertyName, final OALinkInfo li) {
        // recursive, goto top owner first
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        
        OALinkInfo lix = oi.getOwnedByOne();
        if (lix != null) {
            OAObject objOwner = (OAObject) lix.getValue(oaObj);
            if (objOwner != null) {
                lix = lix.getReverseLinkInfo();
                _recursiveProcess(editQuery, objOwner, lix.getName(), lix);
            }
        }
        
        String pp;
        boolean b;
        Object valx;
        boolean bPassed = editQuery.getAllowed();
        
        // check @OAEditQuery Annotation
        if (editQuery.getType() == Type.AllowVisible) {
            pp = oi.getVisibleProperty();
            if (bPassed && OAString.isNotEmpty(pp)) {
                b = oi.getVisibleValue();
                valx = OAObjectReflectDelegate.getProperty(oaObj, pp);
                bPassed = (b == OAConv.toBoolean(valx));
                if (!bPassed) {
                    editQuery.setAllowed(false);
                    String s = "Not visible, rule for "+oaObj.getClass().getSimpleName()+", "+pp+" must be "+b;
                    editQuery.setResponse(s);
                }
            }
            pp = oi.getUserVisibleProperty();
            if (bPassed && OAString.isNotEmpty(pp)) {
                b = oi.getUserVisibleValue();
                OAObject user = OAAuthDelegate.getUser();
                if (user == null) bPassed = false;
                else {
                    valx = OAObjectReflectDelegate.getProperty(user, pp);
                    bPassed = (b == OAConv.toBoolean(valx));
                }
                if (!bPassed) {
                    editQuery.setAllowed(false);
                    String s = "Not visible, user rule for "+oaObj.getClass().getSimpleName()+", ";
                    if (user == null) s = "OAAuthDelegate.getUser returned null";
                    else s = "User."+pp+" must be "+b;
                    editQuery.setResponse(s);
                }
            }
            
            // this can overwrite editQuery.allowed        
            callEditQuery(oaObj, null, editQuery);
            bPassed = editQuery.getAllowed();
            if (!bPassed && OAString.isEmpty(editQuery.getResponse())) {
                String s = "Not visible, edit query for "+oaObj.getClass().getSimpleName()+" allowVisible returned false";
                editQuery.setResponse(s);
            }
            
            if (bPassed && li != null) {
                pp = li.getVisibleProperty();
                if (OAString.isNotEmpty(pp)) {
                    b = li.getVisibleValue();
                    valx = OAObjectReflectDelegate.getProperty(oaObj, pp);
                    bPassed = (b == OAConv.toBoolean(valx));
                    if (!bPassed) {
                        editQuery.setAllowed(false);
                        String s = "Not visible, rule for "+oaObj.getClass().getSimpleName()+"."+propertyName+", "+pp+" must be "+b;
                        editQuery.setResponse(s);
                    }
                }
            }
            if (bPassed && li != null) {
                pp = li.getUserVisibleProperty();
                if (OAString.isNotEmpty(pp)) {
                    b = li.getUserVisibleValue();
                    OAObject user = OAAuthDelegate.getUser();
                    if (user == null) bPassed = false;
                    else {
                        valx = OAObjectReflectDelegate.getProperty(user, pp);
                        bPassed = (b == OAConv.toBoolean(valx));
                    }
                    if (!bPassed) {
                        editQuery.setAllowed(false);
                        String s = "Not visible, user rule for "+oaObj.getClass().getSimpleName()+"."+propertyName+", ";
                        if (user == null) s = "OAAuthDelegate.getUser returned null";
                        else s = "User."+pp+" must be "+b;
                        editQuery.setResponse(s);
                    }
                }
            }
            
            // this can overwrite editQuery.allowed        
            if (li != null && OAString.isNotEmpty(propertyName)) {
                callEditQuery(oaObj, propertyName, editQuery);
                bPassed = editQuery.getAllowed();
                if (!bPassed && OAString.isEmpty(editQuery.getResponse())) {
                    String s = "Not visible, edit query for "+oaObj.getClass().getSimpleName()+"." + propertyName + " allowVisible returned false";
                    editQuery.setResponse(s);
                }
            }
        }
        else if (editQuery.getType() == Type.AllowEnabled || editQuery.getType().checkEnabledFirst) {
            pp = oi.getEnabledProperty();
            if (bPassed && OAString.isNotEmpty(pp)) {
                b = oi.getEnabledValue();
                valx = OAObjectReflectDelegate.getProperty(oaObj, pp);
                bPassed = (b == OAConv.toBoolean(valx));
                if (!bPassed) {
                    editQuery.setAllowed(false);
                    String s = "Not enabled, rule for "+oaObj.getClass().getSimpleName()+", "+pp+" must be "+b;
                    editQuery.setResponse(s);
                }
            }
            pp = oi.getUserEnabledProperty();
            if (bPassed && OAString.isNotEmpty(pp)) {
                b = oi.getUserEnabledValue();
                OAObject user = OAAuthDelegate.getUser();
                if (user == null) bPassed = false;
                else {
                    valx = OAObjectReflectDelegate.getProperty(user, pp);
                    bPassed = (b == OAConv.toBoolean(valx));
                }
                if (!bPassed) {
                    editQuery.setAllowed(false);
                    String s = "Not enabled, user rule for "+oaObj.getClass().getSimpleName()+", ";
                    if (user == null) s = "OAAuthDelegate.getUser returned null";
                    else s = "User."+pp+" must be "+b;
                    editQuery.setResponse(s);
                }
            }
            
            // this can overwrite editQuery.allowed        
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            editQueryX.setName(editQuery.getName());
            callEditQuery(oaObj, null, editQueryX);
            bPassed = editQueryX.getAllowed();
            editQuery.setAllowed(bPassed);
            if (!bPassed && OAString.isEmpty(editQuery.getResponse())) {
                String s = "Not enabled, edit query for "+oaObj.getClass().getSimpleName()+" allowEnabled returned false";
                editQuery.setResponse(s);
            }

            if (li != null && bPassed) {
                pp = li.getEnabledProperty();
                if (OAString.isNotEmpty(pp)) {
                    b = li.getEnabledValue();
                    valx = OAObjectReflectDelegate.getProperty(oaObj, pp);
                    bPassed = (b == OAConv.toBoolean(valx));
                    if (!bPassed) {
                        editQuery.setAllowed(false);
                        String s = "Not enabled, rule for "+oaObj.getClass().getSimpleName()+"."+propertyName+", "+pp+" must be "+b;
                        editQuery.setResponse(s);
                    }
                }
            }
            
            if (li != null && bPassed) {
                pp = li.getUserEnabledProperty();
                if (OAString.isNotEmpty(pp)) {
                    b = li.getUserEnabledValue();
                    OAObject user = OAAuthDelegate.getUser();
                    if (user == null) bPassed = false;
                    else {
                        valx = OAObjectReflectDelegate.getProperty(user, pp);
                        bPassed = (b == OAConv.toBoolean(valx));
                    }
                    if (!bPassed) {
                        editQuery.setAllowed(false);
                        String s = "Not enabled, user rule for "+oaObj.getClass().getSimpleName()+"."+propertyName+", ";
                        if (user == null) s = "OAAuthDelegate.getUser returned null";
                        else s = "User."+pp+" must be "+b;
                        editQuery.setResponse(s);
                    }
                }
            }
            
            // this can overwrite editQuery.allowed        
            if (li != null && OAString.isNotEmpty(propertyName)) {
                editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
                editQueryX.setAllowed(editQuery.getAllowed());
                editQueryX.setName(editQuery.getName());
                callEditQuery(oaObj, propertyName, editQueryX);
                bPassed = editQueryX.getAllowed();
                editQuery.setAllowed(bPassed);
                if (!bPassed && OAString.isEmpty(editQuery.getResponse())) {
                    String s = "Not enabled, edit query for "+oaObj.getClass().getSimpleName()+"." + propertyName + " allowEnabled returned false";
                    editQuery.setResponse(s);
                }
            }
        }
    }
    
    // called directly if hub.masterObject=null
    protected static void processEditQueryForHubListeners(OAObjectEditQuery editQuery, final Hub hub, final OAObject oaObj, final String propertyName, final Object oldValue, final Object newValue) {
        if (editQuery.getType().checkEnabledFirst) {
            OAObjectEditQuery editQueryX = new OAObjectEditQuery(Type.AllowEnabled);
            editQueryX.setAllowed(editQuery.getAllowed());
            editQueryX.setName(editQuery.getName());
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
        
        if (propertyName == null) propertyName = "";  // blank will be class onEditQuery(..)
        
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
     * Used by OAObjectModel objects to allow model object to be updated after it is created by calling EditQuery method.
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
    

    /**
     * Used by HubChangedListener.addXxx to listen to dependencies found for an EditQuery.
     */
    public static void addEditQueryChangeListeners(final Hub hub, final Class cz, final String prop, String ppPrefix, final HubChangeListener changeListener, final boolean bEnabled) {
        if (ppPrefix == null) ppPrefix = "";
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(cz);
        String s;
        
        if (bEnabled) s = oi.getEnabledProperty();
        else s = oi.getVisibleProperty(); 
        if (OAString.isNotEmpty(s)) changeListener.add(hub, ppPrefix+s);

        // dependent properties
        addDependentProps(hub, ppPrefix, 
            bEnabled ? null : oi.getViewDependentProperties(), 
            bEnabled ? oi.getUserDependentProperties() : null, 
            (OAString.isEmpty(prop) && oi.getProcessed()),
            changeListener
        );
        
        final Hub hubUser = OAAuthDelegate.getUserHub();
        if (bEnabled) s = oi.getUserEnabledProperty();
        else s = oi.getUserVisibleProperty();
        if (OAString.isNotEmpty(s)) changeListener.add(hubUser, s);
        
        if (OAString.isEmpty(prop)) return;
        
        OAPropertyInfo pi = oi.getPropertyInfo(prop);
        if (pi != null) {
            if (bEnabled) s = pi.getEnabledProperty();
            else s = pi.getVisibleProperty();
            if (OAString.isNotEmpty(s)) changeListener.add(hub, ppPrefix+s);
            addDependentProps(hub, ppPrefix, pi.getViewDependentProperties(), pi.getUserDependentProperties(), (bEnabled && pi.getProcessed()), changeListener);
            
            if (bEnabled) s = pi.getUserEnabledProperty();
            else s = pi.getUserVisibleProperty();
            if (OAString.isNotEmpty(s)) changeListener.add(hubUser, s);
        }
        else {
            OALinkInfo li = oi.getLinkInfo(prop);
            if (li != null) {
                if (bEnabled) s = li.getEnabledProperty();
                else s = li.getVisibleProperty();
                if (OAString.isNotEmpty(s)) changeListener.add(hub, ppPrefix+s);
                addDependentProps(hub, ppPrefix, li.getViewDependentProperties(), li.getUserDependentProperties(), (bEnabled && li.getProcessed()), changeListener);

                if (bEnabled) s = li.getUserEnabledProperty();
                else s = li.getUserVisibleProperty();
                if (OAString.isNotEmpty(s)) changeListener.add(hubUser, s);
            }
            else {
                OACalcInfo ci = oi.getCalcInfo(prop);
                if (ci != null) {
                    if (bEnabled) s = ci.getEnabledProperty();
                    else s = ci.getVisibleProperty();
                    if (OAString.isNotEmpty(s)) changeListener.add(hub, ppPrefix+s);
                    addDependentProps(hub, ppPrefix, ci.getViewDependentProperties(), ci.getUserDependentProperties(), false, changeListener);
                    
                    if (bEnabled) s = ci.getUserEnabledProperty();
                    else s = ci.getUserVisibleProperty();
                    if (OAString.isNotEmpty(s)) changeListener.add(hubUser, s);
                }
                else {
                    OAMethodInfo mi = oi.getMethodInfo(prop);
                    if (mi != null) {
                        if (bEnabled) s = mi.getEnabledProperty();
                        else s = mi.getVisibleProperty(); 
                        if (OAString.isNotEmpty(s)) changeListener.add(hub, ppPrefix+s);
                        addDependentProps(hub, ppPrefix, mi.getViewDependentProperties(), mi.getUserDependentProperties(), false, changeListener);
                        
                        if (bEnabled) s = mi.getUserEnabledProperty();
                        else s = mi.getUserVisibleProperty();
                        if (OAString.isNotEmpty(s)) changeListener.add(hubUser, s);
                    }
                }
            }
        }
    }
    
    protected static void addDependentProps(Hub hub, String prefix,  String[] viewDependentProperties, String[] userDependentProperties, boolean bProcessed, HubChangeListener changeListener) {
        if (viewDependentProperties != null) {
            for (String s : viewDependentProperties) {
                changeListener.add(hub, prefix+s);
            }
        }
        if (userDependentProperties != null) {
            Hub hubUser = OAAuthDelegate.getUserHub();
            if (userDependentProperties.length > 0 && hubUser == null) {
                changeListener.addAlwaysFalse(hub);
            }
            for (String s : userDependentProperties) {
                changeListener.add(hubUser, s);
            }
        }
        if (bProcessed) {
            Hub hubUser = OAAuthDelegate.getUserHub();
            if (hubUser == null) {
                changeListener.addAlwaysFalse(hub);
            }
            changeListener.add(hubUser, OAAuthDelegate.getAllowEditProcessedPropertyPath());
        }
    }
}
