// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class LocationPageGroupPP {
    private static LocationPPx location;
    private static PageGroupPPx pageGroup;
     

    public static LocationPPx location() {
        if (location == null) location = new LocationPPx(LocationPageGroup.P_Location);
        return location;
    }

    public static PageGroupPPx pageGroup() {
        if (pageGroup == null) pageGroup = new PageGroupPPx(LocationPageGroup.P_PageGroup);
        return pageGroup;
    }

    public static String id() {
        String s = LocationPageGroup.P_Id;
        return s;
    }

    public static String created() {
        String s = LocationPageGroup.P_Created;
        return s;
    }

    public static String seq() {
        String s = LocationPageGroup.P_Seq;
        return s;
    }
}
 
