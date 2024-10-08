// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class WidgetPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ProgramPPx programs;
     
    public WidgetPPx(String name) {
        this(null, name);
    }

    public WidgetPPx(PPxInterface parent, String name) {
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

    public ProgramPPx programs() {
        if (programs == null) programs = new ProgramPPx(this, Widget.P_Programs);
        return programs;
    }

    public String id() {
        return pp + "." + Widget.P_Id;
    }

    public String created() {
        return pp + "." + Widget.P_Created;
    }

    public String name() {
        return pp + "." + Widget.P_Name;
    }

    public String link() {
        return pp + "." + Widget.P_Link;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
