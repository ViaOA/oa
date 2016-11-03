package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.Company;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;
import test.hifive.model.oa.Program;
import test.hifive.model.oa.propertypath.ProgramPP;

public class HubFlattenedTest extends OAUnitTest {

    @Test
    public void testA() {
        Hub<Program> hubProgram = new Hub<Program>();
        Program p = new Program();
        hubProgram.add(p);

        Location l = new Location();
        l.setCode("Aaaaa");
        p.getLocations().add(l);

        Location l2 = new Location();
        l2.setCode("Bbbbb");
        p.getLocations().add(l2);

        l2 = new Location();
        l2.setCode("Ccccc");
        p.getLocations().add(l2);
        
        Location lx = new Location();
        lx.setCode("Ddddd");
        l.getLocations().add(lx);

        lx = new Location();
        lx.setCode("Eeeee");
        l.getLocations().add(lx);
        
        Location lz = new Location();
        lz.setCode("Ffffff");
        lx.getLocations().add(lz);
        
        Hub<Location> hubLoc = hubProgram.getDetailHub(Program.P_Locations);
        Hub<Location> hubFlattened = new Hub<Location>(Location.class);

        HubFlattened<Location> hf = new HubFlattened<Location>(hubLoc, hubFlattened);
        
        assertEquals(0, hubFlattened.getSize());
        hubProgram.setPos(0);
        assertEquals(6, hubFlattened.getSize());
        hubProgram.setPos(-1);
        assertEquals(0, hubFlattened.getSize());

        // a new Loc add to hubFlattened should be added to root
        hubProgram.setPos(0);
        Location locx = new Location();
        locx.setName("NEW ONE");
        hubFlattened.add(locx);
        assertEquals(p, locx.getProgram());
        assertTrue(p.getLocations().contains(locx));
    }
    
    public static void main(String[] args) throws Exception {
        HubFlattenedTest test = new HubFlattenedTest();
        test.testA();
    }
    
}
