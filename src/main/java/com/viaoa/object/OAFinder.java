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
 * Support is included for Filters.
 *
 * @param <F> type of hub or OAObject to use as the root (from)
 * @param <T> type of hub for the to class (to).
 * 
 * examples:<code>
    // from Router, find all UserLogin for a userId
    OAFinder<Router, UserLogin> f = new OAFinder<Router, UserLogin>(Router.P_UserLogins);
    String cpp = UserLoginPP.user().userId().pp;
    f.addLikeFilter(cpp, userId);
    UserLogin userLogin = f.findFirst(router);
    
    OAFinder<Program, Employee> f = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp) {
        protected void onFound(Employee obj) {
            //todo: 
        }
    };
    f.find(program);
    
    
    </code>
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
    private OACascade[] cascades;

    private volatile boolean bStop;
    private ArrayList<T> alFound;

    // stack
    private boolean bEnableStack;
    
    
    private int stackPos;
    private StackValue[] stack;
    private int maxFound;
    private F fromObject;
    private Hub<F> fromHub;
    private boolean bUseAll;
    private boolean bEnableRecursiveRoot;
    
    /**
     * flag to know if it should only find data that is currently in memory.
     */
    private boolean bUseOnlyLoadedData;

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
        bEnableRecursiveRoot = fromHub != null && fromHub.getMasterObject() != null;
    }

    public void setAllowRecursiveRoot(boolean b) {
        this.bEnableRecursiveRoot = b;
    }
    /**
     * Flag to know if the root object/hub should allow for recursive duing the find.  
     * Default is determined by the following.  Default is false, unless hub is the root and it has a masterObject.
     */
    public boolean getAllowRecursiveRoot() {
        return this.bEnableRecursiveRoot;
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
     * Called during a find when data was not found.
     * Use stop() to have the find aborted.
     */
    protected void onDataNotFound() {
    }
    
    /**
     * This is used to stop the current find that is in process.
     * This can be used when overwriting the onFound().
     */
    public void stop() {
        bStop = true;
    }

    public void setUseOnlyLoadedData(boolean b) {
        this.bUseOnlyLoadedData = b;
    }

    // 20160306
    /**
     * Flag (default=false) to only use data that is already in memory and not to load from server or datasource.
     */
    public boolean getUseOnlyLoadedData() {
        return bUseOnlyLoadedData;
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
        ArrayList<T> al = find(hubRoot, null);
        return al;
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
    
    // 20171224 update threadloc.getDetail
    public ArrayList<T> find(Hub<F> hubRoot, F objectLastUsed) {
        ArrayList<T> al = null;
        Hub dh = null;
        String dpp = null;
        try {
            dh = OAThreadLocalDelegate.getGetDetailHub();
            if (dh != null) {
                dpp = OAThreadLocalDelegate.getGetDetailPropertyPath();
                OAThreadLocalDelegate.setGetDetailHub(hubRoot, strPropertyPath);
            }
            al = _find(hubRoot, objectLastUsed);
        }
        finally {
            if (dh != null) OAThreadLocalDelegate.resetGetDetailHub(dh, dpp);
        }
        return al;
    }
    
    /**
     * Given the propertyPath, find all of the objects from a Hub,
     * starting after objectLastFound
     */
    protected ArrayList<T> _find(Hub<F> hubRoot, F objectLastUsed) {
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
        return (al != null && al.size() > 0);
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
        if (al != null && al.size() > 0) obj = al.get(0);
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
    
    public OAPropertyPath getPropertyPath() {
        return this.propertyPath;
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
        
        if (liRecursiveRoot != null && linkInfos != null && linkInfos.length > 0) {
            if (linkInfos[0].getType() == OALinkInfo.ONE && linkInfos[0].getReverseLinkInfo().getType() == OALinkInfo.MANY && !linkInfos[0].getReverseLinkInfo().getRecursive()) liRecursiveRoot = null; 
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

        if (linkInfos != null && linkInfos.length > 0) {
            cascades = new OACascade[linkInfos.length];
            for (int i=0; i<linkInfos.length; i++) {
                cascades[i] = new OACascade();
            }
        }
        try {
            find(obj, 0);
        }
        finally {
            cascades = null;
        }
    }

    
    private void find(Object obj, int pos) {
        if (obj==null || bStop) return;
        if (pos > 20) {
            return;
        }
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

        if (!(obj instanceof OAObject)) return;

        if (pos > 0 && cascades != null) {
            if (cascades[pos-1].wasCascaded((OAObject) obj, true)) return;
        }
        
        if (linkInfos == null || pos >= linkInfos.length) {
            boolean bIsUsed;
            if (filter != null && !bStop) bIsUsed = filter.isUsed(obj);
            else bIsUsed = true;
            bIsUsed = !bStop && bIsUsed && isUsed((T) obj);

            if (bIsUsed) {
                onFound((T) obj);
            }
            if (bStop) return;
        }

        // check if recursive
        if (pos == 0) {
            // see if root object is recursive
            if (bEnableRecursiveRoot && liRecursiveRoot != null) {
                if (getUseOnlyLoadedData() && !liRecursiveRoot.isLoaded(obj)) {
                    onDataNotFound();
                }
                else {
                    Object objx = liRecursiveRoot.getValue(obj);
                    find(objx, pos); // go up a level to then go through hub
                }
                if (bStop) return;
            }
        }
        else if (recursiveLinkInfos != null && pos <= recursiveLinkInfos.length) {
            if (recursiveLinkInfos[pos - 1] != null) {
                if (getUseOnlyLoadedData() && !recursiveLinkInfos[pos - 1].isLoaded(obj)) {
                    onDataNotFound();
                }
                else {
                    Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
                    find(objx, pos);
                }
                if (bStop) return;
            }
        }

        if (linkInfos != null && pos < linkInfos.length) {
            if (getUseOnlyLoadedData() && !linkInfos[pos].isLoaded(obj)) {
                onDataNotFound();
                return;
            }
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
    public void addEqualFilter(String pp, Object matchValue, boolean bIgnoreCase) {
        OAEqualFilter f = new OAEqualFilter(pp, matchValue);
        f.setIgnoreCase(bIgnoreCase);
        addFilter(f);
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

    public void addOrFilter(OAFilter f1, OAFilter f2) {
        OAOrFilter f = new OAOrFilter(f1, f2);
        addFilter(f);
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
