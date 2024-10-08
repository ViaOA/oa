// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class InspireOrderItemPP {
    private static InspireOrderPPx inspireOrder;
    private static ProductPPx product;
     

    public static InspireOrderPPx inspireOrder() {
        if (inspireOrder == null) inspireOrder = new InspireOrderPPx(InspireOrderItem.P_InspireOrder);
        return inspireOrder;
    }

    public static ProductPPx product() {
        if (product == null) product = new ProductPPx(InspireOrderItem.P_Product);
        return product;
    }

    public static String id() {
        String s = InspireOrderItem.P_Id;
        return s;
    }

    public static String created() {
        String s = InspireOrderItem.P_Created;
        return s;
    }

    public static String seq() {
        String s = InspireOrderItem.P_Seq;
        return s;
    }

    public static String quantity() {
        String s = InspireOrderItem.P_Quantity;
        return s;
    }

    public static String pointsUsed() {
        String s = InspireOrderItem.P_PointsUsed;
        return s;
    }

    public static String billDate() {
        String s = InspireOrderItem.P_BillDate;
        return s;
    }

    public static String paidDate() {
        String s = InspireOrderItem.P_PaidDate;
        return s;
    }

    public static String itemSentDate() {
        String s = InspireOrderItem.P_ItemSentDate;
        return s;
    }

    public static String itemShippingInfo() {
        String s = InspireOrderItem.P_ItemShippingInfo;
        return s;
    }

    public static String itemLastStatusDate() {
        String s = InspireOrderItem.P_ItemLastStatusDate;
        return s;
    }

    public static String itemLastStatus() {
        String s = InspireOrderItem.P_ItemLastStatus;
        return s;
    }

    public static String completedDate() {
        String s = InspireOrderItem.P_CompletedDate;
        return s;
    }

    public static String invoiceNumber() {
        String s = InspireOrderItem.P_InvoiceNumber;
        return s;
    }

    public static String invoiceDate() {
        String s = InspireOrderItem.P_InvoiceDate;
        return s;
    }

    public static String vendorInvoiced() {
        String s = InspireOrderItem.P_VendorInvoiced;
        return s;
    }
}
 
