package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ProductCategory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
     
     
    public static final String PROPERTY_Products = "Products";
    public static final String PROPERTY_ParentProductCategory = "ParentProductCategory";
    public static final String PROPERTY_ProductCategories = "ProductCategories";
     
    protected String id;
    protected String name;
    protected int seq;
     
    // Links to other objects.
    protected transient Hub hubProducts;
    protected transient ProductCategory parentProductCategory;
    protected transient Hub hubProductCategories;
     
     
    public ProductCategory() {
    }
     
    public ProductCategory(String id) {
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
    
     
    public Hub getProducts() {
        if (hubProducts == null) {
            hubProducts = getHub(PROPERTY_Products);
        }
        return hubProducts;
    }
    
     
    public ProductCategory getParentProductCategory() {
        if (parentProductCategory == null) {
            parentProductCategory = (ProductCategory) getObject(PROPERTY_ParentProductCategory);
        }
        return parentProductCategory;
    }
    
    public void setParentProductCategory(ProductCategory newValue) {
        ProductCategory old = this.parentProductCategory;
        this.parentProductCategory = newValue;
        firePropertyChange(PROPERTY_ParentProductCategory, old, this.parentProductCategory);
    }
     
    public Hub getProductCategories() {
        if (hubProductCategories == null) {
            hubProductCategories = getHub(PROPERTY_ProductCategories, "seq");
            hubProductCategories.setAutoSequence("seq");
        }
        return hubProductCategories;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Products, Product.class, OALinkInfo.MANY, false, false, Product.PROPERTY_ProductCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentProductCategory, ProductCategory.class, OALinkInfo.ONE, false, false, ProductCategory.PROPERTY_ProductCategories));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductCategories, ProductCategory.class, OALinkInfo.MANY, true, true, ProductCategory.PROPERTY_ParentProductCategory));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
