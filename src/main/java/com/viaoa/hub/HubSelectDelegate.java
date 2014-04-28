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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.ds.*;
import com.viaoa.object.*;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;

/**
 * Delegate used for Hub selecting and lazy loading (fetching) from datasource.
 * @author vvia
 *
 */
public class HubSelectDelegate {
    private static Logger LOG = Logger.getLogger(HubSelectDelegate.class.getName());
    
    /** Internal method to retrieve objects from last select() */
    protected static int fetchMore(Hub thisHub) {
        if (HubSelectDelegate.getSelect(thisHub) == null) return 0;
        int x = HubSelectDelegate.getSelect(thisHub).getFetchAmount();
        x = fetchMore(thisHub, x);
    	HubEventDelegate.fireAfterFetchMoreEvent(thisHub);
        return x;
    }
    /** Internal method to retrieve objects from last select() */
    protected static int fetchMore(Hub thisHub, int famt) {
        if (getSelect(thisHub) == null) return 0;
        int fa = HubSelectDelegate.getSelect(thisHub).getFetchAmount();  // default amount to load

        HubData hubData = thisHub.data;         

        boolean bHoldTrackChanges = hubData.bTrackChanges;
        hubData.bTrackChanges = false;
        boolean holdDataChanged = hubData.changed;

        if (famt > 0) fa = famt;
        int cnt = 0;

        try {
        	hubData.bInFetch = true;

			int capacity = hubData.vector.capacity(); // number of available 'slots'
            int size = hubData.vector.size(); // number of elements
            
        	for ( ; cnt < fa || fa == 0; ) {
                Object obj;
                if ( !HubSelectDelegate.isMoreData(thisHub) ) {
                    thisHub.cancelSelect();
                    break;
                }

                obj = HubSelectDelegate.getSelect(thisHub).next();
                if (obj != null) {
					if (size == (capacity-1)) {  // resize Vector according to select
					    /*
						if (thisHub.data.loadingAllData) {
							capacity = HubSelectDelegate.getCount(thisHub);
							if (capacity <= 0) capacity = size+1;
						}
						*/
						capacity += 75;  // this will override the default behaviour of how the Vector grows itself (which is to double in size)
//LOG.config("resizing, from:"+size+", to:"+capacity+", hub:"+thisHub);//qqqqqqqqqqqqqqqqqq                        
						HubDataDelegate.ensureCapacity(thisHub, capacity);
					}
                	HubAddRemoveDelegate.add(thisHub, obj);
                    size++;
                    cnt++;
                }
            }
        }
        catch (Exception ex) {
            LOG.log(Level.WARNING, "Hub="+thisHub+", will cancel select", ex);
            cancelSelect(thisHub, false);
        	throw new RuntimeException(ex);
        }
        finally {
            hubData.bInFetch = false;
            hubData.bTrackChanges = bHoldTrackChanges;
            hubData.changed = holdDataChanged;
        }
        return cnt;
    }

    
    /**
        Used to know if objects are currently being loaded from datasource from last select().
    */
    public static boolean isFetching(Hub thisHub) {
        return thisHub.data.bInFetch;
    }
    public static void setFetching(Hub thisHub, boolean bIsFetching) {
        thisHub.data.bInFetch = bIsFetching;
    }

    public static boolean isLoading(Hub thisHub) {
        return thisHub.data.bInFetch;
    }
    public static void setLoading(Hub thisHub, boolean bIsLoading) {
        thisHub.data.bInFetch = bIsLoading;
    }
	
	/**
	    Find out if more objects are available from last select from OADataSource.
	    @see Hub#needsToSelect
	*/
	public static boolean isMoreData(Hub thisHub) {
	    OASelect sel = getSelect(thisHub);
	    if (sel == null) return false;
	    if (!sel.hasBeenStarted()) {
	        sel.select();
	    }
	    return sel.hasMore();
	}

    /**
	    This will automatically read all records from current select().
	    By default, only 45 objects are read at a time from datasource.
	*/
	public static void loadAllData(Hub thisHub) {
	    if (thisHub.data.select == null) return;
	    
	    // 20121015 adjusted locking
	    for (int i=0; ;i++) {
    	    boolean bCanRun = false;
            synchronized (thisHub.data) {
                if (!thisHub.data.loadingAllData) {
                    thisHub.data.loadingAllData = true;
                    bCanRun = true;
                }
            }
    	    
            if (bCanRun) {
                try {
                    while ( isMoreData(thisHub) ) {
                        fetchMore(thisHub);
                    }
                }
                finally {
                    synchronized (thisHub.data) {
                        thisHub.data.loadingAllData = false;
                    }
                }
                break;
            }
            // else wait and try again
            try {
                Thread.sleep(25);
            }
            catch (Exception e) {
            }
	    }
	    /* was;
        synchronized (thisHub.data) {
            if (!thisHub.data.loadingAllData) {
                thisHub.data.loadingAllData = true;
                try {
                    while ( isMoreData(thisHub) ) {
                        fetchMore(thisHub);
                    }
                }
                finally {
                    thisHub.data.loadingAllData = false;
                }
            }
        }
        */
	}
	

