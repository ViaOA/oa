package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;

public class RCRepoVersionDelegate {

    public static void run(final RCRepoVersion repoVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(repoVersion);
        }
        else {
            // send to server
        }
    }

    private static boolean _run(final RCRepoVersion repoVersion) throws Exception {
        if (repoVersion == null) throw new RuntimeException("repoVersion can not be null");

        if (repoVersion.getRCRepoVersionDetails().getSize() > 0) throw new RuntimeException("already processed");
        
        RCExecute execute = repoVersion.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getRepoVersions);
            execute.setRCCommand(cmd);
            repoVersion.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_getRepoVersions) throw new RuntimeException("execute command type is not for getRepoVersions");
        
        
        Environment env = repoVersion.getEnvironment();
        if (env == null) throw new RuntimeException("no environment for repoVersion");

        String s = cmd.getCommandLine();
        s = OAString.convert(s, "$ENV", env.getName());
        execute.setCommandLine(s);
        
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    
    public static void process(final RCRepoVersion repoVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(repoVersion);
        }
        else {
            // send to server
        }
    }
    private static void _process(final RCRepoVersion repoVersion) throws Exception {
        if (repoVersion == null) throw new RuntimeException("repoVersion can not be null");

        final RCExecute execute = repoVersion.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        String msg = "";
        Object[] objs = RCExecuteDelegate.getJsonObjects(execute);
        if (objs == null) {
            execute.setProcessingOutput("no JSON objects returned");
        }
        else {
            _process2(repoVersion, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCRepoVersion repoVersion, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;
        final Environment env = repoVersion.getEnvironment();
        if (env == null) {
            execute.setProcessingOutput("invalid: no environment");
            return;
        }
        
        repoVersion.getRCRepoVersionDetails().deleteAll();
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, RCExecuteDelegate.JsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("RepoVersionOutput"))  {
                continue;
            }
            
            if (msg == null) msg = "";
            else msg += "\n";
            
            RCRepoVersionDetail detail = new RCRepoVersionDetail();
            repoVersion.getRCRepoVersionDetails().add(detail);
            
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
    private static String _process3(final OAObject oaObj, final Environment env, final RCRepoVersionDetail detail) {
        String buildDate = (String) OAObjectPropertyDelegate.getProperty(oaObj, "build_date");
        String packageId = (String) OAObjectPropertyDelegate.getProperty(oaObj, "package_id");
        String error = (String) OAObjectPropertyDelegate.getProperty(oaObj, "error");
        String version = (String) OAObjectPropertyDelegate.getProperty(oaObj, "version");

        String msg = String.format(" package=%s, buildDate=%s, version=%s, error=%s", 
             packageId, buildDate, version, error
        );

        if ("null".equals(packageId)) packageId = null;
        if ("null".equals(error)) error = null;
        if ("null".equals(version)) version = null;

        detail.setPackageId(packageId);
        detail.setVersion(version);

        if (OAString.isInteger(buildDate))  {
            OADateTime dt = new OADateTime(Long.valueOf(buildDate));
            detail.setBuildDate(new OADate(dt));
        }
        
        String msgInvalid = "";
        if (packageId == null)  {
            msgInvalid += "packageName is null. ";
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
            packageType = PackageTypeDelegate.getPackageTypeUsingCode(packageId, true);
        
            detail.setPackageType(packageType);
            if (packageType == null) {
                msgInvalid += "packageType not found. ";
            }
        }
        
        detail.setInvalidMessage(msgInvalid);

        return msg;
    }

    public static void load(final RCRepoVersion repoVersion) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(repoVersion);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCRepoVersion repoVersion) throws Exception {
        if (repoVersion == null) throw new RuntimeException("repoVersion can not be null");

        final RCExecute execute = repoVersion.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(repoVersion, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final RCRepoVersion repoVersion, final RCExecute execute) {
        Environment env = repoVersion.getEnvironment();
        
        for (RCRepoVersionDetail detail : repoVersion.getRCRepoVersionDetails()) {
            if (!detail.getSelected()) continue;
            if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            PackageType packageType = detail.getPackageType();
            if (packageType == null) continue;
            
            String version = detail.getVersion();
            if (OAString.isEmpty(version)) continue;
            
            if (OAString.isEmpty(version)) continue;
            PackageVersion packageVersion = packageType.getPackageVersions().find(PackageVersion.P_Version, version);
            if (packageVersion == null) {
                packageVersion = new PackageVersion();
                packageVersion.setVersion(version);
                packageVersion.setBuildDate(detail.getBuildDate());
                packageType.getPackageVersions().add(packageVersion);
            }
            else {
                if (packageVersion.getBuildDate() == null) {
                    packageVersion.setBuildDate(detail.getBuildDate());                    
                }
            }
            detail.setPackageVersion(packageVersion);
            detail.setLoaded(true);
        }
    }
}
