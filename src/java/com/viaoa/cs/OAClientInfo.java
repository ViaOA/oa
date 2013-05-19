package com.viaoa.cs;

import java.util.*;
import java.io.*;

import com.viaoa.object.*;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAId;
import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
 * Information about OAClient and OAObjectServer, sent to the server to update the client stats.
 */
//public class OAClientInfo extends OAObject implements Serializable {


@OAClass(
    shortName = "ci",
    displayName = "OAClientInfo",
    initialize = false,
    useDataSource = false,
    localOnly = true,
    addToCache = false
)
public class OAClientInfo extends OAObject {
    static final long serialVersionUID = 1L;
    
    int id;
    String hostName;
    String ipAddress;
    OADateTime created;
    OADateTime lastClientUpdate;
    OADateTime lastClientUpdateReceived;
    String userId;
    String userName;
    String location;
    int serverQueueSize;
    int cacheSize;   // on server
    OADateTime lastGetMessageOnServer;  // last time getMessage was called on the server
    String connectionStatus;
    OADateTime ended;
    int msgSent;  // msg sent by client
    int msgReceived;  // msg recvd by client
    long msgSentMs;

    int threadCount;
    int threadWaiting;
    int queueSize;
    long totalMemory;
    long freeMemory;
    long maxMemory;
    HashMap hubCacheInfo; // key=class, value=Integer (number of objects)
    String[] threadInfo;
    
    int hubListenerCount;
    int hubMergerHubListenerCount;
    
//    System.out.println("DayPanel.refresh ==> HubListenerTree.ListenerCount="+HubListenerTree.ListenerCount+", HubMerger.HubListenerCount="+HubMerger.HubListenerCount);
    
    public OAClientInfo() {
        
    }
    
    public String[] getThreadInfo() {
    	return threadInfo;
    }
    
    public String getHostName() {
        return hostName;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    
    public HashMap getCacheHashMap() {
    	if (hubCacheInfo == null) hubCacheInfo = new HashMap(13, .75f);
    	return hubCacheInfo;
    }
    
    public int getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	public String getConnectionStatus() {
		return connectionStatus;
	}
	public void setConnectionStatus(String connectionStatus) {
		this.connectionStatus = connectionStatus;
	}
	public OADateTime getCreated() {
		return created;
	}
	public OADateTime getLastClientUpdate() {
		return lastClientUpdate;
	}
	public OADateTime getLastClientUpdateReceived() {
        return lastClientUpdateReceived;
    }
    public void setCreated(OADateTime created) {
		this.created = created;
	}
	public OADateTime getEnded() {
		return ended;
	}
	public void setEnded(OADateTime ended) {
		this.ended = ended;
	}
	public long getFreeMemory() {
		return freeMemory;
	}
	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	
    @OAProperty(displayLength = 5)
    @OAId(guid = true)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public OADateTime getLastGetMessageOnServer() {
		return lastGetMessageOnServer;
	}
	public void setLastGetMessageOnServer(OADateTime lastGetMessageOnServer) {
		this.lastGetMessageOnServer = lastGetMessageOnServer;
	}
	public long getMaxMemory() {
		return maxMemory;
	}
	public void setMaxMemory(long maxMemory) {
		this.maxMemory = maxMemory;
	}
	public int getQueueSize() {
		return queueSize;
	}
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	public int getServerQueueSize() {
		return serverQueueSize;
	}
	public void setServerQueueSize(int serverQueueSize) {
		this.serverQueueSize = serverQueueSize;
	}
	public int getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	public int getThreadWaiting() {
		return threadWaiting;
	}
	public void setThreadWaiting(int threadWaiting) {
		this.threadWaiting = threadWaiting;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String user) {
		this.userId = user;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String user) {
		this.userName = user;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String loc) {
		this.location = loc;
	}
	
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner, recursive)
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.setInitializeNewObjects(false);  // dont set to null.
        oaObjectInfo.setAddToCache(false);
        oaObjectInfo.setLocalOnly(true);
        oaObjectInfo.setUseDataSource(false);
    }

    
    public String asString() {
    	
        StringBuilder sb = new StringBuilder(2048);
        sb.append("Id=" + id);
        sb.append(", user="+ userId);
        sb.append(", name=" + userName);
        sb.append(", location=" + location);
        
		String s2 = (serverQueueSize > 100) ? "***" : ""; 
		sb.append(", " + s2 + "queSize=" + serverQueueSize);
		
		s2 = (cacheSize > 10) ? "***" : ""; 
		sb.append(" " + s2 + "cacheSize=" + cacheSize);
		
		if (lastGetMessageOnServer != null) {
		    sb.append(", last=" + lastGetMessageOnServer.toString("HH:mm:ss")); 
		}
		sb.append(", sentByClient=" + OAString.format(msgSent, "#,###"));
		sb.append(", rcvdFromClient=" + OAString.format(msgReceived, "#,###"));
		sb.append(", clientQueSize=" + queueSize);
		sb.append(", clientThreads=" + threadCount); 
		sb.append(", waiting=" + threadWaiting);
		sb.append(", memoryUsed=" + OAString.format(totalMemory, "#,###"));
		sb.append(", memoryFree=" + OAString.format(freeMemory, "#,###"));
        // s += " avg ms: " + msgSentMs;
		if (lastClientUpdate != null) sb.append(", lastUpdate=" + lastClientUpdate.toString("HH:mm:ss"));
		if (created != null) sb.append(", created=" + created.toString("MM/dd HH:mm:ss"));
        if (ended != null) {
            sb.append(", endStatus=" + ended.toString("MM/dd HH:mm:ss") + " " + connectionStatus);
        }

        sb.append(", hubListenerCnt="+hubListenerCount);
        sb.append(", hubMergerHubListenerCnt="+hubMergerHubListenerCount);
        
        
		return sb.toString();
    }
    public String[] asStrings() {
    	if (threadInfo == null) threadInfo = new String[0];
    	int x = threadInfo.length;
		String[] ss = new String[x + 1];
		ss[0] = asString();
		if (x > 0) System.arraycopy(threadInfo, 0, ss, 1, x);
		return ss;
    }

}




