// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class OperatingSystemPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public OperatingSystemPPx(String name) {
        this(null, name);
    }

    public OperatingSystemPPx(PPxInterface parent, String name) {
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

    public OSVersionPPx osVersions() {
        OSVersionPPx ppx = new OSVersionPPx(this, OperatingSystem.P_OSVersions);
        return ppx;
    }

    public String id() {
        return pp + "." + OperatingSystem.P_Id;
    }

    public String name() {
        return pp + "." + OperatingSystem.P_Name;
    }

    public String type() {
        return pp + "." + OperatingSystem.P_Type;
    }

    public String userId() {
        return pp + "." + OperatingSystem.P_UserId;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
