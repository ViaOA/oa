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
package com.tmgsc.hifivetest.model.oa.cs;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAId;
import com.viaoa.annotation.OAMany;
import com.viaoa.annotation.OAOne;
import com.viaoa.annotation.OAProperty;

/**
 * Root Object that is automatically updated between the Server and Clients.
 * ServerController will do the selects for these objects.
 * Model will share these hubs after the application is started.
 * */

@OAClass(
    useDataSource = false,
    displayProperty = "Id"
)
public class ServerRoot extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_ServerInfo = "ServerInfo";
    
    private Hub<Program> hubProgram; 
    public Hub<Program> getPrograms() {
        if (hubProgram == null) {
            hubProgram = new Hub<Program>(Program.class);
        }
        return hubProgram;
    }
    
    /*$$Start: ServerRoot1 $$*/
    public static final String P_ActivePrograms = "ActivePrograms";
    public static final String P_NewServiceAwardOrders = "NewServiceAwardOrders";
    public static final String P_OpenEmails = "OpenEmails";
    public static final String P_OpenInspireRecipients = "OpenInspireRecipients";
    public static final String P_InspireRecipientWOMissingManagerHub = "InspireRecipientWOMissingManagerHub";
    public static final String P_HifiveApprovedItems = "HifiveApprovedItems";
    public static final String P_AddressTypes = "AddressTypes";
    public static final String P_CardVendors = "CardVendors";
    public static final String P_Catalogs = "Catalogs";
    public static final String P_Charities = "Charities";
    public static final String P_Companies = "Companies";
    public static final String P_CountryCodes = "CountryCodes";
    public static final String P_Ecards = "Ecards";
    public static final String P_EcardCategories = "EcardCategories";
    public static final String P_EmailTypes = "EmailTypes";
    public static final String P_EmployeeTypes = "EmployeeTypes";
    public static final String P_ItemCategories = "ItemCategories";
    public static final String P_ItemTypes = "ItemTypes";
    public static final String P_ItemVendors = "ItemVendors";
    public static final String P_LoginImageSets = "LoginImageSets";
    public static final String P_MerchantCategories = "MerchantCategories";
    public static final String P_Pages = "Pages";
    public static final String P_PageGroups = "PageGroups";
    public static final String P_PageThemes = "PageThemes";
    public static final String P_PhoneTypes = "PhoneTypes";
    public static final String P_ReportClasses = "ReportClasses";
    public static final String P_Values = "Values";
    public static final String P_Widgets = "Widgets";
    /*$$End: ServerRoot1 $$*/

	private int id;
    protected ServerInfo serverInfo;
    /*$$Start: ServerRoot2 $$*/
    protected transient Hub<Program> hubActivePrograms;
    protected transient Hub<EmployeeAward> hubNewServiceAwardOrders;
    protected transient Hub<Email> hubOpenEmails;
    protected transient Hub<InspireRecipient> hubOpenInspireRecipients;
    protected transient Hub<InspireRecipient> hubInspireRecipientWOMissingManager;
    protected transient Hub<Item> hubHifiveApprovedItems;
    protected transient Hub<AddressType> hubAddressTypes;
    protected transient Hub<CardVendor> hubCardVendors;
    protected transient Hub<Catalog> hubCatalogs;
    protected transient Hub<Charity> hubCharities;
    protected transient Hub<Company> hubCompanies;
    protected transient Hub<CountryCode> hubCountryCodes;
    protected transient Hub<Ecard> hubEcards;
    protected transient Hub<EcardCategory> hubEcardCategories;
    protected transient Hub<EmailType> hubEmailTypes;
    protected transient Hub<EmployeeType> hubEmployeeTypes;
    protected transient Hub<ItemCategory> hubItemCategories;
    protected transient Hub<ItemType> hubItemTypes;
    protected transient Hub<ItemVendor> hubItemVendors;
    protected transient Hub<LoginImageSet> hubLoginImageSets;
    protected transient Hub<MerchantCategory> hubMerchantCategories;
    protected transient Hub<Page> hubPages;
    protected transient Hub<PageGroup> hubPageGroups;
    protected transient Hub<PageTheme> hubPageThemes;
    protected transient Hub<PhoneType> hubPhoneTypes;
    protected transient Hub<ReportClass> hubReportClasses;
    protected transient Hub<Value> hubValues;
    protected transient Hub<Widget> hubWidgets;
    /*$$End: ServerRoot2 $$*/
    

	public ServerRoot() {
		setId(777);
	}

    @OAProperty(displayName = "Id")
    @OAId
	public int getId() {
		return id;
	}
	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTY_Id, old, id);
	}

    @OAOne()
    public ServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = (ServerInfo) super.getObject(PROPERTY_ServerInfo);
            if (serverInfo == null) {
                serverInfo = new ServerInfo();
                setServerInfo(serverInfo);
            }
        }
        return serverInfo;
    }

    public void setServerInfo(ServerInfo newValue) {
        ServerInfo old = this.serverInfo;
        this.serverInfo = newValue;
        firePropertyChange(PROPERTY_ServerInfo, old, this.serverInfo);
    }



    /*$$Start: ServerRoot3 $$*/
    @OAMany(toClass = Program.class)
    public Hub<Program> getActivePrograms() {
        if (hubActivePrograms == null) {
            hubActivePrograms = (Hub<Program>) super.getHub(P_ActivePrograms);
        }
        return hubActivePrograms;
    }
    @OAMany(toClass = EmployeeAward.class)
    public Hub<EmployeeAward> getNewServiceAwardOrders() {
        if (hubNewServiceAwardOrders == null) {
            hubNewServiceAwardOrders = (Hub<EmployeeAward>) super.getHub(P_NewServiceAwardOrders);
        }
        return hubNewServiceAwardOrders;
    }
    @OAMany(toClass = Email.class)
    public Hub<Email> getOpenEmails() {
        if (hubOpenEmails == null) {
            hubOpenEmails = (Hub<Email>) super.getHub(P_OpenEmails);
        }
        return hubOpenEmails;
    }
    @OAMany(toClass = InspireRecipient.class)
    public Hub<InspireRecipient> getOpenInspireRecipients() {
        if (hubOpenInspireRecipients == null) {
            hubOpenInspireRecipients = (Hub<InspireRecipient>) super.getHub(P_OpenInspireRecipients);
        }
        return hubOpenInspireRecipients;
    }
    @OAMany(toClass = InspireRecipient.class)
    public Hub<InspireRecipient> getInspireRecipientWOMissingManagerHub() {
        if (hubInspireRecipientWOMissingManager == null) {
            hubInspireRecipientWOMissingManager = (Hub<InspireRecipient>) super.getHub(P_InspireRecipientWOMissingManagerHub);
        }
        return hubInspireRecipientWOMissingManager;
    }
    @OAMany(toClass = Item.class)
    public Hub<Item> getHifiveApprovedItems() {
        if (hubHifiveApprovedItems == null) {
            hubHifiveApprovedItems = (Hub<Item>) super.getHub(P_HifiveApprovedItems);
        }
        return hubHifiveApprovedItems;
    }
    @OAMany(toClass = AddressType.class)
    public Hub<AddressType> getAddressTypes() {
        if (hubAddressTypes == null) {
            hubAddressTypes = (Hub<AddressType>) super.getHub(P_AddressTypes);
        }
        return hubAddressTypes;
    }
    @OAMany(toClass = CardVendor.class)
    public Hub<CardVendor> getCardVendors() {
        if (hubCardVendors == null) {
            hubCardVendors = (Hub<CardVendor>) super.getHub(P_CardVendors);
        }
        return hubCardVendors;
    }
    @OAMany(toClass = Catalog.class)
    public Hub<Catalog> getCatalogs() {
        if (hubCatalogs == null) {
            hubCatalogs = (Hub<Catalog>) super.getHub(P_Catalogs);
        }
        return hubCatalogs;
    }
    @OAMany(toClass = Charity.class)
    public Hub<Charity> getCharities() {
        if (hubCharities == null) {
            hubCharities = (Hub<Charity>) super.getHub(P_Charities, Charity.P_Seq, true);
        }
        return hubCharities;
    }
    @OAMany(toClass = Company.class)
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = (Hub<Company>) super.getHub(P_Companies);
        }
        return hubCompanies;
    }
    @OAMany(toClass = CountryCode.class)
    public Hub<CountryCode> getCountryCodes() {
        if (hubCountryCodes == null) {
            hubCountryCodes = (Hub<CountryCode>) super.getHub(P_CountryCodes);
        }
        return hubCountryCodes;
    }
    @OAMany(toClass = Ecard.class)
    public Hub<Ecard> getEcards() {
        if (hubEcards == null) {
            hubEcards = (Hub<Ecard>) super.getHub(P_Ecards);
        }
        return hubEcards;
    }
    @OAMany(toClass = EcardCategory.class)
    public Hub<EcardCategory> getEcardCategories() {
        if (hubEcardCategories == null) {
            hubEcardCategories = (Hub<EcardCategory>) super.getHub(P_EcardCategories);
        }
        return hubEcardCategories;
    }
    @OAMany(toClass = EmailType.class)
    public Hub<EmailType> getEmailTypes() {
        if (hubEmailTypes == null) {
            hubEmailTypes = (Hub<EmailType>) super.getHub(P_EmailTypes, EmailType.P_Seq, true);
        }
        return hubEmailTypes;
    }
    @OAMany(toClass = EmployeeType.class)
    public Hub<EmployeeType> getEmployeeTypes() {
        if (hubEmployeeTypes == null) {
            hubEmployeeTypes = (Hub<EmployeeType>) super.getHub(P_EmployeeTypes);
        }
        return hubEmployeeTypes;
    }
    @OAMany(toClass = ItemCategory.class)
    public Hub<ItemCategory> getItemCategories() {
        if (hubItemCategories == null) {
            hubItemCategories = (Hub<ItemCategory>) super.getHub(P_ItemCategories);
            hubItemCategories.setRootHub();
        }
        return hubItemCategories;
    }
    @OAMany(toClass = ItemType.class)
    public Hub<ItemType> getItemTypes() {
        if (hubItemTypes == null) {
            hubItemTypes = (Hub<ItemType>) super.getHub(P_ItemTypes);
        }
        return hubItemTypes;
    }
    @OAMany(toClass = ItemVendor.class)
    public Hub<ItemVendor> getItemVendors() {
        if (hubItemVendors == null) {
            hubItemVendors = (Hub<ItemVendor>) super.getHub(P_ItemVendors);
        }
        return hubItemVendors;
    }
    @OAMany(toClass = LoginImageSet.class)
    public Hub<LoginImageSet> getLoginImageSets() {
        if (hubLoginImageSets == null) {
            hubLoginImageSets = (Hub<LoginImageSet>) super.getHub(P_LoginImageSets);
        }
        return hubLoginImageSets;
    }
    @OAMany(toClass = MerchantCategory.class)
    public Hub<MerchantCategory> getMerchantCategories() {
        if (hubMerchantCategories == null) {
            hubMerchantCategories = (Hub<MerchantCategory>) super.getHub(P_MerchantCategories);
            hubMerchantCategories.setRootHub();
        }
        return hubMerchantCategories;
    }
    @OAMany(toClass = Page.class)
    public Hub<Page> getPages() {
        if (hubPages == null) {
            hubPages = (Hub<Page>) super.getHub(P_Pages, Page.P_Seq, true);
        }
        return hubPages;
    }
    @OAMany(toClass = PageGroup.class)
    public Hub<PageGroup> getPageGroups() {
        if (hubPageGroups == null) {
            hubPageGroups = (Hub<PageGroup>) super.getHub(P_PageGroups, PageGroup.P_Seq, true);
            hubPageGroups.setRootHub();
        }
        return hubPageGroups;
    }
    @OAMany(toClass = PageTheme.class)
    public Hub<PageTheme> getPageThemes() {
        if (hubPageThemes == null) {
            hubPageThemes = (Hub<PageTheme>) super.getHub(P_PageThemes);
        }
        return hubPageThemes;
    }
    @OAMany(toClass = PhoneType.class)
    public Hub<PhoneType> getPhoneTypes() {
        if (hubPhoneTypes == null) {
            hubPhoneTypes = (Hub<PhoneType>) super.getHub(P_PhoneTypes);
        }
        return hubPhoneTypes;
    }
    @OAMany(toClass = ReportClass.class)
    public Hub<ReportClass> getReportClasses() {
        if (hubReportClasses == null) {
            hubReportClasses = (Hub<ReportClass>) super.getHub(P_ReportClasses);
        }
        return hubReportClasses;
    }
    @OAMany(toClass = Value.class)
    public Hub<Value> getValues() {
        if (hubValues == null) {
            hubValues = (Hub<Value>) super.getHub(P_Values);
        }
        return hubValues;
    }
    @OAMany(toClass = Widget.class)
    public Hub<Widget> getWidgets() {
        if (hubWidgets == null) {
            hubWidgets = (Hub<Widget>) super.getHub(P_Widgets);
        }
        return hubWidgets;
    }
    /*$$End: ServerRoot3 $$*/

}



