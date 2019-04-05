// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsam.delegate;

import test.xice.tsam.delegate.ServerModelDelegate;
import test.xice.tsam.model.oa.*;
import com.viaoa.annotation.OAOne;
import com.viaoa.hub.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAString;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.cs.ClientRoot;
import test.xice.tsam.model.oa.cs.ServerRoot;
import test.xice.tsam.model.oa.filter.*;


/**
 * This is used to access all of the Root level Hubs.  This is so that they 
 * will not have to be passed into and through the models.
 * After client login, the Hubs will be shared with the Hubs in the ServerRoot object from the server.
 * @author vincevia
 * 
 * @see ClientController#initializeClientModel
 */
public class ModelDelegate extends ServerModelDelegate {
    
    private static Hub<AdminUser> hubLoginUser = new Hub<AdminUser>(AdminUser.class);
    
    /*$$Start: ModelDelegate1 $$*/
    private static final Hub<AdminUser> hubUsers = new Hub<AdminUser>(AdminUser.class);
    private static final Hub<Developer> hubDevelopers = new Hub<Developer>(Developer.class);
    private static final Hub<EnvironmentType> hubEnvironmentTypes = new Hub<EnvironmentType>(EnvironmentType.class);
    private static final Hub<ApplicationType> hubApplicationTypes = new Hub<ApplicationType>(ApplicationType.class);
    private static final Hub<IDL> hubIDLVersions = new Hub<IDL>(IDL.class);
    private static final Hub<SiloType> hubSiloTypes = new Hub<SiloType>(SiloType.class);
    private static final Hub<ApplicationStatus> hubServerStatuses = new Hub<ApplicationStatus>(ApplicationStatus.class);
    private static final Hub<Command> hubCommands = new Hub<Command>(Command.class);
    private static final Hub<Timezone> hubTimeZones = new Hub<Timezone>(Timezone.class);
    private static final Hub<PackageType> hubPackageTypes = new Hub<PackageType>(PackageType.class);
    private static final Hub<OperatingSystem> hubOperatingSystems = new Hub<OperatingSystem>(OperatingSystem.class);
    private static final Hub<ApplicationStatus> hubApplicationStatuses = new Hub<ApplicationStatus>(ApplicationStatus.class);
    private static final Hub<IDL> hubIDLs = new Hub<IDL>(IDL.class);
    private static final Hub<Site> hubSites = new Hub<Site>(Site.class);
    private static final Hub<Timezone> hubTimezones = new Hub<Timezone>(Timezone.class);
    /*$$End: ModelDelegate1 $$*/    

    private static volatile Silo defaultSilo;
    private static Hub<Silo> hubDefaultSilo = new Hub<Silo>(Silo.class);

    //  AO is the current logged in user
    public static Hub<AdminUser> getLoginUserHub() {
        return hubLoginUser;
    }
    
    public static AdminUser getLoginUser() {
        return getLoginUserHub().getAO();
    }
    public static void setLoginUser(AdminUser user) {
        if (!getLoginUserHub().contains(user)) {
            getLoginUserHub().add(user);
        }
        getLoginUserHub().setAO(user);
    }

    public static void initialize(ServerRoot rootServer, ClientRoot rootClient) {
        /*$$Start: ModelDelegate2 $$*/
        getUsers().setSharedHub(rootServer.getAdminUsers());
        getDevelopers().setSharedHub(rootServer.getDevelopers());
        getEnvironmentTypes().setSharedHub(rootServer.getEnvironmentTypes());
        getApplicationTypes().setSharedHub(rootServer.getApplicationTypes());
        getIDLVersions().setSharedHub(rootServer.getIDLs());
        getSiloTypes().setSharedHub(rootServer.getSiloTypes());
        getServerStatuses().setSharedHub(rootServer.getApplicationStatuses());
        getCommands().setSharedHub(rootServer.getCommands());
        getTimeZones().setSharedHub(rootServer.getTimezones());
        getPackageTypes().setSharedHub(rootServer.getPackageTypes());
        getOperatingSystems().setSharedHub(rootServer.getOperatingSystems());
        getApplicationStatuses().setSharedHub(rootServer.getApplicationStatuses());
        getIDLs().setSharedHub(rootServer.getIDLs());
        getSites().setSharedHub(rootServer.getSites());
        getTimezones().setSharedHub(rootServer.getTimezones());
        /*$$End: ModelDelegate2 $$*/ 
        setDefaultSilo(rootServer.getDefaultSilo()); 
    }

