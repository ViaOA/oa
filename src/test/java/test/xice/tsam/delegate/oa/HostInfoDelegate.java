package test.xice.tsam.delegate.oa;

import java.awt.Color;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import test.xice.tsam.delegate.oa.HostInfoDelegate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

import test.xice.tsam.delegate.oa.ApplicationTypeDelegate;
import test.xice.tsam.delegate.oa.OperatingSystemDelegate;
import test.xice.tsam.model.oa.Application;
import test.xice.tsam.model.oa.ApplicationType;
import test.xice.tsam.model.oa.HostInfo;
import test.xice.tsam.model.oa.MRADClient;
import test.xice.tsam.model.oa.OSVersion;
import test.xice.tsam.model.oa.OperatingSystem;
import test.xice.tsam.model.oa.PackageType;
import test.xice.tsam.model.oa.Server;

public class HostInfoDelegate {
    private static Logger LOG = Logger.getLogger(HostInfoDelegate.class.getName());

    private static ReentrantLock lock = new ReentrantLock();

    private static String hostName;
    
    private static class MyHostInfo {
        long ts;

        public MyHostInfo() {
            ts = System.currentTimeMillis();
        }
        MRADClient mc;
        String toolTipText;
        MyStatus status;
        ArrayList<String> al = new ArrayList<String>();
        String[] cmds;
        
        String sshUserName;
    }
    private static enum MyStatus {
        red, // host not reachable, 
        orange, // can reach host and get some info
        yellow, // needs more setup info
        green // good
    }

    public static String getSSHUserName(MRADClient mc) {
        MyHostInfo hi = getHostInfo(mc);
        return hi.sshUserName;
    }
    public static String[] getHostInfoCommands(MRADClient mc) {
        MyHostInfo hi = getHostInfo(mc);
        return hi.cmds;
    }
    public static Color getHostInfoColor(MRADClient mc) {
        MyHostInfo hi = getHostInfo(mc);
        if (hi.status == MyStatus.green) return Color.green;
        if (hi.status == MyStatus.yellow) return Color.yellow;
        if (hi.status == MyStatus.orange) return Color.orange;
        return Color.red;
    }
    public static int getStatusCode(HostInfo hostInfo) {
        if (hostInfo == null) return 3;
        MRADClient mc = hostInfo.getMRADClient();
        if (mc == null) return 3;
        MyHostInfo hi = getHostInfo(mc);
        if (hi.status == MyStatus.green) return 0;
        if (hi.status == MyStatus.yellow) return 1;
        if (hi.status == MyStatus.orange) return 2;
        return 3;
    }
    public static String getHostInfoToolTipText(MRADClient mc) {
        MyHostInfo hi = getHostInfo(mc);
        String tt = "";
        for (String s : hi.al) {
            if (tt.length() > 0) tt += "\n";
            tt += s;
        }
        return tt;
    }

