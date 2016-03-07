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

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAReflect;

/** 
    OAObjectInfo contains information about an OAObject.
    This includes object id, links to other objects, calculated properties. 
    <p>
    Note: this will be replaced by the com.viaoa.model Classes
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

    // 20120827 hubs that have a size of 0
    protected String[] hubProps;

    int weakReferenceable=-1; // flag set/used by OAObjectInfoDelegate.isWeakReferenceable -1=not checked, 0=false, 1=true
    private int supportsStorage=-1; // flag set/used by caching  -1:not checked, 0:false, 1:true
    
    protected volatile boolean bSetRecursive;
    protected OALinkInfo liRecursiveOne, liRecursiveMany;
    protected volatile boolean bSetLinkToOwner;
    protected OALinkInfo liLinkToOwner;  // set by OAObjectInfoDelegate.getLinkToOwner
    
    
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
                if (li.bOwner) x++;
            }
            OALinkInfo[] temp = new OALinkInfo[x];
            int i = 0;
            for (OALinkInfo li : getLinkInfos()) {
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
            OALinkInfo liRev = li.getReverseLinkInfo();
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
            if (li.type != OALinkInfo.ONE) continue;
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev != null && liRev.bOwner) {
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

   
 // 2008/01/02 all of these were created to support the old oa.html package    
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
     * 20160304 callbacks that need to be made when a property/hub is changed.
     */
    public static class MethodCallback {
        Class fromClass;
        String methodName;  // method to call in callback class
        Method method;
        String listenerProperty;  // property/hub to listen to.

        String sppToThisClass;  // propPath from the fromClass to thisClass
        String sppToFromClass;;  // propPath from thisClass to fromClass

        OAPropertyPath ppToThisClass;
        OAPropertyPath ppToFromClass;
        
        boolean bOnlyUseLoadedData;
        boolean bRunOnServer;
        boolean bRunInBackgroundThread;
    }

    protected ConcurrentHashMap<String, ArrayList<MethodCallback>> hmAutoCall = new ConcurrentHashMap<String, ArrayList<MethodCallback>>();

    
    /**
     * used by OAObject OACallback annotations to be able to call a method when a change is made in the callbacks propertyPath.
     */
    public void addCallback(Class fromClass, String methodName, String propertyPathToThisClass, String listenerProperty, 
            final boolean bOnlyUseLoadedData, 
            final boolean bServerSideOnly, 
            final boolean bBackgroundThread
            )
    {
        
        if (fromClass == null || methodName == null || propertyPathToThisClass == null || listenerProperty == null) {
            throw new IllegalArgumentException("args can not be null");
        }
        String s = "fromClass="+fromClass.getSimpleName()+", thisClass="+thisClass.getSimpleName() + ", " + "propertyPathToThis="+propertyPathToThisClass+", method="+methodName;
        LOG.fine(s);

        ArrayList<MethodCallback> al = hmAutoCall.get(listenerProperty.toUpperCase());
        if (al == null) {
            synchronized (hmAutoCall) {
                al = hmAutoCall.get(listenerProperty.toUpperCase());
                if (al == null) {
                    al = new ArrayList<OAObjectInfo.MethodCallback>();
                    hmAutoCall.put(listenerProperty.toUpperCase(), al);
                }                
            }
        }

        for (MethodCallback cb : al) {
            if (cb.fromClass == fromClass) {
                if (cb.methodName.equals(methodName)) {
                    if (cb.ppToThisClass.equals(propertyPathToThisClass)) {
                        if (cb.listenerProperty.equals(listenerProperty)) {
                            return;  // already used by another prop path in this oacallback's list of dependent properties
                        }
                    }
                }
            }
        }

        MethodCallback cb = new MethodCallback();
        al.add(cb);
        cb.fromClass = fromClass;
        cb.methodName = methodName;
        cb.method = OAReflect.getMethod(fromClass, methodName, 4); // 3={String, Object, Object}
        cb.sppToThisClass = propertyPathToThisClass;
        cb.listenerProperty = listenerProperty;
        cb.bOnlyUseLoadedData = bOnlyUseLoadedData;
        cb.bRunOnServer = bServerSideOnly;
        cb.bRunInBackgroundThread = bBackgroundThread;
        
        cb.ppToThisClass = new OAPropertyPath(fromClass, propertyPathToThisClass);
        cb.ppToFromClass = cb.ppToThisClass.getReversePropertyPath();
        if (cb.ppToFromClass != null) cb.sppToFromClass = cb.ppToFromClass.getPropertyPath(); 
    }
    
    /**
     * called by OAObject.propChange, and Hub.add/remove/removeAll/insert when a change is made.
     * This will then check to see if there is callback method to send the change to.
     */
    public void callback(final String prop, final OAObject oaObj, final Object oldValue, final Object newValue) {
        if (prop == null) return;
        if (oaObj == null) return;

        ArrayList<MethodCallback> al = hmAutoCall.get(prop.toUpperCase());
        if (al == null) return;
        
        for (MethodCallback cb : al) {
            _callback(cb, prop, oaObj, oldValue, newValue);
        }
    }        
    private void _callback(final MethodCallback cb, final String prop, final OAObject oaObj, final Object oldValue, final Object newValue) {
        if (cb.bRunOnServer) {
            if (!OASync.isServer()) return;
            OASync.sendMessages();
        }
        
        String s = "thisClass="+thisClass.getSimpleName() + ", " + "propertyPath="+cb.sppToFromClass+", method="+cb.methodName;
        LOG.fine(s);

        s = cb.sppToThisClass;
        if (s == null || s.length() == 0) s = prop;
        else s += "." + prop;
        final String pp = s;
        
        OAFinder finder = new OAFinder(cb.sppToFromClass) {
            @Override
            protected void onFound(OAObject objFrom) {
                try {
                    cb.method.invoke(objFrom, new Object[] {oaObj, pp, oldValue, newValue} );
                }
                catch (Exception e) {
                    throw new RuntimeException("OAObjectInof.autoCall error, "
                        + "thisClass="+thisClass.getSimpleName() + ", "
                        + "propertyPath="+cb.sppToFromClass+", fromClass="+cb.fromClass.getSimpleName()+", method="+cb.methodName,
                        e);
                }
            }
        };
        finder.setUseOnlyLoadedData(cb.bOnlyUseLoadedData);
        finder.find(oaObj);
    }

//    TESTTESTESTTESTTEST
    
//qqqqqqqqqqqq run in another thread ...... flag
    
//?? qqqqqqqqq option to cancel if it is called again while it is being processed qqqqqqqqqqq    
    
}


