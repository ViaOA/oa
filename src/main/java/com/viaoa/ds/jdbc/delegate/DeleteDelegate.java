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

import java.sql.Statement;
import java.util.logging.*;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.object.*;
import com.viaoa.util.OAString;

/**
 * Manages deleting for JDBC datasource.
 * @author vvia
 *
 */
public class DeleteDelegate {
    private static Logger LOG = Logger.getLogger(DeleteDelegate.class.getName());

    public static void delete(OADataSourceJDBC ds, OAObject object) {
        if (object == null || !(object instanceof OAObject)) return;
        if ( ((OAObject)object).getNew() ) {
            LOG.fine("delete called on a new object, class="+object.getClass().getName()+", key="+OAObjectKeyDelegate.getKey(object));
            return;
        }
        delete(ds, object, object.getClass());
    }

    private static void delete(OADataSourceJDBC ds, OAObject oaObj, Class clazz) {
        String sql = null;
        try {
	        sql = getDeleteSQL(ds, oaObj, clazz);

	        /*
            OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
            String s = String.format("Update, class=%s, id=%s, sql=%s",
                    OAString.getClassName(oaObj.getClass()),
                    key.toString(),
                    sql
            );
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            if (oi.getUseDataSource()) {
                OAObject.OALOG.fine(s);
            }
	        LOG.fine(s);
	        */
            performDelete(ds, sql);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception trying to delete, sql="+sql, e);
        	throw new RuntimeException(e);
        }
        Class c = clazz.getSuperclass();
        if (c != null && !c.equals(OAObject.class)) delete(ds, oaObj, c);
    }

    /**
        This is needed when a object has a super class that needs to be deleted.
    */
    private static String getDeleteSQL(OADataSourceJDBC ds, OAObject oaObj, Class clazz) throws Exception {
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) throw new Exception("cant find table for Class "+clazz.getName());
        Column[] columns = table.getColumns();
        StringBuffer where = new StringBuffer(64);
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (!column.primaryKey || column.propertyName == null || column.propertyName.length() == 0) continue;

            Object obj = oaObj.getProperty(column.propertyName); 
            
            String op = "=";
            String value;
            if (obj == null) op = "IS";
            value = ConverterDelegate.convert(ds.getDBMetaData(), column, obj);
            value = ds.getDBMetaData().leftBracket + column.columnName.toUpperCase() + ds.getDBMetaData().rightBracket + " "+op+" "+value;

            if (where.length() > 0) where.append(" AND ");
            where.append(value);
        }
        String str = "DELETE FROM " + ds.getDBMetaData().leftBracket + table.name.toUpperCase() + ds.getDBMetaData().rightBracket + " WHERE "+where;
        return str;
    }

   
    private static void performDelete(OADataSourceJDBC ds, String str) throws Exception {
        LOG.fine(str);            
        Statement statement = null;
        try {
            // DBLogDelegate.logDelete(str);
            statement = ds.getStatement(str);
            int x = statement.executeUpdate(str);
            if (x != 1) LOG.warning("row was not DELETEd, no exception thrown");
        }
        finally {
            if (statement != null) ds.releaseStatement(statement);
        }
    }
	
}
