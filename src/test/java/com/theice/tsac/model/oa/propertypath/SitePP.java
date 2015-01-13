// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class SitePP {
    private static EnvironmentPPx environments;
    private static TimezonePPx timezone;
     

    public static EnvironmentPPx environments() {
        if (environments == null) environments = new EnvironmentPPx(Site.P_Environments);
        return environments;
    }

    public static TimezonePPx timezone() {
        if (timezone == null) timezone = new TimezonePPx(Site.P_Timezone);
        return timezone;
    }

    public static String id() {
        String s = Site.P_Id;
        return s;
    }

    public static String name() {
        String s = Site.P_Name;
        return s;
    }

    public static String production() {
        String s = Site.P_Production;
        return s;
    }
}
 
