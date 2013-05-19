package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import com.viaoa.util.OATime;
 
 
public class ExamItemTaskStepResult extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Result = "Result";
    public static final String PROPERTY_DoneDate = "DoneDate";
    public static final String PROPERTY_DoneTime = "DoneTime";
    public static final String PROPERTY_DueDate = "DueDate";
    public static final String PROPERTY_DueTime = "DueTime";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_ExamItemTaskStep = "ExamItemTaskStep";
    public static final String PROPERTY_User = "User";
     
    protected String id;
    protected String result;
    protected OADate doneDate;
    protected OATime doneTime;
    protected OADate dueDate;
    protected OATime dueTime;
    protected int seq;
     
    // Links to other objects.
    protected transient ExamItemTaskStep examItemTaskStep;
    protected transient User user;
     
     
    public ExamItemTaskStepResult() {
    }
     
    public ExamItemTaskStepResult(String id) {
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
    
     
    public String getResult() {
        return result;
    }
    public void setResult(String newValue) {
        String old = this.result;
        this.result = newValue;
        firePropertyChange(PROPERTY_Result, old, this.result);
    }
    
     
    public OADate getDoneDate() {
        return doneDate;
    }
    public void setDoneDate(OADate newValue) {
        OADate old = this.doneDate;
        this.doneDate = newValue;
        firePropertyChange(PROPERTY_DoneDate, old, this.doneDate);
    }
    
     
    public OATime getDoneTime() {
        return doneTime;
    }
    public void setDoneTime(OATime newValue) {
        OATime old = this.doneTime;
        this.doneTime = newValue;
        firePropertyChange(PROPERTY_DoneTime, old, this.doneTime);
    }
    
     
    public OADate getDueDate() {
        return dueDate;
    }
    public void setDueDate(OADate newValue) {
        OADate old = this.dueDate;
        this.dueDate = newValue;
        firePropertyChange(PROPERTY_DueDate, old, this.dueDate);
    }
    
     
    public OATime getDueTime() {
        return dueTime;
    }
    public void setDueTime(OATime newValue) {
        OATime old = this.dueTime;
        this.dueTime = newValue;
        firePropertyChange(PROPERTY_DueTime, old, this.dueTime);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public ExamItemTaskStep getExamItemTaskStep() {
        if (examItemTaskStep == null) {
            examItemTaskStep = (ExamItemTaskStep) getObject(PROPERTY_ExamItemTaskStep);
        }
        return examItemTaskStep;
    }
    
    public void setExamItemTaskStep(ExamItemTaskStep newValue) {
        ExamItemTaskStep old = this.examItemTaskStep;
        this.examItemTaskStep = newValue;
        firePropertyChange(PROPERTY_ExamItemTaskStep, old, this.examItemTaskStep);
    }
     
    public User getUser() {
        if (user == null) {
            user = (User) getObject(PROPERTY_User);
        }
        return user;
    }
    
    public void setUser(User newValue) {
        User old = this.user;
        this.user = newValue;
        firePropertyChange(PROPERTY_User, old, this.user);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTaskStep, ExamItemTaskStep.class, OALinkInfo.ONE, false, false, ExamItemTaskStep.PROPERTY_ExamItemTaskStepResults));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_User, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_ExamItemTaskStepResults));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
