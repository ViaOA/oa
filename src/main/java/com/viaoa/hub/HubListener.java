/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
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
public interface HubListener<T> extends EventListener {

    /** 
        Event sent whenever a property is changed.  This includes OAObject or Hub properties.<br>
        OAObject: any object property, changed, editable, new<br>
        Hub: allowDelete, allowNew, allowEdit, eof, bof<br>
        <p>
        Note: propertyChanges are sent for any changed object, not just the ActiveObject 
        @see OAObject#fireBeforePropertyChange(String,Object,Object) OAObject.fireBeforePropertyChange
        @see Hub#firePropertyChange(OAObject,String,Object,Object) Hub.firePropertyChange
    */
    public void beforePropertyChange(HubEvent<T> e);
    public void afterPropertyChange(HubEvent<T> e);

    public void beforeInsert(HubEvent<T> e);
    public void afterInsert(HubEvent<T> e);

    /** 
	    Event sent before object is added to Hub,
	    @see Hub#add Hub.add
	*/
	public void beforeAdd(HubEvent<T> e);
    
    /** 
        Event sent after object is added to Hub,
        before cache size is checked and
        before property to master is set.
        @see Hub#add Hub.add
    */
    public void afterAdd(HubEvent<T> e);
    
    /** 
	    Event sent before an object is removed from a Hub.
	    This is needed for cases where the remove will be doing other work.  For example: to create an Undo. 
	*/
    public void beforeRemove(HubEvent<T> e);
    /** 
	    Event sent after an object is removed from a Hub. 
	*/
	public void afterRemove(HubEvent<T> e);

    /**
     * Sent before all objects are removed/cleared from the Hub.
     */
    public void beforeRemoveAll(HubEvent<T> e);
    /**
     * Sent after all objects are removed/cleared from the Hub.
     */
    public void afterRemoveAll(HubEvent<T> e);

    
    /** 
	    Event sent before a Hub move(). 
	    @see Hub#move Hub.move
	*/
	public void beforeMove(HubEvent<T> e);
    /** 
        Event sent at the end of a Hub move(). 
        @see Hub#move Hub.move
    */
    public void afterMove(HubEvent<T> e);


    /** 
        Event sent after ActiveObject has been set.
        @see Hub#setActiveObject(Object,int,boolean,boolean,boolean,boolean) Hub.setActiveObject
    */
    public void afterChangeActiveObject(HubEvent<T> e);

    /** 
        Event sent when a new list of objects is available for a Hub.
    */
    public void onNewList(HubEvent<T> e);
    

    /**
        Event sent from OAObject when save() is being performed.
        @see OAObject#save OAObject.save
    */
    public void beforeSave(HubEvent<T> e);
    
    /**
        Event sent from OAObject when save() is being performed.
        @see OAObject#save OAObject.save
    */
    public void afterSave(HubEvent<T> e);


    /**
        Event sent from OAObject during delete().
        @see OAObject#delete OAObject.delete
    */
    public void beforeDelete(HubEvent<T> e);
    public void afterDelete(HubEvent<T> e);
    
    /**
        Event sent from Hub when a select is performed.
        @see Hub#select Hub.select
    */
    public void beforeSelect(HubEvent<T> e);
    /**
        Event sent from Hub after select is performed.
        @see Hub#select Hub.select
    */

    
    public void afterFetchMore(HubEvent<T> e);
    
    
    /**
        Event sent from Hub when sort is performed.
        @see Hub#sort Hub.sort
    */
    public void afterSort(HubEvent<T> e);


    /**
     * Location that this listener should be added to a listener list.
     * @author vvia
     */
    public enum InsertLocation {
        FIRST, NEXT, LAST;
    }
   
    public void setLocation(InsertLocation pos);
    public InsertLocation getLocation();
    
    public void afterLoad(HubEvent<T> e);
}


