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
 * Used to have two hubs share the same AO.
 */
public class HubShareAO extends HubListenerAdapter {
	private Hub hub1;
	private Hub hub2;

	/**
	 * @param bOneWayOnly if true, then hub1.ao change will update hub2.ao.  If false then both will set the others ao.
	 */
    public HubShareAO(Hub hub1, Hub hub2, boolean bOneWayOnly) {
        this.hub1 = hub1;
        this.hub2 = hub2;

        hub1.addHubListener(this);
        if (!bOneWayOnly) hub2.addHubListener(this);
    }
	
	public HubShareAO(Hub hub1, Hub hub2) {
	    this(hub1, hub2, false);
	}

    @Override
    public void afterChangeActiveObject(HubEvent evt) {
        if (HubShareDelegate.isUsingSameSharedAO(hub1, hub2)) {
            return;
        }
        Hub h = evt.getHub();
        Object obj = h.getAO();
        if (h == hub1) hub2.setAO(obj);
        else if (h == hub2) hub1.setAO(obj);
    }
	
	public void close() {
        hub1.removeHubListener(this);
        hub2.removeHubListener(this);
	}
	
	public Hub getHub1() {
	    return hub1;
	}
    public Hub getHub2() {
        return hub2;
    }
}
