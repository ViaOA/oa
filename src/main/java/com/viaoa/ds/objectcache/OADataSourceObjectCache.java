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

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThread;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;
import com.viaoa.util.filter.OAEqualFilter;
import com.viaoa.util.filter.OAQueryFilter;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.OADataSourceIterator;
import com.viaoa.ds.autonumber.OADataSourceAuto;

// 20140124 
/**
    Uses OAFinder to find objects.
    This will use OAObjectCache.selectAllHubs along with any
    OAObject.OAClass.rootTreePropertyPaths   ex: "[Router]."+Router.P_UserLogins+"."+UserLogin.P_User
    to find all of the objects available.

    subclassed to allow initializeObject(..) to auto assign Object Ids
*/
public class OADataSourceObjectCache extends OADataSourceAuto {

    public OADataSourceObjectCache() {
        this(true);
    }
    public OADataSourceObjectCache(boolean bRegister) {
        super(false);
        if (!bRegister) removeFromList();
    }

    @Override
    public OADataSourceIterator select(Class selectClass, 
        String queryWhere, Object[] params, String queryOrder, 
        OAObject whereObject, String propertyFromWhereObject, String extraWhere, 
        int max, OAFilter filter, boolean bDirty
    )
    {
        if (filter == null) {
            
            if (extraWhere != null) {
                try {
                    filter = new OAQueryFilter(selectClass, extraWhere, null);
                }
                catch (Exception e) {
                    throw new RuntimeException("query parsing failed", e);
                }
            }
            final OAFilter extraFilter = filter;
            
            if (!OAString.isEmpty(queryWhere)) {
                try {
                    filter = new OAQueryFilter(selectClass, queryWhere, params) {
                        public boolean isUsed(Object obj) {
                            boolean b = super.isUsed(obj);
                            if (b && extraFilter != null) {
                                b = extraFilter.isUsed(obj);
                            }
                            return b;
                        }
                    };  
                }
                catch (Exception e) {
                    throw new RuntimeException("query parsing failed", e);
                }
            }
            else if (whereObject != null || propertyFromWhereObject != null) {
                OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(whereObject.getClass());
                OALinkInfo li = oi.getLinkInfo(propertyFromWhereObject);
                if (li != null) li = li.getReverseLinkInfo();
                if (li != null) {
                    filter = new OAEqualFilter(li.getName(), whereObject) {
                        public boolean isUsed(Object obj) {
                            boolean b = super.isUsed(obj);
                            if (b && extraFilter != null) {
                                b = extraFilter.isUsed(obj);
                            }
                            return b;
                        }
                    };
                }
            }
            else {
                filter = new OAFilter() {
                    @Override
                    public boolean isUsed(Object obj) {
                        return true;
                    }
                };
            }
        }
        return new ObjectCacheIterator(selectClass, filter);
    }
    

    @Override
    public OADataSourceIterator selectPassthru(Class selectClass, 
        String queryWhere, String queryOrder, 
        int max, OAFilter filter, boolean bDirty
    )
    {
        if (!OAString.isEmpty(queryWhere)) {
            filter = new OAFilter() {
                @Override
                public boolean isUsed(Object obj) {
                    return false;
                }
            };
        }
        return new ObjectCacheIterator(selectClass, filter);
    }


    public @Override void assignId(OAObject obj) {
        super.assignId(obj);  // have autonumber handle this
    }
    
    public boolean getSupportsPreCount() {
        return false;
    }

    protected boolean isOtherDataSource() {
        OADataSource[] dss = OADataSource.getDataSources();
        return dss != null && dss.length > 1;
    }
    
    @Override
    public boolean isClassSupported(Class clazz, OAFilter filter) {
        if (filter == null) {
            if (isOtherDataSource()) return false;
            return super.isClassSupported(clazz, filter);
        }
        // only if all objects are loaded, or no other DS
        if (!isOtherDataSource()) return true;
        
        if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
            return true;
        }
        return false;
    }
}

