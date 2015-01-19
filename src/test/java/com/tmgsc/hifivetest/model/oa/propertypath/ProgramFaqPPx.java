// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import java.io.Serializable;

import com.tmgsc.hifivetest.model.oa.*;
 
public class ProgramFaqPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private LocationPPx location;
    private ProgramPPx program;
     
    public ProgramFaqPPx(String name) {
        this(null, name);
    }

    public ProgramFaqPPx(PPxInterface parent, String name) {
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

    public LocationPPx location() {
        if (location == null) location = new LocationPPx(this, ProgramFaq.P_Location);
        return location;
    }

    public ProgramPPx program() {
        if (program == null) program = new ProgramPPx(this, ProgramFaq.P_Program);
        return program;
    }

    public String id() {
        return pp + "." + ProgramFaq.P_Id;
    }

    public String created() {
        return pp + "." + ProgramFaq.P_Created;
    }

    public String seq() {
        return pp + "." + ProgramFaq.P_Seq;
    }

    public String question() {
        return pp + "." + ProgramFaq.P_Question;
    }

    public String answer() {
        return pp + "." + ProgramFaq.P_Answer;
    }

    public String managerOnly() {
        return pp + "." + ProgramFaq.P_ManagerOnly;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
