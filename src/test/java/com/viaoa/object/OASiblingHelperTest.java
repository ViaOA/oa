package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.CompanyPP;


public class OASiblingHelperTest extends OAUnitTest {

    @Test
    public void test() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();
        hubCompany.add(company);
        
        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany) {
            @Override
            public void onGetReference(OAObject obj, String prop) {
                ai.incrementAndGet();
                super.onGetReference(obj, prop);
            }
        };
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);

        assertEquals(0, ai.get());
        
        company.getPrograms();
        assertEquals(1, ai.get());
        
        String pp = siblingHelper.getPropertyPath(company, Company.P_Programs, false);
        
        assertEquals(Company.P_Programs, pp);
        
        Employee emp = new Employee();
        EmployeeAward ea = new EmployeeAward();
        ea.setEmployee(emp);
        
        // assertEquals(2, ai.get());
        
        Program prog = new Program();
        company.getPrograms().add(prog);
        // assertEquals(4, ai.get());
        
        Location loc = new Location();
        OAObjectPropertyDelegate.unsafeAddProperty(loc, Location.P_Program, prog);
        loc.getProgram();
        // assertEquals(5, ai.get());
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }

    @Test
    public void test2() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();

        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany) {
            @Override
            public void onGetReference(OAObject obj, String prop) {
                ai.incrementAndGet();
                super.onGetReference(obj, prop);
            }
        };
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);
        
        siblingHelper.add("programs.locations.employees.employeeAwards");
        
        Employee emp = new Employee();
        String pp = siblingHelper.getPropertyPath(emp, "employeeAwards");
        
        assertNotNull(pp);

        siblingHelper.add("programs.locations.employees.parentEmployee");
        
        pp = siblingHelper.getPropertyPath(emp, "parentEmployee");
        assertNotNull(pp);

        int x = ai.get();
        emp.getParentEmployee();
        assertEquals(x+1, ai.get());
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }
    

    @Test
    public void test3() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();

        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany) {
            @Override
            public void onGetReference(OAObject obj, String prop) {
                ai.incrementAndGet();
                super.onGetReference(obj, prop);
            }
        };
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);
        
        Program prog = new Program();
        int x = ai.get();

        String pp = siblingHelper.getPropertyPath(prog, "locations");
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }
    
    @Test
    public void test4() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();

        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany);
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);

        String pp = CompanyPP.programs().calcCharityTotal();
        siblingHelper.add(pp);
        
        Location loc = new Location();
        String s = siblingHelper.getPropertyPath(loc, "employees");
        
        assertEquals("Programs.Locations.Employees", s);
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }
    
    @Test
    public void test5() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();

        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany);
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);

        String pp = CompanyPP.programs().calcCharityTotal();
        siblingHelper.add(pp);
        
        Employee emp = new Employee();
        
        String s = siblingHelper.getPropertyPath(emp, "employeeAwards");
        
        assertEquals("Programs.Locations.Employees.EmployeeAwards", s);
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }
    
    @Test
    public void test6() {
        Hub<Company> hubCompany = new Hub<>(Company.class);
        Company company = new Company();

        final AtomicInteger ai = new AtomicInteger();
        OASiblingHelper<Company> siblingHelper = new OASiblingHelper(hubCompany);
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper);

        String pp = CompanyPP.programs().calcCharityTotal();
        siblingHelper.add(pp);
        
        
        company.getPrograms();
        String s = siblingHelper.getPropertyPath(company, "programs");
        assertEquals("Programs", s);
        
        Program prog = new Program();
        prog.getLocations();
        s = siblingHelper.getPropertyPath(prog, "locations");
        assertEquals("Programs.Locations", s);
        
        Employee emp = new Employee();
        s = siblingHelper.getPropertyPath(emp, "employeeAwards");
        
        assertEquals("Programs.Locations.Employees.EmployeeAwards", s);

        EmployeeAward ea = new EmployeeAward();
        s = siblingHelper.getPropertyPath(ea, ea.P_AddOnProduct);
        assertEquals("Programs.Locations.Employees.EmployeeAwards.AddOnProduct", s);
        
        OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        assertNull(OAThreadLocalDelegate.getSiblingHelpers());
    }

}
