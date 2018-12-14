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

import java.util.ArrayList;

import com.viaoa.auth.OAAuthDelegate;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectEditQuery;
import com.viaoa.object.OAObjectEditQueryDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.util.*;

/**
 * Allows listening for changes to 1 or more Hubs and property paths.
 * Can include compare values, that can then be checked using getValue() to see if all conditions are true. 
 * Use add method to add as many checks and hubs as necessary. 
 * 
 * calling getValue will determine if all of the conditions are true.  ex: AO != null,  editQueryEnabled=true, propValue==X, etc.
 * 
 * @author vincevia
 */
public abstract class HubChangeListener {
    protected HubProp[] hubProps = new HubProp[0];
    public boolean DEBUG;
    private HubEvent lastHubEvent;
    private String failureReason;
    
    /**
     * Specific types of comparisions.
     */
    public enum Type {
        Unknown(true),
        HubValid(true),
        HubNotValid(true),
        HubEmpty(false),
        HubNotEmpty(false),
        AoNull(true),  // hub.activeObject
        AoNotNull(true),
        AlwaysTrue(true),
        AlwaysFalse(true),
        PropertyNull(true),
        PropertyNotNull(true),
        EditQueryEnabled(true),
        EditQueryVisible(true);
        
        public boolean bUseAoOnly;  // instead of the full hub
        Type(boolean b) {
            this.bUseAoOnly = b;
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
     */
    public HubProp add(Hub hub) {
        return add(hub, null, true, Type.HubValid, null, false, null);
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
            return add(hub, propertyPath, true, Type.AlwaysTrue, null, true, null);
        }
    }
    
    /**  Checks to see if hub.isValid */
    public HubProp addHubValid(Hub hub) {
        return add(hub, null, true, Type.HubValid);
    }
    public HubProp addHubValid(Hub hub, String propertyPath) {
        return add(hub, propertyPath, true, Type.HubValid);
    }
    public HubProp addHubNotValid(Hub hub) {
        return add(hub, null, true, Type.HubNotValid);
    }
    /**  Checks to see if hub.size = 0 */
    public HubProp addHubEmpty(Hub hub) {
        return add(hub, null, true, Type.HubEmpty);
    }
    public HubProp addHubNotEmpty(Hub hub) {
        return add(hub, null, true, Type.HubNotEmpty);
    }
    /**  Checks to see if hub.AO = null */
    public HubProp addAoNull(Hub hub) {
        return add(hub, null, true, Type.AoNull);
    }
    public HubProp addAoNotNull(Hub hub) {
        return add(hub, null, true, Type.AoNotNull);
    }
    
    public HubProp addAlwaysTrue(Hub hub) {
        return add(hub, null, true, Type.AlwaysTrue);
    }
    public HubProp addAlwaysTrue() {
        return add(null, null, true, Type.AlwaysTrue);
    }
    public HubProp addAlwaysFalse(Hub hub) {
        return add(hub, null, true, Type.AlwaysFalse);
    }
    public HubProp addAlwaysFalse() {
        return add(null, null, true, Type.AlwaysFalse);
    }
    
    public HubProp addPropertyNull(Hub hub, String prop) {
        return add(hub, prop, true, Type.PropertyNull);
    }
    public HubProp addPropertyNotNull(Hub hub, String prop) {
        return add(hub, prop, true, Type.PropertyNotNull);
    }

    public HubProp addPropertyChange(Hub hub, String prop) {
        return add(hub, prop);
    }
    
