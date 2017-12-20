package com.viaoa.hub;

import com.viaoa.object.*;


/**
 * This works similar to a HubDetail, except that it will first check to see if the data is loaded.
 * If not, it will load using swingWorker thread.
 * @author vvia
 */
public class HubJfcDetail {
    private Hub hubMaster;
    private Hub hubDetail;
    private String prop;
    private OALinkInfo li;

    public HubJfcDetail(Hub hubMaster, Hub hubDetail, String prop) {
        this.hubMaster = hubMaster;
        this.hubDetail = hubDetail;
        this.prop = prop;
        setup();
    }

    protected void setup() {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(hubMaster.getObjectClass());
        this.li = oi.getLinkInfo(prop);
        this.li = this.li.getReverseLinkInfo();
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                update();
            }
        };
        hubMaster.addListener(hl);
        update();
    }

    protected void update() {
        final OAObject obj = (OAObject) hubMaster.getAO();
     
        if (obj == null) {
            hubDetail.setSharedHub(null);
            hubDetail.datam.masterHub = hubMaster;
            hubDetail.datam.liDetailToMaster = li;
            return;
        }
        if (obj.isLoaded(prop)) {
            hubDetail.setSharedHub(((Hub) obj.getProperty(prop)), false);
            hubDetail.datam.masterHub = hubMaster;
            return;
        }
        hubDetail.setSharedHub(null);

        javax.swing.SwingWorker<Void, Void> sw = new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                obj.getProperty(prop);
                return null;
            }

            @Override
            protected void done() {
                if (obj != hubMaster.getAO()) return;
                hubDetail.setSharedHub((Hub) obj.getProperty(prop), false);
                hubDetail.datam.masterHub = hubMaster;
                hubDetail.datam.liDetailToMaster = li;
            }
        };
        sw.execute();
    }

}
