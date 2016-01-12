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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.*;

// 20140124
/**
 * This is used to find all values from one OAObject/Hub to another OAObject/Hub, using a propertyPath.
 * Support is included to include Filters.
 *
 * @param <F> type of hub or OAObject to use as the root (from)
 * @param <T> type of hub for the to class (to).
 * 
 * example:
    // from Router, find all UserLogin for a userId
    OAFinder<Router, UserLogin> f = new OAFinder<Router, UserLogin>(Router.P_UserLogins);
    String cpp = UserLoginPP.user().userId().pp;
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

    private boolean bAddOrFilter;
    private boolean bAddAndFilter;
    private OAFilter filter;
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
        return find(hubRoot, null);
    }

    public ArrayList<T> find(ArrayList<F> alRoot) {
        return find(alRoot, null);
    }

    public ArrayList<T> find(ArrayList<F> alRoot, F objectLastUsed) {
        alFound = new ArrayList<T>();
        if (bEnableStack) stack = new StackValue[5];

        if (alRoot == null) return alFound;
        int x = alRoot.size();
        if (x == 0) return alFound;
        
        F sample = alRoot.get(0);
        
        bStop = false;
        setup(sample.getClass());
        
        int pos;
        if (objectLastUsed == null) pos = 0;
        else pos = alRoot.indexOf(objectLastUsed) + 1;
        
        for ( ; pos<x ;pos++) {
            F objectRoot = alRoot.get(pos);
            if (objectRoot == null) continue;
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
    
    
    /**
     * Given the propertyPath, find all of the objects from a Hub,
     * starting after objectLastFound
     */
    public ArrayList<T> find(Hub<F> hubRoot, F objectLastUsed) {
        alFound = new ArrayList<T>();
        if (bEnableStack) stack = new StackValue[5];

        if (hubRoot == null) return alFound;

        bStop = false;
        setup(hubRoot.getObjectClass());
        
        int pos;
        if (objectLastUsed == null) pos = 0;
        else pos = hubRoot.getPos(objectLastUsed) + 1;
        
        for ( ; ;pos++) {
            F objectRoot = hubRoot.getAt(pos);
            if (objectRoot == null) break;
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

    
    public void clearFilters() {
        filter = null;
    }

    public void addFilter(OAFilter<T> filter) {
        if (this.filter == null) this.filter = filter;
        else {
            if (bAddOrFilter) this.filter = new OAOrFilter(this.filter, filter);
            else this.filter = new OAAndFilter(this.filter, filter);
        }
        bAddAndFilter = bAddOrFilter = false;
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

    
    public T findNext(Hub<F> hub, F objectLastUsed) {
        int holdMax = getMaxFound();
        setMaxFound(1);
        ArrayList<T> al = find(hub, objectLastUsed);
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
        
        if (linkInfos.length != methods.length) {
            // oafinder is to get from one OAObj/Hub to another, not a property/etc
            throw new RuntimeException("propertyPath "+strPropertyPath+" must end in an OAObject/Hub");
        }
        

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

        for (int i = 0; i < x; i++) {
            if (constructors[i] == null) continue;
            try {
                HubFilter hubFilter = createHubFilter(names[i]);
                if (hubFilter == null) hubFilter = ((CustomHubFilter) constructors[i].newInstance(values[i])).getHubFilter();
                if (filter == null) filter = hubFilter;
                else filter = new OAAndFilter(filter, hubFilter);
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
        if (obj==null || bStop) return;
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
        
        if (linkInfos == null || pos >= linkInfos.length) {
            boolean bIsUsed;
            if (filter != null) bIsUsed = filter.isUsed(obj);
            else bIsUsed = true;
            bIsUsed = bIsUsed && isUsed((T) obj);

            if (bIsUsed) {
                onFound((T) obj);
            }
            if (bStop) return;
        }

        if (!(obj instanceof OAObject)) return;
        if (cascade != null && cascade.wasCascaded((OAObject) obj, true)) return;

        // check if recursive
        if (pos == 0) {
            // 20151026 see if root object is recursive
            if (liRecursiveRoot != null) {
                Object objx = liRecursiveRoot.getValue(obj);
                find(objx, pos); // go up a level to then go through hub
                if (bStop) return;
            }
        }
        else if (recursiveLinkInfos != null && pos <= recursiveLinkInfos.length) {
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

    public void addBetweenFilter(String pp, Object val1, Object val2) {
        addFilter(new OABetweenFilter(pp, val1, val2));
    }
    public void addBetweenOrEqualFilter(String pp, Object val1, Object val2) {
        addFilter(new OABetweenOrEqualFilter(pp, val1, val2));
    }
    public void addEmptyFilter(String pp) {
        addFilter(new OAEmptyFilter(pp));
    }
    public void addNotEmptyFilter(String pp) {
        addFilter(new OANotEmptyFilter(pp));
    }
    public void addEqualFilter(String pp, Object val) {
        addFilter(new OAEqualFilter(pp, val));
    }
    public void addEqualFilter(String pp, Object val, boolean bIgnoreCase) {
        addFilter(new OAEqualFilter(pp, val, bIgnoreCase));
    }
    public void addGreaterFilter(String pp, Object val) {
        addFilter(new OAGreaterFilter(pp, val));
    }
    public void addGreaterOrEqualFilter(String pp, Object val) {
        addFilter(new OAGreaterOrEqualFilter(pp, val));
    }
    public void addLessFilter(String pp, Object val) {
        addFilter(new OALessFilter(pp, val));
    }
    public void addLessOrEqualFilter(String pp, Object val) {
        addFilter(new OALessOrEqualFilter(pp, val));
    }
    public void addLikeFilter(String pp, Object val) {
        addFilter(new OALikeFilter(pp, val));
    }
    public void addNotLikeFilter(String pp, Object val) {
        addFilter(new OANotLikeFilter(pp, val));
    }
  
    
    /**
     * This will create an Or with the existing filter and the next filter that is added.
     */
    public void addOrFilter() {
        bAddOrFilter = true;
        bAddAndFilter = false;
    }
    /**
     * This will create an And with the existing filter and the next filter that is added.
     */
    public void addAndFilter() {
        bAddAndFilter = true;
        bAddOrFilter = false;
    }
    
}
