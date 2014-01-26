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
package com.viaoa.ds.jdbc.query;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.*;

import com.viaoa.object.*;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.ClassModifier;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OATime;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;

public class ResultSetIterator implements Iterator {
    private static Logger LOG = Logger.getLogger(ResultSetIterator.class.getName());
    
	OADataSourceJDBC ds;
    Class clazz;
    String query;
    Statement statement;
    PreparedStatement preparedStatement;
    OATransaction transaction; 
    ResultSet rs;
    Column[] columns;
    Object[] values;
    ColumnInfo[] columnInfos;
    boolean bMore = false;
    int lastPkeyColumn;  // last column needed to be able to create an ObjectKey, to do a cache lookup
    int max;
    int cnter;

    // used when first selecting only the primary key column
    String query2;
    Statement statement2;
    ResultSet rs2;
    int idColumnCount;
    OAObjectInfo oi;
    Object[] pkeyValues;
    boolean bDatesIncludeTime;
    Object objectTrue, objectFalse;
    boolean bDirty;  //qqqq Not yet implemented
    boolean bIsSelecting;
    boolean bInit;
    DataAccessObject dataAccessObject; 
    DataAccessObject.ResultSetInfo resultSetInfo = new DataAccessObject.ResultSetInfo();
    Object[] arguments; // when using preparedStatement
    boolean bUsePreparedStatement;
    OAFilter filter;

    class ColumnInfo {
        Column column;
        int pkeyPos=-1;
    }
    
    
    public ResultSetIterator(OADataSourceJDBC ds, Class clazz, DataAccessObject dataAccessObject, String query, String query2, int max) {
        this(ds, clazz, null, query, query2, max, dataAccessObject);
    }

    // 20121013 to use with preparedStatement
    public ResultSetIterator(OADataSourceJDBC ds, Class clazz, DataAccessObject dataAccessObject, String query, Object[] arguments) {
        this.ds = ds;
        this.clazz = clazz;
        this.dataAccessObject = dataAccessObject;
        this.query = query;
        this.arguments = arguments;
        bUsePreparedStatement = true;
    }
    
    
    public ResultSetIterator(OADataSourceJDBC ds, Class clazz, Column[] columns, String query, int max) {
        this(ds, clazz, columns, query, null, max, null);
    }
    
static int qqq;
static PrintWriter printWriter2;
static PrintWriter printWriter;

    /**
     * @param query2 used if the first query only returns pkIds.  
     * Query2 will need to use ? to position where the id values will be inserted.
     * Query2 needs to be the correct SQL statement.
     */
    public ResultSetIterator(OADataSourceJDBC ds, Class clazz, Column[] columns, String query, String query2, int max) {
        this(ds, clazz, columns, query, query2, max, null);
    }
    
    private ResultSetIterator(OADataSourceJDBC ds, Class clazz, Column[] columns, String query, String query2, int max, DataAccessObject dataAccessObject) {
        // LOG.fine("query="+query+", query2="+query2+", columns.length="+columns.length+", max="+max);
        this.ds = ds;
        this.clazz = clazz;
        this.columns = columns;
        this.query = query;
        this.query2 = query2;
        this.max = max;
        this.dataAccessObject = dataAccessObject;
    }

    public void setFilter(OAFilter f) {
        this.filter = f;
    }
    
