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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.object.*;

/**
 * Used to get seq numbers to assign for new object ids.
 * 
 * Logging: all are set to "finer"
 * 
  */
public class AutonumberDelegate {
    private static Logger LOG = Logger.getLogger(AutonumberDelegate.class.getName());

    private static ConcurrentHashMap<String, AtomicInteger> hashNext = new ConcurrentHashMap<String, AtomicInteger>(39, .75f);  // Table, Integer
    private static Object LOCK = new Object();
	
    /**
	    Assigns autonumber properties.
	    If guid is being used, then it will prefix the autonumber.
	*/
	public static void assignNumber(OADataSourceJDBC ds, OAObject object, Table table, Column column) {
        // LOG.finer("table="+table.name+", column="+column.columnName);
	    int id = getNextNumber(ds, table, column, true);
        // LOG.finer("table="+table.name+", column="+column.columnName+", nextId="+id);
	    DBMetaData dbmd = ds.getDBMetaData();
	    Object value;
	    
	    if (column.guid && dbmd.guid != null) value = dbmd.guid + "-"+id;
	    else value = new Integer(id);
	    
	    try {
	        OAThreadLocalDelegate.setAssigningObjectKey(true); 
	        OAObjectReflectDelegate.setProperty(object, column.propertyName, value, null);
	    }
	    finally {
            OAThreadLocalDelegate.setAssigningObjectKey(false); 
	    }
	}
	
	/**
	 * This is used to determine if an assigned ID needs to change the autoNextNumber ID
	 */
	public static void verifyNumberUsed(OADataSourceJDBC ds, OAObject object, Table table, Column column, final int id) {
        // LOG.finer("table="+table.name+", column="+column.columnName+", verifyId="+id);
        for (;;) {
            int idNext = getNextNumber(ds, table, column, false);
            if (id < idNext) break;
            AtomicInteger ai = hashNext.get(table.name);
            if (ai.compareAndSet(idNext, id+1)) break; // else need to try again
        }
	}

	public static void setNextNumber(OADataSourceJDBC ds, Table table, int nextNumberToUse) {
        // LOG.finer("table="+table.name+", nextNumberToUse="+nextNumberToUse);
        Column[] columns = table.getColumns();
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (column.primaryKey) {
                verifyNumberUsed(ds, null, table, column, nextNumberToUse);
                break;
            }
        }
	}

    protected static int getNextNumber(OADataSourceJDBC ds, Table table, Column pkColumn, boolean bAutoIncrement) {
 //qqqqqqqqqqqqqqq
        int x = _getNextNumber(ds, table, pkColumn, bAutoIncrement);
//        LOG.warning("table="+table+", name="+table.name+", bAutoIncrement="+bAutoIncrement+", returning="+x);
        return x;
    }	
    //========================= Utilities ===========================
    protected static int _getNextNumber(OADataSourceJDBC ds, Table table, Column pkColumn, boolean bAutoIncrement) {
        // LOG.finer("table="+table.name+", column="+pkColumn.columnName+", bAutoIncrement="+bAutoIncrement);
        
        int max = 0;
        AtomicInteger ai = hashNext.get(table.name);
        if (ai == null) {
    	    DBMetaData dbmd = ds.getDBMetaData();
            synchronized(LOCK) {
                ai = hashNext.get(table.name);
                if (ai == null) {
                    String query = "";
                    if (dbmd.guid != null && dbmd.guid.length() > 0) {
                    	query = getMaxGuidQuery(dbmd, table, pkColumn);
                    }
                    else {
                    	query = getMaxIdQuery(dbmd, table, pkColumn);
                    }

                    Statement statement = null;
                    try {
                        statement = ds.getStatement(query);
                        ResultSet rs = statement.executeQuery(query);
                        if (rs.next()) max = (rs.getInt(1) + 1);
                        rs.close();
                    }
                    catch (Exception e) {
                        throw new RuntimeException("OADataSource.getNextNumber() failed for "+table.name+" Query:"+query, e);
                    }
                    finally {
                        if (statement != null) ds.releaseStatement(statement);
                    }
                    // LOG.finer("table="+table.name+", column="+pkColumn.columnName+", got max="+max);
                    ai = new AtomicInteger(max);
                	hashNext.put(table.name, ai);
                }
            }
        }
        
        if (bAutoIncrement) {
            max = ai.getAndIncrement();
        }
        else {
            max = ai.get();
        }
        return max;
    }
    

	protected static String getMaxGuidQuery(DBMetaData dbmd, Table table, Column dbcolumn) {
        // ACCESS Version to get string value of seq number
        String column = dbcolumn.columnName;
		String s;
		String from = " from "+dbmd.leftBracket+table.name+dbmd.rightBracket;
		String where;
		if (dbmd.databaseType == dbmd.ACCESS) {
            s = "select max(val(right$("+column+", len("+column+")-"+(dbmd.guid.length()+1)+") ))";
            where = " WHERE "+column+" like '"+ dbmd.guid + "-%'";
        }
        else if (dbmd.databaseType == dbmd.DERBY) {
        	s = "select max(integer(substr("+column+", " + (dbmd.guid.length()+2) +")))";
            where = " WHERE "+column+" like '"+ dbmd.guid + "-%'";
        }
        else if (dbmd.databaseType == dbmd.SQLSERVER) {
            s = "select max(right("+column+", len("+column+")-"+(dbmd.guid.length()+1)+"))";
            where = " WHERE "+column+" like '"+ dbmd.guid + "-%'";
        }
        else {
            // MYSQL
            s = "select max(convert(right("+column+", length("+column+")-"+(dbmd.guid.length()+1)+"), UNSIGNED INTEGER))";
            where = " WHERE "+column+" like '"+ dbmd.guid + "-%'";
        }
		s = s + from + where;;
        //LOG.finer("table="+table.name+", column="+dbcolumn.columnName+", query="+s);
		
		return s;
	}
		
	protected static String getMaxIdQuery(DBMetaData dbmd, Table table, Column dbcolumn) {
        String column = dbcolumn.columnName;
		String s;
        if (dbmd.databaseType == dbmd.ACCESS) {
            // ACCESS Version to get string value of seq number
            s = "select max(val("+column+"))";
        }
        else if (dbmd.databaseType == dbmd.DERBY) {
            s = "select max("+column+")";
        }
        else if (dbmd.databaseType == dbmd.SQLSERVER) {
            s = "select max("+column+")";
        }
        else {
            // MYSQL
            s = "SELECT MAX(CONVERT("+column+", UNSIGNED INTEGER))";
        }
        s += " FROM " +table.name;
        LOG.fine("table="+table.name+", column="+dbcolumn.columnName+", query="+s);
        return s;
	}
}
