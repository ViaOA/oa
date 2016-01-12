package com.theicetest.tsac.delegate.oa;

import com.theicetest.tsac.delegate.ModelDelegate;
import com.theicetest.tsac.model.oa.Site;
import com.viaoa.util.OAString;

public class SiteDelegate {
    
    public static Site getSite(String name, boolean bAutoCreate) {
        if (name == null) return null;
        for (Site site : ModelDelegate.getSites()) {
            if (name.equalsIgnoreCase(site.getName())) return site;
        }
        if (!bAutoCreate) return null;
        Site site = new Site();
        site.setName(name);
        ModelDelegate.getSites().add(site);
        
        return site;
    }

    public static Site getSiteFromHostName(String hostName, boolean bAutoCreate) {
        if (hostName == null) return null;
        String name = OAString.field(hostName, "-", 1);
        if (name == null) name = "LOCAL";

        if (hostName.endsWith(".intcx.net")) {  // dns name
            name = OAString.field(hostName, ".", 3);
        }

        for (Site site : ModelDelegate.getSites()) {
            if (name.equalsIgnoreCase(site.getName())) return site;
        }
        if (!bAutoCreate) return null;
        Site site = new Site();
        site.setName(name);
        ModelDelegate.getSites().add(site);
        
        return site;
    }
    
}
