// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class InspireAwardLevelPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmployeePPx employee;
    private InspireApprovalPPx inspireApprovals;
    private InspireAwardLevelLocationValuePPx inspireAwardLevelLocationValues;
    private InspireRecipientPPx inspireRecipients;
    private InspirePPx inspires;
    private ProgramPPx program;
     
    public InspireAwardLevelPPx(String name) {
        this(null, name);
    }

    public InspireAwardLevelPPx(PPxInterface parent, String name) {
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

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, InspireAwardLevel.P_Employee);
        return employee;
    }

    public InspireApprovalPPx inspireApprovals() {
        if (inspireApprovals == null) inspireApprovals = new InspireApprovalPPx(this, InspireAwardLevel.P_InspireApprovals);
        return inspireApprovals;
    }

    public InspireAwardLevelLocationValuePPx inspireAwardLevelLocationValues() {
        if (inspireAwardLevelLocationValues == null) inspireAwardLevelLocationValues = new InspireAwardLevelLocationValuePPx(this, InspireAwardLevel.P_InspireAwardLevelLocationValues);
        return inspireAwardLevelLocationValues;
    }

    public InspireRecipientPPx inspireRecipients() {
        if (inspireRecipients == null) inspireRecipients = new InspireRecipientPPx(this, InspireAwardLevel.P_InspireRecipients);
        return inspireRecipients;
    }

    public InspirePPx inspires() {
        if (inspires == null) inspires = new InspirePPx(this, InspireAwardLevel.P_Inspires);
        return inspires;
    }

    public ProgramPPx program() {
        if (program == null) program = new ProgramPPx(this, InspireAwardLevel.P_Program);
        return program;
    }

    public String id() {
        return pp + "." + InspireAwardLevel.P_Id;
    }

    public String name() {
        return pp + "." + InspireAwardLevel.P_Name;
    }

    public String seq() {
        return pp + "." + InspireAwardLevel.P_Seq;
    }

    public String approvalLevels() {
        return pp + "." + InspireAwardLevel.P_ApprovalLevels;
    }

    public String approveFromTop() {
        return pp + "." + InspireAwardLevel.P_ApproveFromTop;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
