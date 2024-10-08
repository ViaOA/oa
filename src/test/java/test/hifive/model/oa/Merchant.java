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
    shortName = "mer",
    displayName = "Merchant",
    displayProperty = "name",
    sortProperty = "name",
    rootTreePropertyPaths = {
        "[MerchantCategory]."+MerchantCategory.P_Merchants
    }
)
@OATable(
)
public class Merchant extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
     
     
    public static final String PROPERTY_Cards = "Cards";
    public static final String P_Cards = "Cards";
    public static final String PROPERTY_ImageStore = "ImageStore";
    public static final String P_ImageStore = "ImageStore";
    public static final String PROPERTY_MerchantCategories = "MerchantCategories";
    public static final String P_MerchantCategories = "MerchantCategories";
     
    protected int id;
    protected OADate created;
    protected String name;
    protected String description;
    protected String text;
     
    // Links to other objects.
    protected transient Hub<Card> hubCards;
    protected transient ImageStore imageStore;
    protected transient Hub<MerchantCategory> hubMerchantCategories;
     
    public Merchant() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Merchant(int id) {
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
    @OAProperty(maxLength = 150, displayLength = 25, columnLength = 20)
    @OAColumn(maxLength = 150)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 175, displayLength = 25, columnLength = 20)
    @OAColumn(maxLength = 175)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        fireBeforePropertyChange(P_Description, this.description, newValue);
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
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
    @OAMany(
        toClass = Card.class, 
        reverseName = Card.P_Merchants, 
        sortProperty = Card.P_Name
    )
    @OALinkTable(name = "MerchantCard", indexName = "CardMerchant", columns = {"MerchantId"})
    public Hub<Card> getCards() {
        if (hubCards == null) {
            hubCards = (Hub<Card>) getHub(P_Cards);
        }
        return hubCards;
    }
    
    @OAOne(
        displayName = "Image", 
        owner = true, 
        reverseName = ImageStore.P_Merchant, 
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
        displayName = "Merchant Categories", 
        toClass = MerchantCategory.class, 
        recursive = false, 
        reverseName = MerchantCategory.P_Merchants
    )
    @OALinkTable(name = "MerchantCategoryMerchant", indexName = "MerchantCategoryMerchant", columns = {"MerchantId"})
    public Hub<MerchantCategory> getMerchantCategories() {
        if (hubMerchantCategories == null) {
            hubMerchantCategories = (Hub<MerchantCategory>) getHub(P_MerchantCategories);
        }
        return hubMerchantCategories;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        this.description = rs.getString(4);
        this.text = rs.getString(5);
        int imageStoreFkey = rs.getInt(6);
        if (!rs.wasNull() && imageStoreFkey > 0) {
            setProperty(P_ImageStore, new OAObjectKey(imageStoreFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
