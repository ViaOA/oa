package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ExamItemTask extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Required = "Required";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_ExamItem = "ExamItem";
    public static final String PROPERTY_ExamItemTaskSteps = "ExamItemTaskSteps";
    public static final String PROPERTY_SchDateTimes = "SchDateTimes";
     
    protected String id;
    protected String name;
    protected String description;
    protected boolean required;
    protected int seq;
     
    // Links to other objects.
    protected transient ExamItem examItem;
    protected transient Hub hubExamItemTaskSteps;
    protected transient SchDateTimes schDateTimes;
     
     
    public ExamItemTask() {
    }
     
    public ExamItemTask(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public ExamItem getExamItem() {
        if (examItem == null) {
            examItem = (ExamItem) getObject(PROPERTY_ExamItem);
        }
        return examItem;
    }
    
    public void setExamItem(ExamItem newValue) {
        ExamItem old = this.examItem;
        this.examItem = newValue;
        firePropertyChange(PROPERTY_ExamItem, old, this.examItem);
    }
     
    public Hub getExamItemTaskSteps() {
        if (hubExamItemTaskSteps == null) {
            hubExamItemTaskSteps = getHub(PROPERTY_ExamItemTaskSteps, "seq");
            hubExamItemTaskSteps.setAutoSequence("seq");
        }
        return hubExamItemTaskSteps;
    }
    
     
    public SchDateTimes getSchDateTimes() {
        if (schDateTimes == null) {
            schDateTimes = (SchDateTimes) getObject(PROPERTY_SchDateTimes);
        }
        return schDateTimes;
    }
    
    public void setSchDateTimes(SchDateTimes newValue) {
        SchDateTimes old = this.schDateTimes;
        this.schDateTimes = newValue;
        firePropertyChange(PROPERTY_SchDateTimes, old, this.schDateTimes);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, thisOwner, recursive)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItem, ExamItem.class, OALinkInfo.ONE, false, false, ExamItem.PROPERTY_ExamItemTasks));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTaskSteps, ExamItemTaskStep.class, OALinkInfo.MANY, true, true, ExamItemTaskStep.PROPERTY_ExamItemTask, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTimes, SchDateTimes.class, OALinkInfo.ONE, true, true, SchDateTimes.PROPERTY_ExamItemTasks, true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
