// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import java.io.Serializable;

import com.theice.tsactest2.model.oa.*;
 
public class RCCommandPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RCCommandPPx(String name) {
        this(null, name);
    }

    public RCCommandPPx(PPxInterface parent, String name) {
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

    public RCExecutePPx rcExecutes() {
        RCExecutePPx ppx = new RCExecutePPx(this, RCCommand.P_RCExecutes);
        return ppx;
    }

    public String id() {
        return pp + "." + RCCommand.P_Id;
    }

    public String description() {
        return pp + "." + RCCommand.P_Description;
    }

    public String commandLine() {
        return pp + "." + RCCommand.P_CommandLine;
    }

    public String type() {
        return pp + "." + RCCommand.P_Type;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
