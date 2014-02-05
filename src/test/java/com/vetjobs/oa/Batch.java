package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "bat",
    displayName = "Batch"
)
@OATable(
    indexes = {
        @OAIndex(name = "BatchEmployer", columns = { @OAIndexColumn(name = "EmployerId") })
    }
)
public class Batch extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_FileName = "FileName";
    public static final String PROPERTY_LoadDate = "LoadDate";
    public static final String PROPERTY_ProcessDate = "ProcessDate";
    public static final String PROPERTY_QtyRow = "QtyRow";
    public static final String PROPERTY_QtyReject = "QtyReject";
    public static final String PROPERTY_QtyNew = "QtyNew";
     
     
    public static final String PROPERTY_Employer = "Employer";
    public static final String PROPERTY_BatchRows = "BatchRows";
     
    protected int id;
    protected String fileName;
    protected OADate loadDate;
    protected OADate processDate;
    protected int qtyRow;
    protected int qtyReject;
    protected int qtyNew;
     
    // Links to other objects.
    protected transient Employer employer;
    protected transient Hub<BatchRow> hubBatchRows;
     
     
    public Batch() {
    }
     
    public Batch(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    @OAProperty(displayName = "File Name", maxLength = 80, displayLength = 8)
    @OAColumn(maxLength = 80)
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String newValue) {
        String old = fileName;
        this.fileName = newValue;
        firePropertyChange(PROPERTY_FileName, old, this.fileName);
    }
    
     
    @OAProperty(displayName = "Load Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLoadDate() {
        return loadDate;
    }
    
    public void setLoadDate(OADate newValue) {
        OADate old = loadDate;
        this.loadDate = newValue;
        firePropertyChange(PROPERTY_LoadDate, old, this.loadDate);
    }
    
     
    @OAProperty(displayName = "Process Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getProcessDate() {
        return processDate;
    }
    
    public void setProcessDate(OADate newValue) {
        OADate old = processDate;
        this.processDate = newValue;
        firePropertyChange(PROPERTY_ProcessDate, old, this.processDate);
    }
    
     
    @OAProperty(displayName = "Qty Row", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyRow() {
        return qtyRow;
    }
    
    public void setQtyRow(int newValue) {
        int old = qtyRow;
        this.qtyRow = newValue;
        firePropertyChange(PROPERTY_QtyRow, old, this.qtyRow);
    }
    
     
    @OAProperty(displayName = "Qty Reject", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyReject() {
        return qtyReject;
    }
    
    public void setQtyReject(int newValue) {
        int old = qtyReject;
        this.qtyReject = newValue;
        firePropertyChange(PROPERTY_QtyReject, old, this.qtyReject);
    }
    
     
    @OAProperty(displayName = "Qty New", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyNew() {
        return qtyNew;
    }
    
    public void setQtyNew(int newValue) {
        int old = qtyNew;
        this.qtyNew = newValue;
        firePropertyChange(PROPERTY_QtyNew, old, this.qtyNew);
    }
    
     
    @OAOne(reverseName = Employer.PROPERTY_Batches, required = true)
    @OAFkey(columns = {"EmployerId"})
    public Employer getEmployer() {
        if (employer == null) {
            employer = (Employer) getObject(PROPERTY_Employer);
        }
        return employer;
    }
    
    public void setEmployer(Employer newValue) {
        Employer old = this.employer;
        this.employer = newValue;
        firePropertyChange(PROPERTY_Employer, old, this.employer);
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, owner = true, reverseName = BatchRow.PROPERTY_Batch, cascadeSave = true, cascadeDelete = true)
    public Hub<BatchRow> getBatchRows() {
        if (hubBatchRows == null) {
            hubBatchRows = (Hub<BatchRow>) getHub(PROPERTY_BatchRows);
        }
        return hubBatchRows;
    }
    
     
}
 
