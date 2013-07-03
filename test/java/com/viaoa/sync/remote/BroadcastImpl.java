package com.viaoa.sync.remote;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.oa.Company;
import com.viaoa.util.OAString;

public class BroadcastImpl implements BroadcastInterface {
    private Hub<Company> hub;
    int cnt, cnt2;
    public BroadcastImpl(Hub<Company> hub) {
        this.hub = hub;
    }
    
    @Override
    public void displayCompanyName(int cnt) {
        cnt++;
        Company company = hub.getAt(0);
        String s = company.getName();
        System.out.println(cnt+") "+s);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
    
}