    /*$$Start: ModelDelegate3 $$*/
    public static Hub<AdminUser> getUsers() {
        return hubUsers;
    }
    public static Hub<Developer> getDevelopers() {
        return hubDevelopers;
    }
    public static Hub<EnvironmentType> getEnvironmentTypes() {
        return hubEnvironmentTypes;
    }
    public static Hub<ApplicationType> getApplicationTypes() {
        return hubApplicationTypes;
    }
    public static Hub<IDL> getIDLVersions() {
        return hubIDLVersions;
    }
    public static Hub<SiloType> getSiloTypes() {
        return hubSiloTypes;
    }
    public static Hub<ApplicationStatus> getServerStatuses() {
        return hubServerStatuses;
    }
    public static Hub<Command> getCommands() {
        return hubCommands;
    }
    public static Hub<Timezone> getTimeZones() {
        return hubTimeZones;
    }
    public static Hub<PackageType> getPackageTypes() {
        return hubPackageTypes;
    }
    public static Hub<OperatingSystem> getOperatingSystems() {
        return hubOperatingSystems;
    }
    public static Hub<ApplicationStatus> getApplicationStatuses() {
        return hubApplicationStatuses;
    }
    public static Hub<IDL> getIDLs() {
        return hubIDLs;
    }
    public static Hub<Site> getSites() {
        return hubSites;
    }
    public static Hub<Timezone> getTimezones() {
        return hubTimezones;
    }
    /*$$End: ModelDelegate3 $$*/    

    public static Silo getDefaultSilo() {
        return defaultSilo;
    }
    
    public static void setDefaultSilo(Silo newValue) {
        defaultSilo = newValue;
        if (newValue != null) {
            getDefaultSiloHub().add(newValue);
            getDefaultSiloHub().setAO(newValue);
        }
    }
    
    public static Hub<Silo> getDefaultSiloHub() {
        return hubDefaultSilo;
    }
    
    // helper methods
    
/*    
    private static Hub<MRADServer> hubMRADServer;
    public static Hub<MRADServer> getMRADServers() {
        if (hubMRADServer == null) {
            hubMRADServer = new Hub<MRADServer>(MRADServer.class);
            String pp = OAString.cpp(
                Site.PROPERTY_Environments, 
                Environment.PROPERTY_Silos, 
                Silo.PROPERTY_MRADServer);
            HubMerger hm = new HubMerger(getSites(), hubMRADServer, pp, true);
        }
        return hubMRADServer;
    }
    
    private static Hub<Server> hubServer;
    public static Hub<Server> getServers() {
        if (hubServer == null) {
            hubServer = new Hub<Server>(Server.class);
            String pp = OAString.cpp(
                Site.PROPERTY_Environments, 
                Environment.PROPERTY_Silos, 
                Silo.PROPERTY_Servers);
            HubMerger hm = new HubMerger(getSites(), hubServer, pp, true);
        }
        return hubServer;
    }
*/
    /*
    private static Hub<GSMRServer> hubGSMRServer;
    public static Hub<GSMRServer> getGSMRServers() {
        if (hubGSMRServer == null) {
            hubGSMRServer = new Hub<GSMRServer>(GSMRServer.class);
            String pp = OAString.cpp(
                Site.PROPERTY_Environments, 
                Environment.PROPERTY_Silos, 
                Silo.PROPERTY_GSMRServers);
            HubMerger hm = new HubMerger(getSites(), hubGSMRServer, pp, true);
        }
        return hubGSMRServer;
    }
    */
}



