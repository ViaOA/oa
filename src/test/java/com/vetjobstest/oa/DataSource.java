package com.vetjobstest.oa;

import java.sql.*;

import com.viaoa.ds.OASelect;
import com.viaoa.ds.autonumber.NextNumber;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;

public class DataSource {
    protected OADataSourceJDBC datasource;
    
    public OADataSourceJDBC getOADataSourceJDBC() {
    	return datasource;
    }
    
    public OADataSourceJDBC getOADataSource() {
        return datasource;
    }
    
    public DataSource() {
    }
    
    public void startup(String user, String password, String driver, String jdbcUrl, int databaseType, int max, int min) throws Exception {
        if (datasource == null) {
            Database db = createDatabase();
            DBMetaData dbmd = new DBMetaData(databaseType, user, password, driver, jdbcUrl);
            dbmd.setMaxConnections(max);
            dbmd.setMinConnections(min);
            datasource = new OADataSourceJDBC(db, dbmd);
        }
    }

    private Database createDatabase() {
        int NextNumber = 0;
        // TABLES
        int Employer = 1;
        int Job = 2;
        int Resume = 3;
        int Service = 4;
        int Rank = 5;
        int ServiceStatus = 6;
        int Education = 7;
        int Category = 8;
        int Folder = 9;
        int State = 10;
        int MilitaryJobCode = 11;
        int VetUser = 12;
        int VetSecurity = 13;
        int Location = 14;
        int BatchRow = 15;
        int Batch = 16;
        int EmployerUser = 17;
        int Privilege = 18;
        int Customer = 19;
        int Banner = 20;
        int Stat = 21;
        int BannerStat = 22;
        
        // LINK TABLES
        int JobCategoryLink = 23;
        int CategoryMilitaryJobCodeLink = 24;
        int VetUserCategoryLink = 25;
        int VetUserLocationLink = 26;
        int VetUserLocationLink1 = 27;
        int JobLocationLink = 28;
        int BatchRowCategoryLink = 29;
        int EmployerPrivilegeLink = 30;

        int EMPQUERY = 31;
        int EMPQUERYVET = 32;

        int MAX = 33;
        
        Database db = new Database();
        Table[] tables = new Table[MAX];
        Column[] columns;
        Link[] links;
        Column[] fkeys;
        
        // TABLES
        tables[NextNumber] = new Table("NextNumber", NextNumber.class); // ** Used by all OADataSource Database
        tables[Employer] = new Table("Employer",Employer.class);
        tables[Job] = new Table("Job",Job.class);
        tables[Resume] = new Table("Resume",Resume.class);
        tables[Service] = new Table("Service",Service.class);
        tables[Rank] = new Table("Rank",Rank.class);
        tables[ServiceStatus] = new Table("ServiceStatus",ServiceStatus.class);
        tables[Education] = new Table("Education",Education.class);
        tables[Category] = new Table("Category",Category.class);
        tables[Folder] = new Table("Folder",Folder.class);
        tables[State] = new Table("State",State.class);
        tables[MilitaryJobCode] = new Table("MilitaryJobCode",MilitaryJobCode.class);
        tables[VetUser] = new Table("VetUser",VetUser.class);
        tables[VetSecurity] = new Table("VetSecurity",VetSecurity.class);
        tables[Location] = new Table("Location",Location.class);
        tables[BatchRow] = new Table("BatchRow",BatchRow.class);
        tables[Batch] = new Table("Batch",Batch.class);
        tables[EmployerUser] = new Table("EmployerUser",EmployerUser.class);
        tables[Privilege] = new Table("Privilege",Privilege.class);
        tables[Customer] = new Table("Customer",Customer.class);
        tables[Banner] = new Table("Banner",Banner.class);
        tables[Stat] = new Table("Stat",Stat.class);
        tables[BannerStat] = new Table("BannerStat",BannerStat.class);
        tables[EMPQUERY] = new Table("EmpQuery",EmpQuery.class);
        tables[EMPQUERYVET] = new Table("EmpQueryVet",EmpQueryVet.class);
        
        // LINK TABLES
        tables[JobCategoryLink] = new Table("JobCategoryLink",true);
        tables[CategoryMilitaryJobCodeLink] = new Table("CategoryMilitaryJobCodeLink",true);
        tables[VetUserCategoryLink] = new Table("VetUserCategoryLink",true);
        tables[VetUserLocationLink] = new Table("VetUserLocationLink",true);
        tables[VetUserLocationLink1] = new Table("VetUserLocationLink1",true);
        tables[JobLocationLink] = new Table("JobLocationLink",true);
        tables[BatchRowCategoryLink] = new Table("BatchRowCategoryLink",true);
        tables[EmployerPrivilegeLink] = new Table("EmployerPrivilegeLink",true);
        
        // TABLE COLUMNS
        // NextNumber COLUMNS
        columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER, 5);
        tables[NextNumber].setColumns(columns);
        
