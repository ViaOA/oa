package test.xice.tsam.delegate.oa;

import java.util.HashMap;

import com.viaoa.util.OAString;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.EnvironmentTypePP;

public class EnvironmentTypeDelegate {

    public static EnvironmentType getEnvironmentType(Environment env) {
        if (env == null) return null;
        EnvironmentType et = env.getEnvironmentType();
        if (et != null) return et;
        
        String abbrevName = env.getAbbrevName();
        if (OAString.isEmpty(abbrevName)) return null;
        
        String nameToFind = abbrevName;
        if (abbrevName.equalsIgnoreCase("pr")) {
            Site site = env.getSite();
            if (site == null) return null;
            nameToFind = site.getAbbrevName();
        }

        String result = null;
        int iResult = 0;
        for (int i=0; ;i++) {
            String s = EnvironmentType.hubType.getAt(i);
            if (s == null) break;
            if (nameToFind.indexOf(s.toLowerCase()) >= 0) {
                if (result == null || (s.length() > result.length())) {
                    result = s;
                    iResult = i;
                }
            }
        }
        
        et = ModelDelegate.getEnvironmentTypes().find(EnvironmentTypePP.type(), iResult);
        return et;
    }


    private static final HashMap<String, String> hmTEEnvName = new HashMap<String, String>();  
    static {
        hmTEEnvName.put("am", "automation");
        hmTEEnvName.put("am2", "automation2");
        hmTEEnvName.put("at", "prod");
        hmTEEnvName.put("at-mg", "prod");
        hmTEEnvName.put("csi1", "csi1");
        hmTEEnvName.put("dv1", "dev1");
        hmTEEnvName.put("dv2", "dev2");
        hmTEEnvName.put("dv3", "dev3");
        hmTEEnvName.put("dv4", "dev4");
        hmTEEnvName.put("dv5", "dev5");
        hmTEEnvName.put("ft2", "ft2");
        hmTEEnvName.put("lt", "loadtest");
        hmTEEnvName.put("ord", "prod");
        hmTEEnvName.put("ord-mg", "prod");
        hmTEEnvName.put("mrtest", "mrtest");
        hmTEEnvName.put("ap", "pdk-api");
        hmTEEnvName.put("dm", "pdk-dm");
        hmTEEnvName.put("ft", "pdk-ft");
        hmTEEnvName.put("ft3", "pdk-ft3");
        hmTEEnvName.put("pf", "pdk-pf");
        hmTEEnvName.put("pl", "platts");
        hmTEEnvName.put("pl2", "platts2");
        hmTEEnvName.put("ps", "pdk-ps");
        hmTEEnvName.put("st", "pdk-st");
        hmTEEnvName.put("ut1", "pdk-ut1");
        hmTEEnvName.put("pmt", "ProductMgmt");
        hmTEEnvName.put("sbs", "");
        hmTEEnvName.put("sb7", "sandbox7");
        hmTEEnvName.put("techtest", "techtest");
        hmTEEnvName.put("tt", "techtest");
        hmTEEnvName.put("tr1", "trs1");
        hmTEEnvName.put("tr2", "trs2");
        hmTEEnvName.put("tr3", "trs3");
        hmTEEnvName.put("sr1", "pdk-sr1");
        hmTEEnvName.put("pt2", "pdk-pt2");
    }
    
    // see: https://icespace.cpex.com/docs/DOC-76217
    public static String getDefaultTEAbbrevName(EnvironmentType et) {
        if (et == null) return null;
        String s = et.getName();
        if (OAString.isEmpty(s)) return null;
        s = s.toLowerCase();
        String teName = hmTEEnvName.get(s);
        return teName;
    }
    

    
    
    private static final HashMap<String, String> hmCEEnvName = new HashMap<String, String>();  
    static {
        hmCEEnvName.put("at", "atl");
        hmCEEnvName.put("am", "automation");
        hmCEEnvName.put("dv2", "dev2");
        hmCEEnvName.put("dv3", "dev3");
        hmCEEnvName.put("dv4", "dev4");
        hmCEEnvName.put("dv5", "dev5");
        hmCEEnvName.put("ft2", "ft2");
        hmCEEnvName.put("lt", "loadtest");
        hmCEEnvName.put("ord", "ord");
        hmCEEnvName.put("ap", "pdk-api");
        hmCEEnvName.put("ft", "pdk-ft");
        hmCEEnvName.put("ft3", "pdk-ft3");
        hmCEEnvName.put("ps", "pdk-ps");
        hmCEEnvName.put("st", "pdk-st");
        hmCEEnvName.put("ut1", "pdk-ut1");
        hmCEEnvName.put("tt", "techtest");
        hmCEEnvName.put("tr1", "trs1");
        hmCEEnvName.put("tr2", "trs2");
        hmCEEnvName.put("tr3", "trs3");
        hmCEEnvName.put("sr1", "pdk-sr1");
        hmCEEnvName.put("pt2", "pdk-pt2");
    }
    
    // see: https://icespace.cpex.com/docs/DOC-76217
    public static String getDefaultCEAbbrevName(EnvironmentType et) {
        if (et == null) return null;
        String s = et.getName();
        if (OAString.isEmpty(s)) return null;
        s = s.toLowerCase();
        String teName = hmCEEnvName.get(s);
        return teName;
    }
    
    
    
}
