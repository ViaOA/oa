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

public class OAFinder<F,T> implements OAFilter<T>{
    private Hub<F> hubFrom;
    private Hub<T> hubTo;
    private OAPropertyPath<?> propertyPathNavTo, propertyPathMatch;
    
    private OALinkInfo liRecursiveRoot;
    
    private OALinkInfo[] liNavTo;
    private OALinkInfo[] liNavToRecursive;
    private boolean bNavRequiresCasade;
    private OACascade navCascade;
    private OAFilter[] navFilters;
    
    private OALinkInfo[] liMatch;
    private OALinkInfo[] liMatchRecursive;
    private Method[] liMatchMethods ;
    private boolean bMatchRequiresCasade;
    private OACascade matchCascade;
    private OAFilter[] matchFilters;
    

    /**
     * 
     * @param hubRoot hub to begin searching from
     * @param propertyPathNav path to find objects to then match
     * @param propertyPathMatch path of value to match
     */
    public OAFinder(Hub<F> hubFrom, String propPathNavTo, Hub<T> hubTo, String propPathMatch) {
        if (hubFrom == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        this.hubFrom = hubFrom;
        this.hubTo = hubTo;

        propertyPathNavTo = new OAPropertyPath(hubFrom.getObjectClass(), propPathNavTo);
        
        liNavTo = propertyPathNavTo.getLinkInfos();
        liNavToRecursive = propertyPathNavTo.getRecursiveLinkInfos();

        Class c;
        Class[] cs = propertyPathNavTo.getClasses();
        if (cs == null || cs.length == 0) c = hubFrom.getObjectClass();
        else c = cs[cs.length-1];
        if (!c.equals(hubTo.getObjectClass())) {
            throw new RuntimeException("hubTo is expected to be for class="+hubTo.getObjectClass()+", but class="+c);
        }
        propertyPathMatch = new OAPropertyPath(hubTo.getObjectClass(), propPathMatch);
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

    public void find(Object matchValue) {
        if (bNavRequiresCasade) navCascade = new OACascade();
        try {
            this._find(hubFrom, matchValue);
        }
        finally {
            navCascade = null;
        }
    }

    private void _find(Hub<F> hub, Object matchValue) {
        for (Object obj : hub) {
            _find(obj, matchValue, 0);
            if (liRecursiveRoot != null) {
                Hub h = (Hub) liRecursiveRoot.getValue(obj);
                _find(h, matchValue);
            }
        }
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
            if (!navFilters[pos-1].isUsed(obj)) return;
        }
        if (!(obj instanceof OAObject)) return;
        if (navCascade != null && navCascade.wasCascaded((OAObject)obj, true)) return;
        
        if (liNavTo == null || pos >= liNavTo.length) {
            onMatchValue( (T) obj, matchValue);
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
        
        if (bMatchRequiresCasade) matchCascade = new OACascade();
        try {
            _match(obj, obj, matchObj, 0);
        }
        finally {
            matchCascade = null;
        }
        
    }
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
            if (!matchFilters[pos-1].isUsed(obj)) return;
        }

        if (liMatch == null || pos >= liMatch.length) {
            if (pos > 0) {
                // see if last property in propertyPath is not link
                if (liMatchMethods.length > liMatch.length) {
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
            }
            onMatchFound(thisObj);
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
    }
}

