// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class SiloTypePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public SiloTypePPx(String name) {
        this(null, name);
    }

    public SiloTypePPx(PPxInterface parent, String name) {
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

    public ApplicationTypePPx applicationTypes() {
        ApplicationTypePPx ppx = new ApplicationTypePPx(this, SiloType.P_ApplicationTypes);
        return ppx;
    }

    public SiloPPx silos() {
        SiloPPx ppx = new SiloPPx(this, SiloType.P_Silos);
        return ppx;
    }

    public String id() {
        return pp + "." + SiloType.P_Id;
    }

    public String name() {
        return pp + "." + SiloType.P_Name;
    }

    public String type() {
        return pp + "." + SiloType.P_Type;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
