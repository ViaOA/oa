package test.theice.tsam.delegate.oa;

import test.theice.tsam.delegate.oa.CommandDelegate;

import test.theice.tsam.model.oa.*;

public class MRADClientCommandDelegate {
    
    public static String getCommandLine(MRADClientCommand mcc) {
        if (mcc == null) return null;
        MRADServerCommand msc = mcc.getMRADServerCommand();
        if (msc == null) return null;
        String s = CommandDelegate.getCommandLine(mcc.getMRADClient(), msc.getCommand());
        return s;
    }
    
    
}
