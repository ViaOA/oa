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

    public T _next() {
        if (filter == null) return null;
        if (posFetchObjects >= alFetchObjects.size()) {
            posFetchObjects = 0;
            alFetchObjects.clear();
            if (bFetchIsDone) return null;
            lastFetchObject = (T) OAObjectCacheDelegate.fetch(clazz, lastFetchObject, 50, (ArrayList) alFetchObjects);
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
