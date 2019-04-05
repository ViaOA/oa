package test.hifive;


import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

import test.hifive.Resource;
import test.hifive.model.oa.*;

import com.viaoa.object.OAAnnotationDelegate;
import com.viaoa.annotation.OATable;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;

public class DataSource {
    private static Logger LOG = Logger.getLogger(DataSource.class.getName());
    protected OADataSourceJDBC jdbcDataSource;
    protected Database database;
    
    public DataSource() {
    }
    
    public void open() throws Exception {
        String driver = Resource.getValue(Resource.DB_JDBC_Driver);
        String jdbcUrl = Resource.getValue(Resource.DB_JDBC_URL);
        String user = Resource.getValue(Resource.DB_User);
        String pw = Resource.getValue(Resource.DB_Password);
        String pwBase64 = Resource.getValue(Resource.DB_Password_Base64);
        int dbmdType = Resource.getInt(Resource.DB_DBMD_Type);
        int minConnections = Resource.getInt(Resource.DB_MinConnections);
        int maxConnections = Resource.getInt(Resource.DB_MaxConnections);
    
        if (OAString.isEmpty(pw)) {
            String s = Base64.decode(pwBase64);
            if (!OAString.isEmpty(s)) pw = s;
        }
        open(driver, jdbcUrl, dbmdType, user, pw, minConnections, maxConnections);
    }
    
    protected void open(String driver, String jdbcUrl, int dbmdType, String user, String password, int min, int max) throws Exception {
        if (jdbcDataSource != null) return;
        String s = String.format("JDBC: driver=%s, url=%s, dbmdType=%d, user=%s", driver, jdbcUrl, dbmdType, user);
        LOG.fine(s);
        Database db = getDatabase();
    
        createDAO(db);
        DBMetaData dbmd = new DBMetaData(dbmdType, user, password, driver, jdbcUrl);
        dbmd.setMinConnections(min);
        dbmd.setMaxConnections(max);
        jdbcDataSource = new OADataSourceJDBC(db, dbmd);
    }
    
    public void close() {
        getOADataSource().close();
    }
    
    
    public OADataSourceJDBC getOADataSource() {
        return jdbcDataSource;
    }

    public Database getDatabase() {
        if (database != null) return database;
        try {
            database = createDatabaseFromClasses();
        }
        catch (Exception e) {
            throw new RuntimeException("error creating database", e);
        }
        return database;
    }    
    
    private Database createDatabaseFromClasses() throws Exception {
        Database database = new Database();
    
        Table table = new Table("NextNumber",com.viaoa.ds.autonumber.NextNumber.class); // ** Used by all OADataSource Database
        // NextNumber COLUMNS
        Column[] columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER);
        table.setColumns(columns);
        database.addTable(table);
    
        String packageName = Program.class.getPackage().getName();
        String[] fnames = OAReflect.getClasses(packageName);
    
        Class[] classes = null;
        for (String fn : fnames) {
            Class c = Class.forName(packageName + "." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            classes = (Class[]) OAArray.add(Class.class, classes, c);
        }
        OAAnnotationDelegate.update(database, classes);
        return database;
    }
    private Database createDatabase() {
        int NextNumber = 0;
        // TABLES
        int ABOUTME = 1;
        int ADDONITEM = 2;
        int ADDRESS = 3;
        int ADDRESSTYPE = 4;
        int ANSWER = 5;
        int ANSWERRESULT = 6;
        int AWARDCARDORDER = 7;
        int AWARDTYPE = 8;
        int CARD = 9;
        int CARDVENDOR = 10;
        int CASHSTARORDER = 11;
        int CATALOG = 12;
        int CHARITY = 13;
        int CODE = 14;
        int COMPANY = 15;
        int COUNTRYCODE = 16;
        int CURRENCYTYPE = 17;
        int CUSTOMDATA = 18;
        int ECARD = 19;
        int ECARDCATEGORY = 20;
        int EMAIL = 21;
        int EMAILTYPE = 22;
        int EMPLOYEE = 23;
        int EMPLOYEEAWARD = 24;
        int EMPLOYEEAWARDCHARITY = 25;
        int EMPLOYEECUSTOMDATA = 26;
        int EMPLOYEEECARD = 27;
        int EMPLOYEEECARDTO = 28;
        int EMPLOYEESURVEY = 29;
        int EMPLOYEESURVEYQUESTION = 30;
        int EMPLOYEETYPE = 31;
        int FAMILYMEMBER = 32;
        int HINDAORDER = 33;
        int HINDAORDERLINE = 34;
        int IMAGESTORE = 35;
        int INSPIRE = 36;
        int INSPIREAPPROVAL = 37;
        int INSPIREAWARDLEVEL = 38;
        int INSPIREAWARDLEVELLOCATIONVALUE = 39;
        int INSPIRECOREVALUE = 40;
        int INSPIREORDER = 41;
        int INSPIREORDERCHARITY = 42;
        int INSPIREORDERITEM = 43;
        int INSPIRERECIPIENT = 44;
        int ITEM = 45;
        int ITEMCATEGORY = 46;
        int ITEMTYPE = 47;
        int ITEMVENDOR = 48;
        int LINEITEM = 49;
        int LOCATION = 50;
        int LOCATIONPAGEGROUP = 51;
        int LOCATIONPAGEINFO = 52;
        int LOCATIONTYPE = 53;
        int LOGINIMAGE = 54;
        int LOGINIMAGESET = 55;
        int LOT = 56;
        int MERCHANT = 57;
        int MERCHANTCATEGORY = 58;
        int ORDERITEMTRACKING = 59;
        int ORDERTRACKING = 60;
        int ORDERTRACKINGSTATUS = 61;
        int PAGE = 62;
        int PAGEGROUP = 63;
        int PAGEGROUPPAGEINFO = 64;
        int PAGEINFO = 65;
        int PAGETHEME = 66;
        int PAGETHEMEPAGEINFO = 67;
        int PHONE = 68;
        int PHONETYPE = 69;
        int POINTSAPPROVAL = 70;
        int POINTSAWARDLEVEL = 71;
        int POINTSCONFIGURATION = 72;
        int POINTSCOREVALUE = 73;
        int POINTSRECORD = 74;
        int POINTSREQUEST = 75;
        int PRODUCT = 76;
        int PRODUCTAUDIT = 77;
        int PROGRAM = 78;
        int PROGRAMDOCUMENT = 79;
        int PROGRAMEMAILTYPE = 80;
        int PROGRAMEVENT = 81;
        int PROGRAMFAQ = 82;
        int PROGRAMPAGEGROUP = 83;
        int PROGRAMPAGEINFO = 84;
        int QUESTION = 85;
        int QUESTIONRESULT = 86;
        int QUIZ = 87;
        int QUIZRESULT = 88;
        int REPORT = 89;
        int REPORTCLASS = 90;
        int SECTION = 91;
        int SHIPTO = 92;
        int SURVEY = 93;
        int SURVEYANSWER = 94;
        int SURVEYQUESTION = 95;
        int USER = 96;
        int VALUE = 97;
        int WIDGET = 98;
        
        // LINK TABLES
        int AWARDTYPEINCLUDEITEM = 99;
        int EMPLOYEEPHONE = 100;
        int PROGRAMECARD = 101;
        int PROGRAMPROGRAMDOCUMENT = 102;
        int MERCHANTCARD = 103;
        int PROGRAMCARD = 104;
        int ITEMCATEGORYITEM = 105;
        int SECTIONITEM = 106;
        int AWARDTYPEEXCLUDEITEM = 107;
        int PROGRAMIMAGESTORE = 108;
        int IMAGESTOREPROGRAM = 109;
        int MERCHANTCATEGORYMERCHANT = 110;
        int PROGRAMWIDGET = 111;
        int REPLACEITEMITEM = 112;
        int ITEMTYPEITEM = 113;
        int LOCATIONECARD = 114;
        int LOCATIONPROGRAMFAQ = 115;
        int AWARDTYPEITEMTYPE = 116;
        int LOCATIONCARD = 117;
        int PROGRAMCHARITY = 118;
        int LOCATIONCHARITY = 119;
        int LOCATIONINSPIRECOREVALUE = 120;
        int CARDVALUE = 121;
        int EMAILEMPLOYEEAWARD = 122;
        int ADDONITEMAWARDTYPE = 123;
        int PROGRAMADDONITEM = 124;
        int INSPIREAPPROVALEMAIL = 125;
        int AWARDTYPEPRODUCT = 126;
        int INSPIREORDEREMAIL = 127;
        int POINTSREQUESTEMAIL = 128;
        int MAX = 129;
        
        Database db = new Database();
        Table[] tables = new Table[MAX];
        Column[] columns;
        Link[] links;
        Column[] fkeys;
        
        // TABLES
        tables[NextNumber] = new Table("NextNumber",com.viaoa.ds.autonumber.NextNumber.class); // ** Used by all OADataSource Database
        //tables[ABOUTME] = new Table("AboutMe", AboutMe.class);
        tables[ADDONITEM] = new Table("AddOnItem", AddOnItem.class);
        tables[ADDRESS] = new Table("Address", Address.class);
        tables[ADDRESSTYPE] = new Table("AddressType", AddressType.class);
        tables[ANSWER] = new Table("Answer", Answer.class);
        tables[ANSWERRESULT] = new Table("AnswerResult", AnswerResult.class);
        tables[AWARDCARDORDER] = new Table("AwardCardOrder", AwardCardOrder.class);
        tables[AWARDTYPE] = new Table("AwardType", AwardType.class);
        tables[CARD] = new Table("Card", Card.class);
        tables[CARDVENDOR] = new Table("CardVendor", CardVendor.class);
        tables[CASHSTARORDER] = new Table("CashstarOrder", CashstarOrder.class);
        tables[CATALOG] = new Table("Catalog", Catalog.class);
        tables[CHARITY] = new Table("Charity", Charity.class);
        //tables[CODE] = new Table("Code", Code.class);
        tables[COMPANY] = new Table("Company", Company.class);
        tables[COUNTRYCODE] = new Table("CountryCode", CountryCode.class);
        tables[CURRENCYTYPE] = new Table("CurrencyType", CurrencyType.class);
        tables[CUSTOMDATA] = new Table("CustomData", CustomData.class);
        tables[ECARD] = new Table("Ecard", Ecard.class);
        tables[ECARDCATEGORY] = new Table("EcardCategory", EcardCategory.class);
        tables[EMAIL] = new Table("Email", Email.class);
        tables[EMAILTYPE] = new Table("EmailType", EmailType.class);
        tables[EMPLOYEE] = new Table("Employee", Employee.class);
        tables[EMPLOYEEAWARD] = new Table("EmployeeAward", EmployeeAward.class);
        tables[EMPLOYEEAWARDCHARITY] = new Table("EmployeeAwardCharity", EmployeeAwardCharity.class);
        tables[EMPLOYEECUSTOMDATA] = new Table("EmployeeCustomData", EmployeeCustomData.class);
        tables[EMPLOYEEECARD] = new Table("EmployeeEcard", EmployeeEcard.class);
        tables[EMPLOYEEECARDTO] = new Table("EmployeeEcardTo", EmployeeEcardTo.class);
        tables[EMPLOYEESURVEY] = new Table("EmployeeSurvey", EmployeeSurvey.class);
        tables[EMPLOYEESURVEYQUESTION] = new Table("EmployeeSurveyQuestion", EmployeeSurveyQuestion.class);
        tables[EMPLOYEETYPE] = new Table("EmployeeType", EmployeeType.class);
        //tables[FAMILYMEMBER] = new Table("FamilyMember", FamilyMember.class);
        tables[HINDAORDER] = new Table("HindaOrder", HindaOrder.class);
        tables[HINDAORDERLINE] = new Table("HindaOrderLine", HindaOrderLine.class);
        tables[IMAGESTORE] = new Table("ImageStore", ImageStore.class);
        tables[INSPIRE] = new Table("Inspire", Inspire.class);
        tables[INSPIREAPPROVAL] = new Table("InspireApproval", InspireApproval.class);
        tables[INSPIREAWARDLEVEL] = new Table("InspireAwardLevel", InspireAwardLevel.class);
        tables[INSPIREAWARDLEVELLOCATIONVALUE] = new Table("InspireAwardLevelLocationValue", InspireAwardLevelLocationValue.class);
        tables[INSPIRECOREVALUE] = new Table("InspireCoreValue", InspireCoreValue.class);
        tables[INSPIREORDER] = new Table("InspireOrder", InspireOrder.class);
        tables[INSPIREORDERCHARITY] = new Table("InspireOrderCharity", InspireOrderCharity.class);
        tables[INSPIREORDERITEM] = new Table("InspireOrderItem", InspireOrderItem.class);
        tables[INSPIRERECIPIENT] = new Table("InspireRecipient", InspireRecipient.class);
        tables[ITEM] = new Table("Item", Item.class);
        tables[ITEMCATEGORY] = new Table("ItemCategory", ItemCategory.class);
        tables[ITEMTYPE] = new Table("ItemType", ItemType.class);
        tables[ITEMVENDOR] = new Table("ItemVendor", ItemVendor.class);
        //tables[LINEITEM] = new Table("LineItem", LineItem.class);
        tables[LOCATION] = new Table("Location", Location.class);
        tables[LOCATIONPAGEGROUP] = new Table("LocationPageGroup", LocationPageGroup.class);
        tables[LOCATIONPAGEINFO] = new Table("LocationPageInfo", LocationPageInfo.class);
        tables[LOCATIONTYPE] = new Table("LocationType", LocationType.class);
        tables[LOGINIMAGE] = new Table("LoginImage", LoginImage.class);
        tables[LOGINIMAGESET] = new Table("LoginImageSet", LoginImageSet.class);
        //tables[LOT] = new Table("Lot", Lot.class);
        tables[MERCHANT] = new Table("Merchant", Merchant.class);
        tables[MERCHANTCATEGORY] = new Table("MerchantCategory", MerchantCategory.class);
        tables[ORDERITEMTRACKING] = new Table("OrderItemTracking", OrderItemTracking.class);
        tables[ORDERTRACKING] = new Table("OrderTracking", OrderTracking.class);
        tables[ORDERTRACKINGSTATUS] = new Table("OrderTrackingStatus", OrderTrackingStatus.class);
        tables[PAGE] = new Table("Page", Page.class);
        tables[PAGEGROUP] = new Table("PageGroup", PageGroup.class);
        tables[PAGEGROUPPAGEINFO] = new Table("PageGroupPageInfo", PageGroupPageInfo.class);
        tables[PAGEINFO] = new Table("PageInfo", PageInfo.class);
        tables[PAGETHEME] = new Table("PageTheme", PageTheme.class);
        tables[PAGETHEMEPAGEINFO] = new Table("PageThemePageInfo", PageThemePageInfo.class);
        tables[PHONE] = new Table("Phone", Phone.class);
        tables[PHONETYPE] = new Table("PhoneType", PhoneType.class);
        tables[POINTSAPPROVAL] = new Table("PointsApproval", PointsApproval.class);
        tables[POINTSAWARDLEVEL] = new Table("PointsAwardLevel", PointsAwardLevel.class);
        tables[POINTSCONFIGURATION] = new Table("PointsConfiguration", PointsConfiguration.class);
        tables[POINTSCOREVALUE] = new Table("PointsCoreValue", PointsCoreValue.class);
        tables[POINTSRECORD] = new Table("PointsRecord", PointsRecord.class);
        tables[POINTSREQUEST] = new Table("PointsRequest", PointsRequest.class);
        tables[PRODUCT] = new Table("Product", Product.class);
        tables[PRODUCTAUDIT] = new Table("ProductAudit", ProductAudit.class);
        tables[PROGRAM] = new Table("Program", Program.class);
        tables[PROGRAMDOCUMENT] = new Table("ProgramDocument", ProgramDocument.class);
        tables[PROGRAMEMAILTYPE] = new Table("ProgramEmailType", ProgramEmailType.class);
        tables[PROGRAMEVENT] = new Table("ProgramEvent", ProgramEvent.class);
        tables[PROGRAMFAQ] = new Table("ProgramFaq", ProgramFaq.class);
        tables[PROGRAMPAGEGROUP] = new Table("ProgramPageGroup", ProgramPageGroup.class);
        tables[PROGRAMPAGEINFO] = new Table("ProgramPageInfo", ProgramPageInfo.class);
        tables[QUESTION] = new Table("Question", Question.class);
        tables[QUESTIONRESULT] = new Table("QuestionResult", QuestionResult.class);
        tables[QUIZ] = new Table("Quiz", Quiz.class);
        tables[QUIZRESULT] = new Table("QuizResult", QuizResult.class);
        tables[REPORT] = new Table("Report", Report.class);
        tables[REPORTCLASS] = new Table("ReportClass", ReportClass.class);
        tables[SECTION] = new Table("Section", Section.class);
        tables[SHIPTO] = new Table("ShipTo", ShipTo.class);
        tables[SURVEY] = new Table("Survey", Survey.class);
        tables[SURVEYANSWER] = new Table("SurveyAnswer", SurveyAnswer.class);
        tables[SURVEYQUESTION] = new Table("SurveyQuestion", SurveyQuestion.class);
        tables[USER] = new Table("UserTable", User.class);
        tables[VALUE] = new Table("Value", Value.class);
        tables[WIDGET] = new Table("Widget", Widget.class);
        
        // LINK TABLES
        tables[AWARDTYPEINCLUDEITEM] = new Table("AwardTypeIncludeItem",true);
        tables[EMPLOYEEPHONE] = new Table("EmployeePhone",true);
        tables[PROGRAMECARD] = new Table("ProgramEcard",true);
        tables[PROGRAMPROGRAMDOCUMENT] = new Table("ProgramProgramDocument",true);
        tables[MERCHANTCARD] = new Table("MerchantCard",true);
        tables[PROGRAMCARD] = new Table("ProgramCard",true);
        tables[ITEMCATEGORYITEM] = new Table("ItemCategoryItem",true);
        tables[SECTIONITEM] = new Table("SectionItem",true);
        tables[AWARDTYPEEXCLUDEITEM] = new Table("AwardTypeExcludeItem",true);
        tables[PROGRAMIMAGESTORE] = new Table("ProgramImageStore",true);
        tables[IMAGESTOREPROGRAM] = new Table("ImageStoreProgram",true);
        tables[MERCHANTCATEGORYMERCHANT] = new Table("MerchantCategoryMerchant",true);
        tables[PROGRAMWIDGET] = new Table("ProgramWidget",true);
        tables[REPLACEITEMITEM] = new Table("ReplaceItemItem",true);
        tables[ITEMTYPEITEM] = new Table("ItemTypeItem",true);
        tables[LOCATIONECARD] = new Table("LocationEcard",true);
        tables[LOCATIONPROGRAMFAQ] = new Table("LocationProgramFaq",true);
        tables[AWARDTYPEITEMTYPE] = new Table("AwardTypeItemType",true);
        tables[LOCATIONCARD] = new Table("LocationCard",true);
        tables[PROGRAMCHARITY] = new Table("ProgramCharity",true);
        tables[LOCATIONCHARITY] = new Table("LocationCharity",true);
        tables[LOCATIONINSPIRECOREVALUE] = new Table("LocationInspireCoreValue",true);
        tables[CARDVALUE] = new Table("CardValue",true);
        tables[EMAILEMPLOYEEAWARD] = new Table("EmailEmployeeAward",true);
        tables[ADDONITEMAWARDTYPE] = new Table("AddOnItemAwardType",true);
        tables[PROGRAMADDONITEM] = new Table("ProgramAddOnItem",true);
        tables[INSPIREAPPROVALEMAIL] = new Table("InspireApprovalEmail",true);
        tables[AWARDTYPEPRODUCT] = new Table("AwardTypeProduct",true);
        tables[INSPIREORDEREMAIL] = new Table("InspireOrderEmail",true);
        tables[POINTSREQUESTEMAIL] = new Table("PointsRequestEmail",true);
        
        // TABLE COLUMNS
        // NextNumber COLUMNS
        columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER);
        tables[NextNumber].setColumns(columns);
        
        // AboutMe COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("RecognitionPreference", "recognitionPreference", Types.INTEGER);
        columns[2] = new Column("FavoriteCandy", "favoriteCandy", Types.VARCHAR, 13);
        columns[3] = new Column("FavoriteRestaurant", "favoriteRestaurant", Types.VARCHAR, 25);
        columns[4] = new Column("FavoriteStore", "favoriteStore", Types.VARCHAR, 25);
        columns[5] = new Column("FavoriteTeam", "favoriteTeam", Types.VARCHAR, 25);
        columns[6] = new Column("FavoriteColor", "favoriteColor", Types.VARCHAR, 20);
        columns[7] = new Column("EmployeeId", true);
        tables[ABOUTME].setColumns(columns);
        tables[ABOUTME].addIndex(new Index("AboutMeEmployee", "EmployeeId"));
        
        // AddOnItem COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Value", "value", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("ItemId", true);
        columns[4] = new Column("LocationId", true);
        tables[ADDONITEM].setColumns(columns);
        tables[ADDONITEM].addIndex(new Index("AddOnItemAwardType", "AwardTypeId"));
        tables[ADDONITEM].addIndex(new Index("AddOnItemLocation", "LocationId"));
        tables[ADDONITEM].addIndex(new Index("AddOnItemProgram", "ProgramId"));
        
        // Address COLUMNS
        columns = new Column[12];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Address1", "address1", Types.VARCHAR, 75);
        columns[3] = new Column("Address2", "address2", Types.VARCHAR, 75);
        columns[4] = new Column("Address3", "address3", Types.VARCHAR, 75);
        columns[5] = new Column("Address4", "address4", Types.VARCHAR, 75);
        columns[6] = new Column("City", "city", Types.VARCHAR, 55);
        columns[7] = new Column("State", "state", Types.VARCHAR, 45);
        columns[8] = new Column("Zip", "zip", Types.VARCHAR, 12);
        columns[9] = new Column("Country", "country", Types.VARCHAR, 45);
        columns[10] = new Column("AddressTypeId", true);
        columns[11] = new Column("EmployeeId", true);
        tables[ADDRESS].setColumns(columns);
        tables[ADDRESS].addIndex(new Index("AddressEmployee", "EmployeeId"));
        
        // AddressType COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 45);
        columns[2] = new Column("Type", "type", Types.INTEGER);
        tables[ADDRESSTYPE].setColumns(columns);
        
        // Answer COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("AnswerText", "answerText", Types.CLOB, 120);
        columns[2] = new Column("Value", "value", Types.INTEGER);
        columns[3] = new Column("QuestionId", true);
        tables[ANSWER].setColumns(columns);
        tables[ANSWER].addIndex(new Index("AnswerQuestion", "QuestionId"));
        
        // AnswerResult COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("AnswerText", "answerText", Types.CLOB, 120);
        columns[2] = new Column("AnswerValue", "answerValue", Types.INTEGER);
        columns[3] = new Column("CurrentAnswerId", true);
        columns[4] = new Column("QuestionResultId", true);
        tables[ANSWERRESULT].setColumns(columns);
        tables[ANSWERRESULT].addIndex(new Index("AnswerResultCurrentAnswer", "CurrentAnswerId"));
        tables[ANSWERRESULT].addIndex(new Index("AnswerResultQuestionResult", "QuestionResultId"));
        
