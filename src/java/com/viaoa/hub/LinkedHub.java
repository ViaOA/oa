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

/**
*/
public class LinkedHub<TYPE> extends Hub<TYPE> {

    /* this one is not a good idea - since the hub is not populated when created and it could need to be linked to hubTo.AO
    public LinkedHub(Class<TYPE> clazz, Hub<?> hubTo, String toPropertyName) {
    	super(clazz);
    	setLinkHub(hubTo, toPropertyName);
    }
    */
    public LinkedHub(Class<TYPE> clazz) {
        super(clazz);
    }
}

