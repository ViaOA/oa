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

/**
 * Callback object for OAObjectSerializer
 * @author vincevia
 * @see OAObjectSerializer
 */
public abstract class OAObjectSerializerCallback {
    private OAObjectSerializer os;
    

    /** 
     * Called by OAObjectServializer
     */
    void setOAObjectSerializer(OAObjectSerializer os) {
        this.os = os;
    }
    
    
    protected void includeProperties(String... props) {
        if (os == null) return;
        os.includeProperties(props);
    }

    /*
    protected void excludeProperties(String[] props) {
        if (os == null) return;
        os.excludeProperties(props);
    }
    */
    
    protected void excludeProperties(String ... props) {
        if (os == null) return;
        os.excludeProperties(props);
    }
    protected void includeAllProperties() {
        if (os == null) return;
        os.includeAllProperties();
    }
    protected void excludeAllProperties() {
        if (os == null) return;
        os.excludeAllProperties();
    }
    protected int getStackSize() {
        if (os == null) return 0;
        return os.getStackSize();
    }
    protected Object getPreviousObject() {
        if (os == null) return null;
        return os.getPreviousObject();
    }
    protected Object getStackObject(int pos) {
        if (os == null) return null;
        return os.getStackObject(pos);
    }
    /**
     * first object is level 0
     * @return
     */
    public int getLevelsDeep() {
        if (os == null) return 0;
        return os.getLevelsDeep();
    }
    
    public boolean shouldSerializeReference(OAObject oaObj, String propertyName, Object obj, boolean bDefault) {
        return bDefault;
    }
    
    /**
     * Callback from OAObjectSerializer.  Use this method to include/exclude properties per object.
     */
    protected abstract void beforeSerialize(OAObject obj);
    // return IncludeProperties.DEFAULT;
    
    /**
     * Callback from OAObjectSerializer.
     */
    protected void afterSerialize(OAObject obj) {
    }
    
    
    public Object getReferenceValueToSend(Object obj) {
        return obj;
    }
}
