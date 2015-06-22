// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class MRADClientPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public MRADClientPPx(String name) {
        this(null, name);
    }

    public MRADClientPPx(PPxInterface parent, String name) {
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
        ApplicationPPx ppx = new ApplicationPPx(this, MRADClient.P_Application);
        return ppx;
    }

    public MRADClientCommandPPx lastMRADClientCommand() {
        MRADClientCommandPPx ppx = new MRADClientCommandPPx(this, MRADClient.P_LastMRADClientCommand);
        return ppx;
    }

    public MRADClientMessagePPx lastMRADClientMessage() {
        MRADClientMessagePPx ppx = new MRADClientMessagePPx(this, MRADClient.P_LastMRADClientMessage);
        return ppx;
    }

    public MRADClientCommandPPx mradClientCommands() {
        MRADClientCommandPPx ppx = new MRADClientCommandPPx(this, MRADClient.P_MRADClientCommands);
        return ppx;
    }

    public MRADClientMessagePPx mradClientMessages() {
        MRADClientMessagePPx ppx = new MRADClientMessagePPx(this, MRADClient.P_MRADClientMessages);
        return ppx;
    }

    public MRADServerPPx mradServer() {
        MRADServerPPx ppx = new MRADServerPPx(this, MRADClient.P_MRADServer);
        return ppx;
    }

    public String id() {
        return pp + "." + MRADClient.P_Id;
    }

    public String created() {
        return pp + "." + MRADClient.P_Created;
    }

    public String hostName() {
        return pp + "." + MRADClient.P_HostName;
    }

    public String ipAddress() {
        return pp + "." + MRADClient.P_IpAddress;
    }

    public String name() {
        return pp + "." + MRADClient.P_Name;
    }

    public String description() {
        return pp + "." + MRADClient.P_Description;
    }

    public String routerAbsolutePath() {
        return pp + "." + MRADClient.P_RouterAbsolutePath;
    }

    public String startScript() {
        return pp + "." + MRADClient.P_StartScript;
    }

    public String stopScript() {
        return pp + "." + MRADClient.P_StopScript;
    }

    public String snapshotStartScript() {
        return pp + "." + MRADClient.P_SnapshotStartScript;
    }

    public String directory() {
        return pp + "." + MRADClient.P_Directory;
    }

    public String version() {
        return pp + "." + MRADClient.P_Version;
    }

    public String applicationStatus() {
        return pp + "." + MRADClient.P_ApplicationStatus;
    }

    public String started() {
        return pp + "." + MRADClient.P_Started;
    }

    public String ready() {
        return pp + "." + MRADClient.P_Ready;
    }

    public String serverTypeId() {
        return pp + "." + MRADClient.P_ServerTypeId;
    }

    public String applicationTypeCode() {
        return pp + "." + MRADClient.P_ApplicationTypeCode;
    }

    public String dtConnected() {
        return pp + "." + MRADClient.P_DtConnected;
    }

    public String dtDisconnected() {
        return pp + "." + MRADClient.P_DtDisconnected;
    }

    public String totalMemory() {
        return pp + "." + MRADClient.P_TotalMemory;
    }

    public String freeMemory() {
        return pp + "." + MRADClient.P_FreeMemory;
    }

    public String javaVendor() {
        return pp + "." + MRADClient.P_JavaVendor;
    }

    public String javaVersion() {
        return pp + "." + MRADClient.P_JavaVersion;
    }

    public String osArch() {
        return pp + "." + MRADClient.P_OsArch;
    }

    public String osName() {
        return pp + "." + MRADClient.P_OsName;
    }

    public String osVersion() {
        return pp + "." + MRADClient.P_OsVersion;
    }

    public String autoComplete() {
        return pp + "." + MRADClient.P_AutoComplete;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
