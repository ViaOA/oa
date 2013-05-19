package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class SchDateTimes extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_DurationType = "DurationType";
    public static final String PROPERTY_Min = "Min";
    public static final String PROPERTY_Max = "Max";
    public static final String PROPERTY_Units = "Units";
     
     
    public static final String PROPERTY_ExamItemTasks = "ExamItemTasks";
    public static final String PROPERTY_ItemTasks = "ItemTasks";
    public static final String PROPERTY_IncludeSchDateTimes = "IncludeSchDateTimes";
    public static final String PROPERTY_ExcludeSchDateTimes = "ExcludeSchDateTimes";
     
    protected String id;
    protected int durationType;
    public static final int DURATIONTYPE_AMOUNT = 0;
    public static final int DURATIONTYPE_TIMEFRAME = 1;
    public static final int DURATIONTYPE_AGE = 2;
    public static final Hub hubDurationType;
    static {
        hubDurationType = new Hub(String.class);
        hubDurationType.addElement("Amount");
        hubDurationType.addElement("Timeframe");
        hubDurationType.addElement("Age");
    }
    protected int min;
    protected int max;
    protected int units;
    public static final int UNITS_YEAR = 0;
    public static final int UNITS_MONTH = 1;
    public static final int UNITS_WEEK = 2;
    public static final int UNITS_DAY = 3;
    public static final int UNITS_HOUR = 4;
    public static final int UNITS_MINUTE = 5;
    public static final Hub hubUnits;
    static {
        hubUnits = new Hub(String.class);
        hubUnits.addElement("Year");
        hubUnits.addElement("Month");
        hubUnits.addElement("Week");
        hubUnits.addElement("Day");
        hubUnits.addElement("Hour");
        hubUnits.addElement("Minute");
    }
     
    // Links to other objects.
    protected transient Hub hubIncludeSchDateTimes;
    protected transient Hub hubExcludeSchDateTimes;
     
     
    public SchDateTimes() {
    }
     
    public SchDateTimes(String id) {
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
    
     
    public int getDurationType() {
        return durationType;
    }
    public void setDurationType(int newValue) {
        int old = this.durationType;
        this.durationType = newValue;
        firePropertyChange(PROPERTY_DurationType, old, this.durationType);
    }
    public static Hub getDurationTypes() {
        return hubDurationType;
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
    
     
    public int getUnits() {
        return units;
    }
    public void setUnits(int newValue) {
        int old = this.units;
        this.units = newValue;
        firePropertyChange(PROPERTY_Units, old, this.units);
    }
    public static Hub getUnitss() {
        return hubUnits;
    }
    
     
    public Hub getIncludeSchDateTimes() {
        if (hubIncludeSchDateTimes == null) {
            hubIncludeSchDateTimes = getHub(PROPERTY_IncludeSchDateTimes);
        }
        return hubIncludeSchDateTimes;
    }
    
     
    public Hub getExcludeSchDateTimes() {
        if (hubExcludeSchDateTimes == null) {
            hubExcludeSchDateTimes = getHub(PROPERTY_ExcludeSchDateTimes);
        }
        return hubExcludeSchDateTimes;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTasks, ExamItemTask.class, OALinkInfo.MANY, false, false, ExamItemTask.PROPERTY_SchDateTimes));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTasks, ItemTask.class, OALinkInfo.MANY, false, false, ItemTask.PROPERTY_SchDateTimes));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_IncludeSchDateTimes, SchDateTime.class, OALinkInfo.MANY, false, false, SchDateTime.PROPERTY_SchDateTimes2));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExcludeSchDateTimes, SchDateTime.class, OALinkInfo.MANY, false, false, SchDateTime.PROPERTY_SchDateTimes));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