    public HubProp addAddEnabled(final Hub hub) {
        return addNewEnabled(hub);
    }
    public HubProp addNewEnabled(final Hub hub) {
        if (hub == null) return null;
        
        OAFilter filter = new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                OAObjectEditQuery eq = OAObjectEditQueryDelegate.getAllowAddEditQuery(hub, true);
                boolean b = eq.getAllowed();
                if (!b) failureReason = eq.getDisplayResponse();
                return b;
            }
        };
        HubProp hp = add(hub, null, false, null, filter, false, "EditQuery.AllowAdd");

        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, hub.getObjectClass(), null, null, this, true);

        Hub hx = hub.getMasterHub();
        if (hx != null) {
            add(hx, Type.AoNotNull);
            OAObjectEditQueryDelegate.addEditQueryChangeListeners(hx, hx.getObjectClass(), null, null, this, true);
        }
        return hp;
    }

    public HubProp addDeleteEnabled(final Hub hub) {
        if (hub == null) return null;
        
        OAFilter filter = new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                OAObjectEditQuery eq = (obj instanceof OAObject) ? OAObjectEditQueryDelegate.getAllowDeleteEditQuery(hub, (OAObject)obj, true) : null;
                boolean b = eq != null && eq.getAllowed();
                if (!b) {
                    failureReason = eq.getDisplayResponse();
                }
                return b;
            }
        };
        HubProp hp = add(hub, null, false, null, filter, false, "EditQuery.AllowDelete");

        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, hub.getObjectClass(), null, null, this, true);

        Hub hx = hub.getMasterHub();
        if (hx != null) {
            add(hx, Type.AoNotNull);
            OAObjectEditQueryDelegate.addEditQueryChangeListeners(hx, hx.getObjectClass(), null, null, this, true);
        }
        
        return hp;
    }

