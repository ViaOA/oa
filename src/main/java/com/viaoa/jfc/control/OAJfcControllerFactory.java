package com.viaoa.jfc.control;

import javax.swing.JComponent;

import com.viaoa.hub.*;

/**
 * factory methods for OAJfcController. 
 * @author vvia
 *
 */
public class OAJfcControllerFactory {


    
    
    public static OAJfcController createHubValid(Hub hub, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, comp, HubChangeListener.Type.HubValid);
        return jc;
    }
    public static OAJfcController createHubValid(Hub hub, String prop, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, prop, comp, HubChangeListener.Type.HubValid);
        return jc;
    }
    public static OAJfcController createAoNotNull(Hub hub, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, comp, HubChangeListener.Type.AoNotNull);
        return jc;
    }
    public static OAJfcController createAoNotNull(Hub hub, String prop, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, prop, comp, HubChangeListener.Type.AoNotNull);
        return jc;
    }
    public static OAJfcController createHubNotEmpty(Hub hub, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, comp, HubChangeListener.Type.HubNotEmpty);
        return jc;
    }

    
    // dont include extended checks
    
    public static OAJfcController createOnlyHubNotEmpty(Hub hub, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, null, null, comp, HubChangeListener.Type.HubNotEmpty, false, false);
        return jc;
    }
    public static OAJfcController createOnlyAoNotNull(Hub hub, JComponent comp) {
        OAJfcController jc = new OAJfcController(hub, null, null, comp, HubChangeListener.Type.AoNotNull, false, false);
        return jc;
    }

    
    
}
