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
package com.viaoa.object;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.annotation.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.hub.Hub;
import com.viaoa.util.*;


/**
 * Delegate used work with OAObject annotations.
 * @author vvia
 *
 */
public class OAAnnotationDelegate {
    private static Logger LOG = Logger.getLogger(OAAnnotationDelegate.class.getName());
    /**
     * Load/update ObjectInfo using annotations
     */
    public static void update(OAObjectInfo oi, Class clazz) {
        HashSet<String> hs = new HashSet<String>();
        for ( ; clazz != null; ) {
            if (OAObject.class.equals(clazz)) break;
            _update(oi, clazz, hs);
            clazz = clazz.getSuperclass();
        }
    }    
    
    private static void _update(OAObjectInfo oi, Class clazz, HashSet<String> hs) {
        String s;
        
        if (!hs.contains("OAClass")) {
            OAClass oaclass = (OAClass) clazz.getAnnotation(OAClass.class);
            if (oaclass != null) {
                hs.add("OAClass");
                oi.setUseDataSource(oaclass.useDataSource());
                oi.setLocalOnly(oaclass.localOnly());
                oi.setAddToCache(oaclass.addToCache());
                oi.setInitializeNewObjects(oaclass.initialize());
                oi.setDisplayName(oaclass.displayName());

                // 20140118 rootTreePropertyPaths
                String[] pps = oaclass.rootTreePropertyPaths();
                oi.setRootTreePropertyPaths(pps);
            }
        }
        // prop ids
        Method[] methods = clazz.getDeclaredMethods();
        String[] ss = oi.getIdProperties();
        for (Method m : methods) {
            OAId oaid = m.getAnnotation(OAId.class);
            if (oaid == null) continue;
            s = getPropertyName(m.getName());
            if (hs.contains("OAId." + s)) continue;
            hs.add("OAId."+s);
            
            int pos = oaid.pos();

            if (ss == null) {
                ss = new String[pos+1];
            }
            else {
                if (pos >= ss.length) {
                    String[] ss2 = new String[pos+1];
                    System.arraycopy(ss, 0, ss2, 0, pos);
                    ss = ss2;
                }
                if (ss[pos] != null) {
                    if (!ss[pos].equalsIgnoreCase(s)) {
                        throw new RuntimeException("annotation OAId - duplicate pos for property id");
                    }
                }
            }
            ss[pos] = s;
        }
        
        for (String sx : ss) {
            if (sx == null) {
                throw new RuntimeException("annotation OAId - missing pos for property id(s)");
            }
        }
        oi.setPropertyIds(ss);
        

        // properties
        for (Method m : methods) {
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            if (oaprop == null) continue;
            String name = getPropertyName(m.getName());
            if (hs.contains("prop." + name)) continue;
            hs.add("prop."+name);
            
            OAPropertyInfo pi = oi.getPropertyInfo(name);            
            if (pi == null) {
                pi = new OAPropertyInfo();
                pi.setName(name);
                oi.addPropertyInfo(pi);
            }
            pi.setDisplayName(pi.getDisplayName());
            pi.setColumnName(pi.getColumnName());
            pi.setMaxLength(oaprop.maxLength());
            pi.setDisplayLength(oaprop.displayLength());
            pi.setColumnLength(oaprop.columnLength());
            pi.setRequired(oaprop.required());
            pi.setDecimalPlaces(oaprop.decimalPlaces());
            pi.setId(m.getAnnotation(OAId.class) != null);
            pi.setClassType(m.getReturnType());
            
            boolean b = oaprop.isBlob();
            if (b) {
                b = false;
                Class c = m.getReturnType();
                if (c.isArray()) {
                    c = c.getComponentType();
                    if (c.equals(byte.class)) b = true;
                }
            }
            pi.setBlob(b);
            pi.setNameValue(oaprop.isNameValue());
            pi.setUnicode(oaprop.isUnicode());
        }
        
      
        // Verify calcProperties
        ArrayList<OACalcInfo> alCalc = oi.getCalcInfos();
        for (Method m : methods) {
            OACalculatedProperty annotation = (OACalculatedProperty) m.getAnnotation(OACalculatedProperty.class);
            if (annotation == null) continue;

            boolean bHub = false;
            Class[] cs = m.getParameterTypes();
            if (cs != null && cs.length == 1 && Hub.class.equals(cs[0])) {
                if (Modifier.isStatic(m.getModifiers())) bHub = true;
            }
            
            String name = getPropertyName(m.getName(), false);
            if (hs.contains("calc." + name)) continue;
            hs.add("calc."+name);
            
            OACalcInfo ci = OAObjectInfoDelegate.getOACalcInfo(oi, name);
            if (ci == null) {
                ci = new OACalcInfo(name, annotation.properties(), bHub);
                oi.addCalcInfo(ci);
            }
            else {
                ci.setPropeties(annotation.properties());
            }
        }

        // linkInfos
        List<OALinkInfo> alLinkInfo = oi.getLinkInfos();
        // Ones
        for (Method m : methods) {
            OAOne annotation = (OAOne) m.getAnnotation(OAOne.class);
            if (annotation == null) continue;
            Class c = m.getReturnType();
                        
            String name = getPropertyName(m.getName(), false);
            if (hs.contains("link." + name)) continue;
            hs.add("link."+name);
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            if (li == null) {
                li = new OALinkInfo(name, m.getReturnType(), OALinkInfo.ONE);
                oi.addLinkInfo(li);
            }
          
            li.setCascadeSave(annotation.cascadeSave());
            li.setCascadeDelete(annotation.cascadeDelete());
            li.setReverseName(annotation.reverseName());
            li.setOwner(annotation.owner());
            li.setAutoCreateNew(annotation.autoCreateNew());
            li.setMustBeEmptyForDelete(annotation.mustBeEmptyForDelete());
            li.setCalculated(annotation.isCalculated());
            //li.setRecursive(annotation.recursive());
        }
        // Manys
        for (Method m : methods) {
            Class c = m.getReturnType();
            if (!Hub.class.isAssignableFrom(c)) continue; // 20111027 added
            if ((m.getModifiers() & Modifier.STATIC) > 0) continue;
                        
            String name = getPropertyName(m.getName(), false);
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            OAMany annotation = (OAMany) m.getAnnotation(OAMany.class);
            Class cx = OAAnnotationDelegate.getHubObjectClass(annotation, m);

            if (li == null) {
                li = new OALinkInfo(name, cx, OALinkInfo.MANY);
                oi.addLinkInfo(li);
            }
            
            if (cx != null) li.setToClass(cx);
            if (annotation == null) continue;

            if (hs.contains("link." + name)) continue;
            hs.add("link."+name);
            
            li.setCascadeSave(annotation.cascadeSave());
            li.setCascadeDelete(annotation.cascadeDelete());
            li.setReverseName(annotation.reverseName());
            li.setOwner(annotation.owner());
            li.setRecursive(annotation.recursive());
            li.setCacheSize(annotation.cacheSize());

            s = annotation.matchHub();
            if (s != null && s.length() == 0) s = null;
            li.setMatchHub(s);

            s = annotation.matchProperty();
            if (s != null && s.length() == 0) s = null;
            li.setMatchProperty(s);
            
            s = annotation.uniqueProperty();
            if (s != null && s.length() == 0) s = null;
            li.setUniqueProperty(s);

            s = annotation.sortProperty();
            if (s != null && s.length() == 0) s = null;
            li.setSortProperty(s);
            
            li.setMustBeEmptyForDelete(annotation.mustBeEmptyForDelete());
            li.setCalculated(annotation.isCalculated());
            li.setServerSideCalc(annotation.isServerSideCalc());
            li.setPrivateMethod(!annotation.createMethod());
            li.setCacheSize(annotation.cacheSize());
            Class[] cs = annotation.triggerClasses();
            if (cs != null) {
                li.setTriggerClasses(cs);
            }
        }
    }