/* not used, OAObjectEditQueryDelegate._processEditQuery handles this    
    // uses ObjectInfo.isProcessed and OAAuth to determine if user has permission 
    public HubProp addProcessedEnabled(final Hub hub) {
        if (hub == null) return null;
        
        OAObjectInfo oi = hub.getOAObjectInfo();
        if (!oi.getProcessed()) return null;
        
        OAFilter filter = new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                boolean b = OAAuthDelegate.canEditProcessed();
                return b;
            }
        };
        Hub hubUser = OAAuthDelegate.getUserHub();
        HubProp hp = add(hubUser, OAAuthDelegate.getAllowEditProcessedPropertyPath());
        return hp;
    }
*/    
    
    public HubProp addRemoveEnabled(final Hub hub) {
        if (hub == null) return null;
        
        addAoNotNull(hub);
        OAFilter filter = new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                boolean b;
                if (obj instanceof OAObject) b = hub.canRemove((OAObject) obj);
                else b = hub.canRemove();
                return b;
            }
        };
        HubProp hp = add(hub, null, false, null, filter, false, "Hub.canRemove");
        
        Hub hx = hub.getMasterHub();
        if (hx != null) {
            add(hx, Type.AoNotNull);
            OAObjectEditQueryDelegate.addEditQueryChangeListeners(hx, hx.getObjectClass(), null, null, this, true);
        }
        return hp;
    }
    public HubProp addSaveEnabled(final Hub hub) {
        if (hub == null) return null;
        addAoNotNull(hub);
        HubProp hp = add(hub, OAObjectDelegate.WORD_Changed, true);
        return hp;
    }
    
    
    /** add a rule to check the return value for an EditQuery isEnabled 
     * */
    public HubProp addEditQueryEnabled(Hub hub, String prop) {
        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, hub.getObjectClass(), prop, null, this, true);
        
        // include master
        Hub hx = hub.getMasterHub();
        if (hx != null) {
            OALinkInfo li = HubDetailDelegate.getLinkInfoFromMasterObjectToDetail(hub);
            if (li.getOwner()) {
                String propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
                OAObjectEditQueryDelegate.addEditQueryChangeListeners(hx, hx.getObjectClass(), propx, null, this, true);
            }
        }
        return add(hub, prop, true, Type.EditQueryEnabled);
    }
    public HubProp addEditQueryEnabled(Hub hub, Class cz, String prop, String ppPrefix) {
        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, cz, prop, ppPrefix, this, true);
        return add(hub, prop, true, Type.EditQueryEnabled);
    }

    /** add a rule to check the return value for an EditQuery isVisible */
    public HubProp addEditQueryVisible(Hub hub, String prop) {
        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, hub.getObjectClass(), prop, null, this, false);
        return add(hub, prop, true, Type.EditQueryVisible);
    }
    public HubProp addEditQueryVisible(Hub hub, Class cz, String prop, String ppPrefix) {
        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, cz, prop, ppPrefix, this, false);

        // include master
        Hub hx = hub.getMasterHub();
        if (hx != null) {
            OALinkInfo li = HubDetailDelegate.getLinkInfoFromMasterObjectToDetail(hub);
            if (li.getOwner()) {
                String propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
                OAObjectEditQueryDelegate.addEditQueryChangeListeners(hx, hx.getObjectClass(), propx, null, this, false);
            }
        }

        return add(hub, prop, true, Type.EditQueryVisible);
    }
    
    public HubProp add(Hub hub, HubChangeListener.Type type) {
        return add(hub, null, (type==null?false:true), type, null, (type==null?true:type.bUseAoOnly), null);
    }
    public HubProp add(Hub hub, String property, HubChangeListener.Type type) {
        return add(hub, property, type==null?false:true, type, null, (type==null?true:type.bUseAoOnly), null);
    }
    
    
    /**
     * Add an addition hub/property to base the check on.
     * @param compareValue can be null, OANullObject.instance, OANotNullObject.instance, OAAnyValueObject.instance, Type.PropertyNull, Type.PropertyNotNull
     *      or any other value.  
     */
    public HubProp add(Hub hub, final String propertyPath, Object compareValue) {
        return add(hub, propertyPath, true, compareValue, null, true, null);
    }

    public HubProp add(Hub hub, OAFilter filter) {
        return add(hub, null, true, null, filter, true, "filter");
    }
    
    public HubProp add(Hub hub, final String propertyPath, boolean bUseCompareValue, Object compareValue) {
        Type type = null;
        if (bUseCompareValue && compareValue instanceof Type) {
            type = (Type) compareValue;
        }
        return this.add(hub, propertyPath, bUseCompareValue, compareValue, null, (type==null?true:type.bUseAoOnly), null);
    }
        
    public HubProp add(Hub hub, final String propertyPath, boolean bUseCompareValue, Object compareValue, OAFilter filter, final boolean bAoOnly, String description) {
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

        final HubProp newHubProp = new HubProp(hub, propertyPath, newPropertyPath, props, bUseCompareValue, compareValue, filter, bAoOnly, description);
        
        // see if there is a listener with same hub - and one without a propertyName used
        for (HubProp hp : hubProps) {
            if (hp.equals(newHubProp)) {
                return null;
            }
        }
        
        if (bUseCompareValue && compareValue == Type.EditQueryEnabled) {
            for (HubProp hp : hubProps) {
                if (hp.bUseCompareValue && hp.compareValue == Type.EditQueryEnabled && hub == hp.hub) {
                    if (OAString.isEmpty(hp.propertyPath)) {
                        hp.bIgnore = true;
                    }
                    else {
                        if (OAString.isEmpty(propertyPath)) return null;
                        if (hp.propertyPath.equalsIgnoreCase(propertyPath)) {
                            hp.bIgnore = true;
                        }
                    }
                }
            }
        }
        if (bUseCompareValue && compareValue == Type.EditQueryVisible) {
            for (HubProp hp : hubProps) {
                if (hp.bUseCompareValue && hp.compareValue == Type.EditQueryVisible && hub == hp.hub) {
                    if (OAString.isEmpty(hp.propertyPath)) {
                        hp.bIgnore = true;
                    }
                    else {
                        if (OAString.isEmpty(propertyPath)) return null;
                        if (hp.propertyPath.equalsIgnoreCase(propertyPath)) {
                            hp.bIgnore = true;
                        }
                    }
                }
            }
        }
            
        assignHubListener(newHubProp);
        
        hubProps = (HubProp[]) OAArray.add(HubProp.class, hubProps, newHubProp);
        callOnChange();

        Hub h = (hub == null) ? null : hub.getLinkHub(true);
        if (h != null) {
            if (HubLinkDelegate.isLinkAutoCreated(hub, true)) {
                // need to listen for AO changes, newList, etc from the linkTo Hub
                add(h, null, OAAnyValueObject.instance);
            }
            else addHubValid(h);
        }
        return newHubProp;
    }        

    protected void assignHubListener(final HubProp newHubProp) {
        // see if a new hubListener is needed
        for (HubProp hp : hubProps) {
            if (hp.bIgnore) continue;
            if (hp.hub != newHubProp.hub) continue;
            if (newHubProp.propertyPath != null) {
                if (!newHubProp.propertyPath.equalsIgnoreCase(hp.propertyPath)) continue; 
            }
            newHubProp.hubListener = hp.hubListener;
            break;
        }

        if (newHubProp.hubListener != null) {
            return;
        }
        
        newHubProp.hubListener = new HubListenerAdapter() {
            public void afterChangeActiveObject(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;
                callOnChange();
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;

                String s = e.getPropertyName();
                for (HubProp hp : hubProps) {
                    if (hp.bIgnore) continue;
                    if (hp.hub == null) continue;
                    if (hp.hub != newHubProp.hub) continue;
                    
                    if (!hp.bAoOnly || e.getObject() == newHubProp.hub.getAO()) {
                        if (s != null && s.equalsIgnoreCase(hp.listenToPropertyName)) {
                            callOnChange();
                            break;
                        }
                    }
                }
            }
            // linked to hub listener
            @Override
            public void onNewList(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;
                callOnChange();
            }
            @Override
            public void afterAdd(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;
                for (HubProp hp : hubProps) {
                    if (hp.bIgnore) continue;
                    if (hp.hub != newHubProp.hub) continue;
                    if (!hp.bAoOnly || hp.propertyPath == null) {
                        callOnChange();
                        break;
                    }
                }
            }
            @Override
            public void afterInsert(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;
                for (HubProp hp : hubProps) {
                    if (hp.bIgnore) continue;
                    if (hp.hub != newHubProp.hub) continue;
                    if (!hp.bAoOnly || hp.propertyPath == null) {
                        callOnChange();
                        break;
                    }
                }
            }
            @Override
            public void afterRemove(HubEvent e) {
                if (e == lastHubEvent) return;
                lastHubEvent = e;
                for (HubProp hp : hubProps) {
                    if (hp.bIgnore) continue;
                    if (hp.hub != newHubProp.hub) continue;
                    if (!hp.bAoOnly || hp.propertyPath == null) {
                        callOnChange();
                        break;
                    }
                }
            }
        };

        if (newHubProp.hub != null) {
            if (newHubProp.props == null) {
                if (newHubProp.propertyPath == null) {
                    newHubProp.hub.addHubListener(newHubProp.hubListener);
                }
                else newHubProp.hub.addHubListener(newHubProp.hubListener, newHubProp.listenToPropertyName, newHubProp.bAoOnly);
            }
            else {
                newHubProp.hub.addHubListener(newHubProp.hubListener, newHubProp.listenToPropertyName, newHubProp.props, newHubProp.bAoOnly);
            }
        }
    }
    
    public void clear() {
        close();
        hubProps = new HubProp[0];
    }
    
    public void close() {
        for (HubProp hp : hubProps) {
            if (hp.hub != null && hp.hubListener != null) {
                hp.hub.removeHubListener(hp.hubListener);
                for (HubProp hpx : hubProps) {
                    if (hpx.hubListener == hp.hubListener) hpx.hubListener = null;
                }
            }
        }
    }
    
    public void remove(Hub hub) {
        remove(hub, null);
    }
    public void remove(Hub hub, String prop) {
        if (hub == null) return;
        for (HubProp hp : hubProps) {
            if (hp.hub != hub) continue;
            if (!OAString.equals(prop, hp.propertyPath)) continue;
            if (hp.hubListener == null) continue;

            boolean b = false;
            for (HubProp hpx : hubProps) {
                if (hpx == hp) continue;
                if (hpx.hubListener == hp.hubListener) {
                    b = true;
                    break;
                }
            }
            if (!b) hp.hub.removeHubListener(hp.hubListener);
            hp.hubListener = null;
            break;
        }
    }
    public void remove(HubProp hp) {
        if (hp == null) return;
        remove(hp.hub, hp.propertyPath);
        hubProps = (HubProp[]) OAArray.removeValue(HubProp.class, hubProps, hp);
    }
    
    
    /**
     * Checks all of the compare values that are being listened to.  All must be true to return true, else returns false.
     */
    public boolean getValue() {
        failureReason = null;
        boolean b = true;
        for (HubProp hp : hubProps) {
            if (hp.bIgnore) continue;
            if (hp.filter != null) {
                if (hp.hub == null) b = hp.filter.isUsed(null);
                else b = hp.filter.isUsed(hp.hub.getAO());
            }
            else b = hp.getValue();
            if (!b && failureReason == null) {
                failureReason = hp.failureReason;
                break;
            }
        }
        return b;
    }
    
    public String getToolTipText() {
        String tt = "";
        for (HubProp hp : hubProps) {
            if (hp.bIgnore) continue;
            String s = hp.getToolTipText();
            if (s == null) s = hp.description;
            if (OAString.isNotEmpty(s)) {
                tt = OAString.append(tt, s, "<br>");
            }
        }
        tt = OAString.append(tt, failureReason, "<br>Reason: ");
        return tt;
    }

    public String getFailureReason() {
        return failureReason;
    }
    
    public HubProp getFalseValue() {
        boolean b = true;
        for (HubProp hp : hubProps) {
            if (hp.bIgnore) continue;
            if (hp.filter != null) {
                if (hp.hub == null) b = hp.filter.isUsed(null);
                else b = hp.filter.isUsed(hp.hub.getAO());
            }
            else b = hp.getValue();
            if (!b) return hp;
        }
        return null;
    }
    
    
    public static class HubProp {
        public Hub<?> hub;
        public String propertyPath;  // original propertyPath
        public String listenToPropertyName;  // name used for listener - in case property path has '.' in it, then this will replace with '_' 
        public String[] props;
        public HubListener hubListener;
        public Object compareValue;
        public boolean bUseCompareValue;
        public OAFilter filter;
        public boolean bAoOnly;
        public boolean bIgnore; // flag used when another rule overrides this one
        public String failureReason;
        public String description;

        public HubProp(Hub<?> h, String propertyPath, String listenPropertyName, String[] props, boolean bUseCompareValue, Object compareValue, OAFilter filter, boolean bAoOnly, String description) {
            this.hub = h;
            this.propertyPath = propertyPath;
            this.listenToPropertyName = listenPropertyName;
            this.props = props;
            this.bUseCompareValue = bUseCompareValue;
            this.compareValue = compareValue;
            this.filter = filter;
            this.bAoOnly = bAoOnly;
            this.description = description;
        }

        public boolean getValue() {
            failureReason = null;
            boolean bValid = hub != null && hub.isValid();
            if (bUseCompareValue && compareValue != null) {
                if (compareValue == Type.HubValid) return bValid;
                if (compareValue == Type.HubNotValid) return !bValid;
                if (compareValue == Type.HubEmpty) return (bValid && hub.getSize() == 0);
                if (compareValue == Type.HubNotEmpty) return (bValid && hub.getSize() > 0);
                if (compareValue == Type.AoNull) return (bValid && hub.getAO() == null);
                if (compareValue == Type.AoNotNull) return (bValid && hub.getAO() != null);
                if (compareValue == Type.AlwaysTrue) return true;
                if (compareValue == Type.AlwaysFalse) {
                    failureReason = "always false";
                    return false;
                }
                if (compareValue == Type.Unknown) return true;
            }

            Object value = (bValid) ? hub.getAO() : null;
            
            if (compareValue == Type.EditQueryEnabled) {
                if (!bValid) return false;
                if (!(value instanceof OAObject)) return true;
                OAObjectEditQuery eq  = OAObjectEditQueryDelegate.getAllowEnabledEditQuery((OAObject) value, propertyPath, true);
                boolean b = eq.getAllowed();
                if (!b) {
                    failureReason = eq.getDisplayResponse();
                    if (OAString.isEmpty(failureReason)) failureReason = "edit query returned false";
                }
                return b;
            }
            if (compareValue == Type.EditQueryVisible) {
                if (!bValid) return true;
                if (!(value instanceof OAObject)) {
                    if (hub == null) return true;
                    Class cx = hub.getObjectClass();
                    if (!OAObject.class.isAssignableFrom(cx)) return true;
                    OAObjectEditQuery eq = OAObjectEditQueryDelegate.getAllowVisibleEditQuery(cx, propertyPath);
                    boolean b = eq.getAllowed();
                    if (!b) failureReason = eq.getDisplayResponse();
                    return b;
                }
                OAObjectEditQuery eq = OAObjectEditQueryDelegate.getAllowVisibleEditQuery((OAObject) value, propertyPath);
                boolean b = eq.getAllowed();
                if (!b) {
                    failureReason = eq.getDisplayResponse();
                    if (OAString.isEmpty(failureReason)) failureReason = "edit query returned false";
                }
                return b;
            }
            
            if (!bValid) return false;
            
            if (propertyPath != null) {
                if (value instanceof OAObject) value = ((OAObject)value).getProperty(propertyPath);
            }
            
            if (bUseCompareValue && compareValue != null) {
                if (compareValue == Type.PropertyNull) return (hub != null && hub.getAO() != null && value == null);
                if (compareValue == Type.PropertyNotNull) return (value != null);
            }
            
            boolean b;
            if (bUseCompareValue) {
                b = OACompare.compare(compareValue, value) == 0;
            }
            else {
                b = OAConv.toBoolean(value);
            }
            if (!b) failureReason = "compare value is false";
            return b;
        }
        
        public String getToolTipText() {
            if (bIgnore) return null;
            String tt = null;
            /*
            if (compareValue instanceof Type) {
                tt = OAString.append(tt, compareValue.toString(), "<br>");
            }
            else if (compareValue == Type.EditQueryEnabled) {
                tt = OAString.append(tt, "editQueryEnabled", "<br>");
            }
            else if (compareValue == Type.EditQueryVisible) {
                tt = OAString.append(tt, "editQueryVisible", "<br>");
            }
            */
            if (bUseCompareValue && compareValue != null && compareValue != Type.AlwaysTrue) {
                tt = OAString.append(tt, "compareValue="+compareValue, "<br>");
            }
//todo:  see if you can figure out if editQueries exists or not qqqq            
            return tt;
        }
        
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HubProp)) return false;
            HubProp hp = (HubProp) obj;
            if (this.hub != hp.hub) return false;
            if (this.bUseCompareValue != hp.bUseCompareValue) return false;
            
            if (this.compareValue != null) {
                if (hp.compareValue == null) return false;
                //if (this.compareValue != hp.compareValue) {
                    if (!this.compareValue.equals(hp.compareValue)) {
                        if (!this.compareValue.equals(OAConv.convert(this.compareValue.getClass(), hp.compareValue))) return false;
                    }
                //}
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
            if (hub == null) return super.hashCode();
            return hub.hashCode();
        }
    }
    
    protected ArrayList<HubChangeListener> alHubChangeListener;
    /**
     * Allows for chaining this HCL with other HCLs.
     */
    public void addHubChangeListener(HubChangeListener hcl) {
        if (hcl == null) return;
            
        if (alHubChangeListener == null) alHubChangeListener = new ArrayList<>();
        alHubChangeListener.add(hcl);
        hcl.onChange();
    }
    protected void callOnChange() {
        onChange();
        if (alHubChangeListener == null) return;
        for (HubChangeListener hcl : alHubChangeListener) {
            hcl.callOnChange();
        }
    }
    
    protected abstract void onChange();
}
