package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectHubDelegate;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;

public class HubDataDelegateTest extends OAUnitTest {

    @Test
    public void test() {
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        for (Program program : ModelDelegate.getPrograms()) {
            Hub<Ecard> hub = program.getEcards();
            for (Ecard ec : hub) {
                //boolean b = OAObjectHubDelegate.isInHub(ec, hub);
                boolean b2 = hub.contains(ec);
                assertTrue(b2);
            }
        }
        
        
        Hub<Ecard> hubEcard = new Hub<>(Ecard.class);
        
        HubFilter filter = new HubFilter(ModelDelegate.getPrograms().getAt(0).getEcards(), hubEcard);
        assertEquals(ModelDelegate.getPrograms().getAt(0).getEcards().size(), hubEcard.size());
        reset();
    }
    
}
