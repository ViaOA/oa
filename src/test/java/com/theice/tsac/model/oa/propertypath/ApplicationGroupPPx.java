// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class ApplicationGroupPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public ApplicationGroupPPx(String name) {
        this(null, name);
    }

    public ApplicationGroupPPx(PPxInterface parent, String name) {
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

    public ApplicationPPx applications() {
        ApplicationPPx ppx = new ApplicationPPx(this, ApplicationGroup.P_Applications);
        return ppx;
    }

    public ApplicationTypePPx applicationTypes() {
        ApplicationTypePPx ppx = new ApplicationTypePPx(this, ApplicationGroup.P_ApplicationTypes);
        return ppx;
    }

    public SchedulePPx schedules() {
        SchedulePPx ppx = new SchedulePPx(this, ApplicationGroup.P_Schedules);
        return ppx;
    }

    public SiloPPx silo() {
        SiloPPx ppx = new SiloPPx(this, ApplicationGroup.P_Silo);
        return ppx;
    }

    public String id() {
        return pp + "." + ApplicationGroup.P_Id;
    }

    public String code() {
        return pp + "." + ApplicationGroup.P_Code;
    }

    public String name() {
        return pp + "." + ApplicationGroup.P_Name;
    }

    public String seq() {
        return pp + "." + ApplicationGroup.P_Seq;
    }

    public String start() {
        return pp + ".start";
    }

    public String stop() {
        return pp + ".stop";
    }

    public String kill() {
        return pp + ".kill";
    }

    public String suspend() {
        return pp + ".suspend";
    }

    public String resume() {
        return pp + ".resume";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
