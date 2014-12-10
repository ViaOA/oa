package com.viaoa.jfc.console;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADateTime;

@OAClass(initialize=false, localOnly=true, useDataSource=false)
public class Console extends OAObject {

    public static final String P_DateTime = "DateTime";
    public static final String P_Text = "Text";
    
    protected OADateTime dateTime;
    protected String text;

    public Console() {
        if (!isLoading()) {
            setDateTime(new OADateTime());
        }
    }
    
    @OAProperty()
    public OADateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(OADateTime newValue) {
        fireBeforePropertyChange(P_DateTime, this.dateTime, newValue);
        OADateTime old = dateTime;
        this.dateTime = newValue;
        firePropertyChange(P_DateTime, old, this.dateTime);
    }
    
    @OAProperty()
    public String getText() {
        return text;
    }
    public void setText(String newValue) {
        fireBeforePropertyChange(P_Text, this.text, newValue);
        String old = text;
        this.text = newValue;
        firePropertyChange(P_Text, old, this.text);
    }
}
