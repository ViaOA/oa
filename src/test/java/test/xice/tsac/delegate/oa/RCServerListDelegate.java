package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;

public class RCServerListDelegate {

    public static void run(final RCServerList serverList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(serverList);
        }
        else {
            // send to server
        }
    }

    private static boolean _run(final RCServerList serverList) throws Exception {
        if (serverList == null) throw new RuntimeException("serverList can not be null");
        
        Environment env = serverList.getEnvironment();
        if (env == null) throw new RuntimeException("serverList.Environment can not be null");

        if (serverList.getRCServerListDetails().getSize() > 0) throw new RuntimeException("already processed");
        
        RCExecute execute = serverList.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getServerList);
            execute.setRCCommand(cmd);
            serverList.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_getServerList) throw new RuntimeException("execute command type is not for getServerLists");
        
        
        execute.setInput(null);

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCServerList serverList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(serverList);
        }
        else {
            // send to server
        }
    }
    private static void _process(final RCServerList serverList) throws Exception {
        if (serverList == null) throw new RuntimeException("serverList can not be null");

        final RCExecute execute = serverList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getYamlObjects(execute);
        if (objs == null) {
            execute.setProcessingOutput("no YAML objects returned");
        }
        else {
            _process2(serverList, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCServerList serverList, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;

        serverList.getRCServerListDetails().deleteAll();
        
        Environment env = serverList.getEnvironment();
        if (env == null) return;
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            if (msg == null) msg = "";
            else msg += "\n";
            
            RCServerListDetail detail = new RCServerListDetail();
            serverList.getRCServerListDetails().add(detail);
            
            String s = _process3(env, oaObj, detail);
            
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

    private static String _process3(final Environment env, final OAObject oaObj, final RCServerListDetail detail) {
        String hostName = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name1");
        String packages = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name2");

        String msg = String.format(" hostName=%s, packages=%s", 
             hostName, packages
        );

        detail.setHostName(hostName);
        detail.setPackages(packages);
        
        Server server = ServerDelegate.getServerUsingHostName(env, hostName);
        String msgInvalid = "";
        if (server == null)  {
            msgInvalid += "server not found. ";
        }

        String s = packages;
        s = OAString.convert(s, "[", null);
        s = OAString.convert(s, "]", null);
        s = OAString.convert(s, ",", null);
        s = OAString.trim(s);
        for (int i=0; ;i++) {
            String s2 = OAString.field(s, ' ', i+1);
            if (s2 == null) break;
            
            ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(s2, false);
            if (appType == null) {
                msgInvalid += "application type "+s2+" not found. ";
                continue;
            }
            if (server == null) continue;
            Application app = server.getApplications().find(Application.P_ApplicationType, appType);
            if (app == null) {
                msgInvalid += "application "+s2+" not found for server "+hostName+". ";
            }
        }

        detail.setInvalidMessage(msgInvalid);
        detail.setSelected(OAString.isEmpty(msgInvalid));

        return msg;
    }

    public static void load(final RCServerList serverList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(serverList);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCServerList serverList) throws Exception {
        if (serverList == null) throw new RuntimeException("serverList can not be null");

        final RCExecute execute = serverList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");

        _load2(serverList, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final RCServerList serverList, final RCExecute execute) {
        final Environment env = serverList.getEnvironment();
        for (RCServerListDetail detail : serverList.getRCServerListDetails()) {
            if (!detail.getSelected()) continue;

            String hostName = detail.getHostName();
            String packages = detail.getPackages();
            
            Server server = ServerDelegate.getServerUsingHostName(env, hostName);
            String msgInvalid = "";
            if (server == null)  {
                server = new Server();
                server.setHostName(hostName);
            }
            if (server.getSilo() == null) {
                Silo silo = SiloDelegate.getSiloUsingHostName(env, hostName, true);
                server.setSilo(silo);
            }

            
            if (OAString.isEmpty(packages)) continue;
            String s = packages;
            s = OAString.convert(s, "[", null);
            s = OAString.convert(s, "]", null);
            s = OAString.convert(s, ",", null);
            s = OAString.trim(s);
            for (int i=0; ;i++) {
                String s2 = OAString.field(s, ' ', i+1);
                if (s2 == null) break;
                
                ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(s2, false);
                if (appType == null) continue;
                
                Application app = ApplicationDelegate.getApplication(server, appType, 1, true);
            }
            detail.setLoaded(true);
        }
    }
}
