// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class RCServerListDetailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCServerListDetailPPx(String name) {
        this(null, name);
    }

    public RCServerListDetailPPx(PPxInterface parent, String name) {
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

    public ApplicationPPx applications() {
        ApplicationPPx ppx = new ApplicationPPx(this, RCServerListDetail.P_Applications);
        return ppx;
    }

    public RCServerListPPx rcServerList() {
        RCServerListPPx ppx = new RCServerListPPx(this, RCServerListDetail.P_RCServerList);
        return ppx;
    }

    public ServerPPx server() {
        ServerPPx ppx = new ServerPPx(this, RCServerListDetail.P_Server);
        return ppx;
    }

    public String id() {
        return pp + "." + RCServerListDetail.P_Id;
    }

    public String hostName() {
        return pp + "." + RCServerListDetail.P_HostName;
    }

    public String packages() {
        return pp + "." + RCServerListDetail.P_Packages;
    }

    public String invalidMessage() {
        return pp + "." + RCServerListDetail.P_InvalidMessage;
    }

    public String selected() {
        return pp + "." + RCServerListDetail.P_Selected;
    }

    public String loaded() {
        return pp + "." + RCServerListDetail.P_Loaded;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
