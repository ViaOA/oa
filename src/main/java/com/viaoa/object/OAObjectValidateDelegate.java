package com.viaoa.object;

import java.lang.reflect.Method;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.hub.HubObjectValidateListener;
import com.viaoa.object.OAEditMessage.Type;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OAString;

/**
 * used to call OAObject.is[Property]Valid(Object newValue, OAEditMessage em) 
 * @author vvia
 */
public class OAObjectValidateDelegate {

    public static boolean isValid(OAObject obj, String property, Object newValue, OAEditMessage em) {
        if (obj == null || OAString.isEmpty(property)) return true;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        Method m = OAObjectInfoDelegate.getMethod(oi, "isValid" + property, 2);
        if (m == null) return true;
        
        Class[] cs = m.getParameterTypes();
        if (!cs[1].equals(OAEditMessage.class)) return true;
        
        Object param = OAConverter.convert(cs[0], newValue);
        
        boolean b = false;
        try {
            Object objx = m.invoke(obj, new Object[] {param, em});
            b = (objx instanceof Boolean) && ((Boolean) objx).booleanValue();
        }
        catch (Exception e) {
            if (em != null) em.setThrowable(e);
            b = false;
        }
        return b;
    }

    
    public static boolean setupHubValidator(final OAObject oaObj, String property, Hub<?> hub) {
        if (oaObj == null || OAString.isEmpty(property) || hub == null) return false;
        if (hub.getMasterObject() == null) return false;
        
        // make sure hub does not already have validator listener
        if (HubEventDelegate.getObjectValidateListener(hub) != null) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        final Method methodValidate = OAObjectInfoDelegate.getMethod(oi, "isValid" + property, 2);
        if (methodValidate == null) return false;
                
        Class[] cs = methodValidate.getParameterTypes();
        if (!cs[1].equals(OAEditMessage.class)) return false;
        
        Class c = hub.getObjectClass();
        if (c == null || !cs[0].equals(c)) return false;
        
        // add hub listener
        hub.addHubListener(new HubObjectValidateListener(oaObj, methodValidate));
        return true;
    }
    
    
}
