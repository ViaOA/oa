// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class EmailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmployeeAwardPPx employeeAwardConfirm;
    private EmployeeAwardPPx employeeAwardManagerNotify;
    private EmployeeAwardPPx employeeAwardNotify;
    private EmployeeAwardPPx employeeAwardShipped;
    private EmployeeEcardPPx employeeEcardConfirmed;
    private EmployeeEcardPPx employeeEcardDelivered;
    private EmployeeEcardToPPx employeeEcardTo;
    private InspirePPx inspire;
    private InspireApprovalPPx inspireApproval;
    private InspireApprovalPPx inspireApprovalReminder;
    private InspireOrderPPx inspireOrder;
    private InspireRecipientPPx inspireRecipient;
    private InspireRecipientPPx inspireRecipientCompleted;
    private LocationEmailTypePPx locationEmailType;
    private PointsRecordPPx pointsRecord;
    private PointsRequestPPx pointsRequest;
    private ProgramEmailTypePPx programEmailType;
     
    public EmailPPx(String name) {
        this(null, name);
    }

    public EmailPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public EmployeeAwardPPx employeeAwardConfirm() {
        if (employeeAwardConfirm == null) employeeAwardConfirm = new EmployeeAwardPPx(this, Email.P_EmployeeAwardConfirm);
        return employeeAwardConfirm;
    }

    public EmployeeAwardPPx employeeAwardManagerNotify() {
        if (employeeAwardManagerNotify == null) employeeAwardManagerNotify = new EmployeeAwardPPx(this, Email.P_EmployeeAwardManagerNotify);
        return employeeAwardManagerNotify;
    }

    public EmployeeAwardPPx employeeAwardNotify() {
        if (employeeAwardNotify == null) employeeAwardNotify = new EmployeeAwardPPx(this, Email.P_EmployeeAwardNotify);
        return employeeAwardNotify;
    }

    public EmployeeAwardPPx employeeAwardShipped() {
        if (employeeAwardShipped == null) employeeAwardShipped = new EmployeeAwardPPx(this, Email.P_EmployeeAwardShipped);
        return employeeAwardShipped;
    }

    public EmployeeEcardPPx employeeEcardConfirmed() {
        if (employeeEcardConfirmed == null) employeeEcardConfirmed = new EmployeeEcardPPx(this, Email.P_EmployeeEcardConfirmed);
        return employeeEcardConfirmed;
    }

    public EmployeeEcardPPx employeeEcardDelivered() {
        if (employeeEcardDelivered == null) employeeEcardDelivered = new EmployeeEcardPPx(this, Email.P_EmployeeEcardDelivered);
        return employeeEcardDelivered;
    }

    public EmployeeEcardToPPx employeeEcardTo() {
        if (employeeEcardTo == null) employeeEcardTo = new EmployeeEcardToPPx(this, Email.P_EmployeeEcardTo);
        return employeeEcardTo;
    }

    public InspirePPx inspire() {
        if (inspire == null) inspire = new InspirePPx(this, Email.P_Inspire);
        return inspire;
    }

    public InspireApprovalPPx inspireApproval() {
        if (inspireApproval == null) inspireApproval = new InspireApprovalPPx(this, Email.P_InspireApproval);
        return inspireApproval;
    }

    public InspireApprovalPPx inspireApprovalReminder() {
        if (inspireApprovalReminder == null) inspireApprovalReminder = new InspireApprovalPPx(this, Email.P_InspireApprovalReminder);
        return inspireApprovalReminder;
    }

    public InspireOrderPPx inspireOrder() {
        if (inspireOrder == null) inspireOrder = new InspireOrderPPx(this, Email.P_InspireOrder);
        return inspireOrder;
    }

    public InspireRecipientPPx inspireRecipient() {
        if (inspireRecipient == null) inspireRecipient = new InspireRecipientPPx(this, Email.P_InspireRecipient);
        return inspireRecipient;
    }

    public InspireRecipientPPx inspireRecipientCompleted() {
        if (inspireRecipientCompleted == null) inspireRecipientCompleted = new InspireRecipientPPx(this, Email.P_InspireRecipientCompleted);
        return inspireRecipientCompleted;
    }

    public LocationEmailTypePPx locationEmailType() {
        if (locationEmailType == null) locationEmailType = new LocationEmailTypePPx(this, Email.P_LocationEmailType);
        return locationEmailType;
    }

    public PointsRecordPPx pointsRecord() {
        if (pointsRecord == null) pointsRecord = new PointsRecordPPx(this, Email.P_PointsRecord);
        return pointsRecord;
    }

    public PointsRequestPPx pointsRequest() {
        if (pointsRequest == null) pointsRequest = new PointsRequestPPx(this, Email.P_PointsRequest);
        return pointsRequest;
    }

    public ProgramEmailTypePPx programEmailType() {
        if (programEmailType == null) programEmailType = new ProgramEmailTypePPx(this, Email.P_ProgramEmailType);
        return programEmailType;
    }

    public String id() {
        return pp + "." + Email.P_Id;
    }

    public String created() {
        return pp + "." + Email.P_Created;
    }

    public String fromEmail() {
        return pp + "." + Email.P_FromEmail;
    }

    public String toEmail() {
        return pp + "." + Email.P_ToEmail;
    }

    public String ccEmail() {
        return pp + "." + Email.P_CcEmail;
    }

    public String subject() {
        return pp + "." + Email.P_Subject;
    }

    public String sentDateTime() {
        return pp + "." + Email.P_SentDateTime;
    }

    public String cancelDate() {
        return pp + "." + Email.P_CancelDate;
    }

    public String body() {
        return pp + "." + Email.P_Body;
    }

    public String attachment() {
        return pp + "." + Email.P_Attachment;
    }

    public String attachmentName() {
        return pp + "." + Email.P_AttachmentName;
    }

    public String attachmentMimeType() {
        return pp + "." + Email.P_AttachmentMimeType;
    }

    public String open() {
        return pp + "." + Email.P_Open;
    }

    public String sendEmail() {
        return pp + ".sendEmail";
    }

    public String updateEmail() {
        return pp + ".updateEmail";
    }

    public String viewAttachment() {
        return pp + ".viewAttachment";
    }

    public String cancel() {
        return pp + ".cancel";
    }

    public String openFilter() {
        return pp + ":open()";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
