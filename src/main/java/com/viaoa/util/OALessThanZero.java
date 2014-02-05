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
package com.viaoa.util;

/** 
    Object used internally to represent a number < 0, or null 
*/
public class OALessThanZero implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    public static final OALessThanZero instance = new OALessThanZero();
    
    private OALessThanZero() {
    }

    public OALessThanZero getGreaterThanZeroObject() {
        return instance;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return true;
        Number num = (Number) OAConv.convert(Number.class, obj, null);
        if (num == null) return false;
        return (num.doubleValue() < 0.0);
    }
}
