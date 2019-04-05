// Generated by OABuilder
package test.xice.tsac2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsac2.model.oa.filter.*;
import test.xice.tsac2.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "rcpld",
    displayName = "RCPackage List Detail",
    displayProperty = "code"
)
@OATable(
    indexes = {
        @OAIndex(name = "RCPackageListDetailRcPackageList", columns = { @OAIndexColumn(name = "RcPackageListId") })
    }
)
public class RCPackageListDetail extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Code = "Code";
    public static final String P_Code = "Code";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_RepoDirectory = "RepoDirectory";
    public static final String P_RepoDirectory = "RepoDirectory";
    public static final String PROPERTY_InvalidMessage = "InvalidMessage";
    public static final String P_InvalidMessage = "InvalidMessage";
    public static final String PROPERTY_Selected = "Selected";
    public static final String P_Selected = "Selected";
    public static final String PROPERTY_Loaded = "Loaded";
    public static final String P_Loaded = "Loaded";
     
     
    public static final String PROPERTY_PackageType = "PackageType";
    public static final String P_PackageType = "PackageType";
    public static final String PROPERTY_RCPackageList = "RCPackageList";
    public static final String P_RCPackageList = "RCPackageList";
     
    protected int id;
    protected String code;
    protected String name;
    protected String repoDirectory;
    protected String invalidMessage;
    protected boolean selected;
    protected boolean loaded;
     
    // Links to other objects.
    protected transient PackageType packageType;
    protected transient RCPackageList rcPackageList;
     
    public RCPackageListDetail() {
    }
     
    public RCPackageListDetail(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
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
    @OAProperty(maxLength = 25, displayLength = 14)
    @OAColumn(maxLength = 25)
    public String getCode() {
        return code;
    }
    
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    @OAProperty(maxLength = 50, displayLength = 20, columnLength = 10)
    @OAColumn(maxLength = 50)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayName = "Repo Directory", maxLength = 128, displayLength = 25, columnLength = 18)
    @OAColumn(maxLength = 128)
    public String getRepoDirectory() {
        return repoDirectory;
    }
    
    public void setRepoDirectory(String newValue) {
        fireBeforePropertyChange(P_RepoDirectory, this.repoDirectory, newValue);
        String old = repoDirectory;
        this.repoDirectory = newValue;
        firePropertyChange(P_RepoDirectory, old, this.repoDirectory);
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
        reverseName = PackageType.P_RCPackageListDetails, 
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
        displayName = "RCPackage List", 
        reverseName = RCPackageList.P_RCPackageListDetails, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"RcPackageListId"})
    public RCPackageList getRCPackageList() {
        if (rcPackageList == null) {
            rcPackageList = (RCPackageList) getObject(P_RCPackageList);
        }
        return rcPackageList;
    }
    
    public void setRCPackageList(RCPackageList newValue) {
        fireBeforePropertyChange(P_RCPackageList, this.rcPackageList, newValue);
        RCPackageList old = this.rcPackageList;
        this.rcPackageList = newValue;
        firePropertyChange(P_RCPackageList, old, this.rcPackageList);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.code = rs.getString(2);
        this.name = rs.getString(3);
        this.repoDirectory = rs.getString(4);
        this.invalidMessage = rs.getString(5);
        this.selected = rs.getBoolean(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RCPackageListDetail.P_Selected, true);
        }
        this.loaded = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RCPackageListDetail.P_Loaded, true);
        }
        int packageTypeFkey = rs.getInt(8);
        if (!rs.wasNull() && packageTypeFkey > 0) {
            setProperty(P_PackageType, new OAObjectKey(packageTypeFkey));
        }
        int rcPackageListFkey = rs.getInt(9);
        if (!rs.wasNull() && rcPackageListFkey > 0) {
            setProperty(P_RCPackageList, new OAObjectKey(rcPackageListFkey));
        }
        if (rs.getMetaData().getColumnCount() != 9) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
