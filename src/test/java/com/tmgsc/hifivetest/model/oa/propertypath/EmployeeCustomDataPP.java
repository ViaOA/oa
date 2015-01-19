// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class EmployeeCustomDataPP {
    private static CustomDataPPx customData;
    private static EmployeePPx employee;
     

    public static CustomDataPPx customData() {
        if (customData == null) customData = new CustomDataPPx(EmployeeCustomData.P_CustomData);
        return customData;
    }

    public static EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(EmployeeCustomData.P_Employee);
        return employee;
    }

    public static String id() {
        String s = EmployeeCustomData.P_Id;
        return s;
    }

    public static String value() {
        String s = EmployeeCustomData.P_Value;
        return s;
    }

    public static String code() {
        String s = EmployeeCustomData.P_Code;
        return s;
    }
}
 
