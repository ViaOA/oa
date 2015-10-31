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


import java.util.ArrayList;

import com.viaoa.hub.Hub;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.Tuple;

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
	protected int hubHubMergerIsChanging;  

    protected int sendingEvent;  // HubEventDelegate is sending an event.  Used so that calcPropertyEvents (see HubListenerTree) are only sent out once

    protected int hubListenerTreeCount;  // tracks how deep listeners are for a single listener
    
	// === COUNTERS ===========================
	
	/**
	 *  If set, then OAObject.initialize methods are not called.  Used by DataSource when creating new objects.
	 *  @see OAObjectDelegate#initialize(OAObject)
	 */
	protected int skipObjectInitialize;

	/**
	 *  Flag to know that an objects properties are being loaded by an internal source, to be set to original values.
	 */
	protected int loadingObject;

	/**
	 *  Flag to know if OAObjectEventDelegate.firePropertyChange() should not run.
	 *  If this is true, then all property changes that affect the management of the object and references to it, will not be done.
	 *  This is used by DataSource when it is creating/loading objects.
	 *  @see OAObjectEventDelegate#firePropertyChange(OAObject, String, Object, Object, boolean, boolean)
	 */
	protected int skipFirePropertyChange;
	
	// counter
	protected int suppressCSMessages;
	
    /**
     *  Flag to know that an object key property is being assigned
     */
    protected int assigningObjectKey;

    
    // 20110104
    /**
     * List of objects that are locked by this thread.  Should only be used by
     * OAThreadLocalDelegate, where a rwLock used when accessing it.
     * @see OAThreadLocalDelegate#lock(OAThreadInfo, Object)  
     */
    protected volatile Object[] locks; 
    protected boolean bIsWaitingOnLock;  // used on last lock - which is the only one that this could be waiting on.
 
    protected Object[] flags;

    /** used by OAUndoManager.start/endCapturePropertyChanges - to create undoable for oaObj.propertyChanges
     *  @see OAUndoableManager#startCapturePropertyChanges
    */
    protected boolean createUndoablePropertyChanges;
    protected String compoundUndoableName;
    protected Tuple<Object, String>[] calcPropertyEvents;
    
    
    protected String ignoreTreeListenerProperty;
    
    protected Hub getDetailHub; // hub that a get detail is being called for. This is a helper for getting detail from server
    
    public OAThreadLocal() {
        this.threadName = Thread.currentThread().getName();
    }

    // 20140121
    // current remote request that is being invoked
    protected RequestInfo requestInfo;

/*    
    // 20140303
    // these are from RemoteThread evetns, will be queue up and executed 
    protected ArrayList<Runnable> alRunnable;
*/    

    protected RemoteMultiplexerClient remoteMultiplexerClient;

}




