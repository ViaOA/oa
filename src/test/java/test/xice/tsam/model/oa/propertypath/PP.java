// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.model.oa.propertypath;

import test.xice.tsam.model.oa.propertypath.AdminUserCategoryPPx;
import test.xice.tsam.model.oa.propertypath.AdminUserPPx;
import test.xice.tsam.model.oa.propertypath.ApplicationGroupPPx;
import test.xice.tsam.model.oa.propertypath.ApplicationPPx;
import test.xice.tsam.model.oa.propertypath.ApplicationStatusPPx;
import test.xice.tsam.model.oa.propertypath.ApplicationTypeCommandPPx;
import test.xice.tsam.model.oa.propertypath.ApplicationTypePPx;
import test.xice.tsam.model.oa.propertypath.ApplicationVersionPPx;
import test.xice.tsam.model.oa.propertypath.CommandPPx;
import test.xice.tsam.model.oa.propertypath.DeveloperPPx;
import test.xice.tsam.model.oa.propertypath.EnvironmentPPx;
import test.xice.tsam.model.oa.propertypath.EnvironmentTypePPx;
import test.xice.tsam.model.oa.propertypath.HostInfoPPx;
import test.xice.tsam.model.oa.propertypath.IDLPPx;
import test.xice.tsam.model.oa.propertypath.MRADClientCommandPPx;
import test.xice.tsam.model.oa.propertypath.MRADClientMessagePPx;
import test.xice.tsam.model.oa.propertypath.MRADClientPPx;
import test.xice.tsam.model.oa.propertypath.MRADServerCommandPPx;
import test.xice.tsam.model.oa.propertypath.MRADServerPPx;
import test.xice.tsam.model.oa.propertypath.OSVersionPPx;
import test.xice.tsam.model.oa.propertypath.OperatingSystemPPx;
import test.xice.tsam.model.oa.propertypath.PackageTypePPx;
import test.xice.tsam.model.oa.propertypath.PackageVersionPPx;
import test.xice.tsam.model.oa.propertypath.SSHExecutePPx;
import test.xice.tsam.model.oa.propertypath.ServerPPx;
import test.xice.tsam.model.oa.propertypath.SiloConfigPPx;
import test.xice.tsam.model.oa.propertypath.SiloConfigVersioinPPx;
import test.xice.tsam.model.oa.propertypath.SiloPPx;
import test.xice.tsam.model.oa.propertypath.SiloTypePPx;
import test.xice.tsam.model.oa.propertypath.SitePPx;
import test.xice.tsam.model.oa.propertypath.TimezonePPx;

/**
 * Used to build compiler safe property paths.
 * @author vvia
 */
