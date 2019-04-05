package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;

public class RCServiceListDelegate {

    public static void run(final RCServiceList serviceList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(serviceList);
        }
        else {
            // send to server
        }
    }

    private static boolean _run(final RCServiceList serviceList) throws Exception {
        if (serviceList == null) throw new RuntimeException("serviceList can not be null");

        if (serviceList.getRCServiceListDetails().getSize() > 0) throw new RuntimeException("already processed");
        
        RCExecute execute = serviceList.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getServiceList);
            execute.setRCCommand(cmd);
            serviceList.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_getServiceList) throw new RuntimeException("execute command type is not for getServiceLists");
        
        
        execute.setInput(null);

        String s = cmd.getCommandLine();
        execute.setCommandLine(s);
        
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCServiceList serviceList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(serviceList);
        }
        else {
            // send to server
        }
    }
    private static void _process(final RCServiceList serviceList) throws Exception {
        if (serviceList == null) throw new RuntimeException("serviceList can not be null");

        final RCExecute execute = serviceList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getYamlObjects(execute);
        if (objs == null) {
            execute.setProcessingOutput("no YAML objects returned");
        }
        else {
            _process2(serviceList, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCServiceList serviceList, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;

        serviceList.getRCServiceListDetails().deleteAll();
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            if (msg == null) msg = "";
            else msg += "\n";
            
            RCServiceListDetail detail = new RCServiceListDetail();
            serviceList.getRCServiceListDetails().add(detail);
            
            String s = _process3(oaObj, detail);
            
            if (detail.getInvalidMessage() != null) {
                s += "\n  error: "+detail.getInvalidMessage(); 
            }
            else detail.setSelected(true);
            msg += (++cnt) + ") " + s;
        }
        if (msg == null) msg = "";
        else msg += "\n";;
        msg += "Total records read = " + cnt;
        execute.setProcessingOutput(msg);
    }
    private static String _process3(final OAObject oaObj, final RCServiceListDetail detail) {
        String name = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name1");
        String login = (String) OAObjectPropertyDelegate.getProperty(oaObj, "login"); 
        String packages = (String) OAObjectPropertyDelegate.getProperty(oaObj, "packages");
        String type = (String) OAObjectPropertyDelegate.getProperty(oaObj, "type");
        String baseDir = (String) OAObjectPropertyDelegate.getProperty(oaObj, "base_dir");
        String startCommand = (String) OAObjectPropertyDelegate.getProperty(oaObj, "start_command");
        String stopCommand = (String) OAObjectPropertyDelegate.getProperty(oaObj, "stop_command");
        String healthPort = (String) OAObjectPropertyDelegate.getProperty(oaObj, "health_port");

        String msg = String.format(" name=%s, packages=%s", 
             name, packages
        );

        detail.setName(name);
        detail.setLogin(login);
        detail.setPackages(packages);
        detail.setType(type);
        detail.setBaseDirectory(baseDir);
        detail.setStartCommand(startCommand);
        detail.setStopCommand(stopCommand);
        if (OAString.isNumber(healthPort)) detail.setHealthPort(OAConv.toInt(healthPort));
        
        ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(name, false);
        
        String msgInvalid = "";
        if (appType == null)  {
            msgInvalid += "application type not found. ";
        }
        
        String s = packages;
        s = OAString.convert(s, "[", null);
        s = OAString.convert(s, "]", null);
        s = OAString.convert(s, ",", null);
        s = OAString.trim(s);
        for (int i=0; ;i++) {
            String s2 = OAString.field(s, ' ', i+1);
            if (s2 == null) break;
            
            PackageType packageType = PackageTypeDelegate.getPackageTypeUsingCode(s2, false);
            if (packageType == null) {
                msgInvalid += "package type "+s2+" not found. ";
            }
        }
        
        detail.setInvalidMessage(msgInvalid);
        detail.setSelected(OAString.isEmpty(msgInvalid));

        return msg;
    }

    public static void load(final RCServiceList serviceList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(serviceList);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCServiceList serviceList) throws Exception {
        if (serviceList == null) throw new RuntimeException("serviceList can not be null");

        final RCExecute execute = serviceList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(serviceList, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final RCServiceList serviceList, final RCExecute execute) {
        for (RCServiceListDetail detail : serviceList.getRCServiceListDetails()) {
            if (!detail.getSelected()) continue;
            // if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            
            ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(detail.getName(), true);
            if (appType == null) return;

            if (OAString.isEmpty(appType.getStartCommand())) appType.setStartCommand(detail.getStartCommand()); 
            if (OAString.isEmpty(appType.getStopCommand())) appType.setStopCommand(detail.getStopCommand()); 
            if (OAString.isEmpty(appType.getDirectory())) appType.setDirectory(detail.getBaseDirectory()); 
            
            if (appType.getF5Port() < 1) appType.setF5Port(detail.getHealthPort());

            String s = detail.getPackages();
            s = OAString.convert(s, "[", null);
            s = OAString.convert(s, "]", null);
            s = OAString.convert(s, ",", null);
            s = OAString.trim(s);
            for (int i=0; ;i++) {
                String s2 = OAString.field(s, ' ', i+1);
                if (s2 == null) break;
                
                PackageType packageType = PackageTypeDelegate.getPackageTypeUsingCode(s2, false);
                if (packageType != null) {
                    appType.getPackageTypes().add(packageType);
                }
            }
            detail.setLoaded(true);
        }
    }
}
