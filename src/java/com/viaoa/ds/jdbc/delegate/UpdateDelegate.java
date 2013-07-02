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

import java.util.*;
import java.util.logging.Logger;
import java.io.*;
import java.sql.*;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.object.*;
import com.viaoa.util.OANullObject;
import com.viaoa.util.OAString;

/**
 * Manages updates for JDBC datasource.
 * @author vvia
 */
public class UpdateDelegate {
    private static Logger LOG = Logger.getLogger(UpdateDelegate.class.getName());

    public static void removeReference(OADataSourceJDBC ds, OAObject oaObj, String propertyName) {
        if (oaObj == null) return;
        update(ds, oaObj, oaObj.getClass(), new String[] {propertyName}, null);
    }
    
    public static void update(OADataSourceJDBC ds, OAObject oaObj) {
        if (oaObj == null) return;
        update(ds, oaObj, oaObj.getClass(), null, null);
    }

    public static void update(OADataSourceJDBC ds, OAObject oaObj, String[] includeProperties, String[] excludeProperties) {
        if (oaObj == null) return;
        update(ds, oaObj, oaObj.getClass(), includeProperties, excludeProperties);
    }
    
    
    protected static void update(OADataSourceJDBC ds, OAObject oaObj, Class clazz, String[] includeProperties, String[] excludeProperties) {
        Class cx = clazz.getSuperclass();
        if (cx != null && !cx.equals(OAObject.class)) update(ds, oaObj, cx, includeProperties, excludeProperties);
        
        Object[] objs = null;
        try {
            objs = getUpdateSQL(ds, oaObj, clazz, includeProperties, excludeProperties);
            if (objs == null) return; // could be subclass with only an Id property
            Object[] params = null;
            Vector v = (Vector) objs[1];
            if (v != null) {
                int x = v.size();
                params = new Object[x];
                for (int i=0; i<x; i++) {
                    params[i] = v.elementAt(i);
                }
            }

            OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
            String s = String.format("Update, class=%s, id=%s, sql=%s",
                    OAString.getClassName(oaObj.getClass()),
                    key.toString(),
                    objs[0]
            );
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            if (oi.getUseDataSource()) {
                OAObject.OALOG.fine(s);
            }
            
            LOG.fine(s);            
            performUpdate(ds, (String) objs[0], params);
        }
        catch (Exception e) {
            if (objs == null || objs.length == 0) objs = new String[] {"no sql generated"};
            System.out.println("update(), sql="+objs[0]+" exception="+e);
            e.printStackTrace();
        	throw new RuntimeException(e);
        }
    }

    private static Object[] getUpdateSQL(OADataSourceJDBC ds, OAObject oaObj, Class clazz, String[] includeProperties, String[] excludeProperties) throws Exception {
        String s;
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) throw new Exception("cant find table for Class "+clazz.getName());
        Vector vecParam = null;
        Column[] columns = table.getColumns();
        StringBuffer sbSet = new StringBuffer(128);;
        StringBuffer sbWhere = new StringBuffer(128);
        String value;
        DBMetaData dbmd = ds.getDBMetaData();
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (column.propertyName == null || column.propertyName.length() == 0) continue;
            if (column.readOnly) continue;
            
            if (!column.primaryKey && includeProperties != null && includeProperties.length > 0) {
                boolean b = false;
                for (int j=0; !b && j<includeProperties.length; j++) {
                    if (column.propertyName.equalsIgnoreCase(includeProperties[j])) {
                        b = true;
                    }
                }
                if (!b) continue;
            }
            if (!column.primaryKey && excludeProperties != null && excludeProperties.length > 0) {
                boolean b = false;
                for (int j=0; !b && j<excludeProperties.length; j++) {
                    if (column.propertyName.equalsIgnoreCase(excludeProperties[j])) {
                        b = true;
                    }
                }
                if (b) continue;
            }
            
            
            // 20130318 check for blob
            if (column.type == java.sql.Types.BLOB) {
                OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
                OAPropertyInfo pi = oi.getPropertyInfo(column.propertyName);
                if (pi != null && pi.isBlob()) {
                    Object obj = OAObjectPropertyDelegate.getProperty(oaObj, column.propertyName);
                    if (obj == null) continue; // not loaded, no change to it
                }
            }

            Object obj = oaObj.getProperty(column.propertyName); 
            
            if (dbmd.blanksAsNulls) {
                if ( ((obj instanceof String) && ((String)obj).length() == 0) ) obj = null;
            }

