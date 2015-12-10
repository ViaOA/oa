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
package com.viaoa.ds.objectcache;

import java.util.*;

import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAFilter;

/**
 * Used to find and filter objects in OAObjectCache.
 * Note, all queries require that a non-null Filter be used.  If filter is null, then
 * no results will be returned.
 * @author vvia
 */
public class ObjectCacheIterator<T> implements Iterator {
    protected Class<T> clazz;
    protected OAFilter<T> filter;
    protected T nextObject, lastFetchObject;
    protected ArrayList<T> alFetchObjects = new ArrayList<T>(50);
    protected int posFetchObjects;
    protected boolean bFetchIsDone;
    protected int max;

    public ObjectCacheIterator(Class<T> c) {
        this.clazz = c;
    }
    public ObjectCacheIterator(Class<T> c, OAFilter<T> filter) {
        this.clazz = c;
        this.filter = filter;
    }
    public synchronized T next() {
        T obj;
        if (nextObject != null) {
            obj = nextObject;
            nextObject = null;
            return obj;
        }
        for (;;) {
            obj = _next();
            if (obj == null) break;
            if (filter == null || filter.isUsed(obj)) break;
        }
        return obj;
    }
    
//qqqqqqqqq not implemented     
    public void setMax(int x) {
        this.max = x;
    }
    public int getMax() {
        return max;
    }

    public T _next() {
        if (posFetchObjects >= alFetchObjects.size()) {
            posFetchObjects = 0;
            alFetchObjects.clear();
            if (bFetchIsDone) return null;
            lastFetchObject = (T) OAObjectCacheDelegate.find(lastFetchObject, clazz, filter, false, false, 50, (ArrayList) alFetchObjects);
            if (lastFetchObject == null) {
                bFetchIsDone = true;
                if (alFetchObjects.size() == 0) return null;
            }
        }
        T obj = alFetchObjects.get(posFetchObjects++);
        return obj;
    }
    
    public synchronized boolean hasNext() {
        if (nextObject == null) {
            nextObject = next();
        }
        return (nextObject != null);
    }
    
    public void remove() {
    }
}
