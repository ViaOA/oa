// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class ServerInstallPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EnvironmentPPx calcEnvironment;
    private ServerTypeVersionPPx newServerTypeVersion;
    private ServerPPx server;
     
    public ServerInstallPPx(String name) {
        this(null, name);
    }

    public ServerInstallPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public EnvironmentPPx calcEnvironment() {
        if (calcEnvironment == null) calcEnvironment = new EnvironmentPPx(this, ServerInstall.P_CalcEnvironment);
        return calcEnvironment;
    }

    public ServerTypeVersionPPx newServerTypeVersion() {
        if (newServerTypeVersion == null) newServerTypeVersion = new ServerTypeVersionPPx(this, ServerInstall.P_NewServerTypeVersion);
        return newServerTypeVersion;
    }

    public ServerPPx server() {
        if (server == null) server = new ServerPPx(this, ServerInstall.P_Server);
        return server;
    }

    public String id() {
        return pp + "." + ServerInstall.P_Id;
    }

    public String created() {
        return pp + "." + ServerInstall.P_Created;
    }

    public String downloadedZip() {
        return pp + "." + ServerInstall.P_DownloadedZip;
    }

    public String propagated() {
        return pp + "." + ServerInstall.P_Propagated;
    }

    public String installed() {
        return pp + "." + ServerInstall.P_Installed;
    }

    public String cancelled() {
        return pp + "." + ServerInstall.P_Cancelled;
    }

    public String errored() {
        return pp + "." + ServerInstall.P_Errored;
    }

    public String errorMessage() {
        return pp + "." + ServerInstall.P_ErrorMessage;
    }

    public String completed() {
        return pp + "." + ServerInstall.P_Completed;
    }

    public String activeFilter() {
        return pp + ":active()";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
