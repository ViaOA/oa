// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class RCStagePP {
    private static RCDeployPPx rcDeploy;
    private static RCExecutePPx rcExecute;
    private static RCStageDetailPPx rcStageDetails;
     

    public static RCDeployPPx rcDeploy() {
        if (rcDeploy == null) rcDeploy = new RCDeployPPx(RCStage.P_RCDeploy);
        return rcDeploy;
    }

    public static RCExecutePPx rcExecute() {
        if (rcExecute == null) rcExecute = new RCExecutePPx(RCStage.P_RCExecute);
        return rcExecute;
    }

    public static RCStageDetailPPx rcStageDetails() {
        if (rcStageDetails == null) rcStageDetails = new RCStageDetailPPx(RCStage.P_RCStageDetails);
        return rcStageDetails;
    }

    public static String id() {
        String s = RCStage.P_Id;
        return s;
    }

    public static String created() {
        String s = RCStage.P_Created;
        return s;
    }

    public static String run() {
        String s = "run";
        return s;
    }

    public static String process() {
        String s = "process";
        return s;
    }

    public static String load() {
        String s = "load";
        return s;
    }
}
 
