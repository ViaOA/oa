// Generated by OABuilder
package test.xice.tsam.remote;

import test.xice.tsam.model.oa.MRADServerCommand;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

import test.xice.tsam.delegate.*;
import test.xice.tsam.delegate.oa.*;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.remote.RemoteMRADInterface;

@OARemoteInterface
public interface RemoteModelInterface {
    public final static String BindName = "RemoteModel";

    void mradServerCommand_runOnServer(MRADServerCommand mradServerCommand) throws Exception;
}

