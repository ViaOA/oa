package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OATime;
 
 
public class SchTime extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Type = "Type";
    public static final String PROPERTY_BeginTime = "BeginTime";
    public static final String PROPERTY_EndTime = "EndTime";
    public static final String PROPERTY_Every = "Every";
    public static final String PROPERTY_Min = "Min";
    public static final String PROPERTY_Max = "Max";
    public static final String PROPERTY_Length = "Length";
     
    public static final String PROPERTY_DisplayText = "DisplayText";
    public static final String PROPERTY_NextTime = "NextTime";
    public static final String PROPERTY_IsNeeded = "IsNeeded";
    public static final String PROPERTY_PastDue = "PastDue";
     
    public static final String PROPERTY_SchDateTime = "SchDateTime";
     
    protected String id;
    protected int type;
    public static final int TYPE_ALLDAY = 0;
    public static final int TYPE_SETTIME = 1;
    public static final int TYPE_INTERVAL = 2;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("All day");
        hubType.addElement("Set time");
        hubType.addElement("Interval");
    }
    protected OATime beginTime;
    protected OATime endTime;
    protected int every;
    protected int min;
    protected int max;
    protected int length;
     
    // Links to other objects.
    protected transient SchDateTime schDateTime;
     
     
    public SchTime() {
        if (!isLoading()) {
            setEvery(-1);
            setMin(-1);
            setMax(-1);
            setLength(-1);
        }
    }
     
    public SchTime(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    public void setId(String newValue) {
        String old = this.id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    public int getType() {
        return type;
    }
    public void setType(int newValue) {
        int old = this.type;
        this.type = newValue;
        firePropertyChange(PROPERTY_Type, old, this.type);
    }
    public static Hub getTypes() {
        return hubType;
    }
    
     
    public OATime getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(OATime newValue) {
        OATime old = this.beginTime;
        this.beginTime = newValue;
        firePropertyChange(PROPERTY_BeginTime, old, this.beginTime);
    }
    
     
    public OATime getEndTime() {
        return endTime;
    }
    public void setEndTime(OATime newValue) {
        OATime old = this.endTime;
        this.endTime = newValue;
        firePropertyChange(PROPERTY_EndTime, old, this.endTime);
    }
    
     
    public int getEvery() {
        return every;
    }
    public void setEvery(int newValue) {
        int old = this.every;
        this.every = newValue;
        firePropertyChange(PROPERTY_Every, old, this.every);
    }
    
     
    public int getMin() {
        return min;
    }
    public void setMin(int newValue) {
        int old = this.min;
        this.min = newValue;
        firePropertyChange(PROPERTY_Min, old, this.min);
    }
    
     
    public int getMax() {
        return max;
    }
    public void setMax(int newValue) {
        int old = this.max;
        this.max = newValue;
        firePropertyChange(PROPERTY_Max, old, this.max);
    }
    
     
    public int getLength() {
        return length;
    }
    public void setLength(int newValue) {
        int old = this.length;
        this.length = newValue;
        firePropertyChange(PROPERTY_Length, old, this.length);
    }
    
     
    public String getDisplayText() {
        switch (type) {
/*        
            case ALLDAY: 
            String s = "";
            if (length > 0) s = ", taking " + length + " minutes to do.";
            return "Anytime"+s;
            case SETTIME: return getSetTimeDisplayText();
            case INTERVAL: return getIntervalDisplayText();
*/            
        }   
        return "";
    }
     
    public OATime getNextTime() {
        return null;//getNextTime(getLastTime(), getAmountAlreadyDone());
    }
    
    public OATime getNextTime(OATime lastTime) {
        return getNextTime(lastTime, 0);
    }
    
    public OATime getNextTime(OATime lastTime, int amountAlreadyDone) {
    	/*
        if (getType() == ALLDAY) {
            if (amountAlreadyDone > 0) return null;
            return new OATime();
        }
        else if (getType() == SETTIME) return getNextSetTime(lastTime, amountAlreadyDone);
        else return getNextIntervalTime(lastTime, amountAlreadyDone);
        */
    	return null;
    }
     
    public boolean getIsNeeded() {
        return false;//getIsNeeded(getLastTime(), getAmountAlreadyDone());
    }
    
    public boolean getIsNeeded(OATime lastTime, int amountAlreadyDone) {
    	return false;
    	/*
        if (getType() == ALLDAY) return (amountAlreadyDone < 1);
        else if (getType() == SETTIME) return isSetNeeded(lastTime);
        else return isIntervalNeeded(lastTime, amountAlreadyDone);
        */
    }
     
    public boolean getPastDue() {
        return false; //getPastDue(getLastTime(), getAmountAlreadyDone());
    }
    
    public boolean getPastDue(OATime lastTime, int amountAlreadyDone) {
    	return false;
    	/*
        if (!getIsNeeded()) return false;
        if (getType() == ALLDAY) return getPastDueAllDay(lastTime, amountAlreadyDone);
        else if (getType() == SETTIME) return getPastDueSet(lastTime);
        else return getPastDueInterval(lastTime, amountAlreadyDone);
        */
    }
     
    public SchDateTime getSchDateTime() {
        if (schDateTime == null) {
            schDateTime = (SchDateTime) getObject(PROPERTY_SchDateTime);
        }
        return schDateTime;
    }
    
    public void setSchDateTime(SchDateTime newValue) {
        SchDateTime old = this.schDateTime;
        this.schDateTime = newValue;
        firePropertyChange(PROPERTY_SchDateTime, old, this.schDateTime);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTime, SchDateTime.class, OALinkInfo.ONE, false, false, SchDateTime.PROPERTY_SchTime));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_DisplayText, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_NextTime, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_IsNeeded, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_PastDue, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
