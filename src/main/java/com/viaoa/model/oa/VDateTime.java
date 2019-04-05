package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADateTime;

@OAClass(
    shortName = "dt",
    displayName = "DateTime",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VDateTime extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VDateTime.class.getName());
    
    public static final String P_Value = "Value";
    
    private OADateTime value;
    
    @OAProperty(displayLength = 14)
    public OADateTime getValue() {
        return value;
    }
    public void setValue(OADateTime newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        OADateTime old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
