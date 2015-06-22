// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class PackageVersionPP {
    private static ApplicationVersionPPx currentApplicationVersions;
    private static IDLPPx idL;
    private static ApplicationVersionPPx nepApplicationVersions;
    private static PackageTypePPx packageType;
    private static RCInstallDetailPPx rcAfterInstallDetails;
    private static RCInstallDetailPPx rcBeforeInstallDetails;
    private static RCDownloadDetailPPx rcDownloadDetails;
    private static RCInstalledVersionDetailPPx rcInstalledVersionDetails;
    private static RCRepoVersionDetailPPx rcRepoVersionDetails;
    private static RCStageDetailPPx rcStageDetails;
    private static RCVerifyDetailPPx rcVerifyDetails;
    private static SiloConfigVersioinPPx siloConfigVersioins;
     

    public static ApplicationVersionPPx currentApplicationVersions() {
        if (currentApplicationVersions == null) currentApplicationVersions = new ApplicationVersionPPx(PackageVersion.P_CurrentApplicationVersions);
        return currentApplicationVersions;
    }

    public static IDLPPx idL() {
        if (idL == null) idL = new IDLPPx(PackageVersion.P_IDL);
        return idL;
    }

    public static ApplicationVersionPPx nepApplicationVersions() {
        if (nepApplicationVersions == null) nepApplicationVersions = new ApplicationVersionPPx(PackageVersion.P_NepApplicationVersions);
        return nepApplicationVersions;
    }

    public static PackageTypePPx packageType() {
        if (packageType == null) packageType = new PackageTypePPx(PackageVersion.P_PackageType);
        return packageType;
    }

    public static RCInstallDetailPPx rcAfterInstallDetails() {
        if (rcAfterInstallDetails == null) rcAfterInstallDetails = new RCInstallDetailPPx(PackageVersion.P_RCAfterInstallDetails);
        return rcAfterInstallDetails;
    }

    public static RCInstallDetailPPx rcBeforeInstallDetails() {
        if (rcBeforeInstallDetails == null) rcBeforeInstallDetails = new RCInstallDetailPPx(PackageVersion.P_RCBeforeInstallDetails);
        return rcBeforeInstallDetails;
    }

    public static RCDownloadDetailPPx rcDownloadDetails() {
        if (rcDownloadDetails == null) rcDownloadDetails = new RCDownloadDetailPPx(PackageVersion.P_RCDownloadDetails);
        return rcDownloadDetails;
    }

    public static RCInstalledVersionDetailPPx rcInstalledVersionDetails() {
        if (rcInstalledVersionDetails == null) rcInstalledVersionDetails = new RCInstalledVersionDetailPPx(PackageVersion.P_RCInstalledVersionDetails);
        return rcInstalledVersionDetails;
    }

    public static RCRepoVersionDetailPPx rcRepoVersionDetails() {
        if (rcRepoVersionDetails == null) rcRepoVersionDetails = new RCRepoVersionDetailPPx(PackageVersion.P_RCRepoVersionDetails);
        return rcRepoVersionDetails;
    }

    public static RCStageDetailPPx rcStageDetails() {
        if (rcStageDetails == null) rcStageDetails = new RCStageDetailPPx(PackageVersion.P_RCStageDetails);
        return rcStageDetails;
    }

    public static RCVerifyDetailPPx rcVerifyDetails() {
        if (rcVerifyDetails == null) rcVerifyDetails = new RCVerifyDetailPPx(PackageVersion.P_RCVerifyDetails);
        return rcVerifyDetails;
    }

    public static SiloConfigVersioinPPx siloConfigVersioins() {
        if (siloConfigVersioins == null) siloConfigVersioins = new SiloConfigVersioinPPx(PackageVersion.P_SiloConfigVersioins);
        return siloConfigVersioins;
    }

    public static String id() {
        String s = PackageVersion.P_Id;
        return s;
    }

    public static String created() {
        String s = PackageVersion.P_Created;
        return s;
    }

    public static String version() {
        String s = PackageVersion.P_Version;
        return s;
    }

    public static String buildDate() {
        String s = PackageVersion.P_BuildDate;
        return s;
    }

    public static String fileSize() {
        String s = PackageVersion.P_FileSize;
        return s;
    }

    public static String fileName() {
        String s = PackageVersion.P_FileName;
        return s;
    }
}
 
