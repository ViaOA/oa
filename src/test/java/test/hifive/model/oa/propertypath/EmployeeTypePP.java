// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class EmployeeTypePP {
    private static EmployeePPx employees;
     

    public static EmployeePPx employees() {
        if (employees == null) employees = new EmployeePPx(EmployeeType.P_Employees);
        return employees;
    }

    public static String id() {
        String s = EmployeeType.P_Id;
        return s;
    }

    public static String name() {
        String s = EmployeeType.P_Name;
        return s;
    }

    public static String type() {
        String s = EmployeeType.P_Type;
        return s;
    }
}
 
