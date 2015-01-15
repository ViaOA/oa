package com.viaoa.object;

import com.theice.tsac.model.oa.Environment;
import com.theice.tsac.model.oa.Server;
import com.theice.tsac.model.oa.ServerInstall;
import com.theice.tsac.model.oa.propertypath.EnvironmentPP;
import com.theice.tsac.model.oa.propertypath.ServerPP;

public class OAFinderTest {

qqqqqqqqq    
    OAFinder finder = new OAFinder<Environment, Server>(EnvironmentPP.silos().servers().pp);
    finder.addEqualFilter(ServerPP.hostName(), hostName);
    finder.addEqualFilter(ServerPP.serverType().packageTypes().packageName(), packageName);
    
    OAFinder finder2 = new OAFinder<Server, ServerInstall>(ServerPP.serverInstalls().pp);
    finder.setFinder(finder2);

}
