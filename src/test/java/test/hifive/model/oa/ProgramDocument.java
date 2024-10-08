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
 
/**
  The CEO/President letter introducing the program.
*/
@OAClass(
    shortName = "pd",
    displayName = "Program Document",
    description = "The CEO/President letter introducing the program.",
    displayProperty = "name",
    rootTreePropertyPaths = {
        "[Company]."+Company.P_Programs+"."+Program.P_AnnouncementDocument, 
        "[Company]."+Company.P_Programs+"."+Program.P_BlogDocuments
    }
)
@OATable(
)
public class ProgramDocument extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
     
     
    public static final String PROPERTY_AnnouncementLocation = "AnnouncementLocation";
    public static final String P_AnnouncementLocation = "AnnouncementLocation";
    public static final String PROPERTY_AnnouncementProgram = "AnnouncementProgram";
    public static final String P_AnnouncementProgram = "AnnouncementProgram";
    public static final String PROPERTY_AwardType = "AwardType";
    public static final String P_AwardType = "AwardType";
    public static final String PROPERTY_AwardTypes = "AwardTypes";
    public static final String P_AwardTypes = "AwardTypes";
    public static final String PROPERTY_BlogPrograms = "BlogPrograms";
    public static final String P_BlogPrograms = "BlogPrograms";
    public static final String PROPERTY_EmployeeAward = "EmployeeAward";
    public static final String P_EmployeeAward = "EmployeeAward";
    public static final String PROPERTY_LocationPageInfo = "LocationPageInfo";
    public static final String P_LocationPageInfo = "LocationPageInfo";
    public static final String PROPERTY_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String P_PageGroupPageInfos = "PageGroupPageInfos";
    public static final String PROPERTY_PageThemePageInfo = "PageThemePageInfo";
    public static final String P_PageThemePageInfo = "PageThemePageInfo";
    public static final String PROPERTY_ProgramPageInfo = "ProgramPageInfo";
    public static final String P_ProgramPageInfo = "ProgramPageInfo";
     
    protected int id;
    protected OADate created;
    protected String name;
    protected String text;
     
    // Links to other objects.
     
    public ProgramDocument() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public ProgramDocument(int id) {
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
    @OAProperty(maxLength = 75, displayLength = 40, columnLength = 22)
    @OAColumn(maxLength = 75)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 4, displayLength = 4)
    @OAColumn(name = "TextValue", sqlType = java.sql.Types.CLOB)
    public String getText() {
        return text;
    }
    
    public void setText(String newValue) {
        fireBeforePropertyChange(P_Text, this.text, newValue);
        String old = text;
        this.text = newValue;
        firePropertyChange(P_Text, old, this.text);
    }
    @OAOne(
        displayName = "Location", 
        reverseName = Location.P_AnnouncementDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private Location getAnnouncementLocation() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Announcement Program", 
        reverseName = Program.P_AnnouncementDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private Program getAnnouncementProgram() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Award Type", 
        isCalculated = true, 
        reverseName = AwardType.P_CalcAnnouncementDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private AwardType getAwardType() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Award Types", 
        toClass = AwardType.class, 
        reverseName = AwardType.P_AnnouncementDocument, 
        createMethod = false
    )
    private Hub<AwardType> getAwardTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Blog Programs", 
        toClass = Program.class, 
        reverseName = Program.P_BlogDocuments, 
        createMethod = false
    )
    @OALinkTable(name = "ProgramProgramDocument", indexName = "ProgramBlogDocument", columns = {"ProgramDocumentId"})
    private Hub<Program> getBlogPrograms() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Employee Award", 
        isCalculated = true, 
        reverseName = EmployeeAward.P_CalcAnnouncementDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private EmployeeAward getEmployeeAward() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Location Page Info", 
        reverseName = LocationPageInfo.P_ProgramDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private LocationPageInfo getLocationPageInfo() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Page Group Page Infos", 
        toClass = PageGroupPageInfo.class, 
        reverseName = PageGroupPageInfo.P_ProgramDocument, 
        createMethod = false
    )
    private Hub<PageGroupPageInfo> getPageGroupPageInfos() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Page Theme Page Info", 
        reverseName = PageThemePageInfo.P_ProgramDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private PageThemePageInfo getPageThemePageInfo() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Program Page Info", 
        reverseName = ProgramPageInfo.P_ProgramDocument, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private ProgramPageInfo getProgramPageInfo() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        this.text = rs.getString(4);
        if (rs.getMetaData().getColumnCount() != 4) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
