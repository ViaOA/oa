package com.viaoa.object;


public class OAEditException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private String property;
    private Object newValue;

    public OAEditException(OAObject obj, String property, Object newValue) {
        super("Invalid entry for "+property);
        this.property = property;
        this.newValue = newValue;
    }

    public OAEditException(OAObject obj, String property, long newValue) {
        this(obj, property, new Long(newValue));
    }

    public OAEditException(OAObject obj, String property, double newValue) {
        this(obj, property, new Double(newValue));
    }
    
    public OAEditException(OAObject obj, String property, boolean newValue) {
        this(obj, property, new Boolean(newValue));
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public String getProperty() {
        return property;
    }
}

