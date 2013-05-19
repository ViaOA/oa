package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Problem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Note = "Note";
    public static final String PROPERTY_EndDate = "EndDate";
     
     
    public static final String PROPERTY_Pet = "Pet";
    public static final String PROPERTY_ExamItems = "ExamItems";
     
    protected String id;
    protected String name;
    protected String note;
    protected OADate endDate;
     
    // Links to other objects.
    protected transient Pet pet;
    protected transient Hub hubExamItems;
     
     
    public Problem() {
    }
     
    public Problem(String id) {
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
    
     
    public String getNote() {
        return note;
    }
    public void setNote(String newValue) {
        String old = this.note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    public OADate getEndDate() {
        return endDate;
    }
    public void setEndDate(OADate newValue) {
        OADate old = this.endDate;
        this.endDate = newValue;
        firePropertyChange(PROPERTY_EndDate, old, this.endDate);
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
     
    public Hub getExamItems() {
        if (hubExamItems == null) {
            hubExamItems = getHub(PROPERTY_ExamItems);
        }
        return hubExamItems;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Pet, Pet.class, OALinkInfo.ONE, false, false, Pet.PROPERTY_Problems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, false, true, ExamItem.PROPERTY_Problem));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
