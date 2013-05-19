package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import com.viaoa.util.OATime;
 
 
public class ExamItemHistory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_Time = "Time";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Comment = "Comment";
     
     
    public static final String PROPERTY_User = "User";
    public static final String PROPERTY_ExamItem = "ExamItem";
    public static final String PROPERTY_ExamItemStatus = "ExamItemStatus";

    public static final String PROPERTY_Users = "Users";
     
    protected String id;
    protected OADate date;
    protected OATime time;
    protected int seq;
    protected String comment;
     
    // Links to other objects.
    protected transient User user;
    protected transient ExamItem examItem;
    protected transient ExamItemStatus examItemStatus;
     
     
    public ExamItemHistory() {
    }
     
    public ExamItemHistory(String id) {
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
    
     
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate newValue) {
        OADate old = this.date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    public OATime getTime() {
        return time;
    }
    public void setTime(OATime newValue) {
        OATime old = this.time;
        this.time = newValue;
        firePropertyChange(PROPERTY_Time, old, this.time);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getComment() {
        return comment;
    }
    public void setComment(String newValue) {
        String old = this.comment;
        this.comment = newValue;
        firePropertyChange(PROPERTY_Comment, old, this.comment);
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
     
    public ExamItemStatus getExamItemStatus() {
        if (examItemStatus == null) {
            examItemStatus = (ExamItemStatus) getObject(PROPERTY_ExamItemStatus);
        }
        return examItemStatus;
    }
    
    public void setExamItemStatus(ExamItemStatus newValue) {
        ExamItemStatus old = this.examItemStatus;
        this.examItemStatus = newValue;
        firePropertyChange(PROPERTY_ExamItemStatus, old, this.examItemStatus);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_User, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_ExamItemHistories, false));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItem, ExamItem.class, OALinkInfo.ONE, false, false, ExamItem.PROPERTY_ExamItemHistories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemStatus, ExamItemStatus.class, OALinkInfo.ONE, false, false, ExamItemStatus.PROPERTY_ExamItemHistories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Users, User.class, OALinkInfo.MANY, false, false, User.PROPERTY_ExamItemHistory));
        
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
