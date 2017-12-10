package com.viaoa.object;

import java.util.ArrayList;
import java.util.Vector;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAFilter;
import com.viaoa.util.filter.OAAndFilter;
import com.viaoa.util.filter.OAEqualFilter;
import com.viaoa.util.filter.OAGreaterFilter;
import com.viaoa.util.filter.OALessFilter;
import com.viaoa.util.filter.OAOrFilter;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;
import test.hifive.model.oa.Program;
import test.hifive.model.oa.propertypath.LocationPP;
import test.hifive.model.oa.propertypath.ProgramPP;
import test.theice.tsam.*;
import test.theice.tsam.model.oa.*;
import test.theice.tsam.model.oa.cs.ServerRoot;
import test.theice.tsam.model.oa.propertypath.*;
import test.theice.tsam.util.DataGenerator;

public class OAFinderTest extends OAUnitTest {
    
    
    @Test
    public void finder1Test() throws Exception {
        ServerRoot root = DataGenerator.getServerRoot();
        
        // a finder without a filter should return all objects
        OAFinder f = new OAFinder();
        ArrayList al = f.find(root.getSites());

        assertEquals(root.getSites().size(), al.size());

        f = new OAFinder() {
            int cnt;
            @Override
            protected boolean isUsed(OAObject obj) {
                assertEquals(true, super.isUsed(obj));
                return (cnt++ == 0);
            }
        };
        al = f.find(root.getSites());
        assertEquals(1, al.size());
    }
        
    @Test
    public void finder2Test() throws Exception {
        ServerRoot root = DataGenerator.getServerRoot();
        
        OAFinder f = new OAFinder();
        ArrayList al;
        
        f = new OAFinder() {
            @Override
            protected boolean isUsed(OAObject obj) {
                assertEquals(true, super.isUsed(obj));
                return false;
            }
        };
        al = f.find(root.getSites());
        assertEquals(0, al.size());

        f = new OAFinder();
        f.addEqualFilter(null, root.getSites().getAt(0));
        al = f.find(root.getSites());
        assertEquals(1, al.size());
        
        int id = root.getSites().getAt(0).getId();
        f.addEqualFilter("id", id+"");
        al = f.find(root.getSites());
        assertEquals(1, al.size());
        
        f.addEqualFilter("id", id);
        al = f.find(root.getSites());
        assertEquals(1, al.size());
        
        reset();
    }

    @Test
    public void findFirstTest() throws Exception {
        ServerRoot root = DataGenerator.getServerRoot();

        String pp = SitePP.environments().silos().servers().pp;

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(pp);
        
        Server server = root.getSites().getAt(0).getEnvironments().getAt(0).getSilos().getAt(0).getServers().getAt(0);
        assertNotNull(server);
        assertEquals(server, finder.findFirst(root.getSites()));
    }

    @Test
    public void maxFoundTest() throws Exception {
        ServerRoot root = DataGenerator.getServerRoot();

        String pp = SitePP.environments().silos().servers().pp;

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(pp);
        finder.setMaxFound(5);
        
        ArrayList<Server> alServer = finder.find(root.getSites());
        
        assertEquals(5, alServer.size());
    }
    
    
    private int cnt;
    @Test
    public void filterTest() throws Exception {
        ServerRoot root = DataGenerator.getServerRoot();

        // set server.cnt
        cnt = 0;
        String pp = SitePP.environments().silos().servers().pp;
        
        OAFinder<Site, Server> f = new OAFinder<Site, Server>(pp) {
            @Override
            protected void onFound(Server obj) {
                obj.setMiscCnt(cnt++);
            }
        };
        f.find(root.getSites());
        
        final int totalServers = cnt;
        

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(pp);

        ArrayList<Server> alServer;
        
        finder.clearFilters();
        finder.addEqualFilter(Server.P_MiscCnt, 3);
        alServer = finder.find(root.getSites());
        assertEquals(alServer.size(), 1);
        assertEquals(3, alServer.get(0).getMiscCnt());

        finder.clearFilters();
        finder.addLessFilter(Server.P_MiscCnt, 3);
        alServer = finder.find(root.getSites());
        assertTrue(alServer.size() == 3);

        
        finder.clearFilters();
        finder.addLessOrEqualFilter(Server.P_MiscCnt, 5);
        alServer = finder.find(root.getSites());
        assertEquals(alServer.size(), 6);

        finder = new OAFinder<Site, Server>(pp);
        finder.addEqualFilter(Server.P_MiscCnt, 1);
        Server server = finder.findFirst(root.getSites());
        assertNotNull(server);
        assertEquals(server.getMiscCnt(), 1);
        
        Site site = root.getSites().getAt(0);
        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.silo().environment().site().name(),  site.getName());
        server = finder.findFirst(root.getSites());
        assertNotNull(server);
    
        finder = new OAFinder<Site, Server>(pp);
        OAFilter f1 = new OAEqualFilter(Server.P_MiscCnt, 4);
        OAFilter f2 = new OAEqualFilter(Server.P_MiscCnt, 3);
        
        finder.addFilter(new OAOrFilter(f1, f2));
        
        alServer = finder.find(root.getSites());
        assertEquals(2, alServer.size());
        
        
        finder = new OAFinder<Site, Server>(pp);
        f1 = new OAGreaterFilter(Server.P_MiscCnt, 2);
        f2 = new OALessFilter(Server.P_MiscCnt, 5);
        
        finder.addFilter(new OAAndFilter(f1, f2));
        
