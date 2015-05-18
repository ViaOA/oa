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

