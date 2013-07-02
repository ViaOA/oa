/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.hub;

import java.util.*;

/** 
   
    The only event listener definition for receiving events from OAObject, Hub, and OAObjectCacheController.
    <p>
    Both Hub and OAObjectCacheController allow for HubListeners to be registered.<br>
    OAObject sends events through the Hubs that it is a member of, and through the OAObjectCacheController.
    
    @see HubEvent
    @see Hub#addListener Hub.addListener
    @see OAObjectCacheController#addListener 
*/
public interface HubListener extends EventListener {

    /** 
        Event sent whenever a property is changed.  This includes OAObject or Hub properties.<br>
        OAObject: any object property, changed, editable, new<br>
        Hub: allowDelete, allowNew, allowEdit, eof, bof<br>
        <p>
        Note: propertyChanges are sent for any changed object, not just the ActiveObject 
        @see OAObject#fireBeforePropertyChange(String,Object,Object) OAObject.fireBeforePropertyChange
        @see Hub#firePropertyChange(OAObject,String,Object,Object) Hub.firePropertyChange
    */
    public void beforePropertyChange(HubEvent e);
    public void afterPropertyChange(HubEvent e);

    public void beforeInsert(HubEvent e);
    public void afterInsert(HubEvent e);

    /** 
	    Event sent before object is added to Hub,
	    @see Hub#add Hub.add
	*/
	public void beforeAdd(HubEvent e);
    
    /** 
        Event sent after object is added to Hub,
        before cache size is checked and
        before property to master is set.
        @see Hub#add Hub.add
    */
    public void afterAdd(HubEvent e);
    
    /** 
	    Event sent before an object is removed from a Hub.
	    This is needed for cases where the remove will be doing other work.  For example: to create an Undo. 
	*/
    public void beforeRemove(HubEvent e);
    /** 
	    Event sent after an object is removed from a Hub. 
	*/
	public void afterRemove(HubEvent e);

    /**
     * Sent before all objects are removed/cleared from the Hub.
     */
    public void beforeRemoveAll(HubEvent e);
    /**
     * Sent after all objects are removed/cleared from the Hub.
     */
    public void afterRemoveAll(HubEvent e);

    
    /** 
	    Event sent before a Hub move(). 
	    @see Hub#move Hub.move
	*/
	public void beforeMove(HubEvent e);
    /** 
        Event sent at the end of a Hub move(). 
        @see Hub#move Hub.move
    */
    public void afterMove(HubEvent e);


    /** 
        Event sent after ActiveObject has been set.
        @see Hub#setActiveObject(Object,int,boolean,boolean,boolean,boolean) Hub.setActiveObject
    */
    public void afterChangeActiveObject(HubEvent e);

    /** 
        Event sent when a new list of objects is available for a Hub.
    */
    public void onNewList(HubEvent e);
    
    /**
        Event sent from OAObject when save() is being performed.
        @see OAObject#save OAObject.save
    */
    public void afterSave(HubEvent e);


    /**
        Event sent from OAObject during delete().
        @see OAObject#delete OAObject.delete
    */
    public void beforeDelete(HubEvent e);
    public void afterDelete(HubEvent e);
    
    /**
        Event sent from Hub when a select is performed.
        @see Hub#select Hub.select
    */
    public void beforeSelect(HubEvent e);
    /**
        Event sent from Hub after select is performed.
        @see Hub#select Hub.select
    */

    
    public void afterFetchMore(HubEvent e);
    
    
    /**
        Event sent from Hub when sort is performed.
        @see Hub#sort Hub.sort
    */
    public void afterSort(HubEvent e);


    /**
     * Location that this listener should be added to a listener list.
     * @author vvia
     */
    public enum InsertLocation {
        FIRST, NEXT, LAST;
    }
   
    public void setLocation(InsertLocation pos);
    public InsertLocation getLocation();
    
    public void afterLoad(HubEvent e);
}


