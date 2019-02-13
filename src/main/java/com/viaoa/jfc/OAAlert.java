package com.viaoa.jfc;

import javax.swing.SwingUtilities;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OATemplate;

public class OAAlert {

    protected Hub hub; 
    protected String propertyName;
    protected String title;
    protected String msg;
    protected OATemplate template;

    /**
     * 
     * @param hub
     * @param propertyName this is checked for hub.AO.  If true, then an popup message will show. 
     * @param title 
     * @param msg popup message, can be an OATemplate string
     */
    public OAAlert(Hub hub, String propertyName, String title, String msg) {
        this.hub = hub;
        this.propertyName = propertyName;
        this.msg = msg;
        this.title = title;
        
        if (hub == null) return;
        
        if (msg != null && msg.indexOf("<%=") >= 0) {
            template = new OATemplate(msg);
        }
        
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                super.afterChangeActiveObject(e);
            }
        });
        
    }
    
    protected void afterChangeActiveObject() {
        final Object obj = hub.getAO();
        if (!(obj instanceof OAObject)) return;
        final Object objx = ((OAObject) obj).getProperty(propertyName);
        if (!OAConv.toBoolean(objx)) return;

        final String msgx = (template != null) ? template.process((OAObject) obj) : msg;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                OAJfcUtil.showMessage(title, msgx);
            }
        });
    }
    
    
}
