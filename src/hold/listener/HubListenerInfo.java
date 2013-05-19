package com.viaoa.hub.listener;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class HubListenerInfo {
    
    Hub hub;
    String prop;
    HubListener hl;
    OACalcInfo ci;
    
    HubMerger hm;  // this can be shared, dont close until it is not used anymore
    
    
    HubListenerInfo[] children;
}

