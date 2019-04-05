package test.xice.tsac.delegate.oa;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.*;

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
