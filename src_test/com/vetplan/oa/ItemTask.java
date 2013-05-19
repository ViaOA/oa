package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemTask extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Required = "Required";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_UseItem = "UseItem";
    public static final String PROPERTY_ItemTaskSteps = "ItemTaskSteps";
    public static final String PROPERTY_SchDateTimes = "SchDateTimes";
     
    protected String id;
    protected String name;
    protected String description;
    protected boolean required;
    protected int seq;
     
    // Links to other objects.
    protected transient Item item;
    protected transient Item useItem;
    protected transient Hub hubItemTaskSteps;
    protected transient SchDateTimes schDateTimes;
     
     
    public ItemTask() {
    }
     
    public ItemTask(String id) {
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
    
     
    public boolean getRequired() {
        return required;
    }
    public void setRequired(boolean newValue) {
        boolean old = this.required;
        this.required = newValue;
        firePropertyChange(PROPERTY_Required, old, this.required);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
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
     
    public Item getUseItem() {
        if (useItem == null) {
            useItem = (Item) getObject(PROPERTY_UseItem);
        }
        return useItem;
    }
    
    public void setUseItem(Item newValue) {
        Item old = this.useItem;
        this.useItem = newValue;
        firePropertyChange(PROPERTY_UseItem, old, this.useItem);
    }
     
    public Hub getItemTaskSteps() {
        if (hubItemTaskSteps == null) {
            hubItemTaskSteps = getHub(PROPERTY_ItemTaskSteps, "seq");
            hubItemTaskSteps.setAutoSequence("seq");
        }
        return hubItemTaskSteps;
    }
    
     
    public SchDateTimes getSchDateTimes() {
        if (schDateTimes == null) {
            schDateTimes = (SchDateTimes) getObject(PROPERTY_SchDateTimes);
        }
        return schDateTimes;
    }
    
    public void setSchDateTimes(SchDateTimes newValue) {
        SchDateTimes old = this.schDateTimes;
        this.schDateTimes = newValue;
        firePropertyChange(PROPERTY_SchDateTimes, old, this.schDateTimes);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_ItemTasks));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_UseItem, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_UseItemTasks));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTaskSteps, ItemTaskStep.class, OALinkInfo.MANY, true, true, ItemTaskStep.PROPERTY_ItemTask, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SchDateTimes, SchDateTimes.class, OALinkInfo.ONE, true, true, SchDateTimes.PROPERTY_ItemTasks));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
