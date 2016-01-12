package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.theicetest.tsactest.model.oa.*;
import com.theicetest.tsactest.model.oa.propertypath.*;
import com.tmgsc.hifivetest.model.oa.Employee;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;

/*
qqqqqq test with multiple hubs
   set weakRef=null
   set weakRef.get=null
*/
public class OAObjectHubDelegateTest extends OAUnitTest {

    @Test
    public void testA() {
        reset();
        
        final Employee emp = new Employee();
        assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        Hub<Employee> hub = new Hub<Employee>(Employee.class);
        hub.add(emp);

        assertEquals(1, OAObjectHubDelegate.getHubReferenceCount(emp));

        hub.add(emp);
        hub.add(emp);
        assertEquals(1, hub.getSize());
        assertEquals(1, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        
        Hub<Employee> hub2 = new Hub<Employee>(Employee.class);
        hub2.add(emp);
        assertEquals(2, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertEquals(2, refs.length);
        refs[0] = new WeakReference(null);
        
        hub.remove(emp);
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertEquals(hub2, refs[0].get());
        assertEquals(1, refs.length);
    }

    @Test
    public void testB() {
        reset();

        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        // add each emp to 3 hubs, all emp.weakHubs should be the same
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
            }            
        }
        for (int j=0; j<3; j++) {
            assertEquals(10, hubs[j].getSize());
        }
        for (int j=3; j<10; j++) {
            assertEquals(0, hubs[j].getSize());
        }

        // remove an emp from one hub, all other emp.weakHubs should stay the same
        hubs[2].remove(emps[0]);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        assertEquals(2, refs.length);
        
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[1]);
        for (int i=1; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }
        
        hubs[2].add(emps[0]);
        WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        assertEquals(3, refs2.length);
        assertEquals(refs, refs2);
    }
    
    @Test
    public void testC() {
        reset();

        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        // add each emp to 3 hubs, all emp.weakHubs should be the same
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
            }            
        }
        for (int j=0; j<3; j++) {
            assertEquals(10, hubs[j].getSize());
        }
        for (int j=3; j<10; j++) {
            assertEquals(0, hubs[j].getSize());
        }

        // remove an emp from one hub, all other emp.weakHubs should stay the same
        hubs[0].remove(emps[0]);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        assertEquals(2, refs.length);
        
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[1]);
        for (int i=1; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }
        
        hubs[0].add(emps[0]);
        WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        assertEquals(3, refs2.length);
        assertNotEquals(refs, refs2);

        for (int i=1; i<emps.length; i++) {
            refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }
    }
    

    @Test
    public void testE() {
        reset();
        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
                assertEquals(3, refs.length);
            }            
        }

        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            refs[2] = new WeakReference(null);
        }
        
        for (int i=0; i<emps.length; i++) {
            hubs[2].remove(emps[i]);
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            assertEquals(2, refs.length);
        }
    }

    @Test
    public void testF() {
        reset();
        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
                assertEquals(3, refs.length);
            }            
        }

        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            refs[1] = new WeakReference(null);
        }
        
        for (int i=0; i<emps.length; i++) {
            hubs[1].remove(emps[i]);
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            assertEquals(2, refs.length);
        }
    }

    @Test
    public void testG() {
        reset();
        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
                assertEquals(3, refs.length);
            }            
        }

        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            refs[1] = new WeakReference(null);
        }
        
        for (int i=0; i<emps.length; i++) {
            hubs[0].remove(emps[i]);
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(2, refs.length);
        }
    }
    @Test
    public void testH() {
        reset();
        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
                assertEquals(3, refs.length);
            }            
        }

        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            refs[1] = new WeakReference(null);
        }
        
        for (int i=0; i<emps.length; i++) {
            hubs[2].remove(emps[i]);
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            assertEquals(1, refs.length);
        }
    }
    
    @Test
    public void testI() {
        reset();
        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<hubs.length; j++) {
                hubs[j].add(emps[i]);
            }
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(10, refs.length);
        }
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        for (int i=1; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertNotEquals(refs, refs2);
            
            for (int j=0; j<10; j++) {
                assertEquals(refs[j], refs2[j]);  // reused weakReference instance
            }
            for (int j=0; j<10; j++) {
                assertEquals(refs[j].get(), refs2[j].get());  // same hub
            }
        }
        
        // removing
        for (int j=0; j<hubs.length; j++) {
            for (int i=0; i<emps.length; i++) {
                refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
                hubs[j].remove(emps[i]);
                
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
                if (j == 9) {
                    assertEquals(null, refs2);
                    continue;
                }
                
                int cnt = 0;
                for (WeakReference wr : refs2) {
                    if (wr == null) break;
                    cnt++;
                    assertNotEquals(hubs[j], wr.get());
                }
                assertEquals(10-(j+1), cnt);
            }
        }
    }
    
    
    @Test
    public void testD() {
        reset();

        Hub<Employee>[] hubs = new Hub[10];
        for (int i=0; i<hubs.length; i++) hubs[i] = new Hub<Employee>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        // add each emp to 3 hubs, all emp.weakHubs should be the same
        for (int i=0; i<emps.length; i++) {
            for (int j=0; j<3; j++) {
                hubs[j].add(emps[i]);
            }
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=0; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertEquals(refs, refs2);
            }            
        }
        for (int j=0; j<3; j++) {
            assertEquals(10, hubs[j].getSize());
        }
        for (int j=3; j<10; j++) {
            assertEquals(0, hubs[j].getSize());
        }
        
        // add to 4th hub, each emps.weakHubs should be different
        for (int i=0; i<emps.length; i++) {
            for (int j=3; j<4; j++) {
                hubs[j].add(emps[i]);
            }
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
            for (int j=1; j<i; j++) {
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[j]);
                assertNotEquals(refs, refs2);
            }            
        }
    }
    
    
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

