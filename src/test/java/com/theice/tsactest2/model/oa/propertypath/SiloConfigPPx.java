// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import java.io.Serializable;

import com.theice.tsactest2.model.oa.*;
 
public class SiloConfigPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public SiloConfigPPx(String name) {
        this(null, name);
    }

    public SiloConfigPPx(PPxInterface parent, String name) {
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

    public ApplicationTypePPx applicationType() {
        ApplicationTypePPx ppx = new ApplicationTypePPx(this, SiloConfig.P_ApplicationType);
        return ppx;
    }

    public SiloPPx silo() {
        SiloPPx ppx = new SiloPPx(this, SiloConfig.P_Silo);
        return ppx;
    }

    public SiloConfigVersioinPPx siloConfigVersioins() {
        SiloConfigVersioinPPx ppx = new SiloConfigVersioinPPx(this, SiloConfig.P_SiloConfigVersioins);
        return ppx;
    }

    public String id() {
        return pp + "." + SiloConfig.P_Id;
    }

    public String minCount() {
        return pp + "." + SiloConfig.P_MinCount;
    }

    public String maxCount() {
        return pp + "." + SiloConfig.P_MaxCount;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
