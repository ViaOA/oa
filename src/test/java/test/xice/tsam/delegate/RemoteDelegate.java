// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsam.delegate;

import java.util.logging.*;

import test.xice.tsam.remote.RemoteAppInterface;
import test.xice.tsam.remote.RemoteFileInterface;
import test.xice.tsam.remote.RemoteModelInterface;
import test.xice.tsam.remote.RemoteSpellCheckInterface;
import com.viaoa.sync.*;

import test.xice.tsam.delegate.RemoteDelegate;
import test.xice.tsam.remote.*;

/**
  This class is used for interactions between workstations (clients) and server.
 */
public class RemoteDelegate {
	private static Logger LOG = Logger.getLogger(RemoteDelegate.class.getName());

    private static RemoteAppInterface remoteApp; 
	private static RemoteFileInterface remoteFile;
    private static RemoteSpellCheckInterface remoteSpellCheck;
    private static RemoteModelInterface remoteModel;

    // Remote Clients
    /*$$Start: RemoteDelegate.remoteClient1 $$*/
    /*$$End: RemoteDelegate.remoteClient1 $$*/

    public static void setRemoteApp(RemoteAppInterface rai) {
        remoteApp = rai;
    }
    public static RemoteAppInterface getRemoteApp() {
        if (remoteApp != null) return remoteApp;
        
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteApp = (RemoteAppInterface) sc.lookup(RemoteAppInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteApp;
    }

    // set by ServerController
    public static void setRemoteSpellCheck(RemoteSpellCheckInterface remoteSpellCheck) {
        RemoteDelegate.remoteSpellCheck = remoteSpellCheck;
    }
    public static RemoteSpellCheckInterface getRemoteSpellCheck() {
        if (remoteSpellCheck != null) return remoteSpellCheck;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteSpellCheck = (RemoteSpellCheckInterface) sc.lookup(RemoteSpellCheckInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteSpellCheck;
    }
	
    // set by ServerController
    public static void setRemoteFile(RemoteFileInterface remoteFile) {
        RemoteDelegate.remoteFile = remoteFile;
    }
    public static RemoteFileInterface getRemoteFile() {
        if (remoteFile != null) return remoteFile;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteFile = (RemoteFileInterface) sc.lookup(RemoteFileInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteFile;
    }
    
    
    // set by ServerController
    public static void setRemoteModel(RemoteModelInterface remoteModel) {
        RemoteDelegate.remoteModel = remoteModel;
    }
    public static RemoteModelInterface getRemoteModel() {
        if (remoteModel != null) return remoteModel;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteModel = (RemoteModelInterface) sc.lookup(RemoteModelInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteModel;
    }

    // Remote Clients ========= set by ServerController
    /*$$Start: RemoteDelegate.remoteClient2 $$*/
    /*$$End: RemoteDelegate.remoteClient2 $$*/
    
}
