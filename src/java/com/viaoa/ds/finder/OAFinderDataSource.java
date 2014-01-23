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
import com.viaoa.ds.*;
import com.viaoa.hub.Hub;

/**
    Uses OAFinder to find objects.
*/
public class OAFinderDataSource extends OADataSource {

//qqqqqqq use nextNubmer to assign IDs    
    
    public OAFinderDataSource() {
    }

    // add root level hubs
    public void addRoot(OAObject obj) {
//qqqqqqq create map of all hub names to support finding hub to go with tree map 
/*
 *     rootTreePropertyPaths = {
        "[Router]."+Router.PROPERTY_UserLogins+"."+UserLogin.PROPERTY_User
 */
    }

    private HashMap<Class, Hub> hashClass;
    
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
        return (hashClass.get(clazz) != null);
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
        Object obj = null;
        return new MyIterator(clazz, obj);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object param, String queryOrder, int max) {
    	return this.select(clazz, queryWhere, new Object[] {param}, queryOrder);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object[] params, String queryOrder, int max) {
        Object obj = null;
        return new MyIterator(clazz, obj);
    }

    public @Override Iterator selectPassthru(Class clazz, String query, int max) {
        Object obj = null;
        return new MyIterator(clazz, obj);
    }


    public @Override Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder,int max) {
        Object obj = null;
        return new MyIterator(clazz, obj);
    }

    public @Override Object execute(String command) {
        return null;
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max) {
        Object obj = null;
        return new MyIterator(selectClass, obj);
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
        Object id;
        Class clazz;

        public MyIterator(Class c, Object id) {
            this.clazz = c;
            this.id = id;
        }
        public MyIterator(OAObjectKey key) {
        }

        public synchronized boolean hasNext() {
            return false;
        }

        public synchronized Object next() {
            return null;
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

