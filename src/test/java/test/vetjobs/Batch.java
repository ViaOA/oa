package test.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class Batch extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String fileName;
    protected OADate loadDate;
    protected OADate processDate;
    protected int qtyRow;
    protected int qtyReject;
    protected int qtyNew;
    
    // Links to other objects
    protected Hub hubBatchRow;
    protected Employer employer;
    
 
    public Batch() {
    	if (!isLoading()) {
	        setLoadDate(new OADate());
	        setQtyReject(0);
	        setQtyNew(0);
	        setQtyRow(0);
    	}
    }
     
    public Batch(int id) {
        setId(id);
        setQtyReject(0);
        setQtyNew(0);
        setQtyRow(0);
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        String old = this.fileName;
        this.fileName = fileName;
        firePropertyChange("fileName",old,fileName);
    }
    
    public OADate getLoadDate() {
        return loadDate;
    }
    public void setLoadDate(OADate loadDate) {
        OADate old = this.loadDate;
        this.loadDate = loadDate;
        firePropertyChange("loadDate",old,loadDate);
    }
    
    public OADate getProcessDate() {
        return processDate;
    }
    public void setProcessDate(OADate processDate) {
        OADate old = this.processDate;
        this.processDate = processDate;
        firePropertyChange("processDate",old,processDate);
    }
    
    public int getQtyRow() {
        return qtyRow;
    }
    public void setQtyRow(int qtyRow) {
        int old = this.qtyRow;
        this.qtyRow = qtyRow;
        firePropertyChange("qtyRow",old,qtyRow);
    }
    
    public int getQtyReject() {
        return qtyReject;
    }
    public void setQtyReject(int qtyReject) {
        int old = this.qtyReject;
        this.qtyReject = qtyReject;
        firePropertyChange("qtyReject",old,qtyReject);
    }
    
    public int getQtyNew() {
        return qtyNew;
    }
    public void setQtyNew(int qtyNew) {
        int old = this.qtyNew;
        this.qtyNew = qtyNew;
        firePropertyChange("qtyNew",old,qtyNew);
    }
    
    public Hub getBatchRows() {
        if (hubBatchRow == null) hubBatchRow = getHub("batchRows");
        return hubBatchRow;
    }
    
    public Employer getEmployer() {
        if (employer == null) employer = (Employer) getObject("employer");
        return employer;
    }
    
    public void setEmployer(Employer employer) {
        Employer old = this.employer;
        this.employer = employer;
        firePropertyChange("employer",old,employer);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("batchRows",BatchRow.class,OALinkInfo.MANY, true,"batch"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("employer",Employer.class,OALinkInfo.ONE, false,"batches"));
    }
}

