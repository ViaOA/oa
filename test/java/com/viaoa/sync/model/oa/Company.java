package com.viaoa.sync.model.oa;

import com.viaoa.object.*;
import com.viaoa.hub.*;

public class Company extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_Name = "Name";
    protected String name;
    private Hub hubUsers;

    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        fireBeforePropertyChange(PROPERTY_Name, this.name, newValue);
        String old = this.name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }

    public Hub getUsers() {
        if (hubUsers == null) hubUsers = getHub("Users");
        return hubUsers;
    }
}
