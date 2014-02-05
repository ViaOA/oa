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
	Internally used by Hub
	that is used to know the owner object of this Hub.  The owner is the object that
	was used to get this Hub.  If this Hub was created by using getDetail(), then
	the MasterHub is set.  When creating a shared Hub, this object will also be
	used for shared Hub.
	<p>
	Example: a Hub of Employee Objects can "come" from a Department Object by calling
	department.getEmployees() method.  For this, the masterObject for the employee Hub will
	be set to the Department Object.
*/
class HubDataMaster implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization
	
	/** Only used for a Detail Hub, created by Hub.getDetail() */
	protected transient Hub masterHub;
	
	/** The object that Hub "belongs" to. */
	protected OAObject masterObject;
	
	/** LinkInfo from Detail (MANY) to Master (ONE).  */
	protected OALinkInfo liDetailToMaster;  // Note: Dont make transient: it will get replaced in resolveObject, but needs the old one to find the match
}

