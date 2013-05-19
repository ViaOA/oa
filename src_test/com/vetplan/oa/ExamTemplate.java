package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ExamTemplate extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Note = "Note";
    public static final String PROPERTY_ShowOnReport = "ShowOnReport";
     
     
    public static final String PROPERTY_Exam = "Exam";
    public static final String PROPERTY_User = "User";
    public static final String PROPERTY_Template = "Template";
     
    protected String id;
    protected int seq;
    protected String note;
    protected boolean showOnReport;
     
    // Links to other objects.
    protected transient Exam exam;
    protected transient User user;
    protected transient Template template;
     
     
    public ExamTemplate() {
    }
     
    public ExamTemplate(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getNote() {
        return note;
    }
    public void setNote(String newValue) {
        String old = this.note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    public boolean getShowOnReport() {
        return showOnReport;
    }
    public void setShowOnReport(boolean newValue) {
        boolean old = this.showOnReport;
        this.showOnReport = newValue;
        firePropertyChange(PROPERTY_ShowOnReport, old, this.showOnReport);
    }
    
     
    public Exam getExam() {
        if (exam == null) {
            exam = (Exam) getObject(PROPERTY_Exam);
        }
        return exam;
    }
    
    public void setExam(Exam newValue) {
        Exam old = this.exam;
        this.exam = newValue;
        firePropertyChange(PROPERTY_Exam, old, this.exam);
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
     
    public Template getTemplate() {
        if (template == null) {
            template = (Template) getObject(PROPERTY_Template);
        }
        return template;
    }
    
    public void setTemplate(Template newValue) {
        Template old = this.template;
        this.template = newValue;
        firePropertyChange(PROPERTY_Template, old, this.template);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Exam, Exam.class, OALinkInfo.ONE, false, false, Exam.PROPERTY_ExamTemplates));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_User, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_ExamTemplates));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Template, Template.class, OALinkInfo.ONE, false, false, Template.PROPERTY_ExamTemplates));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