        // Employer COLUMNS
        columns = new Column[22];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("createDate","createDate", Types.DATE, 10);
        columns[2] = new Column("company","company", Types.VARCHAR, 75);
        columns[3] = new Column("address1","address1",Types.VARCHAR,75);
        columns[4] = new Column("address2","address2",Types.VARCHAR,75);
        columns[5] = new Column("city","city",Types.VARCHAR,50);
        columns[6] = new Column("state","state",Types.VARCHAR,25);
        columns[7] = new Column("zip","zip",Types.VARCHAR,18);
        columns[8] = new Column("country","country",Types.VARCHAR,45);
        columns[9] = new Column("phone","phone",Types.VARCHAR,25);
        columns[10] = new Column("fax","fax",Types.VARCHAR,25);
        columns[11] = new Column("contact","contact",Types.VARCHAR,75);
        columns[12] = new Column("email","email",Types.VARCHAR,75);
        columns[13] = new Column("title","title",Types.VARCHAR,75);
        columns[14] = new Column("industry","industry",Types.VARCHAR,75);
        columns[15] = new Column("url","url",Types.VARCHAR,75);
        columns[16] = new Column("startDate","startDate",Types.DATE, 10);
        columns[17] = new Column("endDate","endDate",Types.DATE, 10);
        columns[18] = new Column("purchaseDate","purchaseDate",Types.DATE, 10);
        columns[19] = new Column("Note","Note",Types.CLOB,99999);
        columns[20] = new Column("parentEmployerId","");
        columns[21] = new Column("companyAlias","companyAlias", Types.VARCHAR, 75);
        tables[Employer].setColumns(columns);
        
        tables[Employer].addIndex(new Index("EmployerCompany", "Company"));
        tables[Employer].addIndex(new Index("EmployerContact", "Contact"));
        
        
        
        // Job COLUMNS
        columns = new Column[33];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("reference","reference",Types.VARCHAR,35);
        columns[2] = new Column("createDate","createDate",Types.DATE,10);
        columns[3] = new Column("refreshDate","refreshDate",Types.DATE,10);
        columns[4] = new Column("rateFrom","rateFrom",Types.FLOAT,8);
        columns[5] = new Column("rateTo","rateTo",Types.FLOAT,6);
        columns[6] = new Column("hourly","hourly",Types.BOOLEAN,6);
        columns[7] = new Column("contract","contract",Types.BOOLEAN,9);
        columns[8] = new Column("fulltime","fulltime",Types.BOOLEAN,8);
        columns[9] = new Column("title","title",Types.VARCHAR,75);
        columns[10] = new Column("benefits","benefits",Types.CLOB,999);
        columns[11] = new Column("description","description",Types.CLOB,999);
        columns[12] = new Column("city","city",Types.VARCHAR,50);
        columns[13] = new Column("region","region",Types.VARCHAR,50);
        columns[14] = new Column("state","state",Types.VARCHAR,30);
        columns[15] = new Column("country","country",Types.VARCHAR,45);
        columns[16] = new Column("contact","contact",Types.VARCHAR,75);
        columns[17] = new Column("email","email",Types.VARCHAR,200);
        columns[18] = new Column("autoResponse","autoResponse",Types.BOOLEAN,12);
        columns[19] = new Column("positionsAvailable","positionsAvailable",Types.INTEGER,17);
        columns[20] = new Column("folderId","");
        columns[21] = new Column("employerId","");
        columns[22] = new Column("viewCount","viewCount",Types.INTEGER,5);
        columns[23] = new Column("searchCount","searchCount",Types.INTEGER,5);
        columns[24] = new Column("clickCount","clickCount",Types.INTEGER,5);
        columns[25] = new Column("viewCountMTD","viewCountMTD",Types.INTEGER,5);
        columns[26] = new Column("searchCountMTD","searchCountMTD",Types.INTEGER,5);
        columns[27] = new Column("clickCountMTD","clickCountMTD",Types.INTEGER,5);
        columns[28] = new Column("viewCountWTD","viewCountWTD",Types.INTEGER,5);
        columns[29] = new Column("searchCountWTD","searchCountWTD",Types.INTEGER,5);
        columns[30] = new Column("clickCountWTD","clickCountWTD",Types.INTEGER,5);
        columns[31] = new Column("lastMTD","lastMTD",Types.DATE,5);
        columns[32] = new Column("lastWTD","lastWTD",Types.DATE,5);
        tables[Job].setColumns(columns);