    private static MyHostInfo hiLast;
    private static MyHostInfo getHostInfo(MRADClient mc) {
        if (hiLast != null && hiLast.mc == mc) {
            if (hiLast.ts > (System.currentTimeMillis() - 2500)) {
                return hiLast;
            }        
        }
        MyHostInfo hi =_getHostInfo(mc);
        if (mc != null) hiLast = hi;
        return hi;
    }
    private static MyHostInfo _getHostInfo(MRADClient mc) {
        if (mc == null) return null;
        if (hostName == null) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while getting hostName for localhost", e);
                hostName = "tsac";
            }
        }

        final HostInfo hostInfo = mc.getHostInfo();
        MyHostInfo hi = new MyHostInfo();
        hi.mc = mc;
        hi.status = MyStatus.red;
        
        if (mc == null) {
            hi.al.add("Client is null");
            return hi;
        }
        final Application app = mc.getApplication();
        if (app == null) {
            hi.al.add("Application is null");
            return hi;
        }
        final Server server = app.getServer();
        if (server == null) {
            hi.al.add("Server is null");
            return hi;
        }
        final ApplicationType appType = app.getApplicationType();
        if (appType == null) {
            hi.al.add("Application Type is null");
            return hi;
        }

        boolean bIsMR = true;
        if (appType == ApplicationTypeDelegate.getTEApplicationType()) {
            hi.al.add("Application is for TE");
            bIsMR = false;
        }
        if (appType == ApplicationTypeDelegate.getCEApplicationType()) {
            hi.al.add("Application is for CE");
            bIsMR = false;
        }

        if (OAString.isEmpty(mc.getIpAddress())) {
            hi.al.add("IPAdress is not defined");
            return hi;
        }

        hi.status = MyStatus.orange;
        
        OSVersion osv = server.getOSVersion();
        OperatingSystem os = null;
        if (osv == null) {
            hi.al.add("OS Version is not defined");
        }
        else {
            os = osv.getOperatingSystem();
            if (os == null) {
                hi.al.add("Operating System is not defined");
            }
            else hi.status = MyStatus.yellow;
        }


        ArrayList<String> al = new ArrayList<String>();
        if (bIsMR) {
            al.add("date");
            al.add("hostname");
            al.add("uname");
            al.add("crontab -l");
            al.add("telnet " + hostName + " 9001");
        }

        boolean bHasError = false;
        
        String s = appType.getDirectory();
        
        if (OAString.isEmpty(s)) {
            bHasError = true;
            hi.al.add("Root directory is null for app type "+appType.getCode());
            hi.status = MyStatus.orange;
        }        
        
        String jarDir = appType.getJarDirectoryName(); // ex: "/var/opt/gsmr/jar"
        if (OAString.isEmpty(jarDir)) jarDir = "lib";

        if (!OAString.isEmpty(s) && !OAString.isEmpty(jarDir)) {
            al.add("ls -l " + s+"/"+jarDir + "/*.jar");
        }

        PackageType pt = appType.getPackageTypes().getAt(0);
        if (pt != null && bIsMR) {
            String pkgName = pt.getPackageName();

            if (!OAString.isEmpty(pkgName) && os != null) {
                if (os.getType() == os.TYPE_Linux) {
                    al.add("rpm -qi " + pkgName + " |grep -i version");
                }
                else {
                    al.add("pkginfo -l " + pkgName + "|grep -i version");
                }
                if (!bHasError) hi.status = MyStatus.yellow;
                OADateTime dt = hostInfo.getLastSSHCheck();
                OADateTime dtNow = new OADateTime();
                if (dt != null && dt.after(dtNow.addDays(-5))) {
                    if (!bHasError) hi.status = MyStatus.green;
                }
                else {
                    if (!bHasError) hi.status = MyStatus.yellow;
                    bHasError = true;
                    hi.al.add("host info needs to be refreshed");
                }
            }
            else {
                if (OAString.isEmpty(pkgName)) {
                    hi.al.add("Package name is not set, needed to get installed version");
                    if (!bHasError) hi.status = MyStatus.orange;
                }
            }
        }
        else {
            hi.al.add("Package type is not set");
            hi.status = MyStatus.orange;
            bHasError = true;
        }

        String[] cmds = new String[al.size()];
        al.toArray(cmds);
        hi.cmds = cmds;
        if (!bIsMR) hi.cmds = null;
        
        // find userId to use
        String userName = null;
        s = app.getUserId();
        if (!OAString.isEmpty(s)) userName = s;
        else if (server != null) {
            s = server.getUserId();
            if (!OAString.isEmpty(s)) userName = s;
            else {
                if (appType != null) {
                    s = appType.getUserId();
                    if (!OAString.isEmpty(s)) userName = s;
                }
            }
        }

        s = hostInfo.getJarDirectory();
        if (s != null && s.indexOf("Error: exit code=") > 0) s = null;
        if (OAString.isEmpty(s)) {
            if (hi.status == MyStatus.green) {
                hi.status = MyStatus.yellow;
                hi.al.add("jar directory is empty, please verify that it's correct for app type "+appType.getCode());
            }
        }
        
        // make sure valid version of iceadmin
        s = hostInfo.getAdminClientVersion();
        if (!OAString.isEmpty(s)) {
            s = s.substring(s.length()-2);
            if (OAString.isNumber(s)) {
                if (OAConv.toInt(s) < 49) {
                    hi.al.add("wrong version of iceadminclient "+hostInfo.getAdminClientVersion()+", should be 49");
                    hi.status = MyStatus.orange;
                }
            }
        }
        
        
        if (userName == null) {
            if (os != null) {
                s = os.getUserId();
                if (!OAString.isEmpty(s)) userName = s;
            }
            if (userName == null) {
                hi.al.add("No SSH user set for any of App/Server/AppType/OS");
                hi.status = MyStatus.red;
                hi.cmds = null;
            }
        }        
        hi.sshUserName = userName;
        
        if (!OAString.isEmpty(hostInfo.getSSHError())) {
            hi.status = MyStatus.red;
            hi.al.add("last SSH failed, error:"+ OAString.fmt(hostInfo.getSSHError(), "25L."));
        }
        
        return hi;
    }

    
}
