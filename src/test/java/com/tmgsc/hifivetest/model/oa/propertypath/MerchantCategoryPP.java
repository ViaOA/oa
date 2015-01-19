// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class MerchantCategoryPP {
    private static MerchantCategoryPPx merchantCategories;
    private static MerchantPPx merchants;
    private static MerchantCategoryPPx parentMerchantCategory;
     

    public static MerchantCategoryPPx merchantCategories() {
        if (merchantCategories == null) merchantCategories = new MerchantCategoryPPx(MerchantCategory.P_MerchantCategories);
        return merchantCategories;
    }

    public static MerchantPPx merchants() {
        if (merchants == null) merchants = new MerchantPPx(MerchantCategory.P_Merchants);
        return merchants;
    }

    public static MerchantCategoryPPx parentMerchantCategory() {
        if (parentMerchantCategory == null) parentMerchantCategory = new MerchantCategoryPPx(MerchantCategory.P_ParentMerchantCategory);
        return parentMerchantCategory;
    }

    public static String id() {
        String s = MerchantCategory.P_Id;
        return s;
    }

    public static String name() {
        String s = MerchantCategory.P_Name;
        return s;
    }

    public static String seq() {
        String s = MerchantCategory.P_Seq;
        return s;
    }
}
 
