// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.remote;

import java.util.ArrayList;

import test.xice.tsam.remote.RemoteAppInterface;
import com.viaoa.util.OAProperties;

import test.xice.tsam.model.oa.AdminUser;
import test.xice.tsam.model.oa.cs.ClientRoot;
import test.xice.tsam.model.oa.cs.ServerRoot;

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

