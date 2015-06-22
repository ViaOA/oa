package com.theice.tsac.delegate.oa;

import com.theice.tsac.model.oa.*;
import com.theice.tsac.model.oa.propertypath.*;
import com.viaoa.object.OAFinder;

public class ApplicationDelegate {
    
    public static Application getApplication(Server server, ApplicationType appType, int instanceNumber, boolean bAutoCreate) {
        if (server == null || appType == null) return null;
        
        OAFinder<Server, Application> finder = new OAFinder<Server, Application>(ServerPP.applications().pp);
        finder.addEqualFilter(ApplicationPP.applicationType().pp, appType);
        finder.addEqualFilter(ApplicationPP.instanceNumber(), instanceNumber);
        
        Application app = finder.findFirst(server);
        
        if (app == null && bAutoCreate) {
            app = new Application();
            app.setApplicationType(appType);
            app.setInstanceNumber(instanceNumber);
            server.getApplications().add(app);
        }
        
        return app;
    }

}




