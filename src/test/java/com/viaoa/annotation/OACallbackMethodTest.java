package com.viaoa.annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;

import test.hifive.model.oa.*;

public class OACallbackMethodTest extends OAUnitTest {

    @Test
    public void test() {
        
        Employee emp = new Employee();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        ArrayList<String> al = oi.getCallbackPropertNames();
        assertEquals(2, al.size());
        
        OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
        al = oi2.getCallbackPropertNames();
        assertEquals(6, al.size());
        
        
/*        
    P_Location, 
    P_Location+"."+Location.P_Program, 
    P_Location+"."+Location.P_AwardTypes, 
    P_Location+"."+Location.P_Ecards, 
    P_Location+"."+Location.P_InspireCoreValues, 
    P_Location+"."+Location.P_InspireAwardType, 
    P_Location+"."+Location.P_AddOnItems
    P_Program+"."+Program.P_AwardTypes, 
    P_Program+"."+Program.P_Ecards, 
    P_Program+"."+Program.P_InspireCoreValues, 
    P_Program+"."+Program.P_InspireAwardType, 
  
*/        

        emp.cntCallback = 0;
        
        OAObjectInfo oi3 = OAObjectInfoDelegate.getOAObjectInfo(Program.class);
        al = oi3.getCallbackPropertNames();
        assertEquals(4, al.size());

        assertEquals(0, emp.cntCallback);
        
        Location loc = new Location();
        emp.setLocation(loc);
        assertEquals(1, emp.cntCallback);
        AwardType at = new AwardType();
        loc.getAwardTypes().add(at);
        assertEquals(2, emp.cntCallback);
        loc.getAwardTypes().remove(at);
        assertEquals(3, emp.cntCallback);
        
        Program prog = new Program();
        loc.setProgram(prog);
        assertEquals(4, emp.cntCallback);
        
        Ecard ec = new Ecard();
        loc.getEcards().add(ec);
        assertEquals(5, emp.cntCallback);
    }
}
