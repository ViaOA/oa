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
package com.viaoa.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import com.viaoa.hub.*;
import com.viaoa.util.*;

// 20140124
/**
 * This is used to find all values of a propertyPath. 
 *
 * @param <F> type of hub or OAObject to use as the root (from)
 * @param <T> type of hub for the to class.
 */
public class OAFinder<F extends OAObject, T> {
    private String strPropertyPath;
    private OAPropertyPath<T> propertyPath;

    private OALinkInfo liRecursiveRoot;

    private OALinkInfo[] linkInfos;
    private OALinkInfo[] recursiveLinkInfos;
    private Method[] methods;
    private boolean bRequiresCasade;
    private OACascade cascade;
    private OAFilter[] filters;

    private boolean bStop;
    private ArrayList<T> alFound;

    // optional filter that can be used on objects <T> that are found.
    private OAFilter<T> filter;
    // optional finder that can be used on objects <T> that are found.
    private OAFinder finder;    
    
    // stack
    private boolean bEnableStack;
    private int stackPos;
    private StackValue[] stack;

    private int maxFound;

    // if true, then values will need to be null to have onFound(..) called
    private boolean bEqualNull;  
    // if true, then values will need to be not null to have onFound(..) called 
    private boolean bEqualNotNull;
    // if set, then values need to be equal for onFound(..) called
    private T equalValue;
    
    private T betweenFromVal, betweenToVal;
    private T equalBetweenFromVal, equalBetweenToVal;
    private String likeValue;

    private F fromObject;
    private Hub<F> fromHub;

    private ArrayList<OAFilter> alFilters;
    
    public OAFinder() {
    }
    public OAFinder(String propPath) {
        this.strPropertyPath = propPath;
    }
    public OAFinder(F fromObject, String propPath) {
        this.fromObject = fromObject;
        this.strPropertyPath = propPath;
    }
    public OAFinder(Hub<F> fromHub, String propPath) {
        this.fromHub = fromHub;
        this.strPropertyPath = propPath;
    }
    /**
     * Add the found object to the list that is returned by find.
     * This can be overwritten to get all of the objects as they are found.
     * @see stop to be able to have the find stop searching.
     */
    protected void onFound(T obj) {
        alFound.add(obj);
        if (maxFound > 0 && alFound.size() >= maxFound) stop();
    }
    /**
     * This is used to stop the current find that is in process.
     * This can be used when overwriting the onFound().
     */
    public void stop() {
        bStop = true;
    }

    /**
     * Used for all objects that are found, to determine if the onFound(..) should be called.
     */
    public void setFilter(OAFilter<T> filter) {
        this.filter = filter;
    }
    public OAFilter<T> getFilter() {
        return this.filter;
    }
    
    /**
     * Used for all objects that are found, to perform another find to see if
     * that onFound(..) should be called for the object.
     */
    public void setFinder(OAFinder finder) {
        if (finder == this) throw new IllegalArgumentException("finder can not be itself");
        this.finder = finder;
    }
    public OAFinder getFinder() {
        return this.finder;
    }
    
    public void setMaxFound(int x) {
        this.maxFound = x;
    }
    public int getMaxFound() {
        return this.maxFound;
    }

    public ArrayList<T> find() {
        if (fromObject != null) return find(fromObject);
        if (fromHub != null) return find(fromHub);
        return null;
    }
    public void setRoot(F obj) {
        this.fromObject = obj;
    }
    public void setRoot(Hub<F> hub) {
        this.fromHub = hub;
    }
    
    /**
     * Given the propertyPath, find all of the objects from a Hub.
     */
    public ArrayList<T> find(Hub<F> hubRoot) {
        alFound = new ArrayList<T>();
        if (bEnableStack) stack = new StackValue[5];

        if (hubRoot == null) return alFound;

        bStop = false;
        setup(hubRoot.getObjectClass());
        
        for (F objectRoot : hubRoot) {
            stackPos = 0;
            performFind(objectRoot);
            if (bStop) break;
        }
        ArrayList<T> al = alFound;
        this.alFound = null;
        this.stack = null;
        this.stackPos = 0;
        return al;        
    }

    
    public void setLikeValue(String val) {
        this.likeValue = val;
    }
    public String getLikeValue() {
        return likeValue;
    }
    public void setEqualValue(T val) {
        this.equalValue = val;
    }
    public T getEqualValue() {
        return equalValue;
    }

