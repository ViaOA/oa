package test.xice.tsac.delegate.oa;

import java.io.BufferedReader;
import java.io.StringReader;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.PackageType;
import test.xice.tsac.model.oa.RCCommand;
import test.xice.tsac.model.oa.RCExecute;
import test.xice.tsac.model.oa.RCPackageList;
import test.xice.tsac.model.oa.RCPackageListDetail;

public class RCPackageListDelegate {

    public static void run(final RCPackageList packageList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _run(packageList);
        }
        else {
            // send to server
        }
    }

    private static boolean _run(final RCPackageList packageList) throws Exception {
        if (packageList == null) throw new RuntimeException("packageList can not be null");

        if (packageList.getRCPackageListDetails().getSize() > 0) throw new RuntimeException("already processed");
        
        RCExecute execute = packageList.getRCExecute(); 
        if (execute == null) {
            execute = new RCExecute();
            RCCommand cmd = RCCommandDelegate.getExecuteCommand(RCCommand.TYPE_getPackageList);
            execute.setRCCommand(cmd);
            packageList.setRCExecute(execute);
        }
        
        RCCommand cmd = execute.getRCCommand();
        if (cmd == null) throw new RuntimeException("execute command can not be null");
        if (cmd.getType() != cmd.TYPE_getPackageList) throw new RuntimeException("execute command type is not for getPackageLists");
        
        
        execute.setInput(null);

        String s = cmd.getCommandLine();
        execute.setCommandLine(s);
        
        RCExecuteDelegate.runOnRCInstance(execute);
        
        return true;
    }
    
    public static void process(final RCPackageList packageList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _process(packageList);
        }
        else {
            // send to server
        }
    }
    private static void _process(final RCPackageList packageList) throws Exception {
        if (packageList == null) throw new RuntimeException("packageList can not be null");

        final RCExecute execute = packageList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");

        String txt = execute.getOutput();
        txt = cleanupOutput(txt);
        
        Object[] objs = RCExecuteDelegate.getYamlObjects(txt);
        if (objs == null) {
            execute.setProcessingOutput("no YAML objects returned");
        }
        else {
            _process2(packageList, execute, objs);
        }
        execute.setProcessed(new OADateTime());
    }

    private static void _process2(final RCPackageList packageList, final RCExecute execute, final Object[] objs) {
        if (objs == null) return;

        packageList.getRCPackageListDetails().deleteAll();
        
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            if (msg == null) msg = "";
            else msg += "\n";
            
            RCPackageListDetail detail = new RCPackageListDetail();
            packageList.getRCPackageListDetails().add(detail);
            
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
    private static String _process3(final OAObject oaObj, final RCPackageListDetail detail) {
        String code = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name1");
        String packageName = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name");
        String repoDir = (String) OAObjectPropertyDelegate.getProperty(oaObj, "repo_dir");
        

        String msg = String.format(" code=%s, packageName=%s", 
             code, packageName
        );

        detail.setCode(code);
        detail.setName(packageName);
        detail.setRepoDirectory(repoDir);
        
        PackageType packageType = PackageTypeDelegate.getPackageTypeUsingCode(code, false);
        
        String msgInvalid = "";
        if (packageType == null)  {
            msgInvalid += "package type not found. ";
        }
        detail.setPackageType(packageType);
        
        detail.setInvalidMessage(msgInvalid);
        detail.setSelected(OAString.isEmpty(msgInvalid));

        return msg;
    }

    public static void load(final RCPackageList packageList) throws Exception {
        if (OASyncDelegate.isServer()) {
            _load(packageList);
        }
        else {
            // send to server
        }
    }

    private static void _load(final RCPackageList packageList) throws Exception {
        if (packageList == null) throw new RuntimeException("packageList can not be null");

        final RCExecute execute = packageList.getRCExecute(); 
        if (execute == null) throw new RuntimeException("execute can not be null, must call run first");
        
        _load2(packageList, execute);
        execute.setLoaded(new OADateTime());
    }
    
    private static void _load2(final RCPackageList packageList, final RCExecute execute) {
        for (RCPackageListDetail detail : packageList.getRCPackageListDetails()) {
            if (!detail.getSelected()) continue;
            // if (!OAString.isEmpty(detail.getInvalidMessage())) continue;
            
            
            PackageType packageType = PackageTypeDelegate.getPackageTypeUsingCode(detail.getCode(), true);
            if (packageType == null) return;

            if (OAString.isEmpty(packageType.getPackageName())) packageType.setPackageName(detail.getName()); 
            if (OAString.isEmpty(packageType.getRepoDirectory())) packageType.setRepoDirectory(detail.getRepoDirectory()); 
            
            detail.setLoaded(true);
        }
    }

    
    private static String cleanupOutput(String output) {
        if (output == null) return "";
        BufferedReader br = new BufferedReader(new StringReader(output));
        StringBuilder sb = new StringBuilder(output.length());
        
        try {
            boolean bSkip = true;
            for (int i=0;; ) {
                String line = br.readLine();
                if (line == null) break;
                if (bSkip) {
                    if (line.indexOf("packages:") >= 0) { 
                        bSkip = false;
                    }
                    continue;
                }
                if (i++ > 0) sb.append("\n");

                String s = OAString.trim(line);
                String s2 = OAString.field(s, ':', 2);
                if (s2 == null || s2.length() == 0) {
                    String s1 = OAString.field(s, ':', 1);
                    if (!"name".equals(s1) && !"extension".equals(s1) && !"repo_dir".equals(s1)) {
                        line = OAString.trim(line);
                    }
                }
                sb.append(line);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("error parsing output", e);
        }
        return new String(sb);
    }

}
