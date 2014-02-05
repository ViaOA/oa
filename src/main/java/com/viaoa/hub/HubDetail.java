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
    @see Hub#getDetail
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
    protected OALinkInfo liMasterToDetail;

	/**
	   Dont use WeakReference to the masterHub,
	   since if the master was garbage collected, it would cause
	   problems with the detail hub.
	*/
	protected Hub masterHub;
    
    
    /**
        Used by Hub.getDetail() to create new Hub Detail.
        @param hub is master hub.
        @param linkInfo is from master object to detail property
        @param type of value in property.
    */
    public HubDetail(Hub hub, OALinkInfo liMasterToDetail, int type, String path) {
        this.masterHub = hub;
        this.liMasterToDetail = liMasterToDetail;
        this.type = type;
        this.referenceCount = 0;
        this.path = path;
    }

    /**
	    Used by HubMerger
	*/
    HubDetail(String path, Hub hub) {
        this.masterHub = hub;
        this.path = path;
        this.type = HUBMERGER;
        this.referenceCount = 0;
    }
}
	
