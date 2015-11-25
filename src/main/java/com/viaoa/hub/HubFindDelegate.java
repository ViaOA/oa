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
package com.viaoa.hub;

import java.util.ArrayList;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.util.OACompare;
import com.viaoa.util.filter.OALikeFilter;

/**
 * Delegate used for the Find methods in Hub.
 * @author vvia
 *
 */
public class HubFindDelegate {
// 20140120 changed from HubFinder to OAFinder

	/**
	    Returns first object in Hub that is Like propertyPath findValue.
	    Returns null if not found.
	    @param bSetAO if true then the active object is set to the found object.
	    @see OAFinder#
	    @see OACompare#isLike(Object, Object)
	*/
    public static Object findFirst(Hub thisHub, String propertyPath, final Object findValue, final boolean bSetAO, OAObject lastFoundObject) {
        if (thisHub == null) return null;
        
        OAFinder finder = new OAFinder();
        finder.addFilter(new OALikeFilter(propertyPath, findValue));
        Object foundObj = finder.findNext(thisHub, (OAObject) lastFoundObject);
        
        if (bSetAO) thisHub.setAO(foundObj);
        return foundObj;
	}
}

