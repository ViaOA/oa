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
package com.viaoa.ds.finder;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.ds.*;
import com.viaoa.hub.Hub;

// 20140124 
/**
    Uses OAFinder to find objects.
    This will use OAObjectCache.selectAllHubs along with any
    OAObject.OAClass.rootTreePropertyPaths   ex: "[Router]."+Router.PROPERTY_UserLogins+"."+UserLogin.PROPERTY_User
    to find all of the objects available.
    
    This will return all of the objects, and does not use the query.  For now, it's
    expected that this will be called using an OASelect that has a Filter set.
*/
public class OAFinderDataSource extends OADataSource {
    private HashSet<Class> hashClasses = new HashSet<Class>();

//qqqqqqq use nextNubmer to assign IDs    
    
    public OAFinderDataSource() {
    }
    
    public void setAssignNumberOnCreate(boolean b) {
    }
    public boolean getAssignNumberOnCreate() {
        return true;
    }

    public boolean isAvailable() {
        return true;
    }

    public int getMaxLength(Class c, String propertyName) {
        return -1;
    }

    
    //NOTE: this needs to see if any of "clazz" superclasses are supported
    public boolean isClassSupported(Class clazz) {
        if (clazz == null) return false;
        if (hashClasses.contains(clazz)) return true;
        
        Hub h = OAObjectCacheDelegate.getSelectAllHub(clazz);
        if (h != null) {
            hashClasses.add(clazz);
            return true;
        }
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        String[] ss = oi.getRootTreePropertyPaths();
        if (ss != null) {
            for (String s : ss) {
                OAPropertyPath pp = new OAPropertyPath(clazz, s);
                h = OAObjectCacheDelegate.getSelectAllHub(pp.getFromClass());
                if (h != null) {
                    hashClasses.add(clazz);
                    return true;
                }
            }
        }
        return false;
    }
    
    public void insertWithoutReferences(OAObject obj) {
    }
    
    public void insert(OAObject obj) {
    }

    public @Override void update(OAObject obj, String[] includeProperties, String[] excludeProperties) {
    }

    public @Override void save(OAObject obj) {
    }

    public @Override void delete(OAObject obj) {
    }

    public @Override int count(Class clazz, String queryWhere, int max) {
        return -1;
    }

    public @Override int count(Class clazz, String queryWhere, Object param, int max) {
    	return count(clazz, queryWhere, new Object[] {param});
    }
    public @Override int count(Class clazz, String queryWhere, Object[] params, int max) {
        return -1;
    }

    public @Override int countPassthru(String query, int max) {
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, int max) {
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String propertyNameFromMaster, int max) {
        return -1;
    }

    /** does this dataSource support selecting/storing/deleting  */
    public @Override boolean supportsStorage() {
        return false;
    }

    public @Override Iterator select(Class clazz, String queryWhere, String queryOrder, int max) {
        return new MyIterator(clazz);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object param, String queryOrder, int max) {
    	return this.select(clazz, queryWhere, new Object[] {param}, queryOrder);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object[] params, String queryOrder, int max) {
        return new MyIterator(clazz);
    }

    public @Override Iterator selectPassthru(Class clazz, String query, int max) {
        return new MyIterator(clazz);
    }


    public @Override Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder,int max) {
        return new MyIterator(clazz);
    }

    public @Override Object execute(String command) {
        return null;
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max) {
        return new MyIterator(selectClass);
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder, int max) {
        return select(selectClass, whereObject, null, null, propertyNameFromMaster, queryOrder);
    }

    public @Override void initializeObject(OAObject obj) {
    }

    public @Override boolean willCreatePropertyValue(OAObject object, String propertyName) {
        return false;
    }

    /**
        Iterator Class that is used by select methods, works directly with OADataSource on OAServer.
    */
    class MyIterator implements Iterator {
        Class clazz;
        Hub hubSelectAll;
        int posSelectAll;
        
        // this is to track all OAClass.rootTreePropertyPaths
        OAFind[] finds;
        Hub[] findHubs;
        int posFinds;
        int posCurrentFindHubs;
        ArrayList<Object> alFindObjects;
        int posFindObjects;
        Object nextObject;

        public MyIterator(Class c) {
            this.clazz = c;
            if (clazz == null) return;
            hubSelectAll = OAObjectCacheDelegate.getSelectAllHub(clazz);
            if (hubSelectAll != null) return;

            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            String[] ss = oi.getRootTreePropertyPaths();
            
            if (ss == null) return;
                
            int x = ss.length;
            finds = new OAFind[x];
            findHubs = new Hub[x];
                
            for (int i=0; i<x ;i++) {
                String s = ss[i];
                OAPropertyPath pp = new OAPropertyPath(clazz, s);
                findHubs[i] = OAObjectCacheDelegate.getSelectAllHub(pp.getFromClass());
                finds[i] = new OAFind(pp.getPropertyPath());
            }
        }

        public synchronized Object next() {
            Object obj = null;
            
            // 0: see if hasNext has preloaded an obj
            if (nextObject != null) {
                obj = nextObject;
                nextObject = null;
                return obj;
            }
            
            // 1: check in selectAll hub
            if (hubSelectAll != null) {
                obj = hubSelectAll.getAt(posSelectAll++);
                return obj;
            }

            // 2: check to see if there are valid oaObject.rootTreePropertyPaths
            if (finds == null) return null;
            
            // 3: get from last find list results
            if (alFindObjects != null && posFindObjects < alFindObjects.size()) {
                obj = alFindObjects.get(posFindObjects++);
                return obj;
            }
            alFindObjects = null;

            // 4: 
            if (posFinds >= finds.length) return null;
            
            // 5: go to next rootHub object, and run another Find
            OAFind find = finds[posFinds];
            Hub h = findHubs[posFinds];
            if (find == null || h == null) {
                posCurrentFindHubs = 0;
                posFinds++;
                return next();
            }
            obj = h.getAt(posCurrentFindHubs++);
            if (obj == null) {
                posCurrentFindHubs = 0;
                posFinds++;
                return next();
            }
            
            alFindObjects = find.find((OAObject) obj);
            posFindObjects = 0;
            return next();
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

	public @Override void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
	}
	
	@Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        return null;
    }
}

