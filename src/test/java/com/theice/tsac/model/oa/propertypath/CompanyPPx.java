// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class CompanyPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public CompanyPPx(String name) {
        this(null, name);
    }

    public CompanyPPx(PPxInterface parent, String name) {
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

    public CompanyPPx companies() {
        CompanyPPx ppx = new CompanyPPx(this, Company.P_Companies);
        return ppx;
    }

    public EnvironmentPPx environment() {
        EnvironmentPPx ppx = new EnvironmentPPx(this, Company.P_Environment);
        return ppx;
    }

    public CompanyPPx parentCompany() {
        CompanyPPx ppx = new CompanyPPx(this, Company.P_ParentCompany);
        return ppx;
    }

    public UserPPx users() {
        UserPPx ppx = new UserPPx(this, Company.P_Users);
        return ppx;
    }

    public String id() {
        return pp + "." + Company.P_Id;
    }

    public String companyId() {
        return pp + "." + Company.P_CompanyId;
    }

    public String name() {
        return pp + "." + Company.P_Name;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
