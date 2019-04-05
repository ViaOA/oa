package test.xice.tsam.delegate.oa;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAString;
import com.viaoa.util.OAYamlReader;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.delegate.oa.ApplicationTypeDelegate;
import test.xice.tsam.delegate.oa.PackageTypeDelegate;
import test.xice.tsam.model.oa.ApplicationType;
import test.xice.tsam.model.oa.PackageType;

public class ApplicationTypeDelegate {
    private static Logger LOG = Logger.getLogger(ApplicationTypeDelegate.class.getName());
    /* see:
    http://icespace.cpex.com/docs/DOC-24614 
    
    */
    
    
    public static ApplicationType getApplicationTypeUsingServerTypeId(int severTypeId, boolean bAutoCreate) {
        initialize();
        ApplicationType at = ModelDelegate.getApplicationTypes().find(ApplicationType.PROPERTY_ServerTypeId, severTypeId);
        if (at == null && bAutoCreate) {
            at = new ApplicationType();
            at.setServerTypeId(severTypeId);
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
    private static ApplicationType atTE;
    public static ApplicationType getTEApplicationType() {
        if (atTE == null) {
            initialize();
            atTE = getApplicationTypeUsingServerTypeId(8, true);
        }
        return atTE;
    }
    public static ApplicationType getGSMRApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(36, true);
    }
    public static ApplicationType getLLADApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(40, true);
    }
    public static ApplicationType getSDMRApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(12, true);
    }
    public static ApplicationType getAdminServerApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(10, true);
    }
    public static ApplicationType getMRADApplicationType() {
        initialize();
        return getApplicationTypeUsingServerTypeId(10, true);
    }

    private static ApplicationType atCE;
    public static ApplicationType getCEApplicationType() {
        if (atCE == null) {
            initialize();
            atCE = getApplicationTypeUsingServerTypeId(17, true);
        }
        return atCE;
    }
    
    private static boolean bInit;
    public static void initialize() {
        if (bInit) return;
        bInit = true;
        if (!OASync.isServer()) return;

        // https://icespace.cpex.com/docs/DOC-24614
        
        String serverTypes = 
            "UNKNOWN  0  Unknown type\n"+
            "ORMR    01  Webice Order Router.\n"+
            "PDMR    02  Webice Public Data Market Router - Futures\n"+
            "POMR    03  Webice Public Data Market Router - Options\n"+
            "MMMR    04  Market Monitor Router (Sales GUI and ATLAS/Website).\n"+
            "Legacy  05  Legacy Webice Router\n"+
            "WAMR    06  Web App Market Router\n"+
            "TSAC    07  Trading System Admin Client\n"+
            "TE      08  Trading Engine\n"+
            "TBMR    09  Top of Book Router\n"+
            "MRAD    10  MR Admin Server\n"+
            "FIXOS   11  FIX Order Router\n"+
            "SDMR    12  Static Data Server\n"+
            "DHMR    13  Deal History Server\n"+
            "TCMR    14  Trade Capture Server\n"+
            "IBMR    15  ICE Block\n"+
            "OFMR    16  Private Order Feed\n"+
            "CEM     17  Curve Engine Manager\n"+
            "TSAM    18  Application Manager\n"+
            "TSSM    19  Schedule Manager\n"+
            "IRTS    20  ICE Restful Trading Service\n"+
            "OCMR    21  Order Cache MR\n"+
            "OHMR    22  Order History MR\n"+
            "OBMR    23  Oracle Bridge MR\n"+
            "FXAD    24  FIX OS Admin\n"+
            "FXMR    25  FIX Adapter\n"+
            "POF     26  \n"+
            "EXAS    27  Endex services\n"+
            "CFMR    29  Creditex Feed Service\n"+
            "SOMR    30  Smart Order MR\n"+
            "PFMR    31  Price Feed Market Router\n"+
            "MCMR    32  Multicast Market Router\n"+
            "RPMR    33  Replay Marker Router\n"+
            "UPSMR   34  User Private Strategy Router\n"+
            "FPMR    35  FPML Router.\n"+
            "GSMR    36  Gemstone Router\n"+
            "SDSWeb  37  SDS web router\n"+
            "HDMR    38  Hex Data Feed\n"+
            "PKMR    39  Position Keeper Market Router\n"+
            "IMMR    41  web mobile\n"+
            "DRMR    42  Deal recovery server\n"+
            "DLMR    43  Deal Loader\n"+
            "RMMR    44  Risk Manager MR\n"+
            "FRS     45  Futures Reasonability Service\n"+
            "MTS     46  Multicast Tunneling Service\n"+
            "MTSR    47  Remote MTS\n"+
            "ATLAS   48  Atlas 24x7\n"+
            "LLAD    49  Login Logout Admin Server\n"+
            "LLS     50  Login Logout Server\n"+
            "TRAPI   51  XML Trade Registration API for external clients";
            
        

        BufferedReader br = new BufferedReader(new StringReader(serverTypes));

        try {
            for (int i=0;; ) {
                String line = br.readLine();
                if (line == null) break;
                line = OAString.trim(line);
                String code = OAString.field(line, " ", 1);
                int id = OAConv.toInt(OAString.field(line, " ", 2));
                String desc = OAString.field(line, " ", 3, 999);
                
                ApplicationType at = getApplicationTypeUsingCode(code, true);
                at.setServerTypeId(id);
                if (OAString.isEmpty(at.getDescription())) at.setDescription(desc);
                at.setRegistered(true);
            }
        }
        catch (Exception e) {
            
        }

        ApplicationType at = getApplicationTypeUsingCode("TE", false);
        if (at != null) {
            at.setConnectsToMRAD(false);
            at.setUserId("gemdba");
        }
        at = getApplicationTypeUsingCode("CEM", false);
        if (at != null) {
            at.setConnectsToMRAD(false);
            at.setUserId("cewausr");
        }
        
    }

    
    // from RC project (Shane's team)
    public static void importYaml() throws Exception {
        LOG.fine("checking for services.yaml");
        
        File file = new File("services.yaml");
        if (!file.exists()) return;
        String data = OAFile.readTextFile(file, 16000);
        LOG.fine("loading services.yaml, data="+data);
        
        Object[] objs = getYamlObjects(data);
        if (objs == null) return;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;
            loadFromYaml(oaObj);
        }
    }    
    private static Object[] getYamlObjects(final String txt) {
        if (OAString.isEmpty(txt)) return null;
        
        OAYamlReader yamlReader = new OAYamlReader("Service", "name1", "name2") {
            @Override
            protected String getClassName(String className) {
                return "com.viaoa.object.OAObject";
            }
            
            @Override
            protected Object getValue(OAObject obj, String name, Object value) {
                if (value instanceof String) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, name, (String) value);
                }
                return super.getValue(obj, name, value);
            }
        };

        Object[] objs = yamlReader.parse(txt, OAObject.class);
        
        return objs;
    }
    private static void loadFromYaml(final OAObject oaObj) {
        String name = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name1");
        String login = (String) OAObjectPropertyDelegate.getProperty(oaObj, "login"); 
        String packages = (String) OAObjectPropertyDelegate.getProperty(oaObj, "packages");
        String type = (String) OAObjectPropertyDelegate.getProperty(oaObj, "type");
        String baseDir = (String) OAObjectPropertyDelegate.getProperty(oaObj, "base_dir");
        String startCommand = (String) OAObjectPropertyDelegate.getProperty(oaObj, "start_command");
        String stopCommand = (String) OAObjectPropertyDelegate.getProperty(oaObj, "stop_command");
        String healthPort = (String) OAObjectPropertyDelegate.getProperty(oaObj, "health_port");


        ApplicationType at = ApplicationTypeDelegate.getApplicationTypeUsingCode(name, true);
        
        at.setCode(name);
        //at.setLogin(login);
        //at.setPackages(packages);
        at.setDirectory(baseDir);
        at.setStartCommand(startCommand);
        at.setStopCommand(stopCommand);
        if (OAString.isNumber(healthPort)) at.setF5Port(OAConv.toInt(healthPort));
        
        String s = packages;
        s = OAString.convert(s, "[", null);
        s = OAString.convert(s, "]", null);
        s = OAString.convert(s, ",", null);
        s = OAString.trim(s);
        for (int i=0; ;i++) {
            String s2 = OAString.field(s, ' ', i+1);
            if (s2 == null) break;
            
            PackageType packageType = PackageTypeDelegate.getPackageTypeUsingCode(s2, false);
            if (packageType != null) at.getPackageTypes().add(packageType);
        }
    }
    
    
}
