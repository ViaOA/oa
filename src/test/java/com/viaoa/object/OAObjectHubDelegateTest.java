package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.theice.tsactest.model.oa.*;
import com.theice.tsactest.model.oa.propertypath.*;
import com.tmgsc.hifivetest.model.oa.Employee;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;

public class OAObjectHubDelegateTest extends OAUnitTest {
    
    @Test
    public void test() {
        reset();
        
        Employee emp = new Employee();
        assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        Hub<Employee> hub = new Hub<Employee>(Employee.class);
        hub.add(emp);
        
        assertEquals(1, OAObjectHubDelegate.getHubReferenceCount(emp));
     
        hub.remove(emp);
        assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));

        assertNull(OAObjectHubDelegate.getHubReferences(emp));
    }
    
    @Test
    public void test2() {

        ArrayList<Employee> alEmp = new ArrayList<Employee>();
        for (int j=0; j<40; j++) {
            Employee emp = new Employee();
            assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));

            alEmp.add(emp);
        }
        
        ArrayList<Hub<Employee>> al = new ArrayList<Hub<Employee>>();

        // have objects added to 3 hubs
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            al.add(hub);

            for (Employee emp : alEmp) {
                assertEquals(i, OAObjectHubDelegate.getHubReferenceCount(emp));
                hub.add(emp);
                assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(emp));
            }
        }

        assertTrue(OAObjectHubDelegate.aiReuseWeakRefArray.get() > 0);
        
        
        Employee empx = alEmp.get(0);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        for (Employee emp : alEmp) {
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        }        

        
        // add another, check it, and then remove it and check
        empx = new Employee();
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = al.get(i);
            hub.add(empx);
            assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(empx));
        }
        assertTrue(OAObjectHubDelegate.aiReuseWeakRef.get() > 0);
        
        WeakReference<Hub<?>>[] xrefs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        assertEquals(3, xrefs.length);
        assertEquals(refs, xrefs);
        assertEquals(refs.length, xrefs.length);
        
        
        for (int i=0; i<refs.length; i++) {
            WeakReference wf = refs[i];
            WeakReference xwf = xrefs[i];
            assertEquals(wf.get(), xwf.get());
        }
        
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = al.get(i);
            assertEquals(3-i, OAObjectHubDelegate.getHubReferenceCount(empx));
            hub.remove(empx);
            assertEquals(2-i, OAObjectHubDelegate.getHubReferenceCount(empx));
            
            xrefs = OAObjectHubDelegate.getHubReferencesNoCopy(alEmp.get(0));
            for (Employee emp : alEmp) {
                if (emp == alEmp.get(0)) continue;
                assertEquals(xrefs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
            }
        }


        for (Hub h : al) {
            assertEquals(40, h.getSize());
        }
        
        
        xrefs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        assertNull(xrefs);
        
        // now add more (over 3) so that it will not share
        
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            al.add(hub);

            for (Employee emp : alEmp) {
                assertEquals(i+3, OAObjectHubDelegate.getHubReferenceCount(emp));
                hub.add(emp);
                assertEquals(i+4, OAObjectHubDelegate.getHubReferenceCount(emp));

                refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
                for (Employee empz : alEmp) {
                    if (emp == empz) continue;
                    assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(empz));
                }        
            }
        }

        for (Employee empz : alEmp) {
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(empz);
            int x = 6;
            assertEquals(x, hubs.length);
            
            for (Hub h : hubs) {
                h.remove(empz);
                assertEquals(--x, OAObjectHubDelegate.getHubReferenceCount(empz));
                for (Employee empk : alEmp) {
                    if (empk == empz) continue;
                    refs = OAObjectHubDelegate.getHubReferencesNoCopy(empz);
                    if (x > 0) assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(empk));
                }        
            }
        }
    }

    @Test
    public void testLarge() {
        OAObjectHubDelegate.ShowWarnings = false;
        Employee emp = new Employee();
        assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
        ArrayList<Hub<Employee>> al = new ArrayList<Hub<Employee>>();

        int max = 5000;
        for (int i=0; i<max; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            al.add(hub);
            hub.add(emp);

            assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(emp));
            
            assertTrue(OAObjectHubDelegate.getHubReferencesNoCopy(emp).length <= (i + 1 + (i*.10)));
        }

        for (Hub hub : al) {
            hub.remove(emp);

            assertEquals(--max, OAObjectHubDelegate.getHubReferenceCount(emp));
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
            if (refs == null) continue;
            
            int x = refs.length;
            if (x >= (max + 1 + (max*.5))) {
                int xx = 4;
                xx++;
            }
            assertTrue(x <= 10 || x <= (max + 1 + (max*.5)));
        }
    }
}

