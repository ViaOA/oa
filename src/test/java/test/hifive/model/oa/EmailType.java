// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;
 
@OAClass(
    shortName = "et",
    displayName = "Email Type",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "seq"
)
@OATable(
)
public class EmailType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
    public static final String PROPERTY_Subject = "Subject";
    public static final String P_Subject = "Subject";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
    public static final String PROPERTY_Note = "Note";
    public static final String P_Note = "Note";
     
     
    public static final String PROPERTY_LocationEmailTypes = "LocationEmailTypes";
    public static final String P_LocationEmailTypes = "LocationEmailTypes";
    public static final String PROPERTY_ProgramEmailTypes = "ProgramEmailTypes";
    public static final String P_ProgramEmailTypes = "ProgramEmailTypes";

    protected boolean testFlag;
    protected boolean aTestFlag;
    protected int id;
    protected OADate created;
    protected int seq;
    protected String name;
    protected int type;
    public static final int TYPE_Inspire = 0;
    public static final int TYPE_InspireApproval = 1;
    public static final int TYPE_InspireRecipient = 2;
    public static final int TYPE_InspireCertificate = 3;
    public static final int TYPE_InspireOrder = 4;
    public static final int TYPE_ForgotPassword = 5;
    public static final int TYPE_ECardTo = 6;
    public static final int TYPE_EmployeeAwardNotify = 7;
    public static final int TYPE_EmployeeAwardManagerNotify = 8;
    public static final int TYPE_EmployeeAwardConfirm = 9;
    public static final int TYPE_EmployeeAwardShipped = 10;
    public static final int TYPE_Register = 11;
    public static final int TYPE_ECardConfirmed = 12;
    public static final int TYPE_ECardDelivered = 13;
    public static final int TYPE_ResetPassword = 14;
    public static final int TYPE_ECardToPdf = 15;
    public static final int TYPE_InspireRecipientCompleted = 16;
    public static final int TYPE_InspireApprovalReminder = 17;
    public static final int TYPE_PointsUploadAutomatedRequest = 18;
    public static final int TYPE_PointsBuyNotify = 19;
    public static final int TYPE_PointsBuyAccounting = 20;
    public static final int TYPE_PointsBuyAdminNotify = 21;
    public static final int TYPE_PointsShoppingNotify = 22;
    public static final int TYPE_PointsDistributeRecipientNotify = 23;
    public static final int TYPE_PointsDistributeRequestorNofity = 24;
    public static final int TYPE_PointsDistributeAdminNotify = 25;
    public static final int TYPE_PointsTransferFromNotify = 26;
    public static final int TYPE_PointsTransferToNotify = 27;
    public static final int TYPE_PointsTransferRequesterNotify = 28;
    public static final int TYPE_PointsRequestApproverNotify = 29;
    public static final int TYPE_PointsRequestApproval = 30;
    public static final int TYPE_PointsRequestDenial = 31;
    public static final int TYPE_PointsRecognitionRecipientNotify = 32;
    public static final int TYPE_PointsRecognitionRecognizerNotify = 33;
    public static final int TYPE_PointsRecognitionThresholdNotify = 34;
    public static final int TYPE_PointsNominationToApprove = 35;
    public static final int TYPE_PointsNominationToNominator = 36;
    public static final int TYPE_PointsNominationDenied = 37;
    public static final int TYPE_PointsNominationApproved = 38;
    public static final int TYPE_PointsNominationThreshold = 39;
    public static final int TYPE_PointsNominationCertificate = 40;
    public static final int TYPE_PointsNominationToRecipientOnApproval = 41;
    public static final int TYPE_PointsNominationApprovedNotifyManager = 42;
    public static final int TYPE_PointsNominationApprovalReminder = 43;
    public static final int TYPE_PointsNominationCertificateEmail = 44;
    public static final int TYPE_PointsNominationHRBPNotification = 45;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("Inspire");
        hubType.addElement("Inspire Approval");
        hubType.addElement("Inspire Recipient");
        hubType.addElement("Inspire Certificate");
        hubType.addElement("Inspire Order");
        hubType.addElement("Forgot Password");
        hubType.addElement("ECard To");
        hubType.addElement("Employee Award Notify");
        hubType.addElement("Employee Award Manager Notify");
        hubType.addElement("Employee Award Confirm");
        hubType.addElement("Employee Award Shipped");
        hubType.addElement("Register");
        hubType.addElement("ECard Confirmed");
        hubType.addElement("ECard Delivered");
        hubType.addElement("Reset Password");
        hubType.addElement("ECard To Pdf");
        hubType.addElement("Inspire Recipient Completed");
        hubType.addElement("Inspire Approval Reminder");
        hubType.addElement("Points Upload Automated Request");
        hubType.addElement("Points Buy Notify");
        hubType.addElement("Points Buy Accounting");
        hubType.addElement("Points Buy Admin Notify");
        hubType.addElement("Points Shopping Notify");
        hubType.addElement("Points Distribute Recipient Notify");
        hubType.addElement("Points Distribute Requestor Nofity");
        hubType.addElement("Points Distribute Admin Notify");
        hubType.addElement("Points Transfer From Notify");
        hubType.addElement("Points Transfer To Notify");
        hubType.addElement("Points Transfer Requester Notify");
        hubType.addElement("Points Request Approver Notify");
        hubType.addElement("Points Request Approval");
        hubType.addElement("Points Request Denial");
        hubType.addElement("Points Recognition Recipient Notify");
        hubType.addElement("Points Recognition Recognizer Notify");
        hubType.addElement("Points Recognition Threshold Notify");
        hubType.addElement("Points Nomination To Approve");
        hubType.addElement("Points Nomination To Nominator");
        hubType.addElement("Points Nomination Denied");
        hubType.addElement("Points Nomination Approved");
        hubType.addElement("Points Nomination Threshold");
        hubType.addElement("Points Nomination Certificate");
        hubType.addElement("Points Nomination To Recipient On Approval");
        hubType.addElement("Points Nomination Approved Notify Manager");
        hubType.addElement("Points Nomination Approval Reminder");
        hubType.addElement("Points Nomination Certificate Email");
        hubType.addElement("Points Nomination HRBPNotification");
    }
    protected String subject;
    protected String text;
    protected String note;
     
    // Links to other objects.
     
    public EmailType() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public EmailType(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreated() {
        return created;
    }
    
    public void setCreated(OADate newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADate old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAProperty(maxLength = 75, displayLength = 40)
    @OAColumn(maxLength = 75)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayLength = 45, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        fireBeforePropertyChange(P_Type, this.type, newValue);
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    public String getTypeAsString() {
        if (isNull(P_Type)) return "";
        String s = hubType.getAt(getType());
        if (s == null) s = "";
        return s;
    }
    @OAProperty(maxLength = 150, isUnicode = true, displayLength = 25)
    @OAColumn(maxLength = 150)
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String newValue) {
        fireBeforePropertyChange(P_Subject, this.subject, newValue);
        String old = subject;
        this.subject = newValue;
        firePropertyChange(P_Subject, old, this.subject);
    }
    @OAProperty(maxLength = 4, isUnicode = true, displayLength = 4)
    @OAColumn(name = "TextValue", sqlType = java.sql.Types.CLOB)
    public String getText() {
        return text;
    }
    
    public void setText(String newValue) {
        fireBeforePropertyChange(P_Text, this.text, newValue);
        String old = text;
        this.text = newValue;
        firePropertyChange(P_Text, old, this.text);
    }
    @OAProperty(maxLength = 4, displayLength = 4)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getNote() {
        return note;
    }
    
    public void setNote(String newValue) {
        fireBeforePropertyChange(P_Note, this.note, newValue);
        String old = note;
        this.note = newValue;
        firePropertyChange(P_Note, old, this.note);
    }
    @OAMany(
        displayName = "Location Email Types", 
        toClass = LocationEmailType.class, 
        reverseName = LocationEmailType.P_EmailType, 
        createMethod = false
    )
    private Hub<LocationEmailType> getLocationEmailTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Program Email Types", 
        toClass = ProgramEmailType.class, 
        reverseName = ProgramEmailType.P_EmailType, 
        createMethod = false
    )
    private Hub<ProgramEmailType> getProgramEmailTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    // textFieldClass - this will return the root class used to insert fields into HTMLTextPane
    /**
        This is the method that the OAHTMLTextPane editor will look for to be able to 
        get the root class for inserting dynamic field values.
        pattern:  "get" + textFieldName + "Class()"
    */
    /*qqqq
    public Class getTextFieldClass() {
        return EmailTypeDelegate.getTextFieldClass(this);
    }
    */
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, EmailType.P_Seq, true);
        }
        this.name = rs.getString(4);
        this.type = (int) rs.getInt(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, EmailType.P_Type, true);
        }
        this.subject = rs.getString(6);
        this.text = rs.getString(7);
        this.note = rs.getString(8);
        if (rs.getMetaData().getColumnCount() != 8) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }

    @OAProperty(displayLength = 5)
    public boolean getTestFlag() {
        return testFlag;
    }
    public void setTestFlag(boolean newValue) {
        fireBeforePropertyChange("TestFlag", this.testFlag, newValue);
        boolean old = testFlag;
        this.testFlag = newValue;
        firePropertyChange("TestFlag", old, this.testFlag);
    }
    
    @OAProperty(displayLength = 5)
    public boolean getATestFlag() {
        return aTestFlag;
    }
    public void setATestFlag(boolean newValue) {
        fireBeforePropertyChange("ATestFlag", this.aTestFlag, newValue);
        boolean old = aTestFlag;
        this.aTestFlag = newValue;
        firePropertyChange("ATestFlag", old, this.aTestFlag);
    }
    
}
 
