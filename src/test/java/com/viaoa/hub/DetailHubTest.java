package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac.DataGenerator;
import test.theice.tsac.delegate.ModelDelegate;
import test.theice.tsac.model.Model;
import test.theice.tsac.model.oa.*;

public class DetailHubTest extends OAUnitTest {

	@Test
    public void testDetailHub() {
        reset();

        Model model = new Model();
        
        DataGenerator dg = new DataGenerator(model);
        
        Hub<Site> hubSite = ModelDelegate.getSites();
        hubSite.setAO(null);
        assertNull(hubSite.getAO());

        DetailHub<Environment> dhEnv = new DetailHub(hubSite, Site.P_Environments);
        assertNull(dhEnv.getAO());
        
        DetailHub<Silo> dhSilo = new DetailHub(dhEnv, Environment.P_Silos);
        assertNull(dhSilo.getAO());

        hubSite.setPos(0);
        
//qqqqqqqqqqqqq gen sample data
        
//        assertNotNull(hubSite.getAO());
        
        assertNotNull(dhEnv.getAO());
		
	}
	
	
	
	
}



