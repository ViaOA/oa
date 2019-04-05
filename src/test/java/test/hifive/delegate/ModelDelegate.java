// Copied from OATemplate project by OABuilder 11/27/13 10:36 AM
// Copied from OAHifive project by OABuilder 11/27/13 09:56 AM
// Copied from OAHifive project by OABuilder 11/27/13 09:37 AM
// Copied from OAHifive project by OABuilder 11/22/13 09:28 AM
// Copied from OAHifive project by OABuilder 11/21/13 09:37 AM
// Copied from OAHifive project by OABuilder 11/21/13 09:32 AM
// Copied from OAHifive project by OABuilder 11/21/13 09:29 AM
// Copied from OAHifive project by OABuilder 11/21/13 08:31 AM
// Copied from OAHifive project by OABuilder 11/20/13 01:07 PM
// Copied from OAHifive project by OABuilder 11/20/13 09:58 AM
package test.hifive.delegate;

import com.viaoa.hub.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAString;

import test.hifive.model.oa.*;
import test.hifive.model.oa.cs.ServerRoot;
import test.hifive.model.oa.filter.*;

/**
 * This is used to access all of the Root level Hubs. This is so that they will
 * not have to be passed into and through the models. After client login, the
 * Hubs will be shared with the Hubs in the ServerRoot object from the server.
 * 
 * @author vincevia
 * 
 * @see ClientController#initializeClientModel
 */
public class ModelDelegate {

    private static Hub<User> hubLoginUser;

    private static Hub<Program> hubProgram; 
    public static Hub<Program> getPrograms() {
        if (hubProgram == null) {
            hubProgram = new Hub<Program>(Program.class);
        }
        return hubProgram;
    }
    
    /*$$Start: ModelDelegate1 $$*/
    private static Hub<Program> hubActivePrograms;
    private static Hub<EmployeeAward> hubNewServiceAwardOrders;
    private static Hub<Email> hubOpenEmails;
    private static Hub<InspireRecipient> hubOpenInspireRecipients;
    private static Hub<InspireRecipient> hubInspireRecipientWOMissingManager;
    private static Hub<AwardCardOrder> hubAwardCardOrderSearch;
    private static Hub<Card> hubCardSearch;
    private static Hub<CardVendor> hubCardVendorSearch;
    private static Hub<Company> hubCompanySearch;
    private static Hub<Ecard> hubEcardSearch;
    private static Hub<Employee> hubEmployeeSearch;
    private static Hub<EmployeeAward> hubEmployeeAwardSearch;
    private static Hub<EmployeeEcard> hubEmployeeEcardSearch;
    private static Hub<Inspire> hubInspireSearch;
    private static Hub<InspireApproval> hubInspireApprovalSearch;
    private static Hub<InspireOrder> hubInspireOrderSearch;
    private static Hub<InspireRecipient> hubInspireRecipientSearch;
    private static Hub<Item> hubItemSearch;
    private static Hub<ItemCategory> hubItemCategorySearch;
    private static Hub<ItemVendor> hubItemVendorSearch;
    private static Hub<Location> hubLocationSearch;
    private static Hub<Merchant> hubMerchantSearch;
    private static Hub<PointsIssuance> hubPointsIssuanceSearch;
    private static Hub<PointsRecord> hubPointsRecordSearch;
    private static Hub<PointsRequest> hubPointsRequestSearch;
    private static Hub<Product> hubProductSearch;
    private static Hub<Program> hubProgramSearch;
    private static Hub<AddressType> hubAddressTypes;
    private static Hub<CardVendor> hubCardVendors;
    private static Hub<Charity> hubCharities;
    private static Hub<Company> hubCompanies;
    private static Hub<CountryCode> hubCountryCodes;
    private static Hub<Ecard> hubEcards;
    private static Hub<EcardCategory> hubEcardCategories;
    private static Hub<EmailType> hubEmailTypes;
    private static Hub<EmployeeType> hubEmployeeTypes;
    private static Hub<ItemCategory> hubItemCategories;
    private static Hub<ItemType> hubItemTypes;
    private static Hub<ItemVendor> hubItemVendors;
    private static Hub<LoginImageSet> hubLoginImageSets;
    private static Hub<MerchantCategory> hubMerchantCategories;
    private static Hub<Page> hubPages;
    private static Hub<PageGroup> hubWebPageGroups;
    private static Hub<PageTheme> hubPageThemes;
    private static Hub<PhoneType> hubPhoneTypes;
    private static Hub<ReportClass> hubReportClasses;
    private static Hub<User> hubUsers;
    private static Hub<Value> hubValues;
    private static Hub<Widget> hubWidgets;
    private static Hub<Catalog> hubCatalogs;
    private static Hub<Item> hubHifiveApprovedItems;
    private static Hub<PageGroup> hubPageGroups;
    /*$$End: ModelDelegate1 $$*/

