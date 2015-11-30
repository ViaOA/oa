package com.viaoa.object;

public class OAEditMode extends OAObject {
    
    public static final String PROPERTY_AllowEdit = "AllowEdit";
    public static final String P_AllowEdit = "AllowEdit";

    protected boolean allowEdit;
    
    public boolean getAllowEdit() {
        return allowEdit;
    }
    
    public void setAllowEdit(boolean newValue) {
        fireBeforePropertyChange(P_AllowEdit, this.allowEdit, newValue);
        boolean old = allowEdit;
        this.allowEdit = newValue;
        firePropertyChange(P_AllowEdit, old, this.allowEdit);
    }
}
