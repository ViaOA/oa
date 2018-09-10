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
package com.viaoa.object;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThread;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.util.OAArray;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAInteger;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/** 
    OAObjectInfo contains information about an OAObject.
    This includes object id, links to other objects, calculated properties. 
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAObjectInfo { //implements java.io.Serializable {
    private static Logger LOG = Logger.getLogger(OAObjectInfo.class.getName());
    static final long serialVersionUID = 1L;
    static final Object vlock = new Object();

    protected Class thisClass; // the Class for this ObjectInfo.  Set when calling OAObjectDelegete.getOAObjectInfo
    
    protected List<OALinkInfo> alLinkInfo;
    protected ArrayList<OACalcInfo> alCalcInfo;
    protected HashSet<String> hsHubCalcInfoName = new HashSet<String>();
    protected String[] idProperties;
    protected String[] importMatchProperties;
    protected ArrayList<OAPropertyInfo> alPropertyInfo;
    
    protected boolean bUseDataSource = true;
    protected boolean bLocalOnly = false;  // dont send to OAServer
    protected boolean bAddToCache = true;  // add object to Cache
    protected boolean bInitializeNewObjects = true;  // initialize object properties (used by OAObject)
    protected String displayName;
    protected String[] rootTreePropertyPaths;

    // this is set by OAObjectInfoDelegate.initialize()
    // All primitive properties, in uppercase and sorted.
    // This is used by OAObject.nulls, to get the bit position for an objects primitive properties.
    protected String[] primitiveProps;
    protected byte[] primitiveMask; // used to mask boolean to not default to null, instead false
    

    // 20120827 hubs that have a size of 0
    protected String[] hubProps;

    int weakReferenceable=-1; // flag set/used by OAObjectInfoDelegate.isWeakReferenceable -1=not checked, 0=false, 1=true
    private int supportsStorage=-1; // flag set/used by caching  -1:not checked, 0:false, 1:true
    
    protected volatile boolean bSetRecursive;
    protected OALinkInfo liRecursiveOne, liRecursiveMany;
    protected volatile boolean bSetLinkToOwner;
    protected OALinkInfo liLinkToOwner;  // set by OAObjectInfoDelegate.getLinkToOwner
    
    protected boolean bLookup;
    
    
    public OAObjectInfo() {
        this(new String[] { });
    }
    
    public Class getForClass() {
        return thisClass;
    }
    public OAObjectInfo(String objectIdProperty) {
        this(new String[] { objectIdProperty });
    }
    public OAObjectInfo(String[] idProperties) {
    	this.idProperties = idProperties;
    }
    void setPropertyIds(String[] ss) {
        this.idProperties = ss;
    }
    
    public String[] getIdProperties() {
    	if (this.idProperties == null) this.idProperties = new String[0];
    	return this.idProperties;
    }

    public boolean isIdProperty(String prop) {
        if (prop == null) return false;
        for (String s : getIdProperties()) {
            if (prop.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
    
    public boolean hasImportMatchProperties() {
        return getImportMatchProperties().length > 0;
    }
  
    public String[] getImportMatchProperties() {
        if (this.importMatchProperties == null) {
            this.importMatchProperties = new String[0];
            for (OAPropertyInfo pi : getPropertyInfos()) {
                if (pi.isImportMatch()) {
                    this.importMatchProperties = (String[]) OAArray.add(this.importMatchProperties, pi.getName());
                }
            }
            for (OALinkInfo li : getLinkInfos()) {
                if (!li.getUsed()) continue;
                if (li.getType() == li.ONE && li.isImportMatch()) {
                    this.importMatchProperties = (String[]) OAArray.add(this.importMatchProperties, li.getName());
                }
            }
        }
        return this.importMatchProperties;
    }
    
    public List<OALinkInfo> getLinkInfos() {
    	if (alLinkInfo == null) alLinkInfo = new CopyOnWriteArrayList<OALinkInfo>() {
    	    void reset() {
                hmLinkInfo = null;
                ownedLinkInfos = null;
                bSetRecursive = false;
    	    }
    	    @Override
    	    public boolean add(OALinkInfo e) {
                reset();
                return super.add(e);
    	    }
    	    @Override
    	    public OALinkInfo remove(int index) {
                reset();
    	        return super.remove(index);
    	    }
    	    @Override
    	    public boolean removeAll(Collection<?> c) {
    	        reset();
    	        return super.removeAll(c);
    	    }
    	    @Override
    	    public boolean remove(Object o) {
                reset();
    	        return super.remove(o);
    	    }
    	};
    	return alLinkInfo;
    }
    public void addLink(OALinkInfo li) {
    	addLinkInfo(li);
    }
    public void addLinkInfo(OALinkInfo li) {
        getLinkInfos().add(li);
    }
    private HashMap<String,OALinkInfo> hmLinkInfo;
    public OALinkInfo getLinkInfo(String propertyName) {
        if (propertyName == null) return null;
        HashMap<String,OALinkInfo> hm = hmLinkInfo;
        if (hm == null) {
            hm = new HashMap<String, OALinkInfo>();
            for (OALinkInfo li : getLinkInfos()) {
                String s = li.getName();
                if (s == null) continue;
                hm.put(s.toUpperCase(), li);
            }
            hmLinkInfo = hm;
        }
        return hm.get(propertyName.toUpperCase());
    }
    
    private OALinkInfo[] ownedLinkInfos;
    public OALinkInfo[] getOwnedLinkInfos() {
        if (ownedLinkInfos == null) {
            int x = 0;
            for (OALinkInfo li : getLinkInfos()) {
                if (!li.getUsed()) continue;
                if (li.bOwner) x++;
            }
            OALinkInfo[] temp = new OALinkInfo[x];
            int i = 0;
            for (OALinkInfo li : getLinkInfos()) {
                if (!li.getUsed()) continue;
                if (li.bOwner) {
                    if (i == x) {
                        return getOwnedLinkInfos();
                    }
                    temp[i++] = li; 
                }
            }
            ownedLinkInfos = temp;
        }
        return ownedLinkInfos;
    }

    private boolean bOwnedAndNoMany;
    private boolean bOwnedAndNoManyCheck;
    public boolean isOwnedAndNoReverseMany() {
        if (bOwnedAndNoManyCheck) return bOwnedAndNoMany;
        for (OALinkInfo li : getLinkInfos()) {
            if (!li.getUsed()) continue;
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev == null) continue;
            if (!liRev.getUsed()) continue;
            if (liRev.type == OALinkInfo.MANY) {
                bOwnedAndNoMany = false;
                break;
            }
            if (li.type != OALinkInfo.ONE) continue;
            if (liRev.bOwner) {
                bOwnedAndNoMany = true;
            }
        }
        bOwnedAndNoManyCheck = true;
        return bOwnedAndNoMany;
    }
    
    private boolean bOwnedByOne;
    private OALinkInfo liOwnedByOne;
    public OALinkInfo getOwnedByOne() {
        if (bOwnedByOne) return liOwnedByOne;
        for (OALinkInfo li : getLinkInfos()) {
            if (!li.getUsed()) continue;
            if (li.type != OALinkInfo.ONE) continue;
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev != null && liRev.bOwner) {
                if (!liRev.getUsed()) continue;
                liOwnedByOne = li;
                break;
            }
        }
        bOwnedByOne = true;
        return liOwnedByOne;
    }
    
    public ArrayList<OACalcInfo> getCalcInfos() {
    	if (alCalcInfo == null) alCalcInfo = new ArrayList<OACalcInfo>(5);
    	return alCalcInfo;
    }
    public OACalcInfo getCalcInfo(String s) {
        if (alCalcInfo == null) return null;
        for (OACalcInfo ci : alCalcInfo) {
            if (ci.name.equalsIgnoreCase(s)) return ci;
        }
        return null;
    }
    public void addCalc(OACalcInfo ci) {
        addCalcInfo(ci);
    }
    public void addCalcInfo(OACalcInfo ci) {
    	getCalcInfos().add(ci);
    	if (ci.bIsForHub) {
        	String s = ci.getName();
        	if (s != null) {
        	    hsHubCalcInfoName.add(s.toUpperCase());
        	}
    	}
    }
    public boolean isHubCalcInfo(String name) {
        if (name == null) return false;
        return hsHubCalcInfoName.contains(name.toUpperCase());
    }
    
    /**
     * This is for regular properties, and does not include reference properties.
     * @see #getLinkInfos() to get list of reference properties.
     */
    public ArrayList<OAPropertyInfo> getPropertyInfos() {
    	if (alPropertyInfo == null) alPropertyInfo = new ArrayList(5);
    	return alPropertyInfo;
    }
    public void addProperty(OAPropertyInfo ci) {
    	getPropertyInfos().add(ci);
        hmPropertyInfo = null; 
    }
    public void addPropertyInfo(OAPropertyInfo ci) {
    	getPropertyInfos().add(ci);
        hmPropertyInfo = null; 
    }

    private HashMap<String,OAPropertyInfo> hmPropertyInfo;
    
    public OAPropertyInfo getPropertyInfo(String propertyName) {
        if (propertyName == null) return null;
        HashMap<String,OAPropertyInfo> hm = hmPropertyInfo;
        if (hm == null) {
            hm = new HashMap<String, OAPropertyInfo>();
            for (OAPropertyInfo pi : getPropertyInfos()) {
                String s = pi.getName();
                if (s == null) continue;
                hm.put(s.toUpperCase(), pi);
            }
            hmPropertyInfo = hm;
        }
        return hm.get(propertyName.toUpperCase());
    }
    
    
    // set by OAObjectInfoDelegate.initialize().  All primitive properties, in uppercase, sorted - 
    //   used for the bit position for OAObject.nulls
    public String[] getPrimitiveProperties() {
        return primitiveProps;
    }
    
    
    // 20180325  20180403 removed, not used
    /**
     * used to set which primitive properties should be set to null for new instances.
     * boolean props will not be set to null.
     * @return
     * /
    public byte[] getPrimitiveMask() {
        if (primitiveMask != null) return primitiveMask; 
        
        String[] ps = getPrimitiveProperties();
        int x = (ps==null) ? 0 : ((int) Math.ceil(ps.length / 8.0d));
        primitiveMask = new byte[x];
        for (int i=0; i<x; i++) {
            primitiveMask[i] = ((byte) 0xFF);
        }
        
        int pos = -1;  // bit pos 
        for (String prop : ps) {
            pos++;
            OAPropertyInfo pi = getPropertyInfo(prop);
            if (pi == null) continue;
            //if (!pi.isNameValue()) {
                Class c = pi.getClassType();
                if (!c.equals(boolean.class)) continue;
            //}
            int posByte = (pos / 8);
            int posBit = 7 - (pos % 8);

            byte b = (byte) 0;
            b |= ((byte) 1) << posBit;
            
            primitiveMask[posByte] ^= b;
        }
        return primitiveMask;
    }
    */
    
    // 20120827
    public String[] getHubProperties() {
        return hubProps;
    }

    public void setUseDataSource(boolean b) {
        bUseDataSource = b;
    }
    public boolean getUseDataSource() {
        return bUseDataSource;
    }

    public void setLocalOnly(boolean b) {
        bLocalOnly = b;
    }
    public boolean getLocalOnly() {
        return bLocalOnly;
    }

    public void setAddToCache(boolean b) {
        bAddToCache = b;
    }
    public boolean getAddToCache() {
        return bAddToCache;
    }

    public void setInitializeNewObjects(boolean b) {
        bInitializeNewObjects = b;
    }
    public boolean getInitializeNewObjects() {
        return bInitializeNewObjects;
    }

    public String getDisplayName() {
        if (displayName == null && thisClass != null) displayName = thisClass.getName();
        return displayName;
    }
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    public String[] getRootTreePropertyPaths() {
        return rootTreePropertyPaths;
    }
    public void setRootTreePropertyPaths(String[] paths) {
        this.rootTreePropertyPaths = paths;
    }
    
    public void addRequired(String prop) {
    	ArrayList al = getPropertyInfos();
    	for (int i=0; i<al.size(); i++)  {
    		OAPropertyInfo pi = (OAPropertyInfo) al.get(i);
    		if (pi.getName().equalsIgnoreCase(prop)) {
    			pi.setRequired(true);
    		}
    	}
    }

    public OALinkInfo getRecursiveLinkInfo(int type) {
    	return OAObjectInfoDelegate.getRecursiveLinkInfo(this, type);
    }

    private int lastDataSourceChangeCnter;
    public boolean getSupportsStorage() {
        if (supportsStorage == -1 || lastDataSourceChangeCnter != OADataSource.getChangeCounter()) {
            supportsStorage = -1;
            lastDataSourceChangeCnter = OADataSource.getChangeCounter();
            OADataSource ds = OADataSource.getDataSource(thisClass);
            if (ds != null) {
                supportsStorage = ds.supportsStorage() ? 1 : 0;
            }
        }
        return supportsStorage == 1;
    }


    /**
     * June 2016
     * triggers when a property/hub is changed.
     */
    protected static class TriggerInfo {
        OATrigger trigger;
        String ppFromRootClass;;  
        String ppToRootClass;;  // reverse propPath from thisClass to root Class
        String listenProperty;  // property/hub to listen to.
        boolean bNoReverseFinder;
        boolean bReverseHasMany;
    }
    
    // list of triggers per prop/link name
    protected ConcurrentHashMap<String, CopyOnWriteArrayList<TriggerInfo>> hmTriggerInfo = new ConcurrentHashMap<String, CopyOnWriteArrayList<TriggerInfo>>();
    private AtomicInteger aiTrigger = new AtomicInteger();
    private AtomicInteger aiTriggerBackgroundThread = new AtomicInteger();

    private final static AtomicInteger aiAllTrigger = new AtomicInteger();
    /**
     * total triggers created.
     */
    public static int getTotalTriggers() {
        return aiAllTrigger.get();
    }
    
    public ArrayList<String> getTriggerPropertNames() {
        ArrayList<String> al = new ArrayList<String>();
        for (String s : hmTriggerInfo.keySet()) {
            al.add(s);
        }
        return al;
    }

    
    /**
     * Not called directly, but used by OATriggerDelegate.createTrigger(..) to create, register and call a trigger.
     * @param trigger
     * @param bSkipFirstNonManyProperty if true then the first property of type prop/calc/one will not be listened to.  This is
     * used when a hubListener takes care of these changes.
     */
    protected void createTrigger(final OATrigger trigger, final boolean bSkipFirstNonManyProperty) {
        if (trigger == null) return;

        if (trigger.propertyPaths == null) {
            return;
        }

        String s = "";
        if (trigger.propertyPaths != null) {
            for (String triggerPropPath : trigger.propertyPaths) {
                if (s.length() > 0) s += ", ";
                s += triggerPropPath;
            }
        }        
        s = (thisClass.getSimpleName()+", name="+trigger.name+", propPaths=["+s+"], skipFirst="+bSkipFirstNonManyProperty);
        LOG.fine(s);
        if (OAPerformance.IncludeTriggers) OAPerformance.LOG.fine(s);
        
        
        for (String triggerPropPath : trigger.propertyPaths) {
            if (OAString.isEmpty(triggerPropPath)) continue;
            OAPropertyPath pp = new OAPropertyPath(thisClass, triggerPropPath);  
          
            // addTrigger for every prop in the propPath
            String propPath = "";
            String revPropPath = "";
            OAObjectInfo oix = this;
            boolean bNoReverseFinder = false;
            boolean bReverseHasMany = false;
            
            for (int i=0; i<pp.getLinkInfos().length; i++) {
                OALinkInfo li = pp.getLinkInfos()[i];
                OALinkInfo rli = li.getReverseLinkInfo();
                if (rli == null) {
                    bNoReverseFinder = true;
                }
                else if (rli.getType() == OALinkInfo.MANY) {
                    bReverseHasMany = true;
                }
                
                if (bSkipFirstNonManyProperty && i == 0 && (li.getType() == OALinkInfo.ONE)) {
                }
                else {
                    TriggerInfo ti = oix._addTrigger(trigger, propPath, revPropPath, li.getName());
                    ti.bNoReverseFinder = bNoReverseFinder;
                    ti.bReverseHasMany = bReverseHasMany;
                }
                
                if (propPath.length() > 0) {
                    propPath += ".";
                    revPropPath = "." + revPropPath;
                }
                propPath += li.getName();
                revPropPath = li.getReverseName() + revPropPath;
                
                // todo: reverse path might not work (if it has a private method)    
                oix = OAObjectInfoDelegate.getOAObjectInfo(li.getToClass());
            }

            if (!pp.isLastPropertyLinkInfo()) {
                String[] ss = pp.getProperties();
                if (!bSkipFirstNonManyProperty || ss.length > 1) {
                    TriggerInfo ti = oix._addTrigger(trigger, propPath, revPropPath, ss[ss.length-1]);
                    ti.bNoReverseFinder = bNoReverseFinder;
                    ti.bReverseHasMany = bReverseHasMany;
                }
            }
        }
    }    
    
    // add the trigger to the correct OI for a propertyPath
    private TriggerInfo _addTrigger(final OATrigger trigger, final String propPath, final String revPropPath, final String listenProperty) { 
        if (trigger == null || listenProperty == null) {
            throw new IllegalArgumentException("args can not be null");
        }

        boolean bFound = true;
        CopyOnWriteArrayList<TriggerInfo> al = hmTriggerInfo.get(listenProperty.toUpperCase());
        if (al == null) {
            bFound = false;
            synchronized (hmTriggerInfo) {
                al = hmTriggerInfo.get(listenProperty.toUpperCase());
                if (al == null) {
                    al = new CopyOnWriteArrayList<OAObjectInfo.TriggerInfo>();
                    hmTriggerInfo.put(listenProperty.toUpperCase(), al);
                }                
            }
        }
        for (TriggerInfo ti : al) {
            if (ti.trigger.triggerListener == trigger.triggerListener) {
                if (OACompare.isEqual(propPath, ti.ppFromRootClass, true)) {
                    return ti;
                }
            }
        }

        int x = aiTrigger.incrementAndGet();
        if (trigger.bUseBackgroundThread) aiTriggerBackgroundThread.incrementAndGet();
        int x2 = aiTriggerBackgroundThread.get();
        
        aiAllTrigger.incrementAndGet();
        
        TriggerInfo ti = new TriggerInfo();
        ti.trigger = trigger;
        ti.ppFromRootClass = propPath;
        ti.ppToRootClass = revPropPath;
        ti.listenProperty = listenProperty;
        
        String s = (thisClass.getSimpleName()+", name="+trigger.name+", listenPropName="+listenProperty+", revPropPath="+revPropPath+", trigger.cnt="+x+", trigger.background="+x2+", system total="+aiAllTrigger.get());
        LOG.fine(s);
        if (false && OAPerformance.IncludeTriggers) OAPerformance.LOG.fine(s);
        else if ((x-x2) > 50) {
            LOG.warning(s);
        }
        

        if (!bFound) {
            String[] calcProps = null;
            for (OACalcInfo ci : getCalcInfos()) {
                if (ci.getName().equalsIgnoreCase(listenProperty)) {
                    calcProps = ci.getProperties();
                    break;
                }
            }    
            
            if (calcProps != null) {
                OATriggerListener tl = new OATriggerListener() {
                    @Override
                    public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                        // notify prop
                        onChange(obj, listenProperty, hubEvent);
                    }
                };
                OATrigger t = OATriggerDelegate.createTrigger(listenProperty, thisClass, tl, calcProps, trigger.bOnlyUseLoadedData, trigger.bServerSideOnly, trigger.bUseBackgroundThread, true);
                trigger.dependentTriggers = (OATrigger[]) OAArray.add(OATrigger.class, trigger.dependentTriggers, t); 
            }
        }
        al.add(ti);
        return ti;
    }

    public void removeTrigger(OATrigger trigger) {
        if (trigger == null) return;
        
        String s = "";
        if (trigger.propertyPaths != null) {
            for (String triggerPropPath : trigger.propertyPaths) {
                if (s.length() > 0) s += ", ";
                s += triggerPropPath;
            }
        }        
        s = (thisClass.getSimpleName()+", name="+trigger.name+", propPaths=["+s+"]");
        LOG.fine(s);
        if (OAPerformance.IncludeTriggers) OAPerformance.LOG.fine(s);
        
        _removeTrigger(trigger);
        
        if (trigger.propertyPaths == null) return;
        
        for (String spp : trigger.propertyPaths) {
            OAPropertyPath pp = new OAPropertyPath(thisClass, spp);  
            
            OAObjectInfo oix = this;
            for (int i=0; i<pp.getLinkInfos().length; i++) {
                OALinkInfo li = pp.getLinkInfos()[i];                    
                oix = OAObjectInfoDelegate.getOAObjectInfo(li.getToClass());
                oix._removeTrigger(trigger);
            }
        }
        if (trigger.dependentTriggers == null) return;
        
        // close any child/calc triggers
        for (OATrigger t : trigger.dependentTriggers) {
            OAObjectInfo oix =  OAObjectInfoDelegate.getOAObjectInfo(t.rootClass);
            oix.removeTrigger(t);
        }
    }
    protected void _removeTrigger(OATrigger trigger) {
        if (trigger == null) return;
        synchronized (hmTriggerInfo) {
            // find all that use this trigger (1+)
            for (CopyOnWriteArrayList<TriggerInfo> al : hmTriggerInfo.values()) {
                TriggerInfo tiFound = null;
                for (TriggerInfo ti : al) {
                    if (ti.trigger == trigger) {
                        tiFound = ti;
                        break;
                    }
                }
                if (tiFound == null) continue;
                al.remove(tiFound);
                int x = aiTrigger.decrementAndGet();
                aiAllTrigger.decrementAndGet();
                if (al.size() == 0) {
                    hmTriggerInfo.remove(tiFound.listenProperty.toUpperCase());
                }
                
                String s = (thisClass.getSimpleName()+", name="+trigger.name+", prop="+tiFound.listenProperty+", revPropPath="+tiFound.ppToRootClass+", trigger.cnt="+x+", total="+aiAllTrigger.get());
                LOG.fine(s);
                if (false && OAPerformance.IncludeTriggers) OAPerformance.LOG.fine(s);
            }
        }
    }
    
    public boolean getHasTriggers() {
        return hmTriggerInfo.size() > 0;
    }

    public ArrayList<OATrigger> getTriggers(String propertyName) {
        if (propertyName == null) return null;
        CopyOnWriteArrayList<TriggerInfo> al = hmTriggerInfo.get(propertyName.toUpperCase());
        if (al == null) return null;
        ArrayList<OATrigger> alTrigger = new ArrayList<OATrigger>();
        for (TriggerInfo ti : al) {
            alTrigger.add(ti.trigger);
        }
        return alTrigger;
    }

    
    
    /**
     * called by OAObject.propChange, Hub.add/remove/removeAll/insert, and OAObjectCacheDelegate when a change is made.
     * This will then check to see if there is trigger method to send the change to.
     */
    public void onChange(final OAObject fromObject, final String prop, final HubEvent hubEvent) {
        if (prop == null || hubEvent == null) return;

        final int x = OAThreadLocalDelegate.getRecursiveTriggerCount();
        if (x > 25) {
            throw new RuntimeException("onChange for Triggers has caused a loop over 25"); 
        }
        
        try {
            OAThreadLocalDelegate.setRecursiveTriggerCount(x+1);
            
            CopyOnWriteArrayList<TriggerInfo> al = hmTriggerInfo.get(prop.toUpperCase());
            if (al == null) return;
            
            for (TriggerInfo ti : al) {
                _onChange(fromObject, prop, ti, hubEvent);
            }
        }
        finally {
            OAThreadLocalDelegate.setRecursiveTriggerCount(x);
        }
    }        
    
    private void _onChange(final OAObject fromObject, final String prop, final TriggerInfo ti, final HubEvent hubEvent) {
        boolean b = false;
        boolean b2 = false; 
        if (ti.trigger.bServerSideOnly) {
            if (!OASync.isServer()) return;
            b = true;
            b2 = OASync.sendMessages();
        }

        
        String s = "";
        if (ti.trigger.propertyPaths != null) {
            for (String triggerPropPath : ti.trigger.propertyPaths) {
                if (s.length() > 0) s += ", ";
                s += triggerPropPath;
            }
        }        
        s = (thisClass.getSimpleName()+", name="+ti.trigger.name+", propPaths=["+s+"]");
        LOG.finer(s);
        if (OAPerformance.IncludeTriggers) OAPerformance.LOG.finer(s);
        
        
        long ts = System.currentTimeMillis();
        try {
            _onChange2(fromObject, prop, ti, hubEvent);
        }
        finally {
            if (b) {
                OASync.sendMessages(b2);
            }
        }
        ts = System.currentTimeMillis() - ts;
        
        if (ts > 3) {
            s = "over 3ms, fromObject=";
            if (fromObject == null) s += fromObject; 
            else s += fromObject.getClass().getSimpleName();
            s += ", name="+ti.trigger.name+", property="+ti.ppFromRootClass+", ts="+ts;
            LOG.finer(s);
            OAPerformance.LOG.fine(s);
        }
    }

    private void _onChange2(final OAObject fromObject, final String prop, final TriggerInfo ti, final HubEvent hubEvent) {
        if (ti.trigger.bServerSideOnly) {
            if (!OASync.isServer()) return;
            OASync.sendMessages();
        }

        boolean b = false;
        if (!ti.trigger.bUseBackgroundThread && ti.trigger.bUseBackgroundThreadIfNeeded && SwingUtilities.isEventDispatchThread()) {
            // if swing thread, then run in bg thread if it has to do a many reverse pp, or if no rev pp 
            if (ti.bNoReverseFinder) {
                b = true;
            }
            else if (ti.bReverseHasMany) {
                if (OASync.isServer()) {
                    OADataSource ds = OADataSource.getDataSource(thisClass);
                    b = (ds != null && ds.supportsStorage());  // might have to go to ds
                }
                else b = true; // if client
            }
        }
        
        if ( (b || ti.trigger.bUseBackgroundThread) && !OARemoteThreadDelegate.isRemoteThread()) {
            OATriggerDelegate.runTrigger(new Runnable() {
                @Override
                public void run() {
                    _runOnChange2(fromObject, prop, ti, hubEvent);
                }
            });
        }
        else {
            _runOnChange2(fromObject, prop, ti, hubEvent);
        }
    }        
    private void _runOnChange2(final OAObject fromObject, final String prop, final TriggerInfo ti, final HubEvent hubEvent) {
        if (ti.ppToRootClass == null || ti.ppToRootClass.length() == 0) {
            try {
                ti.trigger.triggerListener.onTrigger(fromObject, hubEvent, ti.ppToRootClass);
            }
            catch (Exception e) {
                throw new RuntimeException("OAObjectInof.autoCall error, "
                    + "thisClass="+thisClass.getSimpleName() + ", "
                    + "propertyPath="+ti.ppToRootClass+", rootClass="+ti.trigger.rootClass.getSimpleName(),
                    e);
            }
            return;
        }
        
        if (ti.bNoReverseFinder) {
            try {
                ti.trigger.triggerListener.onTrigger(null, hubEvent, ti.ppFromRootClass);
            }
            catch (Exception e) {
                throw new RuntimeException("OAObjectInfo.trigger error, "
                    + "thisClass="+thisClass.getSimpleName() + ", "
                    + "propertyPath="+ti.ppToRootClass+", rootClass="+ti.trigger.rootClass.getSimpleName(),
                    e);
            }
            return;
        }


        final AtomicInteger aiStatus = new AtomicInteger(0);
        // 1 = check to see if data is loaded
        // 2 = data not found
        
        OAFinder finder = new OAFinder(ti.ppToRootClass) {
            HashSet<Integer> hs = new HashSet<Integer>();
            @Override
            protected void onFound(OAObject objRoot) {
                if (aiStatus.get() == 1) return;
                
                int g = OAObjectKeyDelegate.getKey(objRoot).getGuid();
                if (hs.contains(g)) return;
                hs.add(g);
                try {
                    ti.trigger.triggerListener.onTrigger(objRoot, hubEvent, ti.ppFromRootClass);
                }
                catch (Exception e) {
                    throw new RuntimeException("OAObjectInfo.autoCall error, "
                        + "thisClass="+thisClass.getSimpleName() + ", "
                        + "propertyPathToRoot="+ti.ppToRootClass+", rootClass="+ti.trigger.rootClass.getSimpleName(),
                        e);
                }
            }
            @Override
            protected void onDataNotFound() {
                if (aiStatus.get() == 1) {
                    aiStatus.set(2);
                    stop();
                }
            }
        };
        finder.setUseOnlyLoadedData(ti.trigger.bOnlyUseLoadedData);
        
        if (ti.bReverseHasMany) {
            // see if all of the data is already loaded, so that a reverse pp + finder can be used.
            boolean b = false;
            if (OASync.isServer()) {
                OADataSource ds = OADataSource.getDataSource(thisClass);
                b = (ds == null || !ds.supportsStorage());  // server must have all data loaded
            }
            
            if (!b) {
                // see if finder has has all of the data loaded
                aiStatus.set(1);
                try {
                    finder.find(fromObject);
                }
                catch (Exception e) {
                    ti.bNoReverseFinder = true;
                    _onChange2(fromObject, prop, ti, hubEvent);
                }
                b = (aiStatus.get() != 2);  // else: data is loaded, so use reverse pp to get data
                aiStatus.set(0);
            }
            
            if (!b) {
                try {
                    ti.trigger.triggerListener.onTrigger(null, hubEvent, ti.ppFromRootClass);
                }
                catch (Exception e) {
                    throw new RuntimeException("OAObjectInfo.trigger error, "
                        + "thisClass="+thisClass.getSimpleName() + ", "
                        + "propertyPath="+ti.ppToRootClass+", rootClass="+ti.trigger.rootClass.getSimpleName(),
                        e);
                }
                return;
            }
        }
        
        
        try {
            finder.find(fromObject);
        }
        catch (Exception e) {
            ti.bNoReverseFinder = true;
            _onChange2(fromObject, prop, ti, hubEvent);
        }
    }
    
    public void setLookup(boolean b) {
        this.bLookup = b;
    }
    public boolean getLookup() {
        return bLookup;
    }
    protected boolean bPreSelect;
    public void setPreSelect(boolean b) {
        this.bPreSelect = b;
    }
    public boolean getPreSelect() {
        return this.bPreSelect;
    }

    private String enabledProperty;
    private boolean enabledValue;
    private String visibleProperty;
    private boolean visibleValue;
    public String getEnabledProperty() {
        return enabledProperty;
    }
    public void setEnabledProperty(String s) {
        enabledProperty = s;
    }
    public boolean getEnabledValue() {
        return enabledValue;
    }
    public void setEnabledValue(boolean b) {
        enabledValue = b;
    }

    public String getVisibleProperty() {
        return visibleProperty;
    }
    public void setVisibleProperty(String s) {
        visibleProperty = s;
    }
    public boolean getVisibleValue() {
        return visibleValue;
    }
    public void setVisibleValue(boolean b) {
        visibleValue = b;
    }

}
