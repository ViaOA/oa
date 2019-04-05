// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class HindaOrderPP {
    private static EmployeeAwardPPx employeeAward;
    private static HifiveOrderPPx hifiveOrder;
    private static HindaOrderLinePPx hindaOrderLines;
     

    public static EmployeeAwardPPx employeeAward() {
        if (employeeAward == null) employeeAward = new EmployeeAwardPPx(HindaOrder.P_EmployeeAward);
        return employeeAward;
    }

    public static HifiveOrderPPx hifiveOrder() {
        if (hifiveOrder == null) hifiveOrder = new HifiveOrderPPx(HindaOrder.P_HifiveOrder);
        return hifiveOrder;
    }

    public static HindaOrderLinePPx hindaOrderLines() {
        if (hindaOrderLines == null) hindaOrderLines = new HindaOrderLinePPx(HindaOrder.P_HindaOrderLines);
        return hindaOrderLines;
    }

    public static String id() {
        String s = HindaOrder.P_Id;
        return s;
    }

    public static String orderNumber() {
        String s = HindaOrder.P_OrderNumber;
        return s;
    }

    public static String clientOrderNumber() {
        String s = HindaOrder.P_ClientOrderNumber;
        return s;
    }

    public static String orderDate() {
        String s = HindaOrder.P_OrderDate;
        return s;
    }
}
 
