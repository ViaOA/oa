package test.xice.tsac.delegate.oa;

import com.viaoa.util.OADateTime;

import test.xice.tsac.model.oa.*;

public class RCInstallDetailDelegate {

    public static String getToolTipText(RCInstallDetail dd) {
        if (dd == null) return null;
        String msg = null;
        msg = addLine(msg, "Package", dd.getPackageId());
        msg = addLine(msg, "Before Version", dd.getBeforeVersion());
        msg = addLine(msg, "After Version", dd.getAfterVersion());
        msg = addLine(msg, "Error", dd.getError());
        msg = addLine(msg, "Process Message", dd.getInvalidMessage());
        return msg;
    }
    
    private static String addLine(String msg, String title, String value) {
        if (value == null) return msg;
        if (msg == null) msg = "";
        else msg = "<br>";
        msg += title+ ": <b>"+ value + "</b>";
        return msg;
    }
    
}
