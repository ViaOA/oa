// Copied from OATemplate project by OABuilder 04/03/14 04:05 PM
package test.xice.tsam.delegate;

import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.hub.*;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.util.*;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.AdminUser;
import test.xice.tsam.model.oa.ConnectionInfo;
import test.xice.tsam.model.oa.ExceptionType;
import test.xice.tsam.model.oa.IniInfo;
import test.xice.tsam.model.oa.ServerInfo;
import test.xice.tsam.model.oa.SystemInfo;
import test.xice.tsam.model.oa.custom.*;

/**
 * This is used to access all of the Root level Hubs.  This is so that they 
 * will not have to be passed into and through the models.
 * After client login, the Hubs will be shared with the Hubs in the ServerRoot object from the server.
 * @author vincevia
 * 
 * @see ClientController#initializeClientModel
 */
public class ServerModelDelegate {

    private static Hub<AdminUser> hubAdminUser;
    private static OAProperties properties;
    private static ConnectionInfo connectionInfo;
    
    private static Hub<ServerInfo> hubServerInfos = new Hub<ServerInfo>(ServerInfo.class); 
    private static Hub<IniInfo> hubIniInfos;
    private static Hub<SystemInfo> hubSystemInfos;
    private static Hub<ConnectionInfo> hubConnectionInfos;
    private static Hub<ExceptionType> hubExceptionTypes;
    
    public static Hub<AdminUser> getAdminUsers() {
        if (hubAdminUser == null) {
            hubAdminUser = new Hub<AdminUser>(AdminUser.class);
        }
        return hubAdminUser;
    }

    
// qqqq not currently used     
    public static OAProperties getProperties() {
        if (properties == null) properties = new OAProperties();
        return properties;
    }
    public static void setProperties(OAProperties props) {
        properties = props;
    }


    // set from ServerRoot
    public static Hub<ServerInfo> getServerInfoHub() {
        return hubServerInfos;
    }
    public static Hub<ServerInfo> getServerInfos() {
        return hubServerInfos;
    }

    public static ServerInfo getServerInfo() {
        ServerInfo si = hubServerInfos.getAt(0);
        return si;
    }
    
    public static Hub<IniInfo> getIniInfos() {
        if (hubIniInfos == null) {
            hubIniInfos = new Hub<IniInfo>(IniInfo.class);
        }
        return hubIniInfos;
    }

    public static Hub<SystemInfo> getSystemInfos() {
        if (hubSystemInfos == null) {
            hubSystemInfos = new Hub<SystemInfo>(SystemInfo.class);
        }
        return hubSystemInfos;
    }

    public static Hub<ConnectionInfo> getConnectionInfos() {
        if (hubConnectionInfos == null) {
            hubConnectionInfos = new Hub<ConnectionInfo>(ConnectionInfo.class);
        }
        return hubConnectionInfos;
    }

    public static Hub<ExceptionType> getExceptionTypes() {
        if (hubExceptionTypes == null) {
            hubExceptionTypes = new Hub<ExceptionType>(ExceptionType.class);
        }
        return hubExceptionTypes;
    }

    public static ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public static void setConnectionInfo(ConnectionInfo newValue) {
        connectionInfo = newValue;
    }
    
    public static AdminUser getAdminUser(ConnectionInfo ci) {
        if (ci == null) return null;
        String userId = ci.getUserId();
        if (OAString.isEmpty(userId)) return null;
        
        for (AdminUser user : getAdminUsers()) {
            if (userId.equals(user.getLoginId())) return user;
        }
        return null;
    }

    /**
     * Get the AdminUser for the current thread.  If this is a remote call,
     * then this will return the UserId for the client that is making the call.
     */
    public static AdminUser getRemoteAdminUser() {
        RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
        ConnectionInfo ci = null;
        if (ri != null) ci = connectionInfo;
        if (ci == null) {
            ci = connectionInfo;
            if (ci == null) return null;
        }

        ci = ModelDelegate.getConnectionInfos().getObject(ri.connectionId);
        return getAdminUser(ci);
    }
    
}



