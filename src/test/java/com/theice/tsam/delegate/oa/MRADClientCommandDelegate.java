package com.theice.tsam.delegate.oa;

import com.theice.tsam.model.oa.MRADClientCommand;
import com.theice.tsam.model.oa.MRADServerCommand;
import com.theice.tsam.model.oa.*;

public class MRADClientCommandDelegate {
    
    public static String getCommandLine(MRADClientCommand mcc) {
        if (mcc == null) return null;
        MRADServerCommand msc = mcc.getMRADServerCommand();
        if (msc == null) return null;
        String s = CommandDelegate.getCommandLine(mcc.getMRADClient(), msc.getCommand());
        return s;
    }
    
    
}
