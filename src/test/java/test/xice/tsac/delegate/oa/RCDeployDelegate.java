package test.xice.tsac.delegate.oa;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubCopy;
import com.viaoa.object.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.*;

public class RCDeployDelegate {

    public static void initialize(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _initialize(rcDeploy);
        }
        else {
            // send to server
        }
    }

    private static void _initialize(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        rcDeploy.getRCDeployDetails().deleteAll();
        if (rcDeploy.getRCDownload() != null) rcDeploy.getRCDownload().delete();
        
        RCStage rs = rcDeploy.getRCStage();
        if (rs != null) rs.delete();
        
        RCStop rcstop = rcDeploy.getRCStop();
        if (rcstop != null) rcstop.delete();
        
        RCInstall ri = rcDeploy.getRCInstall();
        if (ri != null) ri.delete();
        
        RCVerify rv = rcDeploy.getRCVerify();
        if (rv != null) rv.delete();
        
        RCStart rcstart = rcDeploy.getRCStart();
        if (rcstart != null) rcstart.delete();

        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for deploy");

        
        // find servers
        String pp = RCDeployPP.environment().silos().servers().applications().applicationVersions().pp;
        OAFinder<RCDeploy, ApplicationVersion> finder = new OAFinder<RCDeploy, ApplicationVersion>(pp) {
            @Override
            protected void onFound(ApplicationVersion obj) {
                RCDeployDetail detail = new RCDeployDetail();
                detail.setApplicationVersion(obj);
                detail.setRCDeploy(rcDeploy);
                
                boolean b = obj.getNewPackageVersion() != null && obj.getCurrentPackageVersion() != obj.getNewPackageVersion();
                detail.setSelected(b);
                
                Application app = obj.getApplication();
                Server server = app.getServer(); 
                rcDeploy.setConsole("Server "+server.getHostName()+", Application "+app.getApplicationType().getCode());
            }
        };
        finder.find(rcDeploy);
    }

    public static void download(final RCDeploy rcDeploy) throws Exception {

        if (rcDeploy == null) return;
        
        if (rcDeploy.getRCDeployDetails().find(RCDeployDetailPP.selected(), true) == null) {
            return;
        }
        
        if (OASyncDelegate.isServer()) {
            _download(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _download(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCDownload download = rcDeploy.getRCDownload();
        if (download == null) {
            download = new RCDownload();
            rcDeploy.setRCDownload(download);
        }
        if (RCDownloadDelegate._run(download)) {
            RCDownloadDelegate._process(download);
            RCDownloadDelegate.load(download);
        }
    }
    
    public static void stage(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _stage(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _stage(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCStage stage = rcDeploy.getRCStage();
        if (stage == null) {
            stage = new RCStage();
            rcDeploy.setRCStage(stage);
        }
        if (RCStageDelegate._run(stage)) {
            RCStageDelegate._process(stage);
            RCStageDelegate.load(stage);
        }
    }
    public static void install(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _install(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _install(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCInstall install = rcDeploy.getRCInstall();
        if (install == null) {
            install = new RCInstall();
            rcDeploy.setRCInstall(install);
        }
        
        if (RCInstallDelegate._run(install)) {
            RCInstallDelegate._process(install);
            RCInstallDelegate.load(install);
        }
    }

    public static void verify(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _verify(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _verify(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCVerify verify = rcDeploy.getRCVerify();
        if (verify == null) {
            verify = new RCVerify();
            rcDeploy.setRCVerify(verify);
        }
        
        if (RCVerifyDelegate._run(verify)) {
            RCVerifyDelegate._process(verify);
        }
    }
    
    
    public static void start(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _start(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _start(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCStart start = rcDeploy.getRCStart();
        if (start == null) {
            start = new RCStart();
            rcDeploy.setRCStart(start);
        }
        
        if (RCStartDelegate._run(start)) {
            RCStartDelegate._process(start);
        }
    }

    public static void stop(final RCDeploy rcDeploy) throws Exception {
        if (OASyncDelegate.isServer()) {
            _stop(rcDeploy);
        }
        else {
            // send to server
        }
    }
    private static void _stop(final RCDeploy rcDeploy) throws Exception {
        if (rcDeploy == null) throw new RuntimeException("deploy can not be null");

        RCStop stop = rcDeploy.getRCStop();
        if (stop == null) {
            stop = new RCStop();
            rcDeploy.setRCStop(stop);
        }
        
        if (RCStopDelegate._run(stop)) {
            RCStopDelegate._process(stop);
        }
    }

    public static String createHostYaml(RCDeploy deploy, OAFilter<RCDeployDetail> filter) {
        String text = null;
        
        Hub<RCDeployDetail> hub = new Hub<RCDeployDetail>(RCDeployDetail.class);
        HubCopy<RCDeployDetail> hubCopy = new HubCopy<RCDeployDetail>(deploy.getRCDeployDetails(), hub, false);
        hub.sort(
            RCDeployDetailPP.applicationVersion().packageType().pp,
            RCDeployDetailPP.applicationVersion().newPackageVersion().version()
        );
        
        String lastPackageTypeCode = null;
        String lastPackageVersion = null;
        String lastAppType = null;
        for (RCDeployDetail detail : hub) {
            if (!detail.getSelected()) continue;
            if (filter != null && !filter.isUsed(detail)) continue;

            ApplicationVersion appVersion = detail.getApplicationVersion();
            if (appVersion == null) continue;
            
            Application app = appVersion.getApplication();
            if (app == null) continue;

            ApplicationType appType = app.getApplicationType();
            if (appType == null) continue;
            
            PackageVersion packageVersion = appVersion.getNewPackageVersion();
            if (packageVersion == null) continue;
            
            PackageType packageType = packageVersion.getPackageType();
            if (packageType == null) continue;
            
            Server server = app.getServer();
            if (server == null) continue;
             
            if (text == null) text = "";
            else text += "\n";
            
            String packageTypeCode = packageType.getCode();
            
            if (!OAString.isEqual(lastPackageTypeCode, packageTypeCode)) {
                text +=  packageTypeCode + ":\n";
                lastPackageTypeCode = packageTypeCode;
                lastPackageVersion = null;
                lastAppType = null;
            }

            String version = packageVersion.getVersion();
            if (!OAString.isEqual(lastPackageVersion, version)) {
                text += "  '" + version + "':\n";
                lastPackageVersion = version;
                lastAppType = null;
            }
            
            String at = appType.getCode();
            if (at == null) at = "";
            at = at.toLowerCase();
            if (!OAString.isEqual(lastAppType, at)) {
                text += "    " + at + ":\n";
                lastAppType = at;
            }
            
            text += "      - " + server.getHostName();
        }
        
        hubCopy.close();
        return text;
    }

    public static String createHostForVerify(RCDeploy deploy) {
        String text = "";
        
        Hub<RCDeployDetail> hub = new Hub<RCDeployDetail>(RCDeployDetail.class);
        HubCopy<RCDeployDetail> hubCopy = new HubCopy<RCDeployDetail>(deploy.getRCDeployDetails(), hub, false);
        hub.sort(RCDeployDetailPP.applicationVersion().application().server().pp);
        
        String lastHostName = null;
        for (RCDeployDetail detail : hub) {
            if (!detail.getSelected()) continue;

            ApplicationVersion appVersion = detail.getApplicationVersion();
            if (appVersion == null) continue;
            
            Application app = appVersion.getApplication();
            if (app == null) continue;

            ApplicationType appType = app.getApplicationType();
            if (appType == null) continue;
            
            PackageVersion packageVersion = appVersion.getNewPackageVersion();
            if (packageVersion == null) continue;
            
            PackageType packageType = packageVersion.getPackageType();
            if (packageType == null) continue;
            
            Server server = app.getServer();
            if (server == null) continue;
            
            String hostName = server.getHostName();
            if (!OAString.isEqual(lastHostName, hostName)) {
                if (lastHostName != null) text += "]\n";
                text += hostName + ": [";
                lastHostName = hostName;
            }
            else {
                text += ", ";            
            }
            text += OAString.toString(appType.getCode()).toLowerCase();
        }
        if (lastHostName != null) text += "]\n";
        
        hubCopy.close();
        return text;
    }
    
    public static String createHostAppList(RCDeploy deploy) {
        String text = "";
        
        Hub<RCDeployDetail> hub = new Hub<RCDeployDetail>(RCDeployDetail.class);
        HubCopy<RCDeployDetail> hubCopy = new HubCopy<RCDeployDetail>(deploy.getRCDeployDetails(), hub, false);
        hub.sort(RCDeployDetailPP.applicationVersion().application().applicationType().pp);
        
        String lastAppTypeCode = null;
        
        for (RCDeployDetail detail : hub) {
            if (!detail.getSelected()) continue;

            ApplicationVersion appVersion = detail.getApplicationVersion();
            if (appVersion == null) continue;
            
            Application app = appVersion.getApplication();
            if (app == null) continue;

            ApplicationType appType = app.getApplicationType();
            if (appType == null) continue;
            
            Server server = app.getServer();
            if (server == null) continue;
            
            String appTypeCode = appType.getCode();

            // gsmr=ll-lt-gsmr-01,ocmr=ll-lt-ocmr-01
            // gsmr=ll-lt-gsmr-02:ll-lt-gsmr-01,ocmr=ll-lt-ocmr-01
            
            if (lastAppTypeCode == null || !lastAppTypeCode.equalsIgnoreCase(appTypeCode)) {
                lastAppTypeCode = appTypeCode;
                if (text.length() > 0) text += ",";
                text += appTypeCode + "=";
            }
            else {
                text += ":";
            }
            text += server.getHostName();
        }
        
        hubCopy.close();
        return text;
    }
    
}
