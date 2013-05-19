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
