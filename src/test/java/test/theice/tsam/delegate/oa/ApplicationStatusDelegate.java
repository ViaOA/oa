package test.theice.tsam.delegate.oa;

import java.awt.Color;

import test.theice.tsam.delegate.ModelDelegate;
import test.theice.tsam.model.oa.ApplicationStatus;

public class ApplicationStatusDelegate {

    
    public static ApplicationStatus getApplicationStatus(int type) {
        ApplicationStatus ss = ModelDelegate.getApplicationStatuses().find(ApplicationStatus.PROPERTY_Type, type);
        return ss;
    }
 
    public static void setDefaultColors() {
        int x = ApplicationStatus.hubType.getSize();
        for (ApplicationStatus ss : ModelDelegate.getApplicationStatuses()) {
            ss.getColor();
        }
    }
    
}
