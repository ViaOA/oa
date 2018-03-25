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
    shortName = "ct",
    displayName = "Currency Type",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "name"
)
@OATable(
)
public class CurrencyType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Abbreviation = "Abbreviation";
    public static final String P_Abbreviation = "Abbreviation";
    public static final String PROPERTY_Symbol = "Symbol";
    public static final String P_Symbol = "Symbol";
    public static final String PROPERTY_ExchangeRate = "ExchangeRate";
    public static final String P_ExchangeRate = "ExchangeRate";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
     
     
    public static final String PROPERTY_CountryCodes = "CountryCodes";
    public static final String P_CountryCodes = "CountryCodes";
     
    protected int id;
    protected String name;
    protected String abbreviation;
    protected String symbol;
    protected double exchangeRate;
    protected OADate created;
     
    // Links to other objects.
    protected transient Hub<CountryCode> hubCountryCodes;
     
    public CurrencyType() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public CurrencyType(int id) {
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
    @OAProperty(maxLength = 25, displayLength = 25)
    @OAColumn(maxLength = 25)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 12, displayLength = 12)
    @OAColumn(maxLength = 12)
    public String getAbbreviation() {
        return abbreviation;
    }
    
    public void setAbbreviation(String newValue) {
        fireBeforePropertyChange(P_Abbreviation, this.abbreviation, newValue);
        String old = abbreviation;
        this.abbreviation = newValue;
        firePropertyChange(P_Abbreviation, old, this.abbreviation);
    }
    @OAProperty(maxLength = 6, isUnicode = true, displayLength = 6)
    @OAColumn(maxLength = 6)
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String newValue) {
        fireBeforePropertyChange(P_Symbol, this.symbol, newValue);
        String old = symbol;
        this.symbol = newValue;
        firePropertyChange(P_Symbol, old, this.symbol);
    }
    @OAProperty(displayName = "Exchange Rate", decimalPlaces = 4, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getExchangeRate() {
        return exchangeRate;
    }
    
    public void setExchangeRate(double newValue) {
        fireBeforePropertyChange(P_ExchangeRate, this.exchangeRate, newValue);
        double old = exchangeRate;
        this.exchangeRate = newValue;
        firePropertyChange(P_ExchangeRate, old, this.exchangeRate);
    }
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isReadOnly = true)
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
    @OAMany(
        displayName = "Country Codes", 
        toClass = CountryCode.class, 
        reverseName = CountryCode.P_CurrencyType
    )
    public Hub<CountryCode> getCountryCodes() {
        if (hubCountryCodes == null) {
            hubCountryCodes = (Hub<CountryCode>) getHub(P_CountryCodes);
        }
        return hubCountryCodes;
    }
    
    // convertTo
    public double convertTo (double usd) {
        return 0;//CurrencyTypeDelegate.convertTo(usd, exchangeRate);
    }
     
    // convertFrom
    public double convertFrom (double foreign) {
       return 0;//CurrencyTypeDelegate.convertFrom(foreign, exchangeRate);
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.abbreviation = rs.getString(3);
        this.symbol = rs.getString(4);
        this.exchangeRate = (double) rs.getDouble(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, CurrencyType.P_ExchangeRate, true);
        }
        java.sql.Date date;
        date = rs.getDate(6);
        if (date != null) this.created = new OADate(date);
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }

    public static final String P_Id1 = "Id1";
    public static final String P_Id2 = "Id2";
    public static final String P_Id3 = "Id3";
    public static final String P_Id4 = "Id4";
    public static final String P_Id5 = "Id5";
    public static final String P_Id6 = "Id6";
    public static final String P_Id7 = "Id7";
    
    protected int id1,id2,id3,id4,id5,id6,id7;
    @OAProperty()
    public int getId1() {
        return id1;
    }
    public void setId1(int newValue) {
        fireBeforePropertyChange(P_Id1, this.id1, newValue);
        int old = id1;
        this.id1 = newValue;
        firePropertyChange(P_Id1, old, this.id1);
    }
    @OAProperty()
    public int getId2() {
        return id2;
    }
    public void setId2(int newValue) {
        fireBeforePropertyChange(P_Id2, this.id2, newValue);
        int old = id2;
        this.id2 = newValue;
        firePropertyChange(P_Id2, old, this.id2);
    }
    @OAProperty()
    public int getId3() {
        return id3;
    }
    public void setId3(int newValue) {
        fireBeforePropertyChange(P_Id3, this.id3, newValue);
        int old = id3;
        this.id3 = newValue;
        firePropertyChange(P_Id3, old, this.id3);
    }
    @OAProperty()
    public int getId4() {
        return id4;
    }
    public void setId4(int newValue) {
        fireBeforePropertyChange(P_Id4, this.id4, newValue);
        int old = id4;
        this.id4 = newValue;
        firePropertyChange(P_Id4, old, this.id4);
    }
    @OAProperty()
    public int getId5() {
        return id5;
    }
    public void setId5(int newValue) {
        fireBeforePropertyChange(P_Id5, this.id, newValue);
        int old = id5;
        this.id5 = newValue;
        firePropertyChange(P_Id5, old, this.id5);
    }
    @OAProperty()
    public int getId6() {
        return id6;
    }
    public void setId6(int newValue) {
        fireBeforePropertyChange(P_Id6, this.id6, newValue);
        int old = id6;
        this.id6 = newValue;
        firePropertyChange(P_Id6, old, this.id6);
    }
    @OAProperty()
    public int getId7() {
        return id7;
    }
    public void setId7(int newValue) {
        fireBeforePropertyChange(P_Id7, this.id7, newValue);
        int old = id7;
        this.id7 = newValue;
        firePropertyChange(P_Id7, old, this.id7);
    }

}
 
