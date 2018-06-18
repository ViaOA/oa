package com.viaoa.hub;

import java.lang.reflect.Method;

import com.viaoa.object.OAEditMessage;
import com.viaoa.object.OAEditMessage.Type;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/**
 * Validation listener used to validate an OAObject.Hub property, that will be first called by Hub.beforeX events.
 * @author vvia
 *
 */
public class HubObjectValidateListener extends HubListenerAdapter {
    final OAObject oaObj;
    final Method methodValidate;
    
    public HubObjectValidateListener(OAObject oaObj, Method method) {
        this.oaObj = oaObj;
        this.methodValidate = method;
    }
    @Override
    public void beforeAdd(HubEvent e) {
        validate(e, Type.Add);
    }
    @Override
    public void beforeInsert(HubEvent e) {
        validate(e, Type.Insert);
    }
    @Override
    public void beforeDelete(HubEvent e) {
        validate(e, Type.Delete);
    }
    @Override
    public void beforeRemove(HubEvent e) {
        validate(e, Type.Remove);
    }
    @Override
    public void beforeRemoveAll(HubEvent e) {
        validate(e, Type.RemoveAll);
    }

    void validate(HubEvent e, OAEditMessage.Type type) {
        if (e == null || type == null || oaObj == null || methodValidate == null) return;
        OAEditMessage em = new OAEditMessage(type);
        
        boolean b = false;
        try {
            Object objx = methodValidate.invoke(oaObj, new Object[] {e.getObject(), em});
            b = (objx instanceof Boolean) && ((Boolean) objx).booleanValue();
        }
        catch (Exception ex) {
            em.setMessage("Exception: " + ex.getMessage());
            b = false;
        }
        
        if (!b) {
            String msg = em.getMessage();
            if (OAString.isEmpty(msg)) msg = "Invalid "+type;
            throw new RuntimeException(msg);  // so Hub will not allow it
        }
    }
}
