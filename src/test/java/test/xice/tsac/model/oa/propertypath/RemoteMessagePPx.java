// Generated by OABuilder
package test.xice.tsac.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac.model.oa.*;
 
public class RemoteMessagePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public RemoteMessagePPx(String name) {
        this(null, name);
    }

    public RemoteMessagePPx(PPxInterface parent, String name) {
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

    public LLADServerPPx lladServer() {
        LLADServerPPx ppx = new LLADServerPPx(this, RemoteMessage.P_LLADServer);
        return ppx;
    }

    public RemoteClientPPx remoteClient() {
        RemoteClientPPx ppx = new RemoteClientPPx(this, RemoteMessage.P_RemoteClient);
        return ppx;
    }

    public String id() {
        return pp + "." + RemoteMessage.P_Id;
    }

    public String created() {
        return pp + "." + RemoteMessage.P_Created;
    }

    public String name() {
        return pp + "." + RemoteMessage.P_Name;
    }

    public String message() {
        return pp + "." + RemoteMessage.P_Message;
    }

    public String error() {
        return pp + "." + RemoteMessage.P_Error;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
