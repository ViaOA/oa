package test.xice.tsac.delegate.oa;

import java.util.concurrent.locks.ReentrantLock;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.Application;
import test.xice.tsac.model.oa.ApplicationStatus;
import test.xice.tsac.model.oa.ApplicationType;
import test.xice.tsac.model.oa.Environment;
import test.xice.tsac.model.oa.MRADClient;
import test.xice.tsac.model.oa.MRADServer;
import test.xice.tsac.model.oa.OSVersion;
import test.xice.tsac.model.oa.OperatingSystem;
import test.xice.tsac.model.oa.Server;
import test.xice.tsac.model.oa.Silo;
import test.xice.tsac.model.oa.SiloType;
import test.xice.tsac.model.oa.propertypath.*;

public class MRADClientDelegate {

    private static ReentrantLock lock = new ReentrantLock();
    
    public static MRADClient getMRADClient(MRADServer mradServer, test.xice.tsac.mrad.model.Application clientApp) {
        try {
            lock.lock();
            MRADClient mc = _getMRADClient(mradServer, clientApp);
            return mc;
        }
        finally {
            lock.unlock();
        }
    }
    private static MRADClient _getMRADClient(MRADServer mradServer, test.xice.tsac.mrad.model.Application clientApp) {
        if (mradServer == null) return null;
        if (clientApp == null) return null;
        
        final Environment env = mradServer.getEnvironment();
        if (env == null) return null;
        
        test.xice.tsac.mrad.model.Server clientServer = clientApp.getServer();
        if (clientServer == null) return null;
        
        test.xice.tsac.mrad.model.ApplicationType clientAppType = clientApp.getApplicationType();
        if (clientAppType == null) return null;
        
        test.xice.tsac.mrad.model.SiloType clientSiloType = clientServer.getSiloType();
        if (clientSiloType == null) return null;
        final SiloType siloType = SiloTypeDelegate.getSiloType(clientSiloType);
        
        Silo silo = SiloDelegate.getSilo(env, siloType);
        if (silo == null) {
            silo = ModelDelegate.getDefaultSilo();
            if (silo == null) return null;
        }
        
        // find matching server
        Server server = null;
        boolean bValid = false;
        String s = clientServer.getIpAddress();
        if (!OAString.isEmpty(s)) {
            server = silo.getServers().find(ServerPP.ipAddress(), s);
            bValid = true;
        }
        if (server == null) {
            s = clientServer.getHostName();
            if (!OAString.isEmpty(s)) {
                server = silo.getServers().find(ServerPP.hostName(), s);
                bValid = true;
            }
        }
        if (!bValid) return null;
        
        if (server == null) {
            server = new Server();
            server.setSilo(silo);
        }
        s = clientServer.getIpAddress();
        if (!OAString.isEmpty(s)) {
            server.setIpAddress(s);
        }
        s = clientServer.getHostName();
        if (!OAString.isEmpty(s)) {
            server.setHostName(s);
        }
        
        // find the matching ApplicationType
        
        ApplicationType applicationType = null;
        s = clientAppType.getCode();
        if (!OAString.isEmpty(s)) {
            applicationType = ModelDelegate.getApplicationTypes().find(ApplicationTypePP.code(), s);
        }
        if (applicationType == null) {
            int stid = clientAppType.getServerTypeId();
            if (stid > 0) {
                applicationType = ModelDelegate.getApplicationTypes().find(ApplicationTypePP.serverTypeId(), stid);
            }
        }
        if (applicationType == null) {
            s = clientAppType.getCode();
            if (OAString.isEmpty(s)) {
                return null;
            }
            applicationType = new ApplicationType();
            applicationType.setCode(s);
            applicationType.setServerTypeId(clientAppType.getServerTypeId());
            ModelDelegate.getApplicationTypes().add(applicationType);            
        }
            
        //OAFinder<Server, Application> finder = new OAFinder<Server, Application>(ServerPP.applications().pp);
        Application application = server.getApplications().find(ApplicationPP.applicationType().pp, applicationType);
        
        if (application == null) {
            application = new Application();
            application.setApplicationType(applicationType);
            server.getApplications().add(application);
        }
        
        // custom: wait for the autoCreate to be done on the server.
        MRADClient mradClient = null;
        for (int i=0; mradClient == null && i < 10; i++) {
            mradClient = mradServer.getMRADClients().find(MRADClientPP.application().pp, application);
            if (mradClient != null) break;
            try {
                Thread.sleep(20);
            }
            catch (Exception e) {}
        }
        if (mradClient == null) {
            mradClient = new MRADClient();
            mradClient.setApplication(application);
            
            mradServer.getMRADClients().add(mradClient);
        }
        return mradClient;
    }
    
