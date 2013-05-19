package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Product extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_DateCreated = "DateCreated";
    public static final String PROPERTY_Warning = "Warning";
    public static final String PROPERTY_Label = "Label";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_ItemProducts = "ItemProducts";
    public static final String PROPERTY_Manufacturer = "Manufacturer";
    public static final String PROPERTY_ProductCategories = "ProductCategories";
    public static final String PROPERTY_ProductPackages = "ProductPackages";
     
    protected String id;
    protected String name;
    protected String description;
    protected OADate dateCreated;
    protected String warning;
    protected String label;
     
    // Links to other objects.
    protected transient Hub hubItemProducts;
    protected transient Manufacturer manufacturer;
    protected transient Hub hubProductCategories;
    protected transient Hub hubProductPackages;
     
     
    public Product() {
    }
     
    public Product(String id) {
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
    
     
    public String getWarning() {
        return warning;
    }
    public void setWarning(String newValue) {
        String old = this.warning;
        this.warning = newValue;
        firePropertyChange(PROPERTY_Warning, old, this.warning);
    }
    
     
    public String getLabel() {
        return label;
    }
    public void setLabel(String newValue) {
        String old = this.label;
        this.label = newValue;
        firePropertyChange(PROPERTY_Label, old, this.label);
    }
    
     
    public String getFullName() {
        String fullName = getName(); 
        if (fullName == null) fullName = "";
        
        Manufacturer man = getManufacturer();
        if (man != null) fullName += " (" + man.getName() + ")";
        return fullName;
    }
     
    public Hub getItemProducts() {
        if (hubItemProducts == null) {
            hubItemProducts = getHub(PROPERTY_ItemProducts);
        }
        return hubItemProducts;
    }
    
     
    public Manufacturer getManufacturer() {
        if (manufacturer == null) {
            manufacturer = (Manufacturer) getObject(PROPERTY_Manufacturer);
        }
        return manufacturer;
    }
    
    public void setManufacturer(Manufacturer newValue) {
        Manufacturer old = this.manufacturer;
        this.manufacturer = newValue;
        firePropertyChange(PROPERTY_Manufacturer, old, this.manufacturer);
    }
     
    public Hub getProductCategories() {
        if (hubProductCategories == null) {
            hubProductCategories = getHub(PROPERTY_ProductCategories);
        }
        return hubProductCategories;
    }
    
     
    public Hub getProductPackages() {
        if (hubProductPackages == null) {
            hubProductPackages = getHub(PROPERTY_ProductPackages);
        }
        return hubProductPackages;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemProducts, ItemProduct.class, OALinkInfo.MANY, true, true, ItemProduct.PROPERTY_Product));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Manufacturer, Manufacturer.class, OALinkInfo.ONE, false, false, Manufacturer.PROPERTY_Products));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductCategories, ProductCategory.class, OALinkInfo.MANY, false, false, ProductCategory.PROPERTY_Products));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductPackages, ProductPackage.class, OALinkInfo.MANY, false, false, ProductPackage.PROPERTY_Product));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
