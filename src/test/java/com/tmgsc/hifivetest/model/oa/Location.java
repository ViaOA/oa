// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
import com.viaoa.util.OADate;
 
@OAClass(
    shortName = "loc",
    displayName = "Location",
    displayProperty = "code",
    rootTreePropertyPaths = {
        "[Company]."+Company.P_Programs+"."+Program.P_Locations
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "LocationInspireAwardType", columns = { @OAIndexColumn(name = "InspireAwardTypeId") }), 
        @OAIndex(name = "LocationParentLocation", columns = { @OAIndexColumn(name = "ParentLocationId") }), 
        @OAIndex(name = "LocationProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class Location extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Name2 = "Name2";
    public static final String P_Name2 = "Name2";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Code = "Code";
    public static final String P_Code = "Code";
    public static final String PROPERTY_CharityGoal = "CharityGoal";
    public static final String P_CharityGoal = "CharityGoal";
    public static final String PROPERTY_FromEmailAddress = "FromEmailAddress";
    public static final String P_FromEmailAddress = "FromEmailAddress";
    public static final String PROPERTY_TestEmail = "TestEmail";
    public static final String P_TestEmail = "TestEmail";
     
    public static final String PROPERTY_CalcCharityTotal = "CalcCharityTotal";
    public static final String P_CalcCharityTotal = "CalcCharityTotal";
     
    public static final String PROPERTY_AddOnItems = "AddOnItems";
    public static final String P_AddOnItems = "AddOnItems";
    public static final String PROPERTY_Address = "Address";
    public static final String P_Address = "Address";
    public static final String PROPERTY_AnnouncementDocument = "AnnouncementDocument";
    public static final String P_AnnouncementDocument = "AnnouncementDocument";
    public static final String PROPERTY_AwardTypes = "AwardTypes";
    public static final String P_AwardTypes = "AwardTypes";
    public static final String PROPERTY_CalcCountryCode = "CalcCountryCode";
    public static final String P_CalcCountryCode = "CalcCountryCode";
    public static final String PROPERTY_CalcLogoStampImageStore = "CalcLogoStampImageStore";
    public static final String P_CalcLogoStampImageStore = "CalcLogoStampImageStore";
    public static final String PROPERTY_Cards = "Cards";
    public static final String P_Cards = "Cards";
    public static final String PROPERTY_CeoImageStore = "CeoImageStore";
    public static final String P_CeoImageStore = "CeoImageStore";
    public static final String PROPERTY_CeoSignatureImageStore = "CeoSignatureImageStore";
    public static final String P_CeoSignatureImageStore = "CeoSignatureImageStore";
    public static final String PROPERTY_Charities = "Charities";
    public static final String P_Charities = "Charities";
    public static final String PROPERTY_CountryCode = "CountryCode";
    public static final String P_CountryCode = "CountryCode";
    public static final String PROPERTY_Ecards = "Ecards";
    public static final String P_Ecards = "Ecards";
    public static final String PROPERTY_Employees = "Employees";
    public static final String P_Employees = "Employees";
    public static final String PROPERTY_HifiveQualities = "HifiveQualities";
    public static final String P_HifiveQualities = "HifiveQualities";
    public static final String PROPERTY_ImagineCard = "ImagineCard";
    public static final String P_ImagineCard = "ImagineCard";
    public static final String PROPERTY_InspireAwardLevelLocationValues = "InspireAwardLevelLocationValues";
    public static final String P_InspireAwardLevelLocationValues = "InspireAwardLevelLocationValues";
    public static final String PROPERTY_InspireAwardType = "InspireAwardType";
    public static final String P_InspireAwardType = "InspireAwardType";
    public static final String PROPERTY_InspireCoreValues = "InspireCoreValues";
    public static final String P_InspireCoreValues = "InspireCoreValues";
    public static final String PROPERTY_LocationEmailTypes = "LocationEmailTypes";
    public static final String P_LocationEmailTypes = "LocationEmailTypes";
    public static final String PROPERTY_LocationPageGroups = "LocationPageGroups";
    public static final String P_LocationPageGroups = "LocationPageGroups";
    public static final String PROPERTY_LocationPageInfos = "LocationPageInfos";
    public static final String P_LocationPageInfos = "LocationPageInfos";
    public static final String PROPERTY_Locations = "Locations";
    public static final String P_Locations = "Locations";
    public static final String PROPERTY_LocationType = "LocationType";
    public static final String P_LocationType = "LocationType";
    public static final String PROPERTY_LogoImageStore = "LogoImageStore";
    public static final String P_LogoImageStore = "LogoImageStore";
    public static final String PROPERTY_LogoStampImageStore = "LogoStampImageStore";
    public static final String P_LogoStampImageStore = "LogoStampImageStore";
    public static final String PROPERTY_NominationQuiz = "NominationQuiz";
    public static final String P_NominationQuiz = "NominationQuiz";
    public static final String PROPERTY_PageTheme = "PageTheme";
    public static final String P_PageTheme = "PageTheme";
    public static final String PROPERTY_ParentLocation = "ParentLocation";
    public static final String P_ParentLocation = "ParentLocation";
    public static final String PROPERTY_PointsAwardLevels = "PointsAwardLevels";
    public static final String P_PointsAwardLevels = "PointsAwardLevels";
    public static final String PROPERTY_PointsCoreValues = "PointsCoreValues";
    public static final String P_PointsCoreValues = "PointsCoreValues";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
    public static final String PROPERTY_ProgramEvents = "ProgramEvents";
    public static final String P_ProgramEvents = "ProgramEvents";
    public static final String PROPERTY_ProgramFaqs = "ProgramFaqs";
    public static final String P_ProgramFaqs = "ProgramFaqs";
     
    protected int id;
    protected OADate created;
    protected String name;
    protected String name2;
    protected int seq;
    protected String code;
    protected double charityGoal;
    protected String fromEmailAddress;
    protected String testEmail;
     
    // Links to other objects.
    protected transient Hub<AddOnItem> hubAddOnItems;
    protected transient Address address;
    protected transient ProgramDocument announcementDocument;
    protected transient Hub<AwardType> hubAwardTypes;
    protected transient CountryCode calcCountryCode;
    protected transient ImageStore calcLogoStampImageStore;
    protected transient Hub<Card> hubCards;
    protected transient ImageStore ceoImageStore;
    protected transient ImageStore ceoSignatureImageStore;
    protected transient Hub<Charity> hubCharities;
    protected transient CountryCode countryCode;
    protected transient Hub<Ecard> hubEcards;
    // protected transient Hub<Employee> hubEmployees;
    protected transient Hub<HifiveQuality> hubHifiveQualities;
    protected transient Card imagineCard;
    protected transient Hub<InspireAwardLevelLocationValue> hubInspireAwardLevelLocationValues;
    protected transient AwardType inspireAwardType;
    protected transient Hub<InspireCoreValue> hubInspireCoreValues;
    protected transient Hub<LocationEmailType> hubLocationEmailTypes;
    protected transient Hub<LocationPageGroup> hubLocationPageGroups;
    protected transient Hub<LocationPageInfo> hubLocationPageInfos;
    protected transient Hub<Location> hubLocations;
    protected transient LocationType locationType;
    protected transient ImageStore logoImageStore;
    protected transient ImageStore logoStampImageStore;
    protected transient Quiz nominationQuiz;
    protected transient PageTheme pageTheme;
    protected transient Location parentLocation;
    protected transient Hub<PointsAwardLevel> hubPointsAwardLevels;
    protected transient Hub<PointsCoreValue> hubPointsCoreValues;
    protected transient Program program;
    protected transient Hub<ProgramEvent> hubProgramEvents;
    protected transient Hub<ProgramFaq> hubProgramFaqs;
     
    public Location() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Location(int id) {
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
    @OAProperty(maxLength = 125, displayLength = 25, columnLength = 24)
    @OAColumn(maxLength = 125)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 125, displayLength = 22, columnLength = 15)
    @OAColumn(maxLength = 125)
    public String getName2() {
        return name2;
    }
    
    public void setName2(String newValue) {
        fireBeforePropertyChange(P_Name2, this.name2, newValue);
        String old = name2;
        this.name2 = newValue;
        firePropertyChange(P_Name2, old, this.name2);
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
    @OAProperty(maxLength = 24, displayLength = 18, columnLength = 15)
    @OAColumn(maxLength = 24)
    public String getCode() {
        return code;
    }
    
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    @OAProperty(displayName = "Charity Goal", decimalPlaces = 2, isCurrency = true, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getCharityGoal() {
        return charityGoal;
    }
    
    public void setCharityGoal(double newValue) {
        fireBeforePropertyChange(P_CharityGoal, this.charityGoal, newValue);
        double old = charityGoal;
        this.charityGoal = newValue;
        firePropertyChange(P_CharityGoal, old, this.charityGoal);
    }
    @OAProperty(displayName = "From Email Address", maxLength = 75, displayLength = 25, columnLength = 18)
    @OAColumn(maxLength = 75)
    public String getFromEmailAddress() {
        return fromEmailAddress;
    }
    
    public void setFromEmailAddress(String newValue) {
        fireBeforePropertyChange(P_FromEmailAddress, this.fromEmailAddress, newValue);
        String old = fromEmailAddress;
        this.fromEmailAddress = newValue;
        firePropertyChange(P_FromEmailAddress, old, this.fromEmailAddress);
    }
    @OAProperty(displayName = "Test Email", maxLength = 125, displayLength = 25, columnLength = 18)
    @OAColumn(maxLength = 125)
    public String getTestEmail() {
        return testEmail;
    }
    
    public void setTestEmail(String newValue) {
        fireBeforePropertyChange(P_TestEmail, this.testEmail, newValue);
        String old = testEmail;
        this.testEmail = newValue;
        firePropertyChange(P_TestEmail, old, this.testEmail);
    }
    @OACalculatedProperty(displayName = "Calc Charity Total", decimalPlaces = 2, displayLength = 7, properties = {P_Locations+"."+Location.P_CalcCharityTotal})
    public double getCalcCharityTotal() {
        double tot = 0;
        for (Employee emp : getEmployees()) {
            tot += emp.getCalcCharityTotal();
        }
        for (Location loc : getLocations()) {
            tot += loc.getCalcCharityTotal();
        }
        return tot;
    }
     
    @OAMany(
        displayName = "Add On Items", 
        toClass = AddOnItem.class, 
        reverseName = AddOnItem.P_Location
    )
    public Hub<AddOnItem> getAddOnItems() {
        if (hubAddOnItems == null) {
            hubAddOnItems = (Hub<AddOnItem>) getHub(P_AddOnItems);
        }
        return hubAddOnItems;
    }
    
    @OAOne(
        reverseName = Address.P_Location, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"AddressId"})
    public Address getAddress() {
        if (address == null) {
            address = (Address) getObject(P_Address);
        }
        return address;
    }
    
    public void setAddress(Address newValue) {
        fireBeforePropertyChange(P_Address, this.address, newValue);
        Address old = this.address;
        this.address = newValue;
        firePropertyChange(P_Address, old, this.address);
    }
    
    @OAOne(
        displayName = "CEO Letter", 
        reverseName = ProgramDocument.P_AnnouncementLocation, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"AnnouncementDocumentId"})
    public ProgramDocument getAnnouncementDocument() {
        if (announcementDocument == null) {
            announcementDocument = (ProgramDocument) getObject(P_AnnouncementDocument);
        }
        return announcementDocument;
    }
    
    public void setAnnouncementDocument(ProgramDocument newValue) {
        fireBeforePropertyChange(P_AnnouncementDocument, this.announcementDocument, newValue);
        ProgramDocument old = this.announcementDocument;
        this.announcementDocument = newValue;
        firePropertyChange(P_AnnouncementDocument, old, this.announcementDocument);
    }
    
    @OAMany(
        displayName = "Award Types", 
        toClass = AwardType.class, 
        owner = true, 
        reverseName = AwardType.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true, 
        sortProperty = AwardType.P_YearsService
    )
    public Hub<AwardType> getAwardTypes() {
        if (hubAwardTypes == null) {
            hubAwardTypes = (Hub<AwardType>) getHub(P_AwardTypes);
        }
        return hubAwardTypes;
    }
    
    @OAOne(
        displayName = "Calc Country Code", 
        isCalculated = true, 
        reverseName = CountryCode.P_CalcLocations
    )
    public CountryCode getCalcCountryCode() {
        CountryCode cc = null;
        Location loc = this;
        Program prog = null;
        for ( ; loc != null && cc == null; loc = loc.getParentLocation()) {
            cc = loc.getCountryCode();
            if (prog == null) prog = loc.getProgram();
        }
        if (cc == null && prog != null) cc = prog.getCountryCode();
        return cc;
    }
    
    public void setCalcCountryCode(CountryCode newValue) {
    }
    
    @OAOne(
        displayName = "Calc Logo Stamp Image Store", 
        isCalculated = true, 
        owner = true, 
        reverseName = ImageStore.P_CalcLogoStampLocation, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    public ImageStore getCalcLogoStampImageStore() {
        ImageStore is = getLogoStampImageStore();
        if (is != null) return is;    
        Location loc = getParentLocation();
        if (loc != null) return loc.getCalcLogoStampImageStore(); 
        return null;
    }
    
    @OAMany(
        displayName = "Gift Cards", 
        toClass = Card.class, 
        reverseName = Card.P_Locations
    )
    @OALinkTable(name = "LocationCard", indexName = "CardLocation", columns = {"LocationId"})
    public Hub<Card> getCards() {
        if (hubCards == null) {
            hubCards = (Hub<Card>) getHub(P_Cards);
        }
        return hubCards;
    }
    
    @OAOne(
        displayName = "CEO Image", 
        owner = true, 
        reverseName = ImageStore.P_CeoLocation, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"CeoImageStoreId"})
    public ImageStore getCeoImageStore() {
        if (ceoImageStore == null) {
            ceoImageStore = (ImageStore) getObject(P_CeoImageStore);
        }
        return ceoImageStore;
    }
    
    public void setCeoImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_CeoImageStore, this.ceoImageStore, newValue);
        ImageStore old = this.ceoImageStore;
        this.ceoImageStore = newValue;
        firePropertyChange(P_CeoImageStore, old, this.ceoImageStore);
    }
    
    @OAOne(
        displayName = "Ceo Signature", 
        owner = true, 
        reverseName = ImageStore.P_CeoSignatureLocation, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"CeoSignatureImageStoreId"})
    public ImageStore getCeoSignatureImageStore() {
        if (ceoSignatureImageStore == null) {
            ceoSignatureImageStore = (ImageStore) getObject(P_CeoSignatureImageStore);
        }
        return ceoSignatureImageStore;
    }
    
    public void setCeoSignatureImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_CeoSignatureImageStore, this.ceoSignatureImageStore, newValue);
        ImageStore old = this.ceoSignatureImageStore;
        this.ceoSignatureImageStore = newValue;
        firePropertyChange(P_CeoSignatureImageStore, old, this.ceoSignatureImageStore);
    }
    
    @OAMany(
        toClass = Charity.class, 
        reverseName = Charity.P_Locations
    )
    @OALinkTable(name = "LocationCharity", indexName = "CharityLocation", columns = {"LocationId"})
    public Hub<Charity> getCharities() {
        if (hubCharities == null) {
            hubCharities = (Hub<Charity>) getHub(P_Charities);
        }
        return hubCharities;
    }
    
    @OAOne(
        displayName = "Country Code", 
        reverseName = CountryCode.P_Locations
    )
    @OAFkey(columns = {"CountryCodeId"})
    public CountryCode getCountryCode() {
        if (countryCode == null) {
            countryCode = (CountryCode) getObject(P_CountryCode);
        }
        return countryCode;
    }
    
    public void setCountryCode(CountryCode newValue) {
        fireBeforePropertyChange(P_CountryCode, this.countryCode, newValue);
        CountryCode old = this.countryCode;
        this.countryCode = newValue;
        firePropertyChange(P_CountryCode, old, this.countryCode);
    }
    
    @OAMany(
        toClass = Ecard.class, 
        reverseName = Ecard.P_Location
    )
    @OALinkTable(name = "LocationEcard", indexName = "EcardLocation", columns = {"LocationId"})
    public Hub<Ecard> getEcards() {
        if (hubEcards == null) {
            hubEcards = (Hub<Ecard>) getHub(P_Ecards);
        }
        return hubEcards;
    }
    
    @OAMany(
        toClass = Employee.class, 
        recursive = false, 
        reverseName = Employee.P_Location, 
        cascadeSave = true,
        cacheSize=20
    )
    public Hub<Employee> getEmployees() {
        Hub<Employee> hubEmployees = (Hub<Employee>) getHub(P_Employees);
        return hubEmployees;
    }
    
    @OAMany(
        displayName = "Hifive Qualities", 
        toClass = HifiveQuality.class, 
        reverseName = HifiveQuality.P_Location
    )
    @OALinkTable(name = "LocationHifiveQuality", indexName = "HifiveQualityLocation", columns = {"LocationId"})
    public Hub<HifiveQuality> getHifiveQualities() {
        if (hubHifiveQualities == null) {
            hubHifiveQualities = (Hub<HifiveQuality>) getHub(P_HifiveQualities);
        }
        return hubHifiveQualities;
    }
    
    @OAOne(
        displayName = "Imagine Card", 
        reverseName = Card.P_ImageLocations, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ImagineCardId"})
    public Card getImagineCard() {
        if (imagineCard == null) {
            imagineCard = (Card) getObject(P_ImagineCard);
        }
        return imagineCard;
    }
    
    public void setImagineCard(Card newValue) {
        fireBeforePropertyChange(P_ImagineCard, this.imagineCard, newValue);
        Card old = this.imagineCard;
        this.imagineCard = newValue;
        firePropertyChange(P_ImagineCard, old, this.imagineCard);
    }
    
    @OAMany(
        displayName = "Inspire Values", 
        toClass = InspireAwardLevelLocationValue.class, 
        owner = true, 
        reverseName = InspireAwardLevelLocationValue.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true, 
        matchHub = (Location.P_Program+"."+Program.P_InspireAwardLevels), 
        matchProperty = InspireAwardLevelLocationValue.P_InspireAwardLevel, 
        sortProperty = (InspireAwardLevelLocationValue.P_InspireAwardLevel+"."+InspireAwardLevel.P_Seq)
    )
    public Hub<InspireAwardLevelLocationValue> getInspireAwardLevelLocationValues() {
        if (hubInspireAwardLevelLocationValues == null) {
            hubInspireAwardLevelLocationValues = (Hub<InspireAwardLevelLocationValue>) getHub(P_InspireAwardLevelLocationValues);
        }
        return hubInspireAwardLevelLocationValues;
    }
    
    @OAOne(
        displayName = "Inspire Award Type", 
        owner = true, 
        reverseName = AwardType.P_InspireLocation, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"InspireAwardTypeId"})
    public AwardType getInspireAwardType() {
        if (inspireAwardType == null) {
            inspireAwardType = (AwardType) getObject(P_InspireAwardType);
        }
        return inspireAwardType;
    }
    
    public void setInspireAwardType(AwardType newValue) {
        fireBeforePropertyChange(P_InspireAwardType, this.inspireAwardType, newValue);
        AwardType old = this.inspireAwardType;
        this.inspireAwardType = newValue;
        firePropertyChange(P_InspireAwardType, old, this.inspireAwardType);
    }
    
    @OAMany(
        displayName = "Inspire Core Values", 
        toClass = InspireCoreValue.class, 
        owner = true, 
        reverseName = InspireCoreValue.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true, 
        seqProperty = InspireCoreValue.P_Seq, 
        sortProperty = InspireCoreValue.P_Seq
    )
    @OALinkTable(name = "LocationInspireCoreValue", indexName = "InspireCoreValueLocation", columns = {"LocationId"})
    public Hub<InspireCoreValue> getInspireCoreValues() {
        if (hubInspireCoreValues == null) {
            hubInspireCoreValues = (Hub<InspireCoreValue>) getHub(P_InspireCoreValues);
        }
        return hubInspireCoreValues;
    }
    
    @OAMany(
        displayName = "Location Email Types", 
        toClass = LocationEmailType.class, 
        owner = true, 
        reverseName = LocationEmailType.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<LocationEmailType> getLocationEmailTypes() {
        if (hubLocationEmailTypes == null) {
            hubLocationEmailTypes = (Hub<LocationEmailType>) getHub(P_LocationEmailTypes);
        }
        return hubLocationEmailTypes;
    }
    
    @OAMany(
        displayName = "Location Page Groups", 
        toClass = LocationPageGroup.class, 
        owner = true, 
        reverseName = LocationPageGroup.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true, 
        seqProperty = LocationPageGroup.P_Seq, 
        uniqueProperty = LocationPageGroup.P_PageGroup, 
        sortProperty = LocationPageGroup.P_Seq
    )
    public Hub<LocationPageGroup> getLocationPageGroups() {
        if (hubLocationPageGroups == null) {
            hubLocationPageGroups = (Hub<LocationPageGroup>) getHub(P_LocationPageGroups);
        }
        return hubLocationPageGroups;
    }
    
    @OAMany(
        displayName = "Web Pages", 
        toClass = LocationPageInfo.class, 
        owner = true, 
        reverseName = LocationPageInfo.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true, 
        uniqueProperty = LocationPageInfo.P_PageInfo
    )
    public Hub<LocationPageInfo> getLocationPageInfos() {
        if (hubLocationPageInfos == null) {
            hubLocationPageInfos = (Hub<LocationPageInfo>) getHub(P_LocationPageInfos);
        }
        return hubLocationPageInfos;
    }
    
    @OAMany(
        toClass = Location.class, 
        recursive = true, 
        reverseName = Location.P_ParentLocation
    )
    public Hub<Location> getLocations() {
        if (hubLocations == null) {
            hubLocations = (Hub<Location>) getHub(P_Locations);
        }
        return hubLocations;
    }
    
    @OAOne(
        displayName = "Location Type", 
        reverseName = LocationType.P_Locations, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"LocationTypeId"})
    public LocationType getLocationType() {
        if (locationType == null) {
            locationType = (LocationType) getObject(P_LocationType);
        }
        return locationType;
    }
    
    public void setLocationType(LocationType newValue) {
        fireBeforePropertyChange(P_LocationType, this.locationType, newValue);
        LocationType old = this.locationType;
        this.locationType = newValue;
        firePropertyChange(P_LocationType, old, this.locationType);
    }
    
    @OAOne(
        displayName = "Logo Image", 
        owner = true, 
        reverseName = ImageStore.P_LocationLogo, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"LogoImageStoreId"})
    public ImageStore getLogoImageStore() {
        if (logoImageStore == null) {
            logoImageStore = (ImageStore) getObject(P_LogoImageStore);
        }
        return logoImageStore;
    }
    
    public void setLogoImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_LogoImageStore, this.logoImageStore, newValue);
        ImageStore old = this.logoImageStore;
        this.logoImageStore = newValue;
        firePropertyChange(P_LogoImageStore, old, this.logoImageStore);
    }
    
    @OAOne(
        displayName = "Stamp Image", 
        owner = true, 
        reverseName = ImageStore.P_LogoStampLocation, 
        cascadeSave = true, 
        cascadeDelete = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"LogoStampImageStoreId"})
    public ImageStore getLogoStampImageStore() {
        if (logoStampImageStore == null) {
            logoStampImageStore = (ImageStore) getObject(P_LogoStampImageStore);
        }
        return logoStampImageStore;
    }
    
    public void setLogoStampImageStore(ImageStore newValue) {
        fireBeforePropertyChange(P_LogoStampImageStore, this.logoStampImageStore, newValue);
        ImageStore old = this.logoStampImageStore;
        this.logoStampImageStore = newValue;
        firePropertyChange(P_LogoStampImageStore, old, this.logoStampImageStore);
    }
    
    @OAOne(
        displayName = "Nomination Quiz", 
        reverseName = Quiz.P_Location
    )
    public Quiz getNominationQuiz() {
        if (nominationQuiz == null) {
            nominationQuiz = (Quiz) getObject(P_NominationQuiz);
        }
        return nominationQuiz;
    }
    
    public void setNominationQuiz(Quiz newValue) {
        fireBeforePropertyChange(P_NominationQuiz, this.nominationQuiz, newValue);
        Quiz old = this.nominationQuiz;
        this.nominationQuiz = newValue;
        firePropertyChange(P_NominationQuiz, old, this.nominationQuiz);
    }
    
    @OAOne(
        displayName = "Page Theme", 
        reverseName = PageTheme.P_Locations
    )
    @OAFkey(columns = {"PageThemeId"})
    public PageTheme getPageTheme() {
        if (pageTheme == null) {
            pageTheme = (PageTheme) getObject(P_PageTheme);
        }
        return pageTheme;
    }
    
    public void setPageTheme(PageTheme newValue) {
        fireBeforePropertyChange(P_PageTheme, this.pageTheme, newValue);
        PageTheme old = this.pageTheme;
        this.pageTheme = newValue;
        firePropertyChange(P_PageTheme, old, this.pageTheme);
    }
    
    @OAOne(
        displayName = "Parent Location", 
        reverseName = Location.P_Locations
    )
    @OAFkey(columns = {"ParentLocationId"})
    public Location getParentLocation() {
        if (parentLocation == null) {
            parentLocation = (Location) getObject(P_ParentLocation);
        }
        return parentLocation;
    }
    
    public void setParentLocation(Location newValue) {
        fireBeforePropertyChange(P_ParentLocation, this.parentLocation, newValue);
        Location old = this.parentLocation;
        this.parentLocation = newValue;
        firePropertyChange(P_ParentLocation, old, this.parentLocation);
    }
    
    @OAMany(
        displayName = "Points Award Levels", 
        toClass = PointsAwardLevel.class, 
        reverseName = PointsAwardLevel.P_Location, 
        seqProperty = PointsAwardLevel.P_Seq, 
        sortProperty = PointsAwardLevel.P_Seq
    )
    public Hub<PointsAwardLevel> getPointsAwardLevels() {
        if (hubPointsAwardLevels == null) {
            hubPointsAwardLevels = (Hub<PointsAwardLevel>) getHub(P_PointsAwardLevels);
        }
        return hubPointsAwardLevels;
    }
    
    @OAMany(
        displayName = "Points Core Values", 
        toClass = PointsCoreValue.class, 
        reverseName = PointsCoreValue.P_Location
    )
    public Hub<PointsCoreValue> getPointsCoreValues() {
        if (hubPointsCoreValues == null) {
            hubPointsCoreValues = (Hub<PointsCoreValue>) getHub(P_PointsCoreValues);
        }
        return hubPointsCoreValues;
    }
    
    @OAOne(
        reverseName = Program.P_Locations, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ProgramId"})
    public Program getProgram() {
        if (program == null) {
            program = (Program) getObject(P_Program);
        }
        return program;
    }
    
    public void setProgram(Program newValue) {
if (newValue == null) {
    int xx = 4;
    xx++;
}
        fireBeforePropertyChange(P_Program, this.program, newValue);
        Program old = this.program;
        this.program = newValue;
        firePropertyChange(P_Program, old, this.program);
    }
    
    @OAMany(
        displayName = "Program Events", 
        toClass = ProgramEvent.class, 
        owner = true, 
        reverseName = ProgramEvent.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<ProgramEvent> getProgramEvents() {
        if (hubProgramEvents == null) {
            hubProgramEvents = (Hub<ProgramEvent>) getHub(P_ProgramEvents);
        }
        return hubProgramEvents;
    }
    
    @OAMany(
        displayName = "Program Faqs", 
        toClass = ProgramFaq.class, 
        owner = true, 
        reverseName = ProgramFaq.P_Location, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    @OALinkTable(name = "LocationProgramFaq", indexName = "ProgramFaqLocation", columns = {"LocationId"})
    public Hub<ProgramFaq> getProgramFaqs() {
        if (hubProgramFaqs == null) {
            hubProgramFaqs = (Hub<ProgramFaq>) getHub(P_ProgramFaqs);
        }
        return hubProgramFaqs;
    }
    
    // deleteEmployees - delete all employees for ths location
    public void deleteEmployees() {
        //LocationDelegate.deleteEmployees(this);
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        this.name2 = rs.getString(4);
        this.seq = (int) rs.getInt(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Location.P_Seq, true);
        }
        this.code = rs.getString(6);
        this.charityGoal = (double) rs.getDouble(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Location.P_CharityGoal, true);
        }
        this.fromEmailAddress = rs.getString(8);
        this.testEmail = rs.getString(9);
        int addressFkey = rs.getInt(10);
        if (!rs.wasNull() && addressFkey > 0) {
            setProperty(P_Address, new OAObjectKey(addressFkey));
        }
        int announcementDocumentFkey = rs.getInt(11);
        if (!rs.wasNull() && announcementDocumentFkey > 0) {
            setProperty(P_AnnouncementDocument, new OAObjectKey(announcementDocumentFkey));
        }
        int ceoImageStoreFkey = rs.getInt(12);
        if (!rs.wasNull() && ceoImageStoreFkey > 0) {
            setProperty(P_CeoImageStore, new OAObjectKey(ceoImageStoreFkey));
        }
        int ceoSignatureImageStoreFkey = rs.getInt(13);
        if (!rs.wasNull() && ceoSignatureImageStoreFkey > 0) {
            setProperty(P_CeoSignatureImageStore, new OAObjectKey(ceoSignatureImageStoreFkey));
        }
        int countryCodeFkey = rs.getInt(14);
        if (!rs.wasNull() && countryCodeFkey > 0) {
            setProperty(P_CountryCode, new OAObjectKey(countryCodeFkey));
        }
        int imagineCardFkey = rs.getInt(15);
        if (!rs.wasNull() && imagineCardFkey > 0) {
            setProperty(P_ImagineCard, new OAObjectKey(imagineCardFkey));
        }
        int inspireAwardTypeFkey = rs.getInt(16);
        if (!rs.wasNull() && inspireAwardTypeFkey > 0) {
            setProperty(P_InspireAwardType, new OAObjectKey(inspireAwardTypeFkey));
        }
        int locationTypeFkey = rs.getInt(17);
        if (!rs.wasNull() && locationTypeFkey > 0) {
            setProperty(P_LocationType, new OAObjectKey(locationTypeFkey));
        }
        int logoImageStoreFkey = rs.getInt(18);
        if (!rs.wasNull() && logoImageStoreFkey > 0) {
            setProperty(P_LogoImageStore, new OAObjectKey(logoImageStoreFkey));
        }
        int logoStampImageStoreFkey = rs.getInt(19);
        if (!rs.wasNull() && logoStampImageStoreFkey > 0) {
            setProperty(P_LogoStampImageStore, new OAObjectKey(logoStampImageStoreFkey));
        }
        int pageThemeFkey = rs.getInt(20);
        if (!rs.wasNull() && pageThemeFkey > 0) {
            setProperty(P_PageTheme, new OAObjectKey(pageThemeFkey));
        }
        int parentLocationFkey = rs.getInt(21);
        if (!rs.wasNull() && parentLocationFkey > 0) {
            setProperty(P_ParentLocation, new OAObjectKey(parentLocationFkey));
        }
        int programFkey = rs.getInt(22);
        if (!rs.wasNull() && programFkey > 0) {
            setProperty(P_Program, new OAObjectKey(programFkey));
        }
        if (rs.getMetaData().getColumnCount() != 22) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
