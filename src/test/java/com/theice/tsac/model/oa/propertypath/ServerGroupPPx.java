// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class ServerGroupPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private MRADClientPPx mradClients;
    private SchedulePPx schedules;
    private SiloPPx silo;
     
    public ServerGroupPPx(String name) {
        this(null, name);
    }

    public ServerGroupPPx(PPxInterface parent, String name) {
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

    public MRADClientPPx mradClients() {
        if (mradClients == null) mradClients = new MRADClientPPx(this, ServerGroup.P_MRADClients);
        return mradClients;
    }

    public SchedulePPx schedules() {
        if (schedules == null) schedules = new SchedulePPx(this, ServerGroup.P_Schedules);
        return schedules;
    }

    public SiloPPx silo() {
        if (silo == null) silo = new SiloPPx(this, ServerGroup.P_Silo);
        return silo;
    }

    public String id() {
        return pp + "." + ServerGroup.P_Id;
    }

    public String code() {
        return pp + "." + ServerGroup.P_Code;
    }

    public String name() {
        return pp + "." + ServerGroup.P_Name;
    }

    public String seq() {
        return pp + "." + ServerGroup.P_Seq;
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
 
