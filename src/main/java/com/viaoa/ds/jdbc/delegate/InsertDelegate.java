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

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.object.*;

/**
 * Manages inserts for JDBC datasource.
 * @author vvia
 */
public class InsertDelegate {

	private static Logger LOG = Logger.getLogger(InsertDelegate.class.getName());
	
    public static void insertWithoutReferences(OADataSourceJDBC ds, OAObject obj) {
        if (obj == null) return;
        insert(ds, obj, obj.getClass(), false);
    }
    
    public static void insert(OADataSourceJDBC ds, OAObject object) {
        if (object == null) return;
        insert(ds, object, object.getClass(), true);
    }

    private static void insert(OADataSourceJDBC ds, OAObject oaObj, Class clazz, boolean bIncludeRefereces) {
        Class c = clazz.getSuperclass();
        if (c != null && !c.equals(OAObject.class)) {
           insert(ds, oaObj, c, bIncludeRefereces);
        }

        Column columnSkip = null;
        Table table = ds.getDatabase().getTable(clazz);
        if (table != null) {
	        Column[] columns = table.getColumns();
	        for (int i=0; columns != null && i < columns.length; i++) {
	            if (ds.getDBMetaData().supportsAutoAssign && columns[i].assignNextNumber && columns[i].assignedByDatabase) {
	            	columnSkip = columns[i];
	            	break;
	            }
	        }
        }
        Object[] objs = null;
        try {
            objs = getInsertSQL(ds, oaObj, clazz, columnSkip, bIncludeRefereces);
            Object[] params = null;
            Vector v = (Vector) objs[1];
            if (v != null) {
                int x = v.size();
                params = new Object[x];
                for (int i=0; i<x; i++) {
                    params[i] = v.elementAt(i);
                }
            }
            
            performInsert(ds, oaObj, (String) objs[0], params, columnSkip);
        }
        catch (Exception e) {
            if (objs == null || objs.length == 0) objs = new String[] {"no sql generated"};
            //LOG.log(Level.WARNING, "insert(), sql="+objs[0], e);
        	throw new RuntimeException("Error on insert, sql="+objs[0], e);
        }
    }

    private static Object[] getInsertSQL(OADataSourceJDBC ds, OAObject oaObj, Class clazz, Column columnSkip, boolean bIncludeRefereces) throws Exception {
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) {
            throw new Exception("cant find table for Class "+clazz.getName());
        }

        Column[] columns = table.getColumns();
        StringBuffer str = new StringBuffer(128);
        StringBuffer values = new StringBuffer(128);
        Vector<Object> vecValue = null;
        String value;
        DBMetaData dbmd = ds.getDBMetaData();
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (column == columnSkip) continue;
            if (column.propertyName == null || column.propertyName.length() == 0) continue;
            if (column.readOnly) continue;

            Object obj = oaObj.getProperty(column.propertyName); 
            // see if column needs to be assigned to a seq number
            // support for DB generated keys
            if (column.primaryKey) {
                boolean b = false;
            	if (obj == null) b = true;
            	else if (obj instanceof Number) {
            	    if (((Number) obj).intValue() == 0) b = true;
            	    else {
            	        // this will make sure that the assigned number does not mess up the nextNumber generator
                        AutonumberDelegate.verifyNumberUsed(ds, oaObj, table, column, ((Number) obj).intValue());
            	    }
            	}
            	if (b) {
                    if (column.assignedByDatabase && dbmd.supportsAutoAssign) continue;  // generated by DB
                    AutonumberDelegate.assignNumber(ds, oaObj, table, column);
                    obj = oaObj.getProperty(column.propertyName); 
            	}
            }

            boolean bNull = (obj == null);

