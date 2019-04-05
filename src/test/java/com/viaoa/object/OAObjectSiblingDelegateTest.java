package com.viaoa.object;

import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.*;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.cs.ServerRoot;
import test.hifive.model.oa.propertypath.*;
import test.hifive.model.oa.*;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author vvia
 *
 */
public class OAObjectSiblingDelegateTest extends OAUnitTest {

    @Test
    public void test() throws Exception {
        init();
        OAObject.setDebugMode(true);        

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        final Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getEmployees().getAt(0);

        OAObjectKey[] oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(0, oks.length);
        
        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(0, oks.length);  // all have empawards
        
        
        final EmployeeType et = new EmployeeType();
        OAFinder<Program, Employee> f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.unsafeSetProperty(obj, Employee.P_EmployeeType, et.getObjectKey());
            }
        };
        f.find(ModelDelegate.getPrograms());

        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(0, oks.length);  // will find the objKey object in oacache

        final OAObjectKey objKey = new OAObjectKey(9999);
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.unsafeSetProperty(obj, Employee.P_EmployeeType, objKey);
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(1, oks.length); 
        
        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(0, oks.length);  // all have empawards

        final EmployeeType et2 = new EmployeeType();
        final Hub<Employee> hubEmp = new Hub<>(Employee.class);
        hubEmp.add(emp);
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                hubEmp.add(obj);
                obj.setEmployeeType(et);
                //if (hubEmp.size() > 15) stop();
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(0, oks.length); // all the same 
        
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.removeProperty(obj, Employee.P_EmployeeAwards, false);
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(25, oks.length);

        oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 10);
        assertEquals(10, oks.length);

        OAObject.setDebugMode(false);
        reset();
    }


    @Test
    public void test2() throws Exception {
        init();
        OAObject.setDebugMode(true);        

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        ServerRoot serverRoot = new ServerRoot();
        
        Hub h = ModelDelegate.getPrograms();
        serverRoot.getActivePrograms().add(h);
        
        final Employee emp = serverRoot.getActivePrograms().getAt(0).getLocations().getAt(0).getEmployees().getAt(0);

        final AtomicInteger ai = new AtomicInteger();
        OAFinder<Program, Employee> f = new OAFinder<Program, Employee>("locations.employees"){
            int id = 999;
            @Override
            protected void onFound(Employee emp) {
                ai.incrementAndGet();
                emp.setEmployeeType(null);
                OAObjectPropertyDelegate.unsafeSetProperty(emp, Employee.P_EmployeeType, new OAObjectKey(id++));
            }
        };
        f.find(serverRoot.getActivePrograms());
        OAObjectKey[] oks = OASiblingHelperDelegate.getSiblings(emp, Employee.P_EmployeeType, 1000);
        assertEquals(ai.get()-1, oks.length);

        OAObject.setDebugMode(false);        
        reset();
    }
        
        
    @Test
    public void test3() throws Exception {
        init();
        OAObject.setDebugMode(true);        

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        ServerRoot serverRoot = new ServerRoot();
        
        Hub h = ModelDelegate.getPrograms();
        serverRoot.getActivePrograms().add(h);
        
        final Employee emp = serverRoot.getActivePrograms().getAt(0).getLocations().getAt(0).getEmployees().getAt(0);

        final AtomicInteger ai = new AtomicInteger();
        OAFinder<Program, AddOnItem> f = new OAFinder<Program, AddOnItem>(ProgramPP.locations().employees().employeeAwards().awardType().addOnItems().pp) {
            int id = 999;
            @Override
            protected void onFound(AddOnItem aoi) {
                ai.incrementAndGet();
                aoi.setItem(null);
                OAObjectPropertyDelegate.unsafeSetProperty(aoi, AddOnItem.P_Item, new OAObjectKey(id++));
            }
        };
        f.find(serverRoot.getActivePrograms());
        
        Hub<Location> hubLocation = new Hub(Location.class);
        hubLocation.add(serverRoot.getActivePrograms().getAt(0).getLocations());
        
        OASiblingHelper sh = new OASiblingHelper<>(serverRoot.getActivePrograms());
        sh.add(ProgramPP.locations().employees().employeeAwards().awardType().addOnItems().pp);
        OAThreadLocalDelegate.addSiblingHelper(sh);
        
        AwardType at = emp.getEmployeeAwards().getAt(0).getAwardType();
        AddOnItem aoi = at.getAddOnItems().getAt(0);
 
        OAObjectKey[] oks = OASiblingHelperDelegate.getSiblings(aoi, AddOnItem.P_Item, 1000);
        
        // works stand alone
        // assertEquals(9, oks.length);
        OAThreadLocalDelegate.removeSiblingHelper(sh);
        
        OAObject.setDebugMode(false);        
        reset();
    }
        
        
 //   @Test
    public void test4() throws Exception {
        init();
        OAObject.setDebugMode(true);        
        
        
/*qqqqqqqqqqqqq        
        
        ai.set(0);
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee emp) {
                ai.incrementAndGet();
                emp.setEmployeeType(null);
                OAObjectPropertyDelegate.unsafeSetProperty(emp, Employee.P_EmployeeType, new OAObjectKey(999));
            }
        };
        f.find(serverRoot.getActivePrograms());
        

        
        
        OASiblingHelper sh = new OASiblingHelper<>(serverRoot.getActivePrograms());
        OAThreadLocalDelegate.addSiblingHelper(sh);
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeType, 1000);
        OAThreadLocalDelegate.removeSiblingHelper(sh);

        assertEquals(0, oks.length);
*/
        OAObject.setDebugMode(false);        
        reset();
    }
    
    

}
