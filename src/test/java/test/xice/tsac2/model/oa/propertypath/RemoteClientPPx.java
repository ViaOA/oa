// Generated by OABuilder
package test.xice.tsac2.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac2.model.oa.*;
 
public class RemoteClientPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RemoteClientPPx(String name) {
        this(null, name);
    }

    public RemoteClientPPx(PPxInterface parent, String name) {
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

    public AdminServerPPx adminServers() {
        AdminServerPPx ppx = new AdminServerPPx(this, RemoteClient.P_AdminServers);
        return ppx;
    }

    public RCExecutePPx rcExecutes() {
        RCExecutePPx ppx = new RCExecutePPx(this, RemoteClient.P_RCExecutes);
        return ppx;
    }

    public RCInstalledVersionPPx rcInstalledVersions() {
        RCInstalledVersionPPx ppx = new RCInstalledVersionPPx(this, RemoteClient.P_RCInstalledVersions);
        return ppx;
    }

    public RCRepoVersionPPx rcRepoVersions() {
        RCRepoVersionPPx ppx = new RCRepoVersionPPx(this, RemoteClient.P_RCRepoVersions);
        return ppx;
    }

    public RCServiceListPPx rcServiceLists() {
        RCServiceListPPx ppx = new RCServiceListPPx(this, RemoteClient.P_RCServiceLists);
        return ppx;
    }

    public RemoteMessagePPx remoteMessages() {
        RemoteMessagePPx ppx = new RemoteMessagePPx(this, RemoteClient.P_RemoteMessages);
        return ppx;
    }

    public String id() {
        return pp + "." + RemoteClient.P_Id;
    }

    public String name() {
        return pp + "." + RemoteClient.P_Name;
    }

    public String type() {
        return pp + "." + RemoteClient.P_Type;
    }

    public String started() {
        return pp + "." + RemoteClient.P_Started;
    }

    public String status() {
        return pp + "." + RemoteClient.P_Status;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
