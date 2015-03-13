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
package com.viaoa.ds.objectcache;

import java.util.*;
import com.viaoa.object.*;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;
import com.viaoa.ds.OADataSource;
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
    public Iterator select(Class selectClass, 
        String queryWhere, Object[] params, String queryOrder, 
        OAObject whereObject, String propertyFromMaster, String extraWhere, 
        int max, OAFilter filter, boolean bDirty
    )
    {
        if (filter == null) {
            if (!OAString.isEmpty(queryWhere) || whereObject != null || propertyFromMaster != null || extraWhere != null) {
                filter = new OAFilter() {
                    @Override
                    public boolean isUsed(Object obj) {
                        return false;
                    }
                };
            }
        }
        return new ObjectCacheIterator(selectClass, filter);
    }
    

    @Override
    public Iterator selectPassthru(Class selectClass, 
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


    public @Override void initializeObject(OAObject obj) {
        super.initializeObject(obj);  // have autonumber handle this
    }
    
    public boolean getSupportsPreCount() {
        return false;
    }
    @Override
    public boolean supportsInitializeObject() {
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

