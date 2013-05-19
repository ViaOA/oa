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

import java.rmi.*;

import com.viaoa.object.*;

/** 
    RMI Class used between OAClient and OAServer.
    This object is automatically created for OAClient when OAClient connects to OAServer.
    <p>
    OAObjectServer has a <i>queue</i> of messages that it recieves from OAServer.  These messages
    are then retrieved by OAClient.  OAClient uses OAObjectServer to send messages to OAServer.
    @see OAServer
    @see OAClient
*/
public interface OAObjectServer extends Remote {
    /** 
        Unique Identifier assigned by OAObjectServer when it is created by OAServer 
    */
    public int getId() throws RemoteException;
    
    /** 
        Send message to OAServer.  
        Used by OAObject, Hub, HubController, OAClientDataSource, and custom calls to OAObjectPublisher. 
    */
    public void sendMessage(OAObjectMessage msg) throws RemoteException;
    
    
    /** 
    Used by OAClient to receive message from OAObjectServer.
	*/
	public OAObjectMessage[] getMessages() throws RemoteException;
    
    /**
        Lock object on OAServer.
    */
    public void lock(Class clazz, Object[] objectIds, Object miscObject) throws RemoteException;

    /**
        Unlock object on OAServer.
    */
    public void unlock(Class clazz, Object[] objectIds) throws RemoteException;

    /**
        Calls isLocked for object on OAServer.
    */
    public boolean isLocked(Class clazz, Object[] objectIds) throws RemoteException;

    /**
        Calls getLocked for object on OAServer.
    */
    public OALock getLock(Class clazz, Object[] objectIds) throws RemoteException;

    /**
        Calls getAllLockedObjects on OAServer.
    */
    public Object[] getAllLockedObjects() throws RemoteException;
    
    /**
        Called by OAClient to close the connection with OAServer.
    */
    public void close() throws RemoteException;

    /**
        Used by OAObject to assign a guid to a new object 
    */
    public int getNextFiftyObjectGuids() throws RemoteException;
    
    /**
        Send an OADataSource command to be processed by OADataSource on OAServer.
    */
    public Object datasource(OAObjectMessage msg) throws RemoteException;


    /**
        Add an object to client cache on server.
    */
    public int addToCache(Object obj) throws RemoteException;

    /**
        Remove an object from client cache on server.
    */
    public int removeFromCache(Class clazz, OAObjectKey[] keys) throws RemoteException;

    // public void setUser(Object user) throws RemoteException;

    
}

