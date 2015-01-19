// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
import com.viaoa.util.OADate;
 
@OAClass(
    shortName = "lpg",
    displayName = "Location Page Group",
    displayProperty = "pageGroup.name"
)
@OATable(
    indexes = {
        @OAIndex(name = "LocationPageGroupLocation", columns = { @OAIndexColumn(name = "LocationId") })
    }
)
public class LocationPageGroup extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_PageGroup = "PageGroup";
    public static final String P_PageGroup = "PageGroup";
     
    protected int id;
    protected OADate created;
    protected int seq;
     
    // Links to other objects.
    protected transient Location location;
    protected transient PageGroup pageGroup;
     
    public LocationPageGroup() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public LocationPageGroup(int id) {
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
    @OAOne(
        reverseName = Location.P_LocationPageGroups, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"LocationId"})
    public Location getLocation() {
        if (location == null) {
            location = (Location) getObject(P_Location);
        }
        return location;
    }
    
    public void setLocation(Location newValue) {
        fireBeforePropertyChange(P_Location, this.location, newValue);
        Location old = this.location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    
    @OAOne(
        displayName = "Page Group", 
        reverseName = PageGroup.P_LocationPageGroups, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"PageGroupId"})
    public PageGroup getPageGroup() {
        if (pageGroup == null) {
            pageGroup = (PageGroup) getObject(P_PageGroup);
        }
        return pageGroup;
    }
    
    public void setPageGroup(PageGroup newValue) {
        fireBeforePropertyChange(P_PageGroup, this.pageGroup, newValue);
        PageGroup old = this.pageGroup;
        this.pageGroup = newValue;
        firePropertyChange(P_PageGroup, old, this.pageGroup);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, LocationPageGroup.P_Seq, true);
        }
        int locationFkey = rs.getInt(4);
        if (!rs.wasNull() && locationFkey > 0) {
            setProperty(P_Location, new OAObjectKey(locationFkey));
        }
        int pageGroupFkey = rs.getInt(5);
        if (!rs.wasNull() && pageGroupFkey > 0) {
            setProperty(P_PageGroup, new OAObjectKey(pageGroupFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
