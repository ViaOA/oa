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

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAReflect;

public class OAFinder {
    private String path; 
    private Hub<?> hubRoot;
    private OAPropertyPath<?> oaPropPath;

    public OAFinder(Hub<?> hubRoot, String propertyPath) {
        if (hubRoot == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        this.hubRoot = hubRoot;
        this.path = propertyPath;
        oaPropPath = new OAPropertyPath(path);

        oaPropPath = new OAPropertyPath(path);
        try {
            oaPropPath.setup(hubRoot.getObjectClass());
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cant find property for PropertyPath=\"" + path + "\" starting with Class " + hubRoot.getObjectClass().getName(), e);
        }
        methods = oaPropPath.getMethods();
    }
    private Method[] methods;
    
    public void find(Hub hubRoot, Object findObj) {
        for (Object obj : hubRoot) {
            find(obj, 0);
        }
    qqqqqqqqqqq    
    }
    protected void find(Object obj, int pos) {
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                find(objx, pos+1);
                //qqqqqqq check if recursive
            }
            return;
        }
        Object objx = OAReflect.getPropertyValue(obj, methods[pos]);

        
        if (objx != null) {
            find(objx, pos+1);
        }
    }
    

}