    public void setEqualNull(boolean b) {
        this.bEqualNull = b;
        if (b) bEqualNotNull = false;
    }
    public boolean getEqualNull() {
        return this.bEqualNull;
    }
    public void setEqualNotNull(boolean b) {
        this.bEqualNotNull = b;
        if (b) bEqualNull = false;
    }
    public boolean getEqualNotNull() {
        return this.bEqualNotNull;
    }
    public void setBetweenValues(T val1, T val2) {
        this.betweenFromVal = val1;
        this.betweenToVal = val2;
    }
    public void setEqualOrBetweenValues(T val1, T val2) {
        this.equalBetweenFromVal = val1;
        this.equalBetweenToVal = val2;
    }
    
    public void clearFilters() {
        alFilters = null;
    }

    /**
     * Create a filter that is used on every object for this finder.
     * @param propPath property path from this Finder from object to the object that will be compared.
     * @param value value to compare with using OACompare.isEqual(..)
     */
    public void addEqualFilter(final String propPath, final Object value) {
        OAFilter<T> f;
        if (OAString.isEmpty(propPath)) {
            f = new OAFilter<T>() {
                @Override
                public boolean isUsed(T obj) {
                    return OACompare.isEqual(obj, value);
                }
            };
        }
        else {
            f = new OAFilter<T>() {
                OAFinder finder;
                public boolean isUsed(T obj) {
                    if (finder == null && obj instanceof OAObject) {
                        finder = new OAFinder(propPath);
                        finder.setEqualValue(value);
                    }
                    return finder.canFindFirst((OAObject)obj);
                }
            };
        }
        if (alFilters == null) alFilters = new ArrayList<OAFilter>();
        alFilters.add(f);
    }
    /**
     * Create a filter that is used on every object for this finder.
     * @param propPath property path from this Finder from object to the object that will be compared.
     * @param value value to compare with using OACompare.isLike(..).
     */
    public void addLikeFilter(final String propPath, final Object value) {
        OAFilter<T> f;
        if (OAString.isEmpty(propPath)) {
            f = new OAFilter<T>() {
                @Override
                public boolean isUsed(T obj) {
                    return OACompare.isLike(obj, value);
                }
            };
        }
        else {
            f = new OAFilter<T>() {
                OAFinder finder;
                public boolean isUsed(T obj) {
                    if (finder == null && obj instanceof OAObject) {
                        finder = new OAFinder(propPath);
                        String s = OAConv.toString(value);
                        finder.setLikeValue(s);
                    }
                    return finder.canFindFirst((OAObject)obj);
                }
            };
        }
        if (alFilters == null) alFilters = new ArrayList<OAFilter>();
        alFilters.add(f);
    }

    /**
     * Returns true if a matching value is found.
     */
    public boolean canFindFirst(F objectRoot) {
        int holdMax = getMaxFound();
        setMaxFound(1);
        ArrayList<T> al = find(objectRoot);
        if (getMaxFound() == 1) setMaxFound(holdMax);
        return (al.size() > 0);
    }

    /**
     *  Finds the first matching value.  If searching for a null, then this would return a null, so
     *  use the canFindFirst method instead.
     */
    public T findFirst(F objectRoot) {
        int holdMax = getMaxFound();
        setMaxFound(1);
        ArrayList<T> al = find(objectRoot);
        T obj;
        if (al.size() > 0) obj = al.get(0);
        else obj = null;
        if (getMaxFound() == 1) setMaxFound(holdMax);
        return obj;
    }
    public T findFirst(Hub<F> hub) {
        int holdMax = getMaxFound();
        setMaxFound(1);
        ArrayList<T> al = find(hub);
        T obj;
        if (al.size() > 0) obj = al.get(0);
        else obj = null;
        if (getMaxFound() == 1) setMaxFound(holdMax);
        return obj;
    }
    
