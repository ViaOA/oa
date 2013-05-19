package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class TemplateCategory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Species = "Species";
    public static final String PROPERTY_Templates = "Templates";
    public static final String PROPERTY_ParentTemplateCategory = "ParentTemplateCategory";
    public static final String PROPERTY_TemplateCategories = "TemplateCategories";
     
    protected String id;
    protected String name;
    protected String description;
    protected int seq;
     
    // Links to other objects.
    protected transient Species species;
    protected transient Hub hubTemplates;
    protected transient TemplateCategory parentTemplateCategory;
    protected transient Hub hubTemplateCategories;
     
     
    public TemplateCategory() {
    }
     
    public TemplateCategory(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
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
     
    public Hub getTemplates() {
        if (hubTemplates == null) {
            hubTemplates = getHub(PROPERTY_Templates);
        }
        return hubTemplates;
    }
    
     
    public TemplateCategory getParentTemplateCategory() {
        if (parentTemplateCategory == null) {
            parentTemplateCategory = (TemplateCategory) getObject(PROPERTY_ParentTemplateCategory);
        }
        return parentTemplateCategory;
    }
    
    public void setParentTemplateCategory(TemplateCategory newValue) {
        TemplateCategory old = this.parentTemplateCategory;
        this.parentTemplateCategory = newValue;
        firePropertyChange(PROPERTY_ParentTemplateCategory, old, this.parentTemplateCategory);
    }
     
    public Hub getTemplateCategories() {
        if (hubTemplateCategories == null) {
            hubTemplateCategories = getHub(PROPERTY_TemplateCategories, "seq");
            hubTemplateCategories.setAutoSequence("seq");
        }
        return hubTemplateCategories;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Species, Species.class, OALinkInfo.ONE, false, false, Species.PROPERTY_TemplateCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Templates, Template.class, OALinkInfo.MANY, true, false, Template.PROPERTY_TemplateCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentTemplateCategory, TemplateCategory.class, OALinkInfo.ONE, false, false, TemplateCategory.PROPERTY_TemplateCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TemplateCategories, TemplateCategory.class, OALinkInfo.MANY, true, true, TemplateCategory.PROPERTY_ParentTemplateCategory));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
