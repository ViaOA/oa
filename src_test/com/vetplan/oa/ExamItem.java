package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import java.math.BigDecimal;
import java.util.*;
import com.viaoa.util.*;
 
 
public class ExamItem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_CheckedValue = "CheckedValue";
    public static final String PROPERTY_Severity = "Severity";
    public static final String PROPERTY_Priority = "Priority";
    public static final String PROPERTY_Reminder = "Reminder";
    public static final String PROPERTY_CarriedForward = "CarriedForward";
    public static final String PROPERTY_AutoChecked = "AutoChecked";
    public static final String PROPERTY_DontPrint = "DontPrint";
    public static final String PROPERTY_Comment = "Comment";
    public static final String PROPERTY_TechDescription = "TechDescription";
    public static final String PROPERTY_ClientDescription = "ClientDescription";
    public static final String PROPERTY_Instruction = "Instruction";
    public static final String PROPERTY_NextDate = "NextDate";
    public static final String PROPERTY_Price = "Price";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_LastDate = "LastDate";
    public static final String PROPERTY_WellnessItem = "WellnessItem";
    public static final String PROPERTY_ReasonForVisit = "ReasonForVisit";
    public static final String PROPERTY_ReminderDate = "ReminderDate";
    public static final String PROPERTY_ClientDescriptionChanged = "ClientDescriptionChanged";
    public static final String PROPERTY_ExpiredDate = "ExpiredDate";
    public static final String PROPERTY_Quantity = "Quantity";
    public static final String PROPERTY_ReportType = "ReportType";
    public static final String PROPERTY_ShortDescriptionOnly = "ShortDescriptionOnly";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_Result = "Result";
     
    public static final String PROPERTY_GetExamItemStatusType = "GetExamItemStatusType";
    public static final String PROPERTY_TotalPrice = "TotalPrice";
     
    public static final String PROPERTY_Exam = "Exam";
    public static final String PROPERTY_User = "User";
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_ExamItemStatus = "ExamItemStatus";
    public static final String PROPERTY_SectionItem = "SectionItem";
    public static final String PROPERTY_Problem = "Problem";
    public static final String PROPERTY_ExamItemHistories = "ExamItemHistories";
    public static final String PROPERTY_ExamItemTasks = "ExamItemTasks";
     
    protected String id;
    protected int checkedValue;
    protected int severity;
    protected int priority;
    protected boolean reminder;
    protected boolean carriedForward;
    protected boolean autoChecked;
    protected boolean dontPrint;
    protected String comment;
    protected String techDescription;
    protected String clientDescription;
    protected String instruction;
    protected OADate nextDate;
    protected BigDecimal price;
    protected int seq;
    protected OADate lastDate;
    protected boolean wellnessItem;
    protected boolean reasonForVisit;
    protected OADate reminderDate;
    protected boolean clientDescriptionChanged;
    protected OADate expiredDate;
    protected int quantity;
    protected int reportType;
    protected boolean shortDescriptionOnly;
    protected String pmsId;
    protected String result;
     
    // Links to other objects.
    protected transient Exam exam;
    protected transient User user;
    protected transient Item item;
    public transient ExamItemStatus examItemStatus; // made public for debugging
    protected transient SectionItem sectionItem;
    protected transient Problem problem;
    protected transient Hub hubExamItemHistories;
    protected transient Hub hubExamItemTasks;
     
     
    public ExamItem() {
    }
     
    public ExamItem(String id) {
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
    
     
    public int getCheckedValue() {
        return checkedValue;
    }
    public void setCheckedValue(int newValue) {
        int old = this.checkedValue;
        this.checkedValue = newValue;
        firePropertyChange(PROPERTY_CheckedValue, old, this.checkedValue);
    }
    
     
    public int getSeverity() {
        return severity;
    }
    public void setSeverity(int newValue) {
        int old = this.severity;
        this.severity = newValue;
        firePropertyChange(PROPERTY_Severity, old, this.severity);
    }
    
     
    public int getPriority() {
        return priority;
    }
    public void setPriority(int newValue) {
        int old = this.priority;
        this.priority = newValue;
        firePropertyChange(PROPERTY_Priority, old, this.priority);
    }
    
     
    public boolean getReminder() {
        return reminder;
    }
    public void setReminder(boolean newValue) {
        boolean old = this.reminder;
        this.reminder = newValue;
        firePropertyChange(PROPERTY_Reminder, old, this.reminder);
    }
    
     
    public boolean getCarriedForward() {
        return carriedForward;
    }
    public void setCarriedForward(boolean newValue) {
        boolean old = this.carriedForward;
        this.carriedForward = newValue;
        firePropertyChange(PROPERTY_CarriedForward, old, this.carriedForward);
    }
    
     
    public boolean getAutoChecked() {
        return autoChecked;
    }
    public void setAutoChecked(boolean newValue) {
        boolean old = this.autoChecked;
        this.autoChecked = newValue;
        firePropertyChange(PROPERTY_AutoChecked, old, this.autoChecked);
    }
    
     
    public boolean getDontPrint() {
        return dontPrint;
    }
    public void setDontPrint(boolean newValue) {
        boolean old = this.dontPrint;
        this.dontPrint = newValue;
        firePropertyChange(PROPERTY_DontPrint, old, this.dontPrint);
    }
    
     
    public String getComment() {
        return comment;
    }
    public void setComment(String newValue) {
        String old = this.comment;
        this.comment = newValue;
        firePropertyChange(PROPERTY_Comment, old, this.comment);
    }
    
     
    public String getTechDescription() {
        return techDescription;
    }
    public void setTechDescription(String newValue) {
        String old = this.techDescription;
        this.techDescription = newValue;
        firePropertyChange(PROPERTY_TechDescription, old, this.techDescription);
    }
    
     
    public String getClientDescription() {
        return clientDescription;
    }
    public void setClientDescription(String newValue) {
        String old = this.clientDescription;
        this.clientDescription = newValue;
        firePropertyChange(PROPERTY_ClientDescription, old, this.clientDescription);
    }
    
     
    public String getInstruction() {
        return instruction;
    }
    public void setInstruction(String newValue) {
        String old = this.instruction;
        this.instruction = newValue;
        firePropertyChange(PROPERTY_Instruction, old, this.instruction);
    }
    
     
    public OADate getNextDate() {
        return nextDate;
    }
    public void setNextDate(OADate newValue) {
        OADate old = this.nextDate;
        this.nextDate = newValue;
        firePropertyChange(PROPERTY_NextDate, old, this.nextDate);
    }
    
     
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal newValue) {
        BigDecimal old = this.price;
        this.price = newValue;
        firePropertyChange(PROPERTY_Price, old, this.price);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public OADate getLastDate() {
        return lastDate;
    }
    public void setLastDate(OADate newValue) {
        OADate old = this.lastDate;
        this.lastDate = newValue;
        firePropertyChange(PROPERTY_LastDate, old, this.lastDate);
    }
    
     
    public boolean getWellnessItem() {
        return wellnessItem;
    }
    public void setWellnessItem(boolean newValue) {
        boolean old = this.wellnessItem;
        this.wellnessItem = newValue;
        firePropertyChange(PROPERTY_WellnessItem, old, this.wellnessItem);
    }
    
     
    public boolean getReasonForVisit() {
        return reasonForVisit;
    }
    public void setReasonForVisit(boolean newValue) {
        boolean old = this.reasonForVisit;
        this.reasonForVisit = newValue;
        firePropertyChange(PROPERTY_ReasonForVisit, old, this.reasonForVisit);
    }
    
     
    public OADate getReminderDate() {
        return reminderDate;
    }
    public void setReminderDate(OADate newValue) {
        OADate old = this.reminderDate;
        this.reminderDate = newValue;
        firePropertyChange(PROPERTY_ReminderDate, old, this.reminderDate);
    }
    
     
    public boolean getClientDescriptionChanged() {
        return clientDescriptionChanged;
    }
    public void setClientDescriptionChanged(boolean newValue) {
        boolean old = this.clientDescriptionChanged;
        this.clientDescriptionChanged = newValue;
        firePropertyChange(PROPERTY_ClientDescriptionChanged, old, this.clientDescriptionChanged);
    }
    
     
    public OADate getExpiredDate() {
        return expiredDate;
    }
    public void setExpiredDate(OADate newValue) {
        OADate old = this.expiredDate;
        this.expiredDate = newValue;
        firePropertyChange(PROPERTY_ExpiredDate, old, this.expiredDate);
    }
    
     
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int newValue) {
        int old = this.quantity;
        this.quantity = newValue;
        firePropertyChange(PROPERTY_Quantity, old, this.quantity);
    }
    
     
    public int getReportType() {
        return reportType;
    }
    public void setReportType(int newValue) {
        int old = this.reportType;
        this.reportType = newValue;
        firePropertyChange(PROPERTY_ReportType, old, this.reportType);
    }
    
     
    public boolean getShortDescriptionOnly() {
        return shortDescriptionOnly;
    }
    public void setShortDescriptionOnly(boolean newValue) {
        boolean old = this.shortDescriptionOnly;
        this.shortDescriptionOnly = newValue;
        firePropertyChange(PROPERTY_ShortDescriptionOnly, old, this.shortDescriptionOnly);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public String getResult() {
        return result;
    }
    public void setResult(String newValue) {
        String old = this.result;
        this.result = newValue;
        firePropertyChange(PROPERTY_Result, old, this.result);
    }
    
     
    public int getExamItemStatusType() {
        int result=0;
    
        ExamItemStatus eis = getExamItemStatus();
        if (eis != null) result = eis.getType();
        else {
        	/*
            if (getCheckedValue() == 1) result = ExamItemStatus.DONE;
            else if (autoChecked) result = ExamItemStatus.RECOMMEND;
            else result = ExamItemStatus.NONE;
            */
        }
        return result;
    }
     
    public BigDecimal getTotalPrice() {
        BigDecimal price = getPrice();
        if (price == null) return new BigDecimal(0.0);
        return price.multiply(new BigDecimal(getQuantity()));
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
     
    public Item getItem() {
        if (item == null) {
            item = (Item) getObject(PROPERTY_Item);
        }
        return item;
    }
    
    public void setItem(Item newValue) {
        Item old = this.item;
        this.item = newValue;
        firePropertyChange(PROPERTY_Item, old, this.item);
    }
     
    public ExamItemStatus getExamItemStatus() {
        if (examItemStatus == null) {
            examItemStatus = (ExamItemStatus) getObject(PROPERTY_ExamItemStatus);
        }
        return examItemStatus;
    }
    
    
    public int debug_setExamItemStatusCnt;
    public void setExamItemStatus(ExamItemStatus newValue) {
    	debug_setExamItemStatusCnt++;
        ExamItemStatus old = this.examItemStatus;
        this.examItemStatus = newValue;
        firePropertyChange(PROPERTY_ExamItemStatus, old, this.examItemStatus);
    }
     
    public SectionItem getSectionItem() {
        if (sectionItem == null) {
            sectionItem = (SectionItem) getObject(PROPERTY_SectionItem);
        }
        return sectionItem;
    }
    
    public void setSectionItem(SectionItem newValue) {
        SectionItem old = this.sectionItem;
        this.sectionItem = newValue;
        firePropertyChange(PROPERTY_SectionItem, old, this.sectionItem);
    }
     
    public Problem getProblem() {
        if (problem == null) {
            problem = (Problem) getObject(PROPERTY_Problem);
        }
        return problem;
    }
    
    public void setProblem(Problem newValue) {
        Problem old = this.problem;
        this.problem = newValue;
        firePropertyChange(PROPERTY_Problem, old, this.problem);
    }
     
    public Hub getExamItemHistories() {
        if (hubExamItemHistories == null) {
            hubExamItemHistories = getHub(PROPERTY_ExamItemHistories, "seq");
            hubExamItemHistories.setAutoSequence("seq");
        }
        return hubExamItemHistories;
    }
    
     
    public Hub getExamItemTasks() {
        if (hubExamItemTasks == null) {
            hubExamItemTasks = getHub(PROPERTY_ExamItemTasks, "seq");
            hubExamItemTasks.setAutoSequence("seq");
        }
        return hubExamItemTasks;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Exam, Exam.class, OALinkInfo.ONE, false, false, Exam.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_User, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemStatus, ExamItemStatus.class, OALinkInfo.ONE, false, false, ExamItemStatus.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItem, SectionItem.class, OALinkInfo.ONE, false, false, SectionItem.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Problem, Problem.class, OALinkInfo.ONE, false, false, Problem.PROPERTY_ExamItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemHistories, ExamItemHistory.class, OALinkInfo.MANY, true, true, ExamItemHistory.PROPERTY_ExamItem, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemTasks, ExamItemTask.class, OALinkInfo.MANY, true, true, ExamItemTask.PROPERTY_ExamItem, true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_GetExamItemStatusType, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_TotalPrice, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
