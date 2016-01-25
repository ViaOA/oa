// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.theice.tsam.remote;

import java.util.ArrayList;

import test.theice.tsam.remote.RemoteAppInterface;
import com.viaoa.util.OAProperties;

import test.theice.tsam.model.oa.AdminUser;
import test.theice.tsam.model.oa.cs.ClientRoot;
import test.theice.tsam.model.oa.cs.ServerRoot;

public abstract class RemoteAppImpl implements RemoteAppInterface {

    @Override
    public abstract void saveData();

    @Override
    public abstract AdminUser getUser(int clientId, String userId, String password, String location, String userComputerName);

    @Override
    public abstract ServerRoot getServerRoot();

    @Override
    public abstract ClientRoot getClientRoot(int clientId);

    @Override
    public String getRelease() {
        return "12345"; // expecting a String
    }

    @Override
    public abstract boolean isRunningAsDemo();

    @Override
    public Object testBandwidth(Object data) {
        return data;
    }

    @Override
    public long getServerTime() {
        return System.currentTimeMillis();
    }

    @Override
    public abstract boolean disconnectDatabase();

    @Override
    public abstract OAProperties getServerProperties();

    @Override
    public String getResourceValue(String name) {
        return "test";
    }

    @Override
    public abstract boolean writeToClientLogFile(int clientId, ArrayList al);

}

