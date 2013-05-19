package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class TemplateRow extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Heading = "Heading";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Template = "Template";
    public static final String PROPERTY_Sections = "Sections";
     
    protected String id;
    protected String name;
    protected String heading;
    protected int seq;
     
    // Links to other objects.
    protected transient Template template;
    protected transient Hub hubSections;
     
     
    public TemplateRow() {
    }
     
    public TemplateRow(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public Template getTemplate() {
        if (template == null) {
            template = (Template) getObject(PROPERTY_Template);
        }
        return template;
    }
    
    public void setTemplate(Template newValue) {
        Template old = this.template;
        this.template = newValue;
        firePropertyChange(PROPERTY_Template, old, this.template);
    }
     
    public Hub getSections() {
        if (hubSections == null) {
            hubSections = getHub(PROPERTY_Sections, "seq");
            hubSections.setAutoSequence("seq");
        }
        return hubSections;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Template, Template.class, OALinkInfo.ONE, false, false, Template.PROPERTY_TemplateRows));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Sections, Section.class, OALinkInfo.MANY, false, false, Section.PROPERTY_TemplateRow, true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