        alServer = finder.find(root.getSites());
        assertEquals(2, alServer.size());
    }
    
    @Test
    public void recursiveFinderTest() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        emp.setLastName("xxx");
        
        OAFinder<Program, Employee> finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        finder.addEqualFilter(Employee.P_LastName, "xxx");
        Employee empx = finder.findFirst(ModelDelegate.getPrograms());
        assertEquals(emp, empx);
        
        emp.setLastName("");
        empx = finder.findFirst(ModelDelegate.getPrograms());
        assertNull(empx);
        
        reset();
    }
    @Test
    public void recursiveFinderTest2() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0); 
        emp.setLastName("xxx");
        
        OAFinder<Location, Employee> finder = new OAFinder<Location, Employee>(LocationPP.employees().pp);
        finder.addEqualFilter(Employee.P_LastName, "xxx");
        Employee empx = finder.findFirst(ModelDelegate.getPrograms().getAt(0).getLocations());
        assertEquals(emp, empx);
        
        emp.setLastName("");
        empx = finder.findFirst(ModelDelegate.getPrograms().getAt(0).getLocations());
        assertNull(empx);
        
        reset();
    }
    @Test
    public void recursiveFinderTest3() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        emp.setLastName("xxx");
        
        OAFinder<Location, Employee> finder = new OAFinder<Location, Employee>(LocationPP.employees().pp);
        finder.addEqualFilter(Employee.P_LastName, "xxx");
        Employee empx = finder.findFirst(ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0));
        assertEquals(empx, emp);
        emp.setLastName("");
        reset();
    }


    @Test
    public void recursiveFinderTest4() {
        init();

        ModelDelegate.getPrograms().clear();
        
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        String pp = "location.program.locations.employees";
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        emp.setLastName("xxx");

        OAFinder<Employee, Employee> finder = new OAFinder<Employee, Employee>(pp);
        finder.addEqualFilter(Employee.P_LastName, "xxx");
        
        Employee empx = finder.findFirst(emp);
        assertEquals(emp, empx);
        
        
        for (int i=1; i < data.empIdCnt; i++) {
            System.out.print(" "+i);
            if (i % 40 == 0) System.out.println("");
            OAFinder<Program, Employee> f = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
            f.addEqualFilter(Employee.P_EmployeeCode, i+"");
            
            Hub<Program> hx = ModelDelegate.getPrograms();
            
            ArrayList<Employee> al = f.find(ModelDelegate.getPrograms());
            boolean b = (al != null && al.size() == 1);
            if (!b) {
//qqqqqqqqqqqqqqqqq
                al = f.find(ModelDelegate.getPrograms());
                al = f.find(ModelDelegate.getPrograms());
                al = f.find(ModelDelegate.getPrograms());
                b = (al != null && al.size() == 1);
            }
            assertTrue(b);
        }
        
        
        reset();
    }

    public final void testGetEmployee() {
        String empCode = "123C";
        String empCodeLower = "123c";
        String empCode2 = 123+"c";
        Program p = new Program();
        Location l = new Location();
        Location l2 = new Location();
        l.setProgram(p);
        l2.setProgram(p);
        l2.setParentLocation(l);
        Employee man1 = new Employee();
        Employee emp1 = new Employee();
        emp1.setLocation(l2);
        man1.setLocation(l2);
        emp1.setParentEmployee(man1);
        emp1.setEmployeeCode(empCode);
        man1.setEmployeeCode(empCode2);
        
        
        OAFinder<Program, Employee> finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        assertEquals(2, finder.find(p).size());
        OAFilter<Employee> f1 = new OAEqualFilter(Employee.P_EmployeeCode, empCode);
        finder.addFilter(f1);
        Hub<Employee> x = new Hub<Employee>(Employee.class);
        for(Employee e : finder.find(p)) {
            x.add(e);
        }
        assertEquals(1, x.getSize());
        
        finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        f1 = new OAEqualFilter(Employee.P_EmployeeCode, empCode);
        finder.addFilter(f1);
        Employee emp = finder.findFirst(p);
        assertNotNull(emp);
        
        finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        f1 = new OAEqualFilter(Employee.P_EmployeeCode, empCodeLower);
        finder.addFilter(f1);
        emp = finder.findFirst(p);
        assertNull(emp);
        
        finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        f1 = new OAEqualFilter(Employee.P_EmployeeCode, empCodeLower);
        ((OAEqualFilter)f1).setIgnoreCase(true);
        finder.addFilter(f1);
        emp = finder.findFirst(p);
        assertNotNull(emp);
    }

    public void test() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        Vector<String> vec = OAObjectCacheDelegate.getInfo();
        for (String s : vec) {
            System.out.println(s);
        }
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        emp.setLastName("xxx");
        
        for (int i=0; i<10; i++) {
            long ts1 = System.currentTimeMillis();
            
            OAFinder<Location, Employee> finder = new OAFinder<Location, Employee>(LocationPP.employees().pp);
            finder.addEqualFilter(Employee.P_LastName, "xxx");
            finder.find(ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0));
            
            long ts2 = System.currentTimeMillis();
            System.out.println(i+" "+(ts2-ts1));
        }
    }

    @Test
    public void test2() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

        Hub<Employee> h = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getEmployees();

        OAFinder<Employee, Employee> finder = new OAFinder<>(h, null);
        finder.setAllowRecursiveRoot(false);
        ArrayList<Employee> al = finder.find();
        
        assertEquals(h.size(), al.size());
    }
    
    
    public static void main(String[] args) {
        OAFinderTest test = new OAFinderTest();
        test.test2();
    }
}
