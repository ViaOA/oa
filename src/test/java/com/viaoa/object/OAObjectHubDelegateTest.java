package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import test.hifive.model.oa.Employee;

/*
   test with multiple hubs
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
                WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
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
        int x = OAObjectHubDelegate.aiReuseWeakRefArray.get();
        assertTrue(x > 115);
        
        
        Employee empx = alEmp.get(0);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        for (Employee emp : alEmp) {
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        }        

        x = OAObjectHubDelegate.aiReuseWeakRef.get();
        int x1 = OAObjectHubDelegate.aiReuseWeakRefArray.get();
        // add another, check it, and then remove it and check
        empx = new Employee();
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = al.get(i);
            hub.add(empx);
            assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(empx));
        }
        int x2 = OAObjectHubDelegate.aiReuseWeakRef.get();
        assertTrue(x2 == x+2);

        x = OAObjectHubDelegate.aiReuseWeakRefArray.get();
        assertTrue(x == x1+1);
        
        WeakReference<Hub<?>>[] xrefs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        assertEquals(3, xrefs.length);
        assertEquals(refs, xrefs);
        
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
            x = 6;
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

        int max = 500;
        for (int i=0; i<max; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            al.add(hub);
            hub.add(emp);

            assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(emp));

            WeakReference<Hub<?>>[] wfs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
            int x1 = wfs.length;
            int x2 = (int) (i + (i/10) + 2);
            assertTrue(x1 <= x2);
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


    @Test
    public void test3() {
        reset();

        Hub<Employee> hub = new Hub<>(Employee.class);

        Employee[] emps = new Employee[10];
        for (int i=0; i<emps.length; i++) emps[i] = new Employee();
        
        for (int i=0; i<emps.length; i++) {
            hub.add(emps[i]);
        }

        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emps[0]);
        assertEquals(1, refs.length);
        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }            
        
        Employee emp = emps[0];
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        WeakReference ref = new WeakReference(null);
        refs[0] = ref;
        assertEquals(1, refs.length);
        
        assertEquals(1, refs.length);
        for (int i=0; i<emps.length; i++) {
            WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }            
        
        assertEquals(refs[0], ref);
        
        hub = new Hub<>(Employee.class);
        hub.add(emp);
        
        WeakReference<Hub<?>>[] refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertNotEquals(refs, refs2);
        
        
        for (int i=1; i<emps.length; i++) {
            refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }
        
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        for (int i=1; i<emps.length; i++) {
            hub.add(emps[i]);
            refs2 = OAObjectHubDelegate.getHubReferencesNoCopy(emps[i]);
            assertEquals(refs, refs2);
        }
    }


    @Test
    public void test4() {
        ArrayList<Employee> alEmp = new ArrayList<Employee>();
        for (int j=0; j<40; j++) {
            Employee emp = new Employee();
            assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
            alEmp.add(emp);
        }
        
        Hub<Employee> hub = new Hub<Employee>(Employee.class);
        for (Employee emp : alEmp) {
            assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
            hub.add(emp);
            assertEquals(1, OAObjectHubDelegate.getHubReferenceCount(emp));
        }
        
        Employee empx = alEmp.get(0);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        for (Employee emp : alEmp) {
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        }        

        // set weakRef to null
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        refs[0] = new WeakReference(null);
        
        Hub hubx = new Hub<>(Employee.class);
        hubx.add(empx);
        assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(empx));
        for (Employee emp : alEmp) {
            if (emp == empx) continue;
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
            hubx.add(emp);
            assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
            assertEquals(OAObjectHubDelegate.getHubReferencesNoCopy(emp), OAObjectHubDelegate.getHubReferencesNoCopy(empx));
        }
        
    }        

    @Test
    public void test5() {
        ArrayList<Employee> alEmp = new ArrayList<Employee>();
        for (int j=0; j<40; j++) {
            Employee emp = new Employee();
            assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));
            alEmp.add(emp);
        }
        
        ArrayList<Hub<Employee>> alHub = new ArrayList<Hub<Employee>>();

        // have objects added to 3 hubs
        for (int i=0; i<3; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            alHub.add(hub);

            for (Employee emp : alEmp) {
                assertEquals(i, OAObjectHubDelegate.getHubReferenceCount(emp));
                hub.add(emp);
                assertEquals(i+1, OAObjectHubDelegate.getHubReferenceCount(emp));
            }
        }
        
        Employee empx = alEmp.get(0);
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        for (Employee emp : alEmp) {
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        }        

        // set weakRef to null
        refs = OAObjectHubDelegate.getHubReferencesNoCopy(empx);
        refs[0] = new WeakReference(null);
        
        assertEquals(2, OAObjectHubDelegate.getHubReferenceCount(empx));
        assertEquals(3, refs.length);
        Hub hubx = new Hub<>(Employee.class);
        hubx.add(empx);
        assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(empx));
        assertEquals(3, OAObjectHubDelegate.getHubReferenceCount(empx));

        for (Employee emp : alEmp) {
            if (emp == empx) continue;
            assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
            hubx.add(emp);
            assertEquals(OAObjectHubDelegate.getHubReferencesNoCopy(emp), OAObjectHubDelegate.getHubReferencesNoCopy(empx));
            assertEquals(3, OAObjectHubDelegate.getHubReferenceCount(emp));
        }
    } 
    
    @Test
    public void test6() {
        ArrayList<Hub<Employee>> alHub = new ArrayList<Hub<Employee>>();

        // have objects added to 3 hubs
        for (int i=0; i<40; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            alHub.add(hub);
        }

        Employee emp = new Employee();
        assertEquals(0, OAObjectHubDelegate.getHubReferenceCount(emp));

        int cnt = 0;
        for (Hub<Employee> hub : alHub) {
            hub.add(emp);
            assertEquals(++cnt, OAObjectHubDelegate.getHubReferenceCount(emp));
        }
        assertEquals(40, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        refs[0] = new WeakReference(null);
        assertEquals(39, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        alHub.get(0).remove(emp);
        assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        assertNotNull(refs[0].get());
        assertEquals(39, OAObjectHubDelegate.getHubReferenceCount(emp));
        
        alHub.get(0).add(emp);
        int x = OAObjectHubDelegate.getHubReferenceCount(emp);
        assertEquals(40, x);
        WeakReference<Hub<?>>[] refx = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        
        assertEquals(refs[39].get(), alHub.get(0));
        
        for (int i=0; i<5; i++) {
            refs[i*2] = new WeakReference(null);
        }
        x = OAObjectHubDelegate.getHubReferenceCount(emp);
        assertEquals(35, x);
        refx = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        
        Hub<Employee> hub = new Hub<Employee>(Employee.class);
        hub.add(emp);
        x = OAObjectHubDelegate.getHubReferenceCount(emp);
        assertEquals(36, x);

        refx = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
        assertNotEquals(refs, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        
        alHub.get(3).remove(emp);
        assertNotEquals(refx, OAObjectHubDelegate.getHubReferencesNoCopy(emp));
        x = OAObjectHubDelegate.getHubReferenceCount(emp);
        assertEquals(35, x);
    }
        
    @Test
    public void test7() {
        ArrayList<Hub<Employee>> alHub = new ArrayList<Hub<Employee>>();
        Employee emp = new Employee();
        for (int i=0; i<40; i++) {
            Hub<Employee> hub = new Hub<Employee>(Employee.class);
            alHub.add(hub);
            hub.add(emp);

            int x = OAObjectHubDelegate.getHubReferenceCount(emp);
            assertEquals(i+1, x);
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);
            for (int j=0; j<=i; j++) {
                assertEquals(alHub.get(j), refs[j].get());
            }
        }
        
        for (int i=0; i<40; i++) {
            alHub.get(i).remove(emp);

            int x = OAObjectHubDelegate.getHubReferenceCount(emp);
            assertEquals(39-i, x);
            
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferencesNoCopy(emp);

            for (int j=i+1; j<(39-i); j++) {
                Hub h1 = alHub.get(j);
                Hub h2 = refs[j].get();
                assertEquals(h1, h2);
            }
        }
        
    }
}





