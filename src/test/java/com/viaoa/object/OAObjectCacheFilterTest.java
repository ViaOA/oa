package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theice.tsactest.model.oa.*;
import com.theice.tsactest.model.oa.propertypath.*;
import com.tmgsc.hifivetest.model.oa.Employee;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubFilter;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;

public class OAObjectCacheFilterTest extends OAUnitTest {
    
    @Test
    public void test() {
        reset();
        
        Hub<Employee> hubMaster = new Hub<Employee>(Employee.class);
        Hub<Employee> hubFiltered = new Hub<Employee>(Employee.class);
        

        HubFilter<Employee> hf = new HubFilter<Employee>(hubMaster, hubFiltered);
        
        OAObjectCacheFilter<Employee> cf = new OAObjectCacheFilter<Employee>(hf);
        
        Employee emp = new Employee();
        
        assertEquals(1, hubMaster.getSize());
        assertEquals(1, hubFiltered.getSize());
        
        hubMaster.remove(0);

        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());
    }

    @Test
    public void test2() {
        reset();
        
        Hub<Employee> hubMaster = new Hub<Employee>(Employee.class);
        Hub<Employee> hubFiltered = new Hub<Employee>(Employee.class);
        

        HubFilter<Employee> hf = new HubFilter<Employee>(hubMaster, hubFiltered);
        
        OAObjectCacheFilter<Employee> cf = new OAObjectCacheFilter<Employee>(hf) {
            @Override
            public boolean isUsed(Employee obj) {
                return false;
            }
        };
        
        Employee emp = new Employee();
        
        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());
        
        hubMaster.remove(0);

        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());
    }

    @Test
    public void test3() {
        reset();
        
        Hub<Employee> hubMaster = new Hub<Employee>(Employee.class);
        Hub<Employee> hubFiltered = new Hub<Employee>(Employee.class);
        
        HubFilter<Employee> hf = new HubFilter<Employee>(hubMaster, hubFiltered);
        hf.addDependentProperty(Employee.P_LastName);
        
        OAObjectCacheFilter<Employee> cf = new OAObjectCacheFilter<Employee>(hf) {
            @Override
            public boolean isUsed(Employee obj) {
                return obj.getLastName() != null;
            }
        };
        
        Employee emp = new Employee();
        
        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());
        
        emp.setLastName("test");
        assertEquals(1, hubMaster.getSize());
        assertEquals(1, hubFiltered.getSize());
        
        
        hubMaster.remove(0);

        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());

        emp.setLastName(null);
        assertEquals(0, hubMaster.getSize());
        assertEquals(0, hubFiltered.getSize());

        emp.setLastName("test");
        assertEquals(1, hubMaster.getSize());
        assertEquals(1, hubFiltered.getSize());
        
        for (int i=0; i<10; i++) {
            emp = new Employee();
            assertEquals(i+1, hubMaster.getSize());
            assertEquals(i+1, hubFiltered.getSize());
            emp.setLastName("x");
            assertEquals(i+2, hubMaster.getSize());
            assertEquals(i+2, hubFiltered.getSize());
        }
    }
    
}
