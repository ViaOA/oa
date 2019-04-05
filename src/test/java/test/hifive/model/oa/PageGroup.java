// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;
 
@OAClass(
    shortName = "pg",
    displayName = "Web Page Group",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "seq"
)
@OATable(
    indexes = {
        @OAIndex(name = "PageGroupParentPageGroup", columns = { @OAIndexColumn(name = "ParentPageGroupId") })
    }
)
public class PageGroup extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
     
     
    public static final String PROPERTY_LocationPageGroups = "LocationPageGroups";
    public static final String P_LocationPageGroups = "LocationPageGroups";
    public static final String PROPERTY_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String P_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String PROPERTY_PageGroups = "PageGroups";
    public static final String P_PageGroups = "PageGroups";
    public static final String PROPERTY_ParentPageGroup = "ParentPageGroup";
    public static final String P_ParentPageGroup = "ParentPageGroup";
    public static final String PROPERTY_ProgramPageGroups = "ProgramPageGroups";
    public static final String P_ProgramPageGroups = "ProgramPageGroups";
     
    protected int id;
    protected OADate created;
    protected int seq;
    protected String name;
     
    // Links to other objects.
    protected transient Hub<PageGroupPageInfo> hubPageGroupPageInfos;
    protected transient Hub<PageGroup> hubPageGroups;
    protected transient PageGroup parentPageGroup;
     
    public PageGroup() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public PageGroup(int id) {
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
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreated() {
        return created;
    }
    
    public void setCreated(OADate newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADate old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAProperty(maxLength = 35, displayLength = 20)
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
    @OAMany(
        displayName = "Location Page Groups", 
        toClass = LocationPageGroup.class, 
        reverseName = LocationPageGroup.P_PageGroup, 
        createMethod = false
    )
    private Hub<LocationPageGroup> getLocationPageGroups() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Page Group Page Infos", 
        toClass = PageGroupPageInfo.class, 
        owner = true, 
        reverseName = PageGroupPageInfo.P_PageGroup, 
        cascadeSave = true, 
        cascadeDelete = true, 
        uniqueProperty = PageGroupPageInfo.P_PageInfo
    )
    public Hub<PageGroupPageInfo> getPageGroupPageInfos() {
        if (hubPageGroupPageInfos == null) {
            hubPageGroupPageInfos = (Hub<PageGroupPageInfo>) getHub(P_PageGroupPageInfos);
        }
        return hubPageGroupPageInfos;
    }
    
    @OAMany(
        displayName = "Page Groups", 
        toClass = PageGroup.class, 
        recursive = true, 
        owner = true, 
        reverseName = PageGroup.P_ParentPageGroup, 
        cascadeSave = true, 
        cascadeDelete = true, 
        seqProperty = PageGroup.P_Seq, 
        sortProperty = PageGroup.P_Seq
    )
    public Hub<PageGroup> getPageGroups() {
        if (hubPageGroups == null) {
            hubPageGroups = (Hub<PageGroup>) getHub(P_PageGroups);
        }
        return hubPageGroups;
    }
    
    @OAOne(
        displayName = "Parent Page Group", 
        reverseName = PageGroup.P_PageGroups, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ParentPageGroupId"})
    public PageGroup getParentPageGroup() {
        if (parentPageGroup == null) {
            parentPageGroup = (PageGroup) getObject(P_ParentPageGroup);
        }
        return parentPageGroup;
    }
    
    public void setParentPageGroup(PageGroup newValue) {
        fireBeforePropertyChange(P_ParentPageGroup, this.parentPageGroup, newValue);
        PageGroup old = this.parentPageGroup;
        this.parentPageGroup = newValue;
        firePropertyChange(P_ParentPageGroup, old, this.parentPageGroup);
    }
    
    @OAMany(
        displayName = "Program Page Groups", 
        toClass = ProgramPageGroup.class, 
        reverseName = ProgramPageGroup.P_PageGroup, 
        createMethod = false
    )
    private Hub<ProgramPageGroup> getProgramPageGroups() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, PageGroup.P_Seq, true);
        }
        this.name = rs.getString(4);
        int parentPageGroupFkey = rs.getInt(5);
        if (!rs.wasNull() && parentPageGroupFkey > 0) {
            setProperty(P_ParentPageGroup, new OAObjectKey(parentPageGroupFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