	/**
	    Returns OASelect used for querying datasource.
	*/
	protected static OASelect getSelect(Hub hub) {
	    return hub.data.select;
	}
	
	/**
	    Used to populate Hub with objects returned from a OADataSource select.
	    By default, all objects will first be removed from the Hub, OASelect.select() will
	    be called, and the first 45 objects will be added to Hub and active object will be
	    set to null.  As the Hub is accessed for more objects, more will be returned until
	    the query is exhausted of objects.
	    @see OASelect
	    @see #loadAllData
	    @see #hasMoreData
	    @see #isFetching
	*/
	public static void select(final Hub thisHub, OASelect select) {  // This is the main select method for Hub that all of the other select methods call.
        cancelSelect(thisHub, true);
	    if (select == null) {
	        return;
	    }
	
	    if (thisHub.datau.sharedHub != null) {
	        if (thisHub.datau.selectOrder != null) thisHub.datau.sharedHub.setSelectOrder(thisHub.datau.selectOrder);
	        select(thisHub.datau.sharedHub, select);
	        return;
	    }
	    if (thisHub.datau.objClass == null) {
	    	thisHub.datau.objClass = select.getSelectClass();
	    	if (thisHub.datau.objClass == null) return;
	    }
	
	    if (thisHub.datam.masterObject != null && thisHub.datam.liDetailToMaster != null) {
	        if (select != thisHub.data.select && thisHub.data.select != null) {
	            throw new RuntimeException("select cant be changed for detail hub");
	        }
	        
	        
	        if (thisHub.datam.masterObject != null) {
	            if (thisHub.datam.masterObject != select.getWhereObject()) {
    	            if (select.getWhere() == null || select.getWhere().length() == 0) {
    	                OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.datam.masterObject.getClass());
    	                if (oi.getUseDataSource()) {
    	                    select.setWhereObject(thisHub.datam.masterObject);
    	                }
    	            }
    	            else {
    	                // cant call select on a hub that is a detail hub
    	                // 20140308 removed, ex:  ServerRoot.getOrders() has a select
    	                // throw new RuntimeException("cant call select on a detail hub");
    	            }
	            }
	        }
	    }
	    if (select.getWhereObject() != null) {
	        if (thisHub.datam.liDetailToMaster != null && select.getWhereObject() == thisHub.datam.masterObject) {
	            select.setPropertyFromWhereObject(thisHub.datam.liDetailToMaster.getReverseName());
	        }
	    }
	
