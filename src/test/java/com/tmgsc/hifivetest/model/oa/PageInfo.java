// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
 
@OAClass(
    shortName = "pi",
    displayName = "Page Info",
    displayProperty = "code",
    rootTreePropertyPaths = {
        "[Page]."+Page.P_PageInfos
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "PageInfoPage", columns = { @OAIndexColumn(name = "PageId") })
    }
)
public class PageInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Code = "Code";
    public static final String P_Code = "Code";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_LocationPageInfos = "LocationPageInfos";
    public static final String P_LocationPageInfos = "LocationPageInfos";
    public static final String PROPERTY_Page = "Page";
    public static final String P_Page = "Page";
    public static final String PROPERTY_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String P_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String PROPERTY_PageThemePageInfos = "PageThemePageInfos";
    public static final String P_PageThemePageInfos = "PageThemePageInfos";
    public static final String PROPERTY_ProgramPageInfos = "ProgramPageInfos";
    public static final String P_ProgramPageInfos = "ProgramPageInfos";
     
    protected int id;
    protected String code;
    protected String description;
    protected int seq;
     
    // Links to other objects.
    protected transient Page page;
     
    public PageInfo() {
    }
     
    public PageInfo(int id) {
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
    @OAProperty(maxLength = 25, displayLength = 8)
    @OAColumn(maxLength = 25)
    public String getCode() {
        return code;
    }
    
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    @OAProperty(maxLength = 55, displayLength = 20, columnLength = 28)
    @OAColumn(maxLength = 55)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        fireBeforePropertyChange(P_Description, this.description, newValue);
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
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
    @OAMany(
        displayName = "Location Page Infos", 
        toClass = LocationPageInfo.class, 
        reverseName = LocationPageInfo.P_PageInfo, 
        createMethod = false
    )
    private Hub<LocationPageInfo> getLocationPageInfos() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Page.P_PageInfos, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"PageId"})
    public Page getPage() {
        if (page == null) {
            page = (Page) getObject(P_Page);
        }
        return page;
    }
    
    public void setPage(Page newValue) {
        fireBeforePropertyChange(P_Page, this.page, newValue);
        Page old = this.page;
        this.page = newValue;
        firePropertyChange(P_Page, old, this.page);
    }
    
    @OAMany(
        displayName = "Page Group Page Infos", 
        toClass = PageGroupPageInfo.class, 
        reverseName = PageGroupPageInfo.P_PageInfo, 
        createMethod = false
    )
    private Hub<PageGroupPageInfo> getPageGroupPageInfos() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Page Theme Page Infos", 
        toClass = PageThemePageInfo.class, 
        reverseName = PageThemePageInfo.P_PageInfo, 
        createMethod = false
    )
    private Hub<PageThemePageInfo> getPageThemePageInfos() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Program Page Infos", 
        toClass = ProgramPageInfo.class, 
        reverseName = ProgramPageInfo.P_PageInfo, 
        createMethod = false
    )
    private Hub<ProgramPageInfo> getProgramPageInfos() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.code = rs.getString(2);
        this.description = rs.getString(3);
        this.seq = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, PageInfo.P_Seq, true);
        }
        int pageFkey = rs.getInt(5);
        if (!rs.wasNull() && pageFkey > 0) {
            setProperty(P_Page, new OAObjectKey(pageFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
