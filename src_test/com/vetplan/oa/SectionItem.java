package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class SectionItem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_ItemName = "ItemName";
    public static final String PROPERTY_ItemSubItemFlag = "ItemSubItemFlag";
    public static final String PROPERTY_AutoCheckFlag = "AutoCheckFlag";
    public static final String PROPERTY_Type = "Type";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_ExamItems = "ExamItems";
    public static final String PROPERTY_ExamItemStatus = "ExamItemStatus";
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_LabTestSpecies = "LabTestSpecies";
    public static final String PROPERTY_AutoSelects = "AutoSelects";
    public static final String PROPERTY_Section = "Section";
     
    protected String id;
    protected int seq;
    protected String itemName;
    protected boolean itemSubItemFlag;
    protected boolean autoCheckFlag;
    protected int type;
     
    // Links to other objects.
    protected transient ExamItemStatus examItemStatus;
    protected transient Item item;
    protected transient LabTestSpecies labTestSpecies;
    protected transient Hub hubAutoSelects;
    protected transient Section section;
     
     
    public SectionItem() {
    }
     
    public SectionItem(String id) {
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
    
     
    public String getItemName() {
        return itemName;
    }
    public void setItemName(String newValue) {
        String old = this.itemName;
        this.itemName = newValue;
        firePropertyChange(PROPERTY_ItemName, old, this.itemName);
    }
    
     
    public boolean getItemSubItemFlag() {
        return itemSubItemFlag;
    }
    public void setItemSubItemFlag(boolean newValue) {
        boolean old = this.itemSubItemFlag;
        this.itemSubItemFlag = newValue;
        firePropertyChange(PROPERTY_ItemSubItemFlag, old, this.itemSubItemFlag);
    }
    
     
    public boolean getAutoCheckFlag() {
        return autoCheckFlag;
    }
    public void setAutoCheckFlag(boolean newValue) {
        boolean old = this.autoCheckFlag;
        this.autoCheckFlag = newValue;
        firePropertyChange(PROPERTY_AutoCheckFlag, old, this.autoCheckFlag);
    }
    
     
    public int getType() {
        return type;
    }
    public void setType(int newValue) {
        int old = this.type;
        this.type = newValue;
        firePropertyChange(PROPERTY_Type, old, this.type);
    }
    
     
    public String getFullName() {
        // CUSTOM CODE
        String s = getItemName();
        Section sec = getSection();
        if (sec != null) {
            s = sec.getName() + " / " + s;
            TemplateRow tr = sec.getTemplateRow();
            if (tr != null) {
                s = tr.getName() + " / " + s;
                Template t = tr.getTemplate();
                if (t != null) {
                    s = t.getName() + " / " + s;
                    String ss = "";
                    Hub h = t.getTemplateCategories();
                    for (int i=0; ;i++) {
                        TemplateCategory tc = (TemplateCategory) h.elementAt(i);
                        if (tc == null) break;
                        Species sp = tc.getSpecies();
                        if (ss.length() > 0) ss += ", ";
                        if (sp != null) ss += sp.getName();
                        ss += " (" + tc.getName() + ")";
                    }
                    s = ss + " " + s;
                
                }
            }
        }
        return s;
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
     
    public LabTestSpecies getLabTestSpecies() {
        if (labTestSpecies == null) {
            labTestSpecies = (LabTestSpecies) getObject(PROPERTY_LabTestSpecies);
        }
        return labTestSpecies;
    }
    
    public void setLabTestSpecies(LabTestSpecies newValue) {
        LabTestSpecies old = this.labTestSpecies;
        this.labTestSpecies = newValue;
        firePropertyChange(PROPERTY_LabTestSpecies, old, this.labTestSpecies);
    }
     
    public Hub getAutoSelects() {
        if (hubAutoSelects == null) {
            hubAutoSelects = getHub(PROPERTY_AutoSelects, "seq");
            hubAutoSelects.setAutoSequence("seq");
        }
        return hubAutoSelects;
    }
    
     
    public Section getSection() {
        if (section == null) {
            section = (Section) getObject(PROPERTY_Section);
        }
        return section;
    }
    
    public void setSection(Section newValue) {
        Section old = this.section;
        this.section = newValue;
        firePropertyChange(PROPERTY_Section, old, this.section);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, false, false, ExamItem.PROPERTY_SectionItem));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemStatus, ExamItemStatus.class, OALinkInfo.ONE, false, false, ExamItemStatus.PROPERTY_SectionItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_SectionItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestSpecies, LabTestSpecies.class, OALinkInfo.ONE, false, false, LabTestSpecies.PROPERTY_SectionItems));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AutoSelects, AutoSelect.class, OALinkInfo.MANY, true, true, AutoSelect.PROPERTY_SectionItem));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Section, Section.class, OALinkInfo.ONE, false, false, Section.PROPERTY_SectionItems));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
