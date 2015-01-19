// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
import com.viaoa.util.OADate;
 
@OAClass(
    shortName = "hoi",
    displayName = "Hifive Order Item",
    displayProperty = "product"
)
@OATable(
    indexes = {
        @OAIndex(name = "HifiveOrderItemHifiveOrder", columns = { @OAIndexColumn(name = "HifiveOrderId") })
    }
)
public class HifiveOrderItem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Quantity = "Quantity";
    public static final String P_Quantity = "Quantity";
    public static final String PROPERTY_PointsUsed = "PointsUsed";
    public static final String P_PointsUsed = "PointsUsed";
    public static final String PROPERTY_BillDate = "BillDate";
    public static final String P_BillDate = "BillDate";
    public static final String PROPERTY_PaidDate = "PaidDate";
    public static final String P_PaidDate = "PaidDate";
    public static final String PROPERTY_ItemSentDate = "ItemSentDate";
    public static final String P_ItemSentDate = "ItemSentDate";
    public static final String PROPERTY_ItemShippingInfo = "ItemShippingInfo";
    public static final String P_ItemShippingInfo = "ItemShippingInfo";
    public static final String PROPERTY_ItemLastStatusDate = "ItemLastStatusDate";
    public static final String P_ItemLastStatusDate = "ItemLastStatusDate";
    public static final String PROPERTY_ItemLastStatus = "ItemLastStatus";
    public static final String P_ItemLastStatus = "ItemLastStatus";
    public static final String PROPERTY_CompletedDate = "CompletedDate";
    public static final String P_CompletedDate = "CompletedDate";
     
     
    public static final String PROPERTY_HifiveOrder = "HifiveOrder";
    public static final String P_HifiveOrder = "HifiveOrder";
    public static final String PROPERTY_Product = "Product";
    public static final String P_Product = "Product";
     
    protected int id;
    protected OADate created;
    protected int seq;
    protected int quantity;
    protected double pointsUsed;
    protected OADate billDate;
    protected OADate paidDate;
    protected OADate itemSentDate;
    protected String itemShippingInfo;
    protected OADate itemLastStatusDate;
    protected String itemLastStatus;
    protected OADate completedDate;
     
    // Links to other objects.
    protected transient HifiveOrder hifiveOrder;
    protected transient Product product;
     
    public HifiveOrderItem() {
        if (!isLoading()) {
            setCreated(new OADate());
            setQuantity(1);
        }
    }
     
    public HifiveOrderItem(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(defaultValue = "new OADate()", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreated() {
        return created;
    }
    
    public void setCreated(OADate newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADate old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAProperty(defaultValue = "1", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int newValue) {
        fireBeforePropertyChange(P_Quantity, this.quantity, newValue);
        int old = quantity;
        this.quantity = newValue;
        firePropertyChange(P_Quantity, old, this.quantity);
    }
    @OAProperty(displayName = "Points Used", decimalPlaces = 2, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getPointsUsed() {
        return pointsUsed;
    }
    
    public void setPointsUsed(double newValue) {
        fireBeforePropertyChange(P_PointsUsed, this.pointsUsed, newValue);
        double old = pointsUsed;
        this.pointsUsed = newValue;
        firePropertyChange(P_PointsUsed, old, this.pointsUsed);
    }
    @OAProperty(displayName = "Bill Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getBillDate() {
        return billDate;
    }
    
    public void setBillDate(OADate newValue) {
        fireBeforePropertyChange(P_BillDate, this.billDate, newValue);
        OADate old = billDate;
        this.billDate = newValue;
        firePropertyChange(P_BillDate, old, this.billDate);
    }
    @OAProperty(displayName = "Paid Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getPaidDate() {
        return paidDate;
    }
    
    public void setPaidDate(OADate newValue) {
        fireBeforePropertyChange(P_PaidDate, this.paidDate, newValue);
        OADate old = paidDate;
        this.paidDate = newValue;
        firePropertyChange(P_PaidDate, old, this.paidDate);
    }
    @OAProperty(displayName = "Item Sent Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getItemSentDate() {
        return itemSentDate;
    }
    
    public void setItemSentDate(OADate newValue) {
        fireBeforePropertyChange(P_ItemSentDate, this.itemSentDate, newValue);
        OADate old = itemSentDate;
        this.itemSentDate = newValue;
        firePropertyChange(P_ItemSentDate, old, this.itemSentDate);
    }
    @OAProperty(displayName = "Item Shipping Info", maxLength = 150, displayLength = 40, columnLength = 20)
    @OAColumn(maxLength = 150)
    public String getItemShippingInfo() {
        return itemShippingInfo;
    }
    
    public void setItemShippingInfo(String newValue) {
        fireBeforePropertyChange(P_ItemShippingInfo, this.itemShippingInfo, newValue);
        String old = itemShippingInfo;
        this.itemShippingInfo = newValue;
        firePropertyChange(P_ItemShippingInfo, old, this.itemShippingInfo);
    }
    @OAProperty(displayName = "Item Last Status Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getItemLastStatusDate() {
        return itemLastStatusDate;
    }
    
    public void setItemLastStatusDate(OADate newValue) {
        fireBeforePropertyChange(P_ItemLastStatusDate, this.itemLastStatusDate, newValue);
        OADate old = itemLastStatusDate;
        this.itemLastStatusDate = newValue;
        firePropertyChange(P_ItemLastStatusDate, old, this.itemLastStatusDate);
    }
    @OAProperty(displayName = "Item Last Status", maxLength = 75, displayLength = 40)
    @OAColumn(maxLength = 75)
    public String getItemLastStatus() {
        return itemLastStatus;
    }
    
    public void setItemLastStatus(String newValue) {
        fireBeforePropertyChange(P_ItemLastStatus, this.itemLastStatus, newValue);
        String old = itemLastStatus;
        this.itemLastStatus = newValue;
        firePropertyChange(P_ItemLastStatus, old, this.itemLastStatus);
    }
    @OAProperty(displayName = "Completed Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(OADate newValue) {
        fireBeforePropertyChange(P_CompletedDate, this.completedDate, newValue);
        OADate old = completedDate;
        this.completedDate = newValue;
        firePropertyChange(P_CompletedDate, old, this.completedDate);
    }
    @OAOne(
        displayName = "Hifive Order", 
        reverseName = HifiveOrder.P_HifiveOrderItems, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"HifiveOrderId"})
    public HifiveOrder getHifiveOrder() {
        if (hifiveOrder == null) {
            hifiveOrder = (HifiveOrder) getObject(P_HifiveOrder);
        }
        return hifiveOrder;
    }
    
    public void setHifiveOrder(HifiveOrder newValue) {
        fireBeforePropertyChange(P_HifiveOrder, this.hifiveOrder, newValue);
        HifiveOrder old = this.hifiveOrder;
        this.hifiveOrder = newValue;
        firePropertyChange(P_HifiveOrder, old, this.hifiveOrder);
    }
    
    @OAOne(
        reverseName = Product.P_HifiveOrderItems
    )
    @OAFkey(columns = {"ProductId"})
    public Product getProduct() {
        if (product == null) {
            product = (Product) getObject(P_Product);
        }
        return product;
    }
    
    public void setProduct(Product newValue) {
        fireBeforePropertyChange(P_Product, this.product, newValue);
        Product old = this.product;
        this.product = newValue;
        firePropertyChange(P_Product, old, this.product);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveOrderItem.P_Seq, true);
        }
        this.quantity = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveOrderItem.P_Quantity, true);
        }
        this.pointsUsed = (double) rs.getDouble(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveOrderItem.P_PointsUsed, true);
        }
        date = rs.getDate(6);
        if (date != null) this.billDate = new OADate(date);
        date = rs.getDate(7);
        if (date != null) this.paidDate = new OADate(date);
        date = rs.getDate(8);
        if (date != null) this.itemSentDate = new OADate(date);
        this.itemShippingInfo = rs.getString(9);
        date = rs.getDate(10);
        if (date != null) this.itemLastStatusDate = new OADate(date);
        this.itemLastStatus = rs.getString(11);
        date = rs.getDate(12);
        if (date != null) this.completedDate = new OADate(date);
        int hifiveOrderFkey = rs.getInt(13);
        if (!rs.wasNull() && hifiveOrderFkey > 0) {
            setProperty(P_HifiveOrder, new OAObjectKey(hifiveOrderFkey));
        }
        int productFkey = rs.getInt(14);
        if (!rs.wasNull() && productFkey > 0) {
            setProperty(P_Product, new OAObjectKey(productFkey));
        }
        if (rs.getMetaData().getColumnCount() != 14) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
