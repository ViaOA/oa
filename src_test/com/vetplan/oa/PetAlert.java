package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class PetAlert extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Description = "Description";
     
     
    public static final String PROPERTY_Pet = "Pet";
     
    protected String id;
    protected String description;
     
    // Links to other objects.
    protected transient Pet pet;
     
     
    public PetAlert() {
    }
     
    public PetAlert(String id) {
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
    
     
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        String old = this.description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    public Pet getPet() {
        if (pet == null) {
            pet = (Pet) getObject(PROPERTY_Pet);
        }
        return pet;
    }
    
    public void setPet(Pet newValue) {
        Pet old = this.pet;
        this.pet = newValue;
        firePropertyChange(PROPERTY_Pet, old, this.pet);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Pet, Pet.class, OALinkInfo.ONE, false, false, Pet.PROPERTY_PetAlerts));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
