package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.vetjobs.VetUser;

public class HubCopyTest extends OAUnitTest {

    @Test
    public void test() {

        // create 2 Hubs
        
        Hub h1 = new Hub(VetUser.class);
        for (int i=0; i<10; i++) h1.add(createVetUser());
        
        Hub h2 = new Hub(VetUser.class);
        for (int i=0; i<10; i++) h2.add(createVetUser());

        Hub h3 = new Hub(VetUser.class);
        HubFilter hf = new HubFilter(h2,h3) {
            @Override
            public boolean isUsed(Object object) {
                return true;
            }
        };
        
        
        // create a Main hub that will share h1 or h2
        Hub hubMain = new Hub(VetUser.class);
        
        // create a copyHub that will have the same objects as main
        Hub hubCopy = new Hub(VetUser.class);
        
        // have the copy share the same object as hubMain, which is sharing same AO as h1 or h2
        HubCopy hc = new HubCopy(hubMain, hubCopy, true);
        if (hubCopy.getSize() != 0) p("error 1");
        
        hubMain.setSharedHub(h1, true);
        if (!verifyCopy(hubMain, hubCopy)) p("error 2.1");
    
        if (hubCopy.getSize() != 10) p("error 2");
        
        if (hubCopy.getAO() != null) p("error 3");

        h1.setPos(3);

        if (hubCopy.getAO() != h1.getAO()) p("error 4");

        hubCopy.setPos(8);
        VetUser vu = (VetUser) hubCopy.getAO();
        if (hubCopy.getAO() != h1.getAO()) p("error 5");
        
        
        // use filtered hub
        hubMain.setSharedHub(h3, true);
        
        if (!verifyCopy(hubMain, hubCopy)) p("error 5.1");
        if (hubCopy.getAO() != null) p("error 5.2");

        VetUser vu3 = (VetUser) h3.setPos(4);
        if (hubCopy.getAO() != vu3) p("error 5.2.1, "+hubCopy.getAO());
        

        // go back to h1, make sure that AO is being used
        hubMain.setSharedHub(h1, true);
        
        if (h1.getAO() != vu) p("error 5.3");
        if (hubMain.getAO() != vu) p("error 5.4");
        if (!verifyCopy(hubMain, hubCopy)) p("error 5.5");
        
        
        if (hubMain.getAO() != vu) p("error 6, "+hubCopy.getAO());
        
        if (hubCopy.getAO() != vu) p("error 6.1, "+hubCopy.getAO());
        if (hubCopy.getAO() != h1.getAO()) p("error 6.2");
    
    
    
    
    }
    
    void p(String msg) {
        System.out.println("error ===> "+msg);
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

    void display(Hub<VetUser> h) {
        for (VetUser vu : h) {
            System.out.println("==> "+vu.getId());
        }
        VetUser vu = h.getAO();
        System.out.println("AO ==> " + (vu==null?"null":vu.getId()) );
    }

    public static void main(String[] args) {
        HubCopyTest test = new HubCopyTest();
        test.test();
        System.out.println("done - goal is perfection :)");
    }
    
}