    public static boolean update(MRADClient mradClient, test.xice.tsac.mrad.model.Application clientApplication) {
        if (mradClient == null || clientApplication == null) return false;
        
        test.xice.tsac.mrad.model.Server clientServer = clientApplication.getServer();
        if (clientServer == null) return false;

        mradClient.setIpAddress(clientServer.getIpAddress());
        mradClient.setHostName(clientServer.getHostName());
        mradClient.setName(clientServer.getName());

        mradClient.setOsArch(clientServer.getOsArch());
        mradClient.setOsName(clientServer.getOsName());
        mradClient.setOsVersion(clientServer.getOsVersion());
        
        mradClient.setTotalMemory(clientApplication.getTotalMemory());
        mradClient.setFreeMemory(clientApplication.getFreeMemory());
        
        mradClient.setJavaVendor(clientApplication.getJavaVendor());
        mradClient.setJavaVersion(clientApplication.getJavaVersion());
        
        Application application = mradClient.getApplication();
        if (application == null) return false;
        
        ApplicationType applicationType = application.getApplicationType();
        if (applicationType == null) return false;
        
        
        Server server = application.getServer();
        if (server == null) return false;
        server.setIpAddress(clientServer.getIpAddress());
        server.setHostName(clientServer.getHostName());

        
        
        test.xice.tsac.mrad.model.ApplicationType clientApplicationType = clientApplication.getApplicationType();
        if (clientApplicationType == null) return false;
        
        mradClient.setStartScript(clientApplicationType.getStartCommand());
        mradClient.setStopScript(clientApplicationType.getStopCommand());
        mradClient.setSnapshotStartScript(clientApplicationType.getSnapshotStartCommand());
        mradClient.setVersion(clientApplication.getVersion());
        mradClient.setDirectory(clientApplicationType.getDirectory());
        
        long ts = clientApplication.getStartTime();
        if (ts > 0) mradClient.setStarted(new OADateTime(ts));
        ts = clientApplication.getReadyTime();
        if (ts > 0) mradClient.setReady(new OADateTime(ts));
        
        
        if (OAString.isEmpty(applicationType.getStartCommand())) {
            applicationType.setStartCommand(clientApplicationType.getStartCommand());
        }
        if (OAString.isEmpty(applicationType.getStopCommand())) {
            applicationType.setStartCommand(clientApplicationType.getStopCommand());
        }
        if (OAString.isEmpty(applicationType.getSnapshotStartCommand())) {
            applicationType.setStartCommand(clientApplicationType.getSnapshotStartCommand());
        }
        if (OAString.isEmpty(applicationType.getDirectory())) {
            applicationType.setDirectory(clientApplicationType.getDirectory());
        }
        if (applicationType.getServerTypeId() == 0) { 
            applicationType.setServerTypeId(clientApplicationType.getServerTypeId());
        }
        mradClient.setApplicationTypeCode(clientApplicationType.getCode());
        
        
        mradClient.setServerTypeId(applicationType.getServerTypeId());
        application.setTradingSystemId(clientApplication.getTradingSystemId());
        application.setInstanceNumber(clientApplication.getInstanceNumber());
        
        mradClient.setVersion(clientApplication.getVersion());
        
        
        MRADServer mradServer = mradClient.getMRADServer();
        if (mradServer == null) return false;
        final Environment env = mradServer.getEnvironment();
        if (env == null) return false;

        test.xice.tsac.mrad.model.ApplicationStatus clientAppStatus = clientApplication.getApplicationStatus();
        ApplicationStatus appStatus = ApplicationStatusDelegate.getApplicationStatus(clientAppStatus);  
        application.setApplicationStatus(appStatus);
        
        test.xice.tsac.mrad.model.SiloType clientSiloType = clientServer.getSiloType();
        final SiloType siloType = SiloTypeDelegate.getSiloType(clientSiloType);
        
        final Silo silo = SiloDelegate.getSilo(env, siloType);
        if (silo == null) return false;
        
        // operating system
        OperatingSystem os = OperatingSystemDelegate.getOperatingSystem(clientServer.getOsName(), true);
        
        if (os != null && !OAString.isEmpty(clientServer.getOsVersion())) {
            OSVersion osVersion = os.getOSVersions().find(OSVersionPP.name(), clientServer.getOsVersion());
            if (osVersion == null) {
                osVersion = new OSVersion();
                osVersion.setName(clientServer.getOsVersion());
                osVersion.setOperatingSystem(os);
            }
            server.setOSVersion(osVersion);
        }        
        return true;
    }
}
