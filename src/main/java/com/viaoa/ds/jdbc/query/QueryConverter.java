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
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAString;
import com.viaoa.ds.query.*;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.delegate.*;


/** 
    Used internally for queries/selects to convert OA object queries into SQL.<br>
    Uses JOINs or LEFT OUTER JOINS to connect super/sub classes and ONE type references.<br>
    Uses EXISTS for MANY type references.<br>
    Will work with any valid object path.<br>
    Allows OR, ||, AND, &&, (), =, !=, NULL, &lt;, &lt;=, &gt;, &gt;=
    <p>
    Converts to SQL using OAQueryTokenizer to parse object query into tokens, along with Database to Object Mapping.
    @see #getUseDistinct to see if the query will require the use of the "DISTINCT" keyword.
*/
public class QueryConverter {
    private static Logger LOG = Logger.getLogger(QueryConverter.class.getName());
    
	private DBMetaData dbmd;
	private Database database;
    private boolean bEmpty; // if set, then the whereObject does not have any records to select
    private Object selectObject; // the object to use in place of select, instead of using query
    private Linkinfo root;   // root of tree that keeps track of tables/objects that are needed for query.  Method getJoins() uses this to build Join clause
    private int counter;     // used by LinkInfo 
    private boolean bUseLeftJoin;    // all joins need to use "LEFT OUTER JOIN"
    private boolean bUsingOR;    // an "OR" is used in where clause.  If other Joins will be needed then bUseLeftJoin will be set to true
    private boolean bUseExists;
    private String LB, RB;
    private String extraWhere;
    private boolean bUseDistinct;
    
    public QueryConverter(OADataSourceJDBC ds) {
    	this.dbmd = ds.getDBMetaData();
    	this.database = ds.getDatabase();
    	reset();
    }
    public QueryConverter(Database database, DBMetaData dbmd) {
    	this.database = database;
    	this.dbmd = dbmd;
    	reset();
    }

	/** 
     * returns true if this query will only be able to select the Pk columns
     * and the "Distinct" keyword should be used by the query to make sure that duplicates
     * are not selected.
     */ 
    public boolean getUseDistinct() {
    	return bUseDistinct;
    }
    
    protected void reset() {
    	this.LB = dbmd.leftBracket;
    	this.RB = dbmd.rightBracket;
    	this.bUseExists = dbmd.useExists;
    	this.arguments = null;
    }
    
    /** returns list of columns to select for clazz primary key column */
    public String getPrimaryKeyColumns(Class clazz) {
        Table table = database.getTable(clazz);
        if (table == null) return "";

        if (table.selectPKColumns != null) return table.selectPKColumns;
        
        String strColumn = null;
        DataAccessObject dao = table.getDataAccessObject();
        if (dao != null) {
            strColumn = dao.getPkeySelectColumns();
        }
        else {
            String tableName = table.name.toUpperCase();
    
            Column[] cols = table.getColumns();
            for (int i=0; cols != null && i < cols.length; i++) {
                if (!cols[i].primaryKey) continue;
                if (strColumn == null) strColumn = "";
                else strColumn += ", ";
                strColumn += LB + tableName + RB + "." + LB + cols[i].columnName.toUpperCase()+RB;
            }
        }
        table.selectPKColumns = strColumn;
        return strColumn;
    }


    // only used if table.DataAccessObject is not used
    public Column[] getSelectColumnArray(Class clazz) {
        Table tableMain = database.getTable(clazz);
        if (tableMain.selectColumnArray != null) return tableMain.selectColumnArray;
    	
    	Class[] classes = getSelectClasses(clazz);
        ArrayList al = null;

        for (int ii=0; classes != null && ii < classes.length; ii++) {
            Class c = classes[ii];
            OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(c);
            
            Table table = database.getTable(c);
            String tableName = table.name.toUpperCase();

            // get all columns that map to properties
            Column[] columns = table.getSelectColumns();
            
            if (ii == 0 && classes.length == 1) return columns;
            if (al == null) al = new ArrayList(25);
            for (int i=0; columns != null && i < columns.length; i++) {
                // 20121001 skip byte[]
                Class cz = OAObjectInfoDelegate.getPropertyClass(oi, columns[i].propertyName);
                if (cz.isArray()) {
                    cz = cz.getComponentType();
                    if (cz.equals(byte.class)) {
                        continue;
                    }
                }
                al.add(columns[i]);
            }
        }
        Column[] cols = new Column[al.size()];
        al.toArray(cols);
        tableMain.selectColumnArray = cols;
        return cols;
    }        
    
    /** returns list of columns to select for clazz, not pkey or fkey columns, includes columns for superClasses */
    public String getSelectColumns(Class clazz, boolean bDirty) {
        Table tableMain = database.getTable(clazz);
        if (!bDirty && tableMain.selectColumns != null) return tableMain.selectColumns;

    	Class[] classes = getSelectClasses(clazz);
        String strColumn = "";
        for (int ii=0; classes != null && ii < classes.length; ii++) {
            Class c = classes[ii];
            
            Table table = database.getTable(c);
            
            DataAccessObject dao = table.getDataAccessObject();
            if (!bDirty && dao != null) {
                if (strColumn.length() > 0) strColumn += ", ";
                strColumn += dao.getSelectColumns();
                tableMain.selectColumns = strColumn;
            }
            else {
                String tableName = table.name.toUpperCase();
    
                // get all columns that map to properties
                Column[] columns = table.getSelectColumns();
                for (int i=0; columns != null && i < columns.length; i++) {
                    if (ii > 0 || i > 0) strColumn += ", ";
                    strColumn += LB + tableName + RB + "." + LB + columns[i].columnName.toUpperCase()+RB;
                }
            }
        }
        return strColumn;
    }        

    public Class[] getSelectClasses(Class startClass) {
        Table table = database.getTable(startClass);
        if (table.selectClasses != null) return table.selectClasses;
    	
        Vector vec = new Vector(10,10);
        getSelectSuperclasses(vec, startClass);
        getSelectSubclasses(vec, startClass, false);
        Class[] cs = new Class[vec.size()];
        vec.copyInto(cs);
        
        table.selectClasses = cs;
        return cs;
    }
    private void getSelectSuperclasses(Vector vec, Class clazz) {
        Table table = database.getTable(clazz);
        if (table == null) return;
 
        Class c = clazz.getSuperclass();
        if (c != null && !(c.equals(OAObject.class)) ) getSelectSuperclasses(vec, c);
        vec.addElement(clazz);
    }
    private void getSelectSubclasses(Vector vec, Class clazz, boolean bIncludeThis) {
        Table table = database.getTable(clazz);
        if (table == null) return;
 
        if (bIncludeThis) vec.addElement(clazz);
        
        Class[] subclasses = table.subclasses;
        for (int i=0; subclasses != null && i < subclasses.length; i++) {
            getSelectSubclasses(vec, subclasses[i], true);
        }
    }
    

    // 20121013
    public String convertForPreparedStatmentSql(Class selectClass, Object whereObject, String propertyFromMaster, String orderBy)  {

        reset();
        Vector vecParam = new Vector(3,3);
        String where = convertToWhere(selectClass, whereObject, propertyFromMaster, vecParam);
    
        Object[] params = new Object[vecParam.size()];
        vecParam.copyInto(params);
        
        String sql = convertToSql(selectClass, where, params, orderBy, true, true);
        
        return sql;
    }

    /**
        propertyFromMaster name of link to use, from whereObject from Table(clazz).getLinks()[].propertyName = propertyFromMaster
    */
    public String convertToSql(Class selectClass, Object whereObject, String extraWhere, Object[] args, String propertyFromMaster, String orderBy)  {
        reset();
    	Vector vecParam = new Vector(3,3);
    	String s = convertToWhere(selectClass, whereObject, propertyFromMaster, vecParam);
        if (selectObject != null || bEmpty) return "";
    
        if (!OAString.isEmpty(extraWhere)) {
            if (!OAString.isEmpty(s)) s = "(" + s + ") AND ";
            s += extraWhere;
        }
        
        for (int i=0; args != null && i > args.length; i++) {
            vecParam.add(args[i]);
        }
        Object[] params = new Object[vecParam.size()];
        vecParam.copyInto(params);
        
        return convertToSql(selectClass, s, params, orderBy);
    }

