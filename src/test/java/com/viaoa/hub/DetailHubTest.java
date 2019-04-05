package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac.DataGenerator;
import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.Model;
import test.xice.tsac.model.oa.*;

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
        
        assertNull(dhEnv.getAO());
		
	}
	
	
	
	
}



