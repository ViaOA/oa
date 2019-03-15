package com.viaoa.context;

import java.util.concurrent.ConcurrentHashMap;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

public class OAContext {
    private static final ConcurrentHashMap<Object, Hub<? extends OAObject>> hmContextHub = new ConcurrentHashMap<>();
    private static final Object NullContext = new Object();

    
    /**
     * Property path used to find the user property for allowing users to edit objects/properties/etc that are annotatied as processed.
     * Defaults to "EditProcessed"
     */
    private static String allowEditProcessedPropertyPath = "EditProcessed"; 
    public static void setAllowEditProcessedPropertyPath(String pp) {
        OAContext.allowEditProcessedPropertyPath = pp;
    }
    public static String getAllowEditProcessedPropertyPath() {
        return OAContext.allowEditProcessedPropertyPath;
    }
    public static boolean canEditProcessed() {
        return canEditProcessed(NullContext);
    }
    public static boolean canEditProcessed(Object context) {
        if (context == null) context = NullContext;
        if (OAString.isEmpty(allowEditProcessedPropertyPath)) return false;
        OAObject oaObj = getContext(context);
        if (oaObj == null) return false;
        
        Object val = oaObj.getProperty(OAContext.allowEditProcessedPropertyPath);
        boolean b = OAConv.toBoolean(val);
        return b;
    }
    

    private static String allowAdminEditPropertyPath = "EditProcessed"; 
    public static void setAllowAdminEditPropertyPath(String pp) {
        OAContext.allowAdminEditPropertyPath = pp;
    }
    public static String getAllowAdminEditPropertyPath() {
        return OAContext.allowAdminEditPropertyPath;
    }
    public static boolean canAdminEdit() {
        return canAdminEdit(NullContext);
    }
    public static boolean isAdmin() {
        return canAdminEdit(null);
    }
    public static boolean isAdmin(Object context) {
        return canAdminEdit(context);
    }
    public static boolean canAdminEdit(Object context) {
        if (context == null) context = NullContext;
        if (OAString.isEmpty(allowAdminEditPropertyPath)) return false;
        OAObject oaObj = getContext(context);
        if (oaObj == null) return false;
        
        Object val = oaObj.getProperty(OAContext.allowAdminEditPropertyPath);
        boolean b = OAConv.toBoolean(val);
        return b;
    }
    
    public static void setContext(Object context, OAObject obj) {
        if (obj == null) return;
        if (context == null) context = NullContext;
        Hub h = new Hub();
        h.add(obj);
        h.setAO(obj);
        addContextHub(context, h);
    }
    public static OAObject getContext() {
        return getContext(null);
    }
    public static OAObject getContext(Object context) {
        Hub<? extends OAObject> hub = getContextHub(context);
        if (hub == null) return null;
        return hub.getAO();
    }

    
    public static void addContextHub(Object context, Hub<? extends OAObject> hub) {
        if (hub == null) return;
        if (context == null) context = NullContext;
        if (hub.getAO() == null) hub.setPos(0);
        hmContextHub.put(context, hub);
    }
    public static void removeContextHub(Object context) {
        if (context == null) context = NullContext;
        hmContextHub.remove(context);
    }

    public static Hub<? extends OAObject> getContextHub() {
        return getContextHub(null);
    }
    public static Hub<? extends OAObject> getContextHub(Object context) {
        // if (context == null)  //todo: qqqqqq look in threadLocal            
        if (context == null) context = NullContext;
        return hmContextHub.get(context); 
    }

    
}
