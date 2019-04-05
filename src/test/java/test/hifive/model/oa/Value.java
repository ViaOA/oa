// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "val",
    displayName = "Value",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "value",
    sortProperty = "value"
)
@OATable(
)
public class Value extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Value = "Value";
    public static final String P_Value = "Value";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
     
     
    public static final String PROPERTY_Cards = "Cards";
    public static final String P_Cards = "Cards";
    public static final String PROPERTY_EmployeeAward = "EmployeeAward";
    public static final String P_EmployeeAward = "EmployeeAward";
    public static final String PROPERTY_InspireAwardCardOrder = "InspireAwardCardOrder";
    public static final String P_InspireAwardCardOrder = "InspireAwardCardOrder";
     
    protected int id;
    protected double value;
    protected String name;
     
    // Links to other objects.
    protected transient AwardCardOrder inspireAwardCardOrder;
     
    public Value() {
    }
     
    public Value(int id) {
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
    @OAProperty(maxLength = 55, displayLength = 20)
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
    @OAMany(
        toClass = Card.class, 
        reverseName = Card.P_Values, 
        createMethod = false
    )
    @OALinkTable(name = "CardValue", indexName = "CardValue", columns = {"ValueId"})
    private Hub<Card> getCards() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Employee Award", 
        isCalculated = true, 
        reverseName = EmployeeAward.P_Values, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private EmployeeAward getEmployeeAward() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Inspire Award Card Order", 
        isCalculated = true, 
        reverseName = AwardCardOrder.P_InspireValues
    )
    public AwardCardOrder getInspireAwardCardOrder() {
        if (inspireAwardCardOrder == null) {
            inspireAwardCardOrder = (AwardCardOrder) getObject(P_InspireAwardCardOrder);
        }
        return inspireAwardCardOrder;
    }
    
    public void setInspireAwardCardOrder(AwardCardOrder newValue) {
        fireBeforePropertyChange(P_InspireAwardCardOrder, this.inspireAwardCardOrder, newValue);
        AwardCardOrder old = this.inspireAwardCardOrder;
        this.inspireAwardCardOrder = newValue;
        firePropertyChange(P_InspireAwardCardOrder, old, this.inspireAwardCardOrder);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.value = (double) rs.getDouble(2);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Value.P_Value, true);
        }
        this.name = rs.getString(3);
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
