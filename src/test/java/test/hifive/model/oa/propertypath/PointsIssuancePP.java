// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class PointsIssuancePP {
    private static EmployeePPx fromEmployee;
    private static EmployeePPx toEmployee;
     

    public static EmployeePPx fromEmployee() {
        if (fromEmployee == null) fromEmployee = new EmployeePPx(PointsIssuance.P_FromEmployee);
        return fromEmployee;
    }

    public static EmployeePPx toEmployee() {
        if (toEmployee == null) toEmployee = new EmployeePPx(PointsIssuance.P_ToEmployee);
        return toEmployee;
    }

    public static String id() {
        String s = PointsIssuance.P_Id;
        return s;
    }

    public static String created() {
        String s = PointsIssuance.P_Created;
        return s;
    }

    public static String points() {
        String s = PointsIssuance.P_Points;
        return s;
    }

    public static String description() {
        String s = PointsIssuance.P_Description;
        return s;
    }
}
 
