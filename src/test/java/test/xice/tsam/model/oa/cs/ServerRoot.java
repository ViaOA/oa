// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.model.oa.cs;

import test.xice.tsam.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

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
    public static final String PROPERTY_ApplicationStatuses = "ApplicationStatuses";
    public static final String P_ApplicationStatuses = "ApplicationStatuses";
    public static final String PROPERTY_ApplicationTypes = "ApplicationTypes";
    public static final String P_ApplicationTypes = "ApplicationTypes";
    public static final String PROPERTY_Commands = "Commands";
    public static final String P_Commands = "Commands";
    public static final String PROPERTY_Developers = "Developers";
    public static final String P_Developers = "Developers";
    public static final String PROPERTY_EnvironmentTypes = "EnvironmentTypes";
    public static final String P_EnvironmentTypes = "EnvironmentTypes";
    public static final String PROPERTY_IDLs = "IDLs";
    public static final String P_IDLs = "IDLs";
    public static final String PROPERTY_OperatingSystems = "OperatingSystems";
    public static final String P_OperatingSystems = "OperatingSystems";
    public static final String PROPERTY_PackageTypes = "PackageTypes";
    public static final String P_PackageTypes = "PackageTypes";
    public static final String PROPERTY_SiloTypes = "SiloTypes";
    public static final String P_SiloTypes = "SiloTypes";
    public static final String PROPERTY_Sites = "Sites";
    public static final String P_Sites = "Sites";
    public static final String PROPERTY_Timezones = "Timezones";
    public static final String P_Timezones = "Timezones";
    /*$$End: ServerRoot1 $$*/
    public static final String P_DefaultSilo = "DefaultSilo";

    protected int id;
    protected transient ServerInfo serverInfo;
    protected transient Hub<AdminUser> hubAdminUsers;
    protected transient Silo defaultSilo;
    
    /*$$Start: ServerRoot2 $$*/
    protected transient Hub<ApplicationStatus> hubApplicationStatuses;
    protected transient Hub<ApplicationType> hubApplicationTypes;
    protected transient Hub<Command> hubCommands;
    protected transient Hub<Developer> hubDevelopers;
    protected transient Hub<EnvironmentType> hubEnvironmentTypes;
    protected transient Hub<IDL> hubIDLs;
    protected transient Hub<OperatingSystem> hubOperatingSystems;
    protected transient Hub<PackageType> hubPackageTypes;
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
    @OAMany(toClass = Command.class)
    public Hub<Command> getCommands() {
        if (hubCommands == null) {
            hubCommands = (Hub<Command>) super.getHub(P_Commands);
        }
        return hubCommands;
    }
    @OAMany(toClass = Developer.class)
    public Hub<Developer> getDevelopers() {
        if (hubDevelopers == null) {
            hubDevelopers = (Hub<Developer>) super.getHub(P_Developers);
        }
        return hubDevelopers;
    }
    @OAMany(toClass = EnvironmentType.class)
    public Hub<EnvironmentType> getEnvironmentTypes() {
        if (hubEnvironmentTypes == null) {
            hubEnvironmentTypes = (Hub<EnvironmentType>) super.getHub(P_EnvironmentTypes);
        }
        return hubEnvironmentTypes;
    }
    @OAMany(toClass = IDL.class)
    public Hub<IDL> getIDLs() {
        if (hubIDLs == null) {
            hubIDLs = (Hub<IDL>) super.getHub(P_IDLs, IDL.P_Seq, true);
        }
        return hubIDLs;
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

