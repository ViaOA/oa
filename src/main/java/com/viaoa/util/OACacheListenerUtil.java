package com.viaoa.util;

import java.util.ArrayList;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectCacheListener;

/**
 * Helper to find thread+stacktrace when a class.property is changed.
 * @author vvia
 *
 */
public class OACacheListenerUtil {

    private final Class clazz;
    private final String property;
    private OAObjectCacheListener listener;
    
    
    public OACacheListenerUtil(Class clazz, String property) {
        this.clazz = clazz;
        this.property = property;
        init();
    }
    
    protected void init() {
        if (listener != null) return;
        listener = new OAObjectCacheListener() {
            @Override
            public void afterPropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
                if (!property.equalsIgnoreCase(propertyName)) return;

                Thread t = Thread.currentThread();
                StringBuilder sb = new StringBuilder(2048);
                String s = (new OADateTime()) + ", Thread="+t.getName();
                sb.append(s + "\n");
                
                StackTraceElement[] stes = t.getStackTrace();
                if (stes != null) {
                    for (StackTraceElement ste : stes) {
                        sb.append(ste.toString());
                        sb.append("\n");
                    }
                }
                String sx = sb.toString();
                OACacheListenerUtil.this.onEvent(obj, propertyName, oldValue, newValue, sx);
            }
            @Override
            public void afterAdd(OAObject obj) {
            }
            @Override
            public void afterAdd(Hub hub, OAObject obj) {
            }
            @Override
            public void afterRemove(Hub hub, OAObject obj) {
            }
            @Override
            public void afterLoad(OAObject obj) {
            }
        };
        OAObjectCacheDelegate.addListener(clazz, listener);
    }
    
    public void close() {
        OAObjectCacheDelegate.removeListener(clazz, listener);
        listener = null;
    }

    /**
     * called when the property is changed.
     * @param stackTrace from current thread
     */
    public void onEvent(OAObject obj, String propertyName, Object oldValue, Object newValue, String stackTrace) {
    }
}

