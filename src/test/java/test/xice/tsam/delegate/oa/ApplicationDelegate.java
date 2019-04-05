package test.xice.tsam.delegate.oa;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;
import com.viaoa.util.filter.OAEqualFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

public class ApplicationDelegate {
    
    public static Application getApplication(Server server, ApplicationType appType, int instanceNumber, boolean bAutoCreate) {
        return getApplication(server, appType, instanceNumber, null, bAutoCreate, true);
    }

    public static Application getApplication(Server server, ApplicationType appType, int instanceNumber, String appName, boolean bAutoCreate) {
        return getApplication(server, appType, instanceNumber, appName, bAutoCreate, true);
    }
    
    public static Application getApplication(Server server, ApplicationType appType, int instanceNumber, String appName, boolean bAutoCreate, boolean bAddToServer) {
        if (server == null || appType == null) return null;
        
        OAFinder<Server, Application> finder = new OAFinder<Server, Application>(ServerPP.applications().pp);
        finder.addFilter(new OAEqualFilter(ApplicationPP.applicationType().pp, appType));
        if (instanceNumber > 0) finder.addFilter(new OAEqualFilter(ApplicationPP.instanceNumber(), instanceNumber));
        if (!OAString.isEmpty(appName)) finder.addFilter(new OAEqualFilter(ApplicationPP.name(), appName));
        
        Application app = finder.findFirst(server);
        
        if (app == null && bAutoCreate) {
            app = new Application();
            app.setApplicationType(appType);
            app.setInstanceNumber(instanceNumber);

            if (bAddToServer) server.getApplications().add(app);
        }
        return app;
    }
}
