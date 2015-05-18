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


import com.viaoa.object.*;


/**
 * Delegate used for Hub save functionality.
 * @author vvia
 *
 */
public class HubSaveDelegate {

	// verified: not called by code that already has a OACascade
    public static void saveAll(Hub thisHub, int cascadeRule) {
        OACascade cascade = new OACascade(); 
        HubSaveDelegate.saveAll(thisHub, cascadeRule, cascade);
    }
	
    /**
     * Note: setting iCascadeRule to OAObject.CASCADE_NONE will not save the objects, but will update the M2M links.
     */
    public static void saveAll(Hub thisHub, int iCascadeRule, OACascade cascade) {
        if (thisHub == null) return; //qq need to log this
        if (cascade.wasCascaded(thisHub, true)) return;

        boolean bM2M = false;
        if (iCascadeRule != OAObject.CASCADE_NONE) {
	        boolean b = thisHub.isOAObject();
	        int x = thisHub.getCurrentSize(); // only check the objects that are loaded
	        for (int i=0; i<x ; i++) {
	            Object obj = thisHub.elementAt(i);
	            if (obj == null) break;
	            if (b) {
	            	OAObjectSaveDelegate.save((OAObject)obj, iCascadeRule, cascade);
	            }
	            else {
	            	// OAObjectDSDelegate.save(obj, true);  // true=insert.  Could be update?
	            	//todo: qqqqqqqq 
	            }
	        }
        }
        else {
	        // if Many2Many, then save all Added objects that are New, so that a valid DB record exists before calling updateHubAddsAndRemoves()
			HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
	        bM2M = dm.liDetailToMaster != null && OAObjectInfoDelegate.isMany2Many(dm.liDetailToMaster);
	        
	        if (bM2M) {
		        Object[] objAdds = HubDataDelegate.getAddedObjects(thisHub);
	        	for (int i=0; objAdds!=null && i<objAdds.length; i++) {
	        		Object obj = objAdds[i];
	        		if (obj instanceof OAObject && ((OAObject)obj).getNew()) {
			            OAObjectSaveDelegate._saveObjectOnly((OAObject) obj, cascade);
	        		}
	        	}
	        }
        }
        
    	HubDelegate._updateHubAddsAndRemoves(thisHub, cascade);
    	thisHub.setChanged(false); // removes all vecAdd, vecRemove objects
    	
        HubDelegate.setReferenceable(thisHub, false);
    }

	
	
}



