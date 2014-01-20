/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
 */
package com.viaoa.hub;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;

import com.viaoa.object.OACascade;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

public class OAFinder<F,T> implements OAFilter<T>{
    
    private Hub<F> hubFrom;
    private Hub<T> hubTo;
    private Class classTo;
    private String propPathNavTo, propPathMatch;
    private OAPropertyPath<?> propertyPathNavTo, propertyPathMatch;
    
    private OALinkInfo liRecursiveRoot;
    
    private OALinkInfo[] liNavTo;
    private OALinkInfo[] liNavToRecursive;
    private boolean bNavRequiresCasade;
    private OACascade navCascade;
    private OAFilter<F>[] navFilters;
    
    private OALinkInfo[] liMatch;
    private OALinkInfo[] liMatchRecursive;
    private Method[] liMatchMethods ;
    private boolean bMatchRequiresCasade;
    private OACascade matchCascade;
    private OAFilter<T>[] matchFilters;
    

    public OAFinder(Hub<F> hubFrom, String propPathNavTo, String propPathMatch) {
        this(hubFrom, propPathNavTo, propPathMatch, null);
    }    
    
    /**
     * Create finder to use 2 propertyPaths, one that will go from rootHub to
     * find objects that will be selected, and the second that is the path to the
     * value that will be compared. 
     * @param hubRoot hub to begin searching from
     * @param propertyPathNav path to find objects to then match
     * @param propertyPathMatch property path of value to match
     */
    public OAFinder(Hub<F> hubFrom, String propPathNavTo, String propPathMatch, Hub<T> hubTo) {
        if (hubFrom == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        this.hubFrom = hubFrom;
        this.hubTo = hubTo;
        this.propPathNavTo = propPathNavTo;
        this.propPathMatch = propPathMatch;
        
        propertyPathNavTo = new OAPropertyPath(hubFrom.getObjectClass(), propPathNavTo);
        
        liNavTo = propertyPathNavTo.getLinkInfos();
        liNavToRecursive = propertyPathNavTo.getRecursiveLinkInfos();

        Class[] cs = propertyPathNavTo.getClasses();
        if (OAString.isEmpty(propPathNavTo) || cs == null || cs.length == 0) classTo = hubFrom.getObjectClass();
        else classTo = cs[cs.length-1];
        if (hubTo != null&& !classTo.equals(hubTo.getObjectClass())) {
            throw new RuntimeException("hubTo is expected to be for class="+hubTo.getObjectClass()+", but class="+classTo);
        }
        propertyPathMatch = new OAPropertyPath(classTo, propPathMatch);
        liMatch = propertyPathMatch.getLinkInfos();
        liMatchRecursive = propertyPathMatch.getRecursiveLinkInfos();
        liMatchMethods =  propertyPathMatch.getMethods();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hubFrom.getObjectClass());
        liRecursiveRoot = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        
        bNavRequiresCasade = true;
        if (liNavTo != null) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : liNavTo) {
                if (hs.contains(li.getToClass())) {
                    bNavRequiresCasade = false;
                    break;
                }
                hs.add(li.getToClass());
            }
        }
        
        bMatchRequiresCasade = true;
        if (liMatch != null) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : liMatch) {
                if (hs.contains(li.getToClass())) {
                    bMatchRequiresCasade = false;
                    break;
                }
                hs.add(li.getToClass());
            }
        }

        // nav filters
        String[] names = propertyPathNavTo.getFilterNames();
        Object[] values = propertyPathNavTo.getFilterParamValues();
        Constructor[] constructors = propertyPathNavTo.getFilterConstructors();
        
        int x = names.length;
        navFilters = new OAFilter[x];
        for (int i=0; i<x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                navFilters[i] = hubFilter;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Filter "+names[i]+" can not be created", e);
            }
        }
        
        // match filters
        names = propertyPathMatch.getFilterNames();
        values = propertyPathMatch.getFilterParamValues();
        constructors = propertyPathMatch.getFilterConstructors();
        
        x = names.length;
        matchFilters = new OAFilter[x];
        for (int i=0; i<x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                matchFilters[i] = hubFilter;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Filter "+names[i]+" can not be created", e);
            }
        }
    }

    private boolean bFindFirst; 
    private boolean bReturnNow;  // this will be set when it should no longer keep searching for more matches
    private Object lastMatchValue;
    private int lastFoundPos;
    
    /**
     * Find the first object in hubFrom that matches.
     */
    public F findFirst(Object matchValue) {
        bFindFirst = true;
        F objectFound = null;
        lastMatchValue = matchValue;
        lastFoundPos = -1;
        if (hubTo != null) hubTo.clear();
        if (bNavRequiresCasade) navCascade = new OACascade();
        try {
            objectFound = _findTop(hubFrom, matchValue, 0);
        }
        finally {
            navCascade = null;
            bFindFirst = false;
            bReturnNow = false;
            if (objectFound == null) lastMatchValue = matchValue;
        }
        return objectFound;
    }    
    /**
     * Find the next object in hubFrom that matches.
     * Note: this will only work using the hubFrom.
     */
    public F findNext() {
        F objectFound = null;
        if (hubTo != null) hubTo.clear();
        try {
            if (lastFoundPos >= 0) {
                if (bNavRequiresCasade) navCascade = new OACascade();
                bFindFirst = true;
                objectFound = null;
                objectFound = _findTop(hubFrom, lastMatchValue, lastFoundPos+1);
                if (objectFound == null) lastMatchValue = null;
            }
        }
        finally {
            navCascade = null;
            bFindFirst = false;
            bReturnNow = false;
        }
        return objectFound;
    }    
    
    
    /**
     * This will clear the hubTo, and then populated it with objects that are found.
     * @param matchValue value to match with propPathMatch value.
     */
    public void find(Object matchValue) {
        lastMatchValue = null;
        lastFoundPos = -1;
        if (hubTo != null) hubTo.clear();
        if (bNavRequiresCasade) navCascade = new OACascade();
        try {
            _findTop(hubFrom, matchValue, 0);
        }
        finally {
            navCascade = null;
        }
    }

    private F _findTop(Hub<F> hub, Object matchValue, int startPos) {
        for (int i=startPos; ;i++) {
            F obj = hub.getAt(i);
            if (obj == null) break;
            _find(obj, matchValue, 0);
            if (bReturnNow) {
                lastFoundPos = i;
                return obj;
            }
            if (liRecursiveRoot != null) {
                Hub h = (Hub) liRecursiveRoot.getValue(obj);
                _findTop(h, matchValue, 0);
                if (bReturnNow) {
                    lastFoundPos = i;
                    return obj;
                }
            }
        }
        return null;
    }
    
    private void _find(Object obj, Object matchValue, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _find(objx, matchValue, pos);
            }
            return;
        }

        if (pos > 0 && navFilters[pos-1] != null) {
            if (!navFilters[pos-1].isUsed((F) obj)) return;
        }
        if (!(obj instanceof OAObject)) return;
        if (navCascade != null && navCascade.wasCascaded((OAObject)obj, true)) return;
        
        if (liNavTo == null || pos >= liNavTo.length) {
            onMatchValue( (T) obj, matchValue);
            if (bReturnNow) return;
        }
        
        // check if recursive
        if (pos > 0 && liNavToRecursive != null && pos <= liNavToRecursive.length) {
            if (liNavToRecursive[pos-1] != null) {
                Object objx = liNavToRecursive[pos-1].getValue(obj);
                _find(objx, matchValue, pos); // go up a level to then go through hub
            }
        }

        if (liNavTo != null && pos < liNavTo.length) {
            Object objx = liNavTo[pos].getValue(obj);
            _find(objx, matchValue, pos+1);
        }
    }

    /**
     * This will be called to create a filter that is in the propertyPaths.
     * @param name name of the filter in the propertyPath
     */
    protected HubFilter createHubFilter(String name) {
        return null;
    }

    
    
    /**
     * This is called when an object is found using the propPathNavTo.  
     * @param obj object object found in propPathNavTo
     */
    protected void onMatchValue(T obj, Object matchObj) {
        if (!isUsed(obj)) return;
       
        if (!OAString.isEmpty(propPathMatch)) {
            if (bMatchRequiresCasade) matchCascade = new OACascade();
        }
        try {
            _match(obj, obj, matchObj, 0);
        }
        finally {
            matchCascade = null;
        }
        
    }
    
    /**
     * This is called to determine if a found object should be used.
     * If true and there is a propPathMatch, then it will then compare those values.
     */
    @Override
    public boolean isUsed(T obj) {
        return true;
    }

    private void _match(T thisObj, Object obj, Object matchObj, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _match(thisObj, objx, matchObj, pos);
            }
            return;
        }

        if (pos > 0 && matchFilters[pos-1] != null) {
            if (!matchFilters[pos-1].isUsed((T)obj)) return;
        }

        if (liMatch == null || pos >= liMatch.length) {
            // see if last property in propertyPath is not link
            if (liMatchMethods.length > (liMatch==null?0:liMatch.length)) {
                try {
                    Object objx =  liMatchMethods[liMatchMethods.length-1].invoke(obj);
                    obj = objx;
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }
            
            // compare value
            if (!isEqual(obj, matchObj)) return;

            onMatchFound(thisObj);
            if (bReturnNow) return;
        }

        if (!(obj instanceof OAObject)) return;
        if (matchCascade != null && matchCascade.wasCascaded((OAObject)obj, true)) return;
        
        // check if recursive
        if (pos > 0 && liMatchRecursive != null && pos <= liMatchRecursive.length) {
            if (liMatchRecursive[pos-1] != null) {
                Object objx = liMatchRecursive[pos-1].getValue(obj);
                _match(thisObj, objx, matchObj, pos); // go up a level to then go through hub
            }
        }

        if (liMatch != null && pos < liMatch.length) {
            Object objx = liMatch[pos].getValue(obj);
            _match(thisObj, objx, matchObj, pos+1);
        }
    }
    
    protected boolean isEqual(Object value, Object matchValue) {
        if (value == matchValue) return true;
        if (value == null || matchValue == null) return false;

        if (value.equals(matchValue)) return true;

        Class c = matchValue.getClass();
        if (!c.equals(value.getClass())) {
            value = OAConverter.convert(c, value);
            if (value == null) return false;
            if (value.equals(matchValue)) return true;
        }
    
        if (value instanceof String) { // ignore case
            return ((String)value).equalsIgnoreCase((String)matchValue);
        }
        return false;
    }
    
    int cnter;    
    protected void onMatchFound(T obj) {
        System.out.println((++cnter)+") "+obj);
        if (bFindFirst) {
            bReturnNow = true;
        }
        if (hubTo != null) hubTo.add(obj);
    }
    
    public Hub<T> getToHub() {
        return hubTo;
    }
    public void setToHub(Hub<T> hub) {
        this.hubTo = hub;
    }
}

