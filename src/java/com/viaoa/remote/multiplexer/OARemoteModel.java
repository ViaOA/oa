package com.viaoa.remote.multiplexer;

import com.viaoa.object.OAObject;


public interface OARemoteModel {
    
    void propertyChange(OAObject oaObj, String propertyName, Object oldObj, Object newObj);
    
    
}
