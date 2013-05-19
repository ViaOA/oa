package com.viaoa.hub;

/**
 * Used to have two hubs use the same objects, but in different order.
 */
public class HubCopy {

	private Hub hubMaster;
	private Hub hubCopy;
	private HubFilter hf;
	private boolean bShareAO;
	private HubListener hlMaster, hlCopy;
	private boolean bClosed;	
	
	public HubCopy(Hub hubMaster, Hub hubCopy, boolean bShareAO) {
		this.hubMaster = hubMaster;
		this.hubCopy = hubCopy;
		this.bShareAO = bShareAO;
		
		if (hubMaster == hubCopy) {
		    throw new RuntimeException("both hubs are the same");
		}
		
		hf = new HubFilter(hubMaster, hubCopy, true) {
			@Override
			public boolean isUsed(Object object) {
				return true;
			}
		};
		
		hlMaster = new HubListenerAdapter() {
			@Override
			public void afterChangeActiveObject(HubEvent evt) {
			    if (HubCopy.this.bClosed) return;
				if (HubCopy.this.bShareAO) {
				    Object obj = HubCopy.this.hubMaster.getAO();
				    if (obj == null || HubCopy.this.hubCopy.contains(obj)) HubCopy.this.hubCopy.setAO(obj);
				}
			}
			@Override
			public void onNewList(HubEvent e) {
                if (HubCopy.this.bClosed) return;
			    afterChangeActiveObject(e);
			}
		};
		hubMaster.addHubListener(hlMaster);
		
		hlCopy = new HubListenerAdapter() {
			@Override
			public void afterChangeActiveObject(HubEvent evt) {
                if (HubCopy.this.bClosed) return;
                if (HubCopy.this.bShareAO) {
                    HubCopy.this.hubMaster.setAO(HubCopy.this.hubCopy.getAO());
                }
			}
			@Override
			public void afterAdd(HubEvent e) {
                if (HubCopy.this.bClosed) return;
			    Object obj = e.getObject();
			    if (obj != null && !HubCopy.this.hubMaster.contains(obj)) {
			        HubCopy.this.hubMaster.add(obj);
			    }
			}
			@Override
			public void afterRemove(HubEvent e) {
                if (HubCopy.this.bClosed) return;
                Object obj = e.getObject();
                if (HubCopy.this.hubMaster.contains(obj)) HubCopy.this.hubMaster.remove(obj);
			}
		};
		hubCopy.addHubListener(hlCopy);
	}
	
	public void close() {
 	    // need to make sure that no more events get processed
	    bClosed = true;
	    if (hlMaster != null && hubMaster != null) {
	        hubMaster.removeHubListener(hlMaster);
	        hlMaster = null;
	    }
        if (hlCopy != null && hubCopy != null) {
            hubCopy.removeHubListener(hlCopy);
            hlCopy = null;
        }
        if (hf != null) {
            hf.close();
            hf = null;
        }
	}
	
}
