// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class InspireRecipientPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmployeePPx approvedReceipientEmployee;
    private EmailPPx completedEmail;
    private EmailPPx email;
    private EmployeePPx employee;
    private InspirePPx inspire;
    private InspireApprovalPPx inspireApprovals;
    private InspireAwardLevelPPx inspireAwardLevel;
    private InspireAwardLevelLocationValuePPx inspireAwardLevelLocationValue;
    private ProgramPPx program;
     
    public InspireRecipientPPx(String name) {
        this(null, name);
    }

    public InspireRecipientPPx(PPxInterface parent, String name) {
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

    public EmployeePPx approvedReceipientEmployee() {
        if (approvedReceipientEmployee == null) approvedReceipientEmployee = new EmployeePPx(this, InspireRecipient.P_ApprovedReceipientEmployee);
        return approvedReceipientEmployee;
    }

    public EmailPPx completedEmail() {
        if (completedEmail == null) completedEmail = new EmailPPx(this, InspireRecipient.P_CompletedEmail);
        return completedEmail;
    }

    public EmailPPx email() {
        if (email == null) email = new EmailPPx(this, InspireRecipient.P_Email);
        return email;
    }

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, InspireRecipient.P_Employee);
        return employee;
    }

    public InspirePPx inspire() {
        if (inspire == null) inspire = new InspirePPx(this, InspireRecipient.P_Inspire);
        return inspire;
    }

    public InspireApprovalPPx inspireApprovals() {
        if (inspireApprovals == null) inspireApprovals = new InspireApprovalPPx(this, InspireRecipient.P_InspireApprovals);
        return inspireApprovals;
    }

    public InspireAwardLevelPPx inspireAwardLevel() {
        if (inspireAwardLevel == null) inspireAwardLevel = new InspireAwardLevelPPx(this, InspireRecipient.P_InspireAwardLevel);
        return inspireAwardLevel;
    }

    public InspireAwardLevelLocationValuePPx inspireAwardLevelLocationValue() {
        if (inspireAwardLevelLocationValue == null) inspireAwardLevelLocationValue = new InspireAwardLevelLocationValuePPx(this, InspireRecipient.P_InspireAwardLevelLocationValue);
        return inspireAwardLevelLocationValue;
    }

    public ProgramPPx program() {
        if (program == null) program = new ProgramPPx(this, InspireRecipient.P_Program);
        return program;
    }

    public String id() {
        return pp + "." + InspireRecipient.P_Id;
    }

    public String points() {
        return pp + "." + InspireRecipient.P_Points;
    }

    public String completedDate() {
        return pp + "." + InspireRecipient.P_CompletedDate;
    }

    public String approvalStatus() {
        return pp + "." + InspireRecipient.P_ApprovalStatus;
    }

    public String hasMissingManager() {
        return pp + "." + InspireRecipient.P_HasMissingManager;
    }

    public String certficatePdfBytes() {
        return pp + "." + InspireRecipient.P_CertficatePdfBytes;
    }

    public String approved() {
        return pp + "." + InspireRecipient.P_Approved;
    }

    public String approvalStatusAsString() {
        return pp + "." + InspireRecipient.P_ApprovalStatusAsString;
    }

    public String updateApprovals() {
        return pp + ".updateApprovals";
    }

    public String updateEmail() {
        return pp + ".updateEmail";
    }

    public String viewCertificatePdf() {
        return pp + ".viewCertificatePdf";
    }

    public String openFilter() {
        return pp + ":open()";
    }

    public String missingManagerFilter() {
        return pp + ":missingManager()";
    }

    public String approvedOnlyFilter() {
        return pp + ":approvedOnly()";
    }

    public String recentFilter() {
        return pp + ":recent()";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