    public static int DisplayMod = 5000;    
    protected synchronized void init() {
        if (bInit) return;
        bInit = true;

        if ( ((++qqq)%DisplayMod==0)) {
            String s = query;
            int pos = s.toUpperCase().indexOf("FROM");
            if (pos > 0) s = s.substring(pos);
            System.out.println((qqq)+") ResultSetIterator: query="+s);
        }
        /*
        if ( (qqq%(DisplayMod*4)==0)) {        
            Vector v = OADataSource.getInfo();
            for (int i=0; i<v.size(); i++) {
                System.out.println("  "+v.elementAt(i));
            }
        }
        */
        // 20120227 add transaction
        //        transaction = new OATransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
        //        transaction.start();

        
        this.oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        
        DBMetaData dbmd = ds.getDBMetaData();
        this.bDatesIncludeTime = dbmd.getDatesIncludeTime();
        this.objectTrue = dbmd.getObjectTrue();
        this.objectFalse = dbmd.getObjectFalse();

        String[] pkeys = this.oi.getIdProperties();
        if (dataAccessObject != null) {
            // no-op needed
            idColumnCount = (pkeys == null) ? 0 : pkeys.length;
        }
        else {
            this.values = new Object[columns.length];
            this.columnInfos = new ColumnInfo[columns.length];
    
            this.pkeyValues = new Object[pkeys.length];
    
            // create column infos
            for (int i=0; i<columns.length; i++) {
                columnInfos[i] = new ColumnInfo();
                columnInfos[i].column = columns[i]; 
                if (columns[i].primaryKey) idColumnCount++;
                assert (columns[i].clazz != null);
                if (columns[i].propertyName == null) {
                    assert(columns[i].fkeyLink != null);
                    continue;
                }
                for (int j=0; j<pkeys.length; j++) {
                    if (pkeys[j].equalsIgnoreCase(columns[i].propertyName)) {
                        columnInfos[i].pkeyPos = j;
                        lastPkeyColumn = Math.max(lastPkeyColumn, i);
                    }
                }
            }
        }
        
        rs = null;
        try {
            // 20121013
            if (bUsePreparedStatement) {
                preparedStatement = ds.getConnectionPool().getPreparedStatement(query, false);
                for (int i=0; arguments!=null && i < arguments.length; i++) {
                    preparedStatement.setObject(i+1, arguments[i]);
                }
                rs = preparedStatement.executeQuery();
            }
            else if (statement == null && ds != null) {
                statement = ds.getStatement(query);
                if (max > 0) statement.setMaxRows(max);
                rs = statement.executeQuery(query);
            }
            bIsSelecting = true;
            
            bMore = rs != null && rs.next(); // goto first
            bIsSelecting = false;
            if (!bMore) close();
        }
        catch (Exception e) {
            close();
            throw new RuntimeException(e + ", query: "+query, e);
        }
        finally {
            bIsSelecting = false;
        }
    }
    
    public boolean hasNext() {
        if (nextObject == null) {
            nextObject = next();
        }
        return (nextObject != null);
        //if (!bInit) init();
        //return (bMore);
    }

