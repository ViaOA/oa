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
	public static void write(Hub thisHub, OAXMLWriter ow, boolean bKeyOnly, OACascade cascade) {
		write(thisHub, ow, bKeyOnly ? OAXMLWriter.WRITE_KEYONLY : OAXMLWriter.WRITE_YES, cascade);
	}
	// 2006/09/26
	public static void write(Hub thisHub, OAXMLWriter ow, int writeType, OACascade cascade) {
		boolean bKeyOnly = (writeType == OAXMLWriter.WRITE_KEYONLY || writeType == OAXMLWriter.WRITE_NONEW_KEYONLY);
	    ow.indent();
	    ow.println("<"+thisHub.getClass().getName()+" ObjectClass=\""+thisHub.getObjectClass().getName()+"\" total=\""+thisHub.getSize()+"\">");
	    ow.indent++;
	    for (int i=0; ;i++) {
	        Object obj = thisHub.elementAt(i);
	        if (obj == null) break;
	        if (writeType == OAXMLWriter.WRITE_NONEW_KEYONLY && obj instanceof OAObject) {
	        	if (((OAObject) obj).getNew()) continue;
	        }
	        if (obj instanceof OAObject) OAObjectXMLDelegate.write((OAObject)obj, ow, bKeyOnly, cascade);
	    }
	    ow.indent--;
	    ow.indent();
        ow.println("</"+thisHub.getClass().getName()+">");
    }
	
	
}
