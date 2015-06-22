package com.theice.tsac.delegate.oa;

import com.theice.tsac.delegate.ModelDelegate;
import com.theice.tsac.model.oa.*;

public class OperatingSystemDelegate {

    public static OperatingSystem getOperatingSystem(String osName, boolean bAutoCreate) {
        OperatingSystem os = ModelDelegate.getOperatingSystems().find(OperatingSystem.PROPERTY_Name, osName);
        if (os == null && bAutoCreate) {
            os = new OperatingSystem();
            os.setName(osName);
            ModelDelegate.getOperatingSystems().add(os);
        }
        return os;
    }
    
    public static OperatingSystem getOperatingSystem(int type) {
        OperatingSystem ss = ModelDelegate.getOperatingSystems().find(OperatingSystem.PROPERTY_Type, type);
        return ss;
    }
 
}
