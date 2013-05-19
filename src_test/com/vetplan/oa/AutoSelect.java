package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class AutoSelect extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_ExamItemStatus = "ExamItemStatus";
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_SectionItem = "SectionItem";
    public static final String PROPERTY_SubItem = "SubItem";
     
    protected String id;
    protected int seq;
     
    // Links to other objects.
    protected transient ExamItemStatus examItemStatus;
    protected transient Item item;
    protected transient SectionItem sectionItem;
    protected transient Item subItem;
     
     
    public AutoSelect() {
    }
     
    public AutoSelect(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public ExamItemStatus getExamItemStatus() {
        if (examItemStatus == null) {
            examItemStatus = (ExamItemStatus) getObject(PROPERTY_ExamItemStatus);
        }
        return examItemStatus;
    }
    
    public void setExamItemStatus(ExamItemStatus newValue) {
        ExamItemStatus old = this.examItemStatus;
        this.examItemStatus = newValue;
        firePropertyChange(PROPERTY_ExamItemStatus, old, this.examItemStatus);
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
     
    public SectionItem getSectionItem() {
        if (sectionItem == null) {
            sectionItem = (SectionItem) getObject(PROPERTY_SectionItem);
        }
        return sectionItem;
    }
    
    public void setSectionItem(SectionItem newValue) {
        SectionItem old = this.sectionItem;
        this.sectionItem = newValue;
        firePropertyChange(PROPERTY_SectionItem, old, this.sectionItem);
    }
     
    public Item getSubItem() {
        if (subItem == null) {
            subItem = (Item) getObject(PROPERTY_SubItem);
        }
        return subItem;
    }
    
    public void setSubItem(Item newValue) {
        Item old = this.subItem;
        this.subItem = newValue;
        firePropertyChange(PROPERTY_SubItem, old, this.subItem);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemStatus, ExamItemStatus.class, OALinkInfo.ONE, false, false, ExamItemStatus.PROPERTY_AutoSelects));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_AutoSelects));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItem, SectionItem.class, OALinkInfo.ONE, false, false, SectionItem.PROPERTY_AutoSelects));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SubItem, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_AutoSelectSubitems));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
