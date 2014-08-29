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