    // AO is the current logged in user
    public static Hub<User> getLoginUserHub() {
        if (hubLoginUser == null) {
            hubLoginUser = new Hub<User>(User.class);
        }
        return hubLoginUser;
    }

    public static User getLoginUser() {
        return getLoginUserHub().getAO();
    }

    public static void setLoginUser(User user) {
        if (!getLoginUserHub().contains(user)) {
            getLoginUserHub().add(user);
        }
        getLoginUserHub().setAO(user);
    }

    public static void initialize(ServerRoot rootServer) {
        /*$$Start: ModelDelegate2 $$*/
        getActivePrograms().setSharedHub(rootServer.getActivePrograms());
        getNewServiceAwardOrders().setSharedHub(rootServer.getNewServiceAwardOrders());
        getOpenEmails().setSharedHub(rootServer.getOpenEmails());
        getOpenInspireRecipients().setSharedHub(rootServer.getOpenInspireRecipients());
        getInspireRecipientWOMissingManagerHub().setSharedHub(rootServer.getInspireRecipientWOMissingManagerHub());
        getAddressTypes().setSharedHub(rootServer.getAddressTypes());
        getCardVendors().setSharedHub(rootServer.getCardVendors());
        getCharities().setSharedHub(rootServer.getCharities());
        getCompanies().setSharedHub(rootServer.getCompanies());
        getCountryCodes().setSharedHub(rootServer.getCountryCodes());
        getEcards().setSharedHub(rootServer.getEcards());
        getEcardCategories().setSharedHub(rootServer.getEcardCategories());
        getEmailTypes().setSharedHub(rootServer.getEmailTypes());
        getEmployeeTypes().setSharedHub(rootServer.getEmployeeTypes());
        getItemCategories().setSharedHub(rootServer.getItemCategories());
        getItemTypes().setSharedHub(rootServer.getItemTypes());
        getItemVendors().setSharedHub(rootServer.getItemVendors());
        getLoginImageSets().setSharedHub(rootServer.getLoginImageSets());
        getMerchantCategories().setSharedHub(rootServer.getMerchantCategories());
        getPages().setSharedHub(rootServer.getPages());
        getWebPageGroups().setSharedHub(rootServer.getPageGroups());
        getPageThemes().setSharedHub(rootServer.getPageThemes());
        getPhoneTypes().setSharedHub(rootServer.getPhoneTypes());
        getReportClasses().setSharedHub(rootServer.getReportClasses());
        //getUsers().setSharedHub(rootServer.getUsers());
        getValues().setSharedHub(rootServer.getValues());
        getWidgets().setSharedHub(rootServer.getWidgets());
        getCatalogs().setSharedHub(rootServer.getCatalogs());
        getHifiveApprovedItems().setSharedHub(rootServer.getHifiveApprovedItems());
        getPageGroups().setSharedHub(rootServer.getPageGroups());
        /*$$End: ModelDelegate2 $$*/
    }

