package com.vetjobstest.oa;
 
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
    public static final String P_Id = "Id";
    public static final String P_FileName = "FileName";
    public static final String P_LoadDate = "LoadDate";
    public static final String P_ProcessDate = "ProcessDate";
    public static final String P_QtyRow = "QtyRow";
    public static final String P_QtyReject = "QtyReject";
    public static final String P_QtyNew = "QtyNew";
     
     
    public static final String P_Employer = "Employer";
    public static final String P_BatchRows = "BatchRows";
     
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
        firePropertyChange(P_Id, old, this.id);
    }
    
     
    @OAProperty(displayName = "File Name", maxLength = 80, displayLength = 8)
    @OAColumn(maxLength = 80)
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String newValue) {
        String old = fileName;
        this.fileName = newValue;
        firePropertyChange(P_FileName, old, this.fileName);
    }
    
     
    @OAProperty(displayName = "Load Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLoadDate() {
        return loadDate;
    }
    
    public void setLoadDate(OADate newValue) {
        OADate old = loadDate;
        this.loadDate = newValue;
        firePropertyChange(P_LoadDate, old, this.loadDate);
    }
    
     
    @OAProperty(displayName = "Process Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getProcessDate() {
        return processDate;
    }
    
    public void setProcessDate(OADate newValue) {
        OADate old = processDate;
        this.processDate = newValue;
        firePropertyChange(P_ProcessDate, old, this.processDate);
    }
    
     
    @OAProperty(displayName = "Qty Row", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyRow() {
        return qtyRow;
    }
    
    public void setQtyRow(int newValue) {
        int old = qtyRow;
        this.qtyRow = newValue;
        firePropertyChange(P_QtyRow, old, this.qtyRow);
    }
    
     
    @OAProperty(displayName = "Qty Reject", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyReject() {
        return qtyReject;
    }
    
    public void setQtyReject(int newValue) {
        int old = qtyReject;
        this.qtyReject = newValue;
        firePropertyChange(P_QtyReject, old, this.qtyReject);
    }
    
     
    @OAProperty(displayName = "Qty New", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getQtyNew() {
        return qtyNew;
    }
    
    public void setQtyNew(int newValue) {
        int old = qtyNew;
        this.qtyNew = newValue;
        firePropertyChange(P_QtyNew, old, this.qtyNew);
    }
    
     
    @OAOne(reverseName = Employer.P_Batches, required = true)
    @OAFkey(columns = {"EmployerId"})
    public Employer getEmployer() {
        if (employer == null) {
            employer = (Employer) getObject(P_Employer);
        }
        return employer;
    }
    
    public void setEmployer(Employer newValue) {
        Employer old = this.employer;
        this.employer = newValue;
        firePropertyChange(P_Employer, old, this.employer);
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, owner = true, reverseName = BatchRow.P_Batch, cascadeSave = true, cascadeDelete = true)
    public Hub<BatchRow> getBatchRows() {
        if (hubBatchRows == null) {
            hubBatchRows = (Hub<BatchRow>) getHub(P_BatchRows);
        }
        return hubBatchRows;
    }
    
     
}
 
