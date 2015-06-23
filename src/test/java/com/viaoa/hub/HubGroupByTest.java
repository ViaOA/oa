package com.viaoa.hub;

import java.nio.charset.Charset;
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

    // @Test
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
    
    
    // @Test
    public void Test2() {
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

        hubApplication.getAt(0).getApplicationGroups().clear();

        assertEquals(0, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(0, hubApplicationGroup.getAt(0).getApplications().size()); 
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());


        // Test: add 3
        appGroup.getApplications().add(hubApplication.getAt(0));
        appGroup.getApplications().add(hubApplication.getAt(1));
        appGroup.getApplications().add(hubApplication.getAt(2));
        
        assertEquals(hubCombined.getAt(0).getHub().size(), hubApplication.size()-3);
        assertEquals(3, hubCombined.getAt(1).getHub().size());
        
        // clear
        hubApplication.getAt(0).getApplicationGroups().clear();
        hubApplication.getAt(1).getApplicationGroups().clear();
        hubApplication.getAt(2).getApplicationGroups().clear();

        assertEquals(0, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(0, hubApplicationGroup.getAt(0).getApplications().size()); 
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());
        

        // Test: add 3 again
        appGroup.getApplications().add(hubApplication.getAt(0));
        appGroup.getApplications().add(hubApplication.getAt(1));
        appGroup.getApplications().add(hubApplication.getAt(2));
        
        assertEquals(hubCombined.getAt(0).getHub().size(), hubApplication.size()-3);
        assertEquals(3, hubCombined.getAt(1).getHub().size());
        
        // clear
        appGroup.getApplications().clear();

        assertEquals(0, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(0, hubApplicationGroup.getAt(0).getApplications().size()); 
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());

        // Test: add 3 again, and use remove
        appGroup.getApplications().add(hubApplication.getAt(0));
        appGroup.getApplications().add(hubApplication.getAt(1));
        appGroup.getApplications().add(hubApplication.getAt(2));
        
        assertEquals(hubCombined.getAt(0).getHub().size(), hubApplication.size()-3);
        assertEquals(3, hubCombined.getAt(1).getHub().size());
        
        // clear
        appGroup.getApplications().remove(0);
        appGroup.getApplications().remove(0);
        appGroup.getApplications().remove(0);

        assertEquals(0, hubApplication.getAt(0).getApplicationGroups().size()); 
        assertEquals(0, hubApplicationGroup.getAt(0).getApplications().size()); 
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        assertEquals(hubApplication.size(), hubCombined.getAt(0).getHub().size());
        
        
        int xx = 0;
        xx++;
    }    

    // @Test
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

        pp = ApplicationPP.applicationType().applicationGroups().pp;
        
        Hub<ApplicationGroup> hubApplicationGroup = silo.getApplicationGroups();
        
        HubGroupBy<Application, ApplicationGroup> hgb = new HubGroupBy<Application, ApplicationGroup>(hubApplication, hubApplicationGroup, pp);

        Hub<OAGroupBy<ApplicationGroup, Application>> hubCombined = (Hub<OAGroupBy<ApplicationGroup, Application>>) hgb.getCombinedHub();

        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        
        OAGroupBy gb = hubCombined.getAt(0);
        for (Application app : hubApplication) {
            app.setApplicationType(null);
        }
        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        
        ApplicationType appType = model.getApplicationTypes().getAt(0);
        hubApplication.getAt(0).setApplicationType(appType);
        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        
        
        // set up AppType + AppGroup
        ApplicationGroup appGroup = new ApplicationGroup();
        hubApplicationGroup.add(appGroup);
        appGroup.getApplicationTypes().add(appType);

        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(1).getHub().size());
        assertEquals(4, hubCombined.getAt(0).getHub().size());
        assertNull(hubCombined.getAt(0).getGroupBy());
        
        
        appGroup.getApplicationTypes().remove(appType);
        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
    }    

    @Test
    public void TestSplit2() {
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

        pp = ApplicationPP.applicationType().applicationGroups().pp;
        
        Hub<ApplicationGroup> hubApplicationGroup = silo.getApplicationGroups();
        
        HubGroupBy<Application, ApplicationGroup> hgb = new HubGroupBy<Application, ApplicationGroup>(hubApplication, hubApplicationGroup, pp);

        Hub<OAGroupBy<ApplicationGroup, Application>> hubCombined = (Hub<OAGroupBy<ApplicationGroup, Application>>) hgb.getCombinedHub();

        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        assertNull(hubCombined.getAt(0).getGroupBy());

        
        int max = 10;
        for (int i=0; i<max; i++) {
            ApplicationGroup appGroup = new ApplicationGroup();
            silo.getApplicationGroups().add(appGroup);
            assertEquals(2+i, hubCombined.size());
        }
        
        for (int i=max; i>0; i--) {
            hubApplicationGroup.removeAt(0);
            assertEquals(i, hubCombined.size());
        }
        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        
        for (int i=0; i<max; i++) {
            ApplicationGroup appGroup = new ApplicationGroup();
            silo.getApplicationGroups().add(appGroup);
            assertEquals(2+i, hubCombined.size());
        }
        assertEquals(5, hubCombined.getAt(0).getHub().size());
        hubApplicationGroup.clear();
        assertEquals(0, silo.getApplicationGroups().size());
        assertEquals(1, hubCombined.size());
        assertEquals(5, hubCombined.getAt(0).getHub().size());

        
        hubApplication.clear();
        for (OAGroupBy gb : hubCombined) {
            int x  = gb.getHub().size();
            assertEquals(0, x);
        }
        
        hubApplicationGroup.clear();
        assertEquals(1, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        //qqqqq have the null one removed if hub=0
        
//qqqqqqqqqq        
        ApplicationGroup appGroup = new ApplicationGroup();
        hubApplicationGroup.add(appGroup);
        assertEquals(2, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        Application app = new Application();
        hubApplication.add(app);
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());

        ApplicationType appType = new ApplicationType();
        appGroup.getApplicationTypes().add(appType);
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());

        appGroup.getApplicationTypes().add(appType);
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        app.setApplicationType(appType);
        assertEquals(2, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        assertEquals(1, hubCombined.getAt(1).getHub().size());
        
        app.setApplicationType(null);
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        hubApplication.removeAll();
        //hubApplication.remove(0);
        assertEquals(2, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        hubApplication.add(app);
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());
        
        app.setApplicationType(appType);
        assertEquals(2, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        assertEquals(1, hubCombined.getAt(1).getHub().size());
        
        appGroup.getApplicationTypes().removeAt(0);
        // appGroup.getApplicationTypes().clear();
        assertEquals(1, hubApplicationGroup.size());
        assertEquals(2, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        assertEquals(0, hubCombined.getAt(1).getHub().size());

        hubApplicationGroup.clear();
        assertEquals(0, hubApplicationGroup.size());
        assertEquals(1, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        
        hubApplication.clear();
        assertEquals(1, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());
        
        
        for (int i=0; i<max; i++) {
            appGroup = new ApplicationGroup();
            hubApplicationGroup.add(appGroup);
            assertEquals(i+1, hubApplicationGroup.size());
            assertEquals(i+2, hubCombined.size());
        }        

        hubApplication.add(app);
        assertEquals(max+1, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());

        app.setApplicationType(appType);
        assertEquals(max+1, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        for (OAGroupBy gb : hubCombined) {
            if (gb.getGroupBy() == null) assertEquals(1, gb.getHub().size());
            else assertEquals(0, gb.getHub().size());
        }

        app.setApplicationType(null);
        assertEquals(max+1, hubCombined.size());
        assertEquals(1, hubCombined.getAt(0).getHub().size());
        for (OAGroupBy gb : hubCombined) {
            if (gb.getGroupBy() == null) assertEquals(1, gb.getHub().size());
            else assertEquals(0, gb.getHub().size());
        }
        
        // clear all
        hubApplication.clear();
        hubApplicationGroup.clear();
        assertEquals(1, hubCombined.size());
        assertEquals(0, hubCombined.getAt(0).getHub().size());

                
        for (int i=0; i<max; i++) {
            appGroup = new ApplicationGroup();
            hubApplicationGroup.add(appGroup);
            assertEquals(2+i, hubCombined.size());

            appType = new ApplicationType();
            appGroup.getApplicationTypes().add(appType);
            assertEquals(2+i, hubCombined.size());
            
            app = new Application();
            hubApplication.add(app);
            assertEquals(1, hubCombined.getAt(0).getHub().size());
            
            app.setApplicationType(appType);
            assertEquals(0, hubCombined.getAt(0).getHub().size());
            
            assertEquals(1, hubCombined.getAt(i+1).getHub().size());
        }

        assertEquals(max+1, hubCombined.size());
        for (OAGroupBy gb : hubCombined) {
            if (gb.getGroupBy() == null) assertEquals(0, gb.getHub().size());
            else assertEquals(1, gb.getHub().size());
        }
        
        hubApplication.clear();
        assertEquals(max+1, hubCombined.size());
        for (OAGroupBy gb : hubCombined) {
            if (gb.getGroupBy() == null) assertEquals(0, gb.getHub().size());
            else assertEquals(0, gb.getHub().size());
        }
        
        
        
//qqqqqqqq        
        hubApplication.clear();
        assertEquals(max+1, hubCombined.size());

        for (OAGroupBy gb : hubCombined) {
            int x  = gb.getHub().size();
            assertEquals(0, x);
        }
        
        for (int i=silo.getApplicationGroups().size(); i>0; i--) {
            silo.getApplicationGroups().remove(0);
            assertEquals(i, hubCombined.size());
        }
        
    }    

    
    @Test
    public void TestMultipleHgbInOne() {
        // using:  public HubGroupBy(Hub<OAGroupBy<A, B>> hubCombined, Hub<A> hubA, Hub<B> hubB, String propertyPath)
        
    }

}
