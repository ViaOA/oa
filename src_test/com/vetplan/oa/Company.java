package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Company extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Name2 = "Name2";
     
     
    public static final String PROPERTY_Clinics = "Clinics";
     
    protected String id;
    protected OADate date;
    protected String name;
    protected String name2;
     
    // Links to other objects.
    protected transient Hub hubClinics;
     
     
    public Company() {
    }
     
    public Company(String id) {
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
    
     
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate newValue) {
        OADate old = this.date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        String old = this.name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    public String getName2() {
        return name2;
    }
    public void setName2(String newValue) {
        String old = this.name2;
        this.name2 = newValue;
        firePropertyChange(PROPERTY_Name2, old, this.name2);
    }
    
     
    public Hub getClinics() {
        if (hubClinics == null) {
            hubClinics = getHub(PROPERTY_Clinics);
        }
        return hubClinics;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Clinics, Clinic.class, OALinkInfo.MANY, true, true, Clinic.PROPERTY_Company));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
