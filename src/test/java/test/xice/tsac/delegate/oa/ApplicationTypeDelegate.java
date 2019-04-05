package test.xice.tsac.delegate.oa;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.ApplicationType;
import test.xice.tsac.model.oa.PackageType;
import test.xice.tsac.model.oa.propertypath.ApplicationTypePP;

public class ApplicationTypeDelegate {

    
    public static ApplicationType getApplicationTypeUsingServerTypeId(int severTypeId, boolean bAutoCreate) {
        initialize();
        ApplicationType at = ModelDelegate.getApplicationTypes().find(ApplicationType.PROPERTY_ServerTypeId, severTypeId);
        if (at == null && bAutoCreate) {
            at = new ApplicationType();
            at.setServerTypeId(severTypeId);
            //at.setCreatedBy("new serverTypeId");
            ModelDelegate.getApplicationTypes().add(at);
        }
        return at;
    }
    
    public static ApplicationType getApplicationTypeUsingCode(String code, boolean bAutoCreate) {
        initialize();
        if (OAString.isEmpty(code)) return null;
        
        String s = code.toLowerCase();
        if (s.startsWith("ex") || s.startsWith("lx") || s.startsWith("ix")) code = code.substring(2);
        
        ApplicationType at = ModelDelegate.getApplicationTypes().find(ApplicationType.PROPERTY_Code, code);
        if (at == null && bAutoCreate) {
            at = new ApplicationType();
            at.setCode(code);
            //at.setCreatedBy("new code");
            ModelDelegate.getApplicationTypes().add(at);
        }
        return at;
    }
    
    public static ApplicationType getApplicationTypeUsingHostName(String hostName, boolean bAutoCreate) {
        if (OAString.isEmpty(hostName)) return null;
        
        String name = OAString.field(hostName, "-", 3);
        if (name == null) {
            if (hostName.toLowerCase().endsWith(".intcx.net")) {  // dns name
                name = OAString.field(hostName, ".", 1);
            }
            else {
                if (hostName.indexOf(".") < 0) name = hostName;
            }
        }
        if (name == null) name = "LOCAL";
        ApplicationType at = getApplicationTypeUsingCode(name, bAutoCreate);
        return at;
    }

    /*qqqqqqqqq
    public static ApplicationType getApplicationTypeUsingPackageName(String packageName, boolean bAutoCreate) {
        initialize();
        ApplicationType at = ModelDelegate.getApplicationTypes().find(ApplicationTypePP.packageTypes().packageName(), packageName);
        if (at == null && bAutoCreate) {
            at = new ApplicationType();
            at.setServerTypeId(severTypeId);
            at.setCreatedBy("new serverTypeId");
        }
        return at;
    }
    */
    
    public static ApplicationType getGSMRApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(36, true);
    }
    public static ApplicationType getLLADApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(40, true);
    }
    public static ApplicationType getAdminServerApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(10, true);
    }
    public static ApplicationType getMRADApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(10, true);
    }
    
    private static boolean bInit;
    public static void initialize() {
        if (bInit) return;
        bInit = true;

        // unknown
        int serverTypeId = 0;
        ApplicationType appType = getApplicationTypeUsingServerTypeId(serverTypeId, false);
        if (appType == null) {
            appType = new ApplicationType();
            appType.setServerTypeId(serverTypeId);
            appType.setCode("unknown");
            //appType.setName("Unknown");
            appType.setDescription("Unknown");
            //appType.setCreatedBy("default");
            ModelDelegate.getApplicationTypes().add(appType);
        }
        
        
        // GSMR
        serverTypeId = 36;
        appType = getApplicationTypeUsingServerTypeId(serverTypeId, false);
        if (appType == null) {
            appType = new ApplicationType();
            appType.setServerTypeId(serverTypeId);
            appType.setCode("gsmr");
            //appType.setName("GSMR");
            appType.setDescription("MR for accessing Gemstone");
            //appType.setCreatedBy("default");
            ModelDelegate.getApplicationTypes().add(appType);
        }

    
        // LLAD 
        serverTypeId = 40;
        appType = getApplicationTypeUsingServerTypeId(serverTypeId, false);
        if (appType == null) {
            appType = new ApplicationType();
            appType.setServerTypeId(serverTypeId);
            appType.setCode("llad");
            //appType.setName("LLAD");
            appType.setDescription("Login Logout admin server");
            //appType.setCreatedBy("default");
            ModelDelegate.getApplicationTypes().add(appType);
        }

        // MR AdminServer 
        serverTypeId = 10;
        appType = getApplicationTypeUsingServerTypeId(serverTypeId, false);
        if (appType == null) {
            appType = new ApplicationType();
            appType.setServerTypeId(serverTypeId);
            appType.setCode("mrad");
            //appType.setName("MRAD");
            appType.setDescription("MR Admin Server");
            //appType.setCreatedBy("default");
            ModelDelegate.getApplicationTypes().add(appType);
        }
    }

    
    
    
}
