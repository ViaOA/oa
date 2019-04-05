// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;
 
@OAClass(
    shortName = "ioi",
    displayName = "Inspire Order Item",
    displayProperty = "product.item"
)
@OATable(
    indexes = {
        @OAIndex(name = "InspireOrderItemInspireOrder", columns = { @OAIndexColumn(name = "InspireOrderId") })
    }
)
public class InspireOrderItem extends OAObject {
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
    public static final String PROPERTY_InvoiceNumber = "InvoiceNumber";
    public static final String P_InvoiceNumber = "InvoiceNumber";
    public static final String PROPERTY_InvoiceDate = "InvoiceDate";
    public static final String P_InvoiceDate = "InvoiceDate";
    public static final String PROPERTY_VendorInvoiced = "VendorInvoiced";
    public static final String P_VendorInvoiced = "VendorInvoiced";
     
     
    public static final String PROPERTY_InspireOrder = "InspireOrder";
    public static final String P_InspireOrder = "InspireOrder";
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
    protected String invoiceNumber;
    protected OADate invoiceDate;
    protected boolean vendorInvoiced;
     
    // Links to other objects.
    protected transient InspireOrder inspireOrder;
    protected transient Product product;
     
    public InspireOrderItem() {
        if (!isLoading()) {
            setCreated(new OADate());
            setQuantity(1);
        }
    }
     
    public InspireOrderItem(int id) {
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
        if(quantity==0) {quantity=1;}
        return quantity;
    }
    
    public void setQuantity(int newValue) {
        fireBeforePropertyChange(PROPERTY_Quantity, this.quantity, newValue);
        int old = quantity;
        this.quantity = newValue;
        firePropertyChange(PROPERTY_Quantity, old, this.quantity);
        //custom
        if (isLoading()) return;
        if (!isServer()) return;
        if (product == null) { 
            setPointsUsed(0); 
            return; 
        }
        InspireOrder io = getInspireOrder();
        if (io != null) {
            Employee emp = io.getEmployee();
            if (emp != null) {
                Item item = product.getItem();
                if (item != null) {
                    double  x = 0;//EmployeeDelegate.getPointValueForItem(emp, item);
                    setPointsUsed(x*getQuantity());
                }
            }
        }
    }
    @OAProperty(displayName = "Points Used", decimalPlaces = 2, displayLength = 7, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getPointsUsed() {
        return pointsUsed;
    }
    protected void setPointsUsed(double newValue) {
        double old = pointsUsed;
        fireBeforePropertyChange(PROPERTY_PointsUsed, old, newValue);
        this.pointsUsed = newValue;
        firePropertyChange(PROPERTY_PointsUsed, old, this.pointsUsed);
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
    @OAProperty(displayName = "Invoice #", maxLength = 5, displayLength = 5)
    @OAColumn(maxLength = 5)
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String newValue) {
        fireBeforePropertyChange(P_InvoiceNumber, this.invoiceNumber, newValue);
        String old = invoiceNumber;
        this.invoiceNumber = newValue;
        firePropertyChange(P_InvoiceNumber, old, this.invoiceNumber);
    }
    @OAProperty(displayName = "Invoice Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(OADate newValue) {
        fireBeforePropertyChange(P_InvoiceDate, this.invoiceDate, newValue);
        OADate old = invoiceDate;
        this.invoiceDate = newValue;
        firePropertyChange(P_InvoiceDate, old, this.invoiceDate);
    }
    @OAProperty(displayName = "Vendor Invoiced", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getVendorInvoiced() {
        return vendorInvoiced;
    }
    
    public void setVendorInvoiced(boolean newValue) {
        fireBeforePropertyChange(P_VendorInvoiced, this.vendorInvoiced, newValue);
        boolean old = vendorInvoiced;
        this.vendorInvoiced = newValue;
        firePropertyChange(P_VendorInvoiced, old, this.vendorInvoiced);
    }
    @OAOne(
        displayName = "Inspire Order", 
        reverseName = InspireOrder.P_InspireOrderItems, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"InspireOrderId"})
    public InspireOrder getInspireOrder() {
        if (inspireOrder == null) {
            inspireOrder = (InspireOrder) getObject(PROPERTY_InspireOrder);
        }
        return inspireOrder;
    }
    public void setInspireOrder(InspireOrder newValue) {
        setInspireOrderItem(newValue, true);
    }
    public void setInspireOrderTemp(InspireOrder newValue) {
        setInspireOrderItem(newValue, false);
    }
    public void setInspireOrderItem(InspireOrder newValue, boolean bFirePropChange) {
        InspireOrder old = this.inspireOrder;
        if (bFirePropChange) fireBeforePropertyChange(PROPERTY_InspireOrder, old, newValue);
        this.inspireOrder = newValue;
        if (bFirePropChange) firePropertyChange(PROPERTY_InspireOrder, old, this.inspireOrder);
        else OAObjectPropertyDelegate.setProperty(this, PROPERTY_InspireOrder, this.inspireOrder);
    }
    @OAOne(
        reverseName = Product.P_InspireOrderItems, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ProductId"})
    public Product getProduct() {
        if (product == null) {
            product = (Product) getObject(PROPERTY_Product);
        }
        return product;
    }
    public void setProduct(Product newValue) {
        Product old = this.product;
        fireBeforePropertyChange(PROPERTY_Product, old, newValue);
        this.product = newValue;
        firePropertyChange(PROPERTY_Product, old, this.product);
        
        // custom
        if (isLoading()) return;
        if (!isServer()) return;
        if (product == null) { 
            setPointsUsed(0); 
            return; 
        }
        InspireOrder io = getInspireOrder();
        if (io != null) {
            Employee emp = io.getEmployee();
            if (emp != null) {
                Item item = product.getItem();
                if (item != null) {
                    double  x = 0;//EmployeeDelegate.getPointValueForItem(emp, item);
                    setPointsUsed(x*getQuantity());
                }
            }
        }
    }
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, InspireOrderItem.P_Seq, true);
        }
        this.quantity = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, InspireOrderItem.P_Quantity, true);
        }
        this.pointsUsed = (double) rs.getDouble(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, InspireOrderItem.P_PointsUsed, true);
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
        this.invoiceNumber = rs.getString(13);
        date = rs.getDate(14);
        if (date != null) this.invoiceDate = new OADate(date);
        this.vendorInvoiced = rs.getBoolean(15);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, InspireOrderItem.P_VendorInvoiced, true);
        }
        int inspireOrderFkey = rs.getInt(16);
        if (!rs.wasNull() && inspireOrderFkey > 0) {
            setProperty(P_InspireOrder, new OAObjectKey(inspireOrderFkey));
        }
        int productFkey = rs.getInt(17);
        if (!rs.wasNull() && productFkey > 0) {
            setProperty(P_Product, new OAObjectKey(productFkey));
        }
        if (rs.getMetaData().getColumnCount() != 17) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
