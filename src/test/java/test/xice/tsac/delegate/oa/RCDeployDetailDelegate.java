package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.model.oa.*;

public class RCDeployDetailDelegate {

    public static boolean hasOtherNewVersion(RCDeployDetail dd) {
        if (dd == null) return false;
        RCDeploy deploy = dd.getRCDeploy();
        if (deploy == null) return false;
        
        ApplicationVersion av = dd.getApplicationVersion();
        if (av == null) return false;
        
        PackageType pt = av.getPackageType();
        if (pt == null) return false;
        
        PackageVersion pv = av.getNewPackageVersion();
        
        for (RCDeployDetail ddx : deploy.getRCDeployDetails()) {
            ApplicationVersion avx = ddx.getApplicationVersion();
            if (avx == null) continue;
            if (avx.getPackageType() != pt) continue;
            PackageVersion pvx = avx.getNewPackageVersion();
            if (pv != pvx) return true;
        }
        return false;
    }

    public static boolean isDownloadNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        ApplicationVersion av = dd.getApplicationVersion();
        if (av == null) return false;
        PackageVersion pv = av.getNewPackageVersion();
        if (pv == null) return false;
        if (dd.getRCDownloadDetail() != null) return false;
        return true;
    }
    public static boolean didDownloadFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCDownloadDetail down = dd.getRCDownloadDetail();
        if (down == null) return false;
        return !OAString.isEmpty(down.getInvalidMessage());
    }
    public static boolean didDownloadSucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCDownloadDetail down = dd.getRCDownloadDetail();
        if (down == null) return false;
        return OAString.isEmpty(down.getInvalidMessage());
    }
    public static boolean isDownloadRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCDownloadDetail down = dd.getRCDownloadDetail();
        if (down != null) return false; // already done
        
        RCDownload rcDownload = dd.getRCDeploy().getRCDownload(); 
        if (rcDownload == null) return false; // not started
        
        if (rcDownload.getRCDownloadDetails().getSize() > 0) return false;
        
        return true;
    }
    

    public static boolean isStageNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        if (!didDownloadSucceed(dd)) return false;
        return dd.getRCStageDetail() == null;
    }
    public static boolean didStageFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStageDetail stage = dd.getRCStageDetail();
        if (stage == null) return false;
        return !OAString.isEmpty(stage.getInvalidMessage());
    }
    public static boolean didStageSucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStageDetail stage = dd.getRCStageDetail();
        if (stage == null) return false;
        return OAString.isEmpty(stage.getInvalidMessage());
    }
    public static boolean isStageRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCStageDetail stage = dd.getRCStageDetail();
        if (stage != null) return false; // already done
        if (!didDownloadSucceed(dd)) return false;
        
        RCStage rcStage = dd.getRCDeploy().getRCStage(); 
        if (rcStage == null) return false; // not started
        
        if (rcStage.getRCStageDetails().getSize() > 0) return false;
        return !isDownloadNext(dd);
    }
    
    
    public static boolean isStopNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        if (!didStageSucceed(dd)) return false;
        return dd.getRCStopDetail() == null;
    }
    public static boolean didStopFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStopDetail stop = dd.getRCStopDetail();
        if (stop == null) return false;
        return !OAString.isEmpty(stop.getInvalidMessage());
    }
    public static boolean didStopSucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStopDetail stop = dd.getRCStopDetail();
        if (stop == null) return false;
        return OAString.isEmpty(stop.getInvalidMessage());
    }
    public static boolean isStopRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCStopDetail stop = dd.getRCStopDetail();
        if (stop != null) return false; // already done
        if (!didStageSucceed(dd)) return false;
        
        RCStop rcStop = dd.getRCDeploy().getRCStop(); 
        if (rcStop == null) return false; // not started
        
        if (rcStop.getRCStopDetails().getSize() > 0) return false;
        return !isStageNext(dd);
    }
    
    
    public static boolean isInstallNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        if (!didStopSucceed(dd)) return false;
        return dd.getRCInstallDetail() == null;
    }
    public static boolean didInstallFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCInstallDetail install = dd.getRCInstallDetail();
        if (install == null) return false;
        return !OAString.isEmpty(install.getInvalidMessage());
    }
    public static boolean didInstallSucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCInstallDetail install = dd.getRCInstallDetail();
        if (install == null) return false;
        return OAString.isEmpty(install.getInvalidMessage());
    }
    public static boolean isInstallRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCInstallDetail install = dd.getRCInstallDetail();
        if (install != null) return false; // already done
        if (!didStopSucceed(dd)) return false;
        
        RCInstall rcInstall = dd.getRCDeploy().getRCInstall(); 
        if (rcInstall == null) return false; // not started
        
        if (rcInstall.getRCInstallDetails().getSize() > 0) return false;
        return !isStopNext(dd);
    }
    

    public static boolean isVerifyNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        if (!didInstallSucceed(dd)) return false;
        return dd.getRCVerifyDetail() == null;
    }
    public static boolean didVerifyFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCVerifyDetail verify = dd.getRCVerifyDetail();
        if (verify == null) return false;
        return !OAString.isEmpty(verify.getInvalidMessage());
    }
    public static boolean didVerifySucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCVerifyDetail verify = dd.getRCVerifyDetail();
        if (verify == null) return false;
        return OAString.isEmpty(verify.getInvalidMessage());
    }
    public static boolean isVerifyRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCVerifyDetail verify = dd.getRCVerifyDetail();
        if (verify != null) return false; // already done
        if (!didInstallSucceed(dd)) return false;
        
        RCVerify rcVerify = dd.getRCDeploy().getRCVerify(); 
        if (rcVerify == null) return false; // not started
        
        if (rcVerify.getRCVerifyDetails().getSize() > 0) return false;
        return !isInstallNext(dd);
    }

    public static boolean isStartNext(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        if (!didVerifySucceed(dd)) return false;
        return dd.getRCStartDetail() == null;
    }
    public static boolean didStartFail(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStartDetail start = dd.getRCStartDetail();
        if (start == null) return false;
        return !OAString.isEmpty(start.getInvalidMessage());
    }
    public static boolean didStartSucceed(RCDeployDetail dd) {
        if (dd == null) return false;
        RCStartDetail start = dd.getRCStartDetail();
        if (start == null) return false;
        return OAString.isEmpty(start.getInvalidMessage());
    }

    public static boolean isStartRunning(RCDeployDetail dd) {
        if (dd == null) return false;
        if (!dd.getSelected()) return false;
        RCStartDetail start = dd.getRCStartDetail();
        if (start != null) return false; // already done
        if (!didVerifySucceed(dd)) return false;
        
        RCStart rcStart = dd.getRCDeploy().getRCStart(); 
        if (rcStart == null) return false; // not started
        
        if (rcStart.getRCStartDetails().getSize() > 0) return false;
        return !isVerifyNext(dd);
    }
    
    public static void clearStages(RCDeployDetail dd) {
        if (dd == null) return;
        RCDeploy rcDeploy = dd.getRCDeploy();
        if (rcDeploy == null) return;

        RCDownloadDetail ddx = dd.getRCDownloadDetail();
        if (ddx != null) {
            if (ddx.getRCDeployDetails().getSize() == 1) ddx.delete();
            else dd.setRCDownloadDetail(null);
        }
        RCDownload rcDownload = rcDeploy.getRCDownload();
        if (rcDownload != null && rcDownload.getRCDownloadDetails().getSize() == 0) {
            rcDownload.delete();
        }
        
        RCStageDetail sd = dd.getRCStageDetail(); 
        if (sd != null) sd.delete();
        RCStage rcStage = rcDeploy.getRCStage();
        if (rcStage != null && rcStage.getRCStageDetails().getSize() == 0) {
            rcStage.delete();
        }
        
        
        RCStopDetail sdx = dd.getRCStopDetail();
        if (sdx != null) sdx.delete();
        RCStop rcStop = rcDeploy.getRCStop();
        if (rcStop != null && rcStop.getRCStopDetails().getSize() == 0) {
            rcStop.delete();
        }
        
        RCInstallDetail id = dd.getRCInstallDetail();
        if (id != null) id.delete();
        RCInstall rcInstall = rcDeploy.getRCInstall();
        if (rcInstall != null && rcInstall.getRCInstallDetails().getSize() == 0) {
            rcInstall.delete();
        }
        
        RCVerifyDetail vd = dd.getRCVerifyDetail();
        if (vd != null) vd.delete();
        
        RCStartDetail sdz = dd.getRCStartDetail();
        if (sdz != null) sdz.delete();
        RCStart rcStart = rcDeploy.getRCStart();
        if (rcStart != null && rcStart.getRCStartDetails().getSize() == 0) {
            rcStart.delete();
        }
    }
}



