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
package com.viaoa.object;


public class OAEditException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private String property;
    private Object newValue;

    public OAEditException(OAObject obj, String property, Object newValue) {
        super("Invalid entry for "+property);
        this.property = property;
        this.newValue = newValue;
    }

    public OAEditException(OAObject obj, String property, long newValue) {
        this(obj, property, new Long(newValue));
    }

    public OAEditException(OAObject obj, String property, double newValue) {
        this(obj, property, new Double(newValue));
    }
    
    public OAEditException(OAObject obj, String property, boolean newValue) {
        this(obj, property, new Boolean(newValue));
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public String getProperty() {
        return property;
    }
}

