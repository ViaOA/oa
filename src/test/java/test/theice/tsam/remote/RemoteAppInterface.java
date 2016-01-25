// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.theice.tsam.remote;

import java.util.ArrayList;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.util.OAProperties;

import test.theice.tsam.model.oa.AdminUser;
import test.theice.tsam.model.oa.cs.ClientRoot;
import test.theice.tsam.model.oa.cs.ServerRoot;

@OARemoteInterface
public interface RemoteAppInterface {

    public final static String BindName = "RemoteApp";

    public void saveData();
    
    public AdminUser getUser(int clientId, String userId, String password, String location, String userComputerName);
    public ServerRoot getServerRoot();
    public ClientRoot getClientRoot(int clientId);

    public String getRelease();
    public boolean isRunningAsDemo();
    public Object testBandwidth(Object data);
    public long getServerTime();
    public boolean disconnectDatabase();

    public OAProperties getServerProperties();
    public String getResourceValue(String name);
    
    public boolean writeToClientLogFile(int clientId, ArrayList al);

    // remote clients
    /*$$Start: RemoteAppInterface.remoteClient $$*/
    /*$$End: RemoteAppInterface.remoteClient $$*/
}
