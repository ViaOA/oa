package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.viaoa.OAUnitTest;
import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAColumn;
import com.viaoa.annotation.OAFkey;
import com.viaoa.annotation.OAId;
import com.viaoa.annotation.OAMany;
import com.viaoa.annotation.OAOne;
import com.viaoa.annotation.OAProperty;
import com.viaoa.annotation.OATable;
import com.viaoa.ds.jdbc.db.Column;
import com.viaoa.ds.jdbc.db.Database;
import com.viaoa.ds.jdbc.db.Table;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

import test.theice.tsam.model.oa.*;


public class OAAnnotationDelegateTest extends OAUnitTest {

    @Test
    public void testUpdate() throws Exception  {
        testUpdate1();
        
        testUpdate2(Server.class);
        testUpdateDatabase(Server.class);
        
        testUpdate2(Application.class);  // match
        testUpdateDatabase(Application.class);
        
        testUpdate2(AdminUserCategory.class); // recursive
        testUpdateDatabase(AdminUserCategory.class);
    }
    
    
    protected void testUpdate1() {
        final Class c = Server.class;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
        
        // @OAProperty(maxLength = 55, displayLength = 12, columnLength = 10)
        // @OAColumn(maxLength = 55)

        OAPropertyInfo pi = oi.getPropertyInfo(Server.P_Name);
        
        OAAnnotationDelegate.update(oi, c);

        assertEquals(10, pi.getColumnLength());
        assertEquals(55, pi.getMaxLength());

        
        OALinkInfo li = oi.getLinkInfo(Server.P_Applications);
        
        assertEquals(li.MANY, li.getType());
        assertEquals(true, li.getCascadeSave());
    }
 
    protected void testUpdate2(final Class clazz) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
    
        OAClass oaclass = (OAClass) clazz.getAnnotation(OAClass.class);
        assertNotNull(oaclass);
        
        assertEquals(oaclass.useDataSource(), oi.getUseDataSource());
        assertEquals(oaclass.localOnly(), oi.getLocalOnly());
        assertEquals(oaclass.addToCache(), oi.getAddToCache());
        assertEquals(oaclass.initialize(), oi.getInitializeNewObjects());
        assertEquals(oaclass.displayName(), oi.getDisplayName());
        
        String[] pps1 = oaclass.rootTreePropertyPaths();
        String[] pps2 = oi.getRootTreePropertyPaths();
        assertTrue(OAArray.isEqual(pps1, pps2));
        
        // propertyIds
        String[] ss = oi.getIdProperties();        
        Method[] methods = clazz.getDeclaredMethods();
        int cnt = 0;
        for (Method m : methods) {
            OAId oaid = m.getAnnotation(OAId.class);
            if (oaid == null) continue;
            cnt++;
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            assertNotNull(oaprop);
        }       
        assertEquals(ss.length, cnt);
        
        // props
        cnt = 0;
        for (Method m : methods) {
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            if (oaprop == null) continue;
            cnt++;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            
            OAPropertyInfo pi = oi.getPropertyInfo(name);
            assertNotNull(pi);
        
            assertEquals(pi.getMaxLength(), oaprop.maxLength());
            assertEquals(pi.getUnique(), oaprop.isUnique());
            
            assertEquals(pi.getClassType(), m.getReturnType());
        }
        ArrayList<OAPropertyInfo> al = oi.getPropertyInfos();
        assertEquals(al.size(), cnt);
        
        // calc props
        cnt = 0;
        ArrayList<OACalcInfo> alCalc = oi.getCalcInfos();
        for (Method m : methods) {
            OACalculatedProperty oacalc = (OACalculatedProperty) m.getAnnotation(OACalculatedProperty.class);
            if (oacalc == null) continue;
            cnt++;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            String[] props = oacalc.properties();
            oi.getCalcInfos();
            boolean b = false;
            for (OACalcInfo ci : alCalc) {
                if (!ci.getName().equalsIgnoreCase(name)) continue;
                b = true;
                assertTrue(OAArray.isEqual(props, ci.getProperties()));
            }
            assertTrue(b);
        }
        assertEquals(alCalc.size(), cnt);
        
