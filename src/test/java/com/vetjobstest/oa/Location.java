package com.vetjobstest.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "loc",
    displayName = "Location",
    isLookup = true,
    isPreSelect = true
)
@OATable(
    indexes = {
        @OAIndex(name = "LocationParentLocation", columns = { @OAIndexColumn(name = "ParentLocationId") })
    }
)
public class Location extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Name = "Name";
     
     
    public static final String P_VetUsers = "VetUsers";
    public static final String P_VetUser2S = "VetUser2S";
    public static final String P_Jobs = "Jobs";
    public static final String P_Locations = "Locations";
    public static final String P_ParentLocation = "ParentLocation";
    public static final String P_BatchRows = "BatchRows";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
    protected transient Hub<Location> hubLocations;
    protected transient Location parentLocation;
     
     
    public Location() {
    }
     
    public Location(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 4)
    @OAColumn(maxLength = 75)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.P_PreferLocations, createMethod = false)
    @OALinkTable(name = "VetUserLocationLink", indexName = "VetUserPreferLocation", columns = {"LocationId"})
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Vet User2s", toClass = VetUser.class, reverseName = VetUser.P_RejectLocations, createMethod = false)
    @OALinkTable(name = "VetUserLocationLink1", indexName = "VetUserRejectLocation", columns = {"LocationId"})
    private Hub<VetUser> getVetUser2S() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(toClass = Job.class, reverseName = Job.P_Locations, createMethod = false)
    @OALinkTable(name = "JobLocationLink", indexName = "JobLocation", columns = {"LocationId"})
    private Hub<Job> getJobs() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(toClass = Location.class, reverseName = Location.P_ParentLocation)
    public Hub<Location> getLocations() {
        if (hubLocations == null) {
            hubLocations = (Hub<Location>) getHub(P_Locations);
        }
        return hubLocations;
    }
    
     
    @OAOne(displayName = "Parent Location", reverseName = Location.P_Locations)
    @OAFkey(columns = {"ParentLocationId"})
    public Location getParentLocation() {
        if (parentLocation == null) {
            parentLocation = (Location) getObject(P_ParentLocation);
        }
        return parentLocation;
    }
    
    public void setParentLocation(Location newValue) {
        Location old = this.parentLocation;
        this.parentLocation = newValue;
        firePropertyChange(P_ParentLocation, old, this.parentLocation);
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, reverseName = BatchRow.P_Location, createMethod = false)
    private Hub<BatchRow> getBatchRows() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
}
 
