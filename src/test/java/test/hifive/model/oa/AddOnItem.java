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
    shortName = "aoi",
    displayName = "Add On Item",
    displayProperty = "item"
)
@OATable(
    indexes = {
        @OAIndex(name = "AddOnItemAwardType", columns = { @OAIndexColumn(name = "AwardTypeId") }), 
        @OAIndex(name = "AddOnItemLocation", columns = { @OAIndexColumn(name = "LocationId") }), 
        @OAIndex(name = "AddOnItemProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class AddOnItem extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Value = "Value";
    public static final String P_Value = "Value";
     
     
    public static final String PROPERTY_AwardType = "AwardType";
    public static final String P_AwardType = "AwardType";
    public static final String PROPERTY_EmployeeAwards = "EmployeeAwards";
    public static final String P_EmployeeAwards = "EmployeeAwards";
    public static final String PROPERTY_Item = "Item";
    public static final String P_Item = "Item";
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
     
    protected int id;
    protected OADate created;
    protected double value;
     
    // Links to other objects.
    protected transient Item item;
    protected transient Location location;
     
    public AddOnItem() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public AddOnItem(int id) {
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
    @OAOne(
        displayName = "Award Type", 
        reverseName = AwardType.P_AddOnItems, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "AddOnItemAwardType", indexName = "AwardTypeAddOnItem", columns = {"AddOnItemId"})
    private AwardType getAwardType() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Employee Awards", 
        toClass = EmployeeAward.class, 
        isCalculated = true, 
        reverseName = EmployeeAward.P_AddOnItems, 
        createMethod = false
    )
    private Hub<EmployeeAward> getEmployeeAwards() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Item.P_AddOnItems, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ItemId"})
    public Item getItem() {
        if (item == null) {
            item = (Item) getObject(P_Item);
        }
        return item;
    }
    
    public void setItem(Item newValue) {
        fireBeforePropertyChange(P_Item, this.item, newValue);
        Item old = this.item;
        this.item = newValue;
        firePropertyChange(P_Item, old, this.item);
    }
    
    @OAOne(
        reverseName = Location.P_AddOnItems
    )
    @OAFkey(columns = {"LocationId"})
    public Location getLocation() {
        if (location == null) {
            location = (Location) getObject(P_Location);
        }
        return location;
    }
    
    public void setLocation(Location newValue) {
        fireBeforePropertyChange(P_Location, this.location, newValue);
        Location old = this.location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    
    @OAOne(
        reverseName = Program.P_AddOnItems, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "ProgramAddOnItem", indexName = "ProgramAddOnItem", columns = {"AddOnItemId"})
    private Program getProgram() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.value = (double) rs.getDouble(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, AddOnItem.P_Value, true);
        }
        int itemFkey = rs.getInt(4);
        if (!rs.wasNull() && itemFkey > 0) {
            setProperty(P_Item, new OAObjectKey(itemFkey));
        }
        int locationFkey = rs.getInt(5);
        if (!rs.wasNull() && locationFkey > 0) {
            setProperty(P_Location, new OAObjectKey(locationFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
