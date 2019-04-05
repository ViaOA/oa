// Generated by OABuilder
package test.xice.tsac.model.oa.propertypath;
 
import test.xice.tsac.model.oa.*;
 
public class SiloConfigPP {
    private static ApplicationTypePPx applicationType;
    private static SiloPPx silo;
    private static SiloConfigVersioinPPx siloConfigVersioins;
     

    public static ApplicationTypePPx applicationType() {
        if (applicationType == null) applicationType = new ApplicationTypePPx(SiloConfig.P_ApplicationType);
        return applicationType;
    }

    public static SiloPPx silo() {
        if (silo == null) silo = new SiloPPx(SiloConfig.P_Silo);
        return silo;
    }

    public static SiloConfigVersioinPPx siloConfigVersioins() {
        if (siloConfigVersioins == null) siloConfigVersioins = new SiloConfigVersioinPPx(SiloConfig.P_SiloConfigVersioins);
        return siloConfigVersioins;
    }

    public static String id() {
        String s = SiloConfig.P_Id;
        return s;
    }

    public static String minCount() {
        String s = SiloConfig.P_MinCount;
        return s;
    }

    public static String maxCount() {
        String s = SiloConfig.P_MaxCount;
        return s;
    }
}
 
