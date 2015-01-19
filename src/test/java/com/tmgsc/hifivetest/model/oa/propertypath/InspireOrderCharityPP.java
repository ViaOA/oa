// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class InspireOrderCharityPP {
    private static CharityPPx charity;
    private static InspireOrderPPx inspireOrder;
     

    public static CharityPPx charity() {
        if (charity == null) charity = new CharityPPx(InspireOrderCharity.P_Charity);
        return charity;
    }

    public static InspireOrderPPx inspireOrder() {
        if (inspireOrder == null) inspireOrder = new InspireOrderPPx(InspireOrderCharity.P_InspireOrder);
        return inspireOrder;
    }

    public static String id() {
        String s = InspireOrderCharity.P_Id;
        return s;
    }

    public static String created() {
        String s = InspireOrderCharity.P_Created;
        return s;
    }

    public static String value() {
        String s = InspireOrderCharity.P_Value;
        return s;
    }

    public static String sentDate() {
        String s = InspireOrderCharity.P_SentDate;
        return s;
    }

    public static String pointsUsed() {
        String s = InspireOrderCharity.P_PointsUsed;
        return s;
    }

    public static String invoiceNumber() {
        String s = InspireOrderCharity.P_InvoiceNumber;
        return s;
    }

    public static String invoiceDate() {
        String s = InspireOrderCharity.P_InvoiceDate;
        return s;
    }

    public static String vendorInvoiced() {
        String s = InspireOrderCharity.P_VendorInvoiced;
        return s;
    }
}
 
