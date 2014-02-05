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
import com.viaoa.util.*;


/**
 * Delegate used by Hub for JSON functionality.
 * @author vvia
 *
 */
public class HubJsonDelegate {

    /**
	    Called by OAJsonWriter to store all objects in JSON file.
	*/
	public static void write(Hub thisHub, OAJsonWriter ow, OACascade cascade) {
        ow.println("[");
	    ow.indent++;
        ow.indent();
	    for (int i=0; ;i++) {
	        Object obj = thisHub.elementAt(i);
	        if (obj == null) break;
	        
            if (i>0) {
                ow.println(",");
                ow.indent();
            }
	        
	        if (obj instanceof OAObject) OAObjectJsonDelegate.write((OAObject)obj, ow, false, cascade);
	    }
        ow.println("");
	    ow.indent--;
	    ow.indent();
        ow.print("]");
    }
}
