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
package com.viaoa.object;

import com.viaoa.hub.*;

public class OALogRecord extends OAObject {
    private static final long serialVersionUID = 1L;
   
    public static final String COMMAND_SAVE = "save";
    public static final String COMMAND_DELETE = "delete";
    
    private String command;
    private transient OAObject object;

    public OAObject getObject() {
        if (object == null) {
            object = (OAObject) getObject("object");
        }
        return object;
    }

    public void setObject(OAObject newObject) {
        OAObject old = getObject();
        this.object = newObject;
        firePropertyChange("object", old, object);
    }
    
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String newCommand) {
        String old = command;
        this.command = newCommand;
        firePropertyChange("command", old, command);
    }
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner, recursive)
        oaObjectInfo.addLinkInfo(new OALinkInfo("object", OAObject.class, OALinkInfo.ONE, false, false, "", true));
         
        oaObjectInfo.setAddToCache(false);
        oaObjectInfo.setInitializeNewObjects(false);
        oaObjectInfo.setLocalOnly(true);
        oaObjectInfo.setUseDataSource(false);
    }
}
