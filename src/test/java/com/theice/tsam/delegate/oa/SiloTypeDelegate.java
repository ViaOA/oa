package com.theice.tsam.delegate.oa;

import com.theice.tsam.delegate.ModelDelegate;
import com.theice.tsam.model.oa.Environment;
import com.theice.tsam.model.oa.Silo;
import com.theice.tsam.model.oa.SiloType;
import com.viaoa.util.OAString;

public class SiloTypeDelegate {

    public static SiloType getSiloType(int type) {
        for (SiloType st : ModelDelegate.getSiloTypes()) {
            if (st.getType() == type) return st;
        }
        return null;
    }
    
    
    public static SiloType getSiloTypeUsingHostName(String hostName) {
        if (hostName == null) return null;
        
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
        
        return getSiloType(iSiloType);
    }
    
    
}