            // 20100514
            boolean byteArray = (obj instanceof byte[]);
            if (byteArray) {
                if (vecValue == null) vecValue = new Vector(3,3);
                vecValue.addElement(obj);
                value = "?";
            }
            else {
                boolean bOver512 = (obj instanceof String && ((String)obj).length() > 512);
                
                // this will convert to SQL string
                value = ConverterDelegate.convertToString(dbmd, obj, !bOver512, Delegate.getMaxLength(column), column.decimalPlaces, column);
                
                String origValue = value;
                if (value != null && bOver512) {
                    if (vecValue == null) vecValue = new Vector<Object>(3,3);
                    vecValue.addElement(value);
                    value = "?";
                }
            }
            if (str.length() > 0) {
                str.append(", ");
                values.append(", ");
            }
            str.append(dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket );
            values.append( value );

            
            /***
            // check for case sensitive column
        	if (!byteArray && dbmd.caseSensitive) {
            	String colName = column.columnLowerName;
            	if (colName != null && colName.trim().length() > 0 && !colName.equalsIgnoreCase(column.columnName)) {
                    value = origValue;
            		if (!bNull) value = value.toLowerCase();
            		if (bOver512) {
                        vecValue.addElement(value);
                        value = "?";
                    }
            		str.append(", ");
                    values.append(", ");
	                str.append( dbmd.leftBracket + colName.toUpperCase() + dbmd.rightBracket );
	                values.append( value );
            	}
        	}
        	***/
        }

        // update Fkeys
        Link[] links = table.getLinks();
        for (int i=0; bIncludeRefereces && links != null && i < links.length; i++) {
            if (links[i].fkeys == null || (links[i].fkeys.length == 0)) continue;
            if (links[i].fkeys[0].primaryKey) continue;    // one2many, or one2one (where Key is the fkey)
            
            OAObjectKey key = OAObjectReflectDelegate.getPropertyObjectKey(oaObj, links[i].propertyName);
            if (key == null) continue; // null
            Object[] ids;
            ids = key.getObjectIds();

            Column[] fkeys = table.getLinkToColumns(links[i], links[i].toTable);
            if (fkeys == null) continue;
            if (fkeys.length != links[i].fkeys.length) continue;
            for (int j=0; j<fkeys.length; j++) {
                Object objProperty = ((ids == null) || (j >= ids.length)) ? null : ids[j];
                value = ConverterDelegate.convert(dbmd, fkeys[j], objProperty);
                if (str.length() > 0) {
                    str.append(", ");
                    values.append(", ");
                }

                str.append( dbmd.leftBracket + links[i].fkeys[j].columnName.toUpperCase() + dbmd.rightBracket );
                values.append( value );
            }
        }
        str = new StringBuffer("INSERT INTO " + dbmd.leftBracket + table.name.toUpperCase() + dbmd.rightBracket + " (" + str + ") VALUES (" + values + ")");
        return new Object[] { new String(str), vecValue };
    }

    private static void performInsert(OADataSourceJDBC ds, OAObject oaObj, String sqlInsert, Object[] params, Column columnAutoGen) throws Exception {

        /*
        OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
        String s = String.format("Insert, class=%s, id=%s, sql=%s",
                OAString.getClassName(oaObj.getClass()),
                key.toString(),
                sqlInsert
        );
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        if (oi.getUseDataSource()) {
            OAObject.OALOG.fine(s);
        }
        
        LOG.fine(s);
                   
        DBLogDelegate.logInsert(sqlInsert, params);
        */
//qqqqqqqqqqqqqqq        
DBLogDelegate.logInsert(sqlInsert, params);

        Statement statement = null;
        PreparedStatement preparedStatement = null;
        try {
            int x = 0;
            if (params != null && params.length > 0) {
                if (ds.getDBMetaData().databaseType == DBMetaData.ACCESS) {
                    preparedStatement = ds.getPreparedStatement(sqlInsert);
                    columnAutoGen = null;
                }
                else {
                	preparedStatement = ds.getPreparedStatement(sqlInsert, (columnAutoGen != null));
                }

                for (int i=0; i<params.length; i++) {
                    if (params[i] instanceof String) {
                        preparedStatement.setAsciiStream(i+1, new StringBufferInputStream((String) params[i]), ((String)params[i]).length());
                    }
                    else {
                        // 20100504
                        preparedStatement.setBytes(i+1, (byte[]) (params[i]));
                    }
                    
                }
                x = preparedStatement.executeUpdate();
            }
            else {
                statement = ds.getStatement(sqlInsert);
                if (ds.getDBMetaData().databaseType == DBMetaData.ACCESS) {
                	x = statement.executeUpdate(sqlInsert);
                	columnAutoGen = null;
                }
                else {
                	int param = (columnAutoGen != null) ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
                    x = statement.executeUpdate(sqlInsert, param);
                }
            }
            if (x != 1) {
                LOG.warning("could not insert, sql="+sqlInsert);
                throw new Exception("row was not INSERTed, no exception thrown");
            }

            if (columnAutoGen != null) {
            	ResultSet rs;
            	if (preparedStatement != null) rs = preparedStatement.getGeneratedKeys();
            	else rs = statement.getGeneratedKeys();
            	
            	if (rs.next()) {
            		Object val = rs.getObject(1);
            		
                    try {
                        OAObjectDSDelegate.setAssigningId(oaObj, true);
                        oaObj.setProperty(columnAutoGen.propertyName, val);
                    }
                    finally {
                        OAObjectDSDelegate.setAssigningId(oaObj, false);
                    }
            	}
                rs.close();
            }
        }
        finally {
            if (statement != null) ds.releaseStatement(statement);
            if (preparedStatement != null) ds.releasePreparedStatement(preparedStatement);
        }
    }
	
}



