package test.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Location extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    
    // Links to other objects
    // protected Hub hubJob;
    protected Hub hubLocation;
    protected Location parentLocation;
    
    public Location() {
    }
     
    public Location(int id) {
        setId(id);
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name",old,name);
    }
    
    /**2008 was:
    public Hub getJobs() {
        if (hubJob == null) hubJob = getHub("jobs");
        return hubJob;
    }
    **/
    public Hub getLocations() {
        if (hubLocation == null) hubLocation = getHub("locations");
        return hubLocation;
    }
    
    public Location getParentLocation() {
        if (parentLocation == null) parentLocation = (Location) getObject("parentLocation");
        return parentLocation;
    }
    
    public void setParentLocation(Location parentLocation) {
        Location old = this.parentLocation;
        this.parentLocation = parentLocation;
        firePropertyChange("parentLocation",old,parentLocation);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUsers",VetUser.class,OALinkInfo.MANY, false,"preferLocations"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("jobs",Job.class,OALinkInfo.MANY, false,"locations"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUsers",VetUser.class,OALinkInfo.MANY, false,"rejectLocations"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("locations",Location.class,OALinkInfo.MANY, false,"parentLocation"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("parentLocation",Location.class,OALinkInfo.ONE, false,"locations"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("batchRows",BatchRow.class,OALinkInfo.MANY, false,"location"));
    }
}

