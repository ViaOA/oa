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

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.query.*;
import com.viaoa.object.*;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.OAString;

/**
 * Manages Selects/Queries for JDBC datasource.
 * @author vvia
 *
 */
public class SelectDelegate {
    private static Logger LOG = Logger.getLogger(SelectDelegate.class.getName());
    
    public static Iterator select(OADataSourceJDBC ds, Class clazz, String queryWhere, String queryOrder, int max) {
    	return select(ds, clazz, queryWhere, (Object[]) null, queryOrder, max);
    }

    public static Iterator select(OADataSourceJDBC ds, Class clazz, String queryWhere, Object param, String queryOrder, int max) {
    	Object[] params = null;
    	if (param != null) params = new Object[] {param};
    	return select(ds, clazz, queryWhere, params, queryOrder, max);
    }

    public static Iterator select(OADataSourceJDBC ds, Class clazz, String queryWhere, Object[] params, String queryOrder, int max) {
        if (ds == null) return null;
        if (clazz == null) return null;
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) return null;

        QueryConverter qc = new QueryConverter(ds);
    	String[] queries = getSelectSQL(qc, ds, clazz, queryWhere, params, queryOrder, max);
        
        ResultSetIterator rsi;
        DataAccessObject dao = table.getDataAccessObject();
        if (dao != null) {
            rsi = new ResultSetIterator(ds, clazz, dao, queries[0], queries[1], max);
        }    	
        else {    	
        	Column[] columns = qc.getSelectColumnArray(clazz);
            if (queries[1] != null) {
            	// this will take 2 queries.  The first will only select pkey columns.
            	//   the second query will then select the record using the pkey values in the where clause.
                rsi = new ResultSetIterator(ds, clazz, columns, queries[0], queries[1], max);
            }
            else {
                rsi = new ResultSetIterator(ds, clazz, columns, queries[0], max);
            }
        }
        return rsi;
    }

    /**
     *  @returns array [0]=sql [1]=sql2 (if needed) 
     */
    private static String[] getSelectSQL(QueryConverter qc, OADataSourceJDBC ds, Class clazz, String queryWhere, Object[] params, String queryOrder, int max) {
        String[] queries = new String[2];
        
        queries[0] = qc.convertToSql(clazz, queryWhere, params, queryOrder);
        if (qc.getUseDistinct()) {
            // distinct query will also need to have the order by keys
            String s = " ORDER BY ";
            int x = queries[0].indexOf(s);
            if (x > 0) {
                x += s.length();
                s = queries[0].substring(x);
                
                // need to remove ASC, DESC
                // todo: this might not be needed anymore                  
                StringTokenizer st = new StringTokenizer(s, ", ", false);
                String s1 = null;
                for ( ;st.hasMoreElements();) {
                    String s2 = (String) st.nextElement();
                    String s3 = s2.toUpperCase();
                    if (s3.equals("ASC")) continue;
                    if (s3.equals("DESC")) continue;
                    if (s1 == null) s1 = s2;
                    else s1 += ", " + s2;
                }
                s = ", " + s1;
            }
            else s = "";
            
        	// this will take 2 queries.  The first will only select pkey columns.
        	//   the second query will then select the record using the pkey values in the where clause.
        	queries[0] = "SELECT " + ds.getDBMetaData().distinctKeyword + " " + qc.getPrimaryKeyColumns(clazz) + s + " " + queries[0];

        	OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
			String[] ids = oi.getIdProperties();
			params = new Object[ids.length];
			queries[1] = "";
			for (int i=0; ids != null && i<ids.length; i++) {
				if (i > 0) queries[1] += " AND ";
				queries[1] += ids[i] + " = ?";
				params[i] = "7"; // fake out/position holder
			}
			queries[1] = qc.convertToSql(clazz, queries[1], params, null);
			queries[1] = "SELECT " + qc.getSelectColumns(clazz) + " " + queries[1];
			queries[1] = OAString.convert(queries[1], "7", "?");
        }
        else {
        	queries[0] = "SELECT " + getMax(ds,max) + qc.getSelectColumns(clazz) + " " + queries[0];
        }
        return queries;
    }

    
    private static class WhereObjectSelect {
        private Class clazz;
        private Class whereClazz;
        private String propertyFromMaster;
        public WhereObjectSelect(Class clazz, Class whereClazz, String propertyFromMaster) {
            this.clazz = clazz;
            this.whereClazz = whereClazz;
            this.propertyFromMaster = propertyFromMaster;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WhereObjectSelect)) return false;
            WhereObjectSelect x = (WhereObjectSelect) obj;
            
            if (clazz != x.clazz) {
                if (clazz == null || x.clazz == null) return false;
                if (!clazz.equals(x.clazz)) return false;
            }
            if (whereClazz != x.whereClazz) {
                if (whereClazz == null || x.whereClazz == null) return false;
                if (!whereClazz.equals(x.whereClazz)) return false;
            }
            if (propertyFromMaster != x.propertyFromMaster) {
                if (propertyFromMaster == null || x.propertyFromMaster == null) return false;
                if (!propertyFromMaster.equals(x.propertyFromMaster)) return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            int x = 0;
            if (clazz != null) x += clazz.hashCode();
            if (whereClazz != null) x += whereClazz.hashCode();
            if (propertyFromMaster != null) x += propertyFromMaster.hashCode();
            return x;
        } 
    }
    private static ConcurrentHashMap<WhereObjectSelect, String> hmPreparedStatementSql = new ConcurrentHashMap<WhereObjectSelect, String>();
    
    // 20121013 changes to use PreparedStatements for Selecting Many link
    public static Iterator select(OADataSourceJDBC ds, Class clazz, OAObject whereObject, String extraWhere, Object[] args, String propertyFromMaster, String queryOrder, int max) {
        // dont need to select if master object (whereObject) is new
        if (whereObject.getNew()) return null;

        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) return null;
        DataAccessObject dao = table.getDataAccessObject();
    
        if (dao == null || whereObject == null || OAString.isEmpty(propertyFromMaster) || (args != null && args.length > 0) || max > 0) {
            QueryConverter qc = new QueryConverter(ds);
            String query = getSelectSQL(ds, qc, clazz, whereObject, extraWhere, args, propertyFromMaster, queryOrder, max);
            
            ResultSetIterator rsi;
            if (dao != null) {
                rsi = new ResultSetIterator(ds, clazz, dao, query, null, max);
            }       
            else {      
                Column[] columns = qc.getSelectColumnArray(clazz);
                rsi = new ResultSetIterator(ds, clazz, columns, query, max);
            }
            return rsi;
        }

        WhereObjectSelect wos = new WhereObjectSelect(clazz, whereObject==null?null:whereObject.getClass(), propertyFromMaster);
        String query = hmPreparedStatementSql.get(wos);
        
        if (query == null) {
            QueryConverter qc = new QueryConverter(ds);
            query = "SELECT " + qc.getSelectColumns(clazz);
            query  += " " + qc.convertToPreparedStatementSql(clazz, whereObject, extraWhere, args, propertyFromMaster, queryOrder);

            args = qc.getArguments();
            if (args == null || args.length == 0) return null; // null reference
            hmPreparedStatementSql.put(wos, query);
        }
        else {
            OAObjectKey key = OAObjectKeyDelegate.getKey(whereObject);
            args = key.getObjectIds();
        }
        
        ResultSetIterator rsi = new ResultSetIterator(ds, clazz, dao, query, args);
        return rsi;
    }
    
    private static ConcurrentHashMap<Class, String> hmPreparedStatementSqlx = new ConcurrentHashMap<Class, String>();
    // 20121013
    public static Iterator selectObject(OADataSourceJDBC ds, Class clazz, OAObjectKey key) throws Exception {
        if (ds == null) return null;
        if (clazz == null) return null;

        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) return null;
        DataAccessObject dao = table.getDataAccessObject();

        if (dao == null) {
            return null;
        }
        
        
        String sql = hmPreparedStatementSqlx.get(clazz);
        if (sql == null) {
            sql = dao.getSelectColumns();
            sql = "SELECT " + sql;
            sql  += " FROM " + table.name + " WHERE ";
            
            Column[] cols = table.getSelectColumns();
            boolean b = false;
            for (Column c : cols) {
                if (c.primaryKey) {
                    if (!b) b = true;
                    else sql += " AND ";
                    sql  += c.columnName + " = ?";
                }
            }
            hmPreparedStatementSqlx.put(clazz, sql);
        }
        ResultSetIterator rsi = new ResultSetIterator(ds, clazz, dao, sql, key.getObjectIds());
        return rsi;
    }
    
    
    public static String getSelectSQL(OADataSourceJDBC ds, QueryConverter qc, Class clazz, OAObject whereObject, String extraWhere, Object[] args, String propertyFromMaster, String queryOrder, int max) {
        if (propertyFromMaster == null) propertyFromMaster = "";
        String query = "SELECT " + getMax(ds,max) + qc.getSelectColumns(clazz);
        query  += " " + qc.convertToSql(clazz, whereObject, extraWhere, args, propertyFromMaster, queryOrder);
    	return query;
    }
    
    
    public static Iterator selectPassthru(OADataSourceJDBC ds, Class clazz, String query, int max) {
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) return null;

        QueryConverter qc = new QueryConverter(ds);

        query = qc.getSelectColumns(clazz) + " " + query;

        ResultSetIterator rsi;
        DataAccessObject dao = table.getDataAccessObject();
        if (dao != null) {
            rsi = new ResultSetIterator(ds, clazz, dao, query, null, max);
        }       
        else {      
            Column[] columns = qc.getSelectColumnArray(clazz);
            rsi = new ResultSetIterator(ds, clazz, columns, "SELECT "+getMax(ds,max)+query, max);
        }
        return rsi;
    }

    private static String getMax(OADataSourceJDBC ds, int max) {
		String str = "";
		if (max > 0) {
			DBMetaData dbmd = ds.getDBMetaData();
			if (dbmd.maxString != null) {
				str = OAString.convert(dbmd.maxString, "?", (max+"")) + " ";
			}
		}
		return str;
    }
    
    /**
     * Note: queryWhere needs to begin with "FROM TABLENAME WHERE ..."
     * queryOrder will be prefixed with "ORDER BY "
     */
    public static Iterator selectPassthru(OADataSourceJDBC ds, Class clazz, String queryWhere, String queryOrder, int max) {
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) return null;
        
        QueryConverter qc = new QueryConverter(ds);
        String query = qc.getSelectColumns(clazz);
        if (queryWhere != null && queryWhere.length() > 0) query += " " + queryWhere;
        if (queryOrder != null && queryOrder.length() > 0) {
            query += " ORDER BY " + queryOrder;
        }
        
        ResultSetIterator rsi;
        DataAccessObject dao = table.getDataAccessObject();
        if (dao != null) {
            rsi = new ResultSetIterator(ds, clazz, dao, query, null, max);
        }       
        else {      
            Column[] columns = qc.getSelectColumnArray(clazz);
            rsi = new ResultSetIterator(ds, clazz, columns, "SELECT "+getMax(ds,max)+query, max);
        }
        return rsi;
    }
    
    
    public static Object execute(OADataSourceJDBC ds, String command) {
        // LOG.fine("command="+command);
        Statement st = null;
        try {
            st = ds.getStatement(command);
            st.execute(command);
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException("OADataSourceJDBC.execute() " + command, e);
        }
        finally {
            if (st != null) ds.releaseStatement(st);
        }
    }

    // Note: queryWhere needs to begin with "FROM TABLENAME WHERE ..."
    public static int countPassthru(OADataSourceJDBC ds, String query, int max) {
        String s = "SELECT "+getMax(ds, max)+"COUNT(*) ";
        if (query != null && query.length() > 0) s += query;
        // LOG.fine("sql="+s);
        Statement st = null;
        try {
            st = ds.getStatement(s);
            java.sql.ResultSet rs = st.executeQuery(s);
            rs.next();
            int x = rs.getInt(1);
            if (max > 0 && x > max) x = max;
            return x;
        }
        catch (Exception e) {
            throw new RuntimeException("OADataSourceJDBC.count() "+query, e);
        }
        finally {
            if (st != null) ds.releaseStatement(st);
        }

    }
    public static int count(OADataSourceJDBC ds, Class selectClass, Object whereObject, String propertyFromMaster, int max) {
        return count(ds, selectClass, whereObject, null, null, propertyFromMaster, max);
    }
    public static int count(OADataSourceJDBC ds, Class selectClass, Object whereObject, String extraWhere, Object[] args, String propertyFromMaster, int max) {
        if ( whereObject instanceof OAObject ) {
            if ( ((OAObject) whereObject).getNew() ) return 0;
        }

        if (propertyFromMaster == null) propertyFromMaster = "";
        QueryConverter qc = new QueryConverter(ds);
        String s = qc.convertToSql(selectClass, whereObject, extraWhere, args, propertyFromMaster, "");

        s = "SELECT "+getMax(ds, max)+"COUNT(*) " + s;
        // LOG.fine("selectClass="+selectClass.getName()+", whereObject="+whereObject+", extraWhere="+extraWhere+", propertyFromMaster="+propertyFromMaster+", sql="+s);

        Statement st = null;
        try {
            st = ds.getStatement(s);
            java.sql.ResultSet rs = st.executeQuery(s);
            rs.next();
            int x = rs.getInt(1);
            if (max > 0 && x > max) x = max;
            return x;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (st != null) ds.releaseStatement(st);
        }
    }
    public static int count(OADataSourceJDBC ds, Class clazz, String queryWhere, int max) {
    	return count(ds, clazz, queryWhere, (Object[]) null, max);
    }

    public static int count(OADataSourceJDBC ds, Class clazz, String queryWhere, Object param, int max) {
    	Object[] params = null;
    	if (param != null) params = new Object[] {param};
    	return count(ds, clazz, queryWhere, params, max);
    }

    public static int count(OADataSourceJDBC ds, Class clazz, String queryWhere, Object[] params, int max) {
        QueryConverter qc = new QueryConverter(ds);

        String s = qc.convertToSql(clazz, queryWhere, params, "");
        s = "SELECT "+getMax(ds,max)+"COUNT(*) " + s;
        // LOG.fine("selectClass="+clazz.getName()+", querWhere="+queryWhere+", sql="+s);

        Statement st = null;
        try {
            st = ds.getStatement(s);
            java.sql.ResultSet rs = st.executeQuery(s);
            rs.next();
            int x = rs.getInt(1);
            if (max > 0 && x > max) x = max;
            return x;
        }
        catch (Exception e) {
            throw new RuntimeException("OADataSourceJDBC.count() ", e);
        }
        finally {
            if (st != null) ds.releaseStatement(st);
        }
    }

    public static byte[] getPropertyBlobValue(OADataSourceJDBC ds, OAObject whereObject, String property) throws Exception {
        if (whereObject.getNew()) return null;
        if (property == null) return null;

        Class clazz = whereObject.getClass();
        Table table = ds.getDatabase().getTable(clazz);
        if (table == null) {
            throw new Exception("table not found for class="+clazz+", property="+property);
        }
        //  qqqqqqqqqqqqqqqqqqqqqq USE preparedStatement        
        QueryConverter qc = new QueryConverter(ds);

        Column[] cols = qc.getSelectColumnArray(clazz);
        String colName = "";
        String pkeyColName = "";
        String pkey = null; 
        Column[] columns = null;
        for (Column c : cols) {
            if (property.equalsIgnoreCase(c.propertyName)) {
                colName = c.columnName;
                columns = new Column[]{c};
            }
            else if (c.primaryKey) {
                pkeyColName = c.columnName;
                pkey = whereObject.getPropertyAsString(c.propertyName);
            }
        }
        if (columns == null) {
            throw new Exception("column name not found for class="+clazz+", property="+property);
        }
        if (pkey == null) {
            throw new Exception("pkey column not found for class="+clazz+", property="+property);
        }
        
        String query = "SELECT " + colName;
        query  += " FROM " + table.name + " WHERE " + pkeyColName + " = " + pkey;

        byte[] result = null;
        Statement statement = null;
        OATransaction trans = null;
        try {
            trans = new OATransaction(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
            trans.start();
            
            statement = ds.getStatement(query);
            ResultSet rs = statement.executeQuery(query);
            boolean b = rs.next();
            if (!b) return null;
            
            Blob blob = rs.getBlob(1);
            if (blob != null) result = blob.getBytes(1, (int) blob.length());
            rs.close();
        }
        finally {
            ds.releaseStatement(statement);
            trans.commit();
        }
        return result;
    }

}

