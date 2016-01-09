package com.viaoa;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tmgsc.hifivetest.delegate.ModelDelegate;
import com.tmgsc.hifivetest.model.oa.*;
import com.tmgsc.hifivetest.model.oa.propertypath.ProgramPP;
import com.viaoa.hub.*;
import com.viaoa.object.*;

public class HifiveDataGenerator {

    public static final int MaxSiteLoop = 3;
    public static final int MaxEnviromentLoop = 3;
    public static final int MaxSiloLoop = 3;
    public static final int MaxServerLoop = 5;
    public static final int MaxServerInstallLoop = 2;
    
    public int cntEmployee;
    
    public void createSampleData1() {
        // OAThreadLocalDelegate.setLoadingObject(true); // dont set, since it wont set recursive parents
        
        for (int i=0; i<5; i++) {
            Program program = new Program();
            program.setName("program."+i);
            ModelDelegate.getPrograms().add(program);
            
            for (int i2=0; i2<3; i2++) {
                createLocations(program.getLocations(), 0, 3, 3);
            }
        }        

        OAFinder<Program, Location> f = new OAFinder<Program, Location>(ProgramPP.locations().pp) {
            @Override
            protected void onFound(Location loc) {
                assertNotNull(loc.getProgram());
            }
        };
        f.find(ModelDelegate.getPrograms());
        
        
        for (int i=0; i<5; i++) {
            Catalog catalog = new Catalog();
            catalog.setName("catalog."+i);
            ModelDelegate.getCatalogs().add(catalog);
            for (int i2=0; i2<3; i2++) {
                createSections(catalog, catalog.getSections(), 0, 3);
            }
        }
    }

    private void createSections(final Catalog catalog, Hub<Section> hub, int level, int maxLevels) {
        Section sec = new Section();
        hub.add(sec);
        assertEquals(sec.getCatalog(), catalog);
        
        if (level+1 < maxLevels) {
            createSections(catalog, sec.getSections(), level+1, maxLevels);
        }
        for (int i=0; i<5; i++) {
            Item item = new Item();
            sec.getItems().add(item);
            
        }
    }
    private void createLocations(Hub<Location> hub, int level, int maxLevels, int maxEmpLevels) {
        Location loc = new Location();
        hub.add(loc);
        if (level+1 < maxLevels) {
            createLocations(loc.getLocations(), level+1, maxLevels, maxEmpLevels);
        }
        createEmployees(loc.getEmployees(), 0, maxEmpLevels);
        assertEquals(loc.getEmployees().getAt(0).getLocation(), loc);
    }
    private void createEmployees(Hub<Employee> hub, int level, int maxLevels) {
        Employee emp = new Employee();
        cntEmployee++;
        hub.add(emp);
        if (level+1 < maxLevels) {
            createEmployees(emp.getEmployees(), level+1, maxLevels);
            assertEquals(emp.getEmployees().getAt(0).getParentEmployee(), emp);
        }
        for (int i=0; i<2; i++) {
            createEmployeeAwards(emp.getEmployeeAwards());
        }
    }
    private void createEmployeeAwards(Hub<EmployeeAward> hub) {
        EmployeeAward ea = new EmployeeAward();
        hub.add(ea);
    }
}
