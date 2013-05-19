package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemProduct extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
     
     
    public static final String PROPERTY_Item = "Item";
    public static final String PROPERTY_ItemDosages = "ItemDosages";
    public static final String PROPERTY_Product = "Product";
    public static final String PROPERTY_ProductPackage = "ProductPackage";
     
    protected String id;
     
    // Links to other objects.
    protected transient Item item;
    protected transient Hub hubItemDosages;
    protected transient Product product;
    protected transient ProductPackage productPackage;
     
     
    public ItemProduct() {
    }
     
    public ItemProduct(String id) {
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
    
     
    public Item getItem() {
        if (item == null) {
            item = (Item) getObject(PROPERTY_Item);
        }
        return item;
    }
    
    public void setItem(Item newValue) {
        Item old = this.item;
        this.item = newValue;
        firePropertyChange(PROPERTY_Item, old, this.item);
    }
     
    public Hub getItemDosages() {
        if (hubItemDosages == null) {
            hubItemDosages = getHub(PROPERTY_ItemDosages);
        }
        return hubItemDosages;
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Item, Item.class, OALinkInfo.ONE, false, false, Item.PROPERTY_ItemProducts));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemDosages, ItemDosage.class, OALinkInfo.MANY, true, true, ItemDosage.PROPERTY_ItemProduct));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Product, Product.class, OALinkInfo.ONE, false, false, Product.PROPERTY_ItemProducts));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductPackage, ProductPackage.class, OALinkInfo.ONE, false, false, ProductPackage.PROPERTY_ItemProducts));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
