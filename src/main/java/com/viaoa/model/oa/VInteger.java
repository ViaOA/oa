package com.viaoa.model.oa;

import java.util.logging.Logger;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;

@OAClass(
    shortName = "int",
    displayName = "Integer",
    displayProperty = "value",
    sortProperty = "value",
    localOnly = true
)
public class VInteger extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(VInteger.class.getName());
    
    public static final String P_Value = "Value";
    
    public int value;
    
    public VInteger() {
    }
    public VInteger(int x) {
        setValue(x);
    }
    
    public void inc() {
        setValue(value+1);
    }
    public void dec() {
        setValue(value-1);
    }
    public void add(int x) {
        setValue(value+x);
    }
    public void sub(int x) {
        setValue(value-x);
    }
    
    @OAProperty(displayLength = 3)
    public int getValue() {
        return value;
    }
    public void setValue(int newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        int old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
}
