// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class HifivePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmployeePPx employee;
    private EmployeeSurveyPPx employeeSurvey;
    private EmployeePPx fromEmployee;
    private HifiveQualityPPx hifiveQualities;
    private HifiveReasonPPx hifiveReason;
    private EmployeePPx manager;
     
    public HifivePPx(String name) {
        this(null, name);
    }

    public HifivePPx(PPxInterface parent, String name) {
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
        if (employee == null) employee = new EmployeePPx(this, Hifive.P_Employee);
        return employee;
    }

    public EmployeeSurveyPPx employeeSurvey() {
        if (employeeSurvey == null) employeeSurvey = new EmployeeSurveyPPx(this, Hifive.P_EmployeeSurvey);
        return employeeSurvey;
    }

    public EmployeePPx fromEmployee() {
        if (fromEmployee == null) fromEmployee = new EmployeePPx(this, Hifive.P_FromEmployee);
        return fromEmployee;
    }

    public HifiveQualityPPx hifiveQualities() {
        if (hifiveQualities == null) hifiveQualities = new HifiveQualityPPx(this, Hifive.P_HifiveQualities);
        return hifiveQualities;
    }

    public HifiveReasonPPx hifiveReason() {
        if (hifiveReason == null) hifiveReason = new HifiveReasonPPx(this, Hifive.P_HifiveReason);
        return hifiveReason;
    }

    public EmployeePPx manager() {
        if (manager == null) manager = new EmployeePPx(this, Hifive.P_Manager);
        return manager;
    }

    public String id() {
        return pp + "." + Hifive.P_Id;
    }

    public String created() {
        return pp + "." + Hifive.P_Created;
    }

    public String approvedDate() {
        return pp + "." + Hifive.P_ApprovedDate;
    }

    public String ackDate() {
        return pp + "." + Hifive.P_AckDate;
    }

    public String points() {
        return pp + "." + Hifive.P_Points;
    }

    public String comment() {
        return pp + "." + Hifive.P_Comment;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
