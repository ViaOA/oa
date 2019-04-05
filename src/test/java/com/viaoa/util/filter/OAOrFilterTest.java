package com.viaoa.util.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

import test.xice.tsac3.model.oa.*;

public class OAOrFilterTest extends OAUnitTest {

    @Test
    public void test() {
        // String query = "id IN (1,2)";
        String query = "(id = 1 || id = 2)";
        OAQueryFilter<Server> f = new OAQueryFilter<>(Server.class, query);
        Server server = new Server();
        server.setId(5);
        f.isUsed(server);
    }
    
}
