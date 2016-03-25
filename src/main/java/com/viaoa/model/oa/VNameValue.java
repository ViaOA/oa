package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;

@OAClass(
    shortName = "nv",
    displayName = "NameValue",
    displayProperty = "name",
    sortProperty = "value",
    localOnly = true
)
public class VNameValue extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VNameValue.class.getName());
    
    public static final String P_Name = "Name";
    public static final String P_Value = "Value";
    
    private String name;
    private String value;

    @OAProperty(displayLength = 12)
    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.value, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
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
