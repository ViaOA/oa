package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class Section extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Heading = "Heading";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Columns = "Columns";
    public static final String PROPERTY_Rows = "Rows";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_TemplateRow = "TemplateRow";
    public static final String PROPERTY_SectionItems = "SectionItems";
     
    protected String id;
    protected String name;
    protected String heading;
    protected String description;
    protected String gifFileName;
    protected int columns;
    protected int rows;
    protected int seq;
     
    // Links to other objects.
    protected transient TemplateRow templateRow;
    protected transient Hub hubSectionItems;
     
     
    public Section() {
    }
     
    public Section(String id) {
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
    
     
    public String getHeading() {
        return heading;
    }
    public void setHeading(String newValue) {
        String old = this.heading;
        this.heading = newValue;
        firePropertyChange(PROPERTY_Heading, old, this.heading);
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
    
     
    public int getColumns() {
        return columns;
    }
    public void setColumns(int newValue) {
        int old = this.columns;
        this.columns = newValue;
        firePropertyChange(PROPERTY_Columns, old, this.columns);
    }
    
     
    public int getRows() {
        return rows;
    }
    public void setRows(int newValue) {
        int old = this.rows;
        this.rows = newValue;
        firePropertyChange(PROPERTY_Rows, old, this.rows);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public TemplateRow getTemplateRow() {
        if (templateRow == null) {
            templateRow = (TemplateRow) getObject(PROPERTY_TemplateRow);
        }
        return templateRow;
    }
    
    public void setTemplateRow(TemplateRow newValue) {
        TemplateRow old = this.templateRow;
        this.templateRow = newValue;
        firePropertyChange(PROPERTY_TemplateRow, old, this.templateRow);
    }
     
    public Hub getSectionItems() {
        if (hubSectionItems == null) {
            hubSectionItems = getHub(PROPERTY_SectionItems);
        }
        return hubSectionItems;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TemplateRow, TemplateRow.class, OALinkInfo.ONE, false, false, TemplateRow.PROPERTY_Sections));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItems, SectionItem.class, OALinkInfo.MANY, false, false, SectionItem.PROPERTY_Section));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
