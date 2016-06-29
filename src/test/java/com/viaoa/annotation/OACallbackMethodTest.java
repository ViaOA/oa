package com.viaoa.annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.object.OATriggerListener;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OATriggerDelegate;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.LocationPP;
import test.hifive.model.oa.propertypath.ProgramPP;

public class OACallbackMethodTest extends OAUnitTest {

    @Test
    public void test() {
        Employee emp = new Employee();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);

        ArrayList<String> al = oi.getTriggerPropertNames();
        assertTrue(al.size() >= 2);
        
        OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
        al = oi2.getTriggerPropertNames();
        assertTrue(al.size() >= 6);
        
        emp.cntCallback = 0;
        
        OAObjectInfo oi3 = OAObjectInfoDelegate.getOAObjectInfo(Program.class);
        al = oi3.getTriggerPropertNames();
        assertEquals(4, al.size());

        assertEquals(0, emp.cntCallback);
        
        
        Location loc = new Location();
        emp.setLocation(loc);
        assertEquals(1, emp.cntCallback);
        AwardType at = new AwardType();
        loc.getAwardTypes().add(at);
        // callback is ran in bg thread
        for (int i=0; i<3; i++) {
            if (emp.cntCallback == 2) break;
            try {
                Thread.sleep(5);
            }
            catch (Exception e) {
            }
        }
        assertEquals(2, emp.cntCallback);

        AwardType at2 = new AwardType();
        Hub h = new Hub();
        h.add(at2);
        assertEquals(2, emp.cntCallback);
        
        h.remove(at2);
        assertEquals(2, emp.cntCallback);
        
        loc.getAwardTypes().remove(at);
        // callback is ran in bg thread
        for (int i=0; i<3; i++) {
            if (emp.cntCallback == 3) break;
            try {
                Thread.sleep(5);
            }
            catch (Exception e) {
            }
        }
        assertEquals(3, emp.cntCallback);
        
        Program prog = new Program();
        loc.setProgram(prog);
        // callback is ran in bg thread
        for (int i=0; i<3; i++) {
            if (emp.cntCallback == 4) break;
            try {
                Thread.sleep(5);
            }
            catch (Exception e) {
            }
        }
        assertEquals(4, emp.cntCallback);
        
        Ecard ec = new Ecard();
        loc.getEcards().add(ec);
        // callback is ran in bg thread
        for (int i=0; i<3; i++) {
            if (emp.cntCallback == 5) break;
            try {
                Thread.sleep(5);
            }
            catch (Exception e) {
            }
        }
        assertEquals(5, emp.cntCallback);
    }
    
    
    @Test
    public void test2() {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);

        OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
        
        OAObjectInfo oi3 = OAObjectInfoDelegate.getOAObjectInfo(Program.class);
        ArrayList<String> al = oi3.getTriggerPropertNames();
        assertEquals(4, al.size());

        
        OATriggerListener cl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
            }
        };

        
        String[] ss = new String[]{ ProgramPP.locations().employees().employeeAwards().pp };
        
        OATriggerDelegate.createTrigger("xx", Program.class, cl, ss, false, false, false, false);

        al = oi3.getTriggerPropertNames();
        assertEquals(5, al.size());
        
      //qqqqqqqq        
//      oi.removeCallback        
      
    }
}
