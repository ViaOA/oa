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
    This is used by Hub.getDetail() to create a (Detail) Hub that is automatically populated with the
    value from a property in the active object of the controlling (Master) Hub.  Whenever the Master
    Hub's active object is changed, the Detail Hub will automatically be updated.
    <p>
    If the value is a Hub, then the Detail Hub will share the same data.
    <p>
    Example:
    <pre>
    Hub hubDept = new Hub(Department.class);
    hubDept.select();  // select all existing dept objects.
    Hub hubEmp = hubDept.getDetail("Employees"); // Department has a method "Hub getEmployees()"
    // hubEmp will always have the Employee Objects for the active object in the hubDept.
    </pre>
    <p>
    Note: This does not get serialized with Hub.
    see Hub#getDetail
*/
class HubDetail implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization

    /** types of values. */
    public final static int ARRAY = 0;
    public final static int HUB = 1;
    public final static int OAOBJECT = 2;
    public final static int OBJECT = 3;
    public final static int OAOBJECTARRAY = 4;
    public final static int OBJECTARRAY = 5;
    public final static int HUBMERGER = 6;


    /** type of detail Hub, see static list above. */
    protected int type;

    protected String path; // added for use when using a HubMerger

    /**
        true if the property value is a Hub, and the detail hub should use the same active object
        as the Hub that it is sharing.  This is used to remember what object was active for the
        detail Hub.
    */
    protected boolean bShareActiveObject;

    /** number of references to this HubDetail. */
    protected int referenceCount;

    /** Information about the reference, from master to detail. */
    protected transient OALinkInfo liMasterToDetail;

    protected Hub hubMaster;
	protected Hub hubDetail;
    
    
    /**
        Used by Hub.getDetail() to create new Hub Detail.
        @param hub is master hub.
        @param linkInfo is from master object to detail property
        @param type of value in property.
    */
    public HubDetail(Hub hubMaster, Hub hubDetail, OALinkInfo liMasterToDetail, int type, String path) {
        this.hubMaster = hubMaster;
        this.hubDetail = hubDetail;
        this.liMasterToDetail = liMasterToDetail;
        this.type = type;
        this.referenceCount = 0;
        this.path = path;
        setup();
    }

    /**
	    Used by HubMerger
	*/
    HubDetail(String path, Hub hubDetail) {
        this.hubDetail = hubDetail;
        this.path = path;
        this.type = HUBMERGER;
        this.referenceCount = 0;
    }
    
    boolean bIgnoreUpdate;

    /** 20150119, 20160204
     *  this is for master.detail that are recursive, in cases where the detail hub could be
     *  pointing (shared) to a child hub, which leaves it disconnected from the masterHub.
     *  This is used by HubDetailDelegate.updateDetail(..), where it will be reconnected to the masterHub.
     */
    protected void setup() {
        if (hubMaster == null) return;
        if (hubDetail == null) return;
        if (liMasterToDetail == null) return;
        
        final OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(hubDetail.data.getObjectInfo(), OALinkInfo.ONE);
        if (liRecursive == null) return;
        if (liRecursive == liMasterToDetail) return;

        final OALinkInfo liDetailToMaster = liMasterToDetail.getReverseLinkInfo();
        if (liDetailToMaster == null) return;
        
        // 20150920 only if master is a one, not many
        if (liDetailToMaster.getType() != OALinkInfo.ONE) return;
        
        hubDetail.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                
                Object obj = e.getObject();
                if (!(obj instanceof OAObject)) return;

                Object parent = null;
                for (;;) {
                    Object objx = OAObjectReflectDelegate.getProperty((OAObject)obj, liDetailToMaster.getName());
                    if (objx == null) break;
                    parent = objx;
                    if (!(parent instanceof OAObject)) {
                        return; 
                    }
                    if (liDetailToMaster != liRecursive) {
                        break;
                    }
                    if (hubMaster.contains(parent)) break; 
                    obj = objx;
                }
                
                if (hubMaster.getAO() == parent) return;

                try {
                    bIgnoreUpdate = true;
                    hubMaster.setAO(parent);
                }
                finally {
                    bIgnoreUpdate = false;
                }
            }
        });
        
    }
}
	
