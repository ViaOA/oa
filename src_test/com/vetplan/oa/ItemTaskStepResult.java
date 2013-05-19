package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemTaskStepResult extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Value = "Value";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Min = "Min";
    public static final String PROPERTY_Max = "Max";
     
     
    public static final String PROPERTY_RecommendItems = "RecommendItems";
    public static final String PROPERTY_ItemTaskStep = "ItemTaskStep";
     
    protected String id;
    protected String name;
    protected String value;
    protected int seq;
    protected String min;
    protected String max;
     
    // Links to other objects.
    protected transient Hub hubRecommendItems;
    protected transient ItemTaskStep itemTaskStep;
     
     
    public ItemTaskStepResult() {
    }
     
    public ItemTaskStepResult(String id) {
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
    
     
    public String getValue() {
        return value;
    }
    public void setValue(String newValue) {
        String old = this.value;
        this.value = newValue;
        firePropertyChange(PROPERTY_Value, old, this.value);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public String getMin() {
        return min;
    }
    public void setMin(String newValue) {
        String old = this.min;
        this.min = newValue;
        firePropertyChange(PROPERTY_Min, old, this.min);
    }
    
     
    public String getMax() {
        return max;
    }
    public void setMax(String newValue) {
        String old = this.max;
        this.max = newValue;
        firePropertyChange(PROPERTY_Max, old, this.max);
    }
    
     
    public Hub getRecommendItems() {
        if (hubRecommendItems == null) {
            hubRecommendItems = getHub(PROPERTY_RecommendItems, "seq");
            hubRecommendItems.setAutoSequence("seq");
        }
        return hubRecommendItems;
    }
    
     
    public ItemTaskStep getItemTaskStep() {
        if (itemTaskStep == null) {
            itemTaskStep = (ItemTaskStep) getObject(PROPERTY_ItemTaskStep);
        }
        return itemTaskStep;
    }
    
    public void setItemTaskStep(ItemTaskStep newValue) {
        ItemTaskStep old = this.itemTaskStep;
        this.itemTaskStep = newValue;
        firePropertyChange(PROPERTY_ItemTaskStep, old, this.itemTaskStep);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_RecommendItems, RecommendItem.class, OALinkInfo.MANY, true, true, RecommendItem.PROPERTY_ItemTaskStepResult));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemTaskStep, ItemTaskStep.class, OALinkInfo.ONE, false, false, ItemTaskStep.PROPERTY_ItemTaskStepResults));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
