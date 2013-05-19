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


/** 
    Used when creating Client/Server applications where client needs to have custom requests
    sent to OAServer.
*/
public interface OAObjectPublisher {
    
    /**
        Called by OAClient.getServerObject(), OAClient.getObject(), OAClient.sendCommand(), OAClient.getPublisherObject.
        This is used to have OAClient communicate with OAServer to create custom features.
        <p>
        Can return OAObjectSerializeWrapper that will wrap the return object and control serialization.
    */
    public Object getObject(int clientId, Class clazz, Object[] objectIds);
    
    /**
        Called by OAServer when a new user (OAClient) is connected.
    */
    public void addUser(OAObjectServerImpl objectServer);

    /**
        Called by OAServer when a user (OAClient) is disconnected.
    */
    public void removeUser(OAObjectServerImpl objectServer, Object user);

    
    /**
        Called by OAClient.getDetail() to receive a reference Object or Hub.
        <p>
        Can return OAObjectSerializeWrapper that will wrap the return object and control how serialization
        works.
        <P><pre>

    public Object getDetail(Object objectMaster, String property, Object object) {
        if (objectMaster instanceof Template && property.equalsIgnoreCase("TemplateRows") ) {
            OAObjectSerializeInterface si = new OAObjectSerializeInterface() {
                public String[] getSerializedProperties(Object obj) {
                    if (obj instanceof SectionItem) {
                        return new String[] { "AutoSelects","Item" };
                    }
                    if (obj instanceof Template) {
                        return null;  // strip all
                    }
                    return null;
                }
            };
            // put object in a wrapper that will call the interface just created
            OAObjectSerializeWrapper sw = new OAObjectSerializeWrapper(object, si);
            return sw;
        }
        return object;
    }
        
        </pre>
        
        @see OAObject#setSerializedMode
        @see OAObject#setSerializedMode
        @see OAObjectSerializeInterface
    */
    public Object getDetail(Object objectMaster, String property, Object object);
    
    /**
	    Called by OAServerImpl to allow for "hook" for all messages.
		NOTE: this is called for every client that is sent a message!!!!! 2006/06/22
        @return msg or null to "not" send message to client clientId.
    */
    public OAObjectMessage sendMessage(int clientId, OAObjectMessage msg);
    
}

