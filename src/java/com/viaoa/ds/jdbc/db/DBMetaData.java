package com.viaoa.ds.jdbc.db;

import com.viaoa.annotation.OAClass;
import com.viaoa.ds.jdbc.delegate.*;
import com.viaoa.object.*;

// DBMetaData.ORACLE:  http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
// SqlServer: http://msdn.microsoft.com/en-us/library/ms191530.aspx

/**
 * Database MetaData for various JDBC databases.
 * @author vvia
 *
 * see for data types: 
 *    http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html#table1
 *
 */
@OAClass(
   useDataSource = false,
   localOnly = true
)
public class DBMetaData extends OAObject {
    static final long serialVersionUID = 1L;
	
    public final static int OTHER = 0;
    public final static int DERBY = 1;
    public final static int SQLSERVER = 2;
    public final static int ORACLE = 3;
    public final static int ACCESS = 4;
    public final static int MYSQL = 5;
    public final static int BRIDGE = 6;
	
    public int databaseType; 
    public String note;
    public String name;
    public String description;
    public boolean useBracket = true;  // use "[" and "]" around table and column names
    public String leftBracket="";
    public String rightBracket="";
    public String distinctKeyword = "DISTINCT";
    public boolean blanksAsNulls = false;
    public boolean useOuterJoinEscape = false;
    public String outerjoinStart="";
    public String outerjoinEnd="";
    public Object objectTrue, objectFalse; // values to use for storing Boolean properties
    public boolean booleanKeyword; // boolean values use a keyword.  ex: TRUE instead of 'TRUE'
    public boolean datesIncludeTime; // flag to know that the DB saves Dates as both Date and Time
    public boolean useExists=true;
    public boolean useBackslashForEscape = false;  // NOTE: no database are currently set up as true
    public boolean caseSensitive;  // for case sensitive databases
    public String lowerCaseFunction;
    public boolean supportsAutoAssign;  // if true, db assigns id
    public String autoAssignValue;
	public String maxString;  // use "?" to have the max amount entered
    public String guid;
    public boolean allowStatementPooling = true;
    public boolean fkeysAutoCreateIndex = false;
    public int maxVarcharLength;
    
    /**
        DERBY:                   "org.apache.derby.jdbc.EmbeddedDriver"
        MS SQL Server            "com.microsoft.sqlserver.jdbc.SQLServerDriver"      

	    ODBC Bridge:             "sun.jdbc.odbc.JdbcOdbcDriver"
	    INET (SQL-SERVER):       "com.inet.tds.TdsDriver"
	    WEBLOGIC (SQL-SERVER):   "weblogic.jdbc.mssqlserver4.Driver"
	    ODBC-BRIDGE:             "sun.jdbc.odbc.JdbcOdbcDriver"
	    ORACLE:                  "oracle.jdbc.driver.OracleDriver"
    
    */
    public String driverJDBC;

    
    /**
        Derby:                   "jdbc:derby:database"   to create: "jdbc:derby:database;create=true;collation=TERRITORY_BASED"
        MS SQL Server            "jdbc:sqlserver://localhost;port=1433;database=vetjobs;sendStringParametersAsUnicode=false;SelectMethod=cursor;ConnectionRetryCount=2;ConnectionRetryDelay=2"      

	    INET (SQL-SERVER):       "jdbc:inetdae:127.0.0.1:1433?database=northwind&sql7=true"
	    WEBLOGIC (SQL-SERVER):   "jdbc:weblogic:mssqlserver4:northwind@127.0.0.1:1433"    
	    ODBC-BRIDGE:             "jdbc:odbc:northwind"
	    Access:                  "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=c:\\temp\\vetplan.mdb";
    */

    public String urlJDBC;
    
    public String user;
    public String password;
    
    public int maxConnections=10;
    public int minConnections=3;

    public DBMetaData() {
        int xx = 4;
        xx++;
    }

    public DBMetaData(int databaseType) {
    	setDatabaseType(databaseType);
    }
    
    
    public DBMetaData(int databaseType, String user, String password, String driverJDBC, String urlJDBC) {
    	setDatabaseType(databaseType);
	    setUser(user);
	    setPassword(password);
	    setDriverJDBC(driverJDBC);
	    setUrlJDBC(urlJDBC);
    }

