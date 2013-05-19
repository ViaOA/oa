package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class User extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_LoginId = "LoginId";
    public static final String PROPERTY_Password = "Password";
    public static final String PROPERTY_Admin = "Admin";
    public static final String PROPERTY_Vet = "Vet";
    public static final String PROPERTY_FirstName = "FirstName";
    public static final String PROPERTY_LastName = "LastName";
    public static final String PROPERTY_Title = "Title";
    public static final String PROPERTY_PrefixName = "PrefixName";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String PROPERTY_InactiveReason = "InactiveReason";
    public static final String PROPERTY_PmsId = "PmsId";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_VetExams = "VetExams";
    public static final String PROPERTY_TechExams = "TechExams";
    public static final String PROPERTY_ReceptionistExams = "ReceptionistExams";
    public static final String PROPERTY_PreparedByExams = "PreparedByExams";
    public static final String PROPERTY_ExamItems = "ExamItems";
    public static final String PROPERTY_ExamTemplates = "ExamTemplates";
    public static final String PROPERTY_Versions = "Versions";
    public static final String PROPERTY_ExamItemHistories = "ExamItemHistories";
    public static final String PROPERTY_ExamItemTaskStepResults = "ExamItemTaskStepResults";
    
    public static final String PROPERTY_ExamItemHistory = "ExamItemHistory";
        
     
    protected String id;
    protected String loginId;
    protected String password;
    protected boolean admin;
    protected boolean vet;
    protected String firstName;
    protected String lastName;
    protected String title;
    protected String prefixName;
    protected OADate inactiveDate;
    protected String inactiveReason;
    protected String pmsId;
    
    protected transient ExamItemHistory examItemHistory;
     
    // Links to other objects.
     
     
    public ExamItemHistory getExamItemHistory() {
    	if (examItemHistory == null) examItemHistory = (ExamItemHistory) getObject(PROPERTY_ExamItemHistory);
    	return examItemHistory;
    }
    public void setExamItemHistory(ExamItemHistory eih) {
    	ExamItemHistory old = this.examItemHistory;
    	this.examItemHistory = eih;
        firePropertyChange(PROPERTY_ExamItemHistory, old, this.examItemHistory);
    }
    
    
    public User() {
    }
     
    public User(String id) {
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
    
     
    public String getLoginId() {
        return loginId;
    }
    public void setLoginId(String newValue) {
        String old = this.loginId;
        this.loginId = newValue;
        firePropertyChange(PROPERTY_LoginId, old, this.loginId);
    }
    
     
    public String getPassword() {
        return password;
    }
    public void setPassword(String newValue) {
        String old = this.password;
        this.password = newValue;
        firePropertyChange(PROPERTY_Password, old, this.password);
    }
    
     
    public boolean getAdmin() {
        return admin;
    }
    public void setAdmin(boolean newValue) {
        boolean old = this.admin;
        this.admin = newValue;
        firePropertyChange(PROPERTY_Admin, old, this.admin);
    }
    
     
    public boolean getVet() {
        return vet;
    }
    public void setVet(boolean newValue) {
        boolean old = this.vet;
        this.vet = newValue;
        firePropertyChange(PROPERTY_Vet, old, this.vet);
    }
    
     
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String newValue) {
        String old = this.firstName;
        this.firstName = newValue;
        firePropertyChange(PROPERTY_FirstName, old, this.firstName);
    }
    
     
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String newValue) {
        String old = this.lastName;
        this.lastName = newValue;
        firePropertyChange(PROPERTY_LastName, old, this.lastName);
    }
    
     
    public String getTitle() {
        return title;
    }
    public void setTitle(String newValue) {
        String old = this.title;
        this.title = newValue;
        firePropertyChange(PROPERTY_Title, old, this.title);
    }
    
     
    public String getPrefixName() {
        return prefixName;
    }
    public void setPrefixName(String newValue) {
        String old = this.prefixName;
        this.prefixName = newValue;
        firePropertyChange(PROPERTY_PrefixName, old, this.prefixName);
    }
    
     
    public OADate getInactiveDate() {
        return inactiveDate;
    }
    public void setInactiveDate(OADate newValue) {
        OADate old = this.inactiveDate;
        this.inactiveDate = newValue;
        firePropertyChange(PROPERTY_InactiveDate, old, this.inactiveDate);
    }
    
     
    public String getInactiveReason() {
        return inactiveReason;
    }
    public void setInactiveReason(String newValue) {
        String old = this.inactiveReason;
        this.inactiveReason = newValue;
        firePropertyChange(PROPERTY_InactiveReason, old, this.inactiveReason);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public String getFullName() {
        String fullName = "";
        if (prefixName != null) fullName = prefixName+" ";
        if (firstName != null) fullName += firstName+" ";
        if (lastName != null) fullName += lastName;
        return fullName;
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_VetExams, Exam.class, OALinkInfo.MANY, false, false, Exam.PROPERTY_VetUser));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TechExams, Exam.class, OALinkInfo.MANY, false, false, Exam.PROPERTY_TechUser));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ReceptionistExams, Exam.class, OALinkInfo.MANY, false, false, Exam.PROPERTY_ReceptionistUser));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_PreparedByExams, Exam.class, OALinkInfo.MANY, false, false, Exam.PROPERTY_PreparedByUser));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, false, false, ExamItem.PROPERTY_User));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamTemplates, ExamTemplate.class, OALinkInfo.MANY, false, false, ExamTemplate.PROPERTY_User));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Versions, Version.class, OALinkInfo.MANY, false, false, Version.PROPERTY_User));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemHistories, ExamItemHistory.class, OALinkInfo.MANY, false, false, ExamItemHistory.PROPERTY_User));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTaskStepResults, ExamItemTaskStepResult.class, OALinkInfo.MANY, false, false, ExamItemTaskStepResult.PROPERTY_User));
         
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemHistory, ExamItemHistory.class, OALinkInfo.ONE, false, false, ExamItemHistory.PROPERTY_Users));
        
                
        
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
