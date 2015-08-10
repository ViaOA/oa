// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class RCInstallDetailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCInstallDetailPPx(String name) {
        this(null, name);
    }

    public RCInstallDetailPPx(PPxInterface parent, String name) {
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

    public PackageVersionPPx afterPackageVersion() {
        PackageVersionPPx ppx = new PackageVersionPPx(this, RCInstallDetail.P_AfterPackageVersion);
        return ppx;
    }

    public PackageVersionPPx beforePackageVersion() {
        PackageVersionPPx ppx = new PackageVersionPPx(this, RCInstallDetail.P_BeforePackageVersion);
        return ppx;
    }

    public RCDeployDetailPPx rcDeployDetail() {
        RCDeployDetailPPx ppx = new RCDeployDetailPPx(this, RCInstallDetail.P_RCDeployDetail);
        return ppx;
    }

    public RCInstallPPx rcInstall() {
        RCInstallPPx ppx = new RCInstallPPx(this, RCInstallDetail.P_RCInstall);
        return ppx;
    }

    public ServerPPx server() {
        ServerPPx ppx = new ServerPPx(this, RCInstallDetail.P_Server);
        return ppx;
    }

    public String id() {
        return pp + "." + RCInstallDetail.P_Id;
    }

    public String selected() {
        return pp + "." + RCInstallDetail.P_Selected;
    }

    public String error() {
        return pp + "." + RCInstallDetail.P_Error;
    }

    public String message() {
        return pp + "." + RCInstallDetail.P_Message;
    }

    public String packageId() {
        return pp + "." + RCInstallDetail.P_PackageId;
    }

    public String packageName() {
        return pp + "." + RCInstallDetail.P_PackageName;
    }

    public String beforeVersion() {
        return pp + "." + RCInstallDetail.P_BeforeVersion;
    }

    public String afterVersion() {
        return pp + "." + RCInstallDetail.P_AfterVersion;
    }

    public String destHost() {
        return pp + "." + RCInstallDetail.P_DestHost;
    }

    public String totalTime() {
        return pp + "." + RCInstallDetail.P_TotalTime;
    }

    public String invalidMessage() {
        return pp + "." + RCInstallDetail.P_InvalidMessage;
    }

    public String loaded() {
        return pp + "." + RCInstallDetail.P_Loaded;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
