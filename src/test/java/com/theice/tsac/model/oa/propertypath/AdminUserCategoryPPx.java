// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class AdminUserCategoryPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public AdminUserCategoryPPx(String name) {
        this(null, name);
    }

    public AdminUserCategoryPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null && name.length() > 0) {
            if (s.length() > 0 && name.charAt(0) != ':') s += ".";
            s += name;
        }
        pp = s;
    }

    public AdminUserCategoryPPx adminUserCategories() {
        AdminUserCategoryPPx ppx = new AdminUserCategoryPPx(this, AdminUserCategory.P_AdminUserCategories);
        return ppx;
    }

    public AdminUserCategoryPPx parentAdminUserCategory() {
        AdminUserCategoryPPx ppx = new AdminUserCategoryPPx(this, AdminUserCategory.P_ParentAdminUserCategory);
        return ppx;
    }

    public String id() {
        return pp + "." + AdminUserCategory.P_Id;
    }

    public String name() {
        return pp + "." + AdminUserCategory.P_Name;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
