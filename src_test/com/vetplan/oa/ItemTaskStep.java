package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemTaskStep extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Required = "Required";
    public static final String PROPERTY_Type = "Type";
    public static final String PROPERTY_Min = "Min";
    public static final String PROPERTY_Max = "Max";
    public static final String PROPERTY_Seq = "Seq";
     
    public static final String PROPERTY_DisplayName = "DisplayName";
     
    public static final String PROPERTY_ExamItemTaskSteps = "ExamItemTaskSteps";
    public static final String PROPERTY_ItemTask = "ItemTask";
    public static final String PROPERTY_ItemTaskStepResults = "ItemTaskStepResults";
     
    protected String id;
    protected String name;
    protected String description;
    protected boolean required;
    protected int type;
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_TIME = 3;
    public static final int TYPE_YESNO = 4;
    public static final int TYPE_SPECIES = 5;
    public static final int TYPE_SEX = 6;
    public static final int TYPE_AGE = 7;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("Text");
        hubType.addElement("Number");
        hubType.addElement("Date");
        hubType.addElement("Time");
        hubType.addElement("Yesno");
        hubType.addElement("Species");
        hubType.addElement("Sex");
        hubType.addElement("Age");
    }
    protected String min;
    protected String max;
    protected int seq;
     
    // Links to other objects.
    protected transient ItemTask itemTask;
    protected transient Hub hubItemTaskStepResults;
     
     
    public ItemTaskStep() {
    }
     
    public ItemTaskStep(String id) {
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
    
     
    public boolean getRequired() {
        return required;
    }
    public void setRequired(boolean newValue) {
        boolean old = this.required;
        this.required = newValue;
        firePropertyChange(PROPERTY_Required, old, this.required);
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
    
     
    public String getMin() {
        return min;
    }
    public void setMin(String newValue) {
        String old = this.min;
        this.min = newValue;
        firePropertyChange(PROPERTY_Min, old, this.min);
    }
    
     
    public String getMax() {
        return max;
    }
    public void setMax(String newValue) {
        String old = this.max;
        this.max = newValue;
        firePropertyChange(PROPERTY_Max, old, this.max);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getDisplayName() {
        String displayName; 
        
        if (name != null) displayName = name;
        else displayName = "";
        return "STEP #"+ (seq+1) + " "+ displayName;
    }
     
    public ItemTask getItemTask() {
        if (itemTask == null) {
            itemTask = (ItemTask) getObject(PROPERTY_ItemTask);
        }
        return itemTask;
    }
    
    public void setItemTask(ItemTask newValue) {
        ItemTask old = this.itemTask;
        this.itemTask = newValue;
        firePropertyChange(PROPERTY_ItemTask, old, this.itemTask);
    }
     
    public Hub getItemTaskStepResults() {
        if (hubItemTaskStepResults == null) {
            hubItemTaskStepResults = getHub(PROPERTY_ItemTaskStepResults, "seq");
            hubItemTaskStepResults.setAutoSequence("seq");
        }
        return hubItemTaskStepResults;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTaskSteps, ExamItemTaskStep.class, OALinkInfo.MANY, false, false, ExamItemTaskStep.PROPERTY_ItemTaskStep));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTask, ItemTask.class, OALinkInfo.ONE, false, false, ItemTask.PROPERTY_ItemTaskSteps));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTaskStepResults, ItemTaskStepResult.class, OALinkInfo.MANY, true, true, ItemTaskStepResult.PROPERTY_ItemTaskStep));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_DisplayName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
