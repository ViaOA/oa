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
    shortName = "car",
    displayName = "Card",
    displayProperty = "name",
    filterClasses = {CardDigitalOnlyFilter.class},
    rootTreePropertyPaths = {
        "[CardVendor]."+CardVendor.P_Cards, 
        "[MerchantCategory]."+MerchantCategory.P_Merchants+"."+Merchant.P_Cards, 
        "[Company]."+Company.P_Programs+"."+Program.P_Cards
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "CardName", columns = {@OAIndexColumn(name = "Name")}),
        @OAIndex(name = "CardMerchantCode", columns = {@OAIndexColumn(name = "MerchantCode")}),
        @OAIndex(name = "CardCardVendor", columns = { @OAIndexColumn(name = "CardVendorId") })
    }
)
public class Card extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_ActiveDate = "ActiveDate";
    public static final String P_ActiveDate = "ActiveDate";
    public static final String PROPERTY_DigitalCard = "DigitalCard";
    public static final String P_DigitalCard = "DigitalCard";
    public static final String PROPERTY_TraditionalCard = "TraditionalCard";
    public static final String P_TraditionalCard = "TraditionalCard";
    public static final String PROPERTY_CelebrateCard = "CelebrateCard";
    public static final String P_CelebrateCard = "CelebrateCard";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String P_InactiveDate = "InactiveDate";
    public static final String PROPERTY_RangeLow = "RangeLow";
    public static final String P_RangeLow = "RangeLow";
    public static final String PROPERTY_RangeHigh = "RangeHigh";
    public static final String P_RangeHigh = "RangeHigh";
    public static final String PROPERTY_RangeIncrement = "RangeIncrement";
    public static final String P_RangeIncrement = "RangeIncrement";
    public static final String PROPERTY_MerchantCode = "MerchantCode";
    public static final String P_MerchantCode = "MerchantCode";
     
     
    public static final String PROPERTY_AwardCardOrders = "AwardCardOrders";
    public static final String P_AwardCardOrders = "AwardCardOrders";
    public static final String PROPERTY_AwardType = "AwardType";
    public static final String P_AwardType = "AwardType";
    public static final String PROPERTY_CalcAwardTypes = "CalcAwardTypes";
    public static final String P_CalcAwardTypes = "CalcAwardTypes";
    public static final String PROPERTY_CardVendor = "CardVendor";
    public static final String P_CardVendor = "CardVendor";
    public static final String PROPERTY_HifiveOrderCards = "HifiveOrderCards";
    public static final String P_HifiveOrderCards = "HifiveOrderCards";
    public static final String PROPERTY_ImageLocations = "ImageLocations";
    public static final String P_ImageLocations = "ImageLocations";
    public static final String PROPERTY_ImageStore = "ImageStore";
    public static final String P_ImageStore = "ImageStore";
    public static final String PROPERTY_ImagineAwardTypes = "ImagineAwardTypes";
    public static final String P_ImagineAwardTypes = "ImagineAwardTypes";
    public static final String PROPERTY_ImagineEmployeeAwards = "ImagineEmployeeAwards";
    public static final String P_ImagineEmployeeAwards = "ImagineEmployeeAwards";
    public static final String PROPERTY_ImaginePrograms = "ImaginePrograms";
    public static final String P_ImaginePrograms = "ImaginePrograms";
    public static final String PROPERTY_InspireImagineEmployees = "InspireImagineEmployees";
    public static final String P_InspireImagineEmployees = "InspireImagineEmployees";
    public static final String PROPERTY_Locations = "Locations";
    public static final String P_Locations = "Locations";
    public static final String PROPERTY_Merchants = "Merchants";
    public static final String P_Merchants = "Merchants";
    public static final String PROPERTY_Programs = "Programs";
    public static final String P_Programs = "Programs";
    public static final String PROPERTY_Values = "Values";
    public static final String P_Values = "Values";
     
    protected int id;
    protected OADate created;
    protected String name;
    protected OADate activeDate;
    protected boolean digitalCard;
    protected boolean traditionalCard;
    protected boolean celebrateCard;
    protected String text;
    protected OADate inactiveDate;
    protected int rangeLow;
    protected int rangeHigh;
    protected int rangeIncrement;
    protected String merchantCode;
     
    // Links to other objects.
    protected transient CardVendor cardVendor;
    protected transient ImageStore imageStore;
    protected transient Hub<Merchant> hubMerchants;
    protected transient Hub<Value> hubValues;
     
    public Card() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Card(int id) {
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
    @OAProperty(maxLength = 75, displayLength = 25, columnLength = 20)
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
    @OAProperty(displayName = "Active Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getActiveDate() {
        return activeDate;
    }
    
    public void setActiveDate(OADate newValue) {
        fireBeforePropertyChange(P_ActiveDate, this.activeDate, newValue);
        OADate old = activeDate;
        this.activeDate = newValue;
        firePropertyChange(P_ActiveDate, old, this.activeDate);
    }
    @OAProperty(displayName = "Digital", displayLength = 5, columnLength = 8)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getDigitalCard() {
        return digitalCard;
    }
    
    public void setDigitalCard(boolean newValue) {
        fireBeforePropertyChange(P_DigitalCard, this.digitalCard, newValue);
        boolean old = digitalCard;
        this.digitalCard = newValue;
        firePropertyChange(P_DigitalCard, old, this.digitalCard);
    }
    @OAProperty(displayName = "Traditional", displayLength = 5, columnLength = 14)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getTraditionalCard() {
        return traditionalCard;
    }
    
    public void setTraditionalCard(boolean newValue) {
        fireBeforePropertyChange(P_TraditionalCard, this.traditionalCard, newValue);
        boolean old = traditionalCard;
        this.traditionalCard = newValue;
        firePropertyChange(P_TraditionalCard, old, this.traditionalCard);
    }
    @OAProperty(displayName = "Celebrate", displayLength = 5, columnLength = 14)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getCelebrateCard() {
        return celebrateCard;
    }
    
    public void setCelebrateCard(boolean newValue) {
        fireBeforePropertyChange(P_CelebrateCard, this.celebrateCard, newValue);
        boolean old = celebrateCard;
        this.celebrateCard = newValue;
        firePropertyChange(P_CelebrateCard, old, this.celebrateCard);
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
    @OAProperty(displayName = "Inactive Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getInactiveDate() {
        return inactiveDate;
    }
    
    public void setInactiveDate(OADate newValue) {
        fireBeforePropertyChange(P_InactiveDate, this.inactiveDate, newValue);
        OADate old = inactiveDate;
        this.inactiveDate = newValue;
        firePropertyChange(P_InactiveDate, old, this.inactiveDate);
    }
    @OAProperty(displayName = "Range Low", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getRangeLow() {
        return rangeLow;
    }
    
    public void setRangeLow(int newValue) {
        fireBeforePropertyChange(P_RangeLow, this.rangeLow, newValue);
        int old = rangeLow;
        this.rangeLow = newValue;
        firePropertyChange(P_RangeLow, old, this.rangeLow);
    }
    @OAProperty(displayName = "Range High", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getRangeHigh() {
        return rangeHigh;
    }
    
    public void setRangeHigh(int newValue) {
        fireBeforePropertyChange(P_RangeHigh, this.rangeHigh, newValue);
        int old = rangeHigh;
        this.rangeHigh = newValue;
        firePropertyChange(P_RangeHigh, old, this.rangeHigh);
    }
    @OAProperty(displayName = "Range Increment", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getRangeIncrement() {
        return rangeIncrement;
    }
    
    public void setRangeIncrement(int newValue) {
        fireBeforePropertyChange(P_RangeIncrement, this.rangeIncrement, newValue);
        int old = rangeIncrement;
        this.rangeIncrement = newValue;
        firePropertyChange(P_RangeIncrement, old, this.rangeIncrement);
    }
    @OAProperty(displayName = "Merchant Code", maxLength = 25, displayLength = 15, columnLength = 10)
    @OAColumn(maxLength = 25)
    public String getMerchantCode() {
        return merchantCode;
    }
    
    public void setMerchantCode(String newValue) {
        fireBeforePropertyChange(P_MerchantCode, this.merchantCode, newValue);
        String old = merchantCode;
        this.merchantCode = newValue;
        firePropertyChange(P_MerchantCode, old, this.merchantCode);
    }
    @OAMany(
        displayName = "Award Card Orders", 
        toClass = AwardCardOrder.class, 
        reverseName = AwardCardOrder.P_Card, 
        createMethod = false
    )
    private Hub<AwardCardOrder> getAwardCardOrders() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Award Type", 
        isCalculated = true, 
        reverseName = AwardType.P_AvailableCards, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private AwardType getAwardType() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Calc Award Types", 
        toClass = AwardType.class, 
        isCalculated = true, 
        reverseName = AwardType.P_CalcImagineCard, 
        createMethod = false
    )
    private Hub<AwardType> getCalcAwardTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Card Vendor", 
        reverseName = CardVendor.P_Cards, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"CardVendorId"})
    public CardVendor getCardVendor() {
        if (cardVendor == null) {
            cardVendor = (CardVendor) getObject(P_CardVendor);
        }
        return cardVendor;
    }
    
    public void setCardVendor(CardVendor newValue) {
        fireBeforePropertyChange(P_CardVendor, this.cardVendor, newValue);
        CardVendor old = this.cardVendor;
        this.cardVendor = newValue;
        firePropertyChange(P_CardVendor, old, this.cardVendor);
    }
    
    @OAMany(
        displayName = "Hi5 Order Cards", 
        toClass = HifiveOrderCard.class, 
        reverseName = HifiveOrderCard.P_Card, 
        createMethod = false
    )
    private Hub<HifiveOrderCard> getHifiveOrderCards() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Image Locations", 
        toClass = Location.class, 
        recursive = false, 
        reverseName = Location.P_ImagineCard, 
        createMethod = false
    )
    private Hub<Location> getImageLocations() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Image", 
        owner = true, 
        reverseName = ImageStore.P_Card, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"ImageStoreId"})
    public ImageStore getImageStore() {
        if (imageStore == null) {
            imageStore = (ImageStore) getObject(P_ImageStore);
        }
        return imageStore;
    }
    
    public void setImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_ImageStore, this.imageStore, newValue);
        ImageStore old = this.imageStore;
        this.imageStore = newValue;
        firePropertyChange(P_ImageStore, old, this.imageStore);
    }
    
    @OAMany(
        displayName = "Imagine Award Types", 
        toClass = AwardType.class, 
        reverseName = AwardType.P_ImagineCard, 
        createMethod = false
    )
    private Hub<AwardType> getImagineAwardTypes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Imagine Employee Awards", 
        toClass = EmployeeAward.class, 
        isCalculated = true, 
        reverseName = EmployeeAward.P_ImagineCard, 
        createMethod = false
    )
    private Hub<EmployeeAward> getImagineEmployeeAwards() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Imagine Programs", 
        toClass = Program.class, 
        reverseName = Program.P_ImagineCard, 
        createMethod = false
    )
    private Hub<Program> getImaginePrograms() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Inspire Imagine Employees", 
        toClass = Employee.class, 
        recursive = false, 
        isCalculated = true, 
        reverseName = Employee.P_InspireImagineCard, 
        createMethod = false
    )
    private Hub<Employee> getInspireImagineEmployees() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        toClass = Location.class, 
        recursive = false, 
        reverseName = Location.P_Cards, 
        createMethod = false
    )
    @OALinkTable(name = "LocationCard", indexName = "LocationCard", columns = {"CardId"})
    private Hub<Location> getLocations() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        toClass = Merchant.class, 
        reverseName = Merchant.P_Cards, 
        sortProperty = Merchant.P_Name
    )
    @OALinkTable(name = "MerchantCard", indexName = "MerchantCard", columns = {"CardId"})
    public Hub<Merchant> getMerchants() {
        if (hubMerchants == null) {
            hubMerchants = (Hub<Merchant>) getHub(P_Merchants);
        }
        return hubMerchants;
    }
    
    @OAMany(
        toClass = Program.class, 
        reverseName = Program.P_Cards, 
        createMethod = false
    )
    @OALinkTable(name = "ProgramCard", indexName = "ProgramCard", columns = {"CardId"})
    private Hub<Program> getPrograms() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        toClass = Value.class, 
        reverseName = Value.P_Cards, 
        sortProperty = Value.P_Value
    )
    @OALinkTable(name = "CardValue", indexName = "ValueCard", columns = {"CardId"})
    public Hub<Value> getValues() {
        if (hubValues == null) {
            hubValues = (Hub<Value>) getHub(P_Values);
        }
        return hubValues;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        date = rs.getDate(4);
        if (date != null) this.activeDate = new OADate(date);
        this.digitalCard = rs.getBoolean(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_DigitalCard, true);
        }
        this.traditionalCard = rs.getBoolean(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_TraditionalCard, true);
        }
        this.celebrateCard = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_CelebrateCard, true);
        }
        this.text = rs.getString(8);
        date = rs.getDate(9);
        if (date != null) this.inactiveDate = new OADate(date);
        this.rangeLow = (int) rs.getInt(10);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_RangeLow, true);
        }
        this.rangeHigh = (int) rs.getInt(11);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_RangeHigh, true);
        }
        this.rangeIncrement = (int) rs.getInt(12);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Card.P_RangeIncrement, true);
        }
        this.merchantCode = rs.getString(13);
        int cardVendorFkey = rs.getInt(14);
        if (!rs.wasNull() && cardVendorFkey > 0) {
            setProperty(P_CardVendor, new OAObjectKey(cardVendorFkey));
        }
        int imageStoreFkey = rs.getInt(15);
        if (!rs.wasNull() && imageStoreFkey > 0) {
            setProperty(P_ImageStore, new OAObjectKey(imageStoreFkey));
        }
        if (rs.getMetaData().getColumnCount() != 15) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
