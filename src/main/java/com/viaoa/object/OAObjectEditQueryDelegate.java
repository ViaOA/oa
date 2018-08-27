package com.viaoa.object;

import java.lang.reflect.Method;

import javax.swing.JLabel;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.OAObjectEditQueryHubListener;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.util.OAString;

/**
 * used to call OAObject.onEdit[Property](Object value, OAEditMessage em) 
 * @author vvia
 */
public class OAObjectEditQueryDelegate {

    public static boolean getAllowChange(OAObject obj, String name) {
        return isAllowed(getAllowChangeEditQuery(obj, name));
    }
    public static boolean getAllowChange(OAObject obj, String name, boolean defaultValue) {
        return isAllowed(getAllowChangeEditQuery(obj, name, defaultValue));
    }
    public static boolean getOnChange(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnChangeEditQuery(obj, name, newValue));
    }
    public static boolean getAllowVisible(OAObject obj, String name) {
        return isAllowed(getAllowChangeEditQuery(obj, name));
    }
    public static boolean getAllowAdd(OAObject obj, String name) {
        return isAllowed(getAllowAddEditQuery(obj, name));
    }
    public static boolean getOnAdd(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnAddEditQuery(obj, name, newValue));
    }
    public static boolean getAllowInsert(OAObject obj, String name) {
        return isAllowed(getAllowInsertEditQuery(obj, name));
    }
    public static boolean getOnInsert(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnInsertEditQuery(obj, name, newValue));
    }
    public static boolean getAllowRemove(OAObject obj, String name) {
        return isAllowed(getAllowRemoveEditQuery(obj, name));
    }
    public static boolean getOnRemove(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnRemoveEditQuery(obj, name, newValue));
    }
    public static boolean getAllowRemoveAll(OAObject obj, String name) {
        return isAllowed(getAllowRemoveAllEditQuery(obj, name));
    }
    public static boolean getOnRemoveAll(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnRemoveAllEditQuery(obj, name, newValue));
    }
    public static boolean getAllowDelete(OAObject obj, String name) {
        return isAllowed(getAllowDeleteEditQuery(obj, name));
    }
    public static boolean getOnDelete(OAObject obj, String name, Object newValue) {
        return isAllowed(getOnDeleteEditQuery(obj, name, newValue));
    }

    public static String getFormat(OAObject obj, String name, String defaultFormat) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.GetFormat);
        em.setName(name);
        em.setFormat(defaultFormat);
        performEditQuery(obj, em);
        return em.getFormat();
    }
    public static String getToolTip(OAObject obj, String name, String defaultToolTip) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.GetToolTip);
        em.setName(name);
        em.setToolTip(defaultToolTip);
        performEditQuery(obj, em);
        return em.getToolTip();
    }
    public static void RenderLabel(OAObject obj, String name, JLabel label) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.RenderLabel);
        em.setName(name);
        performEditQuery(obj, em);
    }
    
    
    public static OAObjectEditQuery getAllowChangeEditQuery(OAObject obj, String name) {
        return getAllowChangeEditQuery(obj, name, true);
    }
    public static OAObjectEditQuery getAllowChangeEditQuery(OAObject obj, String name, boolean defaultValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowChange);
        em.setAllowChange(defaultValue);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnChangeEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnChange);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowVisibleEditQuery(OAObject obj, String name) {
        return getAllowVisibleEditQuery(obj, name, true);
    }
    public static OAObjectEditQuery getAllowVisibleEditQuery(OAObject obj, String name, boolean defaultValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowVisible);
        em.setAllowVisible(defaultValue);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowAddEditQuery(OAObject obj, String name) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowAdd);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnAddEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnAdd);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowInsertEditQuery(OAObject obj, String name) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowInsert);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnInsertEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnInsert);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowRemoveEditQuery(OAObject obj, String name) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemove);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnRemoveEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnRemove);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowRemoveAllEditQuery(OAObject obj, String name) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemoveAll);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnRemoveAllEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnRemoveAll);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getAllowDeleteEditQuery(OAObject obj, String name) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowDelete);
        em.setName(name);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnDeleteEditQuery(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnDelete);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnConfirm(OAObject obj, String name, Object newValue) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnConfirm);
        em.setName(name);
        em.setValue(newValue);
        performEditQuery(obj, em);
        return em;
    }
    public static OAObjectEditQuery getOnConfirm(OAObject obj, String name, Object newValue, String defaultConfirmMessage, String defaultConfirmTitle) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnConfirm);
        em.setName(name);
        em.setValue(newValue);
        em.setConfirmMessage(defaultConfirmMessage);
        em.setConfirmTitle(defaultConfirmTitle);
        performEditQuery(obj, em);
        return em;
    }
    
    
    
    
    // get the return boolean from a "allow" or "on" query
    public static boolean isAllowed(OAObjectEditQuery em) {
        if (em == null) return false;
        if (em.getThrowable() != null) return false;
        
        switch (em.getType()) {
        case AllowChange:
        case OnChange:
            if (!em.getAllowChange()) return false;
            break;
        case AllowVisible:
            if (!em.getAllowVisible()) return false;
            break;
        case AllowAdd:
        case OnAdd:
            if (!em.getAllowAdd()) return false;
            break;
        case AllowInsert:
        case OnInsert:
            if (!em.getAllowInsert()) return false;
            break;
        case AllowRemove:
        case OnRemove:
            if (!em.getAllowRemove()) return false;
            break;
        case AllowRemoveAll:
        case OnRemoveAll:
            if (!em.getAllowRemoveAll()) return false;
            break;
        case AllowDelete:
        case OnDelete:
            if (!em.getAllowDelete()) return false;
            break;
        }
        return true;
    }
    
    
    /**
     * Allows interaction with OAObject property, using information in em
     * @see OAObject#onEdit(String, OAObjectEditQuery)
     */
    public static void performEditQuery(OAObject obj, OAObjectEditQuery em) {
        if (obj == null || em == null) return;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        
        // query object first
        Method m = OAObjectInfoDelegate.getMethod(oi, "query", 1);
        if (m != null) {
            Class[] cs = m.getParameterTypes();
            if (cs[0].equals(OAObjectEditQuery.class)) {
                try {
                    Object objx = m.invoke(obj, new Object[] {em});
                }
                catch (Exception e) {
                    if (em != null) em.setThrowable(e);
                }
            }
        }

        // query property/method 
        String name = em.getName();
        if (OAString.isNotEmpty(name)) {
            m = OAObjectInfoDelegate.getMethod(oi, "query" + name, 1);
            if (m != null) {
                Class[] cs = m.getParameterTypes();
                if (cs[0].equals(OAObjectEditQuery.class)) {
                    try {
                        Object objx = m.invoke(obj, new Object[] {em});
                    }
                    catch (Exception e) {
                        if (em != null) em.setThrowable(e);
                    }
                }
            }
        }
    }

    
    public static boolean setupEditQueryHubListener(final OAObject oaObj, String property, Hub<?> hub) {
        if (oaObj == null || OAString.isEmpty(property) || hub == null) return false;
        if (hub.getMasterObject() == null) return false;
        
        // make sure hub does not already have validator listener
        if (HubEventDelegate.getOAObjectEditQueryHubListener(hub) != null) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        final Method methodValidate = OAObjectInfoDelegate.getMethod(oi, "onEdit" + property, 1);
        if (methodValidate == null) return false;
                
        Class[] cs = methodValidate.getParameterTypes();
        if (!cs[1].equals(OAObjectEditQuery.class)) return false;
        
        Class c = hub.getObjectClass();
        if (c == null || !cs[0].equals(c)) return false;
        
        // add hub listener
        hub.addHubListener(new OAObjectEditQueryHubListener(oaObj, methodValidate));
        return true;
    }
    
    
}
