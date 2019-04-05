// Generated by OABuilder
package test.xice.tsac.model.oa.propertypath;
 
import test.xice.tsac.model.oa.*;
 
public class RCDownloadDetailPP {
    private static PackageVersionPPx packageVersion;
    private static RCDeployDetailPPx rcDeployDetails;
    private static RCDownloadPPx rcDownload;
     

    public static PackageVersionPPx packageVersion() {
        if (packageVersion == null) packageVersion = new PackageVersionPPx(RCDownloadDetail.P_PackageVersion);
        return packageVersion;
    }

    public static RCDeployDetailPPx rcDeployDetails() {
        if (rcDeployDetails == null) rcDeployDetails = new RCDeployDetailPPx(RCDownloadDetail.P_RCDeployDetails);
        return rcDeployDetails;
    }

    public static RCDownloadPPx rcDownload() {
        if (rcDownload == null) rcDownload = new RCDownloadPPx(RCDownloadDetail.P_RCDownload);
        return rcDownload;
    }

    public static String id() {
        String s = RCDownloadDetail.P_Id;
        return s;
    }

    public static String selected() {
        String s = RCDownloadDetail.P_Selected;
        return s;
    }

    public static String error() {
        String s = RCDownloadDetail.P_Error;
        return s;
    }

    public static String message() {
        String s = RCDownloadDetail.P_Message;
        return s;
    }

    public static String packageId() {
        String s = RCDownloadDetail.P_PackageId;
        return s;
    }

    public static String packageName() {
        String s = RCDownloadDetail.P_PackageName;
        return s;
    }

    public static String packageFile() {
        String s = RCDownloadDetail.P_PackageFile;
        return s;
    }

    public static String pomFile() {
        String s = RCDownloadDetail.P_PomFile;
        return s;
    }

    public static String version() {
        String s = RCDownloadDetail.P_Version;
        return s;
    }

    public static String totalTime() {
        String s = RCDownloadDetail.P_TotalTime;
        return s;
    }

    public static String invalidMessage() {
        String s = RCDownloadDetail.P_InvalidMessage;
        return s;
    }
}
 
