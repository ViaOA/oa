package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Species extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_DefaultGifFileName = "DefaultGifFileName";
    public static final String PROPERTY_WellnessGifFileName = "WellnessGifFileName";
     
     
    public static final String PROPERTY_Pets = "Pets";
    public static final String PROPERTY_TemplateCategories = "TemplateCategories";
    public static final String PROPERTY_Breeds = "Breeds";
    public static final String PROPERTY_LabTestSpecies = "LabTestSpecies";
     
    protected String id;
    protected String name;
    protected String description;
    protected String gifFileName;
    protected int seq;
    protected String defaultGifFileName;
    protected String wellnessGifFileName;
     
    // Links to other objects.
    protected transient Hub hubTemplateCategories;
    protected transient Hub hubBreeds;
    protected transient Hub hubLabTestSpecies;
     
     
    public Species() {
    }
     
    public Species(String id) {
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
    
     
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        String old = this.description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    public String getGifFileName() {
        return gifFileName;
    }
    public void setGifFileName(String newValue) {
        String old = this.gifFileName;
        this.gifFileName = newValue;
        firePropertyChange(PROPERTY_GifFileName, old, this.gifFileName);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getDefaultGifFileName() {
        return defaultGifFileName;
    }
    public void setDefaultGifFileName(String newValue) {
        String old = this.defaultGifFileName;
        this.defaultGifFileName = newValue;
        firePropertyChange(PROPERTY_DefaultGifFileName, old, this.defaultGifFileName);
    }
    
     
    public String getWellnessGifFileName() {
        return wellnessGifFileName;
    }
    public void setWellnessGifFileName(String newValue) {
        String old = this.wellnessGifFileName;
        this.wellnessGifFileName = newValue;
        firePropertyChange(PROPERTY_WellnessGifFileName, old, this.wellnessGifFileName);
    }
    
     
    public Hub getTemplateCategories() {
        if (hubTemplateCategories == null) {
            hubTemplateCategories = getHub(PROPERTY_TemplateCategories);
        }
        return hubTemplateCategories;
    }
    
     
    public Hub getBreeds() {
        if (hubBreeds == null) {
            hubBreeds = getHub(PROPERTY_Breeds);
        }
        return hubBreeds;
    }
    
     
    public Hub getLabTestSpecies() {
        if (hubLabTestSpecies == null) {
            hubLabTestSpecies = getHub(PROPERTY_LabTestSpecies);
        }
        return hubLabTestSpecies;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Pets, Pet.class, OALinkInfo.MANY, false, false, Pet.PROPERTY_Species));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TemplateCategories, TemplateCategory.class, OALinkInfo.MANY, true, true, TemplateCategory.PROPERTY_Species, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Breeds, Breed.class, OALinkInfo.MANY, true, true, Breed.PROPERTY_Species, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestSpecies, LabTestSpecies.class, OALinkInfo.MANY, false, false, LabTestSpecies.PROPERTY_Species));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
