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
import test.xice.tsac.model.oa.propertypath.ApplicationPP;
import test.xice.tsac.model.oa.propertypath.ApplicationVersionPP;
import test.xice.tsac.model.oa.propertypath.SiloPP;

public class RCInstalledVersionDelegate {

    public static void run(final RCInstalledVersion installedVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(installedVersion);
        }
        else {
            // send to server
        }
    }

    private static boolean _run(final RCInstalledVersion installedVersion) throws Exception {
        if (installedVersion == null) throw new RuntimeException("installedVersion can not be null");

        if (installedVersion.getRCInstalledVersionDetails().getSize() > 0) throw new RuntimeException("already processed");
        
        RCExecute execute = installedVersion.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getInstalledVersions);
            execute.setRCCommand(cmd);
            installedVersion.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_getInstalledVersions) throw new RuntimeException("execute command type is not for getInstalledVersions");
        
        
        Environment env = installedVersion.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for installedVersion");
        
        execute.setConfigFileName("/home/tsacadmin/remote_control/config/rc/tsac_temp/hosts.yaml");
        
        
        OAFinder<Silo, Server> finder = new OAFinder<Silo, Server>(env.getSilos(), SiloPP.servers().pp);
        ArrayList<Server> al = finder.find();

        String text = null;
        for (Server server : al) {
            if (server == null) continue;
            
            String strApps = null;
            for (Application app : server.getApplications()) {
                ApplicationType at = app.getApplicationType();
                if (at == null) continue;
                
                String s = at.getCode();
                if (s == null) continue;
                s = s.toLowerCase();
                
                if (strApps == null) strApps = "";
                else strApps += ", ";
                
                strApps += s;
            }
            if (strApps == null) continue;
            
            if (text == null) text = "";
            else text += "\n";
            
            String s = server.getHostName() + ": [" + strApps + "]";
            text += s;
        }
        execute.setInput(text);

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCInstalledVersion installedVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(installedVersion);
        }
        else {
            // send to server
        }
    }
    private static void _process(final RCInstalledVersion installedVersion) throws Exception {
        if (installedVersion == null) throw new RuntimeException("installedVersion can not be null");

        final RCExecute execute = installedVersion.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(installedVersion, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCInstalledVersion installedVersion, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        final Environment env = installedVersion.getEnvironment();
        if (env == null) {
            execute.setProcessingOutput("invalid: no environment");
            return;
        }
        
        installedVersion.getRCInstalledVersionDetails().deleteAll();
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("VersionOutput"))  {
                continue;
            }
            
            if (msg == null) msg = "";
            else msg += "\n";
            
            RCInstalledVersionDetail detail = new RCInstalledVersionDetail();
            installedVersion.getRCInstalledVersionDetails().add(detail);
            
            String s = _process3(oaObj, env, detail);
            
            if (!OAString.isEmpty(detail.getInvalidMessage())) {
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
    private static String _process3(final OAObject oaObj, final Environment env, final RCInstalledVersionDetail detail) {
        String hostName = (String) OAObjectPropertyDelegate.getProperty(oaObj, "host");
        String packageId = (String) OAObjectPropertyDelegate.getProperty(oaObj, "package_id");
        String installDate = (String) OAObjectPropertyDelegate.getProperty(oaObj, "install_date");
        String error = (String) OAObjectPropertyDelegate.getProperty(oaObj, "error");
        String version = (String) OAObjectPropertyDelegate.getProperty(oaObj, "version");

        String msg = String.format("hostName=%s, package=%s, installDate=%s, version=%s, error=%s", 
            hostName, packageId, installDate, version, error
        );

        if ("null".equals(hostName)) hostName = null;
        if ("null".equals(packageId)) packageId = null;
        if ("null".equals(error)) error = null;
        if ("null".equals(version)) version = null;

        detail.setHostName(hostName);
        detail.setPackageId(packageId);
        detail.setVersion(version);

        if (OAString.isInteger(installDate))  {
            OADateTime dt = new OADateTime(Long.valueOf(installDate));
            detail.setInstalledDate(new OADate(dt));
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
                if (!OAString.isEmpty(version)) packageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, version);
                detail.setPackageVersion(packageVersion);
                if (packageVersion == null) {
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
                msgInvalid += "application not found. ";
            }
        }        
        
        detail.setInvalidMessage(msgInvalid);
        return msg;
    }

    public static void load(final RCInstalledVersion installedVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(installedVersion);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCInstalledVersion installedVersion) throws Exception {
        if (installedVersion == null) throw new RuntimeException("installedVersion can not be null");

        final RCExecute execute = installedVersion.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(installedVersion, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final RCInstalledVersion installedVersion, final RCExecute execute) {
        Environment env = installedVersion.getEnvironment();
        
        for (RCInstalledVersionDetail detail : installedVersion.getRCInstalledVersionDetails()) {
            if (!detail.getSelected()) continue;
            if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            PackageVersion packageVersion = detail.getPackageVersion();
            if (packageVersion == null) continue;
            
            PackageType packageType = packageVersion.getPackageType();
            if (packageType == null) continue;

            Server server = ServerDelegate.getServerUsingHostName(env, detail.getHostName());
            if (server == null) continue;

            OAFinder<Application, ApplicationVersion> finder = new OAFinder<Application, ApplicationVersion>(ApplicationPP.applicationVersions().pp);
            finder.addEqualFilter(ApplicationVersionPP.packageType().pp, packageType);
            
            for (ApplicationVersion av :  finder.find(server.getApplications())) {
                av.setCurrentPackageVersion(packageVersion);
            }
            detail.setLoaded(true);
        }
    }
}
