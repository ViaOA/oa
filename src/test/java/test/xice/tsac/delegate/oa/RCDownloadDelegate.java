package test.xice.tsac.delegate.oa;

import java.io.File;
import java.util.ArrayList;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.ApplicationPP;
import test.xice.tsac.model.oa.propertypath.ApplicationVersionPP;
import test.xice.tsac.model.oa.propertypath.RCDeployDetailPP;
import test.xice.tsac.model.oa.propertypath.RCDeployPP;
import test.xice.tsac.model.oa.propertypath.RCDownloadDetailPP;
import test.xice.tsac.model.oa.propertypath.SiloPP;

public class RCDownloadDelegate {

    public static void run(final RCDownload download) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(download);
        }
        else {
            // send to server
        }
    }

    protected static boolean _run(final RCDownload rcDownload) throws Exception {
        if (rcDownload == null) throw new RuntimeException("RCDownload can not be null");

        final RCDeploy rcDeploy = rcDownload.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");

        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for download");
        
        final String input = RCDeployDelegate.createHostYaml(rcDeploy, new OAFilter<RCDeployDetail>() {
            @Override
            public boolean isUsed(RCDeployDetail dd) {
                if (dd == null) return false;
                return (RCDeployDetailDelegate.isDownloadNext(dd));
            }
        });
        if (OAString.isEmpty(input)) return false;
        
        
        RCExecute execute = rcDownload.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_download);
            execute.setRCCommand(cmd);
            rcDownload.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (OAString.isEmpty(cmd)) throw new RuntimeException("execute command for download is null");
        if (cmd.getType() != cmd.TYPE_download) throw new RuntimeException("execute command type is not for Downloads");
        
        
        execute.setConfigFileName("/home/tsacadmin/remote_control/config/tsac_deployment.yaml");
        execute.setInput(input);

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        execute.setConsole("Calling RCExecute to perform Download ...");
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCDownload download) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(download);
        }
        else {
            // send to server
        }
    }
    protected static void _process(final RCDownload rcDownload) throws Exception {
        if (rcDownload == null) throw new RuntimeException("download can not be null");

        final RCExecute execute = rcDownload.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        final RCDeploy rcDeploy = rcDownload.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");

        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("Environment can not be null");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null || objs.length == 0) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(env, rcDeploy, rcDownload, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final Environment env, final RCDeploy rcDeploy, final RCDownload download, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("DeployDownloadOutput"))  {
                continue;
            }
            
            RCDownloadDetail detail = _process3(env, rcDeploy, download, oaObj);

            if (msg == null) msg = "";
            else msg += "\n";
            String s = String.format("packageId=%s, packageName=%s, version=%s, error=%s", 
                detail.getPackageId(), detail.getPackageName(), detail.getVersion(), detail.getError()
            );
            msg += (++cnt) + ") " + s;
            
            if (!OAString.isEmpty(detail.getInvalidMessage())) {
                msg += "\n  error: "+detail.getInvalidMessage(); 
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
    
    private static RCDownloadDetail _process3(final Environment env, final RCDeploy rcDeploy, final RCDownload rcDownload, final OAObject oaObj) {
        String error = getValue(oaObj, "error");
        String message = getValue(oaObj, "message");
        String packageName = getValue(oaObj, "package");
        String packageId = getValue(oaObj, "pkg_id");
        String packageFileName = getValue(oaObj, "pkg_file");
        String pomFileName = getValue(oaObj, "pom_file");
        String version = getValue(oaObj, "version");
        String totalTime = getValue(oaObj, "elapsed_time");
        
        OAFinder<RCDownloadDetail,RCDownloadDetail> finder = new OAFinder<RCDownloadDetail, RCDownloadDetail>();
        finder.addEqualFilter(RCDownloadDetailPP.packageId(), packageId);
        finder.addEqualFilter(RCDownloadDetailPP.version(), version);
        
        RCDownloadDetail rcDownloadDetail = finder.findFirst(rcDownload.getRCDownloadDetails());
        if (rcDownloadDetail == null) {
            rcDownloadDetail = new RCDownloadDetail();
            rcDownload.getRCDownloadDetails().add(rcDownloadDetail);
        }
        
        rcDownloadDetail.setError(error);
        rcDownloadDetail.setMessage(message);
        rcDownloadDetail.setPackageId(packageId);
        rcDownloadDetail.setPackageName(packageName);
        rcDownloadDetail.setVersion(version);
        rcDownloadDetail.setPackageFile(packageFileName);
        rcDownloadDetail.setPomFile(pomFileName);
        rcDownloadDetail.setTotalTime(OAConv.toInt(totalTime));
        
        String msgInvalid = "";
        if (OAString.isEmpty(packageId))  {
            if (OAString.isEmpty(packageName)) {
                msgInvalid += "packageId is null. ";
            }
        }
        if (error != null)  {
            msgInvalid += "error from command. ";
        }
        if (version == null)  {
            msgInvalid += "no version found. ";
        }

        // PackageType
        PackageType packageType = null;
        PackageVersion packageVersion = null;
        
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
            // PackageVersion
            if (!OAString.isEmpty(version)) {
                packageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, version);
            }
            if (packageVersion == null) {
                msgInvalid += "packageVersion not found. ";
            }
            rcDownloadDetail.setPackageVersion(packageVersion);
        }

        if (packageVersion != null) {
            // make sure that we requested it
            String pp = RCDownloadDetailPP.rcDownload().rcDeploy().rcDeployDetails().pp;

            OAFinder<RCDownloadDetail, RCDeployDetail> find = new OAFinder<RCDownloadDetail, RCDeployDetail>(pp);

            pp = RCDeployDetailPP.applicationVersion().newPackageVersion().pp;
            find.addEqualFilter(pp, packageVersion);
            
            RCDeployDetail rcdd = find.findFirst(rcDownloadDetail);
            
            if (rcdd == null) {
                msgInvalid += "packageVersion not in request list. ";
            }
        }
        
        rcDownloadDetail.setInvalidMessage(msgInvalid);
        rcDownloadDetail.setSelected(OAString.isEmpty(msgInvalid));
        return rcDownloadDetail;
    }


    public static void load(final RCDownload download) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(download);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCDownload rcDownload) throws Exception {
        if (rcDownload == null) throw new RuntimeException("download can not be null");

        final RCDeploy rcDeploy = rcDownload.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("RCDeploy environment can not be null");
        
        final RCExecute execute = rcDownload.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(env, rcDownload, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final Environment env, final RCDownload rcDownload, final RCExecute execute) {
        for (final RCDownloadDetail rcDownloadDetail : rcDownload.getRCDownloadDetails()) {
            if (!rcDownloadDetail.getSelected()) continue;
            if (!OAString.isEmpty(rcDownloadDetail.getInvalidMessage())) continue;
            
            PackageVersion packageVersion = rcDownloadDetail.getPackageVersion();
            if (packageVersion == null) continue;
            
            String pp = RCDownloadDetailPP.rcDownload().rcDeploy().rcDeployDetails().pp;
            OAFinder<RCDownloadDetail, RCDeployDetail> finder = new OAFinder<RCDownloadDetail, RCDeployDetail>(pp) {
                @Override
                protected void onFound(RCDeployDetail dd) {
                    dd.setRCDownloadDetail(rcDownloadDetail);
                }
            };
            pp = RCDeployDetailPP.applicationVersion().newPackageVersion().pp;
            finder.addEqualFilter(pp, packageVersion);
            finder.find(rcDownloadDetail);
        }
    }

//qqqqq move to unit test    
    public static void main(String[] args) throws Exception {
        String txt = OAFile.readTextFile(new File("testdata/rcdownload.txt"), 8000);

        RCDeploy rcDeploy = new RCDeploy();        
        
        RCDownload rcDownload = new RCDownload();
        RCExecute execute = new RCExecute();
        execute.setOutput(txt);
        
        Environment env = new Environment();
        
    }
}




