// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package com.theice.tsac.delegate;

import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.*;
import com.viaoa.hub.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAString;


/**
 * This is used to access all of the Root level Hubs. 
 */
public class ModelDelegate {
    
    private static Hub<AdminUser> hubLoginUser;
    /*$$Start: ModelDelegate1 $$*/
    private static Hub<Site> hubTradingSystemSites;
    private static Hub<User> hubUserLogins;
    private static Hub<User> hubAllUsers;
    private static Hub<Company> hubCompanies;
    private static Hub<LLADServer> hubLLADServers;
    private static Hub<GSMRServer> hubGSMRServers;
    private static Hub<AdminUser> hubAdminUsers;
    private static Hub<RemoteClient> hubRemoteClients;
    private static Hub<ClientAppType> hubClientApplicationTypes;
    private static Hub<EnvironmentType> hubEnvironmentTypes;
    private static Hub<RCCommand> hubExecuteCommands;
    private static Hub<LoginType> hubLoginTypes;
    private static Hub<ServerStatus> hubServerStatuses;
    private static Hub<ServerType> hubServerTypes;
    private static Hub<SiloType> hubSiloTypes;
    private static Hub<Timezone> hubTimeZones;
    private static Hub<IDL> hubIDLVersions;
    private static Hub<ClientAppType> hubClientAppTypes;
    private static Hub<IDL> hubIDLS;
    private static Hub<RCCommand> hubRCCommands;
    private static Hub<RequestMethod> hubRequestMethods;
    private static Hub<Site> hubSites;
    private static Hub<Timezone> hubTimezones;
    /*$$End: ModelDelegate1 $$*/    

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


    /*$$Start: ModelDelegate3 $$*/
    public static Hub<Site> getTradingSystemSites() {
        if (hubTradingSystemSites == null) {
            hubTradingSystemSites = new Hub<Site>(Site.class);
        }
        return hubTradingSystemSites;
    }
    public static Hub<User> getUserLogins() {
        if (hubUserLogins == null) {
            hubUserLogins = new Hub<User>(User.class);
        }
        return hubUserLogins;
    }
    public static Hub<User> getAllUsers() {
        if (hubAllUsers == null) {
            hubAllUsers = new Hub<User>(User.class);
        }
        return hubAllUsers;
    }
    public static Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = new Hub<Company>(Company.class);
        }
        return hubCompanies;
    }
    public static Hub<LLADServer> getLLADServers() {
        if (hubLLADServers == null) {
            hubLLADServers = new Hub<LLADServer>(LLADServer.class);
        }
        return hubLLADServers;
    }
    public static Hub<GSMRServer> getGSMRServers() {
        if (hubGSMRServers == null) {
            hubGSMRServers = new Hub<GSMRServer>(GSMRServer.class);
        }
        return hubGSMRServers;
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
    public static Hub<ServerStatus> getServerStatuses() {
        if (hubServerStatuses == null) {
            hubServerStatuses = new Hub<ServerStatus>(ServerStatus.class);
        }
        return hubServerStatuses;
    }
    public static Hub<ServerType> getServerTypes() {
        if (hubServerTypes == null) {
            hubServerTypes = new Hub<ServerType>(ServerType.class);
        }
        return hubServerTypes;
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



