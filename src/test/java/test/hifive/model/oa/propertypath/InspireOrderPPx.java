// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class InspireOrderPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AwardCardOrderPPx awardCardOrders;
    private EmailPPx emails;
    private EmployeePPx employee;
    private InspireOrderCharityPPx inspireOrderCharities;
    private InspireOrderItemPPx inspireOrderItems;
    private PointsRecordPPx pointsRecord;
    private ShipToPPx shipTo;
     
    public InspireOrderPPx(String name) {
        this(null, name);
    }

    public InspireOrderPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public AwardCardOrderPPx awardCardOrders() {
        if (awardCardOrders == null) awardCardOrders = new AwardCardOrderPPx(this, InspireOrder.P_AwardCardOrders);
        return awardCardOrders;
    }

    public EmailPPx emails() {
        if (emails == null) emails = new EmailPPx(this, InspireOrder.P_Emails);
        return emails;
    }

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, InspireOrder.P_Employee);
        return employee;
    }

    public InspireOrderCharityPPx inspireOrderCharities() {
        if (inspireOrderCharities == null) inspireOrderCharities = new InspireOrderCharityPPx(this, InspireOrder.P_InspireOrderCharities);
        return inspireOrderCharities;
    }

    public InspireOrderItemPPx inspireOrderItems() {
        if (inspireOrderItems == null) inspireOrderItems = new InspireOrderItemPPx(this, InspireOrder.P_InspireOrderItems);
        return inspireOrderItems;
    }

    public PointsRecordPPx pointsRecord() {
        if (pointsRecord == null) pointsRecord = new PointsRecordPPx(this, InspireOrder.P_PointsRecord);
        return pointsRecord;
    }

    public ShipToPPx shipTo() {
        if (shipTo == null) shipTo = new ShipToPPx(this, InspireOrder.P_ShipTo);
        return shipTo;
    }

    public String id() {
        return pp + "." + InspireOrder.P_Id;
    }

    public String created() {
        return pp + "." + InspireOrder.P_Created;
    }

    public String billDate() {
        return pp + "." + InspireOrder.P_BillDate;
    }

    public String paidDate() {
        return pp + "." + InspireOrder.P_PaidDate;
    }

    public String completedDate() {
        return pp + "." + InspireOrder.P_CompletedDate;
    }

    public String cashSelectedDate() {
        return pp + "." + InspireOrder.P_CashSelectedDate;
    }

    public String cashAmount() {
        return pp + "." + InspireOrder.P_CashAmount;
    }

    public String cashPointsUsed() {
        return pp + "." + InspireOrder.P_CashPointsUsed;
    }

    public String cashSentDate() {
        return pp + "." + InspireOrder.P_CashSentDate;
    }

    public String internationalVisaSelectedDate() {
        return pp + "." + InspireOrder.P_InternationalVisaSelectedDate;
    }

    public String internationalVisaAmount() {
        return pp + "." + InspireOrder.P_InternationalVisaAmount;
    }

    public String internationalVisaPointsUsed() {
        return pp + "." + InspireOrder.P_InternationalVisaPointsUsed;
    }

    public String internationalVisaSentDate() {
        return pp + "." + InspireOrder.P_InternationalVisaSentDate;
    }

    public String pointsOrdered() {
        return pp + "." + InspireOrder.P_PointsOrdered;
    }

    public String cashInvoiceNumber() {
        return pp + "." + InspireOrder.P_CashInvoiceNumber;
    }

    public String cashInvoiceDate() {
        return pp + "." + InspireOrder.P_CashInvoiceDate;
    }

    public String internationalVisaInvoiceNumber() {
        return pp + "." + InspireOrder.P_InternationalVisaInvoiceNumber;
    }

    public String internationalVisaInvoiceDate() {
        return pp + "." + InspireOrder.P_InternationalVisaInvoiceDate;
    }

    public String internationalVisaVendorInvoiced() {
        return pp + "." + InspireOrder.P_InternationalVisaVendorInvoiced;
    }

    public String pointsUsed() {
        return pp + "." + InspireOrder.P_PointsUsed;
    }

    public String numberOfCartItems() {
        return pp + "." + InspireOrder.P_NumberOfCartItems;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
