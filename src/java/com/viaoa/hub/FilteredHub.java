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
package com.viaoa.hub;

import java.util.Hashtable;

import com.viaoa.object.OAObject;


/**
 * A Hub that combines with a HubFilter.
*/
public abstract class FilteredHub<TYPE> extends Hub<TYPE> {
    
    private HubFilter filter;

    public FilteredHub(Hub<TYPE> hubMaster) {
        super(hubMaster.getObjectClass());
    
        filter = new HubFilter(hubMaster, this) {
            @Override
            public boolean isUsed(Object object) {
                return FilteredHub.this.isUsed((TYPE) object);
            }
        };
    }

    public HubFilter getFilter() {
        return filter;
    }
    
    public void addProperty(String prop) {
        filter.addProperty(prop);
    }
    public void addDependentProperty(OAObject obj, String prop) {
        filter.addDependentProperty(obj, prop);
    }
    public void addDependentProperty(Hub hub, String prop) {
        filter.addDependentProperty(hub, prop);
    }

    public void refresh() {
        getFilter().refresh();
    }
    
    protected abstract boolean isUsed(TYPE obj);
    
    
}

