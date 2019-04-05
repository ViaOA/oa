package test.xice.tsac.delegate.oa;

import java.util.ArrayList;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.*;

public class RCStageDelegate {

    public static void run(final RCStage stage) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(stage);
        }
        else {
            // send to server
        }
    }

    protected static boolean _run(final RCStage stage) throws Exception {
        if (stage == null) throw new RuntimeException("stage can not be null");

        final RCDeploy rcDeploy = stage.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        final String input = RCDeployDelegate.createHostYaml(rcDeploy, new OAFilter<RCDeployDetail>() {
            @Override
            public boolean isUsed(RCDeployDetail dd) {
                if (dd == null) return false;
                return (RCDeployDetailDelegate.isStageNext(dd));
            }
        });
        if (OAString.isEmpty(input)) return false;
        
        RCExecute execute = stage.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_stage);
            execute.setRCCommand(cmd);
            stage.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_stage) throw new RuntimeException("execute command type is not for Stage");
        
        
        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for stage");

        execute.setConfigFileName("/home/tsacadmin/remote_control/config/tsac_deployment.yaml");
        execute.setInput(input);

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        execute.setConsole("Calling RCExecute to perform Stage ...");
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCStage stage) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(stage);
        }
        else {
            // send to server
        }
    }
    protected static void _process(final RCStage stage) throws Exception {
        if (stage == null) throw new RuntimeException("stage can not be null");

        final RCExecute execute = stage.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        final RCDeploy rcDeploy = stage.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null || objs.length == 0) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(rcDeploy, stage, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCDeploy rcDeploy, final RCStage stage, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        final Environment env = rcDeploy.getEnvironment();
        if (env == null) {
            execute.setProcessingOutput("invalid: no environment");
            return;
        }
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null) continue;
            if (!objectName.equalsIgnoreCase("DeployStageOutput"))  {
                if (!objectName.equalsIgnoreCase("DeployPropagateOutput"))  {
                    continue;
                }
            }
            
            if (msg == null) msg = "";
            else msg += "\n";

            RCStageDetail detail = _process3(env, rcDeploy, stage, oaObj);
            
            String s = String.format("host=%s, packageId=%s, version=%s, error=%s", 
                detail.getDestHost(), detail.getPackageId(), detail.getVersion(), detail.getError()
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
    
    private static RCStageDetail _process3(final Environment env, final RCDeploy deploy, final RCStage rcStage, final OAObject oaObj) {
        /*        
        DeployStageOutput
        {
          "dest_host": "pdk-st-lxte-03", 
          "elapsed_time": 3251, 
          "error": null, 
          "message": "package staged", 
          "package": "ICEteconfig", 
          "start_time": 1417797323917, 
          "stop_time": 1417797327168, 
          "version": "12.925.7"
        }
        DeployPropagateOutput
        {
          "dest_host": "pdk-st-lxte-01", 
          "elapsed_time": 3821, 
          "error": null, 
          "message": "package propagated", 
          "orig_host": "pdk-st-lxte-03", 
          "package": "ICEteconfig", 
          "start_time": 1417797326900, 
          "stop_time": 1417797330721, 
          "version": "12.925.7"
        }
        */        
        
        String hostName = getValue(oaObj, "dest_host");
        String origHostName = getValue(oaObj, "orig_host");
        String totalTime = getValue(oaObj, "elapsed_time");
        String error = getValue(oaObj, "error");
        String message = getValue(oaObj, "message");
        String packageName = getValue(oaObj, "package");
        String packageId = getValue(oaObj, "package_id");
        String version = getValue(oaObj, "version");

        OAFinder<RCStageDetail,RCStageDetail> finder = new OAFinder<RCStageDetail, RCStageDetail>();
        finder.addEqualFilter(RCStageDetailPP.packageId(), packageId);
        finder.addEqualFilter(RCStageDetailPP.version(), version);
        finder.addEqualFilter(RCStageDetailPP.destHost(), hostName);
        
        RCStageDetail rcStageDetail = finder.findFirst(rcStage.getRCStageDetails());
        if (rcStageDetail == null) {
            rcStageDetail = new RCStageDetail();
            rcStage.getRCStageDetails().add(rcStageDetail);
        }
        
        rcStageDetail.setError(error);
        rcStageDetail.setMessage(message);
        rcStageDetail.setPackageId(packageId);
        rcStageDetail.setPackageName(packageName);
        rcStageDetail.setVersion(version);
        rcStageDetail.setTotalTime(OAConv.toInt(totalTime));
        rcStageDetail.setDestHost(hostName);
        rcStageDetail.setOrigHost(origHostName);

        String msgInvalid = "";
        if (packageId == null) {
            if (packageName == null) {
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
            rcStageDetail.setPackageVersion(packageVersion);
        }
        
        // orig server
        Server server = null;
        if (!OAString.isEmpty(origHostName)) {
            server = ServerDelegate.getServerUsingHostName(env, origHostName);
            if (server == null) {
                msgInvalid += "orig server not found. ";
            }
        }
        rcStageDetail.setOrigServer(server);
        
        // dest application
        server = ServerDelegate.getServerUsingHostName(env, hostName);
        if (server == null) {
            msgInvalid += "host server not found. ";
        }
        rcStageDetail.setServer(server);
        
        
        rcStageDetail.setInvalidMessage(msgInvalid);
        rcStageDetail.setSelected(OAString.isEmpty(msgInvalid.length()));
        return rcStageDetail;
    }

    public static void load(final RCStage stage) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(stage);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCStage rcStage) throws Exception {
        
        if (rcStage == null) throw new RuntimeException("stage can not be null");

        final RCDeploy rcDeploy = rcStage.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");

        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("RCDeploy environment can not be null");
        
        final RCExecute execute = rcStage.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(env, rcStage, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final Environment env, final RCStage stage, final RCExecute execute) {
        for (final RCStageDetail detail : stage.getRCStageDetails()) {
            if (!detail.getSelected()) continue;
            if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            PackageVersion packageVersion = detail.getPackageVersion();
            if (packageVersion == null) continue;
            
            String pp = RCStageDetailPP.rcStage().rcDeploy().rcDeployDetails().pp;
            OAFinder<RCStageDetail, RCDeployDetail> finder = new OAFinder<RCStageDetail, RCDeployDetail>(pp) {
                @Override
                protected void onFound(RCDeployDetail dd) {
                    detail.setRCDeployDetail(dd);
                }
            };
            pp = RCDeployDetailPP.applicationVersion().application().server().hostName();
            finder.addEqualFilter(pp, detail.getDestHost());
            
            pp = RCDeployDetailPP.applicationVersion().newPackageVersion().pp;
            finder.addEqualFilter(pp, packageVersion);
            finder.find(detail);
        }
    }
}
