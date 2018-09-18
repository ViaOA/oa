/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.hub;

import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
 * Allows listening for changes to 1 or more Hubs and property paths.
 * Can include compare values, that can then be checked using getValue() to see if all conditions are true.  
 * 
 * @author vincevia
 */
public abstract class HubChangeListener {
    protected HubProp[] hubProps = new HubProp[0];
    public boolean DEBUG;
    
    /**
     * Specific types of comparsions.
     */
    public enum Type {
        Unknown,
        HubValid,
        HubNotValid,
        HubEmpty,
        HubNotEmpty,
        AoNull,  // hub.activeObject
        AoNotNull,
        AlwaysTrue,
        PropertyNull,
        PropertyNotNull
    }
    
    
    public static class HubProp {
        public Hub<?> hub;
        public String propertyPath;  // original propertyPath
        public String listenToPropertyName;  // name used for listener - in case property path has '.' in it, then this will replace with '_' 
        public HubListener hubListener;
        public Object compareValue;
        public boolean bUseCompareValue;
        public OAFilter filter;

        public HubProp(Hub<?> h, String propertyPath, String listenPropertyName, boolean bUseCompareValue, Object compareValue, OAFilter filter) {
            this.hub = h;
            this.propertyPath = propertyPath;
            this.listenToPropertyName = listenPropertyName;
            this.bUseCompareValue = bUseCompareValue;
            this.compareValue = compareValue;
            this.filter = filter;
        }

        public boolean getValue() {
            if (hub == null) return true;
            
            if (bUseCompareValue && compareValue != null) {
                if (compareValue == Type.AlwaysTrue) return true;
                if (compareValue == Type.HubValid) return hub.isValid();
                if (compareValue == Type.HubNotValid) return !hub.isValid();
                if (compareValue == Type.HubEmpty) return (hub.getSize() == 0);
                if (compareValue == Type.HubNotEmpty) return (hub.getSize() > 0);
                if (compareValue == Type.AoNull) return (hub.getAO() == null);
                if (compareValue == Type.AoNotNull) return (hub.getAO() != null);
            }

            if (!hub.isValid()) return false;
            
            Object value = hub.getAO();
            if (propertyPath != null) {
                if (value instanceof OAObject) value = ((OAObject)value).getProperty(propertyPath);
            }
            
            if (bUseCompareValue && compareValue != null) {
                if (compareValue == Type.PropertyNull) return (value == null);
                if (compareValue == Type.PropertyNotNull) return (value != null);
            }
            
            if (bUseCompareValue) {
                return OACompare.compare(compareValue, value) == 0;
            }
            else {
                return OAConv.toBoolean(value);
            }
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
        @Override
        public int hashCode() {
            return hub.hashCode();
        }
    }
    
    public HubChangeListener() {
    }    
    public HubChangeListener(Hub hub) {
        add(hub);
    }
    public HubChangeListener(Hub hub, String propertyName) {
        add(hub, propertyName);
    }
    public HubChangeListener(Hub hub, String propertyName, Object compareValue) {
        add(hub, propertyName, compareValue);
    }

    public HubChangeListener(Hub hub, HubChangeListener.Type type) {
        add(hub, type);
    }

    /**
     * Add an additional hub to base the check on.  
     * Since there is no propertyName, then it will be based on AoNotNull.
     */
    public HubProp add(Hub hub) {
        return add(hub, null, Type.AoNotNull);
    }    

    /**
     * adds property without any check.  This is good for adding dependendent properties
     * @see #addPropertyNotNull(Hub, String)
     * @see #addPropertyNull(Hub, String)
     * @see #add(Hub, String, Object)
     */
    public HubProp add(Hub hub, String propertyPath) {
        if (propertyPath == null) return add(hub);
        else {
            return add(hub, propertyPath, true, Type.AlwaysTrue, null, true);
        }
    }

    
    /**  Checks to see if hub.isValid */
    public HubProp addHubValid(Hub hub) {
        return add(hub, null, true, Type.HubValid, null, true);
    }
    public HubProp addHubNotValid(Hub hub) {
        return add(hub, null, true, Type.HubNotValid, null, true);
    }
    /**  Checks to see if hub.size = 0 */
    public HubProp addHubEmpty(Hub hub) {
        return add(hub, null, true, Type.HubEmpty, null, true);
    }
    public HubProp addHubNotEmpty(Hub hub) {
        return add(hub, null, true, Type.HubNotEmpty, null, true);
    }
    /**  Checks to see if hub.AO = null */
    public HubProp addAoNull(Hub hub) {
        return add(hub, null, true, Type.AoNull, null, true);
    }
    public HubProp addAoNotNull(Hub hub) {
        return add(hub, null, true, Type.AoNotNull, null, true);
    }
    
