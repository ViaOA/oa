package com.viaoa.model.oa;


import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADate;

@OAClass(
    shortName = "date",
    displayName = "Date",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VDate extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VDate.class.getName());
    
    public static final String P_Value = "Value";
    
    private OADate value;
    
    @OAProperty(displayLength = 8)
    public OADate getValue() {
        return value;
    }
    public void setValue(OADate newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        OADate old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
