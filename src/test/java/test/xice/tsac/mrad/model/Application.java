package test.xice.tsac.mrad.model;

import java.io.Serializable;


/**
 * Information about the actual application that is connecting to tsac.
 * @author vvia
 * @see #getServer() get/set the server information and silo
 * @see #getApplicationStatus() get/set the current status of the application
 * @see MRADClient#update(Application) to send the App info to tsac server.
 */
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Server server;

    protected int instanceNumber;
    protected int tradingSystemId;
    
    protected ApplicationType applicationType;
    protected String version;
    protected long startTime;
    protected long readyTime;
    
    protected ApplicationStatus applicationStatus;

    protected long totalMemory;
    protected long freeMemory;
    protected String javaVendor;
    protected String javaVersion;
    
    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }
    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
    public Server getServer() {
        return server;
    }
    public void setServer(Server server) {
        this.server = server;
    }
    public int getInstanceNumber() {
        return instanceNumber;
    }
    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }
    public int getTradingSystemId() {
        return tradingSystemId;
    }
    public void setTradingSystemId(int tradingSystemId) {
        this.tradingSystemId = tradingSystemId;
    }
    public ApplicationType getApplicationType() {
        return applicationType;
    }
    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }
    public long getFreeMemory() {
        return freeMemory;
    }
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }
    public String getJavaVersion() {
        return javaVersion;
    }
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    public String getJavaVendor() {
        return javaVendor;
    }
    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }

    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getReadyTime() {
        return readyTime;
    }
    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }
}