    public HubProp addAlwaysTrue(Hub hub) {
        return add(hub, null, true, Type.AlwaysTrue, null, true);
    }
    
    public HubProp addPropertyNull(Hub hub, String prop) {
        return add(hub, prop, true, Type.PropertyNull, null, true);
    }
    public HubProp addPropertyNotNull(Hub hub, String prop) {
        return add(hub, prop, true, Type.PropertyNotNull, null, true);
    }
    
    public HubProp add(Hub hub, HubChangeListener.Type type) {
        return add(hub, null, type==null?false:true, type, null, true);
    }
    public HubProp add(Hub hub, String property, HubChangeListener.Type type) {
        return add(hub, property, type==null?false:true, type, null, true);
    }
    
    
    /**
     * Add an addition hub/property to base the check on.
     * @param compareValue can be null, OANullObject.instance, OANotNullObject.instance, OAAnyValueObject.instance, Type.PropertyNull, Type.PropertyNotNull
     *      or any other value.  
     *      Note: OAAnyValueObject is used so that hub.isValid is the only check that is needed.
     */
    public HubProp add(Hub hub, final String propertyPath, Object compareValue) {
        return add(hub, propertyPath, true, compareValue, null, true);
    }

    public HubProp add(Hub hub, OAFilter filter) {
        return add(hub, null, true, null, filter, true);
    }
    
        
    public HubProp add(Hub hub, final String propertyPath, boolean bUseCompareValue, Object compareValue, OAFilter filter, final boolean bAoOnly) {
        if (hub == null) return null;

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

        final HubProp newHubProp = new HubProp(hub, propertyPath, newPropertyPath, bUseCompareValue, compareValue, filter);
        
        // see if there is a listener with same hub - and one without a propertyName used
        for (HubProp hp : hubProps) {
            if (hp.equals(newHubProp)) return null;
        }

        newHubProp.hubListener = new HubListenerAdapter() {
            public void afterChangeActiveObject(HubEvent e) {
                onChange();
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s != null && s.equalsIgnoreCase(newHubProp.listenToPropertyName)) {
                    onChange();
                }
            }
            // linked to hub listener
            @Override
            public void onNewList(HubEvent e) {
                if (newHubProp.listenToPropertyName == null) {
                    onChange();
                }
            }
            @Override
            public void afterAdd(HubEvent e) {
                if (propertyPath == null) onChange();
            }
            @Override
            public void afterInsert(HubEvent e) {
                if (propertyPath == null) onChange();
            }
            @Override
            public void afterRemove(HubEvent e) {
                if (propertyPath == null) onChange();
            }
        };
        
        if (props == null) {
            if (propertyPath == null) hub.addHubListener(newHubProp.hubListener);
            else hub.addHubListener(newHubProp.hubListener, newPropertyPath, bAoOnly);
        }
        else {
            hub.addHubListener(newHubProp.hubListener, newPropertyPath, props, bAoOnly);
        }
        
        hubProps = (HubProp[]) OAArray.add(HubProp.class, hubProps, newHubProp);
        onChange();

        Hub h = hub.getLinkHub();
        if (h != null) {
            if (HubLinkDelegate.isLinkAutoCreated(hub, true)) {
                // need to listen for AO changes, newList, etc from the linkTo Hub
                add(h, null, OAAnyValueObject.instance);
            }
            else addHubValid(h);
        }
        return newHubProp;
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
    
    public void remove(Hub hub) {
        remove(hub, null);
    }
    public void remove(Hub hub, String prop) {
        for (HubProp hp : hubProps) {
            if (hp.hub == hub) {
                if (OAString.equals(prop, hp.propertyPath)) {
                    if (hp.hubListener != null) hp.hub.removeHubListener(hp.hubListener);
                }
            }
        }
    }
    public void remove(HubProp hp) {
        if (hp != null && hp.hubListener != null) {
            hp.hub.removeHubListener(hp.hubListener);
            hubProps = (HubProp[]) OAArray.removeValue(HubProp.class, hubProps, hp);
        }
    }
    
    
    /**
     * Checks all of the compare values that are being listened to.  All must be true to return true, else returns false.
     */
    public boolean getValue() {
        boolean b = true;
        for (HubProp hp : hubProps) {
            if (hp.filter != null) {
                if (hp.hub == null) b = hp.filter.isUsed(null);
                else b = hp.filter.isUsed(hp.hub.getAO());
            }
            else b = hp.getValue();
            if (!b) break;
        }
        return b;
    }

    protected abstract void onChange();
}
