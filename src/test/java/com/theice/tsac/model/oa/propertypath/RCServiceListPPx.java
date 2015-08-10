// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class RCServiceListPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCServiceListPPx(String name) {
        this(null, name);
    }

    public RCServiceListPPx(PPxInterface parent, String name) {
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

    public EnvironmentPPx environment() {
        EnvironmentPPx ppx = new EnvironmentPPx(this, RCServiceList.P_Environment);
        return ppx;
    }

    public RCExecutePPx rcExecute() {
        RCExecutePPx ppx = new RCExecutePPx(this, RCServiceList.P_RCExecute);
        return ppx;
    }

    public RCServiceListDetailPPx rcServiceListDetails() {
        RCServiceListDetailPPx ppx = new RCServiceListDetailPPx(this, RCServiceList.P_RCServiceListDetails);
        return ppx;
    }

    public RemoteClientPPx remoteClient() {
        RemoteClientPPx ppx = new RemoteClientPPx(this, RCServiceList.P_RemoteClient);
        return ppx;
    }

    public String id() {
        return pp + "." + RCServiceList.P_Id;
    }

    public String created() {
        return pp + "." + RCServiceList.P_Created;
    }

    public String canRun() {
        return pp + "." + RCServiceList.P_CanRun;
    }

    public String canProcess() {
        return pp + "." + RCServiceList.P_CanProcess;
    }

    public String canLoad() {
        return pp + "." + RCServiceList.P_CanLoad;
    }

    public String run() {
        return pp + ".run";
    }

    public String process() {
        return pp + ".process";
    }

    public String load() {
        return pp + ".load";
    }

    public String selectAll() {
        return pp + ".selectAll";
    }

    public String deselectAll() {
        return pp + ".deselectAll";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
