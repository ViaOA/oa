package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class RecommendItem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_ComplianceType = "ComplianceType";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Insert = "Insert";
     
     
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_ItemTaskStepResult = "ItemTaskStepResult";
     
    protected String id;
    protected String description;
    protected int complianceType;
    public static final int COMPLIANCETYPE_HIGH = 0;
    public static final int COMPLIANCETYPE_MEDIUM = 1;
    public static final int COMPLIANCETYPE_LOW = 2;
    public static final Hub hubComplianceType;
    static {
        hubComplianceType = new Hub(String.class);
        hubComplianceType.addElement("High");
        hubComplianceType.addElement("Medium");
        hubComplianceType.addElement("Low");
    }
    protected int seq;
    protected boolean insert;
     
    // Links to other objects.
    protected transient Item item;
    protected transient ItemTaskStepResult itemTaskStepResult;
     
     
    public RecommendItem() {
    }
     
    public RecommendItem(String id) {
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
    
     
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        String old = this.description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    public int getComplianceType() {
        return complianceType;
    }
    public void setComplianceType(int newValue) {
        int old = this.complianceType;
        this.complianceType = newValue;
        firePropertyChange(PROPERTY_ComplianceType, old, this.complianceType);
    }
    public static Hub getComplianceTypes() {
        return hubComplianceType;
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public boolean getInsert() {
        return insert;
    }
    public void setInsert(boolean newValue) {
        boolean old = this.insert;
        this.insert = newValue;
        firePropertyChange(PROPERTY_Insert, old, this.insert);
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
     
    public ItemTaskStepResult getItemTaskStepResult() {
        if (itemTaskStepResult == null) {
            itemTaskStepResult = (ItemTaskStepResult) getObject(PROPERTY_ItemTaskStepResult);
        }
        return itemTaskStepResult;
    }
    
    public void setItemTaskStepResult(ItemTaskStepResult newValue) {
        ItemTaskStepResult old = this.itemTaskStepResult;
        this.itemTaskStepResult = newValue;
        firePropertyChange(PROPERTY_ItemTaskStepResult, old, this.itemTaskStepResult);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_RecommendItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTaskStepResult, ItemTaskStepResult.class, OALinkInfo.ONE, false, false, ItemTaskStepResult.PROPERTY_RecommendItems));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
