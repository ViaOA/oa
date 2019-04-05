package test.xice.tsam.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.Site;

public class SiteDelegate {
    
    public static Site getSite(String abbrevName, boolean bAutoCreate) {
        if (abbrevName == null) return null;
        for (Site site : ModelDelegate.getSites()) {
            if (abbrevName.equalsIgnoreCase(site.getAbbrevName())) return site;
        }
        if (!bAutoCreate) return null;
        Site site = new Site();
        site.setAbbrevName(abbrevName);
        site.setName(abbrevName);
        ModelDelegate.getSites().add(site);
        
        return site;
    }

    public static Site getSiteFromHostName(String hostName, boolean bAutoCreate) {
        if (hostName == null) return null;
        String abbrevName = OAString.field(hostName, "-", 1);
        if (abbrevName == null) abbrevName = "LOCAL";

        if (hostName.endsWith(".intcx.net")) {  // dns name
            abbrevName = OAString.field(hostName, ".", 3);
        }

        return getSite(abbrevName, bAutoCreate);
    }
}
