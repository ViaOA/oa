// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class ItemCategoryPP {
    private static ItemCategoryPPx itemCategories;
    private static ItemPPx items;
    private static ItemCategoryPPx parentItemCategory;
     

    public static ItemCategoryPPx itemCategories() {
        if (itemCategories == null) itemCategories = new ItemCategoryPPx(ItemCategory.P_ItemCategories);
        return itemCategories;
    }

    public static ItemPPx items() {
        if (items == null) items = new ItemPPx(ItemCategory.P_Items);
        return items;
    }

    public static ItemCategoryPPx parentItemCategory() {
        if (parentItemCategory == null) parentItemCategory = new ItemCategoryPPx(ItemCategory.P_ParentItemCategory);
        return parentItemCategory;
    }

    public static String id() {
        String s = ItemCategory.P_Id;
        return s;
    }

    public static String name() {
        String s = ItemCategory.P_Name;
        return s;
    }

    public static String code() {
        String s = ItemCategory.P_Code;
        return s;
    }

    public static String seq() {
        String s = ItemCategory.P_Seq;
        return s;
    }

    public static String hifiveRating() {
        String s = ItemCategory.P_HifiveRating;
        return s;
    }

    public static String hifiveRatingDate() {
        String s = ItemCategory.P_HifiveRatingDate;
        return s;
    }

    public static String hifiveRatingNote() {
        String s = ItemCategory.P_HifiveRatingNote;
        return s;
    }
}
 