public class PP {
    /*$$Start: PPInterface.code $$*/
    public static AdminUserPPx adminUser() {
        return new AdminUserPPx("AdminUser");
    }
    public static AdminUserPPx adminUsers() {
        return new AdminUserPPx("AdminUsers");
    }
    public static AdminUserCategoryPPx adminUserCategory() {
        return new AdminUserCategoryPPx("AdminUserCategory");
    }
    public static AdminUserCategoryPPx adminUserCategories() {
        return new AdminUserCategoryPPx("AdminUserCategories");
    }
    public static ApplicationPPx application() {
        return new ApplicationPPx("Application");
    }
    public static ApplicationPPx applications() {
        return new ApplicationPPx("Applications");
    }
    public static ApplicationGroupPPx applicationGroup() {
        return new ApplicationGroupPPx("ApplicationGroup");
    }
    public static ApplicationGroupPPx applicationGroups() {
        return new ApplicationGroupPPx("ApplicationGroups");
    }
    public static ApplicationStatusPPx applicationStatus() {
        return new ApplicationStatusPPx("ApplicationStatus");
    }
    public static ApplicationStatusPPx applicationStatuses() {
        return new ApplicationStatusPPx("ApplicationStatuses");
    }
    public static ApplicationTypePPx applicationType() {
        return new ApplicationTypePPx("ApplicationType");
    }
    public static ApplicationTypePPx applicationTypes() {
        return new ApplicationTypePPx("ApplicationTypes");
    }
    public static ApplicationTypeCommandPPx applicationTypeCommand() {
        return new ApplicationTypeCommandPPx("ApplicationTypeCommand");
    }
    public static ApplicationTypeCommandPPx applicationTypeCommands() {
        return new ApplicationTypeCommandPPx("ApplicationTypeCommands");
    }
    public static ApplicationVersionPPx applicationVersion() {
        return new ApplicationVersionPPx("ApplicationVersion");
    }
    public static ApplicationVersionPPx applicationVersions() {
        return new ApplicationVersionPPx("ApplicationVersions");
    }
    public static CommandPPx command() {
        return new CommandPPx("Command");
    }
    public static CommandPPx commands() {
        return new CommandPPx("Commands");
    }
    public static DeveloperPPx developer() {
        return new DeveloperPPx("Developer");
    }
    public static DeveloperPPx developers() {
        return new DeveloperPPx("Developers");
    }
    public static EnvironmentPPx environment() {
        return new EnvironmentPPx("Environment");
    }
    public static EnvironmentPPx environments() {
        return new EnvironmentPPx("Environments");
    }
    public static EnvironmentTypePPx environmentType() {
        return new EnvironmentTypePPx("EnvironmentType");
    }
    public static EnvironmentTypePPx environmentTypes() {
        return new EnvironmentTypePPx("EnvironmentTypes");
    }
    public static HostInfoPPx hostInfo() {
        return new HostInfoPPx("HostInfo");
    }
    public static HostInfoPPx hostInfos() {
        return new HostInfoPPx("HostInfos");
    }
    public static IDLPPx idL() {
        return new IDLPPx("IDL");
    }
    public static IDLPPx idLs() {
        return new IDLPPx("IDLs");
    }
    public static MRADClientPPx mradClient() {
        return new MRADClientPPx("MRADClient");
    }
    public static MRADClientPPx mradClients() {
        return new MRADClientPPx("MRADClients");
    }
    public static MRADClientCommandPPx mradClientCommand() {
        return new MRADClientCommandPPx("MRADClientCommand");
    }
    public static MRADClientCommandPPx mradClientCommands() {
        return new MRADClientCommandPPx("MRADClientCommands");
    }
    public static MRADClientMessagePPx mradClientMessage() {
        return new MRADClientMessagePPx("MRADClientMessage");
    }
    public static MRADClientMessagePPx mradClientMessages() {
        return new MRADClientMessagePPx("MRADClientMessages");
    }
    public static MRADServerPPx mradServer() {
        return new MRADServerPPx("MRADServer");
    }
    public static MRADServerPPx mradServers() {
        return new MRADServerPPx("MRADServers");
    }
    public static MRADServerCommandPPx mradServerCommand() {
        return new MRADServerCommandPPx("MRADServerCommand");
    }
    public static MRADServerCommandPPx mradServerCommands() {
        return new MRADServerCommandPPx("MRADServerCommands");
    }
    public static OperatingSystemPPx operatingSystem() {
        return new OperatingSystemPPx("OperatingSystem");
    }
    public static OperatingSystemPPx operatingSystems() {
        return new OperatingSystemPPx("OperatingSystems");
    }
    public static OSVersionPPx osVersion() {
        return new OSVersionPPx("OSVersion");
    }
    public static OSVersionPPx osVersions() {
        return new OSVersionPPx("OSVersions");
    }
    public static PackageTypePPx packageType() {
        return new PackageTypePPx("PackageType");
    }
    public static PackageTypePPx packageTypes() {
        return new PackageTypePPx("PackageTypes");
    }
    public static PackageVersionPPx packageVersion() {
        return new PackageVersionPPx("PackageVersion");
    }
    public static PackageVersionPPx packageVersions() {
        return new PackageVersionPPx("PackageVersions");
    }
    public static ServerPPx server() {
        return new ServerPPx("Server");
    }
    public static ServerPPx servers() {
        return new ServerPPx("Servers");
    }
    public static SiloPPx silo() {
        return new SiloPPx("Silo");
    }
    public static SiloPPx silos() {
        return new SiloPPx("Silos");
    }
    public static SiloConfigPPx siloConfig() {
        return new SiloConfigPPx("SiloConfig");
    }
    public static SiloConfigPPx siloConfigs() {
        return new SiloConfigPPx("SiloConfigs");
    }
    public static SiloConfigVersioinPPx siloConfigVersioin() {
        return new SiloConfigVersioinPPx("SiloConfigVersioin");
    }
    public static SiloConfigVersioinPPx siloConfigVersioins() {
        return new SiloConfigVersioinPPx("SiloConfigVersioins");
    }
    public static SiloTypePPx siloType() {
        return new SiloTypePPx("SiloType");
    }
    public static SiloTypePPx siloTypes() {
        return new SiloTypePPx("SiloTypes");
    }
    public static SitePPx site() {
        return new SitePPx("Site");
    }
    public static SitePPx sites() {
        return new SitePPx("Sites");
    }
    public static SSHExecutePPx sshExecute() {
        return new SSHExecutePPx("SSHExecute");
    }
    public static SSHExecutePPx sshExecutes() {
        return new SSHExecutePPx("SSHExecutes");
    }
    public static TimezonePPx timezone() {
        return new TimezonePPx("Timezone");
    }
    public static TimezonePPx timezones() {
        return new TimezonePPx("Timezones");
    }
    /*$$End: PPInterface.code $$*/
}