        // links
        cnt = 0;
        List<OALinkInfo> alLinkInfo = oi.getLinkInfos();
        // Ones
        for (Method m : methods) {
            OAOne oaone = (OAOne) m.getAnnotation(OAOne.class);
            if (oaone == null) continue;
            cnt++;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            Class c = m.getReturnType();
            boolean b = false;
            for (OALinkInfo li : alLinkInfo) {
                if (!li.getName().equalsIgnoreCase(name)) continue;
                b = true;
                assertEquals(li.isImportMatch(), oaone.isImportMatch());
                assertEquals(li.getCascadeSave(), oaone.cascadeSave());
                assertEquals(li.getCascadeDelete(), oaone.cascadeDelete());
                assertEquals(li.getOwner(), oaone.owner());
                assertEquals(li.getAutoCreateNew(), oaone.autoCreateNew());
                assertEquals(li.getCalculated(), oaone.isCalculated());
                assertEquals(li.getMustBeEmptyForDelete(), oaone.mustBeEmptyForDelete());
            }
            assertTrue(b);
        }        
        
        // manys
        for (Method m : methods) {
            OAMany oamany = (OAMany) m.getAnnotation(OAMany.class);
            if (oamany == null) continue;
            cnt++;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
            Class c = m.getReturnType();
            boolean b = false;
            for (OALinkInfo li : alLinkInfo) {
                if (!li.getName().equalsIgnoreCase(name)) continue;
                b = true;
                assertEquals(li.getCascadeSave(), oamany.cascadeSave());
                assertEquals(li.getCascadeDelete(), oamany.cascadeDelete());
                assertEquals(li.getOwner(), oamany.owner());
                assertEquals(li.getCalculated(), oamany.isCalculated());
                assertEquals(li.getMustBeEmptyForDelete(), oamany.mustBeEmptyForDelete());
                assertEquals(li.getRecursive(), oamany.recursive());
                assertTrue(OAString.isEqual(OAString.toString(li.getMatchProperty()), oamany.matchProperty()));
                assertEquals(li.getPrivateMethod(), !oamany.createMethod());
                assertEquals(li.getCacheSize(), oamany.cacheSize());
                Class[] cs1 = oamany.triggerClasses();
                Class[] cs2 = li.getTriggerClasses();
                assertTrue(OAArray.isEqual(cs1, cs2));
            }
            assertTrue(b);
        }
        assertEquals(alLinkInfo.size(), cnt);
    }

    
    protected void testUpdateDatabase(final Class clazz) throws Exception {
        Database database = new Database();
        
        // need to load all classes to be able to create database
        String packageName = clazz.getPackage().getName();
        String[] fnames = OAReflect.getClasses(packageName);
        Class[] classes = null;
        for (String fn : fnames) {
            Class c = Class.forName(packageName + "." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            classes = (Class[]) OAArray.add(Class.class, classes, c);
        }
        OAAnnotationDelegate.update(database, classes);
        
        
        
        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        assertNotNull(dbTable);
        
        Table table = database.getTable(clazz);
        assertNotNull(table);
        assertTrue(dbTable.name().equals("") || OAString.isEqual(dbTable.name(), table.name, true));
        
        // columns
        Method[] methods = clazz.getDeclaredMethods();
        int cnt = 0;
        for (Method m : methods) {
            OAColumn dbcol = (OAColumn) m.getAnnotation(OAColumn.class);
            if (dbcol == null) continue;
            cnt++;
            String name = OAAnnotationDelegate.getPropertyName(m.getName());

            Column col = table.getColumn(dbcol.name(), name);
            assertNotNull(col);
            
            assertEquals(dbcol.maxLength(), col.maxLength);
            
            assertEquals(dbcol.isFullTextIndex(), col.fullTextIndex);
        }
        
        for (Method m : methods) {
            OAFkey dbfk = (OAFkey) m.getAnnotation(OAFkey.class);
            if (dbfk == null) continue;
            cnt++;
            
            OAOne oaone = (OAOne) m.getAnnotation(OAOne.class);
            assertNotNull(oaone);
            String name = OAAnnotationDelegate.getPropertyName(m.getName());
        }
        assertEquals(table.getColumns().length, cnt);
    }
}



