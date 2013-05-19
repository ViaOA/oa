package com.viaoa.hub.listener;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.viaoa.annotation.OAMany;
import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.OAReflect;

public class HubListenerTree {

    Hub hub;
    String property;
    String listenProperty;
    String dependentName;
    HubMerger hubMerger;
    HubListener[] hubListeners;
    HubListenerTree[] children;
    
    public HubListenerTree(Hub thisHub, HubListener hl, String property) {
        this.hub = thisHub;

        int x = (hubListeners==null) ? 1 : hubListeners.length+1;
        HubListener[] hls = new HubListener[x];
        hls[x-1] = hl;
        
        createCalcs(thisHub, property);
    }

    private HubListenerTree() {
    }
    
    public void createCalcs(Hub thisHub, String property) {
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(thisHub.getObjectClass());
        for (OACalcInfo ci : oi.getCalcInfos()) {
            if (ci.getName().equalsIgnoreCase(property)) {
                setup(thisHub, ci);
            }
        }            
    }
    
    protected void setup(Hub hub, final OACalcInfo calcInfo) {
        Class c = hub.getObjectClass();
        
        String[] calcProps = calcInfo.getProperties();
        for (int i=0; calcProps != null && i < calcProps.length ; i++) {
            String pp = calcProps[i];
            if (pp == null) continue;
         
            HubListenerTree tree = this;
            for (;;) {
                int pos = pp.indexOf('.');
                String property;
                if (pos >= 0) property = pp.substring(0, pos);
                else property = pp;
                
                Method m = OAReflect.getMethod(c, property);
                if (m == null) {
                    //qqqqqqq error
                    break;
                }
                Class returnClass = m.getReturnType();
                Class hubClass;
                
                if (OAObject.class.isAssignableFrom(returnClass)) {
                    hubClass = returnClass;
                }
                else if (Hub.class.isAssignableFrom(returnClass)) {
                    OAMany om = m.getAnnotation(OAMany.class);
                    if (om != null) {
                        hubClass = om.toClass();
                    }
                    else {
                        //qqq find using OAObjectInfo.properties
                        break;
                    }
                }
                else {
                    hubClass = null;
                }
                if (hubClass != null) {
                    Hub h = new Hub(hubClass);
                    HubMerger hm = new HubMerger(hub, h, property, (i>0));
                    hub = h;
                    
                    hub.addHubListener(new HubListenerAdapter() {
                        //qqqqqqqq add,insert,remove events
                        // send fpc event using calcInfo.name
                    });
                }
                else {
                    hub.addHubListener(new HubListenerAdapter() {
                        //qqqqqqqq property Change events
                        // send fpc event using calcInfo.name
                    });
                }
                
                if (pos < 0) {
                    break;
                }
            }
            
            
        }
    }
    

    
    
    
}
