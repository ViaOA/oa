package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;

import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;

public class HubTest extends OAUnitTest {

    @Test
    public void setAO() {
        reset();
        
        Hub h = new Hub();
        h.add("one");
        h.add("two");
        
        h.setAO("one");
        
        assertEquals(h.getAO(), "one");
    }

    @Test
    public void setAO2() {
        reset();
        
        Hub h = new Hub();
        Row r = new Row();
        h.add(new Row());
        h.add(new Row());
        h.add(r);
        h.add(new Row());
        
        h.setAO(r);
        
        assertEquals(h.getAO(), r);
    }
    
    class Row {
    }
    
    @Test
    public void testIterator() {
        Location loc = new Location();
        for (int i=0; i<50; i++) {
            Employee emp = new Employee();
            loc.getEmployees().add(emp);
        }
        int cnt = 0;
        for (Employee emp : loc.getEmployees()) {
            cnt++;
        }
        assertEquals(50, cnt);

        cnt = 0;
        for (Employee emp : loc.getEmployees()) {
            cnt++;
            emp.delete();
        }
        assertEquals(50, cnt);
    }
}

