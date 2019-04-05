// Generated by OABuilder
package test.xice.tsam.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsam.model.oa.MRADClientCommand;
import test.xice.tsam.model.oa.propertypath.MRADClientPPx;
import test.xice.tsam.model.oa.propertypath.MRADServerCommandPPx;
import test.xice.tsam.model.oa.propertypath.PPxInterface;
import test.xice.tsam.model.oa.propertypath.SSHExecutePPx;

import test.xice.tsam.model.oa.*;
 
public class MRADClientCommandPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public MRADClientCommandPPx(String name) {
        this(null, name);
    }

    public MRADClientCommandPPx(PPxInterface parent, String name) {
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

    public MRADClientPPx mradClient() {
        MRADClientPPx ppx = new MRADClientPPx(this, MRADClientCommand.P_MRADClient);
        return ppx;
    }

    public MRADClientPPx mradClient2() {
        MRADClientPPx ppx = new MRADClientPPx(this, MRADClientCommand.P_MRADClient2);
        return ppx;
    }

    public MRADServerCommandPPx mradServerCommand() {
        MRADServerCommandPPx ppx = new MRADServerCommandPPx(this, MRADClientCommand.P_MRADServerCommand);
        return ppx;
    }

    public SSHExecutePPx sshExecute() {
        SSHExecutePPx ppx = new SSHExecutePPx(this, MRADClientCommand.P_SSHExecute);
        return ppx;
    }

    public String id() {
        return pp + "." + MRADClientCommand.P_Id;
    }

    public String created() {
        return pp + "." + MRADClientCommand.P_Created;
    }

    public String started() {
        return pp + "." + MRADClientCommand.P_Started;
    }

    public String ended() {
        return pp + "." + MRADClientCommand.P_Ended;
    }

    public String error() {
        return pp + "." + MRADClientCommand.P_Error;
    }

    public String success() {
        return pp + "." + MRADClientCommand.P_Success;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
