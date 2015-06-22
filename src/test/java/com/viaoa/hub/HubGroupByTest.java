package com.viaoa.hub;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAGroupBy;
import com.viaoa.util.OAString;
import com.theice.tsac.TsacDataGenerator;
import com.theice.tsac.model.Model;
import com.theice.tsac.model.oa.propertypath.*;
import com.theice.tsac.model.oa.*;

public class HubGroupByTest extends OAUnitTest {

    //@Test
    public void Test() {
        reset();
        Model model = new Model();
        TsacDataGenerator data = new TsacDataGenerator(model);
        data.createSampleData1();

        String pp = SitePP.environments().silos().pp;
        OAFinder<Site, Silo> finder = new OAFinder<Site, Silo>(model.getSites(), pp);
        ArrayList<Silo> al = finder.find();  // all silos

        Hub<Silo> hubSilo = new Hub<Silo>();
        new HubMerger(model.getSites(), hubSilo, pp, true);
        
        pp = SiloPP.siloType().pp;
        HubGroupBy<Silo, SiloType> hgb = new HubGroupBy<Silo, SiloType>(hubSilo, model.getSiloTypes(), pp);
        
        Hub<OAGroupBy<SiloType, Silo>> hubCombined = (Hub<OAGroupBy<SiloType, Silo>>) hgb.getCombinedHub();
        
        Object[] objs = model.getSiloTypes().toArray();
        objs = hubCombined.toArray();
        
        assertEquals(hubCombined.getSize(), model.getSiloTypes().getSize());
        
        Silo silo = new Silo();
        hubSilo.add(silo);
        objs = hubCombined.toArray();
        
        assertEquals(hubCombined.getSize(), model.getSiloTypes().getSize()+1);
        OAGroupBy gb = hubCombined.getAt(3);
    }
    
//qqqqqqqqqqqqqq    
    
    @Test
    public void TestSplit() {
        reset();
        Model model = new Model();
        TsacDataGenerator data = new TsacDataGenerator(model);
        data.createSampleData1();

        String pp = SitePP.environments().silos().pp;
        OAFinder<Site,Silo> f = new OAFinder<Site,Silo>(pp);
        Silo silo = f.findFirst(model.getSites());

        pp = SiloPP.servers().applications().pp;
        Hub<Application> hubApplication = new Hub<Application>();
        HubMerger<Silo,Application> hm  = new HubMerger<Silo, Application>(silo, hubApplication, pp);
        
        
        
        pp = ApplicationPP.applicationGroups().pp;
        
        
        Hub<ApplicationGroup> hubApplicationGroup = silo.getApplicationGroups();
        
        HubGroupBy<Application, ApplicationGroup> hgb = new HubGroupBy<Application, ApplicationGroup>(hubApplication, hubApplicationGroup, pp);

        Hub<OAGroupBy<ApplicationGroup, Application>> hubCombined = (Hub<OAGroupBy<ApplicationGroup, Application>>) hgb.getCombinedHub();
        
        assertEquals(1, hubCombined.size());
        assertEquals(hubCombined.getAt(0).getHub().size(), hubApplication.size());
        
        ApplicationGroup appGroup = new ApplicationGroup();
        silo.getApplicationGroups().add(appGroup);
        
        assertEquals(2, hubCombined.size());
        assertEquals(appGroup, hubCombined.getAt(1).getGroupBy());
        
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        appGroup.getApplications().add(hubApplication.getAt(0));
        
        assertEquals(hubCombined.getAt(0).getHub().size(), hubApplication.size()-1);
        assertEquals(1, hubCombined.getAt(1).getHub().size());
        
        assertEquals(hubCombined.getAt(1).getHub().getAt(0), hubApplication.getAt(0));
        
        assertEquals(1, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(appGroup, hubApplication.getAt(0).getApplicationGroups().getAt(0)); 

  //qqqqqqqqq
        hubApplication.getAt(0).getApplicationGroups().clear();
//        hubApplication.getAt(0).getApplicationGroups().removeAt(0);

        assertEquals(0, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(0, hubApplicationGroup.getAt(0).getApplications().size()); 
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());

        
        
        
        int xx = 0;
        xx++;
        
    }    
    
    @Test
    public void TestMultipleHgbInOne() {
        // using:  public HubGroupBy(Hub<OAGroupBy<A, B>> hubCombined, Hub<A> hubA, Hub<B> hubB, String propertyPath)
        
    }

}
