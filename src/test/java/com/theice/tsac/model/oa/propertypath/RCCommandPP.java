// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class RCCommandPP {
    private static RCExecutePPx rcExecutes;
     

    public static RCExecutePPx rcExecutes() {
        if (rcExecutes == null) rcExecutes = new RCExecutePPx(RCCommand.P_RCExecutes);
        return rcExecutes;
    }

    public static String id() {
        String s = RCCommand.P_Id;
        return s;
    }

    public static String description() {
        String s = RCCommand.P_Description;
        return s;
    }

    public static String commandLine() {
        String s = RCCommand.P_CommandLine;
        return s;
    }

    public static String type() {
        String s = RCCommand.P_Type;
        return s;
    }
}
 
