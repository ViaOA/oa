// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class HifiveQualityPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private HifivePPx hifives;
    private LocationPPx location;
    private ProgramPPx program;
     
    public HifiveQualityPPx(String name) {
        this(null, name);
    }

    public HifiveQualityPPx(PPxInterface parent, String name) {
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

    public HifivePPx hifives() {
        if (hifives == null) hifives = new HifivePPx(this, HifiveQuality.P_Hifives);
        return hifives;
    }

    public LocationPPx location() {
        if (location == null) location = new LocationPPx(this, HifiveQuality.P_Location);
        return location;
    }

    public ProgramPPx program() {
        if (program == null) program = new ProgramPPx(this, HifiveQuality.P_Program);
        return program;
    }

    public String id() {
        return pp + "." + HifiveQuality.P_Id;
    }

    public String seq() {
        return pp + "." + HifiveQuality.P_Seq;
    }

    public String name() {
        return pp + "." + HifiveQuality.P_Name;
    }

    public String description() {
        return pp + "." + HifiveQuality.P_Description;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
