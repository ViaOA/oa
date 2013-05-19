package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class SchDate extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Type = "Type";
    public static final String PROPERTY_Days = "Days";
    public static final String PROPERTY_Months = "Months";
    public static final String PROPERTY_MonthWeeks = "MonthWeeks";
    public static final String PROPERTY_Quarters = "Quarters";
    public static final String PROPERTY_QuarterMonths = "QuarterMonths";
     
    public static final String PROPERTY_DisplayText = "DisplayText";
    public static final String PROPERTY_NextDate = "NextDate";
     
    public static final String PROPERTY_SchDateTime = "SchDateTime";
     
    protected String id;
    protected int type;
    protected int days;
    protected int months;
    protected int monthWeeks;
    protected int quarters;
    protected int quarterMonths;
     
    // Links to other objects.
    protected transient SchDateTime schDateTime;
     
     
    public SchDate() {
    }
     
    public SchDate(String id) {
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
    
     
    public int getDays() {
        return days;
    }
    public void setDays(int newValue) {
        int old = this.days;
        this.days = newValue;
        firePropertyChange(PROPERTY_Days, old, this.days);
    }
    
     
    public int getMonths() {
        return months;
    }
    public void setMonths(int newValue) {
        int old = this.months;
        this.months = newValue;
        firePropertyChange(PROPERTY_Months, old, this.months);
    }
    
     
    public int getMonthWeeks() {
        return monthWeeks;
    }
    public void setMonthWeeks(int newValue) {
        int old = this.monthWeeks;
        this.monthWeeks = newValue;
        firePropertyChange(PROPERTY_MonthWeeks, old, this.monthWeeks);
    }
    
     
    public int getQuarters() {
        return quarters;
    }
    public void setQuarters(int newValue) {
        int old = this.quarters;
        this.quarters = newValue;
        firePropertyChange(PROPERTY_Quarters, old, this.quarters);
    }
    
     
    public int getQuarterMonths() {
        return quarterMonths;
    }
    public void setQuarterMonths(int newValue) {
        int old = this.quarterMonths;
        this.quarterMonths = newValue;
        firePropertyChange(PROPERTY_QuarterMonths, old, this.quarterMonths);
    }
    
     
    public String getDisplayText() {
        switch (type) {
/*        
            case DAILY: return getDailyDisplayText();
            case WEEKLY: return getWeeklyDisplayText();
            case MONTHLY: return getMonthlyDisplayText();
            case QUARTERLY: return getQuarterlyDisplayText();
            case YEARLY: return getYearlyDisplayText();
*/            
        }   
        return "";
    }
     
    public OADate getNextDate() {
        return null;//getNextDate(getStartDate(), getFromDate(), getOccurance());
    }
    
    public OADate getNextDate(OADate startDate, OADate fromDate, int occurance) {
        if (fromDate == null) fromDate = new OADate();
        else fromDate = new OADate(fromDate);
        
        if (startDate != null) startDate = new OADate(startDate);
        
        if (occurance < 1) occurance = 1;
        switch (type) {
/*        
            case DAILY: return getNextDailyDate(startDate, fromDate, occurance);
            case WEEKLY: return getNextWeeklyDate(startDate, fromDate, occurance);
            case MONTHLY: return getNextMonthlyDate(startDate, fromDate, occurance);
            case QUARTERLY: return getNextQuarterlyDate(startDate, fromDate, occurance);
            case YEARLY: return getNextYearlyDate(startDate, fromDate, occurance);
*/            
        }
        return null;
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTime, SchDateTime.class, OALinkInfo.ONE, false, false, SchDateTime.PROPERTY_SchDate));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_DisplayText, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_NextDate, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
