// Copied from OATemplate project by OABuilder 09/10/14 05:33 PM
package test.xice.tsac.model.oa.cs;


import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.SitePP;

/**
 * Root Object that is automatically updated between the Server and Clients.
 * ServerController will do the selects for these objects.
 * Model will share these hubs after the application is started.
 * */

@OAClass(
    useDataSource = false,
    displayProperty = "Id"
)
public class ServerRoot extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_ServerInfo = "ServerInfo";
    public static final String PROPERTY_AdminUsers = "AdminUsers";
    /*$$Start: ServerRoot1 $$*/
    public static final String P_Companies = "Companies";
    public static final String P_ApplicationStatuses = "ApplicationStatuses";
    public static final String P_ApplicationTypes = "ApplicationTypes";
    public static final String P_ClientAppTypes = "ClientAppTypes";
    public static final String P_EnvironmentTypes = "EnvironmentTypes";
    public static final String P_IDLS = "IDLS";
    public static final String P_LoginTypes = "LoginTypes";
    public static final String P_OperatingSystems = "OperatingSystems";
    public static final String P_PackageTypes = "PackageTypes";
    public static final String P_RCCommands = "RCCommands";
    public static final String P_RemoteClients = "RemoteClients";
    public static final String P_RequestMethods = "RequestMethods";
    public static final String P_SiloTypes = "SiloTypes";
    public static final String P_Sites = "Sites";
    public static final String P_Timezones = "Timezones";
    /*$$End: ServerRoot1 $$*/
    public static final String P_DefaultSilo = "DefaultSilo";

    protected int id;
    protected transient ServerInfo serverInfo;
    protected transient Hub<AdminUser> hubAdminUsers;
    protected transient Silo defaultSilo;
    
    /*$$Start: ServerRoot2 $$*/
    protected transient Hub<Company> hubCompanies;
    protected transient Hub<ApplicationStatus> hubApplicationStatuses;
    protected transient Hub<ApplicationType> hubApplicationTypes;
    protected transient Hub<ClientAppType> hubClientAppTypes;
    protected transient Hub<EnvironmentType> hubEnvironmentTypes;
    protected transient Hub<IDL> hubIDLS;
    protected transient Hub<LoginType> hubLoginTypes;
    protected transient Hub<OperatingSystem> hubOperatingSystems;
    protected transient Hub<PackageType> hubPackageTypes;
    protected transient Hub<RCCommand> hubRCCommands;
    protected transient Hub<RemoteClient> hubRemoteClients;
    protected transient Hub<RequestMethod> hubRequestMethods;
    protected transient Hub<SiloType> hubSiloTypes;
    protected transient Hub<Site> hubSites;
    protected transient Hub<Timezone> hubTimezones;
    /*$$End: ServerRoot2 $$*/
    

	public ServerRoot() {
		setId(777);
	}

    @OAProperty(displayName = "Id")
    @OAId
	public int getId() {
		return id;
	}
	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTY_Id, old, id);
	}

    @OAOne()
    public ServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = (ServerInfo) super.getObject(PROPERTY_ServerInfo);
            if (serverInfo == null) {
                ServerInfo si = new ServerInfo();
                setServerInfo(si);
            }
        }
        return serverInfo;
    }

    public void setServerInfo(ServerInfo newValue) {
        ServerInfo old = this.serverInfo;
        this.serverInfo = newValue;
        firePropertyChange(PROPERTY_ServerInfo, old, this.serverInfo);
    }

    @OAMany()
    public Hub<AdminUser> getAdminUsers() {
        if (hubAdminUsers == null) {
            hubAdminUsers = (Hub<AdminUser>) super.getHub(PROPERTY_AdminUsers);
        }
        return hubAdminUsers;
    }
    
    /*$$Start: ServerRoot3 $$*/
    @OAMany(toClass = Company.class, isCalculated = true)
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = (Hub<Company>) super.getHub(P_Companies);
            String pp = SitePP.environments().companies().pp;
            HubMerger hm = new HubMerger(this.getSites(), hubCompanies, pp, false, true);
        }
        return hubCompanies;
    }
    @OAMany(toClass = ApplicationStatus.class)
    public Hub<ApplicationStatus> getApplicationStatuses() {
        if (hubApplicationStatuses == null) {
            hubApplicationStatuses = (Hub<ApplicationStatus>) super.getHub(P_ApplicationStatuses);
        }
        return hubApplicationStatuses;
    }
    @OAMany(toClass = ApplicationType.class)
    public Hub<ApplicationType> getApplicationTypes() {
        if (hubApplicationTypes == null) {
            hubApplicationTypes = (Hub<ApplicationType>) super.getHub(P_ApplicationTypes);
        }
        return hubApplicationTypes;
    }
    @OAMany(toClass = ClientAppType.class)
    public Hub<ClientAppType> getClientAppTypes() {
        if (hubClientAppTypes == null) {
            hubClientAppTypes = (Hub<ClientAppType>) super.getHub(P_ClientAppTypes, ClientAppType.P_Seq, true);
        }
        return hubClientAppTypes;
    }
    @OAMany(toClass = EnvironmentType.class)
    public Hub<EnvironmentType> getEnvironmentTypes() {
        if (hubEnvironmentTypes == null) {
            hubEnvironmentTypes = (Hub<EnvironmentType>) super.getHub(P_EnvironmentTypes);
        }
        return hubEnvironmentTypes;
    }
    @OAMany(toClass = IDL.class)
    public Hub<IDL> getIDLS() {
        if (hubIDLS == null) {
            hubIDLS = (Hub<IDL>) super.getHub(P_IDLS, IDL.P_Seq, true);
        }
        return hubIDLS;
    }
    @OAMany(toClass = LoginType.class)
    public Hub<LoginType> getLoginTypes() {
        if (hubLoginTypes == null) {
            hubLoginTypes = (Hub<LoginType>) super.getHub(P_LoginTypes, LoginType.P_Seq, true);
        }
        return hubLoginTypes;
    }
    @OAMany(toClass = OperatingSystem.class)
    public Hub<OperatingSystem> getOperatingSystems() {
        if (hubOperatingSystems == null) {
            hubOperatingSystems = (Hub<OperatingSystem>) super.getHub(P_OperatingSystems);
        }
        return hubOperatingSystems;
    }
    @OAMany(toClass = PackageType.class)
    public Hub<PackageType> getPackageTypes() {
        if (hubPackageTypes == null) {
            hubPackageTypes = (Hub<PackageType>) super.getHub(P_PackageTypes);
        }
        return hubPackageTypes;
    }
    @OAMany(toClass = RCCommand.class)
    public Hub<RCCommand> getRCCommands() {
        if (hubRCCommands == null) {
            hubRCCommands = (Hub<RCCommand>) super.getHub(P_RCCommands);
        }
        return hubRCCommands;
    }
    @OAMany(toClass = RemoteClient.class)
    public Hub<RemoteClient> getRemoteClients() {
        if (hubRemoteClients == null) {
            hubRemoteClients = (Hub<RemoteClient>) super.getHub(P_RemoteClients);
        }
        return hubRemoteClients;
    }
    @OAMany(toClass = RequestMethod.class)
    public Hub<RequestMethod> getRequestMethods() {
        if (hubRequestMethods == null) {
            hubRequestMethods = (Hub<RequestMethod>) super.getHub(P_RequestMethods);
        }
        return hubRequestMethods;
    }
    @OAMany(toClass = SiloType.class)
    public Hub<SiloType> getSiloTypes() {
        if (hubSiloTypes == null) {
            hubSiloTypes = (Hub<SiloType>) super.getHub(P_SiloTypes);
        }
        return hubSiloTypes;
    }
    @OAMany(toClass = Site.class)
    public Hub<Site> getSites() {
        if (hubSites == null) {
            hubSites = (Hub<Site>) super.getHub(P_Sites);
        }
        return hubSites;
    }
    @OAMany(toClass = Timezone.class)
    public Hub<Timezone> getTimezones() {
        if (hubTimezones == null) {
            hubTimezones = (Hub<Timezone>) super.getHub(P_Timezones);
        }
        return hubTimezones;
    }
    /*$$End: ServerRoot3 $$*/

    @OAOne()
    public Silo getDefaultSilo() {
        if (defaultSilo == null) {
            defaultSilo = (Silo) getObject(P_DefaultSilo);
        }
        return defaultSilo;
    }
    
    public void setDefaultSilo(Silo newValue) {
        fireBeforePropertyChange(P_DefaultSilo, this.defaultSilo, newValue);
        Silo old = this.defaultSilo;
        this.defaultSilo = newValue;
        firePropertyChange(P_DefaultSilo, old, this.defaultSilo);
    }

}

