/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.ds.jdbc.delegate;

import java.sql.*;
import java.util.logging.Logger;

import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.*;
import com.viaoa.util.OAString;

/**
 * Methods to validate that OAObject database metadata matches database scheme.  
 * @author vvia
 *
 */
public class VerifyDelegate {
    private static Logger LOG = Logger.getLogger(VerifyDelegate.class.getName());

    public static boolean verify(OADataSourceJDBC ds) throws Exception {
        Connection connection = null;
        try {
            connection = ds.getConnection(true);
            boolean b = _verify(ds, connection);
            return b;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            if (connection != null) {
                ds.releaseConnection(connection);
            }
        }
    }
    /** 
	    Verifies Tables, Columns and Indexes.  Prints to console window.
	    @returns true if all tables, columns and indexes exist, else returns false if any are missing.
	*/
	private static boolean _verify(OADataSourceJDBC ds, Connection connection) throws Exception {
	    DatabaseMetaData dbmd = connection.getMetaData();
	    ResultSet rs;
	    boolean bResult = true;
	    Table[] tables = ds.getDatabase().getTables();
	    for (int i=0; i<tables.length; i++) {
	        Table t = tables[i];
	        rs = dbmd.getTables(null,null,t.name.toUpperCase(), null);
	        boolean b = rs.next();
	        rs.close();
	        if (!b) {
	           bResult = false;
	           LOG.warning("DB ERROR: Table not found: "+t.name);
	           continue;
	        }
	        
	        Column[] columns = t.getColumns();
	        for (int j=0; j<columns.length; j++) {
	            Column c = columns[j];
	            rs = dbmd.getColumns(null,null,t.name.toUpperCase(),c.columnName.toUpperCase());
	            b = rs.next();
	            if (b) {
	    	        int iType = rs.getInt(5);
	    	        String sType = rs.getString(6);
	    	        int iSize = rs.getInt(7);
	    	        
	    	        if (c.getSqlType() == 0) {
	    	        	if (c.propertyName != null && c.propertyName.trim().length() != 0) {
	    	        	    LOG.warning("DB WARNING: Column missing TYPE "+t.name+"."+c.columnName+" property: " + c.propertyName);
	    	        	}
	    	        }
	    	        else if (iType == c.getSqlType()) {
                        // check size
	    	            if (OAString.isEmpty(c.propertyName)) {
	                        // could be fkey, which will not have a maxLength
	    	                
	    	            }
	    	            else if (iType == java.sql.Types.VARCHAR) {
	    	        		if (iSize != c.maxLength) {
	    	        		    LOG.warning("DB NOTE: Column SIZE mismatch: "+t.name+"."+c.columnName+" ds:"+c.maxLength+" != db:"+iSize);
	
	    	        		    /*
		    	        		if (c.maxLength > iSize) {
		    	        			String s = DDLDelegate.getAlterColumnSQL(ds.getDBMetaData(), t.name, c.columnName, "VARCHAR(" + c.maxLength + ")");
		    	        	        ds.execute(s);
		    	        	        LOG.warning("-- resized Column: "+t.name+"."+c.columnName+" from " + iSize + " to " + c.maxLength+ " : " + s);
		    	        		}
		    	        		else {
		    	        			c.maxLength = iSize;
		    	        			LOG.warning("-- will use column size of " + c.maxLength);
		    	        		}
		    	        		*/
		    	        		continue;
	    	        		}
	    	        	}
	    	        }
	    	        else {
	    	        	b = false;
	    	        	// see if CLOB or LONGVARCHAR match
	    	        	if (iType == java.sql.Types.LONGVARCHAR || iType == java.sql.Types.CLOB) {
		    	        	if (c.getSqlType() == java.sql.Types.LONGVARCHAR || c.getSqlType() == java.sql.Types.CLOB) {
		    	        		b = true;
		    	        	}
	    	        	}
	    	        	DBMetaData dbx = ds.getDBMetaData();
                        if (!b && iType == java.sql.Types.VARCHAR && dbx != null && dbx.databaseType == dbx.SQLSERVER) {
                            if (c.getSqlType() == java.sql.Types.CLOB && iSize > Math.pow(10, 9)) {
                                b = true;
                            }
                        }
                        if (!b && iType == java.sql.Types.VARBINARY && dbx != null && dbx.databaseType == dbx.SQLSERVER) {
                            if (c.getSqlType() == java.sql.Types.BLOB) {
                                b = true;
                            }
                        }
                        
	    	        	if (iType == java.sql.Types.BIT || iType == java.sql.Types.BOOLEAN) {
		    	        	if (c.getSqlType() == java.sql.Types.BIT || c.getSqlType() == java.sql.Types.BOOLEAN) {
		    	        		b = true;
		    	        	}
	    	        	}
	    	        	if (iType == java.sql.Types.SMALLINT || iType == java.sql.Types.BOOLEAN || iType == java.sql.Types.CHAR || iType == java.sql.Types.BIT) {
		    	        	if (c.getSqlType() == java.sql.Types.BIT || c.getSqlType() == java.sql.Types.BOOLEAN) {
		    	        		b = true;
		    	        	}
	    	        	}
	    	        	if (!b && (iType == java.sql.Types.REAL || iType == java.sql.Types.DOUBLE)) {
		    	        	if (c.getSqlType() == java.sql.Types.REAL || c.getSqlType() == java.sql.Types.DOUBLE) {
		    	        		b = true;
		    	        	}
	    	        	}
	    	        	if (!b && (iType == java.sql.Types.TIMESTAMP)) {
		    	        	if (c.getSqlType() == java.sql.Types.DATE || c.getSqlType() == java.sql.Types.TIME) {
		    	        		b = true;
		    	        	}
	    	        	}
	    	        	if (!b) {
	    	        	    LOG.warning("DB ERROR: Column TYPE mismatch: "+t.name+"."+c.columnName+" ds:"+c.getSqlType()+" != db:"+iType);
	    	        		continue;
	    	        	}
	    	        }
	            }
	            rs.close();
	            if (!b) {
	                bResult = false;
	                LOG.warning("DB ERROR: Column not found: "+t.name+"."+c.columnName);
	                continue;
	            }
	
	            if (c.columnLowerName != null && c.columnLowerName.length() > 0) {
		            rs = dbmd.getColumns(null,null,t.name.toUpperCase(),c.columnLowerName.toUpperCase());
		            b = rs.next();
		            rs.close();
		            if (!b) {
		                bResult = false;
		                LOG.warning("DB ERROR: Column not found: "+t.name+"."+c.columnLowerName);
		                continue;
		            }
	            }
	
	            if (c.primaryKey) {
	            	rs = dbmd.getPrimaryKeys(null, null, t.name.toUpperCase());
		            b = rs.next();
                    String colname=null;
                    String pkname=null;
		            if (b) {
		            	colname = rs.getString(4); // column name
		            	pkname = rs.getString(6); // pk name
	            		b = pkname.equalsIgnoreCase("PK"+t.name);
		            }	            	
		            rs.close();
		            if (!b) {
		                LOG.warning("DB ERROR: PK missing: "+" PK" + t.name+" "+t.name+"."+c.columnName + " - FOUND: pkName="+pkname+", colname="+colname);
		            }
	            }
	        	// public static String convert(DBMetaData dbmd, Column column, Object value) {
	            try {
	            	ConverterDelegate.convert(ds.getDBMetaData(), c, "1");
	            }
	            catch (Exception e) {
	                LOG.warning("DB ERROR: ConverterDelegate wont be able to convert column type: Table:" + t.name + " Column:" + c.columnName);
	            	bResult = false;
	            }
	        }
            Index[] indexes = t.getIndexes();

	        for (int j=0; j<indexes.length; j++) {
	    		Index ind = indexes[j];

	    		rs = dbmd.getIndexInfo(null, null, t.name.toUpperCase(), false, false);
	            int foundCnt = 0;
	            boolean bNameMatch = false;
	            for ( ;b = rs.next(); ) {
	            	String name = rs.getString(6); // index name
                    bNameMatch = (name != null && name.equalsIgnoreCase(ind.name));
	            	
	            	name = rs.getString(9); // column name
	            	for (int k=0; name != null && k < ind.columns.length; k++) {
	            		if (name.equalsIgnoreCase(ind.columns[k])) {
	            			foundCnt++;
	            		}
	            	}
	            }
	            rs.close();

	            if (foundCnt < ind.columns.length) {
                    LOG.warning("DB ERROR: Index not in database: "+t.name+"."+ind.name);
	            }
	        }
            
            
	        // check to see if there are "extra" indexes
            rs = dbmd.getIndexInfo(null, null, t.name.toUpperCase(), false, false);
            for ( ;b = rs.next(); ) {
                boolean bFound = false;
                String name = rs.getString(6); // index name
                if (OAString.isEmpty(name)) continue;
                for (int j=0; j<indexes.length; j++) {
                    if (name != null && name.equalsIgnoreCase(indexes[j].name)) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    // could be pkey or fkey 
                    String s = rs.getString(9); // column name
                    for (int j=0; j<columns.length; j++) {
                        Column c = columns[j];
                        if (c.columnName.equalsIgnoreCase(s)) {
                            if (c.primaryKey) bFound = true;
                            if (c.foreignKey) {
                                bFound = true;
                            }
                        }
                    }
                    if (!bFound) {
                        LOG.warning("DB warning: DB Index not in datasource: table="+t.name+", index="+name);
                    }
                }
            }	        
            rs.close();
	        if (!verifyLinks(t)) bResult = false;
	    }
	    return bResult;
	}

	public static boolean verifyLinks(Table t) {
		boolean bError = false;
        // verify Links
        Link[] links = t.getLinks();
        for (int i=0; links!= null && i<links.length; i++) {
        	Link link = links[i];
            Column[] cols = link.fkeys;
        	Table toTable = link.toTable;
        	Link revLink = link.getReverseLink();
        	if (revLink == null) continue;
            Column[] revCols = revLink.fkeys;
            if ((cols == null && revCols != null) || (cols != null && revCols == null)) {
                LOG.warning("DB ERROR: key columns for link do not match: "+t.name+"."+link.propertyName);
                bError = true;
            }
            if (cols == null) continue;
            if (cols.length != revCols.length) {
                LOG.warning("DB ERROR: key columns for link do not match: "+t.name+"."+link.propertyName);
                bError = true;
            }
            for (int j=0; j<cols.length; j++) {
            	int t1 = cols[j].type;
            	int t2 = revCols[j].type;
            	if (t1 == 0) {
            		cols[j].type = t1 = t2;
            	}
            	if (t1 != t2) {
            	    LOG.warning("DB ERROR: key columns for link do not match types: "+t.name+"."+link.propertyName);
                    bError = true;
            	}
            }
        }
        return bError;
	}
	
}













