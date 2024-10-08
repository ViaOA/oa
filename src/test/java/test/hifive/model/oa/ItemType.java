// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
/**
  Used for Item &quot;flags&quot;, that describe the item.
*/
@OAClass(
    shortName = "it",
    displayName = "Item Type",
    description = "Used for Item &quot;flags&quot;, that describe the item.",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name"
)
@OATable(
)
public class ItemType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
     
     
    public static final String PROPERTY_AwardTypes = "AwardTypes";
    public static final String P_AwardTypes = "AwardTypes";
    public static final String PROPERTY_Items = "Items";
    public static final String P_Items = "Items";
     
    protected int id;
    protected String name;
    protected int type;
    public static final int TYPE_USA = 0;
    public static final int TYPE_CANADIAN = 1;
    public static final int TYPE_HELPINGHANDS = 2;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("USA");
        hubType.addElement("Canadian");
        hubType.addElement("Helping Hands");
    }
     
    // Links to other objects.
     
    public ItemType() {
    }
     
    public ItemType(int id) {
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
    @OAProperty(maxLength = 55, displayLength = 40)
    @OAColumn(maxLength = 55)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayLength = 5, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        fireBeforePropertyChange(P_Type, this.type, newValue);
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    public String getTypeAsString() {
        if (isNull(P_Type)) return "";
        String s = hubType.getAt(getType());
        if (s == null) s = "";
        return s;
    }
    @OAMany(
        displayName = "Award Types", 
        toClass = AwardType.class, 
        reverseName = AwardType.P_ItemTypes, 
        createMethod = false
    )
    @OALinkTable(name = "AwardTypeItemType", indexName = "AwardTypeItemType", columns = {"ItemTypeId"})
    private Hub<AwardType> getAwardTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        toClass = Item.class, 
        reverseName = Item.P_ItemTypes, 
        createMethod = false
    )
    @OALinkTable(name = "ItemTypeItem", indexName = "ItemItemType", columns = {"ItemTypeId"})
    private Hub<Item> getItems() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.type = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ItemType.P_Type, true);
        }
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
