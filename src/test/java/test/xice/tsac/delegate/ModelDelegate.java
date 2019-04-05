// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsac.delegate;


import com.viaoa.annotation.OAOne;
import com.viaoa.hub.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAString;

import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.cs.ClientRoot;
import test.xice.tsac.model.oa.cs.ServerRoot;
import test.xice.tsac.model.oa.filter.*;


/**
 * This is used to access all of the Root level Hubs.  This is so that they 
 * will not have to be passed into and through the models.
 * After client login, the Hubs will be shared with the Hubs in the ServerRoot object from the server.
 * @author vincevia
 * 
 * @see ClientController#initializeClientModel
 */
public class ModelDelegate extends ServerModelDelegate {
    
    private static Hub<AdminUser> hubLoginUser;
    /*$$Start: ModelDelegate1 $$*/
    private static Hub<Site> hubTradingSystemSites;
    private static Hub<Company> hubCompanies;
    private static Hub<AdminUser> hubAdminUsers;
    private static Hub<RemoteClient> hubRemoteClients;
    private static Hub<ClientAppType> hubClientApplicationTypes;
    private static Hub<EnvironmentType> hubEnvironmentTypes;
    private static Hub<RCCommand> hubExecuteCommands;
    private static Hub<LoginType> hubLoginTypes;
    private static Hub<ApplicationStatus> hubServerStatuses;
    private static Hub<ApplicationType> hubApplicationTypes;
    private static Hub<SiloType> hubSiloTypes;
    private static Hub<Timezone> hubTimeZones;
    private static Hub<IDL> hubIDLVersions;
    private static Hub<PackageType> hubPackageTypes;
    private static Hub<ApplicationStatus> hubApplicationStatuses;
    private static Hub<ClientAppType> hubClientAppTypes;
    private static Hub<IDL> hubIDLS;
    private static Hub<OperatingSystem> hubOperatingSystems;
    private static Hub<RCCommand> hubRCCommands;
    private static Hub<RequestMethod> hubRequestMethods;
    private static Hub<Site> hubSites;
    private static Hub<Timezone> hubTimezones;
    /*$$End: ModelDelegate1 $$*/    
    private static Silo defaultSilo;

    //  AO is the current logged in user
    public static Hub<AdminUser> getLoginUserHub() {
        if (hubLoginUser == null) {
            hubLoginUser = new Hub<AdminUser>(AdminUser.class);
        }
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
        getTradingSystemSites().setSharedHub(rootServer.getSites());
        getCompanies().setSharedHub(rootServer.getCompanies());
        getAdminUsers().setSharedHub(rootServer.getAdminUsers());
        getRemoteClients().setSharedHub(rootServer.getRemoteClients());
        getClientApplicationTypes().setSharedHub(rootServer.getClientAppTypes());
        getEnvironmentTypes().setSharedHub(rootServer.getEnvironmentTypes());
        getExecuteCommands().setSharedHub(rootServer.getRCCommands());
        getLoginTypes().setSharedHub(rootServer.getLoginTypes());
        getServerStatuses().setSharedHub(rootServer.getApplicationStatuses());
        getApplicationTypes().setSharedHub(rootServer.getApplicationTypes());
        getSiloTypes().setSharedHub(rootServer.getSiloTypes());
        getTimeZones().setSharedHub(rootServer.getTimezones());
        getIDLVersions().setSharedHub(rootServer.getIDLS());
        getPackageTypes().setSharedHub(rootServer.getPackageTypes());
        getApplicationStatuses().setSharedHub(rootServer.getApplicationStatuses());
        getClientAppTypes().setSharedHub(rootServer.getClientAppTypes());
        getIDLS().setSharedHub(rootServer.getIDLS());
        getOperatingSystems().setSharedHub(rootServer.getOperatingSystems());
        getRCCommands().setSharedHub(rootServer.getRCCommands());
        getRequestMethods().setSharedHub(rootServer.getRequestMethods());
        getSites().setSharedHub(rootServer.getSites());
        getTimezones().setSharedHub(rootServer.getTimezones());
        /*$$End: ModelDelegate2 $$*/ 
        
        setDefaultSilo(rootServer.getDefaultSilo()); 
    }

