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

import java.util.HashSet;
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
        int x = fetchMore(thisHub, HubSelectDelegate.getSelect(thisHub));
        return x;
    }
    protected static int fetchMore(Hub thisHub, OASelect sel) {
        if (sel == null) return 0;
        int x = sel.getFetchAmount();
        x = fetchMore(thisHub, sel, x);
        HubEventDelegate.fireAfterFetchMoreEvent(thisHub);
        return x;
    }
    /** Internal method to retrieve objects from last select() */
    protected static int fetchMore(Hub thisHub, int famt) {
        int x = fetchMore(thisHub, HubSelectDelegate.getSelect(thisHub), famt);
        return x;
    }
    protected static int fetchMore(Hub thisHub, OASelect sel, int famt) {
        if (sel == null) return 0;
        int fa = sel.getFetchAmount();  // default amount to load

        HubData hubData = thisHub.data;         

        boolean holdDataChanged = hubData.changed;

        if (famt > 0) fa = famt;
        int cnt = 0;

        try {
        	hubData.setInFetch(true);

			int capacity = hubData.vector.capacity(); // number of available 'slots'
            int size = hubData.vector.size(); // number of elements
            
        	for ( ; cnt < fa || fa == 0; ) {
                Object obj;
                if ( !HubSelectDelegate.isMoreData(sel) ) {
                    thisHub.cancelSelect();
                    sel.cancel();
                    break;
                }

                obj = sel.next();
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
            sel.cancel();
        	throw new RuntimeException(ex);
        }
        finally {
            hubData.setInFetch(false);
            hubData.changed = holdDataChanged;
        }
        return cnt;
    }

    
    /**
        Used to know if objects are currently being loaded from datasource from last select().
    */
    public static boolean isFetching(Hub thisHub) {
        return thisHub.data.isInFetch();
    }
    public static void setFetching(Hub thisHub, boolean bIsFetching) {
        thisHub.data.setInFetch(bIsFetching);
    }

    public static boolean isLoading(Hub thisHub) {
        return thisHub.data.isInFetch();
    }
    public static void setLoading(Hub thisHub, boolean bIsLoading) {
        thisHub.data.setInFetch(bIsLoading);
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
    public static boolean isMoreData(OASelect sel) {
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
        loadAllData(thisHub, thisHub.getSelect());
    }
	public static void loadAllData(Hub thisHub, OASelect select) {
	    if (thisHub == null || select == null) return;
	    
	    // 20121015 adjusted locking
	    for (int i=0; ;i++) {
    	    boolean bCanRun = false;
            synchronized (thisHub.data) {
                if (!thisHub.data.isLoadingAllData()) {
                    thisHub.data.setLoadingAllData(true);
                    bCanRun = true;
                }
            }
    	    
            if (bCanRun) {
                try {
                    while ( isMoreData(select) ) {
                        fetchMore(thisHub, select);
                    }
                }
                finally {
                    synchronized (thisHub.data) {
                        thisHub.data.setLoadingAllData(false);
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
	    return hub.data.getSelect();
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
	public static void select(final Hub thisHub, OASelect select, boolean bCancelPrevious) {  // This is the main select method for Hub that all of the other select methods call.
        if (bCancelPrevious) cancelSelect(thisHub, true);
	    if (select == null) {
	        return;
	    }
	
	    if (thisHub.datau.getSharedHub() != null) {
	        select(thisHub.datau.getSharedHub(), select);
	        return;
	    }
	    if (thisHub.data.objClass == null) {
	    	thisHub.data.objClass = select.getSelectClass();
	    	if (thisHub.data.objClass == null) return;
	    }
	
	    if (thisHub.datam.masterObject != null && thisHub.datam.liDetailToMaster != null) {
	        if (select != thisHub.data.getSelect() && thisHub.data.getSelect() != null) {
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
	    
        thisHub.data.setSelect(select);
        
        boolean bRunSelect;
        bRunSelect = oi.getUseDataSource();

        // 20160110 selects now have hubFinders, etc to do selects
        bRunSelect = (bRunSelect && (select.getDataSource() != null || select.getFinder() != null));
        //was: bRunSelect = (bRunSelect && select.getDataSource() != null);
        
	    
	    HubDataDelegate.incChangeCount(thisHub);
	    if (!select.getAppend()) {
            thisHub.setAO(null); // 20100507
	    	if (thisHub.isOAObject()) {
	            int z = HubDataDelegate.getCurrentSize(thisHub);
	            for (int i=0; i<z; i++) {
	            	OAObject oa = (OAObject) HubDataDelegate.getObjectAt(thisHub,i);
	            	OAObjectHubDelegate.removeHub(oa, thisHub, false);
	            }
	        }
	    	HubDataDelegate.clearAllAndReset(thisHub);
	        
	    	if (select.getRewind()) {
	    	    
                // 20120716
                OAFilter<Hub> filter = new OAFilter<Hub>() {
                    @Override
                    public boolean isUsed(Hub h) {
                        if (h != thisHub && h.dataa != thisHub.dataa) {
                            if (h.datau.getLinkToHub() == null) return true;
                        }
                        return false;
                    }
                };
                Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
	    	    
	    	    
	            //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub);
	            for (int i=0; i<hubs.length; i++) {
	            	if (hubs[i] != thisHub && hubs[i].dataa != thisHub.dataa) {
	        			if (hubs[i].datau.getLinkToHub() == null) {
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
			thisHub.data.setSelectAllHub(true);
			OAObjectCacheDelegate.setSelectAllHub(thisHub);
		}
		else {
			thisHub.data.setSelectAllHub(false);
			OAObjectCacheDelegate.removeSelectAllHub(thisHub);
		}
	    
	    if (!select.getAppend()) {
	    	HubEventDelegate.fireOnNewListEvent(thisHub, true);
	    }
	}

	/**
	    Creates a new OASelect object by first calling cancelSelect.
	*/
	public static OASelect createNewSelect(Hub hub, boolean bAddToHub) {
	    OASelect sel;
	    synchronized (hub.data) {
		    String s = null;
		    OASelect selHold = hub.data.getSelect();
		    cancelSelect(hub, true);

qqqqqqqqqqqqqq might not want to keep filter qqqqqqqqqqqqqqqq		    
		    sel = new OASelect(hub.getObjectClass());
		    if (bAddToHub) hub.data.setSelect(sel);
		    if (selHold != null) {
		        sel.setHubFilter(selHold.getHubFilter());
		    }
		}
	    return sel;
	}
	
	/**
	    Cancels current OASelect, calling select.cancel()
	    This will also set SelectLater to false and RequiredWhere to null.
	*/
	protected static void cancelSelect(Hub thisHub, boolean bRemove) {
	    OASelect sel = thisHub.data.getSelect();
	    boolean bHasMoreData;
		if (sel != null) {
		    boolean b = sel.hasBeenStarted();
		    bHasMoreData = (b && sel.hasMore());
	    	if (b) sel.cancel();
	        if (bRemove) thisHub.data.setSelect(null);
	        if (b) HubDataDelegate.resizeToFit(thisHub);
	    }
		else bHasMoreData = false;
		
        if (thisHub.data.isSelectAllHub() && bHasMoreData) {
        	thisHub.data.setSelectAllHub(false);
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
	        sel = createNewSelect(thisHub, true);
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
		thisHub.data.setSortProperty(s);

		OASelect sel = getSelect(thisHub);
	    if (!OAString.isEmpty(s) && sel == null) {
	        sel = createNewSelect(thisHub, true);
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

qqqqqqqqqqqqqqqqqqqqqqqq	
	public static void select(Hub thisHub, boolean bAppendFlag) {
		OASelect sel = getSelect(thisHub);
		boolean bCancelFirst;
		if (sel == null) {
		    sel = createNewSelect(thisHub, true);
		    bCancelFirst = false;
		}
		else {
		    sel.setWhereObject(null);
		    sel.setParams(null);
		    sel.setWhere(null);
            sel.setOrder(null);
		    sel.setPassthru(false);
		    bCancelFirst = true;
		}
	    sel.setAppend(bAppendFlag);
        select(thisHub, sel, bCancelFirst);
	}	

	// Main Select here:
	protected static void select(Hub thisHub, OAObject whereObject, String whereClause, Object[] whereParams, String orderByClause, boolean bAppendFlag) {
		OASelect sel = getSelect(thisHub);
		boolean bCancelFirst;
		if (sel == null)  {
		    sel = createNewSelect(thisHub, false);
		    bCancelFirst = false;
		}
		else {
		    bCancelFirst = true;
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
		if (sel == null) sel = createNewSelect(thisHub, true);
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
        if (sel == null) sel = createNewSelect(thisHub, true);
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
	public static boolean refreshSelect(Hub thisHub) {
        if (thisHub == null) return false;
        Object objAO = thisHub.getAO();
        OASelect sel = getSelect(thisHub);

        final Object master = thisHub.getMasterObject();
        if (sel != null) {
            cancelSelect(thisHub, false);  // dont remove select from hub
            sel.reset();
        }
        else {
            if (master == null) return false;
            OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(thisHub);
            if (li == null) return false;
            li = li.getReverseLinkInfo();
            if (li == null) return false;
            OADataSource ds = OADataSource.getDataSource(thisHub.getObjectClass());
            if (ds == null) return false;
            
            sel = new OASelect(thisHub.getObjectClass());
            sel.setWhereObject((OAObject) master);
            sel.setPropertyFromWhereObject(li.getName());
            sel.setOrder(li.getSortProperty());
        }
        
        sel.setDirty(true);
        sel.select();
        HashSet<Object> hs = new HashSet<Object>();
        for ( ;sel.hasMore(); ) {
            Object objx = sel.next();
            hs.add(objx);
            thisHub.add(objx);
        }
        sel.setDirty(false);

        // check to see if any objects need to be removed from the original list
        if (master == null) {
            for (Object obj : thisHub) {
                if (!hs.contains(obj)) {
                    thisHub.remove(obj);
                }
            }
        }
        
        thisHub.setAO(objAO);
        return true;
	}
}
