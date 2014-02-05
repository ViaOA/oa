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
package com.viaoa.util.converter;

import com.viaoa.util.*;

public class OAConverterArray implements OAConverterInterface {

    /**
     * Used to convert arrays into another value type.
     * @param value will always be an array.
     */
	public Object convert(Class clazz, Object value, String fmt) {
    	if (value != null && value.getClass().isArray() && value.getClass().equals(byte.class)) {
    		Object hold = value;
    		if (Number.class.isAssignableFrom(clazz) ) {
				value = new java.math.BigInteger((byte[]) value);
			}
			else if (java.util.Date.class.isAssignableFrom(clazz) || OADateTime.class.isAssignableFrom(clazz)) {
				value = new java.util.Date(new java.math.BigInteger((byte[]) value).longValue());
			}
			else if (clazz.equals(String.class) ) {
				value = new String((byte[]) value);
			}
    		if (value != hold) return OAConverter.convert(clazz, value, fmt);
    	}
        return value;
    }        

    

}

