package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ServiceProduct extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Qty = "Qty";
    public static final String PROPERTY_Note = "Note";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Service = "Service";
    public static final String PROPERTY_ProductPackage = "ProductPackage";
     
    protected String id;
    protected double qty;
    protected String note;
    protected int seq;
     
    // Links to other objects.
    protected transient Service service;
    protected transient ProductPackage productPackage;
     
     
    public ServiceProduct() {
    }
     
    public ServiceProduct(String id) {
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
    
     
    public double getQty() {
        return qty;
    }
    public void setQty(double newValue) {
        double old = this.qty;
        this.qty = newValue;
        firePropertyChange(PROPERTY_Qty, old, this.qty);
    }
    
     
    public String getNote() {
        return note;
    }
    public void setNote(String newValue) {
        String old = this.note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    public int getSeq() {
        return seq;
    }
    public void setSeq(int newValue) {
        int old = this.seq;
        this.seq = newValue;
        firePropertyChange(PROPERTY_Seq, old, this.seq);
    }
    
     
    public Service getService() {
        if (service == null) {
            service = (Service) getObject(PROPERTY_Service);
        }
        return service;
    }
    
    public void setService(Service newValue) {
        Service old = this.service;
        this.service = newValue;
        firePropertyChange(PROPERTY_Service, old, this.service);
    }
     
    public ProductPackage getProductPackage() {
        if (productPackage == null) {
            productPackage = (ProductPackage) getObject(PROPERTY_ProductPackage);
        }
        return productPackage;
    }
    
    public void setProductPackage(ProductPackage newValue) {
        ProductPackage old = this.productPackage;
        this.productPackage = newValue;
        firePropertyChange(PROPERTY_ProductPackage, old, this.productPackage);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Service, Service.class, OALinkInfo.ONE, false, false, Service.PROPERTY_ServiceProducts));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductPackage, ProductPackage.class, OALinkInfo.ONE, false, false, ProductPackage.PROPERTY_ServiceProducts));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
