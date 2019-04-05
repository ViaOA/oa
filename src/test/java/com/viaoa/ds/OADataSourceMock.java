package com.viaoa.ds;

import java.util.Iterator;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectKey;
import com.viaoa.util.OAFilter;

import test.xice.tsam.model.oa.Server;

/**
 * OADataSource that works with tsam.Server
 * 
 * @author vvia
 *
 */
public class OADataSourceMock extends OADataSource {
    int maxSelect;
    
    /**
     * @param max number of objects that will be returned by select.
     */
    public OADataSourceMock(int max) {
        maxSelect = max;
    }
    
    @Override
    public boolean isClassSupported(Class clazz) {
        if (Server.class.equals(clazz)) return true;
        return false;
    }

    @Override
    public boolean isClassSupported(Class clazz, OAFilter filter) {
        return true;
    }

    @Override
    public boolean supportsStorage() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean b) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean getAllowIdChange() {
        return false;
    }

    @Override
    public void setAssignIdOnCreate(boolean b) {
    }

    @Override
    public boolean getAssignIdOnCreate() {
        return false;
    }

    @Override
    public boolean getSupportsPreCount() {
        return false;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void reopen(int pos) {
    }

    @Override
    public void assignId(OAObject object) {
    }

    @Override
    public boolean willCreatePropertyValue(OAObject object, String propertyName) {
        return false;
    }

    @Override
    public void save(OAObject obj) {
    }

    @Override
    public void update(OAObject object, String[] includeProperties, String[] excludeProperties) {
    }

    @Override
    public void update(OAObject obj) {
    }

    @Override
    public void insert(OAObject object) {
    }

    @Override
    public void insertWithoutReferences(OAObject obj) {
    }

    @Override
    public void delete(OAObject object) {
    }

    @Override
    public void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster) {
    }

    @Override
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, OAObject whereObject, String propertyFromWhereObject, String extraWhere, int max, OAFilter filter, boolean bDirty) {
        MyIterator it = new MyIterator();
        return it;
    }

    @Override
    public OADataSourceIterator selectPassthru(Class selectClass, String queryWhere, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return null;
    }

    @Override
    public Object execute(String command) {
        return null;
    }

    @Override
    public int count(Class selectClass, String queryWhere, Object[] params, OAObject whereObject, String propertyFromWhereObject, String extraWhere, int max) {
        return 0;
    }

    @Override
    public int countPassthru(Class selectClass, String queryWhere, int max) {
        return 0;
    }

    @Override
    public Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key, boolean bDirty) {
        return null;
    }

    @Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        return null;
    }

    @Override
    public int getMaxLength(Class c, String propertyName) {
        return 0;
    }

    class MyIterator implements OADataSourceIterator {
        int cnt;
        @Override
        public boolean hasNext() {
            return cnt < maxSelect;
        }

        @Override
        public Object next() {
            if (!hasNext()) return null;
            cnt++;
            return new Server();
        }

        @Override
        public void remove() {
        }

        @Override
        public String getQuery() {
            return null;
        }
        @Override
        public String getQuery2() {
            return null;
        }
    }
    
}