    private Object nextObject;
    public synchronized Object next() {
        Object obj;
        if (nextObject != null) {
            obj = nextObject;
            nextObject = null;
            return obj;
        }
        for (;;) {
            obj = _next();
            if (obj == null || filter == null || filter.isUsed(obj)) break;
        }
        return obj;
    }    
    private Object _next() {
        if (!bInit) init();
        if (rs == null) return null;
        if (max > 0 && cnter > max) { 
            close();
            return null;
        }
    
        boolean bDataSourceLoadingObject = true;
        OAObject oaObject = null;
        boolean bLoadedObject = false;
        boolean bSetChangedAndNew = false;
        try {
            OAThreadLocalDelegate.setDataSourceLoadingObject(true);
            
            ResultSet resultSet = rs;
            if (query2 != null) {  // need to do a seperate select to get data for each row
                if (statement2 == null && ds != null) {
                    statement2 = ds.getStatement(query);
                }
                for (;bMore;) {
                    String newQuery = query2;
                    int pos = 0;
                    for (int i=0; i<idColumnCount; i++) {
                        Object obj = rs.getObject(i+1);
                        String s;
                        if (rs.wasNull()) s = null;
                        else s = OAConverter.toString(obj);
                        pos = newQuery.indexOf('?', pos);
                        if (pos >= 0) {
                            newQuery = newQuery.substring(0,pos) + s + newQuery.substring(pos+1);
                            if (s == null) pos += 4;
                            else pos += s.length();
                        }
                        else throw new RuntimeException("parameter mismatch in query "+query2);
                    }
                    rs2 = statement2.executeQuery(newQuery);
                    if (rs2.next()) {
                        resultSet = rs2;
                        break;
                    }
                    bMore = rs.next();  // goto next
                    rs2.close();
                    if (!bMore) return null;
                }
            }

/** qqqqqqqqqqqq  todo:  when implementing bDirty, if true, then need to run the following code, so that events will get sent out correctly
             if (bDataSourceLoadingObject) {
                OAThreadLocalDelegate.setDataSourceLoadingObject(false);
                bDataSourceLoadingObject = false;
            }
 */

            if (dataAccessObject != null) {
                resultSetInfo.reset(resultSet);
                oaObject = dataAccessObject.getObject(resultSetInfo);
                bLoadedObject = !resultSetInfo.getFoundInCache();
                bSetChangedAndNew = true;
                
                if (bLoadedObject) {
                    oaObject = (OAObject) OAObjectCacheDelegate.add(oaObject, false, true);
                }
            }
            else {
                for (int i=0; i < columnInfos.length; i++) {
                    if (columnInfos[i].column.clazz.equals(String.class)) {
                        values[i] =  resultSet.getString(i+1);
                    }
                    else if (columnInfos[i].column.clazz.equals(byte[].class)) {
                        // 20100514
                        Blob blob =  resultSet.getBlob(i+1);
                        if (blob != null) {
                            values[i] = blob.getBytes(1, (int) blob.length());
                        }
                        else values[i] = null;
                    }
                    else {
                        values[i] =  resultSet.getObject(i+1);
                        if (values[i] == null) {
                        }
                        else if (resultSet.wasNull()) {
                            values[i] = null;
                        }
                        else {
                            values[i] = convert(columnInfos[i].column.clazz, values[i]);                    
                        }
                    }
                    if (columnInfos[i].pkeyPos >= 0) {
                        pkeyValues[columnInfos[i].pkeyPos] = values[i];
                        if (i == lastPkeyColumn) {
                            // try to find existing object
                            oaObject = OAObjectCacheDelegate.get(clazz, new OAObjectKey(pkeyValues));
                            if (oaObject != null && !bDirty) break;
                        }
                    }
                }
                
                if (oaObject == null || bDirty) {
                    boolean bNew;
                    if (oaObject == null) {
                        bNew = true;
                        oaObject = (OAObject) OAObjectReflectDelegate.createNewObject(clazz);
                    }
                    else bNew = false;
                    
                    for (int i=0; i < columns.length; i++) {
                        if (!bNew && columnInfos[i].pkeyPos >= 0) continue;
                        if (columnInfos[i].pkeyPos >= 0 || columns[i].fkeyLink == null) { 
                            try {
                                oaObject.setProperty(columns[i].propertyName, values[i]);
                            }
                            catch (Exception e) {
                                if (bNew && columnInfos[i].pkeyPos >= 0) {
                                    OAObject objx = OAObjectCacheDelegate.get(clazz, new OAObjectKey(pkeyValues));
                                    if (objx != null) {
                                        LOG.log(Level.WARNING, "Error while setting property "+columns[i].propertyName+", object has been found in cache, so everything is good", e);
                                        oaObject = objx;
                                        bNew = false;
                                        if (!bDirty) break;
                                    }
                                    else {
                                        LOG.log(Level.WARNING, "Error while setting property "+columns[i].propertyName+", NOT found in cache as hoped :(  will continue anyway", e); 
                                    }
                                }
                                else {
                                    LOG.log(Level.WARNING, "Error while setting property "+columns[i].propertyName+", will continue anyway", e);
                                }
                            }
                        }
                        else {
                            // fkey
                            if (columns[i].fkeyLink.fkeys.length == 1) {
                                oaObject.setProperty(columns[i].fkeyLink.propertyName, values[i]);
                                continue;
                            }
                            
                            if (columns[i].fkeyLinkPos > 0) continue; // already loaded (in next code) 
                            Object[] ids = new Object[columns[i].fkeyLink.fkeys.length];
                            for (int j=i; j < columns.length; j++) {
                                if (columns[j].fkeyLink == columns[i].fkeyLink) {
                                    ids[columns[j].fkeyLinkPos] = values[j];
                                }
                            }
                            oaObject.setProperty(columns[i].fkeyLink.propertyName, new OAObjectKey(ids));
                        }
                    }

                    if (bNew && oi.getAddToCache()) { // 20110731 add to cache, OAThreadLocal.SkipObjectInitialize
                        oaObject = (OAObject) OAObjectCacheDelegate.add(oaObject, false, true);
                    }
                    
                    OAObjectDelegate.setNew(oaObject, false);
                    oaObject.setChanged(false);
                    bLoadedObject = true;
                    bSetChangedAndNew = true;
                }
            }

            ++cnter;
            
            if (bDataSourceLoadingObject) {
                OAThreadLocalDelegate.setDataSourceLoadingObject(false);
                bDataSourceLoadingObject = false;
            }
            if (bLoadedObject) oaObject.afterLoad();

            if (rs != null) {
                bMore = rs.next();  // goto next
                if (!bMore) close();
            }
            return oaObject;
        }
        catch (Exception e) {
            String s = String.format("Exception in next(), thread=%s, query=%s, bClosed=%b", Thread.currentThread().getName(), query, bClosed);
            LOG.log(Level.WARNING, s, e);
            throw new RuntimeException(e);
        }
        finally {
            if (bLoadedObject && !bSetChangedAndNew && oaObject != null) {
                OAObjectDelegate.setNew(oaObject, false);
                oaObject.setChanged(false);
            }
            if (bDataSourceLoadingObject) {
                OAThreadLocalDelegate.setDataSourceLoadingObject(false);
            }
        }
    }    
    
boolean bClosed;//qqqqqq temp for debugging
    // part of iterator interface
    public void remove() {
        close();
    }

