// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import java.io.Serializable;

import com.theice.tsactest2.model.oa.*;
 
public class RCRepoVersionDetailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCRepoVersionDetailPPx(String name) {
        this(null, name);
    }

    public RCRepoVersionDetailPPx(PPxInterface parent, String name) {
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

    public PackageTypePPx packageType() {
        PackageTypePPx ppx = new PackageTypePPx(this, RCRepoVersionDetail.P_PackageType);
        return ppx;
    }

    public PackageVersionPPx packageVersion() {
        PackageVersionPPx ppx = new PackageVersionPPx(this, RCRepoVersionDetail.P_PackageVersion);
        return ppx;
    }

    public RCRepoVersionPPx rcRepoVersion() {
        RCRepoVersionPPx ppx = new RCRepoVersionPPx(this, RCRepoVersionDetail.P_RCRepoVersion);
        return ppx;
    }

    public String id() {
        return pp + "." + RCRepoVersionDetail.P_Id;
    }

    public String packageName() {
        return pp + "." + RCRepoVersionDetail.P_PackageName;
    }

    public String buildDate() {
        return pp + "." + RCRepoVersionDetail.P_BuildDate;
    }

    public String version() {
        return pp + "." + RCRepoVersionDetail.P_Version;
    }

    public String error() {
        return pp + "." + RCRepoVersionDetail.P_Error;
    }

    public String invalidMessage() {
        return pp + "." + RCRepoVersionDetail.P_InvalidMessage;
    }

    public String selected() {
        return pp + "." + RCRepoVersionDetail.P_Selected;
    }

    public String loaded() {
        return pp + "." + RCRepoVersionDetail.P_Loaded;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
