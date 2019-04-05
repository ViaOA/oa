package test.xice.tsam.delegate.oa;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import test.xice.tsam.delegate.oa.EnvironmentTypeDelegate;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.OAString;
import com.viaoa.util.OAXMLReader;
import com.viaoa.util.OAXMLReader1;
import com.viaoa.util.OAXMLWriter;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.delegate.oa.ApplicationTypeDelegate;
import test.xice.tsam.delegate.oa.SiteDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.custom.EnvironmentApplication;
import test.xice.tsam.model.oa.propertypath.*;

public class EnvironmentDelegate {
    
    public static Environment getEnvironment(Site site, String abbrevName, boolean bAutoCreate) {
        if (site == null || abbrevName == null) return null;

        for (Environment env : site.getEnvironments()) {
            if (abbrevName.equalsIgnoreCase(env.getAbbrevName())) return env;
        }
        if (!bAutoCreate) return null;
        Environment env = new Environment();
        env.setAbbrevName(abbrevName);
        env.setName(abbrevName);

        if (abbrevName.equalsIgnoreCase("pr")) {
            site.setProduction(true);
            if ("ord".equalsIgnoreCase(site.getAbbrevName())) {
                env.setColorCode(Color.red);
            }
            else {
                env.setColorCode(Color.blue);
            }
        }
        
        site.getEnvironments().add(env);
        env.setEnvironmentType(EnvironmentTypeDelegate.getEnvironmentType(env));
        return env;
    }

    public static Environment getEnvironment(String hostName, boolean bAutoCreate) {
        if (hostName == null) return null;

        Site site = SiteDelegate.getSite(hostName, false);
        if (site == null) return null;

        String name = OAString.field(hostName, "-", 2);
        if (name == null) {
            if (hostName.toLowerCase().endsWith(".intcx.net")) { // dns name
                name = OAString.field(hostName, ".", 2);
            }
        }
        if (name == null) name = "LOCAL";
        String abbrevName = name;

        boolean bIsProd = false;
        if (abbrevName.equalsIgnoreCase("pr")) {
            bIsProd = true;
            name = OAString.field(hostName, "-", 1);
            if (name == null) {
                if (hostName.toLowerCase().endsWith(".intcx.net")) { // dns name
                    name = OAString.field(hostName, ".", 1);
                }
            }
        }
        
        for (Environment env : site.getEnvironments()) {
            if (name.equalsIgnoreCase(env.getName())) return env;
            if (name.equalsIgnoreCase(env.getAbbrevName())) return env;
        }
        if (!bAutoCreate) return null;
        Environment env = new Environment();
        env.setName(name);
        env.setAbbrevName(abbrevName);
        if (bIsProd) {
            site.setProduction(bIsProd);
            env.setColorCode(Color.red);
        }

        env.setEnvironmentType(EnvironmentTypeDelegate.getEnvironmentType(env));
        site.getEnvironments().add(env);
        return env;
    }


//qqqqqqqq move to Silo delegate qqqqq    
    private static boolean _mradminImport(final Environment env, File file) {
        if (env == null) return false;

        final Silo silo = ModelDelegate.getDefaultSilo();
        
        MRADServer ms = silo.getMRADServer();
        if (ms != null) ms.delete();

        silo.getServers().deleteAll();
        
        final MRADServer mradServer = new MRADServer();
        silo.setMRADServer(mradServer);
        
        OAXMLReader1 xr = new OAXMLReader1() {
            String lastValue;
            boolean bIsRouter;
            @Override
            public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
                // qName = "routerClass"
                if ("router".equalsIgnoreCase(qName)) bIsRouter = true;
                lastValue = attrs.getValue("xmlns");
                super.startElement(namespaceURI, sName, qName, attrs);
            }
            @Override
            public void endObject(OAObject obj, boolean hasParent) {
                if (!bIsRouter) return;
                bIsRouter = false;
                
                /*                
                  <router>
                    <name xmlns="RouterDV2GHOST"></name>
                    <routerIp xmlns="10.3.50.1"></routerIp>
                    <routerClass xmlns="Fix Order Routing"></routerClass>
                    <routerDirectory xmlns="/var/opt/ice_fix"></routerDirectory>
                    <routerStartScriptKeepIor xmlns="/var/opt/ice_fix/orderServer.sh start"></routerStartScriptKeepIor>
                    <routerStartScriptNoIor xmlns="/var/opt/ice_fix/orderServer.sh start"></routerStartScriptNoIor>
                    <routerStopScript xmlns="/var/opt/ice_fix/stop.sh"></routerStopScript>
                    <routerDeleted xmlns="true"></routerDeleted>
                  </router>
                */       
                
                String s;
                String name = obj.getPropertyAsString("name");
                String ipAddress = obj.getPropertyAsString("routerIp");
                String routerClass = obj.getPropertyAsString("routerClass");
                String directory = obj.getPropertyAsString("routerDirectory");
                String startScript = obj.getPropertyAsString("routerStartScriptNoIor");
                String snapshotScript = obj.getPropertyAsString("routerStartScriptKeepIor");
                String stopScript = obj.getPropertyAsString("routerStopScript");
                s = obj.getPropertyAsString("routerDeleted");
                if ("true".equals(s)) return;

                ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(routerClass, true);
                
                Server server = silo.getServers().find(ServerPP.ipAddress(), ipAddress);
                if (server == null) {
                    server = new Server();
                    server.setIpAddress(ipAddress);
                    silo.getServers().add(server);
                }
                if (OAString.isEmpty(server.getHostName())) server.setHostName(ipAddress);
                Application app = server.getApplications().find(ApplicationPP.name(), name);
                if (app == null) {
                    app = new Application();
                    app.setName(name);
                    server.getApplications().add(app);
                }
                app.setApplicationType(appType);
                
                MRADClient mradClient = null;
                for (int i=0; i<5; i++) {
                    mradClient = mradServer.getMRADClients().find(MRADClientPP.application().name(), name);
                    if (mradClient != null) break;
                    try {
                        Thread.sleep(50);                        
                    }
                    catch (Exception e) {
                    }
                }
                if (mradClient == null) {
                    mradClient = new MRADClient();
                    mradServer.getMRADClients().add(mradClient);
                    mradClient.setIpAddress(ipAddress);
                }
                mradClient.setHostName(ipAddress);
                mradClient.setIpAddress(ipAddress);
                mradClient.setName(name);
                mradClient.setApplication(app);
                mradClient.setRouterAbsolutePath(directory);
                mradClient.setDirectory(directory);
                mradClient.setStartScript(startScript);
                mradClient.setSnapshotStartScript(snapshotScript);
                mradClient.setStopScript(stopScript);
            }
            @Override
            protected String resolveClassName(String className) {
                return null;
            }
            @Override
            protected void processProperty(String eName, String value, Class conversionClass, Hashtable hash) {
                value = lastValue;
                super.processProperty(eName, value, conversionClass, hash);
            }
        };
        try {
            xr.read(file.getAbsolutePath());
        }
        catch (Exception e) {
            throw new RuntimeException("failed to read " + file, e);
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        Environment env = new Environment();
        _mradminImport(env, new File("routers.xml"));
    }

}
