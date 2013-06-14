package com.viaoa.sync.model;

import java.io.Serializable;

import com.viaoa.util.OADateTime;

/**
 * Information about a single instance of a Server.
 */
public class ServerInfo implements Serializable{
    private static final long serialVersionUID = 1L;
     
    /** created time */
    protected OADateTime created;
    
    /** server information */
    protected String hostName;
    protected String ipAddress;
    protected String version;
    protected boolean discoveryEnabled;
    
    /** flag to know when the start method was called. */
    private volatile boolean started;

    /** flag to know if server has been suspended. */
    private volatile boolean suspended;

    
    public ServerInfo() {
    }

    
    public OADateTime getCreated() {
        return created;
    }
    public void setCreated(OADateTime newValue) {
        this.created = newValue;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String newValue) {
        this.ipAddress = newValue;
    }
    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String newValue) {
        this.hostName = newValue;
    }
    
    public String getVersion() {
        return version;
    }
    public void setVersion(String newValue) {
        this.version = newValue;
    }
    
    public boolean isStarted() {
        return started;
    }
    public void setStarted(boolean started) {
        this.started = started;
    }
    public boolean isSuspended() {
        return suspended;
    }
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
    
    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }
    public void setDiscoveryEnabled(boolean b) {
        this.discoveryEnabled = b;
    }
}