    public void finalize() throws Throwable {
        super.finalize();
        close();
    }

    // 20110407 added synchronized, since OASelectManager could close iterator while it is performing next()
    public synchronized void close() {
        bClosed = true;        
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (rs2 != null) {
                rs2.close();
                rs2 = null;
            }
            if (transaction != null) {
                transaction.commit();
            }

            if (statement != null) {
                if (bIsSelecting) {
                    try {                    
                        statement.cancel();
                    }
                    catch (Exception exx) {
                    }
                }
            }
        }
        catch (Exception e) {
            // throw new OADataSourceException(OADataSourceJDBC.this, "OADataSource.getStatement() "+e);
        }
        finally {
            rs = null;
            rs2 = null;
            bMore = false;
            if (ds != null) {
                if (preparedStatement != null) {  // 20121013
                    ds.getConnectionPool().releasePreparedStatement(preparedStatement, true);
                }
                else {
                    ds.releaseStatement(statement);
                    if (statement2 != null) ds.releaseStatement(statement2);
                }
            }
            statement = null;
            statement2 = null;
            transaction = null;
            preparedStatement = null;
        }
    }

    private Object convert(Class paramType, Object obj) throws Exception {
        if (obj != null && obj.getClass().equals(paramType)) return obj;

        if (obj instanceof Clob) {
            obj = ((Clob)obj).getSubString(1, (int) ((Clob)obj).length());
        }
        else if (obj.getClass().isArray()) {
            // 2006/06/01
            Class c = ClassModifier.getClassWrapper(paramType);
            if (Number.class.isAssignableFrom(c) ) {
                obj = new java.math.BigInteger((byte[]) obj);
            }
            else if (java.util.Date.class.isAssignableFrom(paramType) ) {
                obj = new java.util.Date(new java.math.BigInteger((byte[]) obj).longValue());
            }
            else if (paramType.equals(String.class) ) {  // 2006/11/08
                obj = new String((byte[]) obj);
            }
        }
        
        if (obj instanceof String) {
            String s = (String) obj;
            String fmt = null;
            if (paramType.equals(String.class)) {
                obj = repairSingleQuotes((String) obj);
            }
            else if (paramType.equals(int.class)) obj = Integer.valueOf(s);
            else if (paramType.equals(double.class)) obj = Double.valueOf(s);
            else if (paramType.equals(long.class)) obj = Long.valueOf(s);
            else if (paramType.equals(short.class)) obj = Short.valueOf(s);
            else if (paramType.equals(float.class)) obj = Float.valueOf(s);
            else if (paramType.equals(char.class)) obj = new Character(s.charAt(0));
            else {
                if ( java.util.Date.class.isAssignableFrom(paramType) ) {
                    if (bDatesIncludeTime) fmt = "yyyy-MM-dd hh:mm:ss.SSS"; // 1999-11-21 14:21:53.123
                    else {
                        fmt = "yyyy-MM-dd"; // 1999-11-21
                        if (paramType.equals(Time.class)) fmt = "hh:mm:ss.SSS"; // 14:21:53
                    }
                }
                else {
                    if (bDatesIncludeTime) fmt = "yyyy-MM-dd hh:mm:ss.SSS"; // 1999-11-21 14:21:53.123
                    else {
                        if (paramType.equals(OADate.class)) fmt = "yyyy-MM-dd";
                        else if (paramType.equals(OATime.class)) fmt = "hh:mm:ss.SSS";
                        else if (paramType.equals(OADateTime.class)) fmt = "yyyy-MM-dd hh:mm:ss.SSS";
                    }
                }
                obj = OAConverter.convert(paramType, (String) obj, fmt);
            }
        }
        else if (obj instanceof Number) {
            Number num = (Number) obj;
            if (paramType.equals(int.class)) obj = new Integer(num.intValue());
            else if (paramType.equals(boolean.class)) obj = new Boolean(num.intValue() != 0);
            else if (paramType.equals(double.class)) obj = new Double(num.doubleValue());
            else if (paramType.equals(String.class)) obj = num.toString();
            else if (paramType.equals(long.class)) obj = new Long(num.longValue());
            else if (paramType.equals(short.class)) obj = new Short(num.shortValue());
            else if (paramType.equals(float.class)) obj = new Float(num.floatValue());
            else if (paramType.equals(char.class)) obj = new Character((char) num.shortValue());
            else if (paramType.equals(java.awt.Color.class)) obj = new java.awt.Color(num.intValue());
        }
        else if (obj instanceof Double && paramType.equals(float.class)) {
            obj = new Float( ((Double)obj).floatValue() ) ;
        }
        else if (obj instanceof java.util.Date) {
            if (paramType.equals(Time.class)) obj = new Time( ((java.util.Date)obj).getTime() );
            else if (paramType.equals(java.sql.Timestamp.class)) obj = new Timestamp( ((java.util.Date)obj).getTime() );
            else if (paramType.equals(OADate.class)) obj = new OADate((java.util.Date)obj);
            else if (paramType.equals(OATime.class)) obj = new OATime( (java.util.Date)obj );
            else if (paramType.equals(OADateTime.class)) obj = new OADateTime( (java.util.Date)obj ); // 2006/11/08
        }
        else if (obj instanceof Boolean) {
            boolean b = ((Boolean) obj).booleanValue();
            if (paramType.equals(boolean.class));
            else if (paramType.equals(int.class)) obj = new Integer(b?1:0);
            else if (paramType.equals(double.class)) obj = new Double(b?1.0:0.0);
            else if (paramType.equals(String.class)) obj = obj.toString();
            else if (paramType.equals(long.class)) obj = new Long((long) (b?1:0));
            else if (paramType.equals(short.class)) obj = new Short((short)(b?1:0));
            else if (paramType.equals(float.class)) obj = new Float((float)(b?1.0f:0.0f));
            else if (paramType.equals(char.class)) obj = new Character((char) (b?'1':'0'));
        }

        if (paramType.equals(boolean.class)) {
            if (!(obj instanceof Boolean)) {
                if (objectTrue == null || objectFalse == null) {
                    if (obj instanceof Number) new Boolean( ((Number)obj).intValue()!=0);
                    //else throw new OADataSourceException(OADataSourceJDBC.this,"ResultSetIterator.next() "+" method "+method.getName()+" uses a boolean and database stores data as "+obj.getClass());
                }
                else {
                    if (obj.equals(objectTrue)) {
                        obj = new Boolean(true);
                    }
                    else if (obj.equals(objectFalse)) {
                        obj = new Boolean(false);
                    }
                    else {
                       // throw new OADataSourceException(OADataSourceJDBC.this,"ResultSetIterator.next() "+" method "+method.getName()+" cant convert "+obj+" to a boolean, it does not match objectTrue or objectFalse values");
                    }
                }
            }
        }
        return obj;
    }
    protected String repairSingleQuotes(String value) {
        return value;
    }
    
}    

