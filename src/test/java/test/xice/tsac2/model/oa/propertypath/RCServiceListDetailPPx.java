// Generated by OABuilder
package test.xice.tsac2.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac2.model.oa.*;
 
public class RCServiceListDetailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCServiceListDetailPPx(String name) {
        this(null, name);
    }

    public RCServiceListDetailPPx(PPxInterface parent, String name) {
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

    public ApplicationTypePPx applicationType() {
        ApplicationTypePPx ppx = new ApplicationTypePPx(this, RCServiceListDetail.P_ApplicationType);
        return ppx;
    }

    public RCServiceListPPx rcServiceList() {
        RCServiceListPPx ppx = new RCServiceListPPx(this, RCServiceListDetail.P_RCServiceList);
        return ppx;
    }

    public String id() {
        return pp + "." + RCServiceListDetail.P_Id;
    }

    public String name() {
        return pp + "." + RCServiceListDetail.P_Name;
    }

    public String login() {
        return pp + "." + RCServiceListDetail.P_Login;
    }

    public String packages() {
        return pp + "." + RCServiceListDetail.P_Packages;
    }

    public String type() {
        return pp + "." + RCServiceListDetail.P_Type;
    }

    public String baseDirectory() {
        return pp + "." + RCServiceListDetail.P_BaseDirectory;
    }

    public String startCommand() {
        return pp + "." + RCServiceListDetail.P_StartCommand;
    }

    public String stopCommand() {
        return pp + "." + RCServiceListDetail.P_StopCommand;
    }

    public String healthPort() {
        return pp + "." + RCServiceListDetail.P_HealthPort;
    }

    public String invalidMessage() {
        return pp + "." + RCServiceListDetail.P_InvalidMessage;
    }

    public String selected() {
        return pp + "." + RCServiceListDetail.P_Selected;
    }

    public String loaded() {
        return pp + "." + RCServiceListDetail.P_Loaded;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
