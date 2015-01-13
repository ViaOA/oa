// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class SiloServerInfoPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ServerTypePPx serverType;
    private ServerTypeVersionPPx serverTypeVersion;
    private SiloPPx silo;
     
    public SiloServerInfoPPx(String name) {
        this(null, name);
    }

    public SiloServerInfoPPx(PPxInterface parent, String name) {
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

    public ServerTypePPx serverType() {
        if (serverType == null) serverType = new ServerTypePPx(this, SiloServerInfo.P_ServerType);
        return serverType;
    }

    public ServerTypeVersionPPx serverTypeVersion() {
        if (serverTypeVersion == null) serverTypeVersion = new ServerTypeVersionPPx(this, SiloServerInfo.P_ServerTypeVersion);
        return serverTypeVersion;
    }

    public SiloPPx silo() {
        if (silo == null) silo = new SiloPPx(this, SiloServerInfo.P_Silo);
        return silo;
    }

    public String id() {
        return pp + "." + SiloServerInfo.P_Id;
    }

    public String minCount() {
        return pp + "." + SiloServerInfo.P_MinCount;
    }

    public String maxCount() {
        return pp + "." + SiloServerInfo.P_MaxCount;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