        tables[Job].addIndex(new Index("JobState", "State"));
        tables[Job].addIndex(new Index("JobRefreshDate", "RefreshDate"));
        tables[Job].addIndex(new Index("JobEmployerId", new String[] {"EmployerId", "FolderId"}));
        tables[Job].addIndex(new Index("JobReference", "reference"));
        
        
        // Resume COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id","id",Types.INTEGER, 5);
        columns[0].primaryKey = true;
        columns[1] = new Column("title","title",Types.VARCHAR,255);
        columns[2] = new Column("createDate","createDate",Types.DATE,10);
        columns[3] = new Column("resume","resume",Types.CLOB,999);
        columns[4] = new Column("refreshDate","refreshDate",Types.DATE,10);
        columns[5] = new Column("summary","summary",Types.CLOB,999);
        columns[6] = new Column("hold","hold",Types.BOOLEAN,4);
        columns[7] = new Column("viewCount","viewCount",Types.INTEGER, 5);
        columns[8] = new Column("searchCount","searchCount",Types.INTEGER,5);
        columns[9] = new Column("clickCount","clickCount",Types.INTEGER,5);
        tables[Resume].setColumns(columns);
        tables[Resume].addIndex(new Index("ResumeRefreshDate", "RefreshDate"));

        // Service COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,50);
        tables[Service].setColumns(columns);
        
        // Rank COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,50);
        tables[Rank].setColumns(columns);
        
        // ServiceStatus COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,50);
        tables[ServiceStatus].setColumns(columns);
        
        // Education COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,75);
        tables[Education].setColumns(columns);

        // EmpQuery COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id","id",Types.INTEGER,6);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("queryText", "queryText", Types.VARCHAR,254);
        columns[2] = new Column("queryDescription", "queryDescription", Types.CLOB,9999);
        columns[3] = new Column("date", "date", Types.DATE,75);
        // columns[4] = new Column("time", "time",75);
        columns[4] = new Column("employerUserId", "");
        tables[EMPQUERY].setColumns(columns);
        tables[EMPQUERY].addIndex(new Index("EmpQueryUserId", "employerUserId"));

        
        // EmpQueryVet COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id","id",Types.INTEGER,6);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("viewType", "viewType", Types.INTEGER,5);
        columns[2] = new Column("empQueryId", "");
        columns[3] = new Column("vetUserId", "");
        tables[EMPQUERYVET].setColumns(columns);
        tables[EMPQUERYVET].addIndex(new Index("empQueryId", "empQueryId"));
        tables[EMPQUERYVET].addIndex(new Index("empQueryUserId", "vetUserId"));
        
        
        // Category COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,75);
        columns[2] = new Column("parentCategoryId","");
        tables[Category].setColumns(columns);
        
        // Folder COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,75);
        columns[2] = new Column("description","description",Types.VARCHAR,120);
        columns[3] = new Column("hide","hide",Types.BOOLEAN,5);
        tables[Folder].setColumns(columns);
        
        // State COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("abbrev","abbrev",Types.VARCHAR,15);
        columns[2] = new Column("name","name",Types.VARCHAR,70);
        tables[State].setColumns(columns);
        
        // MilitaryJobCode COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id","id",Types.INTEGER,7);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("jobCode","jobCode",Types.VARCHAR,35);
        columns[2] = new Column("description","description",Types.VARCHAR,70);
        tables[MilitaryJobCode].setColumns(columns);
        
        // VetUser COLUMNS
        columns = new Column[30];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("createDate","createDate",Types.DATE,10);
        columns[2] = new Column("loginId","loginId",Types.VARCHAR,20);
        columns[3] = new Column("password","password",Types.VARCHAR,20);
        columns[4] = new Column("firstName","firstName",Types.VARCHAR,55);
        columns[5] = new Column("lastName","lastName",Types.VARCHAR,55);
        columns[6] = new Column("address1","address1",Types.VARCHAR,75);
        columns[7] = new Column("address2","address2",Types.VARCHAR,75);
        columns[8] = new Column("city","city",Types.VARCHAR,55);
        columns[9] = new Column("state","state",Types.VARCHAR,25);
        columns[10] = new Column("zip","zip",Types.VARCHAR,18);
        columns[11] = new Column("country","country",Types.VARCHAR,45);
        columns[12] = new Column("phone","phone",Types.VARCHAR,55);
        columns[13] = new Column("email","email",Types.VARCHAR,75);
        columns[14] = new Column("relocate","relocate",Types.BOOLEAN,8);
        columns[15] = new Column("minIncomeYear","minIncomeYear",Types.INTEGER,14);
        columns[16] = new Column("minIncomeHourly","minIncomeHourly",Types.FLOAT,16);
        columns[17] = new Column("availableDate","availableDate",Types.DATE,10);
        columns[18] = new Column("inactiveDate","inactiveDate",Types.DATE,10);
        columns[19] = new Column("spouseName","spouseName",Types.VARCHAR,70);
        columns[20] = new Column("lastYearService","lastYearService",Types.INTEGER,15);
        columns[21] = new Column("totalYears","totalYears",Types.INTEGER,10);
        columns[22] = new Column("militaryMoveAvail","militaryMoveAvail",Types.BOOLEAN,17);
        columns[23] = new Column("vetFlag","vetFlag",Types.BOOLEAN,7);
        columns[24] = new Column("educationId","");
        columns[25] = new Column("vetSecurityId","");
        columns[26] = new Column("rankId","");
        columns[27] = new Column("serviceId","");
        columns[28] = new Column("serviceStatusId","");
//vvvv        
        columns[29] = new Column("resumeId","");
        
