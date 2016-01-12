package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class OAArrayTest extends OAUnitTest {

    @Test
    public void test() {
        
        Server server = new Server();
        Server[] servers = (Server[]) OAArray.add(Server.class, (Server[]) null, server);
        
        
        assertNotNull(servers);
        assertEquals(1, servers.length);
        assertEquals(server, servers[0]);

        servers = null;
        for (int i=0; i<100; i++) {
            server = new Server();
            servers = (Server[]) OAArray.add(Server.class, servers, server);
            assertEquals(i+1, servers.length);
            assertEquals(server, servers[i]);
        }

        int x = servers.length;
        for (int i=0; i<100; i++) {
            server = new Server();
            servers = (Server[]) OAArray.insert(Server.class, servers, server, 0);
            assertEquals(i+x+1, servers.length);
            assertEquals(server, servers[0]);
        }
        
        x = servers.length;
        for (int i=0; i<x; i++) {
            int pos = (int) (Math.random() * servers.length);
            server = servers[pos];
            servers = (Server[]) OAArray.removeAt(Server.class, servers, pos);
            assertEquals(x - (i+1), servers.length);
            if (servers.length > pos) assertNotEquals(server, servers[pos]);
        }
        
        // random
        for (int i=0; i<25000; i++) {
            x = servers.length;

            int pos;
            double d = Math.random();

            if (x > 1000 || (x > 0 && d < .33)) {
                pos = (int) (Math.random() * x);
                server = servers[pos];
                servers = (Server[]) OAArray.removeAt(Server.class, servers, pos);
                assertEquals(x - 1, servers.length);
                if (servers.length > pos) assertNotEquals(server, servers[pos]);
            }
            else {
                if (d < .66) {
                    server = new Server();
                    servers = (Server[]) OAArray.add(Server.class, servers, server);
                    assertEquals(x+1, servers.length);
                    assertEquals(server, servers[x]);
                }
                else {
                    pos = (int) (Math.random() * x);
                    server = new Server();
                    servers = (Server[]) OAArray.insert(Server.class, servers, server, x);
                    assertEquals(x+1, servers.length);
                    assertEquals(server, servers[x]);
                }
            }            
            
        }
        
        
        
    }
    
}
