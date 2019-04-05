// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsac.model;

import com.viaoa.hub.*;

import test.xice.tsac.model.oa.*;

/**
 * This is used to access all of the Root level Hubs. 
 */
public class Model {
    
    private Hub<AdminUser> hubLoginUser;
    private Hub<Site> hubTradingSystemSites;
    private Hub<User> hubUserLogins;
    private Hub<User> hubAllUsers;
    private Hub<Company> hubCompanies;
    private Hub<LLADServer> hubLLADServers;
    private Hub<GSMRServer> hubGSMRServers;
    private Hub<AdminUser> hubAdminUsers;
    private Hub<RemoteClient> hubRemoteClients;
    private Hub<ClientAppType> hubClientApplicationTypes;
    private Hub<EnvironmentType> hubEnvironmentTypes;
    private Hub<RCCommand> hubExecuteCommands;
    private Hub<LoginType> hubLoginTypes;
    private Hub<ApplicationStatus> hubApplicationStatuses;
    private Hub<ApplicationType> hubApplicationTypes;
    private Hub<SiloType> hubSiloTypes;
    private Hub<Timezone> hubTimeZones;
    private Hub<IDL> hubIDLVersions;
    private Hub<ClientAppType> hubClientAppTypes;
    private Hub<IDL> hubIDLS;
    private Hub<RCCommand> hubRCCommands;
    private Hub<RequestMethod> hubRequestMethods;
    private Hub<Site> hubSites;
    private Hub<Timezone> hubTimezones;

    //  AO is the current logged in user
    public Hub<AdminUser> getLoginUserHub() {
        if (hubLoginUser == null) {
            hubLoginUser = new Hub<AdminUser>(AdminUser.class);
        }
        return hubLoginUser;
    }
    
    public AdminUser getLoginUser() {
        return getLoginUserHub().getAO();
    }
    public void setLoginUser(AdminUser user) {
        if (!getLoginUserHub().contains(user)) {
            getLoginUserHub().add(user);
        }
        getLoginUserHub().setAO(user);
    }


    public Hub<Site> getTradingSystemSites() {
        if (hubTradingSystemSites == null) {
            hubTradingSystemSites = new Hub<Site>(Site.class);
        }
        return hubTradingSystemSites;
    }
    public Hub<User> getUserLogins() {
        if (hubUserLogins == null) {
            hubUserLogins = new Hub<User>(User.class);
        }
        return hubUserLogins;
    }
    public Hub<User> getAllUsers() {
        if (hubAllUsers == null) {
            hubAllUsers = new Hub<User>(User.class);
        }
        return hubAllUsers;
    }
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = new Hub<Company>(Company.class);
        }
        return hubCompanies;
    }
    public Hub<LLADServer> getLLADServers() {
        if (hubLLADServers == null) {
            hubLLADServers = new Hub<LLADServer>(LLADServer.class);
        }
        return hubLLADServers;
    }
    public Hub<GSMRServer> getGSMRServers() {
        if (hubGSMRServers == null) {
            hubGSMRServers = new Hub<GSMRServer>(GSMRServer.class);
        }
        return hubGSMRServers;
    }
    public Hub<AdminUser> getAdminUsers() {
        if (hubAdminUsers == null) {
            hubAdminUsers = new Hub<AdminUser>(AdminUser.class);
        }
        return hubAdminUsers;
    }
    public Hub<RemoteClient> getRemoteClients() {
        if (hubRemoteClients == null) {
            hubRemoteClients = new Hub<RemoteClient>(RemoteClient.class);
        }
        return hubRemoteClients;
    }
    public Hub<ClientAppType> getClientApplicationTypes() {
        if (hubClientApplicationTypes == null) {
            hubClientApplicationTypes = new Hub<ClientAppType>(ClientAppType.class);
        }
        return hubClientApplicationTypes;
    }
    public Hub<EnvironmentType> getEnvironmentTypes() {
        if (hubEnvironmentTypes == null) {
            hubEnvironmentTypes = new Hub<EnvironmentType>(EnvironmentType.class);
        }
        return hubEnvironmentTypes;
    }
    public Hub<RCCommand> getExecuteCommands() {
        if (hubExecuteCommands == null) {
            hubExecuteCommands = new Hub<RCCommand>(RCCommand.class);
        }
        return hubExecuteCommands;
    }
    public Hub<LoginType> getLoginTypes() {
        if (hubLoginTypes == null) {
            hubLoginTypes = new Hub<LoginType>(LoginType.class);
        }
        return hubLoginTypes;
    }
    public Hub<ApplicationStatus> getApplicationStatuses() {
        if (hubApplicationStatuses == null) {
            hubApplicationStatuses = new Hub<ApplicationStatus>(ApplicationStatus.class);
        }
        return hubApplicationStatuses;
    }
    public Hub<ApplicationType> getApplicationTypes() {
        if (hubApplicationTypes == null) {
            hubApplicationTypes = new Hub<ApplicationType>(ApplicationType.class);
        }
        return hubApplicationTypes;
    }
    public Hub<SiloType> getSiloTypes() {
        if (hubSiloTypes == null) {
            hubSiloTypes = new Hub<SiloType>(SiloType.class);
        }
        return hubSiloTypes;
    }
    public Hub<Timezone> getTimeZones() {
        if (hubTimeZones == null) {
            hubTimeZones = new Hub<Timezone>(Timezone.class);
        }
        return hubTimeZones;
    }
    public Hub<IDL> getIDLVersions() {
        if (hubIDLVersions == null) {
            hubIDLVersions = new Hub<IDL>(IDL.class);
        }
        return hubIDLVersions;
    }
    public Hub<ClientAppType> getClientAppTypes() {
        if (hubClientAppTypes == null) {
            hubClientAppTypes = new Hub<ClientAppType>(ClientAppType.class);
        }
        return hubClientAppTypes;
    }
    public Hub<IDL> getIDLS() {
        if (hubIDLS == null) {
            hubIDLS = new Hub<IDL>(IDL.class);
        }
        return hubIDLS;
    }
    public Hub<RCCommand> getRCCommands() {
        if (hubRCCommands == null) {
            hubRCCommands = new Hub<RCCommand>(RCCommand.class);
        }
        return hubRCCommands;
    }
    public Hub<RequestMethod> getRequestMethods() {
        if (hubRequestMethods == null) {
            hubRequestMethods = new Hub<RequestMethod>(RequestMethod.class);
        }
        return hubRequestMethods;
    }
    public Hub<Site> getSites() {
        if (hubSites == null) {
            hubSites = new Hub<Site>(Site.class);
        }
        return hubSites;
    }
    public Hub<Timezone> getTimezones() {
        if (hubTimezones == null) {
            hubTimezones = new Hub<Timezone>(Timezone.class);
        }
        return hubTimezones;
    }

}



