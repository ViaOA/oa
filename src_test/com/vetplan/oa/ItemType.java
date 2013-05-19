package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Type = "Type";
     
     
    public static final String PROPERTY_Items = "Items";
     
    protected String id;
    protected String name;
    protected String description;
    protected String gifFileName;
    protected int seq;
    protected int type;
    public static final int TYPE_SOAP_S = 0;
    public static final int TYPE_SOAP_O = 1;
    public static final int TYPE_SOAP_A = 2;
    public static final int TYPE_SOAP_P = 3;
    public static final int TYPE_OTHER = 4;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("SOAP S");
        hubType.addElement("SOAP O");
        hubType.addElement("SOAP A");
        hubType.addElement("SOAP P");
        hubType.addElement("Other");
    }
     
    // Links to other objects.
     
     
    public ItemType() {
    }
     
    public ItemType(String id) {
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
    
     
    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        String old = this.name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        String old = this.description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    public String getGifFileName() {
        return gifFileName;
    }
    public void setGifFileName(String newValue) {
        String old = this.gifFileName;
        this.gifFileName = newValue;
        firePropertyChange(PROPERTY_GifFileName, old, this.gifFileName);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public int getType() {
        return type;
    }
    public void setType(int newValue) {
        int old = this.type;
        this.type = newValue;
        firePropertyChange(PROPERTY_Type, old, this.type);
    }
    public static Hub getTypes() {
        return hubType;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Items, Item.class, OALinkInfo.MANY, false, false, Item.PROPERTY_ItemType));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
