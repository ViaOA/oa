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

import java.net.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

/** 
    OARegistry can be used to start a RMI registry for OAServer. 
*/
public class OARegistry {

    public OARegistry() {        
        String host;
        String codeBase;
        
        // setup RMI to use the current local address
        try {
            InetAddress address = InetAddress.getLocalHost();
            host = address.getHostName();
//     host = "192.22.21.1";  //qqqqqqqqqq
            codeBase = "http:/"+host+"/projects/java"; //was: "file:/d:\\projects\\java";
        }
        catch (java.net.UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("HOST: "+host);
        System.out.println("CODEBASE: "+codeBase);

        try {
            Properties currentProperties = System.getProperties();
            currentProperties.put("java.rmi.server.hostname", host);
            currentProperties.put("java.rmi.server.codebase", codeBase);
            System.setProperties(currentProperties);

            Registry reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

            // load objects
            OAServerImpl oaServer = new OAServerImpl();

            reg.rebind("OAServer", oaServer);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
          
        System.out.println("OARegistry ready");
    }

    public static void main(String[] argv) {
        new OARegistry();
    }
}

