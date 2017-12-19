package com.viaoa.object;

import static org.junit.Assert.*;

import org.junit.Test;
import com.viaoa.OAUnitTest;
import test.hifive.model.oa.*;

public class OAHierFinderTest extends OAUnitTest {

    @Test
    public void test() {
        AwardType at = new AwardType();
        Location loc = new Location();
        at.setLocation(loc);

        Location loc2 = new Location();
        loc.setParentLocation(loc2);
        Program prog = new Program();
        loc2.setProgram(prog);
        
        ProgramDocument doc = new ProgramDocument();
        loc2.setAnnouncementDocument(doc);
        
        ProgramDocument doc2 = new ProgramDocument();
        prog.setAnnouncementDocument(doc2);
        ProgramDocument docx = at.getCalcAnnouncementDocument();
        
        assertEquals(doc, docx);
    }
    @Test
    public void test1() {
        AwardType at = new AwardType();
        Location loc = new Location();
        at.setLocation(loc);
        Program prog = new Program();
        loc.setProgram(prog);
        
        ProgramDocument doc = new ProgramDocument();
        loc.setAnnouncementDocument(doc);
        ProgramDocument doc2 = new ProgramDocument();
        prog.setAnnouncementDocument(doc2);
        ProgramDocument docx = at.getCalcAnnouncementDocument();
        assertEquals(doc, docx);
    }
    @Test
    public void test2() {
        AwardType at = new AwardType();
        Location loc = new Location();
        at.setLocation(loc);
        Program prog = new Program();
        loc.setProgram(prog);
        
        ProgramDocument doc = new ProgramDocument();
        prog.setAnnouncementDocument(doc);
        ProgramDocument docx = at.getCalcAnnouncementDocument();
        assertEquals(doc, docx);
    }
    @Test
    public void test3() {
        AwardType at = new AwardType();
        ProgramDocument doc = new ProgramDocument();
        at.setAnnouncementDocument(doc);
        ProgramDocument docx = at.getCalcAnnouncementDocument();
        assertEquals(doc, docx);
    }
    @Test
    public void test4() {
        AwardType at = new AwardType();
        Location loc = new Location();
        at.setLocation(loc);
        ProgramDocument doc = new ProgramDocument();
        loc.setAnnouncementDocument(doc);
        ProgramDocument docx = at.getCalcAnnouncementDocument();
        assertEquals(doc, docx);
    }
    @Test 
    public void test5() {
        Program prog1 = new Program();
        Program prog2 = new Program();
        
        Location loc1 = new Location();
        loc1.setProgram(prog1);
        
        Location loc2 = new Location();
        loc2.setProgram(prog2);
        
        Employee emp1 = new Employee();
        Employee emp2 = new Employee();
        emp1.setLocation(loc1);
        emp2.setLocation(loc2);
        emp2.setParentEmployee(emp1);
        
        Program p = emp2.getProgram();
        assertEquals(prog2, p);
    }
    
}
