package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.ApplicationPP;
import test.xice.tsac.model.oa.propertypath.RCDeployDetailPP;
import test.xice.tsac.model.oa.propertypath.RCVerifyDetailPP;

public class RCVerifyDelegate {

    public static void run(final RCVerify verify) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(verify);
        }
        else {
            // send to server
        }
    }

    protected static boolean _run(final RCVerify rcVerify) throws Exception {
        if (rcVerify == null) throw new RuntimeException("verify can not be null");

        final RCDeploy rcDeploy = rcVerify.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for download");

        final String input = RCDeployDelegate.createHostForVerify(rcDeploy);
        if (OAString.isEmpty(input)) return false;
        
        RCExecute execute = rcVerify.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getInstalledVersions);
            execute.setRCCommand(cmd);
            rcVerify.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (OAString.isEmpty(cmd)) throw new RuntimeException("execute command for download is null");
        if (cmd.getType() != cmd.TYPE_getInstalledVersions) throw new RuntimeException("execute command type is not for getInstalledVersions");
        
        execute.setConfigFileName("/home/tsacadmin/remote_control/config/rc/tsac_temp/hosts.yaml");
        execute.setInput(input);
        
        String s = cmd.getCommandLine();
        execute.setCommandLine(s);
        
        execute.setConsole("Calling RCExecute to perform Verify ...");
        RCExecuteDelegate.runOnRCInstance(execute);
        return true;
    }
    
    public static void process(final RCVerify rcVerify) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(rcVerify);
        }
        else {
            // send to server
        }
    }
    protected static void _process(final RCVerify rcVerify) throws Exception {
        if (rcVerify == null) throw new RuntimeException("RCVerify can not be null");

        final RCExecute execute = rcVerify.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");

        final RCDeploy rcDeploy = rcVerify.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");

        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("Environment can not be null");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null || objs.length == 0) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(rcDeploy, env, rcVerify, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCDeploy rcDeploy, final Environment env, final RCVerify rcVerify, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("VersionOutput"))  {
                continue;
            }
            
            RCVerifyDetail detail = _process3(env, rcDeploy, rcVerify, oaObj);

            if (msg == null) msg = "";
            else msg += "\n";
            String s = String.format("hostName=%s, packageId=%s, version=%s, error=%s", 
                detail.getHostName(), detail.getPackageId(), detail.getVersion(), detail.getError()
            );
            msg += (++cnt) + ") " + s;
            
            if (!OAString.isEmpty(detail.getInvalidMessage())) {
                msg += "\n  error: "+detail.getInvalidMessage(); 
            }
            
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
    
    private static RCVerifyDetail _process3(final Environment env, final RCDeploy rcDeploy, final RCVerify rcVerify, final OAObject oaObj) {
        String hostName = getValue(oaObj, "host");
        String packageId = getValue(oaObj, "package_id");
        String installDate = getValue(oaObj, "install_date");
        String error = getValue(oaObj, "error");
        String version = getValue(oaObj, "version");

        RCVerifyDetail rcVerifyDetail = null;
        {
            OAFinder<RCVerifyDetail,RCVerifyDetail> finder = new OAFinder<RCVerifyDetail, RCVerifyDetail>();
            finder.addEqualFilter(RCVerifyDetailPP.hostName(), hostName);
            finder.addEqualFilter(RCVerifyDetailPP.packageId(), packageId);
            rcVerifyDetail = finder.findFirst(rcVerify.getRCVerifyDetails());
        }
        if (rcVerifyDetail == null) {
            rcVerifyDetail = new RCVerifyDetail();
            rcVerify.getRCVerifyDetails().add(rcVerifyDetail);
        }

        rcVerifyDetail.setHostName(hostName);
        rcVerifyDetail.setPackageId(packageId);
        rcVerifyDetail.setVersion(version);

        if (OAString.isInteger(installDate))  {
            OADateTime dt = new OADateTime(Long.valueOf(installDate));
            rcVerifyDetail.setInstalledDate(new OADate(dt));
        }
        
        String msgInvalid = "";
        if (hostName == null) {
            msgInvalid += "hostName is null. ";
        }
        if (packageId == null)  {
            msgInvalid += "packageId is null. ";
        }
        if (error != null)  {
            msgInvalid += "error from command. ";
        }
        if (version == null)  {
            msgInvalid += "no version found. ";
        }

        // Server
        Server server = ServerDelegate.getServerUsingHostName(env, hostName);
        if (server == null) {
            msgInvalid += "server not found. ";
        }
        
        // PackageType
        PackageType packageType = null;
        PackageVersion packageVersion = null;
        
        if (!OAString.isEmpty(packageId)) {
            packageType = PackageTypeDelegate.getPackageTypeUsingCode(packageId, false);
        
            if (packageType == null) {
                msgInvalid += "packageType not found. ";
            }
            else {
                // PackageVersion
                if (!OAString.isEmpty(version)) {
                    packageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, version);
                    if (packageVersion == null) {
                        // could be snapshot, will store
                    }
                }
                else {
                    msgInvalid += "packageVersion not found. ";
                }
            }
        }
        
        
        // make sure that the server has an application that is using the PackageType
        if (server != null && packageType != null) {
            OAFinder<Application, Application> finder = new OAFinder<Application, Application>();
            finder.addEqualFilter(ApplicationPP.applicationVersions().packageType().pp, packageType);
            Application application = finder.findFirst(server.getApplications());
            if (application == null) {
                msgInvalid += "application not found for this server. ";
            }
        }        

        rcVerifyDetail.setPackageType(packageType);
        rcVerifyDetail.setPackageVersion(packageVersion);
        
        
        // update RCDeployDetail
        OAFinder<RCDeployDetail, RCDeployDetail> finder = new OAFinder<RCDeployDetail, RCDeployDetail>();
        
        String pp = RCDeployDetailPP.applicationVersion().application().server().pp;
        finder.addEqualFilter(pp, server);
        
        pp = RCDeployDetailPP.applicationVersion().packageType().pp;
        finder.addEqualFilter(pp, packageType);
        
        RCDeployDetail deployDetail = finder.findFirst(rcDeploy.getRCDeployDetails());
        if (deployDetail == null) {
            msgInvalid += "deployDetail not found. ";
        }
        else {
            rcVerifyDetail.setRCDeployDetail(deployDetail);
            deployDetail.getApplicationVersion().setCurrentVersion(version);            
            deployDetail.getApplicationVersion().setCurrentPackageVersion(packageVersion);
            
            if (OAString.isEmpty(msgInvalid) && packageVersion != null) {
                if (deployDetail.getApplicationVersion().getNewPackageVersion() == packageVersion) {
                    // deployDetail.getApplicationVersion().setNewPackageVersion(null);
                }
            }
        }
        
        rcVerifyDetail.setInvalidMessage(msgInvalid);
        return rcVerifyDetail;
    }
}
