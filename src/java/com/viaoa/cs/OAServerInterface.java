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

import com.viaoa.remote.multiplexer.annotation.RemoteInterface;


/** 
     OAServer is a single RMI Distributed Object used for creating Client/Server applications that automatically
     stay synchronized.  
     <p>
     OAServer is responsible for creating and managing OAObjectServer objects for each connection.  OAServer 
     sends and receives messages from OAObjectServer to communicate with OAClient objects.
*/
@RemoteInterface
public interface OAServerInterface {
    /**
        Create a new OAObjectServer object for an OAClient.  This is automatically created by
        OAClient.
    */
    public OAObjectServerInterface getOAObjectServer() throws RemoteException;
    
    /**
        Method used to test if OAServer is working correctly.
    */
    public String test() throws RemoteException;


}

