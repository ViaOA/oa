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
import com.viaoa.util.filter.*;

// 20140124
/**
 * This is used to find all values in a propertyPath. 
 *
 * @param <F> type of hub or OAObject to use as the root (from)
 * @param <T> type of hub for the to class (to).
 * 
 * example:
    // UserLogin
    OAFinder<Router, UserLogin> f = new OAFinder<Router, UserLogin>(Router.P_UserLogins);
    String cpp = OAString.cpp(UserLogin.P_User, User.P_UserId);
    f.addLikeFilter(cpp, userId);
    UserLogin userLogin = f.findFirst(router);
 * 
 */
public class OAFinder<F extends OAObject, T extends OAObject> {
    private String strPropertyPath;
    private OAPropertyPath<T> propertyPath;

    private OALinkInfo liRecursiveRoot;

    private OALinkInfo[] linkInfos;
    private OALinkInfo[] recursiveLinkInfos;
    private Method[] methods;
    private OAFilter[] filters;
    private boolean bRequiresCasade;
    private OACascade cascade;

    private boolean bStop;
    private ArrayList<T> alFound;

    
    // stack
    private boolean bEnableStack;
    private int stackPos;
    private StackValue[] stack;

    private int maxFound;

    private F fromObject;
    private Hub<F> fromHub;
    private boolean bUseAll;

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
        this(fromHub, propPath, true);
    }
    public OAFinder(Hub<F> fromHub, String propPath, boolean bUseAll) {
        this.fromHub = fromHub;
        this.strPropertyPath = propPath;
        this.bUseAll = bUseAll;
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
     * Used for all objects that are found, to perform another find to see if
     * that onFound(..) should be called for the object.
     */
/*    
    public void setFinder(OAFinder finder) {
        if (finder == this) throw new IllegalArgumentException("finder can not be itself");
        this.finder = finder;
    }
    public OAFinder getFinder() {
        return this.finder;
    }
*/    
    public void setMaxFound(int x) {
        this.maxFound = x;
    }
    public int getMaxFound() {
        return this.maxFound;
    }

    public ArrayList<T> find() {
        if (fromObject != null) return find(fromObject);
        if (fromHub != null) {
            if (bUseAll) return find(fromHub);
            F obj = fromHub.getAO();
            if (obj != null) return find(obj);
        }
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

/*    
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
*/    
    public void clearFilters() {
        alFilters = null;
    }

    public void addFilter(OAFilter<T> filter) {
        if (alFilters == null) alFilters = new ArrayList<OAFilter>();
        alFilters.add(filter);
    }
    public void addEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAEqualFilter(value));
    }
    public void addNotEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OANotEqualFilter(value));
    }
    /**
     * Create a filter that is used on every object for this finder.
     * @param propPath property path from this Finder from object to the object that will be compared.
     * @param value value to compare with using OACompare.isLike(..).
     */
    public void addLikeFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALikeFilter(value));
    }
    public void addNotLikeFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OANotLikeFilter(value));
    }
    public void addGreaterFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAGreaterFilter(value));
    }
    public void addGreaterOrEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAGreaterOrEqualFilter(value));
    }
    public void addLessFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALessFilter(value));
    }
    public void addLessOrEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALessOrEqualFilter(value));
    }
    public void addBetweenFilter(final String propPath, final Object value1, final Object value2) {
        _addFilter(propPath, new OABetweenFilter(value1, value2));
    }
    
    
    /**
     * Create a filter that is used on every object for this finder.
     * @param propPath property path from this Finder from object to the object that will be compared.
     */
    private void _addFilter(final String propPath, final OAFilter filter) {
        if (filter == null) return;
        OAFilter<T> f;
        if (OAString.isEmpty(propPath)) {
            f = filter;
        }
        else if (OAString.dcount(propPath, '.') == 1) {
            f = new OAFilter<T>() {
                @Override
                public boolean isUsed(T obj) {
                    if (obj == null) return false;
                    Object objx = obj.getProperty(propPath);
                    return filter.isUsed(objx);
                }
            };
        }
        else {
            int dcnt = OAString.dcount(propPath, '.');
            final OAFinder<T, OAObject> find = new OAFinder<T, OAObject>(OAString.field(propPath, '.', 1, dcnt-1));
            String prop = OAString.field(propPath, '.', dcnt);
            find._addFilter(prop, filter);
            
            f = new OAFilter<T>() {
                public boolean isUsed(T obj) {
                    return find.canFindFirst(obj);
                }
            };
        }
        addFilter(f);
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
            if (bIsUsed && alFilters != null) {
                for (OAFilter f : alFilters) {
                    bIsUsed = f.isUsed((T) obj);
                    if (!bIsUsed) break;
                }
            }
            if (bIsUsed) {
                onFound((T) obj);
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
    
    public void setEqualValue(T val) {
        addEqualFilter(null, val);
    }
    public void setNotEqualValue(T val) {
        addNotEqualFilter(null, val);
    }
    public void setLikeValue(T val) {
        addLikeFilter(null, val);
    }
    public void setNotLikeValue(T val) {
        addNotLikeFilter(null, val);
    }
    public void setGreaterValue(T val) {
        addGreaterFilter(null, val);
    }
    public void setGreaterOrEqualValue(T val) {
        addGreaterOrEqualFilter(null, val);
    }
    public void setLessValue(T val) {
        addLessFilter(null, val);
    }
    public void setLessOrEqualValue(T val) {
        addLessOrEqualFilter(null, val);
    }
}
