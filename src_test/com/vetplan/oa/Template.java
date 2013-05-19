package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Template extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Type = "Type";
     
     
    public static final String PROPERTY_ExamTemplates = "ExamTemplates";
    public static final String PROPERTY_TemplateCategories = "TemplateCategories";
    public static final String PROPERTY_TemplateRows = "TemplateRows";
     
    protected String id;
    protected String name;
    protected String description;
    protected String gifFileName;
    protected int type;
    public static final int TYPE_OTHER = 0;
    public static final int TYPE_EXAM = 1;
    public static final int TYPE_WELLNESS = 2;
    public static final int TYPE_GROOMING = 3;
    public static final int TYPE_BOARDING = 4;
    public static final int TYPE_SOAP = 5;
    public static final int TYPE_HISTORY = 6;
    public static final int TYPE_LAB = 7;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("Other");
        hubType.addElement("Exam");
        hubType.addElement("Wellness");
        hubType.addElement("Grooming");
        hubType.addElement("Boarding");
        hubType.addElement("Soap");
        hubType.addElement("History");
        hubType.addElement("Lab");
    }
     
    // Links to other objects.
    protected transient Hub hubTemplateCategories;
    protected transient Hub hubTemplateRows;
     
     
    public Template() {
    }
     
    public Template(String id) {
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
    
     
    public int getType() {
        return type;
    }
    public void setType(int newValue) {
        int old = this.type;
        this.type = newValue;
        firePropertyChange(PROPERTY_Type, old, this.type);
    }
    public static Hub getTypes() {
        return hubType;
    }
    
     
    public Hub getTemplateCategories() {
        if (hubTemplateCategories == null) {
            hubTemplateCategories = getHub(PROPERTY_TemplateCategories);
        }
        return hubTemplateCategories;
    }
    
     
    public Hub getTemplateRows() {
        if (hubTemplateRows == null) {
            hubTemplateRows = getHub(PROPERTY_TemplateRows);
        }
        return hubTemplateRows;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamTemplates, ExamTemplate.class, OALinkInfo.MANY, false, false, ExamTemplate.PROPERTY_Template));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TemplateCategories, TemplateCategory.class, OALinkInfo.MANY, false, false, TemplateCategory.PROPERTY_Templates));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TemplateRows, TemplateRow.class, OALinkInfo.MANY, false, false, TemplateRow.PROPERTY_Template, true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
