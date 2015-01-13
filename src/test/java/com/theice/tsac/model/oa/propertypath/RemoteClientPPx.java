// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class RemoteClientPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private RemoteMessagePPx remoteMessages;
     
    public RemoteClientPPx(String name) {
        this(null, name);
    }

    public RemoteClientPPx(PPxInterface parent, String name) {
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

    public RemoteMessagePPx remoteMessages() {
        if (remoteMessages == null) remoteMessages = new RemoteMessagePPx(this, RemoteClient.P_RemoteMessages);
        return remoteMessages;
    }

    public String id() {
        return pp + "." + RemoteClient.P_Id;
    }

    public String name() {
        return pp + "." + RemoteClient.P_Name;
    }

    public String type() {
        return pp + "." + RemoteClient.P_Type;
    }

    public String started() {
        return pp + "." + RemoteClient.P_Started;
    }

    public String status() {
        return pp + "." + RemoteClient.P_Status;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
