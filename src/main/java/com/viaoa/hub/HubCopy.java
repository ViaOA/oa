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

/**
 * Used to have two hubs use the same objects, so that the ordering can be different.
 */
public class HubCopy<TYPE> extends HubFilter {

	public HubCopy(Hub<TYPE> hubMaster, Hub<TYPE> hubCopy, boolean bShareAO) {
	    super(hubMaster, hubCopy, bShareAO);
	}
	// if object is directly removed from filtered hub, then remove from hubMaster
	@Override
	protected void afterRemoveFromFilteredHub(Object obj) {
	    if (hubMaster != null && hubMaster.contains(obj)) {
	        hubMaster.remove(obj);
	    }
	}
	
	@Override
	public boolean isUsed(Object object) {
	    return true;
	}
}
