// Generated by OABuilder
package test.xice.tsac3.model.oa.propertypath;
 
import test.xice.tsac3.model.oa.*;
 
public class ServerTypeClientVersionPP {
    private static ServerTypeVersionPPx clientServerTypeVersion;
    private static ServerTypeVersionPPx serverTypeVersion;
     

    public static ServerTypeVersionPPx clientServerTypeVersion() {
        if (clientServerTypeVersion == null) clientServerTypeVersion = new ServerTypeVersionPPx(ServerTypeClientVersion.P_ClientServerTypeVersion);
        return clientServerTypeVersion;
    }

    public static ServerTypeVersionPPx serverTypeVersion() {
        if (serverTypeVersion == null) serverTypeVersion = new ServerTypeVersionPPx(ServerTypeClientVersion.P_ServerTypeVersion);
        return serverTypeVersion;
    }

    public static String id() {
        String s = ServerTypeClientVersion.P_Id;
        return s;
    }
}
 
