// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class EmailPP {
    private static EmployeeAwardPPx employeeAwardConfirm;
    private static EmployeeAwardPPx employeeAwardManagerNotify;
    private static EmployeeAwardPPx employeeAwardNotify;
    private static EmployeeAwardPPx employeeAwardShipped;
    private static EmployeeEcardPPx employeeEcardConfirmed;
    private static EmployeeEcardPPx employeeEcardDelivered;
    private static EmployeeEcardToPPx employeeEcardTo;
    private static InspirePPx inspire;
    private static InspireApprovalPPx inspireApproval;
    private static InspireApprovalPPx inspireApprovalReminder;
    private static InspireOrderPPx inspireOrder;
    private static InspireRecipientPPx inspireRecipient;
    private static InspireRecipientPPx inspireRecipientCompleted;
    private static LocationEmailTypePPx locationEmailType;
    private static PointsRecordPPx pointsRecord;
    private static PointsRequestPPx pointsRequest;
    private static ProgramEmailTypePPx programEmailType;
     

    public static EmployeeAwardPPx employeeAwardConfirm() {
        if (employeeAwardConfirm == null) employeeAwardConfirm = new EmployeeAwardPPx(Email.P_EmployeeAwardConfirm);
        return employeeAwardConfirm;
    }

    public static EmployeeAwardPPx employeeAwardManagerNotify() {
        if (employeeAwardManagerNotify == null) employeeAwardManagerNotify = new EmployeeAwardPPx(Email.P_EmployeeAwardManagerNotify);
        return employeeAwardManagerNotify;
    }

    public static EmployeeAwardPPx employeeAwardNotify() {
        if (employeeAwardNotify == null) employeeAwardNotify = new EmployeeAwardPPx(Email.P_EmployeeAwardNotify);
        return employeeAwardNotify;
    }

    public static EmployeeAwardPPx employeeAwardShipped() {
        if (employeeAwardShipped == null) employeeAwardShipped = new EmployeeAwardPPx(Email.P_EmployeeAwardShipped);
        return employeeAwardShipped;
    }

    public static EmployeeEcardPPx employeeEcardConfirmed() {
        if (employeeEcardConfirmed == null) employeeEcardConfirmed = new EmployeeEcardPPx(Email.P_EmployeeEcardConfirmed);
        return employeeEcardConfirmed;
    }

    public static EmployeeEcardPPx employeeEcardDelivered() {
        if (employeeEcardDelivered == null) employeeEcardDelivered = new EmployeeEcardPPx(Email.P_EmployeeEcardDelivered);
        return employeeEcardDelivered;
    }

    public static EmployeeEcardToPPx employeeEcardTo() {
        if (employeeEcardTo == null) employeeEcardTo = new EmployeeEcardToPPx(Email.P_EmployeeEcardTo);
        return employeeEcardTo;
    }

    public static InspirePPx inspire() {
        if (inspire == null) inspire = new InspirePPx(Email.P_Inspire);
        return inspire;
    }

    public static InspireApprovalPPx inspireApproval() {
        if (inspireApproval == null) inspireApproval = new InspireApprovalPPx(Email.P_InspireApproval);
        return inspireApproval;
    }

    public static InspireApprovalPPx inspireApprovalReminder() {
        if (inspireApprovalReminder == null) inspireApprovalReminder = new InspireApprovalPPx(Email.P_InspireApprovalReminder);
        return inspireApprovalReminder;
    }

    public static InspireOrderPPx inspireOrder() {
        if (inspireOrder == null) inspireOrder = new InspireOrderPPx(Email.P_InspireOrder);
        return inspireOrder;
    }

    public static InspireRecipientPPx inspireRecipient() {
        if (inspireRecipient == null) inspireRecipient = new InspireRecipientPPx(Email.P_InspireRecipient);
        return inspireRecipient;
    }

    public static InspireRecipientPPx inspireRecipientCompleted() {
        if (inspireRecipientCompleted == null) inspireRecipientCompleted = new InspireRecipientPPx(Email.P_InspireRecipientCompleted);
        return inspireRecipientCompleted;
    }

    public static LocationEmailTypePPx locationEmailType() {
        if (locationEmailType == null) locationEmailType = new LocationEmailTypePPx(Email.P_LocationEmailType);
        return locationEmailType;
    }

    public static PointsRecordPPx pointsRecord() {
        if (pointsRecord == null) pointsRecord = new PointsRecordPPx(Email.P_PointsRecord);
        return pointsRecord;
    }

    public static PointsRequestPPx pointsRequest() {
        if (pointsRequest == null) pointsRequest = new PointsRequestPPx(Email.P_PointsRequest);
        return pointsRequest;
    }

    public static ProgramEmailTypePPx programEmailType() {
        if (programEmailType == null) programEmailType = new ProgramEmailTypePPx(Email.P_ProgramEmailType);
        return programEmailType;
    }

    public static String id() {
        String s = Email.P_Id;
        return s;
    }

    public static String created() {
        String s = Email.P_Created;
        return s;
    }

    public static String fromEmail() {
        String s = Email.P_FromEmail;
        return s;
    }

    public static String toEmail() {
        String s = Email.P_ToEmail;
        return s;
    }

    public static String ccEmail() {
        String s = Email.P_CcEmail;
        return s;
    }

    public static String subject() {
        String s = Email.P_Subject;
        return s;
    }

    public static String sentDateTime() {
        String s = Email.P_SentDateTime;
        return s;
    }

    public static String cancelDate() {
        String s = Email.P_CancelDate;
        return s;
    }

    public static String body() {
        String s = Email.P_Body;
        return s;
    }

    public static String attachment() {
        String s = Email.P_Attachment;
        return s;
    }

    public static String attachmentName() {
        String s = Email.P_AttachmentName;
        return s;
    }

    public static String attachmentMimeType() {
        String s = Email.P_AttachmentMimeType;
        return s;
    }

    public static String open() {
        String s = Email.P_Open;
        return s;
    }

    public static String sendEmail() {
        String s = "sendEmail";
        return s;
    }

    public static String updateEmail() {
        String s = "updateEmail";
        return s;
    }

    public static String viewAttachment() {
        String s = "viewAttachment";
        return s;
    }

    public static String cancel() {
        String s = "cancel";
        return s;
    }
}
 
