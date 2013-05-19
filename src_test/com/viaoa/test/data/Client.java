package com.viaoa.test.data;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Client extends OAObject {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String firstName;
    protected String lastName;
    protected String address1;
    protected String address2;
    protected String city;
    protected String state;
    protected String zip;
    protected String phone;
    protected String phone2;
    protected String pmsId;
    protected String email;
    protected String country;

    // Links to other objects
    protected transient Hub hubClientAlerts;
     
    public Client() {
    }
     
    public Client(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    
    public void setId(String newId) {
        String old = id;
        this.id = newId;
        firePropertyChange("id", old, id);
    }
    
     
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String newFirstName) {
        String old = firstName;
        this.firstName = newFirstName;
        firePropertyChange("firstName", old, firstName);
    }
    
     
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String newLastName) {
        String old = lastName;
        this.lastName = newLastName;
        firePropertyChange("lastName", old, lastName);
    }
    
     
    public String getAddress1() {
        return address1;
    }
    
    public void setAddress1(String newAddress1) {
        String old = address1;
        this.address1 = newAddress1;
        firePropertyChange("address1", old, address1);
    }
    
     
    public String getAddress2() {
        return address2;
    }
    
    public void setAddress2(String newAddress2) {
        String old = address2;
        this.address2 = newAddress2;
        firePropertyChange("address2", old, address2);
    }
    
     
    public String getCity() {
        return city;
    }
    
    public void setCity(String newCity) {
        String old = city;
        this.city = newCity;
        firePropertyChange("city", old, city);
    }
    
     
    public String getState() {
        return state;
    }
    
    public void setState(String newState) {
        String old = state;
        this.state = newState;
        firePropertyChange("state", old, state);
    }
    
     
    public String getZip() {
        return zip;
    }
    
    public void setZip(String newZip) {
        String old = zip;
        this.zip = newZip;
        firePropertyChange("zip", old, zip);
    }
    
     
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String newPhone) {
        String old = phone;
        this.phone = newPhone;
        firePropertyChange("phone", old, phone);
    }

    public String getPhone2() {
        return phone2;
    }
    public void setPhone2(String newPhone) {
        String old = phone2;
        this.phone2 = newPhone;
        firePropertyChange("phone2", old, phone2);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    
    public void setPmsId(String newPmsId) {
        String old = pmsId;
        this.pmsId = newPmsId;
        firePropertyChange("pmsId", old, pmsId);
    }
    
     
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newEmail) {
        String old = email;
        this.email = newEmail;
        firePropertyChange("email", old, email);
    }

    public String getCountry() {
        return country;
    }
    
    public void setCountry(String newCountry) {
        String old = country;
        this.country = newCountry;
        firePropertyChange("country", old, country);
    }
    
    public String getTreeName() {
        String s = "";
        if (lastName != null) s = lastName;
        if (firstName != null) {
        	if (s.length() > 0) s += ", ";
        	s += firstName;
        }
        return s;
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
     
     
    public Hub getClientAlerts() {
        if (hubClientAlerts == null) {
            hubClientAlerts = getHub("clientAlerts");
        }
        return hubClientAlerts;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner, recursive)
        oaObjectInfo.addLink(new OALinkInfo("clientAlerts", ClientAlert.class, OALinkInfo.MANY, true, true, "client", false));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo("fullName", new String[] {"firstName","lastName"} ));
        oaObjectInfo.addCalc(new OACalcInfo("csz", new String[] {"city","state","zip"} ));
        oaObjectInfo.addCalc(new OACalcInfo("treeName", new String[] {"firstName","lastName"} ));
         
        oaObjectInfo.addRequired("id");
    }
     
     
}
 
