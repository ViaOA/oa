// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import java.io.Serializable;

import com.tmgsc.hifivetest.model.oa.*;
 
public class InspireApprovalPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmailPPx email;
    private EmployeePPx employee;
    private InspireAwardLevelPPx inspireAwardLevel;
    private InspireRecipientPPx inspireRecipient;
    private EmailPPx reminderEmails;
     
    public InspireApprovalPPx(String name) {
        this(null, name);
    }

    public InspireApprovalPPx(PPxInterface parent, String name) {
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

    public EmailPPx email() {
        if (email == null) email = new EmailPPx(this, InspireApproval.P_Email);
        return email;
    }

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, InspireApproval.P_Employee);
        return employee;
    }

    public InspireAwardLevelPPx inspireAwardLevel() {
        if (inspireAwardLevel == null) inspireAwardLevel = new InspireAwardLevelPPx(this, InspireApproval.P_InspireAwardLevel);
        return inspireAwardLevel;
    }

    public InspireRecipientPPx inspireRecipient() {
        if (inspireRecipient == null) inspireRecipient = new InspireRecipientPPx(this, InspireApproval.P_InspireRecipient);
        return inspireRecipient;
    }

    public EmailPPx reminderEmails() {
        if (reminderEmails == null) reminderEmails = new EmailPPx(this, InspireApproval.P_ReminderEmails);
        return reminderEmails;
    }

    public String id() {
        return pp + "." + InspireApproval.P_Id;
    }

    public String created() {
        return pp + "." + InspireApproval.P_Created;
    }

    public String status() {
        return pp + "." + InspireApproval.P_Status;
    }

    public String statusDate() {
        return pp + "." + InspireApproval.P_StatusDate;
    }

    public String comments() {
        return pp + "." + InspireApproval.P_Comments;
    }

    public String updateEmail() {
        return pp + ".updateEmail";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
