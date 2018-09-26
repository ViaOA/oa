// Generated by OABuilder
package test.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.theice.tsac.delegate.oa.*;
import test.theice.tsac.model.oa.filter.*;
import test.theice.tsac.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "env",
    displayName = "Environment",
    displayProperty = "name",
    rootTreePropertyPaths = {
        "[Site]."+Site.P_Environments
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "EnvironmentIdL", columns = { @OAIndexColumn(name = "IdLId") }), 
        @OAIndex(name = "EnvironmentSite", columns = { @OAIndexColumn(name = "SiteId") })
    }
)
public class Environment extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_AbbrevName = "AbbrevName";
    public static final String P_AbbrevName = "AbbrevName";
    public static final String PROPERTY_TEAbbrevName = "TEAbbrevName";
    public static final String P_TEAbbrevName = "TEAbbrevName";
    public static final String PROPERTY_UsesDNS = "UsesDNS";
    public static final String P_UsesDNS = "UsesDNS";
    public static final String PROPERTY_UsesFirewall = "UsesFirewall";
    public static final String P_UsesFirewall = "UsesFirewall";
    public static final String PROPERTY_UsesVip = "UsesVip";
    public static final String P_UsesVip = "UsesVip";
     
     
    public static final String PROPERTY_Companies = "Companies";
    public static final String P_Companies = "Companies";
    public static final String PROPERTY_EnvironmentType = "EnvironmentType";
    public static final String P_EnvironmentType = "EnvironmentType";
    public static final String PROPERTY_IDL = "IDL";
    public static final String P_IDL = "IDL";
    public static final String PROPERTY_MarketTypes = "MarketTypes";
    public static final String P_MarketTypes = "MarketTypes";
    public static final String PROPERTY_MRADServer = "MRADServer";
    public static final String P_MRADServer = "MRADServer";
    public static final String PROPERTY_RCDeploy = "RCDeploy";
    public static final String P_RCDeploy = "RCDeploy";
    public static final String PROPERTY_RCInstalledVersions = "RCInstalledVersions";
    public static final String P_RCInstalledVersions = "RCInstalledVersions";
    public static final String PROPERTY_RCPackageLists = "RCPackageLists";
    public static final String P_RCPackageLists = "RCPackageLists";
    public static final String PROPERTY_RCRepoVersions = "RCRepoVersions";
    public static final String P_RCRepoVersions = "RCRepoVersions";
    public static final String PROPERTY_RCServerLists = "RCServerLists";
    public static final String P_RCServerLists = "RCServerLists";
    public static final String PROPERTY_RCServiceLists = "RCServiceLists";
    public static final String P_RCServiceLists = "RCServiceLists";
    public static final String PROPERTY_Silos = "Silos";
    public static final String P_Silos = "Silos";
    public static final String PROPERTY_Site = "Site";
    public static final String P_Site = "Site";
     
    protected int id;
    protected String name;
    protected String abbrevName;
    protected String teAbbrevName;
    protected boolean usesDNS;
    protected boolean usesFirewall;
    protected boolean usesVip;
     
    // Links to other objects.
    protected transient Hub<Company> hubCompanies;
    protected transient EnvironmentType environmentType;
    protected transient IDL idL;
    protected transient Hub<MarketType> hubMarketTypes;
    protected transient MRADServer mradServer;
    protected transient RCDeploy rcDeploy;
    protected transient Hub<RCInstalledVersion> hubRCInstalledVersions;
    protected transient Hub<RCPackageList> hubRCPackageLists;
    protected transient Hub<RCRepoVersion> hubRCRepoVersions;
    protected transient Hub<RCServerList> hubRCServerLists;
    protected transient Hub<RCServiceList> hubRCServiceLists;
    // protected transient Hub<Silo> hubSilos;
    protected transient Site site;
     
    public Environment() {
        if (!isLoading()) {
            setMRADServer(new MRADServer());
            setRCDeploy(new RCDeploy());
        }
    }
     
    public Environment(int id) {
        this();
        setId(id);
    }
     
    @OACalculatedProperty(properties = {P_AbbrevName, P_Name})
    public String getDisplayedName() {
        String s = OAString.notNull(getAbbrevName());
        s = OAString.concat(s, getName());
        return s;
    }
    
    
    @OAProperty(isUnique = true, displayLength = 5, isProcessed = true)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(maxLength = 35, displayLength = 20, columnLength = 15)
    @OAColumn(maxLength = 35)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayName = "Abbrev Name", maxLength = 32, displayLength = 9, columnLength = 6)
    @OAColumn(maxLength = 32)
    public String getAbbrevName() {
        return abbrevName;
    }
    
    public void setAbbrevName(String newValue) {
        fireBeforePropertyChange(P_AbbrevName, this.abbrevName, newValue);
        String old = abbrevName;
        this.abbrevName = newValue;
        firePropertyChange(P_AbbrevName, old, this.abbrevName);
    }
    @OAProperty(displayName = "TE Abbrev Name", maxLength = 32, displayLength = 32, columnLength = 6)
    @OAColumn(name = "AbbrevName", maxLength = 32)
    public String getTEAbbrevName() {
        if (OAString.isEmpty(teAbbrevName)) {
            //teAbbrevName = EnvironmentTypeDelegate.getDefaultTEAbbrevName(getEnvironmentType());
        }
        return teAbbrevName;
    }
    
    public void setTEAbbrevName(String newValue) {
        fireBeforePropertyChange(P_TEAbbrevName, this.teAbbrevName, newValue);
        String old = teAbbrevName;
        this.teAbbrevName = newValue;
        firePropertyChange(P_TEAbbrevName, old, this.teAbbrevName);
    }
    @OAProperty(displayName = "Uses DNS", displayLength = 5)
    @OAColumn(name = "HasDNS", sqlType = java.sql.Types.BOOLEAN)
    public boolean getUsesDNS() {
        return usesDNS;
    }
    
    public void setUsesDNS(boolean newValue) {
        fireBeforePropertyChange(P_UsesDNS, this.usesDNS, newValue);
        boolean old = usesDNS;
        this.usesDNS = newValue;
        firePropertyChange(P_UsesDNS, old, this.usesDNS);
    }
    @OAProperty(displayName = "Uses Firewall", displayLength = 5)
    @OAColumn(name = "HasFirewall", sqlType = java.sql.Types.BOOLEAN)
    public boolean getUsesFirewall() {
        return usesFirewall;
    }
    
    public void setUsesFirewall(boolean newValue) {
        fireBeforePropertyChange(P_UsesFirewall, this.usesFirewall, newValue);
        boolean old = usesFirewall;
        this.usesFirewall = newValue;
        firePropertyChange(P_UsesFirewall, old, this.usesFirewall);
    }
    @OAProperty(displayName = "Uses VIP", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getUsesVip() {
        return usesVip;
    }
    
    public void setUsesVip(boolean newValue) {
        fireBeforePropertyChange(P_UsesVip, this.usesVip, newValue);
        boolean old = usesVip;
        this.usesVip = newValue;
        firePropertyChange(P_UsesVip, old, this.usesVip);
    }
    @OAMany(
        toClass = Company.class, 
        owner = true, 
        reverseName = Company.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = (Hub<Company>) getHub(P_Companies);
        }
        return hubCompanies;
    }
    
    @OAOne(
        displayName = "Environment Type", 
        reverseName = EnvironmentType.P_Environments, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"EnvironmentTypeId"})
    public EnvironmentType getEnvironmentType() {
        if (environmentType == null) {
            environmentType = (EnvironmentType) getObject(P_EnvironmentType);
        }
        return environmentType;
    }
    
    public void setEnvironmentType(EnvironmentType newValue) {
        fireBeforePropertyChange(P_EnvironmentType, this.environmentType, newValue);
        EnvironmentType old = this.environmentType;
        this.environmentType = newValue;
        firePropertyChange(P_EnvironmentType, old, this.environmentType);
    }
    
    @OAOne(
        reverseName = IDL.P_Environments, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"IdLId"})
    public IDL getIDL() {
        if (idL == null) {
            idL = (IDL) getObject(P_IDL);
        }
        return idL;
    }
    
    public void setIDL(IDL newValue) {
        fireBeforePropertyChange(P_IDL, this.idL, newValue);
        IDL old = this.idL;
        this.idL = newValue;
        firePropertyChange(P_IDL, old, this.idL);
    }
    
    @OAMany(
        displayName = "Market Types", 
        toClass = MarketType.class, 
        owner = true, 
        reverseName = MarketType.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<MarketType> getMarketTypes() {
        if (hubMarketTypes == null) {
            hubMarketTypes = (Hub<MarketType>) getHub(P_MarketTypes);
        }
        return hubMarketTypes;
    }
    
    @OAOne(
        displayName = "MRAD Server", 
        owner = true, 
        reverseName = MRADServer.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true, 
        autoCreateNew = true, 
        allowAddExisting = false
    )
    public MRADServer getMRADServer() {
        if (mradServer == null) {
            mradServer = (MRADServer) getObject(P_MRADServer);
        }
        return mradServer;
    }
    
    public void setMRADServer(MRADServer newValue) {
        fireBeforePropertyChange(P_MRADServer, this.mradServer, newValue);
        MRADServer old = this.mradServer;
        this.mradServer = newValue;
        firePropertyChange(P_MRADServer, old, this.mradServer);
    }
    
    @OAOne(
        displayName = "RC Deploy", 
        owner = true, 
        reverseName = RCDeploy.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true, 
        autoCreateNew = true, 
        allowAddExisting = false
    )
    public RCDeploy getRCDeploy() {
        if (rcDeploy == null) {
            rcDeploy = (RCDeploy) getObject(P_RCDeploy);
        }
        return rcDeploy;
    }
    
    public void setRCDeploy(RCDeploy newValue) {
        fireBeforePropertyChange(P_RCDeploy, this.rcDeploy, newValue);
        RCDeploy old = this.rcDeploy;
        this.rcDeploy = newValue;
        firePropertyChange(P_RCDeploy, old, this.rcDeploy);
    }
    
    @OAMany(
        displayName = "RC Installed Versions", 
        toClass = RCInstalledVersion.class, 
        owner = true, 
        reverseName = RCInstalledVersion.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<RCInstalledVersion> getRCInstalledVersions() {
        if (hubRCInstalledVersions == null) {
            hubRCInstalledVersions = (Hub<RCInstalledVersion>) getHub(P_RCInstalledVersions);
        }
        return hubRCInstalledVersions;
    }
    
    @OAMany(
        displayName = "RC Package Lists", 
        toClass = RCPackageList.class, 
        owner = true, 
        reverseName = RCPackageList.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<RCPackageList> getRCPackageLists() {
        if (hubRCPackageLists == null) {
            hubRCPackageLists = (Hub<RCPackageList>) getHub(P_RCPackageLists);
        }
        return hubRCPackageLists;
    }
    
    @OAMany(
        displayName = "RC Repo Versions", 
        toClass = RCRepoVersion.class, 
        owner = true, 
        reverseName = RCRepoVersion.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<RCRepoVersion> getRCRepoVersions() {
        if (hubRCRepoVersions == null) {
            hubRCRepoVersions = (Hub<RCRepoVersion>) getHub(P_RCRepoVersions);
        }
        return hubRCRepoVersions;
    }
    
    @OAMany(
        displayName = "RC Server Lists", 
        toClass = RCServerList.class, 
        owner = true, 
        reverseName = RCServerList.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<RCServerList> getRCServerLists() {
        if (hubRCServerLists == null) {
            hubRCServerLists = (Hub<RCServerList>) getHub(P_RCServerLists);
        }
        return hubRCServerLists;
    }
    
    @OAMany(
        displayName = "RC Service Lists", 
        toClass = RCServiceList.class, 
        owner = true, 
        reverseName = RCServiceList.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<RCServiceList> getRCServiceLists() {
        if (hubRCServiceLists == null) {
            hubRCServiceLists = (Hub<RCServiceList>) getHub(P_RCServiceLists);
        }
        return hubRCServiceLists;
    }
    
    @OAMany(
        toClass = Silo.class, 
        owner = true, 
        cacheSize = 100, 
        reverseName = Silo.P_Environment, 
        cascadeSave = true, 
        cascadeDelete = true, 
        uniqueProperty = Silo.P_SiloType
    )
    public Hub<Silo> getSilos() {
        Hub<Silo> hubSilos;
        {
            hubSilos = (Hub<Silo>) getHub(P_Silos);
        }
        return hubSilos;
    }
    
    @OAOne(
        reverseName = Site.P_Environments, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SiteId"})
    public Site getSite() {
        if (site == null) {
            site = (Site) getObject(P_Site);
        }
        return site;
    }
    
    public void setSite(Site newValue) {
        fireBeforePropertyChange(P_Site, this.site, newValue);
        Site old = this.site;
        this.site = newValue;
        firePropertyChange(P_Site, old, this.site);
    }
    
    // envImport - import list of servers from an XML file
    public void envImport() {
            EnvironmentDelegate.envImport(this);
    }
     
    // envExport - save list of servers in an XML file
    public void envExport() {
        EnvironmentDelegate.envExport(this);
    }
     
    // mradminImport - Import MRAdmin xml file
    public void mradminImport() {
        //EnvironmentDelegate.mradminImport(this);
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.abbrevName = rs.getString(3);
        this.teAbbrevName = rs.getString(4);
        this.usesDNS = rs.getBoolean(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Environment.P_UsesDNS, true);
        }
        this.usesFirewall = rs.getBoolean(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Environment.P_UsesFirewall, true);
        }
        this.usesVip = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Environment.P_UsesVip, true);
        }
        int environmentTypeFkey = rs.getInt(8);
        if (!rs.wasNull() && environmentTypeFkey > 0) {
            setProperty(P_EnvironmentType, new OAObjectKey(environmentTypeFkey));
        }
        int idLFkey = rs.getInt(9);
        if (!rs.wasNull() && idLFkey > 0) {
            setProperty(P_IDL, new OAObjectKey(idLFkey));
        }
        int siteFkey = rs.getInt(10);
        if (!rs.wasNull() && siteFkey > 0) {
            setProperty(P_Site, new OAObjectKey(siteFkey));
        }
        if (rs.getMetaData().getColumnCount() != 10) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
