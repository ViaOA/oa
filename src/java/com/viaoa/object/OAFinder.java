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
import java.util.HashSet;

import com.viaoa.hub.*;
import com.viaoa.object.OACascade;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

// 20140124
/**
 * This is used to find and then match objects using a root object or Hub and property paths that define
 * the objects to search for, and the property path to use on the selected objects to do a match.
 * 
 * @param <F> type of hub to use as the root (from)
 * @param <T> type of hub for the to class.
 */
public class OAFinder<F, T> implements OAFilter<T> {
    private F objectRoot;
    private Hub<F> hubRoot;
    private Hub<T> hubTo;
    private Class classTo;
    private String propPathTo, propPathMatch;
    private OAPropertyPath<?> propertyPathTo, propertyPathMatch;

    private OALinkInfo liRecursiveRoot;

    private OALinkInfo[] liTo;
    private OALinkInfo[] liToRecursive;
    private boolean bToRequiresCasade;
    private OACascade toCascade;
    private OAFilter<F>[] toFilters;

    private OALinkInfo[] liMatch;
    private OALinkInfo[] liMatchRecursive;
    private Method[] liMatchMethods;
    private boolean bMatchRequiresCasade;
    private OACascade matchCascade;
    private OAFilter<T>[] matchFilters;

    private boolean bFindFirst;
    private boolean bReturnNow; // this will be set when it should no longer keep searching for more
                                // matches
    private Object lastMatchValue;
    private int lastFoundPos; // in root hub
    private T lastFoundMatch;
    private boolean bHasMatchValue;

    public OAFinder() {
    }
    
    public void find(OAObject obj, String propertyPath) {
    }
    public void match(OAObject obj, String propertyPath, Object value) {
    }
    protected void onMatch(Object obj, Object matchValue) {
    }
    protected void onFoundX(Object obj) {
    }
    
    
    public OAFinder(Hub<F> hubRoot, String propPathTo, String propPathMatch) {
        this(null, hubRoot, null, propPathTo, propPathMatch, null, true);
    }

    public OAFinder(Hub<F> hubRoot, String propPathTo) {
        this(null, hubRoot, null, propPathTo, null, null, false);
    }

    public OAFinder(Hub<F> hubRoot, String propPathTo, Hub<T> hubTo) {
        this(null, hubRoot, null, propPathTo, null, hubTo, false);
    }

    /**
     * Create finder to use 2 propertyPaths, one that will go from rootHub to find objects that will be
     * selected, and the second that is the path to the value that will be compared.
     * 
     * @param hubRoot hub to begin searching from
     * @param propertyPathTo path to find objects to then match
     * @param propertyPathMatch property path of value to match
     */
    public OAFinder(Hub<F> hubRoot, String propPathTo, String propPathMatch, Hub<T> hubTo) {
        this(null, hubRoot, null, propPathTo, propPathMatch, hubTo, true);
    }

    private OAFinder(Class fromClass, Hub<F> hubRoot, F objectRoot, String propPathTo, String propPathMatch, Hub<T> hubTo, boolean bHasMatchValue) {
        this.objectRoot = objectRoot;
        this.hubRoot = hubRoot;
        this.hubTo = hubTo;
        this.propPathTo = propPathTo;
        this.propPathMatch = propPathMatch;
        this.bHasMatchValue = bHasMatchValue;

        Class c;
        if (objectRoot != null) c = objectRoot.getClass();
        else if (hubRoot != null) c = hubRoot.getObjectClass();
        else {
            if (fromClass == null) {
                throw new IllegalArgumentException("hubRootand objectRoot can not be null");
            }
            c = fromClass;
        }
        propertyPathTo = new OAPropertyPath(c, propPathTo);

        liTo = propertyPathTo.getLinkInfos();
        liToRecursive = propertyPathTo.getRecursiveLinkInfos();

        Class[] cs = propertyPathTo.getClasses();
        if (OAString.isEmpty(propPathTo) || cs == null || cs.length == 0) classTo = hubRoot.getObjectClass();
        else classTo = cs[cs.length - 1];
        if (hubTo != null && !classTo.equals(hubTo.getObjectClass())) {
            throw new RuntimeException("hubTo is expected to be for class=" + hubTo.getObjectClass() + ", but class=" + classTo);
        }
        propertyPathMatch = new OAPropertyPath(classTo, propPathMatch);
        liMatch = propertyPathMatch.getLinkInfos();
        liMatchRecursive = propertyPathMatch.getRecursiveLinkInfos();
        liMatchMethods = propertyPathMatch.getMethods();

        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hubRoot.getObjectClass());
        liRecursiveRoot = oi.getRecursiveLinkInfo(OALinkInfo.MANY);