        tables[VetUser].setColumns(columns);
        tables[VetUser].addIndex(new Index("VetUserState", "State"));
        tables[VetUser].addIndex(new Index("VetUserLoginId", "LoginId"));
        
        
        // VetSecurity COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,50);
        columns[2] = new Column("parentVetSecurityId","");
        tables[VetSecurity].setColumns(columns);
        
        // Location COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name","name",Types.VARCHAR,75);
        columns[2] = new Column("parentLocationId","");
        tables[Location].setColumns(columns);
        
        // BatchRow COLUMNS
        columns = new Column[29];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("reference","reference",Types.VARCHAR,25);
        columns[2] = new Column("rateFrom","rateFrom",Types.VARCHAR,15);
        columns[3] = new Column("rateTo","rateTo",Types.VARCHAR,15);
        columns[4] = new Column("hourly","hourly",Types.VARCHAR,15);
        columns[5] = new Column("benefits","benefits",Types.CLOB,1000);
        columns[6] = new Column("city","city",Types.VARCHAR,30);
        columns[7] = new Column("region","region",Types.VARCHAR,30);
        columns[8] = new Column("state","state",Types.VARCHAR,25);
        columns[9] = new Column("country","country",Types.VARCHAR,45);
        columns[10] = new Column("title","title",Types.VARCHAR,75);
        columns[11] = new Column("description","description",Types.CLOB,5000);
        columns[12] = new Column("contact","contact",Types.VARCHAR,50);
        columns[13] = new Column("email","email",Types.VARCHAR,200);
        columns[14] = new Column("positions","positions",Types.VARCHAR,15);
        columns[15] = new Column("origCategory1","origCategory1",Types.VARCHAR,100);
        columns[16] = new Column("origCategory2","origCategory2",Types.VARCHAR,100);
        columns[17] = new Column("origCategory3","origCategory3",Types.VARCHAR,100);
        columns[18] = new Column("origCategory4","origCategory4",Types.VARCHAR,100);
        columns[19] = new Column("origCategory5","origCategory5",Types.VARCHAR,100);
        columns[20] = new Column("contract","contract",Types.VARCHAR,30);
        columns[21] = new Column("errorFlag","errorFlag",Types.BOOLEAN,5);
        columns[22] = new Column("newFlag","newFlag",Types.BOOLEAN,7);
        columns[23] = new Column("error","error",Types.VARCHAR,200);
        columns[24] = new Column("fulltime","fulltime",Types.VARCHAR,30);
        columns[25] = new Column("jobId","");
        columns[26] = new Column("folderId","");
        columns[27] = new Column("locationId","");
        columns[28] = new Column("batchId","");
        tables[BatchRow].setColumns(columns);
        tables[BatchRow].addIndex(new Index("BatchRowBatchId", "BatchId"));
        
        
        // Batch COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("fileName","fileName",Types.VARCHAR,80);
        columns[2] = new Column("loadDate","loadDate",Types.DATE,10);
        columns[3] = new Column("processDate","processDate",Types.DATE,10);
        columns[4] = new Column("qtyRow","qtyRow",Types.INTEGER,6);
        columns[5] = new Column("qtyReject","qtyReject",Types.INTEGER,9);
        columns[6] = new Column("qtyNew","qtyNew",Types.INTEGER,6);
        columns[7] = new Column("employerId","");
        tables[Batch].setColumns(columns);
        tables[Batch].addIndex(new Index("BatchEmployerId", "EmployerId"));

        
        // EmployerUser COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("description","description",Types.VARCHAR,50);
        columns[2] = new Column("loginId","loginId",Types.VARCHAR,20);
        columns[3] = new Column("password","password",Types.VARCHAR,20);
        columns[4] = new Column("email","email",Types.VARCHAR,75);
        columns[5] = new Column("name","name",Types.VARCHAR,75);
        columns[6] = new Column("admin","admin",Types.BOOLEAN,5);
        columns[7] = new Column("employerId","");
        tables[EmployerUser].setColumns(columns);
        tables[EmployerUser].addIndex(new Index("EmployerUserEmployerId", "EmployerId"));
        tables[EmployerUser].addIndex(new Index("EmployerUserLoginId", "LoginId"));
        
