package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ExamItemStatus extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_ShowOnReport = "ShowOnReport";
    public static final String PROPERTY_Performed = "Performed";
    public static final String PROPERTY_Type = "Type";
     
     
    public static final String PROPERTY_ExamItems = "ExamItems";
    public static final String PROPERTY_ExamItemHistories = "ExamItemHistories";
    public static final String PROPERTY_SectionItems = "SectionItems";
    public static final String PROPERTY_AutoSelects = "AutoSelects";
     
    protected String id;
    protected String name;
    protected String description;
    protected String gifFileName;
    protected int seq;
    protected boolean showOnReport;
    protected boolean performed;
    protected int type;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_TODO = 1;
    public static final int TYPE_DONE = 2;
    public static final int TYPE_RECOMMEND = 3;
    public static final int TYPE_DECLINED = 4;
    public static final int TYPE_DDX = 5;
    public static final int TYPE_DX = 6;
    public static final int TYPE_PENDING = 7;
    public static final int TYPE_AGREED = 8;
    public static final int TYPE_LATER = 9;
    public static final int TYPE_DISCUSS = 10;
    public static final int TYPE_DISCUSSED = 11;
    public static final int TYPE_CALLCLIENT = 12;
    public static final int TYPE_CALLEDCLIENT = 13;
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("None");
        hubType.addElement("Todo");
        hubType.addElement("Done");
        hubType.addElement("Recommend");
        hubType.addElement("Declined");
        hubType.addElement("Ddx");
        hubType.addElement("Dx");
        hubType.addElement("Pending");
        hubType.addElement("Agreed");
        hubType.addElement("Later");
        hubType.addElement("Discuss");
        hubType.addElement("Discussed");
        hubType.addElement("Callclient");
        hubType.addElement("Calledclient");
    }
     
    // Links to other objects.
     
     
    public ExamItemStatus() {
    }
     
    public ExamItemStatus(String id) {
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
    
     
    public boolean getShowOnReport() {
        return showOnReport;
    }
    public void setShowOnReport(boolean newValue) {
        boolean old = this.showOnReport;
        this.showOnReport = newValue;
        firePropertyChange(PROPERTY_ShowOnReport, old, this.showOnReport);
    }
    
     
    public boolean getPerformed() {
        return performed;
    }
    public void setPerformed(boolean newValue) {
        boolean old = this.performed;
        this.performed = newValue;
        firePropertyChange(PROPERTY_Performed, old, this.performed);
    }
    
     
    public int getType() {
        return type;
    }
    
    public int debug_setTypeCnt;
    public void setType(int newValue) {
        int old = this.type;
        this.type = newValue;
        debug_setTypeCnt++;
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, false, false, ExamItem.PROPERTY_ExamItemStatus));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamItemHistories, ExamItemHistory.class, OALinkInfo.MANY, false, false, ExamItemHistory.PROPERTY_ExamItemStatus));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItems, SectionItem.class, OALinkInfo.MANY, false, false, SectionItem.PROPERTY_ExamItemStatus));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AutoSelects, AutoSelect.class, OALinkInfo.MANY, false, false, AutoSelect.PROPERTY_ExamItemStatus));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