        bToRequiresCasade = true;
        if (liTo != null) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : liTo) {
                if (hs.contains(li.getToClass())) {
                    bToRequiresCasade = false;
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

        // to filters
        String[] names = propertyPathTo.getFilterNames();
        Object[] values = propertyPathTo.getFilterParamValues();
        Constructor[] constructors = propertyPathTo.getFilterConstructors();

        int x = names.length;
        toFilters = new OAFilter[x];
        for (int i = 0; i < x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                toFilters[i] = hubFilter;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Filter " + names[i] + " can not be created", e);
            }
        }

        // match filters
        names = propertyPathMatch.getFilterNames();
        values = propertyPathMatch.getFilterParamValues();
        constructors = propertyPathMatch.getFilterConstructors();

        x = names.length;
        matchFilters = new OAFilter[x];
        for (int i = 0; i < x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                matchFilters[i] = hubFilter;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Filter " + names[i] + " can not be created", e);
            }
        }
    }

    /**
     * find first object in toHub that matches.
     */
    public T findFirst(Object matchValue) {
        bFindFirst = true;
        F objectFound = null;
        if (hubTo != null) hubTo.clear();
        if (bToRequiresCasade) toCascade = new OACascade();
        try {
            _findTop(hubRoot, matchValue, 0);
        }
        finally {
            toCascade = null;
            bFindFirst = false;
            bReturnNow = false;
            if (objectFound == null) lastMatchValue = matchValue;
        }
        T objx = lastFoundMatch;
        lastFoundMatch = null;
        return objx;
    }

    /**
     * Find the first object in hubRoot that matches.
     */
    public F findFirstRoot(Object matchValue) {
        bFindFirst = true;
        F objectFound = null;
        lastMatchValue = matchValue;
        lastFoundPos = -1;
        if (hubTo != null) hubTo.clear();
        if (bToRequiresCasade) toCascade = new OACascade();
        try {
            objectFound = _findTop(hubRoot, matchValue, 0);
        }
        finally {
            toCascade = null;
            bFindFirst = false;
            bReturnNow = false;
            if (objectFound == null) lastMatchValue = matchValue;
        }
        return objectFound;
    }

    /**
     * Find the next object in hubRoot that matches. Note: this will only work using the hubRoot.
     */
    public F findNextRoot() {
        F objectFound = null;
        if (hubTo != null) hubTo.clear();
        try {
            if (lastFoundPos >= 0) {
                if (bToRequiresCasade) toCascade = new OACascade();
                bFindFirst = true;
                objectFound = null;
                objectFound = _findTop(hubRoot, lastMatchValue, lastFoundPos + 1);
                if (objectFound == null) lastMatchValue = null;
            }
        }
        finally {
            toCascade = null;
            bFindFirst = false;
            bReturnNow = false;
        }
        return objectFound;
    }

    /**
     * This will clear the hubTo, and then populated it with objects that are found.
     * 
     * @param matchValue
     *            value to match with propPathMatch value.
     */
    public void find(Object matchValue) {
        lastMatchValue = null;
        lastFoundPos = -1;
        if (hubTo != null) hubTo.clear();
        if (bToRequiresCasade) toCascade = new OACascade();
        try {
            _findTop(hubRoot, matchValue, 0);
        }
        finally {
            toCascade = null;
        }
    }

    private F _findTop(Hub<F> hub, Object matchValue, int startPos) {
        for (int i = startPos;; i++) {
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

    static class StackValue {
        Object obj;
        int pos;
        boolean bUsingToPropPath;

        StackValue(Object obj, int pos, boolean bUsingToPropPath) {
            this.obj = obj;
            this.pos = pos;
            this.bUsingToPropPath = bUsingToPropPath;
        }
    }

    // keep stack
    private int stackPos;
    private StackValue[] stack = new StackValue[20];

    private void push(Object obj, int pos, boolean bUsingToPropPath) {
        StackValue sv = new StackValue(obj, pos, bUsingToPropPath);
        push(sv);
    }

    private void push(StackValue sv) {
        if (sv == null) return;
        int x = stack.length;
        if (stackPos == x) {
            StackValue[] temp = new StackValue[x + 20];
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

    public Object[] getStackObjects() {
        Object[] objs = new Object[stackPos];
        for (int i = 0; i < stackPos; i++) {
            objs[i] = stack[i].obj;
        }
        return objs;
    }

    public String[] getStackPropertyNames() {
        String[] ss = new String[stackPos];
        for (int i = 0; i < stackPos; i++) {
            String methodName;
            if (!stack[i].bUsingToPropPath) {
                if (stack[i].pos == 0) methodName = "[root]";
                else methodName = liTo[stack[i].pos - 1].getName();
            }
            else {
                if (stack[i].pos == 0) methodName = "[toObject]";
                else if (stack[i].pos <= liMatch.length) methodName = liMatch[stack[i].pos - 1].getName();
                else methodName = liMatchMethods[stack[i - 1].pos].getName();
            }
            ss[i] = methodName;
        }
        return ss;
    }

    private void _find(Object obj, Object matchValue, int pos) {
        try {
            push(obj, pos, false);
            _findx(obj, matchValue, pos);
        }
        finally {
            pop();
        }
    }

    private void _findx(Object obj, Object matchValue, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _find(objx, matchValue, pos);
            }
            return;
        }

        if (pos > 0 && toFilters[pos - 1] != null) {
            if (!toFilters[pos - 1].isUsed((F) obj)) return;
        }
        if (!(obj instanceof OAObject)) return;
        if (toCascade != null && toCascade.wasCascaded((OAObject) obj, true)) return;

        if (liTo == null || pos >= liTo.length) { // to object
            onMatchValue((T) obj, matchValue);
            if (bReturnNow) return;
        }

        // check if recursive
        if (pos > 0 && liToRecursive != null && pos <= liToRecursive.length) {
            if (liToRecursive[pos - 1] != null) {
                Object objx = liToRecursive[pos - 1].getValue(obj);
                _find(objx, matchValue, pos); // go up a level to then go through hub
            }
        }

        if (liTo != null && pos < liTo.length) {
            Object objx = liTo[pos].getValue(obj);
            _find(objx, matchValue, pos + 1);
        }
    }

    /**
     * This will be called to create a filter that is in the propertyPaths.
     * 
     * @param name
     *            name of the filter in the propertyPath
     */
    protected HubFilter createHubFilter(String name) {
        return null;
    }

    /**
     * This is called when an object is found using the propPathTo.
     * 
     * @param obj
     *            object object found in propPathTo
     */
    protected void onMatchValue(T obj, Object matchObj) {
        if (obj == null) return;
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
     * This is called to determine if a found object should be used. If true and there is a
     * propPathMatch, then it will then compare those values.
     */
    @Override
    public boolean isUsed(T obj) {
        return true;
    }

    private void _match(T thisObj, Object obj, Object matchObj, int pos) {
        boolean b = (pos != 0 || !(obj instanceof Hub));
        try {
            if (b) push(obj, pos, true);
            _matchx(thisObj, obj, matchObj, pos);
        }
        finally {
            if (b) pop();
        }
    }

    private void _matchx(T thisObj, Object obj, Object matchObj, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _match(thisObj, objx, matchObj, pos);
            }
            return;
        }

        if (pos > 0 && matchFilters[pos - 1] != null) {
            if (!matchFilters[pos - 1].isUsed((T) obj)) return;
        }

        if (liMatch == null || pos >= liMatch.length) {
            // see if last property in propertyPath is not link
            if (liMatchMethods.length > (liMatch == null ? 0 : liMatch.length)) {
                try {
                    Object objx = liMatchMethods[liMatchMethods.length - 1].invoke(obj);
                    obj = objx;
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }

            // compare value
            if (bHasMatchValue && !isEqual(obj, matchObj)) return;

            onFound(thisObj);
            if (bReturnNow) return;
        }

        if (!(obj instanceof OAObject)) return;
        if (matchCascade != null && matchCascade.wasCascaded((OAObject) obj, true)) return;

        // check if recursive
        if (pos > 0 && liMatchRecursive != null && pos <= liMatchRecursive.length) {
            if (liMatchRecursive[pos - 1] != null) {
                Object objx = liMatchRecursive[pos - 1].getValue(obj);
                _match(thisObj, objx, matchObj, pos); // go up a level to then go through hub
            }
        }

        if (liMatch != null && pos < liMatch.length) {
            Object objx = liMatch[pos].getValue(obj);
            _match(thisObj, objx, matchObj, pos + 1);
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
            return ((String) value).equalsIgnoreCase((String) matchValue);
        }
        return false;
    }

    int cnter;

    protected void onFound(T obj) {
        // System.out.println((++cnter)+") "+obj);
        if (bFindFirst) {
            bReturnNow = true;
            lastFoundMatch = obj;
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
