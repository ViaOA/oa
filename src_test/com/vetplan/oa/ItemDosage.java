package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ItemDosage extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_MinAge = "MinAge";
    public static final String PROPERTY_MaxAge = "MaxAge";
    public static final String PROPERTY_UomAge = "UomAge";
    public static final String PROPERTY_Amount = "Amount";
    public static final String PROPERTY_MinWeight = "MinWeight";
    public static final String PROPERTY_MaxWeight = "MaxWeight";
    public static final String PROPERTY_UomWeight = "UomWeight";
     
     
    public static final String PROPERTY_ItemProduct = "ItemProduct";
    public static final String PROPERTY_ProductPackage = "ProductPackage";
     
    protected String id;
    protected int minAge;
    protected int maxAge;
    protected int uomAge;
    public static final int UOMAGE_DAY = 0;
    public static final int UOMAGE_WEEK = 1;
    public static final int UOMAGE_MONTH = 2;
    public static final int UOMAGE_YEAR = 3;
    public static final Hub hubUomAge;
    static {
        hubUomAge = new Hub(String.class);
        hubUomAge.addElement("Day");
        hubUomAge.addElement("Week");
        hubUomAge.addElement("Month");
        hubUomAge.addElement("Year");
    }
    protected int amount;
    protected int minWeight;
    protected int maxWeight;
    protected int uomWeight;
    public static final int UOMWEIGHT_MILIGRAM = 0;
    public static final int UOMWEIGHT_GRAM = 1;
    public static final int UOMWEIGHT_KILOGRAM = 2;
    public static final int UOMWEIGHT_OUNCE = 3;
    public static final int UOMWEIGHT_POUND = 4;
    public static final Hub hubUomWeight;
    static {
        hubUomWeight = new Hub(String.class);
        hubUomWeight.addElement("Miligram");
        hubUomWeight.addElement("Gram");
        hubUomWeight.addElement("Kilogram");
        hubUomWeight.addElement("Ounce");
        hubUomWeight.addElement("Pound");
    }
     
    // Links to other objects.
    protected transient ItemProduct itemProduct;
    protected transient ProductPackage productPackage;
     
     
    public ItemDosage() {
    }
     
    public ItemDosage(String id) {
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
    
     
    public int getMinAge() {
        return minAge;
    }
    public void setMinAge(int newValue) {
        int old = this.minAge;
        this.minAge = newValue;
        firePropertyChange(PROPERTY_MinAge, old, this.minAge);
    }
    
     
    public int getMaxAge() {
        return maxAge;
    }
    public void setMaxAge(int newValue) {
        int old = this.maxAge;
        this.maxAge = newValue;
        firePropertyChange(PROPERTY_MaxAge, old, this.maxAge);
    }
    
     
    public int getUomAge() {
        return uomAge;
    }
    public void setUomAge(int newValue) {
        int old = this.uomAge;
        this.uomAge = newValue;
        firePropertyChange(PROPERTY_UomAge, old, this.uomAge);
    }
    public static Hub getUomAges() {
        return hubUomAge;
    }
    
     
    public int getAmount() {
        return amount;
    }
    public void setAmount(int newValue) {
        int old = this.amount;
        this.amount = newValue;
        firePropertyChange(PROPERTY_Amount, old, this.amount);
    }
    
     
    public int getMinWeight() {
        return minWeight;
    }
    public void setMinWeight(int newValue) {
        int old = this.minWeight;
        this.minWeight = newValue;
        firePropertyChange(PROPERTY_MinWeight, old, this.minWeight);
    }
    
     
    public int getMaxWeight() {
        return maxWeight;
    }
    public void setMaxWeight(int newValue) {
        int old = this.maxWeight;
        this.maxWeight = newValue;
        firePropertyChange(PROPERTY_MaxWeight, old, this.maxWeight);
    }
    
     
    public int getUomWeight() {
        return uomWeight;
    }
    public void setUomWeight(int newValue) {
        int old = this.uomWeight;
        this.uomWeight = newValue;
        firePropertyChange(PROPERTY_UomWeight, old, this.uomWeight);
    }
    public static Hub getUomWeights() {
        return hubUomWeight;
    }
    
     
    public ItemProduct getItemProduct() {
        if (itemProduct == null) {
            itemProduct = (ItemProduct) getObject(PROPERTY_ItemProduct);
        }
        return itemProduct;
    }
    
    public void setItemProduct(ItemProduct newValue) {
        ItemProduct old = this.itemProduct;
        this.itemProduct = newValue;
        firePropertyChange(PROPERTY_ItemProduct, old, this.itemProduct);
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ItemProduct, ItemProduct.class, OALinkInfo.ONE, false, false, ItemProduct.PROPERTY_ItemDosages));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ProductPackage, ProductPackage.class, OALinkInfo.ONE, false, false, ProductPackage.PROPERTY_ItemDosages));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
