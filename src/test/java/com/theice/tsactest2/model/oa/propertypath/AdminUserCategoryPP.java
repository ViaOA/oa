// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import com.theice.tsactest2.model.oa.*;
 
public class AdminUserCategoryPP {
    private static AdminUserCategoryPPx adminUserCategories;
    private static AdminUserCategoryPPx parentAdminUserCategory;
     

    public static AdminUserCategoryPPx adminUserCategories() {
        if (adminUserCategories == null) adminUserCategories = new AdminUserCategoryPPx(AdminUserCategory.P_AdminUserCategories);
        return adminUserCategories;
    }

    public static AdminUserCategoryPPx parentAdminUserCategory() {
        if (parentAdminUserCategory == null) parentAdminUserCategory = new AdminUserCategoryPPx(AdminUserCategory.P_ParentAdminUserCategory);
        return parentAdminUserCategory;
    }

    public static String id() {
        String s = AdminUserCategory.P_Id;
        return s;
    }

    public static String name() {
        String s = AdminUserCategory.P_Name;
        return s;
    }
}
 
