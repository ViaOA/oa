// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class ValuePP {
    private static CardPPx cards;
    private static EmployeeAwardPPx employeeAward;
    private static AwardCardOrderPPx inspireAwardCardOrder;
     

    public static CardPPx cards() {
        if (cards == null) cards = new CardPPx(Value.P_Cards);
        return cards;
    }

    public static EmployeeAwardPPx employeeAward() {
        if (employeeAward == null) employeeAward = new EmployeeAwardPPx(Value.P_EmployeeAward);
        return employeeAward;
    }

    public static AwardCardOrderPPx inspireAwardCardOrder() {
        if (inspireAwardCardOrder == null) inspireAwardCardOrder = new AwardCardOrderPPx(Value.P_InspireAwardCardOrder);
        return inspireAwardCardOrder;
    }

    public static String id() {
        String s = Value.P_Id;
        return s;
    }

    public static String value() {
        String s = Value.P_Value;
        return s;
    }

    public static String name() {
        String s = Value.P_Name;
        return s;
    }
}
 
