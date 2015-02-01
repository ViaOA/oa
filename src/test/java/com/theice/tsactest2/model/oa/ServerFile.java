// Generated by OABuilder
package com.theice.tsactest2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsactest2.model.oa.filter.*;
import com.theice.tsactest2.model.oa.propertypath.*;
import com.viaoa.util.OADateTime;
import java.io.*;
 
@OAClass(
    shortName = "sf",
    displayName = "Server File",
    displayProperty = "fileName"
)
@OATable(
    indexes = {
        @OAIndex(name = "ServerFileApplication", columns = { @OAIndexColumn(name = "ApplicationId") })
    }
)
public class ServerFile extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_FileName = "FileName";
    public static final String P_FileName = "FileName";
    public static final String PROPERTY_FileType = "FileType";
    public static final String P_FileType = "FileType";
    public static final String PROPERTY_FileTypeAsString = "FileTypeAsString";
    public static final String P_FileTypeAsString = "FileTypeAsString";
    public static final String PROPERTY_DateTime = "DateTime";
    public static final String P_DateTime = "DateTime";
    public static final String PROPERTY_Length = "Length";
    public static final String P_Length = "Length";
    public static final String PROPERTY_CurrentFilePos = "CurrentFilePos";
    public static final String P_CurrentFilePos = "CurrentFilePos";
    public static final String PROPERTY_Notes = "Notes";
    public static final String P_Notes = "Notes";
     
    public static final String PROPERTY_IsLoaded = "IsLoaded";
    public static final String P_IsLoaded = "IsLoaded";
     
    public static final String PROPERTY_Application = "Application";
    public static final String P_Application = "Application";
     
    protected int id;
    protected OADateTime created;
    protected String fileName;
    protected int fileType;
    public static final int FILETYPE_OTHER = 0;
    public static final int FILETYPE_ICEMESSAGE = 1;
    public static final int FILETYPE_TEXT = 2;
    public static final int FILETYPE_BINARY = 3;
    public static final int FILETYPE_CSV = 4;
    public static final Hub<String> hubFileType;
    static {
        hubFileType = new Hub<String>(String.class);
        hubFileType.addElement("Other");
        hubFileType.addElement("ICE Messsage");
        hubFileType.addElement("Text");
        hubFileType.addElement("Binary");
        hubFileType.addElement("Comma separated");
    }
    protected OADateTime dateTime;
    protected long length;
    protected long currentFilePos;
    protected String notes;
     
    // Links to other objects.
    protected transient Application application;
     
    public ServerFile() {
        if (!isLoading()) {
            setCreated(new OADateTime());
        }
    }
     
    public ServerFile(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5, isProcessed = true)
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
    @OAProperty(defaultValue = "new OADateTime()", displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getCreated() {
        return created;
    }
    
    public void setCreated(OADateTime newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADateTime old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayName = "File Name", maxLength = 254, displayLength = 40, columnLength = 38, isProcessed = true)
    @OAColumn(maxLength = 254)
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String newValue) {
        fireBeforePropertyChange(P_FileName, this.fileName, newValue);
        String old = fileName;
        this.fileName = newValue;
        firePropertyChange(P_FileName, old, this.fileName);
    }
    @OAProperty(displayName = "File Type", displayLength = 16, columnLength = 12, isProcessed = true, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getFileType() {
        return fileType;
    }
    
    public void setFileType(int newValue) {
        fireBeforePropertyChange(P_FileType, this.fileType, newValue);
        int old = fileType;
        this.fileType = newValue;
        firePropertyChange(P_FileType, old, this.fileType);
    }
    public String getFileTypeAsString() {
        if (isNull(P_FileType)) return "";
        String s = hubFileType.getAt(getFileType());
        if (s == null) s = "";
        return s;
    }
    @OAProperty(displayName = "Date Time", displayLength = 15, isProcessed = true)
    @OAColumn(name = "DateTimeValue", sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(OADateTime newValue) {
        fireBeforePropertyChange(P_DateTime, this.dateTime, newValue);
        OADateTime old = dateTime;
        this.dateTime = newValue;
        firePropertyChange(P_DateTime, old, this.dateTime);
    }
    @OAProperty(displayLength = 5, columnLength = 6, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getLength() {
        return length;
    }
    
    public void setLength(long newValue) {
        fireBeforePropertyChange(P_Length, this.length, newValue);
        long old = length;
        this.length = newValue;
        firePropertyChange(P_Length, old, this.length);
    }
    @OAProperty(displayName = "Current File Pos", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getCurrentFilePos() {
        return currentFilePos;
    }
    
    public void setCurrentFilePos(long newValue) {
        fireBeforePropertyChange(P_CurrentFilePos, this.currentFilePos, newValue);
        long old = currentFilePos;
        this.currentFilePos = newValue;
        firePropertyChange(P_CurrentFilePos, old, this.currentFilePos);
    }
    @OAProperty(maxLength = 5, displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String newValue) {
        fireBeforePropertyChange(P_Notes, this.notes, newValue);
        String old = notes;
        this.notes = newValue;
        firePropertyChange(P_Notes, old, this.notes);
    }
    @OACalculatedProperty(displayName = "Is Loaded", displayLength = 5, columnLength = 10, properties = {P_CurrentFilePos, P_Length})
    public boolean getIsLoaded() {
        boolean isLoaded;
        // currentFilePos
        long currentFilePos = this.getCurrentFilePos();
    
        // length
        long length = this.getLength();
    
        isLoaded = (currentFilePos+5 > length);
        return isLoaded;
    }
     
    @OAOne(
        reverseName = Application.P_ServerFiles, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ApplicationId"})
    public Application getApplication() {
        if (application == null) {
            application = (Application) getObject(P_Application);
        }
        return application;
    }
    
    public void setApplication(Application newValue) {
        fireBeforePropertyChange(P_Application, this.application, newValue);
        Application old = this.application;
        this.application = newValue;
        firePropertyChange(P_Application, old, this.application);
    }
    
    // readFile - read file from server
    public byte[] readFile(int begPos, int length) {
        if (!isServer()) {
            byte[] result = null;//RemoteDelegate.getRemoteModel().serverFile_readFile(this, begPos, length);
            return result;
        }
        if (begPos > getLength()) return new byte[0];
        
        byte[] bs = new byte[length];
        try {
            RandomAccessFile raf = new RandomAccessFile(getFileName(), "r");
            FileInputStream fis = new FileInputStream(raf.getFD());
            raf.seek(begPos);
            fis.read(bs);
        }
        catch (Exception e) {
            bs = null;
        }
        return bs;
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        this.fileName = rs.getString(3);
        this.fileType = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ServerFile.P_FileType, true);
        }
        timestamp = rs.getTimestamp(5);
        if (timestamp != null) this.dateTime = new OADateTime(timestamp);
        this.length = (long) rs.getLong(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ServerFile.P_Length, true);
        }
        this.currentFilePos = (long) rs.getLong(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ServerFile.P_CurrentFilePos, true);
        }
        this.notes = rs.getString(8);
        int applicationFkey = rs.getInt(9);
        if (!rs.wasNull() && applicationFkey > 0) {
            setProperty(P_Application, new OAObjectKey(applicationFkey));
        }
        if (rs.getMetaData().getColumnCount() != 9) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
