package com.viaoa.hub;

import java.lang.reflect.Method;

import com.viaoa.object.OAObjectEditQuery;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/**
 * Validation listener used to validate an OAObject.Hub property, that will be first called by Hub.beforeX events.
 * @author vvia
 *
 */
public class OAObjectEditQueryHubListener extends HubListenerAdapter {
    final OAObject oaObj;
    final Method methodValidate;
    
    public OAObjectEditQueryHubListener(OAObject oaObj, Method method) {
        this.oaObj = oaObj;
        this.methodValidate = method;
    }
    
    @Override
    public void beforeAdd(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowAdd);
        em.setAllowAdd(true);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (!em.getAllowAdd() || t != null) {
            String s = "Allow Add EditQuery returned false";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void beforeInsert(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowInsert);
        em.setAllowInsert(true);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (!em.getAllowInsert() || t != null) {
            String s = "Allow Insert EditQuery returned false";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void beforeDelete(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowDelete);
        em.setAllowDelete(true);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (!em.getAllowDelete() || t != null) {
            String s = "Allow Delete EditQuery returned false";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void beforeRemove(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemove);
        em.setAllowRemove(true);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (!em.getAllowRemove() || t != null) {
            String s = "Allow Remove EditQuery returned false";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void beforeRemoveAll(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemoveAll);
        em.setAllowRemoveAll(true);
        call(em);
        Throwable t = em.getThrowable();
        if (!em.getAllowRemoveAll() || t != null) {
            String s = "Allow RemoveAll EditQuery returned false";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
//qqqqqqqqqqqqqqqqqqqqqqq
    
    @Override
    public void afterAdd(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnAdd);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (t != null) {
            String s = "Add EditQuery failed";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void afterInsert(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.OnInsert);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (t != null) {
            String s = "Insert EditQuery failed";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void afterDelete(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowDelete);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (t != null) {
            String s = "Delete EditQuery faile";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void afterRemove(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemove);
        em.setValue(e.getObject());
        call(em);
        Throwable t = em.getThrowable();
        if (t != null) {
            String s = "Insert EditQuery failed";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    @Override
    public void afterRemoveAll(HubEvent e) {
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowRemoveAll);
        call(em);
        Throwable t = em.getThrowable();
        if (t != null) {
            String s = "Insert EditQuery failed";
            s = OAString.concat(s, em.getResponse());
            if (t != null) s = OAString.concat(s, t.getMessage(), ", ");
            throw new RuntimeException(s, t);
        }
    }
    
    
    void call(OAObjectEditQuery em) {
        if (em == null || oaObj == null || methodValidate == null) return;
        try {
            methodValidate.invoke(oaObj, new Object[] {em});
        }
        catch (Throwable t) {
            em.setThrowable(t);
        }
    }
}
