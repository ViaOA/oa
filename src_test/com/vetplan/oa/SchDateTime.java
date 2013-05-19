package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class SchDateTime extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
     
    public static final String PROPERTY_DisplayText = "DisplayText";
     
    public static final String PROPERTY_SchDateTimes2 = "SchDateTimes2";
    public static final String PROPERTY_SchDateTimes = "SchDateTimes";
    public static final String PROPERTY_SchDate = "SchDate";
    public static final String PROPERTY_SchTime = "SchTime";
     
    protected String id;
     
    // Links to other objects.
    protected transient SchDateTimes schDateTimes2;
    protected transient SchDateTimes schDateTimes;
    protected transient SchDate schDate;
    protected transient SchTime schTime;
     
     
    public SchDateTime() {
    }
     
    public SchDateTime(String id) {
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
    
     
    public String getDisplayText() {
        String s = getSchDate().getDisplayText();
        if (s == null) s = "";
        else s += " ";
        s += getSchTime().getDisplayText();
        return s;
    }
     
    public SchDateTimes getSchDateTimes2() {
        if (schDateTimes2 == null) {
            schDateTimes2 = (SchDateTimes) getObject(PROPERTY_SchDateTimes2);
        }
        return schDateTimes2;
    }
    
    public void setSchDateTimes2(SchDateTimes newValue) {
        SchDateTimes old = this.schDateTimes2;
        this.schDateTimes2 = newValue;
        firePropertyChange(PROPERTY_SchDateTimes2, old, this.schDateTimes2);
    }
     
    public SchDateTimes getSchDateTimes() {
        if (schDateTimes == null) {
            schDateTimes = (SchDateTimes) getObject(PROPERTY_SchDateTimes);
        }
        return schDateTimes;
    }
    
    public void setSchDateTimes(SchDateTimes newValue) {
        SchDateTimes old = this.schDateTimes;
        this.schDateTimes = newValue;
        firePropertyChange(PROPERTY_SchDateTimes, old, this.schDateTimes);
    }
     
    public SchDate getSchDate() {
        if (schDate == null) {
            schDate = (SchDate) getObject(PROPERTY_SchDate);
        }
        return schDate;
    }
    
    public void setSchDate(SchDate newValue) {
        SchDate old = this.schDate;
        this.schDate = newValue;
        firePropertyChange(PROPERTY_SchDate, old, this.schDate);
    }
     
    public SchTime getSchTime() {
        if (schTime == null) {
            schTime = (SchTime) getObject(PROPERTY_SchTime);
        }
        return schTime;
    }
    
    public void setSchTime(SchTime newValue) {
        SchTime old = this.schTime;
        this.schTime = newValue;
        firePropertyChange(PROPERTY_SchTime, old, this.schTime);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTimes2, SchDateTimes.class, OALinkInfo.ONE, false, false, SchDateTimes.PROPERTY_IncludeSchDateTimes));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTimes, SchDateTimes.class, OALinkInfo.ONE, false, false, SchDateTimes.PROPERTY_ExcludeSchDateTimes));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDate, SchDate.class, OALinkInfo.ONE, false, false, SchDate.PROPERTY_SchDateTime));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchTime, SchTime.class, OALinkInfo.ONE, false, false, SchTime.PROPERTY_SchDateTime));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_DisplayText, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
