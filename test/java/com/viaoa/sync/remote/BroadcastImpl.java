package com.viaoa.sync.remote;

import com.viaoa.sync.model.oa.Company;
import com.viaoa.util.OAString;

public class BroadcastImpl implements BroadcastInterface {
    private Company company;
    int cnt, cnt2;
    public BroadcastImpl(Company comp) {
        this.company = comp;
    }
    
    @Override
    public void sendCompanyName(String name) {
        cnt++;
        String s = company.getName();
        if (!OAString.isEqual(s, name)) {
            s += "  <<<<<<<";
            cnt2++;
        }
        System.out.println(cnt+"/"+cnt2+") server: "+name+", mine: "+s);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
    
}
