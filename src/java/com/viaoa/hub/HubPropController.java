package com.viaoa.hub;


import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
 * Base class that uses one or more Hub, Property, and compare value to update
 * another component.
 * 
 * @author vincevia
 */
public abstract class HubPropController {
    protected HubProp[] hubProps = new HubProp[0];
    protected boolean bDirectlySet;  // used to override the isValid with another value
    protected boolean bDirectlySetValue;
    protected boolean bIsCallingUpdate;  // used when updating value, so that direct setting valid can be ignored
    
    public static class HubProp {
        public Hub<?> hub;
        public String propertyPath;  // original propertyPath
        public String listenToPropertyName;  // name used for listener - in case property path has '.' in it, then this will replace with '_' 
        public HubListener hubListener;
        public Object compareValue;
        
        public HubProp(Hub<?> h, String propertyPath, String listenPropertyName, Object compareValue) {
            this.hub = h;
            this.propertyPath = propertyPath;
            this.listenToPropertyName = listenPropertyName;
            this.compareValue = compareValue;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HubProp)) return false;
            HubProp hp = (HubProp) obj;
            if (this.hub != hp.hub) return false;
            if (this.compareValue != null) {
                if (hp.compareValue == null) return false;
                if (!this.compareValue.equals(hp.compareValue)) {
                    if (!this.compareValue.equals(OAConv.convert(this.compareValue.getClass(), hp.compareValue))) return false;
                }
            }
            else if (hp.compareValue != null) return false;
            
            if (this.propertyPath != null) {
                if (hp.propertyPath == null) return false;
                if (!this.propertyPath.equalsIgnoreCase(hp.propertyPath)) return false;
            }
            else if (hp.propertyPath != null) return false;
            return true;
        }
    }
    public HubPropController() {
    }    
    public HubPropController(Hub hub) {
        add(hub);
    }
    public HubPropController(Hub hub, String propertyName) {
        add(hub, propertyName);
    }
    public HubPropController(Hub hub, String propertyName, Object compareValue) {
        add(hub, propertyName, compareValue);
    }

    /**
     * Add an addition hub to base the check on.  
     * Since there is no propertyName, then it will be based on AO.
     */
    public void add(Hub hub) {
        add(hub, null, OANotNullObject.instance);
    }    

    /**
     * If compare value is null: if propertyPath is null then uses OANotNullObject, else uses Boolean.true 
     * @param hub
     * @param propertyPath
     */
    public void add(Hub hub, String propertyPath) {
        if (propertyPath == null) add(hub);
        else add(hub, propertyPath, Boolean.TRUE);
    }
    
    /**
     * Add an addition hub/property to base the check on.
     * @param compareValue can be null, OANullObject.instance, OANotNullObject.instance, OAAnyValueObject.instance,
     *      or any other value.  
     *      Note: OAAnyValueObject is used so that hub.isValid is the only check that is needed.
     */
    public void add(Hub hub, String propertyPath, Object compareValue) {
        if (hub == null) return;

        String newPropertyPath;
        String[] props;
        
        if (propertyPath != null && propertyPath.indexOf('.') >= 0) {
            newPropertyPath = propertyPath.replace('.', '_');
            props = new String[] {propertyPath};
        }
        else {
            newPropertyPath = propertyPath;
            props = null;
        }

        final HubProp newHubProp = new HubProp(hub, propertyPath, newPropertyPath, compareValue);
        
        // see if there is a listener with same hub - and one without a propertyName used
        for (HubProp hp : hubProps) {
            if (hp.equals(newHubProp)) return;
        }

        HubListener hl = new HubListenerAdapter() {
            public void afterChangeActiveObject(HubEvent e) {
                update();
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s != null && s.equalsIgnoreCase(newHubProp.listenToPropertyName)) {
                    update();
                }
            }
            // linked to hub listener
            @Override
            public void onNewList(HubEvent e) {
                if (newHubProp.listenToPropertyName == null) {
                    update();
                }
            }
        };
        newHubProp.hubListener = hl;
        
        hub.addHubListener(hl, newPropertyPath, props);
        hubProps = (HubProp[]) OAArray.add(HubProp.class, hubProps, newHubProp);
        update();

        // 20110905 need to also listen to linked to hub (if any)
        Hub h = hub.getLinkHub();
        if (h != null) {
            if (hub.datau.bAutoCreate) {
                // need to listen for AO changes, newList, etc from the linkTo Hub
                add(h, null, OAAnyValueObject.instance);
            }
            else add(h);
        }
    }        
    
    public void clear() {
        close();
        hubProps = new HubProp[0];
    }
    
    public void close() {
        for (HubProp hp : hubProps) {
            if (hp.hubListener != null) hp.hub.removeHubListener(hp.hubListener);
        }
    }

    /**
     * Called after by update during a change that affects any of the hub/properties.
     * This can be overwritten to customize the value.
     * @return
     */
    public boolean isValid() {
        boolean b = _isValid();
        if (!bDirectlySet) b = isValid(b);
        return b;
    }
    protected boolean _isValid() {
        if (bDirectlySet) {
            return bDirectlySetValue;
        }
        
        boolean b = true;
        for (HubProp hp : hubProps) {

            boolean bValidHub = hp.hub.isValid();

            Object value = hp.hub.getAO();
            if (hp.propertyPath != null) {
                if (value instanceof OAObject) value = ((OAObject)value).getProperty(hp.propertyPath);
                else value = null;
            }

            if (!bValidHub) {
                b = (OANullObject.instance.equals(hp.compareValue));
            }
            else if (hp.compareValue == null) {
                b = (OANullObject.instance.equals(value));
            }
            else {
                Object objx = OAConv.convert(hp.compareValue.getClass(), value);
                if (objx == null) objx = value;
                b = hp.compareValue.equals(objx);
            }
            if (!b) break;
        }
        return b;
    }

    /**
     * This is called with the Enabled value, as a chance to overwrite the value.
     */
    protected boolean isValid(boolean bEnableValue) {
        return bEnableValue;
    }
    
    /**
     * Called when a change is made to Hub/Property.
     */
static int qqq;    
    public void update() {
qqq++;
//if (qqq % 50 == 0) System.out.printf("%,d%n", qqq);
       boolean b = isValid();
       bIsCallingUpdate = true;
       onUpdate(b);
       bIsCallingUpdate = false;
    }

    /**
     * This allows to set the enabled directly, and not use the Hub/propertie (isValid() method).
     */
    public void directlySet(boolean b, boolean bEnableValue) {
        if (!bIsCallingUpdate) {
            bDirectlySet = b;
            bDirectlySetValue = bEnableValue;
        }
    }
    
    
    protected abstract void onUpdate(boolean bValid);
}
