package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;

@OAClass(
    shortName = "long",
    displayName = "Long",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VLong extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VLong.class.getName());
    
    public static final String P_Value = "Value";
    
    private long value;
    
    @OAProperty(displayLength = 3)
    public long getValue() {
        return value;
    }
    public void setValue(long newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        long old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
