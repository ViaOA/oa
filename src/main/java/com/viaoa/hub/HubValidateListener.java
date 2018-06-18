package com.viaoa.hub;


/**
 * Validation listener that can be used for beforeXxx events.
 * Throw an exception to have the hub not perform the operation.
 * @author vvia
 *
 */
public class HubValidateListener extends HubListenerAdapter {
    
    @Override
    public void beforeAdd(HubEvent e) {
    }
    @Override
    public void beforeInsert(HubEvent e) {
    }
    @Override
    public void beforeDelete(HubEvent e) {
    }
    @Override
    public void beforeRemove(HubEvent e) {
    }
    @Override
    public void beforeRemoveAll(HubEvent e) {
    }
    @Override
    public void beforePropertyChange(HubEvent e) {
    }
    
    
}
