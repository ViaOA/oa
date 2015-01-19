// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class LocationTypePP {
    private static CompanyPPx company;
    private static LocationPPx locations;
     

    public static CompanyPPx company() {
        if (company == null) company = new CompanyPPx(LocationType.P_Company);
        return company;
    }

    public static LocationPPx locations() {
        if (locations == null) locations = new LocationPPx(LocationType.P_Locations);
        return locations;
    }

    public static String id() {
        String s = LocationType.P_Id;
        return s;
    }

    public static String name() {
        String s = LocationType.P_Name;
        return s;
    }

    public static String seq() {
        String s = LocationType.P_Seq;
        return s;
    }
}
 
