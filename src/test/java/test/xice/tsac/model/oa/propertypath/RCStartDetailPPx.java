// Generated by OABuilder
package test.xice.tsac.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac.model.oa.*;
 
public class RCStartDetailPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCStartDetailPPx(String name) {
        this(null, name);
    }

    public RCStartDetailPPx(PPxInterface parent, String name) {
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

    public RCDeployDetailPPx rcDeployDetail() {
        RCDeployDetailPPx ppx = new RCDeployDetailPPx(this, RCStartDetail.P_RCDeployDetail);
        return ppx;
    }

    public RCStartPPx rcStart() {
        RCStartPPx ppx = new RCStartPPx(this, RCStartDetail.P_RCStart);
        return ppx;
    }

    public String id() {
        return pp + "." + RCStartDetail.P_Id;
    }

    public String host() {
        return pp + "." + RCStartDetail.P_Host;
    }

    public String service() {
        return pp + "." + RCStartDetail.P_Service;
    }

    public String result() {
        return pp + "." + RCStartDetail.P_Result;
    }

    public String totalTimeMs() {
        return pp + "." + RCStartDetail.P_TotalTimeMs;
    }

    public String started() {
        return pp + "." + RCStartDetail.P_Started;
    }

    public String stopped() {
        return pp + "." + RCStartDetail.P_Stopped;
    }

    public String error() {
        return pp + "." + RCStartDetail.P_Error;
    }

    public String invalidMessage() {
        return pp + "." + RCStartDetail.P_InvalidMessage;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
