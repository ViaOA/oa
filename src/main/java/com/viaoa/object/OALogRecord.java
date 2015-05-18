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
