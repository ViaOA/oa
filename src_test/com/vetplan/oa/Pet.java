package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import java.util.*;
 
 
public class Pet extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_NickName = "NickName";
    public static final String PROPERTY_Sex = "Sex";
    public static final String PROPERTY_BirthDate = "BirthDate";
    public static final String PROPERTY_Color = "Color";
    public static final String PROPERTY_Breed = "Breed";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String PROPERTY_InactiveReason = "InactiveReason";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_MicroChip = "MicroChip";
    public static final String PROPERTY_MicroChipDate = "MicroChipDate";
    public static final String PROPERTY_Rabies = "Rabies";
    public static final String PROPERTY_RabiesDate = "RabiesDate";
    public static final String PROPERTY_History = "History";
     
    public static final String PROPERTY_DisplayName = "DisplayName";
     
    public static final String PROPERTY_Exams = "Exams";
    public static final String PROPERTY_Species = "Species";
    public static final String PROPERTY_Client = "Client";
    public static final String PROPERTY_Problems = "Problems";
    public static final String PROPERTY_PetAlerts = "PetAlerts";
    public static final String PROPERTY_SpecialClient = "SpecialClient";
     
        
    
    protected String id;
    protected String name;
    protected String nickName;
    protected String sex;
    protected OADate birthDate;
    protected String color;
    protected String breed;
    protected OADate inactiveDate;
    protected String inactiveReason;
    protected String pmsId;
    protected String microChip;
    protected OADate microChipDate;
    protected String rabies;
    protected OADate rabiesDate;
    protected String history;
     
    // Links to other objects.
    protected transient Hub hubExams;
    protected transient Species species;
    protected transient Client client;
    protected transient Hub hubProblems;
    protected transient Hub hubPetAlerts;
    protected transient Client specialClient;
     
     
    public Pet() {
    }
     
    public Pet(String id) {
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
    
     
    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        String old = this.name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String newValue) {
        String old = this.nickName;
        this.nickName = newValue;
        firePropertyChange(PROPERTY_NickName, old, this.nickName);
    }
    
     
    public String getSex() {
        return sex;
    }
    public void setSex(String newValue) {
        String old = this.sex;
        this.sex = newValue;
        firePropertyChange(PROPERTY_Sex, old, this.sex);
    }
    
     
    public OADate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(OADate newValue) {
        OADate old = this.birthDate;
        this.birthDate = newValue;
        firePropertyChange(PROPERTY_BirthDate, old, this.birthDate);
    }
    
     
    public String getColor() {
        return color;
    }
    public void setColor(String newValue) {
        String old = this.color;
        this.color = newValue;
        firePropertyChange(PROPERTY_Color, old, this.color);
    }
    
     
    public String getBreed() {
        return breed;
    }
    public void setBreed(String newValue) {
        String old = this.breed;
        this.breed = newValue;
        firePropertyChange(PROPERTY_Breed, old, this.breed);
    }
    
     
    public OADate getInactiveDate() {
        return inactiveDate;
    }
    public void setInactiveDate(OADate newValue) {
        OADate old = this.inactiveDate;
        this.inactiveDate = newValue;
        firePropertyChange(PROPERTY_InactiveDate, old, this.inactiveDate);
    }
    
     
    public String getInactiveReason() {
        return inactiveReason;
    }
    public void setInactiveReason(String newValue) {
        String old = this.inactiveReason;
        this.inactiveReason = newValue;
        firePropertyChange(PROPERTY_InactiveReason, old, this.inactiveReason);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public String getMicroChip() {
        return microChip;
    }
    public void setMicroChip(String newValue) {
        String old = this.microChip;
        this.microChip = newValue;
        firePropertyChange(PROPERTY_MicroChip, old, this.microChip);
    }
    
     
    public OADate getMicroChipDate() {
        return microChipDate;
    }
    public void setMicroChipDate(OADate newValue) {
        OADate old = this.microChipDate;
        this.microChipDate = newValue;
        firePropertyChange(PROPERTY_MicroChipDate, old, this.microChipDate);
    }
    
     
    public String getRabies() {
        return rabies;
    }
    public void setRabies(String newValue) {
        String old = this.rabies;
        this.rabies = newValue;
        firePropertyChange(PROPERTY_Rabies, old, this.rabies);
    }
    
     
    public OADate getRabiesDate() {
        return rabiesDate;
    }
    public void setRabiesDate(OADate newValue) {
        OADate old = this.rabiesDate;
        this.rabiesDate = newValue;
        firePropertyChange(PROPERTY_RabiesDate, old, this.rabiesDate);
    }
    
     
    public String getHistory() {
        return history;
    }
    public void setHistory(String newValue) {
        String old = this.history;
        this.history = newValue;
        firePropertyChange(PROPERTY_History, old, this.history);
    }
    
     
    public String getDisplayName() {
        String s = "";
        if (nickName != null && nickName.length() > 0 ) s = nickName;
        else if (name != null) s = name;
        return s;
    
    }
     
    public Hub getExams() {
        if (hubExams == null) {
            hubExams = getHub(PROPERTY_Exams);
        }
        return hubExams;
    }
    
     
    public Species getSpecies() {
        if (species == null) {
            species = (Species) getObject(PROPERTY_Species);
        }
        return species;
    }
    
    public void setSpecies(Species newValue) {
        Species old = this.species;
        this.species = newValue;
        firePropertyChange(PROPERTY_Species, old, this.species);
    }
     
    public Client getClient() {
        if (client == null) {
            client = (Client) getObject(PROPERTY_Client);
        }
        return client;
    }
    
    public void setClient(Client newValue) {
        Client old = this.client;
        this.client = newValue;
        firePropertyChange(PROPERTY_Client, old, this.client);
    }
    public Client getSpecialClient() {
        if (specialClient == null) {
        	specialClient = (Client) getObject(PROPERTY_SpecialClient);
        }
        return specialClient;
    }
    
    public void setSpecialClient(Client newValue) {
        Client old = this.specialClient;
        this.specialClient = newValue;
        firePropertyChange(PROPERTY_SpecialClient, old, this.client);
    }
     
    public Hub getProblems() {
        if (hubProblems == null) {
            hubProblems = getHub(PROPERTY_Problems);
        }
        return hubProblems;
    }
    
     
    public Hub getPetAlerts() {
        if (hubPetAlerts == null) {
            hubPetAlerts = getHub(PROPERTY_PetAlerts);
        }
        return hubPetAlerts;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Exams, Exam.class, OALinkInfo.MANY, true, true, Exam.PROPERTY_Pet));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Species, Species.class, OALinkInfo.ONE, false, false, Species.PROPERTY_Pets));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Client, Client.class, OALinkInfo.ONE, false, false, Client.PROPERTY_Pets));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Problems, Problem.class, OALinkInfo.MANY, true, true, Problem.PROPERTY_Pet));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_PetAlerts, PetAlert.class, OALinkInfo.MANY, true, true, PetAlert.PROPERTY_Pet));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SpecialClient, Client.class, OALinkInfo.ONE, true, Client.PROPERTY_SpecialPet));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_DisplayName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
