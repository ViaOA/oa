package com.viaoa.object;

import static com.viaoa.object.OAObjectSerializer.*;

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
    
    
    protected abstract void setup(OAObject obj);
    // return IncludeProperties.DEFAULT;
}
