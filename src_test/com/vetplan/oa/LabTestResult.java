package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class LabTestResult extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_LowRange = "LowRange";
    public static final String PROPERTY_HighRange = "HighRange";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_LabTestSpecies = "LabTestSpecies";
    public static final String PROPERTY_LabTestResultType = "LabTestResultType";
     
    protected String id;
    protected double lowRange;
    protected double highRange;
    protected int seq;
     
    // Links to other objects.
    protected transient LabTestSpecies labTestSpecies;
    protected transient LabTestResultType labTestResultType;
     
     
    public LabTestResult() {
    }
     
    public LabTestResult(String id) {
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
    
     
    public double getLowRange() {
        return lowRange;
    }
    public void setLowRange(double newValue) {
        double old = this.lowRange;
        this.lowRange = newValue;
        firePropertyChange(PROPERTY_LowRange, old, this.lowRange);
    }
    
     
    public double getHighRange() {
        return highRange;
    }
    public void setHighRange(double newValue) {
        double old = this.highRange;
        this.highRange = newValue;
        firePropertyChange(PROPERTY_HighRange, old, this.highRange);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
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
     
    public LabTestResultType getLabTestResultType() {
        if (labTestResultType == null) {
            labTestResultType = (LabTestResultType) getObject(PROPERTY_LabTestResultType);
        }
        return labTestResultType;
    }
    
    public void setLabTestResultType(LabTestResultType newValue) {
        LabTestResultType old = this.labTestResultType;
        this.labTestResultType = newValue;
        firePropertyChange(PROPERTY_LabTestResultType, old, this.labTestResultType);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestSpecies, LabTestSpecies.class, OALinkInfo.ONE, false, false, LabTestSpecies.PROPERTY_LabTestResults));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestResultType, LabTestResultType.class, OALinkInfo.ONE, false, false, LabTestResultType.PROPERTY_LabTestResults));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
