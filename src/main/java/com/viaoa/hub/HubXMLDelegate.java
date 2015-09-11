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
import com.viaoa.util.*;


/**
 * Delegate for Hub XML functionality. 
 * @author vvia
 *
 */
public class HubXMLDelegate {

    /**
	    Called by OAXMLWriter to store all objects in xml file.
	*/
	public static void write(Hub thisHub, OAXMLWriter ow, final String tagName, boolean bKeyOnly, OACascade cascade) {
		write(thisHub, ow, tagName, bKeyOnly ? OAXMLWriter.WRITE_KEYONLY : OAXMLWriter.WRITE_YES, cascade);
	}

    public static void write(Hub thisHub, OAXMLWriter ow, final String tagName, int writeType, OACascade cascade) {
        if (thisHub == null || ow == null) return;
        try {
            if (tagName != null) ow.push(tagName);
            _write(thisHub, ow, tagName, writeType, cascade);
        }
        finally {
            if (tagName != null) ow.pop();
        }
    }
	
	// 2006/09/26
	private static void _write(Hub thisHub, OAXMLWriter ow, final String tagName, int writeType, OACascade cascade) {
	    boolean bKeyOnly = (writeType == OAXMLWriter.WRITE_KEYONLY || writeType == OAXMLWriter.WRITE_NONEW_KEYONLY);
	    ow.indent();
	    
        if (tagName == null) {
            ow.println("<Hub class=\""+ow.getClassName(thisHub.getObjectClass())+"\" total=\""+thisHub.getSize()+"\">");
            // ow.println("<"+ow.getClassName(Hub.class)+" ObjectClass=\""+ow.getClassName(thisHub.getObjectClass())+"\" total=\""+thisHub.getSize()+"\">");
        }
        else {
            ow.println("<"+tagName+" total=\""+thisHub.getSize()+"\">");
        }
	    
	    ow.indent++;
	    for (int i=0; ;i++) {
	        Object obj = thisHub.elementAt(i);
	        if (obj == null) break;
	        if (writeType == OAXMLWriter.WRITE_NONEW_KEYONLY && obj instanceof OAObject) {
	        	if (((OAObject) obj).getNew()) continue;
	        }
	        String name = thisHub.getObjectClass().getSimpleName();
	        if (obj instanceof OAObject) OAObjectXMLDelegate.write((OAObject)obj, ow, name, bKeyOnly, cascade);
	    }
	    ow.indent--;
	    ow.indent();
        if (tagName == null) {
            ow.println("</Hub>");
            // ow.println("</"+ow.getClassName(Hub.class)+">");
        }
        else ow.println("</"+tagName+">");
    }
	
	
}