        // AwardCardOrder COLUMNS
        columns = new Column[16];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Value", "value", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("SentDate", "sentDate", Types.DATE);
        columns[4] = new Column("ShippingInfo", "shippingInfo", Types.VARCHAR, 150);
        columns[5] = new Column("CardType", "cardType", Types.INTEGER);
        columns[6] = new Column("LastStatusDate", "lastStatusDate", Types.DATE);
        columns[7] = new Column("LastStatus", "lastStatus", Types.VARCHAR, 75);
        columns[8] = new Column("CompletedDate", "completedDate", Types.DATE);
        columns[9] = new Column("PointsUsed", "pointsUsed", Types.DOUBLE);
        columns[9].decimalPlaces = 2;
        columns[10] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 5);
        columns[11] = new Column("InvoiceDate", "invoiceDate", Types.DATE);
        columns[12] = new Column("VendorInvoiced", "vendorInvoiced", Types.BOOLEAN);
        columns[13] = new Column("CardId", true);
        columns[14] = new Column("EmployeeAwardId", true);
        columns[15] = new Column("InspireOrderId", true);
        tables[AWARDCARDORDER].setColumns(columns);
        tables[AWARDCARDORDER].addIndex(new Index("AwardCardOrderEmployeeAward", "EmployeeAwardId"));
        tables[AWARDCARDORDER].addIndex(new Index("AwardCardOrderInspireOrder", "InspireOrderId"));
        
        // AwardType COLUMNS
        columns = new Column[33];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Descripton", "descripton", Types.VARCHAR, 254);
        columns[3] = new Column("UsesGiftCards", "usesGiftCards", Types.BOOLEAN);
        columns[4] = new Column("Value", "value", Types.DOUBLE);
        columns[4].decimalPlaces = 2;
        columns[5] = new Column("UsesItems", "usesItems", Types.BOOLEAN);
        columns[6] = new Column("UseHifiveApprovedItems", "useHifiveApprovedItems", Types.BOOLEAN);
        columns[7] = new Column("MinimumItemPrice", "minimumItemPrice", Types.DOUBLE);
        columns[7].decimalPlaces = 2;
        columns[8] = new Column("MaximumItemPrice", "maximumItemPrice", Types.DOUBLE);
        columns[8].decimalPlaces = 2;
        columns[9] = new Column("ServiceAward", "serviceAward", Types.BOOLEAN);
        columns[10] = new Column("YearsService", "yearsService", Types.INTEGER);
        columns[11] = new Column("BillCardPrice", "billCardPrice", Types.DOUBLE);
        columns[11].decimalPlaces = 2;
        columns[12] = new Column("BillOnItemPrice", "billOnItemPrice", Types.BOOLEAN);
        columns[13] = new Column("BillItemPrice", "billItemPrice", Types.DOUBLE);
        columns[13].decimalPlaces = 2;
        columns[14] = new Column("UsesHelpingHands", "usesHelpingHands", Types.BOOLEAN);
        columns[15] = new Column("UsesCash", "usesCash", Types.BOOLEAN);
        columns[16] = new Column("UsesInternationalVisa", "usesInternationalVisa", Types.BOOLEAN);
        columns[17] = new Column("UsesCharity", "usesCharity", Types.BOOLEAN);
        columns[18] = new Column("UsesImagineCard", "usesImagineCard", Types.BOOLEAN);
        columns[19] = new Column("PackageName", "packageName", Types.VARCHAR, 35);
        columns[20] = new Column("PackageInstruction", "packageInstruction", Types.CLOB, 18);
        columns[21] = new Column("PackageBillPrice", "packageBillPrice", Types.DOUBLE);
        columns[21].decimalPlaces = 2;
        columns[22] = new Column("AwardBillCost", "awardBillCost", Types.DOUBLE);
        columns[22].decimalPlaces = 2;
        columns[23] = new Column("CashBillPrice", "cashBillPrice", Types.DOUBLE);
        columns[23].decimalPlaces = 2;
        columns[24] = new Column("ImagineBillPrice", "imagineBillPrice", Types.DOUBLE);
        columns[24].decimalPlaces = 2;
        columns[25] = new Column("InternationalVisaBillPrice", "internationalVisaBillPrice", Types.DOUBLE);
        columns[25].decimalPlaces = 2;
        columns[26] = new Column("AnnouncementDocumentId", true);
        columns[27] = new Column("CeoImageStoreId", true);
        columns[28] = new Column("CeoSignatureImageStoreId", true);
        columns[29] = new Column("ImagineCardId", true);
        columns[30] = new Column("LocationId", true);
        columns[31] = new Column("ProgramId", true);
        columns[32] = new Column("SectionId", true);
        tables[AWARDTYPE].setColumns(columns);
        tables[AWARDTYPE].addIndex(new Index("AwardTypeLocation", "LocationId"));
        tables[AWARDTYPE].addIndex(new Index("AwardTypeProgram", "ProgramId"));
        
        // Card COLUMNS
        columns = new Column[15];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("ActiveDate", "activeDate", Types.DATE);
        columns[4] = new Column("DigitalCard", "digitalCard", Types.BOOLEAN);
        columns[5] = new Column("TraditionalCard", "traditionalCard", Types.BOOLEAN);
        columns[6] = new Column("CelebrateCard", "celebrateCard", Types.BOOLEAN);
        columns[7] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[8] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[9] = new Column("RangeLow", "rangeLow", Types.INTEGER);
        columns[10] = new Column("RangeHigh", "rangeHigh", Types.INTEGER);
        columns[11] = new Column("RangeIncrement", "rangeIncrement", Types.INTEGER);
        columns[12] = new Column("MerchantCode", "merchantCode", Types.VARCHAR, 25);
        columns[13] = new Column("CardVendorId", true);
        columns[14] = new Column("ImageStoreId", true);
        tables[CARD].setColumns(columns);
        tables[CARD].addIndex(new Index("CardName", "Name"));
        tables[CARD].addIndex(new Index("CardMerchantCode", "MerchantCode"));
        tables[CARD].addIndex(new Index("CardCardVendor", "CardVendorId"));
        
        // CardVendor COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("DigitalCard", "digitalCard", Types.BOOLEAN);
        columns[4] = new Column("TraditionalCard", "traditionalCard", Types.BOOLEAN);
        columns[5] = new Column("CelebrateCard", "celebrateCard", Types.BOOLEAN);
        tables[CARDVENDOR].setColumns(columns);
        
        // CashstarOrder COLUMNS
        columns = new Column[20];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("order_number", "orderNumber", Types.VARCHAR, 25);
        columns[2] = new Column("CardStatus", "cardStatus", Types.VARCHAR, 25);
        columns[3] = new Column("EgcCode", "egcCode", Types.VARCHAR, 25);
        columns[4] = new Column("EgcNumber", "egcNumber", Types.VARCHAR, 50);
        columns[5] = new Column("CardUrl", "cardUrl", Types.VARCHAR, 200);
        columns[6] = new Column("BalanaceLastUpdated", "balanaceLastUpdated", Types.TIMESTAMP);
        columns[7] = new Column("ChallengeType", "challengeType", Types.VARCHAR, 25);
        columns[8] = new Column("Currency", "currency", Types.VARCHAR, 20);
        columns[9] = new Column("Active", "active", Types.BOOLEAN);
        columns[10] = new Column("FaceplateCode", "faceplateCode", Types.VARCHAR, 15);
        columns[11] = new Column("Viewed", "viewed", Types.BOOLEAN);
        columns[12] = new Column("ChallengeDescription", "challengeDescription", Types.VARCHAR, 80);
        columns[13] = new Column("Challenge", "challenge", Types.VARCHAR, 50);
        columns[14] = new Column("FirstViewed", "firstViewed", Types.TIMESTAMP);
        columns[15] = new Column("AuditNumber", "auditNumber", Types.VARCHAR, 50);
        columns[16] = new Column("TransactionId", "transactionId", Types.VARCHAR, 25);
        columns[17] = new Column("InitialBalance", "initialBalance", Types.DOUBLE);
        columns[17].decimalPlaces = 2;
        columns[18] = new Column("CurrentBalance", "currentBalance", Types.DOUBLE);
        columns[18].decimalPlaces = 2;
        columns[19] = new Column("AwardCardOrderId", true);
        tables[CASHSTARORDER].setColumns(columns);
        tables[CASHSTARORDER].addIndex(new Index("CashstarOrderAwardCardOrder", "AwardCardOrderId"));
        
        // Catalog COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        tables[CATALOG].setColumns(columns);
        
        // Charity COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 120);
        columns[3] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[4] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[5] = new Column("Seq", "seq", Types.INTEGER);
        columns[6] = new Column("ImageStoreId", true);
        tables[CHARITY].setColumns(columns);
        
        // Code COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Code", "code", Types.VARCHAR, 24);
        columns[2] = new Column("RedeemedDate", "redeemedDate", Types.DATE);
        columns[3] = new Column("ExpirationDate", "expirationDate", Types.DATE);
        columns[4] = new Column("PointValue", "pointValue", Types.INTEGER);
        columns[5] = new Column("LotId", true);
        columns[6] = new Column("PointsRecordId", true);
        columns[7] = new Column("RedeemingEmployeeId", true);
        tables[CODE].setColumns(columns);
        tables[CODE].addIndex(new Index("CodeCode", "Code"));
        tables[CODE].addIndex(new Index("CodeLot", "LotId"));
        tables[CODE].addIndex(new Index("CodePointsRecord", "PointsRecordId"));
        tables[CODE].addIndex(new Index("CodeRedeemingEmployee", "RedeemingEmployeeId"));
        
        // Company COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        tables[COMPANY].setColumns(columns);
        
        // CountryCode COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("Code", "code", Types.VARCHAR, 12);
        columns[3] = new Column("StateIsRequired", "stateIsRequired", Types.BOOLEAN);
        columns[4] = new Column("StateName", "stateName", Types.VARCHAR, 35);
        columns[5] = new Column("ZipCodeIsRequired", "zipCodeIsRequired", Types.BOOLEAN);
        columns[6] = new Column("ZipCodeName", "zipCodeName", Types.VARCHAR, 35);
        columns[7] = new Column("CurrencyTypeId", true);
        tables[COUNTRYCODE].setColumns(columns);
        tables[COUNTRYCODE].addIndex(new Index("CountryCodeCurrencyType", "CurrencyTypeId"));
        
        // CurrencyType COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 25);
        columns[2] = new Column("Abbreviation", "abbreviation", Types.VARCHAR, 12);
        columns[3] = new Column("Symbol", "symbol", Types.VARCHAR, 6);
        columns[4] = new Column("ExchangeRate", "exchangeRate", Types.DOUBLE);
        columns[4].decimalPlaces = 4;
        columns[5] = new Column("Created", "created", Types.DATE);
        columns[5].readOnly = true;
        tables[CURRENCYTYPE].setColumns(columns);
        
        // CustomData COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Code", "code", Types.VARCHAR, 6);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 25);
        columns[3] = new Column("Description", "description", Types.VARCHAR, 125);
        columns[4] = new Column("ProgramId", true);
        tables[CUSTOMDATA].setColumns(columns);
        tables[CUSTOMDATA].addIndex(new Index("CustomDataProgram", "ProgramId"));
        
        // Ecard COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[4] = new Column("EcardCategoryId", true);
        columns[5] = new Column("ImageStoreId", true);
        tables[ECARD].setColumns(columns);
        tables[ECARD].addIndex(new Index("EcardEcardCategory", "EcardCategoryId"));
        tables[ECARD].addIndex(new Index("EcardLocation", "LocationId"));
        
        // EcardCategory COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 150);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        tables[ECARDCATEGORY].setColumns(columns);
        
        // Email COLUMNS
        columns = new Column[13];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("FromEmail", "fromEmail", Types.VARCHAR, 75);
        columns[3] = new Column("ToEmail", "toEmail", Types.VARCHAR, 75);
        columns[4] = new Column("CcEmail", "ccEmail", Types.VARCHAR, 75);
        columns[5] = new Column("Subject", "subject", Types.VARCHAR, 150);
        columns[6] = new Column("SentDateTime", "sentDateTime", Types.TIMESTAMP);
        columns[7] = new Column("CancelDate", "cancelDate", Types.DATE);
        columns[8] = new Column("Body", "body", Types.CLOB, 0);
        columns[9] = new Column("Attachment", "attachment", Types.BLOB);
        columns[10] = new Column("AttachmentName", "attachmentName", Types.VARCHAR, 35);
        columns[11] = new Column("AttachmentMimeType", "attachmentMimeType", Types.VARCHAR, 35);
        columns[12] = new Column("ProgramEmailTypeId", true);
        tables[EMAIL].setColumns(columns);
        tables[EMAIL].addIndex(new Index("EmailSentDateTime", "SentDateTime"));
        tables[EMAIL].addIndex(new Index("EmailEmployeeAwardShipped", "EmployeeAwardShippedId"));
        tables[EMAIL].addIndex(new Index("EmailInspireApprovalReminder", "InspireApprovalReminderId"));
        tables[EMAIL].addIndex(new Index("EmailInspireOrder", "InspireOrderId"));
        tables[EMAIL].addIndex(new Index("EmailPointsRequest", "PointsRequestId"));
        
        // EmailType COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[4] = new Column("Type", "type", Types.INTEGER);
        columns[5] = new Column("Subject", "subject", Types.VARCHAR, 150);
        columns[6] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[7] = new Column("Note", "note", Types.CLOB, 4);
        tables[EMAILTYPE].setColumns(columns);
        
        // Employee COLUMNS
        columns = new Column[45];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("EmployeeCode", "employeeCode", Types.VARCHAR, 25);
        columns[3] = new Column("Title", "title", Types.VARCHAR, 75);
        columns[4] = new Column("PrefixName", "prefixName", Types.VARCHAR, 75);
        columns[5] = new Column("FirstName", "firstName", Types.VARCHAR, 75);
        columns[6] = new Column("MiddleName", "middleName", Types.VARCHAR, 75);
        columns[7] = new Column("LastName", "lastName", Types.VARCHAR, 75);
        columns[8] = new Column("SuffixName", "suffixName", Types.VARCHAR, 75);
        columns[9] = new Column("BirthDate", "birthDate", Types.DATE);
        columns[10] = new Column("HireDate", "hireDate", Types.DATE);
        columns[11] = new Column("LoginId", "loginId", Types.VARCHAR, 75);
        columns[12] = new Column("Passwordx", "password", Types.VARCHAR, 50);
        columns[13] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[14] = new Column("InactiveReason", "inactiveReason", Types.VARCHAR, 150);
        columns[15] = new Column("Admin", "admin", Types.BOOLEAN);
        columns[16] = new Column("Email", "email", Types.VARCHAR, 200);
        columns[17] = new Column("Email2", "email2", Types.VARCHAR, 125);
        columns[18] = new Column("WorkLocation", "workLocation", Types.VARCHAR, 50);
        columns[19] = new Column("CostCenter", "costCenter", Types.VARCHAR, 45);
        columns[20] = new Column("CostCenterDescription", "costCenterDescription", Types.VARCHAR, 100);
        columns[21] = new Column("PasswordAssignedDate", "passwordAssignedDate", Types.DATE);
        columns[22] = new Column("TopLevelManager", "topLevelManager", Types.BOOLEAN);
        columns[23] = new Column("SuperApprover", "superApprover", Types.BOOLEAN);
        columns[24] = new Column("Division", "division", Types.VARCHAR, 100);
        columns[25] = new Column("CompanyCode", "companyCode", Types.VARCHAR, 10);
        columns[26] = new Column("CompanyCodeName", "companyCodeName", Types.VARCHAR, 45);
        columns[27] = new Column("MaxNomLvl", "maxNomLvl", Types.INTEGER);
        columns[28] = new Column("UsesEcards", "usesEcards", Types.BOOLEAN);
        columns[29] = new Column("UsesService", "usesService", Types.BOOLEAN);
        columns[30] = new Column("UsesPoints", "usesPoints", Types.BOOLEAN);
        columns[31] = new Column("IsNominator", "isNominator", Types.BOOLEAN);
        columns[32] = new Column("UsesInspire", "usesInspire", Types.BOOLEAN);
        columns[33] = new Column("CanGetNominated", "canGetNominated", Types.BOOLEAN);
        columns[34] = new Column("UsesAwardGallery", "usesAwardGallery", Types.BOOLEAN);
        columns[35] = new Column("RegionCode", "regionCode", Types.VARCHAR, 10);
        columns[36] = new Column("JobFunctionCode", "jobFunctionCode", Types.VARCHAR, 15);
        columns[37] = new Column("JobFunctionName", "jobFunctionName", Types.VARCHAR, 50);
        columns[38] = new Column("CanRedeemCodes", "canRedeemCodes", Types.BOOLEAN);
        columns[39] = new Column("CanActivateLots", "canActivateLots", Types.BOOLEAN);
        columns[40] = new Column("EmployeeTypeId", true);
        columns[41] = new Column("HRBPartnerId", true);
        columns[42] = new Column("LocationId", true);
        columns[43] = new Column("ParentEmployeeId", true);
        columns[44] = new Column("PointsNextApprovalId", true);
        tables[EMPLOYEE].setColumns(columns);
        tables[EMPLOYEE].addIndex(new Index("EmployeeLastName", "LastName"));
        tables[EMPLOYEE].addIndex(new Index("EmployeeEmployeeCode", "EmployeeCode"));
        tables[EMPLOYEE].addIndex(new Index("EmployeeLoginId", "LoginId"));
        tables[EMPLOYEE].addIndex(new Index("EmployeeHRBPartner", "HRBPartnerId"));
        tables[EMPLOYEE].addIndex(new Index("EmployeeLocation", "LocationId"));
        tables[EMPLOYEE].addIndex(new Index("EmployeeParentEmployee", "ParentEmployeeId"));
        tables[EMPLOYEE].addIndex(new Index("EmployeePointsNextApproval", "PointsNextApprovalId"));
        
        // EmployeeAward COLUMNS
        columns = new Column[45];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("AwardDate", "awardDate", Types.DATE);
        columns[3] = new Column("ApprovedDate", "approvedDate", Types.DATE);
        columns[4] = new Column("PackageSentDate", "packageSentDate", Types.DATE);
        columns[5] = new Column("PackageTracking", "packageTracking", Types.VARCHAR, 25);
        columns[6] = new Column("PackageShippingInfo", "packageShippingInfo", Types.VARCHAR, 150);
        columns[7] = new Column("PackageInvoiceNumber", "packageInvoiceNumber", Types.VARCHAR, 25);
        columns[8] = new Column("PackageBillDate", "packageBillDate", Types.DATE);
        columns[9] = new Column("BillDate", "billDate", Types.DATE);
        columns[10] = new Column("PackagePaidDate", "packagePaidDate", Types.DATE);
        columns[11] = new Column("PaidDate", "paidDate", Types.DATE);
        columns[12] = new Column("ItemSelectedDate", "itemSelectedDate", Types.DATE);
        columns[13] = new Column("ItemSentDate", "itemSentDate", Types.DATE);
        columns[14] = new Column("ItemShippingInfo", "itemShippingInfo", Types.VARCHAR, 150);
        columns[15] = new Column("ItemTracking", "itemTracking", Types.VARCHAR, 25);
        columns[16] = new Column("ItemBillDate", "itemBillDate", Types.DATE);
        columns[17] = new Column("ItemLastStatusDate", "itemLastStatusDate", Types.DATE);
        columns[18] = new Column("ItemLastStatus", "itemLastStatus", Types.VARCHAR, 75);
        columns[19] = new Column("ItemInvoiceNumber", "itemInvoiceNumber", Types.VARCHAR, 25);
        columns[20] = new Column("ItemVendorInvoiced", "itemVendorInvoiced", Types.BOOLEAN);
        columns[21] = new Column("ItemPaidDate", "itemPaidDate", Types.DATE);
        columns[22] = new Column("CompletedDate", "completedDate", Types.DATE);
        columns[23] = new Column("CancelDate", "cancelDate", Types.DATE);
        columns[24] = new Column("CancelReason", "cancelReason", Types.VARCHAR, 254);
        columns[25] = new Column("CashSelectedDate", "cashSelectedDate", Types.DATE);
        columns[26] = new Column("CashSentDate", "cashSentDate", Types.DATE);
        columns[27] = new Column("InternationalVisaSelectedDate", "internationalVisaSelectedDate", Types.DATE);
        columns[28] = new Column("InternationalVisaAmount", "internationalVisaAmount", Types.DOUBLE);
        columns[28].decimalPlaces = 2;
        columns[29] = new Column("InternationalVisaSentDate", "internationalVisaSentDate", Types.DATE);
        columns[30] = new Column("AddOnProductSelectedDate", "addOnProductSelectedDate", Types.DATE);
        columns[31] = new Column("MergeId", "mergeId", Types.INTEGER);
        columns[31].readOnly = true;
        columns[32] = new Column("CashInvoiceNumber", "cashInvoiceNumber", Types.VARCHAR, 25);
        columns[33] = new Column("CashinvoiceDate", "cashinvoiceDate", Types.DATE);
        columns[34] = new Column("InternationalVisaInvoiceNumber", "internationalVisaInvoiceNumber", Types.VARCHAR, 24);
        columns[35] = new Column("InternationVisaInvoiceDate", "internationVisaInvoiceDate", Types.DATE);
        columns[36] = new Column("InternationalVisaVendorInvoiced", "internationalVisaVendorInvoiced", Types.BOOLEAN);
        columns[37] = new Column("AddOnProductId", true);
        columns[38] = new Column("AwardTypeId", true);
        columns[39] = new Column("ConfirmEmailId", true);
        columns[40] = new Column("EmployeeId", true);
        columns[41] = new Column("ManagerNotifyEmailId", true);
        columns[42] = new Column("NotifyEmailId", true);
        columns[43] = new Column("ProductId", true);
        columns[44] = new Column("ShipToId", true);
        tables[EMPLOYEEAWARD].setColumns(columns);
        tables[EMPLOYEEAWARD].addIndex(new Index("EmployeeAwardEmployee", "EmployeeId"));
        
        // EmployeeAwardCharity COLUMNS
        columns = new Column[9];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Value", "value", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("SentDate", "sentDate", Types.DATE);
        columns[4] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 5);
        columns[5] = new Column("InvoiceDate", "invoiceDate", Types.DATE);
        columns[6] = new Column("VendorInvoiced", "vendorInvoiced", Types.BOOLEAN);
        columns[7] = new Column("CharityId", true);
        columns[8] = new Column("EmployeeAwardId", true);
        tables[EMPLOYEEAWARDCHARITY].setColumns(columns);
        tables[EMPLOYEEAWARDCHARITY].addIndex(new Index("EmployeeAwardCharityEmployeeAward", "EmployeeAwardId"));
        
        // EmployeeCustomData COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Value", "value", Types.VARCHAR, 125);
        columns[2] = new Column("CustomDataId", true);
        columns[3] = new Column("EmployeeId", true);
        tables[EMPLOYEECUSTOMDATA].setColumns(columns);
        tables[EMPLOYEECUSTOMDATA].addIndex(new Index("EmployeeCustomDataEmployee", "EmployeeId"));
        
        // EmployeeEcard COLUMNS
        columns = new Column[10];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Subject", "subject", Types.VARCHAR, 125);
        columns[3] = new Column("Message", "message", Types.CLOB, 11);
        columns[4] = new Column("SendDate", "sendDate", Types.DATE);
        columns[5] = new Column("PostToWall", "postToWall", Types.BOOLEAN);
        columns[6] = new Column("ConfirmedEmailId", true);
        columns[7] = new Column("DeliveredEmailId", true);
        columns[8] = new Column("EcardId", true);
        columns[9] = new Column("EmployeeId", true);
        tables[EMPLOYEEECARD].setColumns(columns);
        tables[EMPLOYEEECARD].addIndex(new Index("EmployeeEcardEmployee", "EmployeeId"));
        
        // EmployeeEcardTo COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("EmailAddress", "emailAddress", Types.VARCHAR, 125);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("IncludeManager", "includeManager", Types.BOOLEAN);
        columns[4] = new Column("EmailId", true);
        columns[5] = new Column("EmployeeEcardId", true);
        columns[6] = new Column("ToEmployeeId", true);
        tables[EMPLOYEEECARDTO].setColumns(columns);
        tables[EMPLOYEEECARDTO].addIndex(new Index("EmployeeEcardToEmployeeEcard", "EmployeeEcardId"));
        tables[EMPLOYEEECARDTO].addIndex(new Index("EmployeeEcardToToEmployee", "ToEmployeeId"));
        
        // EmployeeSurvey COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("TextValue", "text", Types.CLOB, 254);
        columns[3] = new Column("Points", "points", Types.DOUBLE);
        columns[3].decimalPlaces = 2;
        columns[4] = new Column("ApprovedDate", "approvedDate", Types.DATE);
        columns[5] = new Column("EmployeeId", true);
        columns[6] = new Column("SurveyId", true);
        tables[EMPLOYEESURVEY].setColumns(columns);
        tables[EMPLOYEESURVEY].addIndex(new Index("EmployeeSurveyEmployee", "EmployeeId"));
        
        // EmployeeSurveyQuestion COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("TextValue", "text", Types.CLOB, 254);
        columns[2] = new Column("EmployeeSurveyId", true);
        columns[3] = new Column("SurveyAnswerId", true);
        columns[4] = new Column("SurveyQuestionId", true);
        tables[EMPLOYEESURVEYQUESTION].setColumns(columns);
        tables[EMPLOYEESURVEYQUESTION].addIndex(new Index("EmployeeSurveyQuestionEmployeeSurvey", "EmployeeSurveyId"));
        
        // EmployeeType COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("Type", "type", Types.INTEGER);
        tables[EMPLOYEETYPE].setColumns(columns);
        
        // FamilyMember COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Relationship", "relationship", Types.INTEGER);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 25);
        columns[3] = new Column("Age", "age", Types.INTEGER);
        columns[4] = new Column("Birthday", "birthday", Types.DATE);
        columns[5] = new Column("Species", "species", Types.VARCHAR, 15);
        columns[6] = new Column("EmployeeId", true);
        tables[FAMILYMEMBER].setColumns(columns);
        tables[FAMILYMEMBER].addIndex(new Index("FamilyMemberEmployee", "EmployeeId"));
        
        // HindaOrder COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("OrderNumber", "orderNumber", Types.VARCHAR, 25);
        columns[2] = new Column("ClientOrderNumber", "clientOrderNumber", Types.VARCHAR, 50);
        columns[3] = new Column("OrderDate", "orderDate", Types.DATE);
        columns[4] = new Column("EmployeeAwardId", true);
        columns[5] = new Column("InspireOrderId", true);
        tables[HINDAORDER].setColumns(columns);
        tables[HINDAORDER].addIndex(new Index("HindaOrderOrderNumber", "OrderNumber"));
        tables[HINDAORDER].addIndex(new Index("HindaOrderEmployeeAward", "EmployeeAwardId"));
        tables[HINDAORDER].addIndex(new Index("HindaOrderInspireOrder", "InspireOrderId"));
        
        // HindaOrderLine COLUMNS
        columns = new Column[13];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("OrderLineNumber", "orderLineNumber", Types.INTEGER);
        columns[2] = new Column("QuantityBackOrdered", "quantityBackOrdered", Types.INTEGER);
        columns[3] = new Column("QuantityOrdered", "quantityOrdered", Types.INTEGER);
        columns[4] = new Column("QuantityCanceled", "quantityCanceled", Types.INTEGER);
        columns[5] = new Column("QuantityReserved", "quantityReserved", Types.INTEGER);
        columns[6] = new Column("QuantityShipped", "quantityShipped", Types.INTEGER);
        columns[7] = new Column("TrackingNumber", "trackingNumber", Types.VARCHAR, 50);
        columns[8] = new Column("CarrierCode", "carrierCode", Types.VARCHAR, 25);
        columns[9] = new Column("TrackingUrl", "trackingUrl", Types.VARCHAR, 150);
        columns[10] = new Column("ShippedDate", "shippedDate", Types.TIMESTAMP);
        columns[11] = new Column("ClientOrderLineNumber", "clientOrderLineNumber", Types.VARCHAR, 25);
        columns[12] = new Column("HindaOrderId", true);
        tables[HINDAORDERLINE].setColumns(columns);
        tables[HINDAORDERLINE].addIndex(new Index("HindaOrderLineHindaOrder", "HindaOrderId"));
        
        // ImageStore COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 7);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("OrigFileName", "origFileName", Types.VARCHAR, 75);
        columns[3] = new Column("Description", "description", Types.VARCHAR, 55);
        columns[4] = new Column("LastUpdate", "lastUpdate", Types.DATE);
        columns[5] = new Column("Bytes", "bytes", Types.BLOB);
        tables[IMAGESTORE].setColumns(columns);
        tables[IMAGESTORE].addIndex(new Index("ImageStoreLogoProgram", "LogoProgramId"));
        tables[IMAGESTORE].addIndex(new Index("ImageStoreProgram", "ProgramId"));
        
        // Inspire COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Reason", "reason", Types.VARCHAR, 250);
        columns[3] = new Column("Message", "message", Types.VARCHAR, 500);
        columns[4] = new Column("EmailId", true);
        columns[5] = new Column("EmployeeId", true);
        columns[6] = new Column("InspireAwardLevelId", true);
        columns[7] = new Column("InspireCoreValueId", true);
        tables[INSPIRE].setColumns(columns);
        tables[INSPIRE].addIndex(new Index("InspireEmployee", "EmployeeId"));
        
        // InspireApproval COLUMNS
        columns = new Column[9];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Status", "status", Types.INTEGER);
        columns[3] = new Column("StatusDate", "statusDate", Types.DATE);
        columns[4] = new Column("Comments", "comments", Types.VARCHAR, 250);
        columns[5] = new Column("EmailId", true);
        columns[6] = new Column("EmployeeId", true);
        columns[7] = new Column("InspireAwardLevelId", true);
        columns[8] = new Column("InspireRecipientId", true);
        tables[INSPIREAPPROVAL].setColumns(columns);
        tables[INSPIREAPPROVAL].addIndex(new Index("InspireApprovalEmployee", "EmployeeId"));
        tables[INSPIREAPPROVAL].addIndex(new Index("InspireApprovalInspireRecipient", "InspireRecipientId"));
        
        // InspireAwardLevel COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Rank", "rank", Types.INTEGER);
        columns[3] = new Column("ApprovalLevels", "approvalLevels", Types.INTEGER);
        columns[4] = new Column("ApproveFromTop", "approveFromTop", Types.BOOLEAN);
        columns[5] = new Column("ProgramId", true);
        tables[INSPIREAWARDLEVEL].setColumns(columns);
        tables[INSPIREAWARDLEVEL].addIndex(new Index("InspireAwardLevelProgram", "ProgramId"));
        
        // InspireAwardLevelLocationValue COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Points", "points", Types.DOUBLE);
        columns[1].decimalPlaces = 2;
        columns[2] = new Column("InspireAwardLevelId", true);
        columns[3] = new Column("LocationId", true);
        tables[INSPIREAWARDLEVELLOCATIONVALUE].setColumns(columns);
        tables[INSPIREAWARDLEVELLOCATIONVALUE].addIndex(new Index("InspireAwardLevelLocationValueLocation", "LocationId"));
        
        // InspireCoreValue COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("ProgramId", true);
        tables[INSPIRECOREVALUE].setColumns(columns);
        tables[INSPIRECOREVALUE].addIndex(new Index("InspireCoreValueLocation", "LocationId"));
        tables[INSPIRECOREVALUE].addIndex(new Index("InspireCoreValueProgram", "ProgramId"));
        
        // InspireOrder COLUMNS
        columns = new Column[21];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("BillDate", "billDate", Types.DATE);
        columns[3] = new Column("PaidDate", "paidDate", Types.DATE);
        columns[4] = new Column("CompletedDate", "completedDate", Types.DATE);
        columns[5] = new Column("CashSelectedDate", "cashSelectedDate", Types.DATE);
        columns[6] = new Column("CashAmount", "cashAmount", Types.DOUBLE);
        columns[6].decimalPlaces = 2;
        columns[7] = new Column("CashPointsUsed", "cashPointsUsed", Types.DOUBLE);
        columns[7].decimalPlaces = 2;
        columns[8] = new Column("CashSentDate", "cashSentDate", Types.DATE);
        columns[9] = new Column("InternationalVisaSelectedDate", "internationalVisaSelectedDate", Types.DATE);
        columns[10] = new Column("InternationalVisaAmount", "internationalVisaAmount", Types.DOUBLE);
        columns[10].decimalPlaces = 2;
        columns[11] = new Column("InternationalVisaPointsUsed", "internationalVisaPointsUsed", Types.DOUBLE);
        columns[11].decimalPlaces = 2;
        columns[12] = new Column("InternationalVisaSentDate", "internationalVisaSentDate", Types.DATE);
        columns[13] = new Column("PointsOrdered", "pointsOrdered", Types.INTEGER);
        columns[14] = new Column("CashInvoiceNumber", "cashInvoiceNumber", Types.VARCHAR, 5);
        columns[15] = new Column("CashInvoiceDate", "cashInvoiceDate", Types.DATE);
        columns[16] = new Column("InternationalVisaInvoiceNumber", "internationalVisaInvoiceNumber", Types.VARCHAR, 5);
        columns[17] = new Column("InternationalVisaInvoiceDate", "internationalVisaInvoiceDate", Types.DATE);
        columns[18] = new Column("InternationalVisaVendorInvoiced", "internationalVisaVendorInvoiced", Types.BOOLEAN);
        columns[19] = new Column("EmployeeId", true);
        columns[20] = new Column("ShipToId", true);
        tables[INSPIREORDER].setColumns(columns);
        tables[INSPIREORDER].addIndex(new Index("InspireOrderEmployee", "EmployeeId"));
        
        // InspireOrderCharity COLUMNS
        columns = new Column[10];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Value", "value", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("SentDate", "sentDate", Types.DATE);
        columns[4] = new Column("PointsUsed", "pointsUsed", Types.DOUBLE);
        columns[4].decimalPlaces = 2;
        columns[5] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 5);
        columns[6] = new Column("InvoiceDate", "invoiceDate", Types.DATE);
        columns[7] = new Column("VendorInvoiced", "vendorInvoiced", Types.BOOLEAN);
        columns[8] = new Column("CharityId", true);
        columns[9] = new Column("InspireOrderId", true);
        tables[INSPIREORDERCHARITY].setColumns(columns);
        tables[INSPIREORDERCHARITY].addIndex(new Index("InspireOrderCharityInspireOrder", "InspireOrderId"));
        
        // InspireOrderItem COLUMNS
        columns = new Column[17];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("Quantity", "quantity", Types.INTEGER);
        columns[4] = new Column("PointsUsed", "pointsUsed", Types.DOUBLE);
        columns[4].decimalPlaces = 2;
        columns[5] = new Column("BillDate", "billDate", Types.DATE);
        columns[6] = new Column("PaidDate", "paidDate", Types.DATE);
        columns[7] = new Column("ItemSentDate", "itemSentDate", Types.DATE);
        columns[8] = new Column("ItemShippingInfo", "itemShippingInfo", Types.VARCHAR, 150);
        columns[9] = new Column("ItemLastStatusDate", "itemLastStatusDate", Types.DATE);
        columns[10] = new Column("ItemLastStatus", "itemLastStatus", Types.VARCHAR, 75);
        columns[11] = new Column("CompletedDate", "completedDate", Types.DATE);
        columns[12] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 5);
        columns[13] = new Column("InvoiceDate", "invoiceDate", Types.DATE);
        columns[14] = new Column("VendorInvoiced", "vendorInvoiced", Types.BOOLEAN);
        columns[15] = new Column("InspireOrderId", true);
        columns[16] = new Column("ProductId", true);
        tables[INSPIREORDERITEM].setColumns(columns);
        tables[INSPIREORDERITEM].addIndex(new Index("InspireOrderItemInspireOrder", "InspireOrderId"));
        
        // InspireRecipient COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Points", "points", Types.DOUBLE);
        columns[1].decimalPlaces = 2;
        columns[2] = new Column("CompletedDate", "completedDate", Types.DATE);
        columns[3] = new Column("CompletedEmailId", true);
        columns[4] = new Column("EmailId", true);
        columns[5] = new Column("EmployeeId", true);
        columns[6] = new Column("InspireId", true);
        columns[7] = new Column("InspireAwardLevelLocationValueId", true);
        tables[INSPIRERECIPIENT].setColumns(columns);
        tables[INSPIRERECIPIENT].addIndex(new Index("InspireRecipientEmployee", "EmployeeId"));
        tables[INSPIRERECIPIENT].addIndex(new Index("InspireRecipientInspire", "InspireId"));
        
        // Item COLUMNS
        columns = new Column[20];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("VendorCode", "vendorCode", Types.VARCHAR, 50);
        columns[3] = new Column("VendorCode2", "vendorCode2", Types.VARCHAR, 50);
        columns[4] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[5] = new Column("BriefText", "briefText", Types.VARCHAR, 175);
        columns[6] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[7] = new Column("DiscontinuedDate", "discontinuedDate", Types.DATE);
        columns[8] = new Column("DiscontinuedReason", "discontinuedReason", Types.VARCHAR, 75);
        columns[9] = new Column("DropShip", "dropShip", Types.BOOLEAN);
        columns[10] = new Column("OtherInformation", "otherInformation", Types.VARCHAR, 254);
        columns[11] = new Column("Manufacturer", "manufacturer", Types.VARCHAR, 75);
        columns[12] = new Column("Model", "model", Types.VARCHAR, 55);
        columns[13] = new Column("LastUpdate", "lastUpdate", Types.DATE);
        columns[14] = new Column("HifiveRating", "hifiveRating", Types.INTEGER);
        columns[15] = new Column("HifiveRatingDate", "hifiveRatingDate", Types.DATE);
        columns[16] = new Column("HifiveRatingNote", "hifiveRatingNote", Types.VARCHAR, 245);
        columns[17] = new Column("AccountNumber", "accountNumber", Types.VARCHAR, 5);
        columns[18] = new Column("ImageStoreId", true);
        columns[19] = new Column("ItemVendorId", true);
        tables[ITEM].setColumns(columns);
        tables[ITEM].addIndex(new Index("ItemName", "Name"));
        tables[ITEM].addIndex(new Index("ItemVendorCode", "VendorCode"));
        tables[ITEM].addIndex(new Index("ItemVendorCode2", "VendorCode2"));
        
        // ItemCategory COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("Code", "code", Types.VARCHAR, 20);
        columns[3] = new Column("Seq", "seq", Types.INTEGER);
        columns[4] = new Column("HifiveRating", "hifiveRating", Types.INTEGER);
        columns[5] = new Column("HifiveRatingDate", "hifiveRatingDate", Types.DATE);
        columns[6] = new Column("HifiveRatingNote", "hifiveRatingNote", Types.VARCHAR, 245);
        columns[7] = new Column("ParentItemCategoryId", true);
        tables[ITEMCATEGORY].setColumns(columns);
        tables[ITEMCATEGORY].addIndex(new Index("ItemCategoryCode", "Code"));
        tables[ITEMCATEGORY].addIndex(new Index("ItemCategoryParentItemCategory", "ParentItemCategoryId"));
        
        // ItemType COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("Type", "type", Types.INTEGER);
        tables[ITEMTYPE].setColumns(columns);
        
        // ItemVendor COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("Notes", "notes", Types.CLOB, 5);
        tables[ITEMVENDOR].setColumns(columns);
        
        // LineItem COLUMNS
        columns = new Column[30];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Description", "description", Types.CLOB, 200);
        columns[2] = new Column("BilledAmount", "billedAmount", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("PaidAmount", "paidAmount", Types.DOUBLE);
        columns[3].decimalPlaces = 2;
        columns[4] = new Column("Billed", "billed", Types.BOOLEAN);
        columns[5] = new Column("Paid", "paid", Types.BOOLEAN);
        columns[6] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 5);
        columns[7] = new Column("Invoicedate", "invoicedate", Types.DATE);
        columns[8] = new Column("Billable", "billable", Types.BOOLEAN);
        columns[9] = new Column("Created", "created", Types.DATE);
        columns[10] = new Column("CurrentValue", "currentValue", Types.DOUBLE);
        columns[10].decimalPlaces = 2;
        columns[11] = new Column("BaseCreationDate", "baseCreationDate", Types.DATE);
        columns[12] = new Column("AwardCardOrderId", true);
        columns[13] = new Column("EmailId", true);
        columns[14] = new Column("EmployeeId", true);
        columns[15] = new Column("EmployeeAwardId", true);
        columns[16] = new Column("EmployeeAwardCashId", true);
        columns[17] = new Column("EmployeeAwardCharityId", true);
        columns[18] = new Column("EmployeeAwardIntlVisaId", true);
        columns[19] = new Column("EmployeeEcardToId", true);
        columns[20] = new Column("InspireOrderId", true);
        columns[21] = new Column("InspireOrderCashId", true);
        columns[22] = new Column("InspireOrderCharityId", true);
        columns[23] = new Column("InspireOrderIntlVisaId", true);
        columns[24] = new Column("InspireOrderItemId", true);
        columns[25] = new Column("InspireRecipientId", true);
        columns[26] = new Column("LocationId", true);
        columns[27] = new Column("PointsRecordId", true);
        columns[28] = new Column("PointsRequestId", true);
        columns[29] = new Column("ProgramId", true);
        tables[LINEITEM].setColumns(columns);
        tables[LINEITEM].addIndex(new Index("LineItemAwardCardOrder", "AwardCardOrderId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployee", "EmployeeId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployeeAward", "EmployeeAwardId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployeeAwardCash", "EmployeeAwardCashId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployeeAwardCharity", "EmployeeAwardCharityId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployeeAwardIntlVisa", "EmployeeAwardIntlVisaId"));
        tables[LINEITEM].addIndex(new Index("LineItemEmployeeEcardTo", "EmployeeEcardToId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireOrder", "InspireOrderId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireOrderCash", "InspireOrderCashId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireOrderCharity", "InspireOrderCharityId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireOrderIntlVisa", "InspireOrderIntlVisaId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireOrderItem", "InspireOrderItemId"));
        tables[LINEITEM].addIndex(new Index("LineItemInspireRecipient", "InspireRecipientId"));
        tables[LINEITEM].addIndex(new Index("LineItemLocation", "LocationId"));
        tables[LINEITEM].addIndex(new Index("LineItemPointsRecord", "PointsRecordId"));
        tables[LINEITEM].addIndex(new Index("LineItemPointsRequest", "PointsRequestId"));
        tables[LINEITEM].addIndex(new Index("LineItemProgram", "ProgramId"));
        
        // Location COLUMNS
        columns = new Column[27];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 125);
        columns[3] = new Column("Name2", "name2", Types.VARCHAR, 125);
        columns[4] = new Column("Seq", "seq", Types.INTEGER);
        columns[5] = new Column("Code", "code", Types.VARCHAR, 24);
        columns[6] = new Column("CharityGoal", "charityGoal", Types.DOUBLE);
        columns[6].decimalPlaces = 2;
        columns[7] = new Column("FromEmailAddress", "fromEmailAddress", Types.VARCHAR, 75);
        columns[8] = new Column("TestEmail", "testEmail", Types.VARCHAR, 125);
        columns[9] = new Column("IndexPageUsesBorders", "indexPageUsesBorders", Types.BOOLEAN);
        columns[10] = new Column("ScrollerShowsLocation", "scrollerShowsLocation", Types.BOOLEAN);
        columns[11] = new Column("ScrollerShowsDate", "scrollerShowsDate", Types.BOOLEAN);
        columns[12] = new Column("ScrollerShowsYears", "scrollerShowsYears", Types.BOOLEAN);
        columns[13] = new Column("ScrollerRowsPerSecond", "scrollerRowsPerSecond", Types.INTEGER);
        columns[14] = new Column("AddressId", true);
        columns[15] = new Column("AnnouncementDocumentId", true);
        columns[16] = new Column("CeoImageStoreId", true);
        columns[17] = new Column("CeoSignatureImageStoreId", true);
        columns[18] = new Column("CountryCodeId", true);
        columns[19] = new Column("ImagineCardId", true);
        columns[20] = new Column("InspireAwardTypeId", true);
        columns[21] = new Column("LocationTypeId", true);
        columns[22] = new Column("LogoImageStoreId", true);
        columns[23] = new Column("LogoStampImageStoreId", true);
        columns[24] = new Column("PageThemeId", true);
        columns[25] = new Column("ParentLocationId", true);
        columns[26] = new Column("ProgramId", true);
        tables[LOCATION].setColumns(columns);
        tables[LOCATION].addIndex(new Index("LocationInspireAwardType", "InspireAwardTypeId"));
        tables[LOCATION].addIndex(new Index("LocationParentLocation", "ParentLocationId"));
        tables[LOCATION].addIndex(new Index("LocationProgram", "ProgramId"));
        
        // LocationPageGroup COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("LocationId", true);
        columns[4] = new Column("PageGroupId", true);
        tables[LOCATIONPAGEGROUP].setColumns(columns);
        tables[LOCATIONPAGEGROUP].addIndex(new Index("LocationPageGroupLocation", "LocationId"));
        
        // LocationPageInfo COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("ImageStoreId", true);
        columns[3] = new Column("LocationId", true);
        columns[4] = new Column("PageInfoId", true);
        columns[5] = new Column("ProgramDocumentId", true);
        tables[LOCATIONPAGEINFO].setColumns(columns);
        tables[LOCATIONPAGEINFO].addIndex(new Index("LocationPageInfoLocation", "LocationId"));
        
        // LocationType COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("CompanyId", true);
        tables[LOCATIONTYPE].setColumns(columns);
        tables[LOCATIONTYPE].addIndex(new Index("LocationTypeCompany", "CompanyId"));
        
        // LoginImage COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Location", "location", Types.INTEGER);
        columns[2] = new Column("XPosition", "xPosition", Types.INTEGER);
        columns[3] = new Column("YPosition", "yPosition", Types.INTEGER);
        columns[4] = new Column("ImageStoreId", true);
        columns[5] = new Column("LoginImageSetId", true);
        tables[LOGINIMAGE].setColumns(columns);
        tables[LOGINIMAGE].addIndex(new Index("LoginImageLoginImageSet", "LoginImageSetId"));
        
        // LoginImageSet COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 35);
        tables[LOGINIMAGESET].setColumns(columns);
        
        // Lot COLUMNS
        columns = new Column[12];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Number", "number", Types.VARCHAR, 12);
        columns[2] = new Column("Sentiment", "sentiment", Types.VARCHAR, 25);
        columns[3] = new Column("CreatedDate", "createdDate", Types.DATE);
        columns[4] = new Column("OrderedDate", "orderedDate", Types.DATE);
        columns[5] = new Column("ActivatedDate", "activatedDate", Types.DATE);
        columns[6] = new Column("ExpirationDate", "expirationDate", Types.DATE);
        columns[7] = new Column("FaceValue", "faceValue", Types.DOUBLE);
        columns[7].decimalPlaces = 2;
        columns[8] = new Column("NumberOfCodes", "numberOfCodes", Types.INTEGER);
        columns[9] = new Column("SameValue", "sameValue", Types.BOOLEAN);
        columns[10] = new Column("ActivatingEmployeeId", true);
        columns[11] = new Column("PointsRequestId", true);
        tables[LOT].setColumns(columns);
        tables[LOT].addIndex(new Index("LotNumber", "Number"));
        tables[LOT].addIndex(new Index("LotActivatingEmployee", "ActivatingEmployeeId"));
        tables[LOT].addIndex(new Index("LotPointsRequest", "PointsRequestId"));
        
        // Merchant COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 150);
        columns[3] = new Column("Description", "description", Types.VARCHAR, 175);
        columns[4] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[5] = new Column("ImageStoreId", true);
        tables[MERCHANT].setColumns(columns);
        
        // MerchantCategory COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("ParentMerchantCategoryId", true);
        tables[MERCHANTCATEGORY].setColumns(columns);
        tables[MERCHANTCATEGORY].addIndex(new Index("MerchantCategoryParentMerchantCategory", "ParentMerchantCategoryId"));
        
        // OrderItemTracking COLUMNS
        columns = new Column[11];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("SentCarrier", "sentCarrier", Types.VARCHAR, 75);
        columns[2] = new Column("ProductionDate", "productionDate", Types.DATE);
        columns[3] = new Column("SentDate", "sentDate", Types.DATE);
        columns[4] = new Column("ExpectedDeliveryDate", "expectedDeliveryDate", Types.DATE);
        columns[5] = new Column("ConfirmEmailDate", "confirmEmailDate", Types.DATE);
        columns[6] = new Column("CarrierTracking", "carrierTracking", Types.VARCHAR, 75);
        columns[7] = new Column("CancelDate", "cancelDate", Types.DATE);
        columns[8] = new Column("ReplaceDate", "replaceDate", Types.DATE);
        columns[9] = new Column("Freight", "freight", Types.DOUBLE);
        columns[9].decimalPlaces = 2;
        columns[10] = new Column("OrderTrackingId", true);
        tables[ORDERITEMTRACKING].setColumns(columns);
        tables[ORDERITEMTRACKING].addIndex(new Index("OrderItemTrackingOrderTracking", "OrderTrackingId"));
        
        // OrderTracking COLUMNS
        columns = new Column[12];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Description", "description", Types.VARCHAR, 254);
        columns[3] = new Column("SentEmailDate", "sentEmailDate", Types.DATE);
        columns[4] = new Column("ReceivedDate", "receivedDate", Types.DATE);
        columns[5] = new Column("CloseDate", "closeDate", Types.DATE);
        columns[6] = new Column("Notes", "notes", Types.CLOB, 5);
        columns[7] = new Column("BillingDate", "billingDate", Types.DATE);
        columns[8] = new Column("BillAmount", "billAmount", Types.DOUBLE);
        columns[8].decimalPlaces = 2;
        columns[9] = new Column("InvoiceDate", "invoiceDate", Types.DATE);
        columns[10] = new Column("InvoiceNumber", "invoiceNumber", Types.VARCHAR, 20);
        columns[11] = new Column("PaidDate", "paidDate", Types.DATE);
        tables[ORDERTRACKING].setColumns(columns);
        
        // OrderTrackingStatus COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Description", "description", Types.VARCHAR, 45);
        columns[3] = new Column("Note", "note", Types.CLOB, 4);
        columns[4] = new Column("EmailAddress", "emailAddress", Types.VARCHAR, 75);
        columns[5] = new Column("EmailText", "emailText", Types.CLOB, 9);
        columns[6] = new Column("EmailDate", "emailDate", Types.DATE);
        columns[7] = new Column("OrderTrackingId", true);
        tables[ORDERTRACKINGSTATUS].setColumns(columns);
        tables[ORDERTRACKINGSTATUS].addIndex(new Index("OrderTrackingStatusOrderTracking", "OrderTrackingId"));
        
        // Page COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 50);
        columns[3] = new Column("Description", "description", Types.VARCHAR, 75);
        columns[4] = new Column("Seq", "seq", Types.INTEGER);
        tables[PAGE].setColumns(columns);
        tables[PAGE].addIndex(new Index("PageName", "Name"));
        
        // PageGroup COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("Name", "name", Types.VARCHAR, 35);
        columns[4] = new Column("ParentPageGroupId", true);
        tables[PAGEGROUP].setColumns(columns);
        tables[PAGEGROUP].addIndex(new Index("PageGroupParentPageGroup", "ParentPageGroupId"));
        
        // PageGroupPageInfo COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("ImageStoreId", true);
        columns[3] = new Column("PageGroupId", true);
        columns[4] = new Column("PageInfoId", true);
        columns[5] = new Column("ProgramDocumentId", true);
        tables[PAGEGROUPPAGEINFO].setColumns(columns);
        tables[PAGEGROUPPAGEINFO].addIndex(new Index("PageGroupPageInfoPageGroup", "PageGroupId"));
        
        // PageInfo COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Code", "code", Types.VARCHAR, 25);
        columns[2] = new Column("Description", "description", Types.VARCHAR, 55);
        columns[3] = new Column("Seq", "seq", Types.INTEGER);
        columns[4] = new Column("PageId", true);
        tables[PAGEINFO].setColumns(columns);
        tables[PAGEINFO].addIndex(new Index("PageInfoPage", "PageId"));
        
        // PageTheme COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("CssFileName", "cssFileName", Types.VARCHAR, 45);
        tables[PAGETHEME].setColumns(columns);
        
        // PageThemePageInfo COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("ImageStoreId", true);
        columns[3] = new Column("PageInfoId", true);
        columns[4] = new Column("PageThemeId", true);
        columns[5] = new Column("ProgramDocumentId", true);
        tables[PAGETHEMEPAGEINFO].setColumns(columns);
        tables[PAGETHEMEPAGEINFO].addIndex(new Index("PageThemePageInfoPageTheme", "PageThemeId"));
        
        // Phone COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("PhoneNumber", "phoneNumber", Types.VARCHAR, 50);
        columns[3] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[4] = new Column("PhoneTypeId", true);
        tables[PHONE].setColumns(columns);
        tables[PHONE].addIndex(new Index("PhoneEmployee", "EmployeeId"));
        
        // PhoneType COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 45);
        columns[2] = new Column("Type", "type", Types.INTEGER);
        tables[PHONETYPE].setColumns(columns);
        
        // PointsApproval COLUMNS
        columns = new Column[10];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Status", "status", Types.INTEGER);
        columns[3] = new Column("StatusDate", "statusDate", Types.DATE);
        columns[4] = new Column("Comments", "comments", Types.CLOB, 8);
        columns[5] = new Column("Seq", "seq", Types.INTEGER);
        columns[6] = new Column("ApprovedAwardLevelId", true);
        columns[7] = new Column("EmployeeId", true);
        columns[8] = new Column("PointsRequestId", true);
        columns[9] = new Column("StartingAwardLevelId", true);
        tables[POINTSAPPROVAL].setColumns(columns);
        tables[POINTSAPPROVAL].addIndex(new Index("PointsApprovalApprovedAwardLevel", "ApprovedAwardLevelId"));
        tables[POINTSAPPROVAL].addIndex(new Index("PointsApprovalEmployee", "EmployeeId"));
        tables[POINTSAPPROVAL].addIndex(new Index("PointsApprovalPointsRequest", "PointsRequestId"));
        tables[POINTSAPPROVAL].addIndex(new Index("PointsApprovalStartingAwardLevel", "StartingAwardLevelId"));
        
        // PointsAwardLevel COLUMNS
        columns = new Column[13];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("ApprovalLevels", "approvalLevels", Types.INTEGER);
        columns[4] = new Column("RequireSuperApprover", "requireSuperApprover", Types.BOOLEAN);
        columns[5] = new Column("Points", "points", Types.INTEGER);
        columns[6] = new Column("QuizRangeMin", "quizRangeMin", Types.INTEGER);
        columns[7] = new Column("QuizRangeMax", "quizRangeMax", Types.INTEGER);
        columns[8] = new Column("Description", "description", Types.CLOB, 11);
        columns[9] = new Column("Level", "level", Types.INTEGER);
        columns[10] = new Column("LocationId", true);
        columns[11] = new Column("ProgramId", true);
        columns[12] = new Column("RequiredApprovalId", true);
        tables[POINTSAWARDLEVEL].setColumns(columns);
        tables[POINTSAWARDLEVEL].addIndex(new Index("PointsAwardLevelLocation", "LocationId"));
        tables[POINTSAWARDLEVEL].addIndex(new Index("PointsAwardLevelProgram", "ProgramId"));
        tables[POINTSAWARDLEVEL].addIndex(new Index("PointsAwardLevelRequiredApproval", "RequiredApprovalId"));
        
        // PointsConfiguration COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("NotificationToNominatorManager", "notificationToNominatorManager", Types.BOOLEAN);
        columns[2] = new Column("NotificationToRecipientManager", "notificationToRecipientManager", Types.BOOLEAN);
        columns[3] = new Column("CertificateToNominator", "certificateToNominator", Types.BOOLEAN);
        columns[4] = new Column("CertificateToRecipient", "certificateToRecipient", Types.BOOLEAN);
        columns[5] = new Column("CertificateToRecipientManager", "certificateToRecipientManager", Types.BOOLEAN);
        columns[6] = new Column("NominationApprovedByRecipientManagement", "nominationApprovedBy", Types.INTEGER);
        columns[7] = new Column("DaysToDelayPoints", "daysToDelayPoints", Types.INTEGER);
        tables[POINTSCONFIGURATION].setColumns(columns);
        
        // PointsCoreValue COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("Description", "description", Types.CLOB, 200);
        columns[4] = new Column("LocationId", true);
        columns[5] = new Column("ProgramId", true);
        tables[POINTSCOREVALUE].setColumns(columns);
        tables[POINTSCOREVALUE].addIndex(new Index("PointsCoreValueLocation", "LocationId"));
        tables[POINTSCOREVALUE].addIndex(new Index("PointsCoreValueProgram", "ProgramId"));
        
        // PointsRecord COLUMNS
        columns = new Column[17];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Points", "points", Types.INTEGER);
        columns[3] = new Column("ToDiscretionary", "toDiscretionary", Types.BOOLEAN);
        columns[4] = new Column("Reason", "reason", Types.CLOB, 150);
        columns[5] = new Column("Comment", "comment", Types.CLOB, 7);
        columns[6] = new Column("Event", "event", Types.CLOB, 5);
        columns[7] = new Column("Custom1", "custom1", Types.CLOB, 7);
        columns[8] = new Column("Custom2", "custom2", Types.CLOB, 7);
        columns[9] = new Column("Custom3", "custom3", Types.CLOB, 7);
        columns[10] = new Column("Engaged", "engaged", Types.BOOLEAN);
        columns[11] = new Column("EmailId", true);
        columns[12] = new Column("InspireOrderId", true);
        columns[13] = new Column("PointsCoreValueId", true);
        columns[14] = new Column("PointsRequestId", true);
        columns[15] = new Column("PointsToEmployeeId", true);
        columns[16] = new Column("PointsToProgramId", true);
        tables[POINTSRECORD].setColumns(columns);
        tables[POINTSRECORD].addIndex(new Index("PointsRecordPointsCoreValue", "PointsCoreValueId"));
        tables[POINTSRECORD].addIndex(new Index("PointsRecordPointsRequest", "PointsRequestId"));
        tables[POINTSRECORD].addIndex(new Index("PointsRecordPointsToEmployee", "PointsToEmployeeId"));
        tables[POINTSRECORD].addIndex(new Index("PointsRecordPointsToProgram", "PointsToProgramId"));
        
        // PointsRequest COLUMNS
        columns = new Column[13];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Approved", "approved", Types.BOOLEAN);
        columns[3] = new Column("Notes", "notes", Types.CLOB, 5);
        columns[4] = new Column("ErrorNotes", "errorNotes", Types.CLOB, 10);
        columns[5] = new Column("Filename", "filename", Types.VARCHAR, 75);
        columns[6] = new Column("ApprovedDate", "approvedDate", Types.DATE);
        columns[7] = new Column("RequestType", "requestType", Types.INTEGER);
        columns[8] = new Column("ApprovingEmployeeId", true);
        columns[9] = new Column("ApprovingUserId", true);
        columns[10] = new Column("PointsAwardLevelId", true);
        columns[11] = new Column("QuizResultId", true);
        columns[12] = new Column("RequestingEmployeeId", true);
        tables[POINTSREQUEST].setColumns(columns);
        tables[POINTSREQUEST].addIndex(new Index("PointsRequestApprovingEmployee", "ApprovingEmployeeId"));
        tables[POINTSREQUEST].addIndex(new Index("PointsRequestApprovingUser", "ApprovingUserId"));
        tables[POINTSREQUEST].addIndex(new Index("PointsRequestPointsAwardLevel", "PointsAwardLevelId"));
        tables[POINTSREQUEST].addIndex(new Index("PointsRequestQuizResult", "QuizResultId"));
        tables[POINTSREQUEST].addIndex(new Index("PointsRequestRequestingEmployee", "RequestingEmployeeId"));
        
        // Product COLUMNS
        columns = new Column[13];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("VendorCode", "vendorCode", Types.VARCHAR, 55);
        columns[2] = new Column("Attribute", "attribute", Types.VARCHAR, 35);
        columns[3] = new Column("Cost", "cost", Types.DOUBLE);
        columns[3].decimalPlaces = 2;
        columns[4] = new Column("HandlingCost", "handlingCost", Types.DOUBLE);
        columns[4].decimalPlaces = 2;
        columns[5] = new Column("ShippingCost", "shippingCost", Types.DOUBLE);
        columns[5].decimalPlaces = 2;
        columns[6] = new Column("TotalCost", "totalCost", Types.DOUBLE);
        columns[6].decimalPlaces = 2;
        columns[7] = new Column("DiscontinuedDate", "discontinuedDate", Types.DATE);
        columns[8] = new Column("DiscontinuedReason", "discontinuedReason", Types.VARCHAR, 75);
        columns[9] = new Column("LastUpdate", "lastUpdate", Types.DATE);
        columns[10] = new Column("Msrp", "msrp", Types.DOUBLE);
        columns[10].decimalPlaces = 2;
        columns[11] = new Column("StreetValue", "streetValue", Types.DOUBLE);
        columns[11].decimalPlaces = 2;
        columns[12] = new Column("ItemId", true);
        tables[PRODUCT].setColumns(columns);
        tables[PRODUCT].addIndex(new Index("ProductVendorCode", "VendorCode"));
        tables[PRODUCT].addIndex(new Index("ProductItem", "ItemId"));
        
        // ProductAudit COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Cost", "cost", Types.DOUBLE);
        columns[2].decimalPlaces = 2;
        columns[3] = new Column("HandlingCost", "handlingCost", Types.DOUBLE);
        columns[3].decimalPlaces = 2;
        columns[4] = new Column("Note", "note", Types.VARCHAR, 125);
        columns[5] = new Column("ProductId", true);
        tables[PRODUCTAUDIT].setColumns(columns);
        tables[PRODUCTAUDIT].addIndex(new Index("ProductAuditProduct", "ProductId"));
        
        // Program COLUMNS
        columns = new Column[64];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Code", "code", Types.VARCHAR, 12);
        columns[3] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[4] = new Column("BeginDate", "beginDate", Types.DATE);
        columns[5] = new Column("EndDate", "endDate", Types.DATE);
        columns[6] = new Column("AwardBeginDate", "awardBeginDate", Types.DATE);
        columns[7] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[8] = new Column("UrlName", "urlName", Types.VARCHAR, 95);
        columns[9] = new Column("FromEmailAddress", "fromEmailAddress", Types.VARCHAR, 75);
        columns[10] = new Column("PointsName", "pointsName", Types.VARCHAR, 35);
        columns[11] = new Column("PointValue", "pointValue", Types.DOUBLE);
        columns[11].decimalPlaces = 3;
        columns[12] = new Column("UsesInspire", "usesInspire", Types.BOOLEAN);
        columns[13] = new Column("UsesHifive", "usesHifive", Types.BOOLEAN);
        columns[14] = new Column("UsesSurveys", "usesSurveys", Types.BOOLEAN);
        columns[15] = new Column("EcardType", "ecardType", Types.INTEGER);
        columns[16] = new Column("HifiveName", "hifiveName", Types.VARCHAR, 55);
        columns[17] = new Column("BirthdayDisplayDays", "birthdayDisplayDays", Types.INTEGER);
        columns[18] = new Column("AnniversaryDisplayDays", "anniversaryDisplayDays", Types.INTEGER);
        columns[19] = new Column("Seq", "seq", Types.INTEGER);
        columns[20] = new Column("LoginMessage", "loginMessage", Types.VARCHAR, 125);
        columns[21] = new Column("EmployeeAwardExpireDays", "employeeAwardExpireDays", Types.INTEGER);
        columns[22] = new Column("CharityGoal", "charityGoal", Types.DOUBLE);
        columns[22].decimalPlaces = 2;
        columns[23] = new Column("CharityTotal", "charityTotal", Types.DOUBLE);
        columns[23].decimalPlaces = 2;
        columns[24] = new Column("ItemUpcharge", "itemUpcharge", Types.DOUBLE);
        columns[24].decimalPlaces = 4;
        columns[25] = new Column("CardUpcharge", "cardUpcharge", Types.DOUBLE);
        columns[25].decimalPlaces = 4;
        columns[26] = new Column("CharityUpcharge", "charityUpcharge", Types.DOUBLE);
        columns[26].decimalPlaces = 4;
        columns[27] = new Column("CashUpcharge", "cashUpcharge", Types.DOUBLE);
        columns[27].decimalPlaces = 4;
        columns[28] = new Column("TestEmail", "testEmail", Types.VARCHAR, 125);
        columns[29] = new Column("UseAdvancedReports", "useAdvancedReports", Types.BOOLEAN);
        columns[30] = new Column("PacketInstructions", "packetInstructions", Types.CLOB, 80);
        columns[31] = new Column("SsoFailureUrl", "ssoFailureUrl", Types.VARCHAR, 120);
        columns[32] = new Column("SsoCode", "ssoCode", Types.VARCHAR, 35);
        columns[33] = new Column("SsoRedirectUrl", "ssoRedirectUrl", Types.VARCHAR, 120);
        columns[34] = new Column("UsesPoints", "usesPoints", Types.BOOLEAN);
        columns[35] = new Column("PointsBillingType", "pointsBillingType", Types.INTEGER);
        columns[36] = new Column("SsoLogoutUrl", "ssoLogoutUrl", Types.VARCHAR, 75);
        columns[37] = new Column("UsesPeerToPeer", "usesPeerToPeer", Types.BOOLEAN);
        columns[38] = new Column("UsesDiscretionary", "usesDiscretionary", Types.BOOLEAN);
        columns[39] = new Column("EmployeeAwardDaysBefore", "employeeAwardDaysBefore", Types.INTEGER);
        columns[40] = new Column("CompanyPaysShipping", "companyPaysShipping", Types.BOOLEAN);
        columns[41] = new Column("UsesNominations", "usesNominations", Types.BOOLEAN);
        columns[42] = new Column("UsesManagerToolkit", "usesManagerToolkit", Types.BOOLEAN);
        columns[43] = new Column("LotExpireDays", "lotExpireDays", Types.INTEGER);
        columns[44] = new Column("UploadApprovals", "uploadApprovals", Types.INTEGER);
        columns[45] = new Column("IndexPageUsesBorders", "indexPageUsesBorders", Types.BOOLEAN);
        columns[46] = new Column("ScrollerShowsLocation", "scrollerShowsLocation", Types.BOOLEAN);
        columns[47] = new Column("ScrollerShowsDate", "scrollerShowsDate", Types.BOOLEAN);
        columns[48] = new Column("ScrollerShowsYears", "scrollerShowsYears", Types.BOOLEAN);
        columns[49] = new Column("ScrollerRowsPerSecond", "scrollerRowsPerSecond", Types.INTEGER);
        columns[50] = new Column("PointsMargin", "pointsMargin", Types.FLOAT);
        columns[50].decimalPlaces = 4;
        columns[51] = new Column("PointsMarkup", "pointsMarkup", Types.FLOAT);
        columns[51].decimalPlaces = 4;
        columns[52] = new Column("AnnouncementDocumentId", true);
        columns[53] = new Column("CeoImageStoreId", true);
        columns[54] = new Column("CeoSignatureImageStoreId", true);
        columns[55] = new Column("CompanyId", true);
        columns[56] = new Column("CountryCodeId", true);
        columns[57] = new Column("ImagineCardId", true);
        columns[58] = new Column("InspireAwardTypeId", true);
        columns[59] = new Column("LoginImageSetId", true);
        columns[60] = new Column("LogoStampImageStoreId", true);
        columns[61] = new Column("ManagerHifiveSurveyId", true);
        columns[62] = new Column("PageThemeId", true);
        columns[63] = new Column("PointsConfigurationId", true);
        tables[PROGRAM].setColumns(columns);
        tables[PROGRAM].addIndex(new Index("ProgramCode", "Code"));
        tables[PROGRAM].addIndex(new Index("ProgramCompany", "CompanyId"));
        tables[PROGRAM].addIndex(new Index("ProgramInspireAwardType", "InspireAwardTypeId"));
        tables[PROGRAM].addIndex(new Index("ProgramLoginImageSet", "LoginImageSetId"));
        tables[PROGRAM].addIndex(new Index("ProgramManagerHifiveSurvey", "ManagerHifiveSurveyId"));
        tables[PROGRAM].addIndex(new Index("ProgramPageTheme", "PageThemeId"));
        tables[PROGRAM].addIndex(new Index("ProgramPointsConfiguration", "PointsConfigurationId"));
        
        // ProgramDocument COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("TextValue", "text", Types.CLOB, 4);
        tables[PROGRAMDOCUMENT].setColumns(columns);
        
        // ProgramEmailType COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("AllowAutomaticSend", "allowAutomaticSend", Types.BOOLEAN);
        columns[3] = new Column("Subject", "subject", Types.VARCHAR, 150);
        columns[4] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[5] = new Column("EmailTypeId", true);
        columns[6] = new Column("LocationId", true);
        columns[7] = new Column("ProgramId", true);
        tables[PROGRAMEMAILTYPE].setColumns(columns);
        tables[PROGRAMEMAILTYPE].addIndex(new Index("ProgramEmailTypeLocation", "LocationId"));
        tables[PROGRAMEMAILTYPE].addIndex(new Index("ProgramEmailTypeProgram", "ProgramId"));
        
        // ProgramEvent COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("BeginDateTime", "beginDateTime", Types.TIMESTAMP);
        columns[3] = new Column("EndDateTime", "endDateTime", Types.TIMESTAMP);
        columns[4] = new Column("Name", "name", Types.VARCHAR, 175);
        columns[5] = new Column("TextValue", "text", Types.CLOB, 4);
        columns[6] = new Column("LocationId", true);
        columns[7] = new Column("ProgramId", true);
        tables[PROGRAMEVENT].setColumns(columns);
        tables[PROGRAMEVENT].addIndex(new Index("ProgramEventLocation", "LocationId"));
        tables[PROGRAMEVENT].addIndex(new Index("ProgramEventProgram", "ProgramId"));
        
        // ProgramFaq COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("Question", "question", Types.VARCHAR, 150);
        columns[4] = new Column("Answer", "answer", Types.CLOB, 11);
        columns[5] = new Column("ManagerOnly", "managerOnly", Types.BOOLEAN);
        columns[6] = new Column("ProgramId", true);
        tables[PROGRAMFAQ].setColumns(columns);
        tables[PROGRAMFAQ].addIndex(new Index("ProgramFaqLocation", "LocationId"));
        tables[PROGRAMFAQ].addIndex(new Index("ProgramFaqProgram", "ProgramId"));
        
        // ProgramPageGroup COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Seq", "seq", Types.INTEGER);
        columns[3] = new Column("PageGroupId", true);
        columns[4] = new Column("ProgramId", true);
        tables[PROGRAMPAGEGROUP].setColumns(columns);
        tables[PROGRAMPAGEGROUP].addIndex(new Index("ProgramPageGroupProgram", "ProgramId"));
        
        // ProgramPageInfo COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("ImageStoreId", true);
        columns[3] = new Column("PageInfoId", true);
        columns[4] = new Column("ProgramId", true);
        columns[5] = new Column("ProgramDocumentId", true);
        tables[PROGRAMPAGEINFO].setColumns(columns);
        tables[PROGRAMPAGEINFO].addIndex(new Index("ProgramPageInfoProgram", "ProgramId"));
        
        // Question COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("QuestionText", "questionText", Types.CLOB, 200);
        columns[2] = new Column("QuizId", true);
        tables[QUESTION].setColumns(columns);
        tables[QUESTION].addIndex(new Index("QuestionQuiz", "QuizId"));
        
        // QuestionResult COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("QuestionText", "questionText", Types.CLOB, 200);
        columns[2] = new Column("QuestionId", true);
        columns[3] = new Column("QuizResultId", true);
        tables[QUESTIONRESULT].setColumns(columns);
        tables[QUESTIONRESULT].addIndex(new Index("QuestionResultQuestion", "QuestionId"));
        tables[QUESTIONRESULT].addIndex(new Index("QuestionResultQuizResult", "QuizResultId"));
        
        // Quiz COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 40);
        columns[2] = new Column("LocationId", true);
        columns[3] = new Column("ProgramId", true);
        columns[4] = new Column("Program2Id", true);
        tables[QUIZ].setColumns(columns);
        tables[QUIZ].addIndex(new Index("QuizLocation", "LocationId"));
        tables[QUIZ].addIndex(new Index("QuizProgram", "ProgramId"));
        tables[QUIZ].addIndex(new Index("QuizProgram2", "Program2Id"));
        
        // QuizResult COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 40);
        columns[2] = new Column("EmployeeId", true);
        columns[3] = new Column("QuizId", true);
        tables[QUIZRESULT].setColumns(columns);
        tables[QUIZRESULT].addIndex(new Index("QuizResultEmployee", "EmployeeId"));
        tables[QUIZRESULT].addIndex(new Index("QuizResultQuiz", "QuizId"));
        
        // Report COLUMNS
        columns = new Column[8];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Code", "code", Types.VARCHAR, 20);
        columns[3] = new Column("Name", "name", Types.VARCHAR, 35);
        columns[4] = new Column("Orientation", "orientation", Types.INTEGER);
        columns[5] = new Column("Template", "template", Types.CLOB, 8);
        columns[6] = new Column("ParentReportId", true);
        columns[7] = new Column("ReportClassId", true);
        tables[REPORT].setColumns(columns);
        tables[REPORT].addIndex(new Index("ReportParentReport", "ParentReportId"));
        tables[REPORT].addIndex(new Index("ReportReportClass", "ReportClassId"));
        
        // ReportClass COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Name", "name", Types.VARCHAR, 25);
        columns[2] = new Column("ClassName", "className", Types.VARCHAR, 50);
        tables[REPORTCLASS].setColumns(columns);
        
        // Section COLUMNS
        columns = new Column[5];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Seq", "seq", Types.INTEGER);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("CatalogId", true);
        columns[4] = new Column("ParentSectionId", true);
        tables[SECTION].setColumns(columns);
        tables[SECTION].addIndex(new Index("SectionCatalog", "CatalogId"));
        tables[SECTION].addIndex(new Index("SectionParentSection", "ParentSectionId"));
        
        // ShipTo COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 125);
        columns[3] = new Column("Note", "note", Types.VARCHAR, 254);
        columns[4] = new Column("Email", "email", Types.VARCHAR, 125);
        columns[5] = new Column("PhoneNumber", "phoneNumber", Types.VARCHAR, 50);
        columns[6] = new Column("AddressId", true);
        tables[SHIPTO].setColumns(columns);
        
        // Survey COLUMNS
        columns = new Column[7];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("Descripiton", "descripiton", Types.VARCHAR, 254);
        columns[4] = new Column("AllowText", "allowText", Types.BOOLEAN);
        columns[5] = new Column("ProgramId", true);
        columns[6] = new Column("QuizProgramId", true);
        tables[SURVEY].setColumns(columns);
        tables[SURVEY].addIndex(new Index("SurveyProgram", "ProgramId"));
        tables[SURVEY].addIndex(new Index("SurveyQuizProgram", "QuizProgramId"));
        
        // SurveyAnswer COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("TextValue", "text", Types.CLOB, 254);
        columns[2] = new Column("SurveyAnswerQuestionId", true);
        columns[3] = new Column("SurveyQuestionId", true);
        tables[SURVEYANSWER].setColumns(columns);
        tables[SURVEYANSWER].addIndex(new Index("SurveyAnswerSurveyAnswerQuestion", "SurveyAnswerQuestionId"));
        tables[SURVEYANSWER].addIndex(new Index("SurveyAnswerSurveyQuestion", "SurveyQuestionId"));
        
        // SurveyQuestion COLUMNS
        columns = new Column[6];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Type", "type", Types.INTEGER);
        columns[2] = new Column("AllowTextResponse", "allowTextResponse", Types.BOOLEAN);
        columns[3] = new Column("TextValue", "text", Types.CLOB, 254);
        columns[4] = new Column("Seq", "seq", Types.INTEGER);
        columns[5] = new Column("SurveyId", true);
        tables[SURVEYQUESTION].setColumns(columns);
        tables[SURVEYQUESTION].addIndex(new Index("SurveyQuestionSurvey", "SurveyId"));
        
        // User COLUMNS
        columns = new Column[12];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("FirstName", "firstName", Types.VARCHAR, 75);
        columns[3] = new Column("LastName", "lastName", Types.VARCHAR, 75);
        columns[4] = new Column("LoginId", "loginId", Types.VARCHAR, 75);
        columns[5] = new Column("Password", "password", Types.VARCHAR, 50);
        columns[6] = new Column("InactiveDate", "inactiveDate", Types.DATE);
        columns[7] = new Column("Admin", "admin", Types.BOOLEAN);
        columns[8] = new Column("Email", "email", Types.VARCHAR, 125);
        columns[9] = new Column("LoggedIn", "loggedIn", Types.BOOLEAN);
        columns[10] = new Column("Location", "location", Types.VARCHAR, 45);
        columns[11] = new Column("EditProcessed", "editProcessed", Types.BOOLEAN);
        tables[USER].setColumns(columns);
        tables[USER].addIndex(new Index("UserTableLastName", "LastName"));
        
        // Value COLUMNS
        columns = new Column[3];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Value", "value", Types.DOUBLE);
        columns[1].decimalPlaces = 2;
        columns[2] = new Column("Name", "name", Types.VARCHAR, 55);
        tables[VALUE].setColumns(columns);
        
        // Widget COLUMNS
        columns = new Column[4];
        columns[0] = new Column("Id", "id", Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("Created", "created", Types.DATE);
        columns[2] = new Column("Name", "name", Types.VARCHAR, 75);
        columns[3] = new Column("Link", "link", Types.VARCHAR, 175);
        tables[WIDGET].setColumns(columns);
        
        // Link Tables Columns
        
        // AwardTypeIncludeItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("AwardTypeId",null);
        columns[1] = new Column("ItemId",null);
        tables[AWARDTYPEINCLUDEITEM].setColumns(columns);
        tables[AWARDTYPEINCLUDEITEM].addIndex(new Index("ItemIncludeAwardType", new String[] {"AwardTypeId"}));
        tables[AWARDTYPEINCLUDEITEM].addIndex(new Index("AwardTypeIncludeItem", new String[] {"ItemId"}));
        
        // EmployeePhone COLUMNS
        columns = new Column[2];
        columns[0] = new Column("EmployeeId",null);
        columns[1] = new Column("PhoneId",null);
        tables[EMPLOYEEPHONE].setColumns(columns);
        tables[EMPLOYEEPHONE].addIndex(new Index("PhoneEmployee", new String[] {"EmployeeId"}));
        tables[EMPLOYEEPHONE].addIndex(new Index("EmployeePhone", new String[] {"PhoneId"}));
        
        // ProgramEcard COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("EcardId",null);
        tables[PROGRAMECARD].setColumns(columns);
        tables[PROGRAMECARD].addIndex(new Index("EcardProgram", new String[] {"ProgramId"}));
        tables[PROGRAMECARD].addIndex(new Index("ProgramEcard", new String[] {"EcardId"}));
        
        // ProgramProgramDocument COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("ProgramDocumentId",null);
        tables[PROGRAMPROGRAMDOCUMENT].setColumns(columns);
        tables[PROGRAMPROGRAMDOCUMENT].addIndex(new Index("ProgramDocumentBlogProgram", new String[] {"ProgramId"}));
        tables[PROGRAMPROGRAMDOCUMENT].addIndex(new Index("ProgramBlogDocument", new String[] {"ProgramDocumentId"}));
        
        // MerchantCard COLUMNS
        columns = new Column[2];
        columns[0] = new Column("MerchantId",null);
        columns[1] = new Column("CardId",null);
        tables[MERCHANTCARD].setColumns(columns);
        tables[MERCHANTCARD].addIndex(new Index("CardMerchant", new String[] {"MerchantId"}));
        tables[MERCHANTCARD].addIndex(new Index("MerchantCard", new String[] {"CardId"}));
        
        // ProgramCard COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("CardId",null);
        tables[PROGRAMCARD].setColumns(columns);
        tables[PROGRAMCARD].addIndex(new Index("CardProgram", new String[] {"ProgramId"}));
        tables[PROGRAMCARD].addIndex(new Index("ProgramCard", new String[] {"CardId"}));
        
        // ItemCategoryItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ItemCategoryId",null);
        columns[1] = new Column("ItemId",null);
        tables[ITEMCATEGORYITEM].setColumns(columns);
        tables[ITEMCATEGORYITEM].addIndex(new Index("ItemItemCategory", new String[] {"ItemCategoryId"}));
        tables[ITEMCATEGORYITEM].addIndex(new Index("ItemCategoryItem", new String[] {"ItemId"}));
        
        // SectionItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("SectionId",null);
        columns[1] = new Column("ItemId",null);
        tables[SECTIONITEM].setColumns(columns);
        tables[SECTIONITEM].addIndex(new Index("ItemSection", new String[] {"SectionId"}));
        tables[SECTIONITEM].addIndex(new Index("SectionItem", new String[] {"ItemId"}));
        
        // AwardTypeExcludeItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("AwardTypeId",null);
        columns[1] = new Column("ItemId",null);
        tables[AWARDTYPEEXCLUDEITEM].setColumns(columns);
        tables[AWARDTYPEEXCLUDEITEM].addIndex(new Index("ItemExcludeAwardType", new String[] {"AwardTypeId"}));
        tables[AWARDTYPEEXCLUDEITEM].addIndex(new Index("AwardTypeExcludeItem", new String[] {"ItemId"}));
        
        // ProgramImageStore COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LogoProgramId",null);
        columns[1] = new Column("ImageStoreId",null);
        tables[PROGRAMIMAGESTORE].setColumns(columns);
        tables[PROGRAMIMAGESTORE].addIndex(new Index("ImageStoreLogoProgram", new String[] {"LogoProgramId"}));
        tables[PROGRAMIMAGESTORE].addIndex(new Index("ProgramLogoImageStore", new String[] {"ImageStoreId"}));
        
        // ImageStoreProgram COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ImageStoreId",null);
        columns[1] = new Column("ProgramId",null);
        tables[IMAGESTOREPROGRAM].setColumns(columns);
        tables[IMAGESTOREPROGRAM].addIndex(new Index("ProgramImageStore", new String[] {"ImageStoreId"}));
        tables[IMAGESTOREPROGRAM].addIndex(new Index("ImageStoreProgram", new String[] {"ProgramId"}));
        
        // MerchantCategoryMerchant COLUMNS
        columns = new Column[2];
        columns[0] = new Column("MerchantCategoryId",null);
        columns[1] = new Column("MerchantId",null);
        tables[MERCHANTCATEGORYMERCHANT].setColumns(columns);
        tables[MERCHANTCATEGORYMERCHANT].addIndex(new Index("MerchantMerchantCategory", new String[] {"MerchantCategoryId"}));
        tables[MERCHANTCATEGORYMERCHANT].addIndex(new Index("MerchantCategoryMerchant", new String[] {"MerchantId"}));
        
        // ProgramWidget COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("WidgetId",null);
        tables[PROGRAMWIDGET].setColumns(columns);
        tables[PROGRAMWIDGET].addIndex(new Index("WidgetProgram", new String[] {"ProgramId"}));
        tables[PROGRAMWIDGET].addIndex(new Index("ProgramWidget", new String[] {"WidgetId"}));
        
        // ReplaceItemItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ItemId2",null);
        columns[1] = new Column("ItemId",null);
        tables[REPLACEITEMITEM].setColumns(columns);
        tables[REPLACEITEMITEM].addIndex(new Index("ItemReplaceItem", new String[] {"ItemId2"}));
        tables[REPLACEITEMITEM].addIndex(new Index("ItemReplacesItem", new String[] {"ItemId"}));
        
        // ItemTypeItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ItemTypeId",null);
        columns[1] = new Column("ItemId",null);
        tables[ITEMTYPEITEM].setColumns(columns);
        tables[ITEMTYPEITEM].addIndex(new Index("ItemItemType", new String[] {"ItemTypeId"}));
        tables[ITEMTYPEITEM].addIndex(new Index("ItemTypeItem", new String[] {"ItemId"}));
        
        // LocationEcard COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[1] = new Column("EcardId",null);
        tables[LOCATIONECARD].setColumns(columns);
        tables[LOCATIONECARD].addIndex(new Index("EcardLocation", new String[] {"LocationId"}));
        tables[LOCATIONECARD].addIndex(new Index("LocationEcard", new String[] {"EcardId"}));
        
        // LocationProgramFaq COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[1] = new Column("ProgramFaqId",null);
        tables[LOCATIONPROGRAMFAQ].setColumns(columns);
        tables[LOCATIONPROGRAMFAQ].addIndex(new Index("ProgramFaqLocation", new String[] {"LocationId"}));
        tables[LOCATIONPROGRAMFAQ].addIndex(new Index("LocationProgramFaq", new String[] {"ProgramFaqId"}));
        
        // AwardTypeItemType COLUMNS
        columns = new Column[2];
        columns[0] = new Column("AwardTypeId",null);
        columns[1] = new Column("ItemTypeId",null);
        tables[AWARDTYPEITEMTYPE].setColumns(columns);
        tables[AWARDTYPEITEMTYPE].addIndex(new Index("ItemTypeAwardType", new String[] {"AwardTypeId"}));
        tables[AWARDTYPEITEMTYPE].addIndex(new Index("AwardTypeItemType", new String[] {"ItemTypeId"}));
        
        // LocationCard COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[1] = new Column("CardId",null);
        tables[LOCATIONCARD].setColumns(columns);
        tables[LOCATIONCARD].addIndex(new Index("CardLocation", new String[] {"LocationId"}));
        tables[LOCATIONCARD].addIndex(new Index("LocationCard", new String[] {"CardId"}));
        
        // ProgramCharity COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("CharityId",null);
        tables[PROGRAMCHARITY].setColumns(columns);
        tables[PROGRAMCHARITY].addIndex(new Index("CharityProgram", new String[] {"ProgramId"}));
        tables[PROGRAMCHARITY].addIndex(new Index("ProgramCharity", new String[] {"CharityId"}));
        
        // LocationCharity COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[1] = new Column("CharityId",null);
        tables[LOCATIONCHARITY].setColumns(columns);
        tables[LOCATIONCHARITY].addIndex(new Index("CharityLocation", new String[] {"LocationId"}));
        tables[LOCATIONCHARITY].addIndex(new Index("LocationCharity", new String[] {"CharityId"}));
        
        // LocationInspireCoreValue COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[1] = new Column("InspireCoreValueId",null);
        tables[LOCATIONINSPIRECOREVALUE].setColumns(columns);
        tables[LOCATIONINSPIRECOREVALUE].addIndex(new Index("InspireCoreValueLocation", new String[] {"LocationId"}));
        tables[LOCATIONINSPIRECOREVALUE].addIndex(new Index("LocationInspireCoreValue", new String[] {"InspireCoreValueId"}));
        
        // CardValue COLUMNS
        columns = new Column[2];
        columns[0] = new Column("CardId",null);
        columns[1] = new Column("ValueId",null);
        tables[CARDVALUE].setColumns(columns);
        tables[CARDVALUE].addIndex(new Index("ValueCard", new String[] {"CardId"}));
        tables[CARDVALUE].addIndex(new Index("CardValue", new String[] {"ValueId"}));
        
        // EmailEmployeeAward COLUMNS
        columns = new Column[2];
        columns[0] = new Column("EmailId",null);
        columns[1] = new Column("EmployeeAwardShippedId",null);
        tables[EMAILEMPLOYEEAWARD].setColumns(columns);
        tables[EMAILEMPLOYEEAWARD].addIndex(new Index("EmployeeAwardShippedEmail", new String[] {"EmailId"}));
        tables[EMAILEMPLOYEEAWARD].addIndex(new Index("EmailEmployeeAwardShipped", new String[] {"EmployeeAwardShippedId"}));
        
        // AddOnItemAwardType COLUMNS
        columns = new Column[2];
        columns[0] = new Column("AddOnItemId",null);
        columns[1] = new Column("AwardTypeId",null);
        tables[ADDONITEMAWARDTYPE].setColumns(columns);
        tables[ADDONITEMAWARDTYPE].addIndex(new Index("AwardTypeAddOnItem", new String[] {"AddOnItemId"}));
        tables[ADDONITEMAWARDTYPE].addIndex(new Index("AddOnItemAwardType", new String[] {"AwardTypeId"}));
        
        // ProgramAddOnItem COLUMNS
        columns = new Column[2];
        columns[0] = new Column("ProgramId",null);
        columns[1] = new Column("AddOnItemId",null);
        tables[PROGRAMADDONITEM].setColumns(columns);
        tables[PROGRAMADDONITEM].addIndex(new Index("AddOnItemProgram", new String[] {"ProgramId"}));
        tables[PROGRAMADDONITEM].addIndex(new Index("ProgramAddOnItem", new String[] {"AddOnItemId"}));
        
        // InspireApprovalEmail COLUMNS
        columns = new Column[2];
        columns[0] = new Column("InspireApprovalReminderId",null);
        columns[1] = new Column("EmailId",null);
        tables[INSPIREAPPROVALEMAIL].setColumns(columns);
        tables[INSPIREAPPROVALEMAIL].addIndex(new Index("EmailInspireApprovalReminder", new String[] {"InspireApprovalReminderId"}));
        tables[INSPIREAPPROVALEMAIL].addIndex(new Index("InspireApprovalReminderEmail", new String[] {"EmailId"}));
        
        // AwardTypeProduct COLUMNS
        columns = new Column[2];
        columns[0] = new Column("AwardTypeId",null);
        columns[1] = new Column("ProductId",null);
        tables[AWARDTYPEPRODUCT].setColumns(columns);
        tables[AWARDTYPEPRODUCT].addIndex(new Index("ProductAwardType", new String[] {"AwardTypeId"}));
        tables[AWARDTYPEPRODUCT].addIndex(new Index("AwardTypePackageProduct", new String[] {"ProductId"}));
        
        // InspireOrderEmail COLUMNS
        columns = new Column[2];
        columns[0] = new Column("InspireOrderId",null);
        columns[1] = new Column("EmailId",null);
        tables[INSPIREORDEREMAIL].setColumns(columns);
        tables[INSPIREORDEREMAIL].addIndex(new Index("EmailInspireOrder", new String[] {"InspireOrderId"}));
        tables[INSPIREORDEREMAIL].addIndex(new Index("InspireOrderEmail", new String[] {"EmailId"}));
        
        // PointsRequestEmail COLUMNS
        columns = new Column[2];
        columns[0] = new Column("PointsRequestId",null);
        columns[1] = new Column("EmailId",null);
        tables[POINTSREQUESTEMAIL].setColumns(columns);
        tables[POINTSREQUESTEMAIL].addIndex(new Index("EmailPointsRequest", new String[] {"PointsRequestId"}));
        tables[POINTSREQUESTEMAIL].addIndex(new Index("PointsRequestEmail", new String[] {"EmailId"}));
        
        // LINKS
        // table.addLink( propertyName, toTableName, reversePropertyName, FKey ColumnNumber(s))
        tables[ABOUTME].addLink("employee", tables[EMPLOYEE], "aboutMe", new int[] {7});
        tables[ADDONITEM].addLink("item", tables[ITEM], "addOnItems", new int[] {3});
        tables[ADDONITEM].addLink("location", tables[LOCATION], "addOnItems", new int[] {4});
        tables[ADDRESS].addLink("addressType", tables[ADDRESSTYPE], "addresses", new int[] {10});
        tables[ADDRESS].addLink("employee", tables[EMPLOYEE], "addresses", new int[] {11});
        tables[ADDRESS].addLink("location", tables[LOCATION], "address", new int[] {0});
        tables[ADDRESS].addLink("shipTos", tables[SHIPTO], "address", new int[] {0});
        tables[ADDRESSTYPE].addLink("addresses", tables[ADDRESS], "addressType", new int[] {0});
        tables[ANSWER].addLink("question", tables[QUESTION], "answers", new int[] {3});
        tables[ANSWER].addLink("savedAnswerResults", tables[ANSWERRESULT], "currentAnswer", new int[] {0});
        tables[ANSWERRESULT].addLink("currentAnswer", tables[ANSWER], "savedAnswerResults", new int[] {3});
        tables[ANSWERRESULT].addLink("questionResult", tables[QUESTIONRESULT], "answerResults", new int[] {4});
        tables[AWARDCARDORDER].addLink("card", tables[CARD], "awardCardOrders", new int[] {13});
        tables[AWARDCARDORDER].addLink("cashstarOrder", tables[CASHSTARORDER], "awardCardOrder", new int[] {0});
        tables[AWARDCARDORDER].addLink("employeeAward", tables[EMPLOYEEAWARD], "awardCardOrders", new int[] {14});
        tables[AWARDCARDORDER].addLink("inspireOrder", tables[INSPIREORDER], "awardCardOrders", new int[] {15});
        tables[AWARDCARDORDER].addLink("lineItems", tables[LINEITEM], "awardCardOrder", new int[] {0});
        tables[AWARDTYPE].addLink("announcementDocument", tables[PROGRAMDOCUMENT], "awardTypes", new int[] {26});
        tables[AWARDTYPE].addLink("ceoImageStore", tables[IMAGESTORE], "awardType", new int[] {27});
        tables[AWARDTYPE].addLink("ceoSignatureImageStore", tables[IMAGESTORE], "awardTypes", new int[] {28});
        tables[AWARDTYPE].addLink("employeeAwards", tables[EMPLOYEEAWARD], "awardType", new int[] {0});
        tables[AWARDTYPE].addLink("imagineCard", tables[CARD], "imagineAwardTypes", new int[] {29});
        tables[AWARDTYPE].addLink("inspireLocation", tables[LOCATION], "inspireAwardType", new int[] {0});
        tables[AWARDTYPE].addLink("inspireProgram", tables[PROGRAM], "inspireAwardType", new int[] {0});
        tables[AWARDTYPE].addLink("location", tables[LOCATION], "awardTypes", new int[] {30});
        tables[AWARDTYPE].addLink("program", tables[PROGRAM], "awardTypes", new int[] {31});
        tables[AWARDTYPE].addLink("section", tables[SECTION], "awardTypes", new int[] {32});
        tables[CARD].addLink("awardCardOrders", tables[AWARDCARDORDER], "card", new int[] {0});
        tables[CARD].addLink("cardVendor", tables[CARDVENDOR], "cards", new int[] {13});
        tables[CARD].addLink("imageLocations", tables[LOCATION], "imagineCard", new int[] {0});
        tables[CARD].addLink("imageStore", tables[IMAGESTORE], "card", new int[] {14});
        tables[CARD].addLink("imagineAwardTypes", tables[AWARDTYPE], "imagineCard", new int[] {0});
        tables[CARD].addLink("imaginePrograms", tables[PROGRAM], "imagineCard", new int[] {0});
        tables[CARDVENDOR].addLink("cards", tables[CARD], "cardVendor", new int[] {0});
        tables[CASHSTARORDER].addLink("awardCardOrder", tables[AWARDCARDORDER], "cashstarOrder", new int[] {19});
        tables[CATALOG].addLink("sections", tables[SECTION], "catalog", new int[] {0});
        tables[CHARITY].addLink("employeeAwardCharities", tables[EMPLOYEEAWARDCHARITY], "charity", new int[] {0});
        tables[CHARITY].addLink("imageStore", tables[IMAGESTORE], "charity", new int[] {6});
        tables[CHARITY].addLink("inspireOrderCharities", tables[INSPIREORDERCHARITY], "charity", new int[] {0});
        tables[CODE].addLink("lot", tables[LOT], "codes", new int[] {5});
        tables[CODE].addLink("pointsRecord", tables[POINTSRECORD], "code", new int[] {6});
        tables[CODE].addLink("redeemingEmployee", tables[EMPLOYEE], "redeemedCodes", new int[] {7});
        tables[COMPANY].addLink("locationTypes", tables[LOCATIONTYPE], "company", new int[] {0});
        tables[COMPANY].addLink("programs", tables[PROGRAM], "company", new int[] {0});
        tables[COUNTRYCODE].addLink("currencyType", tables[CURRENCYTYPE], "countryCodes", new int[] {7});
        tables[COUNTRYCODE].addLink("locations", tables[LOCATION], "countryCode", new int[] {0});
        tables[COUNTRYCODE].addLink("programs", tables[PROGRAM], "countryCode", new int[] {0});
        tables[CURRENCYTYPE].addLink("countryCodes", tables[COUNTRYCODE], "currencyType", new int[] {0});
        tables[CUSTOMDATA].addLink("employeeCustomDatas", tables[EMPLOYEECUSTOMDATA], "customData", new int[] {0});
        tables[CUSTOMDATA].addLink("program", tables[PROGRAM], "customData", new int[] {4});
        tables[ECARD].addLink("ecardCategory", tables[ECARDCATEGORY], "ecards", new int[] {4});
        tables[ECARD].addLink("employeeEcards", tables[EMPLOYEEECARD], "ecard", new int[] {0});
        tables[ECARD].addLink("imageStore", tables[IMAGESTORE], "ecard", new int[] {5});
        tables[ECARDCATEGORY].addLink("ecards", tables[ECARD], "ecardCategory", new int[] {0});
        tables[EMAIL].addLink("employeeAwardConfirm", tables[EMPLOYEEAWARD], "confirmEmail", new int[] {0});
        tables[EMAIL].addLink("employeeAwardManagerNotify", tables[EMPLOYEEAWARD], "managerNotifyEmail", new int[] {0});
        tables[EMAIL].addLink("employeeAwardNotify", tables[EMPLOYEEAWARD], "notifyEmail", new int[] {0});
        tables[EMAIL].addLink("employeeEcardConfirmed", tables[EMPLOYEEECARD], "confirmedEmail", new int[] {0});
        tables[EMAIL].addLink("employeeEcardDelivered", tables[EMPLOYEEECARD], "deliveredEmail", new int[] {0});
        tables[EMAIL].addLink("employeeEcardTo", tables[EMPLOYEEECARDTO], "email", new int[] {0});
        tables[EMAIL].addLink("inspire", tables[INSPIRE], "email", new int[] {0});
        tables[EMAIL].addLink("inspireApproval", tables[INSPIREAPPROVAL], "email", new int[] {0});
        tables[EMAIL].addLink("inspireRecipient", tables[INSPIRERECIPIENT], "email", new int[] {0});
        tables[EMAIL].addLink("inspireRecipientCompleted", tables[INSPIRERECIPIENT], "completedEmail", new int[] {0});
        tables[EMAIL].addLink("lineItem", tables[LINEITEM], "email", new int[] {0});
        tables[EMAIL].addLink("pointsRecord", tables[POINTSRECORD], "email", new int[] {0});
        tables[EMAIL].addLink("programEmailType", tables[PROGRAMEMAILTYPE], "emails", new int[] {12});
        tables[EMAILTYPE].addLink("programEmailTypes", tables[PROGRAMEMAILTYPE], "emailType", new int[] {0});
        tables[EMPLOYEE].addLink("aboutMe", tables[ABOUTME], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("activatedLots", tables[LOT], "activatingEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("addresses", tables[ADDRESS], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("approvedPointsRequests", tables[POINTSREQUEST], "approvingEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("BPEmployees", tables[EMPLOYEE], "HRBPartner", new int[] {0});
        tables[EMPLOYEE].addLink("employeeAwards", tables[EMPLOYEEAWARD], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("employeeCustomDatas", tables[EMPLOYEECUSTOMDATA], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("employeeEcardRecipients", tables[EMPLOYEEECARDTO], "toEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("employeeEcards", tables[EMPLOYEEECARD], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("employees", tables[EMPLOYEE], "parentEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("employeeSurveys", tables[EMPLOYEESURVEY], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("employeeType", tables[EMPLOYEETYPE], "employees", new int[] {40});
        tables[EMPLOYEE].addLink("familyMembers", tables[FAMILYMEMBER], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("HRBPartner", tables[EMPLOYEE], "BPEmployees", new int[] {41});
        tables[EMPLOYEE].addLink("inspireApprovals", tables[INSPIREAPPROVAL], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("inspireOrders", tables[INSPIREORDER], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("inspireRecipients", tables[INSPIRERECIPIENT], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("inspires", tables[INSPIRE], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("lineItems", tables[LINEITEM], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("location", tables[LOCATION], "employees", new int[] {42});
        tables[EMPLOYEE].addLink("parentEmployee", tables[EMPLOYEE], "employees", new int[] {43});
        tables[EMPLOYEE].addLink("pointsApprovals", tables[POINTSAPPROVAL], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("pointsFromRecord", tables[POINTSRECORD], "pointsToEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("pointsNextApproval", tables[EMPLOYEE], "pointsPrevApproval", new int[] {44});
        tables[EMPLOYEE].addLink("pointsPrevApproval", tables[EMPLOYEE], "pointsNextApproval", new int[] {0});
        tables[EMPLOYEE].addLink("pointsRequest", tables[POINTSREQUEST], "requestingEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("quizResults", tables[QUIZRESULT], "employee", new int[] {0});
        tables[EMPLOYEE].addLink("redeemedCodes", tables[CODE], "redeemingEmployee", new int[] {0});
        tables[EMPLOYEE].addLink("requiresMyApproval", tables[POINTSAWARDLEVEL], "requiredApproval", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("addOnProduct", tables[PRODUCT], "addOnProductEmployeeAwards", new int[] {37});
        tables[EMPLOYEEAWARD].addLink("awardCardOrders", tables[AWARDCARDORDER], "employeeAward", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("awardType", tables[AWARDTYPE], "employeeAwards", new int[] {38});
        tables[EMPLOYEEAWARD].addLink("confirmEmail", tables[EMAIL], "employeeAwardConfirm", new int[] {39});
        tables[EMPLOYEEAWARD].addLink("employee", tables[EMPLOYEE], "employeeAwards", new int[] {40});
        tables[EMPLOYEEAWARD].addLink("employeeAwardCharities", tables[EMPLOYEEAWARDCHARITY], "employeeAward", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("hindaOrder", tables[HINDAORDER], "employeeAward", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("lineItemForEmpAwardCash", tables[LINEITEM], "employeeAwardCash", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("lineItemForEmpAwardIntlVisa", tables[LINEITEM], "employeeAwardIntlVisa", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("lineItems", tables[LINEITEM], "employeeAward", new int[] {0});
        tables[EMPLOYEEAWARD].addLink("managerNotifyEmail", tables[EMAIL], "employeeAwardManagerNotify", new int[] {41});
        tables[EMPLOYEEAWARD].addLink("notifyEmail", tables[EMAIL], "employeeAwardNotify", new int[] {42});
        tables[EMPLOYEEAWARD].addLink("product", tables[PRODUCT], "employeeAwards", new int[] {43});
        tables[EMPLOYEEAWARD].addLink("shipTo", tables[SHIPTO], "employeeAward", new int[] {44});
        tables[EMPLOYEEAWARDCHARITY].addLink("charity", tables[CHARITY], "employeeAwardCharities", new int[] {7});
        tables[EMPLOYEEAWARDCHARITY].addLink("employeeAward", tables[EMPLOYEEAWARD], "employeeAwardCharities", new int[] {8});
        tables[EMPLOYEEAWARDCHARITY].addLink("lineItem", tables[LINEITEM], "employeeAwardCharity", new int[] {0});
        tables[EMPLOYEECUSTOMDATA].addLink("customData", tables[CUSTOMDATA], "employeeCustomDatas", new int[] {2});
        tables[EMPLOYEECUSTOMDATA].addLink("employee", tables[EMPLOYEE], "employeeCustomDatas", new int[] {3});
        tables[EMPLOYEEECARD].addLink("confirmedEmail", tables[EMAIL], "employeeEcardConfirmed", new int[] {6});
        tables[EMPLOYEEECARD].addLink("deliveredEmail", tables[EMAIL], "employeeEcardDelivered", new int[] {7});
        tables[EMPLOYEEECARD].addLink("ecard", tables[ECARD], "employeeEcards", new int[] {8});
        tables[EMPLOYEEECARD].addLink("employee", tables[EMPLOYEE], "employeeEcards", new int[] {9});
        tables[EMPLOYEEECARD].addLink("employeeEcardTos", tables[EMPLOYEEECARDTO], "employeeEcard", new int[] {0});
        tables[EMPLOYEEECARDTO].addLink("email", tables[EMAIL], "employeeEcardTo", new int[] {4});
        tables[EMPLOYEEECARDTO].addLink("employeeEcard", tables[EMPLOYEEECARD], "employeeEcardTos", new int[] {5});
        tables[EMPLOYEEECARDTO].addLink("lineItem", tables[LINEITEM], "employeeEcardTo", new int[] {0});
        tables[EMPLOYEEECARDTO].addLink("toEmployee", tables[EMPLOYEE], "employeeEcardRecipients", new int[] {6});
        tables[EMPLOYEESURVEY].addLink("employee", tables[EMPLOYEE], "employeeSurveys", new int[] {5});
        tables[EMPLOYEESURVEY].addLink("employeeSurveyQuestions", tables[EMPLOYEESURVEYQUESTION], "employeeSurvey", new int[] {0});
        tables[EMPLOYEESURVEY].addLink("survey", tables[SURVEY], "employeeSurveys", new int[] {6});
        tables[EMPLOYEESURVEYQUESTION].addLink("employeeSurvey", tables[EMPLOYEESURVEY], "employeeSurveyQuestions", new int[] {2});
        tables[EMPLOYEESURVEYQUESTION].addLink("surveyAnswer", tables[SURVEYANSWER], "employeeSurveyQuestions", new int[] {3});
        tables[EMPLOYEESURVEYQUESTION].addLink("surveyQuestion", tables[SURVEYQUESTION], "employeeSurveyQuestions", new int[] {4});
        tables[EMPLOYEETYPE].addLink("employees", tables[EMPLOYEE], "employeeType", new int[] {0});
        tables[FAMILYMEMBER].addLink("employee", tables[EMPLOYEE], "familyMembers", new int[] {6});
        tables[HINDAORDER].addLink("employeeAward", tables[EMPLOYEEAWARD], "hindaOrder", new int[] {4});
        tables[HINDAORDER].addLink("hindaOrderLines", tables[HINDAORDERLINE], "hindaOrder", new int[] {0});
        tables[HINDAORDER].addLink("inspireOrder", tables[INSPIREORDER], "hindaOrders", new int[] {5});
        tables[HINDAORDERLINE].addLink("hindaOrder", tables[HINDAORDER], "hindaOrderLines", new int[] {12});
        tables[IMAGESTORE].addLink("awardType", tables[AWARDTYPE], "ceoImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("awardTypes", tables[AWARDTYPE], "ceoSignatureImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("card", tables[CARD], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("ceoLocation", tables[LOCATION], "ceoImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("ceoProgram", tables[PROGRAM], "ceoImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("ceoSignatureLocation", tables[LOCATION], "ceoSignatureImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("ceoSignatureProgram", tables[PROGRAM], "ceoSignatureImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("charity", tables[CHARITY], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("ecard", tables[ECARD], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("item", tables[ITEM], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("locationLogo", tables[LOCATION], "logoImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("locationPageInfo", tables[LOCATIONPAGEINFO], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("loginImage", tables[LOGINIMAGE], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("logoStampLocation", tables[LOCATION], "logoStampImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("merchant", tables[MERCHANT], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("pageGroupPageInfos", tables[PAGEGROUPPAGEINFO], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("pageThemePageInfo", tables[PAGETHEMEPAGEINFO], "imageStore", new int[] {0});
        tables[IMAGESTORE].addLink("programLogoStamp", tables[PROGRAM], "logoStampImageStore", new int[] {0});
        tables[IMAGESTORE].addLink("programPageInfo", tables[PROGRAMPAGEINFO], "imageStore", new int[] {0});
        tables[INSPIRE].addLink("email", tables[EMAIL], "inspire", new int[] {4});
        tables[INSPIRE].addLink("employee", tables[EMPLOYEE], "inspires", new int[] {5});
        tables[INSPIRE].addLink("inspireAwardLevel", tables[INSPIREAWARDLEVEL], "inspires", new int[] {6});
        tables[INSPIRE].addLink("inspireCoreValue", tables[INSPIRECOREVALUE], "inspires", new int[] {7});
        tables[INSPIRE].addLink("inspireRecipients", tables[INSPIRERECIPIENT], "inspire", new int[] {0});
        tables[INSPIREAPPROVAL].addLink("email", tables[EMAIL], "inspireApproval", new int[] {5});
        tables[INSPIREAPPROVAL].addLink("employee", tables[EMPLOYEE], "inspireApprovals", new int[] {6});
        tables[INSPIREAPPROVAL].addLink("inspireAwardLevel", tables[INSPIREAWARDLEVEL], "inspireApprovals", new int[] {7});
        tables[INSPIREAPPROVAL].addLink("inspireRecipient", tables[INSPIRERECIPIENT], "inspireApprovals", new int[] {8});
        tables[INSPIREAWARDLEVEL].addLink("inspireApprovals", tables[INSPIREAPPROVAL], "inspireAwardLevel", new int[] {0});
        tables[INSPIREAWARDLEVEL].addLink("inspireAwardLevelLocationValues", tables[INSPIREAWARDLEVELLOCATIONVALUE], "inspireAwardLevel", new int[] {0});
        tables[INSPIREAWARDLEVEL].addLink("inspires", tables[INSPIRE], "inspireAwardLevel", new int[] {0});
        tables[INSPIREAWARDLEVEL].addLink("program", tables[PROGRAM], "inspireAwardLevels", new int[] {5});
        tables[INSPIREAWARDLEVELLOCATIONVALUE].addLink("inspireAwardLevel", tables[INSPIREAWARDLEVEL], "inspireAwardLevelLocationValues", new int[] {2});
        tables[INSPIREAWARDLEVELLOCATIONVALUE].addLink("inspireRecipients", tables[INSPIRERECIPIENT], "inspireAwardLevelLocationValue", new int[] {0});
        tables[INSPIREAWARDLEVELLOCATIONVALUE].addLink("location", tables[LOCATION], "inspireAwardLevelLocationValues", new int[] {3});
        tables[INSPIRECOREVALUE].addLink("inspires", tables[INSPIRE], "inspireCoreValue", new int[] {0});
        tables[INSPIRECOREVALUE].addLink("program", tables[PROGRAM], "inspireCoreValues", new int[] {3});
        tables[INSPIREORDER].addLink("awardCardOrders", tables[AWARDCARDORDER], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("employee", tables[EMPLOYEE], "inspireOrders", new int[] {19});
        tables[INSPIREORDER].addLink("hindaOrders", tables[HINDAORDER], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("inspireOrderCharities", tables[INSPIREORDERCHARITY], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("inspireOrderItems", tables[INSPIREORDERITEM], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("lineItemForInspireCash", tables[LINEITEM], "inspireOrderCash", new int[] {0});
        tables[INSPIREORDER].addLink("lineItemForIntlVisa", tables[LINEITEM], "inspireOrderIntlVisa", new int[] {0});
        tables[INSPIREORDER].addLink("lineItems", tables[LINEITEM], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("pointsRecord", tables[POINTSRECORD], "inspireOrder", new int[] {0});
        tables[INSPIREORDER].addLink("shipTo", tables[SHIPTO], "inspireOrder", new int[] {20});
        tables[INSPIREORDERCHARITY].addLink("charity", tables[CHARITY], "inspireOrderCharities", new int[] {8});
        tables[INSPIREORDERCHARITY].addLink("inspireOrder", tables[INSPIREORDER], "inspireOrderCharities", new int[] {9});
        tables[INSPIREORDERCHARITY].addLink("lineItem", tables[LINEITEM], "inspireOrderCharity", new int[] {0});
        tables[INSPIREORDERITEM].addLink("inspireOrder", tables[INSPIREORDER], "inspireOrderItems", new int[] {15});
        tables[INSPIREORDERITEM].addLink("lineItem", tables[LINEITEM], "inspireOrderItem", new int[] {0});
        tables[INSPIREORDERITEM].addLink("product", tables[PRODUCT], "inspireOrderItems", new int[] {16});
        tables[INSPIRERECIPIENT].addLink("completedEmail", tables[EMAIL], "inspireRecipientCompleted", new int[] {3});
        tables[INSPIRERECIPIENT].addLink("email", tables[EMAIL], "inspireRecipient", new int[] {4});
        tables[INSPIRERECIPIENT].addLink("employee", tables[EMPLOYEE], "inspireRecipients", new int[] {5});
        tables[INSPIRERECIPIENT].addLink("inspire", tables[INSPIRE], "inspireRecipients", new int[] {6});
        tables[INSPIRERECIPIENT].addLink("inspireApprovals", tables[INSPIREAPPROVAL], "inspireRecipient", new int[] {0});
        tables[INSPIRERECIPIENT].addLink("inspireAwardLevelLocationValue", tables[INSPIREAWARDLEVELLOCATIONVALUE], "inspireRecipients", new int[] {7});
        tables[INSPIRERECIPIENT].addLink("lineItem", tables[LINEITEM], "inspireRecipient", new int[] {0});
        tables[ITEM].addLink("addOnItems", tables[ADDONITEM], "item", new int[] {0});
        tables[ITEM].addLink("imageStore", tables[IMAGESTORE], "item", new int[] {18});
        tables[ITEM].addLink("itemVendor", tables[ITEMVENDOR], "items", new int[] {19});
        tables[ITEM].addLink("products", tables[PRODUCT], "item", new int[] {0});
        tables[ITEMCATEGORY].addLink("itemCategories", tables[ITEMCATEGORY], "parentItemCategory", new int[] {0});
        tables[ITEMCATEGORY].addLink("parentItemCategory", tables[ITEMCATEGORY], "itemCategories", new int[] {7});
        tables[ITEMVENDOR].addLink("items", tables[ITEM], "itemVendor", new int[] {0});
        tables[LINEITEM].addLink("awardCardOrder", tables[AWARDCARDORDER], "lineItems", new int[] {12});
        tables[LINEITEM].addLink("email", tables[EMAIL], "lineItem", new int[] {13});
        tables[LINEITEM].addLink("employee", tables[EMPLOYEE], "lineItems", new int[] {14});
        tables[LINEITEM].addLink("employeeAward", tables[EMPLOYEEAWARD], "lineItems", new int[] {15});
        tables[LINEITEM].addLink("employeeAwardCash", tables[EMPLOYEEAWARD], "lineItemForEmpAwardCash", new int[] {16});
        tables[LINEITEM].addLink("employeeAwardCharity", tables[EMPLOYEEAWARDCHARITY], "lineItem", new int[] {17});
        tables[LINEITEM].addLink("employeeAwardIntlVisa", tables[EMPLOYEEAWARD], "lineItemForEmpAwardIntlVisa", new int[] {18});
        tables[LINEITEM].addLink("employeeEcardTo", tables[EMPLOYEEECARDTO], "lineItem", new int[] {19});
        tables[LINEITEM].addLink("inspireOrder", tables[INSPIREORDER], "lineItems", new int[] {20});
        tables[LINEITEM].addLink("inspireOrderCash", tables[INSPIREORDER], "lineItemForInspireCash", new int[] {21});
        tables[LINEITEM].addLink("inspireOrderCharity", tables[INSPIREORDERCHARITY], "lineItem", new int[] {22});
        tables[LINEITEM].addLink("inspireOrderIntlVisa", tables[INSPIREORDER], "lineItemForIntlVisa", new int[] {23});
        tables[LINEITEM].addLink("inspireOrderItem", tables[INSPIREORDERITEM], "lineItem", new int[] {24});
        tables[LINEITEM].addLink("inspireRecipient", tables[INSPIRERECIPIENT], "lineItem", new int[] {25});
        tables[LINEITEM].addLink("location", tables[LOCATION], "lineItems", new int[] {26});
        tables[LINEITEM].addLink("pointsRecord", tables[POINTSRECORD], "lineItems", new int[] {27});
        tables[LINEITEM].addLink("pointsRequest", tables[POINTSREQUEST], "lineItems", new int[] {28});
        tables[LINEITEM].addLink("program", tables[PROGRAM], "lineItems", new int[] {29});
        tables[LOCATION].addLink("addOnItems", tables[ADDONITEM], "location", new int[] {0});
        tables[LOCATION].addLink("address", tables[ADDRESS], "location", new int[] {14});
        tables[LOCATION].addLink("announcementDocument", tables[PROGRAMDOCUMENT], "announcementLocation", new int[] {15});
        tables[LOCATION].addLink("awardTypes", tables[AWARDTYPE], "location", new int[] {0});
        tables[LOCATION].addLink("ceoImageStore", tables[IMAGESTORE], "ceoLocation", new int[] {16});
        tables[LOCATION].addLink("ceoSignatureImageStore", tables[IMAGESTORE], "ceoSignatureLocation", new int[] {17});
        tables[LOCATION].addLink("countryCode", tables[COUNTRYCODE], "locations", new int[] {18});
        tables[LOCATION].addLink("employees", tables[EMPLOYEE], "location", new int[] {0});
        tables[LOCATION].addLink("imagineCard", tables[CARD], "imageLocations", new int[] {19});
        tables[LOCATION].addLink("inspireAwardLevelLocationValues", tables[INSPIREAWARDLEVELLOCATIONVALUE], "location", new int[] {0});
        tables[LOCATION].addLink("inspireAwardType", tables[AWARDTYPE], "inspireLocation", new int[] {20});
        tables[LOCATION].addLink("lineItems", tables[LINEITEM], "location", new int[] {0});
        tables[LOCATION].addLink("locationPageGroups", tables[LOCATIONPAGEGROUP], "location", new int[] {0});
        tables[LOCATION].addLink("locationPageInfos", tables[LOCATIONPAGEINFO], "location", new int[] {0});
        tables[LOCATION].addLink("locations", tables[LOCATION], "parentLocation", new int[] {0});
        tables[LOCATION].addLink("locationType", tables[LOCATIONTYPE], "locations", new int[] {21});
        tables[LOCATION].addLink("logoImageStore", tables[IMAGESTORE], "locationLogo", new int[] {22});
        tables[LOCATION].addLink("logoStampImageStore", tables[IMAGESTORE], "logoStampLocation", new int[] {23});
        tables[LOCATION].addLink("nominationQuiz", tables[QUIZ], "location", new int[] {0});
        tables[LOCATION].addLink("pageTheme", tables[PAGETHEME], "locations", new int[] {24});
        tables[LOCATION].addLink("parentLocation", tables[LOCATION], "locations", new int[] {25});
        tables[LOCATION].addLink("pointsAwardLevels", tables[POINTSAWARDLEVEL], "location", new int[] {0});
        tables[LOCATION].addLink("pointsCoreValues", tables[POINTSCOREVALUE], "location", new int[] {0});
        tables[LOCATION].addLink("program", tables[PROGRAM], "locations", new int[] {26});
        tables[LOCATION].addLink("programEmailTypes", tables[PROGRAMEMAILTYPE], "location", new int[] {0});
        tables[LOCATION].addLink("programEvents", tables[PROGRAMEVENT], "location", new int[] {0});
        tables[LOCATIONPAGEGROUP].addLink("location", tables[LOCATION], "locationPageGroups", new int[] {3});
        tables[LOCATIONPAGEGROUP].addLink("pageGroup", tables[PAGEGROUP], "locationPageGroups", new int[] {4});
        tables[LOCATIONPAGEINFO].addLink("imageStore", tables[IMAGESTORE], "locationPageInfo", new int[] {2});
        tables[LOCATIONPAGEINFO].addLink("location", tables[LOCATION], "locationPageInfos", new int[] {3});
        tables[LOCATIONPAGEINFO].addLink("pageInfo", tables[PAGEINFO], "locationPageInfos", new int[] {4});
        tables[LOCATIONPAGEINFO].addLink("programDocument", tables[PROGRAMDOCUMENT], "locationPageInfo", new int[] {5});
        tables[LOCATIONTYPE].addLink("company", tables[COMPANY], "locationTypes", new int[] {3});
        tables[LOCATIONTYPE].addLink("locations", tables[LOCATION], "locationType", new int[] {0});
        tables[LOGINIMAGE].addLink("imageStore", tables[IMAGESTORE], "loginImage", new int[] {4});
        tables[LOGINIMAGE].addLink("loginImageSet", tables[LOGINIMAGESET], "loginImages", new int[] {5});
        tables[LOGINIMAGESET].addLink("loginImages", tables[LOGINIMAGE], "loginImageSet", new int[] {0});
        tables[LOGINIMAGESET].addLink("programs", tables[PROGRAM], "loginImageSet", new int[] {0});
        tables[LOT].addLink("activatingEmployee", tables[EMPLOYEE], "activatedLots", new int[] {10});
        tables[LOT].addLink("codes", tables[CODE], "lot", new int[] {0});
        tables[LOT].addLink("pointsRequest", tables[POINTSREQUEST], "lot", new int[] {11});
        tables[MERCHANT].addLink("imageStore", tables[IMAGESTORE], "merchant", new int[] {5});
        tables[MERCHANTCATEGORY].addLink("merchantCategories", tables[MERCHANTCATEGORY], "parentMerchantCategory", new int[] {0});
        tables[MERCHANTCATEGORY].addLink("parentMerchantCategory", tables[MERCHANTCATEGORY], "merchantCategories", new int[] {3});
        tables[ORDERITEMTRACKING].addLink("orderTracking", tables[ORDERTRACKING], "orderItemTrackings", new int[] {10});
        tables[ORDERTRACKING].addLink("orderItemTrackings", tables[ORDERITEMTRACKING], "orderTracking", new int[] {0});
        tables[ORDERTRACKING].addLink("orderTrackingStatuses", tables[ORDERTRACKINGSTATUS], "orderTracking", new int[] {0});
        tables[ORDERTRACKINGSTATUS].addLink("orderTracking", tables[ORDERTRACKING], "orderTrackingStatuses", new int[] {7});
        tables[PAGE].addLink("pageInfos", tables[PAGEINFO], "page", new int[] {0});
        tables[PAGEGROUP].addLink("locationPageGroups", tables[LOCATIONPAGEGROUP], "pageGroup", new int[] {0});
        tables[PAGEGROUP].addLink("pageGroupPageInfos", tables[PAGEGROUPPAGEINFO], "pageGroup", new int[] {0});
        tables[PAGEGROUP].addLink("pageGroups", tables[PAGEGROUP], "parentPageGroup", new int[] {0});
        tables[PAGEGROUP].addLink("parentPageGroup", tables[PAGEGROUP], "pageGroups", new int[] {4});
        tables[PAGEGROUP].addLink("programPageGroups", tables[PROGRAMPAGEGROUP], "pageGroup", new int[] {0});
        tables[PAGEGROUPPAGEINFO].addLink("imageStore", tables[IMAGESTORE], "pageGroupPageInfos", new int[] {2});
        tables[PAGEGROUPPAGEINFO].addLink("pageGroup", tables[PAGEGROUP], "pageGroupPageInfos", new int[] {3});
        tables[PAGEGROUPPAGEINFO].addLink("pageInfo", tables[PAGEINFO], "pageGroupPageInfos", new int[] {4});
        tables[PAGEGROUPPAGEINFO].addLink("programDocument", tables[PROGRAMDOCUMENT], "pageGroupPageInfos", new int[] {5});
        tables[PAGEINFO].addLink("locationPageInfos", tables[LOCATIONPAGEINFO], "pageInfo", new int[] {0});
        tables[PAGEINFO].addLink("page", tables[PAGE], "pageInfos", new int[] {4});
        tables[PAGEINFO].addLink("pageGroupPageInfos", tables[PAGEGROUPPAGEINFO], "pageInfo", new int[] {0});
        tables[PAGEINFO].addLink("pageThemePageInfos", tables[PAGETHEMEPAGEINFO], "pageInfo", new int[] {0});
        tables[PAGEINFO].addLink("programPageInfos", tables[PROGRAMPAGEINFO], "pageInfo", new int[] {0});
        tables[PAGETHEME].addLink("locations", tables[LOCATION], "pageTheme", new int[] {0});
        tables[PAGETHEME].addLink("pageThemePageInfos", tables[PAGETHEMEPAGEINFO], "pageTheme", new int[] {0});
        tables[PAGETHEME].addLink("programs", tables[PROGRAM], "pageTheme", new int[] {0});
        tables[PAGETHEMEPAGEINFO].addLink("imageStore", tables[IMAGESTORE], "pageThemePageInfo", new int[] {2});
        tables[PAGETHEMEPAGEINFO].addLink("pageInfo", tables[PAGEINFO], "pageThemePageInfos", new int[] {3});
        tables[PAGETHEMEPAGEINFO].addLink("pageTheme", tables[PAGETHEME], "pageThemePageInfos", new int[] {4});
        tables[PAGETHEMEPAGEINFO].addLink("programDocument", tables[PROGRAMDOCUMENT], "pageThemePageInfo", new int[] {5});
        tables[PHONE].addLink("phoneType", tables[PHONETYPE], "phones", new int[] {4});
        tables[PHONETYPE].addLink("phones", tables[PHONE], "phoneType", new int[] {0});
        tables[POINTSAPPROVAL].addLink("approvedAwardLevel", tables[POINTSAWARDLEVEL], "approvedApprovals", new int[] {6});
        tables[POINTSAPPROVAL].addLink("employee", tables[EMPLOYEE], "pointsApprovals", new int[] {7});
        tables[POINTSAPPROVAL].addLink("pointsRequest", tables[POINTSREQUEST], "pointsApprovals", new int[] {8});
        tables[POINTSAPPROVAL].addLink("startingAwardLevel", tables[POINTSAWARDLEVEL], "startingApprovals", new int[] {9});
        tables[POINTSAWARDLEVEL].addLink("approvedApprovals", tables[POINTSAPPROVAL], "approvedAwardLevel", new int[] {0});
        tables[POINTSAWARDLEVEL].addLink("location", tables[LOCATION], "pointsAwardLevels", new int[] {10});
        tables[POINTSAWARDLEVEL].addLink("pointsRequests", tables[POINTSREQUEST], "pointsAwardLevel", new int[] {0});
        tables[POINTSAWARDLEVEL].addLink("program", tables[PROGRAM], "pointsAwardLevels", new int[] {11});
        tables[POINTSAWARDLEVEL].addLink("requiredApproval", tables[EMPLOYEE], "requiresMyApproval", new int[] {12});
        tables[POINTSAWARDLEVEL].addLink("startingApprovals", tables[POINTSAPPROVAL], "startingAwardLevel", new int[] {0});
        tables[POINTSCONFIGURATION].addLink("program", tables[PROGRAM], "pointsConfiguration", new int[] {0});
        tables[POINTSCOREVALUE].addLink("location", tables[LOCATION], "pointsCoreValues", new int[] {4});
        tables[POINTSCOREVALUE].addLink("pointsRecords", tables[POINTSRECORD], "pointsCoreValue", new int[] {0});
        tables[POINTSCOREVALUE].addLink("program", tables[PROGRAM], "pointsCoreValues", new int[] {5});
        tables[POINTSRECORD].addLink("code", tables[CODE], "pointsRecord", new int[] {0});
        tables[POINTSRECORD].addLink("email", tables[EMAIL], "pointsRecord", new int[] {11});
        tables[POINTSRECORD].addLink("inspireOrder", tables[INSPIREORDER], "pointsRecord", new int[] {12});
        tables[POINTSRECORD].addLink("lineItems", tables[LINEITEM], "pointsRecord", new int[] {0});
        tables[POINTSRECORD].addLink("pointsCoreValue", tables[POINTSCOREVALUE], "pointsRecords", new int[] {13});
        tables[POINTSRECORD].addLink("pointsRequest", tables[POINTSREQUEST], "pointsRecords", new int[] {14});
        tables[POINTSRECORD].addLink("pointsToEmployee", tables[EMPLOYEE], "pointsFromRecord", new int[] {15});
        tables[POINTSRECORD].addLink("pointsToProgram", tables[PROGRAM], "pointsFromRecord", new int[] {16});
        tables[POINTSREQUEST].addLink("approvingEmployee", tables[EMPLOYEE], "approvedPointsRequests", new int[] {8});
        tables[POINTSREQUEST].addLink("approvingUser", tables[USER], "approvedPointsRequests", new int[] {9});
        tables[POINTSREQUEST].addLink("lineItems", tables[LINEITEM], "pointsRequest", new int[] {0});
        tables[POINTSREQUEST].addLink("lot", tables[LOT], "pointsRequest", new int[] {0});
        tables[POINTSREQUEST].addLink("pointsApprovals", tables[POINTSAPPROVAL], "pointsRequest", new int[] {0});
        tables[POINTSREQUEST].addLink("pointsAwardLevel", tables[POINTSAWARDLEVEL], "pointsRequests", new int[] {10});
        tables[POINTSREQUEST].addLink("pointsRecords", tables[POINTSRECORD], "pointsRequest", new int[] {0});
        tables[POINTSREQUEST].addLink("quizResult", tables[QUIZRESULT], "pointsRequest", new int[] {11});
        tables[POINTSREQUEST].addLink("requestingEmployee", tables[EMPLOYEE], "pointsRequest", new int[] {12});
        tables[PRODUCT].addLink("addOnProductEmployeeAwards", tables[EMPLOYEEAWARD], "addOnProduct", new int[] {0});
        tables[PRODUCT].addLink("employeeAwards", tables[EMPLOYEEAWARD], "product", new int[] {0});
        tables[PRODUCT].addLink("inspireOrderItems", tables[INSPIREORDERITEM], "product", new int[] {0});
        tables[PRODUCT].addLink("item", tables[ITEM], "products", new int[] {12});
        tables[PRODUCT].addLink("productAudits", tables[PRODUCTAUDIT], "product", new int[] {0});
        tables[PRODUCTAUDIT].addLink("product", tables[PRODUCT], "productAudits", new int[] {5});
        tables[PROGRAM].addLink("aboutMeQuiz", tables[QUIZ], "program2", new int[] {0});
        tables[PROGRAM].addLink("announcementDocument", tables[PROGRAMDOCUMENT], "announcementProgram", new int[] {52});
        tables[PROGRAM].addLink("awardTypes", tables[AWARDTYPE], "program", new int[] {0});
        tables[PROGRAM].addLink("ceoImageStore", tables[IMAGESTORE], "ceoProgram", new int[] {53});
        tables[PROGRAM].addLink("ceoSignatureImageStore", tables[IMAGESTORE], "ceoSignatureProgram", new int[] {54});
        tables[PROGRAM].addLink("company", tables[COMPANY], "programs", new int[] {55});
        tables[PROGRAM].addLink("countryCode", tables[COUNTRYCODE], "programs", new int[] {56});
        tables[PROGRAM].addLink("customData", tables[CUSTOMDATA], "program", new int[] {0});
        tables[PROGRAM].addLink("imagineCard", tables[CARD], "imaginePrograms", new int[] {57});
        tables[PROGRAM].addLink("inspireAwardLevels", tables[INSPIREAWARDLEVEL], "program", new int[] {0});
        tables[PROGRAM].addLink("inspireAwardType", tables[AWARDTYPE], "inspireProgram", new int[] {58});
        tables[PROGRAM].addLink("inspireCoreValues", tables[INSPIRECOREVALUE], "program", new int[] {0});
        tables[PROGRAM].addLink("lineItems", tables[LINEITEM], "program", new int[] {0});
        tables[PROGRAM].addLink("locations", tables[LOCATION], "program", new int[] {0});
        tables[PROGRAM].addLink("loginImageSet", tables[LOGINIMAGESET], "programs", new int[] {59});
        tables[PROGRAM].addLink("logoStampImageStore", tables[IMAGESTORE], "programLogoStamp", new int[] {60});
        tables[PROGRAM].addLink("managerHifiveSurvey", tables[SURVEY], "managerSurveyProgram", new int[] {61});
        tables[PROGRAM].addLink("nominationQuiz", tables[QUIZ], "program", new int[] {0});
        tables[PROGRAM].addLink("pageTheme", tables[PAGETHEME], "programs", new int[] {62});
        tables[PROGRAM].addLink("pointsAwardLevels", tables[POINTSAWARDLEVEL], "program", new int[] {0});
        tables[PROGRAM].addLink("pointsConfiguration", tables[POINTSCONFIGURATION], "program", new int[] {63});
        tables[PROGRAM].addLink("pointsCoreValues", tables[POINTSCOREVALUE], "program", new int[] {0});
        tables[PROGRAM].addLink("pointsFromRecord", tables[POINTSRECORD], "pointsToProgram", new int[] {0});
        tables[PROGRAM].addLink("programEmailTypes", tables[PROGRAMEMAILTYPE], "program", new int[] {0});
        tables[PROGRAM].addLink("programEvents", tables[PROGRAMEVENT], "program", new int[] {0});
        tables[PROGRAM].addLink("programFaqs", tables[PROGRAMFAQ], "program", new int[] {0});
        tables[PROGRAM].addLink("programPageGroups", tables[PROGRAMPAGEGROUP], "program", new int[] {0});
        tables[PROGRAM].addLink("programPageInfos", tables[PROGRAMPAGEINFO], "program", new int[] {0});
        tables[PROGRAM].addLink("quizSurveys", tables[SURVEY], "quizProgram", new int[] {0});
        tables[PROGRAM].addLink("surveys", tables[SURVEY], "program", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("announcementLocation", tables[LOCATION], "announcementDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("announcementProgram", tables[PROGRAM], "announcementDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("awardTypes", tables[AWARDTYPE], "announcementDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("locationPageInfo", tables[LOCATIONPAGEINFO], "programDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("pageGroupPageInfos", tables[PAGEGROUPPAGEINFO], "programDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("pageThemePageInfo", tables[PAGETHEMEPAGEINFO], "programDocument", new int[] {0});
        tables[PROGRAMDOCUMENT].addLink("programPageInfo", tables[PROGRAMPAGEINFO], "programDocument", new int[] {0});
        tables[PROGRAMEMAILTYPE].addLink("emails", tables[EMAIL], "programEmailType", new int[] {0});
        tables[PROGRAMEMAILTYPE].addLink("emailType", tables[EMAILTYPE], "programEmailTypes", new int[] {5});
        tables[PROGRAMEMAILTYPE].addLink("location", tables[LOCATION], "programEmailTypes", new int[] {6});
        tables[PROGRAMEMAILTYPE].addLink("program", tables[PROGRAM], "programEmailTypes", new int[] {7});
        tables[PROGRAMEVENT].addLink("location", tables[LOCATION], "programEvents", new int[] {6});
        tables[PROGRAMEVENT].addLink("program", tables[PROGRAM], "programEvents", new int[] {7});
        tables[PROGRAMFAQ].addLink("program", tables[PROGRAM], "programFaqs", new int[] {6});
        tables[PROGRAMPAGEGROUP].addLink("pageGroup", tables[PAGEGROUP], "programPageGroups", new int[] {3});
        tables[PROGRAMPAGEGROUP].addLink("program", tables[PROGRAM], "programPageGroups", new int[] {4});
        tables[PROGRAMPAGEINFO].addLink("imageStore", tables[IMAGESTORE], "programPageInfo", new int[] {2});
        tables[PROGRAMPAGEINFO].addLink("pageInfo", tables[PAGEINFO], "programPageInfos", new int[] {3});
        tables[PROGRAMPAGEINFO].addLink("program", tables[PROGRAM], "programPageInfos", new int[] {4});
        tables[PROGRAMPAGEINFO].addLink("programDocument", tables[PROGRAMDOCUMENT], "programPageInfo", new int[] {5});
        tables[QUESTION].addLink("answers", tables[ANSWER], "question", new int[] {0});
        tables[QUESTION].addLink("questionResults", tables[QUESTIONRESULT], "question", new int[] {0});
        tables[QUESTION].addLink("quiz", tables[QUIZ], "questions", new int[] {2});
        tables[QUESTIONRESULT].addLink("answerResults", tables[ANSWERRESULT], "questionResult", new int[] {0});
        tables[QUESTIONRESULT].addLink("question", tables[QUESTION], "questionResults", new int[] {2});
        tables[QUESTIONRESULT].addLink("quizResult", tables[QUIZRESULT], "questionResults", new int[] {3});
        tables[QUIZ].addLink("location", tables[LOCATION], "nominationQuiz", new int[] {2});
        tables[QUIZ].addLink("program", tables[PROGRAM], "nominationQuiz", new int[] {3});
        tables[QUIZ].addLink("program2", tables[PROGRAM], "aboutMeQuiz", new int[] {4});
        tables[QUIZ].addLink("questions", tables[QUESTION], "quiz", new int[] {0});
        tables[QUIZ].addLink("quizResults", tables[QUIZRESULT], "quiz", new int[] {0});
        tables[QUIZRESULT].addLink("employee", tables[EMPLOYEE], "quizResults", new int[] {2});
        tables[QUIZRESULT].addLink("pointsRequest", tables[POINTSREQUEST], "quizResult", new int[] {0});
        tables[QUIZRESULT].addLink("questionResults", tables[QUESTIONRESULT], "quizResult", new int[] {0});
        tables[QUIZRESULT].addLink("quiz", tables[QUIZ], "quizResults", new int[] {3});
        tables[REPORT].addLink("parentReport", tables[REPORT], "reports", new int[] {6});
        tables[REPORT].addLink("reportClass", tables[REPORTCLASS], "reports", new int[] {7});
        tables[REPORT].addLink("reports", tables[REPORT], "parentReport", new int[] {0});
        tables[REPORTCLASS].addLink("reports", tables[REPORT], "reportClass", new int[] {0});
        tables[SECTION].addLink("awardTypes", tables[AWARDTYPE], "section", new int[] {0});
        tables[SECTION].addLink("catalog", tables[CATALOG], "sections", new int[] {3});
        tables[SECTION].addLink("parentSection", tables[SECTION], "sections", new int[] {4});
        tables[SECTION].addLink("sections", tables[SECTION], "parentSection", new int[] {0});
        tables[SHIPTO].addLink("address", tables[ADDRESS], "shipTos", new int[] {6});
        tables[SHIPTO].addLink("employeeAward", tables[EMPLOYEEAWARD], "shipTo", new int[] {0});
        tables[SHIPTO].addLink("inspireOrder", tables[INSPIREORDER], "shipTo", new int[] {0});
        tables[SURVEY].addLink("employeeSurveys", tables[EMPLOYEESURVEY], "survey", new int[] {0});
        tables[SURVEY].addLink("managerSurveyProgram", tables[PROGRAM], "managerHifiveSurvey", new int[] {0});
        tables[SURVEY].addLink("program", tables[PROGRAM], "surveys", new int[] {5});
        tables[SURVEY].addLink("quizProgram", tables[PROGRAM], "quizSurveys", new int[] {6});
        tables[SURVEY].addLink("surveyQuestions", tables[SURVEYQUESTION], "survey", new int[] {0});
        tables[SURVEYANSWER].addLink("employeeSurveyQuestions", tables[EMPLOYEESURVEYQUESTION], "surveyAnswer", new int[] {0});
        tables[SURVEYANSWER].addLink("surveyAnswerQuestion", tables[SURVEYQUESTION], "correctSurveyAnswer", new int[] {2});
        tables[SURVEYANSWER].addLink("surveyQuestion", tables[SURVEYQUESTION], "surveyAnswers", new int[] {3});
        tables[SURVEYQUESTION].addLink("correctSurveyAnswer", tables[SURVEYANSWER], "surveyAnswerQuestion", new int[] {0});
        tables[SURVEYQUESTION].addLink("employeeSurveyQuestions", tables[EMPLOYEESURVEYQUESTION], "surveyQuestion", new int[] {0});
        tables[SURVEYQUESTION].addLink("survey", tables[SURVEY], "surveyQuestions", new int[] {5});
        tables[SURVEYQUESTION].addLink("surveyAnswers", tables[SURVEYANSWER], "surveyQuestion", new int[] {0});
        tables[USER].addLink("approvedPointsRequests", tables[POINTSREQUEST], "approvingUser", new int[] {0});
        
        // Links for Link Tables
        
        // AwardTypeIncludeItem LINKS
        tables[AWARDTYPEINCLUDEITEM].addLink("includeAwardTypes", tables[AWARDTYPE], "includeItems", new int[] {0});
        tables[AWARDTYPE].addLink("includeItems", tables[AWARDTYPEINCLUDEITEM], "includeAwardTypes", new int[] {0});
        tables[AWARDTYPEINCLUDEITEM].addLink("includeItems", tables[ITEM], "includeAwardTypes", new int[] {1});
        tables[ITEM].addLink("includeAwardTypes", tables[AWARDTYPEINCLUDEITEM], "includeItems", new int[] {0});
        
        // EmployeePhone LINKS
        tables[EMPLOYEEPHONE].addLink("employee", tables[EMPLOYEE], "phones", new int[] {0});
        tables[EMPLOYEE].addLink("phones", tables[EMPLOYEEPHONE], "employee", new int[] {0});
        tables[EMPLOYEEPHONE].addLink("phones", tables[PHONE], "employee", new int[] {1});
        tables[PHONE].addLink("employee", tables[EMPLOYEEPHONE], "phones", new int[] {0});
        
        // ProgramEcard LINKS
        tables[PROGRAMECARD].addLink("programs", tables[PROGRAM], "ecards", new int[] {0});
        tables[PROGRAM].addLink("ecards", tables[PROGRAMECARD], "programs", new int[] {0});
        tables[PROGRAMECARD].addLink("ecards", tables[ECARD], "programs", new int[] {1});
        tables[ECARD].addLink("programs", tables[PROGRAMECARD], "ecards", new int[] {0});
        
        // ProgramProgramDocument LINKS
        tables[PROGRAMPROGRAMDOCUMENT].addLink("blogPrograms", tables[PROGRAM], "blogDocuments", new int[] {0});
        tables[PROGRAM].addLink("blogDocuments", tables[PROGRAMPROGRAMDOCUMENT], "blogPrograms", new int[] {0});
        tables[PROGRAMPROGRAMDOCUMENT].addLink("blogDocuments", tables[PROGRAMDOCUMENT], "blogPrograms", new int[] {1});
        tables[PROGRAMDOCUMENT].addLink("blogPrograms", tables[PROGRAMPROGRAMDOCUMENT], "blogDocuments", new int[] {0});
        
        // MerchantCard LINKS
        tables[MERCHANTCARD].addLink("merchants", tables[MERCHANT], "cards", new int[] {0});
        tables[MERCHANT].addLink("cards", tables[MERCHANTCARD], "merchants", new int[] {0});
        tables[MERCHANTCARD].addLink("cards", tables[CARD], "merchants", new int[] {1});
        tables[CARD].addLink("merchants", tables[MERCHANTCARD], "cards", new int[] {0});
        
        // ProgramCard LINKS
        tables[PROGRAMCARD].addLink("programs", tables[PROGRAM], "cards", new int[] {0});
        tables[PROGRAM].addLink("cards", tables[PROGRAMCARD], "programs", new int[] {0});
        tables[PROGRAMCARD].addLink("cards", tables[CARD], "programs", new int[] {1});
        tables[CARD].addLink("programs", tables[PROGRAMCARD], "cards", new int[] {0});
        
        // ItemCategoryItem LINKS
        tables[ITEMCATEGORYITEM].addLink("itemCategories", tables[ITEMCATEGORY], "items", new int[] {0});
        tables[ITEMCATEGORY].addLink("items", tables[ITEMCATEGORYITEM], "itemCategories", new int[] {0});
        tables[ITEMCATEGORYITEM].addLink("items", tables[ITEM], "itemCategories", new int[] {1});
        tables[ITEM].addLink("itemCategories", tables[ITEMCATEGORYITEM], "items", new int[] {0});
        
        // SectionItem LINKS
        tables[SECTIONITEM].addLink("sections", tables[SECTION], "items", new int[] {0});
        tables[SECTION].addLink("items", tables[SECTIONITEM], "sections", new int[] {0});
        tables[SECTIONITEM].addLink("items", tables[ITEM], "sections", new int[] {1});
        tables[ITEM].addLink("sections", tables[SECTIONITEM], "items", new int[] {0});
        
        // AwardTypeExcludeItem LINKS
        tables[AWARDTYPEEXCLUDEITEM].addLink("excludeAwardTypes", tables[AWARDTYPE], "excludeItems", new int[] {0});
        tables[AWARDTYPE].addLink("excludeItems", tables[AWARDTYPEEXCLUDEITEM], "excludeAwardTypes", new int[] {0});
        tables[AWARDTYPEEXCLUDEITEM].addLink("excludeItems", tables[ITEM], "excludeAwardTypes", new int[] {1});
        tables[ITEM].addLink("excludeAwardTypes", tables[AWARDTYPEEXCLUDEITEM], "excludeItems", new int[] {0});
        
        // ProgramImageStore LINKS
        tables[PROGRAMIMAGESTORE].addLink("logoProgram", tables[PROGRAM], "logoImageStores", new int[] {0});
        tables[PROGRAM].addLink("logoImageStores", tables[PROGRAMIMAGESTORE], "logoProgram", new int[] {0});
        tables[PROGRAMIMAGESTORE].addLink("logoImageStores", tables[IMAGESTORE], "logoProgram", new int[] {1});
        tables[IMAGESTORE].addLink("logoProgram", tables[PROGRAMIMAGESTORE], "logoImageStores", new int[] {0});
        
        // ImageStoreProgram LINKS
        tables[IMAGESTOREPROGRAM].addLink("imageStores", tables[IMAGESTORE], "program", new int[] {0});
        tables[IMAGESTORE].addLink("program", tables[IMAGESTOREPROGRAM], "imageStores", new int[] {0});
        tables[IMAGESTOREPROGRAM].addLink("program", tables[PROGRAM], "imageStores", new int[] {1});
        tables[PROGRAM].addLink("imageStores", tables[IMAGESTOREPROGRAM], "program", new int[] {0});
        
        // MerchantCategoryMerchant LINKS
        tables[MERCHANTCATEGORYMERCHANT].addLink("merchantCategories", tables[MERCHANTCATEGORY], "merchants", new int[] {0});
        tables[MERCHANTCATEGORY].addLink("merchants", tables[MERCHANTCATEGORYMERCHANT], "merchantCategories", new int[] {0});
        tables[MERCHANTCATEGORYMERCHANT].addLink("merchants", tables[MERCHANT], "merchantCategories", new int[] {1});
        tables[MERCHANT].addLink("merchantCategories", tables[MERCHANTCATEGORYMERCHANT], "merchants", new int[] {0});
        
        // ProgramWidget LINKS
        tables[PROGRAMWIDGET].addLink("programs", tables[PROGRAM], "widgets", new int[] {0});
        tables[PROGRAM].addLink("widgets", tables[PROGRAMWIDGET], "programs", new int[] {0});
        tables[PROGRAMWIDGET].addLink("widgets", tables[WIDGET], "programs", new int[] {1});
        tables[WIDGET].addLink("programs", tables[PROGRAMWIDGET], "widgets", new int[] {0});
        
        // ReplaceItemItem LINKS
        tables[REPLACEITEMITEM].addLink("replaceItems", tables[ITEM], "replacesItems", new int[] {0});
        tables[ITEM].addLink("replacesItems", tables[REPLACEITEMITEM], "replaceItems", new int[] {0});
        tables[REPLACEITEMITEM].addLink("replacesItems", tables[ITEM], "replaceItems", new int[] {1});
        tables[ITEM].addLink("replaceItems", tables[REPLACEITEMITEM], "replacesItems", new int[] {0});
        
        // ItemTypeItem LINKS
        tables[ITEMTYPEITEM].addLink("itemTypes", tables[ITEMTYPE], "items", new int[] {0});
        tables[ITEMTYPE].addLink("items", tables[ITEMTYPEITEM], "itemTypes", new int[] {0});
        tables[ITEMTYPEITEM].addLink("items", tables[ITEM], "itemTypes", new int[] {1});
        tables[ITEM].addLink("itemTypes", tables[ITEMTYPEITEM], "items", new int[] {0});
        
        // LocationEcard LINKS
        tables[LOCATIONECARD].addLink("location", tables[LOCATION], "ecards", new int[] {0});
        tables[LOCATION].addLink("ecards", tables[LOCATIONECARD], "location", new int[] {0});
        tables[LOCATIONECARD].addLink("ecards", tables[ECARD], "location", new int[] {1});
        tables[ECARD].addLink("location", tables[LOCATIONECARD], "ecards", new int[] {0});
        
        // LocationProgramFaq LINKS
        tables[LOCATIONPROGRAMFAQ].addLink("location", tables[LOCATION], "programFaqs", new int[] {0});
        tables[LOCATION].addLink("programFaqs", tables[LOCATIONPROGRAMFAQ], "location", new int[] {0});
        tables[LOCATIONPROGRAMFAQ].addLink("programFaqs", tables[PROGRAMFAQ], "location", new int[] {1});
        tables[PROGRAMFAQ].addLink("location", tables[LOCATIONPROGRAMFAQ], "programFaqs", new int[] {0});
        
        // AwardTypeItemType LINKS
        tables[AWARDTYPEITEMTYPE].addLink("awardTypes", tables[AWARDTYPE], "itemTypes", new int[] {0});
        tables[AWARDTYPE].addLink("itemTypes", tables[AWARDTYPEITEMTYPE], "awardTypes", new int[] {0});
        tables[AWARDTYPEITEMTYPE].addLink("itemTypes", tables[ITEMTYPE], "awardTypes", new int[] {1});
        tables[ITEMTYPE].addLink("awardTypes", tables[AWARDTYPEITEMTYPE], "itemTypes", new int[] {0});
        
        // LocationCard LINKS
        tables[LOCATIONCARD].addLink("locations", tables[LOCATION], "cards", new int[] {0});
        tables[LOCATION].addLink("cards", tables[LOCATIONCARD], "locations", new int[] {0});
        tables[LOCATIONCARD].addLink("cards", tables[CARD], "locations", new int[] {1});
        tables[CARD].addLink("locations", tables[LOCATIONCARD], "cards", new int[] {0});
        
        // ProgramCharity LINKS
        tables[PROGRAMCHARITY].addLink("programs", tables[PROGRAM], "charities", new int[] {0});
        tables[PROGRAM].addLink("charities", tables[PROGRAMCHARITY], "programs", new int[] {0});
        tables[PROGRAMCHARITY].addLink("charities", tables[CHARITY], "programs", new int[] {1});
        tables[CHARITY].addLink("programs", tables[PROGRAMCHARITY], "charities", new int[] {0});
        
        // LocationCharity LINKS
        tables[LOCATIONCHARITY].addLink("locations", tables[LOCATION], "charities", new int[] {0});
        tables[LOCATION].addLink("charities", tables[LOCATIONCHARITY], "locations", new int[] {0});
        tables[LOCATIONCHARITY].addLink("charities", tables[CHARITY], "locations", new int[] {1});
        tables[CHARITY].addLink("locations", tables[LOCATIONCHARITY], "charities", new int[] {0});
        
        // LocationInspireCoreValue LINKS
        tables[LOCATIONINSPIRECOREVALUE].addLink("location", tables[LOCATION], "inspireCoreValues", new int[] {0});
        tables[LOCATION].addLink("inspireCoreValues", tables[LOCATIONINSPIRECOREVALUE], "location", new int[] {0});
        tables[LOCATIONINSPIRECOREVALUE].addLink("inspireCoreValues", tables[INSPIRECOREVALUE], "location", new int[] {1});
        tables[INSPIRECOREVALUE].addLink("location", tables[LOCATIONINSPIRECOREVALUE], "inspireCoreValues", new int[] {0});
        
        // CardValue LINKS
        tables[CARDVALUE].addLink("cards", tables[CARD], "values", new int[] {0});
        tables[CARD].addLink("values", tables[CARDVALUE], "cards", new int[] {0});
        tables[CARDVALUE].addLink("values", tables[VALUE], "cards", new int[] {1});
        tables[VALUE].addLink("cards", tables[CARDVALUE], "values", new int[] {0});
        
        // EmailEmployeeAward LINKS
        tables[EMAILEMPLOYEEAWARD].addLink("shippedEmails", tables[EMAIL], "employeeAwardShipped", new int[] {0});
        tables[EMAIL].addLink("employeeAwardShipped", tables[EMAILEMPLOYEEAWARD], "shippedEmails", new int[] {0});
        tables[EMAILEMPLOYEEAWARD].addLink("employeeAwardShipped", tables[EMPLOYEEAWARD], "shippedEmails", new int[] {1});
        tables[EMPLOYEEAWARD].addLink("shippedEmails", tables[EMAILEMPLOYEEAWARD], "employeeAwardShipped", new int[] {0});
        
        // AddOnItemAwardType LINKS
        tables[ADDONITEMAWARDTYPE].addLink("addOnItems", tables[ADDONITEM], "awardType", new int[] {0});
        tables[ADDONITEM].addLink("awardType", tables[ADDONITEMAWARDTYPE], "addOnItems", new int[] {0});
        tables[ADDONITEMAWARDTYPE].addLink("awardType", tables[AWARDTYPE], "addOnItems", new int[] {1});
        tables[AWARDTYPE].addLink("addOnItems", tables[ADDONITEMAWARDTYPE], "awardType", new int[] {0});
        
        // ProgramAddOnItem LINKS
        tables[PROGRAMADDONITEM].addLink("program", tables[PROGRAM], "addOnItems", new int[] {0});
        tables[PROGRAM].addLink("addOnItems", tables[PROGRAMADDONITEM], "program", new int[] {0});
        tables[PROGRAMADDONITEM].addLink("addOnItems", tables[ADDONITEM], "program", new int[] {1});
        tables[ADDONITEM].addLink("program", tables[PROGRAMADDONITEM], "addOnItems", new int[] {0});
        
        // InspireApprovalEmail LINKS
        tables[INSPIREAPPROVALEMAIL].addLink("inspireApprovalReminder", tables[INSPIREAPPROVAL], "reminderEmails", new int[] {0});
        tables[INSPIREAPPROVAL].addLink("reminderEmails", tables[INSPIREAPPROVALEMAIL], "inspireApprovalReminder", new int[] {0});
        tables[INSPIREAPPROVALEMAIL].addLink("reminderEmails", tables[EMAIL], "inspireApprovalReminder", new int[] {1});
        tables[EMAIL].addLink("inspireApprovalReminder", tables[INSPIREAPPROVALEMAIL], "reminderEmails", new int[] {0});
        
        // AwardTypeProduct LINKS
        tables[AWARDTYPEPRODUCT].addLink("awardTypes", tables[AWARDTYPE], "packageProducts", new int[] {0});
        tables[AWARDTYPE].addLink("packageProducts", tables[AWARDTYPEPRODUCT], "awardTypes", new int[] {0});
        tables[AWARDTYPEPRODUCT].addLink("packageProducts", tables[PRODUCT], "awardTypes", new int[] {1});
        tables[PRODUCT].addLink("awardTypes", tables[AWARDTYPEPRODUCT], "packageProducts", new int[] {0});
        
        // InspireOrderEmail LINKS
        tables[INSPIREORDEREMAIL].addLink("inspireOrder", tables[INSPIREORDER], "emails", new int[] {0});
        tables[INSPIREORDER].addLink("emails", tables[INSPIREORDEREMAIL], "inspireOrder", new int[] {0});
        tables[INSPIREORDEREMAIL].addLink("emails", tables[EMAIL], "inspireOrder", new int[] {1});
        tables[EMAIL].addLink("inspireOrder", tables[INSPIREORDEREMAIL], "emails", new int[] {0});
        
        // PointsRequestEmail LINKS
        tables[POINTSREQUESTEMAIL].addLink("pointsRequest", tables[POINTSREQUEST], "emails", new int[] {0});
        tables[POINTSREQUEST].addLink("emails", tables[POINTSREQUESTEMAIL], "pointsRequest", new int[] {0});
        tables[POINTSREQUESTEMAIL].addLink("emails", tables[EMAIL], "pointsRequest", new int[] {1});
        tables[EMAIL].addLink("pointsRequest", tables[POINTSREQUESTEMAIL], "emails", new int[] {0});
        db.setTables(tables);
        return db;
    }
    
    protected void createDAO(Database db) {
        DataAccessObject dao;
        
        /*
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AboutMe.Id";
            private static final String columns = "AboutMe.Id, AboutMe.RecognitionPreference, AboutMe.FavoriteCandy, AboutMe.FavoriteRestaurant, AboutMe.FavoriteStore, AboutMe.FavoriteTeam, AboutMe.FavoriteColor, AboutMe.EmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAboutMe(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AboutMe").setDataAccessObject(dao);
        */
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AddOnItem.Id";
            private static final String columns = "AddOnItem.Id, AddOnItem.Created, AddOnItem.Value, AddOnItem.ItemId, AddOnItem.LocationId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAddOnItem(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AddOnItem").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Address.Id";
            private static final String columns = "Address.Id, Address.Created, Address.Address1, Address.Address2, Address.Address3, Address.Address4, Address.City, Address.State, Address.Zip, Address.Country, Address.AddressTypeId, Address.EmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAddress(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Address").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AddressType.Id";
            private static final String columns = "AddressType.Id, AddressType.Name, AddressType.Type";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAddressType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AddressType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Answer.Id";
            private static final String columns = "Answer.Id, Answer.AnswerText, Answer.Value, Answer.QuestionId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAnswer(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Answer").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AnswerResult.Id";
            private static final String columns = "AnswerResult.Id, AnswerResult.AnswerText, AnswerResult.AnswerValue, AnswerResult.CurrentAnswerId, AnswerResult.QuestionResultId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAnswerResult(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AnswerResult").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AwardCardOrder.Id";
            private static final String columns = "AwardCardOrder.Id, AwardCardOrder.Created, AwardCardOrder.Value, AwardCardOrder.SentDate, AwardCardOrder.ShippingInfo, AwardCardOrder.CardType, AwardCardOrder.LastStatusDate, AwardCardOrder.LastStatus, AwardCardOrder.CompletedDate, AwardCardOrder.PointsUsed, AwardCardOrder.InvoiceNumber, AwardCardOrder.InvoiceDate, AwardCardOrder.VendorInvoiced, AwardCardOrder.CardId, AwardCardOrder.EmployeeAwardId, AwardCardOrder.InspireOrderId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAwardCardOrder(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AwardCardOrder").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "AwardType.Id";
            private static final String columns = "AwardType.Id, AwardType.Name, AwardType.Descripton, AwardType.UsesGiftCards, AwardType.Value, AwardType.UsesItems, AwardType.UseHifiveApprovedItems, AwardType.MinimumItemPrice, AwardType.MaximumItemPrice, AwardType.ServiceAward, AwardType.YearsService, AwardType.BillCardPrice, AwardType.BillOnItemPrice, AwardType.BillItemPrice, AwardType.UsesHelpingHands, AwardType.UsesCash, AwardType.UsesInternationalVisa, AwardType.UsesCharity, AwardType.UsesImagineCard, AwardType.PackageName, AwardType.PackageInstruction, AwardType.PackageBillPrice, AwardType.AwardBillCost, AwardType.CashBillPrice, AwardType.ImagineBillPrice, AwardType.InternationalVisaBillPrice, AwardType.AnnouncementDocumentId, AwardType.CeoImageStoreId, AwardType.CeoSignatureImageStoreId, AwardType.ImagineCardId, AwardType.LocationId, AwardType.ProgramId, AwardType.SectionId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getAwardType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("AwardType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Card.Id";
            private static final String columns = "Card.Id, Card.Created, Card.Name, Card.ActiveDate, Card.DigitalCard, Card.TraditionalCard, Card.CelebrateCard, Card.TextValue, Card.InactiveDate, Card.RangeLow, Card.RangeHigh, Card.RangeIncrement, Card.MerchantCode, Card.CardVendorId, Card.ImageStoreId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCard(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Card").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "CardVendor.Id";
            private static final String columns = "CardVendor.Id, CardVendor.Created, CardVendor.Name, CardVendor.DigitalCard, CardVendor.TraditionalCard, CardVendor.CelebrateCard";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCardVendor(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("CardVendor").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "CashstarOrder.Id";
            private static final String columns = "CashstarOrder.Id, CashstarOrder.order_number, CashstarOrder.CardStatus, CashstarOrder.EgcCode, CashstarOrder.EgcNumber, CashstarOrder.CardUrl, CashstarOrder.BalanaceLastUpdated, CashstarOrder.ChallengeType, CashstarOrder.Currency, CashstarOrder.Active, CashstarOrder.FaceplateCode, CashstarOrder.Viewed, CashstarOrder.ChallengeDescription, CashstarOrder.Challenge, CashstarOrder.FirstViewed, CashstarOrder.AuditNumber, CashstarOrder.TransactionId, CashstarOrder.InitialBalance, CashstarOrder.CurrentBalance, CashstarOrder.AwardCardOrderId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCashstarOrder(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("CashstarOrder").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Catalog.Id";
            private static final String columns = "Catalog.Id, Catalog.Created, Catalog.Name";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCatalog(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Catalog").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Charity.Id";
            private static final String columns = "Charity.Id, Charity.Created, Charity.Name, Charity.InactiveDate, Charity.TextValue, Charity.Seq, Charity.ImageStoreId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCharity(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Charity").setDataAccessObject(dao);
        
        /*
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Code.Id";
            private static final String columns = "Code.Id, Code.Code, Code.RedeemedDate, Code.ExpirationDate, Code.PointValue, Code.LotId, Code.PointsRecordId, Code.RedeemingEmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCode(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Code").setDataAccessObject(dao);
        */
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Company.Id";
            private static final String columns = "Company.Id, Company.Created, Company.Name";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCompany(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Company").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "CountryCode.Id";
            private static final String columns = "CountryCode.Id, CountryCode.Name, CountryCode.Code, CountryCode.StateIsRequired, CountryCode.StateName, CountryCode.ZipCodeIsRequired, CountryCode.ZipCodeName, CountryCode.CurrencyTypeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCountryCode(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("CountryCode").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "CurrencyType.Id";
            private static final String columns = "CurrencyType.Id, CurrencyType.Name, CurrencyType.Abbreviation, CurrencyType.Symbol, CurrencyType.ExchangeRate, CurrencyType.Created";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCurrencyType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("CurrencyType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "CustomData.Id";
            private static final String columns = "CustomData.Id, CustomData.Code, CustomData.Name, CustomData.Description, CustomData.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getCustomData(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("CustomData").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Ecard.Id";
            private static final String columns = "Ecard.Id, Ecard.Created, Ecard.Name, Ecard.TextValue, Ecard.EcardCategoryId, Ecard.ImageStoreId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEcard(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Ecard").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EcardCategory.Id";
            private static final String columns = "EcardCategory.Id, EcardCategory.Name, EcardCategory.Seq";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEcardCategory(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EcardCategory").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Email.Id";
            private static final String columns = "Email.Id, Email.Created, Email.FromEmail, Email.ToEmail, Email.CcEmail, Email.Subject, Email.SentDateTime, Email.CancelDate, Email.Body, Email.AttachmentName, Email.AttachmentMimeType, Email.ProgramEmailTypeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmail(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Email").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmailType.Id";
            private static final String columns = "EmailType.Id, EmailType.Created, EmailType.Seq, EmailType.Name, EmailType.Type, EmailType.Subject, EmailType.TextValue, EmailType.Note";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmailType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmailType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Employee.Id";
            private static final String columns = "Employee.Id, Employee.Created, Employee.EmployeeCode, Employee.Title, Employee.PrefixName, Employee.FirstName, Employee.MiddleName, Employee.LastName, Employee.SuffixName, Employee.BirthDate, Employee.HireDate, Employee.LoginId, Employee.Passwordx, Employee.InactiveDate, Employee.InactiveReason, Employee.Admin, Employee.Email, Employee.Email2, Employee.WorkLocation, Employee.CostCenter, Employee.CostCenterDescription, Employee.PasswordAssignedDate, Employee.TopLevelManager, Employee.SuperApprover, Employee.Division, Employee.CompanyCode, Employee.CompanyCodeName, Employee.MaxNomLvl, Employee.UsesEcards, Employee.UsesService, Employee.UsesPoints, Employee.IsNominator, Employee.UsesInspire, Employee.CanGetNominated, Employee.UsesAwardGallery, Employee.RegionCode, Employee.JobFunctionCode, Employee.JobFunctionName, Employee.CanRedeemCodes, Employee.CanActivateLots, Employee.EmployeeTypeId, Employee.HRBPartnerId, Employee.LocationId, Employee.ParentEmployeeId, Employee.PointsNextApprovalId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployee(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Employee").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeAward.Id";
            private static final String columns = "EmployeeAward.Id, EmployeeAward.Created, EmployeeAward.AwardDate, EmployeeAward.ApprovedDate, EmployeeAward.PackageSentDate, EmployeeAward.PackageTracking, EmployeeAward.PackageShippingInfo, EmployeeAward.PackageInvoiceNumber, EmployeeAward.PackageBillDate, EmployeeAward.BillDate, EmployeeAward.PackagePaidDate, EmployeeAward.PaidDate, EmployeeAward.ItemSelectedDate, EmployeeAward.ItemSentDate, EmployeeAward.ItemShippingInfo, EmployeeAward.ItemTracking, EmployeeAward.ItemBillDate, EmployeeAward.ItemLastStatusDate, EmployeeAward.ItemLastStatus, EmployeeAward.ItemInvoiceNumber, EmployeeAward.ItemVendorInvoiced, EmployeeAward.ItemPaidDate, EmployeeAward.CompletedDate, EmployeeAward.CancelDate, EmployeeAward.CancelReason, EmployeeAward.CashSelectedDate, EmployeeAward.CashSentDate, EmployeeAward.InternationalVisaSelectedDate, EmployeeAward.InternationalVisaAmount, EmployeeAward.InternationalVisaSentDate, EmployeeAward.AddOnProductSelectedDate, EmployeeAward.MergeId, EmployeeAward.CashInvoiceNumber, EmployeeAward.CashinvoiceDate, EmployeeAward.InternationalVisaInvoiceNumber, EmployeeAward.InternationVisaInvoiceDate, EmployeeAward.InternationalVisaVendorInvoiced, EmployeeAward.AddOnProductId, EmployeeAward.AwardTypeId, EmployeeAward.ConfirmEmailId, EmployeeAward.EmployeeId, EmployeeAward.ManagerNotifyEmailId, EmployeeAward.NotifyEmailId, EmployeeAward.ProductId, EmployeeAward.ShipToId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeAward(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeAward").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeAwardCharity.Id";
            private static final String columns = "EmployeeAwardCharity.Id, EmployeeAwardCharity.Created, EmployeeAwardCharity.Value, EmployeeAwardCharity.SentDate, EmployeeAwardCharity.InvoiceNumber, EmployeeAwardCharity.InvoiceDate, EmployeeAwardCharity.VendorInvoiced, EmployeeAwardCharity.CharityId, EmployeeAwardCharity.EmployeeAwardId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeAwardCharity(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeAwardCharity").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeCustomData.Id";
            private static final String columns = "EmployeeCustomData.Id, EmployeeCustomData.Value, EmployeeCustomData.CustomDataId, EmployeeCustomData.EmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeCustomData(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeCustomData").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeEcard.Id";
            private static final String columns = "EmployeeEcard.Id, EmployeeEcard.Created, EmployeeEcard.Subject, EmployeeEcard.Message, EmployeeEcard.SendDate, EmployeeEcard.PostToWall, EmployeeEcard.ConfirmedEmailId, EmployeeEcard.DeliveredEmailId, EmployeeEcard.EcardId, EmployeeEcard.EmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeEcard(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeEcard").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeEcardTo.Id";
            private static final String columns = "EmployeeEcardTo.Id, EmployeeEcardTo.EmailAddress, EmployeeEcardTo.Name, EmployeeEcardTo.IncludeManager, EmployeeEcardTo.EmailId, EmployeeEcardTo.EmployeeEcardId, EmployeeEcardTo.ToEmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeEcardTo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeEcardTo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeSurvey.Id";
            private static final String columns = "EmployeeSurvey.Id, EmployeeSurvey.Created, EmployeeSurvey.TextValue, EmployeeSurvey.Points, EmployeeSurvey.ApprovedDate, EmployeeSurvey.EmployeeId, EmployeeSurvey.SurveyId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeSurvey(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeSurvey").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeSurveyQuestion.Id";
            private static final String columns = "EmployeeSurveyQuestion.Id, EmployeeSurveyQuestion.TextValue, EmployeeSurveyQuestion.EmployeeSurveyId, EmployeeSurveyQuestion.SurveyAnswerId, EmployeeSurveyQuestion.SurveyQuestionId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeSurveyQuestion(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeSurveyQuestion").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "EmployeeType.Id";
            private static final String columns = "EmployeeType.Id, EmployeeType.Name, EmployeeType.Type";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getEmployeeType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("EmployeeType").setDataAccessObject(dao);
        
        /*
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "FamilyMember.Id";
            private static final String columns = "FamilyMember.Id, FamilyMember.Relationship, FamilyMember.Name, FamilyMember.Age, FamilyMember.Birthday, FamilyMember.Species, FamilyMember.EmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getFamilyMember(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("FamilyMember").setDataAccessObject(dao);
        */
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "HindaOrder.Id";
            private static final String columns = "HindaOrder.Id, HindaOrder.OrderNumber, HindaOrder.ClientOrderNumber, HindaOrder.OrderDate, HindaOrder.EmployeeAwardId, HindaOrder.InspireOrderId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getHindaOrder(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("HindaOrder").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "HindaOrderLine.Id";
            private static final String columns = "HindaOrderLine.Id, HindaOrderLine.OrderLineNumber, HindaOrderLine.QuantityBackOrdered, HindaOrderLine.QuantityOrdered, HindaOrderLine.QuantityCanceled, HindaOrderLine.QuantityReserved, HindaOrderLine.QuantityShipped, HindaOrderLine.TrackingNumber, HindaOrderLine.CarrierCode, HindaOrderLine.TrackingUrl, HindaOrderLine.ShippedDate, HindaOrderLine.ClientOrderLineNumber, HindaOrderLine.HindaOrderId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getHindaOrderLine(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("HindaOrderLine").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ImageStore.Id";
            private static final String columns = "ImageStore.Id, ImageStore.Created, ImageStore.OrigFileName, ImageStore.Description, ImageStore.LastUpdate";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getImageStore(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ImageStore").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Inspire.Id";
            private static final String columns = "Inspire.Id, Inspire.Created, Inspire.Reason, Inspire.Message, Inspire.EmailId, Inspire.EmployeeId, Inspire.InspireAwardLevelId, Inspire.InspireCoreValueId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspire(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Inspire").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireApproval.Id";
            private static final String columns = "InspireApproval.Id, InspireApproval.Created, InspireApproval.Status, InspireApproval.StatusDate, InspireApproval.Comments, InspireApproval.EmailId, InspireApproval.EmployeeId, InspireApproval.InspireAwardLevelId, InspireApproval.InspireRecipientId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireApproval(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireApproval").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireAwardLevel.Id";
            private static final String columns = "InspireAwardLevel.Id, InspireAwardLevel.Name, InspireAwardLevel.Rank, InspireAwardLevel.ApprovalLevels, InspireAwardLevel.ApproveFromTop, InspireAwardLevel.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireAwardLevel(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireAwardLevel").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireAwardLevelLocationValue.Id";
            private static final String columns = "InspireAwardLevelLocationValue.Id, InspireAwardLevelLocationValue.Points, InspireAwardLevelLocationValue.InspireAwardLevelId, InspireAwardLevelLocationValue.LocationId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireAwardLevelLocationValue(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireAwardLevelLocationValue").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireCoreValue.Id";
            private static final String columns = "InspireCoreValue.Id, InspireCoreValue.Name, InspireCoreValue.Seq, InspireCoreValue.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireCoreValue(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireCoreValue").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireOrder.Id";
            private static final String columns = "InspireOrder.Id, InspireOrder.Created, InspireOrder.BillDate, InspireOrder.PaidDate, InspireOrder.CompletedDate, InspireOrder.CashSelectedDate, InspireOrder.CashAmount, InspireOrder.CashPointsUsed, InspireOrder.CashSentDate, InspireOrder.InternationalVisaSelectedDate, InspireOrder.InternationalVisaAmount, InspireOrder.InternationalVisaPointsUsed, InspireOrder.InternationalVisaSentDate, InspireOrder.PointsOrdered, InspireOrder.CashInvoiceNumber, InspireOrder.CashInvoiceDate, InspireOrder.InternationalVisaInvoiceNumber, InspireOrder.InternationalVisaInvoiceDate, InspireOrder.InternationalVisaVendorInvoiced, InspireOrder.EmployeeId, InspireOrder.ShipToId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireOrder(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireOrder").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireOrderCharity.Id";
            private static final String columns = "InspireOrderCharity.Id, InspireOrderCharity.Created, InspireOrderCharity.Value, InspireOrderCharity.SentDate, InspireOrderCharity.PointsUsed, InspireOrderCharity.InvoiceNumber, InspireOrderCharity.InvoiceDate, InspireOrderCharity.VendorInvoiced, InspireOrderCharity.CharityId, InspireOrderCharity.InspireOrderId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireOrderCharity(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireOrderCharity").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireOrderItem.Id";
            private static final String columns = "InspireOrderItem.Id, InspireOrderItem.Created, InspireOrderItem.Seq, InspireOrderItem.Quantity, InspireOrderItem.PointsUsed, InspireOrderItem.BillDate, InspireOrderItem.PaidDate, InspireOrderItem.ItemSentDate, InspireOrderItem.ItemShippingInfo, InspireOrderItem.ItemLastStatusDate, InspireOrderItem.ItemLastStatus, InspireOrderItem.CompletedDate, InspireOrderItem.InvoiceNumber, InspireOrderItem.InvoiceDate, InspireOrderItem.VendorInvoiced, InspireOrderItem.InspireOrderId, InspireOrderItem.ProductId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireOrderItem(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireOrderItem").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "InspireRecipient.Id";
            private static final String columns = "InspireRecipient.Id, InspireRecipient.Points, InspireRecipient.CompletedDate, InspireRecipient.CompletedEmailId, InspireRecipient.EmailId, InspireRecipient.EmployeeId, InspireRecipient.InspireId, InspireRecipient.InspireAwardLevelLocationValueId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getInspireRecipient(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("InspireRecipient").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Item.Id";
            private static final String columns = "Item.Id, Item.Created, Item.VendorCode, Item.VendorCode2, Item.Name, Item.BriefText, Item.TextValue, Item.DiscontinuedDate, Item.DiscontinuedReason, Item.DropShip, Item.OtherInformation, Item.Manufacturer, Item.Model, Item.LastUpdate, Item.HifiveRating, Item.HifiveRatingDate, Item.HifiveRatingNote, Item.AccountNumber, Item.ImageStoreId, Item.ItemVendorId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getItem(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Item").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ItemCategory.Id";
            private static final String columns = "ItemCategory.Id, ItemCategory.Name, ItemCategory.Code, ItemCategory.Seq, ItemCategory.HifiveRating, ItemCategory.HifiveRatingDate, ItemCategory.HifiveRatingNote, ItemCategory.ParentItemCategoryId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getItemCategory(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ItemCategory").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ItemType.Id";
            private static final String columns = "ItemType.Id, ItemType.Name, ItemType.Type";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getItemType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ItemType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ItemVendor.Id";
            private static final String columns = "ItemVendor.Id, ItemVendor.Created, ItemVendor.Name, ItemVendor.Notes";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getItemVendor(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ItemVendor").setDataAccessObject(dao);
        
        /*
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LineItem.Id";
            private static final String columns = "LineItem.Id, LineItem.Description, LineItem.BilledAmount, LineItem.PaidAmount, LineItem.Billed, LineItem.Paid, LineItem.InvoiceNumber, LineItem.Invoicedate, LineItem.Billable, LineItem.Created, LineItem.CurrentValue, LineItem.BaseCreationDate, LineItem.AwardCardOrderId, LineItem.EmailId, LineItem.EmployeeId, LineItem.EmployeeAwardId, LineItem.EmployeeAwardCashId, LineItem.EmployeeAwardCharityId, LineItem.EmployeeAwardIntlVisaId, LineItem.EmployeeEcardToId, LineItem.InspireOrderId, LineItem.InspireOrderCashId, LineItem.InspireOrderCharityId, LineItem.InspireOrderIntlVisaId, LineItem.InspireOrderItemId, LineItem.InspireRecipientId, LineItem.LocationId, LineItem.PointsRecordId, LineItem.PointsRequestId, LineItem.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLineItem(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LineItem").setDataAccessObject(dao);
        */
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Location.Id";
            private static final String columns = "Location.Id, Location.Created, Location.Name, Location.Name2, Location.Seq, Location.Code, Location.CharityGoal, Location.FromEmailAddress, Location.TestEmail, Location.IndexPageUsesBorders, Location.ScrollerShowsLocation, Location.ScrollerShowsDate, Location.ScrollerShowsYears, Location.ScrollerRowsPerSecond, Location.AddressId, Location.AnnouncementDocumentId, Location.CeoImageStoreId, Location.CeoSignatureImageStoreId, Location.CountryCodeId, Location.ImagineCardId, Location.InspireAwardTypeId, Location.LocationTypeId, Location.LogoImageStoreId, Location.LogoStampImageStoreId, Location.PageThemeId, Location.ParentLocationId, Location.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLocation(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Location").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LocationPageGroup.Id";
            private static final String columns = "LocationPageGroup.Id, LocationPageGroup.Created, LocationPageGroup.Seq, LocationPageGroup.LocationId, LocationPageGroup.PageGroupId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLocationPageGroup(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LocationPageGroup").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LocationPageInfo.Id";
            private static final String columns = "LocationPageInfo.Id, LocationPageInfo.Created, LocationPageInfo.ImageStoreId, LocationPageInfo.LocationId, LocationPageInfo.PageInfoId, LocationPageInfo.ProgramDocumentId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLocationPageInfo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LocationPageInfo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LocationType.Id";
            private static final String columns = "LocationType.Id, LocationType.Name, LocationType.Seq, LocationType.CompanyId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLocationType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LocationType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LoginImage.Id";
            private static final String columns = "LoginImage.Id, LoginImage.Location, LoginImage.XPosition, LoginImage.YPosition, LoginImage.ImageStoreId, LoginImage.LoginImageSetId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLoginImage(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LoginImage").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "LoginImageSet.Id";
            private static final String columns = "LoginImageSet.Id, LoginImageSet.Created, LoginImageSet.Name";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLoginImageSet(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("LoginImageSet").setDataAccessObject(dao);
        
        /*
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Lot.Id";
            private static final String columns = "Lot.Id, Lot.Number, Lot.Sentiment, Lot.CreatedDate, Lot.OrderedDate, Lot.ActivatedDate, Lot.ExpirationDate, Lot.FaceValue, Lot.NumberOfCodes, Lot.SameValue, Lot.ActivatingEmployeeId, Lot.PointsRequestId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getLot(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Lot").setDataAccessObject(dao);
        */
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Merchant.Id";
            private static final String columns = "Merchant.Id, Merchant.Created, Merchant.Name, Merchant.Description, Merchant.TextValue, Merchant.ImageStoreId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getMerchant(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Merchant").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "MerchantCategory.Id";
            private static final String columns = "MerchantCategory.Id, MerchantCategory.Name, MerchantCategory.Seq, MerchantCategory.ParentMerchantCategoryId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getMerchantCategory(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("MerchantCategory").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "OrderItemTracking.Id";
            private static final String columns = "OrderItemTracking.Id, OrderItemTracking.SentCarrier, OrderItemTracking.ProductionDate, OrderItemTracking.SentDate, OrderItemTracking.ExpectedDeliveryDate, OrderItemTracking.ConfirmEmailDate, OrderItemTracking.CarrierTracking, OrderItemTracking.CancelDate, OrderItemTracking.ReplaceDate, OrderItemTracking.Freight, OrderItemTracking.OrderTrackingId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getOrderItemTracking(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("OrderItemTracking").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "OrderTracking.Id";
            private static final String columns = "OrderTracking.Id, OrderTracking.Created, OrderTracking.Description, OrderTracking.SentEmailDate, OrderTracking.ReceivedDate, OrderTracking.CloseDate, OrderTracking.Notes, OrderTracking.BillingDate, OrderTracking.BillAmount, OrderTracking.InvoiceDate, OrderTracking.InvoiceNumber, OrderTracking.PaidDate";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getOrderTracking(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("OrderTracking").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "OrderTrackingStatus.Id";
            private static final String columns = "OrderTrackingStatus.Id, OrderTrackingStatus.Created, OrderTrackingStatus.Description, OrderTrackingStatus.Note, OrderTrackingStatus.EmailAddress, OrderTrackingStatus.EmailText, OrderTrackingStatus.EmailDate, OrderTrackingStatus.OrderTrackingId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getOrderTrackingStatus(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("OrderTrackingStatus").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Page.Id";
            private static final String columns = "Page.Id, Page.Created, Page.Name, Page.Description, Page.Seq";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPage(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Page").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PageGroup.Id";
            private static final String columns = "PageGroup.Id, PageGroup.Created, PageGroup.Seq, PageGroup.Name, PageGroup.ParentPageGroupId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPageGroup(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PageGroup").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PageGroupPageInfo.Id";
            private static final String columns = "PageGroupPageInfo.Id, PageGroupPageInfo.Created, PageGroupPageInfo.ImageStoreId, PageGroupPageInfo.PageGroupId, PageGroupPageInfo.PageInfoId, PageGroupPageInfo.ProgramDocumentId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPageGroupPageInfo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PageGroupPageInfo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PageInfo.Id";
            private static final String columns = "PageInfo.Id, PageInfo.Code, PageInfo.Description, PageInfo.Seq, PageInfo.PageId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPageInfo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PageInfo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PageTheme.Id";
            private static final String columns = "PageTheme.Id, PageTheme.Name, PageTheme.CssFileName";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPageTheme(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PageTheme").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PageThemePageInfo.Id";
            private static final String columns = "PageThemePageInfo.Id, PageThemePageInfo.Created, PageThemePageInfo.ImageStoreId, PageThemePageInfo.PageInfoId, PageThemePageInfo.PageThemeId, PageThemePageInfo.ProgramDocumentId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPageThemePageInfo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PageThemePageInfo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Phone.Id";
            private static final String columns = "Phone.Id, Phone.Created, Phone.PhoneNumber, Phone.InactiveDate, Phone.PhoneTypeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPhone(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Phone").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PhoneType.Id";
            private static final String columns = "PhoneType.Id, PhoneType.Name, PhoneType.Type";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPhoneType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PhoneType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsApproval.Id";
            private static final String columns = "PointsApproval.Id, PointsApproval.Created, PointsApproval.Status, PointsApproval.StatusDate, PointsApproval.Comments, PointsApproval.Seq, PointsApproval.ApprovedAwardLevelId, PointsApproval.EmployeeId, PointsApproval.PointsRequestId, PointsApproval.StartingAwardLevelId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsApproval(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsApproval").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsAwardLevel.Id";
            private static final String columns = "PointsAwardLevel.Id, PointsAwardLevel.Name, PointsAwardLevel.Seq, PointsAwardLevel.ApprovalLevels, PointsAwardLevel.RequireSuperApprover, PointsAwardLevel.Points, PointsAwardLevel.QuizRangeMin, PointsAwardLevel.QuizRangeMax, PointsAwardLevel.Description, PointsAwardLevel.Level, PointsAwardLevel.LocationId, PointsAwardLevel.ProgramId, PointsAwardLevel.RequiredApprovalId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsAwardLevel(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsAwardLevel").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsConfiguration.Id";
            private static final String columns = "PointsConfiguration.Id, PointsConfiguration.NotificationToNominatorManager, PointsConfiguration.NotificationToRecipientManager, PointsConfiguration.CertificateToNominator, PointsConfiguration.CertificateToRecipient, PointsConfiguration.CertificateToRecipientManager, PointsConfiguration.NominationApprovedByRecipientManagement, PointsConfiguration.DaysToDelayPoints";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsConfiguration(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsConfiguration").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsCoreValue.Id";
            private static final String columns = "PointsCoreValue.Id, PointsCoreValue.Name, PointsCoreValue.Seq, PointsCoreValue.Description, PointsCoreValue.LocationId, PointsCoreValue.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsCoreValue(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsCoreValue").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsRecord.Id";
            private static final String columns = "PointsRecord.Id, PointsRecord.Created, PointsRecord.Points, PointsRecord.ToDiscretionary, PointsRecord.Reason, PointsRecord.Comment, PointsRecord.Event, PointsRecord.Custom1, PointsRecord.Custom2, PointsRecord.Custom3, PointsRecord.Engaged, PointsRecord.EmailId, PointsRecord.InspireOrderId, PointsRecord.PointsCoreValueId, PointsRecord.PointsRequestId, PointsRecord.PointsToEmployeeId, PointsRecord.PointsToProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsRecord(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsRecord").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "PointsRequest.Id";
            private static final String columns = "PointsRequest.Id, PointsRequest.Created, PointsRequest.Approved, PointsRequest.Notes, PointsRequest.ErrorNotes, PointsRequest.Filename, PointsRequest.ApprovedDate, PointsRequest.RequestType, PointsRequest.ApprovingEmployeeId, PointsRequest.ApprovingUserId, PointsRequest.PointsAwardLevelId, PointsRequest.QuizResultId, PointsRequest.RequestingEmployeeId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getPointsRequest(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("PointsRequest").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Product.Id";
            private static final String columns = "Product.Id, Product.VendorCode, Product.Attribute, Product.Cost, Product.HandlingCost, Product.ShippingCost, Product.TotalCost, Product.DiscontinuedDate, Product.DiscontinuedReason, Product.LastUpdate, Product.Msrp, Product.StreetValue, Product.ItemId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProduct(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Product").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProductAudit.Id";
            private static final String columns = "ProductAudit.Id, ProductAudit.Created, ProductAudit.Cost, ProductAudit.HandlingCost, ProductAudit.Note, ProductAudit.ProductId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProductAudit(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProductAudit").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Program.Id";
            private static final String columns = "Program.Id, Program.Created, Program.Code, Program.Name, Program.BeginDate, Program.EndDate, Program.AwardBeginDate, Program.InactiveDate, Program.UrlName, Program.FromEmailAddress, Program.PointsName, Program.PointValue, Program.UsesInspire, Program.UsesHifive, Program.UsesSurveys, Program.EcardType, Program.HifiveName, Program.BirthdayDisplayDays, Program.AnniversaryDisplayDays, Program.Seq, Program.LoginMessage, Program.EmployeeAwardExpireDays, Program.CharityGoal, Program.CharityTotal, Program.ItemUpcharge, Program.CardUpcharge, Program.CharityUpcharge, Program.CashUpcharge, Program.TestEmail, Program.UseAdvancedReports, Program.PacketInstructions, Program.SsoFailureUrl, Program.SsoCode, Program.SsoRedirectUrl, Program.UsesPoints, Program.PointsBillingType, Program.SsoLogoutUrl, Program.UsesPeerToPeer, Program.UsesDiscretionary, Program.EmployeeAwardDaysBefore, Program.CompanyPaysShipping, Program.UsesNominations, Program.UsesManagerToolkit, Program.LotExpireDays, Program.UploadApprovals, Program.IndexPageUsesBorders, Program.ScrollerShowsLocation, Program.ScrollerShowsDate, Program.ScrollerShowsYears, Program.ScrollerRowsPerSecond, Program.PointsMargin, Program.PointsMarkup, Program.AnnouncementDocumentId, Program.CeoImageStoreId, Program.CeoSignatureImageStoreId, Program.CompanyId, Program.CountryCodeId, Program.ImagineCardId, Program.InspireAwardTypeId, Program.LoginImageSetId, Program.LogoStampImageStoreId, Program.ManagerHifiveSurveyId, Program.PageThemeId, Program.PointsConfigurationId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgram(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Program").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramDocument.Id";
            private static final String columns = "ProgramDocument.Id, ProgramDocument.Created, ProgramDocument.Name, ProgramDocument.TextValue";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramDocument(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramDocument").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramEmailType.Id";
            private static final String columns = "ProgramEmailType.Id, ProgramEmailType.Created, ProgramEmailType.AllowAutomaticSend, ProgramEmailType.Subject, ProgramEmailType.TextValue, ProgramEmailType.EmailTypeId, ProgramEmailType.LocationId, ProgramEmailType.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramEmailType(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramEmailType").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramEvent.Id";
            private static final String columns = "ProgramEvent.Id, ProgramEvent.Created, ProgramEvent.BeginDateTime, ProgramEvent.EndDateTime, ProgramEvent.Name, ProgramEvent.TextValue, ProgramEvent.LocationId, ProgramEvent.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramEvent(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramEvent").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramFaq.Id";
            private static final String columns = "ProgramFaq.Id, ProgramFaq.Created, ProgramFaq.Seq, ProgramFaq.Question, ProgramFaq.Answer, ProgramFaq.ManagerOnly, ProgramFaq.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramFaq(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramFaq").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramPageGroup.Id";
            private static final String columns = "ProgramPageGroup.Id, ProgramPageGroup.Created, ProgramPageGroup.Seq, ProgramPageGroup.PageGroupId, ProgramPageGroup.ProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramPageGroup(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramPageGroup").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ProgramPageInfo.Id";
            private static final String columns = "ProgramPageInfo.Id, ProgramPageInfo.Created, ProgramPageInfo.ImageStoreId, ProgramPageInfo.PageInfoId, ProgramPageInfo.ProgramId, ProgramPageInfo.ProgramDocumentId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getProgramPageInfo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ProgramPageInfo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Question.Id";
            private static final String columns = "Question.Id, Question.QuestionText, Question.QuizId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getQuestion(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Question").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "QuestionResult.Id";
            private static final String columns = "QuestionResult.Id, QuestionResult.QuestionText, QuestionResult.QuestionId, QuestionResult.QuizResultId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getQuestionResult(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("QuestionResult").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Quiz.Id";
            private static final String columns = "Quiz.Id, Quiz.Name, Quiz.LocationId, Quiz.ProgramId, Quiz.Program2Id";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getQuiz(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Quiz").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "QuizResult.Id";
            private static final String columns = "QuizResult.Id, QuizResult.Name, QuizResult.EmployeeId, QuizResult.QuizId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getQuizResult(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("QuizResult").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Report.Id";
            private static final String columns = "Report.Id, Report.Created, Report.Code, Report.Name, Report.Orientation, Report.Template, Report.ParentReportId, Report.ReportClassId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getReport(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Report").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ReportClass.Id";
            private static final String columns = "ReportClass.Id, ReportClass.Name, ReportClass.ClassName";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getReportClass(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ReportClass").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Section.Id";
            private static final String columns = "Section.Id, Section.Seq, Section.Name, Section.CatalogId, Section.ParentSectionId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getSection(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Section").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "ShipTo.Id";
            private static final String columns = "ShipTo.Id, ShipTo.Created, ShipTo.Name, ShipTo.Note, ShipTo.Email, ShipTo.PhoneNumber, ShipTo.AddressId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getShipTo(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("ShipTo").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Survey.Id";
            private static final String columns = "Survey.Id, Survey.Created, Survey.Name, Survey.Descripiton, Survey.AllowText, Survey.ProgramId, Survey.QuizProgramId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getSurvey(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Survey").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "SurveyAnswer.Id";
            private static final String columns = "SurveyAnswer.Id, SurveyAnswer.TextValue, SurveyAnswer.SurveyAnswerQuestionId, SurveyAnswer.SurveyQuestionId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getSurveyAnswer(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("SurveyAnswer").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "SurveyQuestion.Id";
            private static final String columns = "SurveyQuestion.Id, SurveyQuestion.Type, SurveyQuestion.AllowTextResponse, SurveyQuestion.TextValue, SurveyQuestion.Seq, SurveyQuestion.SurveyId";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getSurveyQuestion(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("SurveyQuestion").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "UserTable.Id";
            private static final String columns = "UserTable.Id, UserTable.Created, UserTable.FirstName, UserTable.LastName, UserTable.LoginId, UserTable.Password, UserTable.InactiveDate, UserTable.Admin, UserTable.Email, UserTable.LoggedIn, UserTable.Location, UserTable.EditProcessed";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getUser(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("UserTable").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Value.Id";
            private static final String columns = "Value.Id, Value.Value, Value.Name";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getValue(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Value").setDataAccessObject(dao);
        
        dao = new DataAccessObject() {
            private static final String pkeyColumns = "Widget.Id";
            private static final String columns = "Widget.Id, Widget.Created, Widget.Name, Widget.Link";
            @Override
            public String getPkeySelectColumns() {
                return pkeyColumns;
            }
            @Override
            public String getSelectColumns() {
                return columns;
            }
            @Override
            public OAObject getObject(DataAccessObject.ResultSetInfo rsi) throws SQLException {
                return getWidget(rsi.getResultSet(), rsi);
            }
        };
        db.getTable("Widget").setDataAccessObject(dao);
    }
    
    /*
    protected AboutMe getAboutMe(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AboutMe aboutMe = (AboutMe) OAObjectCacheDelegate.getObject(AboutMe.class, id);
        if (aboutMe == null) {
            aboutMe = new AboutMe();
            aboutMe.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return aboutMe;
    }
    */
    
    protected AddOnItem getAddOnItem(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AddOnItem addOnItem = (AddOnItem) OAObjectCacheDelegate.getObject(AddOnItem.class, id);
        if (addOnItem == null) {
            addOnItem = new AddOnItem();
            addOnItem.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return addOnItem;
    }
    
    protected Address getAddress(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Address address = (Address) OAObjectCacheDelegate.getObject(Address.class, id);
        if (address == null) {
            address = new Address();
            address.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return address;
    }
    
    protected AddressType getAddressType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AddressType addressType = (AddressType) OAObjectCacheDelegate.getObject(AddressType.class, id);
        if (addressType == null) {
            addressType = new AddressType();
            addressType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return addressType;
    }
    
    protected Answer getAnswer(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Answer answer = (Answer) OAObjectCacheDelegate.getObject(Answer.class, id);
        if (answer == null) {
            answer = new Answer();
            answer.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return answer;
    }
    
    protected AnswerResult getAnswerResult(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AnswerResult answerResult = (AnswerResult) OAObjectCacheDelegate.getObject(AnswerResult.class, id);
        if (answerResult == null) {
            answerResult = new AnswerResult();
            answerResult.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return answerResult;
    }
    
    protected AwardCardOrder getAwardCardOrder(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AwardCardOrder awardCardOrder = (AwardCardOrder) OAObjectCacheDelegate.getObject(AwardCardOrder.class, id);
        if (awardCardOrder == null) {
            awardCardOrder = new AwardCardOrder();
            awardCardOrder.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return awardCardOrder;
    }
    
    protected AwardType getAwardType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        AwardType awardType = (AwardType) OAObjectCacheDelegate.getObject(AwardType.class, id);
        if (awardType == null) {
            awardType = new AwardType();
            awardType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return awardType;
    }
    
    protected Card getCard(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Card card = (Card) OAObjectCacheDelegate.getObject(Card.class, id);
        if (card == null) {
            card = new Card();
            card.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return card;
    }
    
    protected CardVendor getCardVendor(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        CardVendor cardVendor = (CardVendor) OAObjectCacheDelegate.getObject(CardVendor.class, id);
        if (cardVendor == null) {
            cardVendor = new CardVendor();
            cardVendor.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return cardVendor;
    }
    
    protected CashstarOrder getCashstarOrder(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        CashstarOrder cashstarOrder = (CashstarOrder) OAObjectCacheDelegate.getObject(CashstarOrder.class, id);
        if (cashstarOrder == null) {
            cashstarOrder = new CashstarOrder();
            cashstarOrder.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return cashstarOrder;
    }
    
    protected Catalog getCatalog(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Catalog catalog = (Catalog) OAObjectCacheDelegate.getObject(Catalog.class, id);
        if (catalog == null) {
            catalog = new Catalog();
            catalog.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return catalog;
    }
    
    protected Charity getCharity(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Charity charity = (Charity) OAObjectCacheDelegate.getObject(Charity.class, id);
        if (charity == null) {
            charity = new Charity();
            charity.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return charity;
    }
    
    /*
    protected Code getCode(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Code code = (Code) OAObjectCacheDelegate.getObject(Code.class, id);
        if (code == null) {
            code = new Code();
            code.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return code;
    }
    */
    
    protected Company getCompany(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Company company = (Company) OAObjectCacheDelegate.getObject(Company.class, id);
        if (company == null) {
            company = new Company();
            company.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return company;
    }
    
    protected CountryCode getCountryCode(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        CountryCode countryCode = (CountryCode) OAObjectCacheDelegate.getObject(CountryCode.class, id);
        if (countryCode == null) {
            countryCode = new CountryCode();
            countryCode.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return countryCode;
    }
    
    protected CurrencyType getCurrencyType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        CurrencyType currencyType = (CurrencyType) OAObjectCacheDelegate.getObject(CurrencyType.class, id);
        if (currencyType == null) {
            currencyType = new CurrencyType();
            currencyType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return currencyType;
    }
    
    protected CustomData getCustomData(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        CustomData customData = (CustomData) OAObjectCacheDelegate.getObject(CustomData.class, id);
        if (customData == null) {
            customData = new CustomData();
            customData.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return customData;
    }
    
    protected Ecard getEcard(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Ecard ecard = (Ecard) OAObjectCacheDelegate.getObject(Ecard.class, id);
        if (ecard == null) {
            ecard = new Ecard();
            ecard.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return ecard;
    }
    
    protected EcardCategory getEcardCategory(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EcardCategory ecardCategory = (EcardCategory) OAObjectCacheDelegate.getObject(EcardCategory.class, id);
        if (ecardCategory == null) {
            ecardCategory = new EcardCategory();
            ecardCategory.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return ecardCategory;
    }
    
    protected Email getEmail(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Email email = (Email) OAObjectCacheDelegate.getObject(Email.class, id);
        if (email == null) {
            email = new Email();
            email.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return email;
    }
    
    protected EmailType getEmailType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmailType emailType = (EmailType) OAObjectCacheDelegate.getObject(EmailType.class, id);
        if (emailType == null) {
            emailType = new EmailType();
            emailType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return emailType;
    }
    
    protected Employee getEmployee(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Employee employee = (Employee) OAObjectCacheDelegate.getObject(Employee.class, id);
        if (employee == null) {
            employee = new Employee();
            employee.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employee;
    }
    
    protected EmployeeAward getEmployeeAward(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeAward employeeAward = (EmployeeAward) OAObjectCacheDelegate.getObject(EmployeeAward.class, id);
        if (employeeAward == null) {
            employeeAward = new EmployeeAward();
            employeeAward.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeAward;
    }
    
    protected EmployeeAwardCharity getEmployeeAwardCharity(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeAwardCharity employeeAwardCharity = (EmployeeAwardCharity) OAObjectCacheDelegate.getObject(EmployeeAwardCharity.class, id);
        if (employeeAwardCharity == null) {
            employeeAwardCharity = new EmployeeAwardCharity();
            employeeAwardCharity.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeAwardCharity;
    }
    
    protected EmployeeCustomData getEmployeeCustomData(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeCustomData employeeCustomData = (EmployeeCustomData) OAObjectCacheDelegate.getObject(EmployeeCustomData.class, id);
        if (employeeCustomData == null) {
            employeeCustomData = new EmployeeCustomData();
            employeeCustomData.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeCustomData;
    }
    
    protected EmployeeEcard getEmployeeEcard(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeEcard employeeEcard = (EmployeeEcard) OAObjectCacheDelegate.getObject(EmployeeEcard.class, id);
        if (employeeEcard == null) {
            employeeEcard = new EmployeeEcard();
            employeeEcard.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeEcard;
    }
    
    protected EmployeeEcardTo getEmployeeEcardTo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeEcardTo employeeEcardTo = (EmployeeEcardTo) OAObjectCacheDelegate.getObject(EmployeeEcardTo.class, id);
        if (employeeEcardTo == null) {
            employeeEcardTo = new EmployeeEcardTo();
            employeeEcardTo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeEcardTo;
    }
    
    protected EmployeeSurvey getEmployeeSurvey(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeSurvey employeeSurvey = (EmployeeSurvey) OAObjectCacheDelegate.getObject(EmployeeSurvey.class, id);
        if (employeeSurvey == null) {
            employeeSurvey = new EmployeeSurvey();
            employeeSurvey.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeSurvey;
    }
    
    protected EmployeeSurveyQuestion getEmployeeSurveyQuestion(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeSurveyQuestion employeeSurveyQuestion = (EmployeeSurveyQuestion) OAObjectCacheDelegate.getObject(EmployeeSurveyQuestion.class, id);
        if (employeeSurveyQuestion == null) {
            employeeSurveyQuestion = new EmployeeSurveyQuestion();
            employeeSurveyQuestion.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeSurveyQuestion;
    }
    
    protected EmployeeType getEmployeeType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        EmployeeType employeeType = (EmployeeType) OAObjectCacheDelegate.getObject(EmployeeType.class, id);
        if (employeeType == null) {
            employeeType = new EmployeeType();
            employeeType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return employeeType;
    }
    
    /*
    protected FamilyMember getFamilyMember(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        FamilyMember familyMember = (FamilyMember) OAObjectCacheDelegate.getObject(FamilyMember.class, id);
        if (familyMember == null) {
            familyMember = new FamilyMember();
            familyMember.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return familyMember;
    }
    */
    
    protected HindaOrder getHindaOrder(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        HindaOrder hindaOrder = (HindaOrder) OAObjectCacheDelegate.getObject(HindaOrder.class, id);
        if (hindaOrder == null) {
            hindaOrder = new HindaOrder();
            hindaOrder.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return hindaOrder;
    }
    
    protected HindaOrderLine getHindaOrderLine(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        HindaOrderLine hindaOrderLine = (HindaOrderLine) OAObjectCacheDelegate.getObject(HindaOrderLine.class, id);
        if (hindaOrderLine == null) {
            hindaOrderLine = new HindaOrderLine();
            hindaOrderLine.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return hindaOrderLine;
    }
    
    protected ImageStore getImageStore(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ImageStore imageStore = (ImageStore) OAObjectCacheDelegate.getObject(ImageStore.class, id);
        if (imageStore == null) {
            imageStore = new ImageStore();
            imageStore.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return imageStore;
    }
    
    protected Inspire getInspire(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Inspire inspire = (Inspire) OAObjectCacheDelegate.getObject(Inspire.class, id);
        if (inspire == null) {
            inspire = new Inspire();
            inspire.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspire;
    }
    
    protected InspireApproval getInspireApproval(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireApproval inspireApproval = (InspireApproval) OAObjectCacheDelegate.getObject(InspireApproval.class, id);
        if (inspireApproval == null) {
            inspireApproval = new InspireApproval();
            inspireApproval.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireApproval;
    }
    
    protected InspireAwardLevel getInspireAwardLevel(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireAwardLevel inspireAwardLevel = (InspireAwardLevel) OAObjectCacheDelegate.getObject(InspireAwardLevel.class, id);
        if (inspireAwardLevel == null) {
            inspireAwardLevel = new InspireAwardLevel();
            inspireAwardLevel.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireAwardLevel;
    }
    
    protected InspireAwardLevelLocationValue getInspireAwardLevelLocationValue(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireAwardLevelLocationValue inspireAwardLevelLocationValue = (InspireAwardLevelLocationValue) OAObjectCacheDelegate.getObject(InspireAwardLevelLocationValue.class, id);
        if (inspireAwardLevelLocationValue == null) {
            inspireAwardLevelLocationValue = new InspireAwardLevelLocationValue();
            inspireAwardLevelLocationValue.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireAwardLevelLocationValue;
    }
    
    protected InspireCoreValue getInspireCoreValue(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireCoreValue inspireCoreValue = (InspireCoreValue) OAObjectCacheDelegate.getObject(InspireCoreValue.class, id);
        if (inspireCoreValue == null) {
            inspireCoreValue = new InspireCoreValue();
            inspireCoreValue.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireCoreValue;
    }
    
    protected InspireOrder getInspireOrder(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireOrder inspireOrder = (InspireOrder) OAObjectCacheDelegate.getObject(InspireOrder.class, id);
        if (inspireOrder == null) {
            inspireOrder = new InspireOrder();
            inspireOrder.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireOrder;
    }
    
    protected InspireOrderCharity getInspireOrderCharity(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireOrderCharity inspireOrderCharity = (InspireOrderCharity) OAObjectCacheDelegate.getObject(InspireOrderCharity.class, id);
        if (inspireOrderCharity == null) {
            inspireOrderCharity = new InspireOrderCharity();
            inspireOrderCharity.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireOrderCharity;
    }
    
    protected InspireOrderItem getInspireOrderItem(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireOrderItem inspireOrderItem = (InspireOrderItem) OAObjectCacheDelegate.getObject(InspireOrderItem.class, id);
        if (inspireOrderItem == null) {
            inspireOrderItem = new InspireOrderItem();
            inspireOrderItem.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireOrderItem;
    }
    
    protected InspireRecipient getInspireRecipient(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        InspireRecipient inspireRecipient = (InspireRecipient) OAObjectCacheDelegate.getObject(InspireRecipient.class, id);
        if (inspireRecipient == null) {
            inspireRecipient = new InspireRecipient();
            inspireRecipient.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return inspireRecipient;
    }
    
    protected Item getItem(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Item item = (Item) OAObjectCacheDelegate.getObject(Item.class, id);
        if (item == null) {
            item = new Item();
            item.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return item;
    }
    
    protected ItemCategory getItemCategory(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ItemCategory itemCategory = (ItemCategory) OAObjectCacheDelegate.getObject(ItemCategory.class, id);
        if (itemCategory == null) {
            itemCategory = new ItemCategory();
            itemCategory.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return itemCategory;
    }
    
    protected ItemType getItemType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ItemType itemType = (ItemType) OAObjectCacheDelegate.getObject(ItemType.class, id);
        if (itemType == null) {
            itemType = new ItemType();
            itemType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return itemType;
    }
    
    protected ItemVendor getItemVendor(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ItemVendor itemVendor = (ItemVendor) OAObjectCacheDelegate.getObject(ItemVendor.class, id);
        if (itemVendor == null) {
            itemVendor = new ItemVendor();
            itemVendor.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return itemVendor;
    }
    
    /*
    protected LineItem getLineItem(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LineItem lineItem = (LineItem) OAObjectCacheDelegate.getObject(LineItem.class, id);
        if (lineItem == null) {
            lineItem = new LineItem();
            lineItem.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return lineItem;
    }
    */
    protected Location getLocation(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Location location = (Location) OAObjectCacheDelegate.getObject(Location.class, id);
        if (location == null) {
            location = new Location();
            location.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return location;
    }
    
    protected LocationPageGroup getLocationPageGroup(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LocationPageGroup locationPageGroup = (LocationPageGroup) OAObjectCacheDelegate.getObject(LocationPageGroup.class, id);
        if (locationPageGroup == null) {
            locationPageGroup = new LocationPageGroup();
            locationPageGroup.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return locationPageGroup;
    }
    
    protected LocationPageInfo getLocationPageInfo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LocationPageInfo locationPageInfo = (LocationPageInfo) OAObjectCacheDelegate.getObject(LocationPageInfo.class, id);
        if (locationPageInfo == null) {
            locationPageInfo = new LocationPageInfo();
            locationPageInfo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return locationPageInfo;
    }
    
    protected LocationType getLocationType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LocationType locationType = (LocationType) OAObjectCacheDelegate.getObject(LocationType.class, id);
        if (locationType == null) {
            locationType = new LocationType();
            locationType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return locationType;
    }
    
    protected LoginImage getLoginImage(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LoginImage loginImage = (LoginImage) OAObjectCacheDelegate.getObject(LoginImage.class, id);
        if (loginImage == null) {
            loginImage = new LoginImage();
            loginImage.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return loginImage;
    }
    
    protected LoginImageSet getLoginImageSet(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        LoginImageSet loginImageSet = (LoginImageSet) OAObjectCacheDelegate.getObject(LoginImageSet.class, id);
        if (loginImageSet == null) {
            loginImageSet = new LoginImageSet();
            loginImageSet.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return loginImageSet;
    }
    
    /*
    protected Lot getLot(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Lot lot = (Lot) OAObjectCacheDelegate.getObject(Lot.class, id);
        if (lot == null) {
            lot = new Lot();
            lot.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return lot;
    }
    */
    protected Merchant getMerchant(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Merchant merchant = (Merchant) OAObjectCacheDelegate.getObject(Merchant.class, id);
        if (merchant == null) {
            merchant = new Merchant();
            merchant.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return merchant;
    }
    
    protected MerchantCategory getMerchantCategory(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        MerchantCategory merchantCategory = (MerchantCategory) OAObjectCacheDelegate.getObject(MerchantCategory.class, id);
        if (merchantCategory == null) {
            merchantCategory = new MerchantCategory();
            merchantCategory.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return merchantCategory;
    }
    
    protected OrderItemTracking getOrderItemTracking(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        OrderItemTracking orderItemTracking = (OrderItemTracking) OAObjectCacheDelegate.getObject(OrderItemTracking.class, id);
        if (orderItemTracking == null) {
            orderItemTracking = new OrderItemTracking();
            orderItemTracking.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return orderItemTracking;
    }
    
    protected OrderTracking getOrderTracking(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        OrderTracking orderTracking = (OrderTracking) OAObjectCacheDelegate.getObject(OrderTracking.class, id);
        if (orderTracking == null) {
            orderTracking = new OrderTracking();
            orderTracking.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return orderTracking;
    }
    
    protected OrderTrackingStatus getOrderTrackingStatus(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        OrderTrackingStatus orderTrackingStatus = (OrderTrackingStatus) OAObjectCacheDelegate.getObject(OrderTrackingStatus.class, id);
        if (orderTrackingStatus == null) {
            orderTrackingStatus = new OrderTrackingStatus();
            orderTrackingStatus.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return orderTrackingStatus;
    }
    
    protected Page getPage(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Page page = (Page) OAObjectCacheDelegate.getObject(Page.class, id);
        if (page == null) {
            page = new Page();
            page.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return page;
    }
    
    protected PageGroup getPageGroup(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PageGroup pageGroup = (PageGroup) OAObjectCacheDelegate.getObject(PageGroup.class, id);
        if (pageGroup == null) {
            pageGroup = new PageGroup();
            pageGroup.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pageGroup;
    }
    
    protected PageGroupPageInfo getPageGroupPageInfo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PageGroupPageInfo pageGroupPageInfo = (PageGroupPageInfo) OAObjectCacheDelegate.getObject(PageGroupPageInfo.class, id);
        if (pageGroupPageInfo == null) {
            pageGroupPageInfo = new PageGroupPageInfo();
            pageGroupPageInfo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pageGroupPageInfo;
    }
    
    protected PageInfo getPageInfo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PageInfo pageInfo = (PageInfo) OAObjectCacheDelegate.getObject(PageInfo.class, id);
        if (pageInfo == null) {
            pageInfo = new PageInfo();
            pageInfo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pageInfo;
    }
    
    protected PageTheme getPageTheme(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PageTheme pageTheme = (PageTheme) OAObjectCacheDelegate.getObject(PageTheme.class, id);
        if (pageTheme == null) {
            pageTheme = new PageTheme();
            pageTheme.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pageTheme;
    }
    
    protected PageThemePageInfo getPageThemePageInfo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PageThemePageInfo pageThemePageInfo = (PageThemePageInfo) OAObjectCacheDelegate.getObject(PageThemePageInfo.class, id);
        if (pageThemePageInfo == null) {
            pageThemePageInfo = new PageThemePageInfo();
            pageThemePageInfo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pageThemePageInfo;
    }
    
    protected Phone getPhone(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Phone phone = (Phone) OAObjectCacheDelegate.getObject(Phone.class, id);
        if (phone == null) {
            phone = new Phone();
            phone.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return phone;
    }
    
    protected PhoneType getPhoneType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PhoneType phoneType = (PhoneType) OAObjectCacheDelegate.getObject(PhoneType.class, id);
        if (phoneType == null) {
            phoneType = new PhoneType();
            phoneType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return phoneType;
    }
    
    protected PointsApproval getPointsApproval(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsApproval pointsApproval = (PointsApproval) OAObjectCacheDelegate.getObject(PointsApproval.class, id);
        if (pointsApproval == null) {
            pointsApproval = new PointsApproval();
            pointsApproval.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsApproval;
    }
    
    protected PointsAwardLevel getPointsAwardLevel(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsAwardLevel pointsAwardLevel = (PointsAwardLevel) OAObjectCacheDelegate.getObject(PointsAwardLevel.class, id);
        if (pointsAwardLevel == null) {
            pointsAwardLevel = new PointsAwardLevel();
            pointsAwardLevel.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsAwardLevel;
    }
    
    protected PointsConfiguration getPointsConfiguration(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsConfiguration pointsConfiguration = (PointsConfiguration) OAObjectCacheDelegate.getObject(PointsConfiguration.class, id);
        if (pointsConfiguration == null) {
            pointsConfiguration = new PointsConfiguration();
            pointsConfiguration.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsConfiguration;
    }
    
    protected PointsCoreValue getPointsCoreValue(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsCoreValue pointsCoreValue = (PointsCoreValue) OAObjectCacheDelegate.getObject(PointsCoreValue.class, id);
        if (pointsCoreValue == null) {
            pointsCoreValue = new PointsCoreValue();
            pointsCoreValue.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsCoreValue;
    }
    
    protected PointsRecord getPointsRecord(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsRecord pointsRecord = (PointsRecord) OAObjectCacheDelegate.getObject(PointsRecord.class, id);
        if (pointsRecord == null) {
            pointsRecord = new PointsRecord();
            pointsRecord.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsRecord;
    }
    
    protected PointsRequest getPointsRequest(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        PointsRequest pointsRequest = (PointsRequest) OAObjectCacheDelegate.getObject(PointsRequest.class, id);
        if (pointsRequest == null) {
            pointsRequest = new PointsRequest();
            pointsRequest.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return pointsRequest;
    }
    
    protected Product getProduct(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Product product = (Product) OAObjectCacheDelegate.getObject(Product.class, id);
        if (product == null) {
            product = new Product();
            product.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return product;
    }
    
    protected ProductAudit getProductAudit(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProductAudit productAudit = (ProductAudit) OAObjectCacheDelegate.getObject(ProductAudit.class, id);
        if (productAudit == null) {
            productAudit = new ProductAudit();
            productAudit.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return productAudit;
    }
    
    protected Program getProgram(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Program program = (Program) OAObjectCacheDelegate.getObject(Program.class, id);
        if (program == null) {
            program = new Program();
            program.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return program;
    }
    
    protected ProgramDocument getProgramDocument(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramDocument programDocument = (ProgramDocument) OAObjectCacheDelegate.getObject(ProgramDocument.class, id);
        if (programDocument == null) {
            programDocument = new ProgramDocument();
            programDocument.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programDocument;
    }
    
    protected ProgramEmailType getProgramEmailType(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramEmailType programEmailType = (ProgramEmailType) OAObjectCacheDelegate.getObject(ProgramEmailType.class, id);
        if (programEmailType == null) {
            programEmailType = new ProgramEmailType();
            programEmailType.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programEmailType;
    }
    
    protected ProgramEvent getProgramEvent(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramEvent programEvent = (ProgramEvent) OAObjectCacheDelegate.getObject(ProgramEvent.class, id);
        if (programEvent == null) {
            programEvent = new ProgramEvent();
            programEvent.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programEvent;
    }
    
    protected ProgramFaq getProgramFaq(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramFaq programFaq = (ProgramFaq) OAObjectCacheDelegate.getObject(ProgramFaq.class, id);
        if (programFaq == null) {
            programFaq = new ProgramFaq();
            programFaq.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programFaq;
    }
    
    protected ProgramPageGroup getProgramPageGroup(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramPageGroup programPageGroup = (ProgramPageGroup) OAObjectCacheDelegate.getObject(ProgramPageGroup.class, id);
        if (programPageGroup == null) {
            programPageGroup = new ProgramPageGroup();
            programPageGroup.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programPageGroup;
    }
    
    protected ProgramPageInfo getProgramPageInfo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ProgramPageInfo programPageInfo = (ProgramPageInfo) OAObjectCacheDelegate.getObject(ProgramPageInfo.class, id);
        if (programPageInfo == null) {
            programPageInfo = new ProgramPageInfo();
            programPageInfo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return programPageInfo;
    }
    
    protected Question getQuestion(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Question question = (Question) OAObjectCacheDelegate.getObject(Question.class, id);
        if (question == null) {
            question = new Question();
            question.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return question;
    }
    
    protected QuestionResult getQuestionResult(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        QuestionResult questionResult = (QuestionResult) OAObjectCacheDelegate.getObject(QuestionResult.class, id);
        if (questionResult == null) {
            questionResult = new QuestionResult();
            questionResult.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return questionResult;
    }
    
    protected Quiz getQuiz(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Quiz quiz = (Quiz) OAObjectCacheDelegate.getObject(Quiz.class, id);
        if (quiz == null) {
            quiz = new Quiz();
            quiz.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return quiz;
    }
    
    protected QuizResult getQuizResult(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        QuizResult quizResult = (QuizResult) OAObjectCacheDelegate.getObject(QuizResult.class, id);
        if (quizResult == null) {
            quizResult = new QuizResult();
            quizResult.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return quizResult;
    }
    
    protected Report getReport(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Report report = (Report) OAObjectCacheDelegate.getObject(Report.class, id);
        if (report == null) {
            report = new Report();
            report.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return report;
    }
    
    protected ReportClass getReportClass(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ReportClass reportClass = (ReportClass) OAObjectCacheDelegate.getObject(ReportClass.class, id);
        if (reportClass == null) {
            reportClass = new ReportClass();
            reportClass.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return reportClass;
    }
    
    protected Section getSection(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Section section = (Section) OAObjectCacheDelegate.getObject(Section.class, id);
        if (section == null) {
            section = new Section();
            section.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return section;
    }
    
    protected ShipTo getShipTo(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        ShipTo shipTo = (ShipTo) OAObjectCacheDelegate.getObject(ShipTo.class, id);
        if (shipTo == null) {
            shipTo = new ShipTo();
            shipTo.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return shipTo;
    }
    
    protected Survey getSurvey(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Survey survey = (Survey) OAObjectCacheDelegate.getObject(Survey.class, id);
        if (survey == null) {
            survey = new Survey();
            survey.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return survey;
    }
    
    protected SurveyAnswer getSurveyAnswer(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        SurveyAnswer surveyAnswer = (SurveyAnswer) OAObjectCacheDelegate.getObject(SurveyAnswer.class, id);
        if (surveyAnswer == null) {
            surveyAnswer = new SurveyAnswer();
            surveyAnswer.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return surveyAnswer;
    }
    
    protected SurveyQuestion getSurveyQuestion(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        SurveyQuestion surveyQuestion = (SurveyQuestion) OAObjectCacheDelegate.getObject(SurveyQuestion.class, id);
        if (surveyQuestion == null) {
            surveyQuestion = new SurveyQuestion();
            surveyQuestion.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return surveyQuestion;
    }
    
    protected User getUser(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        User user = (User) OAObjectCacheDelegate.getObject(User.class, id);
        if (user == null) {
            user = new User();
            user.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return user;
    }
    
    protected Value getValue(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Value value = (Value) OAObjectCacheDelegate.getObject(Value.class, id);
        if (value == null) {
            value = new Value();
            value.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return value;
    }
    
    protected Widget getWidget(ResultSet rs, DataAccessObject.ResultSetInfo rsi) throws SQLException {
        int id = rs.getInt(1);
        Widget widget = (Widget) OAObjectCacheDelegate.getObject(Widget.class, id);
        if (widget == null) {
            widget = new Widget();
            widget.load(rs, id);
        }
        else {
            rsi.setFoundInCache(true);
        }
        return widget;
    }
}