    /**
     * Given the propertyPath, find all of the objects from a root object.
     * @param objectRoot starting object to begin navigating through the propertyPath.
     */
    public ArrayList<T> find(F objectRoot) {
        if (objectRoot == null) return null;
        alFound = new ArrayList<T>();
        if (bEnableStack) stack = new StackValue[5];
        stackPos = 0;

        if (objectRoot == null) return alFound;

        bStop = false;
        setup(objectRoot.getClass());
        performFind(objectRoot);
        ArrayList<T> al = alFound;
        this.alFound = null;
        this.stack = null;
        this.stackPos = 0;
        return al;        
    }
    
    private boolean bSetup;
    protected void setup(Class c) {
        if (bSetup) return;
        bSetup = true;
        if (propertyPath != null || c == null) return;
        propertyPath = new OAPropertyPath(c, strPropertyPath);
        
        linkInfos = propertyPath.getLinkInfos();
        recursiveLinkInfos = propertyPath.getRecursiveLinkInfos();
        methods = propertyPath.getMethods();

        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(c);
        liRecursiveRoot = oi.getRecursiveLinkInfo(OALinkInfo.MANY);

        bRequiresCasade = true;
        if (linkInfos != null && linkInfos.length > 0) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : linkInfos) {
                if (hs.contains(li.getToClass())) {
                    bRequiresCasade = false;
                    break;
                }
                hs.add(li.getToClass());
            }
        }

        // match filters
        String[] names = propertyPath.getFilterNames();
        Object[] values = propertyPath.getFilterParamValues();
        Constructor[] constructors = propertyPath.getFilterConstructors();

        int x = names.length;
        filters = new OAFilter[x];
        for (int i = 0; i < x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                filters[i] = hubFilter;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Filter " + names[i] + " can not be created", e);
            }
        }
    }
    
    private void performFind(F obj) {
        if (obj == null) return;

        if (bRequiresCasade) cascade = new OACascade();
        try {
            find(obj, 0);
        }
        finally {
            cascade = null;
        }
    }

    private void find(Object obj, int pos) {
        if (bStop) return;
        try {
            if (bEnableStack) push(obj, pos);
            _find(obj, pos);
        }
        finally {
            if (bEnableStack) pop();
        }
    }

    private void _find(Object obj, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                find(objx, pos);
                if (bStop) break;
            }
            return;
        }

        if (pos > 0 && filters != null && filters[pos - 1] != null) {
            if (!filters[pos - 1].isUsed(obj)) return;
        }

        if (linkInfos == null || pos >= linkInfos.length) {
            // see if last property in propertyPath is not link
            if (methods != null && (methods.length > (linkInfos == null ? 0 : linkInfos.length))) {
                try {
                    Object objx = methods[methods.length - 1].invoke(obj);
                    obj = objx;
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }
            
            boolean bIsUsed = isUsed((T) obj);
            OAFilter<T> fltr = getFilter();
            bIsUsed = bIsUsed && (fltr == null || fltr.isUsed((T) obj));
            if (bIsUsed && alFilters != null) {
                for (OAFilter f : alFilters) {
                    bIsUsed = f.isUsed((T) obj);
                    if (!bIsUsed) break;
                }
            }
            if (bIsUsed) {
                OAFinder finder = getFinder();
                ArrayList al = null;

                if (finder != null) {
                    if (obj instanceof OAObject) {
                        bIsUsed = (finder.findFirst((OAObject) obj) != null);
                    }
                    else if (obj instanceof Hub) {
                        bIsUsed = (finder.findFirst((Hub) obj) != null);
                    }
                }
                
                if (obj instanceof Hub) {
                    Hub h = (Hub) obj;
                    bIsUsed = (bIsUsed && (!bEqualNull || h == null || h.getSize() == 0));
                    bIsUsed = (bIsUsed && (!bEqualNotNull || (h != null && h.getSize() > 0)));
                    bIsUsed = (bIsUsed && (equalValue == null || OACompare.isIn(equalValue, h)));
                }
                else {
                    bIsUsed = (bIsUsed && (!bEqualNull || obj == null));
                    bIsUsed = (bIsUsed && (!bEqualNotNull || obj != null));
                    bIsUsed = (bIsUsed && (equalValue == null || OACompare.isEqual(equalValue, obj)));
                    
                    bIsUsed = (bIsUsed && ((betweenFromVal == null && betweenToVal == null) || OACompare.isBetween(obj, betweenFromVal, betweenToVal)));
                    bIsUsed = (bIsUsed && ((equalBetweenFromVal == null && equalBetweenToVal == null) || OACompare.isEqualOrBetween(obj, equalBetweenFromVal, equalBetweenToVal)));
                    bIsUsed = (bIsUsed && (likeValue == null || OACompare.isLike(equalValue, obj)));
                }
                if (bIsUsed) {
                    onFound((T) obj);
                }
            }
            if (bStop) return;
        }

        if (!(obj instanceof OAObject)) return;
        if (cascade != null && cascade.wasCascaded((OAObject) obj, true)) return;

        // check if recursive
        if (pos > 0 && recursiveLinkInfos != null && pos <= recursiveLinkInfos.length) {
            if (recursiveLinkInfos[pos - 1] != null) {
                Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
                find(objx, pos); // go up a level to then go through hub
                if (bStop) return;
            }
        }

        if (linkInfos != null && pos < linkInfos.length) {
            Object objx = linkInfos[pos].getValue(obj);
            find(objx, pos + 1);
            if (bStop) return;
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
     * Called for all objects that are found, and pass the filter.
     * @return true (default) to include in arrayList results, false to skip.
     */
    protected boolean isUsed(T obj) {
        return true;
    }

    /**
     * This will have the internal stack updated when a find is being performed.
     * @param b, default is false
     */
    public void setEnabledStack(boolean b) {
        bEnableStack = b;
    }
    
    // used to keep track of the objects in the stack
    static class StackValue {
        Object obj;
        int pos;
        StackValue(Object obj, int pos) {
            this.obj = obj;
            this.pos = pos;
        }
    }

    private void push(Object obj, int pos) {
        StackValue sv = new StackValue(obj, pos);
        push(sv);
    }

    private void push(StackValue sv) {
        if (sv == null) return;
        int x = stack.length;
        if (stackPos == x) {
            StackValue[] temp = new StackValue[x + 10];
            System.arraycopy(stack, 0, temp, 0, x);
            stack = temp;
        }
        stack[stackPos++] = sv;
    }

    private StackValue pop() {
        if (stackPos == 0) return null;
        StackValue sv = stack[--stackPos];
        stack[stackPos] = null;
        return sv;
    }

    /**
     * The objects that are in the current stack.  This can be used 
     * when overwriting the onFound(..) to know the object path.
     * @see #setEnabledStack(boolean) to enable this information.
     */
    public Object[] getStackObjects() {
        Object[] objs = new Object[stackPos];
        for (int i = 0; i < stackPos; i++) {
            objs[i] = stack[i].obj;
        }
        return objs;
    }

    /**
     * The property name of the objects that are in the current stack.
     * @see #setEnabledStack(boolean) to enable this information.
     */
    public String[] getStackPropertyNames() {
        String[] ss = new String[stackPos];
        for (int i = 0; i < stackPos; i++) {
            String methodName;
            if (stack[i].pos == 0) methodName = "[root]";
            else if (stack[i].pos <= linkInfos.length) methodName = linkInfos[stack[i].pos - 1].getName();
            else methodName = methods[stack[i - 1].pos].getName();
            ss[i] = methodName;
        }
        return ss;
    }
}