    // 20121013 
    public String convertToPreparedStatementSql(Class selectClass, Object whereObject, String extraWhere, Object[] args, String propertyFromMaster, String orderBy)  {
        reset();
        Vector vecParam = new Vector(3,3);
        String s = convertToWhere(selectClass, whereObject, propertyFromMaster, vecParam);
        if (selectObject != null || bEmpty) return "";
    
        if (!OAString.isEmpty(extraWhere)) {
            if (!OAString.isEmpty(s)) s = "(" + s + ") AND ";
            s += extraWhere;
        }
        
        for (int i=0; args != null && i > args.length; i++) {
            vecParam.add(args[i]);
        }
        Object[] params = new Object[vecParam.size()];
        vecParam.copyInto(params);
        
        String sql = convertToSql(selectClass, s, params, orderBy, true, true);
        return sql;
    }
    /** used when calling convertToPreparedStatementSql */
    private Object[] arguments;
    public Object[] getArguments() {
        return arguments;
    }
    
    private String convertToWhere(Class selectClass, Object whereObject, String propertyFromMaster, Vector vecParam) {
        String s = null;

        Table toTable;
        Class c;
        Class whereClass = whereObject.getClass();
                
        for ( ; s == null && whereClass!=null && !(whereClass.equals(OAObject.class)); whereClass = whereClass.getSuperclass()) {
            toTable = database.getTable(whereClass);
            if (toTable == null) return "";  // select all
            
            c = selectClass;
            for ( ;s == null && c!=null && !(c.equals(OAObject.class)); c = c.getSuperclass()) {
                Table fromTable = database.getTable(c);
                if (fromTable == null) throw new RuntimeException("QueryConverter.convertToWhere() cant find link");
                Link[] links = fromTable.getLinks();
                s = getWhere(links, fromTable, toTable, whereObject, propertyFromMaster, vecParam);
            }
        }
         
        // could not find it in direct links, now check any Link tables
        whereClass = whereObject.getClass();
        for (; s == null && whereClass!=null && !(whereClass.equals(OAObject.class)); whereClass = whereClass.getSuperclass()) {
            toTable = database.getTable(whereClass);
            
            c = selectClass;
            for ( ;s == null && c!=null && !(c.equals(OAObject.class)); c = c.getSuperclass()) {
                Table fromTable = database.getTable(c);
                if (fromTable == null) continue;
                Link[] links = fromTable.getLinks();
                for (int i=0; s==null && links!=null && i < links.length; i++) {
                    if (links[i].toTable.bLink) {
                        s = getWhere(links[i].toTable.getLinks(), links[i].toTable, toTable, whereObject, propertyFromMaster, vecParam);
                        bUseExists = false; // dont use EXISTS, use JOIN instead
                    }
                }
            }
        }
        if (s == null) {
            String test = "selectClass="+selectClass.getName()+" whereObject="+whereObject+" propertyFromMaster="+propertyFromMaster;
            throw new RuntimeException("QueryConverter.convertToWhere() Can not find links between objects/database: "+test);
        }
        return s;
    }

