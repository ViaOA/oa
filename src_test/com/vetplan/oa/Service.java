package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Service extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_DateCreated = "DateCreated";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_Seq = "Seq";
    public static final String PROPERTY_Seq2 = "Seq2";
     
     
    public static final String PROPERTY_Items = "Items";
    public static final String PROPERTY_ServiceCategory = "ServiceCategory";
    public static final String PROPERTY_ServiceProducts = "ServiceProducts";
    public static final String PROPERTY_ReverseServices = "ReverseServices";
    public static final String PROPERTY_Services = "Services";
     
    protected String id;
    protected String name;
    protected String description;
    protected OADate dateCreated;
    protected String pmsId;
    protected int seq;
    protected int seq2;
     
    // Links to other objects.
    protected transient Hub hubItems;
    protected transient ServiceCategory serviceCategory;
    protected transient Hub hubServiceProducts;
    protected transient Hub hubReverseServices;
    protected transient Hub hubServices;
     
     
    public Service() {
    }
     
    public Service(String id) {
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
    
     
    public OADate getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(OADate newValue) {
        OADate old = this.dateCreated;
        this.dateCreated = newValue;
        firePropertyChange(PROPERTY_DateCreated, old, this.dateCreated);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public int getSeq2() {
        return seq2;
    }
    public void setSeq2(int newValue) {
        int old = this.seq2;
        this.seq2 = newValue;
        firePropertyChange(PROPERTY_Seq2, old, this.seq2);
    }
    
     
    public Hub getItems() {
        if (hubItems == null) {
            hubItems = getHub(PROPERTY_Items);
        }
        return hubItems;
    }
    
     
    public ServiceCategory getServiceCategory() {
        if (serviceCategory == null) {
            serviceCategory = (ServiceCategory) getObject(PROPERTY_ServiceCategory);
        }
        return serviceCategory;
    }
    
    public void setServiceCategory(ServiceCategory newValue) {
        ServiceCategory old = this.serviceCategory;
        this.serviceCategory = newValue;
        firePropertyChange(PROPERTY_ServiceCategory, old, this.serviceCategory);
    }
     
    public Hub getServiceProducts() {
        if (hubServiceProducts == null) {
            hubServiceProducts = getHub(PROPERTY_ServiceProducts, "seq");
            hubServiceProducts.setAutoSequence("seq");
        }
        return hubServiceProducts;
    }
    
     
    public Hub getReverseServices() {
        if (hubReverseServices == null) {
            hubReverseServices = getHub(PROPERTY_ReverseServices);
        }
        return hubReverseServices;
    }
    
     
    public Hub getServices() {
        if (hubServices == null) {
            hubServices = getHub(PROPERTY_Services);
        }
        return hubServices;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Items, Item.class, OALinkInfo.MANY, true, true, Item.PROPERTY_Service));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ServiceCategory, ServiceCategory.class, OALinkInfo.ONE, false, false, ServiceCategory.PROPERTY_Services));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ServiceProducts, ServiceProduct.class, OALinkInfo.MANY, true, true, ServiceProduct.PROPERTY_Service));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ReverseServices, Service.class, OALinkInfo.MANY, false, false, Service.PROPERTY_Services));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Services, Service.class, OALinkInfo.MANY, false, false, Service.PROPERTY_ReverseServices));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
