package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import java.math.BigDecimal;
 
 
public class Item extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Severity = "Severity";
    public static final String PROPERTY_Priority = "Priority";
    public static final String PROPERTY_Reminder = "Reminder";
    public static final String PROPERTY_TechDescription = "TechDescription";
    public static final String PROPERTY_ClientDescription = "ClientDescription";
    public static final String PROPERTY_PickOneSubItem = "PickOneSubItem";
    public static final String PROPERTY_SubItemFlag = "SubItemFlag";
    public static final String PROPERTY_ShortClientDescription = "ShortClientDescription";
    public static final String PROPERTY_Price = "Price";
    public static final String PROPERTY_IntervalType = "IntervalType";
    public static final String PROPERTY_IntervalAmount = "IntervalAmount";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Type = "Type";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_ExamItems = "ExamItems";
    public static final String PROPERTY_Versions = "Versions";
    public static final String PROPERTY_SectionItems = "SectionItems";
    public static final String PROPERTY_ItemCategories = "ItemCategories";
    public static final String PROPERTY_PetSystem = "PetSystem";
    public static final String PROPERTY_ParentItem = "ParentItem";
    public static final String PROPERTY_Items = "Items";
    public static final String PROPERTY_AutoSelects = "AutoSelects";
    public static final String PROPERTY_ItemType = "ItemType";
    public static final String PROPERTY_ItemProducts = "ItemProducts";
    public static final String PROPERTY_RecommendItems = "RecommendItems";
    public static final String PROPERTY_ItemTasks = "ItemTasks";
    public static final String PROPERTY_UseItemTasks = "UseItemTasks";
    public static final String PROPERTY_Service = "Service";
    public static final String PROPERTY_AutoSelectSubitems = "AutoSelectSubitems";
     
    protected String id;
    protected String pmsId;
    protected String name;
    protected String description;
    protected int severity;
    protected int priority;
    protected boolean reminder;
    protected String techDescription;
    protected String clientDescription;
    protected boolean pickOneSubItem;
    protected boolean subItemFlag;
    protected String shortClientDescription;
    protected BigDecimal price;
    protected int intervalType;
    public static final int INTERVALTYPE_DAY = 0;
    public static final int INTERVALTYPE_WEEK = 1;
    public static final int INTERVALTYPE_MONTH = 2;
    public static final int INTERVALTYPE_YEAR = 3;
    public static final Hub hubIntervalType;
    static {
        hubIntervalType = new Hub(String.class);
        hubIntervalType.addElement("Day");
        hubIntervalType.addElement("Week");
        hubIntervalType.addElement("Month");
        hubIntervalType.addElement("Year");
    }
    protected int intervalAmount;
    protected int seq;
    protected int type;
    public static final int TYPE_OTHER = 0;
    public static final int TYPE_PRODUCT = 1;
    public static final int TYPE_SERVICE = 2;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("Other");
        hubType.addElement("Product");
        hubType.addElement("Service");
    }
     
    // Links to other objects.
    protected transient Hub hubVersions;
    protected transient Hub hubSectionItems;
    protected transient Hub hubItemCategories;
    protected transient PetSystem petSystem;
    protected transient Item parentItem;
    protected transient Hub hubItems;
    protected transient Hub hubAutoSelects;
    protected transient ItemType itemType;
    protected transient Hub hubItemProducts;
    protected transient Hub hubItemTasks;
    protected transient Service service;
     
     
    public Item() {
    }
     
    public Item(String id) {
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
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
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
    
     
    public int getSeverity() {
        return severity;
    }
    public void setSeverity(int newValue) {
        int old = this.severity;
        this.severity = newValue;
        firePropertyChange(PROPERTY_Severity, old, this.severity);
    }
    
     
    public int getPriority() {
        return priority;
    }
    public void setPriority(int newValue) {
        int old = this.priority;
        this.priority = newValue;
        firePropertyChange(PROPERTY_Priority, old, this.priority);
    }
    
     
    public boolean getReminder() {
        return reminder;
    }
    public void setReminder(boolean newValue) {
        boolean old = this.reminder;
        this.reminder = newValue;
        firePropertyChange(PROPERTY_Reminder, old, this.reminder);
    }
    
     
    public String getTechDescription() {
        return techDescription;
    }
    public void setTechDescription(String newValue) {
        String old = this.techDescription;
        this.techDescription = newValue;
        firePropertyChange(PROPERTY_TechDescription, old, this.techDescription);
    }
    
     
    public String getClientDescription() {
        return clientDescription;
    }
    public void setClientDescription(String newValue) {
        String old = this.clientDescription;
        this.clientDescription = newValue;
        firePropertyChange(PROPERTY_ClientDescription, old, this.clientDescription);
    }
    
     
    public boolean getPickOneSubItem() {
        return pickOneSubItem;
    }
    public void setPickOneSubItem(boolean newValue) {
        boolean old = this.pickOneSubItem;
        this.pickOneSubItem = newValue;
        firePropertyChange(PROPERTY_PickOneSubItem, old, this.pickOneSubItem);
    }
    
     
    public boolean getSubItemFlag() {
        return subItemFlag;
    }
    public void setSubItemFlag(boolean newValue) {
        boolean old = this.subItemFlag;
        this.subItemFlag = newValue;
        firePropertyChange(PROPERTY_SubItemFlag, old, this.subItemFlag);
    }
    
     
    public String getShortClientDescription() {
        return shortClientDescription;
    }
    public void setShortClientDescription(String newValue) {
        String old = this.shortClientDescription;
        this.shortClientDescription = newValue;
        firePropertyChange(PROPERTY_ShortClientDescription, old, this.shortClientDescription);
    }
    
     
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal newValue) {
        BigDecimal old = this.price;
        this.price = newValue;
        firePropertyChange(PROPERTY_Price, old, this.price);
    }
    
     
    public int getIntervalType() {
        return intervalType;
    }
    public void setIntervalType(int newValue) {
        int old = this.intervalType;
        this.intervalType = newValue;
        firePropertyChange(PROPERTY_IntervalType, old, this.intervalType);
    }
    public static Hub getIntervalTypes() {
        return hubIntervalType;
    }
    
     
    public int getIntervalAmount() {
        return intervalAmount;
    }
    public void setIntervalAmount(int newValue) {
        int old = this.intervalAmount;
        this.intervalAmount = newValue;
        firePropertyChange(PROPERTY_IntervalAmount, old, this.intervalAmount);
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
    
     
    public String getFullName() {
        String s = "";
        Item item = getParentItem();
        if (item != null) {
            // s = item.getPMSName();
            if (s == null) s = "";
            if (s.length() > 0) s += ": ";
        }
        // s += getPMSName();
        
        return s;
    }
     
    public Hub getVersions() {
        if (hubVersions == null) {
            hubVersions = getHub(PROPERTY_Versions);
        }
        return hubVersions;
    }
    
     
    public Hub getSectionItems() {
        if (hubSectionItems == null) {
            hubSectionItems = getHub(PROPERTY_SectionItems);
        }
        return hubSectionItems;
    }
    
     
    public Hub getItemCategories() {
        if (hubItemCategories == null) {
            hubItemCategories = getHub(PROPERTY_ItemCategories);
        }
        return hubItemCategories;
    }
    
     
    public PetSystem getPetSystem() {
        if (petSystem == null) {
            petSystem = (PetSystem) getObject(PROPERTY_PetSystem);
        }
        return petSystem;
    }
    
    public void setPetSystem(PetSystem newValue) {
        PetSystem old = this.petSystem;
        this.petSystem = newValue;
        firePropertyChange(PROPERTY_PetSystem, old, this.petSystem);
    }
     
    public Item getParentItem() {
        if (parentItem == null) {
            parentItem = (Item) getObject(PROPERTY_ParentItem);
        }
        return parentItem;
    }
    
    public void setParentItem(Item newValue) {
        Item old = this.parentItem;
        this.parentItem = newValue;
        firePropertyChange(PROPERTY_ParentItem, old, this.parentItem);
    }
     
    public Hub getItems() {
        if (hubItems == null) {
            hubItems = getHub(PROPERTY_Items, "seq");
            hubItems.setAutoSequence("seq");
        }
        return hubItems;
    }
    
     
    public Hub getAutoSelects() {
        if (hubAutoSelects == null) {
            hubAutoSelects = getHub(PROPERTY_AutoSelects);
        }
        return hubAutoSelects;
    }
    
     
    public ItemType getItemType() {
        if (itemType == null) {
            itemType = (ItemType) getObject(PROPERTY_ItemType);
        }
        return itemType;
    }
    
    public void setItemType(ItemType newValue) {
        ItemType old = this.itemType;
        this.itemType = newValue;
        firePropertyChange(PROPERTY_ItemType, old, this.itemType);
    }
     
    public Hub getItemProducts() {
        if (hubItemProducts == null) {
            hubItemProducts = getHub(PROPERTY_ItemProducts);
        }
        return hubItemProducts;
    }
    
     
    public Hub getItemTasks() {
        if (hubItemTasks == null) {
            hubItemTasks = getHub(PROPERTY_ItemTasks, "seq");
            hubItemTasks.setAutoSequence("seq");
        }
        return hubItemTasks;
    }
    
     
    public Service getService() {
        if (service == null) {
            service = (Service) getObject(PROPERTY_Service);
        }
        return service;
    }
    
    public void setService(Service newValue) {
        Service old = this.service;
        this.service = newValue;
        firePropertyChange(PROPERTY_Service, old, this.service);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, false, false, ExamItem.PROPERTY_Item));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Versions, Version.class, OALinkInfo.MANY, true, true, Version.PROPERTY_Item));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItems, SectionItem.class, OALinkInfo.MANY, false, false, SectionItem.PROPERTY_Item));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemCategories, ItemCategory.class, OALinkInfo.MANY, false, false, ItemCategory.PROPERTY_Items));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_PetSystem, PetSystem.class, OALinkInfo.ONE, false, false, PetSystem.PROPERTY_Items));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentItem, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_Items));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Items, Item.class, OALinkInfo.MANY, true, true, Item.PROPERTY_ParentItem, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AutoSelects, AutoSelect.class, OALinkInfo.MANY, false, true, AutoSelect.PROPERTY_Item));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemType, ItemType.class, OALinkInfo.ONE, false, false, ItemType.PROPERTY_Items));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemProducts, ItemProduct.class, OALinkInfo.MANY, true, true, ItemProduct.PROPERTY_Item, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_RecommendItems, RecommendItem.class, OALinkInfo.MANY, false, false, RecommendItem.PROPERTY_Item, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTasks, ItemTask.class, OALinkInfo.MANY, true, true, ItemTask.PROPERTY_Item, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_UseItemTasks, ItemTask.class, OALinkInfo.MANY, false, false, ItemTask.PROPERTY_UseItem, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Service, Service.class, OALinkInfo.ONE, false, false, Service.PROPERTY_Items));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AutoSelectSubitems, AutoSelect.class, OALinkInfo.MANY, false, false, AutoSelect.PROPERTY_SubItem));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
