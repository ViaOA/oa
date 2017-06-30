package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import test.hifive.DataGenerator;
import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.EmployeePP;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.object.OATypeAhead.OATypeAheadParams;


public class OATypeAheadTest extends OAUnitTest {

    @Test
    public void test() {
        
        Program prog = new Program();
        for (int i=0; i<5; i++) {
            Location l = new Location();
            l.setName("l."+i);
            prog.getLocations().add(l);
            for (int i2=0; i2<5; i2++) {
                Location l2 = new Location();
                l.getLocations().add(l2);
                l2.setName("l2."+i2);
                for (int i3=0; i3<5; i3++) {
                    Employee emp = new Employee();
                    emp.setFirstName("aaa."+i+"."+i2+"."+i3);
                    l2.getEmployees().add(emp);
                }
            }
        }
        Employee emp = new Employee();
        prog.getLocations().getAt(0).getEmployees().add(emp);
        prog.getLocations().getAt(0).getEmployees().setAO(0);
        
        Hub<Employee> h = prog.getLocations().getDetailHub("Employees");
        prog.getLocations().setPos(0);
        h.setPos(0);
        
        assertTrue(h.isValid());
        assertNotNull(h.getAO());
        
        OATypeAheadParams tap = new OATypeAheadParams<>();
        tap.finderPropertyPath = EmployeePP.location().program().locations().employees().pp;
        tap.matchPropertyPath = EmployeePP.firstName();
        tap.maxResults = 10;
        
        OATypeAhead<Program, Employee> ta = new OATypeAhead(h, tap);
        
        ArrayList<Employee> al = ta.search("aaa");
        assertTrue(al.size() > 0);
        
        for (Employee e : al) {
            String s = ta.getDisplayValue(e);
            assertNotNull(s);
            s = ta.getDropDownDisplayValue(e);
        }
        
        int x = 4;
        x++;
    }
    
    public static void main(String[] args) {
        OATypeAheadTest test = new OATypeAheadTest();
        test.test();
    }
    
}
