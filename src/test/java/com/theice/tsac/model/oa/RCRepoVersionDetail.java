// Generated by OABuilder
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.propertypath.*;
import com.viaoa.util.OADate;
 
@OAClass(
    shortName = "rcrvd",
    displayName = "RCRepo Version Detail",
    displayProperty = "packageId"
)
@OATable(
    indexes = {
        @OAIndex(name = "RCRepoVersionDetailRcRepoVersion", columns = { @OAIndexColumn(name = "RcRepoVersionId") })
    }
)
public class RCRepoVersionDetail extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_PackageId = "PackageId";
    public static final String P_PackageId = "PackageId";
    public static final String PROPERTY_BuildDate = "BuildDate";
    public static final String P_BuildDate = "BuildDate";
    public static final String PROPERTY_Version = "Version";
    public static final String P_Version = "Version";
    public static final String PROPERTY_Error = "Error";
    public static final String P_Error = "Error";
    public static final String PROPERTY_InvalidMessage = "InvalidMessage";
    public static final String P_InvalidMessage = "InvalidMessage";
    public static final String PROPERTY_Selected = "Selected";
    public static final String P_Selected = "Selected";
    public static final String PROPERTY_Loaded = "Loaded";
    public static final String P_Loaded = "Loaded";
     
     
    public static final String PROPERTY_PackageType = "PackageType";
    public static final String P_PackageType = "PackageType";
    public static final String PROPERTY_PackageVersion = "PackageVersion";
    public static final String P_PackageVersion = "PackageVersion";
    public static final String PROPERTY_RCRepoVersion = "RCRepoVersion";
    public static final String P_RCRepoVersion = "RCRepoVersion";
     
    protected int id;
    protected String packageId;
    protected OADate buildDate;
    protected String version;
    protected String error;
    protected String invalidMessage;
    protected boolean selected;
    protected boolean loaded;
     
    // Links to other objects.
    protected transient PackageType packageType;
    protected transient PackageVersion packageVersion;
    protected transient RCRepoVersion rcRepoVersion;
     
    public RCRepoVersionDetail() {
    }
     
    public RCRepoVersionDetail(int id) {
        this();
        setId(id);
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
    @OAProperty(displayName = "Package Id", maxLength = 50, displayLength = 40, isProcessed = true)
    @OAColumn(maxLength = 50)
    public String getPackageId() {
        return packageId;
    }
    
    public void setPackageId(String newValue) {
        fireBeforePropertyChange(P_PackageId, this.packageId, newValue);
        String old = packageId;
        this.packageId = newValue;
        firePropertyChange(P_PackageId, old, this.packageId);
    }
    @OAProperty(displayName = "Build Date", displayLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getBuildDate() {
        return buildDate;
    }
    
    public void setBuildDate(OADate newValue) {
        fireBeforePropertyChange(P_BuildDate, this.buildDate, newValue);
        OADate old = buildDate;
        this.buildDate = newValue;
        firePropertyChange(P_BuildDate, old, this.buildDate);
    }
    @OAProperty(maxLength = 35, displayLength = 35, isProcessed = true)
    @OAColumn(maxLength = 35)
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String newValue) {
        fireBeforePropertyChange(P_Version, this.version, newValue);
        String old = version;
        this.version = newValue;
        firePropertyChange(P_Version, old, this.version);
    }
    @OAProperty(maxLength = 250, displayLength = 40, isProcessed = true)
    @OAColumn(maxLength = 250)
    public String getError() {
        return error;
    }
    
    public void setError(String newValue) {
        fireBeforePropertyChange(P_Error, this.error, newValue);
        String old = error;
        this.error = newValue;
        firePropertyChange(P_Error, old, this.error);
    }
    @OAProperty(displayName = "Invalid Message", maxLength = 120, displayLength = 40, isProcessed = true)
    @OAColumn(maxLength = 120)
    public String getInvalidMessage() {
        return invalidMessage;
    }
    
    public void setInvalidMessage(String newValue) {
        fireBeforePropertyChange(P_InvalidMessage, this.invalidMessage, newValue);
        String old = invalidMessage;
        this.invalidMessage = newValue;
        firePropertyChange(P_InvalidMessage, old, this.invalidMessage);
    }
    @OAProperty(displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getSelected() {
        return selected;
    }
    
    public void setSelected(boolean newValue) {
        fireBeforePropertyChange(P_Selected, this.selected, newValue);
        boolean old = selected;
        this.selected = newValue;
        firePropertyChange(P_Selected, old, this.selected);
    }
    @OAProperty(displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getLoaded() {
        return loaded;
    }
    
    public void setLoaded(boolean newValue) {
        fireBeforePropertyChange(P_Loaded, this.loaded, newValue);
        boolean old = loaded;
        this.loaded = newValue;
        firePropertyChange(P_Loaded, old, this.loaded);
    }
    @OAOne(
        displayName = "Package Type", 
        reverseName = PackageType.P_RCRepoVersionDetails, 
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
        reverseName = PackageVersion.P_RCRepoVersionDetails
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
        displayName = "RCRepo Version", 
        reverseName = RCRepoVersion.P_RCRepoVersionDetails, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"RcRepoVersionId"})
    public RCRepoVersion getRCRepoVersion() {
        if (rcRepoVersion == null) {
            rcRepoVersion = (RCRepoVersion) getObject(P_RCRepoVersion);
        }
        return rcRepoVersion;
    }
    
    public void setRCRepoVersion(RCRepoVersion newValue) {
        fireBeforePropertyChange(P_RCRepoVersion, this.rcRepoVersion, newValue);
        RCRepoVersion old = this.rcRepoVersion;
        this.rcRepoVersion = newValue;
        firePropertyChange(P_RCRepoVersion, old, this.rcRepoVersion);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.packageId = rs.getString(2);
        java.sql.Date date;
        date = rs.getDate(3);
        if (date != null) this.buildDate = new OADate(date);
        this.version = rs.getString(4);
        this.error = rs.getString(5);
        this.invalidMessage = rs.getString(6);
        this.selected = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RCRepoVersionDetail.P_Selected, true);
        }
        this.loaded = rs.getBoolean(8);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RCRepoVersionDetail.P_Loaded, true);
        }
        int packageTypeFkey = rs.getInt(9);
        if (!rs.wasNull() && packageTypeFkey > 0) {
            setProperty(P_PackageType, new OAObjectKey(packageTypeFkey));
        }
        int packageVersionFkey = rs.getInt(10);
        if (!rs.wasNull() && packageVersionFkey > 0) {
            setProperty(P_PackageVersion, new OAObjectKey(packageVersionFkey));
        }
        int rcRepoVersionFkey = rs.getInt(11);
        if (!rs.wasNull() && rcRepoVersionFkey > 0) {
            setProperty(P_RCRepoVersion, new OAObjectKey(rcRepoVersionFkey));
        }
        if (rs.getMetaData().getColumnCount() != 11) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
