// Generated by OABuilder
package test.xice.tsac2.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac2.model.oa.*;
 
public class ApplicationVersionPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public ApplicationVersionPPx(String name) {
        this(null, name);
    }

    public ApplicationVersionPPx(PPxInterface parent, String name) {
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

    public ApplicationPPx application() {
        ApplicationPPx ppx = new ApplicationPPx(this, ApplicationVersion.P_Application);
        return ppx;
    }

    public PackageTypePPx packageType() {
        PackageTypePPx ppx = new PackageTypePPx(this, ApplicationVersion.P_PackageType);
        return ppx;
    }

    public PackageVersionPPx packageVersion() {
        PackageVersionPPx ppx = new PackageVersionPPx(this, ApplicationVersion.P_PackageVersion);
        return ppx;
    }

    public String id() {
        return pp + "." + ApplicationVersion.P_Id;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
