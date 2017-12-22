package com.viaoa.object;

import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.object.*;
import com.viaoa.hub.*;


import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.cs.ServerRoot;
import test.hifive.model.oa.*;

import static org.junit.Assert.*;

public class OAObjectSiblingDelegateTest extends OAUnitTest {

    @Test
    public void test() throws Exception {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        final Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getEmployees().getAt(0);

        OAObjectKey[] oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(0, oks.length);
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(0, oks.length);  // all have empawards
        
        
        final EmployeeType et = new EmployeeType();
        OAFinder<Program, Employee> f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.unsafeSetProperty(obj, Employee.P_EmployeeType, et.getObjectKey());
            }
        };
        f.find(ModelDelegate.getPrograms());

        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(0, oks.length);  // will find the objKey object in oacache

        final OAObjectKey objKey = new OAObjectKey(9999);
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.unsafeSetProperty(obj, Employee.P_EmployeeType, objKey);
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(1, oks.length);
        assertEquals(oks[0], emp.getObjectKey());
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(0, oks.length);  // all have empawards

        final EmployeeType et2 = new EmployeeType();
        final Hub<Employee> hubEmp = new Hub<>(Employee.class);
        hubEmp.add(emp);
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                hubEmp.add(obj);
                obj.setEmployeeType(et);
                if (hubEmp.size() > 15) stop();
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeType, 100);
        assertEquals(1, oks.length);
        
        
        f = new OAFinder<Program, Employee>("locations.employees"){
            @Override
            protected void onFound(Employee obj) {
                OAObjectPropertyDelegate.removeProperty(obj, Employee.P_EmployeeAwards, false);
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        oks = OAObjectSiblingDelegate.getSiblings(emp, Employee.P_EmployeeAwards, 25);
        assertEquals(25, oks.length);

        
    }


}
