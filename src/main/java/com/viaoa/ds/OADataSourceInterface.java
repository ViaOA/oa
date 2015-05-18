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
package com.viaoa.ds;

import java.util.Iterator;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectKey;
import com.viaoa.util.OAFilter;

public interface OADataSourceInterface {
   
    boolean isClassSupported(Class clazz);
    boolean isClassSupported(Class clazz, OAFilter filter);
    boolean supportsStorage();
    boolean isAvailable();
    boolean getEnabled();
    void setEnabled(boolean b);
    boolean getAllowIdChange();
    void setAssignNumberOnCreate(boolean b);
    boolean getAssignNumberOnCreate();
    boolean getSupportsPreCount();
    boolean supportsInitializeObject();
    
    void close();
    void reopen(int pos);
    
    void initializeObject(OAObject object);
    boolean willCreatePropertyValue(OAObject object, String propertyName);
    
    void save(OAObject obj);
    void update(OAObject object, String[] includeProperties, String[] excludeProperties);
    void update(OAObject obj);
    void insert(OAObject object);
    void insertWithoutReferences(OAObject obj);
    void delete(OAObject object);
    void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster);
    
    
    /**
     * 
     * @param selectClass
     * @param queryWhere  where clause for selecting objects.
     * @param params  param values for "?" in the queryWhere.
     * @param queryOrder sort order
     * @param whereObject  master object to select from.
     * @param propertyFromMaster
     * @param extraWhere added to the query.
     * @param max 
     * @param filter this can be used if the datasource does not support a way to query for the results.
     * @param bDirty true if objects should be fully populated, even if they are already loaded (in cache, etc).
     * @return
     */
    Iterator select(Class selectClass, 
        String queryWhere, Object[] params, String queryOrder, 
        OAObject whereObject, String propertyFromMaster, String extraWhere, 
        int max, OAFilter filter, boolean bDirty
    );
    
    
    Iterator selectPassthru(Class selectClass, 
        String queryWhere, String queryOrder, 
        int max, OAFilter filter, boolean bDirty
    );
    
    
    Object execute(String command);
    
    int count(Class selectClass, 
        String queryWhere, Object[] params,   
        OAObject whereObject, String propertyFromMaster, String extraWhere, int max
    );

    int countPassthru(Class selectClass, 
        String queryWhere, int max  
    );
    
    Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key, boolean bDirty);
    
    byte[] getPropertyBlobValue(OAObject obj, String propertyName);

    int getMaxLength(Class c, String propertyName);
}
