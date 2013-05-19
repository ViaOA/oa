package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class LabTest extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Uom = "Uom";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_LabTestSpecies = "LabTestSpecies";
    public static final String PROPERTY_Lab = "Lab";
    public static final String PROPERTY_LabTestType = "LabTestType";
     
    protected String id;
    protected String pmsId;
    protected String name;
    protected String description;
    protected String uom;
    protected int seq;
     
    // Links to other objects.
    protected transient Hub hubLabTestSpecies;
    protected transient Lab lab;
    protected transient LabTestType labTestType;
     
     
    public LabTest() {
    }
     
    public LabTest(String id) {
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
    
     
    public String getUom() {
        return uom;
    }
    public void setUom(String newValue) {
        String old = this.uom;
        this.uom = newValue;
        firePropertyChange(PROPERTY_Uom, old, this.uom);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public Hub getLabTestSpecies() {
        if (hubLabTestSpecies == null) {
            hubLabTestSpecies = getHub(PROPERTY_LabTestSpecies);
        }
        return hubLabTestSpecies;
    }
    
     
    public Lab getLab() {
        if (lab == null) {
            lab = (Lab) getObject(PROPERTY_Lab);
        }
        return lab;
    }
    
    public void setLab(Lab newValue) {
        Lab old = this.lab;
        this.lab = newValue;
        firePropertyChange(PROPERTY_Lab, old, this.lab);
    }
     
    public LabTestType getLabTestType() {
        if (labTestType == null) {
            labTestType = (LabTestType) getObject(PROPERTY_LabTestType);
        }
        return labTestType;
    }
    
    public void setLabTestType(LabTestType newValue) {
        LabTestType old = this.labTestType;
        this.labTestType = newValue;
        firePropertyChange(PROPERTY_LabTestType, old, this.labTestType);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestSpecies, LabTestSpecies.class, OALinkInfo.MANY, true, true, LabTestSpecies.PROPERTY_LabTest));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Lab, Lab.class, OALinkInfo.ONE, false, false, Lab.PROPERTY_LabTests));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestType, LabTestType.class, OALinkInfo.ONE, false, false, LabTestType.PROPERTY_LabTests));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