    // 20111027
    /**
     * Find the OAObject class that use contained by the Hub. 
     * @see OAObjectReflectDelegate#getHubObjectClass(Method)
     */
    public static Class getHubObjectClass(OAMany annotation, Method method) {
        Class cx = OAObjectReflectDelegate.getHubObjectClass(method);
        if (cx == null && annotation != null) {
            // cx = annotation.toClass();
        }
        return cx;
    }
    
    public static void update(Database database, Class[] classes) throws Exception {
        if (classes == null) return;
        for (Class c : classes) {
            _createColumns(database, c);
        }
        for (Class c : classes) {
            _updateTable(database, c);
        }
    }
    
    
    /**
     * Load/update Database using annotations
     */
    private static void _createColumns(Database database, Class clazz) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private. qqqqqq does not get superclass methods

        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        if (dbTable == null) throw new Exception("Annotation for Table not defined for this class");
        
        
        Table table = database.getTable(clazz);
        if (table == null) {
            String s = dbTable.name();
            if (s.length() == 0) s = clazz.getSimpleName();
            table = new Table(s, clazz);
            database.addTable(table);
        }

        // 1: create pkey and regular columns
        for (Method m : methods) {
            OAColumn dbcol = (OAColumn) m.getAnnotation(OAColumn.class);
            if (dbcol == null) continue;

            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            OAId oaid = (OAId) m.getAnnotation(OAId.class);
            
            String name = name = getPropertyName(m.getName());
            
            String colName = dbcol.name();  // will be "", if the property name should be used.
            if (colName == null || colName.length() == 0) {
                colName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            
            Column column = new Column(colName, name, dbcol.sqlType(), dbcol.maxLength());
            if (oaprop != null) column.decimalPlaces = oaprop.decimalPlaces();
            if (oaid != null) {
                column.primaryKey = true;
                column.guid = oaid.guid();
                column.assignNextNumber = oaid.autoAssign();
                // c.assignedByDatabase = qqq
            }
            if (oaprop != null) column.unicode = oaprop.isUnicode();
            table.addColumn(column);
        }
    }

    
    private static void _updateTable(Database database, Class clazz) throws Exception {

        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private. qqqqqq does not get superclass methods

        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        if (dbTable == null) throw new Exception("Annotation for Table not defined for this class"); 
        
        Table table = database.getTable(clazz);
        if (table == null) throw new Exception("Table for class="+clazz+" was not found");
        // 2: create fkey columns and links
        for (Method m : methods) {
            OAColumn dbcol = (OAColumn) m.getAnnotation(OAColumn.class);
            OAFkey dbfk = (OAFkey) m.getAnnotation(OAFkey.class);
            OAOne oaone = (OAOne) m.getAnnotation(OAOne.class);
            OAMany oamany = (OAMany) m.getAnnotation(OAMany.class);
            OALinkTable oalt = (OALinkTable) m.getAnnotation(OALinkTable.class);
            
            if (dbfk != null) {
                if (dbcol != null) throw new Exception("fkey column should not have a column annotation defined, method is "+m.getName());
                if (oaone == null) throw new Exception("method with fkey does not have a One annotation defined, method is "+m.getName());
                if (oamany != null) throw new Exception("method with fkey should not have a Many annotation defined, method is "+m.getName());
                
                Class returnClass = m.getReturnType();
                if (returnClass == null) throw new Exception("method with fkey does not have a return class type, method is "+m.getName());
                OATable toTable = (OATable) returnClass.getAnnotation(OATable.class);
                if (toTable == null) throw new Exception("class for fkey doses not have a Table annotation defined, method is "+m.getName());
                
                Table fkTable = database.getTable(returnClass);
                if (fkTable == null) {
                    fkTable = new Table(toTable.name(), returnClass);
                    database.addTable(fkTable);
                }
                
                // tables[COLORCODE].addLink("orders", tables[ORDER], "colorCode", new int[] {0});
                //   tables[WORKER].addLink("orderProductionAreas", tables[ORDERPRODUCTIONAREA], "worker", new int[] {0});

                String[] fkcols = dbfk.columns();
                int[] poss = new int[0];
                for (String sfk : fkcols) {
                    poss = OAArray.add(poss, table.getColumns().length);
                    Column c = new Column(sfk, true);
                    table.addColumn(c);
                }
                table.addLink( getPropertyName(m.getName()), fkTable, oaone.reverseName(), poss);
            }
            else if (oalt != null) {
                Table linkTable = database.getTable(oalt.name());
                if (linkTable == null) {
                    linkTable = new Table(oalt.name(), true);
                    database.addTable(linkTable);
                }
                // create columns for link table
                // create link for table to linkTable
                // create link for linktable to table
                int[] poss = new int[0];  // pos for pk columns in table
                int[] poss2 = new int[0];  // pos for fkey columsn in linkTable
                String[] indexColumns = new String[0];
                Column[] cols = table.getColumns();
                int j = 0;
                for (int i=0; i<cols.length;i++) {
                    if (!cols[i].primaryKey) continue;

                    poss = OAArray.add(poss, i);
                    poss2 = OAArray.add(poss2, linkTable.getColumns().length);
                    
                    if (j > oalt.columns().length) {
                        throw new Exception("mismatch between linktable fkey columns and pkey columns, more pkeys. method is "+m.getName());
                    }

                    Column c = new Column(oalt.columns()[j], "", cols[i].getSqlType(), cols[i].maxLength);
                    c.primaryKey = false; // no pkeys in linkTable, only indexes
                    linkTable.addColumn(c);
                    
                    indexColumns = (String[]) OAArray.add(String.class, indexColumns, oalt.columns()[j]);
                    j++;
                }
                if (j < oalt.columns().length) throw new Exception("mismatch between fkey columns and pkey columns, more fkeys. method is "+m.getName());
                
                if (oamany != null) table.addLink(getPropertyName(m.getName()), linkTable, oamany.reverseName(), poss);
                else table.addLink(getPropertyName(m.getName()), linkTable, oaone.reverseName(), poss);
                
                if (oamany != null) linkTable.addLink(oamany.reverseName(), table, getPropertyName(m.getName()), poss2);
                else linkTable.addLink(oaone.reverseName(), table, getPropertyName(m.getName()), poss2);
                
                String s = oalt.indexName();
                if (s != null) linkTable.addIndex(new Index(s, indexColumns));
            }
            else if (oaone != null) {
                if (oamany != null) throw new Exception("method with OAOne annotation should not have a OAMany annotation defined, method is "+m.getName());

                // link using pkey columns                
                int[] poss = new int[0];
                Column[] cols = table.getColumns();
                for (int i=0; i<cols.length;i++) {
                    if (cols[i].primaryKey) {
                        poss = OAArray.add(poss, i);
                        break;
                    }
                }
                table.addLink(getPropertyName(m.getName()), database.getTable(m.getReturnType()), oaone.reverseName(), poss);
            }
            else if (oamany != null) {
                Column[] cols = table.getColumns();
                int[] poss = new int[0];  // pos for pk columns in table
                for (int i=0; i<cols.length;i++) {
                    if (!cols[i].primaryKey) continue;
                    poss = OAArray.add(poss, i);
                }
                Class c = OAAnnotationDelegate.getHubObjectClass(oamany, m);
                table.addLink(getPropertyName(m.getName()), database.getTable(c), oamany.reverseName(), poss);
            }
        }
        
        // Indexes
        OAIndex[] indexes = dbTable.indexes();
        for (OAIndex ind : indexes) {
            String[] ss = new String[0];
            OAIndexColumn[] dbics = ind.columns();
            for (OAIndexColumn dbic : dbics) {
                ss = (String[]) OAArray.add(String.class, ss, dbic.name());
            }
            table.addIndex( new Index(ind.name(), ss) );
        }
    }

    
    public static void main(String[] args) throws Exception {
/**        
        OAAnnotationDelegate del = new OAAnnotationDelegate();
        
        DataSource ds = new DataSource("server", "database", "user", "pw");
        Database database = ((OADataSourceJOAC)ds.getOADataSource()).getDatabase();
        OAMetaData dbmd = ((OADataSourceJOAC)ds.getOADataSource()).getOAMetaData();

        String[] fnames = OAReflect.getClasses("com.viaoa.scheduler.oa");

        for (String fn : fnames) {
            System.out.println("oi&ds ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            
            if (c.getAnnotation(OATable.class) == null) continue;
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            del.verify(oi);
            del.verify(c, database);
        }

        
        // Create database
        database = new Database();

        Table table = new Table("NextNumber",com.viaoa.ds.autonumber.NextNumber.class); // ** Used by all OADataSource Database
        // NextNumber COLUMNS
        Column[] columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER);
        table.setColumns(columns);
        database.addTable(table);
        
        
        for (String fn : fnames) {
            System.out.println("create columns ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.createColumns(database, c);
        }
        for (String fn : fnames) {
            System.out.println("update table ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.updateTable(database, c); // fkeys, links, linktables
        }
        
        
        // Verify
        for (String fn : fnames) {
            System.out.println("verify OA ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.verify(c, database);
        }
        
        System.out.println("verify database Links ==>");
        del.verifyLinks(database);
*/

        /* must have database access to run this
        System.out.println("datasource VerifyDelegate.verify database ==>");
        OADataSourceJOAC dsx = new OADataSourceJDBC(database, dbmd);
        VerifyDelegate.verify(dsx);
        */
        
        System.out.println("done");
    }

    static String getPropertyName(String s) {
        return getPropertyName(s, true);
    }
    static String getPropertyName(String s, boolean bToLower) {
        boolean b = true;
        if (s.startsWith("get")) {
            s = s.substring(3);
        }
        else if (s.startsWith("is")) {
            s = s.substring(2);
        }
        else if (s.startsWith("has")) {
            s = s.substring(3);
        }
        else if (s.startsWith("set")) {
            s = s.substring(3);
        }
        else {
            b = false;
        }
        if (bToLower && b && s.length() > 1) {
            s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
        return s;
    }

    
}
