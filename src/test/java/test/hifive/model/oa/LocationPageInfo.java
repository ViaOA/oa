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
    shortName = "lpi",
    displayName = "Location Page Info",
    displayProperty = "pageInfo.code"
)
@OATable(
    indexes = {
        @OAIndex(name = "LocationPageInfoLocation", columns = { @OAIndexColumn(name = "LocationId") })
    }
)
public class LocationPageInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
     
     
    public static final String PROPERTY_ImageStore = "ImageStore";
    public static final String P_ImageStore = "ImageStore";
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_PageInfo = "PageInfo";
    public static final String P_PageInfo = "PageInfo";
    public static final String PROPERTY_ProgramDocument = "ProgramDocument";
    public static final String P_ProgramDocument = "ProgramDocument";
     
    protected int id;
    protected OADate created;
     
    // Links to other objects.
    protected transient ImageStore imageStore;
    protected transient Location location;
    protected transient PageInfo pageInfo;
    protected transient ProgramDocument programDocument;
     
    public LocationPageInfo() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public LocationPageInfo(int id) {
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
    @OAOne(
        displayName = "Image Store", 
        owner = true, 
        reverseName = ImageStore.P_LocationPageInfo, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"ImageStoreId"})
    public ImageStore getImageStore() {
        if (imageStore == null) {
            imageStore = (ImageStore) getObject(P_ImageStore);
        }
        return imageStore;
    }
    
    public void setImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_ImageStore, this.imageStore, newValue);
        ImageStore old = this.imageStore;
        this.imageStore = newValue;
        firePropertyChange(P_ImageStore, old, this.imageStore);
    }
    
    @OAOne(
        reverseName = Location.P_LocationPageInfos, 
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
        displayName = "Page Info", 
        reverseName = PageInfo.P_LocationPageInfos, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"PageInfoId"})
    public PageInfo getPageInfo() {
        if (pageInfo == null) {
            pageInfo = (PageInfo) getObject(P_PageInfo);
        }
        return pageInfo;
    }
    
    public void setPageInfo(PageInfo newValue) {
        fireBeforePropertyChange(P_PageInfo, this.pageInfo, newValue);
        PageInfo old = this.pageInfo;
        this.pageInfo = newValue;
        firePropertyChange(P_PageInfo, old, this.pageInfo);
    }
    
    @OAOne(
        displayName = "Program Document", 
        reverseName = ProgramDocument.P_LocationPageInfo, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"ProgramDocumentId"})
    public ProgramDocument getProgramDocument() {
        if (programDocument == null) {
            programDocument = (ProgramDocument) getObject(P_ProgramDocument);
        }
        return programDocument;
    }
    
    public void setProgramDocument(ProgramDocument newValue) {
        fireBeforePropertyChange(P_ProgramDocument, this.programDocument, newValue);
        ProgramDocument old = this.programDocument;
        this.programDocument = newValue;
        firePropertyChange(P_ProgramDocument, old, this.programDocument);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        int imageStoreFkey = rs.getInt(3);
        if (!rs.wasNull() && imageStoreFkey > 0) {
            setProperty(P_ImageStore, new OAObjectKey(imageStoreFkey));
        }
        int locationFkey = rs.getInt(4);
        if (!rs.wasNull() && locationFkey > 0) {
            setProperty(P_Location, new OAObjectKey(locationFkey));
        }
        int pageInfoFkey = rs.getInt(5);
        if (!rs.wasNull() && pageInfoFkey > 0) {
            setProperty(P_PageInfo, new OAObjectKey(pageInfoFkey));
        }
        int programDocumentFkey = rs.getInt(6);
        if (!rs.wasNull() && programDocumentFkey > 0) {
            setProperty(P_ProgramDocument, new OAObjectKey(programDocumentFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
