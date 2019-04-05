package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.*;
import test.xice.tsac.model.oa.propertypath.SiloPP;

public class SiloDelegate {

    public static Silo getSilo(Environment env, SiloType siloType) {
        if (env == null) return null;
        Silo silo = env.getSilos().find(SiloPP.siloType().pp, siloType);
        return silo;
    }    

    private static Silo siloDefault;
    
    
    /**
     * find the default Silo that this instance of tsac is running under.
     * This will use the environment short name.
     */
    public static Silo getDefault(Environment env) {
        if (siloDefault != null) return siloDefault;

        return siloDefault;        
    }
    
    /**
     * The server hostname's at ICE have the silo and app type in the third part of the name "site-env-[silo]&type-instance 
     */
    public static Silo getSiloUsingHostName(Environment env, String hostName, boolean bAutoCreate) {
        if (hostName == null) return null;
        
        if (env == null) {
            env = EnvironmentDelegate.getEnvironment(hostName, false);
            if (env == null) return null;
        }
        
        String name = OAString.field(hostName, "-", 3);
        if (name == null) {
            if (hostName.toLowerCase().endsWith(".intcx.net")) {  // dns name
                name = OAString.field(hostName, ".", 1);
            }
            else {
                if (hostName.indexOf(".") < 0) name = hostName;
            }
        }
        if (name == null) name = "LOCAL";

        
        int iSiloType;
        if (name.toUpperCase().startsWith("EX")) iSiloType = SiloType.TYPE_Endex;
        else if (name.toUpperCase().startsWith("LX")) iSiloType = SiloType.TYPE_Liffe;
        else iSiloType = SiloType.TYPE_ICE;
        
        SiloType siloType = SiloTypeDelegate.getSiloType(iSiloType);
        
        for (Silo silo : env.getSilos()) {
            SiloType st = silo.getSiloType();
            if (st == siloType) return silo;
        }
        if (!bAutoCreate) return null;
        
        Silo silo = new Silo();
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
    
}
