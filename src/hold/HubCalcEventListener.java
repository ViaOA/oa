package com.viaoa.hub;

import java.util.logging.Logger;

import com.viaoa.object.OACalcInfo;
import com.viaoa.util.OAArray;

/**
	Hub Listener used to manage changes to a calculated property.
	This is used when a listener is created for a calculated property.
	@see HubEventDelegate#addHubListener(Hub, HubListener, String)
	@see Hub#addHubListener(HubListener,String)
*/
class HubCalcEventListener extends HubListenerAdapter implements java.io.Serializable {
    private static Logger LOG = Logger.getLogger(HubCalcEventListener.class.getName());
	Hub hub, detail;
	HubListener[] listeners;
	String property;
	String detailProperty;
	boolean bIgnoreDupEvents;
    boolean bIgnoreHubEvents;

    public HubCalcEventListener(Hub hub, Hub detail, String property, String detailProperty) {
        this.hub = hub;
        this.property = property;
        this.detail = detail;
        this.detailProperty = detailProperty;
    }
	
	public void addListener(HubListener l) {
	    listeners = (HubListener[]) OAArray.add(HubListener.class, listeners, l);
	}
	public boolean removeListener(HubListener l) {
	    HubListener[] ss = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, l);
	    if (ss == listeners) return false;
        listeners = ss;
        if (ss.length == 1) bIgnoreDupEvents = false;  // in case it was turned off by another calcListener that was just removed.
        return true;
	}
	public int getLisenterCount() {
	    return (listeners==null ? 0 : listeners.length);
	}

	// some events can be ignored if there are other listeners for the same CalcProperty that use the same detail hub.
	public void setIgnoreDupEvents(boolean b) {
	    bIgnoreDupEvents = b;
	}

    // Hub events can be ignored since there were no hubs in the property path being used for this listener
	public void setIgnoreHubEvents(boolean b) {
	    bIgnoreHubEvents = b;
    }
	
	
	public @Override void afterChangeActiveObject(HubEvent e) {
	    // 20090622 removed, do not need to know when detail hub changes AO
	    //    otherwise, this creates a lot of calls 
	    // HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	}
	public @Override void afterPropertyChange(HubEvent e) {
	    if (e.getPropertyName().equalsIgnoreCase(detailProperty)) {
	        LOG.fine("detailProperty="+detailProperty+", property="+property);	        
		    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	    }
	}
	public @Override void afterInsert(HubEvent e) {
        if (bIgnoreDupEvents) return;
        if (bIgnoreHubEvents) return;
	    if ( !HubSelectDelegate.isFetching(e.getHub()) ) {
	        LOG.fine("detailProperty="+detailProperty+", property="+property);          
		    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	    }
	}
	public @Override void afterAdd(HubEvent e) {
        if (bIgnoreDupEvents) return;
        if (bIgnoreHubEvents) return;
	    if ( !HubSelectDelegate.isFetching(e.getHub()) ) {
	        LOG.fine("detailProperty="+detailProperty+", property="+property);          
		    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	    }
	}
	public @Override void afterRemove(HubEvent e) {
        if (bIgnoreDupEvents) return;
        if (bIgnoreHubEvents) return;
	    LOG.fine("detailProperty="+detailProperty+", property="+property);          
	    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	}
	public @Override void onNewList(HubEvent e) {
	    if (bIgnoreDupEvents) return;
        if (bIgnoreHubEvents) return;
	    LOG.fine("detailProperty="+detailProperty+", property="+property);          
	    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	}
	public @Override void afterSort(HubEvent e) {
        if (bIgnoreDupEvents) return;
        if (bIgnoreHubEvents) return;
	    LOG.fine("detailProperty="+detailProperty+", property="+property);          
	    HubEventDelegate.fireCalcPropertyChange(hub, null, property);
	}
}
