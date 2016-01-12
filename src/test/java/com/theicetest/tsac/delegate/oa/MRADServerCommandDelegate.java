package com.theicetest.tsac.delegate.oa;

import com.theicetest.tsac.delegate.RemoteDelegate;
import com.theicetest.tsac.model.oa.*;
import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;

public class MRADServerCommandDelegate {

    /**
     * create a new instance.
     */
    public static MRADServerCommand create(AdminUser user, Hub<MRADClient> hub, int command) {
        if (hub == null) return null;
        if (command < 0 || command >= MRADServerCommand.hubType.getSize()) return null;
        if (hub.getSize() == 0) return null;
        
        MRADServerCommand cmd = new MRADServerCommand();
        cmd.setAdminUser(user);
        cmd.setType(command);

        for (MRADClient mc : hub) {
            MRADClientCommand ccmd = new MRADClientCommand();
            ccmd.setMRADClient(mc);
            cmd.getMRADClientCommands().add(ccmd);
        }
        return cmd;
    }

    /**
     * have a command ran on the RemoteMRADServer
     */
    public static void runOnMRADServer(MRADServerCommand cmd) throws Exception {
    }
}