        // Privilege COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("description","description",Types.VARCHAR,75);
        tables[Privilege].setColumns(columns);
        
        // Customer COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("company","company",Types.VARCHAR,75);
        columns[2] = new Column("contact","contact",Types.VARCHAR,75);
        columns[3] = new Column("phone","phone",Types.VARCHAR,25);
        tables[Customer].setColumns(columns);
        
        // Banner COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("createDate","createDate",Types.DATE,10);
        columns[2] = new Column("beginDate","beginDate",Types.DATE,10);
        columns[3] = new Column("endDate","endDate",Types.DATE,10);
        columns[4] = new Column("imageUrl","imageUrl",Types.VARCHAR,200);
        columns[5] = new Column("forwardUrl","forwardUrl",Types.VARCHAR,200);
        columns[6] = new Column("altTag","altTag",Types.VARCHAR,75);
        columns[7] = new Column("group","group",Types.INTEGER,12);
        columns[8] = new Column("description","description",Types.VARCHAR,50);
        columns[9] = new Column("customerId","");
        tables[Banner].setColumns(columns);
        tables[Banner].addIndex(new Index("BannerBeginDate", "BeginDate"));
        
        // Stat COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("date","date",Types.DATE,10);
        columns[2] = new Column("employerLoginCount","employerLoginCount",Types.INTEGER,9);
        columns[3] = new Column("vetUserLoginCount","vetUserLoginCount",Types.INTEGER,17);
        tables[Stat].setColumns(columns);
        tables[Stat].addIndex(new Index("StatDate", "Date"));
        
        // BannerStat COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id","id",Types.INTEGER,5);
        columns[0].primaryKey = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("displayCount","displayCount",Types.INTEGER,5);
        columns[2] = new Column("hitCount","hitCount",Types.INTEGER,8);
        columns[3] = new Column("statId","");
        columns[4] = new Column("bannerId","");
        tables[BannerStat].setColumns(columns);
        tables[BannerStat].addIndex(new Index("BannerStatStatId", "StatId"));
        tables[BannerStat].addIndex(new Index("BannerStatBannerId", "BannerId"));
        
        
        // Link Tables Columns
        
        // JobCategoryLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("JobId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("CategoryId",null);
        columns[1].primaryKey = true;
        tables[JobCategoryLink].setColumns(columns);
        tables[JobCategoryLink].addIndex(new Index("JobCategoryLink1","JobId"));
        tables[JobCategoryLink].addIndex(new Index("JobCategoryLink2", "CategoryId"));

        // CategoryMilitaryJobCodeLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("CategoryId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("MilitaryJobCodeId",null);
        columns[1].primaryKey = true;
        tables[CategoryMilitaryJobCodeLink].setColumns(columns);

        // VetUserCategoryLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("VetUserId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("CategoryId",null);
        columns[1].primaryKey = true;
        tables[VetUserCategoryLink].setColumns(columns);
        tables[VetUserCategoryLink].addIndex(new Index("VetUserCategoryLink1", "VetUserId"));
        tables[VetUserCategoryLink].addIndex(new Index("VetUserCategoryLink2", "CategoryId"));
        
        // VetUserLocationLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("VetUserId",null);
        columns[1].primaryKey = true;
        tables[VetUserLocationLink].setColumns(columns);
        
