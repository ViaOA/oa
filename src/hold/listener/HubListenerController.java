package com.viaoa.hub.listener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
    Hub Listener used to manage changes to a calculated property.
    This is used when a listener is created for a calculated property.
*/
class HubListenerController implements java.io.Serializable {
    private static Logger LOG = Logger.getLogger(HubCalcPropertyListenerController.class.getName());
    
    private Hub hub;
    private OACalcInfo calcInfo;

    
    
    
    private ArrayList<HubListenerInfo> alPropInfo = new ArrayList<HubListenerInfo>(3); 
    class HubListenerInfo {
        HubListener hl;
        Hub hub;
        HubMerger hm;
        String prop;
    }


    public HubListenerController(Hub hub, OACalcInfo calcInfo) {
        this.hub = hub;
        this.calcInfo = calcInfo;
        if (calcInfo != null) setup();
    }

    protected void setup() {
        // listen to all dependent props
        for (String prop : calcInfo.getProperties()) {
            int pos = prop.lastIndexOf('.');
            if (pos < 0) {
                createHubListener(prop);
            }
            else {
                createHubMerger(prop);
            }
        }
    }
    private void createHubMerger(String propertyPath) {
        Method[] methods = OAReflect.getMethods(hub.getObjectClass(), propertyPath);
        if (methods == null || methods.length == 0) return;
        
        Method m = methods[methods.length-1];
        Class c = m.getReturnType();
        
        int pos = propertyPath.lastIndexOf('.');
        String property = propertyPath.substring(pos+1);
        
        if (OAObject.class.isAssignableFrom(c)) {
            property = null;
        }
        else if (Hub.class.isAssignableFrom(c)) {
            property = null;
        }
        else {
            if (methods.length < 2) return;
            m = methods[methods.length-2];
            c = m.getReturnType();
            propertyPath = propertyPath.substring(0, pos);
        }
        
        Hub h = null;
        HubMerger hm = null;
        
        // see if a HubMerger already exists
        for (HubListenerInfo hli : alPropInfo) {
            if (hli.hm != null && hli.hm.getPath().equalsIgnoreCase(propertyPath)) {
                h = hli.hub;
                hm = hli.hm;
            }
        }
        
        if (h == null) {
            h = new Hub(c);
            hm = new HubMerger(hub, h, propertyPath, false);
        }
        else {
            if (property == null) return;  // HM already created, and no property to listen to.
        }
        
        final HubListenerInfo hli = new HubListenerInfo();
        hli.prop = property;

        hli.hl = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (hli.prop == null) return;
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (prop.equalsIgnoreCase(hli.prop)) {
                    HubEventDelegate.fireCalcPropertyChange(hub, hub.getAO(), calcInfo.getName());
                }                
            }
            @Override
            public void afterAdd(HubEvent e) {
                onEvent();
            }
            @Override
            public void afterRemove(HubEvent e) {
                onEvent();
            }
            @Override
            public void afterInsert(HubEvent e) {
                onEvent();
            }
            private void onEvent() {
                if (hli.prop != null) return;
                HubEventDelegate.fireCalcPropertyChange(hub, hub.getAO(), calcInfo.getName());
            }
        };
        
        hli.hub = h;
        hli.hm = hm;
        h.addHubListener(hli.hl, property);
        alPropInfo.add(hli);
        
    }
    
    private HubListenerInfo createHubListener(String calcDependentPropName) {
        final HubListenerInfo hli = new HubListenerInfo();
         
        hli.hl = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (prop.equalsIgnoreCase(hli.prop)) {
                    HubEventDelegate.fireCalcPropertyChange(hub, hub.getAO(), calcInfo.getName());
                }                
            }
        };
        hli.hub = hub;
        hli.prop = calcDependentPropName;
        hub.addHubListener(hli.hl, calcDependentPropName);
        alPropInfo.add(hli);
        return hli;
    }
    
    
    protected void close() {
        for (HubListenerInfo hli : alPropInfo) {
            hli.hub.removeHubListener(hli.hl);
            if (hli.hm != null) hli.hm.close();
        }
    }
    
    
}
