/* 

2004/01/23 changed class name, originally named HubAutoNumber

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
package com.viaoa.hub;

import java.util.*;
import java.util.logging.Logger;
import java.lang.reflect.*;

import com.viaoa.cs.OAClient;
import com.viaoa.object.*;
import com.viaoa.util.*;

/** 
    Used to store the position of an object within a hub in property within the object.
    This can then be used when retrieving the objects from a datasource.
    @see Hub#setAutoSequence
*/
public class HubAutoSequence extends HubListenerAdapter implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(HubAutoSequence.class.getName());

    public static int AutoSequenceHubListenerCount;
    
    protected Hub hub;
    protected String propertyName;
    protected transient Method propertySetMethod;
    protected int startNumber;
    protected boolean bKeepSeq;
    protected boolean bServerSideOnly;
    
    public HubAutoSequence() {
    }

    protected void finalize() throws Throwable {
    	close();
        super.finalize();
    }

    public void close() {
        if (hub != null) setHub(null);
    }
    
    /**
        Create a new HubAutoSequence.
        @param propertyName is int property for storing number.
    */
    public HubAutoSequence(Hub hub, String propertyName, int startNumber) {
        this(hub,propertyName,startNumber, false, false);
    }
    
    /**
        Create a new HubAutoSequence.
        @param propertyName is int property for storing number.
        @param bKeepSeq, if false then seq numbers are not updated when an object is removed        
    */
    public HubAutoSequence(Hub hub, String propertyName, int startNumber, boolean bKeepSeq) {
        this(hub,propertyName,startNumber, bKeepSeq, false);
    }

    /**
     * 
     * @param hub
     * @param propertyName
     * @param startNumber
     * @param bKeepSeq
     * @param bServerSideOnly this is used by Hub.setAutoSequence(...) so that the server will control the seq property and
     * send CS messages to clients.  If true, then the property changes (for seq prop) will need to be sent to clients.
     */
    public HubAutoSequence(Hub hub, String propertyName, int startNumber, boolean bKeepSeq, boolean bServerSideOnly) {
        if (bServerSideOnly && !HubCSDelegate.isServer()) LOG.warning("bServerSideOnly should be false, since this is not the server");
        this.startNumber = startNumber;
        this.bKeepSeq = bKeepSeq;
        this.bServerSideOnly = bServerSideOnly;
        setHub(hub);
        setPropertyName(propertyName);
    }
    

    /**
        Create a new HubAutoSequence.
        @param propertyName is int property for storing number.
    */
    public HubAutoSequence(Hub hub, String propertyName) {
        setHub(hub);
        setPropertyName(propertyName);
    }

    
    
    /** Set the starting number to be used for first object. default is "0". */
    public int getStartNumber() {
        return startNumber;
    }
    /** Set the starting number to be used for first object. default is "0". */
    public void setStartNumber(int i) {
        startNumber = i;
        setup();
    }
    
    public Hub getHub() {
        return hub;
    }
    
    
    public void setHub(Hub hub) {
        if (this.hub != null) {
            this.hub.removeHubListener(this);
            AutoSequenceHubListenerCount--;
        }
        this.hub = hub;
        if (hub != null) {
            hub.addHubListener(this);
            AutoSequenceHubListenerCount++;
        }
        this.propertySetMethod = null;
        setup();
    }

    /** 
        Number property in object that is used to keep track of the order of the object within the hub.
        The hub will set the value based on the objects position within the Hub.  
        <p>
        Note: the object is not automatically saved. 
    */
    public String getPropertyName() {
        return propertyName;
    }
    /** @see getPropertyName */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        this.propertySetMethod = null;
        setup();
    }
    
    protected void setup() {    
        if (propertyName == null || hub == null) return;

        Class c = hub.getObjectClass();
        if (c == null) return;
        
        Method met = OAReflect.getMethod(c, "set"+propertyName);
        Class[] classes = met.getParameterTypes();
        if (classes == null || classes.length != 1) {
            throw new RuntimeException("setPropertyName(\"get"+propertyName+"\") property must accept a numeric parameter");
        }
        c = classes[0];
        if (!c.equals(int.class) && !c.equals(long.class) && !c.equals(char.class)) {
            throw new RuntimeException("setPropertyName(\"set"+propertyName+"\") property must accept a numeric parameter");
        }
        propertySetMethod = met;
        resequence(0);
    }
        
    private int resequenceCnt;  // used instead of synchronization
    protected void resequence(int startPos) {
        if (hub.isDeletingAll()) return;
        int cnt = (++resequenceCnt);
        int x = hub.getSize();  // only seq loaded objects
        for (int i=startPos; i<x; i++) {
            Object obj = hub.elementAt(i);
            if (obj == null) break;
            if (cnt != resequenceCnt) break;
            updateSequence(obj, i+startNumber);
        }
    }
    protected void updateSequence(Object obj, int pos) {
        if (propertySetMethod == null) return;
        if (obj == null) return;
        try {
            // if this is ClientThread then need to send to other clients
            if (bServerSideOnly) OAClient.beginServerOnly();
            propertySetMethod.invoke(obj, new Object[] { new Integer(pos) });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (bServerSideOnly) OAClient.endServerOnly();
        }
    }

    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void afterInsert(HubEvent e) {
        int pos = e.getPos();
        resequence(pos);
    }
    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void afterAdd(HubEvent e) {
        int pos = e.getPos();
        resequence(pos);
    }
    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void afterRemove(HubEvent e) {
        if (bKeepSeq) {
            int pos = e.getPos();
            resequence(pos);
        }
    }
    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void afterMove(HubEvent e) {
        resequence(0);
    }
    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void onNewList(HubEvent e) {
        resequence(0);
    }
    /** HubListener interface method, used to listen to changes to Hub and update sequence numbers. */
    public @Override void afterSort(HubEvent e) {
        resequence(0);
    }
}

