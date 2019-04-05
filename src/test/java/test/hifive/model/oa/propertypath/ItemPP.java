// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class ItemPP {
    private static AddOnItemPPx addOnItems;
    private static AwardTypePPx availableAwardTypes;
    private static AwardTypePPx excludeAwardTypes;
    private static AwardTypePPx helpingHandAwardType;
    private static ImageStorePPx imageStore;
    private static AwardTypePPx includeAwardTypes;
    private static ItemCategoryPPx itemCategories;
    private static ItemTypePPx itemTypes;
    private static ItemVendorPPx itemVendor;
    private static ProductPPx products;
    private static ItemPPx replaceItems;
    private static ItemPPx replacesItems;
    private static SectionPPx sections;
     

    public static AddOnItemPPx addOnItems() {
        if (addOnItems == null) addOnItems = new AddOnItemPPx(Item.P_AddOnItems);
        return addOnItems;
    }

    public static AwardTypePPx availableAwardTypes() {
        if (availableAwardTypes == null) availableAwardTypes = new AwardTypePPx(Item.P_AvailableAwardTypes);
        return availableAwardTypes;
    }

    public static AwardTypePPx excludeAwardTypes() {
        if (excludeAwardTypes == null) excludeAwardTypes = new AwardTypePPx(Item.P_ExcludeAwardTypes);
        return excludeAwardTypes;
    }

    public static AwardTypePPx helpingHandAwardType() {
        if (helpingHandAwardType == null) helpingHandAwardType = new AwardTypePPx(Item.P_HelpingHandAwardType);
        return helpingHandAwardType;
    }

    public static ImageStorePPx imageStore() {
        if (imageStore == null) imageStore = new ImageStorePPx(Item.P_ImageStore);
        return imageStore;
    }

    public static AwardTypePPx includeAwardTypes() {
        if (includeAwardTypes == null) includeAwardTypes = new AwardTypePPx(Item.P_IncludeAwardTypes);
        return includeAwardTypes;
    }

    public static ItemCategoryPPx itemCategories() {
        if (itemCategories == null) itemCategories = new ItemCategoryPPx(Item.P_ItemCategories);
        return itemCategories;
    }

    public static ItemTypePPx itemTypes() {
        if (itemTypes == null) itemTypes = new ItemTypePPx(Item.P_ItemTypes);
        return itemTypes;
    }

    public static ItemVendorPPx itemVendor() {
        if (itemVendor == null) itemVendor = new ItemVendorPPx(Item.P_ItemVendor);
        return itemVendor;
    }

    public static ProductPPx products() {
        if (products == null) products = new ProductPPx(Item.P_Products);
        return products;
    }

    public static ItemPPx replaceItems() {
        if (replaceItems == null) replaceItems = new ItemPPx(Item.P_ReplaceItems);
        return replaceItems;
    }

    public static ItemPPx replacesItems() {
        if (replacesItems == null) replacesItems = new ItemPPx(Item.P_ReplacesItems);
        return replacesItems;
    }

    public static SectionPPx sections() {
        if (sections == null) sections = new SectionPPx(Item.P_Sections);
        return sections;
    }

    public static String id() {
        String s = Item.P_Id;
        return s;
    }

    public static String created() {
        String s = Item.P_Created;
        return s;
    }

    public static String vendorCode() {
        String s = Item.P_VendorCode;
        return s;
    }

    public static String vendorCode2() {
        String s = Item.P_VendorCode2;
        return s;
    }

    public static String name() {
        String s = Item.P_Name;
        return s;
    }

    public static String briefText() {
        String s = Item.P_BriefText;
        return s;
    }

    public static String text() {
        String s = Item.P_Text;
        return s;
    }

    public static String discontinuedDate() {
        String s = Item.P_DiscontinuedDate;
        return s;
    }

    public static String discontinuedReason() {
        String s = Item.P_DiscontinuedReason;
        return s;
    }

    public static String dropShip() {
        String s = Item.P_DropShip;
        return s;
    }

    public static String otherInformation() {
        String s = Item.P_OtherInformation;
        return s;
    }

    public static String manufacturer() {
        String s = Item.P_Manufacturer;
        return s;
    }

    public static String model() {
        String s = Item.P_Model;
        return s;
    }

    public static String lastUpdate() {
        String s = Item.P_LastUpdate;
        return s;
    }

    public static String hifiveRating() {
        String s = Item.P_HifiveRating;
        return s;
    }

    public static String hifiveRatingDate() {
        String s = Item.P_HifiveRatingDate;
        return s;
    }

    public static String hifiveRatingNote() {
        String s = Item.P_HifiveRatingNote;
        return s;
    }

    public static String accountNumber() {
        String s = Item.P_AccountNumber;
        return s;
    }

    public static String cost() {
        String s = Item.P_Cost;
        return s;
    }

    public static String totalCost() {
        String s = Item.P_TotalCost;
        return s;
    }

    public static String handlingCost() {
        String s = Item.P_HandlingCost;
        return s;
    }

    public static String makeApprovedIfNull() {
        String s = "makeApprovedIfNull";
        return s;
    }
}
 
