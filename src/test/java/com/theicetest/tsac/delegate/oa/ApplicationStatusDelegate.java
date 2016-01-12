package com.theicetest.tsac.delegate.oa;

import java.awt.Color;

import com.theicetest.tsac.delegate.ModelDelegate;
import com.theicetest.tsac.model.oa.ApplicationStatus;

public class ApplicationStatusDelegate {

    public static ApplicationStatus getApplicationStatus(com.theicetest.tsac.mrad.model.ApplicationStatus clientAppStatus) {
        int type = ApplicationStatus.TYPE_UNKNOWN;
        if (clientAppStatus != null) {
            if (clientAppStatus == com.theicetest.tsac.mrad.model.ApplicationStatus.Started) {
                type = ApplicationStatus.TYPE_STARTED;
            }
            else if (clientAppStatus == com.theicetest.tsac.mrad.model.ApplicationStatus.Waiting) {
                type = ApplicationStatus.TYPE_WAITING;
            }
            else if (clientAppStatus == com.theicetest.tsac.mrad.model.ApplicationStatus.Running) {
                type = ApplicationStatus.TYPE_RUNNING;
            }
            else if (clientAppStatus == com.theicetest.tsac.mrad.model.ApplicationStatus.Paused) {
                type = ApplicationStatus.TYPE_SUSPENDED;
            }
            else if (clientAppStatus == com.theicetest.tsac.mrad.model.ApplicationStatus.Stopped) {
                type = ApplicationStatus.TYPE_STOPPED;
            }
        }
        ApplicationStatus as = getApplicationStatus(type);
        return as;
    }
    
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
