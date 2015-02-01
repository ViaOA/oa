// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import com.theice.tsactest2.model.oa.*;
 
public class RCRepoVersionDetailPP {
    private static PackageTypePPx packageType;
    private static PackageVersionPPx packageVersion;
    private static RCRepoVersionPPx rcRepoVersion;
     

    public static PackageTypePPx packageType() {
        if (packageType == null) packageType = new PackageTypePPx(RCRepoVersionDetail.P_PackageType);
        return packageType;
    }

    public static PackageVersionPPx packageVersion() {
        if (packageVersion == null) packageVersion = new PackageVersionPPx(RCRepoVersionDetail.P_PackageVersion);
        return packageVersion;
    }

    public static RCRepoVersionPPx rcRepoVersion() {
        if (rcRepoVersion == null) rcRepoVersion = new RCRepoVersionPPx(RCRepoVersionDetail.P_RCRepoVersion);
        return rcRepoVersion;
    }

    public static String id() {
        String s = RCRepoVersionDetail.P_Id;
        return s;
    }

    public static String packageName() {
        String s = RCRepoVersionDetail.P_PackageName;
        return s;
    }

    public static String buildDate() {
        String s = RCRepoVersionDetail.P_BuildDate;
        return s;
    }

    public static String version() {
        String s = RCRepoVersionDetail.P_Version;
        return s;
    }

    public static String error() {
        String s = RCRepoVersionDetail.P_Error;
        return s;
    }

    public static String invalidMessage() {
        String s = RCRepoVersionDetail.P_InvalidMessage;
        return s;
    }

    public static String selected() {
        String s = RCRepoVersionDetail.P_Selected;
        return s;
    }

    public static String loaded() {
        String s = RCRepoVersionDetail.P_Loaded;
        return s;
    }
}
 
