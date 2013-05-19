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