    private String getWhere(Link[] links, Table fromTable, Table toTable, Object whereObject, String propertyFromMaster, Vector vecParams) {
        // these are used to see if the object can be found without using select
        bEmpty = false;
        selectObject = null;
        
        for (int i=0; links!=null && i < links.length; i++) {
            Link link = links[i];
            Table newFromTable = null;
            if (link.toTable != toTable) {
                if (!link.toTable.bLink) continue;

                if (propertyFromMaster == null) continue;
                if (!propertyFromMaster.equalsIgnoreCase(link.reversePropertyName)) continue;
                
                
                // check link table links
                Link[] linx = link.toTable.getLinks();  // will have 2 links
                if (linx == null || linx.length != 2) continue;
                
                newFromTable = link.toTable; // will need to be changed if it is using link table
                if (linx[0].toTable == toTable && propertyFromMaster.equalsIgnoreCase(linx[0].reversePropertyName)) {
                    link = linx[0];
                }
                else {
                    if (linx[1].toTable == toTable && propertyFromMaster.equalsIgnoreCase(linx[1].reversePropertyName)) {
                        link = linx[1];
                    }
                    else continue;
                }
            }
            
            if (propertyFromMaster != null && propertyFromMaster.length() > 0) {
                if (!propertyFromMaster.equalsIgnoreCase(link.reversePropertyName)) continue;
            }
            
            if (newFromTable != null) fromTable = newFromTable;


            Column[] fkeys = link.fkeys;
            
            Column[] toFkeys = fromTable.getLinkToColumns(link, link.toTable);
            if (fkeys == null || toFkeys == null || fkeys.length != toFkeys.length) {
                throw new RuntimeException("different number of links between table "+fromTable.name+" and "+toTable.name );
            }
            String s = null;

            OAObjectKey key;
            if (toFkeys[0].primaryKey) {
                key = OAObjectKeyDelegate.getKey((OAObject)whereObject);
            }
            else {
            	// 20090621
                Object obj = OAObjectReflectDelegate.getRawReference((OAObject)whereObject, propertyFromMaster);
                if (obj instanceof OAObjectKey) key = (OAObjectKey) obj;
                else {
                    if (obj instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject) obj);
                    else key = null;
                }
                
                /* was:  bug: this could cause a inf loop for one2one links
                Object obj = ((OAObject)whereObject).getProperty(propertyFromMaster);
            	key = OAObjectKeyDelegate.getKey((OAObject) obj);
            	*/
            }
            
             
            Object[] ids;
            if (key != null) ids = key.getObjectIds();
            else ids = null;
                
            for (int j=0; fkeys!=null &&j<fkeys.length; j++) {
                if (s == null) s = "";
                else s += " AND ";

                if (fromTable.bLink) {
                    // 2003/05/10
                    s += links[i].propertyName+'.'+toFkeys[j].propertyName;
                    if (ids == null || ids.length <= j || ids[j] == null) s += " == NULL";    
                    else {
                    	s += " == ?";
                    	vecParams.add(ids[j]);
                    }
                }
                else {
                    s += fkeys[j].columnName;
                    if (ids == null || ids.length <= j || ids[j] == null) s += " == NULL";    
                    else {
                    	s += " == ?";
                    	vecParams.add(ids[j]);
                    }
                }
            }
            return s;
        }
        return null;  // not found
    }

    
    private String getWhere_ORIG(Link[] links, Table fromTable, Table toTable, Object whereObject, String propertyFromMaster, Vector vecParams) {
        // these are used to see if the object can be found without using select
        bEmpty = false;
        selectObject = null;
        
        for (int i=0; links!=null && i < links.length; i++) {
            if (links[i].toTable != toTable) continue;
            
            if (propertyFromMaster != null && propertyFromMaster.length() > 0) {
                if (!propertyFromMaster.equalsIgnoreCase(links[i].reversePropertyName)) continue;
            }
            Column[] fkeys = links[i].fkeys;
                
            Column[] toFkeys = fromTable.getLinkToColumns(links[i], toTable);
            if (fkeys == null || toFkeys == null || fkeys.length != toFkeys.length) {
                throw new RuntimeException("different number of links between table "+fromTable.name+" and "+toTable.name );
            }
            String s = null;

            OAObjectKey key;
            if (toFkeys[0].primaryKey) key = OAObjectKeyDelegate.getKey((OAObject)whereObject);
            else {
                // 20090621
                Object obj = OAObjectReflectDelegate.getRawReference((OAObject)whereObject, propertyFromMaster);
                if (obj instanceof OAObjectKey) key = (OAObjectKey) obj;
                else {
                    if (obj instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject) obj);
                    else key = null;
                }
                
                /* was:  bug: this could cause a inf loop for one2one links
                Object obj = ((OAObject)whereObject).getProperty(propertyFromMaster);
                key = OAObjectKeyDelegate.getKey((OAObject) obj);
                */
            }
            
             
            Object[] ids;
            if (key != null) ids = key.getObjectIds();
            else ids = null;
                
            for (int j=0; fkeys!=null &&j<fkeys.length; j++) {
                if (s == null) s = "";
                else s += " AND ";

                if (fromTable.bLink) {
                    // 2003/05/10
                    s += links[i].propertyName+'.'+toFkeys[j].propertyName;
                    if (ids == null || ids.length <= j || ids[j] == null) s += " == NULL";    
                    else {
                        s += " == ?";
                        vecParams.add(ids[j]);
                    }
                }
                else {
                    s += fkeys[j].columnName;
                    if (ids == null || ids.length <= j || ids[j] == null) s += " == NULL";    
                    else {
                        s += " == ?";
                        vecParams.add(ids[j]);
                    }
                }
            }
            return s;
        }
        return null;  // not found
    }    
    
    public String convertToSql(Class clazz, String where, Object[] params, String orderBy) {
    	return convertToSql(clazz, where, params, orderBy, true);
    }


	// returns FROM, WHERE and ORDER BY clauses as a single String
    protected String convertToSql(Class clazz, String where, Object[] params, String orderBy, boolean bGetSubclasses) {
        return convertToSql(clazz, where, params, orderBy, bGetSubclasses, false);
    }
    protected String convertToSql(Class clazz, String where, Object[] params, String orderBy, boolean bGetSubclasses, boolean bUsingPreparedStatement) {
    
    	reset();
        root = new Linkinfo(clazz);  // this will load super and sub classes
        this.arguments = params;

        // make sure all root are flagged to be used and "LEFT JOINED"
        root.bUsed = true;

        Linkinfo li = root;
        for (;;) {
            li = li.superLink;
            if (li == null) break;
            li.bUsed = true;
            li.bUseLeftJoin = true;
        }        
        for (int i=0; root.subLinks != null && i < root.subLinks.length; i++) {
            root.subLinks[i].bUsed = true;
            root.subLinks[i].bUseLeftJoin = true;
        }
        
        // orderBy clause
        if (orderBy != null && orderBy.length() > 0) {
            orderBy = parseOrderBy(clazz, orderBy);
            orderBy = " ORDER BY " + orderBy;  // "ORDER BY" must be in caps, since OADataSourceJDBC might look for it
        }

        // create where clause
        if (where != null && where.length() > 0) {
            if (bUseExists) where = parseWhereUseExist(clazz, where, params, bUsingPreparedStatement);
            else where = parseWhereUseJoin(clazz, where, params, bUsingPreparedStatement);
        }

        extraWhere = "";
        String joins = getJoins();
        if (extraWhere != null && extraWhere.length() > 0) {
            where = extraWhere + ((where != null && where.length() > 0)?(" AND ("+where+")"):"");
        }

        if (where!=null && where.length()>0) where = " WHERE "+where;
        else where = "";


        String s = "FROM " + joins + where + ((orderBy!=null)?orderBy:"");
        return s;
    }
    
    protected String parseOrderBy(Class clazz, String line) {
        if (line == null) return null;

        Table table = database.getTable(clazz);
        String tableName = table.name.toUpperCase();

        String newString="";
        boolean bAllowDesc = false;
        StringTokenizer st = new StringTokenizer(line," ,",true);
        for (;st.hasMoreTokens();) {
            String s = st.nextToken();
            if (s.equals(",")) {
                bAllowDesc = false;
            }
            else if (s.equals(" ")) {
            }
            else {
                if (bAllowDesc && s.equalsIgnoreCase("desc")) {
                    bAllowDesc = false;
                }
                else {
                	// 2006/09/13
                	String funcName = null;
                	int pos = s.indexOf('(');
                	if (pos >= 0) {
                		funcName = s.substring(0, pos);
                    	s = s.substring(pos+1);
                		pos = s.indexOf(')');
                		if (pos >= 0) s = s.substring(0, pos);
                	}
                	
                	Vector vecLink = new Vector(5,5);
                    Column column = parseLink(vecLink, s);  // populates vecLink with Linkinfos to match path in token.value
                    if (column == null) {
                        throw new RuntimeException("Cant use find column in ORDER clause: property \"" + s + "\" in query \""+line+"\"");
                    }
                    int xx =  vecLink.size();
                    for (int ii=1; ii<xx; ii++) {
                        Linkinfo li = (Linkinfo) vecLink.elementAt(ii);
                        li.bUsed = true;
                        li.bUseLeftJoin = true;
                        if (li.base != null) {
                            li.base.bUsed = true;
                            li.bUseLeftJoin = true;
                        }
                        if (li.bMany) {
                            throw new RuntimeException("Cant use link with MANY property in ORDER clause: property \"" + s + "\" in query \""+line+"\"");
                        }
                    }
                    Linkinfo li = (Linkinfo) vecLink.elementAt(xx-1);

                    
					// 2006/09/22 check for using case sensitve searches.
					String colName;
					if (dbmd.caseSensitive && column.type == java.sql.Types.VARCHAR && !column.primaryKey) {
						colName = column.columnLowerName;
						if (colName != null && colName.trim().length() > 0 && !colName.equalsIgnoreCase(column.columnName)) {
						}
						else {
							colName = column.columnName;
							funcName = dbmd.lowerCaseFunction;
						}
					}
					else {
						colName = column.columnName;
					}
					s = (funcName==null?"":(funcName + "(")) + LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName.toUpperCase() + RB + (funcName==null?"":")");
                    bAllowDesc = true;
                }
            }
            newString += s;
        }
        return newString;
    }


    protected String parseWhereUseJoin(Class clazz, String whereClause, Object[] params, boolean bUsingPreparedStatement) {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        Vector vecToken = qa.convertToTokens(whereClause);
        cleanTokens(vecToken, params);

        // 20100821 if where column in joined table uses "is null", then need to also 
        //  add "is not null" to "id" column, to offset records that dont exist
        //  ex: pet.exam.examitems.endDate = null - would return all with endDate=null and where no examItems exists for the pet

        int[] nullColumns = new int[0];
        
        int x = vecToken.size();
        for (int i=1; i<(x-1); i++) {
            OAQueryToken token = (OAQueryToken) vecToken.elementAt(i);
            if (!"IS".equals(token.value)) continue;
            token = (OAQueryToken) vecToken.elementAt(i+1);
            if (!"NULL".equals(token.value)) continue;
            
            token = (OAQueryToken) vecToken.elementAt(i-1);
            if (token.value.indexOf('.') < 0) continue;
            

            int colPos = (i - 1);
            nullColumns = OAArray.add(nullColumns, colPos+1);
            
            // found one 
            // need to add "(" and add "AND" for "is not null" column
            
            // (
            OAQueryToken t = new OAQueryToken();
            t.value = "(";
            t.type = OAQueryTokenType.SEPERATORBEGIN;
            vecToken.insertElementAt(t, colPos);
                
            // AND
            t = new OAQueryToken();
            t.type = OAQueryTokenType.AND;
            t.value = "AND";
            i += 3;
            vecToken.insertElementAt(t, i++);
            
            // COLUMN.PKEY
            token = (OAQueryToken) vecToken.elementAt(colPos+1);
            t = new OAQueryToken();
            t.type = token.type; 
            t.value = token.value;
            vecToken.insertElementAt(t, i++);
            
            // NOTEQUAL            
            t = new OAQueryToken();
            t.type = OAQueryTokenType.NOTEQUAL;
            t.value = "IS NOT";
            vecToken.insertElementAt(t, i++);
                        
            // NULL            
            t = new OAQueryToken();
            t.type = OAQueryTokenType.NULL;
            t.value = "NULL";
            vecToken.insertElementAt(t, i++);
            
            // )
            t = new OAQueryToken();
            t.type = OAQueryTokenType.SEPERATOREND;
            t.value = ")";
            vecToken.insertElementAt(t, i++);
        }        
        
        
        String where = "";
        Table table = null;
        Column column = null;
        Column origColumn = null;
        boolean bLastColumnWasInFunction = false;  // 20121120
        int paramPos = 0;
        x = vecToken.size();
        for (int i=0; i<x; i++) {
            OAQueryToken token = (OAQueryToken) vecToken.elementAt(i);
            String s = null;

            OAQueryToken tokenNext;
            if (i+1 != x) {
                tokenNext = (OAQueryToken) vecToken.elementAt(i+1);
            }
            else tokenNext = null;
            
            
            if (token.type == OAQueryTokenType.VARIABLE && tokenNext != null && tokenNext.type == OAQueryTokenType.FUNCTIONBEGIN) {
                s = token.value;
            }
            else if (token.type == OAQueryTokenType.VARIABLE) {
                Vector vecLink = new Vector(5,5);
                column = parseLink(vecLink, token.value);  // populates vecLink with Linkinfos to match path in token.value
                
                if (OAArray.contains(nullColumns,i)) {
                    OAQueryToken t = (OAQueryToken) vecToken.elementAt(i+4);
                    boolean b = false;
                    for (Column c : column.table.getColumns()) {
                        if (c.primaryKey) {
                            int pos = t.value.lastIndexOf('.');
                            t.value = t.value.substring(0, pos);
                            t.value += "." + c.propertyName;
                            b = true;
                            break;
                        }
                    }
                    if (!b) {
                        //qqqqqqqqq
                        // this needs to have a warning, that there is no other field to check for non-null value
                        //   for now, set it back to the same "IS NULL"
                        t = (OAQueryToken) vecToken.elementAt(i+5);
                        t.type = OAQueryToken.EQUAL;
                    }

                }
                
                if (column == null) {
                    throw new RuntimeException("Cant use find column in WHERE clause: property \"" + token.value + "\" in query \""+whereClause+"\"");
                }
                origColumn = column;
                int xx =  vecLink.size();

                // if a pkey column is being used, see if fkey can be used instead
                if (xx > 1 && column != null && column.primaryKey) {
                    Linkinfo li = (Linkinfo) vecLink.elementAt(xx-1);
                    if (!li.bMany) {
                        if (li.parent.bMany) {
                            /*
                            // many2many that uses a link table
                            int pos = 0; // currently only using one column, could be multipart id
                            for (int j=0; j<li.parent.links.length; j++) {  // parent should be link table qqqqqqqqqqqqqq
                                if (li.parent.links[j] == li) {
                                    pos = j;
                                    break;
                                }
                            }
                            column = li.linkFromParent.fkeys[pos];
                            */
                            column = li.linkFromParent.fkeys[0];                            
                        }
                        else column = li.linkFromParent.fkeys[0]; 
                        xx--;
                        vecLink.removeElementAt(xx);
                    }
                }

                for (int ii=1; ii<xx; ii++) {
                    Linkinfo li = (Linkinfo) vecLink.elementAt(ii);
                    li.bUsed = true;
                    li.bUseLeftJoin = true;
                    if (li.base != null) {
                        li.base.bUsed = true;
                        li.bUseLeftJoin = true;
                    }
                    if (li.bMany) {
                    	bUseDistinct = true;  // need to use "<DISTINCT KEYWORD>" with select
                    }
                }
                Linkinfo li = (Linkinfo) vecLink.elementAt(xx-1);

            	// 2006/09/22 check for using case sensitve searches.
            	String colName;
            	if (dbmd.caseSensitive && column.type == java.sql.Types.VARCHAR && !column.primaryKey) {
	            	colName = column.columnLowerName;
	            	if (colName != null && colName.trim().length() > 0 && !colName.equalsIgnoreCase(column.columnName)) {
	                	colName = colName.toUpperCase();
		            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
	            	}
	            	else {
		            	colName = column.columnName.toUpperCase();
		            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
	            		colName = dbmd.lowerCaseFunction + "(" + colName + ")";
	            	}

            		// need to make search string is lower case
                    if (i+1 < x) {
                    	OAQueryToken t = (OAQueryToken) vecToken.elementAt(i+1);
                    	if (t.isOperator()) {
                    		// find next var and make lowercase  2006/09/22
                            for (int j=i+2; j<x; j++) {
                                t = (OAQueryToken) vecToken.elementAt(j);
                                if (t.type == OAQueryTokenType.STRINGSQ || t.type == OAQueryTokenType.STRINGDQ) {
                                	t.value = t.value.toLowerCase();
                                	break;
                                }
                            }
                    	}
                    }
            	}
            	else {
            		colName = column.columnName;
                	if (colName != null) colName = colName.toUpperCase();
	            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
            	}
                s = colName;
            }
            else if (token.type == OAQueryTokenType.QUESTION) {
                if (params == null || paramPos >= params.length) {
                    throw new RuntimeException("wrong number of params in query");
                }
                if (origColumn == null) throw new RuntimeException("column not found for parameter");
                Object param = params[paramPos++];
                if (origColumn != column) {
                    if (param instanceof OAObject) {
                        param = ((OAObject) param).getProperty(origColumn.propertyName);
                    }
                }

                // 20121013
                if (bUsingPreparedStatement) s = "?";
                else {
                    if (bLastColumnWasInFunction) {
                        bLastColumnWasInFunction = false;
                        if (param instanceof String) s = ConverterDelegate.convert(dbmd, origColumn, param);                        
                        else s = ConverterDelegate.convert(dbmd, null, param);
                    }
                    else s = ConverterDelegate.convert(dbmd, origColumn, param);
                }
            }
            else if (token.type == OAQueryTokenType.STRINGSQ || token.type == OAQueryTokenType.STRINGDQ) {
            	if (column == null) throw new RuntimeException("column not found for parameter");
            	s = ConverterDelegate.convert(dbmd, column, token.value);
            }
            else if (token.type == OAQueryTokenType.NUMBER) {
            	if (column == null) throw new RuntimeException("column not found for parameter");
            	s = ConverterDelegate.convert(dbmd, column, token.value);
            }
            else if (token.type == OAQueryTokenType.FUNCTIONBEGIN) {
                bLastColumnWasInFunction = true;
                s = token.value;
            }
            else {
                s = token.value;
            }
            
            if (s != null && s.length() > 0) {
                if (where.length() > 0) where += " ";
                where += s;
            }
        }
        if (where.indexOf(" NULL") >= 0) {
            where = OAString.convert(where, " = NULL", " IS NULL");
            where = OAString.convert(where, " != NULL", " IS NOT NULL");
            where = OAString.convert(where, " <> NULL", " IS NOT NULL");
        }
        return where;
    }

    
    protected String parseWhereUseExist(Class clazz, String whereClause, Object[] params, boolean bUsingPreparedStatement) {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        Vector vecToken = qa.convertToTokens(whereClause);
        cleanTokens(vecToken, params);

        int existCount = 0;
        Vector vecLink = new Vector(5,5);
        Vector vecPreLink = null;
        
        String where = "";
        OAQueryToken holdToken = null;
        Column column = null;
        Column origColumn = null;
        int paramPos = 0;

        int x = vecToken.size();
        boolean bLastColumnWasInFunction = false;  // 20121120
        for (int i=0; i<x; i++) {
            OAQueryToken token = (OAQueryToken) vecToken.elementAt(i);

            OAQueryToken tokenNext;
            if (i+1 != x) {
                tokenNext = (OAQueryToken) vecToken.elementAt(i+1);
            }
            else tokenNext = null;
            
            String newVar = "";

            if (token.type == OAQueryTokenType.VARIABLE && tokenNext != null && tokenNext.type == OAQueryTokenType.FUNCTIONBEGIN) {
//20090608 to do later, have Datasource convert function name to correct name for the database that is being used.    
                newVar = token.value;
                if (holdToken != null) {
                    newVar = holdToken.value + " " + newVar;
                    holdToken = null;
                }
            }            
            else if (token.type == OAQueryTokenType.VARIABLE) {
                vecPreLink = (Vector) vecLink.clone();
                vecLink.removeAllElements();
                
                column = parseLink(vecLink, token.value);  // populates vecLink with Linkinfos to match path
                origColumn = column;
                int xx =  vecLink.size();
                
                // if a pkey column is being used, see if fkey can be used instead
                if (xx > 1 && column != null && column.primaryKey) {
                    Linkinfo li = (Linkinfo) vecLink.elementAt(xx-1);
                    if (!li.bMany) {
                        // use fkey from previous Linkinfo
                        column = li.linkFromParent.fkeys[0];  
                        li = (Linkinfo) vecLink.elementAt(xx-2);
                        xx--;
                        vecLink.removeElementAt(xx);
                    }
                }
                
                // see if path is same as prev path
                if (vecPreLink != null) {
                    int xxx = vecPreLink.size();
                    if (xxx > 0 && xxx <= xx && column != null) {
                        for (int j=0; j<xxx; j++) {
                            if (vecPreLink.elementAt(j) != vecLink.elementAt(j)) {
                                vecPreLink = null;
                                break;
                            }
                        }
                        for (int j=xxx; vecPreLink != null && j<xx; j++) {
                            Linkinfo li = (Linkinfo) vecLink.elementAt(j);
                            if (!li.bMany) vecPreLink = null;  // cant add JOINS to previous statement, only EXISTS
                        }
                    }
                    else {
                        vecPreLink = null;
                    }
                }

                if (vecPreLink == null) {
                    // close previous
                    for (int j=0; j<existCount; j++) where += ")";
                    existCount = 0;
                }

                if (holdToken != null) {
                    if (where.length() > 0) where += " ";
                    where += holdToken.value;
                    holdToken = null;
                }


                // set up variable name
                Linkinfo li = ( (xx == 0) ? root : (Linkinfo) vecLink.elementAt(0) );

                // check for MANY relationships within property path
                Linkinfo prev;
                Linkinfo liMany = null;
                String exists = "";
                for (int ii=1; ii<xx; ii++) {
                    prev = li;
                    li = (Linkinfo) vecLink.elementAt(ii);
                    if (!li.bMany) {
                        li.bUsed = true;
                        if (li.base != null) li.base.bUsed = true;
                        
                        if (bUsingOR && ii == 1) bUseLeftJoin = true;
                        continue;
                    }
                    if (vecPreLink != null) {
                        if (ii < vecPreLink.size() && (li == (Linkinfo) vecPreLink.elementAt(ii)) ) {
                            continue;  // exists/joins already built from last vecLink
                        }
                    }
    
                    // Many
                    if (liMany != null) {
                        // need to include JOINs for any ONE relationships that might have been in path
                        extraWhere = ""; // built by getJoins for Oracle
                        boolean bHold = bUseLeftJoin;
                        bUseLeftJoin = false;
                        newVar += "EXISTS (SELECT * FROM " + getJoins(vecLink, liMany, prev) + " WHERE " + exists + " AND ";
                        bUseLeftJoin = bHold;
                        if (extraWhere != null && extraWhere.length() > 0) {
                            newVar += extraWhere + " AND ";
                        }
                        existCount++;
                    }

                    exists = LB + prev.table.name.toUpperCase() + (prev.number>0?prev.number+"":"") + RB;
                    exists += ".";
                    exists += LB + li.linkFromParent.fkeys[0].columnName + RB;

                    exists += " = ";

                    Linkinfo lix = (li.base == null) ? li : li.base;  // base link is the "real" link to the parent link
                    exists += LB+ lix.table.name.toUpperCase() + (lix.number>0?li.number+"":"") + RB;
                    exists += ".";
                    exists += LB + lix.linkToParent.fkeys[0].columnName + RB;
                    
                    liMany = li;
                }
                
                if (liMany != null) {
                    if (column == null) {
                        // ex: orders.orderItems = null
                        //     orders.orderItems != null
                        // need to look ahead

                        OAQueryToken t = null;
                        if (i+1 < x) t = (OAQueryToken) vecToken.elementAt(i+1);
                        if (t != null && t.type == OAQueryTokenType.EQUAL) {
                            newVar += "NOT ";
                            vecLink.removeAllElements();  // so that it can not be shared with next path
                        }
                    }
                    extraWhere = ""; // built by getJoins for Oracle
                    boolean bHold = bUseLeftJoin;
                    bUseLeftJoin = false;
                    newVar += "EXISTS (SELECT * FROM " + getJoins(vecLink, liMany, null) + " WHERE " + exists;
                    bUseLeftJoin = bHold;
                    if (extraWhere != null && extraWhere.length() > 0) {
                        newVar += " AND " + extraWhere;
                    }
                    if (column != null) newVar += " AND ";
                    existCount++;
                }

                if (column != null) {
                	// 2006/09/22 check for using case sensitve searches.
                	String colName;
                	if (dbmd.caseSensitive && column.type == java.sql.Types.VARCHAR && !column.primaryKey) {
		            	colName = column.columnLowerName;
		            	if (colName != null && colName.trim().length() > 0 && !colName.equalsIgnoreCase(column.columnName)) {
		                	colName = colName.toUpperCase();
			            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
		            	}
		            	else {
			            	colName = column.columnName.toUpperCase();
			            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
		            		colName = dbmd.lowerCaseFunction + "(" + colName + ")";
		            	}

	            		// need to make search string is lower case
	                    if (i+1 < x) {
	                    	OAQueryToken t = (OAQueryToken) vecToken.elementAt(i+1);
	                    	if (t.isOperator()) {
	                    		// find next var and make lowercase  2006/09/22
	                            for (int j=i+2; j<x; j++) {
	                                t = (OAQueryToken) vecToken.elementAt(j);
	                                if (t.type == OAQueryTokenType.STRINGSQ || t.type == OAQueryTokenType.STRINGDQ) {
	                                	t.value = t.value.toLowerCase();
	                                	break;
	                                }
	                            }
	                    	}
	                    }
                	}
                	else {
                		colName = column.columnName;
                    	if (colName != null) colName = colName.toUpperCase();
		            	colName = LB+li.table.name.toUpperCase()+(li.number>0?li.number+"":"")+RB+"."+LB + colName + RB;
                	}
                	newVar += colName;
                }
                else {
                    // ignore "= null" or "!= null"
                    for (int j=0; j<existCount; j++) {
                        newVar += ")";
                    }
                    OAQueryToken t = null;
                    if (i+1 < x) t = (OAQueryToken) vecToken.elementAt(i+1);
                    if (t != null && t.isOperator()) i += 2;

                    vecLink.removeAllElements();  // so that it can not be shared with next path
                    existCount = 0;
                }
            }
            else if (token.type == OAQueryTokenType.QUESTION) {
            	if (params == null || paramPos >= params.length) {
                	throw new RuntimeException("wrong number of params in query");
            	}
            	if (origColumn == null) throw new RuntimeException("column not found for parameter");
            	Object param = params[paramPos++];
            	if (origColumn != column) {
            	    if (param instanceof OAObject) {
            	        param = ((OAObject) param).getProperty(origColumn.propertyName);
            	        params[paramPos-1] = param;
            	    }
            	}
            	
            	// 20130113
                if (bUsingPreparedStatement) newVar = "?";
                else {
                    if (bLastColumnWasInFunction) {
                        bLastColumnWasInFunction = false;
                        if (param instanceof String) newVar = ConverterDelegate.convert(dbmd, origColumn, param);                        
                        else newVar = ConverterDelegate.convert(dbmd, null, param);
                    }
                    else newVar = ConverterDelegate.convert(dbmd, origColumn, param);
                }
            	
                /*qqqqqqq was:             	
            	if (!bUsingPreparedStatement) {
            	    newVar = ConverterDelegate.convert(dbmd, origColumn, param);
            	}
            	*/
            }
            else if (token.type == OAQueryTokenType.STRINGSQ || token.type == OAQueryTokenType.STRINGDQ) {
            	if (column == null) throw new RuntimeException("column not found for parameter");
            	newVar = ConverterDelegate.convert(dbmd, column, token.value);
            }
            else if (token.type == OAQueryTokenType.NUMBER) {
            	if (column == null) throw new RuntimeException("column not found for parameter");
            	newVar = ConverterDelegate.convert(dbmd, column, token.value);
            }
            else if (token.type == OAQueryTokenType.FUNCTIONBEGIN) {
                bLastColumnWasInFunction = true;
                newVar = token.value;
            }
            else {
                newVar = token.value;
                if (token.type == OAQueryTokenType.OR || token.type == OAQueryTokenType.AND) {
                    if (token.type == OAQueryTokenType.OR) bUsingOR = true;
                    holdToken = token;
                    newVar = null;
                }
                else if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
                    for (int j=0; j<existCount; j++) where += ")";
                    existCount = 0;

                    if (holdToken != null) {
                        newVar = holdToken.value + " " + newVar;
                        vecLink.removeAllElements();  // so that it can not be shared with next path
                        holdToken = null;
                    }
                }
                else if (token.type == OAQueryTokenType.TRUE) {
                    if (dbmd.objectTrue != null) {
                        Object obj = dbmd.objectTrue;
                        if (dbmd.booleanKeyword && (obj instanceof String)) {
                            newVar = (String) obj;
                        }
                        else {
                            newVar = "'" + obj + "'";
                        }
                    }
                    else {
                        newVar = "1";
                    }
                }
                else if (token.type == OAQueryTokenType.FALSE) {
                    if (dbmd.objectFalse != null) {
                        Object obj = dbmd.objectFalse;
                        if (dbmd.booleanKeyword && (obj instanceof String)) {
                            newVar = (String) obj;
                        }
                        else {
                            newVar = "'" + obj + "'";
                        }
                    }
                    else {
                        newVar = "0";
                    }
                }
            }
            
            if (newVar != null && newVar.length() > 0) {
                if (where.length() > 0) where += " ";
                where += newVar;
            }
        }

        for (int j=0; j<existCount; j++) where += ")";

        return where;
    }


    /** @param vec list of Linkinfos to find property. */
    protected Column parseLink(Vector vec, String line)  {
        StringTokenizer st = new StringTokenizer(line,".",true);

        Linkinfo linkinfo = root;

        for (;;) {
        	String s = st.nextToken();
            if (s.equals(".")) {
                if (!st.hasMoreElements()) return null;  // bad path name
                continue;
                /*
                if (i > 0) continue;
                s = "";  // might not have a property name if a "none (1 w/o method)" to many
                */
            }
        	
            if (!st.hasMoreElements()) {
                Column c = linkinfo.findColumn(vec, s);
                if (c != null) return c;
            }

            linkinfo = linkinfo.findLink(vec, s);
            if (linkinfo == null) {
                throw new RuntimeException("QueryConverter.parseLink() cant find link for \""+s+"\"");
            }
            
            
            if (!st.hasMoreElements()) {
                vec.addElement(linkinfo);

                // ex: dept.emps.orders = null
// 20110415 removed this, so that it will use the pkey column                
//                if (linkinfo.bMany) return null;
                
                // ex: hubEmp.select("dept = null") ->  "emp.dept.id = NULL"
                Column[] pcols = linkinfo.table.getPrimaryKeyColumns();
                return pcols[0];
            }            
        }
    }


    protected void cleanTokens(Vector vec, Object[] params) {
        OAQueryToken prevToken = null;
        int questionCnt = 0;
        int x = vec.size();
        for (int i=0; i<x; i++) {
            OAQueryToken token = (OAQueryToken) vec.elementAt(i);
            switch (token.type) {
                case OAQueryTokenType.GT:
                    break;
                case OAQueryTokenType.GE:
                    break;
                case OAQueryTokenType.LT:
                    break;
                case OAQueryTokenType.LE:
                    break;
                case OAQueryTokenType.EQUAL:
                    token.value = "=";
                    break;
                case OAQueryTokenType.NOTEQUAL:
                    token.value = "<>";
                    break;
                case OAQueryTokenType.AND:
                    token.value = "AND";
                    break;
                case OAQueryTokenType.OR:
                    token.value = "OR";
                    break;
                case OAQueryTokenType.QUESTION:
                    if (params == null || questionCnt >= params.length || params[questionCnt++] == null) {
                        if (prevToken.type == OAQueryTokenType.EQUAL) prevToken.value = "IS";
                        else if (prevToken.type == OAQueryTokenType.NOTEQUAL) prevToken.value = "IS NOT";
                    }
                    break;
                case OAQueryTokenType.NULL:
                    token.value = "NULL";
                    if (prevToken.type == OAQueryTokenType.EQUAL) prevToken.value = "IS";
                    else if (prevToken.type == OAQueryTokenType.NOTEQUAL) prevToken.value = "IS NOT";
                    break;
                case OAQueryTokenType.STRINGSQ: 
                    for ( ;; ) {
                        // see if next token is also a single quote
                        if (i+1 == x) break;
                        OAQueryToken tok = (OAQueryToken) vec.elementAt(i+1);
                        if (tok.type != OAQueryTokenType.STRINGSQ) break;
                        token.value += "''" + tok.value;
                        vec.removeElementAt(i+1);
                        x--;
                    }
                    break;
                case OAQueryTokenType.STRINGDQ:
                    break;
                case OAQueryTokenType.STRINGESC:
                    token.value = "{" + token.value + "}";
                    break;
                case OAQueryTokenType.NUMBER:
                    break;
                case OAQueryTokenType.VARIABLE:
                    if (token.value.equalsIgnoreCase("IS")) {
                        if ((i+1) != x) {
                            OAQueryToken token2 = (OAQueryToken) vec.elementAt(i+1);
                            if (token2.type == OAQueryTokenType.NULL) token.type = OAQueryTokenType.EQUAL;
                            else {
                                if (token2.value.equalsIgnoreCase("NOT") && (i+2) != x) {
                                    token2 = (OAQueryToken) vec.elementAt(i+2);
                                    if (token2.type == OAQueryTokenType.NULL) token.type = OAQueryTokenType.NOTEQUAL;
                                }
                            }
                        }
                    }
                    else if (token.value.equalsIgnoreCase("TRUE")) {
                        token.type = OAQueryTokenType.TRUE;
                    }
                    else if (token.value.equalsIgnoreCase("FALSE")) {
                        token.type = OAQueryTokenType.FALSE;
                    }
                    break;
                case OAQueryTokenType.EOF:
                    break;
                default:
            }
            prevToken = token;
        }
    }


    
    protected String getJoins() {
        Table table = root.table;
        String s = LB + table.name.toUpperCase() + RB;
        s = getJoins(s, root, true, true);
        return s;
    }
    
    // strJoin = "(" + strJoin + " {oj LEFT OUTER JOIN " + toTable.name + " ON ";
    // {oj dept A left outer join emp B on A.deptNo = B.deptNo}
    /** uses root Linkinfo to build JOIN clause */
    private String getJoins(String strJoin, Linkinfo li, boolean bSuper, boolean bSub) {
        Table table = li.table;
        Column[] cols = table.getColumns();
        
        if (bSuper && li.superLink != null && !li.bMany) {
            if (li.superLink.bUsed) {
                
                // li might not be a used link, but a superLink above it can be.  If superLink is used, then need to 
                //   join it with first subLink under it that is used
                // find used sub link closest to li.superLink
                Linkinfo lix = li;
                for (Linkinfo l = li.base; l != null && l != li.superLink; l=l.superLink) {
                    if (l.bUsed) lix = l;
                }
                table = lix.table;
                cols = table.getColumns();
                
                String tname = LB + li.superLink.table.name.toUpperCase()  + RB;
                if (li.superLink.number > 0) {
                    tname += " " + LB + li.superLink.table.name.toUpperCase() + li.superLink.number + RB;
                }

                if (dbmd.databaseType == dbmd.ORACLE) {
                    strJoin += "," + tname;
                }
                else {
                    String s = "INNER JOIN"; 
                    if (bUseLeftJoin || lix.bUseLeftJoin) s = "LEFT OUTER JOIN"; // 2006/11/08

                    strJoin = "(" + dbmd.outerjoinStart + strJoin + " "+s+" " + tname + " ON ";
                }
                
                for (int i=0, j=0; i<cols.length; i++) {
                    if (!cols[i].primaryKey) continue;

                    if (dbmd.databaseType == dbmd.ORACLE && extraWhere.length() > 0) {
                        extraWhere += " AND ";
                    }
                    if (j > 0 && dbmd.databaseType != dbmd.ORACLE) {
                        strJoin += " AND ";
                    }
                    j++;
                    tname = table.name.toUpperCase() + ((lix.number>0)?lix.number+"":"");

                    if (dbmd.databaseType == dbmd.ORACLE) {
                        extraWhere += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB;
                        extraWhere += " = ";
                        tname = li.superLink.table.name.toUpperCase() + ((li.superLink.number>0)?li.superLink.number+"":"");
                        extraWhere += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB +"(+)";
                    }
                    else {
                        strJoin += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB;
                        strJoin += " = ";
                        tname = li.superLink.table.name.toUpperCase() + ((li.superLink.number>0)?li.superLink.number+"":"");
                        strJoin += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB;
                    }      
                }
                if (dbmd.databaseType != dbmd.ORACLE) {
                    strJoin += dbmd.outerjoinEnd+")";
                }     
            }
            // even if this "super" is not used, a super above it might be
            strJoin = getJoins(strJoin, li.superLink, true, false); 
        }
            
        if (bSub && li.subLinks != null) {
            for (int i=0; i<li.subLinks.length; i++) {
                if (!li.subLinks[i].bUsed) continue;

                String tname = LB + li.subLinks[i].table.name.toUpperCase()  + RB;
                if (li.subLinks[i].number > 0) {
                    tname += " " + LB + li.subLinks[i].table.name.toUpperCase() + li.subLinks[i].number + RB;
                }

                if (dbmd.databaseType == dbmd.ORACLE) {
                    strJoin += "," + tname;
                }
                else {
                    String s = "LEFT OUTER JOIN"; // always OUTER join subclasses, in case subclass does not exist
                    strJoin = "(" + dbmd.outerjoinStart + strJoin + " " + s + " " + tname + " ON ";
                }                
                for (int ii=0, j=0; ii<cols.length; ii++) {
                    if (!cols[ii].primaryKey) continue;

                    if (dbmd.databaseType == dbmd.ORACLE && extraWhere.length() > 0) {
                        extraWhere += " AND ";
                    }
                    if (j > 0 && dbmd.databaseType != dbmd.ORACLE) {
                        strJoin += " AND ";
                    }
                    j++;
                    tname = table.name.toUpperCase() + ((li.number>0)?li.number+"":"");

                    if (dbmd.databaseType == dbmd.ORACLE) {
                        extraWhere += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB;
                        extraWhere += " = ";
                        tname = li.superLink.table.name.toUpperCase() + ((li.number>0)?li.number+"":"");
                        extraWhere += LB+ tname + RB+"."+LB+ cols[i].columnName.toUpperCase() + RB +"(+)";
                    }
                    else {
                        strJoin += LB + tname + RB+"."+LB + cols[ii].columnName.toUpperCase() + RB;
                        strJoin += " = ";
                        tname = li.subLinks[i].table.name.toUpperCase() + ((li.number>0)?li.number+"":"");
                        strJoin += LB + tname + RB+"."+LB + cols[ii].columnName.toUpperCase() + RB;
                    }
                }

                if (dbmd.databaseType != dbmd.ORACLE) {
                    strJoin += dbmd.outerjoinEnd+")";
                }
                strJoin = getJoins(strJoin, li.subLinks[i], false, true);
            }
        }
        // links ...
        for (int i=0; li.links != null && i<li.links.length; i++) {
            if (!li.links[i].bUsed) continue;
            strJoin = createLinkJoins(li.links[i].bUseLeftJoin, strJoin, table.getLinks()[i], table, table.getLinks()[i].toTable, li.number, li.links[i].number);
            strJoin = getJoins(strJoin, li.links[i], true, true);
        }

        return strJoin;
        
    }

    protected String getJoins(Vector vec, Linkinfo liStart, Linkinfo liEnd) {
        Linkinfo li = liStart;
        
        if (li.base != null) li = li.base;  // use base class in relationship from parent

        String strJoin = LB + li.table.name.toUpperCase() + RB + " " + LB + li.table.name.toUpperCase() + (liStart.number>0?liStart.number+"":"") + RB;

        int x = vec.size();
        boolean b = false;
        for (int i=0; i<x ;i++) {
            li = (Linkinfo) vec.elementAt(i);
            if (!b && li != liStart) continue;
            b = true;

            Linkinfo lix = (li.base == null) ? li : li.base;  // base link is "real" link to parent
            if (!li.bMany) {
                // join with parent link
                strJoin = createLinkJoins(li.bUseLeftJoin, strJoin, lix.linkFromParent, lix.parent.table, lix.table, lix.parent.number, lix.number);
            }
             
            if (li.base != null) {
                // create join from base to super
                String tname = LB + li.table.name.toUpperCase() + RB;
                if (li.number > 0) tname += " " + LB + li.table.name.toUpperCase()+li.number + RB;

                if (dbmd.databaseType == dbmd.ORACLE) {
                    strJoin += ", " + tname;
                }
                else {
                    String s = "INNER JOIN";
                    if (bUseLeftJoin || li.bUseLeftJoin) s = "LEFT OUTER JOIN";
                    strJoin = "(" + dbmd.outerjoinStart + strJoin + " "+s+" " + tname + " ON ";
                }
                    
                Column[] cols = li.table.getColumns();
                for (int ii=0, j=0; ii<cols.length; ii++) {
                    if (!cols[ii].primaryKey) continue;
                    Column column = cols[ii];

                    if (dbmd.databaseType == dbmd.ORACLE && extraWhere.length() > 0) {
                        extraWhere += " AND ";
                    }
                    if (j > 0 && dbmd.databaseType != dbmd.ORACLE) {
                        strJoin += " AND ";
                    }
                    j++;

                    tname = li.table.name.toUpperCase() + ((li.number>0)?li.number+"":"");
                    if (dbmd.databaseType == dbmd.ORACLE) {
                        extraWhere += LB+ tname + RB+"."+LB+ column.columnName.toUpperCase() + RB;
                        extraWhere += " = ";
                        tname = li.base.table.name.toUpperCase() + ((li.base.number>0)?li.base.number+"":"");
                        extraWhere += LB+ tname + RB+"."+LB+ column.columnName.toUpperCase() + RB +"(+)";
                    }
                    else {
                        strJoin += LB+ tname + RB+"."+LB+ column.columnName.toUpperCase() + RB;
                        strJoin += " = ";
                        tname = li.base.table.name.toUpperCase() + ((li.base.number>0)?li.base.number+"":"");
                        strJoin += LB+ tname + RB+"."+LB+ column.columnName.toUpperCase() + RB;
                    }      
                }
                if (dbmd.databaseType != dbmd.ORACLE) {
                    strJoin += dbmd.outerjoinEnd+")";
                }     
            }
            if (li == liEnd) break;
        }
        
        return strJoin;
    }


    private String createLinkJoins(boolean bUseLeftJoin, String strJoin, Link link, Table fromTable, Table toTable, int fromTableNumber, int toTableNumber) {
        Column[] fkeys = link.fkeys;
        Column[] toFkeys = fromTable.getLinkToColumns(link, toTable);

        if (fkeys.length != toFkeys.length) throw new RuntimeException("different number of links between tables");

        String tname = LB + toTable.name.toUpperCase() + RB;
        if (toTableNumber > 0) tname += " " + LB + toTable.name.toUpperCase() + toTableNumber + RB;
        
        if (dbmd.databaseType == dbmd.ORACLE) {
            strJoin += "," + tname;
        }
        else {
            String s = "INNER JOIN";
            if (this.bUseLeftJoin || bUseLeftJoin) s = "LEFT OUTER JOIN";
            strJoin = "(" + dbmd.outerjoinStart + strJoin + " " + s + " " + tname + " ON ";
        }
        for (int i=0, j=0; i<fkeys.length; i++) {
            if (dbmd.databaseType == dbmd.ORACLE && extraWhere.length() > 0) {
                extraWhere += " AND ";
            }
            if (j > 0 && dbmd.databaseType != dbmd.ORACLE) {
                strJoin += " AND ";
            }
            j++;

            tname = fromTable.name.toUpperCase() + ((fromTableNumber>0)?fromTableNumber+"":"");

            if (dbmd.databaseType == dbmd.ORACLE) {
                extraWhere += LB+ tname + RB+"."+LB+ fkeys[i].columnName.toUpperCase() + RB;
                extraWhere += " = ";
                tname = toTable.name.toUpperCase() + ((toTableNumber>0)?toTableNumber+"":"");
                extraWhere += LB+tname + RB+"."+LB + toFkeys[i].columnName.toUpperCase() + RB +"(+)";
            }
            else {
                strJoin += LB+ tname + RB+"."+LB+ fkeys[i].columnName.toUpperCase() + RB;
                strJoin += " = ";
                tname = toTable.name.toUpperCase() + ((toTableNumber>0)?toTableNumber+"":"");
                strJoin += LB+tname + RB+"."+LB + toFkeys[i].columnName.toUpperCase() + RB;
            }
        }

        if (dbmd.databaseType != dbmd.ORACLE) {
            strJoin += dbmd.outerjoinEnd + ")";
        }
        return strJoin;        
    }
    

    
    /** 
        Linkinfo represent a single link from a parent Linkinfo.  Ex:  Emp -> Dept
    **/
    class Linkinfo {
        Table table;
        Class clazz;
        int number;          // unique number
        
        Link linkFromParent; 
        Link linkToParent;
        boolean bLink;     // link table
        
        Linkinfo base;     // base link, used for super/sub links
        Linkinfo parent;
        Linkinfo superLink;
        Linkinfo[] subLinks;
        Linkinfo[] links;    // same order as Table.Links

        boolean bMany;
        boolean bUsed;
        boolean bUseLeftJoin;

        /** used internally by this class for setting nodes from root. */
        private Linkinfo(Class c, int num, Linkinfo parent) {
            this.clazz = c;
            number = num;
            this.parent = parent;
            if (c != null) table = database.getTable(c);
        }

        /** root linkinfo */
        public Linkinfo(Class c) {
            this(c,0, null);
            counter = 0;
            // load all super and sub classes for root
            loadSuper();
            loadSub();
        }

        /** @param vec list of Linkinfos to find property. */
        public Column findColumn(Vector vec, String propertyName) {
            // first look in columns of this class and its superclass(es)
            Column[] columns = table.getColumns();
            // see is column is a match
            for (int i=0; i < columns.length; i++) {
                if ( columns[i].propertyName.equalsIgnoreCase(propertyName) || propertyName.equalsIgnoreCase(columns[i].columnName) ) {
                    vec.addElement(this);
                    return columns[i];
                }
            }

            // check for property in superclass
            if ((loadSuper() != null)) {
                Column c = superLink.findColumn(vec, propertyName);
                if (c != null) return c;
            }
            return null;  // property not found
        }

        /** @param vec list of Linkinfos to find property. */
        public Linkinfo findLink(Vector vec, String propertyName) {
            loadLinks();
            Link[] tabLinks = table.getLinks();

            // see if link is a match
            for (int i=0; i < links.length; i++) {
                if ( tabLinks[i].propertyName.equalsIgnoreCase(propertyName) ) {
                    vec.addElement(this);
                    if (links[i].bLink) return links[i].findLink(vec, propertyName);
                    return links[i];
                }
            }

            // check for property in superclass
            loadSuper();
            if (superLink != null) {
                Linkinfo li = superLink.findLink(vec, propertyName);
                if (li != null) return li;
            }
            
            return null; // not found
        }
        
        private void loadSub() {
            if (subLinks == null) {
                Class[] subclasses = table.subclasses;
                this.subLinks = new Linkinfo[subclasses == null?0:subclasses.length];
                for (int i=0; subclasses != null && i < subclasses.length; i++) {
                    subLinks[i] = new Linkinfo(subclasses[i], number, this.parent);
                    subLinks[i].linkFromParent = this.linkFromParent;
                    subLinks[i].linkToParent = this.linkToParent;
                    subLinks[i].bMany = this.bMany;
                    subLinks[i].base = (this.base == null) ? this : this.base;
                    if (number == 0) {
                        // load all for root object
                        subLinks[i].loadSub();
                    }
                }
            }  
        }
        private Linkinfo loadSuper() {
            if (superLink == null) {
                Class c = clazz.getSuperclass();
                if (c == null || c.equals(OAObject.class)) return null;
                superLink = new Linkinfo(c, number, this.parent);
                superLink.linkFromParent = this.linkFromParent;
                superLink.linkToParent = this.linkToParent;
                superLink.bMany = this.bMany;
                superLink.base = (this.base == null) ? this : this.base;
                if (number == 0) {
                    // load all for root object
                    superLink.loadSuper();
                }
            }
            return superLink;
        }

        private void loadLinks() {
            if (links != null || table == null) return;
            Link[] tabLinks = table.getLinks();
            links = new Linkinfo[tabLinks.length];
            for (int i=0; i < links.length; i++) {
                counter++;
                Table toTable = tabLinks[i].toTable;

                links[i] = new Linkinfo(tabLinks[i].toTable.clazz, counter, this);
                if (toTable.bLink) {
                    links[i].table = toTable;
                    links[i].bLink = true;
                    links[i].bMany = true;
                }
                else {
                    // MANY ?
                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(table.clazz);
                    OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, tabLinks[i].propertyName);
                    if (li != null && li.getType() == li.MANY) links[i].bMany = true;
                }
                links[i].linkFromParent = tabLinks[i];
                links[i].linkToParent = tabLinks[i].getReverseLink();
            }
        }

    }

}

