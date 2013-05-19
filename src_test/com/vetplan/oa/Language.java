package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Language extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Code = "Code";
    public static final String PROPERTY_CountryCode = "CountryCode";
    public static final String PROPERTY_Description = "Description";
     
     
    public static final String PROPERTY_Dictionaries = "Dictionaries";
     
    protected String id;
    protected String code;
    protected String countryCode;
    protected String description;
     
    // Links to other objects.
     
     
    public Language() {
    }
     
    public Language(String id) {
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
    
     
    public String getCode() {
        return code;
    }
    public void setCode(String newValue) {
        String old = this.code;
        this.code = newValue;
        firePropertyChange(PROPERTY_Code, old, this.code);
    }
    
     
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String newValue) {
        String old = this.countryCode;
        this.countryCode = newValue;
        firePropertyChange(PROPERTY_CountryCode, old, this.countryCode);
    }
    
     
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        String old = this.description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Dictionaries, Dictionary.class, OALinkInfo.MANY, false, false, Dictionary.PROPERTY_Language));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