    public void setDatabaseType(int dbType) {
    	int old = this.databaseType;
    	this.databaseType = dbType;
        firePropertyChange("databaseType", old, this.databaseType);
    	if (!isLoading()) {
    		DBMetaDataDelegate.updateAfterTypeChange(this);
    	}
    }
	public int getDatabaseType() {
		return databaseType;
	}    
    
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		String old = this.name;
		this.name = name;
        firePropertyChange("name", old, this.name);
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		String old = this.note;
		this.note = note;
        firePropertyChange("note", old, this.note);
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
        firePropertyChange("description", old, this.description);
	}

	public boolean getFkeysAutoCreateIndex() {
	    return fkeysAutoCreateIndex;
	}
	public void setFkeysAutoCreateIndex(boolean b) {
	    boolean old = this.fkeysAutoCreateIndex;
	    this.fkeysAutoCreateIndex = b;
	    firePropertyChange("fkeysAutoCreateIndex", old, fkeysAutoCreateIndex);
	}

		
    public boolean getAllowStatementPooling() {
        return allowStatementPooling;
    }
    public void setAllowStatementPooling(boolean b) {
        boolean old = this.allowStatementPooling;
        this.allowStatementPooling = b;
        firePropertyChange("allowStatementPooling", old, allowStatementPooling);
    }
		
	public boolean getUseBracket() {
		return useBracket;
	}

	public void setUseBracket(boolean useBracket) {
		boolean old = this.useBracket;
		this.useBracket = useBracket;
        firePropertyChange("useBracket", old, this.useBracket);
        if (!isLoading()) {
        	if (useBracket) {
        		setLeftBracket("[");
        		setRightBracket("]");
        	}
        	else {
        		setLeftBracket("");
        		setRightBracket("");
        	}
        }
	}

	public String getLeftBracket() {
		return leftBracket;
	}

	public void setLeftBracket(String leftBracket) {
		String old = this.leftBracket;
		this.leftBracket = leftBracket;
        firePropertyChange("leftBracket", old, this.leftBracket);
	}

	public String getRightBracket() {
		return rightBracket;
	}

	public void setRightBracket(String rightBracket) {
		String old = this.rightBracket;
		this.rightBracket = rightBracket;
	    firePropertyChange("rightBracket", old, this.rightBracket);
	}

	public String getDistinctKeyword() {
		return distinctKeyword;
	}
	public void setDistinctKeyword(String distinctKeyword) {
		String old = this.distinctKeyword;
		this.distinctKeyword = distinctKeyword;
	    firePropertyChange("distinctKeyword", old, this.distinctKeyword);
	}

	public boolean getBlanksAsNulls() {
		return blanksAsNulls;
	}
	public void setBlanksAsNulls(boolean blanksAsNulls) {
		boolean old = this.blanksAsNulls;
		this.blanksAsNulls = blanksAsNulls;
	    firePropertyChange("blanksAsNulls", old, this.blanksAsNulls);
	}

	public boolean getUseOuterJoinEscape() {
		return useOuterJoinEscape;
	}
	public void setUseOuterJoinEscape(boolean useOuterJoinEscape) {
		boolean old = this.useOuterJoinEscape;
		this.useOuterJoinEscape = useOuterJoinEscape;
	    firePropertyChange("useOuterJoinEscape", old, this.useOuterJoinEscape);
	    if (!isLoading()) {
			if (useOuterJoinEscape) {
		    	setOuterjoinStart("{oj ");
		    	setOuterjoinEnd("}");
		    }
		    else {
		    	setOuterjoinStart("");
		    	setOuterjoinEnd("");
		    }
	    }
	}

	public String getOuterjoinStart() {
		return outerjoinStart;
	}
	public void setOuterjoinStart(String outerjoinStart) {
		String old = this.outerjoinStart;
		this.outerjoinStart = outerjoinStart;
	    firePropertyChange("outerjoinStart", old, this.outerjoinStart);
	}

	public String getOuterjoinEnd() {
		return outerjoinEnd;
	}
	public void setOuterjoinEnd(String outerjoinEnd) {
		Object old = this.outerjoinEnd;
		this.outerjoinEnd = outerjoinEnd;
	    firePropertyChange("outerjoinEnd", old, this.outerjoinEnd);
	}

	public Object getObjectTrue() {
		return objectTrue;
	}
	public void setObjectTrue(Object objectTrue) {
		Object old = this.objectTrue;
		this.objectTrue = objectTrue;
	    firePropertyChange("objectTrue", old, this.objectTrue);
	}

	public Object getObjectFalse() {
		return objectFalse;
	}
	public void setObjectFalse(Object objectFalse) {
		Object old = this.objectFalse;
		this.objectFalse = objectFalse;
	    firePropertyChange("objectFalse", old, this.objectFalse);
	}

	public boolean getBooleanKeyword() {
		return booleanKeyword;
	}
	public void setBooleanKeyword(boolean booleanKeyword) {
		boolean old = this.booleanKeyword;
		this.booleanKeyword = booleanKeyword;
	    firePropertyChange("booleanKeyword", old, this.booleanKeyword);
	}

	public boolean getDatesIncludeTime() {
		return datesIncludeTime;
	}
	public void setDatesIncludeTime(boolean bDatesIncludeTime) {
		boolean old = this.datesIncludeTime;
		this.datesIncludeTime = bDatesIncludeTime;
	    firePropertyChange("DatesIncludeTime", old, this.datesIncludeTime);
	}

	public boolean getUseExists() {
		return useExists;
	}
	public void setUseExists(boolean useExists) {
		boolean old = this.useExists; 
		this.useExists = useExists;
	    firePropertyChange("useExists", old, this.useExists);
	}

	public boolean getUseBackslashForEscape() {
		return useBackslashForEscape;
	}
	public void setUseBackslashForEscape(boolean useBackslashForEscape) {
		boolean old = this.useBackslashForEscape;
		this.useBackslashForEscape = useBackslashForEscape;
	    firePropertyChange("useBackslashForEscape", old, this.useBackslashForEscape);
	}

	public boolean getCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean caseSensitive) {
		boolean old = this.caseSensitive;
		this.caseSensitive = caseSensitive;
	    firePropertyChange("caseSensitive", old, this.caseSensitive);
	}

	public String getLowerCaseFunction() {
		return lowerCaseFunction;
	}
	public void setLowerCaseFunction(String lowerCaseFunction) {
		String old = this.lowerCaseFunction;
		this.lowerCaseFunction = lowerCaseFunction;
	    firePropertyChange("lowerCaseFunction", old, this.lowerCaseFunction);
	}

	public boolean getSupportsAutoAssign() {
		return supportsAutoAssign;
	}
	public void setSupportsAutoAssign(boolean supportsAutoAssign) {
		boolean old = this.supportsAutoAssign;
		this.supportsAutoAssign = supportsAutoAssign;
	    firePropertyChange("supportsAutoAssign", old, this.supportsAutoAssign);
	}

	public String getAutoAssignValue() {
		return autoAssignValue;
	}
	public void setAutoAssignValue(String autoAssignValue) {
		String old = this.autoAssignValue;
		this.autoAssignValue = autoAssignValue;
	    firePropertyChange("autoAssignValue", old, this.autoAssignValue);
	}
	
	public String getMaxString() {
		return maxString;
	}
	public void setMaxString(String maxString) {
		String old = this.maxString;
		this.maxString = maxString;
	    firePropertyChange("maxString", old, this.maxString);
	}

	public String getDriverJDBC() {
		return driverJDBC;
	}
	public void setDriverJDBC(String driverJDBC) {
		String old = this.driverJDBC;
		this.driverJDBC = driverJDBC;
	    firePropertyChange("driverJDBC", old, this.driverJDBC);
	}

	public String getUrlJDBC() {
		return urlJDBC;
	}
	public void setUrlJDBC(String urlJDBC) {
		String old = this.urlJDBC;
		this.urlJDBC = urlJDBC;
	    firePropertyChange("urlJDBC", old, this.urlJDBC);
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		String old = this.user;
		this.user = user;
	    firePropertyChange("user", old, this.user);
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		String old = this.password;
		this.password = password;
	    firePropertyChange("password", old, this.password);
	}

	public int getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(int maxConnections) {
		int old = this.maxConnections;
		this.maxConnections = maxConnections;
	    firePropertyChange("maxConnections", old, this.maxConnections);
	}

	public int getMinConnections() {
		return minConnections;
	}
	public void setMinConnections(int minConnections) {
		int old = this.minConnections;
		this.minConnections = minConnections;
	    firePropertyChange("minConnections", old, this.minConnections);
	}


    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner, recursive)
        oaObjectInfo = new OAObjectInfo(new String[] {});
        oaObjectInfo.setInitializeNewObjects(false);
        
        // oaObjectInfo.addLink(new OALinkInfo("objectDefs",   ObjectDef.class,  OALinkInfo.MANY, true,true, "model"));
        
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // oaObjectInfo.addCalc(new OACalcInfo("fullFileName", new String[] {"fileName","directoryName"} ));
    }

    public int getMaxVarcharLength() {
        return maxVarcharLength;
    }
    public void setMaxVarcharLength(int x) {
        int old = this.maxVarcharLength;
        this.maxVarcharLength = x;
        firePropertyChange("maxVarcharLength", old, this.maxVarcharLength);
    }
}


