package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ServiceCategory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Services = "Services";
    public static final String PROPERTY_ParentServiceCategory = "ParentServiceCategory";
    public static final String PROPERTY_ServiceCategories = "ServiceCategories";
     
    protected String id;
    protected String name;
    protected int seq;
     
    // Links to other objects.
    protected transient Hub hubServices;
    protected transient ServiceCategory parentServiceCategory;
    protected transient Hub hubServiceCategories;
     
     
    public ServiceCategory() {
    }
     
    public ServiceCategory(String id) {
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
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public Hub getServices() {
        if (hubServices == null) {
            hubServices = getHub(PROPERTY_Services, "seq");
            hubServices.setAutoSequence("seq");
        }
        return hubServices;
    }
    
     
    public ServiceCategory getParentServiceCategory() {
        if (parentServiceCategory == null) {
            parentServiceCategory = (ServiceCategory) getObject(PROPERTY_ParentServiceCategory);
        }
        return parentServiceCategory;
    }
    
    public void setParentServiceCategory(ServiceCategory newValue) {
        ServiceCategory old = this.parentServiceCategory;
        this.parentServiceCategory = newValue;
        firePropertyChange(PROPERTY_ParentServiceCategory, old, this.parentServiceCategory);
    }
     
    public Hub getServiceCategories() {
        if (hubServiceCategories == null) {
            hubServiceCategories = getHub(PROPERTY_ServiceCategories, "seq");
            hubServiceCategories.setAutoSequence("seq");
        }
        return hubServiceCategories;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Services, Service.class, OALinkInfo.MANY, true, true, Service.PROPERTY_ServiceCategory));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentServiceCategory, ServiceCategory.class, OALinkInfo.ONE, false, false, ServiceCategory.PROPERTY_ServiceCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ServiceCategories, ServiceCategory.class, OALinkInfo.MANY, true, true, ServiceCategory.PROPERTY_ParentServiceCategory));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
