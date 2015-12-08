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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.viaoa.annotation.OAClass;
import com.viaoa.ds.OADataSource;
import com.viaoa.hub.*;
import com.viaoa.sync.OASync;
import com.viaoa.util.*;

public class OAObjectInfoDelegate {

    private static final Object Lock = new Object();
    
    /**
        Return the OAObjectInfo for this object Class.
    */
    public static OAObjectInfo getOAObjectInfo(OAObject obj) {
        OAObjectInfo oi = getOAObjectInfo(obj == null ? null : obj.getClass());
        return oi;
    }
    public static OAObjectInfo getObjectInfo(OAObject obj) {
        return getOAObjectInfo(obj);
    }

    // 20140305 needs to be able to make sure that reverse link is created
    public static OAObjectInfo getOAObjectInfo(Class clazz) {
        OAObjectInfo oi;
        if (clazz != null) {
            oi = OAObjectHashDelegate.hashObjectInfo.get(clazz);
            if (oi != null) return oi;
        }
        
        if (clazz == null || !OAObject.class.isAssignableFrom(clazz) || OAObject.class.equals(clazz)) {
            oi = OAObjectHashDelegate.hashObjectInfo.get(String.class); // fake out so that null is never returned
            if (oi != null) return oi;
        }

        oi = getOAObjectInfo(clazz, new HashMap<Class, OAObjectInfo>());
        return oi;
    }
    private static OAObjectInfo getOAObjectInfo(Class clazz, HashMap<Class, OAObjectInfo> hash) {
        OAObjectInfo oi;
        if (clazz == null || !OAObject.class.isAssignableFrom(clazz) || OAObject.class.equals(clazz)) {
            oi = hash.get(String.class); // fake out so that null is never returned
        }
        else oi = hash.get(clazz);        
        if (oi != null) return oi;
         
        oi = _getOAObjectInfo(clazz);
        hash.put(clazz, oi);
        
        // make sure that reverse linkInfos are created.
        //   ex: ServerRoot.users, the User.class needs to have LinkInfo to serveRoot
        int x = oi.getLinkInfos().size();
        for (int i=0; i<x; i++) {
            OALinkInfo li = oi.getLinkInfos().get(i);
            if (li.type != li.MANY) continue;
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null) continue;
            Class c = li.getToClass();
            if (c == null) continue;
            OAObjectInfo oiRev = getOAObjectInfo(c, hash);
            
            String revName = li.reverseName;
            if (OAString.isEmpty(revName)) {
                li.reverseName = revName = "Reverse"+li.name;
            }

            liRev = new OALinkInfo(revName, clazz, OALinkInfo.ONE, false, false, li.name);
            liRev.bPrivateMethod = true;
            oiRev.getLinkInfos().add(liRev);
        }
        return oi;
    }    
    
    
    /**
        Used to cache OAObjectInfo based on Class.
        This will always return a valid OAObjectInfo object.
    */
    private static OAObjectInfo _getOAObjectInfo(Class clazz) {
        boolean bSkip = false;
        if (clazz == null || !OAObject.class.isAssignableFrom(clazz) || OAObject.class.equals(clazz)) {
            bSkip = true;
            clazz = String.class; // fake out so that null is never returned
        }
        
        OAObjectInfo oi = OAObjectHashDelegate.hashObjectInfo.get(clazz);
        if (oi != null) return oi;
        
        synchronized (Lock) {
            oi = (OAObjectInfo) OAObjectHashDelegate.hashObjectInfo.get(clazz);
            if (oi != null) return oi;

            if (!bSkip) {
                Method m = null;                
                try {
                    m = clazz.getMethod("getOAObjectInfo", new Class[] { } );
                    if (m != null) oi = (OAObjectInfo) m.invoke(null, null);
                }
                catch (Exception e) {
                    //System.out.println("OAObjectInfoDelegate.getOAObjectInfo "+e);
                    //e.printStackTrace();
                    oi = null;
                }
                if (oi == null) {
                    oi = new OAObjectInfo();
                }
                initialize(oi, clazz);  // this will load all props/links/primitives
                
                Class superClass = clazz.getSuperclass();  // if there is a superclass, then combine with oaobjectinfo
                if (superClass != null && !superClass.equals(OAObject.class)) {
                    OAObjectInfo oi2 = getOAObjectInfo(superClass);
                    oi = createCombinedObjectInfo(oi, oi2);
                    oi.thisClass = clazz;
                }
                
                OAAnnotationDelegate.update(oi, clazz);
                
                // 20120702
                for (OALinkInfo li : oi.getLinkInfos()) {
                    if (li.bPrivateMethod) continue;
                    Method method = OAObjectInfoDelegate.getMethod(oi, "get"+li.getName(), 0);
                    if (method == null) {
                        li.bPrivateMethod = true;
                    }
                }
                
                OAObjectHashDelegate.hashObjectInfo.put(clazz, oi);
            }   

            if (oi == null) {
                oi = new OAObjectInfo();
                initialize(oi, clazz);
                OAObjectHashDelegate.hashObjectInfo.put(clazz, oi);
            }
        }
        return oi;
    }
    public static OAObjectInfo getObjectInfo(Class clazz) {
        return getOAObjectInfo(clazz);
    }

    
    // only "grabs" info from this clazz. If there is a superclass, then it will be combined by getOAObjectInfo (above)
    public static void initialize(OAObjectInfo thisOI, Class clazz) {
        if (thisOI.thisClass != null) return;
        thisOI.thisClass = clazz;

        ArrayList<String> alPrimitive = new ArrayList<String>();
        ArrayList<String> alHub = new ArrayList<String>();
        
        // 20140331 only get props for this class, then combine with superClass(es)        
        String[] props = getPropertyNames(clazz, false);
        for (int i=0; props != null && i < props.length; i++) {
            String name = props[i];
            if (name == null) continue;
            Method m = getMethod(thisOI, "get"+name, 0);
            
            if (m == null) {
                m = getMethod(thisOI, "is"+name);
                if (m == null) {
                    continue;
                }
            }
            
            if (m.getReturnType().equals(Hub.class)) {
                if ((m.getModifiers() & Modifier.STATIC) > 0) continue;
                alHub.add(name.toUpperCase());
                createLink(thisOI, name, null, OALinkInfo.MANY);
                continue;
            }
            
            if (OAObject.class.isAssignableFrom(m.getReturnType())) {
                if ((m.getModifiers() & Modifier.STATIC) > 0) continue;
                createLink(thisOI, name, m.getReturnType(), OALinkInfo.ONE);
                continue;
            }
            
            OAPropertyInfo pi = new OAPropertyInfo();
            pi.setName(name);
            pi.setClassType(m.getReturnType());
            
            for (int j=0; thisOI.idProperties != null && j < thisOI.idProperties.length; j++) {
                if (name.equalsIgnoreCase(thisOI.idProperties[j])) {
                    pi.setId(true);
                    break;
                }
            }
            
            if (pi.getClassType() != null && pi.getClassType().isPrimitive()) {
                alPrimitive.add(pi.getName().toUpperCase());
            }
            else if (pi.getClassType().isArray() && pi.getClassType().getComponentType().equals(byte.class)) { // 20121001
                alPrimitive.add(pi.getName().toUpperCase());
            }
            thisOI.getPropertyInfos().add(pi);
        }
        
        // this must be sorted, so that they will be in the same order used by OAObject.nulls, and created the same on all other computers
        Collections.sort(alPrimitive);
        thisOI.primitiveProps = new String[alPrimitive.size()];
        alPrimitive.toArray(thisOI.primitiveProps);
        
        // 20120827 track empty hubs
        // this must be sorted, so that they will be in the same order used by OAObject.nulls, and created the same on all other computers
        Collections.sort(alHub);
        thisOI.hubProps = new String[alHub.size()];
        alHub.toArray(thisOI.hubProps);
    }

    
    private static void createLink(OAObjectInfo thisOI, String name, Class clazz, int type) {
        List al = thisOI.getLinkInfos();
        for (int i=0; i<al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (name.equalsIgnoreCase(li.getName())) {
                return;  // already exists
            }
        }
        OALinkInfo li = new OALinkInfo(name, clazz, type, false, "");
        thisOI.getLinkInfos().add(li);
    }
    
    // Used by initialize to properties. 
    public static String[] getPropertyNames(Class clazzOrig, boolean bIncludeSuperClasses) {
        Vector vecFound = new Vector(20,10);
        Vector vecFoundUpper = new Vector(20,10);
        Vector vecUpper = new Vector(20,10);
        int cnt = 0;
        for (Class c=clazzOrig; c != null && !c.equals(OAObject.class); c=c.getSuperclass()) {
            if (cnt++ > 0 && !bIncludeSuperClasses) break;
            Method[] methods = c.getDeclaredMethods();
            vecUpper.clear();
            for (int i=0; i<methods.length; i++) {
                if ((methods[i].getModifiers() & Modifier.PUBLIC) == 0) {
                    continue;
                }
    
                String s = methods[i].getName();
                if (s.length() < 3) continue;
                String s2 = s.substring(0,3);
    
                if (s2.equals("get") || s2.startsWith("is")) {
                    Class[] cs = methods[i].getParameterTypes();
                    if (cs.length > 0) continue;
                    storeMethod(clazzOrig, methods[i]);
                    if (s2.equals("get")) s = s.substring(3);
                    else s = s.substring(2);
                }
                else if (s2.equals("set")) {
                    Class[] cs = methods[i].getParameterTypes();
                    if (cs.length != 1) continue;
                    storeMethod(clazzOrig, methods[i]);
                    s = s.substring(3);
                }
                else continue;
    
                String su = s.toUpperCase();
    
                if (vecUpper.contains(su) || methods[i].getReturnType().equals(Hub.class)) {
                    if (!vecFoundUpper.contains(su)) {
                        vecFound.addElement(s);
                        vecFoundUpper.add(su);
                    }
                }
                else {
                    vecUpper.addElement(su);
                }
            }
        }
        String[] ss = new String[vecFound.size()];
        vecFound.copyInto(ss);
        return ss;
    }
    
    
    // used by getOAObjectInfo to combine 2 OAObjectInfo's into one. 
    private static OAObjectInfo createCombinedObjectInfo(OAObjectInfo child, OAObjectInfo parent) {
        OAObjectInfo thisOI = new OAObjectInfo();
        
        OAClass oaclass = (OAClass) child.getForClass().getAnnotation(OAClass.class);
        if (oaclass == null) {
            oaclass = (OAClass) parent.getForClass().getAnnotation(OAClass.class);
        }
        
        if (oaclass != null) {
            thisOI.setUseDataSource(oaclass.useDataSource());
            thisOI.setLocalOnly(oaclass.localOnly());
            thisOI.setAddToCache(oaclass.addToCache());
            thisOI.setInitializeNewObjects(oaclass.initialize());
            thisOI.setDisplayName(oaclass.displayName());
        }
        
        
        
        // combine PropertyInfos
        List alThis = thisOI.getPropertyInfos();
        for (int x=0; x<2; x++) {
            ArrayList al;
            if (x == 0) al = child.getPropertyInfos();
            else al = parent.getPropertyInfos();

            for (int i=0; i<al.size(); i++)  {
                OAPropertyInfo pi = (OAPropertyInfo) al.get(i);
                
                for (int ii=0; ; ii++)  {
                    if (ii == alThis.size()) {
                        alThis.add(pi);
                        break;
                    }
                    OAPropertyInfo piThis = (OAPropertyInfo) alThis.get(ii);
                    if (pi.getName().equalsIgnoreCase(piThis.getName())) break;
                }
            }
        }
        
        // combined primitive properties
        ArrayList<String> alPrimitive = new ArrayList<String>();
        for (String s : parent.getPrimitiveProperties()) {
            alPrimitive.add(s);
        }
        for (String s : child.getPrimitiveProperties()) {
            alPrimitive.add(s);
        }
        Collections.sort(alPrimitive);
        thisOI.primitiveProps = new String[alPrimitive.size()];
        alPrimitive.toArray(thisOI.primitiveProps);
        
        
        // combine LinkInfos
        alThis = thisOI.getLinkInfos();
        for (int x=0; x<2; x++) {
            List al;
            if (x == 0) al = child.getLinkInfos();
            else al = parent.getLinkInfos();

            for (int i=0; i<al.size(); i++)  {
                OALinkInfo li = (OALinkInfo) al.get(i);
                for (int ii=0; ; ii++)  {
                    if (ii == alThis.size()) {
                        alThis.add(li);
                        break;
                    }
                    OALinkInfo liThis = (OALinkInfo) alThis.get(ii);
                    if (li.getName().equalsIgnoreCase(liThis.getName())) break;
                }
            }
        }
        
        // combine CalcInfos
        alThis = thisOI.getCalcInfos();
        for (int x=0; x<2; x++) {
            ArrayList al;
            if (x == 0) al = child.getCalcInfos();
            else al = parent.getCalcInfos();

            for (int i=0; i<al.size(); i++)  {
                OACalcInfo ci = (OACalcInfo) al.get(i);
                for (int ii=0; ; ii++)  {
                    if (ii == alThis.size()) {
                        alThis.add(ci);
                        break;
                    }
                    OACalcInfo ciThis = (OACalcInfo) alThis.get(ii);
                    if (ci.getName().equalsIgnoreCase(ciThis.getName())) break;
                }
            }
        }

        // 20120827
        String[] s1 = child.hubProps;
        String[] s2 = parent.hubProps;
        thisOI.hubProps = new String[s1.length + s2.length];
        System.arraycopy(s1, 0, thisOI.hubProps, 0, s1.length);
        System.arraycopy(s2, 0, thisOI.hubProps, s1.length, s2.length);
        
        return thisOI;
    }
    
    
    public static void addLinkInfo(OAObjectInfo thisOI, OALinkInfo li) {
        if (li == null) return;
        
        String name = li.getName();
        if (name != null && name.length() > 0) {  // see if it was already created
            List al = thisOI.getLinkInfos();
            for (int i=0; i<al.size(); i++) {
                OALinkInfo lix = (OALinkInfo) al.get(i);
                if (name.equalsIgnoreCase(lix.getName())) {
                    al.remove(i);
                    break;
                }
            }
        }       
        thisOI.addLinkInfo(li);
    }

    protected static void addCalcInfo(OAObjectInfo thisOI, OACalcInfo ci) {
        if (ci != null) thisOI.getCalcInfos().add(ci);
    }

    public static OACalcInfo getOACalcInfo(OAObjectInfo thisOI, String name) {
        if (thisOI == null || name == null) return null;
        for (OACalcInfo ci : thisOI.getCalcInfos()) {
            if (name.equalsIgnoreCase(ci.getName())) return ci;
        }
        return null;
    }
    
    /** 
        @return OALinkInfo for recursive link for this class, or null if not recursive
    */
    public static OALinkInfo getRecursiveLinkInfo(OAObjectInfo thisOI, int type) {
        boolean b = thisOI.bSetRecursive;
        try {
            return  _getRecursiveLinkInfo(thisOI, type);
        }
        finally {
            if (!b) thisOI.bSetRecursive = true;
        }
    }
    private static OALinkInfo _getRecursiveLinkInfo(OAObjectInfo thisOI, int type) {
        if (thisOI == null) return null;
        if (thisOI.bSetRecursive) {
            if (type == OALinkInfo.ONE) return thisOI.liRecursiveOne;
            else return thisOI.liRecursiveMany;
        }
        
        if (thisOI.thisClass == null) return null;
        List al = thisOI.getLinkInfos();

        for (int i=0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.bCalculated) continue;
            if (!li.bRecursive) continue; // 20131009
            if (li.toClass != null && li.toClass.equals(thisOI.thisClass)) {
                if (li.getType() == OALinkInfo.MANY) {
                    thisOI.liRecursiveMany = li;
                    break;
                }
                else thisOI.liRecursiveOne = li;
            }
        }

        if (thisOI.liRecursiveMany != null) {
            if (type == OALinkInfo.ONE) {
                if (thisOI.liRecursiveOne == null) {
                    thisOI.liRecursiveOne = getReverseLinkInfo(thisOI.liRecursiveMany);  // 20131010 type=One are not annotated as recursive
                }
                return thisOI.liRecursiveOne;
            }
            return thisOI.liRecursiveMany;
        }
        return null;
    }
    
    public static OALinkInfo getLinkToOwner(OAObjectInfo thisOI) {
        List al = thisOI.getLinkInfos();
        for (int i=0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            OALinkInfo liRev = getReverseLinkInfo(li);
            if (liRev != null && liRev.getOwner() && liRev.getType() == OALinkInfo.MANY) {
                if (!li.toClass.equals(thisOI.thisClass)) {  // make sure that it is not also a recursive link.
                    return li;
                }
            }
        }
        return null;
    }
    
    /**
        if this is a recursive object that does not have an owner, then the root hub can be set for all
        hubs of this class.  Throws and exception if this class has an owner.
    */
    public static void setRootHub(OAObjectInfo thisOI, Hub h) {
        if(thisOI == null) return;
        if (h == null) OAObjectHashDelegate.hashRootHub.remove(thisOI);
        else OAObjectHashDelegate.hashRootHub.put(thisOI, h);
    }
    public static Hub getRootHub(OAObjectInfo thisOI) {
        if(thisOI == null) return null;
        return (Hub) OAObjectHashDelegate.hashRootHub.get(thisOI);
    }


    /** 
        Used by OAObject.getHub() to cache hubs for links that have
        a weakreference only.  
    */
    public static boolean cacheHub(OALinkInfo li, final Hub hub) {
        if (li == null || hub == null || li.cacheSize < 1) return false;
        
        ReentrantReadWriteLock rwLock = OAObjectHashDelegate.hashLinkInfoCacheLock.get(li);
        ArrayList alCache = null;
        HashSet hsCache = null;

        if (rwLock == null) {
            synchronized(OAObjectHashDelegate.hashLinkInfoCacheLock) {
                rwLock = OAObjectHashDelegate.hashLinkInfoCacheLock.get(li);
                if (rwLock == null) {
                    rwLock = new ReentrantReadWriteLock();
                    OAObjectHashDelegate.hashLinkInfoCacheLock.put(li, rwLock);

                    boolean bIsServer = OASync.isServer(hub);
                    
                    alCache = new ArrayList(li.cacheSize * (bIsServer?10:1));
                    OAObjectHashDelegate.hashLinkInfoCacheArrayList.put(li, alCache);
                    hsCache = new HashSet(li.cacheSize * (bIsServer?10:1), .85f);
                    OAObjectHashDelegate.hashLinkInfoCacheHashSet.put(li, hsCache);
                }                
            }
        }
        if (alCache == null) {
            alCache = (ArrayList) OAObjectHashDelegate.hashLinkInfoCacheArrayList.get(li);
            hsCache = (HashSet) OAObjectHashDelegate.hashLinkInfoCacheHashSet.get(li);
        }
        
        try {
            rwLock.writeLock().lock();
            return _cacheHub(li, hub, alCache, hsCache);
        }
        finally {
            rwLock.writeLock().unlock();
        }
    }
    
    private static boolean _cacheHub(OALinkInfo li, Hub hub, ArrayList alCache, HashSet hsCache) {
        if (hsCache.contains(hub)) return false; 
        
        boolean bIsServer = OASync.isServer(hub);
        if (bIsServer) {
            // dont cache on server if there is not storage
            //   by returning false, it will not be stored as a weakRef
            if (!OAObjectInfoDelegate.getOAObjectInfo(li.getToClass()).getSupportsStorage()) return false;  
        }

        alCache.add(hub);
        hsCache.add(hub);
        
        int x = alCache.size();
        if (x > li.cacheSize) {
            if (!bIsServer || (x > li.cacheSize * 10)) {
                hsCache.remove(alCache.remove(0));
            }
        }
        return true;
    }    
    // for testing
    public static boolean isCached(OALinkInfo li, Hub hub) {
        if (li == null || hub == null) return false;
        ReentrantReadWriteLock rwLock = OAObjectHashDelegate.hashLinkInfoCacheLock.get(li);
        if (rwLock == null) return false;
        
        try {
            rwLock.readLock().lock();
            
            HashSet hs = (HashSet) OAObjectHashDelegate.hashLinkInfoCacheHashSet.get(li);
            return hs != null && hs.contains(hub);
        }
        finally {
            rwLock.readLock().unlock();
        }
    }
    
    
    public static OALinkInfo getReverseLinkInfo(OALinkInfo thisLi) {
        if (thisLi == null) return null;
        return thisLi.getReverseLinkInfo();
    }

    public static boolean isMany2Many(OALinkInfo thisLi) {
        OALinkInfo rli = getReverseLinkInfo(thisLi);
        return (rli != null && thisLi.type == OALinkInfo.MANY && rli.type == OALinkInfo.MANY);
    }

    public static boolean isOne2One(OALinkInfo thisLi) {
        OALinkInfo rli = getReverseLinkInfo(thisLi);
        return (rli != null && thisLi.type == OALinkInfo.ONE && rli.type == OALinkInfo.ONE);
    }

    public static Method getMethod(Class clazz, String methodName) {
        OAObjectInfo oi = getOAObjectInfo(clazz);  // this will load up the methods
        return getMethod(oi, methodName);
    }
    public static Method getMethod(OALinkInfo li) {
        if (li == null) return null;
        OALinkInfo liRev = getReverseLinkInfo(li);
        if (liRev == null) return null;
        
        OAObjectInfo oi = getOAObjectInfo(liRev.toClass);  // this will load up the methods
        return getMethod(oi, "get"+li.name, 0);
    }
    public static Method getMethod(OAObjectInfo oi, String methodName) {
        return getMethod(oi, methodName, -1);
    }
    public static Method getMethod(OAObjectInfo oi, String methodName, int argumentCount) {
        if (methodName == null || oi == null) return null;
        methodName = methodName.toUpperCase();
        Class clazz = oi.thisClass;
        Map<String, Method> map = OAObjectHashDelegate.getHashClassMethod(clazz);
        Method method = map.get(methodName);
        if (method != null && argumentCount < 0) {
            return method;
        }
        Set<String> set = OAObjectHashDelegate.getHashClassMethodNotFound(clazz);
        if (set.contains(methodName)) return null;
        
        boolean bRecalc = false;
        if (method != null && argumentCount >= 0) {
            Class[] cs = method.getParameterTypes();
            if (cs.length != argumentCount) {
                bRecalc = true;
                method = null;
            }
        }
        if (method == null) {
            method = OAReflect.getMethod(clazz, methodName, argumentCount);
            if (method == null) {
                if (!bRecalc) {
                    set.add(methodName);
                }
                return null;
            }
            method.setAccessible(true); // 20130131
            map.put(methodName, method);
        }
        return method;
    }
    
    protected static void storeMethod(Class clazz, Method method) {
        Map<String, Method> map = OAObjectHashDelegate.getHashClassMethod(clazz);
        method.setAccessible(true); // 20130131
        map.put(method.getName().toUpperCase(), method);
    }
    // testing
    public static Method[] getAllMethods(OAObjectInfo oi) {
        Class clazz = oi.thisClass;
        Map<String, Method> map = OAObjectHashDelegate.getHashClassMethod(clazz);
        Method[] ms = new Method[map.size()];  // qqqq, not threadsafe, method could be null
        int i = 0;
        for (String s : map.keySet()) {
            ms[i] = (Method) map.get(s);
        }
        return ms;
    }
    

    public static Class getPropertyClass(OAObjectInfo oi, String propertyName) {
        Method m = getMethod(oi, "get"+propertyName, 0);
        if (m == null) return null;
        return m.getReturnType();
    }
    
    public static Class getPropertyClass(Class clazz, String propertyName) {
        Method m = getMethod(clazz, "get"+propertyName);
        if (m == null) return null;
        return m.getReturnType();
    }
    
    
    public static Class getHubPropertyClass(Class clazz, String propertyName) {
        OALinkInfo li = getLinkInfo(clazz, propertyName);
        if (li != null) return li.toClass;
        return null;
    }
    
    
    public static OALinkInfo getLinkInfo(Class clazz, String propertyName) {
        OAObjectInfo oi = getOAObjectInfo(clazz);
        return getLinkInfo(oi, propertyName);
    }
    public static OALinkInfo getLinkInfo(OAObjectInfo oi, String propertyName) {
        OALinkInfo li = oi.getLinkInfo(propertyName);
        return li;
    }
    public static OALinkInfo[] getOwndedLinkInfos(OAObjectInfo oi) {
        return oi.getOwnedLinkInfos();
    }
    // linkinfo that this object owns
    public static OALinkInfo[] getOwndedLinkInfos(OAObject obj) {
        OAObjectInfo oi = getOAObjectInfo(obj);
        return oi.getOwnedLinkInfos();
    }

    /**
     * Find the linkInfo for a refererenc.
     * @param fromObject object to use to find the reference in.
     * @param hub reference object to find linkInfo for.
     * @return
     */
    public static OALinkInfo getLinkInfo(OAObjectInfo oi, OAObject fromObject, Hub hub) {
        List al = oi.getLinkInfos();
        for (int i=0; i<al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            String s = li.getName();
            
            Object objx = OAObjectReflectDelegate.getRawReference(fromObject, s);
            if (objx == hub) return li;
        }
        return null;
    }

    public static OALinkInfo getLinkInfo(Class fromClass, Class toClass) {
        OAObjectInfo oi = getOAObjectInfo(fromClass);
        return getLinkInfo(oi, toClass);
    }
    public static OALinkInfo getLinkInfo(OAObjectInfo oi, Class toClass) {
        List al = oi.getLinkInfos();
        for (int i=0; i<al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.getToClass().equals(toClass)) return li;
        }
        return null;
    }
    
    
    public static OAPropertyInfo getPropertyInfo(OAObjectInfo oi, String propertyName) {
        OAPropertyInfo pi = oi.getPropertyInfo(propertyName);
        return pi;
    }
    
    
    public static boolean isIdProperty(OAObjectInfo oi, String propertyName) {
        for (int i=0; oi.idProperties != null && i<oi.idProperties.length; i++) {
            if (oi.idProperties[i] != null && oi.idProperties[i].equalsIgnoreCase(propertyName)) return true;
        }
        return false;
    }
    
    public static boolean isPrimitive(OAPropertyInfo pi) {
        return (pi != null && pi.getClassType() != null && pi.getClassType().isPrimitive());
    }
    public static boolean isPrimitiveProperty(OAObjectInfo oi, String propertyName) {
        OAPropertyInfo pi = oi.getPropertyInfo(propertyName);
        if (pi != null) {
            Class c = pi.getClassType();
            return (c != null && c.isPrimitive());
        }
        return false;
    }

    public static boolean isHubProperty(OAObjectInfo oi, String propertyName) {
        Method m = getMethod(oi.thisClass, "get" + propertyName);
        return (m != null && m.getReturnType().equals(Hub.class));
    }

    
    public static Object[] getPropertyIdValues(OAObject oaObj) {
        if (oaObj == null) return null;
        OAObjectInfo oi = getOAObjectInfo(oaObj.getClass());
        String[] ids = oi.idProperties;
        Object[] objs = new Object[ids.length];
        for (int i=0; i<ids.length; i++) {
            objs[i] = OAObjectReflectDelegate.getProperty(oaObj, ids[i]);
        }
        return objs;
    }   

    public static boolean isPrimitiveNull(OAObject oaObj, String propertyName) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
        String[] ss = oi.getPrimitiveProperties();
        propertyName = propertyName.toUpperCase();
        for (int i=0; i<ss.length; i++) {
            int x = propertyName.compareTo(ss[i]);
            if (x == 0) {
                int pos = (i/8);
                if (pos >= oaObj.nulls.length) return false;
                byte b = oaObj.nulls[pos];
                int bit = i % 8;
                byte b2 = 1;
                b2 = (byte) (b2<<bit);
                b = (byte) ((byte)b & (byte)b2);
                return b != 0;                   
            }
            if (x < 0) break; // list is sorted
        }
        return false;
    }

    public static void setPrimitiveNull(OAObject oaObj, String propertyName, boolean bSetToNull) {
        if (oaObj == null || propertyName == null) return;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
        propertyName = propertyName.toUpperCase();
        String[] ss = oi.getPrimitiveProperties();
        for (int i=0; i<ss.length; i++) {
            int x = propertyName.compareTo(ss[i]);
            if (x == 0) {
                int pos = (i/8);
                byte b = oaObj.nulls[pos];
                int bit = i % 8;

                byte b2 = 1;
                b2 = (byte) (b2<<bit);
                if (bSetToNull) {
                    b |= b2; 
                }
                else {
                    b &= ~b2; 
                }
                oaObj.nulls[pos] = b;
                break;
            }
            if (x < 0) break; // list is sorted
        }
    }


    /**  20100930 I started this to use for reversing from  TreeNode to get path to top/root
     *         this wont work, unless the parent nodes are also used 
     * Take a property path that is "to" a class, and reverse it.
     * Example:  from a X class, the propPath "dept.manager.address.zipCode" where address.class would be the clazz;
     *    would return "manager.dept", used to get from an address to the dept.
     * @param clazz
     * @param propertyPath
     * @return
     */
    public static String reversePath(Class clazz, String propertyPath) {
        String revPropertyPath = "";
        StringTokenizer st = new StringTokenizer(propertyPath, ".");
        for (int i=0; st.hasMoreTokens(); i++) {
            String value = st.nextToken();
            if (i > 0)  revPropertyPath = "." + revPropertyPath;
            revPropertyPath = value + revPropertyPath;
        }   

        
        propertyPath = revPropertyPath;
        revPropertyPath = null;
        st = new StringTokenizer(propertyPath, ".");
        for (int i=0; st.hasMoreTokens(); i++) {
            String value = st.nextToken();
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);

            boolean bFound = false;
            for (OALinkInfo li : oi.getLinkInfos()) {
                OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
                if (value.equalsIgnoreCase(liRev.getName())) {
                    if (clazz.equals(liRev.getToClass())) {
                        if (revPropertyPath.length() > 0)  revPropertyPath = "." + revPropertyPath;
                        revPropertyPath = li.getName() + revPropertyPath;
                        clazz = li.getToClass();
                        bFound = true;
                        break;
                    }
                }
            }
            if (bFound) continue;

            if (i == 0) { // could be a property, which is discarded
                if (OAObjectInfoDelegate.getPropertyInfo(oi, value) != null) continue;
            }
            
            
            revPropertyPath = null;
            break;
        }   
        
        return revPropertyPath;
    }


    // 20141130 weakReferencealbe 

    /**
     * Returns true if any of the parent links has type=Many and cacheSize>0,
     * which means that this object can be GCd.
     */
    public static boolean isWeakReferenceable(OAObject oaObj) {
        if (oaObj == null) return false;
        OAObjectInfo oi = getObjectInfo(oaObj);
        return isWeakReferenceable(oi, null);
    }
    public static boolean isWeakReferenceable(OAObjectInfo oi) {
        if (oi == null) return false;
        return isWeakReferenceable(oi, null);
    }

    private static boolean isWeakReferenceable(OAObjectInfo oi, HashSet<OAObjectInfo> hsVisited) {
        if (oi == null) return false;
        if (oi.weakReferenceable != -1) return (oi.weakReferenceable==1);
        if (hsVisited != null && hsVisited.contains(oi)) return false;

        boolean b = false;
        for (OALinkInfo li : oi.getLinkInfos()) {
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev == null) continue;
            if (liRev.getType() != liRev.MANY) continue;
            if (liRev.cacheSize > 0) {
                b = true;
                break;
            }
            
            if (hsVisited == null) hsVisited = new HashSet<OAObjectInfo>();
            hsVisited.add(oi);
            OAObjectInfo oix = getObjectInfo(li.getToClass());
            b = isWeakReferenceable(oix, hsVisited);
            if (b) break;
        }
        oi.weakReferenceable = b ? 1 : 0;
        return b;
    }

}

