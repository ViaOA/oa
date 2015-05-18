/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.object;


import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.viaoa.annotation.*;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.hub.Hub;
import com.viaoa.util.*;

/**
 * Verifies OA annotations.
 * @author vvia
 *
 */
public class OAAnnotationVerifier {

    private static Logger LOG = Logger.getLogger(OAAnnotationVerifier.class.getName());
    
    /**
     * Verify class annotations with an ObjectInfo.
     * Call this to verify that an ObjectInfo that was created using annotations is correct.
     * 
     */
    public boolean verify(OAObjectInfo oi) throws Exception {
        Class clazz = oi.getForClass();

        String s;
        
        OAClass oaclass = (OAClass) clazz.getAnnotation(OAClass.class);
        if (oaclass == null) {
            p("no oaclass annotation");
            return false;
        }
        
        boolean bResult = true;
        if (oi.getUseDataSource() != oaclass.useDataSource()) {
            p("useDatasource");
            bResult = false;
        }
        if (oi.getLocalOnly() != oaclass.localOnly()) {
            p("LocalOnly");
            bResult = false;
        }
        if (oi.getAddToCache() != oaclass.addToCache()) {
            p("addToCache");
            bResult = false;
        }
        if (oi.getInitializeNewObjects() != oaclass.initialize()) {
            p("initializeNewObjects");
            bResult = false;
        }
        
        // Verify IDs
        String[] ids = oi.getIdProperties();
        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private. qqqqqq does not get superclass methods
        
        boolean[] bs = new boolean[ids.length];
        for (Method m : methods) {
            OAId oaid = (OAId) m.getAnnotation(OAId.class);
            if (oaid == null) continue;
            s = OAAnnotationDelegate.getPropertyName(m.getName());
            
            int x = OAArray.indexOf(ids, s, true);
            if (x >= 0) {
                bs[x] = true;
                if (oaid.pos() != x) {
                    p("id prop wrong pos");
                    bResult = false;
                }
            }
            else {
                p("id prop mismatch");
                bResult = false;
            }
        }
        for (boolean b : bs) {
            if (!b) {
                p("id prop mismatch2");
                bResult = false;
            }
        }

        // Verify properties
        ArrayList<OAPropertyInfo> al = oi.getPropertyInfos();
        bs = new boolean[al.size()];
        for (Method m : methods) {
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            if (oaprop == null) continue;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            
            int x = 0;
            for (OAPropertyInfo pi : al) {
                s = pi.getName();
                if (name.equalsIgnoreCase(s)) {
                    bs[x] = true;
                    
                    if (m.getReturnType().equals(String.class) && pi.getMaxLength() != oaprop.maxLength()) {
                        OADataSource ds = OADataSource.getDataSource(clazz);
                        if (ds != null) {
                            x = ds.getMaxLength(clazz, s);
                            if (x != oaprop.maxLength()) {
                                if (x > 0) {
                                    p(name+" maxLength, "+x+", "+oaprop.maxLength());
                                    bResult = false;
                                }
                            }
                        }
                    }
                    if (pi.getRequired() != oaprop.required()) {
                        p("required");
                        bResult = false;
                    }
                    if (pi.getId()) {
                        if (m.getAnnotation(OAId.class) == null) {
                            p("id");
                            bResult = false;
                        }
                    }
                    x = -1;
                    break;
                }
                x++;
            }
            if (x != -1) {
                p("prop mismatch3 "+name); 
                bResult = false;
            }
        }
        for (boolean b : bs) {
            if (!b) {
                p("prop mismatch4");
                bResult = false;
            }
        }
        
        
        // Verify calcProperties
        ArrayList<OACalcInfo> alCalc = oi.getCalcInfos();
        bs = new boolean[alCalc.size()];
        for (Method m : methods) {
            OACalculatedProperty annotation = (OACalculatedProperty) m.getAnnotation(OACalculatedProperty.class);
            if (annotation == null) continue;

            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            
            OACalcInfo ci = OAObjectInfoDelegate.getOACalcInfo(oi, name);
            if (ci == null) {
                p("calcinfo not in objectInfo");
                bResult = false;
                continue;
            }
            int pos = alCalc.indexOf(ci);
            bs[pos] = true;
            
            // compare properties
            String[] ss1 = ci.getProperties();
            String[] ss2 = annotation.properties();
            Arrays.sort(ss1);
            Arrays.sort(ss2);
            if (ss1.length != ss2.length) {
                p("calc props mismatch");
                bResult = false;
            }
            else {
                for (int j=0; j<ss1.length; j++) {
                    if (!ss1[j].equalsIgnoreCase(ss2[j])) {
                        p("calc prop name mismatch");
                        bResult = false;
                    }
                }
                
            }
        }
        for (boolean b : bs) {
            if (!b) {
                p("calcInfo mismatch");
                bResult = false;
            }
        }

        
        // Verify links
        List<OALinkInfo> alLinkInfo = oi.getLinkInfos();
        bs = new boolean[alLinkInfo.size()];
        // Ones
        for (Method m : methods) {
            OAOne annotation = (OAOne) m.getAnnotation(OAOne.class);
            Class c = m.getReturnType();
            if (annotation == null) {
                if (OAObject.class.isAssignableFrom(c)) {
                    p("method should be OAOne");
                    bResult = false;
                }
                continue;
            }
            if (!OAObject.class.isAssignableFrom(c)) {
                p("method should return subclass of OAObject");
                bResult = false;
            }
                        
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            if (li == null) {
                p("link does not exist");
                bResult = false;
            }
          
            
            if (li.getToClass() != m.getReturnType()) {
                p("wrong link class");
                bResult = false;
            }

            if (li.getCascadeSave() != annotation.cascadeSave()) {
                p("wrong cascade save");
                bResult = false;
            }
            if (li.getCascadeDelete() != annotation.cascadeDelete()) {
                p("wrong cascade delete");
                bResult = false;
            }
            s = li.getReverseName();
            if (s != annotation.reverseName() && (s != null && !s.equalsIgnoreCase(annotation.reverseName()))) {
                p("wrong reverse name");
                bResult = false;
            }
            if (li.getOwner() != annotation.owner()) {
                p("wrong owner");
                bResult = false;
            }
            
            if (li.getAutoCreateNew() && li.getAutoCreateNew() != annotation.autoCreateNew()) {
                p("wrong autoCreateNew");
                bResult = false;
            }
            
            int x = alLinkInfo.indexOf(li);
            bs[x] = true;
        }
        // Manys
        for (Method m : methods) {
            OAMany annotation = (OAMany) m.getAnnotation(OAMany.class);
            Class c = m.getReturnType();
            if (annotation == null) {
                if (Hub.class.isAssignableFrom(c)) {
                    p("method should be OAMany");
                    bResult = false;
                }
                continue;
            }
            if (!Hub.class.isAssignableFrom(c)) {
                p("method should return a Hub");
                bResult = false;
            }
                        
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            if (li == null) {
                p("link does not exist");
                bResult = false;
            }
          
            
            if (li.getCascadeSave() != annotation.cascadeSave()) {
                p("wrong cascade save");
                bResult = false;
            }
            if (li.getCascadeDelete() != annotation.cascadeDelete()) {
                p("wrong cascade delete");
                bResult = false;
            }
            s = li.getReverseName();
            if (s != annotation.reverseName() && (s != null && !s.equalsIgnoreCase(annotation.reverseName()))) {
                p("wrong reverse name");
                bResult = false;
            }
            if (li.getOwner() != annotation.owner()) {
                p("wrong owner");
                bResult = false;
            }
            
            int x = alLinkInfo.indexOf(li);
            bs[x] = true;
            
        }
        
        int i = 0;
        for (boolean b : bs) {
            if (!b) {
                OALinkInfo li = alLinkInfo.get(i);
                p("link mismatch, name="+li.getName());
                bResult = false;
            }
            i++;
        }
        return bResult;
    }
    
    

