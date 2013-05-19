package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import com.viaoa.util.OATime;
 
 
public class Version extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_Time = "Time";
    public static final String PROPERTY_Note = "Note";
     
     
    public static final String PROPERTY_User = "User";
    public static final String PROPERTY_VersionStatus = "VersionStatus";
    public static final String PROPERTY_Item = "Item";
     
    protected String id;
    protected OADate date;
    protected OATime time;
    protected String note;
     
    // Links to other objects.
    protected transient User user;
    protected transient VersionStatus versionStatus;
    protected transient Item item;
     
     
    public Version() {
    }
     
    public Version(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    public void setId(String newValue) {
        String old = this.id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate newValue) {
        OADate old = this.date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    public OATime getTime() {
        return time;
    }
    public void setTime(OATime newValue) {
        OATime old = this.time;
        this.time = newValue;
        firePropertyChange(PROPERTY_Time, old, this.time);
    }
    
     
    public String getNote() {
        return note;
    }
    public void setNote(String newValue) {
        String old = this.note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    public User getUser() {
        if (user == null) {
            user = (User) getObject(PROPERTY_User);
        }
        return user;
    }
    
    public void setUser(User newValue) {
        User old = this.user;
        this.user = newValue;
        firePropertyChange(PROPERTY_User, old, this.user);
    }
     
    public VersionStatus getVersionStatus() {
        if (versionStatus == null) {
            versionStatus = (VersionStatus) getObject(PROPERTY_VersionStatus);
        }
        return versionStatus;
    }
    
    public void setVersionStatus(VersionStatus newValue) {
        VersionStatus old = this.versionStatus;
        this.versionStatus = newValue;
        firePropertyChange(PROPERTY_VersionStatus, old, this.versionStatus);
    }
     
    public Item getItem() {
        if (item == null) {
            item = (Item) getObject(PROPERTY_Item);
        }
        return item;
    }
    
    public void setItem(Item newValue) {
        Item old = this.item;
        this.item = newValue;
        firePropertyChange(PROPERTY_Item, old, this.item);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_User, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_Versions));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_VersionStatus, VersionStatus.class, OALinkInfo.ONE, false, false, VersionStatus.PROPERTY_Versions));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_Versions));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
