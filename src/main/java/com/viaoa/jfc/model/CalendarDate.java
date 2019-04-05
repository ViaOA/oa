package com.viaoa.jfc.model;

import com.viaoa.object.*;
import com.viaoa.annotation.OAClass;
import com.viaoa.util.*;

@OAClass(localOnly=true, addToCache=false, initialize=false)
public class CalendarDate extends OAObject {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_Date = "Date";

	protected OADate date;
	
	public OADate getDate() {
		return date;
	}
	
	public void setDate(OADate newValue) {
		OADate old = this.date;
        fireBeforePropertyChange(PROPERTY_Date, old, newValue);
		this.date = newValue;
		firePropertyChange(PROPERTY_Date, old, this.date);
	}	
	

}

