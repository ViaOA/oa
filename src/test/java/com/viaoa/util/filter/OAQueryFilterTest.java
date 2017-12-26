package com.viaoa.util.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class OAQueryFilterTest extends OAUnitTest {

    @Test
    public void test() {
        String query = "id IN (1,2 ,3,4)";
        OAQueryFilter<Server> f = new OAQueryFilter<>(Server.class, query);
        Server server = new Server();
        server.setId(5);
        f.isUsed(server);
    }

    @Test
    public void test2() {
        String query = "Id = 1";
        query = "id == 1 && serverFromId = 2";
        query = "(id == 1) && serverFromId = 2";
        query = "id == 1 || id = 2 && serverFromId == 3";
        query = "id == 1 && serverFromId = 2 && id == 3";
        query = "id == 1 && (serverFromId = 2 && id == 3)";

        query = "(id == '1' && (serverFromId = 2 && (id == 3))) || id = 5 && serverFromId = 9 || id in (1,2, 3, 4)";
        
        OAQueryFilter qf = new OAQueryFilter(Server.class, query, null);
        int xx = 4;
        xx++;
    }
    

}
