package test.xice.tsac.mrad.model;

import java.io.Serializable;


/**
 * Information about the server that an application is running on.
 * @author vvia
 * @see Application#getServer() to get/set
 * @see MRADClient#update(Application) to send the App info to tsac. 
 */
public class Server implements Serializable {
    private static final long serialVersionUID = 1L;
    protected SiloType siloType;
    protected String hostName;
    protected String ipAddress;
    protected String name;

    protected String osArch;
    protected String osName;
    protected String osVersion;

    
    public SiloType getSiloType() {
        return siloType;
    }
    public void setSiloType(SiloType siloType) {
        this.siloType = siloType;
    }
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getOsArch() {
        return osArch;
    }
    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }
    public String getOsName() {
        return osName;
    }
    public void setOsName(String osName) {
        this.osName = osName;
    }
    public String getOsVersion() {
        return osVersion;
    }
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}
