package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.*;

public class RCInstallDelegate {

    public static void run(final RCInstall install) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(install);
        }
        else {
            // send to server
        }
    }

    protected static boolean _run(final RCInstall rcInstall) throws Exception {
        if (rcInstall == null) throw new RuntimeException("install can not be null");

        final RCDeploy rcDeploy = rcInstall.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for download");

        final String input = RCDeployDelegate.createHostYaml(rcDeploy, new OAFilter<RCDeployDetail>() {
            @Override
            public boolean isUsed(RCDeployDetail dd) {
                if (dd == null) return false;
                return (RCDeployDetailDelegate.isInstallNext(dd));
            }
        });
        if (OAString.isEmpty(input)) return false;
        
        RCExecute execute = rcInstall.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_install);
            execute.setRCCommand(cmd);
            rcInstall.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (OAString.isEmpty(cmd)) throw new RuntimeException("execute command for download is null");
        if (cmd.getType() != cmd.TYPE_install) throw new RuntimeException("execute command type is not for Installs");
        
        execute.setConfigFileName("/home/tsacadmin/remote_control/config/tsac_deployment.yaml");
        execute.setInput(input);

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        execute.setConsole("Calling RCExecute to perform Install ...");
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }

    public static void process(final RCInstall rcInstall) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(rcInstall);
        }
        else {
            // send to server
        }
    }
    protected static void _process(final RCInstall rcInstall) throws Exception {
        if (rcInstall == null) throw new RuntimeException("install can not be null");

        final RCExecute execute = rcInstall.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        final RCDeploy rcDeploy = rcInstall.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("Environment can not be null");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null || objs.length == 0) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(env, rcDeploy, rcInstall, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final Environment env, final RCDeploy rcDeploy, final RCInstall rcInstall, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("DeployInstallOutput"))  {
                continue;
            }
            
            RCInstallDetail detail = _process3(env, rcDeploy, rcInstall, oaObj);

            if (msg == null) msg = "";
            else msg += "\n";
            String s = String.format("host=%s, packageId=%s, packageName=%s, new version=%s, error=%s", 
                detail.getDestHost(), detail.getPackageId(), detail.getPackageName(), detail.getAfterVersion(), detail.getError()
            );
            msg += (++cnt) + ") " + s;
            
            if (!OAString.isEmpty(detail.getInvalidMessage())) {
                s += "\n  error: "+detail.getInvalidMessage(); 
            }
            else detail.setSelected(true);
        }
        if (msg == null) msg = "";
        else msg += "\n";;
        msg += "Total records read = " + cnt;
        execute.setProcessingOutput(msg);
    }

    private static String getValue(OAObject oaObj, String propName) {
        String prop = (String) OAObjectPropertyDelegate.getProperty(oaObj, propName);
        if ("null".equals(prop)) prop = null;
        return prop;
    }
    
    private static RCInstallDetail _process3(final Environment env, final RCDeploy rcDeploy, final RCInstall rcInstall, final OAObject oaObj) {
        String afterVersion = getValue(oaObj, "after_version");
        String beforeVersion = getValue(oaObj, "before_version");
        String hostName = getValue(oaObj, "dest_host");
        String totalTime = getValue(oaObj, "elapsed_time");
        String error = getValue(oaObj, "error");
        String message = getValue(oaObj, "message");
        String packageName = getValue(oaObj, "package");
        String packageId = getValue(oaObj, "pkg_id");

        
        RCInstallDetail rcInstallDetail = null;
        {
            OAFinder<RCInstallDetail,RCInstallDetail> finder = new OAFinder<RCInstallDetail, RCInstallDetail>();
            finder.addEqualFilter(RCInstallDetailPP.destHost(), hostName);
            finder.addEqualFilter(RCInstallDetailPP.packageId(), packageId);
            finder.addEqualFilter(RCInstallDetailPP.beforeVersion(), beforeVersion);
            
            rcInstallDetail = finder.findFirst(rcInstall.getRCInstallDetails());
        }
        if (rcInstallDetail == null) {
            rcInstallDetail = new RCInstallDetail();
            rcInstall.getRCInstallDetails().add(rcInstallDetail);
        }

        rcInstallDetail.setBeforeVersion(beforeVersion);
        rcInstallDetail.setAfterVersion(afterVersion);
        rcInstallDetail.setDestHost(hostName);
        rcInstallDetail.setTotalTime(OAConv.toInt(totalTime));
        rcInstallDetail.setError(error);
        rcInstallDetail.setMessage(message);
        rcInstallDetail.setPackageName(packageName);
        rcInstallDetail.setPackageId(packageId);
        
        String msgInvalid = "";
        if (packageId == null) {
            if (packageName == null) {
                msgInvalid += "packageId is null. ";
            }
        }
        if (error != null)  {
            msgInvalid += "error from command. ";
        }
        if (afterVersion == null)  {
            msgInvalid += "no after version found. ";
        }

        // PackageType
        PackageType packageType = null;
        PackageVersion afterPackageVersion = null;
        PackageVersion beforePackageVersion = null;
        
        if (!OAString.isEmpty(packageId)) {
            packageType = PackageTypeDelegate.getPackageTypeUsingCode(packageId, false);
        }
        else if (!OAString.isEmpty(packageName)) {
            packageType = PackageTypeDelegate.getPackageTypeUsingPackageName(packageName, false);
        }
        if (packageType == null) {
            msgInvalid += "packageType not found. ";
        }
        else {
            // afterPackageVersion
            if (!OAString.isEmpty(afterVersion)) {
                afterPackageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, afterVersion);
            }
            if (afterPackageVersion == null) {
                msgInvalid += "packageVersion not found. ";
            }
            rcInstallDetail.setAfterPackageVersion(afterPackageVersion);
            
            // beforePackageVersion
            if (!OAString.isEmpty(beforeVersion)) {
                beforePackageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, beforeVersion);
            }
            rcInstallDetail.setBeforePackageVersion(beforePackageVersion);
        }
        
        
        // dest server
        Server server = ServerDelegate.getServerUsingHostName(env, hostName);
        if (server == null) {
            msgInvalid += "host server not found. ";
        }
        rcInstallDetail.setServer(server);
        
        if (rcInstallDetail.getAfterPackageVersion() != null) {
            // make sure that we requested it
            String pp = RCInstallDetailPP.rcInstall().rcDeploy().rcDeployDetails().pp;

            OAFinder<RCInstallDetail, RCDeployDetail> finder = new OAFinder<RCInstallDetail, RCDeployDetail>(pp);
            
            pp = RCDeployDetailPP.applicationVersion().application().server().pp;
            finder.addEqualFilter(pp, server);
            
            pp = RCDeployDetailPP.applicationVersion().packageType().pp;
            finder.addEqualFilter(pp, packageType);
            
            RCDeployDetail deployDetail = finder.findFirst(rcInstallDetail);
            if (deployDetail == null) {
                msgInvalid += "deployDetail not found. ";
            }
        }
        
        rcInstallDetail.setInvalidMessage(msgInvalid);
        rcInstallDetail.setSelected(OAString.isEmpty(msgInvalid.length()));
        return rcInstallDetail;
    }

    public static void load(final RCInstall install) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(install);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCInstall rcInstall) throws Exception {
        if (rcInstall == null) throw new RuntimeException("install can not be null");

        final RCDeploy rcDeploy = rcInstall.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("RCDeploy environment can not be null");
        
        final RCExecute execute = rcInstall.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(env, rcInstall, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final Environment env, final RCInstall rcInstall, final RCExecute execute) {
        for (final RCInstallDetail detail : rcInstall.getRCInstallDetails()) {
            if (!detail.getSelected()) continue;
            if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            PackageType packageType = null;
            if (!OAString.isEmpty(detail.getPackageId())) {
                packageType = PackageTypeDelegate.getPackageTypeUsingCode(detail.getPackageId(), false);
            }
            if (packageType == null && !OAString.isEmpty(detail.getPackageName())) {
                packageType = PackageTypeDelegate.getPackageTypeUsingPackageName(detail.getPackageName(), false);
            }
            if (packageType == null) continue;
            
            Server server = detail.getServer();
            if (server == null) continue;
            
            String pp = RCInstallDetailPP.rcInstall().rcDeploy().rcDeployDetails().pp;
            OAFinder<RCInstallDetail, RCDeployDetail> finder = new OAFinder<RCInstallDetail, RCDeployDetail>(pp);
            
            pp = RCDeployDetailPP.applicationVersion().application().server().pp;
            finder.addEqualFilter(pp, server);

            pp = RCDeployDetailPP.applicationVersion().packageType().pp;
            finder.addEqualFilter(pp, packageType);

            RCDeployDetail rdd = finder.findFirst(detail);
            if (rdd != null) {
                rdd.setRCInstallDetail(detail);
            }
        }
    }
}