    /**
     * Verify class annotations with Database
     */
    public boolean verify(Class clazz, Database database) throws Exception {
        boolean[] bs = null;
        int i;
        String s;
        
        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private. qqqqqq does not get superclass methods

        // columns
        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        if (dbTable == null) {
            p("no table annotation");
            return false;
        }
        
        Table table = database.getTable(clazz);
        if (table == null) {
            p("table not found");
            return false;
        }
        
        boolean bResult = true;
        Column[] columns = table.getColumns();
        bs = new boolean[columns.length];
        
        for (Method m : methods) {
            OAColumn col = (OAColumn) m.getAnnotation(OAColumn.class);
            
            if (col != null) {
                String name = col.name();
                if (name == null || name.length() == 0) {
                    name = OAAnnotationDelegate.getPropertyName(m.getName());
                }
                
                boolean b = false;
                for (i=0; i<columns.length; i++) {
                    if (columns[i].columnName.equalsIgnoreCase(name)) {
                        bs[i] = true;
                        b = true;
                        if (columns[i].type != col.sqlType()) {
                            int xx = col.sqlType();
                            p("column sql type mismatch");
                            bResult = false;
                        }
                        s = OAAnnotationDelegate.getPropertyName(m.getName());
                        if (!s.equalsIgnoreCase(columns[i].propertyName)) {
                            p("column prop name mismatch");
                            bResult = false;
                        }

                        OAId id = (OAId) m.getAnnotation(OAId.class);
                        if (id != null) {
                            if (id.autoAssign() != columns[i].primaryKey) {
                                p("column pkey mismatch");
                                bResult = false;
                            }
                            if (id.guid() != columns[i].guid) {
                                p("column guid mismatch");
                                bResult = false;
                            }
                        }

                        if (col.sqlType() == java.sql.Types.VARCHAR) {
                            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
                            if (oaprop != null) {
                                int x = oaprop.maxLength();
                                if (x > 0) {
                                    if (columns[i].maxLength != x) {
                                        p("varchar maxLength mismatch, column="+columns[i].columnName);
                                        bResult = false;
                                    }
                                }
                                x = oaprop.decimalPlaces();
                                if (x >= 0) {
                                    if (columns[i].decimalPlaces != x) {
                                        p("varchar decimalPlaces mismatch, column="+columns[i].columnName);
                                        bResult = false;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                if (!b) {
                    p("did not find column");
                    bResult = false;
                }
            }
            
            OAFkey fk = (OAFkey) m.getAnnotation(OAFkey.class);
            if (fk == null) continue;
            
            String[] fkcols = fk.columns();
            for (int j=0; j<fkcols.length; j++) {
                boolean b = false;
                for (i=0; i<columns.length; i++) {
                    if (columns[i].columnName.equalsIgnoreCase(fkcols[j])) {
                        b = true;
                        bs[i] = true;
                        break;
                    }
                }
                if (!b) {
                    p("did not find fkcolumn");
                    bResult = false;
                }
            }
        }
        for (boolean b : bs) {
            if (!b) {
                p("table column mismatch");
                bResult = false;
            }
        }

        
        
        // indexes
        OAIndex[] indexes = dbTable.indexes();
        Index[] inds = table.getIndexes();
        bs = new boolean[inds.length];
        for (i=0; i<inds.length; i++) {
            OAIndex indFnd = null;
            for (int j=0; j<indexes.length; j++) {
                if (inds[i].name.equalsIgnoreCase(indexes[j].name())) {
                    bs[i] = true;
                    indFnd = indexes[j];
                    break;
                }
            }
            if ( indFnd == null) {
                p("index mismatch1");
                bResult = false;
                continue;
            }
            // compare columns
            for (String si : inds[i].columns) {
                boolean b = false;
                for (OAIndexColumn ic : indFnd.columns()) {
                    if (ic.name().equalsIgnoreCase(si)) {
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    p("index column mismatch");
                    bResult = false;
                }
            }
        }

        for (boolean b : bs) {
            if (!b) {
                p("index mismatch2");
                bResult = false;
            }
        }
        return bResult;
    }

    /**
     * Make sure that the links in the Tables are also in the class annotations.
     */
    // this is not used
    public void verifyLinks(Database database) throws Exception {
        for (Table table : database.getTables()) {
            Class clazz = table.clazz;
            for (Link link : table.getLinks()) {
                Method method = clazz.getDeclaredMethod("get"+link.propertyName, null);
                if (method == null) {
                    throw new Exception("cant find method for link "+link.propertyName);
                }
                OAOne oaone = (OAOne) method.getAnnotation(OAOne.class);
                OAMany oamany = (OAMany) method.getAnnotation(OAMany.class);
                Class c = method.getReturnType();
                if (oaone != null) {
                    if (oamany != null) throw new Exception("cant have One and Many annotion for same method, for link property "+link.propertyName);
                    if (!OAObject.class.isAssignableFrom(c)) throw new Exception("OAOne annotation for class that does not return an OAObject subclass");
                }
                else {
                    if (oamany == null) throw new Exception("no One or Many annotion for link property "+link.propertyName);
                    if (!Hub.class.isAssignableFrom(c)) throw new Exception("OAMany annotation for class that does not return an Hub");
                }
            }
        }
    }
    

    
    public boolean compare(Database db1, Database db2) {
        int x1 = db1.getTables().length;
        int x2 = db2.getTables().length;
        if (x1 != x2) {
            p("mismatch in number of tables");
            return false;
        }
        boolean bResult = true;
        for (Table t : db1.getTables()) {
            Table t2 = db2.getTable(t.name);
            if (t2 == null) {
                p("table not found, "+t.name);
                bResult = false;
                continue;
            }
            if (!compare(t, t2)) bResult = false;
        }
        return bResult;
    }
    boolean compare(Table t1, Table t2) {
        if (!t1.name.equalsIgnoreCase(t2.name)) {
            p("mismatch in names");
            return false;
        }
        if (t1.clazz != null && !t1.clazz.equals(t2.clazz)) {
            p("mismatch in class");
            return false;
        }
        boolean bResult = true;
        int x1 = t1.getColumns().length;
        int x2 = t2.getColumns().length;
        if (x1 != x2) {
HashSet<String> h = new HashSet<String>();
for (Column c : t1.getColumns()) {
    h.add(c.columnName.toUpperCase());
}
for (Column c : t2.getColumns()) {
    h.remove(c.columnName.toUpperCase());
}

            p("mismatch in number of columns");
            bResult = false;
        }
        for (Column c : t1.getColumns()) {
            Column c2 = t2.getColumn(c.columnName, c.propertyName);
            if (c2 == null) {
                p("column not found");
                bResult = false;
            }
            else {
                if (!compare(c, c2)) bResult = false;
            }
        }
        if (x1 != x2) {
            for (Column c : t2.getColumns()) {
                Column c2 = t1.getColumn(c.columnName, c.propertyName);
                if (c2 == null) {
                    p("column not found");
                    bResult = false;
                }
            }
        }
        x1 = t1.getLinks().length;
        x2 = t2.getLinks().length;
        if (x1 != x2) {
            p("mismatch in number of links");
            return false;
        }
        for (Link link : t1.getLinks()) {
            Link link2 = t2.getLink(link.propertyName);
            if (link2 == null) {
                p("link not found, "+t1.name+"."+link.propertyName);
                bResult = false;
                continue;
            }
            if (!compare(link, link2)) bResult = false;
        }
        for (Index ind : t1.getIndexes()) {
            Index[] inds = t2.getIndexes();
            boolean b = false;
            for (Index ix : inds) {
                if (ind.name.equalsIgnoreCase(ix.name)) {
                    if (!compare(ind, ix)) bResult = false;
                    b = true;
                }
                else {
                    // strip off last word, which would be the index property name
                    String s = ind.name;
                    int pos = 0;
                    int x = s.length();
                    for (int i=0; i<x; i++) {
                        if (Character.isUpperCase(ind.name.charAt(i))) pos = i;
                    }
                    
                    if (!b && pos > 0) {
                        s = s.substring(0, pos);
                        if (s.equalsIgnoreCase(ix.name)) {
                            if (!compare(ind, ix)) bResult = false;
                            b = true;
                        }
                        
                    }
                }
            }
            if (!b) {
                p("Index not found, "+t1.name+"."+ind.name);
                bResult = false;
            }
        }
        return true;
    }
    boolean compare(Column c1, Column c2) {
        if (!c1.columnName.equalsIgnoreCase(c2.columnName)) {
            p("mismatch in columnName: "+c1.columnName+", "+c2.columnName);
            return false;
        }
        if (c1.propertyName == null) {
            if (c2.propertyName != null && c2.propertyName.length() != 0) {
                p("mismatch in propertyName, null");
                return false;
            }
        }
        else if (!c1.propertyName.equalsIgnoreCase(c2.propertyName)) {
            p("mismatch in propertyName: "+c1.propertyName+", "+c2.propertyName);
            return false;
        }
        if (c1.primaryKey != c2.primaryKey) {
//20100714 uncomment after VJ is done qqqqqqqqqqqqqqqqqqqqqqqqqq
//            p("mismatch in primaryKey");
//            return false;
        }
        if (c1.foreignKey != c2.foreignKey) {
//20100714 uncomment after VJ is done qqqqqqqqqqqqqqqqqqqqqqqqqq
//            p("mismatch in foreignKey");
//            return false;
        }
        if (c1.clazz != c2.clazz) {
//20100714 uncomment after VJ is done qqqqqqqqqqqqqqqqqqqqqqqqqq
//            p("mismatch in clazz");
//            return false;
        }
        if (c1.type != c2.type) {
//20100714 uncomment after VJ is done qqqqqqqqqqqqqqqqqqqqqqqqqq
//            p("mismatch in type "+c1.columnName+": "+c1.type+", " + c2.type);
//            return false;
        }
        if (c1.maxLength != c2.maxLength) {
            if (c1.type == Types.VARCHAR) { 
                p("mismatch in maxLength, column="+c1.columnName);
                return false;
            }
        }
        if (c1.decimalPlaces != c2.decimalPlaces) {
//20100714 uncomment after VJ is done qqqqqqqqqqqqqqqqqqqqqqqqqq
if (true) return true;            
//            p("mismatch in decimalPlaces");
            return false;
        }
        if (c1.assignNextNumber != c2.assignNextNumber) {
            p("mismatch in assignNextNumber");
            return false;
        }
        if (c1.guid != c2.guid) {
            p("mismatch in guid");
            return false;
        }
        return true;
    }
    boolean compare(Index ind1, Index ind2) {
        if (!ind1.name.equalsIgnoreCase(ind2.name)) {
//            p("mismatch in index name");
//            return false;
        }
        String[] c1 = ind1.columns;
        String[] c2 = ind2.columns;
        if (c1.length != c2.length) {
            p("mismatch in number of columns for index");
            return false;
        }
        for (String s : c1) {
            if (!OAArray.contains(c2, s, true)) {
                p("column not found in index");
                return false;
            }
        }
        return true;
    }
    boolean compare(Link link1, Link link2) {
        if (!link1.propertyName.equalsIgnoreCase(link2.propertyName)) {
            p("propertyName mismatch for link");
            return false;
        }
        if (!link1.reversePropertyName.equalsIgnoreCase(link2.reversePropertyName)) {
            p("reversePropertyName mismatch for link="+link1.reversePropertyName+","+link2.reversePropertyName);
            return false;
        }
        if (!link1.toTable.name.equalsIgnoreCase(link2.toTable.name)) {
            p("toTable mismatch for link");
            return false;
        }
        if (link1.fkeys.length != link2.fkeys.length) {
            p("mismatch in link fkey length");
            return false;
        }
        for (Column c1 : link1.fkeys) {
            boolean b = false;
            for (Column c2 : link2.fkeys) {
                if (c1.columnName.equalsIgnoreCase(c2.columnName)) {
                    b = true;
                    break;
                }
            }
            if (!b) {
                p("link fkey column match not found");
                return false;
            }
        }
        return true;
    }
    
    
    public boolean compare(OAObjectInfo oi1, OAObjectInfo oi2) {
        if (oi1.thisClass != oi2.thisClass) {
            p("class mismatch");
            return false;
        }
        if (oi1.bUseDataSource != oi2.bUseDataSource) {
            p("class bUseDataSource");
            return false;
        }
        if (oi1.bLocalOnly != oi2.bLocalOnly) {
            p("class bLocalOnly");
            return false;
        }
        if (oi1.bAddToCache != oi2.bAddToCache) {
            p("class bAddToCache");
            return false;
        }
        if (oi1.bInitializeNewObjects != oi2.bInitializeNewObjects) {
            p("class bInitializeNewObjects");
            return false;
        }
        if (oi1.displayName != oi2.displayName) {
            if (oi1.displayName == null || !oi1.displayName.equalsIgnoreCase(oi2.displayName)) {
                // p("class displayName");
            }
        }
        if (oi1.idProperties != oi2.idProperties) {
            boolean b = true;
            if (oi1.idProperties == null || oi2.idProperties == null) b = false;
            else {
                if (oi1.idProperties.length != oi2.idProperties.length) b = false;
                else {
                    int x = oi1.idProperties.length;
                    for (int i=0; i<x; i++) {
                        if (oi1.idProperties[i] == null || oi2.idProperties[i] == null) {
                            b = false;
                            break;
                        }
                        if (!oi1.idProperties[i].equalsIgnoreCase(oi2.idProperties[i])) {
                            b = false;
                            break;
                        }
                    }
                }
            }
            if (!b) {
                p("class idProperties");
                return false;
            }
        }

        if (oi1.primitiveProps != oi2.primitiveProps) {
            boolean b = true;
            if (oi1.primitiveProps == null || oi2.primitiveProps == null) b = false;
            else {
                if (oi1.primitiveProps.length != oi2.primitiveProps.length) b = false;
                else {
                    int x = oi1.primitiveProps.length;
                    for (int i=0; i<x; i++) {
                        if (oi1.primitiveProps[i] == null || oi2.primitiveProps[i] == null) {
                            b = false;
                            break;
                        }
                        if (!oi1.primitiveProps[i].equalsIgnoreCase(oi2.primitiveProps[i])) {
                            b = false;
                            break;
                        }
                    }
                }
            }
            if (!b) {
                p("class primitiveProps");
                return false;
            }
        }

        List<OALinkInfo> al = oi1.getLinkInfos();
        List<OALinkInfo> al2 = oi2.getLinkInfos();
        if (al != al2 && al == null || al.size() != al2.size()) {
            p("LinkInfos mismatch");
            return false;
        }
        int x = al.size();
        for (int i=0; i<x; i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            boolean b = false;
            for (int j=0; j<x; j++) {
                OALinkInfo li2 = (OALinkInfo) al2.get(j);
                if (li2.getName() != null && li2.getName().equalsIgnoreCase(li.getName())) {
                    if (!compare(li, li2)) return false;
                    b = true;
                    break;
                }
            }
            if (!b) {
                p("no matching linkInfo");
                return false;
            }
        }
        
        ArrayList<OACalcInfo> alCalc = oi1.getCalcInfos();
        ArrayList<OACalcInfo> alCalc2 = oi2.getCalcInfos();
        if (alCalc != alCalc2 && alCalc == null || alCalc.size() != alCalc2.size()) {
            p("CalcInfos mismatch");
            return false;
        }
        x = alCalc.size();
        for (int i=0; i<x; i++) {
            OACalcInfo ci = (OACalcInfo) alCalc.get(i);
            boolean b = false;
            for (int j=0; j<x; j++) {
                OACalcInfo ci2 = (OACalcInfo) alCalc2.get(j);
                if (ci.name != null && ci.name.equalsIgnoreCase(ci2.name)) {
                    if (!compare(ci, ci2)) return false;
                    b = true;
                    break;
                }
            }
            if (!b) {
                p("calc matching name not found");
                return false;
            }
        }
        return true;
    }

    boolean compare(OALinkInfo li, OALinkInfo li2) {
        if (li == null || li2 == null) return false;
        if (li.getName() == null || !li.getName().equalsIgnoreCase(li2.getName())) {
            p("link name dont match");
            return false;
        }
        if (li.getToClass() == null || !li.getToClass().equals(li2.getToClass())) {
            p("link toClass dont match");
            return false;
        }
        if (li.getType() != li2.getType()) {
            p("link type dont match");
            return false;
        }
        if (li.getCascadeSave() != li2.getCascadeSave()) {
            p("link cascadeSave dont match");
            return false;
        }
        if (li.getCascadeDelete() != li2.getCascadeDelete()) {
            p("link cascadeDelete dont match");
            return false;
        }

        if (li.getReverseName() == null) {
            // method not created, not needed
        }
        else if (!li.getReverseName().equalsIgnoreCase(li2.getReverseName())) {
            p("link reverseName dont match");
            return false;
        }
        if (li.getOwner() != li2.getOwner()) {
            p("link owner dont match");
            return false;
        }
        if (li.getAutoCreateNew() && li.getAutoCreateNew() != li2.getAutoCreateNew()) {
            p("link autoCreateNew dont match");
            return false;
        }
        return true;
    }

    boolean compare(OACalcInfo ci, OACalcInfo ci2) {
        if (ci == null || ci2 == null) return false;
        if (ci.getName() == null || !ci.getName().equals(ci2.getName())) {
            p("calcProperty name dont match");
            return false;
        }
        String[] p1 = ci.getProperties();
        String[] p2 = ci2.getProperties();
        if (p1 == null || p2 == null) {
            if (p1 != p2) {
                p("calc properties dont match");
                return false;
            }
        }
        if (p1.length != p2.length) {
            p("calc property count dont match");
            return false;
        }
        boolean b = false;
        for (int i=0; !b && i<p1.length; i++) {
            for (int j=0; !b && j<p1.length; j++) {
                if (p1[i].equalsIgnoreCase(p2[j])) b = true;
            }
        }
        if (!b && p1.length > 0) {
            p("calc property name dont match");
            return false;
        }
        return true;
    }
    
    
    void p(String msg) {
        //LOG.warning(msg);
        System.out.println("Error: "+msg);
    }

 /**   
    public static void main(String[] args) throws Exception {
        OAAnnotationVerifier verifier = new OAAnnotationVerifier();
       
        // DataSource ds = new DataSource("server", "database", "user", "pw");
        DataSource ds = new DataSource();
        ds.startup(null, null, null, null, 0, 0, 0);
        Database database = ((OADataSourceJDBC)ds.getOADataSource()).getDatabase();
        DBMetaData dbmd = ((OADataSourceJDBC)ds.getOADataSource()).getDBMetaData();

        String[] fnames = OAReflect.getClasses("com.vetjobs.oa");

        Class[] classes = new Class[0];
        for (String fn : fnames) {
            Class c = Class.forName("com.vetjobs.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            classes = (Class[]) OAArray.add(Class.class, classes, c);
        }
        
/ **
        for (String fn : fnames) {
            System.out.println("oi&ds ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            
            if (c.getAnnotation(OATable.class) == null) continue;
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            del.verify(oi);
            del.verify(c, database);
        }
* /
                
        // Create database
        Database database2 = new Database();

        Table table = new Table("NextNumber",com.viaoa.ds.autonumber.NextNumber.class); // ** Used by all OADataSource Database
        // NextNumber COLUMNS
        Column[] columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER);
        table.setColumns(columns);
        database2.addTable(table);

        OAAnnotationDelegate.update(database2, classes);
/ **                
        // Verify
        for (Class c : classes) {
            System.out.println("verify OA ==>"+c.getSimpleName());
            del.verify(c, database2);
        }
                
        System.out.println("verify database Links ==>");
        del.verifyLinks(database2);
* /

        verifier.compare(database, database2);
        
        for (Class c : classes) {
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            // compare class annotations with ObjectInfo
            verifier.verify(oi);

            OAObjectInfo oi2 = new OAObjectInfo();
            OAAnnotationDelegate.update(oi2, c);
            
            int x = oi2.getCalcInfos().size();
            int x2 = oi2.getLinkInfos().size();
            OAObjectInfoDelegate.initialize(oi2, c);
            
            if (x != oi2.getCalcInfos().size()) verifier.p("CalcInfos missing");
            if (x2 != oi2.getLinkInfos().size()) verifier.p("LinkInfos missing");
            
            verifier.compare(oi, oi2);
        }
        
        
        // must have database access to run this
        // System.out.println("datasource VerifyDelegate.verify database ==>");
        // OADataSourceJOAC dsx = new OADataSourceJOAC(database, dbmd);
        // VerifyDelegate.verify(dsx);
        
        
        System.out.println("done");
    }
*/    
// qqqqqqqqqqqqq need to test where there is a superClass, objectInfo needs to combine    
}



