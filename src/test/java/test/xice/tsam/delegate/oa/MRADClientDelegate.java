package test.xice.tsam.delegate.oa;

import java.awt.Color;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;
import com.viaoa.util.OAXMLReader;
import com.viaoa.util.OAXMLWriter;
import com.viaoa.util.filter.OAEqualFilter;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.delegate.oa.ApplicationDelegate;
import test.xice.tsam.delegate.oa.ApplicationStatusDelegate;
import test.xice.tsam.delegate.oa.ApplicationTypeDelegate;
import test.xice.tsam.delegate.oa.MRADClientDelegate;
import test.xice.tsam.delegate.oa.OperatingSystemDelegate;
import test.xice.tsam.delegate.oa.ServerDelegate;
import test.xice.tsam.delegate.oa.SiloTypeDelegate;
import test.xice.tsam.model.oa.Application;
import test.xice.tsam.model.oa.ApplicationStatus;
import test.xice.tsam.model.oa.ApplicationType;
import test.xice.tsam.model.oa.Environment;
import test.xice.tsam.model.oa.MRADClient;
import test.xice.tsam.model.oa.MRADServer;
import test.xice.tsam.model.oa.OSVersion;
import test.xice.tsam.model.oa.OperatingSystem;
import test.xice.tsam.model.oa.PackageType;
import test.xice.tsam.model.oa.SSHExecute;
import test.xice.tsam.model.oa.Server;
import test.xice.tsam.model.oa.Silo;
import test.xice.tsam.model.oa.SiloType;
import test.xice.tsam.model.oa.Site;
import test.xice.tsam.model.oa.propertypath.*;

public class MRADClientDelegate {
    private static Logger LOG = Logger.getLogger(MRADClientDelegate.class.getName());

    private static ReentrantLock lock = new ReentrantLock();



    public static MRADClient getMRADClient(Silo silo, MRADServer mradServer, String hostName, ApplicationType applicationType, final String appName, boolean bAutoCreate) {

        try {
            lock.lock();
            MRADClient mc = _getMRADClient(silo, mradServer, hostName, applicationType, appName, bAutoCreate);
            return mc;
        }
        finally {
            lock.unlock();
        }
    }

    private static MRADClient _getMRADClient(Silo silo, MRADServer mradServer, String hostName, ApplicationType applicationType, final String appName, boolean bAutoCreate) {
        if (mradServer == null) return null;
        if (silo == null) return null;
        if (applicationType == null) return null;
        if (OAString.isEmpty(hostName)) return null;

        Application application = null;
        MRADClient mradClient = null;

        // might be another with different hostName
        if (applicationType != null && !OAString.isEmpty(appName)) {
            String pp = MRADServerPP.mradClients().application().pp;
            OAFinder<MRADServer, Application> f = new OAFinder<MRADServer, Application>(pp);
            f.addFilter(new OAEqualFilter(ApplicationPP.applicationType().pp, applicationType));
            f.addFilter(new OAEqualFilter(ApplicationPP.name(), appName));
            application = f.findFirst(mradServer);
            if (application != null) {
                mradClient = mradServer.getMRADClients().find(MRADClientPP.application().pp, application);
                if (mradClient != null) {
                    return mradClient;
                }
            }
        }

        // find matching server
        Server server = silo.getServers().find(ServerPP.hostName(), hostName);
        if (server == null) {
            if (!bAutoCreate) return null;
            server = new Server();
            server.setHostName(hostName);
            server.setSilo(silo);
        }

        // find App that has same type and name
        for (;;) {
            application = server.getApplications().findNext(application, ApplicationPP.applicationType().pp, applicationType);
            if (application == null) break;
            if (OAString.isEmpty(appName)) break;

            if (appName.equalsIgnoreCase(application.getName())) break;
        }

        if (application == null) {
            if (!bAutoCreate) return null;
            application = new Application();
            application.setApplicationType(applicationType);
            application.setName(appName);

            // this must be before adding App to Server, else it will autocreate the MRADClient
            mradClient = new MRADClient();
            mradClient.setApplication(application);
            mradServer.getMRADClients().add(mradClient);

            server.getApplications().add(application);
        }
        else {
            mradClient = mradServer.getMRADClients().find(MRADClientPP.application().pp, application);
        }
        if (mradClient != null) {
            mradClient.setName(appName);
            mradClient.setHostName(hostName);
        }
        return mradClient;
    }

    public static void xmlExport(Hub<MRADClient> hub, String fileName) {
        if (hub == null || OAString.isEmpty(fileName)) return;
        final OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(MRADClient.class);

        OAXMLWriter xmlWriter = new OAXMLWriter(fileName) {
            @Override
            public int shouldWriteProperty(Object obj, String propertyName, Object value) {
                if (!(obj instanceof MRADClient)) return WRITE_NO;
                if (oi.getLinkInfo(propertyName) != null) return WRITE_NO;
                return WRITE_YES;
            }
        };
        xmlWriter.setIndentAmount(4);
        xmlWriter.write(hub);
        xmlWriter.close();
    }

    public static void xmlImport(final Hub<MRADClient> hubMRADClient, String fileName) throws Exception {
        if (hubMRADClient == null || OAString.isEmpty(fileName)) return;
        LOG.fine("fileName=" + fileName);

        final Silo silo = ModelDelegate.getDefaultSilo();
        if (silo == null) return;

        final Environment env = silo.getEnvironment();
        if (env == null) return;

        Site site = env.getSite();
        if (site == null) return;

        final String hostPrefix = site.getName() + "-" + env.getAbbrevName();

        OAXMLReader xmlReader = new OAXMLReader() {
            @Override
            public Object getValue(OAObject object, String name, Object value) {
                if (object instanceof MRADClient) {
                    MRADClient mc = (MRADClient) object;
                    if ("id".equalsIgnoreCase(name)) {
                        value = mc.getId(); // dont use the ID in xml file.
                    }
                }
                return value;
            }

            
            int cnt;
            @Override
            protected OAObject getRealObject(OAObject object) {
                if (!(object instanceof MRADClient)) return object;
                final MRADClient mc = (MRADClient) object;

                String origHostName = mc.getHostName();
                if (OAString.dcount(origHostName, "-") < 4) return mc;
                LOG.finest((++cnt) + ") loading " + origHostName);

                String hostName = hostPrefix + "-" + OAString.field(origHostName, "-", 3, 2);
                mc.setHostName(hostName);

                Server server;
                if (OAString.isEqual(origHostName, hostName)) {
                    server = ServerDelegate.getServer(silo, hostName, mc.getIpAddress(), true);
                }
                else {
                    server = ServerDelegate.getServerUsingHostName(silo, hostName, true);
                }

                ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(mc.getApplicationTypeCode(), true);

                Application app = ApplicationDelegate.getApplication(server, appType, -1, mc.getName(), false);

                if (app == null) {
                    hubMRADClient.add(mc);
                    app = ApplicationDelegate.getApplication(server, appType, -1, mc.getName(), true, false);
                    app.setName(mc.getName());
                    mc.setApplicationStatus(null);
                    mc.setApplication(app);
                    server.getApplications().add(app);
                }
                return mc;
            }
        };
        xmlReader.readFile(fileName);
    }

}
