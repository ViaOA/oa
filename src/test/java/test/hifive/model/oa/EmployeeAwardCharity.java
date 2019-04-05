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
    shortName = "eac",
    displayName = "Employee Award Charity",
    displayProperty = "value"
)
@OATable(
    indexes = {
        @OAIndex(name = "EmployeeAwardCharityEmployeeAward", columns = { @OAIndexColumn(name = "EmployeeAwardId") })
    }
)
public class EmployeeAwardCharity extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Value = "Value";
    public static final String P_Value = "Value";
    public static final String PROPERTY_SentDate = "SentDate";
    public static final String P_SentDate = "SentDate";
    public static final String PROPERTY_InvoiceNumber = "InvoiceNumber";
    public static final String P_InvoiceNumber = "InvoiceNumber";
    public static final String PROPERTY_InvoiceDate = "InvoiceDate";
    public static final String P_InvoiceDate = "InvoiceDate";
    public static final String PROPERTY_VendorInvoiced = "VendorInvoiced";
    public static final String P_VendorInvoiced = "VendorInvoiced";
     
     
    public static final String PROPERTY_Charity = "Charity";
    public static final String P_Charity = "Charity";
    public static final String PROPERTY_EmployeeAward = "EmployeeAward";
    public static final String P_EmployeeAward = "EmployeeAward";
     
    protected int id;
    protected OADate created;
    protected double value;
    protected OADate sentDate;
    protected String invoiceNumber;
    protected OADate invoiceDate;
    protected boolean vendorInvoiced;
     
    // Links to other objects.
    protected transient Charity charity;
    protected transient EmployeeAward employeeAward;
     
    public EmployeeAwardCharity() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public EmployeeAwardCharity(int id) {
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
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isProcessed = true)
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
    @OAProperty(decimalPlaces = 2, isCurrency = true, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getValue() {
        return value;
    }
    
    public void setValue(double newValue) {
        fireBeforePropertyChange(P_Value, this.value, newValue);
        double old = value;
        this.value = newValue;
        firePropertyChange(P_Value, old, this.value);
    }
    @OAProperty(displayName = "Sent Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getSentDate() {
        return sentDate;
    }
    
    public void setSentDate(OADate newValue) {
        fireBeforePropertyChange(P_SentDate, this.sentDate, newValue);
        OADate old = sentDate;
        this.sentDate = newValue;
        firePropertyChange(P_SentDate, old, this.sentDate);
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
        reverseName = Charity.P_EmployeeAwardCharities, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"CharityId"})
    public Charity getCharity() {
        if (charity == null) {
            charity = (Charity) getObject(P_Charity);
        }
        return charity;
    }
    
    public void setCharity(Charity newValue) {
        fireBeforePropertyChange(P_Charity, this.charity, newValue);
        Charity old = this.charity;
        this.charity = newValue;
        firePropertyChange(P_Charity, old, this.charity);
    }
    
    @OAOne(
        displayName = "Employee Award", 
        reverseName = EmployeeAward.P_EmployeeAwardCharities, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"EmployeeAwardId"})
    public EmployeeAward getEmployeeAward() {
        if (employeeAward == null) {
            employeeAward = (EmployeeAward) getObject(P_EmployeeAward);
        }
        return employeeAward;
    }
    
    public void setEmployeeAward(EmployeeAward newValue) {
        fireBeforePropertyChange(P_EmployeeAward, this.employeeAward, newValue);
        EmployeeAward old = this.employeeAward;
        this.employeeAward = newValue;
        firePropertyChange(P_EmployeeAward, old, this.employeeAward);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.value = (double) rs.getDouble(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, EmployeeAwardCharity.P_Value, true);
        }
        date = rs.getDate(4);
        if (date != null) this.sentDate = new OADate(date);
        this.invoiceNumber = rs.getString(5);
        date = rs.getDate(6);
        if (date != null) this.invoiceDate = new OADate(date);
        this.vendorInvoiced = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, EmployeeAwardCharity.P_VendorInvoiced, true);
        }
        int charityFkey = rs.getInt(8);
        if (!rs.wasNull() && charityFkey > 0) {
            setProperty(P_Charity, new OAObjectKey(charityFkey));
        }
        int employeeAwardFkey = rs.getInt(9);
        if (!rs.wasNull() && employeeAwardFkey > 0) {
            setProperty(P_EmployeeAward, new OAObjectKey(employeeAwardFkey));
        }
        if (rs.getMetaData().getColumnCount() != 9) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