	    select.setSelectClass(thisHub.getObjectClass());
	

	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());

	    HubEventDelegate.fireBeforeSelectEvent(thisHub);
	    
        thisHub.data.select = select;
        
        boolean bRunSelect;
        bRunSelect = oi.getUseDataSource();
        bRunSelect = (bRunSelect && select.getDataSource() != null);
	    
	    HubDataDelegate.incChangeCount(thisHub);
	    if (!select.getAppend()) {
            thisHub.setAO(null); // 20100507
	    	if (thisHub.isOAObject()) {
	            int z = HubDataDelegate.getCurrentSize(thisHub);
	            for (int i=0; i<z; i++) {
	            	OAObject oa = (OAObject) HubDataDelegate.getObjectAt(thisHub,i);
	            	OAObjectHubDelegate.removeHub(oa, thisHub);
	            }
	        }
	    	HubDataDelegate.clearAllAndReset(thisHub);
	        
	    	if (select.getRewind()) {
	    	    
                // 20120716
                OAFilter<Hub> filter = new OAFilter<Hub>() {
                    @Override
                    public boolean isUsed(Hub h) {
                        if (h != thisHub && h.dataa != thisHub.dataa) {
                            if (h.datau.linkToHub == null) return true;
                        }
                        return false;
                    }
                };
                Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
	    	    
	    	    
	            //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub);
	            for (int i=0; i<hubs.length; i++) {
	            	if (hubs[i] != thisHub && hubs[i].dataa != thisHub.dataa) {
	        			if (hubs[i].datau.linkToHub == null) {
	        				hubs[i].setAO(null);
	        			}
	            	}
	            }
	        }
	    }
	
	    if (bRunSelect) {
	    	select.select(); // run query
	    	fetchMore(thisHub);  // load up fetch amount objects into hub
	    }
	
		if (select.isSelectAll()) {
			thisHub.data.bSelectAllHub = true;
			OAObjectCacheDelegate.setSelectAllHub(thisHub);
		}
		else {
			thisHub.data.bSelectAllHub = false;
			OAObjectCacheDelegate.removeSelectAllHub(thisHub);
		}
	    
	    if (!select.getAppend()) {
	    	HubEventDelegate.fireOnNewListEvent(thisHub, true);
	    }
	}

	/**
	    Creates a new OASelect object by first calling cancelSelect.
	*/
	protected static OASelect createNewSelect(Hub hub) {
	    synchronized (hub.data) {
		    String s = null;
		    OASelect selHold = hub.data.select;
		    cancelSelect(hub, true);
		    hub.data.select = new OASelect(hub.getObjectClass());
		    if (selHold != null) {
		        hub.data.select.setHubFilter(selHold.getHubFilter());
		    }
		}
	    return hub.data.select;
	}
	
	/**
	    Cancels current OASelect, calling select.cancel()
	    This will also set SelectLater to false and RequiredWhere to null.
	*/
	protected static void cancelSelect(Hub thisHub, boolean bRemove) {
		if (thisHub.data.select != null) {
	    	thisHub.data.select.cancel();
	        if (bRemove) thisHub.data.select = null;
	        HubDataDelegate.resizeToFit(thisHub);
	    }
        if (thisHub.data.bSelectAllHub || thisHub.data.select == null || !thisHub.data.select.isSelectAll()) {
        	thisHub.data.bSelectAllHub = false;
        	OAObjectCacheDelegate.removeSelectAllHub(thisHub);
        }
	}
	
	public static int getCount(Hub thisHub) {
		if (thisHub == null) return -1;
		OASelect sel = getSelect(thisHub);
		if (sel == null) return -1;
        return sel.getCount();
	}
	public static boolean isCounted(Hub thisHub) {
		if (thisHub == null) return false;
		OASelect sel = getSelect(thisHub);
		if (sel == null) return true;
        return sel.isCounted();
	}

	/**
	    WHERE clause to use for select.
	    @see #setSelectOrder
	    @see OASelect
	 */
	public static void setSelectWhere(Hub thisHub, String s) {
        OASelect sel = getSelect(thisHub);
	    if (sel == null) {
	        sel = createNewSelect(thisHub);
	    }
	    sel.setWhere(s);
	}
	public static String getSelectWhere(Hub thisHub) {
        OASelect sel = getSelect(thisHub);
	    if (sel == null) return null;
	    return sel.getWhere();
	}
	
	/**
	    Sort Order clause to use for select.
	    @see #getSelectOrder
	    @see OASelect
	*/
	public static void setSelectOrder(Hub thisHub, String s) {
		thisHub.datau.selectOrder = s;

		OASelect sel = getSelect(thisHub);
	    if (!OAString.isEmpty(s) && sel == null) {
	        sel = createNewSelect(thisHub);
	    }
        sel.setOrder(s);
	}
	/**
	    Sort Order clause to use for select.
	    @see #setSelectOrder
	    @see OASelect
	*/
	public static String getSelectOrder(Hub thisHub) {
	    OASelect sel = getSelect(thisHub);
	    if (sel == null) return null;
	    return sel.getOrder();
	}


	
	public static void select(Hub thisHub, boolean bAppendFlag) {
		OASelect sel = getSelect(thisHub);
		if (sel == null) sel = createNewSelect(thisHub);
		else {
		    sel.setWhereObject(null);
		    sel.setParams(null);
		    sel.setWhere(null);
            sel.setOrder(null);
		    sel.setPassthru(false);
		}
	    sel.setAppend(bAppendFlag);
        select(thisHub, sel);
	}	

	// Main Select here:
	protected static void select(Hub thisHub, OAObject whereObject, String whereClause, Object[] whereParams, String orderByClause, boolean bAppendFlag) {
		OASelect sel = getSelect(thisHub);
		if (sel == null) sel = createNewSelect(thisHub);
		else {
	        sel.setPassthru(false);
		}
		sel.setWhereObject(whereObject);
        sel.setParams(whereParams);
        sel.setWhere(whereClause);
	    sel.setAppend(bAppendFlag);
	    sel.setOrder(orderByClause);
        select(thisHub, sel);
    }

    public static void selectPassthru(Hub thisHub, String whereClause, String orderClause) {
		OASelect sel = getSelect(thisHub);
		if (sel == null) sel = createNewSelect(thisHub);
		else {
            sel.setWhereObject(null);
            sel.setParams(null);
            sel.setAppend(false);
		}
        sel.setPassthru(true);
        sel.setWhere(whereClause);
        sel.setOrder(orderClause);
        select(thisHub, sel);
    }
    public static void selectPassthru(Hub thisHub, String whereClause, String orderClause, boolean bAppend) {
        OASelect sel = getSelect(thisHub);
        if (sel == null) sel = createNewSelect(thisHub);
        else {
            sel.setWhereObject(null);
            sel.setParams(null);
        }
        sel.setPassthru(true);
        sel.setAppend(bAppend);
        sel.setWhere(whereClause);
        sel.setOrder(orderClause);
        select(thisHub, sel);
    }
	
    /**
	    This will re-run the last select.
	    @see OASelect
	*/
	public static void refreshSelect(Hub thisHub) {
        OASelect sel = getSelect(thisHub);
	    if (sel != null) {
	        Object obj = thisHub.getAO();
	        cancelSelect(thisHub, false);
	        select(thisHub, sel);
	        thisHub.setAO(obj);
	    }
	}
}




