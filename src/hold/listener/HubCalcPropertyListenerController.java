package com.viaoa.hub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.viaoa.object.OACalcInfo;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAReflect;

/**
	Hub Listener used to manage changes to a calculated property.
	This is used when a listener is created for a calculated property.
*/
class HubCalcPropertyListenerController extends HubListenerAdapter implements java.io.Serializable {
    private static Logger LOG = Logger.getLogger(HubCalcPropertyListenerController.class.getName());
	
    private HubListener hubListenerParent;
    private Hub hub;
	private OACalcInfo calcInfo;

	private ArrayList<PropInfo> alPropInfo = new ArrayList<PropInfo>(3); 
    class PropInfo {
        HubListener hl;
        Hub h;
        HubMerger hm;
        String dependentPropName;
        String hmPropertyPath;
    }
    
    
    public HubCalcPropertyListenerController(HubListener hubListenerParent, Hub hub, OACalcInfo calcInfo) {
        this.hubListenerParent = hubListenerParent;
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
        for (PropInfo pi : alPropInfo) {
            if (pi.hmPropertyPath.equalsIgnoreCase(propertyPath)) {
                h = pi.h;
                hm = pi.hm;
            }
        }
        
        if (h == null) {
            h = new Hub(c);
            hm = new HubMerger(hub, h, propertyPath, false);
        }
        else {
            if (property == null) return;  // HM already created, and no property to listen to.
        }
        
        final PropInfo pi = new PropInfo();
        pi.hmPropertyPath = propertyPath;
        pi.dependentPropName = property;

        pi.hl = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (pi.dependentPropName == null) return;
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (prop.equalsIgnoreCase(pi.dependentPropName)) {
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
                if (pi.dependentPropName != null) return;
                HubEventDelegate.fireCalcPropertyChange(hub, hub.getAO(), calcInfo.getName());
            }
        };
        
        pi.h = h;
        pi.hm = hm;
        h.addHubListener(pi.hl, property);
        alPropInfo.add(pi);
        
    }
    
    private PropInfo createHubListener(String calcDependentPropName) {
        final PropInfo pi = new PropInfo();
         
        pi.hl = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (prop.equalsIgnoreCase(pi.dependentPropName)) {
                    HubEventDelegate.fireCalcPropertyChange(hub, hub.getAO(), calcInfo.getName());
                }                
            }
        };
        pi.h = hub;
        pi.dependentPropName = calcDependentPropName;
        hub.addHubListener(pi.hl, calcDependentPropName);
        alPropInfo.add(pi);
        return pi;
    }
    
    
    protected void close() {
        for (PropInfo pi : alPropInfo) {
            pi.h.removeHubListener(pi.hl);
            if (pi.hm != null) pi.hm.close();
        }
    }
    
    
}
