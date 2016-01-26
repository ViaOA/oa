package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class OAEditExceptionTest extends OAUnitTest {

    @Test
    public void test() {
        Server server = new Server();
        OAEditException ee = new OAEditException(server, Server.P_Name, "test");
        
        assertEquals("test", ee.getNewValue());
        assertEquals(Server.P_Name, ee.getProperty());
    }
    
}
