package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
 
 
public class LabTestSpecies extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_LowRange = "LowRange";
    public static final String PROPERTY_HighRange = "HighRange";
     
     
    public static final String PROPERTY_Species = "Species";
    public static final String PROPERTY_LabTest = "LabTest";
    public static final String PROPERTY_LabTestResults = "LabTestResults";
    public static final String PROPERTY_SectionItems = "SectionItems";
     
    protected String id;
    protected double lowRange;
    protected double highRange;
     
    // Links to other objects.
    protected transient Species species;
    protected transient LabTest labTest;
    protected transient Hub hubLabTestResults;
    protected transient Hub hubSectionItems;
     
     
    public LabTestSpecies() {
    }
     
    public LabTestSpecies(String id) {
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
    
     
    public Species getSpecies() {
        if (species == null) {
            species = (Species) getObject(PROPERTY_Species);
        }
        return species;
    }
    
    public void setSpecies(Species newValue) {
        Species old = this.species;
        this.species = newValue;
        firePropertyChange(PROPERTY_Species, old, this.species);
    }
     
    public LabTest getLabTest() {
        if (labTest == null) {
            labTest = (LabTest) getObject(PROPERTY_LabTest);
        }
        return labTest;
    }
    
    public void setLabTest(LabTest newValue) {
        LabTest old = this.labTest;
        this.labTest = newValue;
        firePropertyChange(PROPERTY_LabTest, old, this.labTest);
    }
     
    public Hub getLabTestResults() {
        if (hubLabTestResults == null) {
            hubLabTestResults = getHub(PROPERTY_LabTestResults);
        }
        return hubLabTestResults;
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Species, Species.class, OALinkInfo.ONE, false, false, Species.PROPERTY_LabTestSpecies));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTest, LabTest.class, OALinkInfo.ONE, false, false, LabTest.PROPERTY_LabTestSpecies));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_LabTestResults, LabTestResult.class, OALinkInfo.MANY, true, true, LabTestResult.PROPERTY_LabTestSpecies));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_SectionItems, SectionItem.class, OALinkInfo.MANY, true, true, SectionItem.PROPERTY_LabTestSpecies));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
