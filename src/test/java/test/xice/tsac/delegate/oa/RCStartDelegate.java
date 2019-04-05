package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.*;

public class RCStartDelegate {

    public static void run(final RCStart rcStart) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(rcStart);
        }
        else {
            // send to server
        }
    }

    protected static boolean _run(final RCStart rcStart) throws Exception {
        if (rcStart == null) throw new RuntimeException("install can not be null");

        final RCDeploy rcDeploy = rcStart.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");
        
        final String input = RCDeployDelegate.createHostYaml(rcDeploy, new OAFilter<RCDeployDetail>() {
            @Override
            public boolean isUsed(RCDeployDetail dd) {
                if (dd == null) return false;
                return (RCDeployDetailDelegate.isStartNext(dd));
            }
        });
        if (OAString.isEmpty(input)) return false;
        
        RCExecute execute = rcStart.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_start);
            execute.setRCCommand(cmd);
            rcStart.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_start) throw new RuntimeException("execute command type is not for Start");
        
        Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for install");

        execute.setConfigFileName("/home/tsacadmin/remote_control/config/tsac_deployment.yaml");
        execute.setInput(input);
        
        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        
        String apps = RCDeployDelegate.createHostAppList(rcDeploy);
        s = OAString.convert(s, "$APPS", apps);
        execute.setCommandLine(s);
        
        execute.setConsole("Calling RCExecute to perform Start ...");
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCStart rcStart) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(rcStart);
        }
        else {
            // send to server
        }
    }
    protected static void _process(final RCStart rcStart) throws Exception {
        if (rcStart == null) throw new RuntimeException("install can not be null");

        final RCExecute execute = rcStart.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        final RCDeploy rcDeploy = rcStart.getRCDeploy();
        if (rcDeploy == null) throw new RuntimeException("RCDeploy can not be null");

        final Environment env = rcDeploy.getEnvironment();
        if (env == null) throw new RuntimeException("Environment can not be null");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null || objs.length == 0) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(env, rcDeploy, rcStart, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final Environment env, final RCDeploy rcDeploy, final RCStart rcStart, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("AdminStartOutput")) {
                continue;
            }
            
            if (msg == null) msg = "";
            else msg += "\n";
            
            RCStartDetail detail = _process3(env, rcDeploy, rcStart, oaObj);

            String s = String.format("host=%s, application=%s, error=%s", 
                detail.getHost(), detail.getService(), detail.getError()
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
    
    private static RCStartDetail _process3(final Environment env, final RCDeploy rcDeploy, final RCStart rcStart, final OAObject oaObj) {
        /*        
        AdminStartOutput
        {
          "elapsed_time": 13982, 
          "error": null, 
          "host": "ll-lt-gsmr-01", 
          "result": "started", 
          "service": "gsmr", 
          "start_time": 1425905804701, 
          "start_time": 1425905818683
        }
        */

        String elapsedTimeMs = getValue(oaObj, "elapsed_time");
        String error = getValue(oaObj, "error");
        String hostName = getValue(oaObj, "host");
        String result = getValue(oaObj, "result");
        String service = getValue(oaObj, "service");
        String startTime = getValue(oaObj, "start_time");
        String stopTime = getValue(oaObj, "stop_time");

        RCStartDetail rcStartDetail = null;
        {
            OAFinder<RCStartDetail,RCStartDetail> finder = new OAFinder<RCStartDetail, RCStartDetail>();
            finder.addEqualFilter(RCStartDetailPP.host(), hostName);
            finder.addEqualFilter(RCStartDetailPP.service(), service);
            
            rcStartDetail = finder.findFirst(rcStart.getRCStartDetails());
        }
        
        if (rcStartDetail == null) {
            rcStartDetail = new RCStartDetail();
            rcStart.getRCStartDetails().add(rcStartDetail);
        }
        
        rcStartDetail.setError(error);
        rcStartDetail.setHost(hostName);
        rcStartDetail.setResult(result);
        rcStartDetail.setService(service);
        rcStartDetail.setTotalTimeMs(OAConv.toInt(elapsedTimeMs));

        if (startTime != null) {
            OADateTime dt = new OADateTime(OAConv.toLong(startTime));
            rcStartDetail.setStarted(dt);
        }
        
        if (stopTime != null) {
            OADateTime dt = new OADateTime(OAConv.toLong(stopTime));
            rcStartDetail.setStopped(dt);
        }        
        
        String msgInvalid = "";
        if (!OAString.isEmpty(error))  {
            msgInvalid += "error from command. ";
        }
        if (OAString.isEmpty(hostName)) {
            msgInvalid += "hostname is null. ";
        }
        if (OAString.isEmpty(service))  {
            msgInvalid += "service is null. ";
        }

        RCDeployDetail deployDetail = null;
        if (OAString.isEmpty(msgInvalid)) {
            Server server = ServerDelegate.getServerUsingHostName(env, hostName);
            if (server == null) {
                msgInvalid += "host server not found. ";
            }
         
            ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(service, false);
            if (appType == null) {
                msgInvalid += "applicationType not found for service "+service+". ";
            }

            if (OAString.isEmpty(msgInvalid)) {
                // find RCDeplyDetail
                OAFinder<RCDeployDetail, RCDeployDetail> finder = new OAFinder<RCDeployDetail, RCDeployDetail>();

                String pp = RCDeployDetailPP.applicationVersion().application().server().hostName();
                finder.addEqualFilter(pp, hostName);
                
                pp = RCDeployDetailPP.applicationVersion().application().applicationType().code();
                finder.addEqualFilter(pp, service);
                
                deployDetail = finder.findFirst(rcDeploy.getRCDeployDetails());
                if (deployDetail == null) {
                    msgInvalid += "deployDetail not found. ";
                }
                else {
                    rcStartDetail.setRCDeployDetail(deployDetail);
                }
            }
        }
        
        rcStartDetail.setInvalidMessage(msgInvalid);
        return rcStartDetail;
    }

}
