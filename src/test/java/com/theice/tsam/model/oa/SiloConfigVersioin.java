// Generated by OABuilder
package com.theice.tsam.model.oa;
 
import java.util.logging.*;
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsam.delegate.oa.*;
import com.theice.tsam.model.oa.filter.*;
import com.theice.tsam.model.oa.propertypath.*;
 
@OAClass(
    shortName = "scv",
    displayName = "Silo Config Versioin",
    displayProperty = "siloConfig.silo"
)
@OATable(
    indexes = {
        @OAIndex(name = "SiloConfigVersioinSiloConfig", columns = { @OAIndexColumn(name = "SiloConfigId") })
    }
)
public class SiloConfigVersioin extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(SiloConfigVersioin.class.getName());
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
     
     
    public static final String PROPERTY_PackageType = "PackageType";
    public static final String P_PackageType = "PackageType";
    public static final String PROPERTY_PackageVersion = "PackageVersion";
    public static final String P_PackageVersion = "PackageVersion";
    public static final String PROPERTY_SiloConfig = "SiloConfig";
    public static final String P_SiloConfig = "SiloConfig";
     
    protected int id;
     
    // Links to other objects.
    protected transient PackageType packageType;
    protected transient PackageVersion packageVersion;
    protected transient SiloConfig siloConfig;
     
    public SiloConfigVersioin() {
    }
     
    public SiloConfigVersioin(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 3)
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
    
    @OAOne(
        displayName = "Package Type", 
        reverseName = PackageType.P_SiloConfigVersioins, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"PackageTypeId"})
    public PackageType getPackageType() {
        if (packageType == null) {
            packageType = (PackageType) getObject(P_PackageType);
        }
        return packageType;
    }
    
    public void setPackageType(PackageType newValue) {
        fireBeforePropertyChange(P_PackageType, this.packageType, newValue);
        PackageType old = this.packageType;
        this.packageType = newValue;
        firePropertyChange(P_PackageType, old, this.packageType);
    }
    
    @OAOne(
        displayName = "Package Version", 
        reverseName = PackageVersion.P_SiloConfigVersioins
    )
    @OAFkey(columns = {"PackageVersionId"})
    public PackageVersion getPackageVersion() {
        if (packageVersion == null) {
            packageVersion = (PackageVersion) getObject(P_PackageVersion);
        }
        return packageVersion;
    }
    
    public void setPackageVersion(PackageVersion newValue) {
        fireBeforePropertyChange(P_PackageVersion, this.packageVersion, newValue);
        PackageVersion old = this.packageVersion;
        this.packageVersion = newValue;
        firePropertyChange(P_PackageVersion, old, this.packageVersion);
    }
    
    @OAOne(
        displayName = "Silo Config", 
        reverseName = SiloConfig.P_SiloConfigVersioins, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SiloConfigId"})
    public SiloConfig getSiloConfig() {
        if (siloConfig == null) {
            siloConfig = (SiloConfig) getObject(P_SiloConfig);
        }
        return siloConfig;
    }
    
    public void setSiloConfig(SiloConfig newValue) {
        fireBeforePropertyChange(P_SiloConfig, this.siloConfig, newValue);
        SiloConfig old = this.siloConfig;
        this.siloConfig = newValue;
        firePropertyChange(P_SiloConfig, old, this.siloConfig);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        int packageTypeFkey = rs.getInt(2);
        if (!rs.wasNull() && packageTypeFkey > 0) {
            setProperty(P_PackageType, new OAObjectKey(packageTypeFkey));
        }
        int packageVersionFkey = rs.getInt(3);
        if (!rs.wasNull() && packageVersionFkey > 0) {
            setProperty(P_PackageVersion, new OAObjectKey(packageVersionFkey));
        }
        int siloConfigFkey = rs.getInt(4);
        if (!rs.wasNull() && siloConfigFkey > 0) {
            setProperty(P_SiloConfig, new OAObjectKey(siloConfigFkey));
        }
        if (rs.getMetaData().getColumnCount() != 4) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