    /*$$Start: ModelDelegate3 $$*/
    public static Hub<Site> getTradingSystemSites() {
        if (hubTradingSystemSites == null) {
            hubTradingSystemSites = new Hub<Site>(Site.class);
        }
        return hubTradingSystemSites;
    }
    public static Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = new Hub<Company>(Company.class);
        }
        return hubCompanies;
    }
    public static Hub<AdminUser> getAdminUsers() {
        if (hubAdminUsers == null) {
            hubAdminUsers = new Hub<AdminUser>(AdminUser.class);
        }
        return hubAdminUsers;
    }
    public static Hub<RemoteClient> getRemoteClients() {
        if (hubRemoteClients == null) {
            hubRemoteClients = new Hub<RemoteClient>(RemoteClient.class);
        }
        return hubRemoteClients;
    }
    public static Hub<ClientAppType> getClientApplicationTypes() {
        if (hubClientApplicationTypes == null) {
            hubClientApplicationTypes = new Hub<ClientAppType>(ClientAppType.class);
        }
        return hubClientApplicationTypes;
    }
    public static Hub<EnvironmentType> getEnvironmentTypes() {
        if (hubEnvironmentTypes == null) {
            hubEnvironmentTypes = new Hub<EnvironmentType>(EnvironmentType.class);
        }
        return hubEnvironmentTypes;
    }
    public static Hub<RCCommand> getExecuteCommands() {
        if (hubExecuteCommands == null) {
            hubExecuteCommands = new Hub<RCCommand>(RCCommand.class);
        }
        return hubExecuteCommands;
    }
    public static Hub<LoginType> getLoginTypes() {
        if (hubLoginTypes == null) {
            hubLoginTypes = new Hub<LoginType>(LoginType.class);
        }
        return hubLoginTypes;
    }
    public static Hub<ApplicationStatus> getServerStatuses() {
        if (hubServerStatuses == null) {
            hubServerStatuses = new Hub<ApplicationStatus>(ApplicationStatus.class);
        }
        return hubServerStatuses;
    }
    public static Hub<ApplicationType> getApplicationTypes() {
        if (hubApplicationTypes == null) {
            hubApplicationTypes = new Hub<ApplicationType>(ApplicationType.class);
        }
        return hubApplicationTypes;
    }
    public static Hub<SiloType> getSiloTypes() {
        if (hubSiloTypes == null) {
            hubSiloTypes = new Hub<SiloType>(SiloType.class);
        }
        return hubSiloTypes;
    }
    public static Hub<Timezone> getTimeZones() {
        if (hubTimeZones == null) {
            hubTimeZones = new Hub<Timezone>(Timezone.class);
        }
        return hubTimeZones;
    }
    public static Hub<IDL> getIDLVersions() {
        if (hubIDLVersions == null) {
            hubIDLVersions = new Hub<IDL>(IDL.class);
        }
        return hubIDLVersions;
    }
    public static Hub<PackageType> getPackageTypes() {
        if (hubPackageTypes == null) {
            hubPackageTypes = new Hub<PackageType>(PackageType.class);
        }
        return hubPackageTypes;
    }
    public static Hub<ApplicationStatus> getApplicationStatuses() {
        if (hubApplicationStatuses == null) {
            hubApplicationStatuses = new Hub<ApplicationStatus>(ApplicationStatus.class);
        }
        return hubApplicationStatuses;
    }
    public static Hub<ClientAppType> getClientAppTypes() {
        if (hubClientAppTypes == null) {
            hubClientAppTypes = new Hub<ClientAppType>(ClientAppType.class);
        }
        return hubClientAppTypes;
    }
    public static Hub<IDL> getIDLS() {
        if (hubIDLS == null) {
            hubIDLS = new Hub<IDL>(IDL.class);
        }
        return hubIDLS;
    }
    public static Hub<OperatingSystem> getOperatingSystems() {
        if (hubOperatingSystems == null) {
            hubOperatingSystems = new Hub<OperatingSystem>(OperatingSystem.class);
        }
        return hubOperatingSystems;
    }
    public static Hub<RCCommand> getRCCommands() {
        if (hubRCCommands == null) {
            hubRCCommands = new Hub<RCCommand>(RCCommand.class);
        }
        return hubRCCommands;
    }
    public static Hub<RequestMethod> getRequestMethods() {
        if (hubRequestMethods == null) {
            hubRequestMethods = new Hub<RequestMethod>(RequestMethod.class);
        }
        return hubRequestMethods;
    }
    public static Hub<Site> getSites() {
        if (hubSites == null) {
            hubSites = new Hub<Site>(Site.class);
        }
        return hubSites;
    }
    public static Hub<Timezone> getTimezones() {
        if (hubTimezones == null) {
            hubTimezones = new Hub<Timezone>(Timezone.class);
        }
        return hubTimezones;
    }
    /*$$End: ModelDelegate3 $$*/    

    public static Silo getDefaultSilo() {
        return defaultSilo;
    }
    
    public static void setDefaultSilo(Silo newValue) {
        defaultSilo = newValue;
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



