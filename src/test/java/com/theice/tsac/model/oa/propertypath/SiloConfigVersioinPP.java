// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class SiloConfigVersioinPP {
    private static PackageTypePPx packageType;
    private static PackageVersionPPx packageVersion;
    private static SiloConfigPPx siloConfig;
     

    public static PackageTypePPx packageType() {
        if (packageType == null) packageType = new PackageTypePPx(SiloConfigVersioin.P_PackageType);
        return packageType;
    }

    public static PackageVersionPPx packageVersion() {
        if (packageVersion == null) packageVersion = new PackageVersionPPx(SiloConfigVersioin.P_PackageVersion);
        return packageVersion;
    }

    public static SiloConfigPPx siloConfig() {
        if (siloConfig == null) siloConfig = new SiloConfigPPx(SiloConfigVersioin.P_SiloConfig);
        return siloConfig;
    }

    public static String id() {
        String s = SiloConfigVersioin.P_Id;
        return s;
    }
}
 
