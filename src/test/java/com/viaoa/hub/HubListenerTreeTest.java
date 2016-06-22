package com.viaoa.hub;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.Employee;
import test.hifive.model.oa.propertypath.EmployeePP;

import org.junit.Test;
import static org.junit.Assert.*;

public class HubListenerTreeTest extends OAUnitTest {

//see:  HubListenerTest
    
    @Test
    public void test() {

        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };
        String[] ss = new String[] {
            EmployeePP.location().program().employees().pp
        };
        
        h.addHubListener(hl, "prop", ss);
        
        
        h.addHubListener(hl, "prop", ss);
        h.addHubListener(hl, "prop", ss);
        h.addHubListener(hl, "prop", ss);

        
    }
    
    public static void main(String[] args) {
        HubListenerTreeTest test = new HubListenerTreeTest();
        test.test();
    }
}
