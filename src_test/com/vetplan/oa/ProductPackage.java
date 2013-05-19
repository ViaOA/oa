package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ProductPackage extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_Amount = "Amount";
    public static final String PROPERTY_Uom = "Uom";
    public static final String PROPERTY_GifFileName = "GifFileName";
    public static final String PROPERTY_Label = "Label";
    public static final String PROPERTY_ManCode = "ManCode";
    public static final String PROPERTY_PmsId = "PmsId";
     
    public static final String PROPERTY_FullName = "FullName";
     
    public static final String PROPERTY_Product = "Product";
    public static final String PROPERTY_ItemDosages = "ItemDosages";
    public static final String PROPERTY_ServiceProducts = "ServiceProducts";
    public static final String PROPERTY_ItemProducts = "ItemProducts";
     
    protected String id;
    protected String name;
    protected String description;
    protected int amount;
    protected int uom;
    public static final int UOM_EACH = 0;
    public static final int UOM_MILIGRAM = 1;
    public static final int UOM_GRAM = 2;
    public static final int UOM_KILOGRAM = 3;
    public static final int UOM_OUNCE = 4;
    public static final int UOM_POUND = 5;
    public static final Hub hubUom;
    static {
        hubUom = new Hub(String.class);
        hubUom.addElement("Each");
        hubUom.addElement("Miligram");
        hubUom.addElement("Gram");
        hubUom.addElement("Kilogram");
        hubUom.addElement("Ounce");
        hubUom.addElement("Pound");
    }
    protected String gifFileName;
    protected String label;
    protected String manCode;
    protected String pmsId;
     
    // Links to other objects.
    protected transient Product product;
    protected transient Hub hubItemDosages;
    protected transient Hub hubServiceProducts;
     
     
    public ProductPackage() {
    }
     
    public ProductPackage(String id) {
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
    
     
    public int getAmount() {
        return amount;
    }
    public void setAmount(int newValue) {
        int old = this.amount;
        this.amount = newValue;
        firePropertyChange(PROPERTY_Amount, old, this.amount);
    }
    
     
    public int getUom() {
        return uom;
    }
    public void setUom(int newValue) {
        int old = this.uom;
        this.uom = newValue;
        firePropertyChange(PROPERTY_Uom, old, this.uom);
    }
    public static Hub getUoms() {
        return hubUom;
    }
    
     
    public String getGifFileName() {
        return gifFileName;
    }
    public void setGifFileName(String newValue) {
        String old = this.gifFileName;
        this.gifFileName = newValue;
        firePropertyChange(PROPERTY_GifFileName, old, this.gifFileName);
    }
    
     
    public String getLabel() {
        return label;
    }
    public void setLabel(String newValue) {
        String old = this.label;
        this.label = newValue;
        firePropertyChange(PROPERTY_Label, old, this.label);
    }
    
     
    public String getManCode() {
        return manCode;
    }
    public void setManCode(String newValue) {
        String old = this.manCode;
        this.manCode = newValue;
        firePropertyChange(PROPERTY_ManCode, old, this.manCode);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public String getFullName() {
        String s = "";
        getProduct();
        if (product != null) {
            s = product.getName();
            if (s == null) {
                s = product.getId();
                if (s == null) s = "";
            }
        }
        if (name != null) s += " " + name;        
        return s;
    }
     
    public Product getProduct() {
        if (product == null) {
            product = (Product) getObject(PROPERTY_Product);
        }
        return product;
    }
    
    public void setProduct(Product newValue) {
        Product old = this.product;
        this.product = newValue;
        firePropertyChange(PROPERTY_Product, old, this.product);
    }
     
    public Hub getItemDosages() {
        if (hubItemDosages == null) {
            hubItemDosages = getHub(PROPERTY_ItemDosages);
        }
        return hubItemDosages;
    }
    
     
    public Hub getServiceProducts() {
        if (hubServiceProducts == null) {
            hubServiceProducts = getHub(PROPERTY_ServiceProducts);
        }
        return hubServiceProducts;
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Product, Product.class, OALinkInfo.ONE, false, false, Product.PROPERTY_ProductPackages));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemDosages, ItemDosage.class, OALinkInfo.MANY, false, false, ItemDosage.PROPERTY_ProductPackage));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ServiceProducts, ServiceProduct.class, OALinkInfo.MANY, false, false, ServiceProduct.PROPERTY_ProductPackage));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemProducts, ItemProduct.class, OALinkInfo.MANY, false, false, ItemProduct.PROPERTY_ProductPackage));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_FullName, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
