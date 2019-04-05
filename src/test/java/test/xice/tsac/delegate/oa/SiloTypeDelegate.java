package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.Environment;
import test.xice.tsac.model.oa.Silo;
import test.xice.tsac.model.oa.SiloType;

public class SiloTypeDelegate {

    public static SiloType getSiloType(int type) {
        for (SiloType st : ModelDelegate.getSiloTypes()) {
            if (st.getType() == type) return st;
        }
        return null;
    }
    
    public static SiloType getSiloType(test.xice.tsac.mrad.model.SiloType clientSiloType) {
        int type = SiloType.TYPE_ICE;
        if (clientSiloType != null) {
            if (clientSiloType == test.xice.tsac.mrad.model.SiloType.Endex) type = SiloType.TYPE_Endex;
            else if (clientSiloType == test.xice.tsac.mrad.model.SiloType.Liffe) type = SiloType.TYPE_Liffe;
        }
        return getSiloType(type);
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
