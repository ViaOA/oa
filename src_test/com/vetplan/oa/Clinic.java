package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Clinic extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Name2 = "Name2";
    public static final String PROPERTY_Display1 = "Display1";
    public static final String PROPERTY_Display2 = "Display2";
     
     
    public static final String PROPERTY_Exams = "Exams";
    public static final String PROPERTY_Company = "Company";
     
    protected String id;
    protected OADate date;
    protected String name;
    protected String name2;
    protected String display1;
    protected String display2;
     
    // Links to other objects.
    protected transient Company company;
     
     
    public Clinic() {
    }
     
    public Clinic(String id) {
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
    
     
    public String getDisplay1() {
        return display1;
    }
    public void setDisplay1(String newValue) {
        String old = this.display1;
        this.display1 = newValue;
        firePropertyChange(PROPERTY_Display1, old, this.display1);
    }
    
     
    public String getDisplay2() {
        return display2;
    }
    public void setDisplay2(String newValue) {
        String old = this.display2;
        this.display2 = newValue;
        firePropertyChange(PROPERTY_Display2, old, this.display2);
    }
    
     
    public Company getCompany() {
        if (company == null) {
            company = (Company) getObject(PROPERTY_Company);
        }
        return company;
    }
    
    public void setCompany(Company newValue) {
        Company old = this.company;
        this.company = newValue;
        firePropertyChange(PROPERTY_Company, old, this.company);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Exams, Exam.class, OALinkInfo.MANY, false, false, Exam.PROPERTY_Clinic));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Company, Company.class, OALinkInfo.ONE, false, false, Company.PROPERTY_Clinics));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
