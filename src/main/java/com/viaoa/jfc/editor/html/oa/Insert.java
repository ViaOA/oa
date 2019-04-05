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
package com.viaoa.jfc.editor.html.oa;


import com.viaoa.annotation.OAClass;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;

/**
 * OAObject used for InsertDialog.
 * @author vvia
 *
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class Insert extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String P_Type = "Type";
    public static final String P_Location = "Location";
    
    protected int type;
    public static final int TYPE_BR = 0;
    public static final int TYPE_DIV = 1;
    public static final int TYPE_P = 2;
    
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("Break");
        hubType.addElement("Div");
        hubType.addElement("Paragraph");
    }

    protected int location;
    public static final int LOCATION_Inside = 0;
    public static final int LOCATION_Before = 1;
    public static final int LOCATION_After = 2;
    
    public static final Hub<String> hubLocation;
    static {
        hubLocation = new Hub<String>(String.class);
        hubLocation.addElement("Inside");
        hubLocation.addElement("Before");
        hubLocation.addElement("After");
    }
    
    public int getType() {
        return type;
    }
    public void setType(int newValue) {
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    
    public int getLocation() {
        return location;
    }
    public void setLocation(int newValue) {
        int old = location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    
}
