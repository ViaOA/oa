package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAFilter;
import com.viaoa.util.filter.OAFilterDelegate;

import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;

public class OAObjectCacheFilterTest extends OAUnitTest {
    
    @Test
    public void test() {
        ArrayList<Employee> al = new ArrayList<Employee>();
        reset();
        
        Hub<Employee> hubFiltered = new Hub<Employee>(Employee.class);
        OAObjectCacheFilter<Employee> objectCacheFilter = new OAObjectCacheFilter<Employee>(hubFiltered);
        
        assertEquals(0, hubFiltered.getSize());
        Employee emp = new Employee();
        assertEquals(0, hubFiltered.getSize());
        
        objectCacheFilter.addFilter(new OAFilter<Employee>() {
            @Override
            public boolean isUsed(Employee obj) {
                return true;
            }
        });
        assertEquals(1, hubFiltered.getSize());

        
        OAObjectCacheDelegate.clearCache(Employee.class);
        objectCacheFilter.refresh();
        assertEquals(0, hubFiltered.getSize());
        
        for (int i=0; i<10; i++) {
            emp = new Employee();
            al.add(emp);
            assertEquals(i+1, hubFiltered.getSize());
        }
        
        OAFilter<Employee> f = new OAFilter<Employee>() {
            public boolean isUsed(Employee emp) {
                return (emp.getId() != 0);
            }
        };
        objectCacheFilter.addFilter(f);
        objectCacheFilter.addDependentProperty("Id");
        assertEquals(0, hubFiltered.getSize());

        int i = 0;
        for (Employee empx : al) {
            empx.setId(++i);
            assertEquals(i, hubFiltered.getSize());
        }

        objectCacheFilter.refresh();
        assertEquals(10, hubFiltered.getSize());
        
        objectCacheFilter.addDependentProperty("id");
        assertEquals(10, hubFiltered.getSize());
        
        al.get(0).setId(0);
        assertEquals(9, hubFiltered.getSize());
        
        al.get(0).setId(1);
        assertEquals(10, hubFiltered.getSize());
     
        f = new OAFilter<Employee>() {
            public boolean isUsed(Employee emp) {
                return (emp.getLastName() != null);
            }
        };
        objectCacheFilter.addFilter(f);
        assertEquals(0, hubFiltered.getSize());

        objectCacheFilter.addDependentProperty("lastName");
        assertEquals(0, hubFiltered.getSize());
        
        i = 0;
        for (Employee empx : al) {
            empx.setLastName("");
            assertEquals(++i, hubFiltered.getSize());
        }
        
        assertEquals(10, hubFiltered.getSize());
        
        objectCacheFilter.addDependentProperty("location.name");
        assertEquals(10, hubFiltered.getSize());
        
        
        f = new OAFilter<Employee>() {
            public boolean isUsed(Employee emp) {
                Location loc = emp.getLocation();
                return (loc != null && loc.getName() != null);
            }
        };
        objectCacheFilter.addFilter(f);
        assertEquals(0, hubFiltered.getSize());

        Location loc = new Location();
        for (Employee empx : al) {
            empx.setLocation(loc);
            assertEquals(0, hubFiltered.getSize());
        }
        
        loc.setName("x");
        assertEquals(10, hubFiltered.getSize());

        loc.setName(null);
        assertEquals(0, hubFiltered.getSize());

        loc.setName("x");
        assertEquals(10, hubFiltered.getSize());
        
        i = 0;
        for (Employee empx : al) {
            empx.setLocation(null);
            assertEquals(10-(++i), hubFiltered.getSize());
        }
        
        objectCacheFilter.close();
    }
}