    /*$$Start: ModelDelegate3 $$*/
    public static Hub<Program> getActivePrograms() {
        if (hubActivePrograms == null) {
            hubActivePrograms = new Hub<Program>(Program.class);
        }
        return hubActivePrograms;
    }
    public static Hub<EmployeeAward> getNewServiceAwardOrders() {
        if (hubNewServiceAwardOrders == null) {
            hubNewServiceAwardOrders = new Hub<EmployeeAward>(EmployeeAward.class);
        }
        return hubNewServiceAwardOrders;
    }
    public static Hub<Email> getOpenEmails() {
        if (hubOpenEmails == null) {
            hubOpenEmails = new Hub<Email>(Email.class);
        }
        return hubOpenEmails;
    }
    public static Hub<InspireRecipient> getOpenInspireRecipients() {
        if (hubOpenInspireRecipients == null) {
            hubOpenInspireRecipients = new Hub<InspireRecipient>(InspireRecipient.class);
        }
        return hubOpenInspireRecipients;
    }
    public static Hub<InspireRecipient> getInspireRecipientWOMissingManagerHub() {
        if (hubInspireRecipientWOMissingManager == null) {
            hubInspireRecipientWOMissingManager = new Hub<InspireRecipient>(InspireRecipient.class);
        }
        return hubInspireRecipientWOMissingManager;
    }
    public static Hub<AwardCardOrder> getAwardCardOrderSearchHub() {
        if (hubAwardCardOrderSearch == null) {
            hubAwardCardOrderSearch = new Hub<AwardCardOrder>(AwardCardOrder.class);
        }
        return hubAwardCardOrderSearch;
    }
    public static Hub<Card> getCardSearchHub() {
        if (hubCardSearch == null) {
            hubCardSearch = new Hub<Card>(Card.class);
        }
        return hubCardSearch;
    }
    public static Hub<CardVendor> getCardVendorSearchHub() {
        if (hubCardVendorSearch == null) {
            hubCardVendorSearch = new Hub<CardVendor>(CardVendor.class);
        }
        return hubCardVendorSearch;
    }
    public static Hub<Company> getCompanySearchHub() {
        if (hubCompanySearch == null) {
            hubCompanySearch = new Hub<Company>(Company.class);
        }
        return hubCompanySearch;
    }
    public static Hub<Ecard> getEcardSearchHub() {
        if (hubEcardSearch == null) {
            hubEcardSearch = new Hub<Ecard>(Ecard.class);
        }
        return hubEcardSearch;
    }
    public static Hub<Employee> getEmployeeSearchHub() {
        if (hubEmployeeSearch == null) {
            hubEmployeeSearch = new Hub<Employee>(Employee.class);
        }
        return hubEmployeeSearch;
    }
    public static Hub<EmployeeAward> getEmployeeAwardSearchHub() {
        if (hubEmployeeAwardSearch == null) {
            hubEmployeeAwardSearch = new Hub<EmployeeAward>(EmployeeAward.class);
        }
        return hubEmployeeAwardSearch;
    }
    public static Hub<EmployeeEcard> getEmployeeEcardSearchHub() {
        if (hubEmployeeEcardSearch == null) {
            hubEmployeeEcardSearch = new Hub<EmployeeEcard>(EmployeeEcard.class);
        }
        return hubEmployeeEcardSearch;
    }
    public static Hub<Inspire> getInspireSearchHub() {
        if (hubInspireSearch == null) {
            hubInspireSearch = new Hub<Inspire>(Inspire.class);
        }
        return hubInspireSearch;
    }
    public static Hub<InspireApproval> getInspireApprovalSearchHub() {
        if (hubInspireApprovalSearch == null) {
            hubInspireApprovalSearch = new Hub<InspireApproval>(InspireApproval.class);
        }
        return hubInspireApprovalSearch;
    }
    public static Hub<InspireOrder> getInspireOrderSearchHub() {
        if (hubInspireOrderSearch == null) {
            hubInspireOrderSearch = new Hub<InspireOrder>(InspireOrder.class);
        }
        return hubInspireOrderSearch;
    }
    public static Hub<InspireRecipient> getInspireRecipientSearchHub() {
        if (hubInspireRecipientSearch == null) {
            hubInspireRecipientSearch = new Hub<InspireRecipient>(InspireRecipient.class);
        }
        return hubInspireRecipientSearch;
    }
    public static Hub<Item> getItemSearchHub() {
        if (hubItemSearch == null) {
            hubItemSearch = new Hub<Item>(Item.class);
        }
        return hubItemSearch;
    }
    public static Hub<ItemCategory> getItemCategorySearchHub() {
        if (hubItemCategorySearch == null) {
            hubItemCategorySearch = new Hub<ItemCategory>(ItemCategory.class);
        }
        return hubItemCategorySearch;
    }
    public static Hub<ItemVendor> getItemVendorSearchHub() {
        if (hubItemVendorSearch == null) {
            hubItemVendorSearch = new Hub<ItemVendor>(ItemVendor.class);
        }
        return hubItemVendorSearch;
    }
    public static Hub<Location> getLocationSearchHub() {
        if (hubLocationSearch == null) {
            hubLocationSearch = new Hub<Location>(Location.class);
        }
        return hubLocationSearch;
    }
    public static Hub<Merchant> getMerchantSearchHub() {
        if (hubMerchantSearch == null) {
            hubMerchantSearch = new Hub<Merchant>(Merchant.class);
        }
        return hubMerchantSearch;
    }
    public static Hub<PointsIssuance> getPointsIssuanceSearchHub() {
        if (hubPointsIssuanceSearch == null) {
            hubPointsIssuanceSearch = new Hub<PointsIssuance>(PointsIssuance.class);
        }
        return hubPointsIssuanceSearch;
    }
    public static Hub<PointsRecord> getPointsRecordSearchHub() {
        if (hubPointsRecordSearch == null) {
            hubPointsRecordSearch = new Hub<PointsRecord>(PointsRecord.class);
        }
        return hubPointsRecordSearch;
    }
    public static Hub<PointsRequest> getPointsRequestSearchHub() {
        if (hubPointsRequestSearch == null) {
            hubPointsRequestSearch = new Hub<PointsRequest>(PointsRequest.class);
        }
        return hubPointsRequestSearch;
    }
    public static Hub<Product> getProductSearchHub() {
        if (hubProductSearch == null) {
            hubProductSearch = new Hub<Product>(Product.class);
        }
        return hubProductSearch;
    }
    public static Hub<Program> getProgramSearchHub() {
        if (hubProgramSearch == null) {
            hubProgramSearch = new Hub<Program>(Program.class);
        }
        return hubProgramSearch;
    }
    public static Hub<AddressType> getAddressTypes() {
        if (hubAddressTypes == null) {
            hubAddressTypes = new Hub<AddressType>(AddressType.class);
        }
        return hubAddressTypes;
    }
    public static Hub<CardVendor> getCardVendors() {
        if (hubCardVendors == null) {
            hubCardVendors = new Hub<CardVendor>(CardVendor.class);
        }
        return hubCardVendors;
    }
    public static Hub<Charity> getCharities() {
        if (hubCharities == null) {
            hubCharities = new Hub<Charity>(Charity.class);
        }
        return hubCharities;
    }
    public static Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = new Hub<Company>(Company.class);
        }
        return hubCompanies;
    }
    public static Hub<CountryCode> getCountryCodes() {
        if (hubCountryCodes == null) {
            hubCountryCodes = new Hub<CountryCode>(CountryCode.class);
        }
        return hubCountryCodes;
    }
    public static Hub<Ecard> getEcards() {
        if (hubEcards == null) {
            hubEcards = new Hub<Ecard>(Ecard.class);
        }
        return hubEcards;
    }
    public static Hub<EcardCategory> getEcardCategories() {
        if (hubEcardCategories == null) {
            hubEcardCategories = new Hub<EcardCategory>(EcardCategory.class);
        }
        return hubEcardCategories;
    }
    public static Hub<EmailType> getEmailTypes() {
        if (hubEmailTypes == null) {
            hubEmailTypes = new Hub<EmailType>(EmailType.class);
        }
        return hubEmailTypes;
    }
    public static Hub<EmployeeType> getEmployeeTypes() {
        if (hubEmployeeTypes == null) {
            hubEmployeeTypes = new Hub<EmployeeType>(EmployeeType.class);
        }
        return hubEmployeeTypes;
    }
    public static Hub<ItemCategory> getItemCategories() {
        if (hubItemCategories == null) {
            hubItemCategories = new Hub<ItemCategory>(ItemCategory.class);
        }
        return hubItemCategories;
    }
    public static Hub<ItemType> getItemTypes() {
        if (hubItemTypes == null) {
            hubItemTypes = new Hub<ItemType>(ItemType.class);
        }
        return hubItemTypes;
    }
    public static Hub<ItemVendor> getItemVendors() {
        if (hubItemVendors == null) {
            hubItemVendors = new Hub<ItemVendor>(ItemVendor.class);
        }
        return hubItemVendors;
    }
    public static Hub<LoginImageSet> getLoginImageSets() {
        if (hubLoginImageSets == null) {
            hubLoginImageSets = new Hub<LoginImageSet>(LoginImageSet.class);
        }
        return hubLoginImageSets;
    }
    public static Hub<MerchantCategory> getMerchantCategories() {
        if (hubMerchantCategories == null) {
            hubMerchantCategories = new Hub<MerchantCategory>(MerchantCategory.class);
        }
        return hubMerchantCategories;
    }
    public static Hub<Page> getPages() {
        if (hubPages == null) {
            hubPages = new Hub<Page>(Page.class);
        }
        return hubPages;
    }
    public static Hub<PageGroup> getWebPageGroups() {
        if (hubWebPageGroups == null) {
            hubWebPageGroups = new Hub<PageGroup>(PageGroup.class);
        }
        return hubWebPageGroups;
    }
    public static Hub<PageTheme> getPageThemes() {
        if (hubPageThemes == null) {
            hubPageThemes = new Hub<PageTheme>(PageTheme.class);
        }
        return hubPageThemes;
    }
    public static Hub<PhoneType> getPhoneTypes() {
        if (hubPhoneTypes == null) {
            hubPhoneTypes = new Hub<PhoneType>(PhoneType.class);
        }
        return hubPhoneTypes;
    }
    public static Hub<ReportClass> getReportClasses() {
        if (hubReportClasses == null) {
            hubReportClasses = new Hub<ReportClass>(ReportClass.class);
        }
        return hubReportClasses;
    }
    public static Hub<User> getUsers() {
        if (hubUsers == null) {
            hubUsers = new Hub<User>(User.class);
        }
        return hubUsers;
    }
    public static Hub<Value> getValues() {
        if (hubValues == null) {
            hubValues = new Hub<Value>(Value.class);
        }
        return hubValues;
    }
    public static Hub<Widget> getWidgets() {
        if (hubWidgets == null) {
            hubWidgets = new Hub<Widget>(Widget.class);
        }
        return hubWidgets;
    }
    public static Hub<Catalog> getCatalogs() {
        if (hubCatalogs == null) {
            hubCatalogs = new Hub<Catalog>(Catalog.class);
        }
        return hubCatalogs;
    }
    public static Hub<Item> getHifiveApprovedItems() {
        if (hubHifiveApprovedItems == null) {
            hubHifiveApprovedItems = new Hub<Item>(Item.class);
        }
        return hubHifiveApprovedItems;
    }
    public static Hub<PageGroup> getPageGroups() {
        if (hubPageGroups == null) {
            hubPageGroups = new Hub<PageGroup>(PageGroup.class);
        }
        return hubPageGroups;
    }
    /*$$End: ModelDelegate3 $$*/

    
}
