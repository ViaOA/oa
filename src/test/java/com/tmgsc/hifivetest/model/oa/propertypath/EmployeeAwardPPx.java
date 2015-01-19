// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import java.io.Serializable;

import com.tmgsc.hifivetest.model.oa.*;
 
public class EmployeeAwardPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AddOnItemPPx addOnItems;
    private ProductPPx addOnProduct;
    private AwardCardOrderPPx awardCardOrders;
    private AwardTypePPx awardType;
    private ProgramDocumentPPx calcAnnouncementDocument;
    private ImageStorePPx calcCeoImageStore;
    private ImageStorePPx calcCeoSignatureImageStore;
    private EmployeePPx calcEmployee;
    private EmailPPx confirmEmail;
    private EmployeePPx employee;
    private EmployeeAwardCharityPPx employeeAwardCharities;
    private HindaOrderPPx hindaOrder;
    private CardPPx imagineCard;
    private EmailPPx managerNotifyEmail;
    private EmailPPx notifyEmail;
    private ProductPPx product;
    private EmailPPx shippedEmails;
    private ShipToPPx shipTo;
    private ValuePPx values;
     
    public EmployeeAwardPPx(String name) {
        this(null, name);
    }

    public EmployeeAwardPPx(PPxInterface parent, String name) {
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

    public AddOnItemPPx addOnItems() {
        if (addOnItems == null) addOnItems = new AddOnItemPPx(this, EmployeeAward.P_AddOnItems);
        return addOnItems;
    }

    public ProductPPx addOnProduct() {
        if (addOnProduct == null) addOnProduct = new ProductPPx(this, EmployeeAward.P_AddOnProduct);
        return addOnProduct;
    }

    public AwardCardOrderPPx awardCardOrders() {
        if (awardCardOrders == null) awardCardOrders = new AwardCardOrderPPx(this, EmployeeAward.P_AwardCardOrders);
        return awardCardOrders;
    }

    public AwardTypePPx awardType() {
        if (awardType == null) awardType = new AwardTypePPx(this, EmployeeAward.P_AwardType);
        return awardType;
    }

    public ProgramDocumentPPx calcAnnouncementDocument() {
        if (calcAnnouncementDocument == null) calcAnnouncementDocument = new ProgramDocumentPPx(this, EmployeeAward.P_CalcAnnouncementDocument);
        return calcAnnouncementDocument;
    }

    public ImageStorePPx calcCeoImageStore() {
        if (calcCeoImageStore == null) calcCeoImageStore = new ImageStorePPx(this, EmployeeAward.P_CalcCeoImageStore);
        return calcCeoImageStore;
    }

    public ImageStorePPx calcCeoSignatureImageStore() {
        if (calcCeoSignatureImageStore == null) calcCeoSignatureImageStore = new ImageStorePPx(this, EmployeeAward.P_CalcCeoSignatureImageStore);
        return calcCeoSignatureImageStore;
    }

    public EmployeePPx calcEmployee() {
        if (calcEmployee == null) calcEmployee = new EmployeePPx(this, EmployeeAward.P_CalcEmployee);
        return calcEmployee;
    }

    public EmailPPx confirmEmail() {
        if (confirmEmail == null) confirmEmail = new EmailPPx(this, EmployeeAward.P_ConfirmEmail);
        return confirmEmail;
    }

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, EmployeeAward.P_Employee);
        return employee;
    }

    public EmployeeAwardCharityPPx employeeAwardCharities() {
        if (employeeAwardCharities == null) employeeAwardCharities = new EmployeeAwardCharityPPx(this, EmployeeAward.P_EmployeeAwardCharities);
        return employeeAwardCharities;
    }

    public HindaOrderPPx hindaOrder() {
        if (hindaOrder == null) hindaOrder = new HindaOrderPPx(this, EmployeeAward.P_HindaOrder);
        return hindaOrder;
    }

    public CardPPx imagineCard() {
        if (imagineCard == null) imagineCard = new CardPPx(this, EmployeeAward.P_ImagineCard);
        return imagineCard;
    }

    public EmailPPx managerNotifyEmail() {
        if (managerNotifyEmail == null) managerNotifyEmail = new EmailPPx(this, EmployeeAward.P_ManagerNotifyEmail);
        return managerNotifyEmail;
    }

    public EmailPPx notifyEmail() {
        if (notifyEmail == null) notifyEmail = new EmailPPx(this, EmployeeAward.P_NotifyEmail);
        return notifyEmail;
    }

    public ProductPPx product() {
        if (product == null) product = new ProductPPx(this, EmployeeAward.P_Product);
        return product;
    }

    public EmailPPx shippedEmails() {
        if (shippedEmails == null) shippedEmails = new EmailPPx(this, EmployeeAward.P_ShippedEmails);
        return shippedEmails;
    }

    public ShipToPPx shipTo() {
        if (shipTo == null) shipTo = new ShipToPPx(this, EmployeeAward.P_ShipTo);
        return shipTo;
    }

    public ValuePPx values() {
        if (values == null) values = new ValuePPx(this, EmployeeAward.P_Values);
        return values;
    }

    public String id() {
        return pp + "." + EmployeeAward.P_Id;
    }

    public String created() {
        return pp + "." + EmployeeAward.P_Created;
    }

    public String awardDate() {
        return pp + "." + EmployeeAward.P_AwardDate;
    }

    public String approvedDate() {
        return pp + "." + EmployeeAward.P_ApprovedDate;
    }

    public String packageSentDate() {
        return pp + "." + EmployeeAward.P_PackageSentDate;
    }

    public String packageTracking() {
        return pp + "." + EmployeeAward.P_PackageTracking;
    }

    public String packageShippingInfo() {
        return pp + "." + EmployeeAward.P_PackageShippingInfo;
    }

    public String packageInvoiceNumber() {
        return pp + "." + EmployeeAward.P_PackageInvoiceNumber;
    }

    public String packageBillDate() {
        return pp + "." + EmployeeAward.P_PackageBillDate;
    }

    public String billDate() {
        return pp + "." + EmployeeAward.P_BillDate;
    }

    public String packagePaidDate() {
        return pp + "." + EmployeeAward.P_PackagePaidDate;
    }

    public String paidDate() {
        return pp + "." + EmployeeAward.P_PaidDate;
    }

    public String itemSelectedDate() {
        return pp + "." + EmployeeAward.P_ItemSelectedDate;
    }

    public String itemSentDate() {
        return pp + "." + EmployeeAward.P_ItemSentDate;
    }

    public String itemShippingInfo() {
        return pp + "." + EmployeeAward.P_ItemShippingInfo;
    }

    public String itemTracking() {
        return pp + "." + EmployeeAward.P_ItemTracking;
    }

    public String itemBillDate() {
        return pp + "." + EmployeeAward.P_ItemBillDate;
    }

    public String itemLastStatusDate() {
        return pp + "." + EmployeeAward.P_ItemLastStatusDate;
    }

    public String itemLastStatus() {
        return pp + "." + EmployeeAward.P_ItemLastStatus;
    }

    public String itemInvoiceNumber() {
        return pp + "." + EmployeeAward.P_ItemInvoiceNumber;
    }

    public String itemVendorInvoiced() {
        return pp + "." + EmployeeAward.P_ItemVendorInvoiced;
    }

    public String itemPaidDate() {
        return pp + "." + EmployeeAward.P_ItemPaidDate;
    }

    public String completedDate() {
        return pp + "." + EmployeeAward.P_CompletedDate;
    }

    public String cancelDate() {
        return pp + "." + EmployeeAward.P_CancelDate;
    }

    public String cancelReason() {
        return pp + "." + EmployeeAward.P_CancelReason;
    }

    public String cashSelectedDate() {
        return pp + "." + EmployeeAward.P_CashSelectedDate;
    }

    public String cashSentDate() {
        return pp + "." + EmployeeAward.P_CashSentDate;
    }

    public String internationalVisaSelectedDate() {
        return pp + "." + EmployeeAward.P_InternationalVisaSelectedDate;
    }

    public String internationalVisaAmount() {
        return pp + "." + EmployeeAward.P_InternationalVisaAmount;
    }

    public String internationalVisaSentDate() {
        return pp + "." + EmployeeAward.P_InternationalVisaSentDate;
    }

    public String addOnProductSelectedDate() {
        return pp + "." + EmployeeAward.P_AddOnProductSelectedDate;
    }

    public String mergeId() {
        return pp + "." + EmployeeAward.P_MergeId;
    }

    public String cashInvoiceNumber() {
        return pp + "." + EmployeeAward.P_CashInvoiceNumber;
    }

    public String cashinvoiceDate() {
        return pp + "." + EmployeeAward.P_CashinvoiceDate;
    }

    public String internationalVisaInvoiceNumber() {
        return pp + "." + EmployeeAward.P_InternationalVisaInvoiceNumber;
    }

    public String internationVisaInvoiceDate() {
        return pp + "." + EmployeeAward.P_InternationVisaInvoiceDate;
    }

    public String internationalVisaVendorInvoiced() {
        return pp + "." + EmployeeAward.P_InternationalVisaVendorInvoiced;
    }

    public String isOpen() {
        return pp + "." + EmployeeAward.P_IsOpen;
    }

    public String balance() {
        return pp + "." + EmployeeAward.P_Balance;
    }

    public String isExpired() {
        return pp + "." + EmployeeAward.P_IsExpired;
    }

    public String hasNotSent() {
        return pp + "." + EmployeeAward.P_HasNotSent;
    }

    public String display() {
        return pp + "." + EmployeeAward.P_Display;
    }

    public String cards() {
        return pp + "." + EmployeeAward.P_Cards;
    }

    public String selectedAwardDescription() {
        return pp + "." + EmployeeAward.P_SelectedAwardDescription;
    }

    public String currentStatus() {
        return pp + "." + EmployeeAward.P_CurrentStatus;
    }

    public String canSelectProduct() {
        return pp + "." + EmployeeAward.P_CanSelectProduct;
    }

    public String canSelectCard() {
        return pp + "." + EmployeeAward.P_CanSelectCard;
    }

    public String canSelectCash() {
        return pp + "." + EmployeeAward.P_CanSelectCash;
    }

    public String canSelectHelpingHands() {
        return pp + "." + EmployeeAward.P_CanSelectHelpingHands;
    }

    public String canSelectCharity() {
        return pp + "." + EmployeeAward.P_CanSelectCharity;
    }

    public String canSelectInternationalVisa() {
        return pp + "." + EmployeeAward.P_CanSelectInternationalVisa;
    }

    public String canSelectAddOnItem() {
        return pp + "." + EmployeeAward.P_CanSelectAddOnItem;
    }

    public String usesImagineCard() {
        return pp + "." + EmployeeAward.P_UsesImagineCard;
    }

    public String isAvailable() {
        return pp + "." + EmployeeAward.P_IsAvailable;
    }

    public String exportToCsv() {
        return pp + ".exportToCsv";
    }

    public String hasNotSentFilter() {
        return pp + ":hasNotSent()";
    }

    public String availableFilter() {
        return pp + ":available()";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
