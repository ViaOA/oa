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
package com.viaoa.util;

/** 
    Object used internally to represent a number &lt; 0, or null 
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
    @Override
    public int hashCode() {
        return 1;
    }
}
