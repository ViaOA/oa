// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class InspireAwardLevelLocationValuePP {
    private static EmployeePPx employee;
    private static InspireAwardLevelPPx inspireAwardLevel;
    private static InspireRecipientPPx inspireRecipients;
    private static LocationPPx location;
     

    public static EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(InspireAwardLevelLocationValue.P_Employee);
        return employee;
    }

    public static InspireAwardLevelPPx inspireAwardLevel() {
        if (inspireAwardLevel == null) inspireAwardLevel = new InspireAwardLevelPPx(InspireAwardLevelLocationValue.P_InspireAwardLevel);
        return inspireAwardLevel;
    }

    public static InspireRecipientPPx inspireRecipients() {
        if (inspireRecipients == null) inspireRecipients = new InspireRecipientPPx(InspireAwardLevelLocationValue.P_InspireRecipients);
        return inspireRecipients;
    }

    public static LocationPPx location() {
        if (location == null) location = new LocationPPx(InspireAwardLevelLocationValue.P_Location);
        return location;
    }

    public static String id() {
        String s = InspireAwardLevelLocationValue.P_Id;
        return s;
    }

    public static String points() {
        String s = InspireAwardLevelLocationValue.P_Points;
        return s;
    }
}
 
