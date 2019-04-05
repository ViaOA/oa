// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class ProductPP {
    private static EmployeeAwardPPx addOnProductEmployeeAwards;
    private static AwardTypePPx awardTypes;
    private static EmployeeAwardPPx employeeAwards;
    private static HifiveOrderItemPPx hifiveOrderItems;
    private static InspireOrderItemPPx inspireOrderItems;
    private static ItemPPx item;
    private static ProductAuditPPx productAudits;
     

    public static EmployeeAwardPPx addOnProductEmployeeAwards() {
        if (addOnProductEmployeeAwards == null) addOnProductEmployeeAwards = new EmployeeAwardPPx(Product.P_AddOnProductEmployeeAwards);
        return addOnProductEmployeeAwards;
    }

    public static AwardTypePPx awardTypes() {
        if (awardTypes == null) awardTypes = new AwardTypePPx(Product.P_AwardTypes);
        return awardTypes;
    }

    public static EmployeeAwardPPx employeeAwards() {
        if (employeeAwards == null) employeeAwards = new EmployeeAwardPPx(Product.P_EmployeeAwards);
        return employeeAwards;
    }

    public static HifiveOrderItemPPx hifiveOrderItems() {
        if (hifiveOrderItems == null) hifiveOrderItems = new HifiveOrderItemPPx(Product.P_HifiveOrderItems);
        return hifiveOrderItems;
    }

    public static InspireOrderItemPPx inspireOrderItems() {
        if (inspireOrderItems == null) inspireOrderItems = new InspireOrderItemPPx(Product.P_InspireOrderItems);
        return inspireOrderItems;
    }

    public static ItemPPx item() {
        if (item == null) item = new ItemPPx(Product.P_Item);
        return item;
    }

    public static ProductAuditPPx productAudits() {
        if (productAudits == null) productAudits = new ProductAuditPPx(Product.P_ProductAudits);
        return productAudits;
    }

    public static String id() {
        String s = Product.P_Id;
        return s;
    }

    public static String vendorCode() {
        String s = Product.P_VendorCode;
        return s;
    }

    public static String attribute() {
        String s = Product.P_Attribute;
        return s;
    }

    public static String cost() {
        String s = Product.P_Cost;
        return s;
    }

    public static String handlingCost() {
        String s = Product.P_HandlingCost;
        return s;
    }

    public static String shippingCost() {
        String s = Product.P_ShippingCost;
        return s;
    }

    public static String totalCost() {
        String s = Product.P_TotalCost;
        return s;
    }

    public static String discontinuedDate() {
        String s = Product.P_DiscontinuedDate;
        return s;
    }

    public static String discontinuedReason() {
        String s = Product.P_DiscontinuedReason;
        return s;
    }

    public static String lastUpdate() {
        String s = Product.P_LastUpdate;
        return s;
    }

    public static String msrp() {
        String s = Product.P_Msrp;
        return s;
    }

    public static String streetValue() {
        String s = Product.P_StreetValue;
        return s;
    }
}
 
