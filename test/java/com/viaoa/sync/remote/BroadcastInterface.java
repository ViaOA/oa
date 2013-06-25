package com.viaoa.sync.remote;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.oa.Company;
import com.viaoa.sync.model.oa.ServerRoot;

public interface BroadcastInterface {
    void sendCompanyName(String name);
    void start();
    void stop();
}
