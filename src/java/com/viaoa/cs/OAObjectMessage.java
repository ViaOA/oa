/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.cs;

import java.io.*;

import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.object.*;


/** 
    Message Class used for passing messages to/from and used internally by OAClient and OAServer.
    Single message that supports all of the communications necessary to keep application in-sync
    across many servers.
*/
public class OAObjectMessage implements Serializable {
    static final long serialVersionUID = 2L;
    static final public int PROPERTY_CHANGE = 0; // sent by HubController when called by OAObject
    static final public int ADD = 1;    // sent by Hub
    static final public int INSERT = 2; // sent by Hub
    static final public int REMOVE = 3; // sent by Hub 
    static final public int MOVE = 4;   // sent by Hub
    static final public int REMOVEOBJECT = 5; // sent by HubController when called by OAObject
    
    static final public int SAVE = 6; 
    static final public int DELETE = 7; 
    
    
    // All of these are used to request objects/services from server
    static final public int GETPUBLISHEROBJECT = 8; 
    static final public int GETOBJECT = 9; 
    static final public int GETDETAIL = 10; 
    static final public int CREATENEWOBJECT = 11;
    static final public int CREATECOPY = 12;
    static final public int DATASOURCE = 13;
    
    static final public int SORT = 14;
    static final public int EXCEPTION = 15;
    static final public int CLIENTINFO = 16;
    static final public int CUSTOM = 17;  // this will be processed by OAClient.processCustom(), so that it can be overwritten on clients
    static final public int ERROR = 18;
    static final public int DELETEALL = 19; 
    static final public int REMOTEMETHODCALL = 20; 
    
    
    public static String[] msgTypes = { 
        "PROPERTY_CHANGE","ADD","INSERT","REMOVE","MOVE","REMOVEOBJECT",
        "SAVE", "DELETE",
        "GETPUBLISHEROBJECT", "GETOBJECT","GETDETAIL", "CREATENEWOBJECT", "CREATCOPY", "DATASOURCE",
        "SORT", "EXCEPTION", "CLIENTINFO", "CUSTOM", "ERROR", "DELETEALL", "REMOTEMETHODCALL"
    };
    
    
    // used by type=ERROR, store as pos=#
    static final public int ERROR_ServerQueueOverrun = 1; // queue loadPos on server is greater then this queue size for current client. 
    
    public static String[] errorTypes = {
        "",
        "Server Queue Overrun"
    };
    
    
    
    static private int gid;
    static final private Object LOCK = new Object();

    protected int id; // unique for client 
    protected int type;
    protected Class objectClass, masterClass;
    protected OAObjectKey objectKey, masterObjectKey;
    protected String property;
    protected Object newValue;  // new property value, or Object to Add/Insert
    protected int objectServerId; // set by OAObjectServer
    protected int pos, posTo;
    boolean bReceived;  // used by OAClient
    
    // server side info
    boolean bPrivate;  // if this is only for this user/client - dont send to other clients
    
    // flag to tell the ClientMessageHandler that it will be notified when it is ok to continue 
    //     with next msg in the queue.
    transient boolean bWillNotifyWhenProcessed; 
    int seq; // set by server.  NOTE: this can be set to 0 (when int hits max value, server will set back to 0)

    // 20090817 flag to know when change has been applied on the server.
    transient boolean bAppliedOnServer;  // used by OAServerImpl, OAClientMessageHandler
    
    
    public OAObjectMessage() {
        synchronized (LOCK) {
            id = ++gid;
        }
        if (OAClient.isServer()) {
            // since this is being created on the server, then the change has already taken place. 
            bAppliedOnServer = true;  
        }
    }

    public OAObjectKey getObjectKey() {
        return objectKey;
    }
    public Class getObjectClass() {
        return objectClass;
    }
    public String getProperty() {
        return property;
    }
    public void setType(int type) {
    	this.type = type;
    	setDefaults();
    }
    public int getType() {
        return type;
    }
    public Object getNewValue() {
    	return newValue;
    }
    public void setNewValue(Object s) {
    	newValue = s;
    }
    public void setPos(int pos) {
    	this.pos = pos;
    }
    public int getPos() {
    	return this.pos;
    }
    public void setPosTo(int pos) {
    	this.posTo = pos;
    }
    public int getPosTo() {
    	return this.posTo;
    }
    
    public void setMasterClass(Class c) {
    	this.masterClass = c;
    }
    public void setMasterObjectKey(OAObjectKey key) {
    	this.masterObjectKey = key;
    }
    
    
    public void setObjectClass(Class c) {
    	this.objectClass = c;
    }
    public void setObjectKey(OAObjectKey key) {
    	this.objectKey = key;
    }
    public void setProperty(String prop) {
    	this.property = prop;
    }
    
    public String toString() {
        String ss = " seq="+seq;
        if (masterClass != null) ss += " mc:" + masterClass.getName();
        if (masterObjectKey != null) ss += " mk:" + masterObjectKey;
        if (objectClass != null) ss += " oc:" + objectClass.getName();
        if (objectKey != null) ss += " ok:" + objectKey;
        if (property != null) ss += " p:" + property;
        if (newValue != null || type==PROPERTY_CHANGE) ss += " nv:" + newValue;
        if (pos > 0) ss += " pos:" + pos;
        return type + " " + msgTypes[type] + ss;
    }
    
    // 2007/04/13
    public boolean isFastDataSourceCommand() {
    	if (type != DATASOURCE) return false;
    	return (
    			pos != OADataSourceClient.INSERT &&
    			pos != OADataSourceClient.UPDATE &&
    			pos != OADataSourceClient.DELETE &&
    			pos != OADataSourceClient.SAVE &&
    			pos != OADataSourceClient.INITIALIZEOBJECT            			
    		);
    }
    
    public void setWillNotifyWhenProcessed(boolean b) {
    	this.bWillNotifyWhenProcessed = b;
    }
    public boolean getWillNotifyWhenProcessed() {
    	return this.bWillNotifyWhenProcessed;
    }
    protected void beforeSend() {
        setDefaults();
    }

    protected void setDefaults() {
if (type == OAObjectMessage.DATASOURCE) {
    int x = 4;
    x++;
}
        // flag as private, dont send to other clients
        switch (type) {
        case OAObjectMessage.GETDETAIL:
        case OAObjectMessage.GETOBJECT:
        case OAObjectMessage.CREATENEWOBJECT:
        case OAObjectMessage.CLIENTINFO:
        case OAObjectMessage.SAVE: 
        case OAObjectMessage.DELETE:
        case OAObjectMessage.GETPUBLISHEROBJECT:
        case OAObjectMessage.CREATECOPY:
        case OAObjectMessage.DATASOURCE:
        case OAObjectMessage.DELETEALL:
        case OAObjectMessage.REMOTEMETHODCALL:
            this.bPrivate = true;
            break;
        default:
            this.bPrivate = false;
        }
    }
        
}

