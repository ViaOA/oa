// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class RCRepoVersionPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCRepoVersionPPx(String name) {
        this(null, name);
    }

    public RCRepoVersionPPx(PPxInterface parent, String name) {
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
        EnvironmentPPx ppx = new EnvironmentPPx(this, RCRepoVersion.P_Environment);
        return ppx;
    }

    public RCExecutePPx rcExecute() {
        RCExecutePPx ppx = new RCExecutePPx(this, RCRepoVersion.P_RCExecute);
        return ppx;
    }

    public RCRepoVersionDetailPPx rcRepoVersionDetails() {
        RCRepoVersionDetailPPx ppx = new RCRepoVersionDetailPPx(this, RCRepoVersion.P_RCRepoVersionDetails);
        return ppx;
    }

    public RemoteClientPPx remoteClient() {
        RemoteClientPPx ppx = new RemoteClientPPx(this, RCRepoVersion.P_RemoteClient);
        return ppx;
    }

    public String id() {
        return pp + "." + RCRepoVersion.P_Id;
    }

    public String created() {
        return pp + "." + RCRepoVersion.P_Created;
    }

    public String canRun() {
        return pp + "." + RCRepoVersion.P_CanRun;
    }

    public String canProcess() {
        return pp + "." + RCRepoVersion.P_CanProcess;
    }

    public String canLoad() {
        return pp + "." + RCRepoVersion.P_CanLoad;
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

    @Override
    public String toString() {
        return pp;
    }
}
 
