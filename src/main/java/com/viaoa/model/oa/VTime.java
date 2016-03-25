package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;
import com.viaoa.util.OATime;

@OAClass(
    shortName = "time",
    displayName = "Time",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VTime extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VTime.class.getName());
    
    public static final String P_Value = "Value";
    
    private OATime value;
    
    @OAProperty(displayLength = 14)
    public OATime getValue() {
        return value;
    }
    public void setValue(OATime newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        OATime old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
