package com.viaoa.sync.model;


import java.io.Serializable;

import com.viaoa.util.OADateTime;


/**
 * Information about a single instance of a Client.
 */
public class ClientInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    protected int connectionId = -1;
    protected OADateTime created;
    protected OADateTime disconnected;

    protected volatile boolean started;
    protected String hostName;
    protected String ipAddress;

    protected String serverHostName;
    protected int serverHostPort;
    
    protected volatile int totalRequests;
    protected volatile long totalRequestTime; // nanoseconds

    public ClientInfo() {
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
    
    public int getConnectionId() {
        return connectionId;
    }
    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public OADateTime getDisconnected() {
        return disconnected;
    }
    public void setDisconnected(OADateTime disconnected) {
        this.disconnected = disconnected;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }
    public void incrementTotalRequests() {
        this.totalRequests++;
    }
    public long getTotalRequestTime() {
        return totalRequestTime;
    }
    public void setTotalRequestTime(long totalRequestTime) {
        this.totalRequestTime = totalRequestTime;
    }
    public void incrementTotalRequestTime(long nsTime) {
        this.totalRequestTime += nsTime;
    }


    public String getServerHostName() {
        return serverHostName;
    }
    public void setServerHostName(String gsmrServerHostName) {
        this.serverHostName = gsmrServerHostName;
    }

    
    public int getServerHostPort() {
        return serverHostPort;
    }
    public void setServerHostPort(int gsmrServerHostPort) {
        this.serverHostPort = gsmrServerHostPort;
    }
    public boolean isStarted() {
        return started;
    }
    public void setStarted(boolean started) {
        this.started = started;
    }
}
