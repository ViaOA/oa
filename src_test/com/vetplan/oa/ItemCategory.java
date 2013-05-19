package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemCategory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Seq = "Seq";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_Items = "Items";
    public static final String PROPERTY_ParentItemCategory = "ParentItemCategory";
    public static final String PROPERTY_ItemCategories = "ItemCategories";
     
    protected String id;
    protected String name;
    protected String description;
    protected int seq;
     
    // Links to other objects.
    protected transient Hub hubItems;
    protected transient ItemCategory parentItemCategory;
    protected transient Hub hubItemCategories;
     
     
    public ItemCategory() {
    }
     
    public ItemCategory(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getFullName() {
        String s = getName();
        ItemCategory ic = getParentItemCategory();
        for ( ; ic != null; ) {
            s = ic.getName() + " / " + s;
            ic = ic.getParentItemCategory();
        }
        return s;
    }
     
    public Hub getItems() {
        if (hubItems == null) {
            hubItems = getHub(PROPERTY_Items);
        }
        return hubItems;
    }
    
     
    public ItemCategory getParentItemCategory() {
        if (parentItemCategory == null) {
            parentItemCategory = (ItemCategory) getObject(PROPERTY_ParentItemCategory);
        }
        return parentItemCategory;
    }
    
    public void setParentItemCategory(ItemCategory newValue) {
        ItemCategory old = this.parentItemCategory;
        this.parentItemCategory = newValue;
        firePropertyChange(PROPERTY_ParentItemCategory, old, this.parentItemCategory);
    }
     
    public Hub getItemCategories() {
        if (hubItemCategories == null) {
            hubItemCategories = getHub(PROPERTY_ItemCategories, "seq");
            hubItemCategories.setAutoSequence("seq");
        }
        return hubItemCategories;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Items, Item.class, OALinkInfo.MANY, false, false, Item.PROPERTY_ItemCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentItemCategory, ItemCategory.class, OALinkInfo.ONE, false, false, ItemCategory.PROPERTY_ItemCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemCategories, ItemCategory.class, OALinkInfo.MANY, true, true, ItemCategory.PROPERTY_ParentItemCategory, true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
