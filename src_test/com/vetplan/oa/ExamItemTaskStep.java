package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ExamItemTaskStep extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Required = "Required";
    public static final String PROPERTY_Type = "Type";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Min = "Min";
    public static final String PROPERTY_Max = "Max";
     
     
    public static final String PROPERTY_ExamItemTask = "ExamItemTask";
    public static final String PROPERTY_ExamItemTaskStepResults = "ExamItemTaskStepResults";
    public static final String PROPERTY_ItemTaskStep = "ItemTaskStep";
     
    protected String id;
    protected String name;
    protected String description;
    protected boolean required;
    protected int type;
    protected int seq;
    protected String min;
    protected String max;
     
    // Links to other objects.
    protected transient ExamItemTask examItemTask;
    protected transient Hub hubExamItemTaskStepResults;
    protected transient ItemTaskStep itemTaskStep;
     
     
    public ExamItemTaskStep() {
    }
     
    public ExamItemTaskStep(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
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
    
     
    public ExamItemTask getExamItemTask() {
        if (examItemTask == null) {
            examItemTask = (ExamItemTask) getObject(PROPERTY_ExamItemTask);
        }
        return examItemTask;
    }
    
    public void setExamItemTask(ExamItemTask newValue) {
        ExamItemTask old = this.examItemTask;
        this.examItemTask = newValue;
        firePropertyChange(PROPERTY_ExamItemTask, old, this.examItemTask);
    }
     
    public Hub getExamItemTaskStepResults() {
        if (hubExamItemTaskStepResults == null) {
            hubExamItemTaskStepResults = getHub(PROPERTY_ExamItemTaskStepResults, "seq");
            hubExamItemTaskStepResults.setAutoSequence("seq");
        }
        return hubExamItemTaskStepResults;
    }
    
     
    public ItemTaskStep getItemTaskStep() {
        if (itemTaskStep == null) {
            itemTaskStep = (ItemTaskStep) getObject(PROPERTY_ItemTaskStep);
        }
        return itemTaskStep;
    }
    
    public void setItemTaskStep(ItemTaskStep newValue) {
        ItemTaskStep old = this.itemTaskStep;
        this.itemTaskStep = newValue;
        firePropertyChange(PROPERTY_ItemTaskStep, old, this.itemTaskStep);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, thisOwner, recursive)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTask, ExamItemTask.class, OALinkInfo.ONE, false, false, ExamItemTask.PROPERTY_ExamItemTaskSteps));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTaskStepResults, ExamItemTaskStepResult.class, OALinkInfo.MANY, true, true, ExamItemTaskStepResult.PROPERTY_ExamItemTaskStep, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTaskStep, ItemTaskStep.class, OALinkInfo.ONE, false, false, ItemTaskStep.PROPERTY_ExamItemTaskSteps));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
