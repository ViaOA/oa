package test.xice.tsac.mrad.model;

import java.io.Serializable;


/**
 * Used by Application to define the current status.
 * 
 * @author vvia
 *
 * @see MRADClient#update(ApplicationStatus) to send the status to the tsac server.
 * @see Application#getApplicationStatus() to get/set Application status
 * @see MRADClient#update(Application) to send the App info to tsac server.
 */
public enum ApplicationStatus {
    Unknown, Started, Waiting, Running, Paused, Stopped 
}
