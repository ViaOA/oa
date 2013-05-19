package com.viaoa.cs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.*;

import com.viaoa.hub.HubListenerTree;
import com.viaoa.hub.HubMerger;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.*;


/**
 * Sends a message to the server to have  OAClientInfo updated, so
 * that the connection information for the server and the client
 * can be known.
 * @author vvia
 */
public class OAClientMonitor {
	private static Logger LOG = Logger.getLogger(OAClientMonitor.class.getName());
	
	private OAClient client;
    private OAClientInfo clientInfo;
    private int sleepSeconds;

    public OAClientMonitor(OAClient client, int sleepSeconds) {
    	LOG.config("sleepSeconds="+sleepSeconds);
    	this.client = client;
    	this.sleepSeconds = sleepSeconds;
    }
    
    protected void start() {
    	LOG.config("starting thread OAClientMonitor");
    	
		Thread t = new Thread(new Runnable() {
			public @Override void run() {
		        for (int i=0; !client.bStop; i++) {
		            try {
						updateClientInfo( (i % 10) == 0);
		            	Thread.sleep(sleepSeconds * 1000);
		            }
		            catch (Exception e) {
		            	LOG.log(Level.WARNING, "OAClientMonitor, updateClientInfo() loop", e);
		            }
		        }
			}	
		}, 
		"OAClientMonitor"
		);
		t.setDaemon(true);
		t.setPriority(t.MIN_PRIORITY);
		t.start();
    }

    
    protected OAClientInfo getClientInfo() {
    	if (clientInfo == null) {
    		clientInfo = new OAClientInfo();
    		try {
                InetAddress ia = InetAddress.getLocalHost();
                clientInfo.hostName = ia.getHostName();
    		    clientInfo.ipAddress = ia.getHostAddress();
    		}
    		catch (UnknownHostException e) {
    		}
    	}
    	return clientInfo;
    }

    
    protected void updateClientInfo(boolean bGetReturnInfo) {
    	getClientInfo();
    	clientInfo.lastClientUpdate = new OADateTime();
    	clientInfo.totalMemory = Runtime.getRuntime().totalMemory();
    	clientInfo.freeMemory = Runtime.getRuntime().freeMemory();
    	clientInfo.maxMemory = Runtime.getRuntime().maxMemory();

    	clientInfo.hubListenerCount = HubListenerTree.ListenerCount;
    	clientInfo.hubMergerHubListenerCount = HubMerger.HubListenerCount;
    	
        if (client.clientMessageHandler != null) {
        	client.clientMessageHandler.updateClientInfo(clientInfo);
        }
        if (client.clientMessageReader != null) {
        	client.clientMessageReader.updateClientInfo(clientInfo);
        }
        
    	OAObjectCacheDelegate.updateClientInfo(clientInfo);
    	OAObjectMessage msg = new OAObjectMessage();
    	msg.type = OAObjectMessage.CLIENTINFO;
    	msg.newValue = clientInfo;
    	msg.pos = (bGetReturnInfo ? 1 : 0);
    	client.sendMessage(msg);
    	sendClientInfoCnt++;
    	if (msg.newValue instanceof OAClientInfo) {
    	    this.clientInfo = (OAClientInfo) msg.newValue;
    	}
    }    

    public int getSendClientInfoCount() {
    	return sendClientInfoCnt;
    }
    private int sendClientInfoCnt;
    
    /*
	public String verify() {
		String result = null;
		if (clientMessageHandler == null) return null;
		String s = null;//clientMessageReader.verify();
		if (s != null) result = s;
		s = clientMessageHandler.getInfo();
		if (s != null) {
			if (result == null) result = "";
			else result += " ";
			result += s;
		}
		return result;
	}
    */
    
    
}
