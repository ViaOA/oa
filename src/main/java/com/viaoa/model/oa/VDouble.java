package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;

@OAClass(
    shortName = "double",
    displayName = "Double",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VDouble extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VDouble.class.getName());
    
    public static final String P_Value = "Value";
    
    private double value;
    
    @OAProperty(displayLength = 3)
    public double getValue() {
        return value;
    }
    public void setValue(double newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        double old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
