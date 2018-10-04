package com.viaoa.jfc.control;

import javax.swing.JComponent;

import com.viaoa.hub.*;

/**
 * factory methods for OAJfcController. 
 * @author vvia
 *
 */
public class OAJfcControllerFactory {


    
    public static OAJfcController createHubValid(JComponent comp, Hub hub) {
        OAJfcController jc = new OAJfcController(comp, hub, HubChangeListener.Type.HubValid);
        return jc;
    }
    public static OAJfcController createHubValid(JComponent comp, Hub hub, String prop) {
        OAJfcController jc = new OAJfcController(comp, hub, prop, HubChangeListener.Type.HubValid);
        return jc;
    }
    public static OAJfcController createAoNotNull(JComponent comp, Hub hub) {
        OAJfcController jc = new OAJfcController(comp, hub, HubChangeListener.Type.AoNotNull);
        return jc;
    }
    public static OAJfcController createAoNotNull(JComponent comp, Hub hub, String prop) {
        OAJfcController jc = new OAJfcController(comp, hub, prop, HubChangeListener.Type.AoNotNull);
        return jc;
    }
    public static OAJfcController createHubNotEmpty(JComponent comp, Hub hub) {
        OAJfcController jc = new OAJfcController(comp, hub, HubChangeListener.Type.HubNotEmpty);
        return jc;
    }

    public static OAJfcController createOnlyHubNotEmpty(JComponent comp, Hub hub) {
        OAJfcController jc = new OAJfcController(hub, null, null, comp, HubChangeListener.Type.HubNotEmpty, null, null, false);
        return jc;
    }
    public static OAJfcController createOnlyAoNotNull(JComponent comp, Hub hub) {
        OAJfcController jc = new OAJfcController(hub, null, null, comp, HubChangeListener.Type.AoNotNull, null, null, false);
        return jc;
    }

    
    
}
