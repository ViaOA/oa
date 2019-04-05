package test.xice.tsam.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.*;

public class OperatingSystemDelegate {

    public static OperatingSystem getOperatingSystem(String osName, boolean bAutoCreate) {
        if (osName == null) return null;
        OperatingSystem os;
        /*
        os = ModelDelegate.getOperatingSystems().find(OperatingSystem.PROPERTY_Name, osName);
        if (os != null) return os;
        */
        osName = osName.toLowerCase().trim();
        
        if (osName.length() == 0 || osName.indexOf("sunos") >= 0 || osName.indexOf("solar") >= 0) {
            os = getOperatingSystem(OperatingSystem.TYPE_Solaris);
        }
        else if (osName.indexOf("window") >= 0 || osName.indexOf("ms") >= 0) {
            os = getOperatingSystem(OperatingSystem.TYPE_Windows);
        }
        else os = getOperatingSystem(OperatingSystem.TYPE_Linux);
        
        if (os != null) return os;
        
        // qqq not used for now
        
        if (!bAutoCreate) return null;
        
        
        
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
