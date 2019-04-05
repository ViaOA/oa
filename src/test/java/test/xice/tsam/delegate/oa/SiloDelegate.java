package test.xice.tsam.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsam.delegate.oa.SiloTypeDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.SiloPP;

public class SiloDelegate {

    public static Silo getSilo(Environment env, String nameFromHostName, boolean bAutoCreate) {
        if (env == null) return null;
        
        SiloType siloType = SiloTypeDelegate.getSiloTypeUsingHostName(nameFromHostName);
        
        for (Silo silo : env.getSilos()) {
            if (silo.getSiloType() == siloType) return silo;
        }
        if (!bAutoCreate) return null;
        
        Silo silo = new Silo();
        silo.setSiloType(siloType);
        env.getSilos().add(silo);
        return silo;
    }
    
    public static Silo getSilo(Environment env, SiloType siloType, boolean bAutoCreate) {
        if (env == null) return null;
        Silo silo = env.getSilos().find(SiloPP.siloType().pp, siloType);
        if (silo != null) return silo;
        
        if (!bAutoCreate) return null;
        
        silo = new Silo();
        silo.setSiloType(siloType);
        env.getSilos().add(silo);
        return silo;
    }    

    /**
     * Finds the environment silo that has a network mask that matches.
     */
    public static Silo getSiloUsingIpAddressMask(Environment env, String ipAddress) {
        if (env == null || ipAddress == null) return null;
        
        for (Silo silo : env.getSilos()) {
            String mask = silo.getNetworkMask();
            if (OAString.isEmpty(mask)) continue;

            // convert the mask to a regex
            mask = OAString.convert(mask, "255", "\\d{1,3}");
            mask = OAString.convert(mask, ".", "\\.");
            if (ipAddress.matches(mask)) return silo; 
        }
        return null;      
    }

    public static boolean isProduction(Silo silo) {
        if (silo == null) return false;
        
        Environment env = silo.getEnvironment();
        if (env != null) {
            Site site = env.getSite();
            if (site != null) {
                if (site.getProduction()) return true;
            }
        }
        // check some of the server host names
        for (Server server : silo.getServers()) {
            String s = server.getHostName();
            if (s == null) continue;
            if (s.indexOf("-pr-") >= 0) return true;
        }
        return false;
    }
    
}
