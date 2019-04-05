package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectEditQuery.Type;

import test.xice.tsac3.model.oa.*;

public class OAEditMessageTest extends OAUnitTest {

    @Test
    public void test() {
        Server server = new Server();
        OAObjectEditQuery em = new OAObjectEditQuery(Type.AllowAdd);
        
        // assertEquals("test", em.getMessage());
    }
    
}