            boolean bNull = (obj == null);
            boolean bOver512 = (obj instanceof String && ((String)obj).length() > 512);
    
            // 20100514
            boolean byteArray = (obj instanceof byte[]);
            if (byteArray) {
                if (vecParam == null) vecParam = new Vector(3,3);
                vecParam.addElement(obj);
                value = "?";
            }
            else {
                value = ConverterDelegate.convertToString(dbmd, obj, !bOver512, Delegate.getMaxLength(column), column.decimalPlaces);
                String origValue = value;
                
                if (value != null && bOver512) {
                    if (vecParam == null) vecParam = new Vector(3,3);
                    vecParam.addElement(value);
                    value = "?";
                }
            }
            value = dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket + " = " + value;
            if (column.primaryKey) {
                if (sbWhere.length() > 0) sbWhere.append(" AND ");
                sbWhere.append(value);
            }
            else {
                if (sbSet.length() > 0) sbSet.append(", ");
                sbSet.append(value);
            }

            /**
            // check for case sensitive column
        	if (dbmd.caseSensitive) {
            	String colName = column.columnLowerName;
            	if (colName != null && colName.trim().length() > 0 && !colName.equalsIgnoreCase(column.columnName)) {
                    value = origValue;
            		if (!bNull) value = value.toLowerCase();
            		if (bOver512) {
            			vecParam.addElement(value);
                        value = "?";
                    }
            		sbSet.append(", ");
                    sbSet.append(dbmd.leftBracket + column.columnLowerName.toUpperCase() + dbmd.rightBracket + " = " + value);
            	}            
        	}
        	**/
        }
        
        Link[] links = table.getLinks();
        for (int i=0; links != null && i < links.length; i++) {
            if (links[i].fkeys == null || (links[i].fkeys.length == 0)) continue;
            if (links[i].fkeys[0].primaryKey) continue;    // one2many, or one2one (where Key is the fkey)

            if (includeProperties != null && includeProperties.length > 0) {
                boolean b = false;
                if (links[i].propertyName == null) continue;
                for (int j=0; !b && j<includeProperties.length; j++) {
                    if (links[i].propertyName.equalsIgnoreCase(includeProperties[j])) {
                        b = true;
                    }
                }
                if (!b) continue;
            }
            if (excludeProperties != null && excludeProperties.length > 0) {
                boolean b = false;
                if (links[i].propertyName == null) b = true;
                else {
                    for (int j=0; !b && j<excludeProperties.length; j++) {
                        if (links[i].propertyName.equalsIgnoreCase(excludeProperties[j])) {
                            b = true;
                        }
                    }
                }
                if (b) continue;
            }
            
            
            
            
            OAObjectKey key = OAObjectReflectDelegate.getPropertyObjectKey(oaObj, links[i].propertyName);
            Object[] ids;
            if (key != null) ids = key.getObjectIds();
            else ids = null;

            Column[] fkeys = table.getLinkToColumns(links[i], links[i].toTable);
            if (fkeys == null) continue;
            if (fkeys.length != links[i].fkeys.length) continue;
            for (int j=0; j<fkeys.length; j++) {
            	Object objProperty = ((ids == null) || (j >= ids.length)) ? null : ids[j];
            	value = ConverterDelegate.convert(dbmd, fkeys[j], objProperty);
                if (sbSet.length() > 0) sbSet.append(", ");
                sbSet.append(dbmd.leftBracket + links[i].fkeys[j].columnName.toUpperCase() + dbmd.rightBracket + " = " + value);
            }
        }
        
        if (sbSet.length() == 0) return null;

        s = ("UPDATE " + dbmd.leftBracket + table.name.toUpperCase() + dbmd.rightBracket + " SET " + sbSet + " WHERE " + sbWhere);
        return new Object[] { s, vecParam };
    }

    private static void performUpdate(OADataSourceJDBC ds, String sqlUpdate, Object[] sqlParams) throws Exception {
        Statement statement = null;

        DBLogDelegate.logUpdate(sqlUpdate, sqlParams);
        
        PreparedStatement preparedStatement = null;
        try {
            int x;
            if (sqlParams != null && sqlParams.length > 0) {
                preparedStatement = ds.getPreparedStatement(sqlUpdate);
                for (int i=0; i<sqlParams.length; i++) {
                    if (sqlParams[i] instanceof String) {
                        preparedStatement.setAsciiStream(i+1, new StringBufferInputStream((String) sqlParams[i]), ((String)sqlParams[i]).length());
                    }
                    else {
                        // 20100504
                        preparedStatement.setBytes(i+1, (byte[]) (sqlParams[i]));
                    }
                }
                x = preparedStatement.executeUpdate();
            }
            else {
                statement = ds.getStatement(sqlUpdate);
                x = statement.executeUpdate(sqlUpdate);
            }
            if (x != 1) {
                LOG.warning("expected executeUpdate to return 1, received="+x+", sql="+sqlUpdate);
                // throw new Exception("row was not UPDATEDed, no exception thrown");
            }
        }
        finally {
            if (statement != null) ds.releaseStatement(statement);
            if (preparedStatement != null) ds.releasePreparedStatement(preparedStatement);
        }
    }

    public static void updateMany2ManyLinks(OADataSourceJDBC ds, OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster) {
        if (masterObject == null) return;
        DBMetaData dbmd = ds.getDBMetaData();
        // get link table (if one exists) and fkeys from masterTable to linkTable
        Table fromTable=null;
        Link[] fromTableLinks=null;
        Column[] fromFKeys = null;
        Table linkTable = null;
        Class clazz = masterObject.getClass();
        for (;clazz != null && !clazz.equals(OAObject.class); clazz = clazz.getSuperclass()) {
        	fromTable = ds.getDatabase().getTable(clazz);
            if (fromTable == null) return;

            fromTableLinks = fromTable.getLinks();
            if (fromTableLinks == null || fromTableLinks.length == 0) return;

            for (int i=0; i < fromTableLinks.length; i++) {
                if (!fromTableLinks[i].toTable.bLink) continue;
                if (!fromTableLinks[i].propertyName.equalsIgnoreCase(propFromMaster)) continue;
                linkTable = fromTableLinks[i].toTable;
                fromFKeys = fromTableLinks[i].fkeys;
                break;
            }
            if (fromFKeys != null) break;
        }
        if (fromFKeys == null) return;
        if (linkTable == null) throw new RuntimeException("Link for table "+fromTable.name+" does not have a toTable");

        // now get fkeys from linkTable to fromTable
        fromTableLinks = linkTable.getLinks();
        Column[] linkTableFromFKeys = null;
        int foundPos = 0;
        for (int i=0; i < fromTableLinks.length; i++) {
            if (fromTableLinks[i].toTable == fromTable) {
            	linkTableFromFKeys = fromTableLinks[i].fkeys;
            	foundPos = i;
                break;
            }
        }
        if (linkTableFromFKeys == null) {
            throw new RuntimeException("Table "+fromTable.name+" does not have a backward link with linkTable "+linkTable.name);
        }

        // now get the toTable from the linkTable and fkeys from linkTable to toTable
        Table toTable = null;
        Column[] linkTableToFKeys = null;
        for (int i=0; i < fromTableLinks.length; i++) {
            if (i != foundPos) {
                // it could be a M2M with same object.  ex: Item m2m item
            // was: if (fromTableLinks[i].toTable != fromTable) {
                toTable = fromTableLinks[i].toTable;
                linkTableToFKeys = fromTableLinks[i].fkeys;
                break;
            }
        }
        if (toTable == null || linkTableToFKeys == null) {
            throw new RuntimeException("LinkTable "+linkTable.name+" does not have a link");
        }

        // now get fkeys between toTable and linkTable
        fromTableLinks = toTable.getLinks();
        Column[] toFKeys = null;
        for (int i=0; i < fromTableLinks.length; i++) {
            if (fromTableLinks[i].toTable == linkTable) {
            	toFKeys = fromTableLinks[i].fkeys;
                break;
            }
        }
        if (toFKeys == null) throw new RuntimeException("Table "+toTable.name+" does not a link to linkTable "+linkTable.name);


        // get properties from masterObject
        Object[] fromPKeyValues = new Object[fromFKeys.length];
        for (int i=0; i<fromFKeys.length; i++) {
            Column column = fromFKeys[i];
            if (column.propertyName == null || column.propertyName.length() == 0) continue;
            fromPKeyValues[i] = masterObject.getProperty(column.propertyName);
        }

        // get properties for each added detailObject and create a linkObject
        Object[] toPKeyValues = new Object[toFKeys.length];
        for (int j=0; adds != null && j<adds.length; j++) {
            OAObject obj = adds[j];
            if (obj.getNew()) continue; // will save with obj
            for (int i=0; i<toFKeys.length; i++) {
                Column column = toFKeys[i];
                if (column.propertyName == null || column.propertyName.length() == 0) continue;
                toPKeyValues[i] = obj.getProperty(column.propertyName);
            }

            // now create Insert into LinkTable
            StringBuffer str = new StringBuffer(64);
            StringBuffer values = new StringBuffer(64);
            String value;
            for (int i=0; i < linkTableFromFKeys.length; i++) {
                Column column = linkTableFromFKeys[i];
                value = ConverterDelegate.convert(ds.getDBMetaData(), column, fromPKeyValues[i]);
                if (str.length() > 0) {
                    str.append(", ");
                    values.append(", ");
                }
                str.append(dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket );
                values.append( value );
            }
            for (int i=0; i < linkTableToFKeys.length; i++) {
                Column column = linkTableToFKeys[i];
                value = ConverterDelegate.convert(ds.getDBMetaData(), column, toPKeyValues[i]);

                if (str.length() > 0) {
                    str.append(", ");
                    values.append(", ");
                }
                str.append(dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket);
                values.append(value);
            }

            str = new StringBuffer("INSERT INTO " + dbmd.leftBracket + linkTable.name.toUpperCase() + dbmd.rightBracket + " (" + str + ") VALUES (" + values + ")");

            Statement statement = null;
            try {
                String sql = str.toString();
                LOG.fine("masterClass="+masterObject.getClass().getName()+", propFromMaster="+propFromMaster+", sql="+sql);            
                statement = ds.getStatement(sql);
                statement.executeUpdate(sql);
            }
            catch (Exception e) {
              //System.out.println("UpdateDelegate.updateMany2ManyLinks(..) failed, exception: "+e);                
                // if (!connectionPool.isDatabaseAvailable()) throw new RuntimeException("Database Connection is not Available.  Error:"+e);
                // could be a duplicate record ... just ignore
                // throw new OADataSourceException(this, "OADataSourceJDBC.saveLinks() - "+e.getMessage());
            }
            finally {
                if (statement != null) ds.releaseStatement(statement);
            }
        }

        // get properties for each removed detailObject and delete linkObject
        for (int j=0; removes != null && j<removes.length; j++) {
            OAObject obj = removes[j];
            if (obj.getNew()) continue;

            for (int i=0; i<toFKeys.length; i++) {
                Column column = toFKeys[i];
                if (column.propertyName == null || column.propertyName.length() == 0) continue;
                toPKeyValues[i] = obj.getProperty(column.propertyName);
            }
            
            // now delete from LinkTable
            StringBuffer str = new StringBuffer(64);
            String value;
            for (int i=0; i < linkTableFromFKeys.length; i++) {
                Column column = linkTableFromFKeys[i];
                String op = "=";
                if (fromPKeyValues[i] == null) op = "IS";
                value = ConverterDelegate.convert(ds.getDBMetaData(), column, fromPKeyValues[i]);
                if (str.length() > 0) str.append(" AND ");
                str.append( dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket + " " + op + " " + value  );
            }
            for (int i=0; i < linkTableToFKeys.length; i++) {
                Column column = linkTableToFKeys[i];
                String op = "=";
                if (toPKeyValues[i] == null) op = "IS";
                value = ConverterDelegate.convert(ds.getDBMetaData(), column, toPKeyValues[i]);
                if (str.length() > 0) str.append(" AND ");
                str.append( dbmd.leftBracket + column.columnName.toUpperCase() + dbmd.rightBracket + " " + op + " " + value  );
            }
            String whereClause = new String(str);

            str = new StringBuffer("DELETE FROM " + dbmd.leftBracket + linkTable.name.toUpperCase() + dbmd.rightBracket + " WHERE " + whereClause);
            Statement statement = null;
            try {
                String sql = str.toString();
                LOG.fine("masterClass="+masterObject.getClass().getName()+", propFromMaster="+propFromMaster+", sql="+sql);            
                statement = ds.getStatement(sql);
                statement.executeUpdate(sql);
            }
            catch (Exception e) {
//System.out.println("UpdateDelegate.updateMany2ManyLinks(..) failed, exception: "+e);                
                // if (!connectionPool.isDatabaseAvailable()) throw new OAException("Database Connection is not Available.  Error:"+e);
                // could be a duplicate ... just ignore
                // throw new OADataSourceException(this, "OADataSourceJDBC.saveLinks() - "+e.getMessage());
            }
            finally {
                if (statement != null) {
                	ds.releaseStatement(statement);
                	statement = null;
                }
            }
        }
	}

}



