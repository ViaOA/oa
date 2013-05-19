package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Client extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_FirstName = "FirstName";
    public static final String PROPERTY_LastName = "LastName";
    public static final String PROPERTY_Address1 = "Address1";
    public static final String PROPERTY_Address2 = "Address2";
    public static final String PROPERTY_City = "City";
    public static final String PROPERTY_State = "State";
    public static final String PROPERTY_Zip = "Zip";
    public static final String PROPERTY_Country = "Country";
    public static final String PROPERTY_Phone = "Phone";
    public static final String PROPERTY_Phone2 = "Phone2";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_Email = "Email";
     
    public static final String PROPERTY_FullName = "FullName";
    public static final String PROPERTY_Csz = "Csz";
     
    public static final String PROPERTY_SpecialPet = "SpecialPet";
    public static final String PROPERTY_Pets = "Pets";
    public static final String PROPERTY_ClientAlerts = "ClientAlerts";
     
    protected String id;
    protected String firstName;
    protected String lastName;
    protected String address1;
    protected String address2;
    protected String city;
    protected String state;
    protected String zip;
    protected String country;
    protected String phone;
    protected String phone2;
    protected String pmsId;
    protected String email;
    
     
    // Links to other objects.
    protected transient Hub hubPets;
    protected transient Hub hubClientAlerts;
    protected transient Pet specialPet;
     
    public Client() {
    }
     
    public Client(String id) {
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
    
     
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String newValue) {
        String old = this.firstName;
        this.firstName = newValue;
        firePropertyChange(PROPERTY_FirstName, old, this.firstName);
    }
    
     
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String newValue) {
        String old = this.lastName;
        this.lastName = newValue;
        firePropertyChange(PROPERTY_LastName, old, this.lastName);
    }
    
     
    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String newValue) {
        String old = this.address1;
        this.address1 = newValue;
        firePropertyChange(PROPERTY_Address1, old, this.address1);
    }
    
     
    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String newValue) {
        String old = this.address2;
        this.address2 = newValue;
        firePropertyChange(PROPERTY_Address2, old, this.address2);
    }
    
     
    public String getCity() {
        return city;
    }
    public void setCity(String newValue) {
        String old = this.city;
        this.city = newValue;
        firePropertyChange(PROPERTY_City, old, this.city);
    }
    
     
    public String getState() {
        return state;
    }
    public void setState(String newValue) {
        String old = this.state;
        this.state = newValue;
        firePropertyChange(PROPERTY_State, old, this.state);
    }
    
     
    public String getZip() {
        return zip;
    }
    public void setZip(String newValue) {
        String old = this.zip;
        this.zip = newValue;
        firePropertyChange(PROPERTY_Zip, old, this.zip);
    }
    
     
    public String getCountry() {
        return country;
    }
    public void setCountry(String newValue) {
        String old = this.country;
        this.country = newValue;
        firePropertyChange(PROPERTY_Country, old, this.country);
    }
    
     
    public String getPhone() {
        return phone;
    }
    public void setPhone(String newValue) {
        String old = this.phone;
        this.phone = newValue;
        firePropertyChange(PROPERTY_Phone, old, this.phone);
    }
    
     
    public String getPhone2() {
        return phone2;
    }
    public void setPhone2(String newValue) {
        String old = this.phone2;
        this.phone2 = newValue;
        firePropertyChange(PROPERTY_Phone2, old, this.phone2);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public String getEmail() {
        return email;
    }
    public void setEmail(String newValue) {
        String old = this.email;
        this.email = newValue;
        firePropertyChange(PROPERTY_Email, old, this.email);
    }
    
     
    public String getFullName() {
        String s = "";
        if (firstName != null) s = firstName;
        if (lastName != null) {
        	if (s.length() > 0) s += " ";
        	s += lastName;
        }
        return s;
    }
     
     
    public String getCsz() {
        String s = "";
        if (city != null) s = city + ", ";
        if (state != null) s += state + " ";
        if (zip != null) s += zip;
        if (country != null && country.trim().length() > 0) s += country.trim();
        return s; 
    }
     
    public Hub getPets() {
        if (hubPets == null) {
            hubPets = getHub(PROPERTY_Pets);
        }
        return hubPets;
    }
    
     
    public Hub getClientAlerts() {
        if (hubClientAlerts == null) {
            hubClientAlerts = getHub(PROPERTY_ClientAlerts);
        }
        return hubClientAlerts;
    }
    
    public Pet getSpecialPet() {
    	if (specialPet == null) specialPet = (Pet) getObject(PROPERTY_SpecialPet);
    	return specialPet;
    }
    public void setSpecialPet(Pet value) {
    	Pet old = this.specialPet;
    	this.specialPet = value;
    	firePropertyChange("specialPet", old, value);
    }
    	
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Pets, Pet.class, OALinkInfo.MANY, true, true, Pet.PROPERTY_Client));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ClientAlerts, ClientAlert.class, OALinkInfo.MANY, true, true, ClientAlert.PROPERTY_Client, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SpecialPet, Pet.class, OALinkInfo.ONE, true, true, Pet.PROPERTY_SpecialClient));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_Csz, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
