package com.viaoa.auth;

import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * System wide service for getting the current User object.
 * @author vvia
 */
public class OAAuthDelegate {
    private static final ConcurrentHashMap<Object, Hub<? extends OAObject>> hmUserHub = new ConcurrentHashMap<>();
    private static final Object NullContext = new Object();
    
    private static String allowEditProcessedPropertyPath = "EditProcessed"; 
    

    /**
     * Property path used to find the user property for allowing users to edit objects/properties/etc that are annotatied as processed.
     * Defaults to "EditProcessed"
     */
    public static void setAllowEditProcessedPropertyPath(String pp) {
        OAAuthDelegate.allowEditProcessedPropertyPath = pp;
    }
    public static String getAllowEditProcessedPropertyPath() {
        return OAAuthDelegate.allowEditProcessedPropertyPath;
    }
    public static boolean canUserEditProcessed(OAObject user) {
        if (user == null) return false;
        if (OAString.isEmpty(allowEditProcessedPropertyPath)) return false;
        Object val = user.getProperty(OAAuthDelegate.allowEditProcessedPropertyPath);
        boolean b = OAConv.toBoolean(val);
        return b;
    }

    public static void addUserHub(Object context, Hub<? extends OAObject> hub) {
        if (hub == null) return;
        if (context == null) context = NullContext;
        hmUserHub.put(context, hub);
    }
    public static void removeUserHub(Object context) {
        if (context == null) context = NullContext;
        hmUserHub.remove(context);
    }

    public static Hub<? extends OAObject> getUserHub() {
        return getUserHub(null);
    }
    public static Hub<? extends OAObject> getUserHub(Object context) {
        if (context == null) {
            //todo: qqqqqq look in threadLocal            
        }
        if (context == null) context = NullContext;
        return hmUserHub.get(context); 
    }

    public static OAObject getUser() {
        return getUser(null);
    }
    public static OAObject getUser(Object context) {
        Hub<? extends OAObject> hub = getUserHub(context);
        if (hub == null) return null;
        return hub.getAO();
    }

    /**
     * Used to know if the logged in user can edit processed data.
     */
    public static boolean canEditProcessed() {
        OAObject user = getUser();
        if (user == null) return false;
        Object valx = OAObjectReflectDelegate.getProperty(user, getAllowEditProcessedPropertyPath());
        boolean bx = OAConv.toBoolean(valx);
        return bx;
    }

}
