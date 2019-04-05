package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;

import test.vetjobs.VetUser;

public class HubCopyTest extends OAUnitTest {

    @Test
    public void test() {

        Hub hub1 = new Hub(VetUser.class);
        for (int i=0; i<10; i++) hub1.add(createVetUser());
        
        Hub hub2 = new Hub(VetUser.class);
        for (int i=0; i<12; i++) hub2.add(createVetUser());

        Hub hubFiltered = new Hub(VetUser.class);
        HubFilter hf = new HubFilter(hub2, hubFiltered) {
            @Override
            public boolean isUsed(Object object) {
                return true;
            }
        };
        
        
        // create a hub that will share h1 or h2
        Hub hubShared = new Hub(VetUser.class);
        
        // create a copyHub that will have the same objects as hubShared
        Hub hubCopy = new Hub(VetUser.class);
        
        // have the copy share the same object as hubShare, which is sharing same AO as h1 or h2
        HubCopy hc = new HubCopy(hubShared, hubCopy, true);
        assertEquals(hubCopy.getSize(), 0);
        
        hubShared.setSharedHub(hub1, true);
        assertTrue(verifyCopy(hubShared, hubCopy));
    
        assertEquals(hubCopy.getSize(), 10);
        
        assertNull(hubCopy.getAO());

        hub1.setPos(3);

        assertEquals(hubCopy.getAO(), hub1.getAO());

        hubCopy.setPos(8);
        VetUser vu = (VetUser) hubCopy.getAO();
        assertEquals(hub1.getAO(), vu);
        assertEquals(hubShared.getAO(), vu);
        
        
        // use filtered hub
        hubShared.setSharedHub(hubFiltered, true);
        
        assertTrue(verifyCopy(hubShared, hubCopy));
        assertNull(hubCopy.getAO());

        VetUser vu3 = (VetUser) hubFiltered.setPos(4);
        assertEquals(hubCopy.getAO(), vu3);
        assertEquals(hubShared.getAO(), vu3);
        

        // go back to h1, make sure that AO is being used
        
        hubShared.setSharedHub(hub1, true);
        
        assertEquals(hub1.getAO(), vu);
        assertEquals(hubShared.getAO(), vu);
        assertTrue(verifyCopy(hubShared, hubCopy));
        
        
        assertEquals(hubShared.getAO(), vu);
        
        assertEquals(hubCopy.getAO(), vu);
        assertEquals(hubCopy.getAO(), hub1.getAO());
    }
    
    boolean verifyCopy(Hub h, Hub h2) {
        if (h.getSize() != h2.getSize()) return false;
        for (Object obj : h) {
            if (!h2.contains(obj)) return false;
        }
        return true;
    }
    
    int id;
    VetUser createVetUser() {
        VetUser vu = new VetUser();
        id++;
        vu.setId(id);
        return vu;
    }

    public static void main(String[] args) {
        HubCopyTest test = new HubCopyTest();
        test.test();
        System.out.println("done - goal is perfection :)");
    }
    
}