        // VetUserLocationLink1 COLUMNS
        columns = new Column[2];
        columns[0] = new Column("LocationId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("VetUserId",null);
        columns[1].primaryKey = true;
        tables[VetUserLocationLink1].setColumns(columns);
        
        // JobLocationLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("JobId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("LocationId",null);
        columns[1].primaryKey = true;
        tables[JobLocationLink].setColumns(columns);
        tables[JobLocationLink].addIndex(new Index("JobLocationLink2", "LocationId"));
        
        // BatchRowCategoryLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("BatchRowId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("CategoryId",null);
        columns[1].primaryKey = true;
        tables[BatchRowCategoryLink].setColumns(columns);
        
        // EmployerPrivilegeLink COLUMNS
        columns = new Column[2];
        columns[0] = new Column("EmployerId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("PrivilegeId",null);
        columns[1].primaryKey = true;
        tables[EmployerPrivilegeLink].setColumns(columns);
        tables[EmployerPrivilegeLink].addIndex(new Index("EmployerPrivilegeLink2", "PrivilegeId"));

        
        // LINKS
        
        // Employer LINKS
        links = new Link[6];
        links[0] = new Link("jobs","employer",tables[Job]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("employers","parentEmployer",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[0];
        links[1].fkeys = fkeys;
        links[2] = new Link("parentEmployer","employers",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[20];
        links[2].fkeys = fkeys;
        links[3] = new Link("batches","employer",tables[Batch]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[0];
        links[3].fkeys = fkeys;
        links[4] = new Link("employerUsers","employer",tables[EmployerUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[0];
        links[4].fkeys = fkeys;
        links[5] = new Link("privileges","employers",tables[EmployerPrivilegeLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Employer].getColumns()[0];
        links[5].fkeys = fkeys;
        tables[Employer].setLinks(links);
        
        // Job LINKS
        links = new Link[5];
        links[0] = new Link("folder","jobs",tables[Folder]);
        fkeys = new Column[1];
        fkeys[0] = tables[Job].getColumns()[20];
        links[0].fkeys = fkeys;
        links[1] = new Link("employer","jobs",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[Job].getColumns()[21];
        links[1].fkeys = fkeys;
        links[2] = new Link("categories","jobs",tables[JobCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Job].getColumns()[0];
        links[2].fkeys = fkeys;
        links[3] = new Link("locations","jobs",tables[JobLocationLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Job].getColumns()[0];
        links[3].fkeys = fkeys;
        links[4] = new Link("batchRows","job",tables[BatchRow]);
        fkeys = new Column[1];
        fkeys[0] = tables[Job].getColumns()[0];
        links[4].fkeys = fkeys;
        tables[Job].setLinks(links);
        
        // Resume LINKS
        links = new Link[1];
        links[0] = new Link("vetUser","resume",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[Resume].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Resume].setLinks(links);
        
        // Service LINKS
        links = new Link[1];
        links[0] = new Link("vetUsers","service",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[Service].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Service].setLinks(links);
        
        // Rank LINKS
        links = new Link[1];
        links[0] = new Link("vetUsers","rank",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[Rank].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Rank].setLinks(links);
        
        // ServiceStatus LINKS
        links = new Link[1];
        links[0] = new Link("vetUsers","serviceStatus",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[ServiceStatus].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[ServiceStatus].setLinks(links);
        
        // Education LINKS
        links = new Link[1];
        links[0] = new Link("vetUsers","education",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[Education].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Education].setLinks(links);
        
        // Category LINKS
        links = new Link[6];
        links[0] = new Link("jobs","categories",tables[JobCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("militaryJobCodes","categories",tables[CategoryMilitaryJobCodeLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[0];
        links[1].fkeys = fkeys;
        links[2] = new Link("categories","parentCategory",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[0];
        links[2].fkeys = fkeys;
        links[3] = new Link("parentCategory","categories",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[2];
        links[3].fkeys = fkeys;
        links[4] = new Link("vetUsers","categories",tables[VetUserCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[0];
        links[4].fkeys = fkeys;
        links[5] = new Link("batchRows","categories",tables[BatchRowCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Category].getColumns()[0];
        links[5].fkeys = fkeys;
        tables[Category].setLinks(links);
        
        // Folder LINKS
        links = new Link[2];
        links[0] = new Link("jobs","folder",tables[Job]);
        fkeys = new Column[1];
        fkeys[0] = tables[Folder].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("batchRows","folder",tables[BatchRow]);
        fkeys = new Column[1];
        fkeys[0] = tables[Folder].getColumns()[0];
        links[1].fkeys = fkeys;
        tables[Folder].setLinks(links);
        
        // State LINKS
        links = new Link[0];
        tables[State].setLinks(links);
        
        // MilitaryJobCode LINKS
        links = new Link[1];
        links[0] = new Link("categories","militaryJobCodes",tables[CategoryMilitaryJobCodeLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[MilitaryJobCode].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[MilitaryJobCode].setLinks(links);
        
        // VetUser LINKS
        links = new Link[10];
        links[0] = new Link("resume","vetUser",tables[Resume]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[0];
//vvvv        
fkeys[0] = tables[VetUser].getColumns()[29];
        links[0].fkeys = fkeys;
        links[1] = new Link("categories","vetUsers",tables[VetUserCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[0];
        links[1].fkeys = fkeys;
        links[2] = new Link("education","vetUsers",tables[Education]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[24];
        links[2].fkeys = fkeys;
        links[3] = new Link("preferLocations","vetUsers",tables[VetUserLocationLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[0];
        links[3].fkeys = fkeys;
        links[4] = new Link("rejectLocations","vetUsers",tables[VetUserLocationLink1]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[0];
        links[4].fkeys = fkeys;
        links[5] = new Link("vetSecurity","vetUsers",tables[VetSecurity]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[25];
        links[5].fkeys = fkeys;
        links[6] = new Link("rank","vetUsers",tables[Rank]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[26];
        links[6].fkeys = fkeys;
        links[7] = new Link("service","vetUsers",tables[Service]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[27];
        links[7].fkeys = fkeys;
        links[8] = new Link("serviceStatus","vetUsers",tables[ServiceStatus]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[28];
        links[8].fkeys = fkeys;
        links[9] = new Link("empQueryVets","vetUser",tables[EMPQUERYVET]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUser].getColumns()[0];
        links[9].fkeys = fkeys;
        tables[VetUser].setLinks(links);
        
        
        
        // VetSecurity LINKS
        links = new Link[3];
        links[0] = new Link("vetSecurities","parentVetSecurity",tables[VetSecurity]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetSecurity].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("parentVetSecurity","vetSecurities",tables[VetSecurity]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetSecurity].getColumns()[2];
        links[1].fkeys = fkeys;
        links[2] = new Link("vetUsers","vetSecurity",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetSecurity].getColumns()[0];
        links[2].fkeys = fkeys;
        tables[VetSecurity].setLinks(links);
        
        // Location LINKS
        links = new Link[6];
        links[0] = new Link("vetUsers","preferLocations",tables[VetUserLocationLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("vetUsers","rejectLocations",tables[VetUserLocationLink1]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[0];
        links[1].fkeys = fkeys;
        links[2] = new Link("locations","parentLocation",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[0];
        links[2].fkeys = fkeys;
        links[3] = new Link("parentLocation","locations",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[2];
        links[3].fkeys = fkeys;
        links[4] = new Link("jobs","locations",tables[JobLocationLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[0];
        links[4].fkeys = fkeys;
        links[5] = new Link("batchRows","location",tables[BatchRow]);
        fkeys = new Column[1];
        fkeys[0] = tables[Location].getColumns()[0];
        links[5].fkeys = fkeys;
        tables[Location].setLinks(links);
        
        // BatchRow LINKS
        links = new Link[5];
        links[0] = new Link("job","batchRows",tables[Job]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRow].getColumns()[25];
        links[0].fkeys = fkeys;
        links[1] = new Link("categories","batchRows",tables[BatchRowCategoryLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRow].getColumns()[0];
        links[1].fkeys = fkeys;
        links[2] = new Link("folder","batchRows",tables[Folder]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRow].getColumns()[26];
        links[2].fkeys = fkeys;
        links[3] = new Link("location","batchRows",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRow].getColumns()[27];
        links[3].fkeys = fkeys;
        links[4] = new Link("batch","batchRows",tables[Batch]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRow].getColumns()[28];
        links[4].fkeys = fkeys;
        tables[BatchRow].setLinks(links);
        
        // Batch LINKS
        links = new Link[2];
        links[0] = new Link("employer","batches",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[Batch].getColumns()[7];
        links[0].fkeys = fkeys;
        links[1] = new Link("batchRows","batch",tables[BatchRow]);
        fkeys = new Column[1];
        fkeys[0] = tables[Batch].getColumns()[0];
        links[1].fkeys = fkeys;
        tables[Batch].setLinks(links);
        
        // EmployerUser LINKS
        links = new Link[2];
        links[0] = new Link("employer","employerUsers",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[EmployerUser].getColumns()[7];
        links[0].fkeys = fkeys;
        links[1] = new Link("empQueries","employerUser",tables[EMPQUERY]);
        fkeys = new Column[1];
        fkeys[0] = tables[EmployerUser].getColumns()[0];
        links[1].fkeys = fkeys;
        tables[EmployerUser].setLinks(links);
        
        // Privilege LINKS
        links = new Link[1];
        links[0] = new Link("employers","privileges",tables[EmployerPrivilegeLink]);
        fkeys = new Column[1];
        fkeys[0] = tables[Privilege].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Privilege].setLinks(links);
        
        // Customer LINKS
        links = new Link[1];
        links[0] = new Link("banners","customer",tables[Banner]);
        fkeys = new Column[1];
        fkeys[0] = tables[Customer].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Customer].setLinks(links);
        
        // Banner LINKS
        links = new Link[2];
        links[0] = new Link("customer","banners",tables[Customer]);
        fkeys = new Column[1];
        fkeys[0] = tables[Banner].getColumns()[9];
        links[0].fkeys = fkeys;
        links[1] = new Link("bannerStats","banner",tables[BannerStat]);
        fkeys = new Column[1];
        fkeys[0] = tables[Banner].getColumns()[0];
        links[1].fkeys = fkeys;
        tables[Banner].setLinks(links);
        
        // Stat LINKS
        links = new Link[1];
        links[0] = new Link("bannerStats","stat",tables[BannerStat]);
        fkeys = new Column[1];
        fkeys[0] = tables[Stat].getColumns()[0];
        links[0].fkeys = fkeys;
        tables[Stat].setLinks(links);
        
        // BannerStat LINKS
        links = new Link[2];
        links[0] = new Link("stat","bannerStats",tables[Stat]);
        fkeys = new Column[1];
        fkeys[0] = tables[BannerStat].getColumns()[3];
        links[0].fkeys = fkeys;
        links[1] = new Link("banner","bannerStats",tables[Banner]);
        fkeys = new Column[1];
        fkeys[0] = tables[BannerStat].getColumns()[4];
        links[1].fkeys = fkeys;
        tables[BannerStat].setLinks(links);
        
        // Link Tables Links
        
        // JobCategoryLink LINKS
        links = new Link[2];
        links[0] = new Link("jobs","categories",tables[Job]);
        fkeys = new Column[1];
        fkeys[0] = tables[JobCategoryLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("categories","jobs",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[JobCategoryLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[JobCategoryLink].setLinks(links);
        
        // CategoryMilitaryJobCodeLink LINKS
        links = new Link[2];
        links[0] = new Link("categories","militaryJobCodes",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[CategoryMilitaryJobCodeLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("militaryJobCodes","categories",tables[MilitaryJobCode]);
        fkeys = new Column[1];
        fkeys[0] = tables[CategoryMilitaryJobCodeLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[CategoryMilitaryJobCodeLink].setLinks(links);
        
        // VetUserCategoryLink LINKS
        links = new Link[2];
        links[0] = new Link("vetUsers","categories",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserCategoryLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("categories","vetUsers",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserCategoryLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[VetUserCategoryLink].setLinks(links);
        
        // VetUserLocationLink LINKS
        links = new Link[2];
        links[0] = new Link("preferLocations","vetUsers",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserLocationLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("vetUsers","preferLocations",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserLocationLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[VetUserLocationLink].setLinks(links);
        
        // VetUserLocationLink1 LINKS
        links = new Link[2];
        links[0] = new Link("rejectLocations","vetUsers",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserLocationLink1].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("vetUsers","rejectLocations",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[VetUserLocationLink1].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[VetUserLocationLink1].setLinks(links);
        
        // JobLocationLink LINKS
        links = new Link[2];
        links[0] = new Link("jobs","locations",tables[Job]);
        fkeys = new Column[1];
        fkeys[0] = tables[JobLocationLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("locations","jobs",tables[Location]);
        fkeys = new Column[1];
        fkeys[0] = tables[JobLocationLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[JobLocationLink].setLinks(links);
        
        // BatchRowCategoryLink LINKS
        links = new Link[2];
        links[0] = new Link("batchRows","categories",tables[BatchRow]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRowCategoryLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("categories","batchRows",tables[Category]);
        fkeys = new Column[1];
        fkeys[0] = tables[BatchRowCategoryLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[BatchRowCategoryLink].setLinks(links);
        
        // EmployerPrivilegeLink LINKS
        links = new Link[2];
        links[0] = new Link("employers","privileges",tables[Employer]);
        fkeys = new Column[1];
        fkeys[0] = tables[EmployerPrivilegeLink].getColumns()[0];
        links[0].fkeys = fkeys;
        links[1] = new Link("privileges","employers",tables[Privilege]);
        fkeys = new Column[1];
        fkeys[0] = tables[EmployerPrivilegeLink].getColumns()[1];
        links[1].fkeys = fkeys;
        tables[EmployerPrivilegeLink].setLinks(links);

        
        // EmpQuery LINKS
        links = new Link[2];
        links[0] = new Link("employerUser","empQueries",tables[EmployerUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[EMPQUERY].getColumns()[4];
        links[0].fkeys = fkeys;
        links[1] = new Link("empQueryVets", "empQuery", tables[EMPQUERYVET]);
        fkeys = new Column[1];
        fkeys[0] = tables[EMPQUERY].getColumns()[0];
        links[1].fkeys = fkeys;
        tables[EMPQUERY].setLinks(links);
        
        // EmpQueryVet LINKS
        links = new Link[2];
        links[0] = new Link("empQuery","empQueryVets",tables[EMPQUERY]);
        fkeys = new Column[1];
        fkeys[0] = tables[EMPQUERYVET].getColumns()[2];
        links[0].fkeys = fkeys;
        links[1] = new Link("vetUser","empQueryVets",tables[VetUser]);
        fkeys = new Column[1];
        fkeys[0] = tables[EMPQUERYVET].getColumns()[3];
        links[1].fkeys = fkeys;
        tables[EMPQUERYVET].setLinks(links);
        
        db.setTables(tables);
        return db;
    }
    
    public static void main(String[] args) throws Exception {
    	new DataSource();
    	
        String s = "employerId = 1234 AND FOLDER.ID = 1";
        OASelect sel = new OASelect(Job.class, s, "");
    	sel.select();
    	System.out.println("Done");
    }
    
}