/** Testing from VP:

 //        OASelect sel = new OASelect(Exam.class, "examItems.id = null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE ( EXAMITEM9.ID IS NULL AND EXAMITEM9.ID IS NOT NULL )         
        
//        OASelect sel = new OASelect(Exam.class, "examItems.id != null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE EXAMITEM9.ID IS NOT NULL        
  
//        OASelect sel = new OASelect(Exam.class, "examItems != null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE EXAMITEM9.ID IS NOT NULL        
        
//      OASelect sel = new OASelect(Exam.class, "examItems = null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE EXAMITEM9.ID IS NULL        
        
//        OASelect sel = new OASelect(Exam.class, "examItems.item.id != null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE EXAMITEM9.ITEMID IS NOT NULL        
        
        //OASelect sel = new OASelect(Exam.class, "examItems.item != null", "");
// FROM (EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) WHERE EXAMITEM9.ITEMID IS NOT NULL        

//        OASelect sel = new OASelect(Exam.class, "examItems.item.name != null", "");
// FROM ((EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) LEFT OUTER JOIN ITEM ITEM14 ON EXAMITEM9.ITEMID = ITEM14.ID) WHERE ITEM14.NAME IS NOT NULL
   
        OASelect sel = new OASelect(Exam.class, "examItems.item.name == null", "");
// FROM ((EXAM LEFT OUTER JOIN EXAMITEM EXAMITEM9 ON EXAM.ID = EXAMITEM9.EXAMID) LEFT OUTER JOIN ITEM ITEM14 ON EXAMITEM9.ITEMID = ITEM14.ID) WHERE ( ITEM14.NAME IS NULL AND EXAMITEM9.ITEMID IS NOT NULL )        
        
        sel.select();

 

 */

