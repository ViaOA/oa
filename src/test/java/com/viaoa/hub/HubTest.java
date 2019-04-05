package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.model.oa.VInteger;
import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.ProgramPP;

public class HubTest extends OAUnitTest {

    private final static AtomicInteger aiCount = new AtomicInteger();
    
    public static void addEmp(Hub h, boolean b) {
        for (int i = 0; i < 10; i++) {
            Employee e = new Employee();
            aiCount.incrementAndGet();
            h.add(e);
            if (b) addEmp(e.getEmployees(), false);
        }
    }

    public static void main(String[] args) {

        final Hub<String> comboRegionHubMNSR = new Hub<String>();
        final Hub<String> comboJobCodeHubMNSR = new Hub<String>();
        final Hub<String> comboJobNameHubMNSR = new Hub<String>();
        final Hub<String> comboCompanyCodeHubMNSR = new Hub<String>();
        final Hub<String> comboCompanyNameHubMNSR = new Hub<String>();
        final Hub<String> comboCostCenterHubMNSR = new Hub<String>();
        final Hub<String> comboCostCenterDescHubMNSR = new Hub<String>();
        comboRegionHubMNSR.sort("");
        comboJobCodeHubMNSR.sort("");
        comboJobNameHubMNSR.sort("");
        comboCompanyCodeHubMNSR.sort("");
        comboCompanyNameHubMNSR.sort("");
        comboCostCenterHubMNSR.sort("");
        comboCostCenterDescHubMNSR.sort("");
        
        long ms1 = System.currentTimeMillis();

        Program prog = new Program();
        for (int i = 0; ; i++) {
            System.out.println("cnt emps:"+aiCount);
            if (aiCount.get() > 50000) break;
            Location loc = new Location();
            prog.getLocations().add(loc);
            addEmp(loc.getEmployees(), true);
            for (int ii = 0; ii<5; ii++) {
                Location locx = new Location();
                loc.getLocations().add(locx);
                addEmp(loc.getEmployees(), true);
            }
        }

        long ms2 = System.currentTimeMillis();
        System.out.println("create time = " + (ms2 - ms1)+", tot emps="+aiCount);
        aiCount.set(0);

        OAFinder<Program, Employee> finderMNSR = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp) {
            protected void onFound(Employee emp) {
                aiCount.incrementAndGet();
                
                String region = OAString.createRandomString(1, 8, true, false, false);
                
                String jobName = OAString.createRandomString(6, 12);
                String jobCode = OAString.createRandomString(4, 4, true, false, false);
                String companyCode = OAString.createRandomString(3, 4); //emp.getCompanyCode();
                String companyCodeName = OAString.createRandomString(2, 6); //emp.getCompanyCodeName();
                String costCenter = OAString.createRandomString(2, 4); // emp.getCostCenter();
                String costCenterDesc = OAString.createRandomString(6, 22); // emp.getCostCenterDescription();
                
                if (!OAString.isEmpty(jobCode)) {
                    comboJobCodeHubMNSR.add(jobCode);
                }
                comboRegionHubMNSR.add(region);
                
                if (!OAString.isEmpty(jobName)) {
                    comboJobNameHubMNSR.add(jobName);
                }
                if (!OAString.isEmpty(companyCode)) {
                    comboCompanyCodeHubMNSR.add(companyCode);
                }
                if (!OAString.isEmpty(companyCodeName)) {
                    comboCompanyNameHubMNSR.add(companyCodeName);
                }
                if (!OAString.isEmpty(costCenter)) {
                    comboCostCenterHubMNSR.add(costCenter);
                }
                if (!OAString.isEmpty(costCenterDesc)) {
                    comboCostCenterDescHubMNSR.add(costCenterDesc);
                }
            }
        };
        finderMNSR.find(prog);

        long ms3 = System.currentTimeMillis();
        System.out.println("add time = " + (ms3 - ms2) + "   tot emps=" + aiCount);
        System.out.println("comboRegionHubMNSR.size="+comboRegionHubMNSR.size());
        System.out.println("comboJobCodeHubMNSR.size="+comboJobCodeHubMNSR.size());
        System.out.println("comboJobNameHubMNSR.size="+comboJobNameHubMNSR.size());
        System.out.println("comboCompanyCodeHubMNSR.size="+comboCompanyCodeHubMNSR.size());
        System.out.println("comboCompanyNameHubMNSR.size="+comboCompanyNameHubMNSR.size());
        System.out.println("comboCostCenterHubMNSR.size="+comboCostCenterHubMNSR.size());
        System.out.println("comboCostCenterDescHubMNSR.size="+comboCostCenterDescHubMNSR.size());
    }

    // 20170608 test hub.sort that was changed to use quicksort
    public static void mainX(String[] args) {

        long ms1 = System.currentTimeMillis();

        ArrayList<String> al = new ArrayList<>();
        for (int i = 0;; i++) {
            String s = OAString.createRandomString(7, 22);
            if (!al.contains(s)) {
                al.add(s);
                if (al.size() > 5000) break;
            }
        }
        long ms2 = System.currentTimeMillis();
        System.out.println("al time = " + (ms2 - ms1));

        int x = al.size();
        Hub<String> h = new Hub<>(String.class);
        for (int i = 0; i < 100000; i++) {
            String s = al.get(i % x);
            h.add(s);
        }

        Hub<Employee> he = new Hub<>(Employee.class);
        he.sort("lastname");
        for (int i = 0; i < 15000; i++) {
            Employee emp = new Employee();
            emp.setLastName(OAString.createRandomString(3, 14));
            he.add(emp);
        }

        long ms3 = System.currentTimeMillis();
        System.out.println("hub time = " + (ms3 - ms2) + "   hub.size=" + h.size());

    }

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
        for (int i = 0; i < 50; i++) {
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
