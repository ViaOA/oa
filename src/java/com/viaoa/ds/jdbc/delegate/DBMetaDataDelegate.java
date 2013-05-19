package com.viaoa.ds.jdbc.delegate;


import com.viaoa.ds.jdbc.db.*;
import com.viaoa.util.OAString;


/**
 * Used to get information about the specific type of database.
 * @author vvia
 *
 *
 * 
 */
public class DBMetaDataDelegate {
	
    /**
	    Set the type of Database SQLSERVER, ACCESS, DERBY, ORACLE, MYSQL  default: SQLSERVER
	*/
	public static void updateAfterTypeChange(DBMetaData dbmd) {
		dbmd.setDistinctKeyword("DISTINCT");
	
		dbmd.setUseBracket(true);
	    dbmd.setDatesIncludeTime(false);
	    dbmd.setUseExists(true);
		dbmd.setLowerCaseFunction("LOWER");
		dbmd.setCaseSensitive(false);
		String distinctKeyword = "DISTINCT";
        dbmd.setAutoAssignValue("");
        dbmd.setMaxVarcharLength(255);
        
        String s;
	    switch(dbmd.databaseType) {
	        case DBMetaData.OTHER:
	        	dbmd.setUseExists(false); // Derby allows EXISTS, but is faster to do a join
	        	dbmd.setName("Other");
	        	dbmd.setUseBracket(false);
	        	dbmd.setCaseSensitive(false);
	        	break;
	        case DBMetaData.DERBY:
	            // connect 'jdbc:derby:database;create=true;collation=TERRITORY_BASED';
                dbmd.setName("Derby Database");
	        	dbmd.setUseExists(false); // Derby allows EXISTS, but is (much!much!) faster to do a join
	        	dbmd.setUseBracket(false);
	        	dbmd.setCaseSensitive(false);  // was true, before 10.4
	        	dbmd.setLowerCaseFunction("LOWER");
                dbmd.setAutoAssignValue("GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)");
                dbmd.setSupportsAutoAssign(true);
                dbmd.setFkeysAutoCreateIndex(true);
                dbmd.setMaxVarcharLength(32672);
                s = dbmd.getDriverJDBC();
                if (OAString.isEmpty(s)) {
                    s = "org.apache.derby.jdbc.EmbeddedDriver";
                    dbmd.setDriverJDBC(s);
                }
                s = dbmd.getUrlJDBC();
                if (OAString.isEmpty(s)) {
                    s = "jdbc:derby:database";
                    dbmd.setUrlJDBC(s);
                }
	        	break;
	        case DBMetaData.SQLSERVER:
	            dbmd.setName("SQL Server Database");
	            dbmd.setMaxString("TOP ?");
	            dbmd.setDatesIncludeTime(true);
	            dbmd.setAutoAssignValue("IDENTITY(1,1)");
	            dbmd.setSupportsAutoAssign(true);
                dbmd.setMaxVarcharLength(8000);
	            
                s = dbmd.getDriverJDBC();
                if (OAString.isEmpty(s)) {
                    s = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    dbmd.setDriverJDBC(s);
                }
                s = dbmd.getUrlJDBC();
                if (OAString.isEmpty(s)) {
                    s = "jdbc:sqlserver://localhost;port=1433;database=dbname;sendStringParametersAsUnicode=false;SelectMethod=cursor;ConnectionRetryCount=2;ConnectionRetryDelay=2";
                    dbmd.setUrlJDBC(s);
                }
	            break;
	        case DBMetaData.ORACLE:
	        	dbmd.setUseBracket(false);
	        	dbmd.setName("ORACLE Database");
                dbmd.setMaxVarcharLength(4000);
	            break;
	        case DBMetaData.ACCESS:
	        	dbmd.setName("Access Database");
	            // distinctKeyword = "DISTINCT ROW";
	        	dbmd.setDatesIncludeTime(true);
	        	distinctKeyword = "DISTINCTROW";
	        	dbmd.setBlanksAsNulls(true);
	            break;
	        case DBMetaData.MYSQL:
	        	dbmd.setName("MySql Database");
	        	dbmd.setUseBracket(false);
	        	dbmd.setDatesIncludeTime(false);
                // ??? dbmd.setMaxVarcharLength();
	            // bUseExists = false; // prior to MySQL 4, now supports subqueries and EXISTS
	            break;
	        case DBMetaData.BRIDGE:
	        	dbmd.setName("ODBC-JDBC Bridge");
	        	break;
	    }
    	dbmd.setDistinctKeyword(distinctKeyword);
	}
	

	public static void close(DBMetaData dbmd) {
	    if (dbmd.databaseType == dbmd.DERBY) {
	    	try {
	    		java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true");  // shuts down Derby, not just a single database
	    	}
	    	catch (Exception e) {
	    		// System.out.println("DataSource.shutdown() " + e);
	    	}
	    }
	}
	
	private static String getValidName(DBMetaData dbmd, String name) {
	    if (name == null) return "";
	    // note: the "XvXvX" will be converted afterwards
	    if (name.equalsIgnoreCase("max")) {
	    	name = "MaxXvXvX";
	    }
	    else if (name.equalsIgnoreCase("min")) {
	    	name = "MinXvXvX";
	    }
	    else if (name.equalsIgnoreCase("text")) {
	    	name = "TextXvXvX";
	    }
	    else if (name.equalsIgnoreCase("memo")) {
	    	name = "MemoXvXvX";
	    }
	    else if (name.equalsIgnoreCase("user")) {
	    	name = "UserXvXvX";
	    }
	    else if (name.equalsIgnoreCase("date")) {
	    	name = "DateXvXvX";
	    }
	    else if (name.equalsIgnoreCase("datetime")) {
	    	name = "DateTimeXvXvX";
	    }
	    else if (name.equalsIgnoreCase("time")) {
	    	name = "TimeXvXvX";
	    }
	    else if (name.equalsIgnoreCase("timestamp")) {
	    	name = "TimeStampXvXvX";
	    }
	    else if (name.equalsIgnoreCase("count")) {
	    	name = "CountXvXvX";
	    }
		return name;
	}

	public static String getValidTableName(DBMetaData dbmd, String name) {
		name = getValidName(dbmd, name);
		return OAString.convert(name, "XvXvX", "Table");
	}
	public static String getValidColumnName(DBMetaData dbmd, String name) {
		name = getValidName(dbmd, name);
		return OAString.convert(name, "XvXvX", "Value");
	}
	public static String getValidIndexName(DBMetaData dbmd, String name) {
		name = getValidName(dbmd, name);
		return OAString.convert(name, "XvXvX", "Index");
	}
	
}





