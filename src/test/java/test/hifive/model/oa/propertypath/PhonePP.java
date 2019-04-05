// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class PhonePP {
    private static EmployeePPx employee;
    private static PhoneTypePPx phoneType;
     

    public static EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(Phone.P_Employee);
        return employee;
    }

    public static PhoneTypePPx phoneType() {
        if (phoneType == null) phoneType = new PhoneTypePPx(Phone.P_PhoneType);
        return phoneType;
    }

    public static String id() {
        String s = Phone.P_Id;
        return s;
    }

    public static String created() {
        String s = Phone.P_Created;
        return s;
    }

    public static String phoneNumber() {
        String s = Phone.P_PhoneNumber;
        return s;
    }

    public static String inactiveDate() {
        String s = Phone.P_InactiveDate;
        return s;
    }
}
 
