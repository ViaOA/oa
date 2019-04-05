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
