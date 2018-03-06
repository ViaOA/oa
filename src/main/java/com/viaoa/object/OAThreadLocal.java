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
package com.viaoa.object;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubMerger;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.Tuple3;

/**
 * Used/created by OAThreadInfoDelegate to manage "flags" for threads.
 * @author vvia
 *
 */
public class OAThreadLocal {

    protected String threadName;
    protected String status;
	protected long time;
	
	protected Object[] deleting;

	// current mode for used by OAObjectCache
	// see: OAObjectCacheDelegate for list of mode
	protected int cacheAddMode; // 0 means that it has not been set and will use OAObjectCacheDelegate.DefaultAddMode
	
	protected OATransaction transaction;
	
	protected OAObjectSerializer objectSerializer;
	
	// flag to know if hub events can be ignored, since hubMerger is doing an internal operation.  
	//      Otherwise, there would be a lot of extra unneeded events. 
	//      used by HubMerger and HubListenerTree
	protected int hubMergerIsChangingCount;  

    protected int sendingEvent;  // HubEventDelegate is sending an event.  Used so that calcPropertyEvents (see HubListenerTree) are only sent out once

    protected int hubListenerTreeCount;  // tracks how deep listeners are for a single listener
    
    protected String ignoreTreeListenerProperty;

	/**
	 * Counter flag to know that an object is being loaded from DataSource.
	 *  
     * Used by OAObject when loading an object from a ds - dont verify, call listeners, send sync events.
     * Used by Hub when it is added/inserted to a Hub by a ds - dont verify, add to vecAdd
	 */
	protected int loading;


	// counter flag
	protected int suppressCSMessages;
	
    /**
     *  Flag to know that an object key property is being assigned
     */
    // protected int assigningObjectKey;
	// use OAObjectDSDelegate.setAssigningId(..)

    
    // 20110104
    /**
     * List of objects that are locked by this thread.  Should only be used by
     * OAThreadLocalDelegate, where a rwLock used when accessing it.
     * see OAThreadLocalDelegate#lock(OAThreadInfo, Object)  
     */
    protected volatile Object[] locks; 
    protected boolean bIsWaitingOnLock;  // used on last lock - which is the only one that this could be waiting on.
 
    protected Object[] flags;

    /** used by OAUndoManager.start/endCapturePropertyChanges - to create undoable for oaObj.propertyChanges
     *  see OAUndoableManager#startCapturePropertyChanges
    */
    protected boolean createUndoablePropertyChanges;
    protected String compoundUndoableName;
    protected Tuple3<Hub, OAObject, String>[] calcPropertyEvents;
    
    
    protected Hub getDetailHub; // hub that a get detail is being called for. This is a helper for getting detail from server
    protected String getDetailPropertyPath; // propertyPath from getDetailHub, set by HubMerger and HubGroupBy

    
    public OAThreadLocal() {
        this.threadName = Thread.currentThread().getName();
    }

    // 20140121
    // current remote request that is being invoked
    protected RequestInfo requestInfo;

    // 20160121
    protected Object notifyObject;
    
    // 20160625
    protected int recursiveTriggerCount;
    
    // 20180223 
    public int oaSyncEventCount;
    
    
    
}

