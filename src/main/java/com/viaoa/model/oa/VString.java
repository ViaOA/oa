package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;

@OAClass(
    shortName = "string",
    displayName = "String",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VString extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VString.class.getName());
    
    public static final String P_Value = "Value";
    
    private String value;
    
    public VString() {
    }
    public VString(String s) {
        setValue(s);
    }
    
    @OAProperty(displayLength = 12)
    public String getValue() {
        return value;
    }
    public void setValue(String newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        String old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
